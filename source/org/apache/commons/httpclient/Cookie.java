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

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * <p>An HTTP "magic-cookie", as specified in RFC 2109.</p>
 *
 * @author	B.C. Holmes
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @author <a href="mailto:dsale@us.britannica.com">Doug Sale</a>
 * @author Rod Waldhoff
 * @version $Revision$ $Date$
 */

public class Cookie extends NameValuePair implements Serializable {

    // ----------------------------------------------------------- Constructors

    /**
     * Create a cookie.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the host this cookie will be sent to
     */
    public Cookie(String domain, String name, String value) {
        this(domain,name,value,null,null,false);
    }

    /**
     * Create a cookie.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the host this cookie will be sent to
     * @param path    the path prefix for which this cookie will be sent
     * @param expires the {@link Date} at which this cookie expires,
     *                or <tt>null</tt> if the cookie expires at the end
     *                of the session
     * @param secure  if true this cookie will only be sent over secure connections
     */
    public Cookie(String domain, String name, String value, String path, Date expires, boolean secure) {
        super(name, value);
        this.setPath(path);
        this.setDomain(domain);
        this.setExpiryDate(expires);
        this.setSecure(secure);
    }

    /**
     * Create a cookie.
     *
     * @param name    the cookie name
     * @param value   the cookie value
     * @param domain  the host this cookie will be sent to
     * @param path    the path prefix for which this cookie will be sent
     * @param maxAge  the number of seconds for which this cookie is valid
     * @param secure  if <tt>true</tt> this cookie will only be sent over secure connections
     */
    public Cookie(String domain, String name, String value, String path, int maxAge, boolean secure) {
        this(domain,name,value,path,new Date(System.currentTimeMillis() + maxAge*1000L),secure);
    }

    /**
     * Returns the comment describing the purpose of this cookie, or
     * <tt>null</tt> if no such comment has been defined.
     *
     * @see #setComment(String)
     */
    public String getComment() {
        return _comment;
    }

    /**
     * If a user agent (web browser) presents this cookie to a user, the
     * cookie's purpose will be described using this comment.
     *
     * @see #getComment()
     */
    public void setComment(String comment) {
        _comment = comment;
    }

    /**
     * Returns my expiration {@link Date}, or <tt>null</tt>
     * if none exists.
     * @return my expiration {@link Date}, or <tt>null</tt>.
     */
    public Date getExpiryDate() {
        return _expiryDate;
    }

    /**
     * Expiration setter.
     * <p>
     * Netscape's original proposal defined an Expires header that took
     * a date value in a fixed-length variant format in place of Max-Age:
     * <br>
     * <tt>Wdy, DD-Mon-YY HH:MM:SS GMT</tt>
     * <br>
     * Note that the Expires date format contains embedded spaces, and that
     * "old" cookies did not have quotes around values.  Clients that
     * implement to this specification should be aware of "old" cookies and
     * Expires.
     * </p>
     * @param expiryDate the {@link Date} after which this cookie is no longer valid.
     */
    public void setExpiryDate (Date expiryDate) {
        _expiryDate = expiryDate;
    }


    /**
     * Returns <tt>false</tt> if I should be discarded at the end
     * of the "session"; <tt>true</tt> otherwise.
     *
     * @return <tt>false</tt> if I should be discarded at the end
     *         of the "session"; <tt>true</tt> otherwise
     */
    public boolean isPersistent() {
        return (null != _expiryDate);
    }


    /**
     * Returns my domain.
     *
     * @see #setDomain(java.lang.String)
     */
    public String getDomain() {
        return _domain;
    }

    /**
     * Sets my domain.
     * <p>
     * I should be presented only to hosts satisfying this domain
     * name pattern.  Read RFC 2109 for specific details of the syntax.
     * Briefly, a domain name name begins with a dot (".foo.com") and means
     * that hosts in that DNS zone ("www.foo.com", but not "a.b.foo.com")
     * should see the cookie.  By default, cookies are only returned to
     * the host which saved them.
     *
     * @see #getDomain
     */
    public void setDomain(String domain) {
        int ndx = domain.indexOf(":");
        if (ndx != -1) {
          domain = domain.substring(0, ndx);
        }
        _domain = domain.toLowerCase();
    }


