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
package com.sshtools.j2ssh.util;


/**
 * @author $author$
 * @version $Revision$
 */
public class Base64 {
    /**  */
    public final static boolean ENCODE = true;

    /**  */
    public final static boolean DECODE = false;
    private final static int MAX_LINE_LENGTH = 76;
    private final static byte EQUALS_SIGN = (byte) '=';
    private final static byte NEW_LINE = (byte) '\n';
    private final static byte[] ALPHABET = {
        (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
        (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
        (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
        (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
        (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd',
        (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j',
        (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p',
        (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v',
        (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1',
        (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
        (byte) '8', (byte) '9', (byte) '+', (byte) '/'
    };
    private final static byte[] DECODABET = {
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9,
        -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9,
        -9, -9, -9, -9, -9, -9, -9,
        // Decimal 33 - 42
        62, -9, -9, -9,
        // Decimal 44 - 46
        63,
        // Slash at decimal 47
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9,
        // Decimal 62 - 64
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
        // Letters 'A' through 'N'
        14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9,

        // Decimal 91 - 96
        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
        // Letters 'a' through 'm'
        39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9
    };
    private final static byte BAD_ENCODING = -9;

    // Indicates error in encoding
    private final static byte white_SPACE_ENC = -5;

    // Indicates white space in encoding
    private final static byte EQUALS_SIGN_ENC = -1;

    // Indicates equals sign in encoding
    private Base64() {
    }

    /**
     * @param s
     * @return
     */
    public static byte[] decode(String s) {
        byte[] bytes = s.getBytes();

        return decode(bytes, 0, bytes.length);
    }

    // end decode
    public static byte[] decode(byte[] source, int off, int len) {
        int len34 = (len * 3) / 4;
        byte[] outBuff = new byte[len34];

        // Upper limit on size of output
        int outBuffPosn = 0;
        byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;

        for (i = 0; i < len; i++) {
            sbiCrop = (byte) (source[i] & 0x7f);

            // Only the low seven bits
            sbiDecode = DECODABET[sbiCrop];

            if (sbiDecode >= white_SPACE_ENC) {
                // White space, Equals sign or better
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    b4[b4Posn++] = sbiCrop;

                    if (b4Posn > 3) {
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
                        b4Posn = 0;

                        // If that was the equals sign, break out of 'for' loop
                        if (sbiCrop == EQUALS_SIGN) {
                            break;
                        }
                    }

                    // end if: quartet built
                }

                // end if: equals sign or better
            }
            // end if: white space, equals sign or better
            else {
                System.err.println("Bad Base64 input character at " + i + ": " +
                        source[i] + "(decimal)");

                return null;
            }

            // end else:
        }

        // each input character
        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);

        return out;
    }

    // end decode
    public static Object decodeToObject(String encodedObject) {
        byte[] objBytes = decode(encodedObject);
        java.io.ByteArrayInputStream bais = null;
        java.io.ObjectInputStream ois = null;

        try {
            bais = new java.io.ByteArrayInputStream(objBytes);
            ois = new java.io.ObjectInputStream(bais);

            return ois.readObject();
        }
                // end try
        catch (java.io.IOException e) {
            e.printStackTrace();

            return null;
        }
                // end catch
        catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();

            return null;
        }
                // end catch
        finally {
            try {
                bais.close();
            }
            catch (Exception e) {
            }

            try {
                ois.close();
            }
            catch (Exception e) {
            }
        }

        // end finally
    }

    // end decodeObject
    public static String decodeToString(String s) {
        return new String(decode(s));
    }

    // end decodeToString
    public static String encodeBytes(byte[] source, boolean ignoreMaxLineLength) {
        return encodeBytes(source, 0, source.length, ignoreMaxLineLength);
    }

    // end encodeBytes
    public static String encodeBytes(byte[] source, int off, int len,
                                     boolean ignoreMaxLineLength) {
        int len43 = (len * 4) / 3;
        byte[] outBuff = new byte[(len43) + (((len % 3) > 0) ? 4 : 0) +
                (len43 / MAX_LINE_LENGTH)];

        // New lines
        int d = 0;
        int e = 0;
        int len2 = len - 2;
        int lineLength = 0;

        for (; d < len2; d += 3, e += 4) {
            encode3to4(source, d + off, 3, outBuff, e);
            lineLength += 4;

            if (!ignoreMaxLineLength) {
                if (lineLength == MAX_LINE_LENGTH) {
                    outBuff[e + 4] = NEW_LINE;
                    e++;
                    lineLength = 0;
                }

                // end if: end of line
            }
        }

        // en dfor: each piece of array
        if (d < len) {
            encode3to4(source, d + off, len - d, outBuff, e);
            e += 4;
        }

        // end if: some padding needed
        return new String(outBuff, 0, e);
    }

    // end encodeBytes
    public static String encodeObject(java.io.Serializable serializableObject) {
        java.io.ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        java.io.ObjectOutputStream oos = null;

        try {
            baos = new java.io.ByteArrayOutputStream();
            b64os = new Base64.OutputStream(baos, Base64.ENCODE);
            oos = new java.io.ObjectOutputStream(b64os);
            oos.writeObject(serializableObject);
        }
                // end try
        catch (java.io.IOException e) {
            e.printStackTrace();

            return null;
        }
                // end catch
        finally {
            try {
                oos.close();
            }
            catch (Exception e) {
            }

            try {
                b64os.close();
            }
            catch (Exception e) {
            }

            try {
                baos.close();
            }
            catch (Exception e) {
            }
        }

        // end finally
        return new String(baos.toByteArray());
    }

    // end encode
    public static String encodeString(String s, boolean ignoreMaxLineLength) {
        return encodeBytes(s.getBytes(), ignoreMaxLineLength);
    }

    // end encodeString
    public static void main(String[] args) {
        String s = "P2/56wAAAgoAAAAmZGwtbW9kcHtzaWdue2RzYS1uaXN0LXNoYTF9LGRoe3Bs" +
                "YWlufX0AAAAIM2Rlcy1jYmMAAAHIifTc7/X/swWj4OHVWX9RsUxWh4citAMwGzv6X9mUG6a" +
                "mh5/2f6IiQ3lOeHFd5J0EAOeGNuLqE/RWJ/fFaZAzD6YTr1GZ5hflMzvRu3jbgZoLRz2TaT" +
                "qeRs1yWrQoqANE2nBx6uDNrRahduqalLg2P/ezRCLGpqbw3HFgXmiZvzhd/rdEgZur7ZPnm" +
                "EK7t4Ldypk/7xcK192JTbBXLDSKOEAqfYQb9CzW8MgEXde0DpMRZ9Fgm0KWPfz4CCJ0F9dd" +
                "zcWl1nuGibL3klLKQANgecTurFlrxkBaHgxgl9nIvf24wH3nscvmD/uFOzacT/LzFaD03HFj" +
                "/QHCiTezxVyyuJ39d3e6BBegV26vEFoGbrZ2mMf08C2MBmLmZELYdBRJ4kLpT5EZkzR8L4rT" +
                "GxNiWkb4dGT42gHH41p2ad053lctyFWp/uQJnvJEiEm3BMURVY7k1S7zgv2FHgHE0LssXvBHx" +
                "n/wnft0ne2NOqEXfs/Y4I39Nd7eDIupSVy/ZFfMmNPIhzKyC5lFMkjIMxPXNk548ZoP9Tnga" +
                "4NPhHNKtcMinVvO2HT6dnIKMNb/NuXooULHIMVISpslRzXiVlTcN9vL/jhJhn9S";
        byte[] buf = Base64.decode(s);
        System.out.println(new String(buf).toString());
        s = "P2/56wAAAgIAAAAmZGwtbW9kcHtzaWdue2RzYS1uaXN0LXNoYTF9LGRoe3BsYWlufX0A" +
                "AAAEbm9uZQAAAcQAAAHAAAAAAAAABACwV9/fUska/ZHWa4YzXIPFJ4RbcFqPVV0vmyWkFekw" +
                "wKE1mA0GVDRq9X0HTjA2DZWcQ46suVP/8mLwpnjTKNiRdFvXWGkxEpavLp+bjPa/NXsEsjZL" +
                "aeO5iPZ11Xw5lx7uor8q/Ewwo9IcYOXzuOWN1EPCpdRv5OOaO3PCMq6QSQAAA/9j/IrSUDtf" +
                "BLBlzPHBrzboKjIMXv9O8CIRtSqnV7GV9wllgh3Cm+Eh+rd5CB698JGQD9tMdBn4s8bj/BDL" +
                "4qsxbQbAsZOIin6fqDKNLDxFo357eXM06I5569PgC6cuBoJXOyQTg+sLrjT8/b3/1N4TjdZN" +
                "JiKiSiuOzkn03tNSbgAAAKDJhI2ZNNvzOXhp+VFuoY//D9GvHwAAA/9NkAROm4wF7NCPsBXd" +
                "/+QfNV3NM/FSpOonZZDg2AVnCCGdLOXCWEj60EVHWEf5FbOjJ1KynbbdZA6q5JtVDIYuU9wH" +
                "BCsT5iCexGD5j2HYNcUXT4VG5a6qzqloR2JizlZOcjiEM2j0/hydFUei0VYmJNY5L//AprO6" +
                "1UJL2OGFEQAAAJ0Ts+KlcAYmJjJWODOG3mYuiTgv7A==";
        buf = Base64.decode(s);
        System.out.println(new String(buf).toString());
        buf = Base64.decode("P2/56wAAAi4AAAA3aWYtbW9kbntzaWdue3JzYS1wa2NzMS1zaGExfSxlbmNyeXB0e3JzYS1wa2NzMXYyLW9hZXB9fQAAAARub25lAAAB3wAAAdsAAAARAQABAAAD/iJ48YNIqLobasqkyRAD6Ejzhe0bK0Nd12iq0X9xG7M5xyVJns5SH3oAPwsa/V63omsQnm/ERG5lCnFGymTSCTpz0jGYLAh81S4XGbZEJRltP75LiM4J1OIfQkF7Zxd/mYFAYpu50fOLTrk+EwOCyQJK63uXzxQHCU1JKzt61m05AAAEALhvA6F1Ffhf/HLPKe3mp/CdTYQyioHzdL2ur6jyvh+b5wb8WuiaL+xu08vA7/Q763M/TXLX3jMWKOfV3HFn656hBCjnePwXp+uJNIQ4+oxg5H7nr8yo2Tc3Umt9fzgajoLDSd488iozmlSgKeRoVy7hKAGuveGtFqqruNAYArNfAAACALXcpb2stcqNdyTGUPIK/uUBkEeEJGgomqFPZbkMNHqZqEPLa7cJdHIl6wiol3ziQKvvUm/8ya4y7tR9Mzay/cIAAAIAwU2/rquz6oQ1GVJVRsO47Ibes2Hcl8tZRC9cBDy5vIPhzPhsD3pxxXnc1gEUybWqkuO6q1XilE/qN/eAKFSDuQAAAgD0QMX768Ucuv2Eu/ZVkebKBBV7jo4seZyd+hKloFotU4mReU7kNq+oYG19pL07n1TN4SodVoykXPSLBowCKCvX");
        System.out.println(new String(buf).toString());
        buf = Base64.decode("P2/56wAAAjsAAAA3aWYtbW9kbntzaWdue3JzYS1wa2NzMS1zaGExfSxlbmNyeXB0e3JzYS1wa2NzMXYyLW9hZXB9fQAAAAgzZGVzLWNiYwAAAegYJSJacx5dZo5rvtyJEp5qFyBXDOkcGH/H4/dJuny1cWnP5eXOaYt1hwc6ZEUIq4bUISGuXSzmRb+mpXZdkAPPt2RLhy66FnwnERnbItyWsNHrMxT5/oug/TW1l+rh0m/46edQhkla+qpgt3ZCJfBRzwihKAAeQJIt18e7XmvVT5g14Xu5fulXPfKT/cPu6Ox1pwRrOTv2ooM8alM2+K+5uCaP9C3qhEhFcyZOsKoigJt8oIZJD7TBrb2adVfzjyNWXZLw5Lq+liWmGTePvf9Mkx+MgFAyIOT4gV391+Rit8ZjSQaJ5jtsSaqw/MgqtTCWz6aXAaLnxP579a+tVubfVQrGLAa6ztGjI/0DmzEH+OvOLfXljeaEPKXhOxTf2O7Pwn8MDBStJHPXPLZZnsoUyTCajnzxw/ohqxOtgE9nqqO1QFVF6Cd74yZlhQSScRKkBcUlqcenxtruEOvvZXgAc8T5UtfvF8AooI22zltyKZDFJx3vJD6TEoFQSq4zu8H4Eipr42HPpUvIFuVAJFlZepI/RVirsU6sDjh8do0vj9ZGdhdBaD8kR7lrPHAJkmROHljJhEI97YWUJZNXS9i63gVvplsi9/x6uEWjn8eNu08IXID82X+LbvEdmTWOhuaSIqyNjyVe7g==");
        System.out.println(new String(buf).toString());
    }

    /*
     *  ********  D E C O D I N G   M E T H O D S  ********
     */
    private static byte[] decode4to3(byte[] fourBytes) {
        byte[] outBuff1 = new byte[3];
        int count = decode4to3(fourBytes, 0, outBuff1, 0);
        byte[] outBuff2 = new byte[count];

        for (int i = 0; i < count; i++) {
            outBuff2[i] = outBuff1[i];
        }

        return outBuff2;
    }

    private static int decode4to3(byte[] source, int srcOffset,
                                  byte[] destination, int destOffset) {
        // Example: Dk==
        if (source[srcOffset + 2] == EQUALS_SIGN) {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) |
                    ((DECODABET[source[srcOffset + 1]] << 24) >>> 12);
            destination[destOffset] = (byte) (outBuff >>> 16);

            return 1;
        }
        // Example: DkL=
        else if (source[srcOffset + 3] == EQUALS_SIGN) {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) |
                    ((DECODABET[source[srcOffset + 1]] << 24) >>> 12) |
                    ((DECODABET[source[srcOffset + 2]] << 24) >>> 18);
            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);

            return 2;
        }
        // Example: DkLE
        else {
            int outBuff = ((DECODABET[source[srcOffset]] << 24) >>> 6) |
                    ((DECODABET[source[srcOffset + 1]] << 24) >>> 12) |
                    ((DECODABET[source[srcOffset + 2]] << 24) >>> 18) |
                    ((DECODABET[source[srcOffset + 3]] << 24) >>> 24);
            destination[destOffset] = (byte) (outBuff >> 16);
            destination[destOffset + 1] = (byte) (outBuff >> 8);
            destination[destOffset + 2] = (byte) (outBuff);

            return 3;
        }
    }

    // end decodeToBytes

    /*
     *  ********  E N C O D I N G   M E T H O D S  ********
     */
    private static byte[] encode3to4(byte[] threeBytes) {
        return encode3to4(threeBytes, 3);
    }

    // end encodeToBytes
    private static byte[] encode3to4(byte[] threeBytes, int numSigBytes) {
        byte[] dest = new byte[4];
        encode3to4(threeBytes, 0, numSigBytes, dest, 0);

        return dest;
    }

    private static byte[] encode3to4(byte[] source, int srcOffset,
                                     int numSigBytes, byte[] destination, int destOffset) {
        //           1         2         3
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
        //          >>18  >>12  >> 6  >> 0  Right shift necessary
        //                0x3f  0x3f  0x3f  Additional AND
        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff = ((numSigBytes > 0) ? ((source[srcOffset] << 24) >>> 8) : 0) |
                ((numSigBytes > 1) ? ((source[srcOffset + 1] << 24) >>> 16) : 0) |
                ((numSigBytes > 2) ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

        switch (numSigBytes) {
            case 3:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = ALPHABET[(inBuff) & 0x3f];

                return destination;

            case 2:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = ALPHABET[(inBuff >>> 6) & 0x3f];
                destination[destOffset + 3] = EQUALS_SIGN;

                return destination;

            case 1:
                destination[destOffset] = ALPHABET[(inBuff >>> 18)];
                destination[destOffset + 1] = ALPHABET[(inBuff >>> 12) & 0x3f];
                destination[destOffset + 2] = EQUALS_SIGN;
                destination[destOffset + 3] = EQUALS_SIGN;

                return destination;

            default:
                return destination;
        }

        // end switch
    }

    // end encode3to4

    /*
     *  ********  I N N E R   C L A S S   I N P U T S T R E A M  ********
     */
    public static class InputStream extends java.io.FilterInputStream {
        private byte[] buffer;

        // Small buffer holding converted data
        private boolean encode;

        // Encoding or decoding
        private int bufferLength;

        // Length of buffer (3 or 4)
        private int numSigBytes;

        // Number of meaningful bytes in the buffer
        private int position;

        // Current position in the buffer
        public InputStream(java.io.InputStream in) {
            this(in, Base64.DECODE);
        }

        // end constructor
        public InputStream(java.io.InputStream in, boolean encode) {
            super(in);
            this.encode = encode;
            this.bufferLength = encode ? 4 : 3;
            this.buffer = new byte[bufferLength];
            this.position = -1;
        }

        // end constructor
        public int read() throws java.io.IOException {
            // Do we need to get data?
            if (position < 0) {
                if (encode) {
                    byte[] b3 = new byte[3];
                    numSigBytes = 0;

                    for (int i = 0; i < 3; i++) {
                        try {
                            int b = in.read();

                            // If end of stream, b is -1.
                            if (b >= 0) {
                                b3[i] = (byte) b;
                                numSigBytes++;
                            }

                            // end if: not end of stream
                        }
                                // end try: read
                        catch (java.io.IOException e) {
                            // Only a problem if we got no data at all.
                            if (i == 0) {
                                throw e;
                            }
                        }

                        // end catch
                    }

                    // end for: each needed input byte
                    if (numSigBytes > 0) {
                        encode3to4(b3, 0, numSigBytes, buffer, 0);
                        position = 0;
                    }

                    // end if: got data
                }
                // end if: encoding
                // Else decoding
                else {
                    byte[] b4 = new byte[4];
                    int i = 0;

                    for (i = 0; i < 4; i++) {
                        int b = 0;

                        do {
                            b = in.read();
                        }
                        while ((b >= 0) &&
                                (DECODABET[b & 0x7f] < white_SPACE_ENC));

                        if (b < 0) {
                            break;

                            // Reads a -1 if end of stream
                        }

                        b4[i] = (byte) b;
                    }

                    // end for: each needed input byte
                    if (i == 4) {
                        numSigBytes = decode4to3(b4, 0, buffer, 0);
                        position = 0;
                    }

                    // end if: got four characters
                }

                // end else: decode
            }

            // end else: get data
            // Got data?
            if (position >= 0) {
                // End of relevant data?
                if (!encode && (position >= numSigBytes)) {
                    return -1;
                }

                int b = buffer[position++];

                if (position >= bufferLength) {
                    position = -1;
                }

                return b;
            }
            // end if: position >= 0
            // Else error
            else {
                return -1;
            }
        }

        // end read
        public int read(byte[] dest, int off, int len)
                throws java.io.IOException {
            int i;
            int b;

            for (i = 0; i < len; i++) {
                b = read();

                if (b < 0) {
                    return -1;
                }

                dest[off + i] = (byte) b;
            }

            // end for: each byte read
            return i;
        }

        // end read
    }

    // end inner class InputStream

    /*
     *  ********  I N N E R   C L A S S   O U T P U T S T R E A M  ********
     */
    public static class OutputStream extends java.io.FilterOutputStream {
        private byte[] buffer;
        private boolean encode;
        private int bufferLength;
        private int lineLength;
        private int position;

        public OutputStream(java.io.OutputStream out) {
            this(out, Base64.ENCODE);
        }

        // end constructor
        public OutputStream(java.io.OutputStream out, boolean encode) {
            super(out);
            this.encode = encode;
            this.bufferLength = encode ? 3 : 4;
            this.buffer = new byte[bufferLength];
            this.position = 0;
            this.lineLength = 0;
        }

        // end constructor
        public void close() throws java.io.IOException {
            this.flush();
            super.close();
            out.close();
            buffer = null;
            out = null;
        }

        // end close
        public void flush() throws java.io.IOException {
            if (position > 0) {
                if (encode) {
                    out.write(Base64.encode3to4(buffer, position));
                }
                // end if: encoding
                else {
                    throw new java.io.IOException("Base64 input not properly padded.");
                }

                // end else: decoding
            }

            // end if: buffer partially full
            super.flush();
            out.flush();
        }

        // end flush
        public void write(int theByte) throws java.io.IOException {
            buffer[position++] = (byte) theByte;

            if (position >= bufferLength) {
                if (encode) {
                    out.write(Base64.encode3to4(buffer, bufferLength));
                    lineLength += 4;

                    if (lineLength >= MAX_LINE_LENGTH) {
                        out.write(NEW_LINE);
                        lineLength = 0;
                    }

                    // end if: end o fline
                }
                // end if: encoding
                else {
                    out.write(Base64.decode4to3(buffer));
                }

                position = 0;
            }

            // end if: convert and flush
        }

        // end write
        public void write(byte[] theBytes, int off, int len)
                throws java.io.IOException {
            for (int i = 0; i < len; i++) {
                write(theBytes[off + i]);
            }

            // end for: each byte written
        }

        // end write
    }

    // end inner class OutputStream
}


// end class Base64
