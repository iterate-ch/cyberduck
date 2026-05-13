package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.io.IRODSDataObjectInputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.high_level.io.IRODSDataObjectStream;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

class IRODSStreamUtils {

    private static final Logger log = LogManager.getLogger(IRODSStreamUtils.class);

    static void seek(InputStream in, long offset) throws IRODSException, IOException {
        if(in instanceof IRODSDataObjectInputStream) {
            IRODSDataObjectInputStream stream = (IRODSDataObjectInputStream) in;
            long totalOffset = offset;
            log.debug("input stream: total offset = [{}]", totalOffset);
            while(totalOffset > 0) {
                long intermediateOffset = Math.min(totalOffset, Integer.MAX_VALUE);
                totalOffset -= intermediateOffset;
                log.debug("input stream: offsetting by [{}]. remaining offset = [{}]", intermediateOffset, totalOffset);
                stream.seek((int) intermediateOffset, IRODSDataObjectStream.SeekDirection.CURRENT);
            }
        }
        else if(in instanceof FileInputStream) {
            log.debug("input stream: seeking to position [{}]", offset);
            FileChannel fc = ((FileInputStream) in).getChannel().position(offset);
            log.debug("input stream: position = [{}]", fc.position());
        }
    }

    static void seek(OutputStream out, long offset) throws IRODSException, IOException {
        if(out instanceof IRODSDataObjectOutputStream) {
            IRODSDataObjectOutputStream stream = (IRODSDataObjectOutputStream) out;
            long totalOffset = offset;
            log.debug("output stream: total offset = [{}]", totalOffset);
            while(totalOffset > 0) {
                long intermediateOffset = Math.min(totalOffset, Integer.MAX_VALUE);
                totalOffset -= intermediateOffset;
                log.debug("output stream: offsetting by [{}]. remaining offset = [{}]", intermediateOffset, totalOffset);
                stream.seek((int) intermediateOffset, IRODSDataObjectStream.SeekDirection.CURRENT);
            }
        }
        else if(out instanceof FileOutputStream) {
            log.debug("output stream: seeking to position [{}]", offset);
            FileChannel fc = ((FileOutputStream) out).getChannel().position(offset);
            log.debug("output stream: position = [{}]", fc.position());
        }
    }

}
