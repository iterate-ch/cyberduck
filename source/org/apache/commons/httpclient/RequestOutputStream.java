/*
 * $Header$
 * $Revision$
 * $Date$
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;


/**
 * <p/>
 * {@link OutputStream} wrapper supporting the chunked transfer encoding.
 * </p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision$ $Date$
 */
public class RequestOutputStream
        extends OutputStream {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct an output stream wrapping the given stream.
     *
     * @param stream Wrapped input stream
     */
    public RequestOutputStream(OutputStream stream) {
        this.stream = stream;
    }

    /**
     * Construct an output stream wrapping the given stream.
     *
     * @param stream Wrapped input stream
     */
    public RequestOutputStream(OutputStream stream, boolean useChunking) {
        this.stream = stream;
        this.useChunking = useChunking;
    }

    // ------------------------------------------------------- Static Variables

    static private final Log wireLog = LogSource.getInstance("httpclient.wire");

    // ----------------------------------------------------- Instance Variables

    /**
     * Has this stream been closed?
     */
    private boolean closed = false;


    /**
     * The underlying input stream from which we should read data.
     */
    private OutputStream stream = null;


    /**
     * True if chunking is allowed.
     */
    private boolean useChunking = false;


    /**
     * True if printing a chunk.
     */
    private boolean writingChunk = false;


    /**
     * End chunk.
     */
    private byte endChunk[] = "\r\n".getBytes();


    /**
     * CRLF.
     */
    private byte crlf[] = "\r\n".getBytes();


    /**
     * 0.
     */
    private byte zero[] = "0".getBytes();


    /**
     * 1.
     */
    private byte one[] = "1".getBytes();


    // ------------------------------------------------------------- Properties


    /**
     * Use chunking flag setter.
     */
    public void setUseChunking(boolean useChunking) {
        this.useChunking = useChunking;
    }


    /**
     * Use chunking flag getter.
     */
    public boolean isUseChunking() {
        return useChunking;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Writes a <code>String</code> to the client,
     * without a carriage return-line feed (CRLF)
     * character at the end.
     *
     * @param s the <code>String</code to send to the client
     * @throws IOException if an input or output exception occurred
     */
    public void print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        int len = s.length();
        for (int i = 0; i < len; i++) {
            write(s.charAt(i));
        }
    }

    /**
     * Writes a carriage return-line feed (CRLF)
     * to the client.
     *
     * @throws IOException if an input or output exception occurred
     */
    public void println() throws IOException {
        print("\r\n");
    }

    /**
     * Writes a <code>String</code> to the client,
     * followed by a carriage return-line feed (CRLF).
     *
     * @param s the </code>String</code> to write to the client
     * @throws IOException if an input or output exception occurred
     */
    public void println(String s) throws IOException {
        print(s);
        println();
    }

    // -------------------------------------------- ServletOutputStream Methods

    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     * @throws IOException if an input/output error occurs
     */
    public void write(int b) throws IOException {
        if (useChunking) {
            stream.write(one, 0, one.length);
            stream.write(crlf, 0, crlf.length);
            stream.write(b);
            stream.write(endChunk, 0, endChunk.length);
            if (wireLog.isInfoEnabled()) {
                wireLog.info(">> byte 1 \\r\\n (chunk length \"header\")");
                wireLog.info(">> byte " + b + "\\r\\n (chunked byte)");
            }
        }
        else {
            stream.write(b);
            if (wireLog.isInfoEnabled()) {
                wireLog.info(">> byte " + b);
            }
        }
    }

    /**
     * Write the specified byte array.
     */
    public void write(byte[] b, int off, int len) throws IOException {
        if (useChunking) {
            byte chunkHeader[] =
                    (Integer.toHexString(len) + "\r\n").getBytes();
            stream.write(chunkHeader, 0, chunkHeader.length);
            stream.write(b, off, len);
            stream.write(endChunk, 0, endChunk.length);
            if (wireLog.isInfoEnabled()) {
                wireLog.info(">> byte(s)" + len + " \\r\\n (chunk length \"header\")");
                wireLog.info(">> \"" + new String(b, off, len) + "\"\\r\\n (chunked bytes)");
            }
        }
        else {
            stream.write(b, off, len);
            if (wireLog.isInfoEnabled() && len > 0) {
                wireLog.info(">> \"" + new String(b, off, len) + "\"");
            }
        }
    }

    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    public void close() throws IOException {

        if (useChunking) {
            // Write the final chunk.
            stream.write(zero, 0, zero.length);
            stream.write(crlf, 0, crlf.length);
            stream.write(endChunk, 0, endChunk.length);
            if (wireLog.isInfoEnabled()) {
                wireLog.info(">> byte 0 \\r\\n\\r\\n (final chunk)");
            }
        }
        super.close();
    }

}
