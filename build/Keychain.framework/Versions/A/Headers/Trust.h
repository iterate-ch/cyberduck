//
//  Trust.h
//  Keychain
//
//  Created by Wade Tregaskis on Wed Feb 05 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Keychain/NSCachedObject.h>
#import <Foundation/Foundation.h>
#import <Security/Security.h>
#import <Keychain/Certificate.h>
#import <Keychain/Policy.h>


@interface Trust : NSCachedObject {
    SecTrustRef trust;
    SecTrustResultType lastEval;
    int error;
}

+ (SecTrustUserSetting)userTrustForCeritifcate:(Certificate*)cert policy:(Policy*)pol;
+ (void)setUserTrustForCertificate:(Certificate*)cert policy:(Policy*)pol trust:(SecTrustUserSetting)tru;

+ (Trust*)trustForCertificates:(NSArray*)certificates policies:(NSArray*)policies;
+ (Trust*)trustWithTrustRef:(SecTrustRef)tru;

- (Trust*)initForCertificates:(NSArray*)certificates policies:(NSArray*)policies;
- (Trust*)initWithTrustRef:(SecTrustRef)tru;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a Trust using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (Trust*)init;

- (void)makeTrustForKeychains:(NSArray*)chains;

- (void)allowExpiredCertificates:(BOOL)allow;

- (BOOL)canEvaluate;

- (BOOL)isInvalid;
- (BOOL)canProceed;
- (BOOL)needsConfirmation;
- (BOOL)userDenied;
- (BOOL)userDidNotSpecify;
- (BOOL)hasRecoverableFailure;
- (BOOL)hasFatalFailure;
- (BOOL)hasUnknownError;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

              Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;
- (SecTrustRef)trustRef;

@end
