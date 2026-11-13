using System;
using System.Globalization;
using System.Windows;
using System.Windows.Data;
using System.Windows.Media.Imaging;
using ch.cyberduck.core.profiles;
using Ch.Cyberduck.Core.Refresh.Services;
using CommunityToolkit.Mvvm.DependencyInjection;

namespace Ch.Cyberduck.Core.Refresh.Views.Services;

[ValueConversion(typeof(ProfileDescription), typeof(BitmapSource), ParameterType = typeof(int))]
public class ProfileDescriptionThumbnailConverter : IValueConverter
{
    private WpfIconProvider iconProvider;

    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is not ProfileDescription profile)
        {
            return DependencyProperty.UnsetValue;
        }

        if (parameter is not int size)
        {
            if (parameter is IConvertible convertible)
            {
                size = convertible.ToInt32(culture);
            }
            else
            {
                return DependencyProperty.UnsetValue;
            }
        }

        if ((iconProvider ??= Ioc.Default.GetService<WpfIconProvider>()) is null)
        {
            return DependencyProperty.UnsetValue;
        }

        return iconProvider.GetThumbnail(profile, size);
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture) => throw new NotSupportedException();
}
