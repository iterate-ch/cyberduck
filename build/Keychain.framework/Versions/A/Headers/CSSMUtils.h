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
	@param GUID The GUID to render in string form.  Should not be NULL.
	@result Returns the given GUID in string form, or nil if an error occurs (such as the argument being invalid). */

NSString* GUIDAsString(const CSSM_GUID *GUID);

/*! @function OIDAsString
    @abstract Returns a CSSM OID in human-readable string form.
    @discussion The form of the returned string is "A, B, C, D, ..." where each field is the unsigned value of each byte in the OID.  e.g. the result for OID_OIW_DSAWithSHA1 is "43, 14, 2, 2, 27".
    @param OID The OID to render in string form.  Should not be NULL.
    @result Returns the given OID in string form, or nil if an error occurs (such as the argument being invalid). */

NSString* OIDAsString(const CSSM_OID *OID);

/*! @function nameOfGUID
    @abstract Returns the human-readable name of a given GUID.
    @discussion The human readable name is something simple like "Apple CSP".
    @param GUID The GUID to name.  Should not be NULL.
    @result Returns the human-readable name of the given GUID, or "Unknown (X)" - where X is the GUID rendered in the form provided by GUIDAsString - if the GUID is not a known value. */

NSString* nameOfGUID(const CSSM_GUID *GUID);

/*! @function nameOfOID
    @abstract Returns the human-readable name of a given OID.
    @discussion The human readable name is something simple like "SHA1 with DSA".
    @param OID The OID to name.  Should not be NULL.
    @result Returns the human-readable name of the given OID, or "Unknown (X)" - where X is the OID rendered in the form provided by OIDAsString - if the OID is not a known value. */

NSString* nameOfOID(const CSSM_OID *OID);

/*! @function nameOfCertificateType
	@abstract Returns the human-readable name of a given certificate type.
	@discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_CERT_PGP returns "PGP".  The names are localised.
	@param certificateType The type to name as a string.
	@result Returns the name of the given type, or (localised) "Unknown (X)" - where X is the integer value of the given type constant - if an unknown type is provided. */

NSString* nameOfCertificateType(CSSM_CERT_TYPE certificateType);

/*! @function nameOfCertificateEncoding
	@abstract Returns the human-readable name of a given certificate encoding.
	@discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_CERT_ENCODING_BER returns "BER".  The names are localised.
	@param certificateEncoding The encoding to name as a string.
	@result Returns the name of the given encoding, or (localised) "Unknown (X)" - where X is the integer value of the given encoding constant - if an unknown encoding is provided. */

NSString* nameOfCertificateEncoding(CSSM_CERT_ENCODING certificateEncoding);

/*! @function nameOfCRLType
    @abstract Returns the human-readable name of a given CRL type.
    @discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_CRL_TYPE_X_509v2 returns "X.509v2".  The names are localised.
    @param type The type to name as a string.
@result Returns the name of the given type, or (localised) "Unknown (X)" - where X is the integer value of the given type constant - if an unknown type is provided. */

NSString* nameOfCRLType(CSSM_CRL_TYPE type);

/*! @function nameOfCRLEncoding
    @abstract Returns the human-readable name of a given CRL encoding.
    @discussion The names returned are simple, short & human-readable.  e.g. the encoding CSSM_CRL_ENCODING_DER returns "DER".  The names are localised.
    @param encoding The encoding to name as a string.
    @result Returns the name of the given encoding, or (localised) "Unknown (X)" - where X is the integer value of the given encoding constant - if an unknown encoding is provided. */

NSString* nameOfCRLEncoding(CSSM_CRL_ENCODING encoding);

/*! @function nameOfBERCode
	@abstract Returns the human-readable name for a given BER code (or tag, if you like).
	@discussion The names returned are simple, short & human-readable.  e.g. the type BER_TAG_INTEGER returns "Integer".  The names are localised.
	@param tag The BER code to name as a string.
	@result Returns the name of the given BER code, or (localised) "Unknown (X)" - where X is the integer value of the given tag constant - if an unknown code is provided. */

