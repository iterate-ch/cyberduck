//
// Copyright (c) 2010-2018 Yves Langisch. All rights reserved.
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

using Ch.Cyberduck.Properties;
using java.security;
using java.util;
using sun.security.mscapi;
using System;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using Windows.Storage;
using StringUtils = org.apache.commons.lang3.StringUtils;
using ch.cyberduck.core.preferences;
using java.nio.charset;
using org.apache.logging.log4j;
using org.apache.logging.log4j.core;
using org.apache.logging.log4j.core.config;
using Logger = org.apache.logging.log4j.Logger;

namespace Ch.Cyberduck.Core.Preferences
{
    public class SettingsDictionaryPreferences : AppConfigPreferences
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SettingsDictionaryPreferences).FullName);

        public SettingsDictionaryPreferences() : base(new DefaultLocales())
        {
        }

        /// <summary>
        /// Get platform specific home directory
        /// </summary>
        public static string HomeFolder
        {
            get
            {
                return (Environment.OSVersion.Platform == PlatformID.Unix ||
                        Environment.OSVersion.Platform == PlatformID.MacOSX)
                    ? Environment.GetEnvironmentVariable("HOME")
                    : Environment.GetEnvironmentVariable("USERPROFILE");
            }
        }

        private static string ApplicationRevision
        {
            get { return Assembly.GetExecutingAssembly().GetName().Version.Revision.ToString(); }
        }

        private static string ApplicationVersion
        {
            get
            {
                Match match = Regex.Match(Application.ProductVersion, @"((\d+)\.(\d+)(\.(\d+))?).*");
                return match.Groups[1].Value;
            }
        }

        /// <summary>
        /// Try to get an OS version specific download path:
        /// - XP : Desktop
        /// - Vista or later : Downloads folder in the user home directory
        /// </summary>
        private string DefaultDownloadPath
        {
            get
            {
                string homePath = HomeFolder;
                if (!string.IsNullOrEmpty(homePath))
                {
                    string downloads = Path.Combine(homePath, "Downloads");
                    if (Directory.Exists(downloads))
                    {
                        return downloads;
                    }
                }
                // fallback is Desktop
                return Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            }
        }

        public string GetDefaultLanguage()
        {
            List sysLocales = systemLocales();
            List appLocales = applicationLocales();
            for (int i = 0; i < sysLocales.size(); i++)
            {
                string s = (string)sysLocales.get(i);
                string match = TryToMatchLocale(s.Replace('-', '_'), appLocales);
                if (null != match)
                {
                    Log.debug(String.Format("Default locale is '{0}' for system locale '{1}'", match, s));
                    return match;
                }
            }
            //default to english
            Log.debug("Fallback to locale 'en'");
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

        public object GetSpecialObject(string property)
        {
            return Settings.Default[property];
        }

        public override string locale()
        {
            return getProperty("application.language");
        }

        protected override void configureLogging(String level)
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

            this.setDefault("application.name", Application.ProductName);
            this.setDefault("application.datafolder.name", Application.ProductName);
            this.setDefault("oauth.handler.scheme",
                String.Format("x-{0}-action", StringUtils.deleteWhitespace(Application.ProductName.ToLower())));

            this.setDefault("application.version", ApplicationVersion);
            this.setDefault("application.revision", ApplicationRevision);
            this.setDefault("application.language.custom", false.ToString());
            this.setDefault("application.localization.enable", true.ToString());

            this.setDefault("editor.bundleIdentifier", "shell:openfilewith");

            this.setDefault("update.feed.release", "https://version.cyberduck.io/windows/changelog.rss");
            this.setDefault("update.feed.beta", "https://version.cyberduck.io/windows/beta/changelog.rss");
            this.setDefault("update.feed.nightly", "https://version.cyberduck.io/windows/nightly/changelog.rss");

            // Importers
            this.setDefault("bookmark.import.winscp.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "WinSCP.ini"));
            this.setDefault("bookmark.import.filezilla.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "FileZilla",
                    "sitemanager.xml"));
            this.setDefault("bookmark.import.smartftp.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "SmartFTP",
                    "Client 2.0", "Favorites"));
            this.setDefault("bookmark.import.totalcommander.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "GHISLER",
                    "wcx_ftp.ini"));
            this.setDefault("bookmark.import.flashfxp3.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "FlashFXP", "3",
                    "Sites.dat"));
            this.setDefault("bookmark.import.flashfxp4.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "FlashFXP", "4",
                    "Sites.dat"));
            this.setDefault("bookmark.import.flashfxp4.common.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData), "FlashFXP",
                    "4",
                    "Sites.dat"));
            this.setDefault("bookmark.import.wsftp.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Ipswitch", "WS_FTP",
                    "Sites"));
            this.setDefault("bookmark.import.fireftp.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Mozilla", "Firefox",
                    "Profiles"));
            this.setDefault("bookmark.import.s3browser.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "S3Browser",
                    "settings.ini"));
            this.setDefault("bookmark.import.crossftp.location", Path.Combine(HomeFolder, ".crossftp", "sites.xml"));
            this.setDefault("bookmark.import.cloudberry.s3.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "CloudBerry S3 Explorer for Amazon S3", "settings.list"));
            this.setDefault("bookmark.import.cloudberry.google.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "CloudBerry Explorer for Google Storage", "settings.list"));
            this.setDefault("bookmark.import.cloudberry.azure.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "CloudBerry Explorer for Azure Blob Storage", "settings.list"));
            this.setDefault("bookmark.import.expandrive3.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "ExpanDrive", "favorites.js"));
            this.setDefault("bookmark.import.expandrive4.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "ExpanDrive", "expandrive4.favorites.js"));
            this.setDefault("bookmark.import.expandrive5.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "ExpanDrive", "expandrive5.favorites.js"));
            this.setDefault("bookmark.import.expandrive6.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "ExpanDrive", "expandrive6.favorites.js"));
            this.setDefault("bookmark.import.netdrive2.location",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData),
                    "NetDrive2", "drives.dat"));

            //disable reminder for protocol handler registration
            this.setDefault("defaulthandler.reminder", false.ToString());

            this.setDefault("update.check.privilege", true.ToString());

            this.setDefault("queue.download.folder", DefaultDownloadPath);
            this.setDefault("queue.upload.permissions.default", true.ToString());

            this.setDefault("queue.dock.badge", true.ToString());

            this.setDefault("ssh.knownhosts",
                Path.Combine(new RoamingSupportDirectoryFinder().find().getAbsolute(), "known_hosts"));
            this.setDefault("browser.enterkey.rename", false.ToString());
            this.setDefault("terminal.openssh.enable", true.ToString());
            this.setDefault("terminal.windowssubsystemlinux.enable", true.ToString());
            this.setDefault("terminal.command.ssh", Path.Combine(HomeFolder, "putty.exe"));
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

            this.setDefault("local.user.home", HomeFolder);
            this.setDefault("local.delimiter", "\\");
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
                catch (PlatformNotSupportedException e)
                {
                    // The operating system does not support retrieving the network domain name.
                }
                catch (InvalidOperationException e)
                {
                    // The network domain name cannot be retrieved.
                }
                try
                {
                    this.setDefault("webdav.ntlm.workstation", Environment.MachineName);
                }
                catch (InvalidOperationException e)
                {
                    // The name of this computer cannot be obtained.
                }
            }
            if (Utils.IsRunningAsUWP)
            {
                SetUWPDefaults();
            }
        }

        [MethodImpl(MethodImplOptions.NoInlining)]
        private void SetUWPDefaults()
        {
            this.setDefault("update.check", $"{false}");
            this.setDefault("tmp.dir", ApplicationData.Current.TemporaryFolder.Path);
        }

        private string TryToMatchLocale(string sysLocale, List appLocales)
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
                int m = sysLocale.IndexOf('_');
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
    }
}
