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

import org.rococoa.ObjCObjectByReference;
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
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
     * Evaluates trust for the specified certificate and policies.
     *
     * @param trust The trust management object to evaluate. A trust management object includes the certificate to be
     *              verified plus the policy or policies to be used in evaluating trust. It can optionally also include
     *              other certificates to be used in verifying the first certificate. Use the
     *              SecTrustCreateWithCertificates function to create a trust management object.
     * @param error An error pointer the method uses to return an error when trust evaluation fails. Set to nil to
     *              ignore the error.
     * @return YES if the certificate is trusted; otherwise, NO.
     */
    boolean SecTrustEvaluateWithError(SecTrustRef trust, ObjCObjectByReference error);

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
    String SecCopyErrorMessageString(int status, Pointer reserved);

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
     *                    typedef struct { CSSM_TP_APPLE_CERT_STATUS   StatusBits; uint32 NumStatusCodes; CSSM_RETURN
     *                    *StatusCodes; uint32                      Index; CSSM_DL_DB_HANDLE DlDbHandle;
     *                    CSSM_DB_UNIQUE_RECORD_PTR   UniqueRecord; } CSSM_TP_APPLE_EVIDENCE_INFO;
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustGetResult(SecTrustRef trustRef, SecTrustResultType result, PointerByReference certChain, PointerByReference statusChain);

    /**
     * Sets option flags for customizing evaluation of a trust object.
     *
     * @param trust   The trust object to modify.
     * @param options The new set of option flags. For a list of options, see SecTrustOptionFlags.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustSetOptions(SecTrustRef trust, int options);

    /**
     * Reenables trusting built-in anchor certificates.
     *
     * @param trust                  The trust management object containing the certificate you want to evaluate. A
     *                               trust management object includes the certificate to be verified plus the policy or
     *                               policies to be used in evaluating trust. It can optionally also include other
     *                               certificates to be used in verifying the first certificate. Use the
     *                               SecTrustCreateWithCertificates function to create a trust management object.
     * @param anchorCertificatesOnly If true, disables trusting any anchors other than the ones passed in with the
     *                               SecTrustSetAnchorCertificates function.  If false, the built-in anchor certificates
     *                               are also trusted. If SecTrustSetAnchorCertificates is called and
     *                               SecTrustSetAnchorCertificatesOnly is not called, only the anchors explicitly passed
     *                               in are trusted.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecTrustSetAnchorCertificatesOnly(SecTrustRef trust, boolean anchorCertificatesOnly);

    /**
     * @param keychain          A reference to the keychain in which to store a generic password. Pass NULL to specify
     *                          the default keychain.
     * @param serviceNameLength The length of the serviceName character string.
     * @param serviceName       A UTF-8 encoded character string representing the service name.
     * @param accountNameLength The length of the accountName character string.
     * @param accountName       A UTF-8 encoded character string representing the account name.
     * @param passwordLength    On return, the length of the buffer pointed to by passwordData.
     * @param passwordData      On return, a pointer to a buffer that holds the password data. Pass NULL if you want to
     *                          obtain the item object but not the password data. In this case, you must also pass NULL
     *                          in the passwordLength parameter. You should use the SecKeychainItemFreeContent function
     *                          to free the memory pointed to by this parameter.
     * @param itemRef           On return, a pointer to a reference to the new keychain item. Pass NULL if you don’t
     *                          want to obtain this object. You must allocate the memory for this pointer. You must call
     *                          the CFRelease function to release this object when you are finished using it.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainAddGenericPassword(
        SecKeychainItemRef keychain,
        int serviceNameLength,
        byte[] serviceName,
        int accountNameLength,
        byte[] accountName,
        int passwordLength,
        byte[] passwordData,
        PointerByReference itemRef
    );

    /**
     * Adds a new Internet password to a keychain. This function adds a new Internet server password to the specified
     * keychain. Required parameters to identify the password are serverName and accountName (you cannot pass NULL for
     * both parameters). In addition, some protocols may require an optional securityDomain when authentication is
     * requested. This function optionally returns a reference to the newly added item.
     * <p>
     * This function sets the initial access rights for the new keychain item so that the application creating the item
     * is given trusted access.
     * <p>
     * This function automatically calls the function SecKeychainUnlock to display the Unlock Keychain dialog box if the
     * keychain is currently locked.
     *
     * @param keychain           A reference to an array of keychains to search, a single keychain or NULL to search the
     *                           user’s default keychain search list.
     * @param serviceNameLength  The length of the serviceName character string.
     * @param serviceName        A UTF-8 encoded character string representing the service name.
     * @param accountNameLength  The length of the accountName character string.
     * @param accountName        A UTF-8 encoded character string representing the account name.
     * @param pathLength         The length of the path character string.
     * @param path               A UTF-8 encoded character string representing the path.
     * @param port               The TCP/IP port number. If no specific port number is associated with this password,
     *                           pass 0.
     * @param protocolType       The protocol associated with this password. See SecProtocolType for a description of
     *                           possible values.
     * @param authenticationType The authentication scheme used. See SecAuthenticationType for a description of possible
     *                           values. Pass the constant kSecAuthenticationTypeDefault, to specify the default
     *                           authentication scheme.
     * @param passwordLength     On return, the length of the buffer pointed to by passwordData.
     * @param passwordData       On return, a pointer to a buffer that holds the password data. Pass NULL if you want to
     *                           obtain the item object but not the password data. In this case, you must also pass NULL
     *                           in the passwordLength parameter. You should use the SecKeychainItemFreeContent function
     *                           to free the memory pointed to by this parameter.
     * @param itemRef            A result code. See Security Framework Result Codes.
     * @return A result code. See Security Framework Result Codes. The result code errSecNoDefaultKeychain indicates
     * that no default keychain could be found. The result code errSecDuplicateItem indicates that you tried to add a
     * password that already exists in the keychain. The result code errSecDataTooLarge indicates that you tried to add
     * more data than is allowed for a structure of this type.
     */
    int SecKeychainAddInternetPassword(
        SecKeychainItemRef keychain,
        int serviceNameLength,
        byte[] serviceName,
        int securityDomainLength,
        byte[] securityDomain,
        int accountNameLength,
        byte[] accountName,
        int pathLength,
        byte[] path,
        int port,
        int protocolType,
        int authenticationType,
        int passwordLength,
        byte[] passwordData,
        PointerByReference itemRef
    );


    /**
     * Updates an existing keychain item after changing its attributes and/or data. If the keychain item has not
     * previously been added to a keychain, a call to this function does nothing and returns noErr.
     * <p>
     * Note that when you use this function to modify a keychain item, Keychain Services updates the modification date
     * of the item. Therefore, you cannot use this function to modify the modification date, as the value you specify
     * will be overwritten with the current time. If you want to change the modification date to something other than
     * the current time, use a CSSM function to do so.
     * <p>
     * You should pair the SecKeychainItemModifyContent function with the SecKeychainItemCopyContent function when
     * dealing with older Keychain Manager functions. The SecKeychainItemCopyAttributesAndData and
     * SecKeychainItemModifyAttributesAndData functions handle more attributes than are support by the old Keychain
     * Manager; however, passing them into older calls yields an invalid attribute error.
     *
     * @param itemRef        A reference to the keychain item to modify.
     * @param attrList       A pointer to the list of attributes to set and their new values. Pass NULL if you have no
     *                       need to modify attributes.
     * @param passwordLength The length of the buffer pointed to by the data parameter. Pass 0 if you pass NULL in the
     *                       data parameter.
     * @param passwordData   A pointer to a buffer containing the data to store. Pass NULL if you do not need to modify
     *                       the data.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainItemModifyContent(
        SecKeychainItemRef itemRef,
        Pointer/*SecKeychainAttributeList**/ attrList,
        int passwordLength,
        byte[] passwordData
    );

    /**
     * Finds the first generic password based on the attributes passed. This function finds the first generic password
     * item that matches the attributes you provide. Most attributes are optional; you should pass only as many as you
     * need to narrow the search sufficiently for your application’s intended use. This function optionally returns a
     * reference to the found item.
     * <p>
     * This function decrypts the password before returning it to you. If the calling application is not in the list of
     * trusted applications, the user is prompted before access is allowed. If the access controls for this item do not
     * allow decryption, the function returns the errSecAuthFailed result code.
     * <p>
     * This function automatically calls the function SecKeychainUnlock to display the Unlock Keychain dialog box if the
     * keychain is currently locked.
     *
     * @param keychainOrArray   A reference to an array of keychains to search, a single keychain or NULL to search the
     *                          user’s default keychain search list.
     * @param serviceNameLength The length of the serviceName character string.
     * @param serviceName       A UTF-8 encoded character string representing the service name.
     * @param accountNameLength The length of the accountName character string.
     * @param accountName       A UTF-8 encoded character string representing the account name.
     * @param passwordLength    On return, the length of the buffer pointed to by passwordData.
     * @param passwordData      On return, a pointer to a buffer that holds the password data. Pass NULL if you want to
     *                          obtain the item object but not the password data. In this case, you must also pass NULL
     *                          in the passwordLength parameter. You should use the SecKeychainItemFreeContent function
     *                          to free the memory pointed to by this parameter.
     * @param itemRef           A result code. See Security Framework Result Codes.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainFindGenericPassword(
        SecKeychainItemRef keychainOrArray,
        int serviceNameLength,
        byte[] serviceName,
        int accountNameLength,
        byte[] accountName,
        IntByReference passwordLength,
        PointerByReference passwordData,
        PointerByReference itemRef
    );

    /**
     * Finds the first Internet password based on the attributes passed. This function finds the first Internet password
     * item that matches the attributes you provide. This function optionally returns a reference to the found item.
     * <p>
     * This function decrypts the password before returning it to you. If the calling application is not in the list of
     * trusted applications, the user is prompted before access is allowed. If the access controls for this item do not
     * allow decryption, the function returns the errSecAuthFailed result code.
     * <p>
     * This function automatically calls the function SecKeychainUnlock to display the Unlock Keychain dialog box if the
     * keychain is currently locked.
     *
     * @param keychainOrArray      A reference to an array of keychains to search, a single keychain or NULL to search
     *                             the user’s default keychain search list.
     * @param serverNameLength     The length of the serverName character string.
     * @param serverName           A UTF-8 encoded character string representing the server name.
     * @param securityDomainLength The length of the securityDomain character string.
     * @param securityDomain       A UTF-8 encoded character string representing the security domain. This parameter is
     *                             optional, as not all protocols require it. Pass NULL if it is not required.
     * @param accountNameLength    The length of the accountName character string.
     * @param accountName          A UTF-8 encoded character string representing the account name.
     * @param pathLength           The length of the path character string.
     * @param path                 A UTF-8 encoded character string representing the path.
     * @param port                 The TCP/IP port number. Pass 0 to ignore the port number.
     * @param protocolType         The protocol associated with this password. See SecProtocolType for a description of
     *                             possible values.
     * @param authenticationType   The authentication scheme used. See SecAuthenticationType for a description of
     *                             possible values. Pass the constant kSecAuthenticationTypeDefault, to specify the
     *                             default authentication scheme.
     * @param passwordData         On return, a pointer to a buffer containing the password data. Pass NULL if you want
     *                             to obtain the item object but not the password data. In this case, you must also pass
     *                             NULL in the passwordLength parameter. You should use the SecKeychainItemFreeContent
     *                             function to free the memory pointed to by this parameter.
     * @param passwordLength       On return, the length of the buffer pointed to by passwordData.
     * @param itemRef              On return, a pointer to the item object of the Internet password. You are responsible
     *                             for releasing your reference to this object. Pass NULL if you don’t want to obtain
     *                             this object.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainFindInternetPassword(
        SecKeychainItemRef keychainOrArray,
        int serverNameLength,
        byte[] serverName,
        int securityDomainLength,
        byte[] securityDomain,
        int accountNameLength,
        byte[] accountName,
        int pathLength,
        byte[] path,
        int port,
        int protocolType,
        int authenticationType,
        IntByReference passwordLength,
        PointerByReference passwordData,
        PointerByReference itemRef
    );

    /**
     * Deletes a keychain item from the default keychain’s permanent data store.
     *
     * @param itemRef A keychain item object of the item to delete. You must call the CFRelease function to release this
     *                object when you are finished using it.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainItemDelete(Pointer itemRef);

    /**
     * Releases the memory used by the keychain attribute list and the keychain data retrieved in a call to the
     * SecKeychainItemCopyContent function.
     *
     * @param attrList A pointer to the attribute list to release. Pass NULL if there is no attribute list to release.
     * @param data     A pointer to the data buffer to release. Pass NULL if there is no data to release.
     * @return A result code. See Security Framework Result Codes.
     */
    int SecKeychainItemFreeContent(Pointer attrList, Pointer data);

    int kSecProtocolTypeFTP = 0x66747020;
    int kSecProtocolTypeFTPAccount = 0x66747061;
    int kSecProtocolTypeHTTP = 0x68747470;
    int kSecProtocolTypeIRC = 0x69726320;
    int kSecProtocolTypeNNTP = 0x6E6E7470;
    int kSecProtocolTypePOP3 = 0x706F7033;
    int kSecProtocolTypeSMTP = 0x736D7470;
    int kSecProtocolTypeSOCKS = 0x736F7820;
    int kSecProtocolTypeIMAP = 0x696D6170;
    int kSecProtocolTypeLDAP = 0x6C646170;
    int kSecProtocolTypeAppleTalk = 0x61746C6B;
    int kSecProtocolTypeAFP = 0x61667020;
    int kSecProtocolTypeTelnet = 0x74656C6E;
    int kSecProtocolTypeSSH = 0x73736820;
    int kSecProtocolTypeFTPS = 0x66747073;
    int kSecProtocolTypeHTTPS = 0x68747073;
    int kSecProtocolTypeHTTPProxy = 0x68747078;
    int kSecProtocolTypeHTTPSProxy = 0x68747378;
    int kSecProtocolTypeFTPProxy = 0x66747078;
    int kSecProtocolTypeCIFS = 0x63696673;
    int kSecProtocolTypeSMB = 0x736D6220;
    int kSecProtocolTypeRTSP = 0x72747370;
    int kSecProtocolTypeRTSPProxy = 0x72747378;
    int kSecProtocolTypeDAAP = 0x64616170;
    int kSecProtocolTypeEPPC = 0x65707063;
    int kSecProtocolTypeIPP = 0x69707020;
    int kSecProtocolTypeNNTPS = 0x6E747073;
    int kSecProtocolTypeLDAPS = 0x6C647073;
    int kSecProtocolTypeTelnetS = 0x74656C73;
    int kSecProtocolTypeIMAPS = 0x696D7073;
    int kSecProtocolTypeIRCS = 0x69726373;
    int kSecProtocolTypePOP3S = 0x706F7073;
    int kSecProtocolTypeCVSpserver = 0x63767370;
    int kSecProtocolTypeSVN = 0x73766E20;
    int kSecProtocolTypeAny = 0x00000000;

    int kSecAuthenticationTypeNTLM = 0x6D6C746E;
    int kSecAuthenticationTypeMSN = 0x616E736D;
    int kSecAuthenticationTypeDPA = 0x61617064;
    int kSecAuthenticationTypeRPA = 0x61617072;
    int kSecAuthenticationTypeHTTPBasic = 0x70747468;
    int kSecAuthenticationTypeHTTPDigest = 0x64747468;
    int kSecAuthenticationTypeHTMLForm = 0x6D726F66;
    int kSecAuthenticationTypeDefault = 0x746C6664;
    int kSecAuthenticationTypeAny = 0x00000000;

    /**
     * The item cannot be found.
     */
    int errSecItemNotFound = -25300;
    /**
     * No error.
     */
    int errSecSuccess = 0;

    /**
     * The system considers an item to be a duplicate for a given keychain when that keychain already has an item of the
     * same class with the same set of composite primary keys. Each class of keychain item has a different set of
     * primary keys, although a few attributes are used in common across all classes. In particular, where applicable,
     * kSecAttrSynchronizable and kSecAttrAccessGroup are part of the set of primary keys. The additional per-class
     * primary keys are listed below:
     * <p>
     * For generic passwords, the primary keys include kSecAttrAccount and kSecAttrService.
     * <p>
     * For internet passwords, the primary keys include kSecAttrAccount, kSecAttrSecurityDomain, kSecAttrServer,
     * kSecAttrProtocol, kSecAttrAuthenticationType, kSecAttrPort, and kSecAttrPath.
     * <p>
     * For certificates, the primary keys include kSecAttrCertificateType, kSecAttrIssuer, and kSecAttrSerialNumber.
     * <p>
     * For key items, the primary keys include kSecAttrKeyClass, kSecAttrKeyType, kSecAttrApplicationLabel,
     * kSecAttrApplicationTag, kSecAttrKeySizeInBits, and kSecAttrEffectiveKeySize.
     * <p>
     * For identity items, which are a certificate and a private key bundled together, the primary keys are the same as
     * for a certificate. Because a private key may be certified more than once, the uniqueness of the certificate
     * determines that of the identity.
     */
    int errSecDuplicateItem = -25299;
}
