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
using System.Collections.Generic;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Ui.Controller;

namespace ch.cyberduck.ui.controller
{
    public delegate void VoidHandler();

    public delegate System.Windows.Forms.DataObject DragHandler(ObjectListView list);
    public delegate void EndDragHandler(System.Windows.Forms.DataObject data);

    public delegate void DropHandler(OlvDropEventArgs dropArgs);
    public delegate void ModelDropHandler(ModelDropEventArgs dropArgs);

    public delegate bool RenamePathname(Path path, string newName);

    public delegate List<string> EditorsHandler();

    public delegate List<string> ArchivesHandler();
    public delegate List<KeyValuePair<String, List<String>>> CopyUrlHandler();
    public delegate List<KeyValuePair<String, List<String>>> OpenUrlHandler();
    public delegate List<Host> BookmarksHandler();

    /// <summary>
    /// This event is triggered by the new browser shortcut (main menu) and the context menu item.
    /// </summary>
    public class NewBrowserEventArgs : EventArgs
    {
        /// <summary>
        /// Open a new browser with the current selected folder as the working directory
        /// </summary>
        public bool SelectedAsWorkingDir;

        public NewBrowserEventArgs(bool selectedAsWorkingDir)
        {
            SelectedAsWorkingDir = selectedAsWorkingDir;
        }
    }

    /// <summary>
    /// This event is triggered by the 'Create Archive' submenu items.
    /// </summary>
    public class CreateArchiveEventArgs : EventArgs
    {
        public string ArchiveName;

        public CreateArchiveEventArgs(string archiveName)
        {
            ArchiveName = archiveName;
        }
    }

    public class ChangeBrowserViewArgs : EventArgs
    {
        public BrowserView View;

        public ChangeBrowserViewArgs(BrowserView view)
        {
            View = view;
        }
    }

    public class EncodingChangedArgs : EventArgs
    {
        public string Encoding;

        public EncodingChangedArgs(string encoding)
        {
            Encoding = encoding;
        }
    }

    public class InfoHelpArgs : EventArgs
    {
        public enum Context
        {
            General,
            Permissions,
            Metdadata,
            Cdn,
            S3
        }

        public Context Section;

        public InfoHelpArgs(Context section)
        {
            Section = section;
        }
    }

    public class ConnectBookmarkArgs : EventArgs
    {
        public Host Bookmark;

        public ConnectBookmarkArgs(Host bookmark)
        {
            Bookmark = bookmark;
        }
    }

    public class PrivateKeyArgs : EventArgs
    {
        public readonly string KeyFile;

        public PrivateKeyArgs(String keyFile)
        {
            KeyFile = keyFile;
        }
    }
}