package ch.cyberduck.core.ftps;

/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class SSLProtocolSocketFactory implements SecureProtocolSocketFactory {
    private static Logger log = Logger.getLogger(SSLProtocolSocketFactory.class);

    private SSLContext sslcontext = null;

    /**
     * Constructor for SSLProtocolSocketFactory.
     */
    public SSLProtocolSocketFactory() {
        super();
    }

    private static SSLContext createEasySSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null,
                    new TrustManager[]{new CustomX509TrustManager(null)},
                    null);
            return context;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            this.sslcontext = createEasySSLContext();
        }
        return this.sslcontext;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(String host,
                               int port,
                               InetAddress clientHost,
                               int clientPort)
            throws IOException, UnknownHostException {

        return getSSLContext().getSocketFactory().createSocket(host,
                port,
                clientHost,
                clientPort);
    }


    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(host,
                port);
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(Socket socket,
                               String host,
                               int port,
                               boolean autoClose)
            throws IOException, UnknownHostException {
        return getSSLContext().getSocketFactory().createSocket(socket,
                host,
                port,
                autoClose);
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(SSLProtocolSocketFactory.class));
    }

    public int hashCode() {
        return SSLProtocolSocketFactory.class.hashCode();
    }

}
