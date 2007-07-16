//
//  x509.h
//  Keychain
//
//  Created by Wade Tregaskis on Wed May 21 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/x509defs.h>
#import <Keychain/UtilitySupport.h>
#import <Keychain/CSSMUtils.h>


@class SignedCRL;
@class TBSCertList;
@class RevokedCertificate;
@class RevokedCertificateList;
@class RevokedCertificateListenumerator;
@class PolicyQualifier;
@class PolicyInfo;
@class PolicyQualifierList;
@class PolicyQualifierListEnumerator;
@class SignedCertificate;
@class X509Signature;
@class TBSCertificate;
@class ExtensionListEnumerator;
@class ExtensionList;
@class Extension;
@class Pair;
@class TagAndValue;
@class Validity;
@class Time;
@class SPKInfo;
@class TypeValuePair;
@class DistinguishedName;
@class NameList;
@class DistinguishedNameEnumerator;
@class NameListEnumerator;
@class AlgorithmIdentifier;


@interface SignedCRL : NSObject {
    CSSM_X509_SIGNED_CRL *_CRL;
    BOOL freeWhenDone;
}

+ (SignedCRL*)signedCRLWithRawRef:(CSSM_X509_SIGNED_CRL*)ref freeWhenDone:(BOOL)fre;
+ (SignedCRL*)signedCRLWithCertificates:(TBSCertList*)certificates signature:(X509Signature*)signature;

- (SignedCRL*)initWithSignedCRLRef:(CSSM_X509_SIGNED_CRL*)ref freeWhenDone:(BOOL)fre;
- (SignedCRL*)initWithCertificates:(TBSCertList*)certificates signature:(X509Signature*)signature;

- (TBSCertList*)certificateList;
- (X509Signature*)signature;

- (NSString*)description;

- (CSSM_X509_SIGNED_CRL*)signedCRLRef;
   
@end


@interface TBSCertList : NSObject {
    CSSM_X509_TBS_CERTLIST *_CertList;
    BOOL freeWhenDone;
}

+ (TBSCertList*)listWithRawRef:(CSSM_X509_TBS_CERTLIST*)ref freeWhenDone:(BOOL)fre;
+ (TBSCertList*)listWithIssuer:(NameList*)issuer signatureAlgorithm:(AlgorithmIdentifier*)signatureAlgorithm thisUpdate:(Time*)thisUpdate nextUpdate:(Time*)nextUpdate certificates:(RevokedCertificateList*)revokedCertificates extensions:(ExtensionList*)extensions;

- (TBSCertList*)initWithTBSCertListRef:(CSSM_X509_TBS_CERTLIST*)ref freeWhenDone:(BOOL)fre;
- (TBSCertList*)initWithIssuer:(NameList*)issuer signatureAlgorithm:(AlgorithmIdentifier*)signatureAlgorithm thisUpdate:(Time*)thisUpdate nextUpdate:(Time*)nextUpdate certificates:(RevokedCertificateList*)revokedCertificates extensions:(ExtensionList*)extensions;

- (NSData*)version;
- (void)setVersion:(NSData*)version;

- (AlgorithmIdentifier*)signatureAlgorithm;
- (NameList*)issuer;
- (Time*)thisUpdate;
- (Time*)nextUpdate;
- (RevokedCertificateList*)certificateList;
- (ExtensionList*)extensionList;

- (NSString*)description;

- (CSSM_X509_TBS_CERTLIST*)TBSCertListRef;

@end


@interface RevokedCertificate : NSObject {
    CSSM_X509_REVOKED_CERT_ENTRY *_RevokedCert;
    BOOL freeWhenDone;
}

+ (RevokedCertificate*)revokedCertificateWithRawRef:(CSSM_X509_REVOKED_CERT_ENTRY*)ref freeWhenDone:(BOOL)fre;
+ (RevokedCertificate*)revokedCertificateWithSerial:(uint32)serial date:(Time*)revocationDate extensions:(ExtensionList*)extensions;

- (RevokedCertificate*)initWithRevokedCertificateRef:(CSSM_X509_REVOKED_CERT_ENTRY*)ref freeWhenDone:(BOOL)fre;
- (RevokedCertificate*)initWithSerial:(uint32)serial date:(Time*)revocationDate extensions:(ExtensionList*)extensions;

