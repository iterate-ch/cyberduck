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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * <p>An abstract base implementation of {@link HttpMethod}.</p>
 * <p>At minimum, subclasses will need to override</p>
 * <ul><dl>
 *  <dt>{@link #getName}</dt>
 *  <dd>to return the approriate name for this method</dd>
 * </dl></ul>
 * <p>When a method's request may contain a body,
 * subclasses will typically want to override:</p>
 * <ul><dl>
 *  <dt>{@link #getRequestContentLength}</dt>
 *  <dd>to indicate the length (in bytes) of that body</dd>
 *  <dt>{@link #writeRequestBody writeRequestBody(HttpState,HttpConnection)}</dt>
 *  <dd>to write the body</dd>
 * </dl></ul>
 * <p>When a method requires additional request headers,
 * subclasses will typically want to override:</p>
 * <ul><dl>
 *  <dt>{@link #addRequestHeaders addRequestHeaders(HttpState,HttpConnection)}</dt>
 *  <dd>to write those headers</dd>
 * </dl></ul>
 * <p>When a method expects specific response headers,
 * subclasses may want to override:</p>
 * <ul><dl>
 *  <dt>{@link #processResponseHeaders processResponseHeaders(HttpState,HttpConnection)}</dt>
 *  <dd>to handle those headers</dd>
 * </dl></ul>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @version $Revision$ $Date$
 * @modified <a href="mailto:dkocher@cyberduck.ch">David Kocher</a>
 */
public abstract class HttpMethodBase implements HttpMethod {

    // ----------------------------------------------------------- Constructors

    /**
     * No-arg constructor.
     */
    public HttpMethodBase() {
    }

    /**
     * Path-specifying constructor.
     *
     * @param path my path
     */
    public HttpMethodBase(String path) {
        this.setPath(path);
    }

    // ------------------------------------------- Property Setters and Getters

    /**
     * Obtain the name of this method, suitable for use in the "request line",
     * for example <tt>GET</tt> or <tt>POST</tt>.
     * @return the name of this method
     */
    public abstract String getName();

    /**
     * Set the path part of my request.
     * @param path the path to request
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the path part of my request.
     * @return the path to request
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Set the specified request header, overwriting any
     * previous value.
     * Note that header-name matching is case-insensitive.
     * @param headerName the header's name
     * @param headerValue the header's value
     */
    public void setRequestHeader(String headerName, String headerValue) {
        Header header = new Header(headerName, headerValue);
        requestHeaders.put(headerName.toLowerCase(),header);
    }

    /**
     * Set the specified request header, overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     * @param header the header
     */
    public void setRequestHeader(Header header) {
        requestHeaders.put(header.getName().toLowerCase(),header);
    }

    /**
     * Add the specified request header, NOT overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     * @param headerName the header's name
     * @param headerValue the header's value
     */
    public void addRequestHeader(String headerName, String headerValue) {
        // "It must be possible to combine the multiple header fields into
        // one "field-name: field-value" pair, without changing the
        // semantics of the message, by appending each subsequent field-value
        // to the first, each separated by a comma."
        //   - HTTP/1.0 (4.3)
        Header header = (Header)(requestHeaders.get(headerName.toLowerCase()));
        if(null == header) {
            header = new Header(headerName, headerValue);
        } else {
            header.setValue( (null == header.getValue() ? "" : header.getValue()) +
                             ", " +
                             (null == headerValue ? "" : headerValue));
        }
        requestHeaders.put(headerName.toLowerCase(),header);
    }

    /**
     * Add the specified request header, NOT overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     * @param header the header
     */
    public void addRequestHeader(Header header) {
        // "It must be possible to combine the multiple header fields into
        // one "field-name: field-value" pair, without changing the
        // semantics of the message, by appending each subsequent field-value
        // to the first, each separated by a comma."
        //   - HTTP/1.0 (4.3)
        Header orig = (Header)(requestHeaders.get(header.getName().toLowerCase()));
        if(null == orig) {
            orig = header;
        } else {
            orig.setValue( (null == orig.getValue() ? "" : orig.getValue()) +
                           ", " +
                           (null == header.getValue() ? "" : header.getValue()));
        }
        requestHeaders.put(orig.getName().toLowerCase(),orig);
    }

    /**
     * Get the request header associated with the given name.
     * Note that header-name matching is case insensitive.
     * @param headerName the header name
     * @return the header
     */
    public Header getRequestHeader(String headerName) {
        return (Header)(requestHeaders.get(headerName.toLowerCase()));
    }

    /**
     * Remove the request header associated with the given name.
     * Note that header-name matching is case insensitive.
     * @param headerName the header name
     * @return the header
     */
    public void removeRequestHeader(String headerName) {
        requestHeaders.remove(headerName.toLowerCase());
    }

    /**
     * Whether or not I should automatically follow
     * HTTP redirects (status code 302, etc.)
     * @return <tt>true</tt> if I will automatically follow HTTP redirects
     */
    public boolean getFollowRedirects() {
        return this.followRedirects;
    }

    /**
     * Set whether or not I should automatically follow
     * HTTP redirects (status code 302, etc.)
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Set my query string.
     * @param queryString the query string
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Set my query string.
     * @param params an array of {@link NameValuePair}s
     *               to add as query string parameterss
     */
    public void setQueryString(NameValuePair[] params) {
        StringBuffer buf = new StringBuffer();
        boolean needAmp = false;
        for(int i=0;i<params.length;i++) {
            if(needAmp) {
                buf.append("&");
            } else {
                needAmp = true;
            }
            if(null != params[i].getName()) {
                buf.append(URIUtil.encode(params[i].getName()));
            }
            if(null != params[i].getValue()) {
                buf.append("=");
                buf.append(URIUtil.encode(params[i].getValue()));
            }
        }
        queryString = buf.toString();
    }

    /**
     * Get my query string.
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Return an array of my request headers.
     */
    public Header[] getRequestHeaders() {
        return (Header[])(requestHeaders.values().toArray(new Header[requestHeaders.size()]));
    }

    // ---------------------------------------------------------------- Queries

    /**
     * Confirm that I am ready to execute.
     * <p>
     * This implementation always returns <tt>true</tt>.
     * @return <tt>true</tt>
     */
    public boolean validate() {
        return true;
    }

    /**
     * Return the status code associated with the latest response.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Return the status text (or "reason phrase") associated with the latest response.
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     * Return an array my response headers.
     */
    public Header[] getResponseHeaders() {
        return (Header[])(responseHeaders.values().toArray(new Header[responseHeaders.size()]));
    }

    /**
     * Return the specified response headers.
     */
    public Header getResponseHeader(String headerName) {
        return (Header)(responseHeaders.get(headerName.toLowerCase()));
    }

    /**
     * Return my response body, if any,
     * as a byte array.
     * Otherwise return <tt>null</tt>.
     */
    public byte[] getResponseBody() {
        return responseBody;
    }

    /**
     * Return my response body, if any,
     * as a {@link String}.
     * Otherwise return <tt>null</tt>.
     */
    public String getResponseBodyAsString() {
        return null == responseBody ? null : new String(responseBody);
    }

    /**
     * Return my response body, if any,
     * as an {@link InputStream}.
     * Otherwise return <tt>null</tt>.
     */
    public InputStream getResponseBodyAsStream() throws IOException {
        return null == responseBody ? null : new ByteArrayInputStream(responseBody);
    }

    /**
     * Return <tt>true</tt> if I have been {@link #execute executed}
     * but not recycled.
     */
    public boolean hasBeenUsed() {
       return used;
    }


    // --------------------------------------------------------- Action Methods


    /**
     * Execute this method.
     *
     * @param state {@link HttpState} information to associate with this request
     * @param connection the {@link HttpConnection} to write to/read from
     *
     * @throws IOException if an I/O error occurs
     * @throws HttpException  if an protocol exception occurs
     *
     * @return the integer status code if one was obtained, or <tt>-1</tt>
     */
    public int execute(HttpState state, HttpConnection connection) throws HttpException, IOException {
        log.debug("HttpMethodBase.execute(HttpState,HttpConnection,HashMap)");

        if(hasBeenUsed()) {
            throw new HttpException("Already used, but not recycled.");
        }

        if(!validate()) {
            throw new HttpException("Not valid");
        }

        Set visited = new HashSet();
        Set realms = new HashSet();

        while(true) {
            visited.add(connection.getHost() + ":" + connection.getPort() + "|" + HttpMethodBase.generateRequestLine(connection, getName(),getPath(),getQueryString(),(http11 ? "HTTP/1.1" : "HTTP/1.0")));

            log.debug("HttpMethodBase.execute(): looping.");

            if(!connection.isOpen()) {
                log.debug("HttpMethodBase.execute(): opening connection.");
                connection.open();
            }

            writeRequest(state,connection);
            used = true;

            // need to close output?, but when?

            readResponse(state,connection);

            if(HttpStatus.SC_CONTINUE == statusCode) {
                if(!bodySent) {
                    bodySent = writeRequestBody(state,connection);
                    readResponse(state,connection);
                } else {
                    log.warn("HttpMethodBase.execute(): received 100 response, but I've already sent the response.");
                    break;
                }
            }

                /*
            if(!http11) {
                log.debug("HttpMethodBase.execute(): closing connection since we're using HTTP/1.0");
                connection.close();
                //***
                throw new HttpException("Closing connection since we're using HTTP/1.0.");
            }
            else {
                Header connectionHeader = getResponseHeader("connection");
                if(null != connectionHeader && "close".equalsIgnoreCase(connectionHeader.getValue())) {
                    log.debug("HttpMethodBase.execute(): closing connection since \"Connection: close\" header found.");
                    connection.close();
                    //TODO: we don't care if the connection isn't persistent. 
                    throw new HttpException("Closing connection since \"Connection: close\" header found.");
                }
            }
                 */

            if(HttpStatus.SC_UNAUTHORIZED == statusCode) {
                Header wwwauth = getResponseHeader("WWW-Authenticate");
                if(null != wwwauth) {
                    String pathAndCreds = getPath() + ":" + wwwauth.getValue();
                    if(realms.contains(pathAndCreds)) {
                        if(log.isInfoEnabled()) {
                            log.info("Already tried to authenticate to \"" + wwwauth.getValue() + "\" but still receiving " + HttpStatus.SC_UNAUTHORIZED + ".");
                        }
                        break;
                    } else {
                        realms.add(pathAndCreds);
                    }

                    boolean authenticated = false;
                    try {
                        authenticated = Authenticator.authenticate(this,state);
                    } catch(HttpException e) {
                        // ignored
                    }
                    if(!authenticated) {
                        // won't be able to authenticate to this challenge
                        // without additional information
                        if(log.isDebugEnabled()) {
                            log.debug("HttpMethodBase.execute(): Server demands authentication credentials, but none are available, so aborting.");
                        }
                        break;
                    } else {
                        if(log.isDebugEnabled()) {
                            log.debug("HttpMethodBase.execute(): Server demanded authentication credentials, will try again.");
                        }
                        // let's try it again, using the credentials
                        continue;
                    }
                }
            }
            else if(HttpStatus.SC_MOVED_TEMPORARILY == statusCode ||
               HttpStatus.SC_MOVED_PERMANENTLY == statusCode ||
               HttpStatus.SC_TEMPORARY_REDIRECT == statusCode) {
                if(getFollowRedirects()) {
                    //
                    // Note that we cannot current support
                    // redirects that change the HttpConnection
                    // parameters (host, port, protocol)
                    // because we don't yet have a good way to
                    // get the new connection.
                    //
                    // For the time being, we just return
                    // the 302 response, and allow the user
                    // agent to resubmit if desired.
                    //
                    Header location = getResponseHeader("location");
                    if(location != null) {
                        URL url = null;
                        try {
                            url = new URL(location.getValue());
                        }
                        catch(MalformedURLException e) {
                            log.error("Exception while parsing location header \"" + location + "\"",e);
                            throw new HttpException("Exception while parsing location header \"" + location + "\"");
                        }
                        if("http".equalsIgnoreCase(url.getProtocol())) {
                            if(connection.isSecure()) {
                                log.info("Server is attempting to redirect an HTTPS request to an HTTP one.");
                                throw new HttpException("Server is attempting to redirect an HTTPS request to an HTTP one.");
                            }
                        }
                        else if("https".equalsIgnoreCase(url.getProtocol())) {
                            if(!connection.isSecure()) {
                                throw new HttpException("Server is attempting to convert an HTTP request to an HTTPS one, which is currently not supported.", statusCode);
                            }
                        }
                        if(!connection.getHost().equalsIgnoreCase(url.getHost())) {
                                throw new HttpException("Server is attempting to redirect a different host, which is currently not supported.", statusCode);
                        }
                        if(url.getPort() == -1) {
                            if(connection.isSecure()) {
                                if(connection.getPort() != 443) {
                                    throw new HttpException("Server is attempting to redirect a different port, which is currently not supported.", statusCode);
                                }
                            }
                            else {
                                if(connection.getPort() != 80) {
                                    throw new HttpException("Server is attempting to redirect a different port, which is currently not supported.", statusCode);
                                }
                            }
                        }
                        else if(connection.getPort() != url.getPort()) {
                            throw new HttpException("Server is attempting to redirect a different port, which is currently not supported.", statusCode);
                        }
                        String absolutePath = url.getPath();
                        if(null == absolutePath) {
                            absolutePath = "/";
                        }
                        String qs = url.getQuery();

                        // if we haven't already, let's try it again with the new path
                        if(visited.contains(connection.getHost() + ":" + connection.getPort() + "|" + HttpMethodBase.generateRequestLine(connection, getName(),absolutePath,qs,(http11 ? "HTTP/1.1" : "HTTP/1.0")))) {
                            throw new HttpException("Redirect going into a loop, visited \"" + absolutePath + "\" already.");
                        }
                        else {
                            if(log.isDebugEnabled()) {
                                log.debug("Changing path from \"" + getPath() + "\" to \"" + absolutePath + "\" in response to " + statusCode + " response.");
                                log.debug("Changing query string from \"" + getQueryString() + "\" to \"" + qs + "\" in response to " + statusCode + " response.");
                            }
                            setPath(URIUtil.decode(absolutePath));
                            setQueryString(qs);
                            continue;
                        }
                    }
                    else {
                        // got a redirect response, but no location header
                        if(log.isInfoEnabled()) {
                            log.info("HttpMethodBase.execute(): Received " + statusCode + " response, but no \"Location\" header. Returning " + statusCode + ".");
                        }
                        break;
                    }
                }
                else {
                    // got a redirect response,
                    // but followRedirects is false
                    log.info("HttpMethodBase.execute(): Received " + statusCode + " response, but followRedirects is false. Returning " + statusCode + ".");
                    break;
                }
            }
            else {
                // neither an UNAUTHORIZED nor a redirect response
                // so exit
                break;
            }
        }

        return statusCode;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * <p>Writes my request to the given {@link HttpConnection}.</p>
     * <p>The request is written according to the following logic:</p>
     * <ol>
     * <li>
     *   {@link #writeRequestLine writeRequestLine(HttpState, HttpConnection)}
     *   is invoked to write the request line.
     * </li>
     * <li>
     *   {@link #writeRequestHeaders writeRequestHeaders(HttpState, HttpConnection)}
     *   is invoked to write the associated headers.</li>
     * <li>
     *   <tt>\r\n</tt> is sent to close the head part of the request.
     * </li>
     * <li>
     *  {@link #writeRequestBody writeRequestBody(HttpState, HttpConnection)}
     *  is invoked to write the body part of the request.
     * </li>
     * </ol>
     * <p>Subclasses may want to override one or more of the above methods to
     * to customize the processing. (Or they may choose to override this method
     * if dramatically different processing is required.)</p>
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to write the request to
     */
    protected void writeRequest(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.writeRequest(HttpState,HttpConnection)");
        writeRequestLine(state,conn);
        writeRequestHeaders(state,conn);
        conn.writeLine(); // close head
        bodySent = writeRequestBody(state,conn);
    }


    /**
     * Writes the "request line" to the given {@link HttpConnection}.
     * <p>
     * Subclasses may want to override this method to
     * to customize the processing.
     *
     * @see #generateRequestLine
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to write to
     */
    protected void writeRequestLine(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.writeRequestLine(HttpState,HttpConnection)");
        String requestLine = HttpMethodBase.generateRequestLine(conn, getName(),getPath(),getQueryString(),(http11 ? "HTTP/1.1" : "HTTP/1.0"));
        conn.print(requestLine);
    }

    /**
     * Writes the request headers to the given {@link HttpConnection}.
     * <p>
     * This implementation invokes
     * {@link #addRequestHeaders addRequestHeaders(HttpState,HttpConnection)},
     * and then writes each header to the request stream.
     * <p>
     * Subclasses may want to override this method to
     * to customize the processing.
     *
     * @see #addRequestHeaders
     * @see #getRequestHeaders
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to write to
     */
    protected void writeRequestHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.writeRequestHeaders(HttpState,HttpConnection)");
        addRequestHeaders(state,conn);
        Iterator it = requestHeaders.values().iterator();
        while(it.hasNext()) {
            conn.print(((Header)it.next()).toExternalForm());
        }
    }

    /**
     * Populates the request headers map to
     * with additional {@link Header headers} to be
     * submitted to the given {@link HttpConnection}.
     * <p>
     * This implementation adds <tt>User-Agent</tt>,
     * <tt>Host</tt>, <tt>Cookie</tt>, <tt>Content-Length</tt>,
     * <tt>Bookmark-Encoding</tt>, and <tt>Authorization</tt>
     * headers, when appropriate.
     * <p>
     * Subclasses may want to override this method to
     * to add additional headers, and may choose to
     * invoke this implementation (via <tt>super</tt>)
     * to add the "standard" headers.
     *
     * @see #writeRequestHeaders
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} the headers will eventually be written to
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        addUserAgentRequestHeader(state,conn);
        addHostRequestHeader(state,conn);
        addCookieRequestHeader(state,conn);
        addAuthorizationRequestHeader(state,conn);
        addContentLengthRequestHeader(state,conn);
    }

    /**
     * Adds a default <tt>User-Agent</tt> request header,
     * as long as no <tt>User-Agent</tt> request header
     * already exists.
     */
    protected void addUserAgentRequestHeader(HttpState state, HttpConnection conn) throws IOException, HttpException {
        if (!requestHeaders.containsKey("user-agent")) {
            setRequestHeader(HttpMethodBase.USER_AGENT);
        }
    }

    /**
     * Adds a <tt>Host</tt> request header,
     * as long as no <tt>Host</tt> request header
     * already exists.
     */
    protected void addHostRequestHeader(HttpState state, HttpConnection conn) throws IOException, HttpException {
        // add host (should do this conditionally?, i.e., don't send to http/1.0?)
        if (!requestHeaders.containsKey("host")) {
            setRequestHeader("Host",conn.getHost());
        }
    }

    /**
     * Adds a <tt>Cookie</tt> request containing the matching {@link Cookie}s,
     * if any, as long as no <tt>Cookie</tt> request header
     * already exists.
     */
    protected void addCookieRequestHeader(HttpState state, HttpConnection conn) throws IOException, HttpException {
        if (!requestHeaders.containsKey("cookie")) {
            Header cookieHeader = Cookie.createCookieHeader(conn.getHost(), conn.getPort(), getPath(), conn.isSecure(), new Date(), state.getCookies());
            if(null != cookieHeader) {
                setRequestHeader(cookieHeader);
            }
        }
    }

    /**
     * Adds an <tt>Authorization</tt> request if needed,
     * as long as no <tt>Authorization</tt> request header
     * already exists.
     */
    protected void addAuthorizationRequestHeader(HttpState state, HttpConnection conn) throws IOException, HttpException {
        // add authorization header, if needed
        if(!requestHeaders.containsKey("authorization")) {
            Header wwwAuthenticateHeader = (Header)(responseHeaders.get("www-authenticate"));
            if(null != wwwAuthenticateHeader) {
                try {
                    Authenticator.authenticate(this,state);
                } catch(HttpException e) {
                    // ignored
                }
            }
        }
    }

    /**
     * Adds a <tt>Content-Length</tt> or
     * <tt>Transer-Encoding: Chunked</tt> request header,
     * as long as no <tt>Content-Length</tt> request header
     * already exists.
     */
    protected void addContentLengthRequestHeader(HttpState state, HttpConnection conn) throws IOException, HttpException {
        // add content length or chunking
        int len = getRequestContentLength();
        if(!requestHeaders.containsKey("content-length")) {
            if(-1 < len) {
                setRequestHeader("Content-Length",String.valueOf(len));
            } else if(http11 && (len < 0)) {
                setRequestHeader("Bookmark-Encoding","chunked");
            }
        }
    }

    /**
     * Return the length (in bytes) of
     * my request body, suitable for use in
     * a <tt>Content-Length</tt> header.
     * <p>
     * Return <tt>-1</tt> when the content-length
     * is unknown.
     * <p>
     * This implementation returns <tt>0</tt>,
     * indicating that the request has no
     * body.
     * @return <tt>0</tt>, indicating that the request has no body.
     */
    protected int getRequestContentLength() {
        return 0;
    }

    /**
     * Write the request body to the given {@link HttpConnection}
     * <p>
     * If an expectation is required, this method should
     * ensure that it has been sent by checking the
     * {@link #getStatusCode status code}.
     * <p>
     * This method should return <tt>true</tt>
     * if the request body was actually sent (or is empty),
     * or <tt>false</tt> if it could not be sent for
     * some reason (for example, expectation required but
     * not present).
     * <p>
     * This implementation writes nothing and returns <tt>true</tt>.
     * @return <tt>true</tt>
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn) throws IOException, HttpException {
        return true;
    }

    /**
     * Reads the response from the given {@link HttpConnection}.
     * <p>
     * The response is written according to the following logic:
     * <ol>
     * <li>
     *   {@link #readStatusLine readStatusLine(HttpState,HttpConnection)}
     *   is invoked to read the request line.
     * </li>
     * <li>
     *   {@link #processStatusLine processStatusLine(HttpState,HttpConnection)}
     *   is invoked, allowing the method to respond to the status line if desired.
     * </li>
     * <li>
     *   {@link #readResponseHeaders readResponseHeaders(HttpState,HttpConnection}
     *   is invoked to read the associated headers.
     * </li>
     * <li>
     *   {@link #processResponseHeaders processResponseHeaders(HttpState,HttpConnection}
     *   is invoked, allowing the method to respond to the headers if desired.
     * </li>
     * <li>
     *   {@link #readResponseBody readResponseBody(HttpState,HttpConnection)}
     *   is invoked to read the associated body (if any).
     * </li>
     * <li>
     *   {@link #processResponseBody processResponseBody(HttpState,HttpConnection}
     *   is invoked, allowing the method to respond to the body if desired.
     * </li>
     * </ol>
     * Subclasses may want to override one or more of the above methods to
     * to customize the processing. (Or they may choose to override this method
     * if dramatically different processing is required.)
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void readResponse(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.readResponse(HttpState,HttpConnection)");
        readStatusLine(state,conn);
        processStatusLine(state,conn);
        readResponseHeaders(state,conn);
        processResponseHeaders(state,conn);
//        readResponseBody(state,conn);
//        processResponseBody(state,conn);
    }

    /**
     * Read the status line from the given {@link HttpConnection},
     * setting my {@link #getStatusCode status code} and
     * {@link #getStatusText status text}.
     * <p>
     * Subclasses may want to override this method to
     * to customize the processing.
     *
     * @see #readResponse
     * @see #processStatusLine
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void readStatusLine(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.readStatusLine(HttpState,HttpConnection)");
        statusCode = -1;
        statusText = null;

        String statusLine = conn.readLine();

        while(statusLine != null && !statusLine.startsWith("HTTP/")) {
            statusLine = conn.readLine();
        }
        if(statusLine == null) {
            throw new HttpException("Error in parsing the status line from the response: unable to find line starting with \"HTTP/\"");
        }

        if((!statusLine.startsWith("HTTP/1.1") &&
            !statusLine.startsWith("HTTP/1.0"))) {
            throw new HttpException("Unrecognized server protocol :" + statusLine);
        }

        http11 = statusLine.startsWith("HTTP/1.1");

        int at = statusLine.indexOf(" ");
        if(at < 0) {
            throw new HttpException("Unable to parse the status line: " + statusLine);
        }

        int to = statusLine.indexOf(" ", at + 1);
        if(to < 0) {
            to = statusLine.length();
        }

        try {
            statusCode = Integer.parseInt(statusLine.substring(at + 1, to));
        } catch (NumberFormatException e) {
            throw new HttpException("Unable to parse status code from status line: " + statusLine);
        }

        try {
            if(to < statusLine.length()) {
                statusText = statusLine.substring(to + 1);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new HttpException("Status text not specified: " + statusLine);
        }
    }

    /**
     * When this method is invoked, the {@link #getStatusCode status code}
     * and {@link #getStatusText status text} values will have been set (in other
     * words, {@link #readStatusLine readStatusLine(HttpState,HttpConnection} will
     * have been invoked).
     * <p>
     * Subclasses may want to override this method to respond to these value.
     * This implementation does nothing.
     *
     * @see #readResponse
     * @see #readStatusLine
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void processStatusLine(HttpState state, HttpConnection conn) {
    }

    /**
     * Read response headers from the given {@link HttpConnection},
     * populating the response headers map.
     * <p>
     * Subclasses may want to override this method to
     * to customize the processing.
     *
     * @see #readResponse
     * @see #processResponseHeaders
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void readResponseHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        // "It must be possible to combine the multiple header fields into
        // one "field-name: field-value" pair, without changing the
        // semantics of the message, by appending each subsequent field-value
        // to the first, each separated by a comma."
        //   - HTTP/1.0 (4.3)

        log.debug("HttpMethodBase.readResponseHeaders(HttpState,HttpConnection)");
        responseHeaders.clear();

        for(;;) {
            String line = conn.readLine();
            if((line == null) || (line.length() < 1)) {
                break;
            }

            // Parse the header name and value
            int colon = line.indexOf(":");
            if (colon < 0) {
                throw new HttpException("Unable to parse header: " + line);
            }
            String name = line.substring(0, colon).trim();
            String match = name.toLowerCase();
            String value = line.substring(colon + 1).trim();
            Header header = (Header)(responseHeaders.get(match));
            if(null == header) {
                header = new Header(name, value);
            } else {
                String oldvalue =  header.getValue();
                if(null != oldvalue) {
                    header = new Header(name,oldvalue + ", " + value);
                } else {
                    header = new Header(name,value);
                }
            }
            responseHeaders.put(match, header);
        }
    }

    /**
     * When this method is invoked, the response headers
     * map will have been populated with the response headers
     * (in other words,
     * {@link #readResponseHeaders readResponseHeaders(HttpState,HttpConnection)}
     * will have been invoked).
     * <p>
     * This implementation will handle the <tt>Set-Cookie</tt>
     * and <tt>Set-Cookie2</tt> headers, if any, adding the
     * relevant cookies to the given {@link HttpState}.
     * <p>
     * Subclasses may want to override this method to
     * specially process additional headers, and/or
     * invoke this method (via <tt>super</tt>) to process
     * the <tt>Set-Cookie</tt> and <tt>Set-Cookie2</tt> headers.
     *
     * @see #readResponse
     * @see #readResponseHeaders
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void processResponseHeaders(HttpState state, HttpConnection conn) {
        // add cookies, if any
        // should we set cookies?
        Header setCookieHeader = getResponseHeader("set-cookie2");
        if(null == setCookieHeader) { //ignore old-style if new is supported
            setCookieHeader = getResponseHeader("set-cookie");
        }

        if(setCookieHeader != null) {
            try {
                Cookie[] cookies = Cookie.parse(conn.getHost(), conn.getPort(), getPath(), conn.isSecure(), setCookieHeader);
                state.addCookies(cookies);
            } catch (Exception e) {
                log.error("processResponseHeaders(HttpState,HttpConnection)",e);
            }
        }
    }

    public InputStream getInputStream(HttpState state, HttpConnection connection) throws IOException {
        return connection.getResponseInputStream(this);
    }
        
    /**
     * Read the response body from the given {@link HttpConnection}.
     * <p>
     * The current implementation simply consumes the expected
     * response body (according to the values of the
     * <tt>Content-Length</tt> and <tt>Bookmark-Encoding</tt>
     * headers, if any).
     * <p>
     * Subclasses may want to override this method to
     * to customize the processing.
     *
     * @see #readResponse
     * @see #processResponseBody
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("HttpMethodBase.readResponseBody(HttpState,HttpConnection)");
        responseBody = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int expectedLength = 0;
        int foundLength = 0;
        {
            Header lengthHeader = getResponseHeader("Content-Length");
            Header transferEncodingHeader = getResponseHeader("Bookmark-Encoding");
            if(null != lengthHeader) {
                try {
                    expectedLength = Integer.parseInt(lengthHeader.getValue());
                }
                catch(NumberFormatException e) {
                    // ignored
                }
            }
            else if(null != transferEncodingHeader) {
                if("chunked".equalsIgnoreCase(transferEncodingHeader.getValue())) {
                    expectedLength = -1;
                }
            }
        }
        InputStream is = conn.getResponseInputStream(this);
        byte[] buffer = new byte[4096];
        int nb = 0;
        while(expectedLength == -1 || foundLength < expectedLength) {
            nb = is.read(buffer);
            if(nb == -1) {
                break;
            }
            if(out == null) {
                throw new IOException("Unable to buffer data");
            }
            if(wireLog.isInfoEnabled()) {
                wireLog.info("<< \"" + new String(buffer,0,nb) + "\"");
            }
            out.write(buffer, 0, nb);
            foundLength += nb;
            if(expectedLength > -1) {
                if(foundLength == expectedLength) {
                    break;
                } else if(foundLength > expectedLength) {
                    log.warn("HttpMethodBase.readResponseBody(): expected length (" + expectedLength + ") exceeded.  Found " + foundLength + " bytes.");
                    break;
                }
            }
        }
        out.close();
        responseBody = out.toByteArray();
    }

    /**
     * When this method is invoked,
     * {@link #readResponseBody readResponseBody(HttpState,HttpConnection)}
     * will have been invoked.
     * <p>
     * This implementation does nothing.
     * <p>
     * Subclasses may want to override this method.
     *
     * @see #readResponse
     * @see #readResponseBody
     *
     * @param state the client state
     * @param conn the {@link HttpConnection} to read the response from
     */
    protected void processResponseBody(HttpState state, HttpConnection conn) {
    }

    /**
     * Recycle this method so that it can be used again.
     * All of my instances variables will be reset
     * once this method has been called.
     */
    public void recycle() {
        path = null;
        followRedirects = false;
        queryString = null;
        requestHeaders.clear();
        responseHeaders.clear();
        statusCode = -1;
        statusText = null;
        used = false;
        http11 = true;
        bodySent = false;
        responseBody = null;
    }

    // ---------------------------------------------- Protected Utility Methods

    /**
     * Return <tt>true</tt> if I should use the HTTP/1.1 protocol.
     * @internal
     */
    public boolean isHttp11() {
        return http11;
    }

    /**
     * Set whether or not I should use the HTTP/1.1 protocol.
     * @internal
     */
    protected void setHttp11(boolean http11) {
        this.http11 = http11;
    }

    /**
     * Throws an {@link IllegalStateException} if
     * used by not recycled.
     */
    protected void checkNotUsed() {
        if(used) {
            throw new IllegalStateException("Already used.");
        }
    }

    /**
     * Throws an {@link IllegalStateException} if
     * not used since last recycle.
     */
    protected void checkUsed() {
        if(!used) {
            throw new IllegalStateException("Not Used.");
        }
    }

    // ------------------------------------------------- Static Utility Methods

    /**
     * Generate an HTTP/S request line according to
     * the specified attributes.
     */
    protected static String generateRequestLine(HttpConnection connection, String name, String reqPath, String qString, String protocol) {
        StringBuffer buf = new StringBuffer();
        buf.append(null == reqPath ? "/" : URIUtil.encode(reqPath,URIUtil.pathSafe()));
        if(null != qString) {
            if(qString.indexOf("?") < 0) {
                buf.append("?");
            }
            buf.append(qString);
        }

        if(!connection.isProxied()) {
            return (name + " " + buf.toString() + " " + protocol + "\r\n");
        } else {
            if(connection.isSecure()) {
                return (name +
                       " https://" +
                       connection.getHost() +
                       ((443 == connection.getPort() || -1 == connection.getPort()) ? "" : (":" + connection.getPort()) ) +
                       buf.toString() +
                       " " +
                       protocol +
                       "\r\n");
            } else {
                return (name +
                       " http://" +
                       connection.getHost() +
                       ((80 == connection.getPort() || -1 == connection.getPort()) ? "" : (":" + connection.getPort()) ) +
                       buf.toString() +
                       " " +
                       protocol +
                       "\r\n");
            }
        }
    }

    // ----------------------------------------------------- Instance Variables
    /** My request path. */
    private String path = null;
    /** Whether or not I should automatically follow redirects. */
    private boolean followRedirects = false;
    /** My query string, if any. */
    private String queryString = null;
    /** My request headers, if any. */
    private HashMap requestHeaders = new HashMap();
    /** My response headers, if any. */
    private HashMap responseHeaders = new HashMap();
    /** My response status code, if any. */
    private int statusCode = -1;
    /** My response status text, if any. */
    private String statusText = null;
    /** Whether or not I have been executed. */
    private boolean used = false;
    /** Whether or not I should use the HTTP/1.1 protocol. */
    private boolean http11 = true;
    /** Whether or not the request body has been sent. */
    private boolean bodySent = false;
    /** The response body, assuming it has not be intercepted by a sub-class. */
    private byte[] responseBody = null;

    // -------------------------------------------------------------- Constants

    /** <tt>org.apache.commons.httpclient.HttpMethod</tt> log. */
    private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.HttpMethod");

    /** <tt>httpclient.wire</tt> log. */
    private static final Log wireLog = LogSource.getInstance("httpclient.wire");

    /** <tt>User-Agent: Jakarta HTTP Client/1.0</tt> header. */
    protected static final Header USER_AGENT = new Header("User-Agent", "Jakarta HTTP Client/2.0.0a1");

}

