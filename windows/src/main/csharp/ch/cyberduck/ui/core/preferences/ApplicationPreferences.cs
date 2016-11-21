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

using ch.cyberduck.core.local;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Bonjour;
using Ch.Cyberduck.Core.Diagnostics;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Core.Local;
using Ch.Cyberduck.Core.Preferences;
using Ch.Cyberduck.Core.Proxy;
using Ch.Cyberduck.Core.Sparkle;
using Ch.Cyberduck.Core.Urlhandler;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Growl;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Threading;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Core.Preferences
{
    public class ApplicationPreferences : SettingsDictionaryPreferences
    {
        private static readonly Logger Log = Logger.getLogger(typeof (ApplicationPreferences).FullName);

        protected override void setFactories()
        {
            base.setFactories();

            defaults.put("factory.supportdirectoryfinder.class",
                typeof (RoamingSupportDirectoryFinder).AssemblyQualifiedName);
            defaults.put("factory.applicationresourcesfinder.class",
                typeof (AssemblyApplicationResourcesFinder).AssemblyQualifiedName);
            defaults.put("factory.local.class", typeof (SystemLocal).AssemblyQualifiedName);
            defaults.put("factory.locale.class", typeof (DictionaryLocale).AssemblyQualifiedName);
            defaults.put("factory.dateformatter.class", typeof (UserDefaultsDateFormatter).AssemblyQualifiedName);
            defaults.put("factory.passwordstore.class", typeof (DataProtectorPasswordStore).AssemblyQualifiedName);
            defaults.put("factory.certificatestore.class", typeof (SystemCertificateStore).AssemblyQualifiedName);
            defaults.put("factory.hostkeycallback.class", typeof (HostKeyController).AssemblyQualifiedName);
            defaults.put("factory.logincallback.class", typeof (PromptLoginController).AssemblyQualifiedName);
            defaults.put("factory.transfererrorcallback.class",
                typeof (DialogTransferErrorCallback).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.download.class",
                typeof (DownloadPromptController).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.upload.class",
                typeof (UploadPromptController).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.copy.class",
                typeof (UploadPromptController).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.sync.class",
                typeof (SyncPromptController).AssemblyQualifiedName);
            defaults.put("factory.proxy.class", typeof (SystemProxy).AssemblyQualifiedName);
            defaults.put("factory.reachability.class", typeof (TcpReachability).AssemblyQualifiedName);
            defaults.put("factory.rendezvous.class", typeof (Rendezvous).AssemblyQualifiedName);

            defaults.put("factory.applicationfinder.class", typeof (RegistryApplicationFinder).AssemblyQualifiedName);
            defaults.put("factory.applicationlauncher.class", typeof (WindowsApplicationLauncher).AssemblyQualifiedName);
            defaults.put("factory.temporaryfiles.class", typeof (WindowsTemporaryFileService).AssemblyQualifiedName);
            defaults.put("factory.browserlauncher.class", typeof (DefaultBrowserLauncher).AssemblyQualifiedName);
            defaults.put("factory.reveal.class", typeof (ExplorerRevealService).AssemblyQualifiedName);
            defaults.put("factory.trash.class", typeof (RecycleLocalTrashFeature).AssemblyQualifiedName);
            defaults.put("factory.symlink.class", typeof (NullLocalSymlinkFeature).AssemblyQualifiedName);
            defaults.put("factory.terminalservice.class", typeof (SshTerminalService).AssemblyQualifiedName);
            defaults.put("factory.editorfactory.class", typeof (SystemWatchEditorFactory).AssemblyQualifiedName);
            defaults.put("factory.notification.class", typeof (ToolstripNotificationService).AssemblyQualifiedName);
            if (Cyberduck.Core.Utils.IsWin7OrLater)
            {
                defaults.put("factory.badgelabeler.class", typeof (TaskbarApplicationBadgeLabeler).AssemblyQualifiedName);
            }
            defaults.put("factory.filedescriptor.class", typeof (Win32FileDescriptor).AssemblyQualifiedName);
            defaults.put("factory.schemehandler.class", typeof (URLSchemeHandlerConfiguration).AssemblyQualifiedName);

            if (!Cyberduck.Core.Utils.IsUWPSupported)
            {
                defaults.put("factory.updater.class", typeof(WinSparklePeriodicUpdateChecker).AssemblyQualifiedName);
            }
        }
    }
}
