# AGENTS.md

This file provides guidance to coding agents (Claude Code, Codex, Gemini CLI, …) when working with code in this
repository.

## What this is

Cyberduck is a libre file transfer client for macOS and Windows, plus a CLI (`duck`) for Linux/macOS/Windows. The same
core libraries power [Mountain Duck](https://mountainduck.io/). The codebase is Java (targeting Java 8 bytecode, built
with JDK 21) organized as a large multi-module Maven reactor. Native desktop UIs are bridged: macOS via Cocoa bindings
(Rococoa/`binding`), Windows via IKVM/.NET (MSBuild invoked from Maven).

## Build & test

Prerequisites: JDK 21, Apache Ant 1.10.1+, Apache Maven 3.5+. macOS also needs Xcode; Windows needs Visual Studio 2022
build tools, the Bonjour SDK, and NuGet credentials for GitHub Package Registry (see README.md).

```bash
mvn verify -DskipTests -DskipSign
```

Common flags and profiles:

- `-DskipTests` skip all tests; `-DskipITs` run unit tests but skip integration tests; `-DskipSign` skip codesigning.
- `-Drevision=0` / `-Drevision=<n>` sets the build number (CI uses `-Drevision=0`).
- `-P no-testcontainers` disables Testcontainers-based tests (CI uses this on Windows/macOS).
- `-Pinstaller` also produces installer packages under `*/target/release/`.
- `-Pdebug` (or `-Dconfiguration=debug`) enables remote debugging on port 5005 (macOS) and debug symbols (Windows).
- `-Psandbox` (macOS) applies app-sandbox entitlements.
- The `osx`, `windows`, `cli/linux`, `cli/osx`, `cli/windows` modules self-activate by OS family, so a plain build only
  produces the platform artifact for the host you build on. `osx` tests are skipped on Windows/Linux and vice versa.

Artifacts: `osx/target/Cyberduck.app`, `windows/target/Cyberduck.exe`, installers under `*/target/release/`.

### Running tests

```bash
mvn test -DskipITs
```

- Unit vs integration: integration tests are annotated `@Category(ch.cyberduck.test.IntegrationTest.class)` (JUnit 4
  categories). The parent POM excludes that category from Surefire by default (`surefire.group.excluded`), so `mvn
  test` runs unit tests only; integration tests run via Failsafe / the nightly `ci.yml` workflow against real servers
  and require credentials (e.g. `VAULT_TOKEN`).
- Run a single test class in one module:
  ```bash
  mvn test -pl ftp -Dtest=FTPWriteFeatureTest
  ```
- Run one method: `-Dtest=FTPWriteFeatureTest#testWrite`.
- To run a test in a downstream protocol module without rebuilding the world, add `-am` (also make dependencies) or
  first `mvn install -DskipTests` once.
- Integration tests for a protocol live in that protocol's module (e.g. `ftp/src/test/java/...`), including
  Cryptomator-vault interoperability variants under `.../core/cryptomator/`.

CI: `build.yml` runs `mvn verify -DskipITs -DskipSign` on macOS/Ubuntu/Windows for every PR; `ci.yml` runs the
integration suite nightly on self-hosted macOS.

## Architecture

### Session / Protocol / Feature

The central abstraction is a **`Session<C>`** (`core/.../Session.java`) — a connection to one remote host, wrapping a
protocol-specific client `C`. Sessions are created reflectively by `SessionFactory` from the `Host`'s `Protocol`
(class name = `<Protocol.getPrefix()>Session`, e.g. `FTPSession`).

Capabilities are **not** methods on `Session`. They are small interfaces in `core/.../features/` (`Read`, `Write`,
`Upload`, `Download`, `Directory`, `Delete`, `Move`, `Copy`, `Find`, `AttributesFinder`, `ListService`, `Versioning`,
`Lock`, `Share`, `AclPermission`, `Quota`, …). Code asks `session.getFeature(Write.class)` and gets back either a
protocol implementation, a shared default, or `null` when unsupported. Feature resolution order:

1. `Session._getFeature(type)` — returns shared `Default*` / `Disabled*` implementations for cross-protocol behavior,
   else delegates to
2. `Protocol.getFeature(type)` (`AbstractProtocol` / the concrete protocol subclass) — protocol-specific wiring.
3. `Session.getFeature(type, feature)` then lets the **`VaultRegistry`** wrap the result, so Cryptomator encryption is
   transparent to callers.

