---
title: Events, Async and State
type: note
permalink: event-and-state/overview
tags: [eventbus, async, state, persistence]
verified: 2026-08-13
branch: master
coverage: partial
sources:
  - src/main/kotlin/com/runt9/kgdf/event/EventBus.kt
  - src/main/kotlin/com/runt9/kgdf/service/GameStateService.kt
  - src/main/kotlin/com/runt9/kgdf/service/ServiceAsync.kt
  - src/main/kotlin/com/runt9/kgdf/async/AsyncFactory.kt
---

# Events, Async and State

> **Incomplete and permanently WIP.** These notes record what has been investigated, not what exists. Anything not mentioned here is almost certainly "not looked at yet" rather than "not there" or "not a problem". `EventBus`, `GameStateService`, `ServiceAsync` and `AsyncFactory` were read in full. Not covered: `SingleFileSaveStateService` (the actual disk format and write path — only its call sites here were read), `EventHandler`/`Event`/`GameEvents`, and how any consuming project names or uses its own additional threads.

Three coupled concerns: how events are dispatched, which named thread work lands on, and how game state is loaded, mutated and persisted. Read this before writing an event handler, before choosing between `update` and `updateAsync`, and before assuming a `save` actually wrote anything.

## Observations

### EventBus

- [invariant] Dispatch matches the **exact runtime class**: the loop does `eventHandlers[event::class]`. A handler registered for a base type never fires for a subclass #silent-failure.
- [fact] It is single-threaded and FIFO. The bus takes a context from `AsyncFactory.newAsyncContext("Event-Thread")`, and `loop()` receives from one `Channel<Event>` sequentially inside one coroutine.
- [fact] The channel is constructed as `Channel<Event>()`, i.e. **rendezvous** (capacity 0), so `enqueueEvent` suspends until the loop takes the event.
- [trap] Because one loop invokes handlers sequentially and handlers may suspend (`callSuspend`), a slow handler blocks every event behind it. Head-of-line blocking is structural, not incidental #order-dependent.
- [trap] `enqueueEventSync` is **not synchronous**. It is `KtxAsync.launch(asyncContext) { enqueueEvent(event) }` — callable from outside a coroutine, but state is not settled when it returns.
- [trap] `registerHandler` appends to a `MutableList` with **no deduplication**, and `ClassHandlerMapping` caches one handler instance per annotated function, so re-registering the same object appends the *same instance* again. `unregisterHandler` calls `remove(handler)`, which drops only one occurrence — so N registrations against one unregistration leave N-1 live copies and the event fires N-1 times #silent-failure.
- [fact] Handlers are discovered reflectively from `@HandlesEvent`-annotated member functions. A zero-parameter function takes its event type from the annotation; a one-parameter function takes it from the parameter type.
- [fact] `dispose()` closes the channel, clears both handler maps, and disposes the context if it is `Disposable`.

### Threads

- [fact] `AsyncFactory.newAsyncContext(name)` returns `newSingleThreadAsyncContext(name)` — every context it hands out is a single thread. It is `open`, so tests can substitute a deterministic scheduler.
- [fact] `ServiceAsync.serviceContext` is a single thread named `Service-Thread`, exposed as `launchOnServiceThread` (fire and forget) and `onServiceThread` (suspending `withContext`).

### GameStateService

- [trap] `load()` returns `gameState.clone()`, **not** the cached instance. Mutating what `load()` gave you changes nothing until `save()` — except for whatever the consumer's `clone()` leaves shallow, which is visible immediately and permanently.
- [trap] `save(state, forceUpdate = false)` **silently does nothing** when the state is already initialised, `forceUpdate` is false, and `state == cachedState`. Whether a mutation is detected therefore depends entirely on the consumer's `equals` #silent-failure. If a mutable object inside the state has identity-ish equality, mutating it is invisible here and the write is dropped — pass `forceUpdate = true` on any path that mutates such an object.
- [fact] `update(forceUpdate) { }` is **fully synchronous**: it is `load().apply { update(); save(this, forceUpdate) }`, running on the calling thread and returning after the save.
- [fact] `updateAsync(forceUpdate) { }` is the same call wrapped in `launchOnServiceThread`, i.e. fire-and-forget on `Service-Thread`.
- [fact] `save` enqueues the consumer's `updatedEvent(clone)` onto the EventBus before writing to disk.
- [fact] `load()` initialises from `stateService.loadState()` when a save file exists, otherwise builds `initNewState()` and immediately saves it.

## Relations

- see_also [[Interception]]
