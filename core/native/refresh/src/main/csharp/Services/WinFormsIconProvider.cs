using ch.cyberduck.core;
using Ch.Cyberduck.Core.Local;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Linq;
using System.Windows.Forms;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public partial class WinFormsIconProvider : IconProvider<Image>
    {
        private readonly ProtocolFactory protocols;

        public WinFormsIconProvider(ProtocolFactory protocols, ProfileListObserver profileListObserver, IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
            this.protocols = protocols;
            BuildProtocolImageList();
            profileListObserver.ProfilesChanged += ProfileListObserver_ProfilesChanged;
        }

        public ImageList ProtocolList { get; } = new ImageList() { ImageSize = new Size(16, 16), ColorDepth = ColorDepth.Depth32Bit };

        public Image AliasFolder()
        {
            if (IconCache.TryGetIcon("path:folder", out Bitmap image, "alias"))
            {
                return image;
            }

            var baseImage = GetFileIcon(default, true, true, false);
            var overlay = GetResource("aliasbadge", 32);
            var overlayed = Overlay(baseImage, overlay, 32);
            IconCache.CacheIcon("path:folder", 32, overlayed, "alias");
            return overlayed;
        }

        public Image DefaultBrowser()
        {
            if (Utils.GetSystemDefaultBrowser() is not ShellApplicationFinder.ProgIdApplication app)
            {
                return default;
            }
            return GetApplication(app, 32);
        }

        public override Image GetDisk(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Image image, "Disk")
            ? image
            : Get(protocol, protocol.disk(), size, "Disk");

        public override Image GetIcon(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Image image, "Icon")
            ? image
            : Get(protocol, protocol.icon(), size, "Icon");

        public Image ResizeImageDangerous(Image image, int size) => new Bitmap(image, new Size(size, size));

        protected override Image Get(string name, int size)
            => Get(name, name, size, default);

        protected override Image Get(string name)
            => Get(name, name, default);

        protected override Image Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            using var icon = Icon.FromHandle(nativeIcon);
            Image bitmap = icon.ToBitmap();
            cacheIcon(IconCache, bitmap.Width, bitmap);
            return bitmap;
        }

        protected override Image NearestFit(IEnumerable<Image> sources, int size, CacheIconCallback cacheCallback)
        {
            var nearest = int.MaxValue;
            Image nearestFit = null;

            foreach (var item in sources)
            {
                int d = size - item.Width;
                if (d == 0)
                {
                    return item;
                }

                if ((d < 0 && (nearest > 0 || nearest < d)) || (nearest > 0 && d < nearest))
                {
                    nearest = d;
                    nearestFit = item;
                }
            }
            nearestFit = new Bitmap(nearestFit, size, size);
            cacheCallback(IconCache, size, nearestFit);
            return nearestFit;
        }

        protected override Image Overlay(Image original, Image overlay, int size)
        {
            Bitmap surface = new(original, new Size(size, size));
            using var graphics = Graphics.FromImage(surface);
            graphics.DrawImage(overlay, graphics.ClipBounds);
            return surface;
        }

        private void BuildProtocolImageList()
        {
            var iterator = protocols.find().iterator();
            HashSet<string> removedKeys = new(ProtocolList.Images.Keys.OfType<string>());
            while (iterator.hasNext())
            {
                var protocol = (Protocol)iterator.next();
                var key = protocol.disk();
                removedKeys.Remove(key);
                ProtocolList.Images.Add(key, GetDisk(protocol, 16));
            }
            foreach (var item in removedKeys)
            {
                ProtocolList.Images.RemoveByKey(item);
            }
        }

        private Image Get(object key, string path, string classifier)
            => IconCache.TryGetIcon(key, out Image image, classifier)
            ? image
            : Get(key, path, 0, classifier, true);

        private Image Get(object key, string path, int size, string classifier)
            => IconCache.TryGetIcon(key, size, out Image image, classifier)
            ? image
            : Get(key, path, size, classifier, false);

        private Image Get(object key, string path, int size, string classifier, bool returnDefault)
        {
            var images = Get(key, path, classifier, returnDefault, out var image);
            return image ?? NearestFit(images, size, (c, s, i) =>
            {
                c.CacheIcon(key, s, i, classifier);
                c.MarkResized(key, s, classifier);
            });
        }

        private void ProfileListObserver_ProfilesChanged(object sender, EventArgs e) => BuildProtocolImageList();
    }
}
