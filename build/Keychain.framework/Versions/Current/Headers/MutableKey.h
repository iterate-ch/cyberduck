//
//  MutableKey.h
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

#import <Foundation/Foundation.h>
#import <Keychain/Key.h>


@interface MutableKey : Key {
    CSSM_KEY *MutableCSSMKey;
    BOOL freeWhenDone;
}

+ (MutableKey*)generateKey:(CSSM_ALGORITHMS)algorithm size:(uint32)keySizeInBits validFrom:(NSCalendarDate*)validFrom validTo:(NSCalendarDate*)validTo usage:(uint32)keyUsage mutable:(BOOL)keyIsMutable extractable:(BOOL)keyIsExtractable sensitive:(BOOL)keyIsSensitive label:(NSString*)label;

+ (MutableKey*)keyWithKeyRef:(SecKeyRef)ke;
+ (MutableKey*)keyWithCSSMKey:(CSSM_KEY*)ke;
+ (MutableKey*)keyWithCSSMKey:(CSSM_KEY*)ke freeWhenDone:(BOOL)freeWhenDo;

- (MutableKey*)initWithKeyRef:(SecKeyRef)ke;
- (MutableKey*)initWithCSSMKey:(CSSM_KEY*)ke freeWhenDone:(BOOL)freeWhenDo;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a MutableKey using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (MutableKey*)init;

- (void)setFreeWhenDone:(BOOL)freeWhenDo;
- (BOOL)freeWhenDone;

- (void)setVersion:(CSSM_HEADERVERSION)version;
- (void)setBlobType:(CSSM_KEYBLOB_TYPE)blobType;
- (void)setFormat:(CSSM_KEYBLOB_FORMAT)format;
- (void)setAlgorithm:(CSSM_ALGORITHMS)algorithm;
- (void)setWrapAlgorithm:(CSSM_ALGORITHMS)wrapAlgorithm;
- (void)setKeyClass:(CSSM_KEYCLASS)keyClass;
- (void)setLogicalSize:(int)size;
- (void)setAttributes:(CSSM_KEYATTR_FLAGS)attributes;
- (void)setUsage:(CSSM_KEYUSE)usage;
- (void)setStartDate:(NSCalendarDate*)date;
- (void)setEndDate:(NSCalendarDate*)date;
- (void)setWrapMode:(CSSM_ENCRYPT_MODE)wrapMode;

- (void)setData:(NSData*)data;

- (CSSM_KEY*)CSSMKey;

@end

NSArray* generateKeyPair(CSSM_ALGORITHMS algorithm, uint32 keySizeInBits, NSCalendarDate *validFrom, NSCalendarDate *validTo, uint32 publicKeyUsage, uint32 privateKeyUsage, NSString *publicKeyLabel, NSString *privateKeyLabel);
