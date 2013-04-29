----------------------------------
Building Cyberduck (Mac & Windows)
----------------------------------

Prerequisites
-------------

- Java 1.5.0 SDK or later
- Apache Ant 1.7.1 or later
	http://ant.apache.org/
- Copy Ant libraries from <cyberduck-source>/lib/ext to ~/.ant/lib/*.jar

Mac
- Xcode 3.2 or later (xcodebuild) and the Mac OS X 10.5 SDK on Mac.
	http://developer.apple.com/technologies/xcode.html
	
Windows
- Microsoft Visual Studio 10
	http://www.microsoft.com/visualstudio/en-us/
- Microsoft Windows SDK for Windows 7 and .NET Framework 4
	http://www.microsoft.com/downloads/details.aspx?FamilyID=6b6c21d2-2006-4afa-9702-529fa782d63b&displaylang=en
- MSBuild Community Tasks
	http://msbuildtasks.tigris.org/
- Latest IKVM binary release in C:/workspace/
	http://sourceforge.net/projects/ikvm/files/
- Bonjour SDK for Windows
    https://developer.apple.com/downloads/index.action?q=Bonjour%20SDK%20for%20Windows

Compiling
---------

- On Windows, run 'ant dll' initially.
- Type 'ant' for executing the default 'build' target. Depending on the platform, build-mac.xml or build-windows.xml is included respectively for native compilation.
- On Mac, to create an application with a bundled runtime, execute

    ant -Dapp.runtime.properties.key=Runtime -Dbuild.xcodeoptions=SDKROOT=macosx

  or to use the system installation of Java 6, build with

    ant -Dapp.runtime.properties.key=Java -Dbuild.xcodeoptions=SDKROOT=macosx

Tagged Releases
---------------

Releases are branched in SVN such as 'release-2-6'. Checkout using 'svn co http://svn.cyberduck.ch/tags/release-2-6'.
