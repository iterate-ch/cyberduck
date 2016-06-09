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

using System;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.sftp;
using Ch.Cyberduck.Core.TaskDialog;
using java.security;
using net.schmizz.sshj.common;
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

        public HostKeyController(WindowController c)
        {
            _parent = c;
        }

        protected override bool isUnknownKeyAccepted(string hostname, PublicKey key)
        {
            AsyncController.AsyncDelegate d = delegate
            {
                _parent.CommandBox(
                    String.Format(LocaleFactory.localizedString("Unknown fingerprint", "Sftp"), hostname),
                    String.Format(LocaleFactory.localizedString("Unknown fingerprint", "Sftp"), hostname),
                    String.Format(
                        LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.",
                            "Sftp"), new SSHFingerprintGenerator().fingerprint(key), KeyType.fromKey(key).name()),
                    String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                        LocaleFactory.localizedString("Deny")), false, LocaleFactory.localizedString("Always"),
                    TaskDialogIcon.Question,
                    PreferencesFactory.get().getProperty("website.help") + "/" + Scheme.sftp.name(),
                    delegate(int option, bool verificationChecked)
                    {
                        switch (option)
                        {
                            case 0:
                                allow(hostname, key, verificationChecked);
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

        protected override bool isChangedKeyAccepted(string hostname, PublicKey key)
        {
            AsyncController.AsyncDelegate d = delegate
            {
                _parent.CommandBox(
                    String.Format(LocaleFactory.localizedString("Changed fingerprint", "Sftp"), hostname),
                    String.Format(LocaleFactory.localizedString("Changed fingerprint", "Sftp"), hostname),
                    String.Format(
                        LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.",
                            "Sftp"), new SSHFingerprintGenerator().fingerprint(key), KeyType.fromKey(key).name()),
                    String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                        LocaleFactory.localizedString("Deny")), false, LocaleFactory.localizedString("Always"),
                    TaskDialogIcon.Warning,
                    PreferencesFactory.get().getProperty("website.help") + "/" + Scheme.sftp.name(),
                    delegate(int option, bool verificationChecked)
                    {
                        switch (option)
                        {
                            case 0:
                                allow(hostname, key, verificationChecked);
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
    }
}