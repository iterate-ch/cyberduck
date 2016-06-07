package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DefaultCertificateStore;
import ch.cyberduck.core.LocaleFactory;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;

import com.amazonaws.util.StringUtils;

public class TerminalCertificateStore extends DefaultCertificateStore {

    private final Console console = new Console();

    private final DefaultHostnameVerifier verifier
            = new DefaultHostnameVerifier();

    private TerminalPromptReader prompt;

    public TerminalCertificateStore() {
        this.prompt = new InteractiveTerminalPromptReader();
    }

    public TerminalCertificateStore(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    @Override
    public boolean display(final List<X509Certificate> certificates) {
        for(X509Certificate c : certificates) {
            console.printf("%n%s", c.toString());
        }
        return true;
    }

    @Override
    public boolean isTrusted(final String hostname, final List<X509Certificate> certificates) {
        if(certificates.isEmpty()) {
            return false;
        }
        for(X509Certificate c : certificates) {
            // Checks that the certificate is currently valid.
            try {
                c.checkValidity();
            }
            catch(CertificateExpiredException e) {
                return prompt.prompt(LocaleFactory.localizedString(StringUtils.replace("The certificate for this server has expired. You might be connecting to a server that " +
                        "is pretending to be “%@” which could put your confidential information at risk. " +
                        "Would you like to connect to the server anyway?", "%@", hostname), "Keychain"));
            }
            catch(CertificateNotYetValidException e) {
                return prompt.prompt(LocaleFactory.localizedString(StringUtils.replace("The certificate for this server is not yet valid. You might be connecting to a server that " +
                        "is pretending to be “%@” which could put your confidential information at risk. Would you like to connect to the server anyway?", "%@", hostname), "Keychain"));
            }
        }
        try {
            verifier.verify(hostname, certificates.get(0));
        }
        catch(SSLException e) {
            return prompt.prompt(LocaleFactory.localizedString(StringUtils.replace("The certificate for this server is invalid. " +
                    "You might be connecting to a server that is pretending to be “%@” which could put " +
                    "your confidential information at risk. Would you like to connect to the server anyway?", "%@", hostname), "Keychain"));
        }
        return true;
    }
}
