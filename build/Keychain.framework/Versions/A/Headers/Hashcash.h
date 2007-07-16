//
//  Hashcash.h
//  Keychain
//
//  Created by Wade Tregaskis on 12/11/04.
//  Copyright 2004 Wade Tregaskis. All rights reserved.
//

#import <Foundation/Foundation.h>


#define kHashcashDefaultSeedLength 16
#define kHashcashSuffixLengthLimit 128


extern NSString *kDefaultHashcashStringFormat;


@interface Hashcash : NSObject {
    unsigned int version;
    unsigned int bits;
    NSCalendarDate *date;
    NSString *resource;
    NSString *extensions;
    NSString *salt;
    NSString *suffix;
}

+ (NSCharacterSet*)stampFieldCharacterSet;
+ (NSCharacterSet*)stampDateCharacterSet;
    
+ (Hashcash*)hashcashFromStamp:(NSString*)stamp;
- (Hashcash*)initWithStamp:(NSString*)stamp;

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
