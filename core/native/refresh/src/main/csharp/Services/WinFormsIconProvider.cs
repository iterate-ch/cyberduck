using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Forms;
using System.Windows.Media.Imaging;
using static System.Drawing.Imaging.PixelFormat;
using static System.Windows.Media.PixelFormats;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    using ch.cyberduck.core;

    public class WinFormsIconProvider : IconProvider<Bitmap>
    {
        private readonly WpfIconProvider wpfIconProvider;

        public WinFormsIconProvider(WpfIconProvider wpfIconProvider, ProtocolFactory protocols, IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache)
        {
            this.wpfIconProvider = wpfIconProvider;
            BuildProtocolImageList(protocols);
        }

        public override Bitmap GetDisk(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Bitmap image, "Disk")
            ? image
            : CacheFromWpfIconProvider(protocol, wpfIconProvider.GetDisk(protocol, size), false, "Disk");

        public override Bitmap GetIcon(Protocol protocol, int size)
            => IconCache.TryGetIcon(protocol, size, out Bitmap image, "Icon")
            ? image
            : CacheFromWpfIconProvider(protocol, wpfIconProvider.GetIcon(protocol, size), false, "Icon");

        public ImageList ProtocolList { get; } = new ImageList() { ImageSize = new Size(16, 16), ColorDepth = ColorDepth.Depth32Bit };

        public Bitmap AliasFolder()
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

        public Bitmap DefaultBrowser() => GetFileIcon(Utils.GetSystemDefaultBrowser(), false, true, true);

        public Bitmap GetPath(Path path, int size)
        {
            string key = "path:" + (path.isDirectory() ? "folder" : path.getExtension());

            Func<(string, Func<int, Bitmap>)> overlayFactory = default;
            if (path.getType().contains(AbstractPath.Type.decrypted))
            {
                overlayFactory = () => ("unlocked", (int size) => GetResource("unlockedbadge", size));
            }
            else if (path.isSymbolicLink())
            {
                overlayFactory = () => ("alias", (int size) => GetResource("aliasbadge", size));
            }
            else
            {
                var permission = path.attributes().getPermission();
                if (path.isFile())
                {
                    return string.IsNullOrWhiteSpace(path.getExtension()) && permission.isExecutable()
                        ? GetResource("executable", size)
                        : GetFileIcon(path.getName(), false, size >= 32, false);
                }
                else if (path.isDirectory())
                {
                    if (Permission.EMPTY != permission)
                    {
                        if (!permission.isExecutable())
                        {
                            overlayFactory = () => ("privatefolder", (int size) => GetResource("privatefolderbadge", size));
                        }
                        else if (!permission.isReadable() && permission.isWritable())
                        {
                            overlayFactory = () => ("dropfolder", (int size) => GetResource("dropfolderbadge", size));
                        }
                        else if (!permission.isWritable() && permission.isReadable())
                        {
                            overlayFactory = () => ("readonlyfolder", (int size) => GetResource("readonlyfolderbadge", size));
                        }
                    }
                }
            }

            (string Class, Func<int, Bitmap> factory) = overlayFactory?.Invoke() ?? default;
            if (IconCache.TryGetIcon(key, out Bitmap image, Class))
            {
                return image;
            }

            var baseImage = GetFileIcon(path.getExtension(), path.isDirectory(), size >= 32, false);
            if (factory is not null)
            {
                var overlayed = Overlay(baseImage, factory(size), size);
                IconCache.CacheIcon(key, size, overlayed, Class);
            }
            return baseImage;
        }

        public Bitmap ResizeImageDangerous(Image image, int size) => new(image, new Size(size, size));

        protected override Bitmap Get(string name)
            => IconCache.TryGetIcon(name, out Bitmap image)
            ? image
            : CacheFromWpfIconProvider(name, wpfIconProvider.GetResource(name), true);

        protected override Bitmap Get(string name, int size)
            => IconCache.TryGetIcon(name, size, out Bitmap image)
            ? image
            : CacheFromWpfIconProvider(name, wpfIconProvider.GetResource(name, size), false);

        private Bitmap CacheFromWpfIconProvider(object key, BitmapSource bitmapSource, bool isDefault, string classifier = default)
        {
            Bitmap bitmap = BitmapFromBitmapSource(bitmapSource);
            if (isDefault)
            {
                IconCache.CacheIcon(key, bitmap.Size.Width, classifier);
            }
            IconCache.CacheIcon(key, bitmap.Size.Width, bitmap, classifier);
            return bitmap;
        }

        private static Bitmap Overlay(Image original, Image overlay, int size)
        {
            var surface = new Bitmap(original, new Size(size, size));
            using var graphics = Graphics.FromImage(surface);
            graphics.DrawImage(overlay, graphics.ClipBounds);
            return surface;
        }

        private void BuildProtocolImageList(ProtocolFactory protocols)
        {
            var iterator = ProtocolFactory.get().find().iterator();
            while (iterator.hasNext())
            {
                var protocol = (Protocol)iterator.next();
                ProtocolList.Images.Add(protocol.disk(), GetDisk(protocol, 16));
            }
        }

        private Bitmap BitmapFromBitmapSource(BitmapSource bitmapSource)
        {
            FormatConvertedBitmap src = new();
            src.BeginInit();
            src.Source = bitmapSource;
            src.DestinationFormat = Bgra32;
            src.EndInit();

            Bitmap copy = new(src.PixelWidth, src.PixelWidth, Format32bppArgb);
            var data = copy.LockBits(new Rectangle(default, copy.Size), ImageLockMode.WriteOnly, copy.PixelFormat);
            src.CopyPixels(default, data.Scan0, data.Height * data.Stride, data.Stride);
            copy.UnlockBits(data);

            return copy;
        }

        protected override Bitmap Get(IntPtr nativeIcon, CacheIconCallback cacheIcon)
        {
            using var icon = Icon.FromHandle(nativeIcon);
            Bitmap bitmap = icon.ToBitmap();
            cacheIcon(IconCache, bitmap.Size.Width, bitmap);
            return bitmap;
        }
    }
}
