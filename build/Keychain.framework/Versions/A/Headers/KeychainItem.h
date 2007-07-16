//
//  KeychainItem.h
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

#import <Keychain/NSCachedObject.h>
#import <Foundation/Foundation.h>
#import <Security/Security.h>
#import <Keychain/Access.h>


@class Certificate;
@class Keychain;


@interface KeychainItem : NSCachedObject {
    SecKeychainItemRef keychainItem;
    int error;
}

/*! @method keychainItemWithKeychainItemRef:
    @abstract Creates and returns a KeychainItem instance based on a SecKeychainItemRef.
    @discussion The SecKeychainItemRef is retained by the new KeychainItem instance for the duration of it's life.  This method caches existing KeychainItem instances, such that multiple calls with the same SecKeychainItemRef will return the same unique KeychainItem instance.
    @param ke The SecKeychainItemRef.
    @result If a KeychainItem instance already returns for the given SecKeychainItemRef, returns that existing instance.  Otherwise, creates a new instance and returns it.  In case of error, returns nil. */

+ (KeychainItem*)keychainItemWithKeychainItemRef:(SecKeychainItemRef)keychainIt;

/*! @method initWithKeychainItemRef:
    @abstract Initiailizes the receiver with a SecKeychainItemRef.
    @discussion The SecKeychainItemRef is retained by the receiver for the duration of it's lifetime.  Changes to the SecKeychainItemRef will reflect on the receiver, and vice versa.  Note that this method caches existing KeychainItem instances, such that calling this with a SecKeychainItemRef that has already been used will release the receiver and return the existing instance.
    @param ke The SecKeychainItemRef.
    @result If SecKeychainItemRef is a valid keychain item, returns the receiver or the existing instance, if available (releasing the receiver in the latter case).  Otherwise, releases the receiver and returns nil. */

- (KeychainItem*)initWithKeychainItemRef:(SecKeychainItemRef)keychainIt;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a KeychainItem using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (KeychainItem*)init;

/*! @method kind
    @abstract Returns the kind of the receiver, e.g. key, certificate, password, etc.
    @discussion You can refer to the Apple CDSA documentation in the file <a href="file:///System/Library/Frameworks/Security.framework/Headers/SecKeychainItem.h">SecKeychainItem.h</a> for a list of 'kinds'.  At time of writing these are:

                kSecInternetPasswordItemClass - Internet password.
                kSecGenericPasswordItemClass - Generic password.
                kSecAppleSharePasswordItemClass - AppleShare password.
                kSecCertificateItemClass - Certificate.
    @result Returns one of the constants specified above, or -1 if an error occurs. */

- (SecItemClass)kind;

/*! @method kindString
    @abstract Returns the kind of the receiver as a natural-language string, e.g. "Internet Password".
    @discussion Avoid using these strings for progmatic comparison - use the 'kind' method and the constants instead.  These are intended primarily for display to end users.
    @result Returns a natural-language descriptive string for the kind, or "Error" if an explicit error occurs, or nil if any other error occurs. */

- (NSString*)kindString;

/*! @method isInternetItem
    @abstract Returns whether or not the receiver is an internet password.
    @discussion Simply a convenience method for the 'kind' method.
    @result Returns YES if the receiver is an internet password item, NO otherwise. */

- (BOOL)isInternetItem;

/*! @method isGenericItem
    @abstract Returns whether or not the receiver is a generic password.
    @discussion Simply a convenience method for the 'kind' method.
    @result Returns YES if the receiver is a generic password item, NO otherwise. */

- (BOOL)isGenericItem;

/*! @method isAppleShareItem
    @abstract Returns whether or not the receiver is an AppleShare password.
    @discussion Simply a convenience method for the 'kind' method.
    @result Returns YES if the receiver is an AppleShare password item, NO otherwise. */

- (BOOL)isAppleShareItem;

/*! @method isCertificate
    @abstract Returns whether or not the receiver is a certificate.
    @discussion Simply a convenience method for the 'kind' method.
    @result Returns YES if the receiver is a certificate item, NO otherwise. */

- (BOOL)isCertificate;

/*! @method setData:
    @abstract Sets the data (e.g. the password) for the receiver.
    @discussion The data for password items is the password itself.  For certificates, it is the raw certificate (try to avoid setting the certificate in this manner; you may add Certificate instances to keychains directly).  To set passwords, naturally use the setDataString: method instead; otherwise any implicit character set conversions that are performed may yield strange results.
    @param data The data to set for the receiver. */

- (void)setData:(NSData*)data;

/*! @method setDataString:
    @abstract Sets the string data (e.g. the password) for the receiver.
    @discussion The data for password items is the password itself.  For certificates, the data is the raw certificate, which needs to be set using setData: rather than this method (although Certificate instances can be added to keychains directly; avoid setting KeychainItem certificate data directly).

                Note: I'm not sure what permissions are required to edit an item; from memory you don't need *any*, meaning you can overwrite any item at will.  This isn't a security flaw (since it doesn't expose any sensitive data), but you could argue it's a bit of a potential pitfall nonetheless.
    @param data The data to set for the receiver, replacing any and all already set for it. */

- (void)setDataString:(NSString*)data;

/*! @method data
    @abstract Returns the data of the receiver (e.g. the password).
    @discussion The data for password items is the password itself.  For certificates, the data is the raw certificate.  You can convert between KeychainItem's & Certificate's automagically using the appropriate methods.  It is not recommended that you access a certificate's data directly using this method.

                Note that unless your application is already in the receiver's Access with the appropriate privileges, the user will be prompted to enter their password and allow access to the receiver (unless of course you have disabled user interaction, in which case anything which requires user interaction will result in the operation failing).  If the user denies access nil is returned.
    @result The data of the receiver, or nil if an error occurs (including insufficient privileges to read the receiver). */

