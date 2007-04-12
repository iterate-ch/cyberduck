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


/*! @constant USER_AUTHENTICATED
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP, if it allows for user authentication, has been authenticated.  You can determine if the CSP requires user authentication using the TOKEN_LOGIN_REQUIRED key.  If TOKEN_LOGIN_REQUIRED is NO, the value returned by this key (USER_AUTHENTICATED) is undefined. */

extern NSString *USER_AUTHENTICATED;

/*! @constant TOKEN_WRITE_PROTECTED
    @abstract Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP is write protected. */

extern NSString *TOKEN_WRITE_PROTECTED;

/*! @constant TOKEN_LOGIN_REQUIRED
    @abstract Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP requires the user to login (or otherwise authenticate using some means) to access some/all items within the CSP.  If YES, you can use the USER_AUTHENTICATED key to see if the user has provided the required authorisation. */

extern NSString *TOKEN_LOGIN_REQUIRED;

/*! @constant TOKEN_USER_PIN_INITIALIZED
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP has initialised the user's pin.  This is rather ambigious, and ultimately up to the CSP implementation in question, but is generally taken as meaning that any user knowledge that must be programmed into the device (e.g. a PIN, password, etc) has been done so.  Note that a return of "NO" doesn't really imply anything, although typically if TOKEN_PROT_AUTHENTICATION is NO it's generally assumed TOKEN_USER_PIN_INITIALIZED is irrelevant. */

extern NSString *TOKEN_USER_PIN_INITIALIZED;

/*! @constant TOKEN_PROT_AUTHENTICATION
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP already has a mechanism for acquiring user authentication, in which case no password should be provided via CSSM_CSP_Login (don't worry about this if you're not dealing the CSSM directly). */

extern NSString *TOKEN_PROT_AUTHENTICATION;

/*! @constant TOKEN_USER_PIN_EXPIRED
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP requires the user to change their authentication mechanism (e.g. PIN) before the CSP can be used. */

extern NSString *TOKEN_USER_PIN_EXPIRED;

/*! @constant TOKEN_SESSION_KEY_PASSWORD
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP requires individual passwords to access session keys, in addition to any "login" password that may also be required. */

extern NSString *TOKEN_SESSION_KEY_PASSWORD;

/*! @constant TOKEN_PRIVATE_KEY_PASSWORD
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP requires individual passwords to access private keys, in addition to any "login" password that may also be required. */

extern NSString *TOKEN_PRIVATE_KEY_PASSWORD;

/*! @constant TOKEN_STORES_PRIVATE_KEYS
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP can store private keys. */

extern NSString *TOKEN_STORES_PRIVATE_KEYS;

/*! @constant TOKEN_STORES_PUBLIC_KEYS
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP can store public keys. */

extern NSString *TOKEN_STORES_PUBLIC_KEYS;

/*! @constant TOKEN_STORES_SESSION_KEYS
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP can store session/secret keys. */

extern NSString *TOKEN_STORES_SESSION_KEYS;

/*! @constant TOKEN_STORES_CERTIFICATES
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP can store certificates using the DL APIs (which doesn't need to mean anything to you unless you're using the CSSM directly). */

extern NSString *TOKEN_STORES_CERTIFICATES;

/*! @constant TOKEN_STORES_GENERIC
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is a boolean NSNumber indicating whether the given CSP can store generic objects (using the DL APIs, which doesn't need to mean anything to you unless you're using the CSSM directly). */

extern NSString *TOKEN_STORES_GENERIC;

/*! @constant MAX_SESSION_COUNT
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating how many sessions may exist simultaneously using the given CSP.  A session is defined by it's handle, so this is ultimately the number of unique handles that can be issued [simultaneously] by the CSP. */

extern NSString *MAX_SESSION_COUNT;

/*! @constant OPEN_SESSION_COUNT
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating how many sessions there presently are using the given CSP.  A session is defined by it's handle, so this is ultimately the number of existing handles issued for the given CSP. */

extern NSString *OPEN_SESSION_COUNT;

/*! @constant MAX_RW_SESSION_COUNT
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating how many sessions may exist simultaneously using the given CSP, with both read <i>and</i> write privileges.  A session is defined by it's handle, so this is ultimately the number of unique handles [with read and write privileges] that can be issued [simultaneously] by the CSP. */

extern NSString *MAX_RW_SESSION_COUNT;

/*! @constant OPEN_RW_SESSION_COUNT
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating how many sessions there presently are using the given CSP, with both read <i>and</i> write privileges.  A session is defined by it's handle, so this is ultimately the number of existing handles [with read and write privileges] issued for the given CSP. */

extern NSString *OPEN_RW_SESSION_COUNT;

/*! @constant TOTAL_PUBLIC_MEMORY
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating the total storage space available within the given CSP.  This may be equal to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose such information. */

extern NSString *TOTAL_PUBLIC_MEMORY;

/*! @constant FREE_PUBLIC_MEMORY
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating the total storage space free (unused or otherwise available for immediate use) within the given CSP.  This may be equal to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose such information. */

extern NSString *FREE_PUBLIC_MEMORY;

/*! @constant TOTAL_PRIVATE_MEMORY
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating the total private storage space available within the given CSP.  This may be equal to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose such information. */

extern NSString *TOTAL_PRIVATE_MEMORY;

/*! @constant FREE_PRIVATE_MEMORY
    @discussion Key for indexing into the dictionary returned by CSPOperatingStatistics, who's object is an integer NSNumber indicating the total private storage space free (unused or otherwise available for immediate use) within the given CSP.  This may be equal to CSSM_VALUE_NOT_AVAILABLE if the CSP does not wish to expose such information. */

extern NSString *FREE_PRIVATE_MEMORY;

/*! @function CSPOperatingStatistics
	@abstract Returns a variety of operational statistics about a particular CSP module.
	@discussion Most of the information returned by this function is only interesting for curiosities sake.  Things such as memory use, capabilities of the CSP in a general sense, etc.  It's primary purpose is with the use of CSP-capable "tokens" - e.g. smartcards and similar devices.

				Note that statistics are not presently available from any other modules, e.g. CLs or TPs.  This is a limitation of the CDSA, although it perhaps speaks about the lack of usefulness of such statistics (remembering that most CL modules will in fact use a CSP module for required cryptographic operations).
    @param handle A handle for the CSP module to query.
	@result Returns a dictionary containing zero or more key-value pairs.  You can use the constants declared in this header to query specific items.  If an error occurs, nil is returned. */

NSDictionary* CSPOperatingStatistics(CSSM_CSP_HANDLE handle);
