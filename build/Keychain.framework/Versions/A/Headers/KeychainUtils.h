//
//  KeychainUtils.h
//  Keychain
//
//  Created by Wade Tregaskis on Wed May 14 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>

#import <Keychain/CSSMDefaults.h>
#import <Keychain/CSSMModule.h>

/*! @header KeychainUtils
    @abstract Defines various functions for generating random data.
    @discussion The CDSA provides for data generators, typically to generate random data.  This header contains functions for accessing that functionality. */


/*! @function generateRandomData
    @abstract Generates an arbitrary amount of random data using a given algorithm & seed.
    @discussion The quality and function of each algorithm varies, so consult relevant documentation.
    @param lengthInBytes The length of random data you wish to generate.
    @param algorithm The algorithm to use.
    @param seed A seed (if any) to start the generator with.  May be NULL.
    @param seedLength The length of 'seed', if it is not NULL, otherwise this parameter is ignored.
    @param CSPModule The CSP to use to generate the random data.  Pass nil to use the current default CSP.
    @result Returns 'lengthInBytes' bytes of generated data, or NULL if an error occurs. */

char* generateRandomData(uint32 lengthInBytes, CSSM_ALGORITHMS algorithm, const char *seed, unsigned int seedLength, CSSMModule *CSPModule);

/*! @function generateRandomNSData
    @abstract Generates an arbitrary amount of random data using a given algorithm & seed.
    @discussion This is merely a convenience function that wraps over generateRandomData.  Consequently, refer to the documentation for generateRandomData for more information.
    @param lengthInBytes The desired length of the random data you want.
    @param algorithm The algorithm to use.
    @param seed The seed (if any) to start the generator with.  May be NULL.
    @param CSPModule The CSP to use to generate the random data.  Pass nil to use the current default CSP.
    @result Returns the generated data, or nil if an error occurs. */

NSData* generateRandomNSData(uint32 lengthInBytes, CSSM_ALGORITHMS algorithm, NSData *seed, CSSMModule *CSPModule);

/*! @function generateGenericRandomData
    @abstract Generates a given number of bytes of random data, using a default algorithm and no seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomData - refer to the relevant documentation for additional details.

                Note that the default algorithm is provided by the DEFAULT_RANDOM_ALGORITHM constant if you're curious.
    @param lengthInBytes The length of the data to generate.
    @result Returns 'lengthInBytes' bytes of generated data, or NULL if an error occurs. */

#define generateGenericRandomData(lengthInBytes) generateRandomData(lengthInBytes, DEFAULT_RANDOM_ALGORITHM, NULL, 0, nil)

/*! @function generateParticularRandomData
    @abstract Generates a given number of bytes of random data, using a given algorithm and no seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomData - refer to the relevant documentation for additional details.
    @param lengthInBytes The length of the data to generate.
    @param algorithm The algorithm to use.
    @result Returns 'lengthInBytes' bytes of generated data, or NULL if an error occurs. */

#define generateParticularRandomData(lengthInBytes, algorithm) generateRandomData(lengthInBytes, algorithm, NULL, 0, nil)

/*! @function generateSeededRandomData
    @abstract Generates a given number of bytes of random data, using a default algorithm and a given seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomData - refer to the relevant documentation for additional details.

                Note that the default algorithm is provided by the DEFAULT_RANDOM_ALGORITHM constant if you're curious.
    @param lengthInBytes The length of the data to generate.
    @param seed The seed (if any) to start the generator with.  May be NULL (although in this case you could just as easily use the generateGenericRandomData function).
    @param seedLength The length of 'seed' in bytes.
    @result Returns 'lengthInBytes' bytes of generated data, or NULL if an error occurs. */

#define generateSeededRandomData(lengthInBytes, seed, seedLength) generateRandomData(lengthInBytes, DEFAULT_RANDOM_ALGORITHM, seed, seedLength, nil)

/*! @function generateGenericRandomNSData
    @abstract Generates a given number of bytes of random data, using a default algorithm and no seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomNSData - refer to the relevant documentation for additional details.

                Note that the default algorithm is provided by the DEFAULT_RANDOM_ALGORITHM constant if you're curious.
    @param lengthInBytes The length of the data to generate.
    @result Returns the generated data, or nil if an error occurs. */

#define generateGenericRandomNSData(lengthInBytes) generateRandomNSData(lengthInBytes, DEFAULT_RANDOM_ALGORITHM, nil, nil)

/*! @function generateParticularRandomNSData
    @abstract Generates a given number of bytes of random data, using a given algorithm and no seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomNSData - refer to the relevant documentation for additional details.
    @param lengthInBytes The length of the data to generate.
    @param algorithm The algorithm to use.
    @result Returns the generated data, or nil if an error occurs. */

#define generateParticularRandomNSData(lengthInBytes, algorithm) generateRandomNSData(lengthInBytes, algorithm, nil, nil)

/*! @function generateSeededRandomNSData
    @abstract Generates a given number of bytes of random data, using a default algorithm and a given seed.
    @discussion You may use this for convenience.  It ultimately calls generateRandomNSData - refer to the relevant documentation for additional details.

                Note that the default algorithm is provided by the DEFAULT_RANDOM_ALGORITHM constant if you're curious.
    @param lengthInBytes The length of the data to generate.
    @param seed The seed (if any) to start the generator with.  May be nil (although in this case you could just as easily use the generateGenericRandomNSData function).
    @result Returns the generated data, or nil if an error occurs. */

#define generateSeededRandomNSData(lengthInBytes, seed) generateRandomNSData(lengthInBytes, DEFAULT_RANDOM_ALGORITHM, seed, nil)
