package ch.cyberduck.core.sftp;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.sftp.openssh.OpenSSHHostnameConfigurator;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.EnumSet;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionMonitor;
import ch.ethz.ssh2.PacketListener;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.ServerHostKeyVerifier;

/**
 * @version $Id$
 */
public class SFTPSession extends Session<Connection> {
    private static final Logger log = Logger.getLogger(SFTPSession.class);

    private SFTPv3Client sftp;

    public SFTPSession(Host h) {
        super(h);
    }

    /**
     * @return True if authentication is complete
     */
    @Override
    public boolean isSecured() {
        if(super.isSecured()) {
            return client.isAuthenticationComplete();
        }
        return false;
    }

    @Override
    public Connection connect(final HostKeyCallback key) throws BackgroundException {
        try {
            final Connection connection = new Connection(new OpenSSHHostnameConfigurator().getHostname(host.getHostname()),
                    host.getPort(),
                    new PreferencesUseragentProvider().get());
            connection.setTCPNoDelay(true);
            connection.addConnectionMonitor(new ConnectionMonitor() {
                @Override
                public void connectionLost(Throwable reason) {
                    log.warn(String.format("Connection lost:%s", (null == reason) ? "Unknown" : reason.getMessage()));
                    disconnect();
                }
            });
            final int timeout = this.timeout();
            connection.connect(new ServerHostKeyVerifier() {
                @Override
                public boolean verifyServerHostKey(final String hostname, final int port,
                                                   final String serverHostKeyAlgorithm, final byte[] serverHostKey)
                        throws IOException, ConnectionCanceledException {
                    return key.verify(hostname, port, serverHostKeyAlgorithm, serverHostKey);
                }
            }, timeout, timeout);
            return connection;
        }
        catch(IllegalStateException e) {
            throw new ConnectionCanceledException(e);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final Cache cache) throws BackgroundException {
        try {
            if(host.getCredentials().isAnonymousLogin()) {
                if(new SFTPNoneAuthentication(this).authenticate(host, prompt)) {
                    log.info("Login successful");
                }
                else {
                    throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString("Login {0} with username and password", "Credentials"), host.getHostname()));
                }
            }
            else if(host.getCredentials().isPublicKeyAuthentication()) {
                if(new SFTPPublicKeyAuthentication(this).authenticate(host, prompt)) {
                    log.info("Login successful");
                }
                else {
                    throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString("Login {0} with username and password", "Credentials"), host.getHostname()));
                }
            }
            else if(new SFTPChallengeResponseAuthentication(this).authenticate(host, prompt)) {
                log.info("Login successful");
            }
            else if(new SFTPPasswordAuthentication(this).authenticate(host, prompt)) {
                log.info("Login successful");
            }
            // Check if authentication is partial
            if(!client.isAuthenticationComplete()) {
                if(client.isAuthenticationPartialSuccess()) {
                    final Credentials additional = new HostCredentials(host, host.getCredentials().getUsername(), null, false);
                    prompt.prompt(host.getProtocol(), additional,
                            LocaleFactory.localizedString("Partial authentication success", "Credentials"),
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), new LoginOptions());
                    if(!new SFTPChallengeResponseAuthentication(this).authenticate(host, additional, prompt)) {
                        throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString("Login {0} with username and password", "Credentials"), host.getHostname()));
                    }
                }
                else {
                    throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString("Login {0} with username and password", "Credentials"), host.getHostname()));
                }
            }
            try {
                sftp = new SFTPv3Client(client, new PacketListener() {
                    @Override
                    public void read(final String packet) {
                        SFTPSession.this.log(false, packet);
                    }

                    @Override
                    public void write(final String packet) {
                        SFTPSession.this.log(true, packet);
                    }
                });
                sftp.setCharset(this.getEncoding());
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
        }
        catch(IllegalStateException e) {
            throw new ConnectionCanceledException(e);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    public SFTPv3Client sftp() throws LoginCanceledException {
        if(null == sftp) {
            throw new LoginCanceledException();
        }
        return sftp;
    }

    @Override
    protected void logout() throws BackgroundException {
        if(sftp != null) {
            sftp.close();
            sftp = null;
        }
        client.close();
    }

    @Override
    public void disconnect() {
        if(client != null) {
            client.close(null, true);
        }
        sftp = null;
        super.disconnect();
    }

    @Override
    public Path workdir() throws BackgroundException {
        // "." as referring to the current directory
        final String directory;
        try {
            directory = this.sftp().canonicalPath(".");
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
        return new Path(directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ?
                        EnumSet.of(Path.Type.volume, Path.Type.directory) : EnumSet.of(Path.Type.directory));
    }

    @Override
    public void noop() throws BackgroundException {
        try {
            client.sendIgnorePacket();
        }
        catch(IllegalStateException e) {
            throw new ConnectionCanceledException();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new SFTPListService(this).list(file, listener);
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == Read.class) {
            if(Preferences.instance().getProperty("ssh.transfer").equals(Scheme.scp.name())) {
                return (T) new SCPReadFeature(this);
            }
            return (T) new SFTPReadFeature(this);
        }
        if(type == Write.class) {
            if(Preferences.instance().getProperty("ssh.transfer").equals(Scheme.scp.name())) {
                return (T) new SCPWriteFeature(this);
            }
            return (T) new SFTPWriteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new SFTPDirectoryFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SFTPDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new SFTPMoveFeature(this);
        }
        if(type == UnixPermission.class) {
            return (T) new SFTPUnixPermissionFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new SFTPTimestampFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SFTPTouchFeature(this);
        }
        if(type == Symlink.class) {
            return (T) new SFTPSymlinkFeature(this);
        }
        if(type == Command.class) {
            return (T) new SFTPCommandFeature(this);
        }
        if(type == Compress.class) {
            return (T) new SFTPCompressFeature(this);
        }
        if(type == Find.class) {
            return (T) new SFTPFindFeature(this);
        }
        if(type == ch.cyberduck.core.features.Attributes.class) {
            return (T) new SFTPAttributesFeature(this);
        }
        return super.getFeature(type);
    }
}