package com.sshtools.j2ssh.io;

import java.io.Serializable;

public class UnsignedInteger32 extends Number implements Serializable {

    final static long serialVersionUID = 200;

    private Long value;

    /**
     * the maximum value this long can have
     */
    public final static long MAX_VALUE = 0xffffffffL;

    /**
     * the minimum value this long can have
     */
    public final static long MIN_VALUE = 0;

    /**
     * Constructor creates an unsigned 32-bit integer object for
     * the specified long value. Only the bottom 32 bits are
     * considered.
     */
    public UnsignedInteger32(long a) {
	if ((a < MIN_VALUE) || (a > MAX_VALUE)) {
	    throw new NumberFormatException();
	}
	value = new Long(a);
    }

    /**
     * Constructor creates an unsigned 32-bit integer object
     * for the specified string. Only the bottom 32 bits are
     * considered.
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
     * Returns the value of this unsigned 32-bit integer object as a byte.
     * This method returns the least significant 8 bits.
     *
     * @return byte	the byte value of this unsigned 32-bit integer object
     *
     */
    public byte byteValue() {
	return value.byteValue();
    }


    /**
     * Returns the value of this unsigned 32-bit integer object as a short
     * This method returns the least significant 16 bits.
     *
     * @return short	value of this unsigned 32-bit integer object as a short
     *
     */
    public short shortValue() {
	return value.shortValue();
    }

    /**
     * Returns the value of this unsigned 32-bit integer object as an int
     * This method returns the least significant 32 bits.
     *
     * @return int	value of this unsigned 32-bit integer object as an int
     *
     */
    public int intValue() {
	return value.intValue();
    }


    /**
     * Returns the value of this unsigned 32-bit integer object as a long
     * This method returns the least significant 64 bits.
     *
     * @return long	value of this unsigned 32-bit integer object as a long
     *
     */
    public long longValue() {
	return value.longValue();
    }

    /**
     * Returns the value of this unsigned 32-bit integer object as a float
     *
     * @return float	value of this unsigned 32-bit integer object as a float
     *
     */
    public float floatValue() {
	return value.floatValue();
    }


    /**
     * Returns the value of this unsigned 32-bit integer object as a double
     *
     * @return double	value of this unsigned 32-bit integer object as a
     * 			double
     *
     */
    public double doubleValue() {
	return value.doubleValue();
    }


    /**
     * Returns the text representation of this unsigned 32-bit integer object
     *
     * @return String	text representation of this unsigned 32-bit integer
     *
     */
    public String toString() {
	return value.toString();
    }


    /**
     * Computes the hash code for this unsigned 32-bit integer object
     *
     * @return int	the integer representing the hash code
     * 			for this unsigned 32-bit integer
     */
    public int hashCode() {
	return value.hashCode();
    }


    /**
     * Compares this unsigned 32-bit integer object with the specified
     * object for equality
     *
     * @return boolen	true if the specified object is an unsigned 32-bit
     * 			integer.
     *			Otherwise, false.
     */
    public boolean equals(Object o) {
	if (!(o instanceof UnsignedInteger32)) {
	    return false;
        }
	return (((UnsignedInteger32)o).value.equals(this.value));
    }


    public static UnsignedInteger32 add(UnsignedInteger32 x, UnsignedInteger32 y) {
      return new UnsignedInteger32(x.longValue()+y.longValue());
    }

    public static UnsignedInteger32 add(UnsignedInteger32 x, int y) {
      return new UnsignedInteger32(x.longValue()+y);
    }

}
