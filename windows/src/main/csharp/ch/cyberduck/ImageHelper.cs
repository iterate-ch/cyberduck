using Ch.Cyberduck.Core.Refresh;
using Ch.Cyberduck.Core.Refresh.Services;
using StructureMap;
using System.ComponentModel;
using System.Drawing;
using static System.ComponentModel.EditorBrowsableState;

namespace Ch.Cyberduck
{
    public static class ImageHelper
    {
        static ImageHelper()
        {
            Images = ObjectFactory.GetInstance<Images>();
            IconProvider = ObjectFactory.GetInstance<WinFormsIconProvider>();
        }

        [EditorBrowsable(Never)]
        public static Bitmap Clean => Images.Clean;

        [EditorBrowsable(Never)]
        public static Bitmap CleanAll => Images.CleanAll;

        public static WinFormsIconProvider IconProvider { get; }

        [EditorBrowsable(Never)]
        public static Images Images { get; }

        [EditorBrowsable(Never)]
        public static Bitmap Log => Images.Log;

        [EditorBrowsable(Never)]
        public static Bitmap Open => Images.Open;

        [EditorBrowsable(Never)]
        public static Bitmap Reload => Images.Reload;

        [EditorBrowsable(Never)]
        public static Bitmap Resume => Images.Resume;

        [EditorBrowsable(Never)]
        public static Bitmap Reveal => Images.Reveal;

        [EditorBrowsable(Never)]
        public static Bitmap StatusGreen => Images.StatusGreen;

        [EditorBrowsable(Never)]
        public static Bitmap Stop => Images.Stop;

        [EditorBrowsable(Never)]
        public static Bitmap ThrobberSmall => Images.ThrobberSmall;

        [EditorBrowsable(Never)]
        public static Bitmap TransferDownload => Images.TransferDownload;

        [EditorBrowsable(Never)]
        public static Bitmap Trash => Images.Trash;

        [EditorBrowsable(Never)]
        public static Bitmap SearchInactive => Images.SearchInactive;
    }
}