    /**
     * Return my path.
     * @see #setPath(java.lang.String)
     */
    public String getPath() {
        return _path;
    }

    /**
     * Sets my path.
     * <p>
     * I should be presented only with requests beginning with this path.
     * See RFC 2109 for a specification of the default behaviour. Basically, URLs
     * in the same "directory" as the one which set the cookie, and in subdirectories,
     * can all see the cookie unless a different path is set.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Return whether this cookie should only be sent over secure connections.
     * @see #setSecure(boolean)
     */
    public boolean getSecure() {
        return _secure;
    }

    /**
     * Set my secure flag.
     * <p>
     * When <tt>true</tt> the cookie should only be sent
     * using a secure protocol (https).  This should only be set when
     * the cookie's originating server used a secure protocol to set the
     * cookie's value.
     *
     * @see #getSecure()
     */
    public void setSecure (boolean secure) {
        _secure = secure;
    }

    /**
     * Return the version of the HTTP cookie specification I use.
     */
    public int getVersion() {
        return _version;
    }

    /**
     * Set the version of the HTTP cookie specification I report.
     * <p>
     * The current implementation only sends version 1 cookies.
     * (See RFC 2109 for details.)
     */
    public void setVersion(int version) {
        _version = version;
    }

    /**
     * Return <tt>true</tt> if I have expired.
     * @return <tt>true</tt> if I have expired.
     */
    public boolean isExpired() {
        return (_expiryDate != null  &&
            _expiryDate.getTime() <= System.currentTimeMillis());
    }

    /**
     * Return <tt>true</tt> if I have expired.
     * @return <tt>true</tt> if I have expired.
     */
    public boolean isExpired(Date now) {
        return (_expiryDate != null  &&
            _expiryDate.getTime() <= now.getTime());
    }


    /**
     * Returns a hash code in keeping with the
     * {@link Object#hashCode general hashCode contract}.
     */
    public int hashCode() {
        return super.hashCode() ^
               (null == _path ? 0 : _path.hashCode()) ^
               (null == _domain ? 0 : _domain.hashCode());
    }


    /**
     * Two cookies are equal if the name, path and domain match.
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof Cookie)) {
            Cookie that = (Cookie) obj;
            return (null == this.getName() ? null == that.getName() : this.getName().equals(that.getName())) &&
                   (null == this.getPath() ? null == that.getPath() : this.getPath().equals(that.getPath())) &&
                   (null == this.getDomain() ? null == that.getDomain() : this.getDomain().equals(that.getDomain()));
        } else {
            return false;
        }
    }


    /**
     * Return a string suitable for sending in a Cookie header.
     * @return a string suitable for sending in a Cookie header.
     */
    public String toExternalForm() {
        StringBuffer buf = new StringBuffer();
        buf.append(getName()).append("=").append(getValue());
        if (_path != null) {
            buf.append(";$Path=");
            buf.append(_path);
        }
        if (_domain != null) {
            buf.append(";$Domain=");
            buf.append(_domain);
        }
        return buf.toString();
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with
     * given attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currenlty ignored)
     * @param path the path to which the request is being submitted
     * @param secure <tt>true</tt> if the request is using the HTTPS protocol
     * @param date the time at which the request is submitted
     */
    public boolean matches(String domain, int port, String path, boolean secure, Date now) {
        // FIXME: RFC2109 doesn't consider ports when filtering/matching
        //        cookies. Quoting from Section 2 - Terminology:
        //          The terms request-host and request-URI refer to
        //          the values the client would send to the server
        //          as, respectively, the host (but not port) and
        //          abs_path portions of the absoluteURI (http_URL)
        //          of the HTTP request line.
        //
        //        RFC2965 includes ports in cookie-sending
        //        determination, but only when the cookie is received
        //        via a 'Set-Cookie2' header.
        //
        //        The current implementation doesn't support RFC2965,
        //        and ignores ports when matching cookies.
        domain = domain.toLowerCase();

        // FIXME: Is path.startsWith(cookie.getPath()) enough?
        //        Or do we need to check that we are comparing
        //        at a path-element break?
        //        E.g.., if "/foo" is the cookie's path,
        //        should /foobar see the cookie? Probably not.
        return (
                (getExpiryDate() == null || getExpiryDate().after(now)) && // only add the cookie if it hasn't yet expired
                domain.endsWith(getDomain()) &&                            // and the domain pattern matches
                ((getPath() == null) || (path.startsWith(getPath()))) &&   // and the path is null or matching
                (getSecure() ? secure : true)                              // and if the secure flag is set, only if the request is actually secure
               );
    }

