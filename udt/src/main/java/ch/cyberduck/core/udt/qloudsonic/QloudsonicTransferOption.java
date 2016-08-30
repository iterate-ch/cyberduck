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
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.udt.UDTProxyProvider;
import ch.cyberduck.core.udt.UDTTransferOption;

import java.util.List;

public class QloudsonicTransferOption implements UDTTransferOption {

    private Preferences preferences
            = PreferencesFactory.get();

    private QloudsonicVoucherFinder voucherFinder
            = new QloudsonicVoucherFinder();

    public QloudsonicTransferOption() {
        //
    }

    public QloudsonicTransferOption(final QloudsonicVoucherFinder voucherFinder) {
        this.voucherFinder = voucherFinder;
    }

    @Override
    public UDTProxyProvider provider() {
        return new QloudsonicProxyProvider(new QloudsonicVoucherFinder());
    }

    @Override
    public boolean prompt(final Host bookmark, final TransferStatus status, final ConnectionCallback prompt)
            throws BackgroundException {
        if(Host.TransferType.unknown == bookmark.getTransfer()) {
            if(!preferences.getBoolean(String.format("connection.qloudsonic.%s", bookmark.getHostname()))) {
                final List<License> receipts = voucherFinder.open();
                if(receipts.isEmpty()) {
                    // No installed voucher found. Continue with direct transfer
                    return false;
//                    try {
//                        prompt.warn(bookmark.getProtocol(), String.format("Qloudsonic"),
//                                LocaleFactory.localizedString(String.format("Exploit bandwidth capacity with Qloudsonic when transferring large file over a high-speed, high-latency wide area network (WAN) link to S3. Qloudsonic uses UDP-based Data Transfer Protocol to route downloads and uploads faster from and to Amazon S3. You will need to purchase a voucher for a transfer quota from https://qloudsonic.io.")),
//                                LocaleFactory.localizedString("Continue", "Credentials"),
//                                LocaleFactory.localizedString("Buy", "Qloudsonic"),
//                                String.format("connection.qloudsonic.%s", bookmark.getHostname())
//                        );
//                        // Continue with direct transfer
//                        return false;
//                    }
//                    catch(ConnectionCanceledException e) {
//                        // Purchase
//                        BrowserLauncherFactory.get().open(preferences.getProperty("website.qloudsonic"));
//                        // Interrupt transfer
//                        throw e;
//                    }
                }
                else {
                    // Already purchased voucher. Confirm to use
                    try {
                        prompt.warn(bookmark.getProtocol(), String.format("Qloudsonic"),
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
        return bookmark.getTransfer() == Host.TransferType.udt;
    }
}
