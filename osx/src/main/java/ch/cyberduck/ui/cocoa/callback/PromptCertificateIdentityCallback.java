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

import ch.cyberduck.binding.AlertRunner;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.KeychainCertificateStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.keychain.SFChooseIdentityPanel;
import ch.cyberduck.core.keychain.SecIdentityRef;
import ch.cyberduck.core.keychain.SecPolicyRef;
import ch.cyberduck.core.keychain.SecurityFunctions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Rococoa;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PromptCertificateIdentityCallback implements CertificateIdentityCallback {
    private static final Logger log = LogManager.getLogger(PromptCertificateIdentityCallback.class);

    private final ProxyController controller;
    /**
     * Parent window or null
     */
    private final NSWindow window;

    public PromptCertificateIdentityCallback(final ProxyController controller) {
        this.controller = controller;
        if(controller instanceof WindowController) {
            this.window = ((WindowController) controller).window();
        }
        else {
            this.window = null;

        }
    }

    @Override
    public X509Certificate prompt(final String hostname, final List<X509Certificate> certificates) throws ConnectionCanceledException {
        final AtomicReference<SFChooseIdentityPanel> ref = new AtomicReference<>();
        final NSArray identities = KeychainCertificateStore.toDEREncodedCertificates(certificates);
        final int option = controller.alert(new SheetController.NoBundleSheetController() {
            @Outlet
            private SFChooseIdentityPanel panel;

            @Override
            public void loadBundle() {
                panel = SFChooseIdentityPanel.sharedChooseIdentityPanel();
                panel.setDomain(hostname);
                final SecPolicyRef policyRef = SecurityFunctions.library.SecPolicyCreateSSL(true, hostname);
                panel.setPolicies(policyRef);
                FoundationKitFunctions.library.CFRelease(policyRef);
                panel.setShowsHelp(false);
                panel.setAlternateButtonTitle(LocaleFactory.localizedString("Disconnect"));
                panel.setInformativeText(MessageFormat.format(LocaleFactory.localizedString(
                        "The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to {0}."), hostname));
            }

            @Override
            public NSWindow window() {
                return panel;
            }
        }, new AlertRunner() {
            @Override
            public void alert(final NSWindow sheet, final SheetCallback callback) {
                final SFChooseIdentityPanel panel = Rococoa.cast(sheet, SFChooseIdentityPanel.class);
                if(null == window) {
                    callback.callback(panel.runModalForIdentities_message(identities, null).intValue());
                }
                else {
                    panel.beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_identities_message(
                            window, new WindowController.SheetDidCloseReturnCodeDelegate(callback).id(),
                            WindowController.SheetDidCloseReturnCodeDelegate.selector, null, identities, null
                    );
                }
            }
        });
        switch(option) {
            case SheetCallback.DEFAULT_OPTION:
                // Use the identity method to obtain the identity chosen by the user.
                final SFChooseIdentityPanel panel = ref.get();
                final SecIdentityRef identityRef = panel.identity();
                if(null == identityRef) {
                    log.warn("No identity selected for {}", hostname);
                    throw new ConnectionCanceledException();
                }
                try {
                    return KeychainCertificateStore.toX509Certificate(identityRef);
                }
                catch(CertificateException e) {
                    throw new ConnectionCanceledException(e);
                }
            default:
                throw new ConnectionCanceledException();
        }
    }
}
