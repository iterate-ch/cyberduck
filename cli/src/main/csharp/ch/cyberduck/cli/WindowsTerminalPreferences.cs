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
using ch.cyberduck.core.preferences;
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
        public WindowsTerminalPreferences() : base(new AppConfigPreferences(new WindowsTerminalLocales()))
        {
        }

        protected override void setDefaults()
        {
            base.setDefaults();

            this.setDefault("application.datafolder.name", "Cyberduck");

            Security.addProvider(new SunMSCAPI());
            this.setDefault("connection.ssl.keystore.type", "Windows-MY");
            this.setDefault("connection.ssl.keystore.provider", "SunMSCAPI");

            // Override secure random strong algorithm. Outputs bytes from the Windows CryptGenRandom() API
            this.setDefault("connection.ssl.securerandom.algorithm", "Windows-PRNG");
            this.setDefault("connection.ssl.securerandom.provider", "SunMSCAPI");
            // Set secure random algorithms for BC
            Security.setProperty("securerandom.strongAlgorithms", "Windows-PRNG:SunMSCAPI,SHA1PRNG:SUN");

            this.setDefault("keychain.secure", true.ToString());
        }

        protected override void setFactories()
        {
            base.setFactories();

            this.setDefault("factory.locale.class", typeof(DictionaryLocale).AssemblyQualifiedName);
            this.setDefault("factory.supportdirectoryfinder.class",
                typeof(RoamingSupportDirectoryFinder).AssemblyQualifiedName);
            this.setDefault("factory.localsupportdirectoryfinder.class",
                typeof(LocalSupportDirectoryFinder).AssemblyQualifiedName);
            this.setDefault("factory.applicationresourcesfinder.class",
                typeof(AssemblyApplicationResourcesFinder).AssemblyQualifiedName);
            this.setDefault("factory.editorfactory.class", typeof(SystemWatchEditorFactory).AssemblyQualifiedName);
            this.setDefault("factory.applicationlauncher.class", typeof(WindowsApplicationLauncher).AssemblyQualifiedName);
            this.setDefault("factory.applicationfinder.class", typeof(RegistryApplicationFinder).AssemblyQualifiedName);
            this.setDefault("factory.local.class", typeof(SystemLocal).AssemblyQualifiedName);
            this.setDefault("factory.passwordstore.class", typeof(PasswordStoreFacade).AssemblyQualifiedName);
            this.setDefault("factory.proxycredentialsstore.class",
                typeof(CredentialManagerProxyCredentialsStore).AssemblyQualifiedName);
            this.setDefault("factory.proxy.class", typeof(SystemProxy).AssemblyQualifiedName);
            this.setDefault("factory.reachability.class", typeof(TcpReachability).AssemblyQualifiedName);
            this.setDefault("factory.filedescriptor.class", typeof(Win32FileDescriptor).AssemblyQualifiedName);
            this.setDefault("factory.browserlauncher.class", typeof(DefaultBrowserLauncher).AssemblyQualifiedName);
        }
    }
}
