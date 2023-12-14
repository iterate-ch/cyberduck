//
// Copyright (c) 2023 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

using ch.cyberduck.core.i18n;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Properties;
using java.security;
using java.util;
using org.apache.commons.lang3;
using org.apache.logging.log4j;
using org.apache.logging.log4j.core.config;
using sun.security.mscapi;
using System;
using System.Collections;
using System.Collections.Specialized;
using System.Configuration;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Runtime.CompilerServices;
using Windows.Storage;

namespace Ch.Cyberduck.Core.Preferences
{
    using System = java.lang.System;

    public class ApplicationPreferences : DefaultPreferences
    {
        private static readonly char[] CultureSeparator = new[] { '_', '-' };
        private static readonly Logger Log = LogManager.getLogger(typeof(ApplicationPreferences).FullName);
        private static SettingsDictionary shared;
        private static SharedSettings sharedSettings;
        private static SettingsDictionary user;
        private static Settings userSettings;
        private readonly Locales locales;

        public ApplicationPreferences(Locales locales, IRuntime runtime)
        {
            this.locales = locales;
            Runtime.Current = runtime;

            System.setProperty("jna.boot.library.path", runtime.Location);
        }

        protected static SettingsDictionary Shared
        {
            get => shared ??= SharedSettings.CdSettings;
            set => SharedSettings.CdSettings = shared = value;
        }

        protected static ApplicationSettingsBase SharedApplicationSettings => SharedSettings;

        protected static SettingsDictionary User
        {
            get => user ??= UserSettings.CdSettings;
            set => UserSettings.CdSettings = user = value;
        }

        protected static ApplicationSettingsBase UserApplicationSettings => UserSettings;

        private static SharedSettings SharedSettings => sharedSettings ??= SharedSettings.Default;

        private static Settings UserSettings => userSettings ??= Settings.Default;

        public override List applicationLocales() => locales.applicationLocales();

        public override void deleteProperty(string property)
        {
            Shared.Remove(property);
            SharedSettings.Default.CdSettingsDirty = true;
        }

        public string GetDefaultLanguage()
        {
            List sysLocales = systemLocales();
            List appLocales = applicationLocales();
            for (int i = 0; i < sysLocales.size(); i++)
            {
                string s = (string)sysLocales.get(i);
                string match = TryToMatchLocale(s, appLocales);
                if (null != match)
                {
                    if (Log.isDebugEnabled())
                    {
                        Log.debug($"Default locale is '{match}' for system locale '{s}'");
                    }

                    return match;
                }
            }

            //default to english
            if (Log.isDebugEnabled())
            {
                Log.debug("Fallback to locale 'en'");
            }
            return "en";
        }

        public override string getDisplayName(string locale)
        {
            //cy is a special case as it is not available in the framework
            if ("cy".Equals(locale))
            {
                return "Welsh";
            }

            CultureInfo cultureInfo = CultureInfo.GetCultureInfo(locale.Replace('_', '-'));
            return cultureInfo.TextInfo.ToTitleCase(cultureInfo.NativeName);
        }

        public override string getProperty(string property)
        {
            if (Shared[property] is not string value || string.IsNullOrWhiteSpace(value))
            {
                value = getDefault(property);
            }
            return value;
        }

        public override void load()
        {
            Shared ??= new();

            MigrateConfig();
        }

        public override string locale()
        {
            return getProperty("application.language");
        }

        public override void save() => Save(SharedSettings);

        public override void setProperty(string property, string value)
        {
            Shared[property] = value;
            SharedSettings.CdSettingsDirty = true;
        }

        public override List systemLocales() => locales.systemLocales();

        protected override void configureLogging(string level)
        {
            base.configureLogging(level);
            if (Debugger.IsAttached)
            {
                Configurator.setRootLevel(Level.DEBUG);
            }
        }

        protected virtual void OnUpgradeUserSettings()
        {
            try
            {
                UserSettings.Upgrade();
                Save(UserSettings);
            }
            catch
            {
                // Don't care about failures saving old user config.
            }
        }

        protected virtual void Save(ApplicationSettingsBase settings)
        {
            try
            {
                settings.Save();
            }
            catch (Exception ex)
            {
                Log.error("Failure saving preferences", ex);
            }
        }

