// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 
using System.Threading;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;

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
                    //todo hmm, wo wird dieser entfernt?
                    _filtered.addListener(new BookmarkListener(_controller, _source));
                }
                return _filtered;
            }

            set
            {
                _source.removeListener(_listener); //remove previous listener
                _source = value;
                //todo listener muss beim schliessen von browserform auch entfernt werden
                _source.addListener(_listener = new BookmarkListener(_controller, _source));
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
                IconCache.Instance.GetProtocolImages(64).Images[
                    h.getProtocol().getIdentifier()
                    ];
        }

        public object GetHostname(object host)
        {
            return ((Host) host).getHostname();
        }

        public object GetUrl(object host)
        {
            Host h = (Host) host;
            return h.toURL() + Path.normalize(h.getDefaultPath());
        }

        public object GetNotes(object host)
        {
            Host h = (Host) host;
            if (Utils.IsNotBlank(h.getComment()))
            {
                return Utils.ReplaceNewlines(h.getComment(), " ");
            }
            return string.Empty;
        }

        public object GetBookmarkStatusImage(object rowobject)
        {
            if (_controller.HasSession())
            {
                Session session = _controller.getSession();
                if (((Host) rowobject).Equals(session.getHost()))
                {
                    if (session.isConnected())
                    {
                        return IconCache.Instance.IconForName("statusGreen", 16);
                    }
                    if (session.isOpening())
                    {
                        return IconCache.Instance.IconForName("statusYellow", 16);
                    }
                }
            }
            return null;
        }

        public object GetNickname(object host)
        {
            return ((Host) host).getNickname();
        }

        private class BookmarkListener : CollectionListener
        {
            private readonly BrowserController _controller;
            private readonly AbstractHostCollection _source;
            private readonly Timer _tickler;

            public BookmarkListener(BrowserController controller, AbstractHostCollection source)
            {
                _controller = controller;
                _source = source;
                _tickler = new Timer(OnTimer, null, Timeout.Infinite, Timeout.Infinite);
            }

            public void collectionItemAdded(object host)
            {
                _controller.Invoke(() => _controller.ReloadBookmarks());
            }

            public void collectionItemRemoved(object host)
            {
                _controller.Invoke(() => _controller.ReloadBookmarks());
            }

            public void collectionItemChanged(object host)
            {
                _controller.Invoke(() => _controller.View.RefreshBookmark(host as Host));
                // Delay to 1 second. When typing changes we don't have to save every iteration.
                _tickler.Change(1000, Timeout.Infinite);
            }

            private void OnTimer(object state)
            {
                _controller.Invoke(() => _source.save());
            }
        }

        private class FilterBookmarkCollection : AbstractHostCollection
        {
            private readonly AbstractHostCollection _source;

            public FilterBookmarkCollection(AbstractHostCollection source)
            {
                _source = source;
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
    }
}