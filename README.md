# Cyberduck

[![GitHub commit](https://img.shields.io/github/last-commit/iterate-ch/cyberduck)](https://github.com/iterate-ch/cyberduck/commits/master)
[![GitHub license](https://img.shields.io/badge/license-GPL-blue.svg)](https://raw.githubusercontent.com/iterate-ch/cyberduck/master/LICENSE)
[![Build Status](https://github.com/iterate-ch/cyberduck/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/iterate-ch/cyberduck/actions)
[![Mastodon](https://img.shields.io/mastodon/follow/109698908353278292?domain=https%3A%2F%2Ffosstodon.org%2F&style=flat)](https://fosstodon.org/@cyberduck)
[![Gurubase](https://img.shields.io/badge/Gurubase-Ask%20Cyberduck%20Guru-006BFF)](https://gurubase.io/g/cyberduck)

This is the development home for Cyberduck, a libre file transfer client for macOS and Windows. Command line interface (CLI) for Linux, macOS and Windows. The core libraries are used in [Mountain Duck](https://mountainduck.io/).

<img src="https://cdn.cyberduck.io/img/cyberduck-icon-rect-512.png" alt="Cyberduck Icon" width="400px"/>

## Mailing Lists

There is a [Google Groups Cyberduck](http://groups.google.com/group/cyberduck/) discussion mailing list.

The following additional [mailing lists](http://lists.cyberduck.io/) are hosted:

* [News](http://lists.cyberduck.io/mailman/listinfo/cyberduck-news) Announcements of new releases
* [Localization](http://lists.cyberduck.io/mailman/listinfo/cyberduck-localization) Notifications about changes to the translations needed

## Source

Source code is available licensed under the [GNU General Public License Version 3](https://www.gnu.org/licenses/gpl-3.0.en.html). Pull requests welcome!

## Localizations

Translations to new languages are welcome. We use [Transifex](https://www.transifex.com/cyberduck/cyberduck/dashboard/) to localize resources. Current available localizations are _English_, _Czech_, _Dutch_, _Finnish_, _French_, _German_, _Italian_, _Japanese_, _Korean_, _Norwegian_, _Portuguese_, _Slovak_, _Spanish_, _Chinese (Traditional & Simplified Han)_, _Russian_, _Swedish_, _Hungarian_, _Danish_, _Polish_, _Indonesian_, _Catalan_, _Welsh_, _Thai_, _Turkish_, _Hebrew_, _Latvian_, _Greek_, _Serbian_, _Georgian_ and _Slovenian_.

Make sure to subscribe to the [localization mailing list](http://lists.cyberduck.ch/mailman/listinfo/cyberduck-localization).

## Documentation

For general help about using Cyberduck, please refer to the [documentation](https://docs.cyberduck.io). The documentation is maintained in its own [repository](https://github.com/iterate-ch/docs).

## Additional Connection Profiles

Additional connection profiles not bundled by default but available in _Preferences → Profiles_ are maintained in its own [repository](https://github.com/iterate-ch/profiles).

## Snapshot and Beta builds

These are nightly snapshot builds from the current development trunk featuring the latest bug fixes and enhancements. Be
warned, though, these builds are potentially unstable and experimental. You can also switch to beta or snapshot builds
in _Preferences → Update_.

## Prerequisites

- Java 11 SDK or later
- Apache Ant 1.10.1 or later
- Apache Maven 3.5 or later

### macOS

- [Xcode 12](https://developer.apple.com/xcode/download/) or later

### Windows

#### Installation

**Manually**
- Visual Studio 2022, following workloads are required:
  - `.NET desktop development`
  - `Universal Windows Platform development`
  - `Desktop development with C++`
- [Bonjour SDK for Windows](https://support.apple.com/kb/dl999)
- [Wix v3](https://wixtoolset.org/docs/wix3/) (Optional)

**Chocolatey**

_Without Visual Studio (IDE)_
```sh
choco install visualstudio2022buildtools -y
choco install visualstudio2022-workload-manageddesktopbuildtools -y
choco install visualstudio2022-workload-vctools -y
choco install visualstudio2022-workload-universalbuildtools -y
```

_With Visual Studio IDE_
```sh
choco install visualstudio2022(edition) -y
choco install visualstudio2022-workload-manageddesktop -y
choco install visualstudio2022-workload-nativedesktop -y
choco install visualstudio2022-workload-universal -y
```

Replace `(edition)` with your licensed IDE SKU: community, professional, enterprise

Install required dependencies, after installing Visual Studio IDE or build tools:
```sh
choco install microsoft-openjdk17 ant maven -y
choco install bonjour -y; choco install bonjour -y --force
```

Optional, see Remarks:
```sh
choco install wixtoolset -y
```

_Remarks_: Installing with Chocolatey may or may not fail spectacularly.<br>
Following issues have been observed on a clean installation:
- Bonjour package fails with `file not found` - though the Bonjour64.msi is extracted from BonjourPSSetup.exe.
- wixtoolset depends on .NET 3.5-package, which never completes<br>
  On Windows 11 installation doesn't work
- `visualstudio*-workload-*` may halt with "Operation canceled",<br>
  Abort Chocolatey-command (Ctrl-C), then open up Visual Studio Installer and Resume installation there

Restart your machine after installing these components.

#### System Configuration
Make sure that `MSBuild`, `mvn`, `ant` and `java` are on your `PATH`-environment variable.
* Open `Developer Command Prompt for VS2022`, then run `where msbuild.exe`, add first directory name to path
  * e.g. `C:\Program Files\Microsoft Visual Studio\Community\Msbuild\Current\Bin\amd64`
* Chocolatey may have added mvn and ant to your `PATH`-variable
* The Microsoft OpenJDK 17 installer automatically adds itself to the system `PATH`.

Additionally include the latest Windows Sdk-binary folder in your `PATH`-environment variable:
* `%ProgramFiles(x86)%\Windows Kits\10\bin\10.0.<Latest>.0\x64`

## Building

Run `mvn verify -DskipTests -DskipSign` to build without running any tests and skip codesign. Find build artifacts in

* `osx/target/Cyberduck.app`
* `windows/target/Cyberduck.exe`

Run with `-Pinstaller` to build installer packages with build artifacts

* `osx/target/release/*.(zip|pkg)`
* `windows/target/release/*.(exe|msi)`
* `cli/osx/target/release/*.(pkg|tar.gz)`
* `cli/windows/target/release/*.(exe|msi)`
* `cli/linux/target/release/*.(deb|rpm)`

### Windows

You will run into warnings from `MSBuild`/`WiX` that are unrelated to how Cyberduck is built. You may safely ignore them.

## Debugging

### macOS

Build with `-Pdebug` to allow attaching the remote debugger on port `5005`.

### Windows

Due to Visual Studio not being able to handle Java projects it is required to follow these steps for debugging:

- Run `mvn verify -Dconfiguration=debug` which ensures that debugging symbols are generated
  This prevents Visual Studio (or `MSBuild invoked from Maven`) from generating optimized assemblies which in turn may
  prevent debugging.
- Open the solution in Visual Studio
- Open a `.java` file and set a breakpoint. Visual Studio breaks either on or near the line selected.
- Debugging capabilities include
  - Step Over
  - Step Into
  - Step Out
  - Continue
  - Local/Auto variables
  - Immediate Window
  
  Go To Symbol is not working due to missing Java support.

## Running Tests

After packaging, run `mvn test -DskipITs` to run unit tests but skip integration tests.

### Maven Artifacts (GPL)

#### Repository Configuration

Maven artifacts are available in a repository hosted on Amazon S3.

- Use the following Maven configuration in your project POM to reference artifacts from Cyberduck

 ```xml
<repositories>
    <repository>
        <id>maven.cyberduck.io-release</id>
        <url>https://s3-eu-west-1.amazonaws.com/repo.maven.cyberduck.io/releases</url>
        <layout>default</layout>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

- You will need to add the AWS Maven Wagon to your build using

```xml
<build>
    <extensions>
        <extension>
            <groupId>org.springframework.build</groupId>
            <artifactId>aws-maven</artifactId>
            <version>5.0.0.RELEASE</version>
        </extension>
    </extensions>
</build>
```

#### Artifacts

- Protocol implementations

```xml
<dependency>
    <groupId>ch.cyberduck</groupId>
    <artifactId>protocols</artifactId>
    <type>pom</type>
    <version>7.1.0</version>
</dependency>
```

- Cocoa Java Bindings (macOS)

```xml
<dependency>
    <groupId>ch.cyberduck</groupId>
    <artifactId>binding</artifactId>
    <version>7.1.0</version>
</dependency>
```

- Implementations (macOS) using Launch Services, SystemConfiguration, Foundation, Keychain and other API

```xml
<dependency>
    <groupId>ch.cyberduck</groupId>
    <artifactId>libcore</artifactId>
    <version>${project.version}</version>
</dependency>
```

## Sponsors

[![YourKit](https://www.yourkit.com/images/yk_logo.svg)](https://www.yourkit.com)

YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
and [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/), innovative and intelligent tools for profiling Java and .NET applications.
