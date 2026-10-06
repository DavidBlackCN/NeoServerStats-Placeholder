# AGENTS.md — NeoServerStats Placeholder

## Mission

You are working on **NeoServerStats Placeholder**, a Minecraft 1.21.1 NeoForge server-side mod.

Your job is to implement a clean, lightweight set of server and player statistic placeholders for **Forge PlaceholderAPI 2.1.0 (NeoForge 1.21.1)**.

Read `PLAN.md` before making implementation decisions.

---

## Hard Constraints

### Platform

Use only:

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21
- Forge PlaceholderAPI 2.1.0 for NeoForge 1.21.1

Do not migrate loaders or Minecraft versions.

### Project Type

This is a standard NeoForge `.jar` mod.

It is NOT:

- a Bukkit plugin
- a Paper plugin
- a Fabric mod
- a datapack
- a KubeJS script
- an Arclight plugin
- a standalone ForgePlaceholderAPI script pack

The project should be distributable as a normal NeoForge mod JAR placed in `mods/`.

### Runtime Scope

Target dedicated servers.

Do not introduce client-only classes or features.

---

## Start-Up Procedure

Before coding:

1. Read all of `PLAN.md`.
2. Inspect the repository and current Gradle/NeoForge template.
3. Run or inspect the build setup.
4. Identify the exact NeoForge 1.21.1 version currently configured.
5. Locate the exact Forge PlaceholderAPI 2.1.0 dependency artifact/source.
6. Inspect its actual API before writing the integration layer.

Do not assume Forge PlaceholderAPI 2.1.0 uses the same classes or registration mechanism as old 1.12.2/1.16.5 examples.

If external source/JAR inspection is available, use it.

If the API is ambiguous, inspect bytecode/source/Javadocs/dependency classes rather than guessing.

---

## Required Features

Implement the required placeholder semantics from `PLAN.md`.

Server:

- server_tps
- server_mspt
- server_cpu_process
- server_cpu_system
- server_online
- server_max_players
- server_players_list
- server_uptime
- server_motd
- server_version
- server_memory_used
- server_memory_committed
- server_memory_max
- server_memory_percent

Player:

- player_name
- player_uuid
- player_ping
- player_dimension
- player_x
- player_y
- player_z
- player_deaths
- player_playtime
- player_playtime_ticks
- player_playtime_seconds
- player_playtime_hours
- player_session_time

The actual textual placeholder syntax must follow the verified Forge PlaceholderAPI 2.1.0 API.

---

## Implementation Rules

### Vanilla Statistics

For player deaths and cumulative playtime:

Use vanilla Minecraft statistics.

Do NOT:

- create JSON save files
- create SQLite
- maintain duplicate counters
- reset vanilla data
- modify player statistics

The goal is to expose existing vanilla values.

### Session Time

Current-session playtime is separate from vanilla cumulative playtime.

Use an in-memory UUID -> login/session start map.

Requirements:

- initialize on player login
- remove on logout
- clear on server stop
- handle unusual disconnects safely
- no persistence

### TPS / MSPT

Prefer stable Minecraft/NeoForge timing data when available.

If no suitable public/stable value exists, implement a lightweight rolling measurement using server tick events.

Rules:

- no expensive calculation during placeholder parsing
- cache the current result
- keep a rolling average
- TPS display should normally be bounded to 20.00
- MSPT and TPS must be internally coherent

Avoid mixins unless absolutely necessary.

### CPU

Use Java 21 management APIs where practical.

Provide:

- JVM process CPU %
- system/host CPU %

Rules:

- cache values
- sample around once per second
- do not run OS shell commands
- do not parse `/proc` unless management APIs prove inadequate and the fallback is clearly isolated
- do not add a heavy third-party monitoring library without a strong reason
- unsupported metric => safe fallback such as `N/A`

### Memory

Prefer `MemoryMXBean` / heap `MemoryUsage`.

Do not label `Runtime.freeMemory()` as total free system memory.

Expose JVM heap semantics clearly.

### Placeholder Resolution

Placeholder handlers must be cheap.

Do not:

- block
- sleep
- perform network I/O
- read files
- write files
- allocate large objects
- run commands

Normal flow should be:

`context -> current/cached metric -> formatter -> string`

---

## Player Context

Some placeholder consumers may request a placeholder without a player.

All player placeholders must use one centralized missing-context policy.

Recommended default:

`N/A`

Do not throw `NullPointerException`.

Do not select an arbitrary online player.

Do not use the first player in the player list as a fallback.

---

## Threading

Treat Minecraft server/player state as server-thread state unless the API explicitly guarantees otherwise.

Investigate whether Forge PlaceholderAPI resolves placeholders synchronously on the server thread.

If resolution can occur asynchronously:

