---
title: Events, Async and State
type: note
permalink: event-and-state/overview
tags: [ eventbus, async, state, persistence ]
verified: 2026-08-14
branch: testability-and-testing-refactor
coverage: partial
sources:
  - src/main/kotlin/com/runt9/kgdf/event/EventBus.kt
  - src/main/kotlin/com/runt9/kgdf/event/EventHandler.kt
  - src/main/kotlin/com/runt9/kgdf/service/GameStateService.kt
  - src/main/kotlin/com/runt9/kgdf/service/GameService.kt
  - src/main/kotlin/com/runt9/kgdf/service/GameServiceRegistry.kt
  - src/main/kotlin/com/runt9/kgdf/service/GameInitializer.kt
  - src/main/kotlin/com/runt9/kgdf/service/ServiceAsync.kt
  - src/main/kotlin/com/runt9/kgdf/async/AsyncFactory.kt
---

# Events, Async and State

> **Incomplete and permanently WIP.** These notes record what has been investigated, not what exists. Anything not mentioned here is almost certainly "not looked at yet" rather than "not there" or "not a problem". `EventBus`, `EventHandler`, `GameStateService`, `GameService`, `GameServiceRegistry`, `GameInitializer`, `ServiceAsync` and `AsyncFactory` were read in full. Not covered: `SingleFileSaveStateService` (the actual disk format and write path — only its call sites here were read), `Event`/`GameEvents`, and how any consuming project names or uses its own additional threads.

Three coupled concerns: how events are dispatched, which named thread work lands on, and how game state is loaded, mutated and persisted. Read this before writing an event handler, before choosing between `update` and `updateAsync`, and before assuming a `save` actually wrote anything.

## Observations

### EventBus

- [invariant] Dispatch matches the **exact runtime class**: the loop does `eventHandlers[event::class]`. A handler registered for a base type never fires for a subclass #silent-failure.
- [fact] It is single-threaded and FIFO. The bus composes an `AsyncWorkQueue<Event>` on an `AsyncFactory` context named `Event-Thread`, which receives from one channel sequentially inside one coroutine and calls `dispatch` per event.
- [fact] The channel is **unbounded**, so `enqueueEvent` is non-suspend and never parks its caller. It was rendezvous (capacity 0) until the queue migration, which is why a `Sync` variant existed to launch onto the bus thread rather than suspend.
- [trap] Because one loop invokes handlers sequentially and handlers may suspend (`callSuspend`), a slow handler blocks every event behind it. Head-of-line blocking is structural, not incidental #order-dependent.
- [question] `dispatch` also runs the handlers *for a single event* one after another, and consumers have worked around the resulting lag by moving logic into interceptors or reading state directly in the View rather than via an event. Running one event's handlers concurrently is a genuinely different proposal from running several events concurrently: it keeps the between-events ordering that consumers depend on, and since the migration to `AsyncWorkQueue` it is contained to one function. Unexplored, and two things would decide it: handlers that hop to the rendering thread would re-serialize there anyway, and concurrent handlers mutating shared state is a hazard the sequential design currently rules out.
- [trap] `enqueueEvent` is **not synchronous**, despite being non-suspend and never parking. It hands the event to the queue and returns; handlers run later on the Event-Thread, so state is not settled when it returns #order-dependent.
- [trap] `registerHandler` appends to a `MutableList` with **no deduplication**, and `ClassHandlerMapping` caches one handler instance per annotated function, so re-registering the same object appends the *same instance* again. `unregisterHandler` calls `remove(handler)`, which drops only one occurrence — so N registrations against one unregistration leave N-1 live copies and the event fires N-1 times #silent-failure.
- [invariant] `EventHandler.handle` is `suspend` and `dispatch` awaits it, so a handler's iteration stays outstanding for the whole of whatever it suspends on — including a hop to another thread and back. Anything observing "is the bus busy" therefore transitively observes work a handler dispatched elsewhere, and needs no separate view of that other queue #order-dependent.
- [trap] The same property is why nothing may block a thread waiting for the bus to quiesce while a handler is suspended waiting for *that* thread: the handler occupies the bus and neither side can proceed.
- [fact] Handlers are discovered reflectively from `@HandlesEvent`-annotated member functions. A zero-parameter function takes its event type from the annotation; a one-parameter function takes it from the parameter type.
- [fact] `dispose()` disposes the queue (closing the channel and its context) and clears both handler maps. Enqueuing afterwards is dropped and logged at DEBUG rather than warned about, because at shutdown it is routine and the queue cannot tell that case from a caller holding a disposed bus.
- [fact] `isIdle` and `awaitIdle()` report whether every enqueued event has finished dispatching. `isIdle` is a plain read and safe on the rendering thread; `awaitIdle()` suspends and must never be called from a handler, which would wait on itself.
- [trap] Dispatch is reflective (`KCallable.call`) and never sets `isAccessible`, so a handler declared on a **private class** cannot be invoked. It compiles, registers without complaint, and throws `IllegalCallableAccessException` when its first event arrives.
- [history] Handler invocation is wrapped in a per-handler `try`/`catch` that logs and continues. Before that, an exception escaping any handler cancelled the loop coroutine and **the bus stopped dispatching for the rest of the process** — which presents as the application quietly freezing rather than as an error, and is the shape of several long-standing "had to restart it" reports #silent-failure.
- [invariant] That `catch` rethrows `CancellationException` before catching `Exception`. Swallowing it would break shutdown, since cancellation is how the loop is meant to stop.
- [fact] The logged throwable is unwrapped from `InvocationTargetException` first, because reflective invocation otherwise buries the real fault under reflection frames.

