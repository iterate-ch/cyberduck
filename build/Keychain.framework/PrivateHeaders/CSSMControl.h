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

//#import <Foundation/Foundation.h>
#import <Security/Security.h>


/*! @function keychainFrameworkCSSMVersion
	@abstract Returns the CSSM version the Keychain framework is built for.
	@discussion Returns the lowest CSSM version supported by the Keychain framework.  Future versions may or may not be supported.  You would not normally use this function yourself - it is used internally when initialising the CSSM.

				Note that if the framework does not work because it cannot be initialised (which may result in any or all Keychain functions not working), you may wish to call cssmInit manually to see if this is the problem.  Alternatively (and probably more usefully), simply enable debug messages (see Logging.h for details, but essentially just make sure NDEBUG is <b>not</b> defined).
	@result Returns the minimum CSSM version supported by the Keychain framework. */

CSSM_VERSION keychainFrameworkCSSMVersion(void);

/*! @function keychainFrameworkGUID
	@abstract Returns the GUID used by the framework to identify itself to the CDSA.
	@discussion You would not normally need to concern yourself with this, as it is only used internally in most cases.  Note that it is not defined to be constant - that is, it may change between subsequent executions of a client program.  It is also not defined to be the same for two or more concurrently executing programs on the same system.

				Note that this is not defined until the framework has initialised itself, which implicitly occurs whenever you use any of it's cryptographic functions, or when you explicitly call one of the init functions (e.g. cssmInit).

				The Keychain framework's GUID cannot be changed at will, unlike the CSP/TP/CL GUIDs.
	@result Returns the GUID used by the framework to identify itself to the CDSA. */

CSSM_GUID keychainFrameworkGuid(void);

/*! @function keychainFrameworkCSPGUID
	@abstract Returns the GUID of the CSP (Cryptographic Module) being used by the Keychain framework.
	@discussion Individual modules within the CDSA have different GUID's to identify them.  This function returns the one currently being used by the framework for Cryptographic operations.  By default, this is gGuidAppleCSP, although you should avoid relying on this default as it may change in future (it is dependant on what Apple provides in their CDMA implementation).  You may change this using setKeychainFrameworkCSPGUID.  If you do change the GUID, keep in mind that the value returned by this function is only guaranteed to represent that used by any future Keychain cryptographic operations (or at least until another call to setKeychainFrameworkCSPGUID) - any operations already underway will continue using the old value.

                Note that this is not defined until the framework has initialised itself, which implicitly occurs whenever you use any of it's cryptographic functions, or when you explicitly call one of the init functions (e.g. cssmInit).
    @result Returns the GUID of the CSP module being used by the Keychain framework. */

CSSM_GUID keychainFrameworkCSPGUID(void);

/*! @function setKeychainFrameworkCSPGUID
	@abstract Chooses an alternative CSP for the Keychain framework to use.
	@discussion If you wish to use an explicit CSP, rather than the default the Keychain chooses, use this function.  There is currently no mechanism in the Keychain framework itself to automatically discover available CSPs (although expect this functionality in future).

				Note that you should not try to change the CSP GUID while any operation requiring the original GUID is still running.  In the particular case of the CSP, you may not be able to use any Key's or instances of similar classes after you change the GUID - they will most likely return errors for any attempt to use them.  Thus, try to change the GUID, if you must do so at all, at your program's load time, before it uses the Keychain framework.
	@param newGUID The new GUID to use.
	@result Returns true if the operation was successful, false otherwise (in which case the Keychain is left in an undefined state - you should call this function again with a valid GUID, such that the function returns true, before trying to use CSP-related functionality). */

bool setKeychainFrameworkCSPGUID(CSSM_GUID newGUID);

/*! @function keychainFrameworkTPGUID
	@abstract Returns the GUID of the TP (Trust Policy Module) being used by the Keychain framework.
	@discussion Individual modules within the CDSA have different GUID's to identify them.  This function returns the one currently being used by the framework for Trust Policy operations.  By default, this is gGuidAppleX509TP, although you should avoid relying on this default as it may change in future (it is dependant on what Apple provides in their CDMA implementation).  You may change this using setKeychainFrameworkTPGUID.  If you do change the GUID, keep in mind that the value returned by this function is only guaranteed to represent that used by any future Keychain Trust operations (or at least until another call to setKeychainFrameworkTPGUID) - any operations already underway will continue using the old value.

                Note that this is not defined until the framework has initialised itself, which implicitly occurs whenever you use any of it's cryptographic functions, or when you explicitly call one of the init functions (e.g. cssmInit).
    @result Returns the GUID of the TP module being used by the Keychain framework. */