    /**
     * Return <tt>true</tt> if I should be submitted with a request with
     * given attributes, <tt>false</tt> otherwise.
     * @param domain the host to which the request is being submitted
     * @param port the port to which the request is being submitted (currenlty ignored)
     * @param path the path to which the request is being submitted
     */
    public boolean matches(String domain, int port, String path, boolean secure) {
        return matches(domain,port,path,secure,new Date());
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i> and
     * <i>path</i>, assuming the connection is not
     * secure.
     * <p>
     * If no cookies match, returns null.
     */
    public static Header createCookieHeader(String domain, String path, Cookie[] cookies) {
        return Cookie.createCookieHeader(domain,path,false,cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>path</i> and
     * <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     * @deprecated use the version which includes port number and date
     */
    public static Header createCookieHeader(String domain, String path, boolean secure, Cookie[] cookies) {
        // parse port from domain, if any
        int port = secure ? 443 : 80;
        int ndx = domain.indexOf(":");
        if (ndx != -1) {
            try {
                port = Integer.parseInt(domain.substring(ndx+1,domain.length()));
            } catch(NumberFormatException e){
                // ignore?
            }
        }
        return Cookie.createCookieHeader(domain,port,path,secure,cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all non-expired cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>,
     * <i>path</i> and <i>https</i> setting.
     * <p>
     * If no cookies match, returns null.
     */
    public static Header createCookieHeader(String domain, int port, String path, boolean secure, Cookie[] cookies) {
        return Cookie.createCookieHeader(domain,port,path,secure,new Date(),cookies);
    }

    /**
     * Create a <tt>Cookie</tt> header containing
     * all cookies in <i>cookies</i>,
     * associated with the given <i>domain</i>, <i>port</i>,
     * <i>path</i> and <i>https</i> setting, and which are
     * not expired according to the given <i>date</i>.
     * <p>
     * If no cookies match, returns null.
     */
    public static Header createCookieHeader(String domain, int port, String path, boolean secure, Date now, Cookie[] cookies) {
        boolean added = false;
        StringBuffer value = new StringBuffer("$Version=1");
        // FIXME: cookies are supposed to be ordered with "better"
        //        matches (more specific path) first
        for(int i=0;i<cookies.length;i++) {
            if(cookies[i].matches(domain,port,path,secure,now)) {
                added = true;
                value.append(";");
                value.append(cookies[i].toExternalForm());
            }
        }
        if(added) {
            return new Header("Cookie", value.toString());
        } else {
            return null;
        }
    }

    /**
     * Return a {@link String} representation of me.
     * @see #toExternalForm
     */
    public String toString() {
        return toExternalForm();
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param port the port from which the {@link Header} was received (currently ignored)
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link Header}
     * @throws HttpException if an exception occurs during parsing
     */
    public static Cookie[] parse(String domain, int port, String path, Header setCookie) throws HttpException {
        return Cookie.parse(domain,port,path,false,setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s, assuming that the cookies were recieved
     * on an insecure channel.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link Header}
     * @throws HttpException if an exception occurs during parsing
     */
    public static Cookie[] parse(String domain, String path, Header setCookie) throws HttpException {
        return Cookie.parse(domain,80,path,false,setCookie);
    }

    /**
     * Parses the Set-Cookie {@link Header} into an array of
     * <tt>Cookie</tt>s.
     *
     * @param domain the domain from which the {@link Header} was received
     * @param path the path from which the {@link Header} was received
     * @param secure <tt>true</tt> when the header was recieved over a secure channel
     * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the server
     * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link Header}
     * @throws HttpException if an exception occurs during parsing
     */
    public static Cookie[] parse(String domain, String path, boolean secure, Header setCookie) throws HttpException {
        return Cookie.parse(domain,(secure ? 443 : 80),path,secure,setCookie);
    }

    /**
      * Parses the Set-Cookie {@link Header} into an array of
      * <tt>Cookie</tt>s.
      *
      * <P>The syntax for the Set-Cookie response header is:
      *
      * <PRE>
      * set-cookie      =    "Set-Cookie:" cookies
      * cookies         =    1#cookie
      * cookie          =    NAME "=" VALUE * (";" cookie-av)
      * NAME            =    attr
      * VALUE           =    value
      * cookie-av       =    "Comment" "=" value
      *                 |    "Domain" "=" value
      *                 |    "Max-Age" "=" value
      *                 |    "Path" "=" value
      *                 |    "Secure"
      *                 |    "Version" "=" 1*DIGIT
      * </PRE>
      *
      * @param domain the domain from which the {@link Header} was received
      * @param path the path from which the {@link Header} was received
      * @param secure <tt>true</tt> when the {@link Header} was received over HTTPS
      * @param setCookie the <tt>Set-Cookie</tt> {@link Header} received from the server
      * @return an array of <tt>Cookie</tt>s parsed from the Set-Cookie {@link Header}
      * @throws HttpException if an exception occurs during parsing
      */
    public static Cookie[] parse(String domain, int port, String path, boolean secure, Header setCookie) throws HttpException {
        HeaderElement[] headerElements =
            HeaderElement.parse(setCookie.getValue());

        Cookie[] cookies = new Cookie[headerElements.length];
        int index = 0;
        for (int i = 0; i < headerElements.length; i++) {

            Cookie cookie = new Cookie(domain,
                                       headerElements[i].getName(),
                                       headerElements[i].getValue());

            // cycle through the parameters
            NameValuePair[] parameters = headerElements[i].getParameters();
            // could be null. In case only a header element and no parameters.
            if (parameters != null) {
                boolean discard_set = false, secure_set = false;
                for (int j = 0; j < parameters.length; j++) {
                    String name = parameters[j].getName().toLowerCase();

                    // check for required value parts
                    if ( (name.equals("version") || name.equals("max-age") ||
                          name.equals("domain") || name.equals("path") ||
                          name.equals("comment") || name.equals("expires")) &&
                          parameters[j].getValue() == null) {
                        if(log.isDebugEnabled()) {
                            log.debug("Cookie.parse(): Unable to parse set-cookie header \"" + setCookie.getValue() + "\" because \"" + parameters[j].getName() + "\" requires a value in cookie \"" + headerElements[i].getName() + "\".");
                        }
                        throw new HttpException(
                            "Bad Set-Cookie header: " + setCookie.getValue() +
                            "\nMissing value for " +
                            parameters[j].getName() +
                            " attribute in cookie '" +
                            headerElements[i].getName() + "'");
                    }

                    if (name.equals("version")) {
                        try {
                           cookie.setVersion(
                               Integer.parseInt(parameters[j].getValue()));
                        } catch (NumberFormatException nfe) {
                            if(log.isDebugEnabled()) {
                                log.debug("Cookie.parse(): Exception attempting to parse set-cookie header \"" + setCookie.getValue() + "\" because version attribute value \"" + parameters[j].getValue() + "\" is not a number in cookie \"" + headerElements[i].getName() + "\".",nfe);
                            }
                            throw new HttpException(
                                    "Bad Set-Cookie header: " +
                                    setCookie.getValue() + "\nVersion '" +
                                    parameters[j].getValue() + "' not a number");
                        }
                    } else if (name.equals("path")) {
                        cookie.setPath(parameters[j].getValue());
                    } else if (name.equals("domain")) {
                        String d = parameters[j].getValue().toLowerCase();
                        // add leading dot if not present and if domain is
                        // not the full host name

                        // FIXME: Is this the right thing to do?
                        //        According to 4.3.2 of RFC 2109,
                        //        we should reject these.
                        //        I'm not sure this rejection logic
                        //        is RFC 2109 compliant otherwise either
                        //        (probably MSIE and Netscape aren't
                        //        either)

                        if (d.charAt(0) != '.' && !d.equals(domain))
                            cookie.setDomain("." + d);
                        else
                            cookie.setDomain(d);
                    } else if (name.equals("max-age")) {
                        int age;
                        try {
                            age = Integer.parseInt(parameters[j].getValue());
                        } catch (NumberFormatException e) {
                            if(log.isDebugEnabled()) {
                                log.debug("Cookie.parse(): Exception attempting to parse set-cookie header \"" + setCookie.getValue() + "\" because max-age attribute value \"" + parameters[j].getValue() + "\" is not a number in cookie \"" + headerElements[i].getName() + "\".",e);
                            }
                            throw new HttpException(
                                    "Bad Set-Cookie header: " +
                                    setCookie.getValue() + " Max-Age '" +
                                    parameters[j].getValue() + "' not a number");
                        }
                        cookie.setExpiryDate(new Date(System.currentTimeMillis() +
                                age * 1000L));
                    } else if (name.equals("secure")) {
                        cookie.setSecure(true);
                    } else if (name.equals("comment")) {
                        cookie.setComment(parameters[j].getValue());
                    } else if (name.equals("expires")) {
                        boolean set = false;
                        String expiryDate = parameters[j].getValue();
                        for(int k=0;k<expiryFormats.length;k++) {
                            try {
                                Date date = expiryFormats[k].parse(expiryDate);
                                cookie.setExpiryDate(date);
                                set = true;
                                break;
                            } catch (ParseException e) {
                                if(log.isDebugEnabled()) {
                                    log.debug("Cookie.parse(): Exception attempting to parse set-cookie header \"" + setCookie.getValue() + "\" because expires attribute value \"" + parameters[j].getValue() + "\" cannot be parsed by date format \"" + k + "\" in cookie " + headerElements[i].getName() + "\". Will try another.");
                                }
                            }
                        }
                        if(!set) {
                            if(log.isInfoEnabled()) {
                                log.info("Cookie.parse(): Unable to parse expiration date parameter: \"" + expiryDate + "\"");
                            }
                            throw new HttpException("Unable to parse expiration date parameter: \"" + expiryDate + "\"");
                        }
                    }
                }
            }

            // check version
            if (cookie.getVersion() != 1) {
                if(log.isInfoEnabled()) {
                    log.info("Cookie.parse(): Rejecting set cookie header \"" + setCookie.getValue() + "\" because it has an unrecognized version attribute (" + cookie.getVersion() + ").");
                }
                throw new HttpException(
                        "Bad Set-Cookie header: " + setCookie.getValue() +
                        " Illegal Version attribute");
            }

            // security check... we musn't allow the server to give us an
            // invalid domain scope

            // domain must be either .local or must contain at least two dots
            if (!cookie.getDomain().equals("localhost")) {

                // Not required to have at least two dots.  RFC 2965.
                // A Set-Cookie2 with Domain=ajax.com will be accepted.

                // domain must domain match host
                if (!domain.endsWith(cookie.getDomain())){
                    if(log.isInfoEnabled()) {
                        log.info("Cookie.parse(): Rejecting set cookie header \"" + setCookie.getValue() + "\" because \"" + cookie.getName() + "\" has an illegal domain attribute (\"" + cookie.getDomain() + "\") for the domain \"" + domain + "\".");
                    }
                    throw new HttpException(
                        "Bad Set-Cookie header: " + setCookie.getValue() +
                        " Illegal domain attribute" + cookie.getDomain());
                }

                // host minus domain may not contain any dots
                if (domain.substring(0,
                        domain.length() -
                        cookie.getDomain().length()).indexOf('.') != -1) {
                    if(log.isInfoEnabled()) {
                        log.info("Cookie.parse(): Rejecting set cookie header \"" + setCookie.getValue() + "\" because \"" + cookie.getName() + "\" has an illegal domain attribute (\"" + cookie.getDomain() + "\") for the given domain \"" + domain + "\".");
                    }
                    throw new HttpException(
                        "Bad Set-Cookie header: " + setCookie.getValue() +
                        " Illegal domain attribute " + cookie.getDomain());
                }
            }

            // another security check... we musn't allow the server to give us a
            // secure cookie over an insecure channel

            if(cookie.getSecure() && !secure) {
                if(log.isInfoEnabled()) {
                    log.info("Cookie.parse(): Rejecting set cookie header \"" + setCookie.getValue() + "\" because \"" + cookie.getName() + "\" has an illegal secure attribute (\"" + cookie.getSecure() + "\") for the given security  \"" + secure + "\".");
                }
                throw new HttpException(
                    "Bad Set-Cookie header: " + setCookie.getValue() +
                    " Secure cookie sent over a non-secure channel.");
            }

            // another security check... we musn't allow the server to give us a
            // cookie that doesn't match this path

            if(cookie.getPath() != null && (!path.startsWith(cookie.getPath()))) {
                if(log.isInfoEnabled()) {
                    log.info("Cookie.parse(): Rejecting set cookie header \"" + setCookie.getValue() + "\" because \"" + cookie.getName() + "\" has an illegal path attribute (\"" + cookie.getPath() + "\") for the given path \"" + path + "\".");
                }
                throw new HttpException(
                    "Bad Set-Cookie header: " + setCookie.getValue() +
                    " Header targets a different path, found \"" +
                    cookie.getPath() + "\" for \"" + path + "\"");
            }

            // set path if not otherwise specified
            if(null == cookie.getPath()) {
                if(null != path) {
                    if(!path.endsWith("/")) {
                        int x = path.lastIndexOf("/");
                        if(0 < x) {
                            cookie.setPath(path.substring(0,x));
                        } else {
                            cookie.setPath("/");
                        }
                    } else {
                        cookie.setPath(path);
                    }
                }
            }
            cookies[index++] = cookie;
        }

        return cookies;
    }

   // ----------------------------------------------------- Instance Variables

   /** My comment. */
   private String  _comment;

   /** My domain. */
   private String  _domain;

   /** My expiration {@link Date}. */
   private Date    _expiryDate;

   /** My path. */
   private String  _path;

   /** My secure flag. */
   private boolean _secure;

   /** The version of the cookie specification I was created from. */
   private int     _version = 1;

   // -------------------------------------------------------------- Constants

   /** List of valid date formats for the "expires" cookie attribute. */
   private static final DateFormat[] expiryFormats = new DateFormat[4];

   /** Static initializer for {@link #expiryFormats} constant. */
   static {
       // RFC 1123, 822, Date and time specification is English.
       expiryFormats[0] = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss z", Locale.US);
       expiryFormats[1] = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
       expiryFormats[2] = new SimpleDateFormat("EEE dd-MMM-yy HH:mm:ss z", Locale.US);
       expiryFormats[3] = new SimpleDateFormat("EEE dd-MMM-yyyy HH:mm:ss z", Locale.US);
   }

   /** <tt>org.apache.commons.httpclient.Cookie</tt> log. */
   private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.Cookie");

}

