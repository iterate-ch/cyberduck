/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.hmac;

/**
 * Implements the hmac-md5-96 message authentication algorithm for j2ssh. This
 * implementation simply extends the hmac-md5 algorithm but only returns the
 * first 96 bits of the mac
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class HmacMd596
    extends HmacMd5 {
    /**
     * Constructor for the mac
     */
    public HmacMd596() {
    }

    /**
     * Return the size of the mac (12 bytes)
     *
     * @return the mac length
     */
    public int getMacLength() {
        return 12;
    }

    /**
     * Generate the mac and return the first 96 bits
     *
     * @param sequenceNo The sequence number of the message
     * @param data The message data
     * @param offset The position to update from
     * @param len The length of data to update
     *
     * @return The message authentication code
     */
    public byte[] generate(long sequenceNo, byte data[], int offset, int len) {
        byte generated[] = super.generate(sequenceNo, data, offset, len);
        byte result[] = new byte[getMacLength()];

        System.arraycopy(generated, 0, result, 0, getMacLength());

        return result;
    }
}
