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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URIUtil;
import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * POST Method.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 */
public class PostMethod extends GetMethod {


    // ----------------------------------------------------------- Constructors

    /**
     * No-arg constructor.
     */
    public PostMethod() {
        super();
    }

    /**
     * Path-setting constructor.
     *
     * @param path the path to request
     */
    public PostMethod(String path) {
        super(path);
    }

    /**
     * Constructor.
     *
     * @param path    the path to request
     * @param tempDir directory to store temp files in
     */
    public PostMethod(String path, String tempDir) {
        super(path, tempDir);
    }

    /**
     * Constructor.
     *
     * @param path     the path to request
     * @param tempDir  directory to store temp files in
     * @param tempFile file to store temporary data in
     */
    public PostMethod(String path, String tempDir, String tempFile) {
        super(path, tempDir, tempFile);
    }

    // ----------------------------------------------------- HttpMethod Methods

    /**
     * Returns <tt>"POST"</tt>.
     *
     * @return <tt>"POST"</tt>
     */
    public String getName() {
        return "POST";
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to clear my request body.
     */
    public void recycle() {
        super.recycle();
        requestBody = null;
        parameters.clear();
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to throw {@link IllegalStateException} if
     * my request body has already been generated.
     *
     * @throws IllegalStateException if my request body has already been generated.
     */
    public void setParameter(String parameterName, String parameterValue) {
        if (null != requestBody) {
            throw new IllegalStateException("Request body already generated.");
        }
        parameters.put(parameterName, parameterValue);
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to throw {@link IllegalStateException} if
     * my request body has already been generated.
     *
     * @throws IllegalStateException if my request body has already been generated.
     */
    public void addParameter(String parameterName, String parameterValue) {
        if (null != requestBody) {
            throw new IllegalStateException("Request body already generated.");
        }
        Object old = parameters.put(parameterName, parameterValue);
        if (null != old) {
            List v = null;
            if (old instanceof String) {
                v = new ArrayList();
                v.add(old);
            }
            else if (old instanceof List) {
                v = (List) old;
            }
            else {
                throw new ClassCastException("Didn't expect to find " +
                        old.getClass().getName() +
                        " as parameter value for \"" +
                        parameterName + "\"");
            }
            v.add(parameterValue);
            parameters.put(parameterName, v);
        }
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to throw {@link IllegalStateException} if
     * my request body has already been generated.
     *
     * @throws IllegalStateException if my request body has already been generated.
     */
    public void removeParameter(String paramName) {
        if (null != requestBody) {
            throw new IllegalStateException("Request body already generated.");
        }
        parameters.remove(paramName);
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to throw {@link IllegalStateException} if
     * my request body has already been generated.
     *
     * @throws IllegalStateException if my request body has already been generated.
     */
    public void removeParameter(String paramName, String paramValue) {
        if (null != requestBody) {
            throw new IllegalStateException("Request body already generated.");
        }
        Object old = parameters.get(paramName);
        if (null != old) {
            if (paramValue.equals(old)) {
                parameters.remove(paramName);
            }
            else if (old instanceof List) {
                List list = (List) old;
                if (list.remove(paramValue)) {
                    if (list.isEmpty()) {
                        parameters.remove(paramName);
                    }
                    else if (list.size() == 1) {
                        parameters.put(paramName, list.get(0));
                    }
                    else {
                        parameters.put(paramValue, list);
                    }
                }
            }
        }
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to also add <tt>Content-Type</tt> header
     * when appropriate.
     */
    protected void addRequestHeaders(HttpState state, HttpConnection conn) throws IOException, HttpException {
        super.addRequestHeaders(state, conn);
        if (!parameters.isEmpty()) {
            setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        }
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to write request parameters as the
     * request body.
     */
    protected boolean writeRequestBody(HttpState state, HttpConnection conn) throws IOException, HttpException {
        log.debug("PostMethod.writeRequestBody(HttpState,HttpConnection)");
        if (null == requestBody) {
            requestBody = generateRequestBody(parameters);
        }
        conn.print(requestBody);
        return true;
    }

    /**
     * Override method of {@link HttpMethodBase}
     * to return the length of the request body.
     * <p/>
     * Once this method has been invoked,
     * the request parameters cannot be altered
     * until I am {@link #recycle recycled}.
     */
    protected int getRequestContentLength() {
        if (null == requestBody) {
            requestBody = generateRequestBody(parameters);
        }
        return requestBody.getBytes().length;
    }

    protected String generateRequestBody(HashMap params) {
        if (!params.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            Iterator it = parameters.keySet().iterator();
            while (it.hasNext()) {
                String name = (String) (it.next());
                Object value = parameters.get(name);
                if (value instanceof List) {
                    List list = (List) value;
                    Iterator valit = list.iterator();
                    while (valit.hasNext()) {
                        if (sb.length() > 0) {
                            sb.append("&");
                        }
                        sb.append(URIUtil.encode(name, URIUtil.queryStringValueSafe(), true));
                        Object val2 = valit.next();
                        if (null != val2) {
                            sb.append("=");
                            sb.append(URIUtil.encode(String.valueOf(val2), URIUtil.queryStringValueSafe(), true));
                        }
                    }
                }
                else {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(URIUtil.encode(name, URIUtil.queryStringValueSafe()));
                    if (null != value) {
                        sb.append("=");
                        sb.append(URIUtil.encode(String.valueOf(value), URIUtil.queryStringValueSafe(), true));
                    }
                }
            }
            return sb.toString();
        }
        else {
            return "";
        }
    }

    private String requestBody = null;
    private HashMap parameters = new HashMap();

    // -------------------------------------------------------------- Constants

    /**
     * <tt>org.apache.commons.httpclient.methods.PostMethod</tt> log.
     */
    private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.methods.PostMethod");
}
