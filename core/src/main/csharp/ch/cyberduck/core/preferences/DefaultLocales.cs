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

using ch.cyberduck.core.i18n;
using java.util;
using System.Collections.Generic;
using System.Globalization;
using System.Resources;
using System.Windows.Forms;

namespace Ch.Cyberduck.Core.Preferences
{
    public class DefaultLocales : Locales
    {
        private static ResourceManager resources;

        static DefaultLocales()
        {
            resources = new ResourceManager("i18n", typeof(DefaultLocales).Assembly);
        }

        public List applicationLocales()
        {
            var list = new List<string>(resources.GetString("Locales").Split(' '));
            if (!HasEastAsianFontSupport())
            {
                list.Remove("ja");
                list.Remove("ko");
                list.Remove("ka");
                list.Remove("zh_CN");
                list.Remove("zh_TW");
            }
            return Utils.ConvertToJavaList(list);
        }

        public List systemLocales()
        {
            List locales = new ArrayList();
            //add current UI culture
            locales.add(CultureInfo.CurrentUICulture.Name);
            //add current system culture
            locales.add(Application.CurrentCulture.Name);
            return locales;
        }

        private bool HasEastAsianFontSupport()
        {
            return true;
        }
    }
}
