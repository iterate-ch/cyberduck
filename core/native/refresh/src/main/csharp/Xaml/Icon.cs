using System;
using System.Windows.Markup;
using System.Windows.Media.Imaging;
using Ch.Cyberduck.Core.Refresh.Services;
using Splat;

namespace Ch.Cyberduck.Core.Refresh.Xaml
{
    public class Icon : MarkupExtension
    {
        private readonly WpfIconProvider icons;
        private readonly string resourceName;

        public int? Size { get; set; }

        public Icon(string resourceName)
        {
            icons = Locator.Current.GetService<WpfIconProvider>();
            this.resourceName = resourceName;
        }

        public override object ProvideValue(IServiceProvider serviceProvider) => icons?.GetResource(resourceName, Size);
    }
}
