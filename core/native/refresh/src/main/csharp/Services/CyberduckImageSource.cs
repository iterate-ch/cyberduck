using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Resources;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public class CyberduckImageSource : IIconProviderImageSource
    {
        private readonly ResourceManager resourceManager = new("Images", typeof(CyberduckImageSource).Assembly);
        private readonly Dictionary<string, string> resourceNameMap = new();

        public CyberduckImageSource()
        {
            var resourceSet = resourceManager.GetResourceSet(CultureInfo.InvariantCulture, true, false);
            foreach (DictionaryEntry item in resourceSet)
            {
                var filename = (string)item.Key;
                resourceNameMap[Path.GetFileNameWithoutExtension(filename)] = filename;
            }
        }

        /// <returns><see cref="UnmanagedMemoryStream"/> or null.</returns>
        public Stream GetStream(string name)
        {
            name = name.ToLowerInvariant();
            return resourceNameMap.TryGetValue(Path.GetFileNameWithoutExtension(name), out string stream)
                ? resourceManager.GetStream(stream)
                : default;
        }
    }
}
