// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.preferences;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.I18n
{
    public class DictionaryLocale : Locale
    {
        private static readonly Logger Log = Logger.getLogger(typeof (DictionaryLocale).FullName);
        private static readonly Regex StringsRegex = new Regex("\"?(.*?)\"?[ ]*=[ ]*\"(.*)\"", RegexOptions.Compiled);

        private readonly IDictionary<string, Dictionary<string, string>> _cache =
            new Dictionary<string, Dictionary<string, string>>();

        private readonly bool _enabled = PreferencesFactory.get().getBoolean("application.localization.enable");

        public string localize(string key, string table)
        {
            if (!_enabled)
            {
                return key;
            }
            Dictionary<string, string> bundle;
            if (!_cache.TryGetValue(table, out bundle))
            {
                lock (this)
                {
                    load(table);
                }
                //try again
                if (!_cache.TryGetValue(table, out bundle))
                {
                    Log.warn(string.Format("Key '{0}' in bundle '{1}' not found", key, table));
                    return key;
                }
            }

            string value;
            return bundle.TryGetValue(key, out value) ? value : key;
        }

        public void setDefault(string language)
        {
            PreferencesFactory.get().setProperty("application.language", language);
        }

        private void load(string bundle)
        {
            Log.debug("Caching bundle " + bundle);
            string language = PreferencesFactory.get().getProperty("application.language");
            Assembly asm = Utils.Me();
            string[] resourceNames = Assembly.GetExecutingAssembly().GetManifestResourceNames();
            Stream stream = null;
            foreach (string resourceName in resourceNames)
            {
                if (resourceName.Contains(string.Format("{0}.lproj.{1}.strings", language, bundle)))
                {
                    stream = asm.GetManifestResourceStream(resourceName + ".1");
                    if (stream == null)
                    {
                        stream = asm.GetManifestResourceStream(resourceName);
                    }
                    break;
                }
            }
            if (null != stream)
            {
                using (StreamReader file = new StreamReader(stream))
                {
                    Dictionary<string, string> bundleDict = new Dictionary<string, string>();
                    _cache[bundle] = bundleDict;
                    string line;
                    while ((line = file.ReadLine()) != null)
                    {
                        if (StringsRegex.IsMatch(line))
                        {
                            Match match = StringsRegex.Match(line);
                            string key = match.Groups[1].Value;
                            string value = match.Groups[2].Value;
                            bundleDict[key] = value;
                        }
                    }
                }
            }
            else
            {
                Log.warn(String.Format("Bundle {0} for language {1} not found", bundle, language));
            }
        }
    }
}