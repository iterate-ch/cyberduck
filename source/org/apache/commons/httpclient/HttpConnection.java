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

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * <p/>
 * An abstraction of an HTTP {@link InputStream} and {@link OutputStream}
 * pair, together with the relevant attributes.
 * </p>
 *
 * @author Rod Waldhoff
 * @version $Revision$ $Date$
 * @modified <a href="mailto:dkocher@cyberduck.ch">David Kocher</a>
 */
public class HttpConnection {
    // ----------------------------------------------------------- Constructors

    /**
     * Constructor.
     *
     * @param host the host I should connect to
     * @param port the port I should connect to
     */
    public HttpConnection(String host, int port) {
        this(null, -1, host, port, false);
    }

    /**
     * Constructor.
     *
     * @param host   the host I should connect to
     * @param port   the port I should connect to
     * @param secure when <tt>true</tt>, connect via HTTPS (SSL)
     */
    public HttpConnection(String host, int port, boolean secure) {
        this(null, -1, host, port, secure);
    }

    /**
     * Constructor.
     *
     * @param proxyHost the host I should proxy via
     * @param proxyPort the port I should proxy via
     * @param host      the host I should connect to
     * @param port      the port I should connect to
     */
    public HttpConnection(String proxyHost, int proxyPort, String host, int port) {
        this(proxyHost, proxyPort, host, port, false);
    }

    /**
     * Fully-specified constructor.
     *
     * @param proxyHost the host I should proxy via
     * @param proxyPort the port I should proxy via
     * @param host      the host I should connect to
     * @param port      the port I should connect to
     * @param secure    when <tt>true</tt>, connect via HTTPS (SSL)
     */
    public HttpConnection(String proxyHost, int proxyPort, String host, int port, boolean secure) {
        log.debug("HttpConnection.HttpConnection");
        _proxyHost = proxyHost;
        _proxyPort = proxyPort;
        _host = host;
        _port = port;
        _ssl = secure;
    }

    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Return my host.
     *
     * @return my host.
     */
    public String getHost() {
        return _host;
    }

    /**
     * Set my host.
     *
     * @param host the host I should connect to
     * @throws IllegalStateException if I am already connected
     */
    public void setHost(String host) throws IllegalStateException {
        assertNotOpen();
        _host = host;
    }

    /**
     * Return my port.
     *
     * @return my port.
     */
    public int getPort() {
        return _port;
    }

    /**
     * Set my port.
     *
     * @param port the port I should connect to
     * @throws IllegalStateException if I am already connected
     */
    public void setPort(int port) throws IllegalStateException {
        assertNotOpen();
        _port = port;
    }

    /**
     * Return my proxy host.
     *
     * @return my proxy host.
     */
    public String getProxyHost() {
        return _proxyHost;
    }

    /**
     * Set the host I should proxy through.
     *
     * @param host the host I should proxy through.
     * @throws IllegalStateException if I am already connected
     */
    public void setProxyHost(String host) throws IllegalStateException {
        assertNotOpen();
        _proxyHost = host;
    }

    /**
     * Return my proxy port.
     *
     * @return my proxy port.
     */
    public int getProxyPort() {
        return _proxyPort;
    }

    /**
     * Set the port I should proxy through.
     *
     * @param port the host I should proxy through.
     * @throws IllegalStateException if I am already connected
     */
    public void setProxyPort(int port) throws IllegalStateException {
        assertNotOpen();
        _proxyPort = port;
    }

    /**
     * Return <tt>true</tt> if I will (or I am) connected over a
     * secure (HTTPS/SSL) protocol.
     *
     * @return <tt>true</tt> if I will (or I am) connected over a
     *         secure (HTTPS/SSL) protocol.
     */
    public boolean isSecure() {
        return _ssl;
    }

    /**
     * Set whether or not I should connect over HTTPS (SSL).
     *
     * @param secure whether or not I should connect over HTTPS (SSL).
     * @throws IllegalStateException if I am already connected
     */
    public void setSecure(boolean secure) throws IllegalStateException {
        assertNotOpen();
        _ssl = secure;
    }

    /**
     * Return <tt>true</tt> if I am connected,
     * <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if I am connected
     */
    public boolean isOpen() {
        return _open;
    }

    /**
     * Return <tt>true</tt> if I am (or I will be)
     * connected via a proxy, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if I am (or I will be)
     *         connected via a proxy, <tt>false</tt> otherwise.
     */
    public boolean isProxied() {
        return (!(null == _proxyHost || 0 >= _proxyPort));
    }

