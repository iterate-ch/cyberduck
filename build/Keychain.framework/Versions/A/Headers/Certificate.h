//
//  Certificate.h
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
#import <Keychain/KeychainItem.h>
#import <Keychain/Key.h>
#import <Keychain/x509.h>


/*! @const KEYCHAIN_BUNDLE_IDENTIFIER
    @discussion An internal NSString used to identity the Keychain framework to the CDSA. */

extern NSString* KEYCHAIN_BUNDLE_IDENTIFIER;

/*! @class Certificate
    @discussion A certificate is a set of data describing something, signed by someone.  More specifically, it is usually used as a way to extend trust from a master source (e.g. a Certificate Authority) to another person or user (e.g. a website), by effectively 'vouching' for their identity, authenticity, or whatever other aspect is appropriate in a given context.

                You can use certificates to take an official stance on something.  For example, you may generate a certificate for someone you know, stating that you trust them for some purpose.  The certificate you generate for this can be handed around, possibly by the person in question, and verified to prove that you did indeed issue it.  Receivers of the certificate can, if they trust you, then extend some trust towards the person to whom the certificate belongs.

                Because trust is dynamic, you may wish to at some point revoke a certificate that you previously issued, declaring it null and void.  See the x509 documentation for details on how to do this in a standard way. */

@interface Certificate : NSCachedObject {
    SecCertificateRef certificate;
    int error;
}

/*! @method certificateWithCertificateRef:
    @abstract Creates and returns a new Certificate instance based on the SecCertificateRef provided.
    @discussion This class method returns a Certificate instance representing the SecCertificateRef given.  The returned instance is tied to the SecCertificateRef, such that changes to the SecCertificateRef reflect in the Certificate, and vice versa.  The returned Certificate retains the given SecCertificateRef for the duration of it's life.

                Note that this method caches unique certificates, based on their SecCertificateRef.  So calling it more than once with the same SecCertificateRef will return the existing instance, rather than create a new one.
    @param cert The SecCertificateRef from which to derive the returned Certificate.  This cannot be nil.
    @result If a Certificate instance already exists for the provided SecCertificateRef, it is returned.  Otherwise, a new instance is created and returned. */

+ (Certificate*)certificateWithCertificateRef:(SecCertificateRef)cert;

/*! @method certificateWithData:type:encoding:
    @abstract Creates a certificate from raw data.
    @discussion This class method creates and returns a certificate derived from the data passed.  You may receive a certificate in such a raw form from some certificate generation libraries, or over a network connection.
    @param data The raw data of the certificate
    @param type The type of certificate
    @param encoding The type of encoding used on the raw data
    @result If the data is valid, and the encoding type properly specified, this will create and return a new Certificate instance.  If an error occurs, nil is returned. */

+ (Certificate*)certificateWithData:(NSData*)data type:(CSSM_CERT_TYPE)type encoding:(CSSM_CERT_ENCODING)encoding;

/*! @method certificateWithEncodedData:
    @abstract Creates a certificate from an encoded certificate.
    @discussion This class method creates and returns a certificate derived from the encoded certificate provided.  While you can just pass this data to certificateWithData:type:encoding, it is better to use this function.
    @param encodedCert The encoded certificate.
    @result If the encoded certificate data is valid, a new Certificate instance is created and returned.  If an error occurs, nil is returned. */

+ (Certificate*)certificateWithEncodedData:(CSSM_ENCODED_CERT*)encodedCert;

/*! @method initWithCertificateRef:
    @abstract Initializes the receiver from the SecCertificateRef provided.
    @discussion Certificate's derived from SecCertificateRef's are cached, such that only one unique Certificate exists for each SecCertificateRef.  The SecCertificateRef is linked to the resulting Certificate instance, such that changes to one reflect on the other.  The SecCertificateRef given is retained for the duration of the Certificate's life.
    @param cert The SecCertificateRef with which to initialize the receiver.
    @result If a Certificate instance already exists for the given SecCertificateRef, the receiver is released and the existing instance returned.  Otherwise, the receiver is initialized as appropriate from the SecCertificateRef.  If an error occurs, the receiver is released and nil returned. */

- (Certificate*)initWithCertificateRef:(SecCertificateRef)cert;

/*! @method initWithData:type:encoding:
    @abstract Initializes the receiver with the raw certificate provided.
    @discussion The raw data provided should be in the encoding specified.  If the encoding is CSSM_ENCODING_UNKNOWN, the initializer will usually be able to deduce the encoding itself.  The same goes for the type - CSSM_TYPE_UNKNOWN will have the initializer take it's best guess.
    @param data The raw certificate data.
    @param type The type of certificate the data represents, or CSSM_TYPE_UNKNOWN if you're not sure.
    @param encoding The encoding of the raw data, or CSSM_ENCODING_UNKNOWN if you're not sure.
    @result If successful, the receiver is initialized as a new certificate based on the data provided.  If an error occurs, the receiver is released and nil returned. */

