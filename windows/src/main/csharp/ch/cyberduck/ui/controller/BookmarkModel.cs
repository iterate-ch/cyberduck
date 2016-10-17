// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
// 

using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Resources;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class BookmarkModel
    {
        private readonly BrowserController _controller;
        private HostFilter _filter;
        private AbstractHostCollection _filtered;
        private CollectionListener _listener;
        private AbstractHostCollection _source = AbstractHostCollection.empty();

        public BookmarkModel(BrowserController controller, AbstractHostCollection source)
        {
            _controller = controller;
            Source = source;
        }

        public AbstractHostCollection Source
        {
            //The filtered collection currently to be displayed within the constraints 
            //given by the comparision with the HostFilter
            get
            {
                if (null == _filter)
                {
                    return _source;
                }
                if (null == _filtered)
                {
                    _filtered = new FilterBookmarkCollection(_source);
                    foreach (Host bookmark in _source)
                    {
                        if (_filter.accept(bookmark))
                        {
                            _filtered.add(bookmark);
                        }
                    }
                    _filtered.addListener(new FilterBookmarkListener(_source));
                }
                return _filtered;
            }

            set
            {
                if (null != value)
                {
                    _source.removeListener(_listener); //remove previous listener
                    _source = value;
                    _source.addListener(_listener = new BookmarkListener(_controller));
                }
                else
                {
                    _source.removeListener(_listener); //remove previous listener
                    _source = value;
                }
                Filter = null;
            }
        }

        public HostFilter Filter
        {
            set
            {
                _filter = value;
                _filtered = null;
            }
        }

        public object GetBookmarkImage(object host)
        {
            Host h = (Host) host;
            return
                IconCache.Instance.GetProtocolImages(PreferencesFactory.get().
                    getInteger("bookmark.icon.size"))[h.getProtocol().disk()];
        }

        public object GetHostname(object host)
        {
            return ((Host) host).getHostname();
        }

        public object GetUrl(object host)
        {
            Host h = (Host) host;
            return new HostUrlProvider(true, true).get(h);
        }

        public object GetNotes(object host)
        {
            Host h = (Host) host;
            return Source != null ? Source.getComment(h) : string.Empty;
        }

        public object GetBookmarkStatusImage(object rowobject)
        {
            if (_controller.HasSession())
            {
                Session session = _controller.Session;
                if (((Host) rowobject).Equals(session.getHost()))
                {
                    if (session.getState().Equals(Session.State.open))
                    {
                        return IconCache.Instance.IconForName("statusGreen", 16);
                    }
                    if (session.getState().Equals(Session.State.closing) ||
                        session.getState().Equals(Session.State.opening))
                    {
                        return IconCache.Instance.IconForName("statusYellow", 16);
                    }
                }
            }
            return null;
        }

        public object GetNickname(object host)
        {
            return BookmarkNameProvider.toString(((Host) host));
        }

        private class BookmarkListener : CollectionListener
        {
            private readonly BrowserController _controller;

            public BookmarkListener(BrowserController controller)
            {
                _controller = controller;
            }

            public void collectionLoaded()
            {
                ;
            }

            public void collectionItemAdded(object host)
            {
                _controller.Invoke(() => _controller.ReloadBookmarks());
            }

            public void collectionItemRemoved(object host)
            {
                _controller.Invoke(() => _controller.ReloadBookmarks(host as Host));
            }

            public void collectionItemChanged(object host)
            {
                _controller.Invoke(() => _controller.View.RefreshBookmark(host as Host));
            }
        }

        private class FilterBookmarkCollection : AbstractHostCollection
        {
            private readonly AbstractHostCollection _source;

            public FilterBookmarkCollection(AbstractHostCollection source)
            {
                _source = source;
            }

            public override string getName()
            {
                return _source.getName();
            }

            public override bool allowsAdd()
            {
                return _source.allowsAdd();
            }

            public override bool allowsDelete()
            {
                return _source.allowsDelete();
            }

            public override bool allowsEdit()
            {
                return _source.allowsEdit();
            }

            public override void save()
            {
                _source.save();
            }

            public override void load()
            {
                _source.load();
            }
        }

        private class FilterBookmarkListener : CollectionListener
        {
            private readonly AbstractHostCollection _source;

            public FilterBookmarkListener(AbstractHostCollection source)
            {
                _source = source;
            }

            public void collectionLoaded()
            {
                _source.collectionLoaded();
            }

            public void collectionItemAdded(object host)
            {
                _source.add(host as Host);
            }

            public void collectionItemRemoved(object host)
            {
                _source.remove(host as Host);
            }

            public void collectionItemChanged(object host)
            {
                _source.collectionItemChanged(host);
            }
        }
    }
}