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

import java.util.Hashtable;

/**
 * <p>Constants enumerating the HTTP status codes.</p>
 * @version $Id$
 * @modified <a href="mailto:dkocher@cyberduck.ch">David Kocher</a>
 */
public class HttpStatus {


	// -------------------------------------------------------------- Variables

	/** Reason phrases (as Strings), by status code (as Integer). */
	private static Hashtable mapStatusCodes = new Hashtable();


	// --------------------------------------------------------- Public Methods

	public static boolean isSuccessfulResponse(int responseCode) {
		return responseCode >= 200 && responseCode < 300;
	}

	/**
	 * Get the "reason phrase" associated with the given
	 * HTTP status code, or <tt>null</tt> if no
	 * such reason phrase can be found.
	 * @param nHttpStatusCode the numeric status code
	 * @return the reason phrase associated with the given status code
	 */
	public static String getStatusText(int nHttpStatusCode) {
		Integer intKey = new Integer(nHttpStatusCode);

		if (!mapStatusCodes.containsKey(intKey)) {
			// No information
			return null;

		}
		else {
			return (String) mapStatusCodes.get(intKey);
		}
	}


	// -------------------------------------------------------- Private Methods

	/**
	 * Store the given reason phrase (as String),
	 * by status code (as Integer).
	 */
	private static void addStatusCodeMap(int nKey, String strVal) {
		mapStatusCodes.put(new Integer(nKey), strVal);
	}


	// -------------------------------------------------------------- Constants

	// --- 1xx Informational ---

	/** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_CONTINUE = 100;
	/** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616)*/
	public static final int SC_SWITCHING_PROTOCOLS = 101;
	/** <tt>102 Processing</tt> (WebDAV - RFC 2518) */
	public static final int SC_PROCESSING = 102;

	// --- 2xx Success ---

	/** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_OK = 200;
	/** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_CREATED = 201;
	/** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_ACCEPTED = 202;
	/** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
	/** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_NO_CONTENT = 204;
	/** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_RESET_CONTENT = 205;
	/** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_PARTIAL_CONTENT = 206;
	/** <tt>207 Multi-Status</tt> (WebDAV - RFC 2518) or <tt>207 Partial Update OK</tt> (HTTP/1.1 - draft-ietf-http-v11-spec-rev-01?) */
	public static final int SC_MULTI_STATUS = 207;

	// --- 3xx Redirection ---

	/** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_MULTIPLE_CHOICES = 300;
	/** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_MOVED_PERMANENTLY = 301;
	/** <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC 1945) */
	public static final int SC_MOVED_TEMPORARILY = 302;
	/** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_SEE_OTHER = 303;
	/** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_NOT_MODIFIED = 304;
	/** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_USE_PROXY = 305;
	/** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_TEMPORARY_REDIRECT = 307;

	// --- 4xx Client Error ---

	/** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_BAD_REQUEST = 400;
	/** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_UNAUTHORIZED = 401;
	/** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_PAYMENT_REQUIRED = 402;
	/** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_FORBIDDEN = 403;
	/** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_NOT_FOUND = 404;
	/** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_METHOD_NOT_ALLOWED = 405;
	/** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_NOT_ACCEPTABLE = 406;
	/** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616)*/
	public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
	/** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_REQUEST_TIMEOUT = 408;
	/** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_CONFLICT = 409;
	/** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_GONE = 410;
	/** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_LENGTH_REQUIRED = 411;
	/** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_PRECONDITION_FAILED = 412;
	/** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_REQUEST_TOO_LONG = 413;
	/** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_REQUEST_URI_TOO_LONG = 414;
	/** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
	/** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	/** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_EXPECTATION_FAILED = 417;

	/**
	 * <tt>418 Unprocessable Entity</tt> (WebDAV drafts?)
	 * or <tt>418 Reauthentication Required</tt> (HTTP/1.1 drafts?)
	 */
	// not used
	// public static final int SC_UNPROCESSABLE_ENTITY = 418;