CSSM_GUID keychainFrameworkTPGUID(void);

/*! @function setKeychainFrameworkTPGUID
	@abstract Chooses an alternative TP for the Keychain framework to use.
	@discussion If you wish to use an explicit TP, rather than the default the Keychain chooses, use this function.  There is currently no mechanism in the Keychain framework itself to automatically discover available TPs (although expect this functionality in future).

				Note that you should not try to change the TP GUID while any operation requiring the original GUID is still running.  In the particular case of the TP, you may not be able to use any Trust's or instances of similar classes after you change the GUID - they will most likely return errors for any attempt to use them.  Thus, try to change the GUID, if you must do so at all, at your program's load time, before it uses the Keychain framework.
	@param newGUID The new GUID to use.
	@result Returns true if the operation was successful, false otherwise (in which case the Keychain is left in an undefined state - you should call this function again with a valid GUID, such that the function returns true, before trying to use TP-related functionality). */

bool setKeychainFrameworkTPGUID(CSSM_GUID newGUID);

/*! @function keychainFrameworkCLGUID
	@abstract Returns the GUID of the CL (Certificate Module) being used by the Keychain framework.
	@discussion Individual modules within the CDSA have different GUID's to identify them.  This function returns the one currently being used by the framework for Certificate operations.  By default, this is gGuidAppleX509CL, although you should avoid relying on this default as it may change in future (it is dependant on what Apple provides in their CDMA implementation).  You may change this using setKeychainFrameworkCLGUID.  If you do change the GUID, keep in mind that the value returned by this function is only guaranteed to represent that used by any future Keychain certificate operations (or at least until another call to setKeychainFrameworkCLGUID) - any operations already underway will continue using the old value.

                Note that this is not defined until the framework has initialised itself, which implicitly occurs whenever you use any of it's cryptographic functions, or when you explicitly call one of the init functions (e.g. cssmInit).
	@result Returns the GUID of the CL module being used by the Keychain framework. */

CSSM_GUID keychainFrameworkCLGUID(void);

/*! @function setKeychainFrameworkCLGUID
	@abstract Chooses an alternative CL for the Keychain framework to use.
	@discussion If you wish to use an explicit CL, rather than the default the Keychain chooses, use this function.  There is currently no mechanism in the Keychain framework itself to automatically discover available CLs (although expect this functionality in future).

				Note that you should not try to change the CL GUID while any operation requiring the original GUID is still running.  In the particular case of the CL, you may not be able to use any Certificate's or instances of similar classes after you change the GUID - they will most likely return errors for any attempt to use them.  Thus, try to change the GUID, if you must do so at all, at your program's load time, before it uses the Keychain framework.
	@param newGUID The new GUID to use.
	@result Returns true if the operation was successful, false otherwise (in which case the Keychain is left in an undefined state - you should call this function again with a valid GUID, such that the function returns true, before trying to use CL-related functionality). */

bool setKeychainFrameworkCLGUID(CSSM_GUID newGUID);

/*! @function keychainFrameworkCSPHandle
	@abstract Returns the CSP handle presently being used by the Keychain framework.
	@discussion If you wish to use any of the CDMA functions yourself, alongside those of the Keychain framework, you should generally use the same CSP to ensure compatibility.

				You cannot change the CSP handle itself, although you can change the CSP the Keychain uses via the setKeychainFrameworkCSPGUID function.  Note that if you do change the GUID, any existing handles may become invalid, and should not be used.
	@result Returns the CSP handle presently being used by the Keychain framework, or NULL if the desired CSP is not available. */

CSSM_CSP_HANDLE keychainFrameworkCSPHandle(void);

/*! @function keychainFrameworkTPHandle
	@abstract Returns the TP handle presently being used by the Keychain framework.
	@discussion If you wish to use any of the CDMA functions yourself, alongside those of the Keychain framework, you should generally use the same TP to ensure compatibility.

				You cannot change the TP handle itself, although you can change the TP the Keychain uses via the setKeychainFrameworkTPGUID function.  Note that if you do change the GUID, any existing handles may become invalid, and should not be used.
	@result Returns the TP handle presently being used by the Keychain framework, or NULL if the desired TP is not available. */

CSSM_TP_HANDLE keychainFrameworkTPHandle(void);

