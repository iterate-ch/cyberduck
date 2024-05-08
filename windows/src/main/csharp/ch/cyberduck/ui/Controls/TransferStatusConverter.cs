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

using System;
using System.Globalization;
using System.Windows.Data;
using Ch.Cyberduck.Core.Refresh.Services;
using Splat;

namespace ch.cyberduck.ui.Controls;

public class TransferStatusConverter : IValueConverter
{
    private readonly WpfIconProvider wpfIcons = Locator.Current.GetService<WpfIconProvider>();

    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        return wpfIcons?.GetResource((value as bool?) switch
        {
            true => "statusGreen",
            false => "statusRed",
            null => "statusYellow",
        }, parameter as int?);
    }

    object IValueConverter.ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotSupportedException();
    }
}
