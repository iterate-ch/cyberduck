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

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;


/**
 * OPTIONS Method.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class OptionsMethod
    extends HttpMethodBase {


	// -------------------------------------------------------------- Constants


	// ----------------------------------------------------------- Constructors


	/**
	 * Method constructor.
	 */
	public OptionsMethod() {
	}


	/**
	 * Method constructor.
	 */
	public OptionsMethod(String path) {
		super(path);
	}


	// ----------------------------------------------------- Instance Variables


	/**
	 * Methods allowed.
	 */
	private Vector methodsAllowed = new Vector();


	// --------------------------------------------------------- Public Methods

	public String getName() {
		return "OPTIONS";
	}


	/**
	 * Is the specified method allowed ?
	 */
	public boolean isAllowed(String method) {
		checkUsed();
		return methodsAllowed.contains(method);
	}


	/**
	 * Get a list of allowed methods.
	 */
	public Enumeration getAllowedMethods() {
		checkUsed();
		return methodsAllowed.elements();
	}


	// ----------------------------------------------------- HttpMethod Methods

	protected void processResponseHeaders(HttpState state, HttpConnection conn) {
		Header allowHeader = getResponseHeader("allow");
		if (allowHeader != null) {
			String allowHeaderValue = allowHeader.getValue();
			StringTokenizer tokenizer =
			    new StringTokenizer(allowHeaderValue, ",");
			while (tokenizer.hasMoreElements()) {
				String methodAllowed =
				    tokenizer.nextToken().trim().toUpperCase();
				methodsAllowed.addElement(methodAllowed);
			}
		}
	}

	/**
	 * Return true if the method needs a content-length header in the request.
	 *
	 * @return true if a content-length header will be expected by the server
	 */
	public boolean needContentLength() {
		return false;
	}


}
