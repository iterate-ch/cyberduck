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

using System;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using Windows.Storage;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Properties;
using java.io;
using java.security;
using java.util;
using org.apache.log4j;
using sun.security.mscapi;
using File = System.IO.File;

namespace Ch.Cyberduck.Core.Preferences
{
    public class SettingsDictionaryPreferences : ch.cyberduck.core.preferences.Preferences
    {
        private static readonly Logger Log = Logger.getLogger(typeof(SettingsDictionaryPreferences).FullName);
        private SettingsDictionary _settings;

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

        private void ValidatePreferences()
        {
            try
            {
                ConfigurationManager.OpenExeConfiguration(ConfigurationUserLevel.PerUserRoaming);
            }
            catch (ConfigurationErrorsException ex)
            {
                if (!string.IsNullOrEmpty(ex.Filename))
                {
                    File.Move(ex.Filename, $"{ex.Filename}_{DateTime.Now:yyyyMMddHHmmssffff}");
                }
            }
        }

        public override void setProperty(string property, string value)
        {
            Log.info("setProperty: " + property + "," + value);
            _settings[property] = value;
            save();
        }

        public override string locale()
        {
            return getProperty("application.language");
        }

        public override void setProperty(string str, List l)
        {
            throw new InvalidOperationException();
        }

        public override void deleteProperty(string property)
        {
            Log.debug("deleteProperty: " + property);
            _settings.Remove(property);
            save();
        }

