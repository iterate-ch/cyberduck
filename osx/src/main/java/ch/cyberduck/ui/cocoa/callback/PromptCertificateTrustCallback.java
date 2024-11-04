package ch.cyberduck.ui.cocoa.callback;

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

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.SheetInvoker;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.core.CertificateTrustCallback;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.KeychainCertificateStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.keychain.SFCertificateTrustPanel;
import ch.cyberduck.core.keychain.SecPolicyRef;
import ch.cyberduck.core.keychain.SecTrustRef;
import ch.cyberduck.core.keychain.SecurityFunctions;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.jna.ptr.PointerByReference;

public class PromptCertificateTrustCallback implements CertificateTrustCallback {
    private static final Logger log = LogManager.getLogger(PromptCertificateTrustCallback.class);

    private final Controller controller;

    public PromptCertificateTrustCallback(final Controller controller) {
        this.controller = controller;
    }

    @Override
    public void prompt(final String hostname, final List<X509Certificate> certificates) throws ConnectionCanceledException {
        final SecPolicyRef policyRef = SecurityFunctions.library.SecPolicyCreateSSL(true, hostname);
        final PointerByReference reference = new PointerByReference();
        SecurityFunctions.library.SecTrustCreateWithCertificates(KeychainCertificateStore.toDEREncodedCertificates(certificates), policyRef, reference);
        final SecTrustRef trustRef = new SecTrustRef(reference.getValue());
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
        log.debug("Display trust panel for controller {}", controller);
        final int option = this.prompt(panel, trustRef);
        FoundationKitFunctions.library.CFRelease(trustRef);
        FoundationKitFunctions.library.CFRelease(policyRef);
        switch(option) {
            case SheetCallback.DEFAULT_OPTION:
                return;
            default:
                throw new ConnectionCanceledException();
        }
    }

    protected int prompt(final SFCertificateTrustPanel panel, final SecTrustRef trustRef) {
        return new SheetInvoker(new DisabledSheetCallback(), (WindowController) controller, panel) {
            @Override
            protected void beginSheet(final NSWindow sheet) {
                panel.beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_trust_message(
                    ((WindowController) controller).window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"),
                    null, trustRef, null);
            }
        }.beginSheet();
    }
}
