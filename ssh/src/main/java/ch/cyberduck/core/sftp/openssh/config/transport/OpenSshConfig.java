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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.sftp.openssh.config.errors.InvalidPatternException;
import ch.cyberduck.core.sftp.openssh.config.fnmatch.FileNameMatcher;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
     * Cached Host and Match blocks read out of the configuration file in order of appearance.
     */
    private List<Block> blocks = Collections.emptyList();

    /**
     * Obtain the user's configuration data.
     * <p/>
     * The configuration file is always returned to the caller, even if no file exists in the user's home directory at
     * the time the call was made. The parsed configuration is cached until {@link #refresh()} is called.
     */
    public OpenSshConfig(final Local configuration) {
        this.configuration = configuration;
        this.refresh();
    }

    /**
     * Locate the configuration for a specific host request.
     *
     * @param hostName the name the user has supplied to the SSH tool. This may be a real host name, or it may just be
     *                 a "Host" block in the configuration file.
     * @return r configuration for the requested name. Never null.
     */
    public Host lookup(final String hostName) {
        return this.lookup(hostName, null);
    }

    public Host lookup(final String hostName, final String user) {
        final Host h = new Host();
        // Blocks applicable to this lookup. Blocks from included files can only match
        // if the block enclosing the
        // Include directive was applicable
        final Set<Block> applied = new HashSet<>();
        // Blocks are applied in order of appearance and the first obtained value for
        // each option is used
        for(final Block b : blocks) {
            if(b.parent != null && !applied.contains(b.parent)) {
                continue;
            }
            if(!isApplicable(b, hostName, h, user)) {
                continue;
            }
            log.debug("Found block applicable for {} in SSH config: {}", hostName, b.host);
            applied.add(b);
            h.copyFrom(b.host);
        }
        if(h.hostName == null) {
            // Defaults to the name given on the command line
            h.hostName = hostName;
        }
        if(h.port == 0) {
            h.port = -1;
        }
        return h;
    }

    public void refresh() {
        try {
            final List<Block> newBlocks = new ArrayList<>();
            this.parse(configuration, Collections.emptySet(), newBlocks, null);
            blocks = newBlocks;
        }
        catch(AccessDeniedException | IOException e) {
            log.warn("Failure reading {}. {}", configuration, e.getMessage());
            blocks = Collections.emptyList();
        }
    }

    /**
     * @param file      Configuration file
     * @param chain     Configuration files including this file. These are skipped when included again to prevent loops.
     * @param blocks    Accumulator for blocks found during parsing.
     * @param enclosing Block containing the Include directive referencing this file. Null for the main configuration
     *                  file.
     */
    private void parse(final Local file, final Set<Local> chain, final List<Block> blocks, final Block enclosing) throws AccessDeniedException, IOException {
        final Set<Local> including = new HashSet<>(chain);
        including.add(file);
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Block options are added to. Options before the first Host or Match block apply within the block
            // enclosing the Include directive, or to all hosts in the main configuration file
            Block block = new Block(enclosing);
            blocks.add(block);
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
                // Arguments may be followed by a comment, except for the command passed to the user's shell
                final String argValue = "ProxyCommand".equalsIgnoreCase(keyword) ? parts[1].trim() : uncomment(parts[1].trim());
                if(argValue.isEmpty()) {
                    continue;
                }
                if("Host".equalsIgnoreCase(keyword)) {
                    final List<String> patterns = new ArrayList<>();
                    for(final String pattern : argValue.split("[ \t]+")) {
                        patterns.add(dequote(pattern));
                    }
                    // Host patterns are matched against the host name given, same as Match originalhost
                    block = new Block(enclosing, Collections.emptyList(), patterns, Collections.emptyList());
                    blocks.add(block);
                    continue;
                }
                if("Match".equalsIgnoreCase(keyword)) {
                    final List<String> hostPatterns = new ArrayList<>();
                    final List<String> originalHostPatterns = new ArrayList<>();
                    final List<String> userPatterns = new ArrayList<>();
                    boolean all = false;
                    boolean unknown = false;
                    final String[] tokens = argValue.split("[ \t]+");
                    for(int i = 0; i < tokens.length && !unknown; i++) {
                        final String criterion = tokens[i];
                        if("all".equalsIgnoreCase(criterion)) {
                            all = true;
                            continue;
                        }
                        final List<String> patterns = "host".equalsIgnoreCase(criterion) ? hostPatterns : "originalhost".equalsIgnoreCase(criterion) ? originalHostPatterns : "user".equalsIgnoreCase(criterion) ? userPatterns : null;
                        if(patterns == null) {
                            log.warn("Unknown Match criterion: {}", criterion);
                            unknown = true;
                        }
                        else if(++i < tokens.length) {
                            patterns(tokens[i], patterns);
                        }
                    }
                    block = new Block(enclosing, hostPatterns, originalHostPatterns, userPatterns);
                    // Requires either all or other known criteria. Otherwise the block is never applied, nor are blocks
                    // from files included within it
                    if(!unknown && all == (hostPatterns.isEmpty() && originalHostPatterns.isEmpty() && userPatterns.isEmpty())) {
                        blocks.add(block);
                    }
                    continue;
                }
                if("Include".equalsIgnoreCase(keyword)) {
                    for(final String pattern : argValue.split("[ \t]+")) {
                        for(final Local included : resolve(file.getParent(), dequote(pattern))) {
                            if(including.contains(included)) {
                                log.warn("Skipping recursive include of SSH config {}", included);
                                continue;
                            }
                            try {
                                // Included files are processed within the enclosing block. Blocks started in the
                                // included file only match if the enclosing block matches.
                                this.parse(included, including, blocks, block);
                            }
                            catch(AccessDeniedException | IOException e) {
                                log.warn("Failure reading included SSH config {}. {}", included, e.getMessage());
                                // Ignore and skip
                            }
                        }
                    }
                    // Options following the Include directive apply after those from the included files
                    block = new Block(block);
                    blocks.add(block);
                    continue;
                }
                // Merged into the block so the first obtained value for each option is used
                final Host option = new Host();
                if("HostName".equalsIgnoreCase(keyword)) {
                    option.hostName = dequote(argValue);
                }
                else if("ProxyJump".equalsIgnoreCase(keyword)) {
                    option.proxyJump = noneToDisabled(dequote(argValue));
                }
                else if("ProxyCommand".equalsIgnoreCase(keyword)) {
                    // The whole argument is passed to the user's shell, do not strip embedded quotes.
                    option.proxyCommand = noneToDisabled(argValue);
                }
                else if("User".equalsIgnoreCase(keyword)) {
                    option.user = dequote(argValue);
                }
                else if("Port".equalsIgnoreCase(keyword)) {
                    try {
                        option.port = Integer.parseInt(dequote(argValue));
                    }
                    catch(NumberFormatException nfe) {
                        // Bad port number. Don't set it.
                    }
                }
                else if("IdentityFile".equalsIgnoreCase(keyword)) {
                    option.identityFile = noneToNull(dequote(argValue));
                }
                else if("IdentityAgent".equalsIgnoreCase(keyword)) {
                    option.identityAgent = noneToDisabled(dequote(argValue));
                }
                else if("PreferredAuthentications".equalsIgnoreCase(keyword)) {
                    option.preferredAuthentications = noneToNull(StringUtils.deleteWhitespace(dequote(argValue)));
                }
                else if("IdentitiesOnly".equalsIgnoreCase(keyword)) {
                    option.identitiesOnly = yesno(dequote(argValue));
                }
                else if("BatchMode".equalsIgnoreCase(keyword)) {
                    option.batchMode = yesno(dequote(argValue));
                }
                block.host.copyFrom(option);
            }
        }
    }

    /**
     * Add comma-separated patterns to the given list
     */
    private static void patterns(final String value, final List<String> patterns) {
        for(final String p : StringUtils.split(value, ',')) {
            final String trimmed = dequote(p.trim());
            if(!trimmed.isEmpty()) {
                patterns.add(trimmed);
            }
        }
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
        final String name = FilenameUtils.getName(pattern);
        // Include accepts the tokens %%, %C, %d, %h, %i, %j, %k, %L, %l, %n, %p, %r, and %u.
        if(StringUtils.containsAny(pattern, '*', '?')) {
            // Each pathname may contain glob(7) wildcards
            if(parent.isDirectory()) {
                log.debug("Resolve files in {} matching {}", parent, name);
                try {
                    for(Local l : parent.list(file -> FilenameUtils.wildcardMatch(file, name))) {
                        result.add(l);
                    }
                }
                catch(AccessDeniedException e) {
                    log.warn("Failure reading directory {}", parent);
                }
                // Wildcards will be expanded and processed in lexical order
                result.sort(Comparator.comparing(Local::getAbsolute));
            }
        }
        else {
            result.add(LocalFactory.get(parent, name));
        }
        return result;
    }

    /**
     * Evaluates whether a block applies. All present criteria must match, a block without criteria always applies.
     *
     * @param mb       Block
     * @param hostName Host name given for the lookup
     * @param h        Configuration obtained from blocks applied so far
     * @param user     User given for the lookup or null
     */
    private static boolean isApplicable(final Block mb, final String hostName, final Host h, final String user) {
        // Host criteria are matched against the target hostname, after any substitution
        // by the Hostname option
        if(!mb.hostPatterns.isEmpty() && !isPatternsMatch(mb.hostPatterns, h.hostName != null ? h.hostName : hostName)) {
            return false;
        }
        if(!mb.originalHostPatterns.isEmpty() && !isPatternsMatch(mb.originalHostPatterns, hostName)) {
            return false;
        }
        if(!mb.userPatterns.isEmpty()) {
            // User criteria are matched against the given user, the user configured so far
            // or the local user
            final String target = user != null ? user : h.user != null ? h.user : System.getProperty("user.name");
            if(!isPatternsMatch(mb.userPatterns, target)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates a pattern list against a value.
     * <p>
     * A list applies when at least one positive pattern matches and no negated pattern matches.
     */
    private static boolean isPatternsMatch(final List<String> patterns, final String value) {
        boolean anyPositiveMatch = false;
        for(final String pattern : patterns) {
            if(pattern.startsWith("!")) {
                if(isHostMatch(pattern.substring(1), value)) {
                    return false;
                }
            }
            else if(isHostMatch(pattern, value)) {
                anyPositiveMatch = true;
            }
        }
        return anyPositiveMatch;
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
        if(value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Remove a trailing comment starting with {@code #} at the beginning of an unquoted argument
     */
    private static String uncomment(final String value) {
        boolean quoted = false;
        for(int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if(c == '"') {
                quoted = !quoted;
            }
            else if(c == '#' && !quoted && (i == 0 || Character.isWhitespace(value.charAt(i - 1)))) {
                return value.substring(0, i).trim();
            }
        }
        return value;
    }

    /**
     * Options disabled with {@code none} are stored as an empty string rather than null, as null denotes an unset
     * option that a later block may still set. Getters normalize the empty string back to null.
     *
     * @return Empty string for {@code none} so a value obtained later cannot override the disabled option
     */
    private static String noneToDisabled(final String value) {
        if("none".equalsIgnoreCase(value)) {
            return StringUtils.EMPTY;
        }
        return value;
    }

    private static Boolean yesno(final String value) {
        if("yes".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * @return Null for {@code none}, leaving the option unset
     */
    private static String noneToNull(final String value) {
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
     * When returned from {@link OpenSshConfig#lookup(String)} all applicable blocks have been merged in order of
     * appearance, with the first obtained value for each option taking precedence.
     */
    public static class Host {
        String hostName;
        String proxyJump;
        String proxyCommand;
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
            if(proxyCommand == null) {
                proxyCommand = src.proxyCommand;
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

        /**
         * @return the jump host or null if not set or disabled with {@code ProxyJump none}
         */
        public String getProxyJump() {
            // Normalize the `none` sentinel (empty string) back to null for callers
            return StringUtils.isEmpty(proxyJump) ? null : proxyJump;
        }

        /**
         * @return the command to use to connect to the server, or null if a direct connection or {@code ProxyJump}
         * should be used, or if disabled with {@code ProxyCommand none}. The returned value may still contain the
         * tokens {@code %h}, {@code %p} and {@code %r}.
         */
        public String getProxyCommand() {
            // Normalize the `none` sentinel (empty string) back to null for callers
            return StringUtils.isEmpty(proxyCommand) ? null : proxyCommand;
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
         * @return Specifies the UNIX-domain socket used to communicate with the authentication agent. Empty string if disabled with {@code IdentityAgent none}.
         */
        public String getIdentityAgent() {
            // Do not normalize the `none` sentinel (empty string) back to null for callers
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
            sb.append("hostName='").append(hostName).append('\'');
            sb.append(", proxyJump='").append(proxyJump).append('\'');
            sb.append(", proxyCommand='").append(proxyCommand).append('\'');
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

    /**
     * Host or Match block with the options parsed for it. A block without criteria applies whenever its parent
     * applies, used for options before the first Host or Match block of a file, options following an Include
     * directive and {@code Match all}.
     */
    private static final class Block {
        /**
         * Block containing the Include directive this block was read from, or the block an Include directive
         * interrupted. Null if not within a block.
         */
        final Block parent;
        /**
         * Matched against the target host name after substitution by the HostName option
         */
        final List<String> hostPatterns;
        /**
         * Matched against the host name given. Used for Host blocks.
         */
        final List<String> originalHostPatterns;
        final List<String> userPatterns;
        final Host host = new Host();

        Block(final Block parent) {
            this(parent, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Block(final Block parent, final List<String> hostPatterns, final List<String> originalHostPatterns, final List<String> userPatterns) {
            this.parent = parent;
            this.hostPatterns = hostPatterns;
            this.originalHostPatterns = originalHostPatterns;
            this.userPatterns = userPatterns;
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
