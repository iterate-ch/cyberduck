-----------------------------------------
Building Cyberduck (Mac, Windows & Linux)
-----------------------------------------

Prerequisites
-------------

- Oracle Java 1.8.0 SDK or later
- Apache Ant 1.7.1 or later
	http://ant.apache.org/

Mac
- Xcode 7.1 or later (xcodebuild) and the Mac OS X 10.7 SDK on Mac.
	https://developer.apple.com/xcode/download/
	
Windows
- Microsoft Visual Studio 2012
	http://www.microsoft.com/visualstudio/en-us/
- Microsoft Windows SDK for Windows 7 and .NET Framework 4
	http://www.microsoft.com/downloads/details.aspx?FamilyID=6b6c21d2-2006-4afa-9702-529fa782d63b&displaylang=en
- MSBuild Community Tasks
	https://github.com/loresoft/msbuildtasks
- Bonjour SDK for Windows
    https://developer.apple.com/downloads/index.action?q=Bonjour%20SDK%20for%20Windows

Compiling
---------

- Type 'ant' for executing the default 'build' target. Depending on the platform, build-mac.xml or build-windows.xml is included respectively for native compilation.
- On Mac, to create an application with a bundled runtime, execute

    ant -Dbuild.xcodeoptions=SDKROOT=macosx

- To build the CLI, execute

    ant cli

Tagged Releases
---------------

Releases are branched in SVN such as 'release-2-6'. Checkout using 'svn co https://svn.cyberduck.io/tags/release-2-6'.
