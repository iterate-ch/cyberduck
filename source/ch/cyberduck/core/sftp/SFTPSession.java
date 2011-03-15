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
import ch.cyberduck.core.i18n.Locale;
import ch.ethz.ssh2.*;
import ch.ethz.ssh2.channel.ChannelClosedException;
import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMDecryptException;
import ch.ethz.ssh2.sftp.PacketListener;
import ch.ethz.ssh2.sftp.SFTPv3Client;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kohsuke.putty.PuTTYKey;
import org.spearce.jgit.transport.OpenSshConfig;

import java.io.*;

/**
 * @version $Id$
 */
public class SFTPSession extends Session {
    private static Logger log = Logger.getLogger(SFTPSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new SFTPSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    private Connection SSH;

    public SFTPSession(Host h) {
        super(h);
    }

    @Override
    protected Connection getClient() throws ConnectionCanceledException {
        if(null == SSH) {
            throw new ConnectionCanceledException();
        }
        return SSH;
    }

    @Override
    public boolean isSecure() {
        if(super.isSecure()) {
            try {
                return this.getClient().isAuthenticationComplete();
            }
            catch(ConnectionCanceledException e) {
                return false;
            }
        }
        return false;
    }

    private SFTPv3Client SFTP;

    /**
     * If never called before opens a new SFTP subsystem. If called before, the cached
     * SFTP subsystem is returned. May not be used concurrently.
     *
     * @throws IOException
     */
    protected SFTPv3Client sftp() throws IOException {
        if(null == SFTP) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            if(!this.getClient().isAuthenticationComplete()) {
                throw new LoginCanceledException();
            }
            this.message(Locale.localizedString("Starting SFTP subsystem", "Status"));
            SFTP = new SFTPv3Client(this.getClient(), new PacketListener() {
                public void read(String packet) {
                    SFTPSession.this.log(false, packet);
                }

                public void write(String packet) {
                    SFTPSession.this.log(true, packet);
                }
            });
            this.message(Locale.localizedString("SFTP subsystem ready", "Status"));
            SFTP.setCharset(this.getEncoding());
        }
        return SFTP;
    }

    /**
     * Opens a new, dedicated SCP channel for this SSH session
     *
     * @throws IOException
     */
    protected SCPClient openScp() throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(!this.getClient().isAuthenticationComplete()) {
            throw new LoginCanceledException();
        }
        final SCPClient client = new SCPClient(this.getClient());
        client.setCharset(this.getEncoding());
        return client;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        SSH = new Connection(this.getHostname(), host.getPort(), this.getUserAgent());
        SSH.addConnectionMonitor(new ConnectionMonitor() {
            public void connectionLost(Throwable reason) {
                log.warn("Connection lost:" + ((null == reason) ? "Unknown" : reason.getMessage()));
                interrupt();
            }
        });

