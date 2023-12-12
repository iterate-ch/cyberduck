using System.Drawing;
using System.Runtime.CompilerServices;
using System.Windows.Media.Imaging;
using Ch.Cyberduck.Core.Refresh.Services;

namespace Ch.Cyberduck.Core.Refresh
{
    public class Images
    {
        private readonly Win32IconProvider win32IconProvider;
        private readonly WinFormsIconProvider winFormsIconProvider;
        private readonly WpfIconProvider wpfIconProvider;

        public Images(WinFormsIconProvider winFormsIconProvider, WpfIconProvider wpfIconProvider, Win32IconProvider win32IconProvider)
        {
            this.winFormsIconProvider = winFormsIconProvider;
            this.wpfIconProvider = wpfIconProvider;
            this.win32IconProvider = win32IconProvider;
        }

        public ResourceRef Add => new(this);
        public ResourceRef AddPressed => new(this);
        public ResourceRef Advanced => new(this);
        public ResourceRef Alert => new(this);
        public ResourceRef Bandwidth => new(this);
        public ResourceRef Bookmark => new(this);
        public ResourceRef Bookmarks => new(this);
        public ResourceRef Browser => new(this);
        public ResourceRef Clean => new(this);
        public ResourceRef CleanAll => new(this);
        public ResourceRef Connect => new(this);
        public ResourceRef Connection => new(this);
        public ResourceRef Cryptomator => new(this);
        public ResourceRef CyberduckApplication => new(this, "cyberduck-application");
        public ResourceRef CyberduckDocument => new(this, "cyberduck-document");
        public ResourceRef Delete => new(this);
        public ResourceRef Download => new(this);
        public ResourceRef Edit => new(this);
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
        public ResourceRef Log => new(this);
        public ResourceRef Multiple => new(this);
        public ResourceRef NavBackward => new(this, "nav-backward");
        public ResourceRef NavForward => new(this, "nav-forward");
        public ResourceRef NavUp => new(this, "nav-up");
        public ResourceRef Open => new(this);
        public ResourceRef Outline => new(this);
        public ResourceRef Pencil => new(this);
        public ResourceRef Permissions => new(this);
        public ResourceRef Plus => new(this);
        public ResourceRef Queue => new(this);
        public ResourceRef Reload => new(this);
        public ResourceRef Remove => new(this);
        public ResourceRef RemovePressed => new(this);
        public ResourceRef Rendezvous => new(this);
        public ResourceRef Resume => new(this);
        public ResourceRef Reveal => new(this);
        public ResourceRef S3 => new(this);
        public ResourceRef SearchActive => new(this, "search-active");
        public ResourceRef SearchInactive => new(this, "search-inactive");
        public ResourceRef Site => new(this);
        public ResourceRef StatusGreen => new(this);
        public ResourceRef StatusRed => new(this);
        public ResourceRef StatusYellow => new(this);
        public ResourceRef Stop => new(this);
        public ResourceRef Sync => new(this);
        public ResourceRef Throbber => new(this);
        public ResourceRef ThrobberSmall => new(this, "throbber_small");
        public ResourceRef TransferDownload => new(this, "transfer-download");
        public ResourceRef TransferUpload => new(this, "transfer-upload");
        public ResourceRef Trash => new(this);
        public ResourceRef Turtle => new(this);
        public ResourceRef Unlocked => new(this);
        public ResourceRef Update => new(this);
        public ResourceRef Upload => new(this);

        public ResourceRef Get(string name) => new(this, name);

        public struct ResourceRef
        {
            private readonly Images images;
            private readonly string name;

            public ResourceRef(Images images, [CallerMemberName] string name = default)
            {
                this.name = name;
                this.images = images;
            }

            public static implicit operator Image(in ResourceRef @this) => @this.images?.winFormsIconProvider.GetResource(@this.name) ?? default;

            public static implicit operator BitmapSource(in ResourceRef @this) => @this.images?.wpfIconProvider.GetResource(@this.name) ?? default;

            public static implicit operator Icon(in ResourceRef @this) => @this.images?.win32IconProvider.GetResource(@this.name) ?? default;

            public SizedResourceRef Size(int size) => new(name, size, images);
        }

        public struct SizedResourceRef
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

            public static implicit operator Image(in SizedResourceRef @this) => @this.images?.winFormsIconProvider.GetResource(@this.name, @this.size) ?? default;

            public static implicit operator BitmapSource(in SizedResourceRef @this) => @this.images?.wpfIconProvider.GetResource(@this.name, @this.size) ?? default;
        }
    }
}