- (Certificate*)initWithData:(NSData*)data type:(CSSM_CERT_TYPE)type encoding:(CSSM_CERT_ENCODING)encoding;

/*! @method initWithEncodedData:
    @abstract Initializes the receiver from the encoded certificate provided.
    @discussion You can also pass the CSSM_ENCODED_CERT straight to initWithData:type:encoding, if you happen to have in an NSData instance to start with.  Pass the appropriate UNKNOWN's for the encoding in this case.  But it is better to use this initializer if you can.
    @param encodedCert The encoded certificate.
    @result If successful, the receiver is initialized from the encoded certificate.  If an error occurs, the receiver is released and nil returned. */

- (Certificate*)initWithEncodedData:(CSSM_ENCODED_CERT*)encodedCert;

/*! @method init
    @abstract Reject initialiser.
    @discussion You cannot initialise a Certificate using "init" - use one of the other initialisation methods.
    @result This method always releases the receiver and returns nil. */

- (Certificate*)init;

/*! @method data
    @abstract Returns the raw data of the certificate.
    @discussion This is the raw data of the certificate itself.  You would pass this data to other applications and services, which don't understand a Certificate or SecCertificateRef.  You may reconstruct a Certificate instance from this data using initWithData:type:encoding:.
    @result The raw data. */

- (NSData*)data;

/*! @method type
    @abstract Returns the type of certificate.
    @discussion Different certificate types have different requirements, in terms of what fields they must have and how they must be presented.  Generally, however, you needn't concern yourself with the specific details between different types.
    @result The type of certificate, some common ones of which are defined in <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h">cssmtype.h</a>. */

- (CSSM_CERT_TYPE)type;

/*! @method encoding
    @abstract Returns the encoding of the certificate.
    @discussion Certificates may be encoded in any of many different formats.  Some libraries only understand certain encodings.  The Keychain framework is dependent on the Security framework, and inherits only the encoding's supported by the Security framework.  At time of writing this is not well documented (in 10.2) and is expected to change (in 10.3).
    @result The encoding of certificate, some common ones of which are defined in <a href="file:///System/Library/Frameworks/Security.framework/Headers/cssmtype.h">cssmtype.h</a>. */

- (CSSM_CERT_ENCODING)encoding;

/*! @method rawValueOfField:
    @abstract Returns a CSSM_DATA* for the raw value of the requested field within the receiver.
    @discussion If multiple fields matching the given field OID are found, only the first is returned (and a warning logged).  As this method is used by many others in the Certificate class, including valueOfField, you can expect this behaviour from them too.
    @param tag The field OID to return.
    @result If at least one field is found with the requested field OID, the first is returned.  Otherwise nil is returned.  Note that you must free the result manually, using CSSM_CL_FreeFieldValue(). */

- (CSSM_DATA*)rawValueOfField:(const CSSM_OID*)tag;

/*! @method valueOfField:
    @abstract Returns the data of a requested field within the receiver.
    @discussion This method simply provides a nice wrapper over the rawValueOfField.
    @param The field OID to return.
    @result If at least one field is found with the requested field OID, the first is returned.  Otherwise nil is returned. */

- (NSData*)valueOfField:(const CSSM_OID*)tag;

/*! @method version
    @abstract Returns the version of the certificate.
    @discussion I'm not entirely sure what this is the version of - whether the certificate type (e.g. X509 v3) or the library that created the certificate.
    @result The major version number, or 0 if one is not present. */

- (int)version;

/*! @method serialNumber
    @abstract Returns the serial number of the receiver.
    @discussion While the serial number should generally be a normal integer value, there is no restriction on it's size.  Thus, in the interests of compatability, the default format is as raw data of any length.  You may convert this to an integer, if it fits, using DERToInt().
    @result The data of the serial number, or nil if one is not present. */

- (NSData*)serialNumber;

/*! @method signature
    @abstract Returns the signature on the certificate.
    @result The signature.  A Certificate instance should not be capable of handling an unsigned certificate, so treat a nil result as an error. */

- (X509Signature*)signature;

/*! @method publicKey
    @abstract Returns the subject's public key.
    @result The subject's public key, or nil if not present [or an error occurs]. */

- (Key*)publicKey;

/*! @method publicKeyInfo
    @abstract Returns the subject's public key info.
    @result An SPKInfo instance describing the subject's public key, or nil if an error occurs or it is not present. */

- (SPKInfo*)publicKeyInfo;

