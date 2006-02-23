//
//  KeychainSearch.h
//  Keychain
//
//  Created by Wade Tregaskis on Fri Jan 24 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>
#import <Keychain/Keychain.h>


/*! @function FindCertificatesMatchingPublicKeyHash
    @abstract Locates and returns all certificates in the current user's keychain(s) matching the public key hash given.
    @discussion This method locates all the certificates matching the given public key hash in the current user's default keychain(s).  It returns nil if not found, the certificate otherwise.

                Note that this function is currently extremely inoptimal.  If performance is poor, please log a bug report to encourage it to be rewritten, or better yet rewrite it yourself. :)
    @param hash The hash of the public key for which to find certificates.
    @result An array of certificates, which may be empty if no matches are found.  Nil is returned on error. */

NSArray* FindCertificatesMatchingPublicKeyHash(NSData *hash);


@interface SearchAttribute : NSObject {
    SecKeychainAttribute attribute;
    BOOL freeWhenDone;
}

+ (SearchAttribute*)attributeWithTag:(SecKeychainAttrType)tag length:(UInt32)length data:(void*)data freeWhenDone:(BOOL)fre;
+ (SearchAttribute*)attributeWithTag:(SecKeychainAttrType)tag length:(UInt32)length data:(const void *)data;

- (SearchAttribute*)initWithTag:(SecKeychainAttrType)tag length:(UInt32)length data:(void*)data freeWhenDone:(BOOL)fre;
- (SearchAttribute*)initWithTag:(SecKeychainAttrType)tag length:(UInt32)length data:(const void *)data;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a SearchAttribute using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (SearchAttribute*)init;

- (SecKeychainAttributePtr)attributePtr;

@end



@interface KeychainSearch : NSObject {
    NSArray *keychainList;
    NSMutableArray *attributes;
    int error;
}

+ (KeychainSearch*)keychainSearchWithKeychains:(NSArray*)keychains;

- (KeychainSearch*)initWithKeychains:(NSArray*)keychains; // parameter may now be NULL, in which case the behaviour is the same as for the init method

/*! @method init
    @abstract Initialises the receiver to search the current user's default list of keychains.
    @discussion The user's default keychain list usually includes - at the very least - their own user keychain as well as the system keychain.  It can, however, be configured by the user to be whatever they like.
    @result Returns the receiver is successful, otherwise releases the receiver and returns nil. */

- (KeychainSearch*)init;

- (void)setCreationDate:(NSDate*)date;
- (void)setModificationDate:(NSDate*)date;
- (void)setDescription:(NSString*)desc;
- (void)setComment:(NSString*)comment;
- (void)setCreator:(NSString*)creator;
- (void)setType:(NSString*)type;
- (void)setLabel:(NSString*)label;
- (void)setIsVisible:(BOOL)visible;
- (void)setPasswordIsValid:(BOOL)valid;
- (void)setHasCustomIcon:(BOOL)customIcon;
- (void)setAccount:(NSString*)account;
- (void)setService:(NSString*)service;
- (void)setAttribute:(NSString*)attr;
- (void)setDomain:(NSString*)domain;
- (void)setServer:(NSString*)server;
- (void)setAuthenticationType:(SecAuthenticationType)type;
- (void)setPort:(UInt16)port;
- (void)setPath:(NSString*)path;
- (void)setAppleShareVolume:(NSString*)volume;
- (void)setAppleShareAddress:(NSString*)address;
- (void)setAppleShareSignature:(SecAFPServerSignature*)sig;
- (void)setProtocol:(SecProtocolType)protocol;
- (void)setCertificateType:(CSSM_CERT_TYPE)type;
- (void)setCertificateEncoding:(CSSM_CERT_ENCODING)encoding;
- (void)setCRLType:(CSSM_CRL_TYPE)type;
- (void)setCRLEncoding:(CSSM_CRL_ENCODING)encoding;
- (void)setIsAlias:(BOOL)alias;

- (NSArray*)searchResultsForClass:(SecItemClass)class;

- (NSArray*)anySearchResults;
- (NSArray*)genericSearchResults;
- (NSArray*)internetSearchResults;
- (NSArray*)appleShareSearchResults;
- (NSArray*)certificateSearchResults;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;
- (NSArray*)keychains;

@end
