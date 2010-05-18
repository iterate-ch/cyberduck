package ch.cyberduck.core.io;

/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * This class wraps an input stream, replacing all occurrences
 * of &lt;CR&gt;&lt;LF&gt; (carriage return followed by a linefeed),
 * which is the NETASCII standard for representing a newline, with the
 * local line separator representation.  You would use this class to
 * implement ASCII file transfers requiring conversion from NETASCII.
 * <p/>
 * <p/>
 *
 * @author Daniel F. Savarese
 */

public class FromNetASCIIInputStream extends PushbackInputStream {

    private boolean _noConversionRequired;
    private String _lineSeparator;
    private byte[] _lineSeparatorBytes;

    private int __length = 0;

    /**
     * Returns true if the NetASCII line separator differs from the system
     * line separator, false if they are the same.  This method is useful
     * to determine whether or not you need to instantiate a
     * FromNetASCIIInputStream object.
     * <p/>
     *
     * @return True if the NETASCII line separator differs from the local
     *         system line separator, false if they are the same.
     *         *
     */
    public final boolean isConversionRequired() {
        return !_noConversionRequired;
    }

    /**
     * Creates a FromNetASCIIInputStream instance that wraps an existing
     * InputStream.
     * *
     */
    public FromNetASCIIInputStream(InputStream input, String lineSeparator) {
        super(input, lineSeparator.getBytes().length + 1);
        _lineSeparator = lineSeparator;
        _lineSeparatorBytes = lineSeparator.getBytes();
        _noConversionRequired = lineSeparator.equals("\r\n");
    }


    private int __read() throws IOException {
        int ch;

        ch = super.read();

        if(ch == '\r') {
            ch = super.read();
            if(ch == '\n') {
                unread(_lineSeparatorBytes);
                ch = super.read();
                // This is a kluge for read(byte[], ...) to read the right amount
                --__length;
            }
            else {
                if(ch != -1) {
                    unread(ch);
                }
                return '\r';
            }
        }

        return ch;
    }


    /**
     * Reads and returns the next byte in the stream.  If the end of the
     * message has been reached, returns -1.  Note that a call to this method
     * may result in multiple reads from the underlying input stream in order
     * to convert NETASCII line separators to the local line separator format.
     * This is transparent to the programmer and is only mentioned for
     * completeness.
     * <p/>
     *
     * @return The next character in the stream. Returns -1 if the end of the
     *         stream has been reached.
     * @throws IOException If an error occurs while reading the underlying
     *                     stream.
     *                     *
     */
    @Override
    public int read() throws IOException {
        if(_noConversionRequired) {
            return super.read();
        }

        return __read();
    }


    /**
     * Reads the next number of bytes from the stream into an array and
     * returns the number of bytes read.  Returns -1 if the end of the
     * stream has been reached.
     * <p/>
     *
     * @param buffer The byte array in which to store the data.
     * @return The number of bytes read. Returns -1 if the
     *         end of the message has been reached.
     * @throws IOException If an error occurs in reading the underlying
     *                     stream.
     *                     *
     */
    @Override
    public int read(byte buffer[]) throws IOException {
        return read(buffer, 0, buffer.length);
    }


    /**
     * Reads the next number of bytes from the stream into an array and returns
     * the number of bytes read.  Returns -1 if the end of the
     * message has been reached.  The characters are stored in the array
     * starting from the given offset and up to the length specified.
     * <p/>
     *
     * @param buffer The byte array in which to store the data.
     * @param offset The offset into the array at which to start storing data.
     * @param length The number of bytes to read.
     * @return The number of bytes read. Returns -1 if the
     *         end of the stream has been reached.
     * @throws IOException If an error occurs while reading the underlying
     *                     stream.
     *                     *
     */
    @Override
    public int read(byte buffer[], int offset, int length) throws IOException {
        int ch, off;

        if(length < 1) {
            return 0;
        }

        ch = available();

        __length = (length > ch ? ch : length);

        // If nothing is available, block to read only one character
        if(__length < 1) {
            __length = 1;
        }

        if(_noConversionRequired) {
            return super.read(buffer, offset, __length);
        }

        if((ch = __read()) == -1) {
            return -1;
        }

        off = offset;

        do {
            buffer[offset++] = (byte) ch;
        }
        while(--__length > 0 && (ch = __read()) != -1);


        return (offset - off);
    }


    // PushbackInputStream in JDK 1.1.3 returns the wrong thing

    /**
     * Returns the number of bytes that can be read without blocking EXCEPT
     * when newline conversions have to be made somewhere within the
     * available block of bytes.  In other words, you really should not
     * rely on the value returned by this method if you are trying to avoid
     * blocking.
     * *
     */
    @Override
    public int available() throws IOException {
        return (buf.length - pos) + in.available();
    }
}
