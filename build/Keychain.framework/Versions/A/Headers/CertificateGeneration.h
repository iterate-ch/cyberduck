//
//  CertificateGeneration.h
//  Keychain
//
//  Created by Wade Tregaskis on Tue May 27 2003.
//
//  Copyright (c) 2003, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Foundation/Foundation.h>
#import <Keychain/x509.h>
#import <Keychain/Key.h>
#import <Security/Security.h>
#import <Keychain/Certificate.h>


/*! @function createCertificateTemplate
    @abstract Creates a certificate template from a series of generic properties.
    @discussion This function takes the parameters given, some optional, and creates some data represeting a certificate, in a kind of template form.  This data can then be signed to create a full certificate.
    @param subject The name of the subject of the certificate - i.e. who it represents.  This parameter is required.
    @param issuer The name of the person or authority which will issue the certificate.  This parameter is required.
    @param validity The times when the certificate will be valid.  This parameter is optional.
    @param publicKey The public key of the subject of the certificate.  This is usually always the public key of the subject.  This parameter is required.
    @param signingAlgorithm The algorithm you intend to use to sign the certificate.  The signing algorithm includes a hash, so if you're using RSA, this algorithm should be something like SHA1 with RSA.  This parameter is required.
    @param serialNumber The serial number to be given to the certificate.  This parameter is optional.
    @param extensions A list of extensions to be included in the certificate.  This parameter is optional.
    @result Provided all the parameters are valid and the required ones present, a NSData instance containing the certificate template is returned.  Otherwise, nil is returned. */

NSData* createCertificateTemplate(NameList *subject, NameList *issuer, Validity *validity, Key *publicKey, AlgorithmIdentifier *signingAlgorithm, NSData *serialNumber, ExtensionList *extensions);

/*! @function signCertificate
    @abstract Signs a certificate template with a given private key.
    @discussion This function takes the certificate template supplied and signs it with the private key supplied, returning the signed certificate in raw data form.
    @param certificate The certificate template to be signed.  A certificate template can be generated using createCertificateTemplate.
    @param privateKey The private key with which to sign the certificate.  This should be the issuer's private key.
    @param signingAlgorithm The algorithm to sign the certificate with.  The signing procedure requires a hash to be performed, so the algorithm should be something like CSSM_ALGID_SHA1WithRSA, not CSSM_ALGID_RSA.  This should match the algorithm passed to createCertificateTemplate - indeed, you can retrieve the appropriate CSSM_ALGORITHMS code using [AlgorithmIdentifier algorithm].
    @result If the certificate template and private key are valid, the signed certificate's data is returned.  Otherwise, nil is returned. */

NSData* signCertificate(NSData *certificate, Key *privateKey, CSSM_ALGORITHMS signingAlgorithm);

/*! @function createCertificate
    @abstract Creates and signs a new certificate.
    @discussion This function simply provides a nice little wrapper around the createCertificateTemplate and signCertificate functions.
    @param subject The name of the subject of the certificate - i.e. who it represents.  This parameter is required.
    @param issuer The name of the person or authority which will issue the certificate.  This parameter is required and may not be nil.  To create a self-signed certificate, simply pass the subject name in for this parameter.
    @param validity The times when the certificate will be valid.  This parameter is optional.
    @param publicKey The public key of the subject of the certificate.  This is usually always the public key of the subject.  This parameter is required.
    @param privateKey The private key with which to sign the certificate.  This should be the issuer's private key.
    @param signingAlgorithm The algorithm you intend to use to sign the certificate.  The signing algorithm includes a hash, so if you're using RSA, this algorithm should be something like SHA1 with RSA.  This parameter is required.
    @param serialNumber The serial number to be given to the certificate.  This parameter is optional - if it is zero, it is ignored.
    @param extensions A list of extensions to be included in the certificate.  This parameter is optional.
    @result Returns a new Certificate instance, or nil if an error occurs. */

Certificate *createCertificate(NameList *subject, NameList *issuer, Validity *validity, Key *publicKey, Key *privateKey, AlgorithmIdentifier *signingAlgorithm, NSData *serialNumber, ExtensionList *extensions);