// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using ch.cyberduck.core.formatter;
using System;
using System.Collections.Generic;
using System.Linq;
using CorePreferences = ch.cyberduck.core.preferences.Preferences;

namespace ch.cyberduck.ui.Model;

public class BandwidthProvider
{
    private static readonly WeakReference<SizeFormatter> formatter = new(null);

    public static SizeFormatter Formatter
    {
        get
        {
            SizeFormatter instance;
            lock (formatter)
            {
                if (!formatter.TryGetTarget(out instance))
                {
                    formatter.SetTarget(instance = SizeFormatterFactory.get(true));
                }
            }

            return instance;
        }
    }

    public IReadOnlyList<BandwidthModel> Bandwidth { get; }

    public BandwidthProvider(CorePreferences preferences)
    {
        Bandwidth = preferences.getProperty("queue.bandwidth.options")
            .Split([','], StringSplitOptions.RemoveEmptyEntries)
            .Select(int.Parse)
            .Select(v => new BandwidthModel(v, Format(v))).ToArray();
    }

    public static string Format(int value) => $"{Formatter.format(value)}/s";
}
