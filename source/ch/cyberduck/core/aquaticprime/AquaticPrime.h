//
// AquaticPrime.h
// AquaticPrime Framework
//
// Copyright (c) 2006, Lucas Newman
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//	¥Redistributions of source code must retain the above copyright notice,
//	 this list of conditions and the following disclaimer.
//	¥Redistributions in binary form must reproduce the above copyright notice,
//	 this list of conditions and the following disclaimer in the documentation and/or
//	 other materials provided with the distribution.
//	¥Neither the name of Aquatic nor the names of its contributors may be used to 
//	 endorse or promote products derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Cocoa/Cocoa.h>

@interface AquaticPrime : NSObject {
}

// Creation
+ (id)aquaticPrimeWithKey:(NSString *)key;
+ (id)aquaticPrimeWithKey:(NSString *)key privateKey:(NSString *)privateKey;
- (id)initWithKey:(NSString *)key;
- (id)initWithKey:(NSString *)key privateKey:(NSString *)privateKey;

// Getters & Setters
- (BOOL)setKey:(NSString *)key;
- (BOOL)setKey:(NSString *)key privateKey:(NSString *)privateKey;
- (NSString *)key;
- (NSString *)privateKey;

// Generating license data/files
- (NSData*)licenseDataForDictionary:(NSDictionary*)dict;
- (BOOL)writeLicenseFileForDictionary:(NSDictionary*)dict toPath:(NSString *)path;

// Validating license data/files
- (NSDictionary*)dictionaryForLicenseData:(NSData *)data;
- (NSDictionary*)dictionaryForLicenseFile:(NSString *)path;
- (BOOL)verifyLicenseData:(NSData *)data;
- (BOOL)verifyLicenseFile:(NSString *)path;

// Blacklisting
- (void)setBlacklist:(NSArray*)hashArray;

// Error handling
- (NSString*)getLastError;

@end
