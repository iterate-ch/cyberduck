package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.random.NonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.AbstractChecksumCompute;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.io.input.NullInputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CryptoChecksumCompute extends AbstractChecksumCompute implements ChecksumCompute {
    private static final Logger log = Logger.getLogger(CryptoChecksumCompute.class);

    private final CryptoVault vault;
    private final ChecksumCompute delegate;

    public CryptoChecksumCompute(final ChecksumCompute delegate, final CryptoVault vault) {
        this.vault = vault;
        this.delegate = delegate;
    }

    @Override
    public Checksum compute(final InputStream in, final TransferStatus status) throws ChecksumException {
        if(Checksum.NONE == delegate.compute(new NullInputStream(0L), status)) {
            return Checksum.NONE;
        }
        final Checksum checksum = this.compute(in, status.getHeader(), status.getNonces());
        return checksum;
    }

    protected Checksum compute(final InputStream in, final ByteBuffer header, final NonceGenerator nonces) throws ChecksumException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Calculate checksum with header %s", header));
        }
        try {
            final PipedOutputStream source = new PipedOutputStream();
            final CryptoOutputStream<Void> out = new CryptoOutputStream<Void>(new VoidStatusOutputStream(source), vault.getCryptor(),
                    vault.getCryptor().fileHeaderCryptor().decryptHeader(header), nonces);
            final PipedInputStream sink = new PipedInputStream(source);
            final ThreadPool pool = ThreadPoolFactory.get();
            try {
                final Future execute = pool.execute(new Callable<TransferStatus>() {
                    @Override
                    public TransferStatus call() throws Exception {
                        source.write(header.array());
                        final TransferStatus status = new TransferStatus();
                        new StreamCopier(status, status).transfer(in, out);
                        return status;
                    }
                });
                try {
                    return delegate.compute(sink, new TransferStatus());
                }
                finally {
                    try {
                        execute.get();
                    }
                    catch(InterruptedException e) {
                        throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
                    }
                    catch(ExecutionException e) {
                        if(e.getCause() instanceof BackgroundException) {
                            throw (BackgroundException) e.getCause();
                        }
                        throw new DefaultExceptionMappingService().map(e.getCause());
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
            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e.getMessage(), e);
        }
    }
}