NSString* nameOfBERCode(CSSM_BER_TAG tag);

/*! @function CSSMErrorAsString
    @abstract Returns a human-readable name of a given CSSM error code, possibly with a short (one-line) description of the error.
    @discussion When displaying errors to the user you should always provide both a code and the name of the error - the code can be easily copied down for reporting back to you, the developer, while the name may provide some aid to the user in determining what the problem is; e.g. if it is "CL CRL already signed", they may be able to deduce that they are trying to re-sign an existing CRL, instead of a new one, or something similar.

                This function looks up the strings in an appropriate strings table, and as such will return localised names.  At time of writing the only localisation supported is English.
    @param error The CSSM error code.
    @result Returns a human-readable string containing at least the name of the error code, and possibly also a very brief description of the error.  Returns "Unknown (XXX)" - where XXX is the integer error code - for any unknown error codes (suitably localised, of course). */

NSString* CSSMErrorAsString(CSSM_RETURN error);

/*! @function stringRepresentationOfBEREncodedData
	@abstract Returns a human-readable representation of some given data of a given BER type.
	@discussion This method is a bit kludgy - it makes a best-effort guess as to what format the data is, based on the tag, and renders it in a form which should be generally suitable.  Ideally you should develop your own custom string rendering for BER data, to better suite your particular use.
	@param dat The data to render.  Should not be NULL.
	@param tag The BER tag indicating what type 'dat' is.
	@result Returns some appropriate representation where possible, a raw hex dump for unknown types.  Returns nil if an error occurs (e.g. 'dat' is NULL). */

NSString* stringRepresentationOfBEREncodedData(const CSSM_DATA *dat, CSSM_BER_TAG tag);

/*! @function nameOfOIDType
	@abstract Returns a localized, human-readable name of a given OID.
	@discussion This function is deprecated - please use the nameOfOID function instead.  It remains for compatibility reasons for the time being, but will eventually be removed.
	@param type The OID to name.  Should not be NULL.
	@result Returns the localized name of the OID, or "Unknown (X)" - where X is the OID rendered in the form returned by OIDAsString - if a name cannot be found.  Returns nil if an error occurs. */

#define nameOfOIDType(type) nameOfOID(type)

/*! @function CSSMDateForCalendarDate
    @abstract Returns the CSSM_DATE represenation of a given NSCalendarDate.
    @discussion Trivial conversion between NSCalendarDate and CSSM_DATE.  Note that the range of an NSCalendarDate is greater than that of a CSSM_DATE (which is limited to 0AD-9999AD (inclusive).  If the given date cannot be represented as a CSSM_DATE, the result returned will be nullified (i.e. every byte set to 0).
                                                                                                                                                        
                A few things worth noting about CSSM_DATEs - they don't have any higher precision than days, and they're in GMT time.  So you'll lose hours/minutes/seconds/etc, and you may find the result appears to be for a different day.  Don't worry, the shift is reversed if you use calendarDateForCSSMDate.
    @param date The date to represent as a CSSM_DATE.  Should not be nil.
    @result Returns the given date as a CSSM_DATE, or a nullified (all bytes set to zero) result if an error occurs (e.g. 'date' was nil, or outside the range representable by CSSM_DATE, etc). */

CSSM_DATE CSSMDateForCalendarDate(NSCalendarDate *date);

/*! @function calendarDateForCSSMDate
    @abstract Returns the NSCalendarDate representation of a given CSSM_DATE.
    @discussion Trivial conversion between a CSSM_DATE and an NSCalendarDate.  Since the range of NSCalendarDate is greater than that of CSSM_DATE, there should not be any mapping issues as there are for the CSSMDateForCalendarDate function.

                Note the degree of sanity checking performed by this function is not guaranteed - if you pass a date which purports to be the 35th day of the 27th month or somesuch, there is no guarantee this function will return an appropriate result (i.e. nil) - it may return a non-nil result who's value is undefined.

                Also note that while CSSM_DATEs are always GMT, the result is converted to and returned in the current default time zone.  You can use NSCalendarDate's setTimeZone: method to override this with whatever you like.
    @param date The date to convert.  Should not be nil.
    @result Returns an appropriate NSCalendarDate, or nil if an error occurs (such as 'date' being NULL, or otherwise invalid). */

