using System;
using System.Globalization;
using System.Windows.Data;
using ch.cyberduck.core.profiles;
using Ch.Cyberduck.Core.Refresh.Services;

namespace Ch.Cyberduck.Core.Refresh.Views.Services;

public class ProfileDescriptionThumbnailConverter : IValueConverter
{
    public WpfIconProvider IconProvider { get; set; }

    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is not ProfileDescription profile)
        {
            return null;
        }

        if (parameter is not int size)
        {
            return null;
        }

        return IconProvider?.GetThumbnail(profile, size);
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => throw new NotSupportedException();
}