- (NSData*)serialNumber;
- (void)setSerialNumber:(NSData*)serial;

- (Time*)revocationDate;
- (ExtensionList*)extensionList;

- (NSString*)description;

- (CSSM_X509_REVOKED_CERT_ENTRY*)revokedCertificateRef;

@end


@interface RevokedCertificateList : NSObject {
    CSSM_X509_REVOKED_CERT_LIST *_RevokedCertList;
    BOOL freeWhenDone;
    uint32 _capacity;
}

+ (RevokedCertificateList*)listWithRCLRef:(CSSM_X509_REVOKED_CERT_LIST*)ref freeWhenDone:(BOOL)fre;
+ (RevokedCertificateList*)initWithCertificates:(NSArray*)arrayOfRevokedCertificates;

- (RevokedCertificateList*)initWithRCLRef:(CSSM_X509_REVOKED_CERT_LIST*)ref freeWhenDone:(BOOL)fre;
- (RevokedCertificateList*)initWithCertificates:(NSArray*)arrayOfRevokedCertificates;

- (RevokedCertificate*)certificateAtIndex:(uint32)index;
- (void)removeCertificateAtIndex:(uint32)index;
- (void)addCertificate:(RevokedCertificate*)certificate;

- (uint32)numberOfCertificates;

- (NSEnumerator*)certificateEnumerator;

- (NSString*)description;

- (CSSM_X509_REVOKED_CERT_LIST*)RCLRef;

@end


@interface RevokedCertificateListEnumerator : NSEnumerator {
    RevokedCertificateList *_list;
    uint32 _index;
}

+ (RevokedCertificateListEnumerator*)enumeratorForRevokedCertificateList:(RevokedCertificateList*)list;

- (RevokedCertificateListEnumerator*)initWithRevokedCertificateList:(RevokedCertificateList*)list;

- (NSArray*)allObjects;
- (RevokedCertificate*)nextObject;

@end


@interface PolicyInfo : NSObject {
    CSSM_X509EXT_POLICYINFO *_PolicyInfo;
    BOOL freeWhenDone;
}

+ (PolicyInfo*)infoWithRawRef:(CSSM_X509EXT_POLICYINFO*)ref freeWhenDone:(BOOL)fre;
+ (PolicyInfo*)infoWithID:(const CSSM_OID*)policyID qualifiers:(PolicyQualifierList*)qualifiers;

- (PolicyInfo*)initWithPolicyInfoRef:(CSSM_X509EXT_POLICYINFO*)ref freeWhenDone:(BOOL)fre;
- (PolicyInfo*)initWithID:(const CSSM_OID*)policyID qualifiers:(PolicyQualifierList*)qualifiers;

- (NSData*)identifier;
- (void)setIdentifier:(NSData*)identifier;

- (PolicyQualifierList*)qualifierList;

- (NSString*)description;

- (CSSM_X509EXT_POLICYINFO*)policyInfoRef;

@end


@interface PolicyQualifier : NSObject {
    CSSM_X509EXT_POLICYQUALIFIERINFO *_PolicyQualifier;
    BOOL freeWhenDone;
}

+ (PolicyQualifier*)qualifierWithRawRef:(CSSM_X509EXT_POLICYQUALIFIERINFO*)ref freeWhenDone:(BOOL)fre;
+ (PolicyQualifier*)qualifierWithID:(const CSSM_OID*)qualifierID value:(NSData*)value;

- (PolicyQualifier*)initWithPolicyQualifierRef:(CSSM_X509EXT_POLICYQUALIFIERINFO*)ref freeWhenDone:(BOOL)fre;
- (PolicyQualifier*)initWithID:(const CSSM_OID*)qualifierID value:(NSData*)value;

- (NSData*)qualifierID;
- (void)setQualifierID:(NSData*)qualifierID;

- (NSData*)value;
- (void)setValue:(NSData*)value;

- (NSString*)description;

- (CSSM_X509EXT_POLICYQUALIFIERINFO*)qualifierRef;

@end


@interface PolicyQualifierList : NSObject {
    CSSM_X509EXT_POLICYQUALIFIERS *_PolicyQualifierList;
    uint32 _capacity;
    BOOL freeWhenDone;
}

