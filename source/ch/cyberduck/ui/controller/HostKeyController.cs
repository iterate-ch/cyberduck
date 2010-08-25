// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.sftp;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ch.ethz.ssh2;
using org.apache.log4j;
using Session = ch.cyberduck.core.Session;

namespace Ch.Cyberduck.Ui.Controller
{
    public class HostKeyController : KnownHostsHostKeyVerifier
    {
        private static readonly Logger Log = Logger.getLogger(typeof (HostKeyController).Name);

        private HostKeyController()
        {
        }

        protected override bool isUnknownKeyAccepted(string hostname, int port, string serverHostKeyAlgorithm,
                                                     byte[] serverHostKey)
        {
            string commands = Locale.localizedString("Allow") + "|" +
                              Locale.localizedString("Deny") +
                              (isHostKeyDatabaseWritable()
                                   ? "|" + Locale.localizedString("Always")
                                   : string.Empty);

            int r = cTaskDialog.ShowCommandBox(Locale.localizedString("Unknown host key for") + " "
                                               + hostname,
                                               null,
                                               Locale.localizedString(
                                                   "The host is currently unknown to the system. The host key fingerprint is")
                                               + ": " +
                                               KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey) +
                                               ".",
                                               null,
                                               null,
                                               null, commands,
                                               false,
                                               eSysIcons.Warning, eSysIcons.Warning);
            switch (r)
            {
                case 0:
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                    return true;
                case 1:
                    Log.warn("Cannot continue without a valid host key");
                    break;
                case 2:
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                    return true;
            }
            throw new ConnectionCanceledException();
        }

        protected override bool isChangedKeyAccepted(string hostname, int port, string serverHostKeyAlgorithm,
                                                     byte[] serverHostKey)
        {
            string commands = Locale.localizedString("Allow") + "|" +
                              Locale.localizedString("Deny") +
                              (isHostKeyDatabaseWritable()
                                   ? "|" + Locale.localizedString("Always")
                                   : string.Empty);

            int r = cTaskDialog.ShowCommandBox(Locale.localizedString("Host key mismatch:") + " " + hostname,
                                               null,
                                               Locale.localizedString("The host key supplied is") + ": " +
                                               KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)
                                               //                                + "\n" + Locale.localizedString("The current allowed key for this host is") + " : "
                                               //                                + allowedHostKey.getFingerprint()
                                               + "\n"
                                               + Locale.localizedString("Do you want to allow the host access?"),
                                               null,
                                               null,
                                               null, commands,
                                               false,
                                               eSysIcons.Warning, eSysIcons.Warning);
            switch (r)
            {
                case 0:
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey, false);
                    return true;
                case 1:
                    Log.warn("Cannot continue without a valid host key");
                    break;
                case 2:
                    allow(hostname, serverHostKeyAlgorithm, serverHostKey, true);
                    return true;
            }
            throw new ConnectionCanceledException();
        }

        public static void Register()
        {
            HostKeyControllerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : HostKeyControllerFactory
        {
            protected override object create()
            {
                return new HostKeyController();
            }

            public override ch.cyberduck.core.sftp.HostKeyController create(Session s)
            {
                return (HostKeyController) create();
            }

            public override ch.cyberduck.core.sftp.HostKeyController create(ch.cyberduck.ui.Controller c)
            {
                return (HostKeyController) create();
            }
        }
    }
}