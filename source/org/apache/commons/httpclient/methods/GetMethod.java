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

package org.apache.commons.httpclient.methods;

import java.io.*;
import java.net.URLEncoder;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * GET Method.
 * Implements an HTTP GET request.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Sung-Gu Park
 */
public class GetMethod extends HttpMethodBase {

    // -------------------------------------------------------------- Constants

    /**
     * <tt>org.apache.commons.httpclient.methods.GetMethod</tt> log.
     */
    private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.methods.GetMethod");

    /**
     * <tt>httpclient.wire</tt> log.
     */
    private static final Log wireLog = LogSource.getInstance("httpclient.wire");

    /**
     * Temporary directory.
     */
    private static final String TEMP_DIR = "temp/";

    // ----------------------------------------------------------- Constructors


    /**
     * No-arg constructor.
     */
    public GetMethod() {
        setFollowRedirects(true);
    }


    /**
     * Path-setting constructor.
     *
     * @param path the path to request
     */
    public GetMethod(String path) {
        super(path);
        setFollowRedirects(true);
    }


    /**
     * Constructor.
     *
     * @param path    the path to request
     * @param tempDir the directory in which to store temporary files
     */
    public GetMethod(String path, String tempDir) {
        super(path);
        setUseDisk(true);
        setTempDir(tempDir);
        setFollowRedirects(true);
    }

    /**
     * Constructor.
     *
     * @param path     the path to request
     * @param tempDir  the directory in which to store temporary files
     * @param tempFile the file (under tempDir) to buffer contents to
     */
    public GetMethod(String path, String tempDir, String tempFile) {
        super(path);
        setUseDisk(true);
        setTempDir(tempDir);
        setTempFile(tempFile);
        setFollowRedirects(true);
    }

