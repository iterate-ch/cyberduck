package ch.cyberduck.core.sftp.auth;

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

import ch.cyberduck.core.Local;

import ch.cyberduck.core.LocalFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exports the ticket granting ticket from the default credentials cache of MIT Kerberos for Windows into a
 * temporary file-based credentials cache. The default cache of MIT Kerberos for Windows (<code>API:</code>) is held
 * in memory by the credentials cache server and cannot be read by the Kerberos implementation of Java, which can
 * only read file-based caches.
 */
public final class MitKerberosTicketCache {
    private static final Logger log = LogManager.getLogger(MitKerberosTicketCache.class);

    private static final Pattern DEFAULT_PRINCIPAL = Pattern.compile("^\\s*Default principal:\\s*(\\S+)\\s*$", Pattern.MULTILINE);

    private static final int FCC_VERSION_4 = 0x0504;
    private static final int FCC_TAG_DELTATIME = 1;
    private static final int NT_PRINCIPAL = 1;
    private static final long TIMEOUT_SECONDS = 15L;

    private final Local directory;

    /**
     * @param directory Location of klist and kcpytkt executables, such as <code>C:\Program Files\MIT\Kerberos\bin</code>
     */
    public MitKerberosTicketCache(final Local directory) {
        this.directory = directory;
    }

    /**
     * @param configured Location configured by the user or empty to look in the default installation folder
     * @return Directory with the MIT Kerberos executables or null when not found
     */
    public static Local locate(final String configured) {
        if(StringUtils.isNotBlank(configured)) {
            final Local directory = LocalFactory.get(configured);
            return LocalFactory.get(directory, "klist.exe").exists() ? directory : null;
        }
        for(String variable : new String[]{"ProgramW6432", "ProgramFiles", "ProgramFiles(x86)"}) {
            final String root = System.getenv(variable);
            if(StringUtils.isBlank(root)) {
                continue;
            }
            final Local directory = LocalFactory.get(root, "MIT\\Kerberos\\bin");
            if(LocalFactory.get(directory, "klist.exe").exists()) {
                return directory;
            }
        }
        return null;
    }

    /**
     * @param output Output of klist for the default credentials cache
     * @return Default principal such as <code>user@REALM</code> or null when no credentials are cached
     */
    static String parseDefaultPrincipal(final String output) {
        final Matcher matcher = DEFAULT_PRINCIPAL.matcher(output);
        if(matcher.find()) {
            final String principal = matcher.group(1);
            return principal.contains("@") ? principal : null;
        }
        return null;
    }

    /**
     * Serialize a credentials cache in the file format version 4 without any credentials but the default principal
     *
     * @param principal Default principal such as <code>user@REALM</code>
     */
    static byte[] emptyCache(final String principal) throws IOException {
        final int at = principal.lastIndexOf('@');
        final String realm = principal.substring(at + 1);
        final String[] components = principal.substring(0, at).split("/");
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try(DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeShort(FCC_VERSION_4);
            // Header length followed by a single tag with the offset to the KDC time of zero
            out.writeShort(12);
            out.writeShort(FCC_TAG_DELTATIME);
            out.writeShort(8);
            out.writeInt(0);
            out.writeInt(0);
            out.writeInt(NT_PRINCIPAL);
            out.writeInt(components.length);
            final byte[] realmBytes = realm.getBytes(StandardCharsets.UTF_8);
            out.writeInt(realmBytes.length);
            out.write(realmBytes);
            for(String component : components) {
                final byte[] componentBytes = component.getBytes(StandardCharsets.UTF_8);
                out.writeInt(componentBytes.length);
                out.write(componentBytes);
            }
        }
        return bytes.toByteArray();
    }

    /**
     * Copy the ticket granting ticket from the default credentials cache of MIT Kerberos
     *
     * @return Temporary credentials cache file to be deleted by the caller
     * @throws IOException When no ticket is available or the executables of MIT Kerberos failed
     */
    public File export() throws IOException {
        final String principal = parseDefaultPrincipal(this.execute("klist.exe"));
        if(null == principal) {
            throw new IOException("No ticket in the default MIT Kerberos credentials cache");
        }
        final String realm = principal.substring(principal.lastIndexOf('@') + 1);
        final File cache = File.createTempFile("cyberduck-krb5cc-", null);
        try {
            Files.write(cache.toPath(), emptyCache(principal));
            this.execute("kcpytkt.exe", String.format("FILE:%s", cache.getAbsolutePath()),
                    String.format("krbtgt/%s@%s", realm, realm));
            log.debug("Exported MIT Kerberos ticket for {} to {}", principal, cache);
            return cache;
        }
        catch(IOException | RuntimeException e) {
            if(!cache.delete()) {
                log.warn("Failure deleting temporary Kerberos credentials cache {}", cache);
            }
            throw e;
        }
    }

    private String execute(final String executable, final String... arguments) throws IOException {
        final String[] command = new String[arguments.length + 1];
        command[0] = LocalFactory.get(directory, executable).getAbsolute();
        System.arraycopy(arguments, 0, command, 1, arguments.length);
        final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            process.getOutputStream().close();
            // Drain output concurrently so the timeout applies even if the process hangs without closing its output
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final Thread reader = new Thread(() -> {
                try(InputStream in = process.getInputStream()) {
                    final byte[] chunk = new byte[1024];
                    int read;
                    while((read = in.read(chunk)) != -1) {
                        buffer.write(chunk, 0, read);
                    }
                }
                catch(IOException e) {
                    log.warn("Failure reading output of {}: {}", executable, e.getMessage());
                }
            }, String.format("%s-output", executable));
            reader.setDaemon(true);
            reader.start();
            if(!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new IOException(String.format("Timeout running %s", executable));
            }
            reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            // Console output uses the native code page
            final String output = buffer.toString(Charset.defaultCharset().name());
            if(process.exitValue() != 0) {
                throw new IOException(String.format("Failure running %s with exit code %d: %s",
                        executable, process.exitValue(), output.trim()));
            }
            return output;
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(String.format("Interrupted running %s", executable), e);
        }
        finally {
            process.destroyForcibly();
        }
    }
}
