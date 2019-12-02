# Cyberduck

[![GitHub license](https://img.shields.io/badge/license-GPL-blue.svg)](https://raw.githubusercontent.com/iterate-ch/cyberduck/master/LICENSE)
[![Build Status](https://travis-ci.org/iterate-ch/cyberduck.svg?branch=master)](https://travis-ci.org/iterate-ch/cyberduck)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/608be33d6e1941858b17984518a4a44b)](https://www.codacy.com/app/dkocher/cyberduck?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=iterate-ch/cyberduck&amp;utm_campaign=Badge_Grade)
[![Twitter](https://img.shields.io/badge/twitter-@cyberduckapp-blue.svg?style=flat)](http://twitter.com/cyberduckapp)

Libre file transfer client for macOS and Windows. Command line interface (CLI) for Linux, macOS and Windows.

## Prerequisites

- Oracle Java 1.8.0 SDK or later
- Apache Ant 1.10.1 or later
- Apache Maven 3.5 or later

### macOS
- [Xcode 9](https://developer.apple.com/xcode/download/) or later

### Windows

- Visual Studio 2017 or later
  - `.NET Desktop development`-Workload
  - Windows SDK (10.0.14393.0)
  - [MSBuild Community Tasks](https://github.com/loresoft/msbuildtasks)
- [Bonjour SDK for Windows](https://developer.apple.com/downloads/index.action?q=Bonjour%20SDK%20for%20Windows)

#### Chocolatey
```
choco install adoptopenjdk8 maven bonjour -y
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

Run `mvn verify -DskipTests -DskipSign` to build without running any tests and skip codesign.

### Windows

You will run into warnings from `MSBuild`/`WiX` that are unrelated to how Cyberduck is built. You may safely ignore them.

## Debugging
### macOS
Edit `setup/app/Info.plist` if you want to debug _Cyberduck.app_ or `setup/pkg/Info.plist` if you want to
 debug`duck` respectively. Add `--agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005` to allow to 
 connect to the running application in your IDE by attaching to the remote JVM.
 
### Windows

Due to Visual Studio not being able to handle Java projects it is required to follow these steps for debugging:

- Run `mvn verify -Dconfiguration=debug` which ensures that debugging symbols are generated
  This prevents Visual Studio (or `MSBuild invoked from Maven) from generating optimized assemblies which in turn may
  prevent debugging.
- Open the solution in Visual Studio
- Open a `.java` file and set a break point. Visual Studio breaks either on or near the line selected.
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
 
         <repositories>
             <repository>
                 <id>maven.cyberduck.io-release</id>
                 <url>http://repo.maven.cyberduck.io.s3.amazonaws.com/releases/</url>
                 <layout>default</layout>
                 <releases>
                     <enabled>true</enabled>
                 </releases>
                 <snapshots>
                     <enabled>false</enabled>
                 </snapshots>
             </repository>
         </repositories>
         
- You will need to add the AWS Maven Wagon to your build using

         <build>
             <extensions>
                 <extension>
                     <groupId>org.springframework.build</groupId>
                     <artifactId>aws-maven</artifactId>
                     <version>5.0.0.RELEASE</version>
                 </extension>
             </extensions>
         </build>

#### Artifacts
- Protocol implementations

        <dependency>
            <groupId>ch.cyberduck</groupId>
            <artifactId>protocols</artifactId>
            <type>pom</type>
            <version>7.1.0</version>
        </dependency>

- Cocoa Java Bindings (macOS)

        <dependency>
            <groupId>ch.cyberduck</groupId>
            <artifactId>binding</artifactId>
            <version>7.1.0</version>
        </dependency>

- Implementations (macOS) using Launch Services, SystemConfiguration, Foundation, Keychain and other API

        <dependency>
            <groupId>ch.cyberduck</groupId>
            <artifactId>libcore</artifactId>
            <version>${project.version}</version>
        </dependency>
