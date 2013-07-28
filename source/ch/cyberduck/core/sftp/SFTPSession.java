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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionMonitor;
import ch.ethz.ssh2.PacketListener;
import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.ServerHostKeyVerifier;

/**
 * @version $Id$
 */
public class SFTPSession extends Session<Connection> implements Delete {
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
    public Connection connect(final HostKeyController key) throws BackgroundException {
        try {
            final Connection connection = new Connection(new OpenSSHHostnameConfigurator().lookup(host.getHostname()), host.getPort(),
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
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            if(host.getCredentials().isPublicKeyAuthentication()) {
                if(new SFTPPublicKeyAuthentication(this).authenticate(host, prompt)) {
                    log.info("Login successful");
                }
            }
            else if(new SFTPChallengeResponseAuthentication(this).authenticate(host, prompt)) {
                log.info("Login successful");
            }
            else if(new SFTPPasswordAuthentication(this).authenticate(host, prompt)) {
                log.info("Login successful");
            }
            else if(new SFTPNoneAuthentication(this).authenticate(host, prompt)) {
                log.info("Login successful");
            }
            // Check if authentication is partial
            if(client.isAuthenticationPartialSuccess()) {
                final Credentials additional = new Credentials(host.getCredentials().getUsername(), null, false) {
                    @Override
                    public String getUsernamePlaceholder() {
                        return host.getCredentials().getUsernamePlaceholder();
                    }

                    @Override
                    public String getPasswordPlaceholder() {
                        return getHost().getProtocol().getPasswordPlaceholder();
                    }
                };
                prompt.prompt(host.getProtocol(), additional,
                        LocaleFactory.localizedString("Partial authentication success", "Credentials"),
                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials") + ".", new LoginOptions());
                if(!new SFTPChallengeResponseAuthentication(this).authenticate(host, additional, prompt)) {
                    prompt.prompt(host.getProtocol(), host.getCredentials(),
                            LocaleFactory.localizedString("Login failed", "Credentials"),
                            LocaleFactory.localizedString("Login with username and password", "Credentials"),
                            new LoginOptions(host.getProtocol()));
                }
            }
            if(client.isAuthenticationComplete()) {
                try {
                    sftp = new SFTPv3Client(client, new PacketListener() {
                        @Override
                        public void read(String packet) {
                            SFTPSession.this.log(false, packet);
                        }

                        @Override
                        public void write(String packet) {
                            SFTPSession.this.log(true, packet);
                        }
                    });
                    sftp.setCharset(this.getEncoding());
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }
            else {
                prompt.prompt(host.getProtocol(), host.getCredentials(),
                        LocaleFactory.localizedString("Login failed", "Credentials"),
                        LocaleFactory.localizedString("Login with username and password", "Credentials"),
                        new LoginOptions(host.getProtocol()));
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
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
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return new Path(directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
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
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean exists(final Path path) throws BackgroundException {
        try {
            try {
                return this.sftp().canonicalPath(path.getAbsolute()) != null;
            }
            catch(SFTPException e) {
                throw new SFTPExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return new SFTPListService(this).list(file, listener);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            this.sftp().mkdir(file.getAbsolute(),
                    Integer.parseInt(new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default")).getOctalString(), 8));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void delete(final List<Path> files) throws BackgroundException {
        for(Path file : files) {
            this.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            try {
                if(file.attributes().isFile() || file.attributes().isSymbolicLink()) {
                    this.sftp().rm(file.getAbsolute());
                }
                else if(file.attributes().isDirectory()) {
                    this.sftp().rmdir(file.getAbsolute());
                }
            }
            catch(IOException e) {
                throw new SFTPExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        return this.getFeature(Read.class, new DisabledLoginController()).read(file, status);
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        return this.getFeature(Write.class, new DisabledLoginController()).write(file, status);
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
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
        if(type == Delete.class) {
            return (T) this;
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
        return super.getFeature(type, prompt);
    }
}