package ch.cyberduck.core.sftp.openssh;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.sftp.openssh.config.transport.OpenSshConfig;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpenSshConfigTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testIncludeAbsolutePath() throws Exception {
        final File config = tmp.newFile("config-include-absolute");
        try(final FileWriter w = new FileWriter(config)) {
            w.write("Include " + Paths.get("src/test/resources/openssh/include-a").toAbsolutePath() + "\n");
        }
        final OpenSshConfig sshConfig = new OpenSshConfig(new Local(config.getAbsolutePath()));
        final OpenSshConfig.Host host = sshConfig.lookup("include-host-a");
        assertEquals("host-a.example.com", host.getHostName());
        assertEquals("auser", host.getUser());
        assertEquals(2222, host.getPort());
    }

    @Test
    public void testIncludeAbsolutePathWithWildcard() throws Exception {
        final File config = tmp.newFile("config-include-absolute-glob");
        final String dir = Paths.get("src/test/resources/openssh").toAbsolutePath().toString();
        try(final FileWriter w = new FileWriter(config)) {
            w.write("Include " + dir + "/include-*\n");
        }
        final OpenSshConfig sshConfig = new OpenSshConfig(new Local(config.getAbsolutePath()));
        final OpenSshConfig.Host hostA = sshConfig.lookup("include-host-a");
        assertEquals("host-a.example.com", hostA.getHostName());
        assertEquals("auser", hostA.getUser());
        final OpenSshConfig.Host hostB = sshConfig.lookup("include-host-b");
        assertEquals("host-b.example.com", hostB.getHostName());
        assertEquals("buser", hostB.getUser());
    }

    @Test
    public void testInclude() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include"));
        final OpenSshConfig.Host host = config.lookup("include-host-a");
        assertEquals("host-a.example.com", host.getHostName());
        // First-match-wins: User from the included Host block takes precedence over Match host block
        assertEquals("auser", host.getUser());
        assertEquals(2222, host.getPort());
        // Match host applies to the HostName from the included file and sets IdentityFile
        assertEquals("~/.ssh/match-key", host.getIdentityFile());
        // Hosts from the main config are available after missing and recursive includes
        final OpenSshConfig.Host main = config.lookup("main-host");
        assertEquals("main.example.com", main.getHostName());
        assertEquals("mainuser", main.getUser());
        assertNull(main.getIdentityFile());
    }

    @Test
    public void testIncludeGlob() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-glob"));
        final OpenSshConfig.Host hostA = config.lookup("include-host-a");
        assertEquals("host-a.example.com", hostA.getHostName());
        assertEquals("auser", hostA.getUser());
        // Global option from include-identityagent matched by the wildcard
        assertEquals("/run/ssh-agent.sock", hostA.getIdentityAgent());
        final OpenSshConfig.Host hostB = config.lookup("include-host-b");
        assertEquals("host-b.example.com", hostB.getHostName());
        assertEquals("buser", hostB.getUser());
        assertEquals("/run/ssh-agent.sock", hostB.getIdentityAgent());
    }

    @Test
    public void testIncludeWithinBlock() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-include-block"));
        // First appearance takes precedence. Host block in main config before include
        final OpenSshConfig.Host mainHost = config.lookup("include-host-b");
        assertEquals("override", mainHost.getUser());
        assertEquals("host-b.example.com", mainHost.getHostName());
        // Include is within the Host include-host-b block, blocks from included files never apply to other hosts
        final OpenSshConfig.Host includedHost = config.lookup("include-host-a");
        assertEquals("override", includedHost.getUser());
        assertEquals("include-host-a", includedHost.getHostName());
        assertEquals("SSH2", includedHost.getIdentityAgent());
        // Options from a file included in a Host block apply to that host
        assertEquals("/run/ssh-agent.sock", config.lookup("first").getIdentityAgent());
        assertEquals("SSH_AUTH_SOCK", config.lookup("with-agent").getIdentityAgent());
        assertEquals("/run/ssh-agent.sock", config.lookup("test-wildcard").getIdentityAgent());
        // Host * with include precedes Host second
        assertEquals("/run/ssh-agent.sock", config.lookup("second").getIdentityAgent());
        // Options following an Include directive apply after the blocks from the included file
        assertEquals("included", config.lookup("foo").getUser());
        assertEquals("main", config.lookup("bar").getUser());
    }

    @Test
    public void testMatchHost() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-match"));
        // Glob pattern
        final OpenSshConfig.Host glob = config.lookup("foo.example.com");
        assertEquals("matchuser", glob.getUser());
        assertEquals(2222, glob.getPort());
        assertNull(glob.getIdentityFile());
        // Exact pattern
        assertEquals("~/.ssh/exact-key", config.lookup("exact.example.com").getIdentityFile());
        // excluded.example.com matches *.example.com but is negated in the third block
        assertNull(config.lookup("excluded.example.com").getIdentityAgent());
        assertEquals("~/.ssh/agent.sock", config.lookup("other.example.com").getIdentityAgent());
        // Host outside *.example.com should not pick up any Match host settings
        final OpenSshConfig.Host unrelated = config.lookup("unrelated.org");
        assertNull(unrelated.getUser());
        assertEquals(-1, unrelated.getPort());
        // Original host criteria are matched against the name given, not after substitution by the Hostname option
        assertEquals("orig", config.lookup("original").getUser());
        assertNull(config.lookup("original.example.org").getUser());
        // A pattern list with only negated patterns never matches
        final OpenSshConfig.Host negated = config.lookup("bar", "bob");
        assertNull(negated.getUser());
        assertEquals(-1, negated.getPort());
        assertNull(negated.getIdentityAgent());
    }

    @Test
    public void testMatchUser() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-match"));
        assertEquals(9999, config.lookup("any.host", "alice").getPort());
        assertEquals(-1, config.lookup("any.host", "bob").getPort());
        // Without a user given, Match user is evaluated against the user configured by preceding blocks
        assertEquals(9999, config.lookup("configured").getPort());
        // Without a user given or configured, Match user is evaluated against the local user
        assertEquals(-1, config.lookup("unconfigured").getPort());
    }

    @Test
    public void testMatchHostAndUser() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-match"));
        // All criteria must match
        assertEquals("~/.ssh/combined-key", config.lookup("foo.example.com", "alice").getIdentityFile());
        assertNull(config.lookup("unrelated.org", "alice").getIdentityFile());
        assertNull(config.lookup("foo.example.com", "bob").getIdentityFile());
    }

    @Test
    public void testProxyCommand() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config"));
        assertEquals("ssh -W %h:%p bastion.example.org", config.lookup("proxycommand-host").getProxyCommand());
        // ProxyCommand none disables any inherited proxy command
        assertNull(config.lookup("proxycommand-none").getProxyCommand());
        assertNull(config.lookup("server2").getProxyCommand());
    }

    @Test
    public void testWildcard() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-wildcard"));
        assertEquals("one", config.lookup("one").getUser());
        // First appearance takes precedence over the later Host two block
        assertEquals("wildcard", config.lookup("two").getUser());
        // Explicit `none` is not overridden by the wildcard block
        final OpenSshConfig.Host disabled = config.lookup("x");
        assertNull(disabled.getIdentityAgent());
        assertNull(disabled.getProxyJump());
        assertNull(disabled.getProxyCommand());
        final OpenSshConfig.Host other = config.lookup("y");
        assertEquals("/other.sock", other.getIdentityAgent());
        assertEquals("bastion", other.getProxyJump());
        assertEquals("ssh -W %h:%p bastion.example.org", other.getProxyCommand());
    }

    @Test
    public void testGlobalOptions() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config-global"));
        final OpenSshConfig.Host host = config.lookup("x");
        // Options before the first Host or Match block apply to all hosts
        assertEquals("global", host.getUser());
        assertEquals("/global.sock", host.getIdentityAgent());
        // Option before the first Host block in an included file
        assertEquals(2201, host.getPort());
        // Match all
        assertEquals("publickey", host.getPreferredAuthentications());
        // Options following an Include directive apply after the blocks from the included file
        assertEquals("included", config.lookup("foo").getUser());
        assertEquals("global", config.lookup("bar").getUser());
    }

    @Test
    public void testTrailingComment() {
        final OpenSshConfig config = new OpenSshConfig(new Local("src/test/resources", "openssh/config"));
        final OpenSshConfig.Host host = config.lookup("comment");
        assertEquals("alice", host.getUser());
        // Quoted arguments are not truncated
        assertEquals("/tmp/a # b", host.getIdentityAgent());
        // The command is passed to the user's shell unmodified
        assertEquals("nc %h %p # passed to the shell", host.getProxyCommand());
    }
}
