using System;
using System.Collections.ObjectModel;
using System.Globalization;
using System.Runtime.CompilerServices;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using Preferences = ch.cyberduck.core.preferences.Preferences;

    public class MetadataTemplateProvider
    {
        public ReadOnlyCollection<Template> Templates { get; }

        public MetadataTemplateProvider(Preferences preferences)
        {
            Templates = new ReadOnlyCollectionBuilder<Template>()
            {
                new("Content-Disposition", static () => "attachment"),
                new("Cache-Control", () => $"public,max-age={preferences.getInteger("s3.cache.seconds")}"),
                new("Expires", () => DateTime.Now.AddSeconds(preferences.getInteger("s3.cache.seconds")).ToString(CultureInfo.InvariantCulture)),
                new("Pragma", static () => ""),
                new("x-amz-website-redirect-location", static () => ""),
            }.ToReadOnlyCollection();
        }

        public record Template(string Name, Template.Factory ValueFactory)
        {
            public delegate string Factory();

            private readonly string name = Name;
            private readonly Factory factory = ValueFactory;

            public string Name => name;

            public Factory ValueFactory => factory;

            public string Create() => ValueFactory();
        }
    }
}
