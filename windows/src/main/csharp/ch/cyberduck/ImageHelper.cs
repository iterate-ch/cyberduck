using Ch.Cyberduck.Core.Refresh;
using Ch.Cyberduck.Core.Refresh.Services;
using StructureMap;
using System;
using System.ComponentModel;
using System.Drawing;
using System.Linq.Expressions;
using System.Reflection;
using static System.ComponentModel.EditorBrowsableState;

namespace Ch.Cyberduck
{
    public static class ImageHelper
    {
        static ImageHelper()
        {
            Images = ObjectFactory.TryGetInstance<Images>();
            IconProvider = ObjectFactory.TryGetInstance<WinFormsIconProvider>();
        }

        [EditorBrowsable(Never)]
        public static Image Clean => Images.Clean;

        [EditorBrowsable(Never)]
        public static Image CleanAll => Images.CleanAll;

        public static WinFormsIconProvider IconProvider { get; }

        [EditorBrowsable(Never)]
        public static Images Images { get; }

        [EditorBrowsable(Never)]
        public static Image Log => Images.Log;

        [EditorBrowsable(Never)]
        public static Image Open => Images.Open;

        [EditorBrowsable(Never)]
        public static Image Reload => Images.Reload;

        [EditorBrowsable(Never)]
        public static Image Resume => Images.Resume;

        [EditorBrowsable(Never)]
        public static Image Reveal => Images.Reveal;

        [EditorBrowsable(Never)]
        public static Image StatusGreen => Images.StatusGreen;

        [EditorBrowsable(Never)]
        public static Image Stop => Images.Stop;

        [EditorBrowsable(Never)]
        public static Image ThrobberSmall => Images.ThrobberSmall;

        [EditorBrowsable(Never)]
        public static Image TransferDownload => Images.TransferDownload;

        [EditorBrowsable(Never)]
        public static Image Trash => Images.Trash;

        [EditorBrowsable(Never)]
        public static Image SearchInactive => Images.SearchInactive;

        public static T TryGet<T>(this WinFormsIconProvider images, Expression<Func<WinFormsIconProvider, T>> expression)
            => expression.Body is MemberExpression member && member.Member is PropertyInfo info && images != null ? (T)info.GetValue(images) : default;

        public static T TryGet<T>(this Images images, Expression<Func<Images, T>> expression)
            => expression.Body is MemberExpression member && member.Member is PropertyInfo info && images != null ? (T)info.GetValue(images) : default;
    }
}
