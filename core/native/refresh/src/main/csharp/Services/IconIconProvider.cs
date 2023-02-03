using System;
using System.Drawing;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public class IconIconProvider : IconProvider<Icon>
    {
        public IconIconProvider(IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
        }

        public override Icon GetDisk(Protocol protocol, int size) => default;

        public override Icon GetIcon(Protocol protocol, int size) => default;

        protected override Icon Get(string name, int size)
            => Get(name, name, default);

        protected override Icon Get(string name)
            => Get(name, name, default);

        protected override Icon Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            var icon = Icon.FromHandle(nativeIcon);
            cacheIcon(IconCache, icon.Width, icon);
            return icon;
        }

        private Icon Get(object key, string path, string classifier)
        {
            if (IconCache.TryGetIcon(key, out Icon image, classifier))
            {
                return image;
            }

            using var stream = GetStream(path);
            Icon icon = new(stream);
            IconCache.CacheIcon(key, 0, icon, classifier);
            return icon;
        }
    }
}
