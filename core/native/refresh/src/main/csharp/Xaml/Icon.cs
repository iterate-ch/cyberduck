using System;
using System.Windows.Markup;
using Ch.Cyberduck.Core.Refresh.Services;
using StructureMap;

namespace Ch.Cyberduck.Core.Refresh.Xaml
{
    public class Icon : MarkupExtension
    {
        private readonly WpfIconProvider icons;
        private readonly string resourceName;

        public int? Size { get; set; }

        public Icon(string resourceName)
        {
            icons = ObjectFactory.TryGetInstance<WpfIconProvider>();
            this.resourceName = resourceName;
        }

        public override object ProvideValue(IServiceProvider serviceProvider) => icons?.GetResource(resourceName, Size);
    }
}