NSCalendarDate* calendarDateForCSSMDate(const CSSM_DATE *date);

/*! @function calendarDateForTime
    @abstract Returns the NSCalendarDate representation of a given CSSM_X509_TIME.
    @discussion This function supports all valid BER generalized time and UTC time representations.  How it handles invalid ones is undefined for all but the most extreme cases, but if an error <i>is</i> detected nil is returned.

                The NSCalendarDate returned has it's timezone set to the default time zone.
    @param time The time to convert.  Should not be NULL, nor contain a NULL data reference or length <= 0.
    @result Returns an NSCalendarDate representing the given time, or nil if an error occurs. */

NSCalendarDate* calendarDateForTime(const CSSM_X509_TIME *time);

/*! @function timeForNSCalendarDate
    @abstract Returns a CSSM_X509_TIME representing a given calendar date.
    @discussion The returned date is written in the smallest valid form (for the given format), but always assumes the most accurate form possible.  For example, if seconds is 0 for the given date, the seconds field may be left out in formats which allow it.  If fractional seconds is non-zero, then both seconds & fractional seconds will be included (for BER_TAG_GENERALIZED_TIME; BER_TAG_UTC_TIME does not support fractional seconds), regardless of the value of seconds.  And so forth.  Similarly, if the time zone is GMT the returned date will not use full "+HHMM" form, but rather will simply suffix the date with 'Z' (or, for BER_TAG_GENERALIZED_TIME, nothing at all, which is equivalent to 'Z' by definition).
    @param date The date to represent as a CSSM_X509_TIME.  Should not be nil.
    @param format The format to represent the date & time in.  Currently BER_TAG_GENERALIZED_TIME & BER_TAG_UTC_TIME are supported.  Should be a supported format.
    @result Returns a CSSM_X509_TIME struct.  If the date could be represented without error, the 'time' field of the result will be filled in appropriately (with appropriate 'Length' and 'Data' [allocated using malloc; must be freed by caller]).  If an error does occur, the 'time' field will be nullified - 'Data' will be set to NULL and 'Length' to 0. */

CSSM_X509_TIME timeForNSCalendarDate(NSCalendarDate *date, CSSM_BER_TAG format);

/*! @function nameOfKeyBlob
    @abstract Returns the human-readable name of a given keyblob type.
    @discussion The names returned are simple, short & human-readable.  e.g. the type CSSM_KEYBLOB_RAW returns "Raw".  The names are localised.
    @param type The keyblob type.
    @result Returns the name of the given keyblob type, or (localised) "Unknown (X)" - where X is the type as an integer value - if an unknown encoding is provided. */

NSString* nameOfKeyBlob(CSSM_KEYBLOB_TYPE type);

/*! @function nameOfTypedFormat
    @abstract Returns the human-readable name of a given keyblob format, given a particular keyblob type.
    @discussion The names returned are simple, short & human-readable.  e.g. the format CSSM_KEYBLOB_RAW_FORMAT_PKCS3 (with type CSSM_KEYBLOB_RAW) returns "RSA PKCS3 (v1.5)".  The names are localised.
    @param format The keyblob format.
    @param type The keyblob type.  This is required because the formats are numbered in sets based on their type - i.e. CSSM_KEYBLOB_RAW_FORMAT_PKCS3 has the same value as CSSM_KEYBLOB_WRAPPED_FORMAT_PKCS7 and CSSM_KEYBLOB_REF_FORMAT_SPKI.  Ensure you do not confuse the type, as the results may be very misleading.
    @result Returns the name of the given keyblob format, or (localised) "Unknown (X-Y)" - where X is the format and Y the type as an integer value - if an unknown type and/or format is provided. */

NSString* nameOfTypedFormat(CSSM_KEYBLOB_FORMAT format, CSSM_KEYBLOB_TYPE type);