+ (PolicyQualifierList*)listWithRawRef:(CSSM_X509EXT_POLICYQUALIFIERS*)ref freeWhenDone:(BOOL)fre;
+ (PolicyQualifierList*)listWithQualifiers:(NSArray*)arrayOfQualifiers;

- (PolicyQualifierList*)initWithPolicyQualifierListRef:(CSSM_X509EXT_POLICYQUALIFIERS*)ref freeWhenDone:(BOOL)fre;
- (PolicyQualifierList*)initWithQualifiers:(NSArray*)arrayOfQualifiers;

- (PolicyQualifier*)qualifierAtIndex:(uint32)index;
- (void)removeQualifierAtIndex:(uint32)index;
- (void)addQualifier:(PolicyQualifier*)entry;

- (uint32)numberOfQualifiers;

- (NSString*)description;

- (CSSM_X509EXT_POLICYQUALIFIERS*)qualifierListRef;

@end


@interface PolicyQualifierListEnumerator : NSEnumerator {
    PolicyQualifierList *_list;
    uint32 _index;
}

+ (PolicyQualifierListEnumerator*)enumeratorForQualifierList:(PolicyQualifierList*)list;

- (PolicyQualifierListEnumerator*)initWithQualifierList:(PolicyQualifierList*)list;

- (NSArray*)allObjects;
- (PolicyQualifier*)nextObject;

@end


@interface SignedCertificate : NSObject {
    CSSM_X509_SIGNED_CERTIFICATE *_SignedCertificate;
    BOOL freeWhenDone;
}

+ (SignedCertificate*)signedCertificateWithRawRef:(CSSM_X509_SIGNED_CERTIFICATE*)ref freeWhenDone:(BOOL)fre;
+ (SignedCertificate*)signedCertificateWithCertificate:(TBSCertificate*)certificate signature:(X509Signature*)signature;

- (SignedCertificate*)initWithSignedCertificateRef:(CSSM_X509_SIGNED_CERTIFICATE*)ref freeWhenDone:(BOOL)fre;
- (SignedCertificate*)initWithCertificate:(TBSCertificate*)certificate signature:(X509Signature*)signature;

- (TBSCertificate*)certificate;
- (X509Signature*)signature;

- (NSString*)description;

- (CSSM_X509_SIGNED_CERTIFICATE*)signedCertificateRef;

@end


@interface X509Signature : NSObject {
    CSSM_X509_SIGNATURE *_Signature;
    BOOL freeWhenDone;
}

+ (X509Signature*)signatureWithRawRef:(CSSM_X509_SIGNATURE*)ref freeWhenDone:(BOOL)fre;
+ (X509Signature*)signatureWithAlgorithm:(AlgorithmIdentifier*)algorithm data:(NSData*)data;

- (X509Signature*)initWithSignatureRef:(CSSM_X509_SIGNATURE*)ref freeWhenDone:(BOOL)fre;
- (X509Signature*)initWithAlgorithm:(AlgorithmIdentifier*)algorithm data:(NSData*)data;

- (AlgorithmIdentifier*)algorithm;
- (NSData*)data;

- (NSString*)description;

- (CSSM_X509_SIGNATURE*)signatureRef;

@end


@interface TBSCertificate : NSObject {
    CSSM_X509_TBS_CERTIFICATE *_TBSCertificate;
    BOOL freeWhenDone;
}

+ (TBSCertificate*)certificateWithRawRef:(CSSM_X509_TBS_CERTIFICATE*)ref freeWhenDone:(BOOL)fre;
+ (TBSCertificate*)certificateWithSerial:(uint32)serial signatureAlgorithm:(AlgorithmIdentifier*)signatureAlgorithm issuer:(NameList*)issuer subject:(NameList*)subject validity:(Validity*)validity publicKeyInfo:(SPKInfo*)publicKeyInfo issuerID:(NSData*)issuerID subjectID:(NSData*)subjectID extensions:(ExtensionList*)extensions;

- (TBSCertificate*)initWithTBSCertificateRef:(CSSM_X509_TBS_CERTIFICATE*)ref freeWhenDone:(BOOL)fre;
- (TBSCertificate*)initWithSerial:(uint32)serial signatureAlgorithm:(AlgorithmIdentifier*)signatureAlgorithm issuer:(NameList*)issuer subject:(NameList*)subject validity:(Validity*)validity publicKeyInfo:(SPKInfo*)publicKeyInfo issuerID:(NSData*)issuerID subjectID:(NSData*)subjectID extensions:(ExtensionList*)extensions;

