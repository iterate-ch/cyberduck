//
//  ABPersonAdditions.h
//  Keychain
//
//  Created by Wade Tregaskis on Fri Nov 14 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <AddressBook/AddressBook.h>
#import <Keychain/Certificate.h>


extern NSString* const kABCertificateProperty;                 // Certificate(s) (multi-data) - depreciated
extern NSString* const kABCertificateRefProperty;              // Certificate ref(s) (multi-date)
        extern NSString* const kABCertificateWorkLabel;        // Home certificate
        extern NSString* const kABCertificateHomeLabel;        // Work certificate
        extern NSString* const kABCertificatePortableLabel;    // Portable certificate


@interface ABPerson (ABPersonCertificateAdditions)

/*! @method primaryCertificates
    @abstract Returns the certificate(s) designated as primary for the receiver.
    @discussion See the Address Book documentation for information regarding primary and non-primary attributes.

                This method works in two steps.  In the first, it tries to locate certificates in the current user's default keychain(s) based on public key references stored in the Address Book (in the receiver).  It then falls back to looking for any certificates actually embedded into the Address Book, for the receiver.  This latter step is only for backwards compatibility and should not be used nor relied on.  See the documentation for addRawCertificate:label:primary: for more information.

                If no primary is designated, or an error occurs, nil is returned.  If there is only one entry, it is returned as the presumed primary.
    @result Nil if nothing found or an error occurs, otherwise an array of one or more certificates relating to the receiver. */

- (NSArray*)primaryCertificates;

/*! @method certificates
    @abstract Returns all the certificate(s) for the receiver.
    @discussion This method works in two steps.  In the first, it tries to locate certificates in the current user's default keychain(s) based on public key references stored in the Address Book (in the receiver).  It then falls back to looking for any certificates actually embedded into the Address Book, for the receiver.  This latter step is only for backwards compatibility and should not be used nor relied on.  See the documentation for addRawCertificate:label:primary: for more information.
    @result Nil if nothing found or an error occurs, otherwise an array of one or more certificates relating to the receiver. */

- (NSArray*)certificates;

/*! @method addRawCertificate:
    @abstract Adds a certificate to the receiver by embedding it in the Address Book.
    @discussion This method actually adds the given certificate into the Address Book, as raw data.  It can thus be reliably retrieved at any point in time, as there are no further dependencies.  However, this has the disadvantage that it fills the Address Book with large amounts of data, which is both unprotected and inaccessible to other security programs, whom may not use the Address Book.  Thus, use of this method is depreciated - use the addCertificate:label:primary: method instead.
    @param certificate The certificate to add (embed).
    @param label The label to give to the entry.
    @param primary YES if the given certificate should be made the primary, NO otherwise.
    @result YES if the add was successful, NO otherwise. */

- (BOOL)addRawCertificate:(Certificate*)certificate label:(NSString*)label primary:(BOOL)primary;

/*! @method addCertificate:
    @abstract Adds a certificate (by reference) the receiver.
    @discussion This method actually adds a reference to the public key in the given certificate, to the ultimate effect that the given certificate may be retrieved at some later date by matching the public key hashes.

                In end user terms, all you need do is call this method for each certificate you wish to add, and then make sure the certificate is available somewhere appropriate - e.g. in one of the current user's keychains.  The primaryCertificates and certificates methods will then deal with the details with regards to locating these again.

                Note that because a reference to the public key is kept, not the particular certificate in itself, after adding just one certificate with a given public key, all certificates for that public key will then be returned by the relevant methods.  This is considered a feature by design, since it makes sense on a practical level.  If you have any concerns with this, submit a bug or feature request, detailing what you find improper about this behaviour.

                Also note that there is no problem with adding multiple certificates for the same public key.  Only one reference to that particular public key will be added, as you would expect, and all adds will be successful.
    @param certificate The certificate to add (by reference).
    @param label The label to give to the given certificate.  Note that this is actually the label for the public key, in a sense.  If a public key reference already exists, but under a different label, then a new duplicate entry will be added with the given label.  This makes sense, since your "Friends" and "Family" certificates, for example, might be the same, while you still reserve a separate set for "Work".
    @param primary YES if the given certificate should be made the primary for the receiver, NO otherwise.
    @result YES if the certificate was added successfully (or was effectively already there), NO otherwise. */

- (BOOL)addCertificate:(Certificate*)certificate label:(NSString*)label primary:(BOOL)primary;

@end
