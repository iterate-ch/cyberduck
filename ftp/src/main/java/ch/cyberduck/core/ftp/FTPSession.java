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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CustomOriginCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.idna.PunycodeConverter;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.TimeZone;

public class FTPSession extends SSLSession<FTPClient> {
    private static final Logger log = Logger.getLogger(FTPSession.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private Timestamp timestamp;

    private UnixPermission permission;

    private Symlink symlink;

    private FTPListService listService;

    private Case casesensitivity = Case.sensitive;

    private final SocketFactory socketFactory;

    public FTPSession(final Host h) {
        this(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public FTPSession(final Host h, final SocketFactory socketFactory) {
        this(h, new DisabledX509TrustManager(), new DefaultX509KeyManager(), socketFactory);
    }

    public FTPSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        this(h, trust, key, new ProxySocketFactory(h.getProtocol(), new DefaultTrustManagerHostnameCallback(h)));
    }

    public FTPSession(final Host h, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        this(h, trust, key, new ProxySocketFactory(h.getProtocol(), new DefaultTrustManagerHostnameCallback(h), proxy));
    }

    public FTPSession(final Host h, final X509TrustManager trust, final X509KeyManager key, final SocketFactory socketFactory) {
        super(h, trust, key);
        this.socketFactory = socketFactory;
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
        finally {
            super.logout();
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
    public void interrupt() throws BackgroundException {
        if(host.getProtocol().isSecure()) {
            // The client and the server must share knowledge that the connection is ending in order to avoid a truncation attack.
            // Either party may initiate the exchange of closing messages.
            log.warn(String.format("Skip disconnect for %s connection to workaround hang in closing socket", host.getProtocol()));
            super.disconnect();
        }
        else {
            super.interrupt();
        }
    }

    protected void configure(final FTPClient client) throws IOException {
        client.setProtocol(host.getProtocol());
        client.setSocketFactory(socketFactory);
        client.setControlEncoding(host.getEncoding());
        final int timeout = preferences.getInteger("connection.timeout.seconds") * 1000;
        client.setConnectTimeout(timeout);
        client.setDefaultTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultPort(host.getProtocol().getDefaultPort());
        client.setParserFactory(new FTPParserFactory());
        client.setRemoteVerificationEnabled(preferences.getBoolean("ftp.datachannel.verify"));
        final int buffer = preferences.getInteger("ftp.socket.buffer");
        client.setBufferSize(buffer);

        if(preferences.getInteger("connection.buffer.receive") > 0) {
            client.setReceiveBufferSize(preferences.getInteger("connection.buffer.receive"));
        }
        if(preferences.getInteger("connection.buffer.send") > 0) {
            client.setSendBufferSize(preferences.getInteger("connection.buffer.send"));
        }
        if(preferences.getInteger("connection.buffer.receive") > 0) {
            client.setReceieveDataSocketBufferSize(preferences.getInteger("connection.buffer.receive"));
        }
        if(preferences.getInteger("connection.buffer.send") > 0) {
            client.setSendDataSocketBufferSize(preferences.getInteger("connection.buffer.send"));
        }
        client.setStrictMultilineParsing(preferences.getBoolean("ftp.parser.multiline.strict"));
    }

    @Override
    public FTPClient connect(final HostKeyCallback callback) throws BackgroundException {
        try {
            final CustomTrustSSLProtocolSocketFactory f
                    = new CustomTrustSSLProtocolSocketFactory(trust, key);

            final LoggingProtocolCommandListener listener = new LoggingProtocolCommandListener(this);
            final FTPClient client = new FTPClient(host.getProtocol(), f, f.getSSLContext()) {
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
            // Default to PASV
            return FTPConnectMode.passive;
        }
        return this.host.getFTPConnectMode();

    }

    @Override
    public Case getCase() {
        return casesensitivity;
    }

    @Override
    public boolean alert(final ConnectionCallback callback) throws BackgroundException {
        if(super.alert(callback)) {
            try {
                if(client.hasFeature("AUTH", "TLS")
                        && client.hasFeature("PBSZ")
                        && client.hasFeature("PROT")) {
                    // Propose protocol change if AUTH TLS is available.
                    try {
                        callback.warn(host.getProtocol(),
                                MessageFormat.format(LocaleFactory.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                                MessageFormat.format("{0} {1}.", MessageFormat.format(LocaleFactory.localizedString("The server supports encrypted connections. Do you want to switch to {0}?", "Credentials"),
                                        new FTPTLSProtocol().getName()), LocaleFactory.localizedString("Please contact your web hosting service provider for assistance", "Support")),
                                LocaleFactory.localizedString("Continue", "Credentials"),
                                LocaleFactory.localizedString("Change", "Credentials"),
                                String.format("connection.unsecure.%s", host.getHostname()));
                        // Continue chosen. Login using plain FTP.
                    }
                    catch(LoginCanceledException e) {
                        // Protocol switch
                        host.setProtocol(new FTPTLSProtocol());
                        // Reconfigure client for TLS
                        this.configure(client);
                        client.execAUTH();
                        client.sslNegotiation();
                    }
                }
                else {
                    // Only alert if no option to switch to TLS later is possible
                    return true;
                }
            }
            catch(IOException e) {
                throw new FTPExceptionMappingService().map(e);
            }
        }
        return false;
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        try {
            if(client.login(host.getCredentials().getUsername(), host.getCredentials().getPassword())) {
                if(host.getProtocol().isSecure()) {
                    client.execPBSZ(0);
                    // Negotiate data connection security
                    client.execPROT(preferences.getProperty("ftp.tls.datachannel"));
                }
                if("UTF-8".equals(host.getEncoding())) {
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
                    if(system.toUpperCase(Locale.ROOT).contains(FTPClientConfig.SYST_NT)) {
                        casesensitivity = Case.insensitive;
                    }
                }
                catch(IOException e) {
                    log.warn(String.format("SYST command failed %s", e.getMessage()));
                }
                listService = new FTPListService(this, keychain, prompt, system, zone);
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
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return listService.list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
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
        if(type == DistributionConfiguration.class) {
            return (T) new CustomOriginCloudFrontDistributionConfiguration(host, this);
        }
        if(type == Home.class) {
            return (T) new FTPWorkdirService(this);
        }
        return super.getFeature(type);
    }
}