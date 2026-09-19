package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FTPClient extends FTPSClient {
    private static final Logger log = LogManager.getLogger(FTPClient.class);

    private final SSLSocketFactory sslSocketFactory;

    private Protocol protocol;

    private final Preferences preferences
            = PreferencesFactory.get();

    /**
     * Session of control connection put in the session cache for the data connection to resume
     */
    private SSLSession resume;

    public FTPClient(final Protocol protocol, final SSLSocketFactory f, final SSLContext c) {
        super(false, c);
        this.protocol = protocol;
        this.sslSocketFactory = f;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected Socket _openDataConnection_(final String command, final String arg) throws IOException {
        final Socket socket = super._openDataConnection_(command, arg);
        if(null == socket) {
            throw new FTPException(this.getReplyCode(), this.getReplyString());
        }
        if(null != resume && socket instanceof SSLSocket) {
            // Handshake for data connection is complete. A resumed session has the identifier of the session of the
            // control connection, both for the abbreviated handshake in TLS 1.2 and PSK resumption in TLS 1.3
            final SSLSession session = ((SSLSocket) socket).getSession();
            if(!Arrays.equals(resume.getId(), session.getId())) {
                log.warn("Session {} for data connection {} does not resume session {} of control connection. Expect data connection to be rejected by server requiring session reuse", session, socket, resume);
            }
        }
        // Wrap socket to ensure proper TCP shutdown sequence
        return new FTPSocket(socket);
    }

    @Override
    protected void _prepareDataSocket_(final Socket socket) {
        resume = null;
        if(preferences.getBoolean("ftp.tls.session.requirereuse")) {
            if(socket instanceof SSLSocket) {
                // Control socket is SSL
                final SSLSession session = ((SSLSocket) _socket_).getSession();
                final SSLSessionContext context = session.getSessionContext();
                if(null == context) {
                    log.warn("No session context for session {} of control connection", session);
                    return;
                }
                context.setSessionCacheSize(preferences.getInteger("ftp.ssl.session.cache.size"));
                try {
                    final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
                    sessionHostPortCache.setAccessible(true);
                    final Object cache = sessionHostPortCache.get(context);
                    // Prefer the session from the cache. The pre shared key received with the session ticket and
                    // required to resume in TLS 1.3 is set on the copy in the cache and not on the session of the
                    // control socket
                    final SSLSession cached = this.resumable(cache, session);
                    if(cached.isValid()) {
                        final Method putMethod = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                        putMethod.setAccessible(true);
                        putMethod.invoke(cache, this.key(socket), cached);
                        resume = cached;
                    }
                    else {
                        log.warn("SSL session {} for socket {} is not rejoinable", cached, socket);
                    }
                }
                catch(NoSuchFieldException e) {
                    // Not running in expected JRE
                    log.warn("No field sessionHostPortCache in SSLSessionContext", e);
                }
                catch(Exception e) {
                    // Not running in expected JRE
                    log.warn(e.getMessage());
                }
            }
        }
    }

    /**
     * @param cache   Session cache of client
     * @param session Session of control socket used as fallback
     * @return Session of control connection in cache to resume for data connection
     */
    private SSLSession resumable(final Object cache, final SSLSession session) {
        try {
            final Method getMethod = cache.getClass().getDeclaredMethod("get", Object.class);
            getMethod.setAccessible(true);
            final Object cached = getMethod.invoke(cache, this.key(_socket_));
            if(cached instanceof SSLSession) {
                return (SSLSession) cached;
            }
            log.warn("No session in cache for control connection {}", _socket_);
        }
        catch(Exception e) {
            // Not running in expected JRE
            log.warn(e.getMessage());
        }
        return session;
    }

    /**
     * @param socket SSL socket
     * @return Key of socket in session cache of client
     */
    private String key(final Socket socket) throws ReflectiveOperationException {
        Method getHostMethod;
        try {
            getHostMethod = socket.getClass().getMethod("getPeerHost");
        }
        catch(NoSuchMethodException e) {
            // Running in IKVM
            getHostMethod = socket.getClass().getDeclaredMethod("getHost");
        }
        getHostMethod.setAccessible(true);
        return String.format("%s:%s", getHostMethod.invoke(socket), socket.getPort()).toLowerCase(Locale.ROOT);
    }

    @Override
    protected void execAUTH() throws IOException {
        if(protocol.isSecure()) {
            if(FTPReply.SECURITY_DATA_EXCHANGE_COMPLETE != this.sendCommand("AUTH", this.getAuthValue())) {
                throw new FTPException(this.getReplyCode(), this.getReplyString());
            }
        }
    }

    @Override
    public void execPROT(final String prot) throws IOException {
        if(protocol.isSecure()) {
            if(FTPReply.COMMAND_OK != this.sendCommand("PROT", prot)) {
                throw new FTPException(this.getReplyCode(), this.getReplyString());
            }
            if("P".equals(prot)) {
                // Private
                this.setSocketFactory(sslSocketFactory);
            }
        }
    }

    @Override
    public void execPBSZ(final long pbsz) throws IOException {
        if(protocol.isSecure()) {
            if(FTPReply.COMMAND_OK != this.sendCommand("PBSZ", String.valueOf(pbsz))) {
                throw new FTPException(this.getReplyCode(), this.getReplyString());
            }
        }
    }

    @Override
    protected void sslNegotiation() throws IOException {
        if(protocol.isSecure()) {
            final SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(_socket_,
                    _socket_.getInetAddress().getHostName(), _socket_.getPort(), false);
            socket.setEnableSessionCreation(true);
            socket.setUseClientMode(true);
            socket.startHandshake();
            _socket_ = socket;
            _controlInput_ = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), getControlEncoding()));
            _controlOutput_ = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), getControlEncoding()));
        }
    }

    public List<String> list(final FTPCmd command) throws IOException {
        return this.list(command, null);
    }

    public List<String> list(final FTPCmd command, final String pathname) throws IOException {
        this.pret(command, null == pathname ? StringUtils.EMPTY : pathname);

        Socket socket = _openDataConnection_(command, pathname);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), getControlEncoding()));
        ArrayList<String> results = new ArrayList<String>();
        String line;
        while((line = reader.readLine()) != null) {
            _commandSupport_.fireReplyReceived(-1, line);
            results.add(line);
        }

        reader.close();
        socket.close();

        if(!this.completePendingCommand()) {
            throw new FTPException(this.getReplyCode(), this.getReplyString());
        }
        return results;
    }

    @Override
    public String getSystemType() {
        try {
            return super.getSystemType();
        }
        catch(IOException e) {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public boolean hasFeature(final String feature, final String value) {
        try {
            return super.hasFeature(feature, value);
        }
        catch(IOException e) {
            return false;
        }
    }

    @Override
    public boolean hasFeature(String feature) {
        try {
            return super.hasFeature(feature);
        }
        catch(IOException e) {
            return false;
        }
    }

    @Override
    public boolean retrieveFile(String remote, OutputStream local) throws IOException {
        this.pret(FTPCmd.RETR, remote);
        return super.retrieveFile(remote, local);
    }

    @Override
    public InputStream retrieveFileStream(String remote) throws IOException {
        this.pret(FTPCmd.RETR, remote);
        return super.retrieveFileStream(remote);
    }

    @Override
    public boolean storeFile(String remote, InputStream local) throws IOException {
        this.pret(FTPCmd.STOR, remote);
        return super.storeFile(remote, local);
    }

    @Override
    public OutputStream storeFileStream(String remote) throws IOException {
        this.pret(FTPCmd.STOR, remote);
        return super.storeFileStream(remote);
    }

    @Override
    public boolean appendFile(String remote, InputStream local) throws IOException {
        this.pret(FTPCmd.APPE, remote);
        return super.appendFile(remote, local);
    }

    @Override
    public OutputStream appendFileStream(String remote) throws IOException {
        this.pret(FTPCmd.APPE, remote);
        return super.appendFileStream(remote);
    }

    /**
     * http://drftpd.org/index.php/PRET_Specifications
     *
     * @param command Command to execute
     * @param file    Remote file
     * @throws IOException I/O failure
     */
    protected void pret(final FTPCmd command, final String file) throws IOException {
        if(this.hasFeature("PRET")) {
            if(!FTPReply.isPositiveCompletion(this.sendCommand("PRET", String.format("%s %s", command.getCommand(), file)))) {
                throw new FTPException(this.getReplyCode(), this.getReplyString());
            }
        }
    }

    @Override
    public String getModificationTime(final String file) throws IOException {
        final String status = super.getModificationTime(file);
        if(null == status) {
            throw new FTPException(this.getReplyCode(), this.getReplyString());
        }
        return StringUtils.chomp(status.substring(3).trim());
    }
}
