package ch.cyberduck.core.keychain;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.CFAllocatorRef;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSString;

import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public interface SecurityFunctions extends Library {
    SecurityFunctions library = Native.load(
        "Security", SecurityFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    /**
     * The certificate object returned by this function is used as input to other functions in the API.
     *
     * @param allocator The CFAllocator object you wish to use to allocate the certificate object. Pass NULL to use the
     *                  default allocator.
     * @param data      A DER (Distinguished Encoding Rules) representation of an X.509 certificate.
     * @return The newly created certificate object. Call the CFRelease function to release this object when you are
     * finished with it. Returns NULL if the data passed in the data parameter is not a valid DER-encoded X.509
     * certificate.
     */
    SecCertificateRef SecCertificateCreateWithData(CFAllocatorRef allocator, NSData data);

    /**
     * Creates a trust management object based on certificates and policies. The trust management object includes a
     * reference to the certificate to be verified, plus pointers to the policies to be evaluated for those
     * certificates. You can optionally include references to other certificates, including anchor certificates, that
     * you think might be in the certificate chain needed to verify the first (leaf) certificate. Any input certificates
     * that turn out to be irrelevant are harmlessly ignored. Call the SecTrustEvaluateWithError function to evaluate
     * the trust management object.
     * <p>
     * If you omit needed intermediate certificates from the certificates parameter, SecTrustEvaluateWithError searches
     * for certificates in the user’s keychain and in the system’s store of anchor certificates (see
     * SecTrustSetAnchorCertificates). You gain a significant performance benefit by passing in the entire certificate
     * chain, in order, in the certificates parameter.
     *
     * @param certificates The certificate to be verified, plus any other certificates you think might be useful for
     *                     verifying the certificate. The certificate to be verified must be the first in the array. If
     *                     you want to specify only one certificate, you can pass a SecCertificateRef object; otherwise,
     *                     pass an array of SecCertificateRef objects.
     * @param policies     References to one or more policies to be evaluated. You can pass a single SecPolicyRef
     *                     object, or an array of one or more SecPolicyRef objects. If you pass in multiple policies,
     *                     all policies must verify for the certificate chain to be considered valid. You typically use
     *                     one of the standard policies, like the one returned by SecPolicyCreateBasicX509.
     * @param trust        On return, points to the newly created trust management object. Call the CFRelease function
     *                     to release this object when you are finished with it.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustCreateWithCertificates(NSArray certificates, SecPolicyRef policies, PointerByReference trust);

    /**
     * This function evaluates a certificate’s validity to establish trust for a particular use—for example, in creating
     * a digital signature or to establish a Secure Sockets Layer connection.
     * <p>
     * It is not safe to call this function concurrently with any other function that uses the same trust management
     * object, or to re-enter this function for the same trust management object.
     * <p>
     * Because this function might look on the network for certificates in the certificate chain, the function might
     * block while attempting network access. You should never call it from your main thread; call it only from within a
     * function running on a dispatch queue or on a separate thread. Alternatively, in macOS, you can use
     * SecTrustEvaluateAsync from your main thread. In iOS, you can do the same thing using dispatch_once.
     *
     * @param trust  The trust management object to evaluate. A trust management object includes the certificate to be
     *               verified plus the policy or policies to be used in evaluating trust. It can optionally also include
     *               other certificates to be used in verifying the first certificate. Use the
     *               SecTrustCreateWithCertificates function to create a trust management object.
     * @param result On return, points to a result type reflecting the result of this evaluation. See SecTrustResultType
     *               for descriptions of possible values. See the discussion below for an explanation of how to handle
     *               specific values.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustEvaluate(SecTrustRef trust, SecTrustResultType result);

    /**
     * Returns a policy object for evaluating SSL certificate chains.
     *
     * @param server   Specify true on the client side to return a policy for SSL server certificates.
     * @param hostname If you specify a value for this parameter, the policy will require the specified value to match
     *                 the host name in the leaf certificate.
     * @return The policy object. Call the CFRelease function to release the object when you are finished with it.
     */
    SecPolicyRef SecPolicyCreateSSL(boolean server, String hostname);

    /**
     * Retrieves a certificate associated with an identity.
     *
     * @param identityRef    The identity object for the identity whose certificate you wish to retrieve.
     * @param certificateRef On return, points to the certificate object associated with the specified identity. Call
     *                       the CFRelease function to release this object when you are finished with it.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecIdentityCopyCertificate(SecIdentityRef identityRef, PointerByReference certificateRef);

    /**
     * Returns a DER representation of a certificate given a certificate object.
     *
     * @param certificate The certificate object for which you wish to return the DER (Distinguished Encoding Rules)
     *                    representation of the X.509 certificate.
     * @return The DER representation of the certificate. Call the CFRelease function to release this object when you
     * are finished with it. Returns NULL if the data passed in the certificate parameter is not a valid certificate
     * object.
     */
    NSData SecCertificateCopyData(SecCertificateRef certificate);

    /**
     * Returns the result code from the most recent trust evaluation.
     *
     * @param trust  The trust object from which results should be obtained
     * @param result A pointer that the function sets to point at a value that is the result type. See
     *               SecTrustResultType for possible values.
     * @return A result code. See Security Framework Result Codes. If the trust object has not yet been evaluated, the
     * result type is kSecTrustResultInvalid.
     */
    int SecTrustGetTrustResult(SecTrustRef trust, SecTrustResultType result);

    /**
     * Returns a string explaining the meaning of a security result code.
     *
     * @param status   A result code of type OSStatus returned by a security function. See Security Framework Result
     *                 Codes for a list of codes.
     * @param reserved Reserved for future use. Pass NULL for this parameter.
     * @return A human-readable string describing the result, or NULL if no string is available for the specified result
     * code. Call the CFRelease function to release this object when you are finished using it.
     */
    NSString SecCopyErrorMessageString(int status, Pointer reserved);

    /**
     * Returns a dictionary containing information about an evaluated trust.
     *
     * @param trust The evaluated trust.
     * @return A dictionary containing keys with values that describe the result of the trust evaluation, or NULL when
     * no information is available or if the trust has not been evaluated. See Trust Result Dictionary Keys for the list
     * of possible keys. Use CFRelease to free the dictionary's memory when you are done with it.
     */
    NSDictionary SecTrustCopyResult(SecTrustRef trust);

    int CSSM_CERT_STATUS_EXPIRED = 0x00000001;
    int CSSM_CERT_STATUS_NOT_VALID_YET = 0x00000002;
    int CSSM_CERT_STATUS_IS_IN_INPUT_CERTS = 0x00000004;
    int CSSM_CERT_STATUS_IS_IN_ANCHORS = 0x00000008;
    int CSSM_CERT_STATUS_IS_ROOT = 0x00000010;
    int CSSM_CERT_STATUS_IS_FROM_NET = 0x00000020;

    /**
     * Retrieves details on the outcome of a call to the function SecTrustEvaluate. You can call the
     * SFCertificateTrustPanel class in the SecurityInterface to display these results to the user.
     *
     * @param trustRef    A trust management object that has previously been sent to the SecTrustEvaluate function for
     *                    evaluation.
     * @param result      A pointer to the result type returned in the result parameter by the SecTrustEvaluate
     *                    function.
     * @param certChain   On return, points to an array of certificates that constitute the certificate chain used to
     *                    verify the input certificate. Call the CFRelease function to release this object when you are
     *                    finished with it.
     * @param statusChain On return, points to an array of CSSM_TP_APPLE_EVIDENCE_INFO structures, one for each
     *                    certificate in the certificate chain. The first item in the array corresponds to the leaf
     *                    certificate, and the last item corresponds to the anchor (assuming that verification of the
     *                    chain did not fail before reaching the anchor certificate). Each structure describes the
     *                    status of one certificate in the chain. This structure is defined in cssmapple.h. Do not
     *                    attempt to free this pointer; it remains valid until the trust management object is released
     *                    or until the next call to the function SecTrustEvaluate that uses this trust management
     *                    object.
     *                    <p>
     *                    typedef struct { CSSM_TP_APPLE_CERT_STATUS   StatusBits; uint32
     *                    NumStatusCodes; CSSM_RETURN                 *StatusCodes; uint32                      Index;
     *                    CSSM_DL_DB_HANDLE           DlDbHandle; CSSM_DB_UNIQUE_RECORD_PTR   UniqueRecord; }
     *                    CSSM_TP_APPLE_EVIDENCE_INFO;
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustGetResult(SecTrustRef trustRef, SecTrustResultType result, PointerByReference certChain, PointerByReference statusChain);
}