/*! @method subject
    @abstract Returns the subject's name.
    @discussion Note that at present this method only looks for and handles names stored in the certificate as C structs.  It does not pay any attention to BER encoded names.  This is considered a bug and may well be changed in future.
    @result The name list of the subject. */

- (NameList*)subject;

/*! @method issuer
    @abstract Returns the issuer's name.
    @discussion Note that at present this method only looks for and handles names stored in the certificate as C structs.  It does not pay any attention to BER encoded names.  This is considered a bug and may well be changed in future.
    @result The name list of the issuer. */

- (NameList*)issuer;

/*! @method signedCertificate
    @abstract Returns a signed certificate contained within the receiver.
    @discussion It is possible to bundle a certificate within another.  This method returns the first included signed certificate in the receiver.  The returned certificate, if any, is not the same as the receiver.
    @result The first signed certificate found within the receiver, or nil if none exists [or an error occurs]. */

- (SignedCertificate*)signedCertificate;

/*! @method validity
    @abstract Returns the validity of the receiver.
    @result The validity of the receiver, or nil if none is specified [or an error occurs]. */

- (Validity*)validity;

/*! @method extensions
    @abstract Returns a list of extensions contained within the receiver.
    @result All the extensions within the receiver, or nil if there are none [or an error occurs]. */

- (ExtensionList*)extensions;
    
/*! @method description
    @abstract Returns a human-readable, somewhat nicely presented representation of the receiver.
    @discussion This does it's best to provide a neat description of the certificate, including all it's fields and signatures.  It is rather verbose, as most certificates contain quite a lot of data, so you probably shouldn't try presenting a certificate to an end user in this form.
    @result The description. */

- (NSString*)description;

/*! @method isEqualToCertificate
    @abstract Compares two certificates and returns YES if they are exactly the same.
    @discussion This method compares the binary data of the two certificates, not any pointer values or similar.  As such, it will tell you authoritatively whether or not a certificate is the same as another.  It is needed because, while the Certificate class will return an existing instance, the Security framework will not.  Thus it is quite possible (indeed, quite probably) that you will in the course of normal keychain usage get returned a different instance of the same certificate.  This is presently unavoidable, and probably won't be fixed any time soon.

                Note that this will not consider two certificates to be the same if their fields are in different orders, even if they have exactly the same fields and data.  This is not considered a bug, although it is a possible future feature to be able to compare independently of field order.  Note, however, that two such dis-ordered certificates cannot be considered the same for any cryptographic and authentication operations, as they will generate different hashes (among other things).  So the missing feature is not greatly missed.
    @param cert The certificate to compare the receiver to.
    @result YES if the receiver is byte-identical to the provided cert, NO otherwise. */

- (BOOL)isEqualToCertificate:(Certificate*)cert;

//- (NSString*)issuer; // figure out the CSSM_X509_NAME data structure
//- (NSString*)subject; // figure out the CSSM_X509_NAME data structure

/*! @method keychainItem
    @abstract Returns a KeychainItem representing the receiver.
    @discussion As it is presently written, Apple's Security framework actually represents Certificates as subclasses of KeychainItem's, so this simply casts the receiver to a KeychainItem.  Note that the result is a separate instance, however, and should be managed separately from the receiver.

                Also note that this method will always function as described, no matter how Apple's Security framework may change.
    @result The receiver represented as a KeychainItem. */

- (KeychainItem*)keychainItem;

/*- (CertificateBundle*)exportAsBundleOfType:(CSSM_CERT_BUNDLE_TYPE)type withEncoding:(CSSM_CERT_BUNDLE_ENCODING)encoding;
- (CertificateBundle*)exportAsDefaultBundle;*/

/*! @method cryptoHandle
    @abstract Returns the CDSA CL handle by which the receiver is managed.
    @discussion This is only provided at the moment because the Certificate class is so limited and incomplete, that you may need to do a lot of stuff using the CDSA directly.  This handle, combined with the SecCertificateRef, should allow for this.

                As such, this method may be deprecated in future releases.  Do not use it unless you have to.
    @result An active handle to the CDSA CL which manages the receiver. */

- (CSSM_CL_HANDLE)cryptoHandle;

/*! @method lastError
    @abstract Returns the last error that occured for the receiver.
    @discussion The set of error codes encompasses those returned by Sec* functions - refer to the Security framework documentation for a list.  At present there are no other error codes defined for Access instances.

                Please note that this error code is local to the receiver only, and not any sort of shared global value.
    @result The last error that occured, or zero if the last operation was successful. */

- (int)lastError;

/*! @method certificateRef
    @abstract Returns a SecCertificateRef derived from the receiver.
    @result A SecCertificateRef derived from the receiver.  Changes to this returned SecCertificateRef will reflect back in the receiver. */

- (SecCertificateRef)certificateRef;

@end
