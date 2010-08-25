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
// yves@langisch.ch
// 
using System;
using System.Collections;
using System.Collections.Generic;
using System.Drawing;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using Ch.Cyberduck.Ui.Winforms.Serializer;

namespace Ch.Cyberduck.Ui.Controller
{
    public enum BrowserView
    {
        File,
        Bookmark,
        History,
        Bonjour
    } ;

    public interface IBrowserView : IBookmarkManagerView
    {
        BrowserView CurrentView { set; get; }
        event EventHandler<ChangeBrowserViewArgs> ChangeBrowserView;

        // mainly used to detect a change of displayed items to update the status label
        event VoidHandler ItemsChanged;

        string WindowTitle { set; }
        string StatusLabel { set; }

        List<TreePathReference> SelectedPaths { get; }
        List<Host> SelectedBookmarks { get; }
        int NumberOfFiles { get; }
        int NumberOfBookmarks { get; }        

        string QuickConnectValue { get; }
        bool HistoryBackEnabled { set; }
        bool HistoryForwardEnabled { set; }
        bool ParentPathEnabled { set; }
        string SelectedComboboxPath { get; }
        event ValidateCommand ValidatePathsCombobox; //path history combobox
        Bitmap EditIcon { set; }
        Bitmap OpenIcon { set; }
        string SelectedEncoding { set; }

        bool SecureConnection { set; }
        bool SecureConnectionVisible { set; }
        bool CertBasedConnection { set; }

        bool ActivityRunning { set; }
        bool ShowActivityEnabled { set; }
        bool HiddenFilesVisible { set; }
        bool LogDrawerVisible { get; set; }

        void StartActivityAnimation();
        void StopActivityAnimation();

        PathFilter FilenameFilter { set; }
        string SearchString { set; get; }
        event ValidateCommand ValidateSearchField;
        List<TreePathReference> VisiblePaths { get; }

        void StartRenaming(Path path);
        void PopulateQuickConnect(List<string> nicknames);
        void PopulatePaths(List<string> paths);
        void PopulateEncodings(List<string> encodings);
        void StartSearch();
        String DownloadAsDialog(string initialDirectory, string fileName);
        String DownloadToDialog(string description, Environment.SpecialFolder root, string selectedPath);
        String[] UploadDialog(string root);
        String SynchronizeDialog(string description, Environment.SpecialFolder root, string selectedPath);

        event ValidateCommand ContextMenuEnabled;

        #region Menu - File

        // Menu - File
        event EventHandler<NewBrowserEventArgs> NewBrowser;
        //event VoidHandler NewBrowser;
        event ValidateCommand ValidateNewBrowser;
        event VoidHandler OpenConnection;
        event ValidateCommand ValidateOpenConnection;
        event VoidHandler NewDownload;
        event ValidateCommand ValidateNewDownload;
        event VoidHandler NewFolder;
        event ValidateCommand ValidateNewFolder;
        event VoidHandler NewFile;
        event ValidateCommand ValidateNewFile;
        event RenamePathname RenameFile;
        event ValidateCommand ValidateRenameFile;
        event VoidHandler DuplicateFile;
        event ValidateCommand ValidateDuplicateFile;        
        event VoidHandler OpenWebUrl;
        event ValidateCommand ValidateOpenWebUrl;        
        // todo irgendwie EditWith...
        event ValidateCommand ValidateEditWith;
        event EditorsHandler GetEditors;        
        event VoidHandler ShowInspector;
        event ValidateCommand ValidateShowInspector;
        event VoidHandler Download;
        event ValidateCommand ValidateDownload;
        event VoidHandler DownloadAs;
        event ValidateCommand ValidateDownloadAs;
        event VoidHandler DownloadTo;
        event ValidateCommand ValidateDownloadTo;
        event VoidHandler Upload;
        event ValidateCommand ValidateUpload;
        event VoidHandler Synchronize;
        event ValidateCommand ValidateSynchronize;
        event VoidHandler Delete;
        event ValidateCommand ValidateDelete;
        event VoidHandler RevertFile;
        event ValidateCommand ValidateRevertFile;
        event ArchivesHandler GetArchives;
        event BookmarksHandler GetBookmarks;
        event BookmarksHandler GetHistory;
        event VoidHandler ClearHistory;
        event BookmarksHandler GetBonjourHosts;
        event EventHandler<CreateArchiveEventArgs> CreateArchive;
        event ValidateCommand ValidateCreateArchive;
        event VoidHandler ExpandArchive;
        event ValidateCommand ValidateExpandArchive;
        event VoidHandler Exit;

