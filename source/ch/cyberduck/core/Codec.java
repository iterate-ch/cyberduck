package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CharsetDecoder;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

/**
 * @version $Id$
 */
public class Codec {
    private static Logger log = Logger.getLogger(Codec.class);

    private Codec() {
        super();
    }

    /**
     * Constructs a new String by decoding the specified array of bytes using the specified charset.
     * The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.
     */
    public static String decode(String text, String encoding) {
        return Codec.decode(text.getBytes(), encoding);
    }

    /**
     * Constructs a new String by decoding the specified array of bytes with the default charset.
     * The length of the new String is a function of the charset, and hence may not be equal to the length of the byte array.
     */
    public static String decode(byte[] text, String encoding) {
        try {
            Charset charset = Charset.forName(encoding);
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer buffer = decoder.decode(ByteBuffer.wrap(text));
            return buffer.toString();
        }
        catch (CharacterCodingException e) {
            log.error(e.getMessage());
        }
        return new String(text);
    }


    /**
     * Encodes this String into a sequence of bytes using the named charset, storing the result into a new byte array.
     * If the encoding with the desired encoding fails, we return the bytes with the default encoding
     *
     * @param text     The string to encode
     * @param encoding The desired encoding
     */
    public static byte[] encode(String text, String encoding) {
        try {
            Charset charset = Charset.forName(encoding);
            CharsetEncoder encoder = charset.newEncoder();
            ByteBuffer buffer = encoder.encode(CharBuffer.wrap(text));
            return buffer.array();
        }
        catch (CharacterCodingException e) {
            log.error(e.getMessage());
        }
        return text.getBytes();
    }
}