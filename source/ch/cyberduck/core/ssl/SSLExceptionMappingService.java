package ch.cyberduck.core.ssl;

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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLKeyException;
import javax.net.ssl.SSLProtocolException;
import java.security.cert.CertificateException;

/**
 * @version $Id:$
 */
public class SSLExceptionMappingService extends AbstractExceptionMappingService<SSLException> {
    private static final Logger log = Logger.getLogger(SSLExceptionMappingService.class);

    /**
     * close_notify(0),
     * unexpected_message(10),
     * bad_record_mac(20),
     * decryption_failed_RESERVED(21),
     * record_overflow(22),
     * decompression_failure(30),
     * handshake_failure(40),
     * no_certificate_RESERVED(41),
     * bad_certificate(42),
     * unsupported_certificate(43),
     * certificate_revoked(44),
     * certificate_expired(45),
     * certificate_unknown(46),
     * illegal_parameter(47),
     * unknown_ca(48),
     * access_denied(49),
     * decode_error(50),
     * decrypt_error(51),
     * export_restriction_RESERVED(60),
     * protocol_version(70),
     * insufficient_security(71),
     * internal_error(80),
     * user_canceled(90),
     * no_renegotiation(100),
     * unsupported_extension(110),
     */
    @Override
    public BackgroundException map(final SSLException failure) {
        if(failure instanceof SSLHandshakeException) {
            if(failure.getCause() instanceof CertificateException) {
                log.warn(String.format("Ignore certificate failure %s and drop connection", failure.getMessage()));
                // Server certificate not accepted
                return new ConnectionCanceledException(failure);
            }
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        if(failure instanceof SSLProtocolException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        if(failure instanceof SSLHandshakeException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        if(failure instanceof SSLKeyException) {
            return new InteroperabilityException(buffer.toString(), failure);
        }
        return this.wrap(failure, buffer);
    }
}
