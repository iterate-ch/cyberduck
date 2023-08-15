// 
// Copyright (c) 2010-2019 Yves Langisch. All rights reserved.
// https://cyberduck.io/
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
using ch.cyberduck.core;
using ch.cyberduck.core.date;
using TimeZone = java.util.TimeZone;

namespace Ch.Cyberduck.Core.Date
{
    public class UserDefaultsDateFormatter : AbstractUserDateFormatter
    {
        private readonly string tz;

        public UserDefaultsDateFormatter(string tz)
        {
            this.tz = tz;
        }

        public override string getLongFormat(long millis, bool natural)
        {
            if (-1 == millis)
            {
                return LocaleFactory.localizedString("Unknown");
            }

            return GetLongFormat(ConvertJavaMillisecondsToDateTime(millis, tz));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="millis">millis Milliseconds since January 1, 1970, 00:00:00 GMT</param>
        /// <returns>A short format string or "Unknown" if there is a problem converting the time to a string</returns>
        public override string getShortFormat(long millis, bool natural)
        {
            if (-1 == millis)
            {
                return LocaleFactory.localizedString("Unknown");
            }

            return GetShortFormat(ConvertJavaMillisecondsToDateTime(millis, tz));
        }

        public override string getMediumFormat(long millis, bool natural)
        {
            return getLongFormat(millis, natural);
        }

        public static string GetShortFormat(DateTime d)
        {
            return d.ToString("G");
        }

        public static string GetLongFormat(DateTime d)
        {
            return d.ToString("F");
        }

        private static DateTime ConvertJavaMillisecondsToDateTime(long javaMS, string tz)
        {
            int offset = TimeZone.getTimeZone(tz).getOffset(javaMS);
            return ConvertJavaMillisecondsToDateTime(javaMS + offset);
        }

        public static DateTime ConvertJavaMillisecondsToDateTime(long javaMS)
        {
            DateTime utcBaseTime = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);
            try
            {
                return utcBaseTime.Add(new TimeSpan(javaMS * TimeSpan.TicksPerMillisecond));
            }
            catch (ArgumentOutOfRangeException)
            {
                return DateTime.Now;
            }
        }

        public static long ConvertDateTimeToJavaMilliseconds(DateTime dateTime)
        {
            TimeSpan timeSpan = dateTime.ToUniversalTime()
                .Subtract(new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc));
            return timeSpan.Ticks / TimeSpan.TicksPerMillisecond;
        }
    }
}
