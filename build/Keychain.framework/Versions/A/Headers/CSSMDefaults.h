//
//  CSSMDefaults.h
//  Keychain
//
//  Created by Wade Tregaskis on Wed May 07 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Security/Security.h>


/*! @constant DEFAULT_RANDOM_ALGORITHM
	@abstract The default random number generation algorithm used by the Keychain framework.
	@discussion The Keychain framework may require the use of a random number generator at numerous points, often without explicit indication of this (e.g. when generating init vectors, keys, or similar data).  The algorithm used by the framework is indicated by this constant.  This value may change in future versions of the framework. */

#define DEFAULT_RANDOM_ALGORITHM CSSM_ALGID_APPLE_YARROW

/*! @constant RAW_KEY_VERSION_CURRENT
	@abstract Returns the current (latest) raw key format supported by the framework.
	@discussion This constant resolves to the newest version of the framework's proprietary raw key format.  The framework is guaranteed to be able to read raw keys in all versions up to and including this version.  By default it will generate raw keys in the latest format - there is at time of writing no way to explicitly choose a particular version.

				Note that use of the framework's proprietary format is not recommended, and may be obsoleted in a future version. */

#define RAW_KEY_VERSION_CURRENT RAW_KEY_VERSION_1

/*! @constant KEYHEADER_VERSION_CURRENT_SIZE
	@abstract The header size of the current (latest) raw key format supported by the framework.
	@discussion This constant resolves to the size of the header (i.e. metadata, not key data itself) of the latest version of the Keychain framework's proprietary raw key format.  The version it corresponds to is given by RAW_KEY_VERSION_CURRENT.

				Note that use of the framework's proprietary format is not recommended, and may be obsoleted in a future version. */

#define KEYHEADER_VERSION_CURRENT_SIZE KEYHEADER_VERSION_1_SIZE


/*! @var keychainFrameworkInitVector
	@abstract A generic 16-byte init vector for cryptographic operations.
	@discussion You should avoid using this vector where possible - it is a legacy item that is due for removal in the near future.

				Note that you should not rely on this being constant, or containing the same data as keychainFrameworkInitVectorData. */

extern uint8 keychainFrameworkInitVector[16];

/*! @constant keychainFrameworkInitVectorData
	@abstract A generic init vector for cryptographic operations.
	@discussion You should avoid using this vector where possible - it is a legacy item that is due for removal in the near future.

				Note that you should not rely on this being constant, or containing the same data as keychainFrameworkInitVector.  Nor should you assume a particular length - use the Length member to determine this. */

extern const CSSM_DATA keychainFrameworkInitVectorData;

/*! @constant RAW_KEY_VERSION_1
	@abstract A constant representing version 1 of the proprietary Keychain raw key format (used by the 'key' extension of NSData, and the corresponding 'data' method of Key).
	@discussion The proprietary format used by the Keychain framework is not recommended for general purpose use.  Nonetheless, it does support versioning in case it needs to be updated or extended in future.  You shouldn't ever really need to use this constant, although you may find it useful to compare it with RAW_KEY_VERSION_CURRENT if you are looking for specific version support. */

extern const uint32 RAW_KEY_VERSION_1;

/*! @constant KEYHEADER_VERSION_1_SIZE
	@abstract The size of a raw key header in the Keychain framework's proprietary format (version 1).
	@discussion The proprietary format used by the Keychain framework is not recommended for general purpose use.  If you must use it, you may use this constant to determine the expected size of a raw key header (i.e. the key metadata, not including the actual key itself, which varies in length for different key types and strengths). */

extern const uint32 KEYHEADER_VERSION_1_SIZE;


/*! @function defaultModeForAlgorithm
	@abstract Returns the default encryption mode for a given algorithm.
	@discussion This function returns a safe default mode for a particular algorithm, and is used automatically by the Keychain framework when necessary.  For example, for any AES cryptographic operations, the framework will use this function to determine that the mode should be CSSM_ALGMODE_CBCPadIV8 (at time of writing).

				At time of writing there is no way to override these defaults, neither here nor on a per-operation basis.  If you require such functionality, submit a feature request to the author or on Sourceforge (http://www.sourceforge.net/projects/keychain/) to voice your interest.
	@param algorithm The algorithm.  Note that not all algorithms are known or supported, in which case CSSM_ALGMODE_NONE is returned.
	@result Returns a [hopefully] appropriate default mode for the given algorithm, or CSSM_ALGMODE_NONE if the algorithm is not explicitly supported. */
                                                                                                                                                                                                                        
CSSM_ENCRYPT_MODE defaultModeForAlgorithm(CSSM_ALGORITHMS algorithm);

/*! @function defaultPaddingForAlgorithm
	@abstract Returns the default padding mode for a given algorithm.
	@discussion This function returns a safe default padding mode for a particular algorithm, and is used automatically by the Keychain framework when necessary.  For example, for any AES cryptographic operations, the framework will use this function to determine that the mode should be CSSM_PADDING_PKCS7 (at time of writing).

				At time of writing there is no way to override these defaults, neither here nor on a per-operation basis.  If you require such functionality, submit a feature request to the author or on Sourceforge (http://www.sourceforge.net/projects/keychain/) to voice your interest.
	@param algorithm The algorithm.  Note that not all algorithms are known or supported, in which case CSSM_PADDING_NONE is returned.
	@result Returns a [hopefully] appropriate default padding mode for the given algorithm, or CSSM_PADDING_NONE if the algorithm is not explicitly supported. */

CSSM_PADDING defaultPaddingForAlgorithm(CSSM_ALGORITHMS algorithm);

/*! @function defaultDigestForAlgorithm
	@abstract Returns the default digest algorithm for a given algorithm.
	@discussion This function returns a safe default digest algorithm for a particular algorithm, and is used automatically by the Keychain framework when necessary.  This is used when performing public-key cryptographic operations, to determine for example an appropriate digest for RSA/DSA/FEE/etc.

				At time of writing there is no way to override these defaults, neither here nor on a per-operation basis.  If you require such functionality, submit a feature request to the author or on Sourceforge (http://www.sourceforge.net/projects/keychain/) to voice your interest.
	@param algorithm The algorithm.  Note that not all algorithms are known or supported, in which case CSSM_ALGID_NONE is returned.
	@result Returns a [hopefully] appropriate default digest algorithm for the given algorithm, or CSSM_ALGID_NONE if the algorithm is not explicitly supported. */

CSSM_ALGORITHMS defaultDigestForAlgorithm(CSSM_ALGORITHMS algorithm);