- (NSData*)version;
- (void)setVersion:(NSData*)version;

- (NSData*)serialNumber;
- (void)setSerialNumber:(NSData*)serial;

- (AlgorithmIdentifier*)signatureAlgorithm;
- (void)setSignatureAlgorithm:(AlgorithmIdentifier*)signatureAlgorithm;

- (NameList*)issuer;
- (void)setIssuer:(NameList*)issuer;

- (Validity*)validity;
- (void)setValidity:(Validity*)validity;

- (NameList*)subject;
- (void)setSubject:(NameList*)subject;

- (SPKInfo*)subjectPublicKeyInfo;
- (void)setSubjectPublicKeyInfo:(SPKInfo*)publicKeyInfo;

- (NSData*)issuerID;
- (void)setIssuerID:(NSData*)issuerID;

- (NSData*)subjectID;
- (void)setSubjectID:(NSData*)subjectID;

- (ExtensionList*)extensions;
- (void)setExtensions:(ExtensionList*)extensions;

- (NSString*)description;

- (CSSM_X509_TBS_CERTIFICATE*)TBSCertificateRef;

@end


@interface ExtensionListEnumerator : NSEnumerator {
    ExtensionList *_list;
    uint32 _index;
}

+ (ExtensionListEnumerator*)enumeratorForExtensionList:(ExtensionList*)list;

- (ExtensionListEnumerator*)initWithExtensionList:(ExtensionList*)list;

- (NSArray*)allObjects;
- (Extension*)nextObject;

@end


@interface ExtensionList : NSObject {
    CSSM_X509_EXTENSIONS *_ExtensionList;
    uint32 _capacity;
    BOOL freeWhenDone;
}

+ (ExtensionList*)listWithRawRef:(CSSM_X509_EXTENSIONS*)ref freeWhenDone:(BOOL)fre;
+ (ExtensionList*)listWithExtensions:(NSArray*)arrayOfExtensions;

- (ExtensionList*)initWithExtensionListRef:(CSSM_X509_EXTENSIONS*)ref freeWhenDone:(BOOL)fre;
- (ExtensionList*)initWithExtensions:(NSArray*)arrayOfExtensions;

- (Extension*)extensionAtIndex:(uint32)index;
- (void)removeExtensionAtIndex:(uint32)index;
- (void)addExtension:(Extension*)entry;

- (uint32)numberOfExtensions;

- (NSEnumerator*)extensionEnumerator;

- (NSString*)description;

- (CSSM_X509_EXTENSIONS*)extensionListRef;

@end


@interface Extension : NSObject {
    CSSM_X509_EXTENSION *_Extension;
    BOOL freeWhenDone;
}

+ (Extension*)extensionWithRawRef:(CSSM_X509_EXTENSION*)ref freeWhenDone:(BOOL)fre;
+ (Extension*)extensionWithID:(const CSSM_OID*)extnId tagAndValue:(TagAndValue*)tagAndValue critical:(BOOL)isCritical;
+ (Extension*)extensionWithID:(const CSSM_OID*)extnId parsedValue:(NSData*)parsedValue critical:(BOOL)isCritical;
+ (Extension*)extensionWithID:(const CSSM_OID*)extnId pairValue:(Pair*)pairValue critical:(BOOL)isCritical;

- (Extension*)initWithExtensionRef:(CSSM_X509_EXTENSION*)ref freeWhenDone:(BOOL)fre;
- (Extension*)initWithID:(const CSSM_OID*)extnId tagAndValue:(TagAndValue*)tagAndValue critical:(BOOL)isCritical;
- (Extension*)initWithID:(const CSSM_OID*)extnId parsedValue:(NSData*)parsedValue critical:(BOOL)isCritical;
- (Extension*)initWithID:(const CSSM_OID*)extnId pairValue:(Pair*)pairValue critical:(BOOL)isCritical;

- (NSData*)extensionID;
- (void)setExtensionID:(NSData*)extensionID;

- (BOOL)critical;
- (void)setCritical:(BOOL)critical;

- (CSSM_X509EXT_DATA_FORMAT)dataFormat;
- (void)setDataFormat:(CSSM_X509EXT_DATA_FORMAT)format;

