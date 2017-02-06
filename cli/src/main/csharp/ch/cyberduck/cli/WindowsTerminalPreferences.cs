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

using ch.cyberduck.cli;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Diagnostics;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Core.Local;
using Ch.Cyberduck.Core.Preferences;
using Ch.Cyberduck.Core.Proxy;
using java.security;
using sun.security.mscapi;

namespace Ch.Cyberduck.Cli
{
    internal class WindowsTerminalPreferences : TerminalPreferences
    {
        protected override void setFactories()
        {
            base.setFactories();

            defaults.put("factory.locale.class", typeof(DictionaryLocale).AssemblyQualifiedName);
            defaults.put("factory.supportdirectoryfinder.class",
                typeof(RoamingSupportDirectoryFinder).AssemblyQualifiedName);
            defaults.put("factory.applicationresourcesfinder.class",
                typeof(AssemblyApplicationResourcesFinder).AssemblyQualifiedName);
            defaults.put("factory.editorfactory.class", typeof(SystemWatchEditorFactory).AssemblyQualifiedName);
            defaults.put("factory.applicationlauncher.class", typeof(WindowsApplicationLauncher).AssemblyQualifiedName);
            defaults.put("factory.applicationfinder.class", typeof(RegistryApplicationFinder).AssemblyQualifiedName);
            defaults.put("factory.local.class", typeof(SystemLocal).AssemblyQualifiedName);
            defaults.put("factory.passwordstore.class", typeof(DataProtectorPasswordStore).AssemblyQualifiedName);
            defaults.put("factory.proxy.class", typeof(SystemProxy).AssemblyQualifiedName);
            defaults.put("factory.reachability.class", typeof(TcpReachability).AssemblyQualifiedName);
            defaults.put("factory.filedescriptor.class", typeof(Win32FileDescriptor).AssemblyQualifiedName);
            defaults.put("factory.browserlauncher.class", typeof(DefaultBrowserLauncher).AssemblyQualifiedName);
        }

        protected override void setDefaults()
        {
            base.setDefaults();

            Security.addProvider(new SunMSCAPI());
            defaults.put("connection.ssl.keystore.type", "Windows-MY");
            defaults.put("connection.ssl.keystore.provider", "SunMSCAPI");

            // Override secure random strong algorithm. Outputs bytes from the Windows CryptGenRandom() API
            defaults.put("connection.ssl.securerandom.algorithm", "Windows-PRNG");
            defaults.put("connection.ssl.securerandom.provider", "SunMSCAPI");
        }
    }
}