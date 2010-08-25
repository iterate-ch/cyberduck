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
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui;

namespace Ch.Cyberduck.Ui.Winforms
{
    internal class UserDefaultsDateFormatter : DateFormatter
    {
        public string getLongFormat(long milliseconds)
        {
            if (-1 == milliseconds)
            {
                return Locale.localizedString("Unknown");
            }
            return GetLongFormat(ConvertJavaMiliSecondToDateTime(milliseconds));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="milliseconds">milliseconds Milliseconds since January 1, 1970, 00:00:00 GMT</param>
        /// <returns>A short format string or "Unknown" if there is a problem converting the time to a string</returns>
        public string getShortFormat(long milliseconds)
        {
            if (-1 == milliseconds)
            {
                return Locale.localizedString("Unknown");
            }
            return GetShortFormat(ConvertJavaMiliSecondToDateTime(milliseconds));
        }

        public string getMediumFormat(long milliseconds)
        {
            return getLongFormat(milliseconds);
        }

        public static string GetShortFormat(DateTime d)
        {
            return d.ToString("G");
        }

        public static string GetLongFormat(DateTime d)
        {
            return d.ToString("F");
        }

        public static void Register()
        {
            DateFormatterFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        public static DateTime ConvertJavaMiliSecondToDateTime(long javaMS)
        {
            DateTime utcBaseTime = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            DateTime dt = utcBaseTime.Add(new TimeSpan(javaMS*
                                                       TimeSpan.TicksPerMillisecond)).ToLocalTime();
            return dt;
        }

        private class Factory : DateFormatterFactory
        {
            protected override object create()
            {
                return new UserDefaultsDateFormatter();
            }
        }
    }
}