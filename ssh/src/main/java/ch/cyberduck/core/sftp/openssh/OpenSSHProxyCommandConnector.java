package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Factory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Launch an external helper process to tunnel the SSH transport through, similar to the OpenSSH {@code ProxyCommand}
 * directive. The standard input and output streams of the spawned process are handed to
 * {@link net.schmizz.sshj.SocketClient#connectVia(InputStream, OutputStream)}.
 */
public class OpenSSHProxyCommandConnector implements Closeable {
    private static final Logger log = LogManager.getLogger(OpenSSHProxyCommandConnector.class);

    private Process process;

    /**
     * Substitute the tokens understood by OpenSSH {@code ProxyCommand} that Cyberduck is able to resolve.
     *
     * @param command  Command with unresolved tokens as found in the configuration file
     * @param hostname Target hostname (%h)
     * @param port     Target port (%p)
     * @param username Login name for the target host (%r)
     * @return Command line to pass to the user's shell
     */
    protected static String substitute(final String command, final String hostname, final int port, final String username) {
        String substituted = command;
        substituted = StringUtils.replace(substituted, "%h", hostname);
        substituted = StringUtils.replace(substituted, "%p", String.valueOf(port));
        if(StringUtils.isNotBlank(username)) {
            substituted = StringUtils.replace(substituted, "%r", username);
        }
        // Literal percent sign
        substituted = StringUtils.replace(substituted, "%%", "%");
        return substituted;
    }

    /**
     * Start the proxy command and return its input stream to read server output from.
     *
     * @param command  Command with unresolved tokens as found in the configuration file
     * @param hostname Target hostname (%h)
     * @param port     Target port (%p)
     * @param username Login name for the target host (%r)
     */
    public OpenSSHProxyCommandConnector connect(final String command, final String hostname, final int port, final String username) throws IOException {
        final String substituted = substitute(command, hostname, port, username);
        final List<String> args = new ArrayList<>();
        switch(Factory.Platform.getDefault()) {
            case windows:
                args.add("cmd");
                args.add("/c");
                break;
            default:
                args.add("sh");
                args.add("-c");
                break;
        }
        args.add(substituted);
        log.info("Start proxy command {}", substituted);
        final ProcessBuilder builder = new ProcessBuilder(args);
        // Forward any diagnostics printed by the helper to the user's terminal as OpenSSH does
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        this.process = builder.start();
        return this;
    }

    public InputStream getInputStream() {
        return process.getInputStream();
    }

    public OutputStream getOutputStream() {
        return process.getOutputStream();
    }

    /**
     * Terminate the helper process. No-op if it already exited or was never started.
     */
    @Override
    public void close() {
        if(null == process) {
            return;
        }
        if(process.isAlive()) {
            log.debug("Destroy proxy command process {}", process);
            process.destroy();
        }
        process = null;
    }
}
