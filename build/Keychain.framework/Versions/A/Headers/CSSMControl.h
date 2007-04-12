//
//  CSSMControl.h
//  Keychain
//
//  Created by Wade Tregaskis on Sat Mar 15 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Security/Security.h>


/*! @header CSSMControl
    @abstract Contains C functions for controlling the Keychain framework at a most basic & fundamental level.
    @discussion There are numerous settings which you may wish to modify regarding the framework's operation, such as which CSSM modules it uses by default, what settings it initialises the CDSA with, etc.  While many such functions are provided by the CSSMModule class (defined in CSSMModule.h), some should be available without requiring the Cocoa runtime to be configured.  Those that suite such a mode of operation are found in this header. */


/*! @function keychainFrameworkDefaultCSSMVersion
	@abstract Returns the CSSM version the Keychain framework is built for.
	@discussion Returns the lowest CSSM version supported by the Keychain framework.  Future versions may or may not be supported.  You would not normally use this function yourself - it is used internally when initialising the CDSA (unless you override it - see the documentation for cssmInit).

				Note that if the framework does not work because it cannot be initialised (which may result in any or all Keychain functions not working), you may wish to call cssmInit manually to see if this is the problem.  Alternatively (and probably more usefully), simply enable debug messages (see Logging.h for details, but essentially just make sure NDEBUG is <b>not</b> defined).

                Note that this may not be the version ultimately provided to CSSM_Init, as cssmInit allows a 3rd party value to be used.  See the documentation for cssmInit for more information.

                At time of writing the default version is {2, 0}.  Of course, don't hard code for this - check at runtime using this function.
	@result Returns the minimum CSSM version supported by the Keychain framework. */

CSSM_VERSION keychainFrameworkDefaultCSSMVersion(void);

/*! @function keychainFrameworkDefaultPrivilegeScope
    @abstract Returns the default privilege scope used by the Keychain framework.
    @discussion This is the privilege scope provided to CSSM_Init if no other is provided by a custom invocation of cssmInit.

                Note that this may not be the scope ultimately provided to CSSM_Init, as cssmInit allows a 3rd party value to be used.  See the documentation for cssmInit for more information.

                At time of writing the default scope is CSSM_PRIVILEGE_SCOPE_PROCESS.  Of course, don't hard code for this - check at runtime using this function.
    @result Returns the default privilege scope used by the Keychain framework. */

CSSM_PRIVILEGE_SCOPE keychainFrameworkDefaultPrivilegeScope(void);

/*! @function keychainFrameworkDefaultGUID
	@abstract Returns the GUID used by the framework to identify itself to the CDSA.
	@discussion You would not normally need to concern yourself with this, as it is only used internally in most cases.  Note that it is not defined to be constant - that is, it may change between subsequent executions of a client program.  It is also not defined to be the same for two or more concurrently executing programs on the same system.

				To use a different GUID to initialise the CSSM, provide your own custom GUID to cssmInit - refer to the documentation for cssmInit.
	@result Returns the default GUID used by the framework to identify itself to the CDSA. */

const CSSM_GUID* keychainFrameworkDefaultGUID(void);

/*! @function keychainFrameworkDefaultKeyHierarchy
    @abstract Returns the default key hierarchy used by the Keychain framework.
    @discussion This is the key hierarchy provided to CSSM_Init if no other value is provided by a custom invocation of cssmInit.

                At time of writing the default hierarchy is CSSM_KEY_HIERARCHY_NONE.  Of course, don't hard code for this - check at runtime using this function.
    @result Returns the default key hierarchy used by the framework when initialising the CDSA. */

CSSM_KEY_HIERARCHY keychainFrameworkDefaultKeyHierarchy(void);

/*! @function keychainFrameworkDefaultPVCPolicy
    @abstract Returns the default PVC (Pointer Validation Checks) policy used by the Keychain framework.
    @discussion This is the PVC policy provided to CSSM_Init if not other value is provided by a custom invocation of cssmInit.

                At time of writing the default policy is CSSM_PVC_NONE.  Of course, don't hard code for this - check at runtime using this function.
    @result Returns the default PVC policy used by the framework when initialising the CDSA. */

CSSM_PVC_MODE keychainFrameworkDefaultPVCPolicy(void);

