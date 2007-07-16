//
//  CSSMErrors.h
//  Keychain
//
//  Created by Wade Tregaskis on Thu May 29 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>

#import <Security/cssmtype.h>


/*! @function CSSMErrorAsString
	@abstract Returns a human-readable name of a given error code, possibly with a short (one-line) description of the error.
	@discussion When displaying errors to the user you should always provide both a code and the name of the error - the code can be easily copied down for reporting back to you, the developer, while the name may provide some aid to the user in determining what the problem is; e.g. if it is "CL CRL already signed", they may be able to deduce that they are trying to re-sign an existing CRL, instead of a new one, or something similar.

				This function looks up the strings in an appropriate strings table, and as such will return localised names.  At time of writing the only localisation supported is English.
	@param error The CSSM error code.
	@result Returns a human-readable string containing at least the name of the error code, and possibly also a very brief description of the error.  Returns "Unknown" for any unknown error codes (suitably localised, of course). */

NSString* CSSMErrorAsString(CSSM_RETURN error);