    // --------------------------------------------------- Other Public Methods

    /**
     * Set my {@link Socket}'s timeout, via
     * {@link Socket#setSoTimeout}.
     *
     * @throws SocketException       - if there is an error in the underlying
     *                               protocol, such as a TCP error.
     * @throws IllegalStateException if I am not connected
     */
    public void setSoTimeout(int timeout) throws SocketException, IllegalStateException {
        log.debug("HttpConnection.setSoTimeout()");
        assertOpen();
        _socket.setSoTimeout(timeout);
    }

    /**
     * Open this connection to the current host and port
     * (via a proxy if so configured).
     */
    public void open() throws IOException {
        log.debug("HttpConnection.open()");
        assertNotOpen(); // ??? is this worth doing?
        try {
            if (null == _socket) {
                if (null == _proxyHost || 0 >= _proxyPort) {
                    if (_ssl) {
                        _socket = SSLSocketFactory.getDefault().createSocket(_host, _port);
                    }
                    else {
                        _socket = new Socket(_host, _port);
                    }
                }
                else {
                    if (_ssl) {
                        _socket = SSLSocketFactory.getDefault().createSocket(_proxyHost, _proxyPort);
                    }
                    else {
                        _socket = new Socket(_proxyHost, _proxyPort);
                    }
                }
            }
            _input = _socket.getInputStream();
            _output = _socket.getOutputStream();
            _open = true;
        }
        catch (IOException e) {
            // Connection wasn't opened properly
            // so close everything out
            closeSocketAndStreams();
            throw e;
        }
    }

    /**
     * Return a {@link RequestOutputStream}
     * suitable for writing (possibly chunked)
     * bytes to my {@link OutputStream}.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public OutputStream getRequestOutputStream() throws IOException, IllegalStateException {
        assertOpen();
        RequestOutputStream out = new RequestOutputStream(_output);
        return out;
    }

    /**
     * Return a {@link RequestOutputStream}
     * suitable for writing (possibly chunked) bytes to my
     * {@link OutputStream}.
     *
     * @param useChunking when <tt>true</tt> the chunked transfer-encoding will be used
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public OutputStream getRequestOutputStream(boolean useChunking) throws IOException, IllegalStateException {
        assertOpen();
        RequestOutputStream out = new RequestOutputStream(_output, useChunking);
        return out;
    }

    /**
     * Return a {@link ResponseInputStream}
     * suitable for reading (possibly chunked)
     * bytes from my {@link InputStream}.
     * <p/>
     * If the given {@link HttpMethod} contains
     * a <tt>Bookmark-Encoding: chunked</tt> header,
     * the returned stream will be configured
     * to read chunked bytes.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public InputStream getResponseInputStream(HttpMethod method) throws IOException, IllegalStateException {
        assertOpen();
        return new ResponseInputStream(_input, method);
    }

    /**
     * Write the specified bytes to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void write(byte[] data) throws IOException, IllegalStateException {
        log.debug("HttpConnection.write(byte[])");
        assertOpen();
        if (wireLog.isInfoEnabled() && (data.length > 0)) {
            wireLog.info(">> \"" + new String(data) + "\"");
        }
        try {
            _output.write(data);
        }
        catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("HttpConnection: Exception while writing data", e);
            }
            throw e;
        }
    }


    /**
     * Write the specified bytes, followed by
     * <tt>"\r\n".getBytes()</tt> to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void writeLine(byte[] data) throws IOException, IllegalStateException {
        log.debug("HttpConnection.writeLine(byte[])");
        assertOpen();
        if (wireLog.isInfoEnabled() && (data.length > 0)) {
            wireLog.info(">> \"" + new String(data) + "\"");
        }
        _output.write(data);
        writeLine();
    }

    /**
     * Write <tt>"\r\n".getBytes()</tt> to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void writeLine() throws IOException, IllegalStateException {
        log.debug("HttpConnection.writeLine()");
        wireLog.info(">> \\r\\n");
        _output.write(CRLF);
    }

    /**
     * Write the specified String (as bytes) to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void print(String data) throws IOException, IllegalStateException {
        write(data.getBytes());
    }

    /**
     * Write the specified String (as bytes), followed by
     * <tt>"\r\n".getBytes()</tt> to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void printLine(String data) throws IOException, IllegalStateException {
        writeLine(data.getBytes());
    }

    /**
     * Write <tt>"\r\n".getBytes()</tt> to my output stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public void printLine() throws IOException, IllegalStateException {
        writeLine();
    }

    /**
     * Read up to <tt>"\r\n"</tt> from my (unchunked) input stream.
     *
     * @throws IllegalStateException if I am not connected
     * @throws IOException           if an I/O problem occurs
     */
    public String readLine() throws IOException, IllegalStateException {
        log.debug("HttpConnection.readLine()");
        assertOpen();
        StringBuffer buf = new StringBuffer();
        for (; ;) {
            int ch = _input.read();
//            log.debug("HttpConnection.readLine() read " + ch);
            if (ch < 0) {
                if (buf.length() == 0) {
                    return null;
                }
                else {
                    break;
                }
            }
            else if (ch == '\r') {
//                log.debug("HttpConnection.readLine() found \\r, continuing");
                continue;
            }
            else if (ch == '\n') {
//                log.debug("HttpConnection.readLine() found \\n, breaking");
                break;
            }
            buf.append((char)ch);
        }
        if (wireLog.isInfoEnabled() && buf.length() > 0) {
            wireLog.info("<< \"" + buf.toString() + "\" [\\r\\n]");
        }
        return (buf.toString());
    }

