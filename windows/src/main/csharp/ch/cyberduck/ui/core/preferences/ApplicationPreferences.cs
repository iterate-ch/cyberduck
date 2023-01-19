//
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
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

using ch.cyberduck.core.bonjour;
using ch.cyberduck.core.cryptomator;
using ch.cyberduck.core.cryptomator.random;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.AquaticPrime;
using Ch.Cyberduck.Core.Date;
using Ch.Cyberduck.Core.Diagnostics;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Core.Local;
using Ch.Cyberduck.Core.Preferences;
using Ch.Cyberduck.Core.Proxy;
using Ch.Cyberduck.Core.Sparkle;
using Ch.Cyberduck.Core.Urlhandler;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Threading;
using org.apache.logging.log4j;
using System.Reflection;
using System.Text.RegularExpressions;
using Application = System.Windows.Forms.Application;
using Rendezvous = Ch.Cyberduck.Core.Bonjour.Rendezvous;

namespace Ch.Cyberduck.Ui.Core.Preferences
{
    public class ApplicationPreferences : SettingsDictionaryPreferences
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(ApplicationPreferences).FullName);

        protected override void setDefaults()
        {
            base.setDefaults();

            this.setDefault("application.language", GetDefaultLanguage());

            this.setDefault("website.store", "ms-windows-store://pdp/?ProductId=9NBLGGH43HTB");

            this.setDefault("update.check.privilege", false.ToString());
        }

        protected override void setFactories()
        {
            base.setFactories();

            this.setDefault("factory.supportdirectoryfinder.class",
                typeof(RoamingSupportDirectoryFinder).AssemblyQualifiedName);
            this.setDefault("factory.localsupportdirectoryfinder.class",
                typeof(LocalSupportDirectoryFinder).AssemblyQualifiedName);
            this.setDefault("factory.applicationresourcesfinder.class",
                typeof(AssemblyApplicationResourcesFinder).AssemblyQualifiedName);
            this.setDefault("factory.local.class", typeof(SystemLocal).AssemblyQualifiedName);
            this.setDefault("factory.locale.class", typeof(DictionaryLocale).AssemblyQualifiedName);
            this.setDefault("factory.dateformatter.class", typeof(UserDefaultsDateFormatter).AssemblyQualifiedName);
            this.setDefault("factory.passwordstore.class", typeof(PasswordStoreFacade).AssemblyQualifiedName);
            this.setDefault("factory.proxycredentialsstore.class",
                typeof(CredentialManagerProxyCredentialsStore).AssemblyQualifiedName);
            this.setDefault("factory.alertcallback.class", typeof(DialogAlertCallback).AssemblyQualifiedName);
            this.setDefault("factory.certificatestore.class", typeof(SystemCertificateStore).AssemblyQualifiedName);
            this.setDefault("factory.hostkeycallback.class", typeof(HostKeyController).AssemblyQualifiedName);
            this.setDefault("factory.logincallback.class", typeof(PromptLoginController).AssemblyQualifiedName);
            this.setDefault("factory.passwordcallback.class", typeof(PromptPasswordController).AssemblyQualifiedName);
            this.setDefault("factory.transfererrorcallback.class",
                typeof(DialogTransferErrorCallback).AssemblyQualifiedName);
            this.setDefault("factory.transferpromptcallback.download.class",
                typeof(DownloadPromptController).AssemblyQualifiedName);
            this.setDefault("factory.transferpromptcallback.upload.class",
                typeof(UploadPromptController).AssemblyQualifiedName);
            this.setDefault("factory.transferpromptcallback.copy.class",
                typeof(UploadPromptController).AssemblyQualifiedName);
            this.setDefault("factory.transferpromptcallback.sync.class",
                typeof(SyncPromptController).AssemblyQualifiedName);
            this.setDefault("factory.proxy.class", typeof(SystemProxy).AssemblyQualifiedName);
            this.setDefault("factory.reachability.class", typeof(TcpReachability).AssemblyQualifiedName);
            this.setDefault("factory.reachability.diagnostics.class", typeof(TcpReachability).AssemblyQualifiedName);

            this.setDefault("factory.applicationfinder.class", typeof(ShellApplicationFinder).AssemblyQualifiedName);
            this.setDefault("factory.applicationlauncher.class", typeof(WindowsApplicationLauncher).AssemblyQualifiedName);
            this.setDefault("factory.browserlauncher.class", typeof(DefaultBrowserLauncher).AssemblyQualifiedName);
            this.setDefault("factory.reveal.class", typeof(ExplorerRevealService).AssemblyQualifiedName);
            this.setDefault("factory.trash.class", typeof(RecycleLocalTrashFeature).AssemblyQualifiedName);
            this.setDefault("factory.symlink.class", typeof(NullLocalSymlinkFeature).AssemblyQualifiedName);
            this.setDefault("factory.terminalservice.class", typeof(SshTerminalService).AssemblyQualifiedName);
            this.setDefault("factory.editorfactory.class", typeof(SystemWatchEditorFactory).AssemblyQualifiedName);
            this.setDefault("factory.badgelabeler.class", typeof(TaskbarApplicationBadgeLabeler).AssemblyQualifiedName);
            this.setDefault("factory.filedescriptor.class", typeof(Win32FileDescriptor).AssemblyQualifiedName);
            this.setDefault("factory.schemehandler.class", typeof(URLSchemeHandlerConfiguration).AssemblyQualifiedName);

            if (Cyberduck.Core.Utils.IsWin10FallCreatorsUpdate)
            {
                this.setDefault("factory.notification.class", typeof(DesktopNotificationService).AssemblyQualifiedName);
            }

            if (Cyberduck.Core.Utils.IsRunningAsUWP)
            {
                this.setDefault("factory.rendezvous.class", typeof(DisabledRendezvous).AssemblyQualifiedName);
                this.setDefault("factory.licensefactory.class", typeof(WindowsStoreLicenseFactory).AssemblyQualifiedName);
            }
            else
            {
                this.setDefault("factory.rendezvous.class", typeof(Rendezvous).AssemblyQualifiedName);
                this.setDefault("factory.updater.class", typeof(WinSparklePeriodicUpdateChecker).AssemblyQualifiedName);
            }
            this.setDefault("factory.vault.class", typeof(CryptoVault).AssemblyQualifiedName);
            this.setDefault("factory.securerandom.class", typeof(FastSecureRandomProvider).AssemblyQualifiedName);
        }
    }
}
