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
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.sftp;
using ch.cyberduck.ui;
using ch.ethz.ssh2;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public class HostKeyController : PreferencesHostKeyVerifier
    {
        private static readonly Logger Log = Logger.getLogger(typeof (HostKeyController).FullName);

        /// <summary>
        /// Parent browser
        /// </summary>
        private readonly WindowController _parent;

        private HostKeyController(WindowController c)
        {
            _parent = c;
        }

        protected override bool isUnknownKeyAccepted(string hostname, int port, string serverHostKeyAlgorithm,
                                                     byte[] serverHostKey)
        {
            if (base.isUnknownKeyAccepted(hostname, port, serverHostKeyAlgorithm, serverHostKey))
            {
                return true;
            }
            AsyncController.AsyncDelegate d = delegate
                {
                    _parent.CommandBox(
                        String.Format(LocaleFactory.localizedString("Unknown host key for {0}."), hostname),
                        String.Format(LocaleFactory.localizedString("Unknown host key for {0}."), hostname),
                        String.Format(
                            LocaleFactory.localizedString(
                                "The host is currently unknown to the system. The host key fingerprint is {0}."),
                            KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey)),
                        String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                                      LocaleFactory.localizedString("Deny")),
                        false,
                        isHostKeyDatabaseWritable() ? LocaleFactory.localizedString("Always") : null,
                        SysIcons.Question,
                        Preferences.instance().getProperty("website.help") + "/" +
                        Scheme.sftp.name(),
                        delegate(int option, bool verificationChecked)
                            {
                                switch (option)
                                {
                                    case 0:
                                        allow(hostname, serverHostKeyAlgorithm, serverHostKey,
                                              verificationChecked);
                                        break;
                                    case 1:
                                        Log.warn("Cannot continue without a valid host key");
                                        throw new ConnectionCanceledException();
                                }
                            });
                };
            _parent.Invoke(d, true);
            return true;
        }

        protected override bool isChangedKeyAccepted(string hostname, int port, string serverHostKeyAlgorithm,
                                                     byte[] serverHostKey)
        {
            AsyncController.AsyncDelegate d = delegate
                {
                    _parent.CommandBox(
                        String.Format(LocaleFactory.localizedString("Host key mismatch for {0}"), hostname),
                        String.Format(LocaleFactory.localizedString("Host key mismatch for {0}"), hostname),
                        String.Format(LocaleFactory.localizedString("The host key supplied is {0}."),
                                      KnownHosts.createHexFingerprint(serverHostKeyAlgorithm,
                                                                      serverHostKey)),
                        String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                                      LocaleFactory.localizedString("Deny")),
                        false,
                        isHostKeyDatabaseWritable() ? LocaleFactory.localizedString("Always") : null,
                        SysIcons.Warning,
                        Preferences.instance().getProperty("website.help") + "/" +
                        Scheme.sftp.name(),
                        delegate(int option, bool verificationChecked)
                            {
                                switch (option)
                                {
                                    case 0:
                                        allow(hostname, serverHostKeyAlgorithm, serverHostKey,
                                              verificationChecked);
                                        break;
                                    case 1:
                                        Log.warn("Cannot continue without a valid host key");
                                        throw new ConnectionCanceledException();
                                }
                            });
                };
            _parent.Invoke(d, true);
            return true;
        }

        public static void Register()
        {
            HostKeyControllerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : HostKeyControllerFactory
        {
            protected override object create()
            {
                return null;
            }

            public override ch.cyberduck.core.HostKeyController create(ch.cyberduck.ui.Controller c, Protocol protocol)
            {
                if(Scheme.sftp.equals(protocol.getScheme())) {
                    return new HostKeyController(c as WindowController);
                }
                return new DefaultHostKeyController();
            }
        }
    }
}