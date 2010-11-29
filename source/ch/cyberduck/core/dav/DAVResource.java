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

import ch.cyberduck.core.Host;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.ResponseEntity;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.util.WebdavStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

/**
 * Webdav Resource adding support for Gzipped streams
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class DAVResource extends WebdavResource {
    private static Logger log = Logger.getLogger(DAVResource.class);

    public DAVResource(Host host) throws IOException {
        super(host.getProtocol().isSecure()
                ? new HttpsURL(host.getHostname(), host.getPort(), null)
                : new HttpURL(host.getHostname(), host.getPort(), null));
    }


    /**
     * Overwritten to make sure the client and its properties are not overwritten when
     * the credentials change. See #2974.
     *
     * @return true if the given httpURL is the client for this resource.
     */
    @Override
    protected synchronized boolean isTheClient() throws URIException {
        final HostConfiguration hostConfig = client.getHostConfiguration();
        // Hack to enable preemptive authentication
        client.getState().setCredentials(null, httpURL.getHost(), hostCredentials);
        return httpURL.getHost().equalsIgnoreCase(hostConfig.getHost())
                && httpURL.getPort() == hostConfig.getProtocol().resolvePort(hostConfig.getPort());

    }

    /**
     *
     */
    public void clearHeaders() {
        headers.clear();
    }

    /**
     * Add all additionals headers that have been previously registered
     * with addRequestHeader to the method
     */
    @Override
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

    @Override
    protected void setStatusCode(int statusCode, String message) {
        latestStatusCode = statusCode;
        final String statusText = WebdavStatus.getStatusText(statusCode);
        StringBuilder text = new StringBuilder();
        text.append(statusCode).append(" ").append(StringUtils.isNotBlank(statusText) ? statusText : "").
                append(" ").append(StringUtils.isNotBlank(message) ? message : "");
        latestStatusMessage = text.toString();
    }

    /**
     * Get InputStream for the GET method for the given path.
     *
     * @param path the server relative path of the resource to get
     * @return InputStream
     * @throws IOException
     */
    @Override
    public InputStream getMethodData(String path)
            throws IOException {
        setClient();

        GetMethod method = new GetMethod(URIUtil.encodePathQuery(path));
        method.setFollowRedirects(super.followRedirects);

        generateTransactionHeader(method);
        generateAdditionalHeaders(method);
        final int statusCode = client.executeMethod(method);
        Header contentRange = method.getResponseHeader("Content-Range");
        resume = contentRange != null;

        setStatusCode(statusCode, method.getStatusText());

        if(isHttpSuccess(statusCode)) {
            Header contentEncoding = method.getResponseHeader("Content-Encoding");
            boolean zipped = contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding.getValue());
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
     * @param path          the server relative path to put the data
     * @param requestEntity The input stream.
     * @return true if the method is succeeded.
     * @throws IOException
     */
    public boolean putMethod(String path, RequestEntity requestEntity)
            throws IOException {

        setClient();

        PutMethod method = new PutMethod(URIUtil.encodePathQuery(path)) {
            @Override
            public boolean getFollowRedirects() {
                // See #3206. Redirects for uploads are not allowed without user interaction by default.
                return true;
            }
        };

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
        method.setRequestEntity(requestEntity);

        generateTransactionHeader(method);
        generateAdditionalHeaders(method);
        int statusCode = client.executeMethod(method);

        method.releaseConnection();

        setStatusCode(statusCode, method.getStatusText());
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

    @Override
    protected void setWebdavProperties(final Enumeration responses)
            throws HttpException, IOException {

        super.setWebdavProperties(new Enumeration() {
            public boolean hasMoreElements() {
                return responses.hasMoreElements();
            }

            public Object nextElement() {
                final ResponseEntity response =
                        (ResponseEntity) responses.nextElement();
                return new ResponseEntity() {

                    public int getStatusCode() {
                        return response.getStatusCode();
                    }

                    public Enumeration getProperties() {
                        return response.getProperties();
                    }

                    public Enumeration getHistories() {
                        return response.getHistories();
                    }

                    public Enumeration getWorkspaces() {
                        return response.getWorkspaces();
                    }

                    public String getHref() {
                        if(StringUtils.isNotBlank(response.getHref())) {
                            // http://trac.cyberduck.ch/ticket/2223
                            final String escaped = StringUtils.replace(response.getHref(), " ", "%20");
                            try {
                                new java.net.URI(escaped);
                            }
                            catch(URISyntaxException e) {
                                log.warn("Href not escaped in respose:" + response.getHref());
                                try {
                                    return URIUtil.encodePath(response.getHref());
                                }
                                catch(URIException failure) {
                                    log.error("Encoding path failed:" + failure.getMessage());
                                }
                            }
                            return escaped;
                        }
                        return response.getHref();
                    }
                };
            }
        });
    }
}