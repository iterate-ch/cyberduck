package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.cryptomator.CryptoOutputStream;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.cryptomator.random.RotatingNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.random.NonceGenerator;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.FileHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

public class CryptoChecksumCompute extends AbstractChecksumCompute {
    private static final Logger log = LogManager.getLogger(CryptoChecksumCompute.class);

    private final CryptoVault cryptomator;
    private final ChecksumCompute delegate;

    public CryptoChecksumCompute(final ChecksumCompute delegate, final CryptoVault vault) {
        this.cryptomator = vault;
        this.delegate = delegate;
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws BackgroundException {
        if(Checksum.NONE == delegate.compute(new NullInputStream(0L), new TransferStatus())) {
            return Checksum.NONE;
        }
        if(null == status.getHeader()) {
            // Write header to be reused in writer
            final FileHeader header = cryptomator.getFileHeaderCryptor().create();
            status.setHeader(cryptomator.getFileHeaderCryptor().encryptHeader(header));
        }
        if(null == status.getNonces()) {
            status.setNonces(status.getLength() == TransferStatus.UNKNOWN_LENGTH ?
                    new RandomNonceGenerator(cryptomator.getNonceSize()) :
                    new RotatingNonceGenerator(cryptomator.getNonceSize(), cryptomator.numberOfChunks(status.getLength())));
        }
        return this.compute(this.normalize(in, status), status, status.getOffset(), status.getLength(), status.getHeader(), status.getNonces());
    }

    protected Checksum compute(final InputStream in, final StreamCancelation cancel,
                               final long offset, final long length, final ByteBuffer header, final NonceGenerator nonces) throws ChecksumException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Calculate checksum with header %s", header));
        }
        try {
            final PipedOutputStream source = new PipedOutputStream();
            final CryptoOutputStream out = new CryptoOutputStream(source, cryptomator.getFileContentCryptor(),
                    cryptomator.getFileHeaderCryptor().decryptHeader(header), nonces, cryptomator.numberOfChunks(offset));
            final PipedInputStream sink = new PipedInputStream(source, PreferencesFactory.get().getInteger("connection.chunksize"));
            final ThreadPool pool = ThreadPoolFactory.get("checksum", 1);
            try {
                final Future<Void> execute = pool.execute(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if(offset == 0) {
                            source.write(header.array());
                        }
                        new StreamCopier(cancel, StreamProgress.noop).transfer(in, out);
                        return null;
                    }
                });
                try {
                    return delegate.compute(sink, new TransferStatus().withLength(cryptomator.toCiphertextSize(offset, length)));
                }
                finally {
                    try {
                        Uninterruptibles.getUninterruptibly(execute);
                    }
                    catch(ExecutionException e) {
                        for(Throwable cause : ExceptionUtils.getThrowableList(e)) {
                            Throwables.throwIfInstanceOf(cause, BackgroundException.class);
                        }
                        throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
                    }
                }
            }
            finally {
                pool.shutdown(true);
            }
        }
        catch(ChecksumException e) {
            throw e;
        }
        catch(IOException | BackgroundException e) {
            throw new ChecksumException(e);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoChecksumCompute{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
