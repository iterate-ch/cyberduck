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

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <p>
 * An HTTP "user-agent", containing an {@link HttpState} and
 * one or more {@link HttpConnection}s, to which
 * {@link HttpMethod}s can be applied.
 * </p>
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @version $Revision$ $Date$
 * @modified <a href="mailto:dkocher@cyberduck.ch">David Kocher</a>
 */
public class HttpClient {


    // -------------------------------------------------------------- Constants

    /** <tt>org.apache.commons.httpclient.HttpClient</tt> log. */
    static private final Log log = LogSource.getInstance("org.apache.commons.httpclient.HttpClient");

    // ----------------------------------------------------------- Constructors

    /**
     * Constructor.
     */
    public HttpClient() {
	super();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * My {@link HttpConnection connection}.
     */
    private HttpConnection connection = null;

    /**
     * My {@link HttpState state}.
     */
    private HttpState state;

    // ------------------------------------------------------------- Properties

    /**
     * Get my {@link HttpState state}.
     */
    public HttpState getState() {
        if(null == state) {
            state = new HttpState();
        }
        return state;
    }

    /**
     * Set my {@link HttpState state}.
     */
    public void setState(HttpState state) {
        this.state = state;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Start an HTTP session with the server at the given
     * <i>host</i> and <i>port</i>.
     * @param host the host to connect to
     * @param port the port to connect to
     */
    public void connect(String host, int port) throws IOException {
        this.connect(host,port,false);
    }

    /**
     * Start an HTTP or HTTPS session with the server at the given
     * <i>host</i> and <i>port</i>.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param https when <code>true</code>, create an HTTPS session
     */
    public void connect(String host, int port, boolean https) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug("HttpClient.connect(String,int,boolean): Host:" + host + " Port:" + port + " HTTPS:" + https);
        }
        connection = new HttpConnection(host,port,https);
	connection.open();
    }

    /**
     * Start an HTTP session with the server at the given
     * <i>host</i> and <i>port</i> using the given default
     * default credentials.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param creds the default credentials to use
     */
    public void connect(String host, int port, Credentials creds) throws IOException {
        this.connect(host,port,creds,false);
    }


    /**
     * Start an HTTP or HTTPS session with the server at the given
     * <i>host</i> and <i>port</i> using the given default
     * default credentials.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param creds the default credentials to use
     * @param https when <code>true</code>, create an HTTPS session
     */
    public void connect(String host, int port, Credentials creds, boolean https) throws IOException {
        log.debug("HttpClient.connect(String,int,Credentials,boolean): Host:" +
                  host + " Port:" + port + " Credentials:" + creds +
                  " HTTPS:" + https);
        this.getState().setCredentials(null,creds);
        connection = new HttpConnection(host,port,https);
    }

    /**
     * Start an HTTP or HTTPS session with the server specified
     * by the protocol, host and port of the given
     * <i>url</i>.
     * <p>
     * Note that everything but the protocol, host and port of the
     * given <i>url</i> is ignored.
     * </p>
     * @param url the {@link URL URL} from which the protocol, host,
     *            and port of the session are determined
     */
    public void connect(URL url) throws IOException {
      if("https".equalsIgnoreCase(url.getProtocol())) {
        this.connect(url.getHost(), url.getPort() == -1 ? 443 : url.getPort(),true);
      }
	else if("http".equalsIgnoreCase(url.getProtocol())) {
        this.connect(url.getHost(), url.getPort() == -1 ? 80 : url.getPort(),false);
      }
	else {
          throw new IllegalArgumentException("Protocol " + url.getProtocol()
                                             + " not supported in URL " + url);
      }
    }

    /**
     * Start an HTTP or HTTPS session with the server specified
     * by the protocol, host and port of the given
     * <i>url</i>, using the given credentials by default.
     * <p>
     * Note that everything but the protocol, host and port of the
     * given <i>url</i> is ignored.
     * </p>
     * @param creds the default credentials to use
     * @param url the {@link URL URL} from which the protocol, host,
     *            and port of the session are determined
     */
    public void connect(URL url, Credentials creds) throws IOException {
        this.getState().setCredentials(null,creds);
        this.connect(url);
    }


    /**
     * Start an HTTP session with the server specified
     * by the given <i>host</i> and <i>port</i>
     * via the given <i>proxyhost</i> and <i>proxyport</i>.
     * @param host the host to connect to
     * @param port the port to connect to
     * @param proxyhost the proxy host to connect via
     * @param proxyport the proxy port to connect via
     */
    public void connect(String host, int port, String proxyhost, int proxyport)  throws IOException {
        connection = new HttpConnection(proxyhost,proxyport,host,port,false);
	connection.open();
    }

    /**
     * Execute the given {@link HttpMethod} using my current
     * {@link HttpConnection connection} and {@link HttpState}.
     *
     * @param method the {@link HttpMethod} to execute
     * @return the method's response code
     *
     * @throws IOException if an I/O error occurs
     * @throws HttpException if a protocol exception occurs
     */
    public synchronized int executeMethod(HttpMethod method) throws IOException, HttpException  {
        if(!connection.isOpen()) {
            connection.open();
        }
        return method.execute(getState(),connection);
    }

    /*
    public java.io.InputStream getBinary(HttpMethod method) throws IOException {
        return this.getInputStream(method);
    }
     */
    
    public InputStream getInputStream(HttpMethod method) throws IOException {
        if(!connection.isOpen()) {
            connection.open();
        }
        return method.getInputStream(getState(),connection);
    }

    public boolean isAlive() {
        if(connection == null)
            return false;
        return connection.isOpen();
    }
    
    /**
     * End the current session, closing my the associated
     * {@link HttpConnection connection} if any.
     */
    public void quit() throws IOException {
        log.debug("HttpClient.quit()");
        if(null != connection) {
            connection.close();
            connection = null;
        }
    }
}