        final int timeout = this.timeout();
        this.getClient().connect(HostKeyControllerFactory.instance(this), timeout, timeout);
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.login();
        if(!this.getClient().isAuthenticationComplete()) {
            throw new LoginCanceledException();
        }
        this.fireConnectionDidOpenEvent();
    }

    /**
     * @return Resolves any alias given in ~/.ssh/config
     */
    private String getHostname() {
        final String alias = OpenSshConfig.create().lookup(host.getHostname()).getHostName();
        if(host.getHostname().equals(alias)) {
            return host.getHostname(true);
        }
        if(log.isInfoEnabled()) {
            log.info("Using hostname alias from ~/.ssh/config:" + alias);
        }
        return alias;
    }

    @Override
    protected Resolver getResolver() {
        return new Resolver(this.getHostname());
    }

    @Override
    protected void login(LoginController controller, final Credentials credentials) throws IOException {
        if(this.getClient().isAuthenticationComplete()) {
            this.message(Locale.localizedString("Login successful", "Credentials"));
            // Already authenticated
            return;
        }
        if(credentials.isPublicKeyAuthentication()) {
            if(this.loginUsingPublicKeyAuthentication(controller, credentials)) {
                this.message(Locale.localizedString("Login successful", "Credentials"));
                return;
            }
        }
        else if(this.loginUsingKBIAuthentication(controller, credentials)) {
            this.message(Locale.localizedString("Login successful", "Credentials"));
            return;
        }
        else if(this.loginUsingPasswordAuthentication(controller, credentials)) {
            this.message(Locale.localizedString("Login successful", "Credentials"));
            return;
        }
        if(this.getClient().isAuthenticationPartialSuccess()) {
            Credentials additional = new Credentials(credentials.getUsername(), null, false) {
                @Override
                public String getUsernamePlaceholder() {
                    return credentials.getUsernamePlaceholder();
                }

                @Override
                public String getPasswordPlaceholder() {
                    return Locale.localizedString("One-time passcode", "Credentials");
                }
            };
            controller.prompt(host.getProtocol(), additional,
                    Locale.localizedString("Partial authentication success", "Credentials"),
                    Locale.localizedString("Provide additional login credentials", "Credentials") + ".", false, false, false);
            if(this.loginUsingKBIAuthentication(controller, additional)) {
                this.message(Locale.localizedString("Login successful", "Credentials"));
                return;
            }
        }
        this.message(Locale.localizedString("Login failed", "Credentials"));
        controller.fail(host.getProtocol(), credentials);
        this.login();
    }

    /**
     * Authenticate with public key
     *
     * @param credentials
     * @return True if authentication succeeded
     * @throws IOException
     */
    private boolean loginUsingPublicKeyAuthentication(LoginController controller, final Credentials credentials)
            throws IOException {

        log.debug("loginUsingPublicKeyAuthentication:" + credentials);
        if(this.getClient().isAuthMethodAvailable(credentials.getUsername(), "publickey")) {
            if(credentials.isPublicKeyAuthentication()) {
                final Local identity = credentials.getIdentity();
                CharArrayWriter privatekey = new CharArrayWriter();
                if(PuTTYKey.isPuTTYKeyFile(identity.getInputStream())) {
                    PuTTYKey putty = new PuTTYKey(identity.getInputStream());
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
                return this.getClient().authenticateWithPublicKey(credentials.getUsername(),
                        privatekey.toCharArray(), credentials.getPassword());
            }
        }
        return false;
    }

    /**
     * Authenticate with plain password.
     *
     * @param credentials
     * @return True if authentication succeeded
     * @throws IOException
     */
    private boolean loginUsingPasswordAuthentication(LoginController controller, final Credentials credentials) throws IOException {
        log.debug("loginUsingPasswordAuthentication:" + credentials);
        if(this.getClient().isAuthMethodAvailable(credentials.getUsername(), "password")) {
            return this.getClient().authenticateWithPassword(credentials.getUsername(), credentials.getPassword());
        }
        return false;
    }

    /**
     * Authenticate using challenge and response method.
     *
     * @param credentials
     * @return True if authentication succeeded
     * @throws IOException
     */
    private boolean loginUsingKBIAuthentication(final LoginController controller, final Credentials credentials) throws IOException {
        log.debug("loginUsingKBIAuthentication" +
                "make:" + credentials);
        if(this.getClient().isAuthMethodAvailable(credentials.getUsername(), "keyboard-interactive")) {
            return this.getClient().authenticateWithKeyboardInteractive(credentials.getUsername(),
                    /**
                     * The logic that one has to implement if "keyboard-interactive" autentication shall be
                     * supported.
                     */
                    new InteractiveCallback() {
                        private int promptCount = 0;

                        /**
                         * The callback may be invoked several times, depending on how
                         * many questions-sets the server sends
                         */
                        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
                                                         boolean[] echo) throws IOException {
                            log.debug("replyToChallenge:" + name);
                            // In its first callback the server prompts for the password
                            if(0 == promptCount) {
                                log.debug("First callback returning provided credentials");
                                promptCount++;
                                return new String[]{host.getCredentials().getPassword()};
                            }
                            String[] response = new String[numPrompts];
                            for(int i = 0; i < numPrompts; i++) {
                                Credentials credentials = new Credentials(host.getCredentials().getUsername(), null, false) {
                                    @Override
                                    public String getUsernamePlaceholder() {
                                        return host.getProtocol().getUsernamePlaceholder();
                                    }

                                    @Override
                                    public String getPasswordPlaceholder() {
                                        return Locale.localizedString("One-time passcode", "Credentials");
                                    }
                                };
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
    public void close() {
        try {
            this.fireConnectionWillCloseEvent();
            if(SFTP != null) {
                SFTP.close();
            }
            this.getClient().close();
        }
        catch(ConnectionCanceledException e) {
            log.warn(e.getMessage());
        }
        finally {
            SFTP = null;
            SSH = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public void interrupt() {
        log.debug("interrupt");
        try {
            this.fireConnectionWillCloseEvent();
            this.getClient().close(null, true);
        }
        catch(ConnectionCanceledException e) {
            log.warn(e.getMessage());
        }
        finally {
            SFTP = null;
            SSH = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public void check() throws IOException {
        this.check(true);
    }

    /**
     * @param sftp
     * @throws IOException
     */
    private void check(final boolean sftp) throws IOException {
        try {
            super.check();
        }
        catch(ChannelClosedException e) {
            log.warn(e.getMessage());
            this.interrupt();
            this.connect();
        }
        if(sftp) {
            if(!this.sftp().isConnected()) {
                this.interrupt();
                this.connect();
            }
            else {
                try {
                    this.sftp().canonicalPath(".");
                }
                catch(IOException e) {
                    log.warn(e.getMessage());
                    this.interrupt();
                    this.connect();
                }
            }
        }
    }

    @Override
    public Path workdir() throws ConnectionCanceledException {
        if(null == workdir) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            try {
                // "." as referring to the current directory
                workdir = PathFactory.createPath(this, this.sftp().canonicalPath("."), Path.DIRECTORY_TYPE);
            }
            catch(IOException e) {
                log.warn(e.getMessage());
                throw new ConnectionCanceledException(e.getMessage());
            }
        }
        return workdir;
    }

    @Override
    protected void noop() throws IOException {
        if(this.isConnected()) {
            try {
                this.getClient().sendIgnorePacket();
            }
            catch(IllegalStateException e) {
                throw new ConnectionCanceledException();
            }
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
    public void sendCommand(String command) throws IOException {
        final ch.ethz.ssh2.Session sess = this.getClient().openSession();
        try {
            this.message(command);

            sess.execCommand(command, host.getEncoding());

            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));

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
                this.error(error.toString(), null);
            }
        }
        finally {
            sess.close();
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

    /**
     * No resume supported for SCP transfers.
     *
     * @return True if SFTP is the selected transfer protocol for SSH sessions.
     */
    private boolean isTransferResumable() {
        return Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.getIdentifier());
    }
}