/*! @function nameOfAlgorithm
    @abstract Returns the human-readable name of a given algorithm.
    @discussion The names returned are simple, short & human-readable.  e.g. the algorithm CSSM_ALGID_SHA1 returns "SHA1".  The names are localised.
    @param algo The algorithm.
    @result Returns the name of the given algorithm, or (localised) "Unknown (X)" - where X is the algorithm as an integer value - if an unknown algorithm is provided. */

NSString* nameOfAlgorithm(CSSM_ALGORITHMS algo);

/*! @function nameOfKeyClass
    @abstract Returns the human-readable name of a given key class.
    @discussion The names returned are simple, short & human-readable.  e.g. the class CSSM_KEYCLASS_PRIVATE_KEY returns "Private key".  The names are localised.
    @param keyClass The key class.
    @result Returns the name of the given key class, or (localised) "Unknown (X)" - where X is the key class as an integer value - if an unknown class is provided. */

NSString* nameOfKeyClass(CSSM_KEYCLASS keyClass);

/*! @function nameOfAlgorithmMode
    @abstract Returns the human-readable name of a given algorithm mode.
    @discussion The names returned are simple, short & human-readable.  e.g. the mode CSSM_ALGMODE_CFBPadIV8 returns "CFB Pad IV8".  The names are localised.
    @param mode The algorithm mode.
    @result Returns the name of the given mode, or (localised) "Unknown (X)" - where X is the mode as an integer value - if an unknown mode is provided. */

NSString* nameOfAlgorithmMode(CSSM_ENCRYPT_MODE mode);

/*! @function namesOfAttributes
    @abstract Returns an NSString naming the set of given attributes.
    @discussion Returns a string with the names of all the attributes set in the given bit field, separate by commas as necessary.  For example, an 'attr' parameter of 0x20000025 will return the string "Permanent, Modifiable, Extractable, Return Reference".  Any unknown values will be represented as their raw hex values, e.g. for 'attr' of 0x21000025 the result will be "Permanent, Modifiable, Extractable, Unknown (0x01000000), Return Reference".
    @param attr The attributes to name.
    @result Returns a composed string as detailed in the discussion, above. */

NSString* namesOfAttributes(CSSM_KEYATTR_FLAGS attr);

/*! @function namesOfUsages
    @abstract Returns an NSString naming the set of given key usages.
    @discussion Returns a string with the names of all the usages set in the given bit field, separate by commas as necessary.  For example, a 'use' parameter of 0x000000c3 will return the string "Encrypt, Decrypt, Wrap, Unwrap".  Any unknown values will be represented as their raw hex values, e.g. for a 'use' of 0x000001c3 the result will be "Encrypt, Decrypt, Wrap, Unwrap, Unknown (0x00000100)".
    @param use The key usages to name.
    @result Returns a composed string as detailed in the discussion, above. */

NSString* namesOfUsages(CSSM_KEYUSE use);

/*! @function subjectPublicKeyAsString
    @abstract Returns a given public key in a [relatively] human-readable form.
    @discussion The result states the algorithm used with the public key, and the raw data of the public key itself.  It is human-readable in so far as the key data is rendered in hex form, but since the keys are typically very long, it is rarely of much use to a normal end-user.  Useful for debugging to manually compare two public keys.
    @param key A pointer to the public key info to render.  Should not be NULL.
    @result Returns an NSString describing the given X509 public key info, or nil if an error occurs (e.g. 'key' is NULL). */

NSString* subjectPublicKeyAsString(const CSSM_X509_SUBJECT_PUBLIC_KEY_INFO *key);

/*! @function signatureAsString
    @abstract Returns a given X509 signature in a [relatively] human-readable form.
    @discussion The result states the algorithm used for the signature, and the raw signature data itself.  It is human-readable in so far as the signature data is rendered in hex form, but since this data is typically very long, it is rarely of much use to a normal end-user.  Useful for debugging to manually compare two signatures, or similar.
    @param sig The signature to render.  Should not be NULL.
    @result Returns an NSString describing the given X509 signature, or nil if an error occurs (e.g. 'sig' is NULL). */