	/**
	 * <tt>419 Insufficient Space on Resource</tt>
	 * (WebDAV - draft-ietf-webdav-protocol-05?)
	 * or <tt>419 Proxy Reauthentication Required</tt>
	 * (HTTP/1.1 drafts?)
	 */
	public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
	/**
	 * <tt>420 Method Failure</tt>
	 * (WebDAV - draft-ietf-webdav-protocol-05?)
	 */
	public static final int SC_METHOD_FAILURE = 420;
	/** <tt>422 Unprocessable Entity</tt> (WebDAV - RFC 2518) */
	public static final int SC_UNPROCESSABLE_ENTITY = 422;
	/** <tt>423 Locked</tt> (WebDAV - RFC 2518) */
	public static final int SC_LOCKED = 423;
	/** <tt>424 Failed Dependency</tt> (WebDAV - RFC 2518) */
	public static final int SC_FAILED_DEPENDENCY = 424;

	// --- 5xx Server Error ---

	/** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_INTERNAL_SERVER_ERROR = 500;
	/** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_NOT_IMPLEMENTED = 501;
	/** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_BAD_GATEWAY = 502;
	/** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
	public static final int SC_SERVICE_UNAVAILABLE = 503;
	/** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_GATEWAY_TIMEOUT = 504;
	/** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
	public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	/** <tt>507 Insufficient Storage</tt> (WebDAV - RFC 2518) */
	public static final int SC_INSUFFICIENT_STORAGE = 507;


	// ----------------------------------------------------- Static Initializer

	/** Set up status code to "reason phrase" map. */
	static {
		// HTTP 1.0 Server status codes -- see RFC 1945
		addStatusCodeMap(SC_OK, "OK");
		addStatusCodeMap(SC_CREATED, "Created");
		addStatusCodeMap(SC_ACCEPTED, "Accepted");
		addStatusCodeMap(SC_NO_CONTENT, "No Content");
		addStatusCodeMap(SC_MOVED_PERMANENTLY, "Moved Permanently");
		addStatusCodeMap(SC_MOVED_TEMPORARILY, "Moved Temporarily");
		addStatusCodeMap(SC_NOT_MODIFIED, "Not Modified");
		addStatusCodeMap(SC_BAD_REQUEST, "Bad Request");
		addStatusCodeMap(SC_UNAUTHORIZED, "Unauthorized");
		addStatusCodeMap(SC_FORBIDDEN, "Forbidden");
		addStatusCodeMap(SC_NOT_FOUND, "Not Found");
		addStatusCodeMap(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		addStatusCodeMap(SC_NOT_IMPLEMENTED, "Not Implemented");
		addStatusCodeMap(SC_BAD_GATEWAY, "Bad Gateway");
		addStatusCodeMap(SC_SERVICE_UNAVAILABLE, "Service Unavailable");

		// HTTP 1.1 Server status codes -- see RFC 2048
		addStatusCodeMap(SC_CONTINUE, "Continue");
		addStatusCodeMap(SC_TEMPORARY_REDIRECT, "Temporary Redirect");
		addStatusCodeMap(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
		addStatusCodeMap(SC_CONFLICT, "Conflict");
		addStatusCodeMap(SC_PRECONDITION_FAILED, "Precondition Failed");
		addStatusCodeMap(SC_REQUEST_TOO_LONG, "Request Too Long");
		addStatusCodeMap(SC_REQUEST_URI_TOO_LONG, "Request-URI Too Long");
		addStatusCodeMap(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");

		addStatusCodeMap(SC_SWITCHING_PROTOCOLS, "Switching Protocols");
		addStatusCodeMap(SC_NON_AUTHORITATIVE_INFORMATION,
		    "Non Authoritative Information");
		addStatusCodeMap(SC_RESET_CONTENT, "Reset Content");
		addStatusCodeMap(SC_GATEWAY_TIMEOUT, "Gateway Timeout");
		addStatusCodeMap(SC_HTTP_VERSION_NOT_SUPPORTED,
		    "Http Version Not Supported");

		// WebDAV Server-specific status codes
		addStatusCodeMap(SC_PROCESSING, "Processing");
		addStatusCodeMap(SC_MULTI_STATUS, "Multi-Status");
		addStatusCodeMap(SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity");
		addStatusCodeMap(SC_INSUFFICIENT_SPACE_ON_RESOURCE,
		    "Insufficient Space On Resource");
		addStatusCodeMap(SC_METHOD_FAILURE, "Method Failure");
		addStatusCodeMap(SC_LOCKED, "Locked");
		addStatusCodeMap(SC_INSUFFICIENT_STORAGE, "Insufficient Storage");
		addStatusCodeMap(SC_FAILED_DEPENDENCY, "Failed Dependency");


		// --- 2xx Success ---
		addStatusCodeMap(SC_PARTIAL_CONTENT, "Partial Content");
	}


}
