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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.UDTProxyConfigurator;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTransferAcceleration;

import java.util.List;

public class QloudsonicTransferAcceleration implements UDTTransferAcceleration {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final QloudsonicVoucherFinder voucher;

    private final Session<?> session;

    private Long udtThreshold = Long.MAX_VALUE;

    public QloudsonicTransferAcceleration(final Session session) {
        this(session, new QloudsonicVoucherFinder());
    }

    public QloudsonicTransferAcceleration(final Session session, final QloudsonicVoucherFinder voucher) {
        this.session = session;
        this.voucher = voucher;
    }

    @Override
    public UDTProxyProvider provider() {
        return new QloudsonicProxyProvider(voucher);
    }

    @Override
    public boolean getStatus(final Path file) {
        return preferences.getBoolean(String.format("connection.qloudsonic.%s", session.getHost().getHostname()));
    }

    @Override
    public void setStatus(final Path file, final boolean enabled) {
        preferences.setProperty(String.format("connection.qloudsonic.%s", session.getHost().getHostname()), enabled);
    }

    @Override
    public boolean prompt(final Host bookmark, final Path file, final TransferStatus status, final ConnectionCallback prompt)
            throws BackgroundException {
        // Only for AWS given threshold
        if(status.getLength() < udtThreshold) {
            return false;
        }
        if(Host.TransferType.unknown == bookmark.getTransferType()) {
            if(!preferences.getBoolean(String.format("connection.qloudsonic.%s", bookmark.getHostname()))) {
                final List<License> receipts = voucher.open();
                if(receipts.isEmpty()) {
                    // No installed voucher found. Continue with direct transfer
                    try {
                        prompt.warn(bookmark.getProtocol(), "Qloudsonic",
                                LocaleFactory.localizedString("Exploit bandwidth capacity with Qloudsonic when transferring large file over a high-speed, high-latency wide area network (WAN) link to S3. Qloudsonic uses UDP-based Data Transfer Protocol to route downloads and uploads faster from and to Amazon S3. You will need to purchase a voucher for a transfer quota from https://qloudsonic.io."),
                                LocaleFactory.localizedString("Continue", "Credentials"),
                                LocaleFactory.localizedString("Buy", "Qloudsonic"),
                                String.format("connection.qloudsonic.%s", bookmark.getHostname())
                        );
                        // Continue with direct transfer
                        return false;
                    }
                    catch(ConnectionCanceledException e) {
                        // Purchase
                        BrowserLauncherFactory.get().open(preferences.getProperty("website.qloudsonic"));
                        // Interrupt transfer
                        throw e;
                    }
                }
                else {
                    // Already purchased voucher. Confirm to use
                    try {
                        prompt.warn(bookmark.getProtocol(), "Qloudsonic",
                                String.format(LocaleFactory.localizedString("Do you want to transfer %s with Qloudsonic?", "Qloudsonic"), SizeFormatterFactory.get().format(status.getLength())),
                                LocaleFactory.localizedString("Continue", "Credentials"),
                                LocaleFactory.localizedString("Cancel"),
                                String.format("connection.qloudsonic.%s", bookmark.getHostname())
                        );
                        if(preferences.getBoolean(String.format("connection.qloudsonic.%s", bookmark.getHostname()))) {
                            bookmark.setTransfer(Host.TransferType.udt);
                        }
                        return true;
                    }
                    catch(ConnectionCanceledException e) {
                        // Continue with direct transfer
                    }
                }
            }
        }
        return bookmark.getTransferType() == Host.TransferType.udt;
    }

    @Override
    public HttpSession<?> open(final Host bookmark, final Path file, final X509TrustManager trust, final X509KeyManager key) throws BackgroundException {
        final Location.Name location = session.getFeature(Location.class).getLocation(file);
        if(Location.unknown.equals(location)) {
            throw new AccessDeniedException("Cannot read bucket location");
        }
        final UDTProxyConfigurator configurator = new UDTProxyConfigurator(location, this.provider(), trust, key);
        return configurator.configure((HttpSession) SessionFactory.create(session.getHost(), trust, key
        ));
    }

    public QloudsonicTransferAcceleration withUdtThreshold(final Long threshold) {
        this.udtThreshold = threshold;
        return this;
    }
}
