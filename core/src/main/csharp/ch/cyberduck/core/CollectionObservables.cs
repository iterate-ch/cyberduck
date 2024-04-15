using DynamicData;
using System;
using System.Collections.Generic;
using System.Reactive;

#nullable enable

namespace ch.cyberduck.core;

public static class CollectionObservables
{
    public delegate TResult ObservableTransform<TSource, TResult>(TSource obj, TResult? cached);

    public static IObservable<IChangeSet<TResult>> ToObservableChangeSet<TSource, TResult>(
        this Collection collection,
        ObservableTransform<TSource, TResult> transform,
        bool withInitial = false)
    {
        return new ObservableCollectionChangeSet<TSource, TResult>(collection, transform, withInitial);
    }

    private sealed class ObservableCollectionChangeSet<TSource, TResult>(Collection collection, ObservableTransform<TSource, TResult> transform, bool withInitial) : ObservableBase<IChangeSet<TResult>>
    {
        protected override IDisposable SubscribeCore(IObserver<IChangeSet<TResult>> observer)
        {
            _ _ = new(observer, this);
            if (withInitial)
            {
                Populate(_);
            }

            collection.addListener(_);
            return _;
        }

        private TResult Transform(TSource source, TResult? cached) => transform(source, cached);

        private void Populate(_ _)
        {
            _.Load(collection);
        }

        private void Dispose(_ _)
        {
            collection.removeListener(_);
        }

        private class _(IObserver<IChangeSet<TResult>> observer, ObservableCollectionChangeSet<TSource, TResult> observable) : CollectionListener, IDisposable
        {
            private readonly Dictionary<TSource, TResult> _cache = new(ReferenceEqualityComparer.Default);
            private readonly object _sync = new();
            private bool disposedValue = false;
            private bool loaded = false;

            public void Dispose()
            {
                if (!disposedValue)
                {
                    disposedValue = true;
                    observable.Dispose(this);
                    observer.OnCompleted();
                    _cache.Clear();
                }
            }

            void CollectionListener.collectionItemAdded(object obj)
            {
                if (obj is not TSource key)
                {
                    return;
                }

                ChangeSet<TResult> changes;
                lock (_sync)
                {
                    if (_cache.TryGetValue(key, out var _))
                    {
                        return;
                    }

                    changes = [];
                    ItemAdded(key, changes);
                }

                observer.OnNext(changes);
            }

            void CollectionListener.collectionItemChanged(object obj)
            {
                if (obj is not TSource key)
                {
                    return;
                }

                TResult item;
                lock (_sync)
                {
                    if (!TransformCache(key, out item))
                    {
                        return;
                    }
                }

                observer.OnNext(new ChangeSet<TResult>()
                {
                    new(ListChangeReason.Add, item)
                });
            }

            void CollectionListener.collectionItemRemoved(object obj)
            {
                if (obj is not TSource key)
                {
                    return;
                }

                TResult item;
                lock (_sync)
                {
                    if (!(_cache.TryGetValue(key, out item) && _cache.Remove(key)))
                    {
                        return;
                    }
                }

                observer.OnNext(new ChangeSet<TResult>()
                {
                    new(ListChangeReason.Remove, item)
                });
            }

            void CollectionListener.collectionLoaded()
            { /* Maybe call into observable.Populate(this)? */ }

            void IDisposable.Dispose() => Dispose();

            internal void Load(Collection arrayList)
            {
                if (loaded)
                {
                    return;
                }

                loaded = true;
                ChangeSet<TResult> changes = [];
                foreach (TSource item in arrayList)
                {
                    ItemAdded(item, changes);
                }

                observer.OnNext(changes);
            }

            private void ItemAdded(TSource obj, ChangeSet<TResult> changes)
            {
                if (TransformCache(obj, out var item))
                {
                    changes.Add(new(ListChangeReason.Add, item));
                }
            }

            private bool TransformCache(TSource obj, out TResult item)
            {
                var isCached = _cache.TryGetValue(obj, out item);
                _cache[obj] = item = observable.Transform(obj, item);
                return !isCached;
            }

            private class ReferenceEqualityComparer : IEqualityComparer<TSource>
            {
                public static ReferenceEqualityComparer Default { get; } = new();

                bool IEqualityComparer<TSource>.Equals(TSource x, TSource y) => ReferenceEquals(x, y);

                int IEqualityComparer<TSource>.GetHashCode(TSource obj) => obj.GetHashCode();
            }
        }
    }
}
