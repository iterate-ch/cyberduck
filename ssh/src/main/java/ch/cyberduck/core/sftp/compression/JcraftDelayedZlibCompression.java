package ch.cyberduck.core.sftp.compression;/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import net.schmizz.sshj.transport.compression.Compression;

public class JcraftDelayedZlibCompression extends JcraftZlibCompression {

    /**
     * Named factory for the ZLib Delayed Compression.
     */
    public static class Factory
            implements net.schmizz.sshj.common.Factory.Named<Compression> {

        @Override
        public Compression create() {
            return new JcraftDelayedZlibCompression();
        }

        @Override
        public String getName() {
            return "zlib@openssh.com";
        }
    }

    @Override
    public boolean isDelayed() {
        return true;
    }
}
