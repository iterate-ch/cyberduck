// 
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
using System.IO;
using System.Reflection;
using System.Text.RegularExpressions;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LocaleImpl : Locale
    {
        private static readonly Logger Log = Logger.getLogger(typeof (LocaleImpl).FullName);
        private static readonly Regex StringsRegex = new Regex("\"?(.*?)\"?[ ]*=[ ]*\"(.*)\"", RegexOptions.Compiled);

        private readonly IDictionary<string, Dictionary<string, string>> _cache =
            new Dictionary<string, Dictionary<string, string>>();

        private readonly string _language = Preferences.instance().getProperty("application.language");

        private void ReadBundleIntoCache(string bundle)
        {
            Log.debug("Caching bundle " + bundle);
            Assembly asm = Assembly.GetExecutingAssembly();
            // the dots apparently come from the relative path in the msbuild file
            Stream stream = asm.GetManifestResourceStream(
                string.Format("Ch.Cyberduck..........{0}.lproj.{1}.strings", _language, bundle));
            if (null == stream)
            {
                stream =
                    asm.GetManifestResourceStream(
                        string.Format(
                            "Ch.Cyberduck..........lib.Sparkle.framework.Versions.A.Resources.{0}.lproj.Sparkle.strings",
                            _language));
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
                Log.error(String.Format("Bundle {0} not found", bundle));
            }
        }

        public override string get(string key, string table)
        {
            Dictionary<string, string> bundle;

            if (!_cache.TryGetValue(table, out bundle))
            {
                ReadBundleIntoCache(table);
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

        public static void Register()
        {
            LocaleFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : LocaleFactory
        {
            protected override object create()
            {
                return new LocaleImpl();
            }
        }
    }
}