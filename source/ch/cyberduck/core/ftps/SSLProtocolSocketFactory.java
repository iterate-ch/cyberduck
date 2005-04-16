package ch.cyberduck.core.ftps;

/*
 * $Header$
 * $Revision$
 * $Date$
 * 
 * ====================================================================
 *
 *  Copyright 2002-2005 The Apache Software Foundation
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
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class SSLProtocolSocketFactory implements SecureProtocolSocketFactory {
    private static Logger log = Logger.getLogger(SSLProtocolSocketFactory.class);

    private SSLContext sslcontext = null;

    private X509TrustManager trustManager;

    public SSLProtocolSocketFactory(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    private SSLContext createEasySSLContext() {
        try {
            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null,
                    new TrustManager[]{trustManager},
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

    public Socket createSocket(String host,
                               int port,
                               InetAddress clientHost,
                               int clientPort)
            throws IOException, UnknownHostException {

        return this.getSSLContext().getSocketFactory().createSocket(host,
                port,
                clientHost,
                clientPort);
    }

    public Socket createSocket(String host, int port)
            throws IOException, UnknownHostException {
        return this.getSSLContext().getSocketFactory().createSocket(host,
                port);
    }

    public Socket createSocket(Socket socket,
                               String host,
                               int port,
                               boolean autoClose)
            throws IOException, UnknownHostException {
        return this.getSSLContext().getSocketFactory().createSocket(socket,
                host,
                port,
                autoClose);
    }

    public ServerSocket createServerSocket(int port)
            throws IOException {
        return getSSLContext().getServerSocketFactory().createServerSocket(port);
    }

    public boolean equals(Object obj) {
        return ((obj != null) && obj.getClass().equals(SSLProtocolSocketFactory.class));
    }

    public int hashCode() {
        return SSLProtocolSocketFactory.class.hashCode();
    }

}