- (BOOL)isEncoded;
- (BOOL)isParsed;
- (void)setIsEncoded:(BOOL)enc;
- (void)setIsParsed:(BOOL)enc;

- (TagAndValue*)tagAndValue;
- (void)setTagAndValue:(TagAndValue*)tagAndValue;

- (void*)parsedValue;
- (void)setParsedValue:(void*)parsedValue;

- (Pair*)pairValue;
- (void)setPairValue:(Pair*)pairValue;

- (NSData*)BERvalue;

- (NSString*)description;

- (CSSM_X509_EXTENSION*)extensionRef;

@end


@interface Pair : NSObject {
    CSSM_X509EXT_PAIR *_Pair;
    BOOL freeWhenDone;
}

+ (Pair*)pairWithRawRef:(CSSM_X509EXT_PAIR*)ref freeWhenDone:(BOOL)fre;
+ (Pair*)pairWithTagAndValue:(TagAndValue*)tagAndValue value:(NSData*)value;

- (Pair*)initWithPairRef:(CSSM_X509EXT_PAIR*)ref freeWhenDone:(BOOL)fre;
- (Pair*)initWithTagAndValue:(TagAndValue*)tagAndValue value:(NSData*)value;

- (TagAndValue*)tagAndValue;
- (NSData*)value;

- (NSString*)description;

- (CSSM_X509EXT_PAIR*)pairRef;

@end


@interface TagAndValue : NSObject {
    CSSM_X509EXT_TAGandVALUE *_TagAndValue;
    BOOL freeWhenDone;
}

+ (TagAndValue*)tagAndValueWithRawRef:(CSSM_X509EXT_TAGandVALUE*)ref freeWhenDone:(BOOL)fre;
+ (TagAndValue*)tagAndValueWithType:(CSSM_BER_TAG)type value:(NSData*)value;

- (TagAndValue*)initWithTagAndValueRef:(CSSM_X509EXT_TAGandVALUE*)ref freeWhenDone:(BOOL)fre;
- (TagAndValue*)initWithType:(CSSM_BER_TAG)type value:(NSData*)value;

- (CSSM_BER_TAG)type;
- (NSData*)value;

- (NSString*)description;

- (CSSM_X509EXT_TAGandVALUE*)tagAndValueRef;

@end


@interface Validity : NSObject {
    CSSM_X509_VALIDITY *_Validity;
    BOOL freeWhenDone;
}

+ (Validity*)validityWithRawRef:(CSSM_X509_VALIDITY*)ref freeWhenDone:(BOOL)fre;
+ (Validity*)validityFrom:(Time*)from to:(Time*)to;

- (Validity*)initWithValidityRef:(CSSM_X509_VALIDITY*)ref freeWhenDone:(BOOL)fre;
- (Validity*)initFrom:(Time*)from to:(Time*)to;

- (Time*)from;
- (Time*)to;

- (BOOL)isCurrentlyValid;
- (BOOL)isValidAtDate:(NSDate*)date;

- (NSString*)description;

- (CSSM_X509_VALIDITY*)validityRef;

@end


@interface Time : NSObject {
    CSSM_X509_TIME *_Time;
    BOOL freeWhenDone;
}

+ (Time*)timeWithRawRef:(CSSM_X509_TIME*)ref freeWhenDone:(BOOL)fre;
+ (Time*)timeWithCalendarDate:(NSCalendarDate*)date format:(CSSM_BER_TAG)format;

- (Time*)initWithTimeRef:(CSSM_X509_TIME*)ref freeWhenDone:(BOOL)fre;
- (Time*)initWithCalendarDate:(NSCalendarDate*)date format:(CSSM_BER_TAG)format;

- (BOOL)isNullTime;
- (NSCalendarDate*)calendarDate;

- (NSString*)description;

- (CSSM_X509_TIME*)timeRef;

@end


@interface SPKInfo : NSObject {
    CSSM_X509_SUBJECT_PUBLIC_KEY_INFO *_SPKInfo;
    BOOL freeWhenDone;
}

+ (SPKInfo*)infoWithRawRef:(CSSM_X509_SUBJECT_PUBLIC_KEY_INFO*)ref freeWhenDone:(BOOL)fre;
+ (SPKInfo*)infoWithAlgorithm:(AlgorithmIdentifier*)algorithm keyData:(NSData*)data;

