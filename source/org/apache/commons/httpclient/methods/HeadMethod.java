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

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * HEAD Method.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class HeadMethod
    extends HttpMethodBase {


	// ----------------------------------------------------------- Constructors


	/**
	 * No-arg constructor.
	 */
	public HeadMethod() {
		setFollowRedirects(true);
	}


	/**
	 * Path-setting constructor.
	 * @param path the path to request
	 */
	public HeadMethod(String path) {
		super(path);
		setFollowRedirects(true);
	}


	// ---------------------------------------------------------------- Methods


	// override recycle to reset redirects default
	public void recycle() {
		super.recycle();
		setFollowRedirects(true);
	}


	// --------------------------------------------------- WebdavMethod Methods

	/**
	 * Returns <tt>"HEAD"</tt>.
	 * @return <tt>"HEAD"</tt>
	 */
	public String getName() {
		return "HEAD";
	}

	/**
	 * Overrides {@link HttpMethodBase} method to
	 * <i>not</i> read a response body, despite the
	 * presence of a <tt>Content-Length</tt> or
	 * <tt>Bookmark-Encoding</tt> header.
	 */
	protected void readResponseBody(HttpState state, HttpConnection conn) throws IOException, HttpException {
		log.debug("HeadMethod.readResponseBody(HttpState,HttpConnection)");
		return; // despite the possible presence of a content-length header, HEAD returns no response body
	}


	// -------------------------------------------------------------- Constants

	/** <tt>org.apache.commons.httpclient.methods.HeadMethod</tt> log. */
	private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.methods.HeadMethod");
}
