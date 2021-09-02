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
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using System.Drawing;
using static Ch.Cyberduck.ImageHelper;

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
                    _filtered = new FilterHostCollection(_source, _filter);
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
            return IconProvider.GetDisk(h.getProtocol(),
                PreferencesFactory.get().getInteger("bookmark.icon.size"));
        }

        public object GetHostname(object host)
        {
            return ((Host) host).getHostname();
        }

        public object GetUsername(object host)
        {
            Host h = (Host) host;
            return h.getCredentials().getUsername();
        }

        public object GetNotes(object host)
        {
            Host h = (Host) host;
            return Source != null ? Source.getComment(h) : string.Empty;
        }

        public object GetBookmarkStatusImage(object rowobject)
        {
            SessionPool session = _controller.Session;
            if (((Host) rowobject).Equals(session.getHost()))
            {
                if (session.getState().Equals(Session.State.open))
                {
                    return (Image)Images.StatusGreen.Size(16);
                }
                if (session.getState().Equals(Session.State.closing) ||
                    session.getState().Equals(Session.State.opening))
                {
                    return (Image)Images.StatusYellow.Size(16);
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
    }
}