- (SPKInfo*)initWithSPKInfoRef:(CSSM_X509_SUBJECT_PUBLIC_KEY_INFO*)ref freeWhenDone:(BOOL)fre;
- (SPKInfo*)initWithAlgorithm:(AlgorithmIdentifier*)algorithm keyData:(NSData*)data;

- (AlgorithmIdentifier*)algorithm;
- (NSData*)keyData;

- (NSString*)description;

- (CSSM_X509_SUBJECT_PUBLIC_KEY_INFO*)infoRef;

@end


@interface TypeValuePair : NSObject {
    CSSM_X509_TYPE_VALUE_PAIR *_TypeValuePair;
    BOOL freeWhenDone;
}

+ (NSArray*)supportedTypes;

+ (TypeValuePair*)pairWithRawRef:(CSSM_X509_TYPE_VALUE_PAIR*)ref freeWhenDone:(BOOL)fre;
+ (TypeValuePair*)pairWithType:(const CSSM_OID*)type valueType:(CSSM_BER_TAG)valueType value:(NSData*)value;

+ (TypeValuePair*)pairFromType:(NSString*)type valueType:(CSSM_BER_TAG)valueType value:(NSData*)value;

+ (TypeValuePair*)pairForCommonName:(NSString*)value;
+ (TypeValuePair*)pairForOrganisation:(NSString*)value;
+ (TypeValuePair*)pairForCountry:(NSString*)value;
+ (TypeValuePair*)pairForState:(NSString*)value;
+ (TypeValuePair*)pairForSurname:(NSString*)value;
+ (TypeValuePair*)pairForSerialNumber:(uint32)value;
+ (TypeValuePair*)pairForLocality:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveStateName:(NSString*)value;
+ (TypeValuePair*)pairForStreetAddress:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveStreetAddress:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveOrganisationName:(NSString*)value;
+ (TypeValuePair*)pairForOrganisationalUnitName:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveOrganisationalUnitName:(NSString*)value;
+ (TypeValuePair*)pairForTitle:(NSString*)value;
+ (TypeValuePair*)pairForDescription:(NSString*)value;
+ (TypeValuePair*)pairForBusinessCategory:(NSString*)value;
+ (TypeValuePair*)pairForPostalAddress:(NSString*)value;
+ (TypeValuePair*)pairForCollectivePostalAddress:(NSString*)value;
+ (TypeValuePair*)pairForPostcode:(uint32)value;
+ (TypeValuePair*)pairForCollectivePostcode:(uint32)value;
+ (TypeValuePair*)pairForPostOfficeBox:(uint32)value;
+ (TypeValuePair*)pairForCollectivePostOfficeBox:(uint32)value;
+ (TypeValuePair*)pairForPhysicalDeliveryOfficeName:(NSString*)value;
+ (TypeValuePair*)pairForCollectivePhysicalDeliveryOfficeName:(NSString*)value;
+ (TypeValuePair*)pairForTelephoneNumber:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveTelephoneNumber:(NSString*)value;
+ (TypeValuePair*)pairForFaxNumber:(NSString*)value;
+ (TypeValuePair*)pairForCollectiveFaxNumber:(NSString*)value;
+ (TypeValuePair*)pairForName:(NSString*)value;
+ (TypeValuePair*)pairForGivenName:(NSString*)value;
+ (TypeValuePair*)pairForInitials:(NSString*)value;
+ (TypeValuePair*)pairForEmailAddress:(NSString*)value;
+ (TypeValuePair*)pairForUnstructuredName:(NSString*)value;
+ (TypeValuePair*)pairForUnstructuredAddress:(NSString*)value;

- (TypeValuePair*)initWithTypeValuePairRef:(CSSM_X509_TYPE_VALUE_PAIR*)ref freeWhenDone:(BOOL)fre;
- (TypeValuePair*)initWithType:(const CSSM_OID*)type valueType:(CSSM_BER_TAG)valueType value:(NSData*)value;

