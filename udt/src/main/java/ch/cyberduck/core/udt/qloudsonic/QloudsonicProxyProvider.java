package ch.cyberduck.core.udt.qloudsonic;

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

import ch.cyberduck.core.Header;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.udt.UDTProtocol;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTLSProtocol;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class QloudsonicProxyProvider implements UDTProxyProvider {
    private static final Logger log = Logger.getLogger(QloudsonicProxyProvider.class);

    private LicenseFactory factory;

    public QloudsonicProxyProvider() {
        this.factory = new QloudsonicVoucherFinder();
    }

    public QloudsonicProxyProvider(final LicenseFactory factory) {
        this.factory = factory;
    }

    @Override
    public Host find(final Location.Name region, final boolean tls) {
        final Protocol protocol;
        if(tls) {
            protocol = new UDTTLSProtocol();
        }
        else {
            protocol = new UDTProtocol();
        }
        return new Host(protocol, String.format("%s.qloudsonic.io",
                region.getIdentifier()), protocol.getScheme().getPort());
    }

    @Override
    public List<Header> headers() throws AccessDeniedException, MissingReceiptException, InvalidReceiptException {
        final List<License> receipts = factory.open();
        if(receipts.isEmpty()) {
            throw new MissingReceiptException();
        }
        final List<Header> headers = new ArrayList<Header>();
        for(License receipt : receipts) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Using voucher %s", receipt));
            }
            if(!receipt.verify()) {
                throw new InvalidReceiptException();
            }
            headers.add(new Header(Headers.HEADER_VOUCHER, receipt.getValue("Voucher")));
        }
        return headers;
    }

    private static final class Headers {
        public static final String PREFIX = "X-Qloudsonic-";

        private Headers() {
            //
        }

        /**
         * Plan
         */
        public static final String HEADER_VOUCHER = String.format("%sVoucher", PREFIX);
    }
}
