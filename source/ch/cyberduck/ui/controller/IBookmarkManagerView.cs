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
using System.Collections;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui.controller;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface IBookmarkManagerView : IView
    {
        Host SelectedBookmark { get; }

        ImageGetterDelegate BookmarkImageGetter { set; }

        AspectGetterDelegate BookmarkNicknameGetter { set; }
        AspectGetterDelegate BookmarkHostnameGetter { set; }
        AspectGetterDelegate BookmarkUrlGetter { set; }
        AspectGetterDelegate BookmarkNotesGetter { set; }

        ImageGetterDelegate BookmarkStatusImageGetter { set; }

        void SetBookmarkModel(IEnumerable hosts);
        void RefreshBookmark(Host host);
        void AddBookmark(Host host);
        void RemoveBookmark(Host host);
        void EnsureBookmarkVisible(Host host);
        void SelectBookmark(Host host);

        event VoidHandler NewBookmark;
        event ValidateCommand ValidateNewBookmark;
        event VoidHandler EditBookmark;
        event ValidateCommand ValidateEditBookmark;
        event VoidHandler DeleteBookmark;
        event ValidateCommand ValidateDeleteBookmark;
        event VoidHandler DuplicateBookmark;
        event ValidateCommand ValidateDuplicateBookmark;
    }
}