/*! @function keychainFrameworkDefaultCredentials
	@abstract Returns the credentials the Keychain framework presents where required by the CDSA.
	@discussion Certain credentials may be required to perform some operations.  The credentials the Keychain automatically presents as required are returned by this function.  At time of writing these default to nothing.  You may change the credentials, as returned by this function, and your changes will be reflected in their future use.
	@result Returns a pointer to the credentials used by the Keychain framework.  You may change these at will, but should be aware of any race conditions that may be present. */

CSSM_ACCESS_CREDENTIALS* keychainFrameworkDefaultCredentials(void);


/*! @function cssmInit
	@abstract Attempts to initialised the CSSM for use by the Keychain framework.
	@discussion Initialisation of the CSSM is required before it may be used in any way.  This function is called as necessary by the Keychain framework automatically - you should never need to call it yourself <i>unless</i> you wish to provide non-default parameters to CSSM_Init.  These may be provided to this function, cssmInit.  If you do call cssmInit yourself, you must make sure to do so before the framework does so - i.e. before you attempt to use any part of it at all.  Somewhere early in main() is a good location; this function is guaranteed not to require an autorelease pool, runloop, or any other Cocoa environment setup; just the generic C environment you'd expect upon entry into main().

                Note that you should always call cssmInit() in preference to calling CSSM_Init directly, as cssmInit() also initialises some relevant aspects of the Keychain framework.  If for some reason you cannot use cssmInit, and believe you must invoke CSSM_Init directly, file a bug report; you may find CSSM_Init is called again by the framework, which may disturb your own initialisation of the CSSM.
    @param customVersion A custom CSSM compatibility version.  The CSSM looks at this to determine if it is compatible with the caller - i.e. if it is 3.0 and the CSSM installed is 2.0, initialisation will fail.  The converse case may or may not fail depending on the backwards compatability of the implementation.

                         If you pass NULL for this parameter, cssmInit will use the Keychain frameworks default value.  You can obtain the default value via the keychainFrameworkDefaultCSSMVersion() function.
    @param customScope A custom scope to use.  You can obtain the default value used by the Keychain framework via the keychainFrameworkDefaultPrivilegeScope() function.  If you do not have a preference for this value, pass whatever keychainFrameworkDefaultPrivilegeScope() returns.
    @param customGUID A custom GUID to use, to identify the calling program to the CSSM.  If NULL a default value will be used, which you can retrieve via the keychainFrameworkDefaultGUID() function.
    @param customHierarchy A custom key hierarchy to use.  If you are unsure as to what value to pass, use the value returned by keychainFrameworkDefaultKeyHierarchy().
    @param customPVCPolicy A custom PVC (Pointer Validation Checks) policy to use.  If you are unsure as to what value to pass, use the value returned by keychainFrameworkDefaultPVCPolicy().
    @param customReserved A custom reserved value to pass to CSSM_Init.  Since the purpose of this presently undefined, the Keychain framework has no notion of what consitutes a sensible 'default' value, so there is no function to obtain any such default.  If you have no other value to pass, pass NULL.
	@result Returns true if initialisation was successful, false otherwise. */

bool cssmInit(const CSSM_VERSION *customVersion, CSSM_PRIVILEGE_SCOPE customScope, const CSSM_GUID *customGUID, CSSM_KEY_HIERARCHY customHierarchy, CSSM_PVC_MODE customPVCPolicy, const void *customReserved);

/*! @function cssmEnd
	@abstract Shuts down the CSSM.
	@discussion This function should [ideally] be called once a program is finished with the CSSM, including at program termination.  It is not required, however, and is not guaranteed to be automatically called by the Keychain framework at any point.  If for whatever reason you require explicit shutdown of the CSSM, call this function manually.

                Note of course that anything that happens to still be using the CSSM after the shutdown will cease to function. */

void cssmEnd(void);


/*! @function setKeychainFrameworkShouldZeroBuffersBeforeFree
    @abstract Tells the Keychain framework whether or not to zero buffers before freeing or otherwise releasing them.
    @discussion This zeroing can be used to add a tiny bit of extra security, potentially, at the cost of performance.

                Note that the default setting for this behaviour is undefined, and always will be.  If you have a specific preference either way, make sure to call this function.  You may call this function at any time - it does not depend on any Cocoa infrastructure.  Somewhere in main(), before you do any Cocoa stuff, is a good place.
    @param shouldZeroBuffers If YES buffers will be zero'd when no longer needed (usually prior to a free() or other deallocation process). */

void setKeychainFrameworkShouldZeroBuffersBeforeFree(bool shouldZeroBuffers);
