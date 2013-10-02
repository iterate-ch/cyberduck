package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends SSLSession<FTPClient> {
    private static final Logger log = Logger.getLogger(FTPSession.class);

    private Timestamp timestamp;

    private UnixPermission permission;

    private FTPListService listService;

    public FTPSession(Host h) {
        super(h);
    }

    @Override
    public boolean isConnected() {
        if(super.isConnected()) {
            return client.isConnected();
        }
        return false;
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.logout();
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    protected void disconnect() {
        try {
            client.disconnect();
        }
        catch(IOException e) {
            log.warn(String.format("Ignore disconnect failure %s", e.getMessage()));
        }
        super.disconnect();
    }

    @Override
    public void noop() throws BackgroundException {
        try {
            client.sendNoOp();
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    protected void configure(final FTPClient client) throws IOException {
        client.setControlEncoding(this.getEncoding());
        client.setConnectTimeout(this.timeout());
        client.setDefaultTimeout(this.timeout());
        client.setDataTimeout(this.timeout());
        client.setDefaultPort(host.getProtocol().getDefaultPort());
        client.setParserFactory(new FTPParserFactory());
        client.setRemoteVerificationEnabled(Preferences.instance().getBoolean("ftp.datachannel.verify"));
        if(host.getProtocol().isSecure()) {
            List<String> protocols = new ArrayList<String>();
            for(String protocol : Preferences.instance().getProperty("connection.ssl.protocols").split(",")) {
                protocols.add(protocol.trim());
            }
            client.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
        }
        final int buffer = Preferences.instance().getInteger("ftp.socket.buffer");
        client.setBufferSize(buffer);
        client.setReceiveBufferSize(buffer);
        client.setSendBufferSize(buffer);
        client.setReceieveDataSocketBufferSize(buffer);
        client.setSendDataSocketBufferSize(buffer);
    }

    /**
     * @return True if the server features AUTH TLS, PBSZ and PROT
     */
    protected boolean isTLSSupported() throws BackgroundException {
        try {
            return client.hasFeature("AUTH", "TLS") && client.hasFeature("PBSZ") && client.hasFeature("PROT");
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    public FTPClient connect(final HostKeyController key) throws BackgroundException {
        final CustomTrustSSLProtocolSocketFactory f
                = new CustomTrustSSLProtocolSocketFactory(this.getTrustManager());

        final FTPClient client = new FTPClient(f, f.getSSLContext());
        client.addProtocolCommandListener(new LoggingProtocolCommandListener() {
            @Override
            public void log(boolean request, String event) {
                FTPSession.this.log(request, event);
            }
        });
        try {
            this.configure(client);
            client.connect(new PunycodeConverter().convert(host.getHostname()), host.getPort());
            client.setTcpNoDelay(false);
            return client;
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    protected FTPConnectMode getConnectMode() {
        if(null == host.getFTPConnectMode()) {
            if(ProxyFactory.get().usePassiveFTP()) {
                return FTPConnectMode.PASV;
            }
            return FTPConnectMode.PORT;
        }
        return this.host.getFTPConnectMode();

    }

    @Override
    public boolean alert() throws BackgroundException {
        if(super.alert()) {
            return !this.isTLSSupported();
        }
        return false;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt, final Cache cache) throws BackgroundException {
        try {
            if(!host.getCredentials().isAnonymousLogin()
                    && !host.getProtocol().isSecure()
                    && this.isTLSSupported()) {
                // Propose protocol change if AUTH TLS is available.
                try {
                    prompt.warn(host.getProtocol(),
                            MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                            MessageFormat.format(LocaleFactory.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"),
                                    ProtocolFactory.FTP_TLS.getName()),
                            LocaleFactory.localizedString("Continue", "Credentials"),
                            LocaleFactory.localizedString("Change", "Credentials"),
                            "connection.unsecure." + host.getHostname());
                    // Continue choosen. Login using plain FTP.
                }
                catch(LoginCanceledException e) {
                    // Protocol switch
                    host.setProtocol(ProtocolFactory.FTP_TLS);
                    // Reconfigure client for TLS
                    this.configure(client);
                    client.execAUTH();
                    client.sslNegotiation();
                }
            }
            if(client.login(host.getCredentials().getUsername(), host.getCredentials().getPassword())) {
                if(host.getProtocol().isSecure()) {
                    client.execPBSZ(0);
                    // Negotiate data connection security
                    client.execPROT(Preferences.instance().getProperty("ftp.tls.datachannel"));
                }
                if("UTF-8".equals(this.getEncoding())) {
                    if(client.hasFeature("UTF8")) {
                        if(!FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8 ON"))) {
                            log.warn(String.format("Failed to negotiate UTF-8 charset %s", client.getReplyString()));
                        }
                    }
                }
                final TimeZone zone = host.getTimezone();
                if(log.isInfoEnabled()) {
                    log.info(String.format("Reset parser to timezone %s", zone));
                }
                String system = null; //Unknown
                try {
                    system = client.getSystemType();
                }
                catch(IOException e) {
                    log.warn(String.format("SYST command failed %s", e.getMessage()));
                }
                listService = new FTPListService(this, system, zone);
                if(client.hasFeature(FTPCmd.MFMT.getCommand())) {
                    timestamp = new FTPMFMTTimestampFeature(this);
                }
                else {
                    timestamp = new FTPUTIMETimestampFeature(this);
                }
                permission = new FTPUnixPermissionFeature(this);
            }
            else {
                throw new LoginFailureException(client.getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    public Path workdir() throws BackgroundException {
        final String directory;
        try {
            directory = client.printWorkingDirectory();
            if(null == directory) {
                throw new FTPException(this.getClient().getReplyCode(), this.getClient().getReplyString());
            }
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
        return new Path(directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return listService.list(file, listener);
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Touch.class) {
            return (T) new DefaultTouchFeature(this);
        }
        if(type == Directory.class) {
            return (T) new FTPDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new FTPDeleteFeature(this);
        }
        if(type == Read.class) {
            return (T) new FTPReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new FTPWriteFeature(this);
        }
        if(type == Move.class) {
            return (T) new FTPMoveFeature(this);
        }
        if(type == UnixPermission.class) {
            return (T) permission;
        }
        if(type == Timestamp.class) {
            return (T) timestamp;
        }
        if(type == Command.class) {
            return (T) new FTPCommandFeature(this);
        }
        return super.getFeature(type);
    }
}