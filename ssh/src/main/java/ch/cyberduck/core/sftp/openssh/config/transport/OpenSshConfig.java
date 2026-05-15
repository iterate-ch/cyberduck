/*
 * Copyright (C) 2008, Google Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.cyberduck.core.sftp.openssh.config.transport;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.sftp.openssh.config.errors.InvalidPatternException;
import ch.cyberduck.core.sftp.openssh.config.fnmatch.FileNameMatcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple configuration parser for the OpenSSH ~/.ssh/config file.
 */
public class OpenSshConfig {
    private static final Logger log = LogManager.getLogger(OpenSshConfig.class);

    /**
     * The .ssh/config file we read and monitor for updates.
     */
    private final Local configuration;

    /**
     * Cached entries read out of the configuration file.
     */
    private Map<String, Host> hosts
            = Collections.emptyMap();

    /**
     * Cached Match host blocks read out of the configuration file.
     */
    private List<MatchBlock> matchBlocks
            = Collections.emptyList();

    /**
     * Obtain the user's configuration data.
     * <p/>
     * The configuration file is always returned to the caller, even if no file exists in the user's home directory at
     * the time the call was made. Lookup requests are cached and are automatically updated if the user modifies the
     * configuration file since the last time it was cached.
     */
    public OpenSshConfig(final Local configuration) {
        this.configuration = configuration;
        this.refresh();
    }

    /**
     * Locate the configuration for a specific host request.
     *
     * @param hostName the name the user has supplied to the SSH tool. This may be a real host name, or it may just be a
     *                 "Host" block in the configuration file.
     * @return r configuration for the requested name. Never null.
     */
    public Host lookup(final String hostName) {
        Host h = hosts.get(hostName);
        if(h == null) {
            h = new Host();
        }
        if(h.patternsApplied) {
            return h;
        }

        for(final Map.Entry<String, Host> e : hosts.entrySet()) {
            if(!isHostPattern(e.getKey())) {
                continue;
            }
            if(!isHostMatch(e.getKey(), hostName)) {
                continue;
            }
            log.debug("Found host match in SSH config:{}", e.getValue());
            h.copyFrom(e.getValue());
        }
        for(final MatchBlock mb : matchBlocks) {
            if(isMatchHostApplicable(mb.patterns, hostName)) {
                log.debug("Found match block applicable for {} in SSH config: {}", hostName, mb.host);
                h.copyFrom(mb.host);
            }
        }
        if(h.port == 0) {
            h.port = -1;
        }
        h.patternsApplied = true;
        return h;
    }

    public Map<String, Host> refresh() {
        try {
            final List<MatchBlock> newMatchBlocks = new ArrayList<>();
            try(final InputStream in = configuration.getInputStream()) {
                hosts = this.parse(in, configuration.getParent(), new HashSet<>(), newMatchBlocks);
            }
            matchBlocks = newMatchBlocks;
        }
        catch(AccessDeniedException | IOException none) {
            log.warn("Failure reading {}", configuration);
            hosts = Collections.emptyMap();
            matchBlocks = Collections.emptyList();
        }
        return hosts;
    }