    /**
     * Shutdown my {@link Socket}'s output, via
     * {@link Socket#shutdownOutput}.
     *
     * @throws IOException if an I/O problem occurs
     */
    public void shutdownOutput() throws IOException {
        log.debug("HttpConnection.shutdownOutput()");
        try {
            // Socket.shutdownOutput is a JDK 1.3
            // method. We'll use reflection in case
            // we're running in an older VM
            Class[] paramsClasses = new Class[0];
            Method shutdownOutput = _socket.getClass().getMethod
                    ("shutdownOutput", paramsClasses);
            Object[] params = new Object[0];
            shutdownOutput.invoke(_socket, params);
        }
        catch (Exception e) {
            // Ignore, and hope everything goes right
        }
        // close output stream?
    }

    /**
     * Close my socket and streams.
     *
     * @throws IOException if an I/O problem occurs
     */
    public void close() throws IOException {
        log.debug("HttpConnection.close()");
        closeSocketAndStreams();
    }

    // ------------------------------------------------------ Protected Methods


    /**
     * Close everything out.
     */
    protected void closeSocketAndStreams() {
        log.debug("HttpConnection.closeSocketAndStreams()");
        try {
            _input.close();
        }
        catch (Exception e) {
            // ignored
        }
        _input = null;

        try {
            _output.close();
        }
        catch (Exception e) {
            // ignored
        }
        _output = null;

        try {
            _socket.close();
        }
        catch (Exception e) {
            // ignored
        }
        _socket = null;
        _open = false;
    }

    /**
     * Throw an {@link IllegalStateException} if I am connected.
     */
    protected void assertNotOpen() throws IllegalStateException {
        if (_open) {
            throw new IllegalStateException("Connection is open");
        }
    }

    /**
     * Throw an {@link IllegalStateException} if I am not connected.
     */
    protected void assertOpen() throws IllegalStateException {
        if (!_open) {
            throw new IllegalStateException("Connection is not open");
        }
    }

    // ------------------------------------------------------------- Attributes

    /**
     * <tt>org.apache.commons.httpclient.HttpConnection</tt> log.
     */
    static private final Log log = LogSource.getInstance("org.apache.commons.httpclient.HttpConnection");
    /**
     * <tt>httpclient.wire</tt> log.
     */
    static private final Log wireLog = LogSource.getInstance("httpclient.wire");
    /**
     * My host.
     */
    private String _host = null;
    /**
     * My port.
     */
    private int _port = -1;
    /**
     * My proxy host.
     */
    private String _proxyHost = null;
    /**
     * My proxy port.
     */
    private int _proxyPort = -1;
    /**
     * My client Socket.
     */
    private Socket _socket = null;
    /**
     * My InputStream.
     */
    private InputStream _input = null;
    /**
     * My OutputStream.
     */
    private OutputStream _output = null;
    /**
     * Whether or not I am connected.
     */
    private boolean _open = false;
    /**
     * Whether or not I am/should connect via SSL.
     */
    private boolean _ssl = false;
    /**
     * <tt>"\r\n"</tt>, as bytes.
     */
    private static final byte[] CRLF = "\r\n".getBytes();

}
