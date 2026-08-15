# Claude Instructions — kgdfw

Kotlin Game Development FrameWork. Hand-written, single-maintainer, consumed by other projects (RogueFlip today). It has **no published documentation, no Stack Overflow answers, and effectively no training data**, so anything not written down here will be guessed at — confidently, with nothing to contradict the guess.

## The knowledge base

`.claude/reference/` is an agent-first knowledge base, tracked in git. **Read `.claude/reference/README.md`
before adding to it** — it is a spec, not a suggestion, and `_validate.py` enforces it.

It is relevance-triggered: check it before reading code on a topic it covers, and expect it to tell you which code to read and what is surprising about it. Current entry points:

- `domains/interception/overview.md` — hooks, contexts, exact-class matching, holders
- `domains/event-and-state/overview.md` — EventBus, the named single-thread contexts, `GameStateService`
- `domains/view-binding/overview.md` — `ViewModel.Binding`, `Updatable`, the binding helpers

Two things bind:

- **Absence from the KB is never evidence about the code.** It means nobody has needed that yet — go read the source.
- **A `[fact]` is something you read in the code, this run.** Not a search result, not a design doc, not memory. See "Read the damn code" in the README; it exists because that rule was broken and produced a note that was confident, well-formatted, and false.

Run `python3 .claude/reference/_validate.py --stale` before finishing any KB work. A clean exit covers errors only — read the warnings.

## Core mechanics — the ones that fail silently

These are the framework's highest-cost surprises, kept here in one line each so an agent knows the hazard exists. **Detail lives in the KB notes above; read them before changing any of this.** Every one of these fails with no compile error, no exception and no log line.

- **`canIntercept` is exact runtime class equality** (`context::class == T::class`). Declare a supertype as the lambda's `ctx:` parameter and the interceptor registers cleanly and never fires. Registration deliberately bypasses the type system with an unchecked cast, so `canIntercept` is the only guard.
- **EventBus dispatch is exact runtime class too** (`eventHandlers[event::class]`). A handler for a base type never fires for a subclass.
- **Handler and interceptor collections do not deduplicate.** Handlers live in a `MutableList`; interceptors in a `MutableSet` of anonymous objects with identity equality. Re-registering the same logical thing adds another copy, and one unregister removes one copy.
- **`enqueueEvent` is not synchronous.** It hands the event to the Event-Thread's queue and returns; dispatch happens later.
- **`GameStateService.save` silently no-ops** when the state compares equal to the cached one and
  `forceUpdate` is false. Whether a mutation is detected therefore depends entirely on the consumer's
  `equals` — a mutable object with identity-ish equality is invisible here and its write is dropped.
- **`GameStateService.load` returns a clone**, not the cached instance. What survives mutation without a save is whatever the consumer's `clone()` left shallow.
- **`update {}` is synchronous; `updateAsync {}` dispatches to Service-Thread.** Do not trust older commit history or memory describing `update {}` as async.
- **`ViewModel.Binding.set` short-circuits on `value == currentValue`** before notifying anything. A binding re-set to an equal value updates nothing — the same consumer-`equals` dependency as `save`.
- **Reading a binding inside an `Updatable` subscribes to it.** `someBinding()` in an updater block both registers and reads; subscription is a side effect of what looks like a getter.

## The split with consuming projects

**This KB owns the mechanism; the consuming project's KB owns the usage.** What `canIntercept` compares belongs here. Which of RogueFlip's effects got it wrong belongs in RogueFlip's KB, which links across rather than restating. Cross-repo references are written as an explicit relative path in prose, never as a
`[[wikilink]]` that cannot resolve in this corpus.

RogueFlip's copy lives at `/mnt/c/Users/runt9/IdeaProjects/rogueflip/.claude/reference/`.

## Working here

- This framework is solely owned and maintained by the same person who owns RogueFlip. It is not off-limits: changes and improvements are welcome, and it is easy to test and deploy.
- Consumers pin kgdfw by JitPack commit hash. A change here is not live in a consumer until that hash is bumped; `mavenLocal` plus the `kgdfVersion` property is the local iteration path.
- Because there is exactly one consumer today, "nothing calls this" is cheap to establish and worth recording when you find it — several APIs here are dead rather than dangerous.
