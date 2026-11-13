using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;

namespace Ch.Cyberduck.Core.Refresh.Controls;

public class DialogContentControl : HeaderedContentControl
{
    public static readonly DependencyProperty DialogContentProperty = DependencyProperty.Register(nameof(DialogContent), typeof(object), typeof(DialogContentControl));
    public static readonly DependencyProperty ImageSourceProperty = DependencyProperty.Register(nameof(ImageSource), typeof(ImageSource), typeof(DialogContentControl));

    public object DialogContent
    {
        get => GetValue(DialogContentProperty);
        set => SetValue(DialogContentProperty, value);
    }

    public ImageSource ImageSource
    {
        get => (ImageSource)GetValue(ImageSourceProperty);
        set => SetValue(ImageSourceProperty, value);
    }

    static DialogContentControl()
    {
        DefaultStyleKeyProperty.OverrideMetadata(typeof(DialogContentControl), new FrameworkPropertyMetadata(typeof(DialogContentControl)));
    }
}
