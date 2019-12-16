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

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.keychain.SFCertificateTrustPanel;
import ch.cyberduck.core.keychain.SFChooseIdentityPanel;
import ch.cyberduck.core.keychain.SecCertificateRef;
import ch.cyberduck.core.keychain.SecIdentityRef;
import ch.cyberduck.core.keychain.SecPolicyRef;
import ch.cyberduck.core.keychain.SecTrustRef;
import ch.cyberduck.core.keychain.SecTrustResultType;
import ch.cyberduck.core.keychain.SecurityFunctions;
import ch.cyberduck.core.ssl.CertificateStoreX509KeyManager;
import ch.cyberduck.core.ssl.DEREncoder;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.jna.ptr.PointerByReference;

public final class KeychainCertificateStore implements CertificateStore {
    private static final Logger log = Logger.getLogger(KeychainCertificateStore.class);

    private final Controller controller;
    private final SecurityFunctions library = SecurityFunctions.library;

    public KeychainCertificateStore(final Controller controller) {
        this.controller = controller;
    }

    /**
     * @param certificates Chain of certificates
     * @return True if chain is trusted
     */
    @Override
    public boolean verify(final String hostname, final List<X509Certificate> certificates) throws CertificateException {
        if(certificates.isEmpty()) {
            return false;
        }
        final SecPolicyRef policyRef = library.SecPolicyCreateSSL(false, hostname);
        final PointerByReference reference = new PointerByReference();
        library.SecTrustCreateWithCertificates(toDEREncodedCertificates(certificates), policyRef, reference);
        final SecTrustRef trustRef = new SecTrustRef(reference.getValue());
        final SecTrustResultType trustResultType = new SecTrustResultType();
        library.SecTrustEvaluate(trustRef, trustResultType);
        switch(trustResultType.getValue()) {
            case SecTrustResultType.kSecTrustResultUnspecified:
                // Accepted by user keychain setting explicitly
            case SecTrustResultType.kSecTrustResultProceed:
                return true;
            default:
                if(log.isDebugEnabled()) {
                    log.debug("Evaluated recoverable trust result failure " + trustResultType.getValue());
                }
                final AtomicReference<SFCertificateTrustPanel> ref = new AtomicReference<>();
                controller.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        ref.set(SFCertificateTrustPanel.sharedCertificateTrustPanel());
                    }
                }, true);
                final SFCertificateTrustPanel panel = ref.get();
                panel.setInformativeText(null);
                panel.setAlternateButtonTitle(LocaleFactory.localizedString("Disconnect"));
                panel.setPolicies(policyRef);
                panel.setShowsHelp(true);
                if(controller instanceof WindowController) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Display trust panel for controller %s", controller));
                    }
                    switch(new SheetInvoker(new DisabledSheetCallback(), (WindowController) controller, panel) {
                        @Override
                        protected void beginSheet(final NSWindow sheet) {
                            panel.beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_trust_message(
                                ((WindowController) controller).window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"),
                                null, trustRef, null);
                        }
                    }.beginSheet()) {
                        case SheetCallback.DEFAULT_OPTION:
                            FoundationKitFunctions.library.CFRelease(trustRef);
                            FoundationKitFunctions.library.CFRelease(policyRef);
                            return true;
                    }
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Display modal trust panel for controller %s", controller));
                    }
                    final AtomicBoolean trusted = new AtomicBoolean(false);
                    controller.invoke(new DefaultMainAction() {
                        @Override
                        public void run() {
                            switch(panel.runModalForTrust_message(trustRef, null).intValue()) {
                                case SheetCallback.DEFAULT_OPTION:
                                    trusted.set(true);
                            }
                        }
                    }, true);
                    FoundationKitFunctions.library.CFRelease(trustRef);
                    FoundationKitFunctions.library.CFRelease(policyRef);
                    if(trusted.get()) {
                        return true;
                    }
                }
                return false;
        }
    }

    @Override
    public X509Certificate choose(final String[] keyTypes, final Principal[] issuers, final Host bookmark, final String prompt) throws ConnectionCanceledException {
        final List<X509Certificate> certificates = new ArrayList<>();
        final CertificateStoreX509KeyManager manager = new KeychainX509KeyManager(bookmark, controller).init();
        final String[] aliases = manager.getClientAliases(keyTypes, issuers);
        if(null == aliases) {
            throw new ConnectionCanceledException(String.format("No certificate matching issuer %s found", Arrays.toString(issuers)));
        }
        for(String alias : aliases) {
            certificates.add(manager.getCertificate(alias, keyTypes, issuers));
        }
        try {
            final AtomicReference<SFChooseIdentityPanel> ref = new AtomicReference<>();
            controller.invoke(new DefaultMainAction() {
                @Override
                public void run() {
                    ref.set(SFChooseIdentityPanel.sharedChooseIdentityPanel());
                }
            }, true);
            final SFChooseIdentityPanel panel = ref.get();
            panel.setDomain(bookmark.getHostname());
            final SecPolicyRef policyRef = library.SecPolicyCreateSSL(false, bookmark.getHostname());
            panel.setPolicies(policyRef);
            FoundationKitFunctions.library.CFRelease(policyRef);
            panel.setShowsHelp(false);
            panel.setAlternateButtonTitle(LocaleFactory.localizedString("Disconnect"));
            panel.setInformativeText(prompt);
            final NSArray identities = toDEREncodedCertificates(certificates);
            if(controller instanceof WindowController) {
                switch(new SheetInvoker(new DisabledSheetCallback(), ((WindowController) controller).window(), panel) {
                    @Override
                    protected void beginSheet(final NSWindow sheet) {
                        panel.beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_identities_message(
                            ((WindowController) controller).window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null,
                            identities, null
                        );
                    }
                }.beginSheet()) {
                    case SheetCallback.DEFAULT_OPTION:
                        // Use the identity method to obtain the identity chosen by the user.
                        final SecIdentityRef identityRef = panel.identity();
                        return this.toX509Certificate(bookmark, identityRef);
                }
            }
            else {
                final AtomicReference<X509Certificate> selected = new AtomicReference<>();
                controller.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        switch(panel.runModalForIdentities_message(identities, null).intValue()) {
                            case SheetCallback.DEFAULT_OPTION:
                                // Use the identity method to obtain the identity chosen by the user.
                                final SecIdentityRef identityRef = panel.identity();
                                selected.set(toX509Certificate(bookmark, identityRef));
                        }
                    }
                }, true);
                if(selected.get() != null) {
                    return selected.get();
                }
            }
            throw new ConnectionCanceledException();
        }
        catch(CertificateException e) {
            throw new ConnectionCanceledException(e);
        }
    }

    private X509Certificate toX509Certificate(final Host bookmark, final SecIdentityRef identityRef) {
        if(null == identityRef) {
            log.warn(String.format("No identity selected for %s", bookmark));
            return null;
        }
        final PointerByReference reference = new PointerByReference();
        library.SecIdentityCopyCertificate(identityRef, reference);
        final SecCertificateRef certificateRef = new SecCertificateRef(reference.getValue());
        try {
            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final NSData dataRef = library.SecCertificateCopyData(certificateRef);
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

    private static NSArray toDEREncodedCertificates(final List<X509Certificate> certificates) throws CertificateException {
        // Prepare the certificate chain
        final Object[] encoded = new DEREncoder().encode(certificates);
        final NSMutableArray certs = NSMutableArray.arrayWithCapacity(new NSUInteger(certificates.size()));
        for(X509Certificate certificate : certificates) {
            certs.addObject(SecurityFunctions.library.SecCertificateCreateWithData(null,
                NSData.dataWithBase64EncodedString(Base64.encodeBase64String(certificate.getEncoded()))));
        }
        return certs;
    }
}
