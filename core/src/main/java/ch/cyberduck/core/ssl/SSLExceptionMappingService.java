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
import ch.cyberduck.core.DefaultSocketExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.SSLNegotiateException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

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
        final StringBuilder buffer = new StringBuilder();
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(cause instanceof SocketException) {
                // Map Connection has been shutdown: javax.net.ssl.SSLException: java.net.SocketException: Broken pipe
                return new DefaultSocketExceptionMappingService().map((SocketException) cause);
            }
        }
        final String message = failure.getMessage();
        for(Alert alert : Alert.values()) {
            if(StringUtils.contains(message, alert.name())) {
                this.append(buffer, alert.getDescription());
                break;
            }
        }
        if(failure instanceof SSLHandshakeException) {
            if(ExceptionUtils.getRootCause(failure) instanceof CertificateException) {
                log.warn(String.format("Ignore certificate failure %s and drop connection", failure.getMessage()));
                // Server certificate not accepted
                return new ConnectionCanceledException(failure);
            }
            return new SSLNegotiateException(buffer.toString(), failure);
        }
        if(ExceptionUtils.getRootCause(failure) instanceof GeneralSecurityException) {
            this.append(buffer, ExceptionUtils.getRootCause(failure).getMessage());
            return new InteroperabilityException(buffer.toString(), failure);
        }
        this.append(buffer, message);
        return new InteroperabilityException(buffer.toString(), failure);
    }

    private enum Alert {
        close_notify(0),
        unexpected_message(10) {
            @Override
            public String getDescription() {
                return String.format("%s. An inappropriate message was received.", super.getDescription());
            }
        },
        bad_record_mac(20) {
            @Override
            public String getDescription() {
                return String.format("%s. A record is received with an incorrect MAC.", super.getDescription());
            }
        },
        decryption_failed_RESERVED(21),
        record_overflow(22),
        decompression_failure(30),
        handshake_failure(40) {
            @Override
            public String getDescription() {
                return String.format("%s. Unable to negotiate an acceptable set of security parameters.", super.getDescription());
            }
        },
        no_certificate_RESERVED(41),
        bad_certificate(42) {
            @Override
            public String getDescription() {
                return String.format("%s. The certificate provided could not be verified by the server.", super.getDescription());
            }
        },
        unsupported_certificate(43) {
            @Override
            public String getDescription() {
                return String.format("%s. The certificate type provided is not supported by the server.", super.getDescription());
            }
        },
        certificate_revoked(44) {
            @Override
            public String getDescription() {
                return String.format("%s. The certificate provided has been revoked by its signer.", super.getDescription());
            }
        },
        certificate_expired(45) {
            @Override
            public String getDescription() {
                return String.format("%s. The certificate provided has expired.", super.getDescription());
            }
        },
        certificate_unknown(46) {
            @Override
            public String getDescription() {
                return String.format("%s. The certificate provided was not accepted by the server.", super.getDescription());
            }
        },
        illegal_parameter(47),
        unknown_ca(48) {
            @Override
            public String getDescription() {
                return String.format("%s. A valid certificate chain or partial chain was received, but " +
                        "the certificate was not accepted because the certificate authority certificate could not be located " +
                        "or couldn't be matched with a known, trusted certificate authority.", super.getDescription());
            }
        },
        access_denied(49) {
            @Override
            public String getDescription() {
                return String.format("%s. A valid certificate was received, but when access control was " +
                        "applied, the server decided not to proceed with negotiation.", super.getDescription());
            }
        },
        decode_error(50),
        decrypt_error(51),
        export_restriction_RESERVED(60),
        protocol_version(70) {
            @Override
            public String getDescription() {
                return String.format("%s. The protocol version attempted to negotiate is recognized but not supported.", super.getDescription());
            }
        },
        insufficient_security(71) {
            @Override
            public String getDescription() {
                return String.format("%s. The server requires ciphers more secure than those supported.", super.getDescription());
            }
        },
        internal_error(80),
        user_canceled(90),
        no_renegotiation(100),
        unsupported_extension(110);

        private final int code;

        Alert(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return StringUtils.capitalize(StringUtils.replaceChars(this.name(), '_', ' '));
        }
    }
}
