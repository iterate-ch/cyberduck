//
//  Hashcash.h
//  Keychain
//
//  Created by Wade Tregaskis on 12/11/04.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>

#import <Keychain/CSSMModule.h>


#define kHashcashDefaultSeedLength 16
#define kHashcashSuffixLengthLimit 128


extern NSString *kDefaultHashcashStringFormat;


@interface Hashcash : NSObject {
    CSSMModule *_CSPModule;
    unsigned int _version;
    unsigned int _bits;
    NSCalendarDate *_date;
    NSString *_resource;
    NSString *_extensions;
    NSString *_salt;
    NSString *_suffix;
}

+ (NSCharacterSet*)stampFieldCharacterSet;
+ (NSCharacterSet*)stampDateCharacterSet;
    
+ (Hashcash*)hashcashFromStamp:(NSString*)stamp module:(CSSMModule*)CSPModule;
- (Hashcash*)initWithStamp:(NSString*)stamp module:(CSSMModule*)CSPModule;
- (Hashcash*)initWithModule:(CSSMModule*)CSPModule;

- (unsigned int)version;
- (int)setVersion:(unsigned int)newVersion;

- (unsigned int)bits;
- (int)setBits:(unsigned int)newBits;

- (NSCalendarDate*)date;
- (int)setDate:(NSDate*)newDate usingDefaultFormat:(BOOL)useDefaultFormat;

- (NSString*)resource;
- (int)setResource:(NSString*)newResource;

- (NSString*)extensions;
- (int)setExtensions:(NSString*)newExtensions;

- (NSString*)salt;
- (int)setSalt:(NSString*)newSalt;

- (NSString*)suffix;
- (int)setSuffix:(NSString*)newSuffix;
- (int)findSuffix;

- (NSString*)stamp;

- (BOOL)valid;

@end
