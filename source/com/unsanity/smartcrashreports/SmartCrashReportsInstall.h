/*!
    @header		SmartCrashReportsInstall
    @abstract   API to the Installer of Smart Crash Reports
    @discussion This API allows you to painlessly install Smart Crash Reports from within your application. It will properly
				detect the versions and whether it should upgrade/install over an older version on the user's computer.
				
				In order to use this library, you have to add the following frameworks to your project:
				<ul>
					<li>/System/Library/Frameworks/Carbon.framework
					<li>/System/Library/Frameworks/CoreFoundation.framework
					<li>/System/Library/Frameworks/Security.framework
					<li>/usr/lib/libz.1.2.3.dylib (or /usr/lib/libz.dylib).
				</ul>
*/

#pragma once
#include <CoreFoundation/CoreFoundation.h>

#ifdef __cplusplus
	extern "C" {
#endif

/*!
    @enum		UnsanitySCR_Install_ErrorCode
    @abstract   Result codes returned from the UnsanitySCR_Install() function
    @discussion These error codes will be returned from the UnsanitySCR_Install() function.
    @constant   kUnsanitySCR_Install_NoError			No error. Installation succeeded.
    @constant   kUnsanitySCR_Install_OutOfMemory		Could not allocate needed memory for the installation.
    @constant   kUnsanitySCR_Install_InstalledGlobally	Smart Crash Reports are already installed globally and this API can not install into /Library
														as it would require additional fiddling with the authorization which is beyond the scope of this API.
	
    @constant   kUnsanitySCR_Install_NoPermissions		Could not install because of file permission issues.
	@constant	kUnsanitySCR_Install_AuthFailure		Could not install because user failed to authenticate for the global install.
    @constant   kUnsanitySCR_Install_WillNotInstall		Could not install because a newer or same version of Smart Crash Reports is already installed.
*/

enum
{
	kUnsanitySCR_Install_NoError			= 0,
	kUnsanitySCR_Install_OutOfMemory		= -108,
	kUnsanitySCR_Install_InstalledGlobally	= -13,
	kUnsanitySCR_Install_NoPermissions		= -54,
	kUnsanitySCR_Install_WillNotInstall		= -14,
	kUnsanitySCR_Install_AuthFailure		= -111
} UnsanitySCR_Install_ErrorCode;

/*!
    @function	UnsanitySCR_InstalledVersion
    @abstract   Get the version of Smart Crash Reports installed on user's system.
    @discussion Returns the version of Smart Crash Reports installed on user's system.
	@param		outOptionalIsInstalledGlobally	If this is not NULL, the Boolean the param points to will hold TRUE if Smart Crash Reports are installed for all
												users, or FALSE if they are installed for the current user only.
    @result     UInt32 containing the version of Smart Crash Reports (in CFBundleGetVersionNumber() format), or 0 if Smart Crash Reports
				are not installed.
*/
UInt32			UnsanitySCR_InstalledVersion(Boolean* outOptionalIsInstalledGlobally);

/*!
    @function	UnsanitySCR_InstallableVersion
    @abstract   Get the version of Smart Crash Reports that this library can install.
    @discussion Returns the version of Smart Crash Reports that this library can install.
    @result     UInt32 containing the version of Smart Crash Reports (in CFBundleGetVersionNumber() format) bundled in this library.
*/
UInt32			UnsanitySCR_InstallableVersion();

/*!
    @function	UnsanitySCR_CanInstall
    @abstract   Check whether this library can install Smart Crash Reports on user's system.
    @discussion Returns whether Smart Crash Reports can be installed.
	@param		outOptionalAuthenticationWillBeRequired		If this is not NULL, the Boolean the param points to will be set to TRUE if an authentication will be
															required to install Smart Crash Reports.
    @result     TRUE if Smart Crash Reports can be installed, or FALSE if they are already installed (of the same or newer version).
*/
Boolean			UnsanitySCR_CanInstall(Boolean* outOptionalAuthenticationWillBeRequired);

/*!
    @function	UnsanitySCR_Install
    @abstract   Attempt to install Smart Crash Reports on user's system.
    @discussion Installs Smart Crash Reports from the library's archive, if possible.
	@param		inInstallGlobally		If TRUE, a global installation will be attempted (provided the user can authenticate). If FALSE, Smart Crash Reports will
										be installed for current user only.
    @result     One of the <code>UnsanitySCR_Install_ErrorCode</code> result codes.
*/
OSStatus		UnsanitySCR_Install(Boolean inInstallGlobally);

#ifdef __cplusplus
	}
#endif