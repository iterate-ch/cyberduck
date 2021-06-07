using System.Collections.Generic;

namespace Cyberduck.Core.Refresh.Services
{
    public class IconResourceProvider
    {
        private readonly IconResourceSource[] sources;

        public IconResourceProvider(IconResourceSource[] sources)
        {
            this.sources = sources;
        }

        public object GetResource(string name)
        {
            foreach (var item in sources)
            {
                if (item.TryGetResource(name, out var resource))
                {
                    return resource;
                }
            }
            throw new KeyNotFoundException();
        }
    }
}
