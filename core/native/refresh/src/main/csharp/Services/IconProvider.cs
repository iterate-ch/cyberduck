using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.Local;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.UI.Controls;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.WindowsAndMessaging;
using static System.Runtime.CompilerServices.Unsafe;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.CoreRefreshMethods;
using static Windows.Win32.Storage.FileSystem.FILE_FLAGS_AND_ATTRIBUTES;
using static Windows.Win32.UI.Shell.SHGFI_FLAGS;
using static Windows.Win32.UI.Shell.SHGSI_FLAGS;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public abstract partial class IconProvider
    {
        public IconProvider(IconCache iconCache, IIconProviderImageSource imageSource)
        {
            IconCache = iconCache;
            ImageSource = imageSource;
        }

        protected IconCache IconCache { get; }

        protected IIconProviderImageSource ImageSource { get; }

        protected Stream GetStream(string name)
        {
            if (Path.IsPathRooted(name))
            {
                return new FileStream(name, FileMode.Open);
            }
            return ImageSource.GetStream(name);
        }
    }

    public abstract partial class IconProvider<T> : IconProvider
    {
        protected static readonly Logger Log = LogManager.getLogger(typeof(T));

        private static char[] PathSeparatorChars => [Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar, Path.VolumeSeparatorChar];

        protected IconProvider(IconCache iconCache, IIconProviderImageSource imageSource) : base(iconCache, imageSource)
        {
        }

        public delegate void CacheIconCallback(IconCache cache, int size, T source);

        public delegate bool GetCacheIconCallback(IconCache cache, int size);

        public T GetApplication(Application application, int size)
        {
            string key = "app:" + application.getIdentifier();
            if (!IconCache.TryGetIcon(key, size, out T image))
            {
                string iconPath, realIconPath;
                int iconIndex;
                switch (application)
                {
                    case ShellApplicationFinder.ShellApplication shell:
                        iconPath = shell.IconPath;
                        iconIndex = shell.IconIndex;
                        break;

                    case ShellApplicationFinder.ProgIdApplication progId:
                        iconPath = progId.IconPath;
                        iconIndex = progId.IconIndex;
                        break;

                    default:
                        return default;
                }
                realIconPath = iconPath;

                DestroyIconSafeHandle largeIcon = null;
                DestroyIconSafeHandle smallIcon = null;
                try
                {
                    iconPath = SHLoadIndirectString(PCWSTR.DangerousFromString(iconPath));

                    SHCreateFileExtractIcon(iconPath, 0, out IExtractIconW icon);
                    icon.Extract(iconPath, (uint)iconIndex, out largeIcon, out smallIcon, 0);
                    if (!largeIcon.IsInvalid)
                    {
                        Get(largeIcon.DangerousGetHandle(), (c, s, i) => c.CacheIcon(key, s, i));
                    }

                    if (!smallIcon.IsInvalid)
                    {
                        Get(smallIcon.DangerousGetHandle(), (c, s, i) => c.CacheIcon(key, s, i));
                    }

                    image = Get(key, size);
                }
                catch (Exception genericException)
                {
                    Log.error(string.Format("Failure extracting icon for {0}. Icon path: {1} (Index: {2}, Indirect: \"{3}\")", application, iconPath, iconIndex, realIconPath), genericException);
                }
                finally
                {
                    largeIcon?.Dispose();
                    smallIcon?.Dispose();
                }
            }
            return image;
        }

        public abstract T GetDisk(Protocol protocol, int size);

        public T GetFileIcon(string filename, bool isFolder, bool large, bool isExecutable)
        {
            string key = string.Empty;
            string fileInfo = filename;
            if (isFolder)
            {
                key = "folder";
                fileInfo = "_unknown";
            }
            else if (!isExecutable
                && filename.LastIndexOf('.') is int index && index != -1
                && filename.IndexOfAny(PathSeparatorChars, index) == -1)
            {
                key = filename.Substring(index + 1);
                fileInfo = filename.Substring(index);
            }
            else
            {
                key = filename.ToUpperInvariant().GetHashCode().ToString("X4");
                IconCache.Temporary("ext", key);
            }

            if (IconCache.TryGetIcon("ext", large ? 32 : 16, out T image, key))
            {
                return image;
            }

            SHGFI_FLAGS flags = SHGFI_ICON | SHGFI_USEFILEATTRIBUTES;
            flags |= large ? SHGFI_LARGEICON : SHGFI_SMALLICON;

            FILE_FLAGS_AND_ATTRIBUTES fileAttributes = isFolder ? FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL;

            SHFILEINFOW shfi = new();
            try
            {
                if (SHGetFileInfo(fileInfo, fileAttributes, shfi, flags) == 0)
                {
                    return default;
                }
                return Get(shfi.hIcon, (c, s, i) => c.CacheIcon("ext", s, i, key));
            }
            finally
            {
                DestroyIcon(shfi.hIcon);
            }
        }

        public abstract T GetIcon(Protocol protocol, int size);

        public T GetResource(string name, int? requestSize = default) => requestSize switch
        {
            int size => Get(name, size),
            _ => Get(name)
        };

        public T GetStockIcon(SHSTOCKICONID stockIconId, int size)
        {
            var filtered = IconCache.Filter<T>(v => stockIconId.Equals(v.Key));
            if (!filtered.Any())
            {
                SHSTOCKICONINFO info = new()
                {
                    cbSize = (uint)SizeOf<SHSTOCKICONINFO>()
                };
                if (SHGetStockIconInfo(stockIconId, SHGSI_SYSICONINDEX, ref info) is { Failed: true, Value: { } shgsiError })
                {
                    Log.error($"Failure retrieving {stockIconId}.", Marshal.GetExceptionForHR(shgsiError));
                    return default;
                }

                List<T> icons = [];
                filtered = icons;
                foreach (var ilSize in (int[])[
                    1 /* SMALL */,
                    0 /* LARGE */,
                    2 /* EXTRA_LARGE */,
                    4 /* JUMBO */
                ])
                {
                    if (SHGetImageList(ilSize, out IImageList imageList) is { Failed: true, Value: { } shgilError })
                    {
                        if (Log.isDebugEnabled())
                        {
                            Log.debug("Failure retrieving Image List", Marshal.GetExceptionForHR(shgilError));
                        }

                        continue;
                    }

                    DestroyIconSafeHandle handle = null;
                    try
                    {
                        imageList.GetIcon(info.iSysImageIndex, 0, out handle);
                        if (!handle.IsInvalid)
                        {
                            icons.Add(Get(handle.DangerousGetHandle(), (c, s, i) => IconCache.CacheIcon(stockIconId, s, i)));
                        }
                    }
                    catch (Exception e)
                    {
                        if (Log.isDebugEnabled())
                        {
                            Log.debug($"Failure retrieving icon {info.iSysImageIndex} from Image List {ilSize}", e);
                        }
                    }
                    finally
                    {
                        handle?.Dispose();
                    }
                }
            }

            return NearestFit(filtered, size, (c, s, i) => IconCache.CacheIcon(stockIconId, s, i));
        }

        protected abstract T Get(string name);

        protected abstract T Get(string name, int size);

        protected abstract T Get(IntPtr nativeIcon, CacheIconCallback cacheIcon);

        protected abstract T NearestFit(IEnumerable<T> sources, int size, CacheIconCallback cacheCallback);

        protected abstract T Overlay(T baseImage, T overlay, int size);
    }
}