NSString* signatureAsString(const CSSM_X509_SIGNATURE *sig);

/*! @function x509NameAsString
    @abstract Returns an X509 name in string form.
    @discussion The return format for this function is undefined at present, and likely to change in future.
    @param name The name to render.  Should not be NULL.
    @result The given X509 as an NSString, or nil if an error occurs. */

NSString* x509NameAsString(const CSSM_X509_NAME *name);

/*! @function nameOfOIDAlgorithm
    @abstract Returns a localized, human-readable name of a given OID.
    @discussion This function is deprecated - please use the nameOfOID function instead.  It remains for compatibility reasons for the time being, but will eventually be removed.
    @param type The OID to name.  Should not be NULL.
    @result Returns the localized name of the OID, or "Unknown (X)" - where X is the OID rendered in the form returned by OIDAsString - if a name cannot be found.  Returns nil if an error occurs. */

#define nameOfOIDAlgorithm(oid) nameOfOID(oid)

/*! @function nameOfExtensionFormat
    @abstract Returns a localized, human-readable name of a given extension data format.
    @discussion The returned name is a simple single-word name, e.g. "Pair" for CSSM_X509_DATAFORMAT_PAIR.
    @param format The format to name.
    @result Returns the localized name of the given extension data format, or "Unknown (X)" - where X is the numerical value of the given format - if the format is not known. */

NSString* nameOfExtensionFormat(CSSM_X509EXT_DATA_FORMAT format);

/*! @function x509AlgorithmAsString
    @abstract Returns a human-readable description of an X509 Algorithm Identifier.
    @discussion The returned value states the name of the algorithm used, and expresses it's parameters (if any) in hexadecimal form for human viewing.
    @param algo The algorithm to render.  Should not be NULL.
    @result Returns the given algorithm rendered in human-readable string form, or nil if an error occurs. */

NSString* x509AlgorithmAsString(const CSSM_X509_ALGORITHM_IDENTIFIER *algo);

/*! @function extensionAsString
    @abstract Returns a human-readable description of an X509 extension.
    @discussion The returned format is undefined and liable to change in future.  The most basic format (for any extensions not specially recognised) expresses the extension name, flags and data in raw form.  Whether or not extension-specific forms are used is not strictly defined, but there is no such special handling at time of writing.
    @param ext The extension to render.  Should not be NULL.
    @result Returns the given extension rendered as a human-readable string, or nil if an error occurs. */

NSString* extensionAsString(const CSSM_X509_EXTENSION *ext);

/*! @function extensionsAsString
    @abstract Returns a human-readable description of a series of X509 extensions.
    @discussion The returned format is undefined and liable to change in future.  This function uses extensionAsString to render each individual extension; consequently, see the documentation for extensionAsString for more details.
    @param ext The extensions to render.  Should not be NULL.
    @result Returns the given extension(s) rendered as a human-readable string, or nil if an error occurs. */

NSString* extensionsAsString(const CSSM_X509_EXTENSIONS *ext);

/*! @function intToDER
    @abstract Converts an unsigned 32-bit integer to it's DER data form.
    @param theInt The integer to render in DER form.
    @param data A pointer to the CSSM_DATA in which to store the result.  Should not be NULL.
    @result Returns YES if the conversion was successful, NO otherwise. */

BOOL intToDER(uint32_t theInt, CSSM_DATA *data);

/*! @function DERToInt
    @abstract Reads a DER-formatted integer and returns it's value.
    @param data The DER integer, rendered in DER form.  Should not be NULL.
    @param result On output the resulting integer value.  Should not be NULL.
    @result Returns YES if successful, NO otherwise. */

BOOL DERToInt(const CSSM_DATA *data, uint32_t *result);

/*! @function NSDataForDERFormattedInteger
    @abstract Returns an NSData instance containing the DER-formatted form of a given integer.
    @discussion This function is obsoleted.  Please don't use it in new code - it may be removed in future versions.
    @param value The value to render.
    @result Returns the resulting DER form integer as an NSData, or nil if an error occurs. */

NSData* NSDataForDERFormattedInteger(uint32_t value);