- (BOOL)isCommonName;
- (BOOL)isOrganisation;
- (BOOL)isCountry;
- (BOOL)isState;
- (BOOL)isSurname;
- (BOOL)isSerialNumber;
- (BOOL)isLocality;
- (BOOL)isCollectiveStateName;
- (BOOL)isStreetAddress;
- (BOOL)isCollectiveStreetAddress;
- (BOOL)isCollectiveOrganisationName;
- (BOOL)isOrganisationalUnitName;
- (BOOL)isCollectiveOrganisationalUnitName;
- (BOOL)isTitle;
- (BOOL)isDescription;
- (BOOL)isBusinessCategory;
- (BOOL)isPostalAddress;
- (BOOL)isCollectivePostalAddress;
- (BOOL)isPostalCode;
- (BOOL)isCollectivePostalCode;
- (BOOL)isPostOfficeBox;
- (BOOL)isCollectivePostOfficeBox;
- (BOOL)isPhysicalDeliveryOfficeName;
- (BOOL)isCollectivePhysicalDeliveryOfficeName;
- (BOOL)isTelephoneNumber;
- (BOOL)isCollectiveTelephoneNumber;
- (BOOL)isFaxNumber;
- (BOOL)isCollectiveFaxNumber;
- (BOOL)isName;
- (BOOL)isGivenName;
- (BOOL)isInitials;
- (BOOL)isEmailAddress;
- (BOOL)isUnstructuredName;
- (BOOL)isUnstructuredAddress;
- (BOOL)isObjectClass;
- (BOOL)isAliasedEntryName;
- (BOOL)isKnowledgeInformation;
- (BOOL)isSearchGuide;
- (BOOL)isTelexNumber;
- (BOOL)isCollectiveTelexNumber;
- (BOOL)isTelexTerminalIdentifier;
- (BOOL)isCollectiveTelexTerminalIdentifier;
- (BOOL)isX121Address;
- (BOOL)isInternationalISDNNumber;
- (BOOL)isCollectiveInternationalISDNNumber;
- (BOOL)isRegisteredAddress;
- (BOOL)isDestinationIndicator;
- (BOOL)isPreferredDeliveryMethod;
- (BOOL)isPresentationAddress;
- (BOOL)isSupportedApplicationContext;
- (BOOL)isMember;
- (BOOL)isOwner;
- (BOOL)isRoleOccupant;
- (BOOL)isSeeAlso;
- (BOOL)isUserPassword;
- (BOOL)isUserCertificate;
- (BOOL)isCACertificate;
- (BOOL)isAuthorityRevocationList;
- (BOOL)isCertificateRevocationList;
- (BOOL)isCrossCertificatePair;
- (BOOL)isGenerationQualifier;
- (BOOL)isUniqueIdentifier;
- (BOOL)isDNQualifier;
- (BOOL)isEnhancedSearchGuide;
- (BOOL)isProtocolInformation;
- (BOOL)isDistinguishedName;
- (BOOL)isUniqueMember;
- (BOOL)isHouseIdentifier;
- (BOOL)isContentType;
- (BOOL)isMessageDigest;
- (BOOL)isSigningTime;
- (BOOL)isCounterSignature;
- (BOOL)isChallengePassword;
- (BOOL)isExtendedCertificateAttributes;
- (BOOL)isQTCPS;
- (BOOL)isQTUNOTICE;

- (void)setType:(const CSSM_OID*)type;
- (const CSSM_OID*)type;

- (void)setValueType:(CSSM_BER_TAG)valueType;
- (CSSM_BER_TAG)valueType;

- (void)setValue:(NSData*)data;
- (NSData*)value;
- (const CSSM_DATA*)rawValue;

- (NSString*)description;

- (CSSM_X509_TYPE_VALUE_PAIR*)typeValuePairRef;

@end


@interface DistinguishedName : NSObject {
    CSSM_X509_RDN *_DistinguishedName;
    uint32 _capacity;
    BOOL freeWhenDone;
}

+ (DistinguishedName*)distinguishedNameWithRawRef:(CSSM_X509_RDN*)ref freeWhenDone:(BOOL)fre;
+ (DistinguishedName*)distinguishedNameWithTypeValuePairs:(NSArray*)arrayOfPairs;
+ (DistinguishedName*)distinguishedNameWithTypeValuePair:(TypeValuePair*)value;
//+ (DistinguishedName*)distinguishedNameWithCommonName:(NSString*)commonName organisation:(NSString*)organisation country:(NSString*)country state:(NSString*)state;

