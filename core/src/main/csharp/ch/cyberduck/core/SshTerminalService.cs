//
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
//

using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using org.apache.logging.log4j;
using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using Application = ch.cyberduck.core.local.Application;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core
{
    public class SshTerminalService : TerminalService
    {
        private static Logger logger = LogManager.getLogger(typeof(SshTerminalService).FullName);

        public void open(Host host, ch.cyberduck.core.Path workdir)
        {
            if (Utils.IsWin101809)
            {
                if (TryStartBuiltinOpenSSH(host, workdir.getAbsolute()))
                {
                    return;
                }
            }
            if (Utils.IsWin10FallCreatorsUpdate)
            {
                if (TryStartBashSSH(host, workdir.getAbsolute()))
                {
                    return;
                }
            }

            TryStartPuTTy(host, workdir.getAbsolute());
        }

        private static string GetSystemPath(string path)
        {
            var system = Environment.GetFolderPath(Environment.SpecialFolder.System);
            var test = Path.Combine(system, path);
            if (!File.Exists(test))
            {
                system = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), "SysNative");
                test = Path.Combine(system, path);
            }

            return test;
        }

        private static bool StartProcess(Process process)
        {
            if (!Utils.StartProcess(process))
            {
                return false;
            }
            if (process.WaitForExit(500))
            {
                return process.ExitCode == 0;
            }
            return true;
        }

        private string CreateOpenSSHCompatibleArguments(string username, string identity, string hostname, int port, string workdir)
        {
            return string.Format(
                PreferencesFactory.get().getProperty("terminal.command.openssh.args"),
                username, identity, hostname, port, workdir);
        }

        private bool TryStartBashSSH(Host host, string workdir)
        {
            if (!PreferencesFactory.get().getBoolean("terminal.windowssubsystemlinux.enable"))
            {
                return false;
            }

            var wsl = GetSystemPath("wsl.exe");
            if (!File.Exists(wsl))
            {
                return false;
            }

            var credentials = host.getCredentials();
            if (credentials.isPublicKeyAuthentication())
            {
                logger.warn("OpenSSH over Bash might complain 0777 permissions on DrvFS locations. Skipping Bash.");
                return false;
            }

            using (var process = new Process()
            {
                StartInfo = new ProcessStartInfo()
                {
                    FileName = wsl,
                    Arguments = "ssh " + CreateOpenSSHCompatibleArguments(
                        credentials.getUsername(),
                        null,
                        host.getHostname(),
                        host.getPort(),
                        workdir)
                }
            })
            {
                return StartProcess(process);
            }
        }

        private bool TryStartBuiltinOpenSSH(Host host, string workdir)
        {
            if (!PreferencesFactory.get().getBoolean("terminal.openssh.enable"))
            {
                return false;
            }

            var ssh = GetSystemPath(Path.Combine("OpenSSH", "ssh.exe"));
            if (!File.Exists(ssh))
            {
                logger.warn("Native openssh ssh.exe not found.");
                return false;
            }

            var credentials = host.getCredentials();
            var identity = credentials.isPublicKeyAuthentication();
            var identityString = identity ? string.Format("-i \"{0}\"", credentials.getIdentity().getAbsolute()) : "";

            using (var process = new Process()
            {
                StartInfo = new ProcessStartInfo()
                {
                    FileName = ssh,
                    Arguments = identityString + " " + CreateOpenSSHCompatibleArguments(
                        credentials.getUsername(),
                        identityString,
                        host.getHostname(),
                        host.getPort(),
                        workdir)
                }
            })
            {
                return StartProcess(process);
            }
        }

        private bool TryStartPuTTy(Host host, string workdir)
        {
            if (!File.Exists(PreferencesFactory.get().getProperty("terminal.command.ssh")))
            {
                OpenFileDialog selectDialog = new OpenFileDialog();
                selectDialog.Filter = "PuTTY executable (.exe)|*.exe";
                selectDialog.FilterIndex = 1;
                DialogResult result = DialogResult.None;
                Thread thread = new Thread(() => result = selectDialog.ShowDialog());
                thread.SetApartmentState(ApartmentState.STA);
                thread.Start();
                thread.Join();
                if (result == DialogResult.OK)
                {
                    PreferencesFactory.get().setProperty("terminal.command.ssh", selectDialog.FileName);
                }
                else
                {
                    return false;
                }
            }
            string tempFile = Path.GetTempFileName();
            bool identity = host.getCredentials().isPublicKeyAuthentication();
            TextWriter tw = new StreamWriter(tempFile);
            tw.WriteLine("cd {0} && exec $SHELL", workdir);
            tw.Close();
            String ssh = String.Format(PreferencesFactory.get().getProperty("terminal.command.ssh.args"),
                identity
                    ? string.Format("-i \"{0}\"", host.getCredentials().getIdentity().getAbsolute())
                    : String.Empty, host.getCredentials().getUsername(), host.getHostname(),
                Convert.ToString(host.getPort()), tempFile);
            return ApplicationLauncherFactory.get()
                .open(
                    new Application(PreferencesFactory.get().getProperty("terminal.command.ssh"), null), ssh);
        }
    }
}
