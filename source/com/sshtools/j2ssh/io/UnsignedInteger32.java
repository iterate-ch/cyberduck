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
package com.sshtools.j2ssh.io;

import java.io.Serializable;


/**
 * @author $author$
 * @version $Revision$
 */
public class UnsignedInteger32 extends Number implements Serializable {
    final static long serialVersionUID = 200;

    /**  */
    public final static long MAX_VALUE = 0xffffffffL;

    /**  */
    public final static long MIN_VALUE = 0;
    private Long value;

    /**
     * Creates a new UnsignedInteger32 object.
     *
     * @param a
     * @throws NumberFormatException
     */
    public UnsignedInteger32(long a) {
        if ((a < MIN_VALUE) || (a > MAX_VALUE)) {
            throw new NumberFormatException();
        }

        value = new Long(a);
    }

    /**
     * Creates a new UnsignedInteger32 object.
     *
     * @param a
     * @throws NumberFormatException
     */
    public UnsignedInteger32(String a) throws NumberFormatException {
        Long temp = new Long(a);
        long longValue = temp.longValue();

        if ((longValue < MIN_VALUE) || (longValue > MAX_VALUE)) {
            throw new NumberFormatException();
        }

        value = new Long(longValue);
    }

    /**
     * @return
     */
    public byte byteValue() {
        return value.byteValue();
    }

    /**
     * @return
     */
    public short shortValue() {
        return value.shortValue();
    }

    /**
     * @return
     */
    public int intValue() {
        return value.intValue();
    }

    /**
     * @return
     */
    public long longValue() {
        return value.longValue();
    }

    /**
     * @return
     */
    public float floatValue() {
        return value.floatValue();
    }

    /**
     * @return
     */
    public double doubleValue() {
        return value.doubleValue();
    }

    /**
     * @return
     */
    public String toString() {
        return value.toString();
    }

    /**
     * @return
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (!(o instanceof UnsignedInteger32)) {
            return false;
        }

        return (((UnsignedInteger32) o).value.equals(this.value));
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public static UnsignedInteger32 add(UnsignedInteger32 x, UnsignedInteger32 y) {
        return new UnsignedInteger32(x.longValue() + y.longValue());
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public static UnsignedInteger32 add(UnsignedInteger32 x, int y) {
        return new UnsignedInteger32(x.longValue() + y);
    }
}
