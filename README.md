# Cyberduck

[![GitHub commit](https://img.shields.io/github/last-commit/iterate-ch/cyberduck)](https://github.com/iterate-ch/cyberduck/commits/master)
[![GitHub license](https://img.shields.io/badge/license-GPL-blue.svg)](https://raw.githubusercontent.com/iterate-ch/cyberduck/master/LICENSE)
[![Build Status](https://github.com/iterate-ch/cyberduck/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/iterate-ch/cyberduck/actions)
[![Twitter](https://img.shields.io/badge/twitter-@cyberduckapp-blue.svg?style=flat)](http://twitter.com/cyberduckapp)

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

## Support & Documentation

For general help about using Cyberduck, please refer to the [documentation](https://docs.cyberduck.io).

## Snapshot and Beta builds

These are nightly snapshot builds from the current development trunk featuring the latest bug fixes and enhancements. Be warned, though, these builds are potentially unstable and experimental. You can also switch to beta or snapshot builds in _Preferences → Update.

## Prerequisites

- Java 11 SDK or later
- Apache Ant 1.10.1 or later
- Apache Maven 3.5 or later

### macOS

- [Xcode 12](https://developer.apple.com/xcode/download/) or later

### Windows

- Visual Studio 2017 or later
  - `.NET Desktop development`-Workload
  - Windows SDK (10.0.14393.0)
  - [MSBuild Community Tasks](https://github.com/loresoft/msbuildtasks)
- [Bonjour SDK for Windows](https://developer.apple.com/downloads/index.action?q=Bonjour%20SDK%20for%20Windows)

#### Chocolatey

```sh
choco install adoptopenjdk15 maven bonjour -y
choco install visualstudio2019buildtools -y
choco install wixtoolset -y
choco install visualstudio2019-workload-manageddesktopbuildtools --params "--add Microsoft.Net.Component.4.7.TargetingPack" -y
choco install visualstudio2019-workload-netcorebuildtools -y
choco install visualstudio2019-workload-vctools --params "--add Microsoft.VisualStudio.Component.Windows10SDK.17763 --add Microsoft.VisualStudio.Component.VC.v141.x86.x64" -y
```

Restart your machine after installing these components.

Additional `%PATH%`:

* `%ProgramFiles(x86)%\Microsoft Visual Studio\2019\BuildTools\MSBuild\Current\Bin`
* `%ProgramFiles(x86)%\Windows Kits\10\bin\10.0.17763.0\x64`

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

Edit `setup/app/Info.plist` if you want to debug _Cyberduck.app_ or `setup/pkg/Info.plist` if you want to
 debug`duck` respectively. Add `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` to allow to
 connect to the running application in your IDE by attaching to the remote JVM.

### Windows

Due to Visual Studio not being able to handle Java projects it is required to follow these steps for debugging:

- Run `mvn verify -Dconfiguration=debug` which ensures that debugging symbols are generated
  This prevents Visual Studio (or `MSBuild invoked from Maven) from generating optimized assemblies which in turn may
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
