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
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.webdav.lib.WebdavResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

/**
 * Webdav Resource adding support for Gzipped streams
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id:$
 */
public class DAVResource extends WebdavResource {

    public DAVResource(String url) throws IOException {
        super(url, true);
    }

    /**
     * Add all additionals headers that have been previously registered
     * with addRequestHeader to the method
     */
    protected void generateAdditionalHeaders(HttpMethod method) {
        Iterator iterator = headers.keySet().iterator();
        while(iterator.hasNext()) {
            String header = (String)iterator.next();
            method.setRequestHeader(header, (String)headers.get(header));
        }
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

        int statusCode = method.getStatusLine().getStatusCode();
        setStatusCode(statusCode);

        if(isHttpSuccess(statusCode)) {
            Header contentEncoding = method.getResponseHeader("Content-Encoding");
            boolean isGZipped = contentEncoding != null && "gzip".equalsIgnoreCase(contentEncoding.getValue());

            if(isGZipped) {
                return new GZIPInputStream(method.getResponseBodyAsStream());
            }
            return method.getResponseBodyAsStream();
        }
        else {
            throw new IOException("Couldn't get file");
        }
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