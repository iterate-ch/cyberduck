package ch.cyberduck.core.transfer.move;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPathFilter;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.normalizer.CopyRootPathsNormalizer;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * @version $Id$
 */
public class MoveTransfer extends Transfer {
    private static Logger log = Logger.getLogger(MoveTransfer.class);

    private Map<Path, Path> files;

    public MoveTransfer(final Map<Path, Path> files) {
        this(new CopyRootPathsNormalizer().normalize(files),
                new BandwidthThrottle(Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    private MoveTransfer(final Map<Path, Path> files, final BandwidthThrottle bandwidth) {
        super(new ArrayList<Path>(files.keySet()), bandwidth);
        this.files = files;
    }

    public <T> MoveTransfer(T dict, Session s) {
        super(dict, s, new BandwidthThrottle(
                Preferences.instance().getFloat("queue.download.bandwidth.bytes")));
    }

    @Override
    public boolean isResumable() {
        return false;
    }

    @Override
    public boolean isReloadable() {
        return false;
    }

    @Override
    public <T> T getAsDictionary() {
        final Serializer dict = super.getSerializer();
        dict.setStringForKey(String.valueOf(KIND_MOVE), "Kind");
        return dict.getSerialized();
    }

    @Override
    public AttributedList<Path> children(Path parent) {
        // Move operation on parent directory will move all children already
        return AttributedList.emptyList();
    }

    @Override
    public TransferAction action(boolean resumeRequested, boolean reloadRequested) {
        return TransferAction.ACTION_OVERWRITE;
    }

    @Override
    public TransferPathFilter filter(final TransferPrompt prompt, final TransferAction action) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter transfer with action %s", action.toString()));
        }
        if(action.equals(TransferAction.ACTION_OVERWRITE)) {
            return new MoveTransferFilter(files);
        }
        return super.filter(prompt, action);
    }

    @Override
    public void transfer(final Path file, final TransferOptions options, final TransferStatus status) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Transfer file %s with options %s", file, options));
        }
        final Path destination = files.get(file);
        file.rename(destination);
    }

    @Override
    public String getName() {
        return MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                files.keySet().iterator().next().getName(), files.values().iterator().next().getName());
    }

    @Override
    public String getStatus() {
        return this.isComplete() ? "Move complete" : "Transfer incomplete";
    }

    @Override
    public String getImage() {
        return "transfer-upload.tiff";
    }
}