    /**
     * Constructor.
     *
     * @param path     the path to request
     * @param tempFile the file to buffer contents to
     */
    public GetMethod(String path, File fileData) {
        this(path);
        useDisk = true;
        this.fileData = fileData;
        setFollowRedirects(true);
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * By default, the get method will buffer read data to the memory.
     */
    protected boolean useDisk = false;


    /**
     * If we're not using the HD, we're using a memory byte buffer.
     */
    protected byte[] memoryData;


    /**
     * File which contains the buffered data.
     */
    protected File fileData;


    /**
     * Temporary directory to use.
     */
    protected String tempDir = TEMP_DIR;


    /**
     * Temporary file to use.
     */
    protected String tempFile = null;


    // ------------------------------------------------------------- Properties


    /**
     * Use disk setter.
     *
     * @param useDisk New value of useDisk
     */
    public void setUseDisk(boolean useDisk) {
        checkNotUsed();
        this.useDisk = useDisk;
    }


    /**
     * Use disk getter.
     *
     * @param boolean useDisk value
     */
    public boolean getUseDisk() {
        return useDisk;
    }

    /**
     * Temporary directory setter.
     *
     * @param tempDir New value of tempDir
     */
    public void setTempDir(String tempDir) {
        checkNotUsed();
        this.tempDir = tempDir;
        setUseDisk(true);
    }


    /**
     * Temporary directory getter.
     */
    public String getTempDir() {
        return tempDir;
    }


    /**
     * Temporary file setter.
     *
     * @param tempFile New value of tempFile
     */
    public void setTempFile(String tempFile) {
        checkNotUsed();
        this.tempFile = tempFile;
    }


    /**
     * Temporary file getter.
     */
    public String getTempFile() {
        return tempFile;
    }


    /**
     * File data getter.
     */
    public File getFileData() {
        return fileData;
    }


    /**
     * File data setter.
     */
    public void setFileData(File fileData) {
        checkNotUsed();
        this.fileData = fileData;
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Returns <tt>"GET"</tt>.
     *
     * @return <tt>"GET"</tt>
     */
    public String getName() {
        return "GET";
    }

    public String getAcceptHeader() {
        return "*/*";
    }


    // override recycle to reset redirects default
    public void recycle() {
        super.recycle();
        setFollowRedirects(true);
    }

    /**
     * Return my response body, if any,
     * as a byte array.
     * Otherwise return <tt>null</tt>.
     */
    public byte[] getResponseBody() {
        checkUsed();
        if (useDisk) {
            try {
                InputStream is = new FileInputStream(fileData);
                byte[] buffer = new byte[4096];
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                int nb = 0;
                while (true) {
                    nb = is.read(buffer);
                    if (nb == -1) {
                        break;
                    }
                    os.write(buffer, 0, nb);
                }
                is.close();
                return os.toByteArray();
            }
            catch (IOException e) {
                log.error("Exception in GetMethod.getResponseBody() while retrieving data from file \"" + fileData + "\".", e);
                return null;
            }
        }
        else {
            return memoryData;
        }
    }

    /**
     * Return my response body, if any,
     * as a {@link String}.
     * Otherwise return <tt>null</tt>.
     */
    public String getResponseBodyAsString() {
        byte[] data = getResponseBody();
        if (null == data) {
            return null;
        }
        else {
            return new String(data);
        }
    }


    /**
     * Return my response body, if any,
     * as an {@link InputStream}.
     * Otherwise return <tt>null</tt>.
     */
    public InputStream getResponseBodyAsStream() throws IOException {
        checkUsed();
        if (useDisk) {
            return new FileInputStream(fileData);
        }
        else {
            if (null == memoryData) {
                return null;
            }
            else {
                return new ByteArrayInputStream(memoryData);
            }
        }
    }


    // ----------------------------------------------------- HttpMethod Methods

    /**
     * Overrides method in {@link HttpMethodBase} to
     * write data to the appropriate buffer.
     */
    protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException {
        log.debug("GetMethod.readResponseBody(HttpState,HttpConnection)");
        OutputStream out = null;
        if (useDisk) {
            if (fileData == null) {
                // Create a temporary file on the HD
                File dir = new File(tempDir);
                dir.deleteOnExit();
                dir.mkdirs();
                String tempFileName = null;
                if (tempFile == null) {
                    String encodedPath = URLEncoder.encode(getPath(), "utf-8");
                    int length = encodedPath.length();
                    if (length > 240) {
                        encodedPath =
                                encodedPath.substring(length - 200, length);
                    }
                    tempFileName = System.currentTimeMillis() + "-"
                            + encodedPath + ".tmp";
                }
                else {
                    tempFileName = tempFile;
                }
                fileData = new File(tempDir, tempFileName);
                fileData.deleteOnExit();
            }
            out = new FileOutputStream(fileData);
        }
        else {
            out = new ByteArrayOutputStream();
        }

        int expectedLength = -1;
        int foundLength = 0;
        {
            Header lengthHeader = getResponseHeader("Content-Length");
            if (null != lengthHeader) {
                try {
                    expectedLength = Integer.parseInt(lengthHeader.getValue());
                }
                catch (NumberFormatException e) {
                    // ignored
                }
            }
        }
        InputStream is = conn.getResponseInputStream(this);
        byte[] buffer = new byte[4096];
        int nb = 0;
        while (true) {
            nb = is.read(buffer);
            if (nb == -1) {
                break;
            }
            if (out == null) {
                throw new IOException("Unable to buffer data");
            }
            if (wireLog.isInfoEnabled()) {
                wireLog.info("<< \"" + new String(buffer, 0, nb) + "\"");
            }
            out.write(buffer, 0, nb);
            foundLength += nb;
            if (expectedLength > -1) {
                if (foundLength == expectedLength) {
                    break;
                }
                else if (foundLength > expectedLength) {
                    log.warn("GetMethod.readResponseBody(): expected length (" + expectedLength + ") exceeded.  Found " + foundLength + " bytes.");
                    break;
                }
            }
        }

        if (!useDisk) {
            memoryData = ((ByteArrayOutputStream)out).toByteArray();
        }

        out.close();
    }

}