        #endregion

        #region Menu - Edit

        event VoidHandler Cut;
        event ValidateCommand ValidateCut;
        event VoidHandler Copy;
        event ValidateCommand ValidateCopy;
        event VoidHandler CopyUrl;
        event ValidateCommand ValidateCopyUrl;
        event VoidHandler Paste;
        event ValidateCommand ValidatePaste;
        event ValidateCommand ValidateSelectAll;
        event VoidHandler ShowPreferences;

        #endregion

        #region Menu - View

        event VoidHandler ToggleToolbar;
        event VoidHandler ShowHiddenFiles;
        event EventHandler<EncodingChangedArgs> EncodingChanged;
        event ValidateCommand ValidateTextEncoding;
        event VoidHandler ToggleLogDrawer;

        #endregion
        
        #region Menu - Go

        event VoidHandler RefreshBrowser;
        event ValidateCommand ValidateRefresh;
        event VoidHandler GotoFolder;
        event ValidateCommand ValidateGotoFolder;
        event VoidHandler HistoryBack;
        event ValidateCommand ValidateHistoryBack;
        event VoidHandler HistoryForward;
        event ValidateCommand ValidateHistoryForward;
        event VoidHandler FolderUp;
        event ValidateCommand ValidateFolderUp;
        event VoidHandler FolderInside;
        event ValidateCommand ValidateFolderInside;
        event VoidHandler Search;
        event ValidateCommand ValidateSearch;
        event VoidHandler SendCustomCommand;
        event ValidateCommand ValidateSendCustomCommand;
        event VoidHandler Stop;
        event ValidateCommand ValidateStop;
        event VoidHandler Disconnect;
        event ValidateCommand ValidateDisconnect;

        #endregion

        #region Menu - Bookmark

        event VoidHandler ToggleBookmarks;
        event EventHandler<ConnectBookmarkArgs> ConnectBookmark;
        event ValidateCommand ValidateConnectBookmark;
        
        #endregion

        event VoidHandler ShowBookmarkManager;
        event VoidHandler SearchFieldChanged;
        event VoidHandler QuickConnect;        
        
        event VoidHandler BrowserDoubleClicked;
                    
        event VoidHandler BrowserSelectionChanged;
        event VoidHandler PathSelectionChanged;
        event VoidHandler EditEvent;
        
        event VoidHandler ShowTransfers;
        event VoidHandler ShowCertificate;

        // Drag'n'Drop events
        event DropHandler CanDrop;
        event DropHandler Dropped;
        event DragHandler Drag;

        bool ToolbarVisible { set; get; }

        void SetBrowserModel(IEnumerable<TreePathReference> model);
        void RefreshBrowserObject(TreePathReference path);
        void RefreshBrowserObjects(List<TreePathReference> list);
        void BrowserActiveStateChanged();

        // model related properties
        TreeListView.CanExpandGetterDelegate ModelCanExpandDelegate { set; }
        TreeListView.ChildrenGetterDelegate ModelChildrenGetterDelegate { set; }

        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelFilenameGetter { set; }
        TypedColumn<TreePathReference>.TypedImageGetterDelegate ModelIconGetter { set; }
        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelSizeGetter { set; }
        AspectToStringConverterDelegate ModelSizeAsStringGetter { set; }
        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelModifiedGetter { set; }

        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelOwnerGetter { set; }
        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelGroupGetter { set; }
        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelPermissionsGetter { set; }
        TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelKindGetter { set; }

        MulticolorTreeListView.ActiveGetterDelegate ModelActiveGetter { set; }

        void AddTranscriptEntry(bool request, string entry);
        void ClearTranscript();
    }
}