//
// Copyright (c) 2010-2018 iterate GmbH. All rights reserved.
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

using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Properties;
using java.util;
using org.apache.commons.lang3;
using org.apache.log4j;
using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NotImplementedException = System.NotImplementedException;

namespace Ch.Cyberduck.Core.Preferences
{
    public class AppConfigPreferences : DefaultPreferences
    {
        private static readonly Logger Log = Logger.getLogger(typeof(AppConfigPreferences).FullName);
        private SettingsDictionary settings;
        private PreferenceLocales locales;

        public AppConfigPreferences(PreferenceLocales locales)
        {
            this.locales = locales;
        }

        public override List applicationLocales() => locales.applicationLocales();

        public override void deleteProperty(string property)
        {
            Log.debug("deleteProperty: " + property);
            settings.Remove(property);
            save();
        }

        public override string getProperty(string property)
        {
            if (settings.ContainsKey(property))
            {
                return settings[property];
            }
            return getDefault(property);
        }

        public override void load()
        {
            ValidatePreferences();
            if (Settings.Default.UpgradeSettings)
            {
                Settings.Default.Upgrade();
                Settings.Default.UpgradeSettings = false;
            }
            settings = Settings.Default.CdSettings ?? new SettingsDictionary();
        }

        public override void save()
        {
            Log.debug("Saving preferences");
            // re-set field to force save
            Settings.Default.CdSettings = settings;
            Settings.Default.Save();
        }

        public override void setProperty(string property, string value)
        {
            Log.info("setProperty: " + property + "," + value);
            settings[property] = value;
            save();
        }

        public override List systemLocales() => locales.systemLocales();

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
    }
}
