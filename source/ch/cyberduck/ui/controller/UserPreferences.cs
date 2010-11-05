﻿﻿//
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Properties;
using java.util;
using org.apache.log4j;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class UserPreferences : Preferences
    {
        private static readonly Logger Log = Logger.getLogger(typeof (UserPreferences).Name);
        private SettingsDictionary _settings;

        private UserPreferences()
        {
            ;
        }

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
                string homePath = (Environment.OSVersion.Platform == PlatformID.Unix ||
                                   Environment.OSVersion.Platform == PlatformID.MacOSX)
                                      ? Environment.GetEnvironmentVariable("HOME")
                                      : Environment.GetEnvironmentVariable("USERPROFILE");
                return homePath;
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

        public override string getDefault(string property)
        {
            if (_settings.ContainsKey(property))
            {
                return _settings[property];
            }
            return (string) defaults.get(property);
        }

        public override string getDisplayName(string locale)
        {
            //cy is a special case as it is not available in the framework
            if ("cy".Equals(locale))
            {
                return "Welsh";
            }

            //new Locale(...) seems to be very expensive (>100ms on my machine)
            CultureInfo cultureInfo = CultureInfo.GetCultureInfo(locale.Replace('_', '-'));            
            return cultureInfo.TextInfo.ToTitleCase(cultureInfo.NativeName);
        }

        public override List applicationLocales()
        {            
            Assembly asm = Assembly.GetExecutingAssembly();
            string[] names = asm.GetManifestResourceNames();
            // the dots apparently come from the relative path in the msbuild file
            Regex regex = new Regex("Ch.Cyberduck..........([^\\..]*).lproj.*"); //exclude Sparkle
            List<string> distinctNames = new List<string>();
            foreach (var name in names)
            {
                Match match = regex.Match(name);
                if (match.Groups.Count > 1)
                {
                    string cand = match.Groups[1].Value.Replace('_', '-');
                    if (!distinctNames.Contains(cand))
                    {                        
                        if (("ja".Equals(cand) ||
                            "ko".Equals(cand) ||
                            "ka".Equals(cand) ||
                            "zh-CN".Equals(cand) ||
                            "zh-TW".Equals(cand)) && !this.HasEastAsianFontSupport())
                        {
                            continue;
                        }
                        distinctNames.Add(cand);
                    }
                }
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

        public override List getList(string str)
        {
            throw new InvalidOperationException();
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
            List sysLocales = new ArrayList();

            //add current UI culture
            sysLocales.add(CultureInfo.CurrentUICulture.Name);
            //add current system culture
            sysLocales.add(Application.CurrentCulture.Name);

            return sysLocales;
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

        protected override void setDefaults()
        {
            defaults.put("application.name", Application.ProductName);
            defaults.put("application.version", ApplicationVersion);
            defaults.put("application.revision", ApplicationRevision);
            defaults.put("application.language", GetDefaultLanguage());
            defaults.put("application.language.custom", false.ToString());

            // Importers
            defaults.put("bookmark.import.filezilla.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FileZilla", "sitemanager.xml"));
            defaults.put("bookmark.import.smartftp.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "SmartFTP", "Client 2.0", "Favorites"));
            defaults.put("bookmark.import.flashfxp3.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FlashFXP", "3", "Sites.dat"));
            defaults.put("bookmark.import.flashfxp4.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.ApplicationData), "FlashFXP", "4", "Sites.dat"));
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
                Environment.SpecialFolder.LocalApplicationData), "CloudBerry Explorer for Google Storage", "settings.list"));
            defaults.put("bookmark.import.cloudberry.azure.location", Path.Combine(Environment.GetFolderPath(
                Environment.SpecialFolder.LocalApplicationData), "CloudBerry Explorer for Azure Blob Storage", "settings.list"));

            base.setDefaults();

            defaults.put("logging", "debug");
            defaults.put("rendezvous.enable", false.ToString());

            defaults.put("protocol.azure.tls.enable", true.ToString());
            defaults.put("application.support.path", RoamingApplicationDataPath);
            defaults.put("update.check.last", "0");

            defaults.put("queue.download.folder", DefaultDownloadPath);
            defaults.put("queue.upload.permissions.useDefault", true.ToString());
            defaults.put("queue.upload.changePermissions", true.ToString());

            defaults.put("queue.dock.badge", true.ToString());

            defaults.put("editor.file.trash", false.ToString());

            defaults.put("connection.host.max", "-1");
            defaults.put("ssh.knownhosts", Path.Combine(RoamingApplicationDataPath, "known_hosts"));

            //default browser toolbar set
            defaults.put("browser.toolbar", true.ToString());
            defaults.put("browser.toolbar.openconnection", true.ToString());
            defaults.put("browser.toolbar.quickconnect", true.ToString());
            defaults.put("browser.toolbar.action", true.ToString());
            defaults.put("browser.toolbar.info", true.ToString());
            defaults.put("browser.toolbar.refresh", true.ToString());
            defaults.put("browser.toolbar.edit", true.ToString());
            defaults.put("browser.toolbar.openinbrowser", false.ToString());
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
        }

        public string GetDefaultLanguage()
        {
            List sysLocales = systemLocales();
            List appLocales = applicationLocales();

            for (int i = 0; i < sysLocales.size(); i++)
            {
                string s = (string) sysLocales.get(i);
                string match = TryToMatchLocale(s, appLocales);
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
                int m = sysLocale.IndexOf('-');
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

        public static void Register()
        {
            PreferencesFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : PreferencesFactory
        {
            protected override object create()
            {
                return new UserPreferences();
            }
        }
    }
}