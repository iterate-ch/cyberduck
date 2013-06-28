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
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.putty.PuTTYKey;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.ConnectionMonitor;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.PacketListener;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import ch.ethz.ssh2.StreamGobbler;
import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMDecryptException;

/**
 * @version $Id$
 */
public class SFTPSession extends Session<Connection> {
    private static final Logger log = Logger.getLogger(SFTPSession.class);

    private Connection connection;

    public SFTPSession(Host h) {
        super(h);
    }

    @Override
    public boolean isSecure() {
        if(super.isSecure()) {
            return connection.isAuthenticationComplete();
        }
        return false;
    }

    private SFTPv3Client client;

    @Override
    public Connection connect(final HostKeyController key) throws BackgroundException {
        try {
            connection = new Connection(HostnameConfiguratorFactory.get(host.getProtocol()).lookup(host.getHostname()), host.getPort(),
                    new PreferencesUseragentProvider().get());
            connection.setTCPNoDelay(true);
            connection.addConnectionMonitor(new ConnectionMonitor() {
                @Override
                public void connectionLost(Throwable reason) {
                    log.warn(String.format("Connection lost:%s", (null == reason) ? "Unknown" : reason.getMessage()));
                    connection.close(null, true);
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
    public void login(final LoginController prompt) throws BackgroundException {
        try {
            if(host.getCredentials().isPublicKeyAuthentication()) {
                if(this.loginUsingPublicKeyAuthentication(prompt, host.getCredentials())) {
                    log.info("Login successful");
                }
            }
            else if(this.loginUsingChallengeResponseAuthentication(prompt, host.getCredentials())) {
                log.info("Login successful");
            }
            else if(this.loginUsingPasswordAuthentication(host.getCredentials())) {
                log.info("Login successful");
            }
            else if(this.getClient().authenticateWithNone(host.getCredentials().getUsername())) {
                log.info("Login successful");
            }
            // Check if authentication is partial
            if(connection.isAuthenticationPartialSuccess()) {
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
                        Locale.localizedString("Provide additional login credentials", "Credentials") + ".", false, false, false);
                if(this.loginUsingChallengeResponseAuthentication(prompt, additional)) {
                    this.message(Locale.localizedString("Login successful", "Credentials"));
                }
                else {
                    prompt.fail(host.getProtocol(), host.getCredentials());
                }
            }
            if(connection.isAuthenticationComplete()) {
                this.message(Locale.localizedString("Starting SFTP subsystem", "Status"));
                try {
                    client = new SFTPv3Client(connection, new PacketListener() {
                        @Override
                        public void read(String packet) {
                            SFTPSession.this.log(false, packet);
                        }

                        @Override
                        public void write(String packet) {
                            SFTPSession.this.log(true, packet);
                        }
                    });
                    this.message(Locale.localizedString("SFTP subsystem ready", "Status"));
                    client.setCharset(this.getEncoding());
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }
            else {
                prompt.fail(host.getProtocol(), host.getCredentials());
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    public SFTPv3Client sftp() {
        return client;
    }

    /**
     * Authenticate with public key
     *
     * @param controller  Login prompt
     * @param credentials Username and password for private key
     * @return True if authentication succeeded
     * @throws IOException Error reading private key
     */
    private boolean loginUsingPublicKeyAuthentication(final LoginController controller, final Credentials credentials)
            throws IOException, LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using public key authentication with credentials %s", credentials));
        }
        if(connection.isAuthMethodAvailable(credentials.getUsername(), "publickey")) {
            if(credentials.isPublicKeyAuthentication()) {
                final Local identity = credentials.getIdentity();
                final CharArrayWriter privatekey = new CharArrayWriter();
                if(PuTTYKey.isPuTTYKeyFile(identity.getInputStream())) {
                    final PuTTYKey putty = new PuTTYKey(identity.getInputStream());
                    if(putty.isEncrypted()) {
                        if(StringUtils.isEmpty(credentials.getPassword())) {
                            controller.prompt(host.getProtocol(), credentials,
                                    Locale.localizedString("Private key password protected", "Credentials"),
                                    Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                            + " (" + identity + ")");
                        }
                    }
                    try {
                        IOUtils.copy(new StringReader(putty.toOpenSSH(credentials.getPassword())), privatekey);
                    }
                    catch(PEMDecryptException e) {
                        this.message(Locale.localizedString("Invalid passphrase", "Credentials"));
                        controller.prompt(host.getProtocol(), credentials,
                                Locale.localizedString("Invalid passphrase", "Credentials"),
                                Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                        + " (" + identity + ")");
                        return this.loginUsingPublicKeyAuthentication(controller, credentials);
                    }
                }
                else {
                    IOUtils.copy(new FileReader(identity.getAbsolute()), privatekey);
                    if(PEMDecoder.isPEMEncrypted(privatekey.toCharArray())) {
                        if(StringUtils.isEmpty(credentials.getPassword())) {
                            controller.prompt(host.getProtocol(), credentials,
                                    Locale.localizedString("Private key password protected", "Credentials"),
                                    Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                            + " (" + identity + ")");
                        }
                    }
                    try {
                        PEMDecoder.decode(privatekey.toCharArray(), credentials.getPassword());
                    }
                    catch(PEMDecryptException e) {
                        this.message(Locale.localizedString("Invalid passphrase", "Credentials"));
                        controller.prompt(host.getProtocol(), credentials,
                                Locale.localizedString("Invalid passphrase", "Credentials"),
                                Locale.localizedString("Enter the passphrase for the private key file", "Credentials")
                                        + " (" + identity + ")");

                        return this.loginUsingPublicKeyAuthentication(controller, credentials);
                    }
                }
                return connection.authenticateWithPublicKey(credentials.getUsername(),
                        privatekey.toCharArray(), credentials.getPassword());
            }
        }
        return false;
    }

    /**
     * Authenticate with plain password.
     *
     * @param credentials Username and password
     * @return True if authentication succeeded
     */
    private boolean loginUsingPasswordAuthentication(final Credentials credentials) throws IOException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using password authentication with credentials %s", credentials));
        }
        if(connection.isAuthMethodAvailable(credentials.getUsername(), "password")) {
            return connection.authenticateWithPassword(credentials.getUsername(), credentials.getPassword());
        }
        return false;
    }

    /**
     * Authenticate using challenge and response method.
     *
     * @param controller  Login prompt
     * @param credentials Username and password
     * @return True if authentication succeeded
     */
    private boolean loginUsingChallengeResponseAuthentication(final LoginController controller, final Credentials credentials) throws IOException {
        log.debug("loginUsingChallengeResponseAuthentication:" + credentials);
        if(connection.isAuthMethodAvailable(credentials.getUsername(), "keyboard-interactive")) {
            return connection.authenticateWithKeyboardInteractive(credentials.getUsername(),
                    /**
                     * The logic that one has to implement if "keyboard-interactive" authentication shall be
                     * supported.
                     */
                    new InteractiveCallback() {
                        private int promptCount = 0;

                        /**
                         * The callback may be invoked several times, depending on how
                         * many questions-sets the server sends
                         */
                        @Override
                        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
                                                         boolean[] echo) throws LoginCanceledException {
                            log.debug("replyToChallenge:" + name);
                            // In its first callback the server prompts for the password
                            if(0 == promptCount) {
                                if(log.isDebugEnabled()) {
                                    log.debug("First callback returning provided credentials");
                                }
                                promptCount++;
                                return new String[]{credentials.getPassword()};
                            }
                            String[] response = new String[numPrompts];
                            for(int i = 0; i < numPrompts; i++) {
                                controller.prompt(host.getProtocol(), credentials,
                                        Locale.localizedString("Provide additional login credentials", "Credentials"), prompt[i], false, false, false);
                                response[i] = credentials.getPassword();
                                promptCount++;
                            }
                            return response;
                        }
                    });
        }
        return false;
    }

    @Override
    public void logout() throws BackgroundException {
        if(client != null) {
            client.close();
            client = null;
        }
        connection.close();
    }

    @Override
    public void interrupt() throws BackgroundException {
        connection.close(null, true);
        client = null;
    }

    @Override
    public Path workdir() throws BackgroundException {
        // "." as referring to the current directory
        final String directory;
        try {
            directory = client.canonicalPath(".");
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return new SFTPPath(this, directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
    }

    @Override
    public void noop() throws BackgroundException {
        try {
            connection.sendIgnorePacket();
        }
        catch(IllegalStateException e) {
            throw new ConnectionCanceledException();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSendCommandSupported() {
        return true;
    }

    @Override
    public boolean isArchiveSupported() {
        return true;
    }

    @Override
    public boolean isUnarchiveSupported() {
        return true;
    }

    @Override
    public void sendCommand(final String command) throws BackgroundException {
        ch.ethz.ssh2.Session sess = null;
        try {
            sess = connection.openSession();

            final BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
            final BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));

            try {
                this.message(command);
                sess.execCommand(command, host.getEncoding());

                // Here is the output from stdout
                while(true) {
                    String line = stdoutReader.readLine();
                    if(null == line) {
                        break;
                    }
                    this.log(false, line);
                }
                // Here is the output from stderr
                StringBuilder error = new StringBuilder();
                while(true) {
                    String line = stderrReader.readLine();
                    if(null == line) {
                        break;
                    }
                    this.log(false, line);
                    // Standard error output contains all status messages, not only errors.
                    if(StringUtils.isNotBlank(error.toString())) {
                        error.append(" ");
                    }
                    error.append(line).append(".");
                }
                if(StringUtils.isNotBlank(error.toString())) {
                    throw new BackgroundException(error.toString(), null);
                }
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
            finally {
                IOUtils.closeQuietly(stdoutReader);
                IOUtils.closeQuietly(stderrReader);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            if(sess != null) {
                sess.close();
            }
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

    @Override
    public boolean isCreateSymlinkSupported() {
        return true;
    }

    @Override
    public boolean isUnixPermissionsSupported() {
        return true;
    }

    /**
     * No resume supported for SCP transfers.
     *
     * @return True if SFTP is the selected transfer protocol for SSH sessions.
     */
    private boolean isTransferResumable() {
        return Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier());
    }
}
