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
import java.io.InputStream;

/**
 * <p/>
 * A request to be applied to an {@link HttpConnection},
 * and a container for the associated response.
 * </p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rod Waldhoff
 * @version $Revision$ $Date$
 */
public interface HttpMethod {

    // ------------------------------------------- Property Setters and Getters

    /**
     * Obtain the name of this method, suitable for use in the "request line",
     * for example <tt>"GET"</tt> or <tt>"POST"</tt>.
     *
     * @return the name of this method
     */
    public String getName();

    /**
     * Set the path part of my request.
     *
     * @param path the path to request
     */
    public void setPath(String path);

    /**
     * Get the path part of my request.
     *
     * @return the path to request
     */
    public String getPath();

    /**
     * Set the specified request header, overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param headerName  the header's name
     * @param headerValue the header's value
     */
    public void setRequestHeader(String headerName, String headerValue);

    /**
     * Set the specified request header, overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param header the header
     */
    public void setRequestHeader(Header header);

    /**
     * Adds the specified request header, NOT overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param headerName  the header's name
     * @param headerValue the header's value
     */
    public void addRequestHeader(String headerName, String headerValue);

    /**
     * Adds the specified request header, NOT overwriting any
     * previous value.
     * Note that header-name matching is case insensitive.
     *
     * @param header the header
     */
    public void addRequestHeader(Header header);

    /**
     * Get the request header associated with the given name.
     * Note that header-name matching is case insensitive.
     *
     * @param headerName the header name
     * @return the header
     */
    public Header getRequestHeader(String headerName);

    /**
     * Remove all request headers associated with the given name.
     * Note that header-name matching is case insensitive.
     *
     * @param headerName the header name
     * @return the header
     */
    public void removeRequestHeader(String headerName);

    /**
     * Whether or not I should automatically follow
     * HTTP redirects (status code 302, etc.)
     *
     * @return <tt>true</tt> if I will automatically follow HTTP redirects
     */
    public boolean getFollowRedirects();

    /**
     * Set whether or not I should automatically follow
     * HTTP redirects (status code 302, etc.)
     */
    public void setFollowRedirects(boolean followRedirects);

    /**
     * Set my query string.
     *
     * @param queryString the query string
     */
    public void setQueryString(String queryString);

    /**
     * Set my query string.
     *
     * @param params an array of {@link NameValuePair}s
     *               to add as query string parameterss
     */
    public void setQueryString(NameValuePair[] params);

    /**
     * Get my query string.
     *
     * @return my query string
     */
    public String getQueryString();

    /**
     * Return an array of my request headers.
     */
    public Header[] getRequestHeaders();

    // ---------------------------------------------------------------- Queries

    /**
     * Confirm that I am ready to execute.
     */
    public boolean validate();

    /**
     * Return the status code associated with the latest response.
     */
    public int getStatusCode();

    /**
     * Return the status text (or "reason phrase") associated with the latest response.
     */
    public String getStatusText();

    /**
     * Return an array of my response headers.
     */
    public Header[] getResponseHeaders();

    /**
     * Return the specified response header.
     * Note that header-name matching is case insensitive.
     */
    public Header getResponseHeader(String headerName);

    /**
     * Return my response body, if any,
     * as a byte array.
     * Otherwise return <tt>null</tt>.
     */
    public byte[] getResponseBody();

    /**
     * Return my response body, if any,
     * as a {@link String}.
     * Otherwise return <tt>null</tt>.
     */
    public String getResponseBodyAsString();

    /**
     * Return my response body, if any,
     * as an {@link InputStream}.
     * Otherwise return <tt>null</tt>.
     */
    public InputStream getResponseBodyAsStream() throws IOException;

    /**
     * Return <tt>true</tt> if I have been {@link #execute executed}
     * but not recycled.
     */
    public boolean hasBeenUsed();

    // --------------------------------------------------------- Action Methods

    /**
     * Execute this method.
     *
     * @param state      state information to associate with this request
     * @param connection the {@link HttpConnection} to write to/read from
     * @return the integer status code if one was obtained, or <tt>-1</tt>
     * @throws IOException   if an I/O error occurs
     * @throws HttpException if an protocol exception occurs
     */
    public int execute(HttpState state, HttpConnection connection) throws HttpException, IOException;

    public InputStream getInputStream(HttpState state, HttpConnection connection) throws IOException;

    /**
     * Recycle this method so that it can be used again.
     * Note that all of my instance variables will be reset
     * once this method has been called.
     */
    public void recycle();
}
