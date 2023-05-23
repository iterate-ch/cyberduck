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
using Ch.Cyberduck.Properties;
using java.security;
using java.util;
using org.apache.commons.lang3;
using org.apache.logging.log4j;
using org.apache.logging.log4j.core.config;
using sun.security.mscapi;
using System;
using System.Collections;
using System.Collections.Concurrent;
using System.Collections.Specialized;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Runtime.CompilerServices;
using System.Text;
using Windows.Storage;
using CorePreferences = ch.cyberduck.core.preferences.Preferences;

namespace Ch.Cyberduck.Core.Preferences
{
    using System = java.lang.System;

    public class ApplicationPreferences : CorePreferences
    {
        private static readonly char[] CultureSeparator = new[] { '_', '-' };
        private static readonly Logger Log = LogManager.getLogger(typeof(ApplicationPreferences).FullName);
        private readonly Locales locales;
        private readonly ConcurrentDictionary<string, Preference> preferences = new();
        private readonly FileInfo userConfig;

        public ApplicationPreferences(Locales locales, IRuntime runtime)
        {
            this.locales = locales;
            Runtime.Current = runtime;

            // store in Packaged cache folder (to ensure clearing after uninstall)
            // store in roaming app data, if not packaged
            var configDirectory = runtime.Packaged
                ? ApplicationData.Current.LocalCacheFolder.Path
                : Path.Combine(EnvironmentInfo.AppDataPath, runtime.DataFolderName);

            userConfig = new(Path.Combine(configDirectory, $"{runtime.ProductName}.user.config"));

            System.setProperty("jna.boot.library.path", runtime.Location);
        }

        public override List applicationLocales() => locales.applicationLocales();

        public override void deleteProperty(string property)
        {
            preferences.AddOrUpdate(property, Preference.Empty, static (_, preference) => preference with { Runtime = null });
        }

        public override string getDefault(string property) => preferences.TryGetValue(property, out var value) ? value.Default : null;

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

            CultureInfo cultureInfo = CultureInfo.GetCultureInfo(locale);
            return cultureInfo.TextInfo.ToTitleCase(cultureInfo.NativeName);
        }

        public override string getProperty(string property)
        {
            if (!preferences.TryGetValue(property, out var settings))
            {
                return null;
            }
            if (!string.IsNullOrWhiteSpace(settings.Runtime))
            {
                return settings.Runtime;
            }
            return settings.Default;
        }

        public override void load() => Load();

        public void Load()
        {
            if (userConfig.Exists)
            {
                try
                {
                    MigrateUserConfig();
                }
                catch
                {
                    // Silently skip failures in migrating the user.config.
                }
            }

            try
            {
                ParseUserConfig();
            }
            catch (Exception ex)
            {
                Log.error("Failure loading preferences", ex);
            }
        }

        public override string locale()
        {
            return getProperty("application.language");
        }

        public override void save() => Save();

        public void Save()
        {
            try
            {
                userConfig.Directory.Create();
            }
            catch (Exception error)
            {
                Log.error($"Failure creating user.config directory {userConfig.Directory.FullName}", error);
            }
            try
            {
                using var stream = userConfig.Open(FileMode.Create, FileAccess.Write, FileShare.Read);
                using StreamWriter writer = new(stream, Encoding.UTF8);
                foreach (var item in preferences)
                {
                    var value = item.Value.Runtime;
                    if (string.IsNullOrWhiteSpace(value))
                    {
                        continue;
                    }

                    if (item.Key.IndexOf('=') != -1)
                    {
                        writer.WriteLine($"\"{item.Key}\"={value}");
                    }
                    else
                    {
                        writer.WriteLine($"{item.Key}={value}");
                    }
                }
            }
            catch (Exception ex)
            {
                Log.error("Failure saving preferences", ex);
            }
        }

        public override void setDefault(string property, string value)
        {
            preferences.AddOrUpdate(property, (_, @default) => new(@default, default), (_, preference, @default) => preference with { Default = @default }, value);
        }

        public override void setProperty(string property, string value)
        {
            preferences.AddOrUpdate(property, (_, value) => new(default, value), (_, preference, value) => preference with { Runtime = value }, value);
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

        private static StringDictionary LoadUserConfig()
        {
            var settingsInstance = Settings.Default;
            if (settingsInstance.UpgradeSettings)
            {
                settingsInstance.Upgrade();
                settingsInstance.UpgradeSettings = false;

                try
                {
                    settingsInstance.Save();
                }
                catch
                {
                    // Don't care about failures saving old user config.
                }
            }

            return settingsInstance.CdSettings;
        }

        private void MigrateUserConfig()
        {
            StringDictionary userConfig;
            try
            {
                userConfig = LoadUserConfig();
            }
            catch
            {
                return;
            }

            if (userConfig is null or { Count: 0 })
            {
                return;
            }

            foreach (DictionaryEntry item in userConfig)
            {
                setProperty((string)item.Key, (string)item.Value);
            }
        }

        private void ParseUserConfig()
        {
            if (!userConfig.Exists)
            {
                return;
            }

            using var stream = userConfig.OpenRead();
            using StreamReader reader = new(stream);

            while (!reader.EndOfStream)
            {
                string line;
                try
                {
                    line = reader.ReadLine();
                }
                catch
                {
                    continue;
                }

                if (string.IsNullOrWhiteSpace(line))
                {
                    continue;
                }

                if (line[0] is ';' or '#' or '/')
                {
                    continue;
                }

                int offset = 0;
                var hay = line.AsSpan().Trim();
                ReadOnlySpan<char> key = default;
                int search;
                while ((search = hay.Slice(offset).IndexOf('=')) != -1)
                {
                    key = hay.Slice(0, offset + search).Trim();
                    if (key[0] != '"' || key.Length > 1 && key[key.Length - 2] == '"')
                    {
                        break;
                    }
                    offset += search + 1;
                }
                if (search == -1)
                {
                    continue;
                }
                // strip quotation marks from key
                if (key[0] == '"' && key.Length > 1 && key[key.Length - 1] == '"')
                {
                    key = key.Slice(1, key.Length - 2);
                }
                var value = hay.Slice(offset + search + 1).Trim();
                // strip quotation marks from value
                if (value[0] == '"' && value.Length > 1 && value[value.Length - 1] == '"')
                {
                    value = value.Slice(1, value.Length - 2);
                }
                setProperty(key.ToString(), value.ToString());
            }
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

        [MethodImpl(MethodImplOptions.NoInlining)]
        private void SetUWPDefaults()
        {
            this.setDefault("update.check", $"{false}");
            this.setDefault("tmp.dir", ApplicationData.Current.TemporaryFolder.Path);
        }

        record struct Preference(string Default, string Runtime)
        {
            public static readonly Preference Empty = default;
        }
    }
}