        protected override void setDefaults()
        {
            base.setDefaults();

            this.setDefault("os.version", Environment.OSVersion.Version.ToString());

            this.setDefault("application.name", Runtime.ProductName);
            this.setDefault("application.datafolder.name", Runtime.DataFolderName);
            this.setDefault("oauth.handler.scheme",
                String.Format("x-{0}-action", StringUtils.deleteWhitespace(Runtime.ProductName.ToLower())));

            this.setDefault("application.version", Runtime.VersionString);
            this.setDefault("application.revision", Runtime.Revision);
            this.setDefault("application.language.custom", false.ToString());
            this.setDefault("application.localization.enable", true.ToString());

            this.setDefault("editor.bundleIdentifier", "shell:openfilewith");

            this.setDefault("update.feed.release", "https://version.cyberduck.io/windows/changelog.rss");
            this.setDefault("update.feed.beta", "https://version.cyberduck.io/windows/beta/changelog.rss");
            this.setDefault("update.feed.nightly", "https://version.cyberduck.io/windows/nightly/changelog.rss");

            // Importers
            this.setDefault("bookmark.import.winscp.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "WinSCP.ini"));
            this.setDefault("bookmark.import.filezilla.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "FileZilla",
                    "sitemanager.xml"));
            this.setDefault("bookmark.import.smartftp.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "SmartFTP",
                    "Client 2.0", "Favorites"));
            this.setDefault("bookmark.import.totalcommander.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "GHISLER",
                    "wcx_ftp.ini"));
            this.setDefault("bookmark.import.flashfxp3.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "FlashFXP", "3",
                    "Sites.dat"));
            this.setDefault("bookmark.import.flashfxp4.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "FlashFXP", "4",
                    "Sites.dat"));
            this.setDefault("bookmark.import.flashfxp4.common.location",
                Path.Combine(EnvironmentInfo.CommonAppDataPath, "FlashFXP",
                    "4",
                    "Sites.dat"));
            this.setDefault("bookmark.import.wsftp.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "Ipswitch", "WS_FTP",
                    "Sites"));
            this.setDefault("bookmark.import.fireftp.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "Mozilla", "Firefox",
                    "Profiles"));
            this.setDefault("bookmark.import.s3browser.location",
                Path.Combine(EnvironmentInfo.AppDataPath, "S3Browser",
                    "settings.ini"));
            this.setDefault("bookmark.import.crossftp.location", Path.Combine(EnvironmentInfo.UserProfilePath, ".crossftp", "sites.xml"));
            this.setDefault("bookmark.import.cloudberry.s3.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "CloudBerry S3 Explorer for Amazon S3", "settings.list"));
            this.setDefault("bookmark.import.cloudberry.google.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "CloudBerry Explorer for Google Storage", "settings.list"));
            this.setDefault("bookmark.import.cloudberry.azure.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "CloudBerry Explorer for Azure Blob Storage", "settings.list"));
            this.setDefault("bookmark.import.expandrive3.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "ExpanDrive", "favorites.js"));
            this.setDefault("bookmark.import.expandrive4.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "ExpanDrive", "expandrive4.favorites.js"));
            this.setDefault("bookmark.import.expandrive5.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "ExpanDrive", "expandrive5.favorites.js"));
            this.setDefault("bookmark.import.expandrive6.location",
                Path.Combine(EnvironmentInfo.LocalAppDataPath,
                    "ExpanDrive", "expandrive6.favorites.js"));
            this.setDefault("bookmark.import.netdrive2.location",
                Path.Combine(EnvironmentInfo.AppDataPath,
                    "NetDrive2", "drives.dat"));

            //disable reminder for protocol handler registration
            this.setDefault("defaulthandler.reminder", false.ToString());

            this.setDefault("update.check.privilege", true.ToString());

            this.setDefault("queue.download.folder", EnvironmentInfo.DownloadsPath);
            this.setDefault("queue.upload.permissions.default", true.ToString());

            this.setDefault("queue.dock.badge", true.ToString());

            this.setDefault("ssh.knownhosts",
                Path.Combine(EnvironmentInfo.UserProfilePath, ".ssh", "known_hosts"));
            this.setDefault("browser.enterkey.rename", false.ToString());
            this.setDefault("terminal.openssh.enable", true.ToString());
            this.setDefault("terminal.windowssubsystemlinux.enable", true.ToString());
            this.setDefault("terminal.command.ssh", Path.Combine(EnvironmentInfo.UserProfilePath, "putty.exe"));
            this.setDefault("terminal.command.ssh.args", "-ssh {0} {1}@{2} -t -P {3} -m \"{4}\"");
            this.setDefault("terminal.command.openssh.args", "{1} {0}@{2} -t -p {3} \"cd '{4}'; $SHELL\"");

            this.setDefault("notifications.timeout.milliseconds", "300");

            //default browser toolbar set
            this.setDefault("browser.toolbar", true.ToString());
            this.setDefault("browser.toolbar.openconnection", true.ToString());
            this.setDefault("browser.toolbar.quickconnect", true.ToString());
            this.setDefault("browser.toolbar.action", true.ToString());
            this.setDefault("browser.toolbar.info", true.ToString());
            this.setDefault("browser.toolbar.refresh", true.ToString());
            this.setDefault("browser.toolbar.edit", true.ToString());
            this.setDefault("browser.toolbar.openinbrowser", false.ToString());
            this.setDefault("browser.toolbar.openinterminal", false.ToString());
            this.setDefault("browser.toolbar.newfolder", false.ToString());
            this.setDefault("browser.toolbar.delete", false.ToString());
            this.setDefault("browser.toolbar.download", false.ToString());
            this.setDefault("browser.toolbar.upload", true.ToString());
            this.setDefault("browser.toolbar.transfers", true.ToString());

            //default transfer toolbar set
            this.setDefault("transfer.toolbar.resume", true.ToString());
            this.setDefault("transfer.toolbar.reload", true.ToString());
            this.setDefault("transfer.toolbar.stop", true.ToString());
            this.setDefault("transfer.toolbar.remove", true.ToString());
            this.setDefault("transfer.toolbar.cleanup", false.ToString());
            this.setDefault("transfer.toolbar.log", false.ToString());
            this.setDefault("transfer.toolbar.open", true.ToString());
            this.setDefault("transfer.toolbar.show", true.ToString());

            // Resolve symbolic links downloading target file instead. Cannot create symbolic links on FAT.
            this.setDefault("path.symboliclink.resolve", true.ToString());
            // Resolve local links uploading target file instead. Currently not supporting shortcuts on Windows.
            this.setDefault("local.symboliclink.resolve", true.ToString());

            this.setDefault("local.user.home", EnvironmentInfo.UserProfilePath);
            this.setDefault("local.delimiter", $"{Path.DirectorySeparatorChar}");
            this.setDefault("local.normalize.tilde", false.ToString());
            this.setDefault("local.normalize.unicode", false.ToString());

            // SSL Keystore
            // Add mscapi security provider
            Security.addProvider(new SunMSCAPI());
            this.setDefault("connection.ssl.keystore.type", "Windows-MY");
            this.setDefault("connection.ssl.keystore.provider", "SunMSCAPI");

            // Override secure random strong algorithm. Outputs bytes from the Windows CryptGenRandom() API
            this.setDefault("connection.ssl.securerandom.algorithm", "Windows-PRNG");
            this.setDefault("connection.ssl.securerandom.provider", "SunMSCAPI");
            // Set secure random algorithms for BC
            Security.setProperty("securerandom.strongAlgorithms", "Windows-PRNG:SunMSCAPI,SHA1PRNG:SUN");

            // Enable Integrated Windows Authentication
            this.setDefault("connection.proxy.windows.authentication.enable", true.ToString());

            this.setDefault("webdav.ntlm.environment", true.ToString());
            if (getBoolean("webdav.ntlm.environment"))
            {
                // NTLM Windows Domain
                try
                {
                    // Gets the network domain name associated with the current user
                    this.setDefault("webdav.ntlm.domain", Environment.UserDomainName);
                }
                catch (PlatformNotSupportedException)
                {
                    // The operating system does not support retrieving the network domain name.
                }
                catch (InvalidOperationException)
                {
                    // The network domain name cannot be retrieved.
                }

                try
                {
                    this.setDefault("webdav.ntlm.workstation", Environment.MachineName);
                }
                catch (InvalidOperationException)
                {
                    // The name of this computer cannot be obtained.
                }
            }

            if (Runtime.Packaged == true)
            {
                SetUWPDefaults();
            }
        }

        protected void UpgradeUserSettings()
        {
            if (!UserSettings.UpgradeSettings)
            {
                return;
            }
            UserSettings.UpgradeSettings = false;

            OnUpgradeUserSettings();
        }

        private static string TryToMatchLocale(string sysLocale, List appLocales)
        {
            for (int i = 0; i < appLocales.size(); i++)
            {
                string l = (string)appLocales.get(i);
                if (l.Equals(sysLocale))
                {
                    //direct match
                    return l;
                }
                //remove region
                int m = sysLocale.IndexOfAny(CultureSeparator);
                if (m > 0)
                {
                    string country = sysLocale.Substring(0, m);
                    if (l.Equals(country))
                    {
                        return l;
                    }
                }
            }
            return null;
        }

        private StringDictionary LoadUserConfig()
        {
            UpgradeUserSettings();
            return User;
        }

        private void MigrateConfig()
        {
            if (!SharedSettings.Migrate)
            {
                return;
            }
            SharedSettings.Migrate = false;

            StringDictionary userConfig = default;
            try
            {
                userConfig = LoadUserConfig();
            }
            catch
            {
                // ignore
            }

            if (userConfig is null or { Count: 0 })
            {
                return;
            }

            SharedSettings.CdSettingsDirty = true;

            foreach (DictionaryEntry item in userConfig)
            {
                setProperty((string)item.Key, (string)item.Value);
            }

            save();
        }

        [MethodImpl(MethodImplOptions.NoInlining)]
        private void SetUWPDefaults()
        {
            this.setDefault("update.check", $"{false}");
            this.setDefault("tmp.dir", ApplicationData.Current.TemporaryFolder.Path);
        }
    }
}