- (NSData*)data;

/*! @method dataAsString
    @abstract Returns the data of the receiver (e.g. the password) as a string.
    @discussion The data for password items is the password itself.  For certificates, the data is the raw certificate.  You should use the 'data' method for retrieving certificate data, as it does not convert well to a string.

                Note that unless your application is already in the receiver's Access with the appropriate privileges, the user will be prompted to enter their password and allow access to the receiver (unless of course you have disabled user interaction, in which case anything which requires user interaction will result in the operation failing).  If the user denies access nil is returned.
    @result The data of the receiver, or nil if an error occurs (including insufficient privileges to read the receiver). */

- (NSString*)dataAsString;

/*! @method setCreationDate:
    @abstract Sets the creation date of the receiver.
    @discussion The creation date should reflect the date at which the receiver was created, *not* necessarily when it was first added to the keychain in which it currently resides.  This is similar to copying files between volumes; the creation date remains the same.  The creation date should be set automatically, as necessary.

                Note that Keychain Access does not follow this behaviour.  Indeed, the built-in behaviour may or may not be as described.  Damn.
    @param date The new creation date for the receiver. */

- (void)setCreationDate:(NSCalendarDate*)date;

/*! @method setModificationDate:
    @abstract Sets the modification date of the receiver.
    @discussion The modification date should reflect the date at which the receiver was last modified, which does not include it's addition to the owning keychain.  The modification date should be updated automatically as necessary.

                Note that Keychain Access does not follow this behaviour.  Indeed, the built-in behaviour may or may not be as described.  Damn.
    @param date The new modification date for the receiver. */

- (void)setModificationDate:(NSCalendarDate*)date;

/*! @method setTypeDescription:
    @abstract Sets the human description of the receiver's type.
    @discussion KeychainItem's can (and 'generic' or custom types <i>should</i>) have a type description associated with them, which concisely summarises their type & purpose.  Obviously, this method can be used to set this description.  Examples include "Proteus Service Password", or "Web Forms Password", etc.
    @param desc The description for the custom type. */

- (void)setTypeDescription:(NSString*)desc;

/*! @method setComment:
    @abstract Sets a human-readable comment for the receiver.
    @discussion The comment can be anything; it is intended to be end-user readable, in a similar manner to file comments in the Finder.
    @param comment The comment. */

- (void)setComment:(NSString*)comment;

/*! @method setCreator:
    @abstract Sets the creator code of the receiver.
    @discussion The creator code should */

- (void)setCreator:(NSString*)creator;
- (void)setType:(NSString*)type;
- (void)setLabel:(NSString*)label;
- (void)setIsVisible:(BOOL)visible;
- (void)setIsValid:(BOOL)valid;
- (void)setHasCustomIcon:(BOOL)icon;
- (void)setAccount:(NSString*)account;
- (void)setService:(NSString*)service;
- (void)setAttribute:(NSString*)attribute;
- (void)setDomain:(NSString*)domain;
- (void)setServer:(NSString*)server;
- (void)setAuthenticationType:(SecAuthenticationType)authType;
- (void)setPort:(UInt16)port;
- (void)setPath:(NSString*)path;
- (void)setAppleShareVolume:(NSString*)volume;
- (void)setAppleShareAddress:(NSString*)address;
- (void)setAppleShareSignature:(SecAFPServerSignature*)sig;
- (void)setProtocol:(SecProtocolType)protocol;
- (void)setCertificateType:(CSSM_CERT_TYPE)certType;
- (void)setCertificateEncoding:(CSSM_CERT_ENCODING)certEncoding;
- (void)setCRLtype:(CSSM_CRL_TYPE)type;
- (void)setCRLencoding:(CSSM_CRL_ENCODING)encoding;
- (void)setIsAlias:(BOOL)alias;

- (NSCalendarDate*)creationDate;
- (NSCalendarDate*)modificationDate;
- (NSString*)typeDescription;
- (NSString*)comment;
- (NSString*)creator;
- (NSString*)type;
- (NSString*)label;
- (BOOL)isVisible;
- (BOOL)passwordIsValid;
- (BOOL)hasCustomIcon;
- (NSString*)account;
- (NSString*)service;
- (NSString*)attribute;
- (NSString*)domain;
- (NSString*)server;
- (SecAuthenticationType)authenticationType;
- (NSString*)authenticationTypeString;
- (UInt16)port;
- (NSString*)path;
- (NSString*)appleShareVolume;
- (NSString*)appleShareAddress;
- (SecAFPServerSignature*)appleShareSignature;
- (NSData*)appleShareSignatureData;
- (SecProtocolType)protocol;
- (NSString*)protocolString;
- (CSSM_CERT_TYPE)certificateType;
- (CSSM_CERT_ENCODING)certificateEncoding;
- (CSSM_CRL_TYPE)CRLtype;
- (NSString*)CRLtypeString;
- (CSSM_CRL_ENCODING)CRLencoding;
- (NSString*)CRLencodingString;
- (BOOL)isAlias;

- (void)setAccess:(Access*)acc;
- (Access*)access;

- (Keychain*)keychain;

- (KeychainItem*)createDuplicate;

- (Certificate*)certificate;

- (void)deleteCompletely;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

              Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;
- (SecKeychainItemRef)keychainItemRef;

@end
