// 
// Copyright (c) 2010-2022 Yves Langisch. All rights reserved.
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

// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using System;
using System.Resources;
using ch.cyberduck.core.i18n;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Core.I18n
{
    using ch.cyberduck.core.preferences;

    public class DictionaryLocale : Locale
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(DictionaryLocale).FullName);

        private readonly bool _enabled;
        private readonly Preferences preferences;
        private readonly Lazy<ResourceManager> resourceManager;

        public DictionaryLocale()
        {
            preferences = PreferencesFactory.get();
            _enabled = preferences.getBoolean("application.localization.enable");
            resourceManager = new Lazy<ResourceManager>(() =>
                new ResourceManager("i18n." + preferences.getProperty("application.language"),
                    typeof(DictionaryLocale).Assembly));
        }

        public string localize(string key, string table)
        {
            if (string.IsNullOrEmpty(key) || !_enabled)
            {
                return key;
            }

            var resources = resourceManager.Value;
            string value;
            try
            {
                value = resources.GetString(table + ":" + key);
            }
            catch (Exception e)
            {
                Log.warn(string.Format("Key '{0}' in bundle '{1}' not found", key, table), e);
                return key;
            }

            if (string.IsNullOrEmpty(value))
            {
                if (Log.isTraceEnabled())
                {
                    Log.trace(string.Format("Key '{0}' in bundle '{1}' not found", key, table));
                }

                return key;
            }

            return value;
        }

        public void setDefault(string language)
        {
            PreferencesFactory.get().setProperty("application.language", language);
        }
    }
}
