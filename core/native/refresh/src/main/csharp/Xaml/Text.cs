using ch.cyberduck.core.i18n;
using Splat;
using System;
using System.Windows.Markup;

namespace Ch.Cyberduck.Core.Refresh.Xaml
{
    public class Text : MarkupExtension
    {
        private static readonly Locale locale;

        static Text()
        {
            locale = Locator.Current.GetService<Locale>();
        }

        public Text(string key)
        {
            Key = key;
        }

        public string Key { get; }

        public string Table { get; set; }

        public override object ProvideValue(IServiceProvider serviceProvider) => locale?.localize(Key, Table) ?? Key;
    }
}