    /**
     *
     * @param in          Configuration file input stream
     * @param directory   Directory containing the configuration file.
     * @param seen        Previously read configuration files.
     * @param matchBlocks Accumulator for Match host blocks found during parsing.
     */
    private Map<String, Host> parse(final InputStream in, final Local directory, final Set<Local> seen, final List<MatchBlock> matchBlocks) throws IOException {
        final Map<String, Host> m = new LinkedHashMap<>();
        final BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        final List<Host> current = new ArrayList<>(4);
        String line;

        while((line = br.readLine()) != null) {
            line = line.trim();
            if(line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            final String[] parts = line.split("[ \t]*[= \t]", 2);
            if(parts.length != 2) {
                continue;
            }
            final String keyword = parts[0].trim();
            final String argValue = parts[1].trim();
            if("Host".equalsIgnoreCase(keyword)) {
                current.clear();
                for(final String pattern : argValue.split("[ \t]")) {
                    final String name = dequote(pattern);
                    Host c = m.computeIfAbsent(name, k -> new Host());
                    current.add(c);
                }
                continue;
            }
            if("Match".equalsIgnoreCase(keyword)) {
                current.clear();
                // Only the "host" criteria keyword is supported; skip any other Match block
                final String[] tokens = argValue.split("[ \t]+", 2);
                if(tokens.length == 2 && "host".equalsIgnoreCase(tokens[0])) {
                    final List<String> patterns = new ArrayList<>();
                    for(final String p : tokens[1].split(",")) {
                        final String trimmed = dequote(p.trim());
                        if(!trimmed.isEmpty()) {
                            patterns.add(trimmed);
                        }
                    }
                    if(!patterns.isEmpty()) {
                        final Host matchHost = new Host();
                        matchBlocks.add(new MatchBlock(patterns, matchHost));
                        current.add(matchHost);
                    }
                }
                continue;
            }
            if("Include".equalsIgnoreCase(keyword)) {
                for(final String pattern : argValue.split("[ \t]")) {
                    for(final Local included : resolve(directory, dequote(pattern))) {
                        if(!seen.add(included)) {
                            log.debug("Skipping already-included SSH config file {}", included);
                            continue;
                        }
                        try {
                            try(final InputStream i = included.getInputStream()) {
                                final Map<String, Host> sub = this.parse(i, included.getParent(), seen, matchBlocks);
                                for(final Map.Entry<String, Host> e : sub.entrySet()) {
                                    m.computeIfAbsent(e.getKey(), k -> e.getValue());
                                }
                            }
                        }
                        catch(AccessDeniedException e) {
                            log.warn("Failure reading included SSH config {}", included);
                            // Ignore and skip
                        }
                    }
                }
                continue;
            }
            if(current.isEmpty()) {
                // We received an option outside a Host block. We don't know who this should match against, so skip.
                continue;
            }
            if("HostName".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.hostName == null) {
                        c.hostName = dequote(argValue);
                    }
                }
            }
            else if("ProxyJump".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.proxyJump == null) {
                        c.proxyJump = none(dequote(argValue));
                    }
                }
            }
            else if("User".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.user == null) {
                        c.user = dequote(argValue);
                    }
                }
            }
            else if("Port".equalsIgnoreCase(keyword)) {
                try {
                    final int port = Integer.parseInt(dequote(argValue));
                    for(final Host c : current) {
                        if(c.port == 0) {
                            c.port = port;
                        }
                    }
                }
                catch(NumberFormatException nfe) {
                    // Bad port number. Don't set it.
                }
            }
            else if("IdentityFile".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.identityFile == null) {
                        c.identityFile = none(dequote(argValue));
                    }
                }
            }
            else if("IdentityAgent".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.identityAgent == null) {
                        c.identityAgent = none(dequote(argValue));
                    }
                }
            }
            else if("PreferredAuthentications".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.preferredAuthentications == null) {
                        c.preferredAuthentications = none(nows(dequote(argValue)));
                    }
                }
            }
            else if("IdentitiesOnly".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.identitiesOnly == null) {
                        c.identitiesOnly = yesno(dequote(argValue));
                    }
                }
            }
            else if("BatchMode".equalsIgnoreCase(keyword)) {
                for(final Host c : current) {
                    if(c.batchMode == null) {
                        c.batchMode = yesno(dequote(argValue));
                    }
                }
            }
        }
        return m;
    }

    /**
     * Resolve include patterns relative to the given directory
     */
    private static List<Local> resolve(final Local directory, final String pattern) {
        final List<Local> result = new ArrayList<>();
        final Local parent;
        if(FilenameUtils.getPrefixLength(pattern) != 0) {
            parent = LocalFactory.get(FilenameUtils.getFullPathNoEndSeparator(pattern));
        }
        else {
            parent = directory;
        }
        // Include accepts the tokens %%, %C, %d, %h, %i, %j, %k, %L, %l, %n, %p, %r, and %u.
        if(StringUtils.containsAny(pattern, '*')) {
            // Each pathname may contain glob(7) wildcards
            if(parent.isDirectory()) {
                log.debug("Resolve files in {} matching {}", parent, PathNormalizer.name(pattern));
                try {
                    for(Local l : directory.list(new NullFilter<String>() {
                        @Override
                        public boolean accept(final String file) {
                            return FilenameUtils.wildcardMatch(file, PathNormalizer.name(pattern));
                        }
                    })) {
                        result.add(l);
                    }
                }
                catch(AccessDeniedException e) {
                    log.warn("Failure reading directory {}", parent);
                }
            }
        }
        else {
            result.add(LocalFactory.get(parent, PathNormalizer.name(pattern)));
        }
        // Wildcards will be expanded and processed in lexical order
        result.sort(Comparator.comparing(Local::getAbsolute));
        return result;
    }

    /**
     * Evaluates whether a {@code Match host} block applies to the given hostname.
     * <p>
     * A block applies when at least one positive pattern matches (or all patterns are negated) and no negated pattern
     * matches. Negated patterns are prefixed with {@code !}.
     */
    private static boolean isMatchHostApplicable(final List<String> patterns, final String hostName) {
        boolean hasPositive = false;
        boolean anyPositiveMatch = false;
        for(final String pattern : patterns) {
            if(pattern.startsWith("!")) {
                if(isHostMatch(pattern.substring(1), hostName)) {
                    return false;
                }
            }
            else {
                hasPositive = true;
                if(isHostMatch(pattern, hostName)) {
                    anyPositiveMatch = true;
                }
            }
        }
        return !hasPositive || anyPositiveMatch;
    }

    private static boolean isHostPattern(final String s) {
        return s.indexOf('*') >= 0 || s.indexOf('?') >= 0;
    }

    private static boolean isHostMatch(final String pattern, final String name) {
        final FileNameMatcher fn;
        try {
            fn = new FileNameMatcher(pattern, null);
        }
        catch(InvalidPatternException e) {
            return false;
        }
        fn.append(name);
        return fn.isMatch();
    }

    private static String dequote(final String value) {
        if(value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static String nows(final String value) {
        final StringBuilder b = new StringBuilder();
        for(int i = 0; i < value.length(); i++) {
            if(!Character.isSpaceChar(value.charAt(i))) {
                b.append(value.charAt(i));
            }
        }
        return b.toString();
    }

    private static Boolean yesno(final String value) {
        if("yes".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static String none(final String value) {
        if("none".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

    /**
     * Configuration of one "Host" block in the configuration file.
     * <p/>
     * If returned from {@link OpenSshConfig#lookup(String)} some or all of the properties may not be populated. The
     * properties which are not populated should be defaulted by the caller.
     * <p/>
     * When returned from {@link OpenSshConfig#lookup(String)} any wildcard entries which appear later in the
     * configuration file will have been already merged into this block.
     */
    public static class Host {
        boolean patternsApplied;

        String hostName;
        String proxyJump;
        int port;
        String identityFile;
        String identityAgent;
        String user;
        String preferredAuthentications;
        Boolean identitiesOnly;
        Boolean batchMode;

        void copyFrom(final Host src) {
            if(hostName == null) {
                hostName = src.hostName;
            }
            if(proxyJump == null) {
                proxyJump = src.proxyJump;
            }
            if(port == 0) {
                port = src.port;
            }
            if(identityFile == null) {
                identityFile = src.identityFile;
            }
            if(identityAgent == null) {
                identityAgent = src.identityAgent;
            }
            if(user == null) {
                user = src.user;
            }
            if(preferredAuthentications == null) {
                preferredAuthentications = src.preferredAuthentications;
            }
            if(batchMode == null) {
                batchMode = src.batchMode;
            }
            if(identitiesOnly == null) {
                identitiesOnly = src.identitiesOnly;
            }
        }

        /**
         * @return the real IP address or host name to connect to; never null.
         */
        public String getHostName() {
            return hostName;
        }

        public String getProxyJump() {
            return proxyJump;
        }

        /**
         * @return the real port number to connect to; never 0.
         */
        public int getPort() {
            return port;
        }

        /**
         * @return path of the private key file to use for authentication; null if the caller should use default
         * authentication strategies.
         */
        public String getIdentityFile() {
            return identityFile;
        }

        /**
         * @return Specifies the UNIX-domain socket used to communicate with the authentication agent.
         */
        public String getIdentityAgent() {
            return identityAgent;
        }

        /**
         * @return the real user name to connect as; never null.
         */
        public String getUser() {
            return user;
        }

        /**
         * @return the preferred authentication methods, separated by commas if more than one authentication method is
         * preferred.
         */
        public String getPreferredAuthentications() {
            return preferredAuthentications;
        }

        /**
         * @return only use the configured authentication identity and certificate files
         */
        public Boolean isIdentitiesOnly() {
            return identitiesOnly;
        }

        /**
         * @return true if batch (non-interactive) mode is preferred for this host connection.
         */
        public boolean isBatchMode() {
            return batchMode != null && batchMode;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Host{");
            sb.append("patternsApplied=").append(patternsApplied);
            sb.append(", hostName='").append(hostName).append('\'');
            sb.append(", proxyJump='").append(proxyJump).append('\'');
            sb.append(", port=").append(port);
            sb.append(", identityFile=").append(identityFile);
            sb.append(", identityAgent=").append(identityAgent);
            sb.append(", user='").append(user).append('\'');
            sb.append(", preferredAuthentications='").append(preferredAuthentications).append('\'');
            sb.append(", identitiesOnly=").append(identitiesOnly);
            sb.append(", batchMode=").append(batchMode);
            sb.append('}');
            return sb.toString();
        }
    }

    private static final class MatchBlock {
        final List<String> patterns;
        final Host host;

        MatchBlock(final List<String> patterns, final Host host) {
            this.patterns = patterns;
            this.host = host;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OpenSshConfig{");
        sb.append("configuration=").append(configuration);
        sb.append('}');
        return sb.toString();
    }
}
