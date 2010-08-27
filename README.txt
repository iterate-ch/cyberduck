----------------------------------
Building Cyberduck (Mac & Windows)
----------------------------------

Prerequisites
-------------

- Java 1.5.0 SDK in ~/.bin/1.5.0 or edit the jdk.home property in build.xml for a different JDK in /System/Library/Frameworks/JavaVM.framework/Versions
- Java Header Files
	(Missing file or directory: /System/Library/Frameworks/JavaVM.framework/Headers/jni.h)
You need to install the Java Developer Tools from http://connect.apple.com (Free online membership).
- Apache Ant 1.7.1 or later

Mac
- Xcode 3.2 or later (xcodebuild) and the Mac OS X 10.5 SDK on Mac.

Windows
- Microsoft Windows SDK for Windows 7 and .NET Framework 4
	http://www.microsoft.com/downloads/details.aspx?FamilyID=6b6c21d2-2006-4afa-9702-529fa782d63b&displaylang=en
- IKVM installation in c:/workspace/ikvm-0.42.0.3
	http://sourceforge.net/projects/ikvm/files/

Compiling
---------

- Install ant-dotnet-1.0.jar in ~/.ant/lib/*.jar
- Install ant-contrib-1.0b3.jar in ~/.ant/lib/*.jar

- Type 'ant' for executing the default 'build' target. Depending on the platform, build-mac.xml or build-windows.xml is included respectively for native compilation.

Tagged Releases
---------------

Releases are branched in SVN such as 'release-2-6'. Checkout using 'svn co http://svn.cyberduck.ch/tags/release-2-6'.
