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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>
 * A container for HTTP attributes that may persist from request
 * to request, such as {@link Cookie}s and authentication
 * {@link Credentials}.
 * </p>
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @version $Revision$ $Date$
 */
public class HttpState {

	// ----------------------------------------------------- Instance Variables

	/**
	 * My {@link Credentials Credentials}s, by realm.
	 */
	private HashMap credMap = new HashMap();

	/**
	 * My {@link Cookie Cookie}s.
	 */
	private ArrayList cookies = new ArrayList();

	// ------------------------------------------------------------- Properties

	/**
	 * Add a cookie.
	 * If the given <i>cookie</i> has already expired,
	 * deletes the corresponding existing cookie (if any).
	 * @param cookie the {@link Cookie} to add
	 */
	public void addCookie(Cookie cookie) {
		if (cookie != null) {
			// first remove any old cookie that is equivalent
			for (Iterator it = cookies.iterator(); it.hasNext();) {
				Cookie tmp = (Cookie) it.next();
				if (cookie.equals(tmp)) {
					it.remove();
					break;
				}
			}
			if (!cookie.isExpired()) {
				cookies.add(cookie);
			}
		}
	}

	/**
	 * Add zero or more cookies
	 * If any given <i>cookie</i> has already expired,
	 * deletes the corresponding existing cookie (if any).
	 * @param newcookies the {@link Cookie}s to add
	 */
	public void addCookies(Cookie[] newcookies) {
		if (newcookies != null) {
			for (int i = 0; i < newcookies.length; i++) {
				this.addCookie(newcookies[i]);
			}
		}
	}

	/**
	 * Obtain an array of my {@link Cookie}s.
	 * @return an array of my {@link Cookie}s.
	 */
	public Cookie[] getCookies() {
		return (Cookie[]) (cookies.toArray(new Cookie[cookies.size()]));
	}

	/**
	 * Obtain an array of my {@link Cookie}s that
	 * match the given request parameters.
	 * @param domain the request domain
	 * @param port the request port
	 * @param path the request path
	 * @param secure <code>true</code> when using HTTPS
	 * @param now the {@link Date} by which expiration is determined
	 * @return an array of my {@link Cookie}s.
	 * @see Cookie#matches
	 */
	public Cookie[] getCookies(String domain, int port, String path, boolean secure, Date now) {
		ArrayList list = new ArrayList(cookies.size());
		for (int i = 0,m = cookies.size(); i < m; i++) {
			Cookie c = (Cookie) (cookies.get(i));
			if (c.matches(domain, port, path, secure, now)) {
				list.add(c);
			}
		}
		return (Cookie[]) (list.toArray(new Cookie[list.size()]));
	}


	/**
	 * Remove all of my {@link Cookie}s that
	 * have expired according to the current
	 * system time.
	 * @see #purgeExpiredCookies(java.util.Date)
	 */
	public boolean purgeExpiredCookies() {
		return purgeExpiredCookies(new Date());
	}

	/**
	 * Remove all of my {@link Cookie}s that
	 * have expired by the specified <i>date</i>.
	 * @see Cookie#isExpired(java.util.Date)
	 */
	public boolean purgeExpiredCookies(Date date) {
		boolean removed = false;
		Iterator it = cookies.iterator();
		while (it.hasNext()) {
			if (((Cookie) (it.next())).isExpired(date)) {
				it.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * <p>
	 * Set the {@link Credentials} for the given authentication realm.
	 * </p>
	 * <p>
	 * When <i>realm</i> is <code>null</code>, I'll use the given
	 * <i>credentials</i> when no other {@link Credentials} have
	 * been supplied for the given challenging realm.
	 * (I.e., use a <code>null</code> realm to set the "default"
	 * credentials.)
	 * </p>
	 * @param realm the authentication realm
	 * @param credentials the authentication credentials for the given realm
	 */
	public void setCredentials(String realm, Credentials credentials) {
		credMap.put(realm, credentials);
	}


	/**
	 * <p>
	 * Get the {@link Credentials} for the given authentication realm.
	 * </p>
	 * <p>
	 * When <i>realm</i> is <code>null</code>, I'll return the
	 * "default" credentials.
	 * (See {@link #setCredentials setCredentials}.)
	 * </p>
	 * @param realm the authentication realm
	 */
	public Credentials getCredentials(String realm) {
		return (Credentials) (credMap.get(realm));
	}

}