When adding a protocol operation, define/implement the relevant `features/` interface in the protocol module and
register it in that protocol's `*Protocol`/`*Session`; don't add methods to `Session`.

### Modules

- `core` — protocol-agnostic model, features, transfer engine, workers, threading, serialization, preferences.
  `core/dylib`, `core/dll`, `core/native` are the platform-native companions.
- `binding` — Cocoa Java bindings (Rococoa) used by `osx`.
- Protocol modules, one per family: `ftp`, `ssh` (SFTP), `s3`, `webdav`, `azure`, `googledrive`, `googlestorage`,
  `dropbox`, `onedrive`, `box`, `backblaze`, `openstack` (Swift), `nio` (local), `smb`, `irods`, `spectra`,
  `dracoon`, `storegate`, `brick`, `nextcloud`, `owncloud`, `ctera`, `eue`, `deepbox`, `manta`, `tus`, `freenet`, …
  `protocols` is an aggregator POM depending on all of them.
- `profiles` — bundled `*.cyberduckprofile` plist files (connection profiles). `ProtocolFactory.register(Local)`
  loads them via `ProfilePlistReader`. Non-bundled profiles live in the separate `iterate-ch/profiles` repo.
- `oauth`, `jersey` — shared OAuth 2 and JAX-RS/HTTP client plumbing.
- `cryptomator` (+ `cryptomator/legacy`, `cryptomator/dll`) — client-side vault encryption, layered over any protocol
  through `VaultRegistry`.
- `defaults` — `default.properties` (the preference defaults) and `log4j.xml` logging config (`default/` vs
  `debug/`).
- `i18n` — localized `*.strings` files compiled into `*.lproj` bundles. Source strings are managed via Transifex; don't
  hand-edit non-English translations.
- `importer` — bookmark/settings import from other apps.
- Platform front-ends: `osx` (Cocoa app), `windows` (.NET/WPF app via IKVM + MSBuild), `cli` + `cli/{linux,osx,
  windows}` (the `duck` command, built on `args4j`).
- `test` — aggregator that runs the integration suite across protocol modules; also publishes `core`'s `test-jar`
  used as a test dependency elsewhere.

### Factories and dependency wiring

There is no DI container. `Factory<T>` subclasses resolve implementation classes by name from preferences
(`PreferencesFactory.get().getProperty(...)`), with `default.properties` supplying defaults and each platform overriding
entries. `*Factory.get()` is the common accessor pattern. Reflection-by-convention (as in
`SessionFactory`) is also used.

### Transfers

`transfer/` defines `Transfer` (download/upload/copy/sync) and `TransferStatus`. `worker/` contains `Worker`
implementations (`SingleTransferWorker`, `ConcurrentTransferWorker`, `ListWorker`, `DeleteWorker`, `CopyWorker`,
`CreateVaultWorker`, …) — units of work run on the `threading/` background-action framework, decoupled from any UI. UI
controllers (`osx`, `windows`, `cli`'s `TerminalController`) submit workers and receive callbacks (`ProgressListener`,
`TranscriptListener`, `ConnectionCallback`, `LoginCallback`, prompt callbacks).

## Conventions

- Formatting is governed by `.editorconfig` (IntelliJ IDEA style). Java: 4-space indent, 8-space continuation indent,
  spaces not tabs, `if(` / `for(` / `while(` with **no space** before the paren, final newline.
- Every source file starts with the GPLv3 license header (newer files use the "iterate GmbH … version 3 of the License"
  wording; older ones say "version 2 … or (at your option) any later version"). Copy the header from a neighboring file
  in the same module.
- Logging: Log4j 2 via `org.apache.logging.log4j.LogManager.getLogger(<Class>.class)`, parameterized messages
  (`log.debug("... {}", value)`).
- Test classes are `*Test.java` (JUnit 4, `@Test`, `@Category(IntegrationTest.class)` for anything hitting a network
  service). Assertions via `org.junit.Assert.*`.
- Commit messages: short, imperative, capitalized single sentence ending with a period (e.g. "Fix expression.",
  "Refactor to reusable workflow.").
- License / distribution: GPLv3. GPL Maven artifacts are published to an S3-hosted repo (see README.md).