### GameService lifecycle

- [trap] A `GameService` registers itself from its own `init` block (`registry.register(this)`, with a `@Suppress("LeakingThis")`). It is therefore in the registry simply by being constructed — there is no registration call to grep for, and it is registered before its own constructor has finished.
- [fact] `GameServiceRegistry` is a flat `mutableSetOf<GameService>` with `startAll` / `stopAll` / `tickAll(delta)` / `dispose`, each a bare `forEach`. No ordering guarantee between services, and no error isolation: one service throwing from `start` aborts the rest of the loop.
- [fact] `GameService.start()` registers the service's own event handlers then calls `startInternal()`; `stop()` unregisters them then calls `stopInternal()`. `tick(delta)` and `dispose()` default to no-ops.
- [invariant] `startAll` / `stopAll` reach the registry only through `GameInitializer.initialize()` / `.dispose()`, and nothing calls `tickAll` from inside the framework. **The framework never starts or ticks a service on its own** — a consuming project decides when, and a service whose screen never runs that code silently never starts #silent-failure.

### Threads

- [fact] `AsyncFactory.newAsyncContext(name)` returns `newSingleThreadAsyncContext(name)` — every context it hands out is a single thread. It is `open`, so tests can substitute a deterministic scheduler.
- [fact] `ServiceAsync` owns a single thread named `Service-Thread`, exposed as `launchOnServiceThread` (fire and forget) and `onServiceThread` (suspending `withContext`). It is an injected class taking an [AsyncFactory], not an object, so a test can substitute a scheduler and drain it.
- [decision] **Finding no callers for `ServiceAsync` or `updateAsync` is expected and is not grounds for deleting them.** Turn-based consumers have nothing to put on a background service thread, because everything they do is reachable from the render loop. Real-time consumers are what it exists for. This has been proposed for deletion once on a zero-callers argument; the answer is no.

### GameStateService

- [trap] `load()` returns `gameState.clone()`, **not** the cached instance. Mutating what `load()` gave you changes nothing until `save()` — except for whatever the consumer's `clone()` leaves shallow, which is visible immediately and permanently.
- [trap] `save(state, forceUpdate = false)` **silently does nothing** when the state is already initialised, `forceUpdate` is false, and `state == cachedState`. Whether a mutation is detected therefore depends entirely on the consumer's `equals` #silent-failure. If a mutable object inside the state has identity-ish equality, mutating it is invisible here and the write is dropped — pass `forceUpdate = true` on any path that mutates such an object.
- [fact] `update(forceUpdate) { }` is **fully synchronous**: it is `load().apply { update(); save(this, forceUpdate) }`, running on the calling thread and returning after the save.
- [fact] `updateAsync(forceUpdate) { }` is the same call wrapped in the injected `ServiceAsync.launchOnServiceThread`, i.e. fire-and-forget on `Service-Thread`. Because that context now comes from `AsyncFactory`, a test scheduler drains it like any other.
- [fact] `save` enqueues the consumer's `updatedEvent(clone)` onto the EventBus before writing to disk.
- [fact] `load()` initialises from `stateService.loadState()` when a save file exists, otherwise builds `initNewState()` and immediately saves it.

## Relations

- see_also [[Interception]]
- see_also [[Logging]]
