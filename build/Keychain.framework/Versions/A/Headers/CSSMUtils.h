//
//  CSSMUtils.h
//  Keychain
//
//  Created by Wade Tregaskis on Thu Mar 13 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Security/Security.h>


/*! @function GUIDAsString
	@abstract Returns a CSSM GUID in human-readable string form.
	@discussion The exact format of the string returned is not strictly defined, but in general terms is the hex form of the GUID, possibly broken up into several words.
	@param GUID The GUID to render in string form.
	@result Returns the given GUID in string form. */

NSString* GUIDAsString(CSSM_GUID GUID);

/*! @function nameOfCertificateType
	@abstract Returns the human-readable name of a given certificate type.
	@discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_CERT_PGP returns "PGP".  The names are localised.
	@param certificateType The type to name as a string.
	@result Returns the name of the given type, or (localised) "Unknown" if an unknown type is provided. */

NSString* nameOfCertificateType(CSSM_CERT_TYPE certificateType);

/*! @function nameOfCertificateEncoding
	@abstract Returns the human-readable name of a given certificate encoding.
	@discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_CERT_ENCODING_BER returns "BER".  The names are localised.
	@param certificateEncoding The encoding to name as a string.
	@result Returns the name of the given encoding, or (localised) "Unknown" if an unknown encoding is provided. */

NSString* nameOfCertificateEncoding(CSSM_CERT_ENCODING certificateEncoding);

/*! @function nameOfBERCode
	@abstract Returns the human-readable name for a given BER code (or tag, if you like).
	@discussion The names returned are simple, short & human-readable.  e.g. the type BER_TAG_INTEGER returns "Integer".  The names are localised.
	@param tag The BER code to name as a string.
	@result Returns the name of the given BER code, or (localised) "Unknown" if an unknown encoding is provided. */

NSString* nameOfBERCode(CSSM_BER_TAG tag);

/*! @function stringRepresentationOfBEREncodedData
	@abstract Returns a human-readable representation of some given data of a given BER type.
	@discussion This method is a bit kludgy - it makes a best-effort guess as to what format the data is, based on the tag, and renders it in a form which should be generally suitable.  Ideally you should develop your own custom string rendering for BER data, to better suite your particular use.
	@param dat The data to render.  Should not be NULL.
	@param tag The BER tag indicating what type 'dat' is.
	@result Returns some appropriate representation where possible, a raw hex dump for unknown types.  Returns nil if an error occurs (e.g. 'dat' is NULL). */

NSString* stringRepresentationOfBEREncodedData(const CSSM_DATA *dat, CSSM_BER_TAG tag);

/*! @function nameOfOIDType
	@abstract Returns a localized, human-readable name of a given OID.
	@discussion If a name cannot be found for the given OID, it is returned as a hex string.
	@param type The OID to name.
	@result Returns the localized name of the OID, or if a name cannot be found a hex string of the OID's raw data.  Returns nil if an error occurs. */

NSString* nameOfOIDType(const CSSM_OID *type);

/*! @function CSSMDateForCalendarDate
    @abstract Returns the CSSM_DATE represenation of a given NSCalendarDate.
    @discussion Trivial conversion between NSCalendarDate and CSSM_DATE.  Note that the range of an NSCalendarDate is greater than that of a CSSM_DATE (which is limited to 0AD-9999AD (inclusive).  If the given date cannot be represented as a CSSM_DATE, the result returned will be nullified (i.e. every byte set to 0).
    @param date The date to represent as a CSSM_DATE.  Should not be nil.
    @result Returns the given date as a CSSM_DATE, or a nullified (all bytes set to zero) result if an error occurs (e.g. 'date' was nil, or outside the range representable by CSSM_DATE, etc). */

CSSM_DATE CSSMDateForCalendarDate(NSCalendarDate *date);

/*! @function calendarDateForCSSMDate
    @abstract Returns the NSCalendarDate representation of a given CSSM_DATE.
    @discussion Trivial conversion between a CSSM_DATE and an NSCalendarDate.  Since the range of NSCalendarDate is greater than that of CSSM_DATE, there should not be any mapping issues as there are for the CSSMDateForCalendarDate function.

                Note the degree of sanity checking performed by this function is not guaranteed - if you pass a date which purports to be the 35th day of the 27th month or somesuch, there is no guarantee this function will return an appropriate result (i.e. nil) - it may return a non-nil result who's value is undefined.
    @param date The date to convert.  Should not be nil.
    @result Returns an appropriate NSCalendarDate, or nil if an error occurs (such as 'date' being NULL, or otherwise invalid). */

NSCalendarDate* calendarDateForCSSMDate(const CSSM_DATE *date);

/*! @function calendarDateForTime
    @abstract Returns the NSCalendarDate representation of a given CSSM_X509_TIME.
    @discussion TODO - but note that the present implementation is NOT THREAD SAFE; it changes the NSTimeZone settings temporarily.
    @param time The time to convert.  Should not be NULL.
    @result Returns an NSCalendarDate representing the given time, or nil if an error occurs. */

NSCalendarDate* calendarDateForTime(const CSSM_X509_TIME *time);
void copyNSCalendarDateToTime(NSCalendarDate *date, CSSM_X509_TIME *time, CSSM_BER_TAG format);

NSString* nameOfKeyBlob(CSSM_KEYBLOB_TYPE type);
NSString* nameOfTypedFormat(CSSM_KEYBLOB_FORMAT format, CSSM_KEYBLOB_TYPE type);
NSString* nameOfAlgorithm(CSSM_ALGORITHMS algo);
NSString* nameOfKeyClass(CSSM_KEYCLASS class);
NSString* nameOfAlgorithmMode(CSSM_ENCRYPT_MODE mode);

NSString* namesOfAttributes(CSSM_KEYATTR_FLAGS attr);
NSString* namesOfUsages(CSSM_KEYUSE use);

NSString* subjectPublicKeyAsString(const CSSM_X509_SUBJECT_PUBLIC_KEY_INFO *key);
NSString* signatureAsString(const CSSM_X509_SIGNATURE *sig);

NSString* x509NameAsString(const CSSM_X509_NAME *name);
NSString* nameOfOIDAlgorithm(const CSSM_OID *oid);

NSString* nameOfDataFormat(CSSM_X509EXT_DATA_FORMAT format);

NSString* x509AlgorithmAsString(const CSSM_X509_ALGORITHM_IDENTIFIER *algo);
NSString* nameOfOIDAttribute(const CSSM_OID *oid);

NSString* nameOfOIDExtension(const CSSM_OID *oid);
NSString* extensionAsString(const CSSM_X509_EXTENSION *ext);
NSString* extensionsAsString(const CSSM_X509_EXTENSIONS *ext);

void intToDER(uint32 theInt, CSSM_DATA *data);
uint32 DERToInt(const CSSM_DATA *data);
NSData* NSDataForDERFormattedInteger(uint32 value);