- use cached global snapshots
- avoid unsafe server/player calls
- create a safe design rather than suppressing exceptions

Do not fix threading problems with random `synchronized` blocks.

---

## Code Quality

Prefer:

- small focused classes
- immutable snapshots where useful
- explicit naming
- constructor injection or simple owned services over global mutable state
- one place for formatting rules
- one place for placeholder registration

Avoid:

- giant main mod class
- reflection unless integration requires it
- duplicated formatting logic
- magic numbers
- unnecessary abstraction frameworks
- Lombok unless already present
- Kotlin unless project already uses it
- mixins for ordinary data access

Add comments for:

- unusual Minecraft API behavior
- Forge PlaceholderAPI compatibility assumptions
- CPU semantics
- TPS/MSPT measurement details

Do not comment obvious Java syntax.

---

## Logging

Use the project's normal logger.

Log useful lifecycle events, for example:

- mod initialization
- placeholder registration count
- unsupported CPU metric once

Do not log every placeholder resolution.

Do not spam tick logs.

Debug logging must be removable or gated.

---

## Dependency Discipline

Keep dependencies minimal.

Required:

- NeoForge
- Forge PlaceholderAPI

Avoid adding anything else unless clearly justified.

If Forge PlaceholderAPI dependency coordinates are not available in a Maven repository:

1. investigate its official distribution/build setup
2. use a clean local/development dependency approach if necessary
3. document it
4. do not silently commit proprietary/random downloaded binaries unless repository policy allows it

Do not shade Forge PlaceholderAPI into this mod unless explicitly required and license-safe.

---

## NeoForge Metadata

Use:

- display name: `NeoServerStats Placeholder`
- mod id: `neoserverstats_placeholder`

Set correct Minecraft and NeoForge dependency ranges for the actual 1.21.1 project.

Forge PlaceholderAPI should be declared required if feasible with its actual mod id.

Do not claim client support that has not been tested.

---

## Build / Validation Commands

Use the project wrapper.

Windows:

```powershell
.\gradlew.bat clean build
.\gradlew.bat runServer
```

Unix-like:

```bash
./gradlew clean build
./gradlew runServer
```

Do not rely on a globally installed Gradle.

---

## Change Discipline

Before editing:

- inspect existing code
- preserve working build configuration
- make the smallest coherent change for the current phase

After meaningful changes:

1. compile/build
2. fix errors immediately
3. run focused validation
4. only then continue to the next phase

Do not accumulate a large pile of uncompiled changes.

---

## Suggested Execution Order

1. Template/build validation
2. Project metadata cleanup
3. Forge PlaceholderAPI 2.1.0 dependency/API inspection
4. One working proof-of-concept placeholder
5. Simple server placeholders
6. Memory metrics
7. Player context and identity/location placeholders
8. Vanilla deaths/playtime
9. Session tracker
10. TPS/MSPT
11. CPU
12. Dedicated-server validation
13. README
14. Final clean build

Do not start with TPS/CPU before the placeholder integration itself is proven.

---

## Acceptance Checklist

Before considering the work complete, verify:

- [ ] Java 21
- [ ] Minecraft 1.21.1
- [ ] NeoForge 21.1.x
- [ ] dedicated server boots
- [ ] Forge PlaceholderAPI loads
- [ ] NeoServerStats Placeholder loads
- [ ] real placeholder registration works
- [ ] all server placeholders resolve
- [ ] all player placeholders resolve with player context
- [ ] missing player context is safe
- [ ] deaths use vanilla stats
- [ ] cumulative playtime uses vanilla stats
- [ ] session time resets after reconnect/restart as intended
- [ ] TPS/MSPT values are plausible
- [ ] CPU values are cached
- [ ] memory semantics are correct
- [ ] no client-only crash
- [ ] no database
- [ ] no HTTP/network dependency
- [ ] no Bukkit/Paper dependency
- [ ] no log spam
- [ ] `clean build` passes
- [ ] README lists exact implemented placeholder syntax

---

## When Something Is Unclear

Do not fabricate an API.

Use this priority:

1. existing repository code
2. exact dependency source/JAR
3. official NeoForge 1.21.1 documentation
4. Forge PlaceholderAPI's actual source/examples
5. minimal experiment in the dev environment

If a requirement cannot be implemented cleanly with the available API, document the blocker and implement the safest partial behavior rather than introducing a fragile hack.

---

## Final Output Expectation

Leave the repository in a release-ready state with:

- source code
- valid NeoForge metadata
- dependency configuration
- README
- PLAN.md
- AGENTS.md
- successful Gradle build
- no temporary debug/test artifacts committed

Do not stop after writing a design or stub. The target is a working NeoForge 1.21.1 server mod.
