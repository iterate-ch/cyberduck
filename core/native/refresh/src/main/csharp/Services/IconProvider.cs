using ch.cyberduck.core;
using System;
using System.Collections.Concurrent;
using System.Drawing;
using System.Threading;
using System.Windows.Media.Imaging;

namespace Cyberduck.Core.Refresh.Services
{
    public class IconProvider
    {
        private readonly IconCache cache;
        private readonly IconResourceProvider iconResources;
        private readonly ConcurrentDictionary<string, AutoResetEvent> sync = new ConcurrentDictionary<string, AutoResetEvent>();

        public IconProvider(IconCache cache, IconResourceProvider iconResources)
        {
            this.cache = cache;
            this.iconResources = iconResources;
        }

        public Source ForFile(string filename, bool isFolder, int size)
        {
            return default;
        }

        public Source GetDisk(Protocol protocol, int size)
        {
            return default;
        }

        public Source GetIcon(Protocol protocol, int size)
        {
            return default;
        }

        public Source GetResource(string name, int size)
        {
            var resource = iconResources.GetResource(name);

            return default;
        }

        private IDisposable AutoLock(string key)
        {
            var @event = sync.GetOrAdd(key, _ => new AutoResetEvent(true));
            @event.WaitOne();
            return new SyncKey(@event);
        }

        public ref struct Source
        {
            private readonly Func<BitmapSource> bitmapSourceFactory;
            private readonly Func<Image> imageFactory;

            public Source(Func<Image> imageFactory, Func<BitmapSource> bitmapSourceFactory)
            {
                this.imageFactory = imageFactory;
                this.bitmapSourceFactory = bitmapSourceFactory;
            }

            public BitmapSource BitmapSource => bitmapSourceFactory();

            public Image Image => imageFactory();
        }

        private class SyncKey : IDisposable
        {
            private readonly AutoResetEvent resetEvent;
            private bool disposedValue;

            public SyncKey(AutoResetEvent resetEvent)
            {
                this.resetEvent = resetEvent;
            }

            ~SyncKey()
            {
                Dispose(disposing: false);
            }

            void IDisposable.Dispose()
            {
                Dispose(disposing: true);
                GC.SuppressFinalize(this);
            }

            protected virtual void Dispose(bool disposing)
            {
                if (!disposedValue)
                {
                    resetEvent.Set();

                    disposedValue = true;
                }
            }
        }
    }
}
