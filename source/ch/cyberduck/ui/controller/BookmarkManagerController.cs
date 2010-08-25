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
using System;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Winforms.Serializer;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    [Obsolete]
    internal class BookmarkManagerController : WindowController<IBookmarkManagerView>, CollectionListener
    {
        public BookmarkManagerController()
        {
            View = ObjectFactory.GetInstance<IBookmarkManagerView>();
            //_browserController = browserController;

            View.EditBookmark += View_EditBookmarkEvent;
            View.NewBookmark += View_NewBookmarkEvent;
            View.DeleteBookmark += View_DeleteBookmarkEvent;
            View.BookmarkImageGetter = GetBookmarkImage;
            View.BookmarkNicknameGetter = GetNickname;
            View.BookmarkHostnameGetter = GetHostname;
            View.BookmarkUrlGetter = GetUrl;
            View.BookmarkNotesGetter = GetNotes;
            View.BookmarkStatusImageGetter = GetActiveImage;

            BookmarkCollection bookmarkCollection = BookmarkCollection.defaultCollection();
            bookmarkCollection.addListener(this);
            View.ViewClosedEvent += delegate { bookmarkCollection.removeListener(this); };
            View.SetBookmarkModel(bookmarkCollection);
        }

        private object GetActiveName(object rowobject)
        {
            return "statusGreen";            
        }

        private object GetActiveImage(object rowobject)
        {
            return IconCache.Instance.IconForName("statusGreen", 16);
        }

        public void collectionItemAdded(object host)
        {
            View.AddBookmark((Host) host);
        }

        public void collectionItemRemoved(object host)
        {
            View.RemoveBookmark((Host) host);
        }

        public void collectionItemChanged(object host)
        {
            View.RefreshBookmark((Host) host);
        }

        public object GetBookmarkImage(object host)
        {
            Host h = (Host) host;
            return
                IconCache.Instance.GetProtocolImages(64).Images[
                        h.getProtocol().getIdentifier()
                    ];
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

        public object GetHostname(object host)
        {
            return ((Host) host).getHostname();
        }

        public object GetBookmarkImageName(object host)
        {
            return ((Host) host).getProtocol().getIdentifier();
        }

        public object GetNickname(object host)
        {
            return ((Host) host).getNickname();
        }

        public object GetUrl(object host)
        {
            Host h = (Host) host;
            return h.toURL() + Path.normalize(h.getDefaultPath());
        }

        private void View_EditBookmarkEvent()
        {
            //todo immer neu?
            /*
            Host host = View.SelectedBookmark;
            BookmarkController bookmarkController =
                new BookmarkController(host);
            bookmarkController.View.Show();
            bookmarkController.View.ViewClosedEvent += delegate { HostCollection.defaultCollection().save(); };
             */
        }

        private void View_NewBookmarkEvent()
        {
            Host host =
                new Host(
                    Protocol.forName(ch.cyberduck.core.Preferences.instance().getProperty("connection.protocol.default")),
                    ch.cyberduck.core.Preferences.instance().getProperty("connection.hostname.default"),
                    ch.cyberduck.core.Preferences.instance().getInteger("connection.port.default"));

            BookmarkCollection.defaultCollection().add(host);

            //todo immer neu?
            //todo die ganze Bookmarks Geschichte ist in der OS X Version noch komplexer gelöst. Da gibt es noch Filter und ein eigenes Model dazu -> mit dko anschauen            
            /*
            BookmarkController bookmarkController =
                new BookmarkController(host);
            bookmarkController.View.Show();
            bookmarkController.View.ViewClosedEvent += delegate { HostCollection.defaultCollection().save(); };
             */
        }

        private void View_DeleteBookmarkEvent()
        {
            BookmarkCollection.defaultCollection().remove(View.SelectedBookmark);
            BookmarkCollection.defaultCollection().save();
        }
    }
}