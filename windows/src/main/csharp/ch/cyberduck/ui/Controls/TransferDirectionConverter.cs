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
using ch.cyberduck.ui.ViewModels;
using Ch.Cyberduck.Core.Refresh.Services;
using Splat;

namespace ch.cyberduck.ui.Controls;

public class TransferDirectionConverter : IValueConverter
{
    private readonly WpfIconProvider wpfIcons = Locator.Current.GetService<WpfIconProvider>();

    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is not TransferDirection direction)
        {
            return null;
        }

        string resourceName;
        if ((resourceName = direction switch
        {
            TransferDirection.Upload => "transfer-upload",
            TransferDirection.Download => "transfer-download",
            TransferDirection.Sync => "transfer-sync",
            _ => null
        }) is null)
        {
            return null;
        }

        return wpfIcons?.GetResource(resourceName, parameter as int?);
    }

    object IValueConverter.ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotSupportedException();
    }
}
