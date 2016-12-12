package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.irods.jargon.core.checksum.ChecksumValue;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.TransferOptions;
import org.irods.jargon.core.pub.DataObjectChecksumUtilitiesAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;

import java.io.File;
import java.text.MessageFormat;

public class IRODSUploadFeature implements Upload<Checksum> {
    private static final Logger log = Logger.getLogger(IRODSUploadFeature.class);

    private final IRODSSession session;

    private final Preferences preferences = PreferencesFactory.get();

    public IRODSUploadFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Checksum upload(final Path file, final Local local, final BandwidthThrottle throttle,
                           final StreamListener listener, final TransferStatus status,
                           final ConnectionCallback callback) throws BackgroundException {
        try {
            final IRODSFileSystemAO fs = session.filesystem();
            final IRODSFile f = fs.getIRODSFileFactory().instanceIRODSFile(file.getAbsolute());
            final TransferControlBlock block = DefaultTransferControlBlock.instance(StringUtils.EMPTY,
                    preferences.getInteger("connection.retry"));
            final TransferOptions options = new DefaultTransferOptionsConfigurer().configure(new TransferOptions());
            options.setUseParallelTransfer(session.getHost().getTransferType().equals(Host.TransferType.concurrent));
            block.setTransferOptions(options);
            final DataTransferOperations transfer = fs.getIRODSAccessObjectFactory().getDataTransferOperations(fs.getIRODSAccount());
            transfer.putOperation(new File(local.getAbsolute()), f, new DefaultTransferStatusCallbackListener(
                    status, listener, block
            ), block);
            if(status.isComplete()) {
                final DataObjectChecksumUtilitiesAO checksum = fs
                        .getIRODSAccessObjectFactory()
                        .getDataObjectChecksumUtilitiesAO(fs.getIRODSAccount());
                final ChecksumValue value = checksum.computeChecksumOnDataObject(f);
                final Checksum fingerprint = Checksum.parse(value.getChecksumStringValue());
                if(null == fingerprint) {
                    log.warn(String.format("Unsupported checksum algorithm %s", value.getChecksumEncoding()));
                }
                else {
                    final Checksum expected = ChecksumComputeFactory.get(fingerprint.algorithm).compute(local.getInputStream(), status);
                    if(!expected.equals(fingerprint)) {
                        throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                MessageFormat.format("Mismatch between {0} hash {1} of uploaded data and ETag {2} returned by the server",
                                        fingerprint.algorithm.toString(), expected, fingerprint.hash));
                    }
                }
                return fingerprint;
            }
            return null;
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public Write.Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        if(session.getFeature(Find.class, new DefaultFindFeature(session)).withCache(cache).find(file)) {
            return Write.override;
        }
        return Write.notfound;
    }
}
