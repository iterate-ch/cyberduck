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
using ch.cyberduck.core.exception;
using ch.cyberduck.core.sftp;
using ch.cyberduck.ui.core;
using Ch.Cyberduck.Core.TaskDialog;
using java.security;
using net.schmizz.sshj.common;
using org.apache.logging.log4j;
using System;
using UiUtils = Ch.Cyberduck.Ui.Core.Utils;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <param name="parent">
    /// Parent browser
    /// </param>
    public class HostKeyController(IWindowController parent) : PreferencesHostKeyVerifier
    {
        private static readonly Logger Log = LogManager.getLogger(typeof (HostKeyController).FullName);

        protected override bool isUnknownKeyAccepted(Host host, PublicKey key)
        {
            AsyncController.AsyncDelegate d = delegate
            {
                UiUtils.CommandBox(
                    owner: parent.Window,
                    title: String.Format(LocaleFactory.localizedString("Unknown fingerprint", "Sftp"), host.getHostname()),
                    mainInstruction: String.Format(LocaleFactory.localizedString("Unknown fingerprint", "Sftp"), host.getHostname()),
                    content: String.Format(
                        LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.",
                            "Sftp"), new SSHFingerprintGenerator().fingerprint(key), KeyType.fromKey(key).toString()),
                    expandedInfo: null,
                    help: ProviderHelpServiceFactory.get().help(Scheme.sftp),
                    verificationText: LocaleFactory.localizedString("Always"),
                    commandButtons: String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                        LocaleFactory.localizedString("Deny")),
                    showCancelButton: false, 
                    mainIcon: TaskDialogIcon.Question,
                    footerIcon: TaskDialogIcon.Information,
                    handler: delegate (int option, bool verificationChecked)
                    {
                        switch (option)
                        {
                            case 0:
                                allow(host, key, verificationChecked);
                                break;
                            case 1:
                                Log.warn("Cannot continue without a valid host key");
                                throw new ConnectionCanceledException();
                        }
                    });
            };
            parent.Invoke(d, true);
            return true;
        }

        protected override bool isChangedKeyAccepted(Host host, PublicKey key)
        {
            AsyncController.AsyncDelegate d = delegate
            {
                UiUtils.CommandBox(
                    owner: parent.Window,
                    title: String.Format(LocaleFactory.localizedString("Changed fingerprint", "Sftp"), host.getHostname()),
                    mainInstruction: String.Format(LocaleFactory.localizedString("Changed fingerprint", "Sftp"), host.getHostname()),
                    content: String.Format(
                        LocaleFactory.localizedString("The fingerprint for the {1} key sent by the server is {0}.",
                            "Sftp"), new SSHFingerprintGenerator().fingerprint(key), KeyType.fromKey(key).toString()),
                    expandedInfo: null,
                    help: ProviderHelpServiceFactory.get().help(Scheme.sftp),
                    verificationText: LocaleFactory.localizedString("Always"),
                    commandButtons: String.Format("{0}|{1}", LocaleFactory.localizedString("Allow"),
                        LocaleFactory.localizedString("Deny")), 
                    showCancelButton: false, 
                    mainIcon: TaskDialogIcon.Warning,
                    footerIcon: TaskDialogIcon.Information,
                    handler: delegate (int option, bool verificationChecked)
                    {
                        switch (option)
                        {
                            case 0:
                                allow(host, key, verificationChecked);
                                break;
                            case 1:
                                Log.warn("Cannot continue without a valid host key");
                                throw new ConnectionCanceledException();
                        }
                    });
            };
            parent.Invoke(d, true);
            return true;
        }
    }
}
