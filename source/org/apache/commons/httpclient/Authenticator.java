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

import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * <p>Utility methods for HTTP authorization and authentication.</p>
 * <p>
 * This class provides utility methods for generating
 * responses to HTTP authentication challenges.
 * </p>
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @version $Revision$ $Date$
 */
class Authenticator {

    /**
     * Add requisite authentication credentials to the given
     * {@link HttpMethod}, if possible.
     *
     * @param method a {@link HttpMethod} which requires authentication
     * @param state a {@link HttpState} object providing {@link Credentials}
     *
     * @throws HttpException when a parsing or other error occurs
     * @throws UnsupportedOperationException when the given challenge type is not supported
     */
    static boolean authenticate(HttpMethod method, HttpState state) throws HttpException {
        log.debug("Authenticator.authenticate(HttpMethod, HttpState)");
        Header challengeHeader = method.getResponseHeader("WWW-Authenticate");
        if(null == challengeHeader) { return false; }
        String challenge = challengeHeader.getValue();
        if(null == challenge) { return false; }

        int space = challenge.indexOf(' ');
        if(space < 0) {
            throw new HttpException("Unable to parse authentication challenge \"" + challenge + "\", expected space");
        }
        String authScheme = challenge.substring(0, space);

        if ("basic".equalsIgnoreCase(authScheme)) {
            // FIXME: Note that this won't work if there
            //        is more than one realm 
            //        the challenge
            // FIXME: We could probably make it a bit
            //        more flexible in parsing as well.

            // parse the realm from the authentication challenge
            if(challenge.length() < space + 1) {
                throw new HttpException("Unable to parse authentication challenge \"" + challenge + "\", expected realm");
            }
            String realmstr = challenge.substring(space+1,challenge.length());
            realmstr.trim();
            if(realmstr.length()<"realm=\"\"".length()) {
                throw new HttpException("Unable to parse authentication challenge \"" + challenge + "\", expected realm");
            }
            String realm = realmstr.substring("realm=\"".length(),realmstr.length()-1);
            log.debug("Parsed realm \"" + realm + "\" from challenge \"" + challenge + "\".");
            Header header = Authenticator.basic(realm,state);
            if(null != header) {
                method.addRequestHeader(header);
                return true;
            } else {
                return false;
            }
        } else if ("digest".equalsIgnoreCase(authScheme)) {
            throw new UnsupportedOperationException("Digest authentication is not supported.");
        } else {
            throw new UnsupportedOperationException("Authentication type \"" + authScheme + "\" is not recognized.");
        }
    }

    /**
     * Create a Basic <tt>Authorization</tt> header for the given
     * <i>realm</i> and <i>state</i> to the given <i>method</i>.
     *
     * @param method the {@link HttpMethod} to authenticate to
     * @param realm the basic realm to authenticate to
     * @param state a {@link HttpState} object providing {@link Credentials}
     *
     * @return a basic <tt>Authorization</tt> value
     *
     * @throws HttpException when no matching credentials are available
     */
    static Header basic(String realm, HttpState state) throws HttpException {
        log.debug("Authenticator.basic(String,HttpState)");
        UsernamePasswordCredentials cred = null;
        try {
            cred = (UsernamePasswordCredentials)(state.getCredentials(realm));
        } catch(ClassCastException e) {
            throw new HttpException("UsernamePasswordCredentials required for Basic authentication.");
        }
        if(null == cred) {
            if(log.isInfoEnabled()) {
                log.info("No credentials found for realm \"" + realm + "\", attempting to use default credentials.");
            }
            try {
                cred = (UsernamePasswordCredentials)(state.getCredentials(null));
            } catch(ClassCastException e) {
                throw new HttpException("UsernamePasswordCredentials required for Basic authentication.");
            }
        }
        if(null == cred) {
            throw new HttpException("No credentials available for the Basic authentication realm \"" + realm + "\"/");
        } else {
            return new Header("Authorization",Authenticator.basic(cred));
        }
    }

    /**
     * Return a Basic <tt>Authorization</tt> header value for the
     * given {@link UsernamePasswordCredentials}.
     */
    static String basic(UsernamePasswordCredentials cred) throws HttpException {
        String authString = cred.getUserName() + ":" + cred.getPassword();
        return "Basic " + new String(base64.encode(authString.getBytes()));
    }

    // -------------------------------------------------------- Class Variables

    /** <tt>org.apache.commons.httpclient.Authenticator</tt> log. */
    private static final Log log = LogSource.getInstance("org.apache.commons.httpclient.Authenticator");

    /** Base 64 encoder. */
    private static Base64 base64 = new Base64();
}
