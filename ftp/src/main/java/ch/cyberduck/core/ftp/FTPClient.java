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
import org.apache.log4j.Logger;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FTPClient extends FTPSClient {
    private static final Logger log = Logger.getLogger(FTPClient.class);

    private SSLSocketFactory sslSocketFactory;

    private Protocol protocol;

    /**
     * Map of FEAT responses. If null, has not been initialised.
     */
    private Map<String, Set<String>> features;

    private Preferences preferences
            = PreferencesFactory.get();

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
        return socket;
    }

    @Override
    protected void _prepareDataSocket_(final Socket socket) throws IOException {
        if(preferences.getBoolean("ftp.tls.session.requirereuse")) {
            if(socket instanceof SSLSocket) {
                // Control socket is SSL
                final SSLSession session = ((SSLSocket) _socket_).getSession();
                if(session.isValid()) {
                    final SSLSessionContext context = session.getSessionContext();
                    context.setSessionCacheSize(preferences.getInteger("ftp.ssl.session.cache.size"));
                    try {
                        final Field sessionHostPortCache = context.getClass().getDeclaredField("sessionHostPortCache");
                        sessionHostPortCache.setAccessible(true);
                        final Object cache = sessionHostPortCache.get(context);
                        final Method method = cache.getClass().getDeclaredMethod("put", Object.class, Object.class);
                        method.setAccessible(true);
                        method.invoke(cache, String.format("%s:%s", socket.getInetAddress().getHostName(),
                                String.valueOf(socket.getPort())).toLowerCase(Locale.ROOT), session);
                        method.invoke(cache, String.format("%s:%s", socket.getInetAddress().getHostAddress(),
                                String.valueOf(socket.getPort())).toLowerCase(Locale.ROOT), session);
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
                else {
                    log.warn(String.format("SSL session %s for socket %s is not rejoinable", session, socket));
                }
            }
        }
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
                    _socket_.getInetAddress().getHostAddress(), _socket_.getPort(), false);
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
        this.pret(command, pathname);

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

    /**
     * Query the server for a supported feature, and returns its values (if any).
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @return if the feature is present, returns the feature values (empty array if none)
     * Returns {@code null} if the feature is not found or the command failed.
     * Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException
     * @since 3.0
     */
    public String[] featureValues(String feature) throws IOException {
        if(!initFeatureMap()) {
            return null;
        }
        Set<String> entries = features.get(feature.toUpperCase(Locale.ROOT));
        if(entries != null) {
            return entries.toArray(new String[entries.size()]);
        }
        return null;
    }

    /**
     * Query the server for a supported feature, and returns the its value (if any).
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @return if the feature is present, returns the feature value or the empty string
     * if the feature exists but has no value.
     * Returns {@code null} if the feature is not found or the command failed.
     * Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException
     * @since 3.0
     */
    public String featureValue(String feature) throws IOException {
        String[] values = featureValues(feature);
        if(values != null) {
            return values[0];
        }
        return null;
    }

    /**
     * Query the server for a supported feature.
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present
     * or the {@link #feat()} command failed. Check {@link #getReplyCode()} or {@link #getReplyString()}
     * if it is necessary to distinguish these cases.
     * @throws IOException
     * @since 3.0
     */
    public boolean hasFeature(String feature) throws IOException {
        if(!initFeatureMap()) {
            return false;
        }
        return features.containsKey(feature.toUpperCase(Locale.ROOT));
    }

    /**
     * Query the server for a supported feature with particular value,
     * for example "AUTH SSL" or "AUTH TLS".
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @param value   the value to find.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present
     * or the {@link #feat()} command failed. Check {@link #getReplyCode()} or {@link #getReplyString()}
     * if it is necessary to distinguish these cases.
     * @throws IOException
     * @since 3.0
     */
    public boolean hasFeature(String feature, String value) throws IOException {
        if(!initFeatureMap()) {
            return false;
        }
        Set<String> entries = features.get(feature.toUpperCase(Locale.ROOT));
        if(entries != null) {
            return entries.contains(value);
        }
        return false;
    }

    /*
     * Create the feature map if not already created.
     */
    private boolean initFeatureMap() throws IOException {
        if(features == null) {
            // Don't create map here, because next line may throw exception
            final int reply = feat();
            if(FTPReply.NOT_LOGGED_IN == reply) {
                return false;
            }
            else {
                // we init the map here, so we don't keep trying if we know the command will fail
                features = new HashMap<String, Set<String>>();
            }
            boolean success = FTPReply.isPositiveCompletion(reply);
            if(!success) {
                return false;
            }
            for(String l : getReplyStrings()) {
                if(l.startsWith(" ")) { // it's a FEAT entry
                    String key;
                    String value = "";
                    int varsep = l.indexOf(' ', 1);
                    if(varsep > 0) {
                        key = l.substring(1, varsep);
                        value = l.substring(varsep + 1);
                    }
                    else {
                        key = l.substring(1);
                    }
                    key = key.toUpperCase(Locale.ROOT);
                    Set<String> entries = features.get(key);
                    if(entries == null) {
                        entries = new HashSet<String>();
                        features.put(key, entries);
                    }
                    entries.add(value);
                }
            }
        }
        return true;
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