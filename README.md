# Cyberduck

[![GitHub license](https://img.shields.io/badge/license-GPL-blue.svg)](https://raw.githubusercontent.com/iterate-ch/cyberduck/master/LICENSE)
[![Build Status](https://travis-ci.org/iterate-ch/cyberduck.svg?branch=master)](https://travis-ci.org/iterate-ch/cyberduck)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/608be33d6e1941858b17984518a4a44b)](https://www.codacy.com/app/dkocher/cyberduck?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=iterate-ch/cyberduck&amp;utm_campaign=Badge_Grade)
[![Twitter](https://img.shields.io/badge/twitter-@cyberduckapp-blue.svg?style=flat)](http://twitter.com/cyberduckapp)

Libre FTP, SFTP, WebDAV, S3, Azure and OpenStack Swift browser for Mac and Windows. Command line interface (CLI) for Linux, OS X and Windows.

## Prerequisites

- Oracle Java 1.8.0 SDK or later
- Apache Ant 1.10.1 or later
- Apache Maven 3.5 or later
- [Xcode 8](https://developer.apple.com/xcode/download/) or later
- Microsoft Visual Studio 2012 or later
- [Microsoft Windows SDK for Windows 7 and .NET Framework 4](http://www.microsoft.com/downloads/details.aspx?FamilyID=6b6c21d2-2006-4afa-9702-529fa782d63b&displaylang=en)
- [MSBuild Community Tasks](https://github.com/loresoft/msbuildtasks)
- [Bonjour SDK for Windows](https://developer.apple.com/downloads/index.action?q=Bonjour%20SDK%20for%20Windows)

## Building

Run `mvn package -DskipTests` to build without running any tests.

## Running Tests

After packaging, run `mvn test -DskipITs` to run unit tests but skip integration tests.

## Releases

Releases are tagged in GIT/SVN such as `release-2-6`. Checkout using `svn co https://svn.cyberduck.io/tags/release-2-6`.

### Maven Artifacts

Maven artifacts are available in a repository hosted on S3. Use the following Maven configuration to reference artifacts in your project:
 
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

You will need to add the AWS Maven Wagon to your build:

         <build>
             <extensions>
                 <extension>
                     <groupId>org.springframework.build</groupId>
                     <artifactId>aws-maven</artifactId>
                     <version>5.0.0.RELEASE</version>
                 </extension>
             </extensions>
         </build>
