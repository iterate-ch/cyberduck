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
        public static Image Clean => Images.TryGet(_ =>_.Clean);

        [EditorBrowsable(Never)]
        public static Image CleanAll => Images.TryGet(_ =>_.CleanAll);

        public static WinFormsIconProvider IconProvider { get; }

        [EditorBrowsable(Never)]
        public static Images Images { get; }

        [EditorBrowsable(Never)]
        public static Image Log => Images.TryGet(_ => _.Log);

        [EditorBrowsable(Never)]
        public static Image Open => Images.TryGet(_ => _.Open);

        [EditorBrowsable(Never)]
        public static Image Reload => Images.TryGet(_ => _.Reload);

        [EditorBrowsable(Never)]
        public static Image Resume => Images.TryGet(_ => _.Resume);

        [EditorBrowsable(Never)]
        public static Image Reveal => Images.TryGet(_ => _.Reveal);

        [EditorBrowsable(Never)]
        public static Image StatusGreen => Images.TryGet(_ => _.StatusGreen);

        [EditorBrowsable(Never)]
        public static Image Stop => Images.TryGet(_ => _.Stop);

        [EditorBrowsable(Never)]
        public static Image ThrobberSmall => Images.TryGet(_ => _.ThrobberSmall);

        [EditorBrowsable(Never)]
        public static Image TransferDownload => Images.TryGet(_ => _.TransferDownload);

        [EditorBrowsable(Never)]
        public static Image Trash => Images.TryGet(_ => _.Trash);

        [EditorBrowsable(Never)]
        public static Image SearchInactive => Images.TryGet(_ => _.SearchInactive);

        public static T TryGet<T>(this WinFormsIconProvider images, Expression<Func<WinFormsIconProvider, T>> expression)
            => expression.Body is MemberExpression member && member.Member is PropertyInfo info && images != null ? (T)info.GetValue(images) : default;

        public static T TryGet<T>(this Images images, Expression<Func<Images, T>> expression)
            => expression.Body is MemberExpression member && member.Member is PropertyInfo info && images != null ? (T)info.GetValue(images) : default;
    }
}
