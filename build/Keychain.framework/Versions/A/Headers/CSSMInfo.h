//
//  CSSMInfo.h
//  Keychain
//
//  Created by Wade Tregaskis on Thu Jul 08 2004.
//
//  Copyright (c) 2004, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


#import <Security/Security.h>
#import <Foundation/Foundation.h>


extern NSString *USER_AUTHENTICATED; // True if the user has authenticated on the token

extern NSString *TOKEN_WRITE_PROTECTED; // Service provider is write protected
extern NSString *TOKEN_LOGIN_REQUIRED; // User must login to access private objects.
extern NSString *TOKEN_USER_PIN_INITIALIZED; // User's PIN has been initialized.
extern NSString *TOKEN_PROT_AUTHENTICATION; // Service provider has protected authentication path for entering a user PIN. No password should be supplied to the CSSM_CSP_Login API.
extern NSString *TOKEN_USER_PIN_EXPIRED; // The user PIN must be changed before the service provider can be used.
extern NSString *TOKEN_SESSION_KEY_PASSWORD; // Session keys held by the CSP require individual passwords, possibly in addition to a login password.
extern NSString *TOKEN_PRIVATE_KEY_PASSWORD; // Private keys held by the CSP require individual passwords, possibly in addition to a login password
extern NSString *TOKEN_STORES_PRIVATE_KEYS; // CSP can store private keys.
extern NSString *TOKEN_STORES_PUBLIC_KEYS; // CSP can store public keys.
extern NSString *TOKEN_STORES_SESSION_KEYS; // CSP can store session/secret keys
extern NSString *TOKEN_STORES_CERTIFICATES; // Service provider can store certs using DL APIs.
extern NSString *TOKEN_STORES_GENERIC; // Service provider can store generic objects using DL APIs.

extern NSString *MAX_SESSION_COUNT; // Maximum number of CSP handles referencing the token that may exist simultaneously.
extern NSString *OPEN_SESSION_COUNT; // Number of existing CSP handles referencing the token.
extern NSString *MAX_RW_SESSION_COUNT; // Maximum number of CSP handles that can reference the token simultaneously in read-write mode.
extern NSString *OPEN_RW_SESSION_COUNT; // Number of existing CSP handles referencing the token in read-write mode.
extern NSString *TOTAL_PUBLIC_MEMORY; // Amount of public storage space in the CSP. This value will be set to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose this information.
extern NSString *FREE_PUBLIC_MEMORY; // Amount of public storage space available for use in the CSP. This value will be set to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose this information.
extern NSString *TOTAL_PRIVATE_MEMORY; // Amount of private storage space in the CSP. This value will be set to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose this information.
extern NSString *FREE_PRIVATE_MEMORY; // Amount of private storage space available for use in the CSP. This value will be set to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose this information.

/*! @function cssmOperatingStatistics
	@abstract Returns a variety of operational statistics about a particular CSP module.
	@discussion Most of the information returned by this function is only interesting for curiosities sake.  Things such as memory use, capabilities of the CSP in a general sense, etc.  It's primary purpose is with the use of CSP-capable "tokens" - e.g. smartcards and similar devices.

				Note that statistics are not presently available from any other modules, e.g. CLs or TPs.  This is a limitation of the CDSA.
	@param handle A handle for the CSP module to query.
	@result Returns a dictionary containing zero or more key-value pairs.  You can use the constants declared in this header to query specific items.  If an error occurs, nil is returned. */

NSDictionary* cspOperatingStatistics(CSSM_CSP_HANDLE handle);