        public override string getProperty(string property)
        {
            if (_settings.ContainsKey(property))
            {
                return _settings[property];
            }
            return getDefault(property);
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

        public override List applicationLocales()
        {
            Assembly asm = Utils.Me();
            string[] names = asm.GetManifestResourceNames();
            // the dots apparently come from the relative path in the msbuild file
            Regex regex = new Regex("Core.*\\.([^\\..]+).lproj\\.Localizable\\.strings");
            List<string> distinctNames = new List<string>();
            foreach (var name in names)
            {
                Match match = regex.Match(name);
                if (match.Groups.Count > 1)
                {
                    distinctNames.Add(match.Groups[1].Value);
                }
            }
            if (!HasEastAsianFontSupport())
            {
                distinctNames.Remove("ja");
                distinctNames.Remove("ko");
                distinctNames.Remove("ka");
                distinctNames.Remove("zh_CN");
                distinctNames.Remove("zh_TW");
            }
            return Utils.ConvertToJavaList(distinctNames);
        }

        private bool HasEastAsianFontSupport()
        {
            if (Utils.IsVistaOrLater)
            {
                return true;
            }
            return
                Convert.ToBoolean(NativeMethods.IsValidLocale(CultureInfo.CreateSpecificCulture("zh").LCID,
                    NativeConstants.LCID_INSTALLED));
        }

        public object GetSpecialObject(string property)
        {
            return Settings.Default[property];
        }

        public override void save()
        {
            Log.debug("Saving preferences");
            // re-set field to force save
            Settings.Default.CdSettings = _settings;
            Settings.Default.Save();
        }

        public override List systemLocales()
        {
            List locales = new ArrayList();
            //add current UI culture
            locales.add(CultureInfo.CurrentUICulture.Name);
            //add current system culture
            locales.add(Application.CurrentCulture.Name);
            return locales;
        }

        public override void load()
        {
            ValidatePreferences();
            if (Settings.Default.UpgradeSettings)
            {
                Settings.Default.Upgrade();
                Settings.Default.UpgradeSettings = false;
            }
            _settings = Settings.Default.CdSettings ?? new SettingsDictionary();
        }

        protected override void setDefaults()
        {
            base.setDefaults();

            this.setDefault("application.name", Application.ProductName);
            this.setDefault("application.container.name", Application.ProductName);
            String support = SupportDirectoryFinderFactory.get().find().getAbsolute();
            this.setDefault("application.support.path", support);
            this.setDefault("application.receipt.path", support);

            this.setDefault("application.version", ApplicationVersion);
            this.setDefault("application.revision", ApplicationRevision);
            this.setDefault("application.language", GetDefaultLanguage());
            this.setDefault("application.language.custom", false.ToString());
            this.setDefault("application.localization.enable", true.ToString());

            this.setDefault("update.feed.release", "https://version.cyberduck.io/windows/changelog.rss");
            this.setDefault("update.feed.beta", "https://version.cyberduck.io/windows/beta/changelog.rss");
            this.setDefault("update.feed.nightly", "https://version.cyberduck.io/windows/nightly/changelog.rss");

            this.setDefault("update.feed", "release");

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

            this.setDefault("update.check.last", "0");
            this.setDefault("update.check.privilege", true.ToString());

            this.setDefault("queue.download.folder", DefaultDownloadPath);
            this.setDefault("queue.upload.permissions.default", true.ToString());
            this.setDefault("queue.upload.permissions.change", true.ToString());

            this.setDefault("queue.dock.badge", true.ToString());

            this.setDefault("ssh.knownhosts",
                Path.Combine(new RoamingSupportDirectoryFinder().find().getAbsolute(), "known_hosts"));
            this.setDefault("browser.enterkey.rename", false.ToString());
            this.setDefault("terminal.command.ssh", Path.Combine(HomeFolder, "putty.exe"));
            this.setDefault("terminal.command.ssh.args", "-ssh {0} {1}@{2} -t -P {3} -m \"{4}\"");

            this.setDefault("editor.bundleIdentifier", new SystemWatchEditorFactory.Notepad().getIdentifier());

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

            // SSL Keystore
            // Add mscapi security provider
            Security.addProvider(new SunMSCAPI());
            this.setDefault("connection.ssl.keystore.type", "Windows-MY");
            this.setDefault("connection.ssl.keystore.provider", "SunMSCAPI");

            // Override secure random strong algorithm. Outputs bytes from the Windows CryptGenRandom() API
            this.setDefault("connection.ssl.securerandom.algorithm", "Windows-PRNG");
            this.setDefault("connection.ssl.securerandom.provider", "SunMSCAPI");

            // Enable Integrated Windows Authentication
            this.setDefault("connection.proxy.windows.authentication.enable", true.ToString());

            this.setDefault("webdav.ntlm.environment", false.ToString());
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

        protected override void post()
        {
            base.post();
            Logger root = Logger.getRootLogger();
            var fileName = Path.Combine(getProperty("application.support.path"),
                getProperty("application.name").ToLower().Replace(" ", "") + ".log");
            RollingFileAppender appender = new RollingFileAppender(new PatternLayout(@"%d [%t] %-5p %c - %m%n"),
                fileName, true);
            appender.setEncoding("UTF-8");
            appender.setMaxFileSize("10MB");
            appender.setMaxBackupIndex(0);
            root.addAppender(appender);
            if (Debugger.IsAttached)
            {
                root.setLevel(Level.DEBUG);
            }
            ApplyGlobalConfig();
        }

        private void ApplyGlobalConfig()
        {
            var config = Path.Combine(PreferencesFactory.get().getProperty("application.support.path"),
                "default.properties");
            if (File.Exists(config))
            {
                try
                {
                    var properties = new java.util.Properties();
                    properties.load(new FileInputStream(config));
                    this.setDefaults(properties);
                }
                catch (Exception e)
                {
                    Log.warn($"Failure while reading {config}", e);
                }
            }
        }

        public string GetDefaultLanguage()
        {
            List sysLocales = systemLocales();
            List appLocales = applicationLocales();
            for (int i = 0; i < sysLocales.size(); i++)
            {
                string s = (string) sysLocales.get(i);
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

        private string TryToMatchLocale(string sysLocale, List appLocales)
        {
            for (int i = 0; i < appLocales.size(); i++)
            {
                string l = (string) appLocales.get(i);
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
