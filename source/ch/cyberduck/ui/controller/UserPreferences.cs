// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 

using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Properties;
using ch.cyberduck.core;
using java.util;
using org.apache.log4j;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class UserPreferences : Preferences
    {
        private static readonly Logger Log = Logger.getLogger(typeof (UserPreferences).FullName);
        private SettingsDictionary _settings;

        /// <summary>
        /// Roaming application data path
        /// </summary>
        private static string RoamingApplicationDataPath
        {
            get
            {
                return Path.Combine(Environment.GetFolderPath(
                    Environment.SpecialFolder.ApplicationData),
                                    instance().getProperty("application.name"));
            }
        }

        /// <summary>
        /// Local application data path
        /// </summary>
        private static string LocalApplicationDataPath
        {
            get
            {
                return Path.Combine(Environment.GetFolderPath(
                    Environment.SpecialFolder.LocalApplicationData),
                                    instance().getProperty("application.name"));
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
            get { return Application.ProductVersion; }
        }

        public override void setProperty(string property, string value)
        {
            Log.info("setProperty: " + property + "," + value);
            _settings[property] = value;
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
            Assembly asm = Assembly.GetExecutingAssembly();
            string[] names = asm.GetManifestResourceNames();
            // the dots apparently come from the relative path in the msbuild file
            Regex regex = new Regex("Ch.Cyberduck\\.\\.\\.\\.\\.\\.\\.\\.\\.\\.([^\\..]*).lproj\\.Localizable\\.strings");
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

        protected override void load()
        {
            Log.debug("Loading preferences");
            // upgrade settings for a new version
            if (Settings.Default.UpgradeSettings)
            {
                Settings.Default.Upgrade();
                Settings.Default.UpgradeSettings = false;
            }
            _settings = Settings.Default.CdSettings ?? new SettingsDictionary();
        }

        protected override void setLogging()
        {
            defaults.put("logging.config", "log4j-windows.xml");

            base.setLogging();
        }

        protected override void setDefaults()
        {
            defaults.put("application.name", Application.ProductName);
            defaults.put("application.version", ApplicationVersion);
            defaults.put("application.revision", ApplicationRevision);
            defaults.put("application.language", GetDefaultLanguage());
            defaults.put("application.language.custom", false.ToString());
            defaults.put("application.profiles.path", "profiles"); // relative to .exe
            defaults.put("application.bookmarks.path", "bookmarks"); // relative to .exe

            defaults.put("update.feed.release", "https://version.cyberduck.io/changelog.wys");
            defaults.put("update.feed.beta", "https://version.cyberduck.io/beta/changelog.wys");
            defaults.put("update.feed.nightly", "https://version.cyberduck.io/nightly/changelog.wys");

            defaults.put("update.feed", "release");

            // Importers
            defaults.put("bookmark.import.winscp.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.Programs), "WinSCP", "winscp.ini"));
            defaults.put("bookmark.import.filezilla.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FileZilla", "sitemanager.xml"));
            defaults.put("bookmark.import.smartftp.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "SmartFTP", "Client 2.0", "Favorites"));
            defaults.put("bookmark.import.totalcommander.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "GHISLER", "wcx_ftp.ini"));
            defaults.put("bookmark.import.flashfxp3.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FlashFXP", "3", "Sites.dat"));
            defaults.put("bookmark.import.flashfxp4.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FlashFXP", "4", "Sites.dat"));
            defaults.put("bookmark.import.flashfxp4.common.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.CommonApplicationData), "FlashFXP", "4", "Sites.dat"));
            defaults.put("bookmark.import.wsftp.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "Ipswitch", "WS_FTP", "Sites"));
            defaults.put("bookmark.import.fireftp.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "Mozilla", "Firefox", "Profiles"));
            defaults.put("bookmark.import.s3browser.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "S3Browser", "settings.ini"));
            defaults.put("bookmark.import.crossftp.location", Path.Combine(HomeFolder, ".crossftp", "sites.xml"));
            defaults.put("bookmark.import.cloudberry.s3.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "CloudBerry S3 Explorer for Amazon S3", "settings.list"));
            defaults.put("bookmark.import.cloudberry.google.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "CloudBerry Explorer for Google Storage",
                                                                                    "settings.list"));
            defaults.put("bookmark.import.cloudberry.azure.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "CloudBerry Explorer for Azure Blob Storage",
                                                                                   "settings.list"));

            defaults.put("logging.config", "log4j-windows.xml");

            base.setDefaults();

            //disable reminder for protocol handler registration
            defaults.put("defaulthandler.reminder", false.ToString());
            defaults.put("application.support.path", RoamingApplicationDataPath);
            defaults.put("application.receipt.path", RoamingApplicationDataPath);            

            defaults.put("update.check.last", "0");

            defaults.put("queue.download.folder", DefaultDownloadPath);
            defaults.put("queue.upload.permissions.default", true.ToString());
            defaults.put("queue.upload.permissions.change", true.ToString());

            defaults.put("queue.dock.badge", true.ToString());

            defaults.put("ssh.knownhosts", Path.Combine(RoamingApplicationDataPath, "known_hosts"));
            defaults.put("browser.enterkey.rename", false.ToString());
            defaults.put("terminal.command.ssh", Path.Combine(HomeFolder, "putty.exe"));
            defaults.put("terminal.command.ssh.args", "-ssh {0} {1}@{2} -t -P {3} -m \"{4}\"");

            defaults.put("editor.bundleIdentifier", new SystemWatchEditorFactory.Notepad().getIdentifier());

            defaults.put("notifications.timeout.milliseconds", "300");

            //default browser toolbar set
            defaults.put("browser.toolbar", true.ToString());
            defaults.put("browser.toolbar.openconnection", true.ToString());
            defaults.put("browser.toolbar.quickconnect", true.ToString());
            defaults.put("browser.toolbar.action", true.ToString());
            defaults.put("browser.toolbar.info", true.ToString());
            defaults.put("browser.toolbar.refresh", true.ToString());
            defaults.put("browser.toolbar.edit", true.ToString());
            defaults.put("browser.toolbar.openinbrowser", false.ToString());
            defaults.put("browser.toolbar.openinterminal", false.ToString());
            defaults.put("browser.toolbar.newfolder", false.ToString());
            defaults.put("browser.toolbar.delete", false.ToString());
            defaults.put("browser.toolbar.download", false.ToString());
            defaults.put("browser.toolbar.upload", true.ToString());
            defaults.put("browser.toolbar.transfers", true.ToString());

            //default transfer toolbar set
            defaults.put("transfer.toolbar.resume", true.ToString());
            defaults.put("transfer.toolbar.reload", true.ToString());
            defaults.put("transfer.toolbar.stop", true.ToString());
            defaults.put("transfer.toolbar.remove", true.ToString());
            defaults.put("transfer.toolbar.cleanup", false.ToString());
            defaults.put("transfer.toolbar.log", false.ToString());
            defaults.put("transfer.toolbar.open", true.ToString());
            defaults.put("transfer.toolbar.show", true.ToString());

            // Resolve symbolic links downloading target file instead. Cannot create symbolic links on FAT.
            defaults.put("path.symboliclink.resolve", true.ToString());
            // Resolve local links uploading target file instead. Currently not supporting shortcuts on Windows.
            defaults.put("local.symboliclink.resolve", true.ToString());

            defaults.put("local.user.home", HomeFolder);
            defaults.put("local.delimiter", "\\");
            defaults.put("local.normalize.tilde", String.valueOf(false));

            // SSL Keystore
            defaults.put("connection.ssl.keystore.type", "Windows-MY");
            defaults.put("connection.ssl.keystore.provider", "SunMSCAPI");
        }

        protected override void setFactories()
        {
            base.setFactories();

            defaults.put("factory.local.class", typeof(Ch.Cyberduck.Core.Local.SystemLocal).AssemblyQualifiedName);
            defaults.put("factory.locale.class", typeof(Ch.Cyberduck.Core.I18n.DictionaryLocale).AssemblyQualifiedName);
            defaults.put("factory.dateformatter.class", typeof(Ch.Cyberduck.Ui.Winforms.UserDefaultsDateFormatter).AssemblyQualifiedName);
            defaults.put("factory.passwordstore.class", typeof(Ch.Cyberduck.Core.Keychain).AssemblyQualifiedName);
            defaults.put("factory.certificatestore.class", typeof(Ch.Cyberduck.Core.Keychain).AssemblyQualifiedName);
            defaults.put("factory.hostkeycallback.class", typeof(Ch.Cyberduck.Ui.Controller.HostKeyController).AssemblyQualifiedName);
            defaults.put("factory.logincallback.class", typeof(Ch.Cyberduck.Ui.Controller.PromptLoginController).AssemblyQualifiedName);
            defaults.put("factory.transfererrorcallback.class", typeof(Ch.Cyberduck.Ui.Winforms.Threading.DialogTransferErrorCallback).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.download.class", typeof(Ch.Cyberduck.Ui.Controller.DownloadPromptController).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.upload.class", typeof(Ch.Cyberduck.Ui.Controller.UploadPromptController).AssemblyQualifiedName);
            defaults.put("factory.transferpromptcallback.sync.class", typeof(Ch.Cyberduck.Ui.Controller.SyncPromptController).AssemblyQualifiedName);
            defaults.put("factory.proxy.class", typeof(Ch.Cyberduck.Core.SystemProxy).AssemblyQualifiedName);
            defaults.put("factory.reachability.class", typeof(Ch.Cyberduck.Core.TcpReachability).AssemblyQualifiedName);
            defaults.put("factory.rendezvous.class", typeof(Ch.Cyberduck.Core.Rendezvous).AssemblyQualifiedName);

            defaults.put("factory.serializer.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.PlistSerializer).AssemblyQualifiedName);
            defaults.put("factory.deserializer.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.PlistDeserializer).AssemblyQualifiedName);
            defaults.put("factory.reader.profile.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.ProfilePlistReader).AssemblyQualifiedName);
            defaults.put("factory.writer.profile.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.PlistWriter).AssemblyQualifiedName);
            defaults.put("factory.reader.transfer.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.TransferPlistReader).AssemblyQualifiedName);
            defaults.put("factory.writer.transfer.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.PlistWriter).AssemblyQualifiedName);
            defaults.put("factory.reader.host.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.HostPlistReader).AssemblyQualifiedName);
            defaults.put("factory.writer.host.class", typeof(Ch.Cyberduck.Core.Serializer.Impl.PlistWriter).AssemblyQualifiedName);

            defaults.put("factory.applicationfinder.class", typeof(Ch.Cyberduck.Core.Editor.RegistryApplicationFinder).AssemblyQualifiedName);
            defaults.put("factory.applicationlauncher.class", typeof(Ch.Cyberduck.Core.Local.WindowsApplicationLauncher).AssemblyQualifiedName);
            defaults.put("factory.temporaryfiles.class", typeof(Ch.Cyberduck.Core.Local.WindowsTemporaryFileService).AssemblyQualifiedName);
            defaults.put("factory.browserlauncher.class", typeof(Ch.Cyberduck.Core.Local.DefaultBrowserLauncher).AssemblyQualifiedName);
            defaults.put("factory.reveal.class", typeof(Ch.Cyberduck.Core.Local.ExplorerRevealService).AssemblyQualifiedName);
            defaults.put("factory.trash.class", typeof(Ch.Cyberduck.Core.Local.RecycleLocalTrashFeature).AssemblyQualifiedName);
            defaults.put("factory.symlink.class", typeof(ch.cyberduck.core.local.NullLocalSymlinkFeature).AssemblyQualifiedName);
            defaults.put("factory.terminalservice.class", typeof(Ch.Cyberduck.Core.SshTerminalService).AssemblyQualifiedName);
            defaults.put("factory.editorfactory.class", typeof(Ch.Cyberduck.Core.Editor.SystemWatchEditorFactory).AssemblyQualifiedName);
            defaults.put("factory.licensefactory.class", typeof(Ch.Cyberduck.Core.Aquaticprime.WindowsLicenseFactory).AssemblyQualifiedName);
            defaults.put("factory.notification.class", typeof(Ch.Cyberduck.Ui.Growl.ToolstripNotificationService).AssemblyQualifiedName);
            if (Utils.IsWin7OrLater)
            {
                defaults.put("factory.badgelabeler.class", typeof(Ch.Cyberduck.Core.Local.TaskbarApplicationBadgeLabeler).AssemblyQualifiedName);
            }
            defaults.put("factory.filedescriptor.class", typeof(Ch.Cyberduck.Core.Local.Win32FileDescriptor).AssemblyQualifiedName);
            defaults.put("factory.pathreference.class", typeof(ch.cyberduck.core.DefaultPathReference).AssemblyQualifiedName);
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