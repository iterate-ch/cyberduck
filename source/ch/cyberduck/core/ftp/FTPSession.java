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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.ProxySocketFactory;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.TrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends SSLSession<FTPClient> {
    private static final Logger log = Logger.getLogger(FTPSession.class);

    private Preferences preferences
            = Preferences.instance();

    private Timestamp timestamp;

    private UnixPermission permission;

    private Symlink symlink;

    private FTPListService listService;

    public FTPSession(final Host h) {
        super(h);
    }

    public FTPSession(final Host host, final X509TrustManager manager) {
        super(host, manager);
    }

    public FTPSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
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

    protected void configure(final FTPClient client) throws IOException {
        client.setSocketFactory(new ProxySocketFactory(host.getProtocol(), new TrustManagerHostnameCallback() {
            @Override
            public String getTarget() {
                return host.getHostname();
            }
        }));
        client.setControlEncoding(this.getEncoding());
        client.setConnectTimeout(this.timeout());
        client.setDefaultTimeout(this.timeout());
        client.setDataTimeout(this.timeout());
        client.setDefaultPort(host.getProtocol().getDefaultPort());
        client.setParserFactory(new FTPParserFactory());
        client.setRemoteVerificationEnabled(preferences.getBoolean("ftp.datachannel.verify"));
        if(host.getProtocol().isSecure()) {
            List<String> protocols = new ArrayList<String>();
            for(String protocol : preferences.getProperty("connection.ssl.protocols").split(",")) {
                protocols.add(protocol.trim());
            }
            client.setEnabledProtocols(protocols.toArray(new String[protocols.size()]));
        }
        final int buffer = preferences.getInteger("ftp.socket.buffer");
        client.setBufferSize(buffer);

        client.setReceiveBufferSize(preferences.getInteger("connection.buffer.receive"));
        client.setSendBufferSize(preferences.getInteger("connection.buffer.send"));
        client.setReceieveDataSocketBufferSize(preferences.getInteger("connection.buffer.receive"));
        client.setSendDataSocketBufferSize(preferences.getInteger("connection.buffer.send"));

        client.setStrictMultilineParsing(preferences.getBoolean("ftp.parser.multiline.strict"));
    }

    /**
     * @return True if the server features AUTH TLS, PBSZ and PROT
     */
    protected boolean isTLSSupported() throws BackgroundException {
        try {
            return client.hasFeature("AUTH", "TLS")
                    && client.hasFeature("PBSZ")
                    && client.hasFeature("PROT");
        }
        catch(IOException e) {
            throw new FTPExceptionMappingService().map(e);
        }
    }

    @Override
    public FTPClient connect(final HostKeyCallback key, final TranscriptListener transcript) throws BackgroundException {
        try {
            final CustomTrustSSLProtocolSocketFactory f
                    = new CustomTrustSSLProtocolSocketFactory(this.getTrustManager(), this.getKeyManager());

            final LoggingProtocolCommandListener listener = new LoggingProtocolCommandListener(transcript);
            final FTPClient client = new FTPClient(f, f.getSSLContext()) {
                @Override
                public void disconnect() throws IOException {
                    try {
                        super.disconnect();
                    }
                    finally {
                        this.removeProtocolCommandListener(listener);
                    }
                }
            };
            client.addProtocolCommandListener(listener);
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
        if(FTPConnectMode.unknown == host.getFTPConnectMode()) {
            if(ProxyFactory.get().usePassiveFTP()) {
                // Default to PASV
                return FTPConnectMode.passive;
            }
            return FTPConnectMode.active;
        }
        return this.host.getFTPConnectMode();

    }

    @Override
    public boolean alert() throws BackgroundException {
        if(super.alert()) {
            // Only alert if no option to switch to TLS later is possible
            return !this.isTLSSupported();
        }
        return false;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache, final TranscriptListener transcript) throws BackgroundException {
        try {
            if(super.alert() && this.isTLSSupported()) {
                // Propose protocol change if AUTH TLS is available.
                try {
                    prompt.warn(host.getProtocol(),
                            MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                            MessageFormat.format(LocaleFactory.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"),
                                    ProtocolFactory.FTP_TLS.getName()),
                            LocaleFactory.localizedString("Continue", "Credentials"),
                            LocaleFactory.localizedString("Change", "Credentials"),
                            String.format("connection.unsecure.%s", host.getHostname()));
                    // Continue chosen. Login using plain FTP.
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
                    client.execPROT(preferences.getProperty("ftp.tls.datachannel"));
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
                if(client.hasFeature("SITE", "SYMLINK")) {
                    symlink = new FTPSymlinkFeature(this);
                }
            }
            else {
                throw new FTPExceptionMappingService().map(new FTPException(this.getClient().getReplyCode(), this.getClient().getReplyString()));
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
                directory.equals(String.valueOf(Path.DELIMITER)) ? EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory));
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
        if(type == Symlink.class) {
            return (T) symlink;
        }
        if(type == Command.class) {
            return (T) new FTPCommandFeature(this);
        }
        return super.getFeature(type);
    }
}