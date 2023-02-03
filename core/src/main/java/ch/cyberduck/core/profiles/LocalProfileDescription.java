package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;

import java.util.function.Predicate;

public final class LocalProfileDescription extends ProfileDescription {

    private final Local file;

    /**
     * @param protocols Registered protocols
     * @param file      File on disk
     */
    public LocalProfileDescription(final ProtocolFactory protocols, final Local file) {
        this(protocols, protocol -> true, file);
    }

    /**
     * @param protocols Registered protocols
     * @param parent    Filter to apply for parent protocol reference in registered protocols
     * @param file      File on disk
     */
    public LocalProfileDescription(final ProtocolFactory protocols, final Predicate<Protocol> parent, final Local file) {
        super(protocols, parent,
                new LazyInitializer<Checksum>() {
                    @Override
                    protected Checksum initialize() throws ConcurrentException {
                        try {
                            // Calculate checksum lazily
                            return ChecksumComputeFactory.get(HashAlgorithm.md5).compute(file.getInputStream(), new TransferStatus());
                        }
                        catch(BackgroundException e) {
                            throw new ConcurrentException(e);
                        }
                    }
                }, new LazyInitializer<Local>() {
                    @Override
                    protected Local initialize() {
                        return file;
                    }
                }
        );
        this.file = file;
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalProfileDescription{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
