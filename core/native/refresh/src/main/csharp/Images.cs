using Cyberduck.Core.Refresh.Services;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Windows.Media.Imaging;

namespace Cyberduck.Core.Refresh
{
    public class Images
    {
        private readonly WinFormsIconProvider winFormsIconProvider;
        private readonly WpfIconProvider wpfIconProvider;

        public Images(WinFormsIconProvider winFormsIconProvider, WpfIconProvider wpfIconProvider)
        {
            this.winFormsIconProvider = winFormsIconProvider;
            this.wpfIconProvider = wpfIconProvider;
        }

        public ResourceRef AddPressed => new(this);

        public ResourceRef Alert => new(this);

        public ResourceRef Bandwidth => new(this);

        public ResourceRef Bookmark => new(this);

        public ResourceRef Browser => new(this);

        public ResourceRef Connect => new(this);

        public ResourceRef Connection => new(this);

        public ResourceRef Cryptomator => new(this);

        public ResourceRef Cyberduck => new(this);

        public ResourceRef Delete => new(this);

        public ResourceRef Download => new(this);

        public ResourceRef EditPressed => new(this);

        public ResourceRef Eject => new(this);

        public ResourceRef FolderPlus => new(this);

        public ResourceRef FTP => new(this);

        public ResourceRef General => new(this);

        public ResourceRef GoogleStorage => new(this);

        public ResourceRef History => new(this);

        public ResourceRef Info => new(this);

        public ResourceRef Language => new(this);

        public ResourceRef Locked => new(this);

        public ResourceRef Multiple => new(this);

        public ResourceRef Pencil => new(this);

        public ResourceRef Plus => new(this);

        public ResourceRef Queue => new(this);

        public ResourceRef Reload => new(this);

        public ResourceRef RemovePressed => new(this);

        public ResourceRef Rendezvous => new(this);

        public ResourceRef Reveal => new(this);

        public ResourceRef S3 => new(this);

        public ResourceRef Site => new(this);

        public ResourceRef StatusGreen => new(this);

        public ResourceRef StatusRed => new(this);

        public ResourceRef StatusYellow => new(this);

        public ResourceRef Stop => new(this);

        public ResourceRef Sync => new(this);

        public ResourceRef TransferDownload => new(this, "transfer-download");

        public ResourceRef TransferUpload => new(this, "transfer-upload");

        public ResourceRef Turtle => new(this);

        public ResourceRef Unlocked => new(this);

        public ResourceRef Update => new(this);

        public ResourceRef Upload => new(this);

        public ResourceRef Get(string name) => new(this, name);

        public ref struct ResourceRef
        {
            private readonly Images images;
            private readonly string name;

            public ResourceRef(Images images, [CallerMemberName] string name = default)
            {
                this.name = name;
                this.images = images;
            }

            public static implicit operator Bitmap(in ResourceRef @this) => @this.images.winFormsIconProvider.GetResource(@this.name);

            public static implicit operator BitmapSource(in ResourceRef @this) => @this.images.wpfIconProvider.GetResource(@this.name);

            public SizedResourceRef Size(int size) => new SizedResourceRef(name, size, images);
        }

        public ref struct SizedResourceRef
        {
            private readonly Images images;
            private readonly string name;
            private readonly int size;

            public SizedResourceRef(string name, int size, Images images)
            {
                this.name = name;
                this.size = size;
                this.images = images;
            }

            public static implicit operator Bitmap(in SizedResourceRef @this) => @this.images.winFormsIconProvider.GetResource(@this.name, @this.size);

            public static implicit operator BitmapSource(in SizedResourceRef @this) => @this.images.wpfIconProvider.GetResource(@this.name, @this.size);
        }
    }
}
