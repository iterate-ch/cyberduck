/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class HttpProxySocketProvider extends Socket implements TransportProvider {
    private String proxyHost;
    private int proxyPort;
    private String remoteHost;
    private int remotePort;
    private HttpResponse responseHeader;
    private String providerDetail;

    private HttpProxySocketProvider(String host, int port, String proxyHost,
        int proxyPort) throws IOException, UnknownHostException {
        super(proxyHost, proxyPort);
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    /**
     *
     *
     * @param host
     * @param port
     * @param proxyHost
     * @param proxyPort
     * @param username
     * @param password
     * @param userAgent
     *
     * @return
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    public static HttpProxySocketProvider connectViaProxy(String host,
        int port, String proxyHost, int proxyPort, String username,
        String password, String userAgent)
        throws IOException, UnknownHostException {
        return connectViaProxy(host, port, proxyHost, proxyPort, null,
            username, password, userAgent);
    }

    /**
     *
     *
     * @param host
     * @param port
     * @param proxyHost
     * @param proxyPort
     * @param protocol
     * @param username
     * @param password
     * @param userAgent
     *
     * @return
     *
     * @throws IOException
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static HttpProxySocketProvider connectViaProxy(String host,
        int port, String proxyHost, int proxyPort, String protocol,
        String username, String password, String userAgent)
        throws IOException, UnknownHostException {
        HttpProxySocketProvider socket = new HttpProxySocketProvider(host,
                port, proxyHost, proxyPort);
        int status;
        String providerDetail;

        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            HttpRequest request = new HttpRequest();

            if (protocol == null) {
                protocol = "";
            }

            request.setHeaderBegin("CONNECT " + protocol + host + ":" + port +
                " HTTP/1.0");
            request.setHeaderField("User-Agent", userAgent);
            request.setHeaderField("Pragma", "No-Cache");
            request.setHeaderField("Proxy-Connection", "Keep-Alive");
            out.write(request.toString().getBytes());
            out.flush();
            socket.responseHeader = new HttpResponse(in);
            providerDetail = socket.responseHeader.getHeaderField("server");

            if (socket.responseHeader.getStatus() == 407) {
                String realm = socket.responseHeader.getAuthenticationRealm();
                String method = socket.responseHeader.getAuthenticationMethod();

                if (realm == null) {
                    realm = "";
                }

                if (method.equalsIgnoreCase("basic")) {
                    socket.close();
                    socket = new HttpProxySocketProvider(host, port, proxyHost,
                            proxyPort);
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    request.setBasicAuthentication(username, password);
                    out.write(request.toString().getBytes());
                    out.flush();
                    socket.responseHeader = new HttpResponse(in);
                } else if (method.equalsIgnoreCase("digest")) {
                    throw new IOException(
                        "Digest authentication is not supported");
                } else {
                    throw new IOException("'" + method + "' is not supported");
                }
            }

            status = socket.responseHeader.getStatus();
        } catch (SocketException e) {
            throw new SocketException("Error communicating with proxy server " +
                proxyHost + ":" + proxyPort + " (" + e.getMessage() + ")");
        }

        if ((status < 200) || (status > 299)) {
            throw new IOException("Proxy tunnel setup failed: " +
                socket.responseHeader.getStartLine());
        }

        socket.providerDetail = providerDetail;

        return socket;
    }

    /**
     *
     *
     * @return
     */
    public String toString() {
        return "HTTPProxySocket [Proxy IP=" + getInetAddress() +
        ",Proxy Port=" + getPort() + ",localport=" + getLocalPort() +
        "Remote Host=" + remoteHost + "Remote Port=" +
        String.valueOf(remotePort) + "]";
    }

    /**
     *
     *
     * @return
     */
    public HttpHeader getResponseHeader() {
        return responseHeader;
    }

    /**
     *
     *
     * @return
     */
    public String getProviderDetail() {
        return providerDetail;
    }

    /*public boolean isConnected() {
       return true;
     }*/
}