- (DistinguishedName*)initWithDistinguishedNameRef:(CSSM_X509_RDN*)ref freeWhenDone:(BOOL)fre;
- (DistinguishedName*)initWithTypeValuePairs:(NSArray*)arrayOfPairs;
- (DistinguishedName*)initWithTypeValuePair:(TypeValuePair*)value;
//- (DistinguishedName*)initWithCommonName:(NSString*)commonName organisation:(NSString*)organisation country:(NSString*)country state:(NSString*)state;

- (TypeValuePair*)firstPairForType:(const CSSM_OID*)type;

- (TypeValuePair*)entryAtIndex:(uint32)index;
- (void)removeEntryAtIndex:(uint32)index;
- (void)addEntry:(TypeValuePair*)entry;

- (uint32)numberOfEntries;

- (NSEnumerator*)fieldEnumerator;

- (NSString*)description;

- (CSSM_X509_RDN*)distinguishedNameRef;

@end


@interface NameList : NSObject {
    CSSM_X509_NAME *_NameList;
    uint32 _capacity;
    BOOL freeWhenDone;
}

+ (NameList*)nameListWithRawRef:(CSSM_X509_NAME*)ref freeWhenDone:(BOOL)fre;
+ (NameList*)nameListWithNames:(NSArray*)arrayOfNames;
+ (NameList*)nameListWithCommonName:(NSString*)commonName organisation:(NSString*)organisation country:(NSString*)country state:(NSString*)state;

- (NameList*)initWithNameListRef:(CSSM_X509_NAME*)ref freeWhenDone:(BOOL)fre;
- (NameList*)initWithNames:(NSArray*)arrayOfNames;
- (NameList*)initWithCommonName:(NSString*)commonName organisation:(NSString*)organisation country:(NSString*)country state:(NSString*)state;

- (TypeValuePair*)firstPairForType:(const CSSM_OID*)type;

- (DistinguishedName*)nameAtIndex:(uint32)index;
- (void)removeNameAtIndex:(uint32)index;
- (void)addName:(DistinguishedName*)entry;

- (uint32)numberOfNames;

- (NSEnumerator*)nameEnumerator;

- (NSString*)description;

- (CSSM_X509_NAME*)nameListRef;

@end


@interface DistinguishedNameEnumerator : NSEnumerator {
    DistinguishedName *_name;
    uint32 _index;
}

+ (DistinguishedNameEnumerator*)enumeratorForDistinguishedName:(DistinguishedName*)name;

- (DistinguishedNameEnumerator*)initWithDistinguishedName:(DistinguishedName*)name;

- (NSArray*)allObjects;
- (TypeValuePair*)nextObject;

@end


@interface NameListEnumerator : NSEnumerator {
    NameList *_list;
    uint32 _index;
}

+ (NameListEnumerator*)enumeratorForNameList:(NameList*)list;

- (NameListEnumerator*)initWithNameList:(NameList*)list;

- (NSArray*)allObjects;
- (DistinguishedName*)nextObject;

@end


@interface AlgorithmIdentifier : NSObject {
    CSSM_X509_ALGORITHM_IDENTIFIER *_AlgorithmIdentifier;
    BOOL freeWhenDone;
}

+ (AlgorithmIdentifier*)identifierWithRawRef:(CSSM_X509_ALGORITHM_IDENTIFIER*)ref freeWhenDone:(BOOL)fre;
+ (AlgorithmIdentifier*)identifierForAlgorithm:(CSSM_ALGORITHMS)algorithm;
+ (AlgorithmIdentifier*)identifierForOIDAlgorithm:(const CSSM_OID*)algorithm;

- (AlgorithmIdentifier*)initWithAlgorithmIdentifierRef:(CSSM_X509_ALGORITHM_IDENTIFIER*)ref freeWhenDone:(BOOL)fre;
- (AlgorithmIdentifier*)initForAlgorithm:(CSSM_ALGORITHMS)algorithm;
- (AlgorithmIdentifier*)initForOIDAlgorithm:(const CSSM_OID*)algorithm;

- (void)setAlgorithm:(const CSSM_OID*)algorithm;
- (const CSSM_OID*)algorithmOID;
- (CSSM_ALGORITHMS)algorithm;
    
- (void)setParameters:(NSData*)parameters;
- (NSData*)parameters;

- (NSString*)description;

- (CSSM_X509_ALGORITHM_IDENTIFIER*)algorithmIdentifierRef;

@end