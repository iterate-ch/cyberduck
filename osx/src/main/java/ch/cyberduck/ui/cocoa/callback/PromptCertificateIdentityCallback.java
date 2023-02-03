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
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.CertificateIdentityCallback;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.KeychainCertificateStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.keychain.SFChooseIdentityPanel;
import ch.cyberduck.core.keychain.SecIdentityRef;
import ch.cyberduck.core.keychain.SecPolicyRef;
import ch.cyberduck.core.keychain.SecurityFunctions;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;

import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PromptCertificateIdentityCallback implements CertificateIdentityCallback {
    private static final Logger log = LogManager.getLogger(PromptCertificateIdentityCallback.class);

    private final Controller controller;

    public PromptCertificateIdentityCallback(final Controller controller) {
        this.controller = controller;
    }

    @Override
    public X509Certificate prompt(final String hostname, final List<X509Certificate> certificates) throws ConnectionCanceledException {
        final AtomicReference<SFChooseIdentityPanel> ref = new AtomicReference<>();
        controller.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                ref.set(SFChooseIdentityPanel.sharedChooseIdentityPanel());
            }
        }, true);
        final SFChooseIdentityPanel panel = ref.get();
        panel.setDomain(hostname);
        final SecPolicyRef policyRef = SecurityFunctions.library.SecPolicyCreateSSL(true, hostname);
        panel.setPolicies(policyRef);
        FoundationKitFunctions.library.CFRelease(policyRef);
        panel.setShowsHelp(false);
        panel.setAlternateButtonTitle(LocaleFactory.localizedString("Disconnect"));
        panel.setInformativeText(MessageFormat.format(LocaleFactory.localizedString(
            "The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to {0}."),
            hostname));
        final NSArray identities = KeychainCertificateStore.toDEREncodedCertificates(certificates);
        final int option = this.prompt(panel, identities);
        switch(option) {
            case SheetCallback.DEFAULT_OPTION:
                // Use the identity method to obtain the identity chosen by the user.
                final SecIdentityRef identityRef = panel.identity();
                if(null == identityRef) {
                    log.warn(String.format("No identity selected for %s", hostname));
                    throw new ConnectionCanceledException();
                }
                return KeychainCertificateStore.toX509Certificate(identityRef);
            default:
                throw new ConnectionCanceledException();
        }
    }

    protected int prompt(final SFChooseIdentityPanel panel, final NSArray identities) {
        return new SheetInvoker(new DisabledSheetCallback(), ((WindowController) controller).window(), panel) {
            @Override
            protected void beginSheet(final NSWindow sheet) {
                panel.beginSheetForWindow_modalDelegate_didEndSelector_contextInfo_identities_message(
                    ((WindowController) controller).window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null,
                    identities, null
                );
            }
        }.beginSheet();
    }
}