/*! @function keychainFrameworkCLHandle
	@abstract Returns the CL handle presently being used by the Keychain framework.
	@discussion If you wish to use any of the CDMA functions yourself, alongside those of the Keychain framework, you should generally use the same CL to ensure compatibility.

				You cannot change the CL handle itself, although you can change the CL the Keychain uses via the setKeychainFrameworkCLGUID function.  Note that if you do change the GUID, any existing handles may become invalid, and should not be used.
	@result Returns the CL handle presently being used by the Keychain framework, or NULL if the desired CL is not available. */

CSSM_CL_HANDLE keychainFrameworkCLHandle(void);

/*! @function keychainFrameworkCredentials
	@abstract Returns the credentials the Keychain framework presents where required by the CDSA.
	@discussion Certain credentials may be required to perform some operations.  The credentials the Keychain automatically presents as required are returned by this function.  At time of writing these default to nothing.  You can change the credentials presented by manipulating the pointer returned by this function.
	@result Returns a pointer to the credentials used by the Keychain framework.  You may change these at will, but should be aware of any race conditions that may be present. */

CSSM_ACCESS_CREDENTIALS* keychainFrameworkCredentials(void);


/*! @function cssmInit
	@abstract Attempts to initialised the CSSM for use by the Keychain framework.
	@discussion Initialisation of the CSSM is required before it may be used in any way.  This function is called as necessary by the Keychain framework automatically - you should never need to call it yourself.
	@result Returns true if initialisation was successful, false otherwise. */

bool cssmInit(void);

/*! @function cssmEnd
	@abstract Shuts down the CSSM.
	@discussion This function should [ideally] be called once a program is finished with the CSSM, including at program termination.  It is not required, however, and is not guaranteed to be automatically called by the Keychain framework at any point.  If for whatever reason you require explicit shutdown of the CSSM, call this function manually. */

void cssmEnd(void);

/*! @function clInit
	@abstract Attempts to load and initialise the CL module.
	@discussion This function must be called prior to any use of certificate (and related) functionality in the CSSM.  It is always called as necessary by the Keychain framework, so you shouldn't ever have need to call it yourself explicitly.

				Note that after a successful load, subsequent calls have no effect and will always return true (until the CL module is unloaded).
	@result Returns true if the CL module was loaded and attached successfully, false otherwise. */

bool clInit(void);

/*! @function clEnd
	@abstract Attempts to unload the CL module.
	@discussion When you are done performing CL-related operations, ideally you should unload the CL module.  In practice this doesn't normally happen; indeed the Keychain framework does not guarantee to ever do so automatically.

				Note that the result of trying to unload a module still in use is undefined.  In any case, after a call to clEnd the CL module should not be used again until a subsequent call to clInit. */

void clEnd(void);

/*! @function tpInit
	@abstract Attempts to load and initialise the TP module.
	@discussion This function must be called prior to any use of trust (and related) functionality in the CSSM.  It is always called as necessary by the Keychain framework, so you shouldn't ever have need to call it yourself explicitly.

				Note that after a successful load, subsequent calls have no effect and will always return true (until the TP module is unloaded).
	@result Returns true if the TP module was loaded and attached successfully, false otherwise. */

bool tpInit(void);

/*! @function tpEnd
	@abstract Attempts to unload the TP module.
	@discussion When you are done performing TP-related operations, ideally you should unload the TP module.  In practice this doesn't normally happen; indeed the Keychain framework does not guarantee to ever do so automatically.

				Note that the result of trying to unload a module still in use is undefined.  In any case, after a call to tpEnd the TP module should not be used again until a subsequent call to tpInit. */

void tpEnd(void);

/*! @function cspInit
	@abstract Attempts to load and initialise the CSP module.
	@discussion This function must be called prior to any use of cryptographic (and related) functionality in the CSSM.  It is always called as necessary by the Keychain framework, so you shouldn't ever have need to call it yourself explicitly.

				Note that after a successful load, subsequent calls have no effect and will always return true (until the CSP module is unloaded).
	@result Returns true if the CSP module was loaded and attached successfully, false otherwise. */

bool cspInit(void);

/*! @function cspEnd
	@abstract Attempts to unload the CSP module.
	@discussion When you are done performing CSP-related operations, ideally you should unload the CSP module.  In practice this doesn't normally happen; indeed the Keychain framework does not guarantee to ever do so automatically.

				Note that the result of trying to unload a module still in use is undefined.  In any case, after a call to cspEnd the CSP module should not be used again until a subsequent call to cspInit. */

void cspEnd(void);
