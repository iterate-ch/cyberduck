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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import ch.ethz.ssh2.*;

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
    public Connection connect(final HostKeyController key) throws BackgroundException {
        try {
            final Connection connection = new Connection(HostnameConfiguratorFactory.get(host.getProtocol()).lookup(host.getHostname()), host.getPort(),
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
                        Locale.localizedString("Partial authentication success", "Credentials"),
                        Locale.localizedString("Provide additional login credentials", "Credentials") + ".", new LoginOptions());
                if(!new SFTPChallengeResponseAuthentication(this).authenticate(host, additional, prompt)) {
                    prompt.prompt(host.getProtocol(), host.getCredentials(),
                            Locale.localizedString("Login failed", "Credentials"),
                            Locale.localizedString("Login with username and password", "Credentials"),
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
                        Locale.localizedString("Login failed", "Credentials"),
                        Locale.localizedString("Login with username and password", "Credentials"),
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
    public boolean isDownloadResumable() {
        return this.isTransferResumable();
    }

    @Override
    public boolean isUploadResumable() {
        return this.isTransferResumable();
    }

    /**
     * No resume supported for SCP transfers.
     *
     * @return True if SFTP is the selected transfer protocol for SSH sessions.
     */
    private boolean isTransferResumable() {
        return Preferences.instance().getProperty("ssh.transfer").equals(Scheme.sftp.name());
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
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        return new SFTPListService(this).list(file);
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
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            if(this.exists(renamed)) {
                this.delete(renamed, new DisabledLoginController());
            }
            this.sftp().mv(file.getAbsolute(), renamed.getAbsolute());
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public void delete(final Path file, final LoginController prompt) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    file.getName()));

            if(file.attributes().isFile() || file.attributes().isSymbolicLink()) {
                this.sftp().rm(file.getAbsolute());
            }
            else if(file.attributes().isDirectory()) {
                for(Path child : this.list(file)) {
                    this.delete(child, prompt);
                }
                this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        file.getName()));

                this.sftp().rmdir(file.getAbsolute());
            }
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Cannot delete {0}", e, file);
        }
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        InputStream in = null;
        try {
            if(Preferences.instance().getProperty("ssh.transfer").equals(Scheme.sftp.name())) {
                final SFTPv3FileHandle handle = this.sftp().openFileRO(file.getAbsolute());
                in = new SFTPInputStream(handle);
                if(status.isResume()) {
                    log.info(String.format("Skipping %d bytes", status.getCurrent()));
                    final long skipped = in.skip(status.getCurrent());
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                // No parallel requests if the file size is smaller than the buffer.
                this.sftp().setRequestParallelism(
                        (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1
                );
            }
            else if(Preferences.instance().getProperty("ssh.transfer").equals(Scheme.scp.name())) {
                final SCPClient client = new SCPClient(this.getClient());
                client.setCharset(this.getEncoding());
                in = client.get(file.getAbsolute());
            }
            return in;
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Download failed", e, file);
        }
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final String mode = Preferences.instance().getProperty("ssh.transfer");
            if(mode.equals(Scheme.sftp.name())) {
                SFTPv3FileHandle handle;
                if(status.isResume()) {
                    handle = this.sftp().openFile(file.getAbsolute(),
                            SFTPv3Client.SSH_FXF_WRITE | SFTPv3Client.SSH_FXF_APPEND, null);
                }
                else {
                    handle = this.sftp().openFile(file.getAbsolute(),
                            SFTPv3Client.SSH_FXF_CREAT | SFTPv3Client.SSH_FXF_TRUNC | SFTPv3Client.SSH_FXF_WRITE, null);
                }
                final OutputStream out = new SFTPOutputStream(handle);
                if(status.isResume()) {
                    long skipped = ((SFTPOutputStream) out).skip(status.getCurrent());
                    log.info(String.format("Skipping %d bytes", skipped));
                    if(skipped < status.getCurrent()) {
                        throw new IOResumeException(String.format("Skipped %d bytes instead of %d", skipped, status.getCurrent()));
                    }
                }
                // No parallel requests if the file size is smaller than the buffer.
                this.sftp().setRequestParallelism(
                        (int) (status.getLength() / Preferences.instance().getInteger("connection.chunksize")) + 1
                );
                return out;
            }
            else if(mode.equals(Scheme.scp.name())) {
                final SCPClient client = new SCPClient(this.getClient());
                client.setCharset(this.getEncoding());
                return client.put(file.getName(), status.getLength(),
                        file.getParent().getAbsolute(),
                        "0" + file.attributes().getPermission().getOctalString());
            }
            throw new IOException("Unknown transfer mode:" + mode);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Upload failed", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
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