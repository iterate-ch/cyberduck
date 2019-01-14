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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.SyncTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.transfer.download.DownloadFilterOptions;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public final class TerminalTransferFactory {

    public Transfer create(final CommandLine input, final Host host, final Path remote, final List<TransferItem> items)
            throws BackgroundException {
        final Transfer transfer;
        final TerminalAction type = TerminalActionFinder.get(input);
        if(null == type) {
            throw new BackgroundException(LocaleFactory.localizedString("Unknown"), "Unknown transfer type");
        }
        switch(type) {
            case download:
                if(StringUtils.containsAny(remote.getName(), '*')) {
                    transfer = new DownloadTransfer(host, items, new DownloadGlobFilter(remote.getName()));
                }
                else {
                    transfer = new DownloadTransfer(host, items);
                }
                if(input.hasOption(TerminalOptionsBuilder.Params.nochecksum.name())) {
                    final DownloadFilterOptions options = new DownloadFilterOptions();
                    options.checksum = Boolean.valueOf(input.getOptionValue(TerminalOptionsBuilder.Params.nochecksum.name()));
                    ((DownloadTransfer) transfer).withOptions(options);
                }
                break;
            case upload:
                transfer = new UploadTransfer(host, items);
                if(input.hasOption(TerminalOptionsBuilder.Params.nochecksum.name())) {
                    final UploadFilterOptions options = new UploadFilterOptions();
                    options.checksum = Boolean.valueOf(input.getOptionValue(TerminalOptionsBuilder.Params.nochecksum.name()));
                    ((UploadTransfer) transfer).withOptions(options);
                }
                break;
            case synchronize:
                transfer = new SyncTransfer(host, items.iterator().next());
                break;
            default:
                throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                        String.format("Unknown transfer type %s", type.name()));
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.throttle.name())) {
            try {
                transfer.setBandwidth(Float.valueOf(input.getOptionValue(TerminalOptionsBuilder.Params.throttle.name())));
            }
            catch(NumberFormatException ignore) {
                //
            }
        }
        return transfer;
    }
}
