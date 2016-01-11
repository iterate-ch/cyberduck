package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TerminalOptionsInputValidatorTest {

    @Test
    public void testValidate() throws Exception {
        assertTrue(new TerminalOptionsInputValidator(new ProtocolFactory(Collections.singleton(new FTPProtocol())))
                .validate("ftp://cdn.duck.sh/"));
        assertFalse(new TerminalOptionsInputValidator(new ProtocolFactory(Collections.singleton(new FTPProtocol())))
                .validate("ftp://cdn.duck.sh/%%~nc"));
    }

    @Test
    public void testValidateProfile() throws Exception {
        final Set<Protocol> list = new HashSet<>(Arrays.asList(
                new SwiftProtocol(),
                new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SwiftProtocol())))
                        .read(new Local("../profiles/Rackspace US.cyberduckprofile"))
        ));
        assertTrue(new TerminalOptionsInputValidator(new ProtocolFactory(list)).validate("rackspace://cdn.duck.sh/%%~nc"));
    }

    @Test
    public void testColonInPath() throws Exception {
        final String uri = "rackspace://cdn.duck.sh/duck-4.6.2.16174:16179M.pkg";
        final Set<Protocol> list = new HashSet<>(Arrays.asList(
                new SwiftProtocol(),
                new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SwiftProtocol())))
                        .read(new Local("../profiles/Rackspace US.cyberduckprofile"))
        ));
        assertTrue(new TerminalOptionsInputValidator(new ProtocolFactory(list)).validate(uri));
    }

    @Test
    public void testListContainers() throws Exception {
        final Set<Protocol> list = new HashSet<>(Arrays.asList(
                new SwiftProtocol(),
                new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new SwiftProtocol())))
                        .read(new Local("../profiles/Rackspace US.cyberduckprofile"))
        ));
        assertTrue(new TerminalOptionsInputValidator(new ProtocolFactory(list)).validate("rackspace:///"));
        assertFalse(new TerminalOptionsInputValidator(new ProtocolFactory(list)).validate("rackspace://"));
    }
}