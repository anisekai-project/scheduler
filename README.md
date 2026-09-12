# Scheduler

Scheduler is a Java library for planning media events and coordinating persistent background tasks. The two APIs share
the same goal: turn scheduling decisions into explicit operations that an application can apply to its own persistence
layer.

## Features

- Schedule episode-aware watch sessions without overlaps.
- Calculate runtimes with optional opening and ending skip time.
- Merge adjacent compatible sessions and recalibrate schedules after viewing progress changes.
- Move selected events earlier or later while checking for conflicts.
- Queue prioritized tasks through typed server and client factories.
- Poll only tasks supported by a client and resolve successful or failed executions.
- Return persistence-neutral `ActionPlan` objects containing create, update, and delete actions.

## Requirements

- Java 25
- Gradle 9.2 (the Gradle wrapper is included)

## Build and test

```shell
./gradlew build
```

## Design overview

### Event scheduling

Implement `WatchTarget` for the media being watched and `Planifiable` for your persisted schedule entity. Construct an
`EventScheduler` with the current persisted state and an ID extractor, then use `canSchedule`, `schedule`, `delay`, or
`calibrate` to produce an `ActionPlan`.

The `delay` operation accepts a positive selection interval and a non-zero shift. A negative shift intentionally moves
matching events earlier.

### Task orchestration

Task factories define stable names plus codecs for their input and output. A server orchestrator queues and assigns
persistent tasks; a client orchestrator executes only factories it supports and reports the result. Unsupported factory
names are intentionally not delivered to a client.

### Applying action plans

Scheduler does not own database transactions. Callers apply the returned create, update, and delete actions in their
persistence layer, ideally in one transaction. This keeps the scheduling rules independent from any database or ORM.

## Contributing

Bug reports and focused pull requests are welcome. Please open an issue before a substantial change so the behavior and
persistence contract can be agreed first, and include tests for changed scheduling behavior.

## License

Licensed under the [Apache License 2.0](LICENSE).
