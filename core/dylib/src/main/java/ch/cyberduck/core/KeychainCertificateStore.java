package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.keychain.SecCertificateRef;
import ch.cyberduck.core.keychain.SecIdentityRef;
import ch.cyberduck.core.keychain.SecPolicyRef;
import ch.cyberduck.core.keychain.SecTrustRef;
import ch.cyberduck.core.keychain.SecTrustResultType;
import ch.cyberduck.core.keychain.SecurityFunctions;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.DEREncoder;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.ptr.PointerByReference;

public final class KeychainCertificateStore implements CertificateStore {
    private static final Logger log = LogManager.getLogger(KeychainCertificateStore.class);

    /**
     * @param certificates Chain of certificates
     * @return True if chain is trusted
     */
    @Override
    public boolean verify(final CertificateTrustCallback prompt, final String hostname, final List<X509Certificate> certificates) throws CertificateException {
        if(certificates.isEmpty()) {
            return false;
        }
        int err;
        // Specify true on the client side to return a policy for SSL server certificates
        final SecPolicyRef policyRef = SecurityFunctions.library.SecPolicyCreateSSL(true, hostname);
        final PointerByReference reference = new PointerByReference();
        err = SecurityFunctions.library.SecTrustCreateWithCertificates(toDEREncodedCertificates(certificates), policyRef, reference);
        if(0 != err) {
            log.error(String.format("SecTrustCreateWithCertificates returning error %d", err));
            return false;
        }
        final SecTrustRef trustRef = new SecTrustRef(reference.getValue());
        final SecTrustResultType trustResultType = new SecTrustResultType();
        err = SecurityFunctions.library.SecTrustEvaluate(trustRef, trustResultType);
        if(0 != err) {
            log.error(String.format("SecTrustEvaluate returning error %d", err));
            return false;
        }
        FoundationKitFunctions.library.CFRelease(trustRef);
        FoundationKitFunctions.library.CFRelease(policyRef);
        switch(trustResultType.getValue()) {
            case SecTrustResultType.kSecTrustResultUnspecified: // Implicitly trusted
            case SecTrustResultType.kSecTrustResultProceed: // Accepted by user keychain setting explicitly
                return true;
            default:
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Evaluated recoverable trust result failure %d", trustResultType.getValue()));
                }
                try {
                    prompt.prompt(hostname, certificates);
                    return true;
                }
                catch(ConnectionCanceledException e) {
                    return false;
                }
        }
    }

    @Override
    public X509Certificate choose(final CertificateIdentityCallback prompt, final String[] keyTypes, final Principal[] issuers, final Host bookmark) throws ConnectionCanceledException {
        final List<X509Certificate> certificates = new ArrayList<>();
        final CertificateStoreX509KeyManager manager = new KeychainX509KeyManager(prompt, bookmark, this).init();
        final String[] aliases = manager.getClientAliases(keyTypes, issuers);
        if(null == aliases) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found", Arrays.toString(issuers)));
        }
        for(String alias : aliases) {
            certificates.add(manager.getCertificate(alias, keyTypes, issuers));
        }
        return prompt.prompt(bookmark.getHostname(), certificates);
    }

    public static X509Certificate toX509Certificate(final SecIdentityRef identityRef) {
        final PointerByReference reference = new PointerByReference();
        int err;
        err = SecurityFunctions.library.SecIdentityCopyCertificate(identityRef, reference);
        if(0 != err) {
            log.error(String.format("SecIdentityCopyCertificate returning error %d", err));
            return null;
        }
        final SecCertificateRef certificateRef = new SecCertificateRef(reference.getValue());
        try {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final NSData dataRef = SecurityFunctions.library.SecCertificateCopyData(certificateRef);
            final X509Certificate selected = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(
                Base64.decodeBase64(dataRef.base64Encoding())));
            if(log.isDebugEnabled()) {
                log.info(String.format("Selected certificate %s", selected));
            }
            FoundationKitFunctions.library.CFRelease(certificateRef);
            return selected;
        }
        catch(CertificateException e) {
            log.error(String.format("Error %s creating certificate from reference", e));
            return null;
        }
    }

    public static NSArray toDEREncodedCertificates(final List<X509Certificate> certificates) {
        final NSMutableArray certs = NSMutableArray.arrayWithCapacity(new NSUInteger(certificates.size()));
        for(X509Certificate certificate : certificates) {
            try {
                final SecCertificateRef certificateRef = SecurityFunctions.library.SecCertificateCreateWithData(null,
                    NSData.dataWithBase64EncodedString(Base64.encodeBase64String(certificate.getEncoded())));
                if(null == certificateRef) {
                    log.error(String.format("Error creating converting from ASN.1 DER encoded certificate %s", certificate));
                    continue;
                }
                certs.addObject(certificateRef);
            }
            catch(CertificateEncodingException e) {
                log.error(String.format("Failure %s retrieving encoded certificate", e));
            }
        }
        return certs;
    }
}
