package ch.cyberduck.core.dav;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.webdav.lib.WebdavResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Webdav Resource adding support for Gzipped streams
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DAVResource extends WebdavResource {

    public DAVResource(String url) throws IOException {
        super(url);
    }

    /**
     * Add all additionals headers that have been previously registered
     * with addRequestHeader to the method
     */
    protected void generateAdditionalHeaders(HttpMethod method) {
        for(Object o : headers.keySet()) {
            String header = (String) o;
            method.setRequestHeader(header, (String) headers.get(header));
        }
    }

    private boolean resume;

    public boolean isResume() {
        return resume;
    }

    private boolean zipped;

    public boolean isZipped() {
        return zipped;
    }

    /**
     * Get InputStream for the GET method for the given path.
     *
     * @param path the server relative path of the resource to get
     * @return InputStream
     * @throws IOException
     */
    public InputStream getMethodData(String path)
            throws IOException {
        setClient();

        GetMethod method = new GetMethod(URIUtil.encodePathQuery(path));
        method.setFollowRedirects(super.followRedirects);

        generateTransactionHeader(method);
        generateAdditionalHeaders(method);
        client.executeMethod(method);
        Header contentRange = method.getResponseHeader("Content-Range");
        resume = contentRange != null;

        int statusCode = method.getStatusLine().getStatusCode();
        setStatusCode(statusCode);

        if(isHttpSuccess(statusCode)) {
            Header contentEncoding = method.getResponseHeader("Content-Encoding");
            zipped = contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding.getValue());
            if(zipped) {
                return new GZIPInputStream(method.getResponseBodyAsStream());
            }
            return method.getResponseBodyAsStream();
        }
        else {
            throw new IOException("Couldn't get file");
        }
    }

    /**
     * Execute the PUT method for the given path.
     *
     * @param path        the server relative path to put the data
     * @param inputStream The input stream.
     * @return true if the method is succeeded.
     * @throws IOException
     */
    public boolean putMethod(String path, InputStream inputStream, long contentLength)
            throws IOException {

        setClient();

        // Fix #2268
        client.getState().setAuthenticationPreemptive(false);

        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path));

        // Activates 'Expect: 100-Continue' handshake. The purpose of
        // the 'Expect: 100-Continue' handshake to allow a client that is
        // sending a request message with a request body to determine if
        // the origin server is willing to accept the request (based on
        // the request headers) before the client sends the request body.
        //
        // Otherwise, upload will fail when using digest authentication.
        // Fix #2268
        method.setUseExpectHeader(true);

        generateIfHeader(method);
        if(getGetContentType() != null && !getGetContentType().equals("")) {
            method.setRequestHeader("Content-Type", getGetContentType());
        }
        method.setRequestContentLength(contentLength);
        method.setRequestBody(inputStream);
        generateTransactionHeader(method);
        generateAdditionalHeaders(method);
        int statusCode = client.executeMethod(method);

        setStatusCode(statusCode);
        return isHttpSuccess(statusCode);
    }

    /**
     * Check if the http status code passed as argument is a success
     *
     * @param statusCode
     * @return true if code represents a HTTP success
     */
    private boolean isHttpSuccess(int statusCode) {
        return (statusCode >= HttpStatus.SC_OK
                && statusCode < HttpStatus.SC_MULTIPLE_CHOICES);
    }
}