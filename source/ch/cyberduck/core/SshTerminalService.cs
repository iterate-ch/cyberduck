// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using System;
using System.IO;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core
{
    public class SshTerminalService : TerminalService
    {
        public void open(Host host, Path workdir)
        {
            string tempFile = System.IO.Path.GetTempFileName();
            bool identity = host.getCredentials().isPublicKeyAuthentication();
            TextWriter tw = new StreamWriter(tempFile);
            tw.WriteLine("cd {0} && exec $SHELL", workdir.getAbsolute());
            tw.Close();
            String ssh = String.Format(PreferencesFactory.get().getProperty("terminal.command.ssh.args"),
                                       identity
                                           ? string.Format("-i \"{0}\"", host.getCredentials().getIdentity().getAbsolute())
                                           : String.Empty, host.getCredentials().getUsername(), host.getHostname(),
                                       Convert.ToString(host.getPort()), tempFile);
            ApplicationLauncherFactory.get()
                                      .open(
                                          new Application(PreferencesFactory.get().getProperty("terminal.command.ssh"),
                                                          null), ssh);
        }
    }
}