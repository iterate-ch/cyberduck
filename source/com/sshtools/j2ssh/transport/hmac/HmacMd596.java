/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.transport.hmac;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class HmacMd596 extends HmacMd5 {
    /**
     * Creates a new HmacMd596 object.
     */
    public HmacMd596() {
    }

    /**
     *
     *
     * @return
     */
    public int getMacLength() {
        return 12;
    }

    /**
     *
     *
     * @param sequenceNo
     * @param data
     * @param offset
     * @param len
     *
     * @return
     */
    public byte[] generate(long sequenceNo, byte[] data, int offset, int len) {
        byte[] generated = super.generate(sequenceNo, data, offset, len);
        byte[] result = new byte[getMacLength()];
        System.arraycopy(generated, 0, result, 0, getMacLength());

        return result;
    }
}
