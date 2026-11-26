package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2026 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.CertificateTrustCallback;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.security.cert.X509Certificate;
import java.util.List;

public class TerminalCertificateTrustCallback implements CertificateTrustCallback {

    private final TerminalPromptReader prompt;

    public TerminalCertificateTrustCallback() {
        this(new InteractiveTerminalPromptReader());
    }

    public TerminalCertificateTrustCallback(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    /**
     * @param hostname     Hostname of server
     * @param message      Reason why the certificate is not trusted, shown to the user with the question to connect anyway
     * @param certificates Certificate chain
     */
    @Override
    public void prompt(final String hostname, final String message, final List<X509Certificate> certificates) throws ConnectionCanceledException {
        if(!prompt.prompt(message)) {
            throw new ConnectionCanceledException(message);
        }
    }
}
