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
using System.Collections.Specialized;
using System.Drawing;
using System.IO;
using System.Threading;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.io;
using ch.cyberduck.core.serializer;
using ch.cyberduck.core.ssl;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using java.lang;
using java.security.cert;
using java.util;
using org.apache.log4j;
using StructureMap;
using Collection = ch.cyberduck.core.Collection;
using DataObject = System.Windows.Forms.DataObject;
using Locale = ch.cyberduck.core.i18n.Locale;
using Object = System.Object;
using Path = ch.cyberduck.core.Path;
using String = System.String;
using StringBuilder = System.Text.StringBuilder;
using Timer = System.Threading.Timer;

namespace Ch.Cyberduck.Ui.Controller
{
    public class BrowserController : WindowController<IBrowserView>, TranscriptListener, CollectionListener
    {
        public delegate void CallbackDelegate();

        public delegate bool DialogCallbackDelegate(DialogResult result);

        internal static readonly PathFilter HiddenFilter = new HiddenFilesPathFilter();

        private static readonly Logger Log = Logger.getLogger(typeof(BrowserController).Name);
        private static readonly PathFilter NullFilter = new NullPathFilter();
        protected static string DEFAULT = Locale.localizedString("Default");
        private readonly List<Path> _backHistory = new List<Path>();
        private readonly BookmarkCollection _bookmarkCollection = BookmarkCollection.defaultCollection();
        private readonly BookmarkModel _bookmarkModel;
        private readonly TreeBrowserModel _browserModel;
        private readonly Comparator _comparator = new NullComparator();
        private readonly List<Path> _forwardHistory = new List<Path>();
        private InfoController _inspector;
        private BrowserView _lastBookmarkView = BrowserView.Bookmark;
        private ConnectionListener _listener;

        /*
         * No file filter.
         */

        private Session _session;
        private bool _sessionShouldBeConnected;
        private bool _showHiddenFiles;
        private Path _workdir;
        private String dropFolder; // holds the drop folder of the current drag operation

        public BrowserController(IBrowserView view)
        {
            View = view;

            ShowHiddenFiles = Preferences.instance().getBoolean("browser.showHidden");

            _browserModel = new TreeBrowserModel(this);
            _bookmarkModel = new BookmarkModel(this, BookmarkCollection.defaultCollection());

            //default view is the bookmark view
            ToggleView(BrowserView.Bookmark);
            View.StopActivityAnimation();

            View.ChangeBrowserView += View_ChangeBrowserView;

            View.QuickConnect += View_QuickConnect;
            View.BrowserDoubleClicked += View_BrowserDoubleClicked;
            View.ViewShownEvent += ViewViewShownEvent;
            View.BrowserSelectionChanged += View_BrowserSelectionChanged;
            View.PathSelectionChanged += View_PathSelectionChanged;
            View.EditEvent += View_EditEvent;
            View.ItemsChanged += View_ItemsChanged;

            View.ShowTransfers += View_ShowTransfers;

            View.BrowserCanDrop += View_BrowserCanDrop;
            View.HostCanDrop += View_HostCanDrop;
            View.BrowserModelCanDrop += View_BrowserModelCanDrop;
            View.HostModelCanDrop += View_HostModelCanDrop;
            View.BrowserDropped += View_BrowserDropped;
            View.HostDropped += View_HostDropped;
            View.HostModelDropped += View_HostModelDropped;
            View.BrowserModelDropped += View_BrowserModelDropped;
            View.BrowserDrag += View_BrowserDrag;
            View.HostDrag += View_HostDrag;
            View.BrowserEndDrag += View_BrowserEndDrag;
            View.HostEndDrag += View_HostEndDrag;
            View.SearchFieldChanged += View_SearchFieldChanged;


            View.ContextMenuEnabled += View_ContextMenuEnabled;

            #region Commands - File

            View.NewBrowser += View_NewBrowser;
            View.ValidateNewBrowser += () => true;
            View.OpenConnection += View_OpenConnection;
            View.ValidateOpenConnection += () => true;
            View.NewDownload += View_NewDownload;
            View.ValidateNewDownload += () => false; //todo implement
            View.NewFolder += View_NewFolder;
            View.ValidateNewFolder += View_ValidateNewFolder;
            View.NewFile += View_NewFile;
            View.ValidateNewFile += View_ValidateNewFile;
            View.RenameFile += View_RenameFile;
            View.ValidateRenameFile += View_ValidateRenameFile;
            View.DuplicateFile += View_DuplicateFile;
            View.ValidateDuplicateFile += View_ValidateDuplicateFile;
            View.OpenUrl += View_OpenUrl;
            View.ValidateOpenWebUrl += View_ValidateOpenWebUrl;
            View.ValidateEditWith += View_ValidateEditWith;
            View.ShowInspector += View_ShowInspector;
            View.ValidateShowInspector += View_ValidateShowInspector;
            View.Download += View_Download;
            View.ValidateDownload += View_ValidateDownload;
            View.DownloadAs += View_DownloadAs;
            View.ValidateDownloadAs += View_ValidateDownloadAs;
            View.DownloadTo += View_DownloadTo;
            View.ValidateDownloadTo += View_ValidateDownload; //use same validation handler
            View.Upload += View_Upload;
            View.ValidateUpload += View_ValidateUpload;
            View.Synchronize += View_Synchronize;
            View.ValidateSynchronize += View_ValidateSynchronize;
            View.Delete += View_Delete;
            View.ValidateDelete += View_ValidateDelete;
            View.RevertFile += View_RevertFile;
            View.ValidateRevertFile += View_ValidateRevertFile;
            View.GetArchives += View_GetArchives;
            View.GetCopyUrls += View_GetCopyUrls;
            View.GetOpenUrls += View_GetOpenUrls;
            View.CreateArchive += View_CreateArchive;
            View.ValidateCreateArchive += View_ValidateCreateArchive;
            View.ExpandArchive += View_ExpandArchive;
            View.ValidateExpandArchive += View_ValidateExpandArchive;

            #endregion

            #region Commands - Edit

            View.Cut += View_Cut;
            View.ValidateCut += View_ValidateCut;
            View.Copy += View_Copy;
            View.ValidateCopy += View_ValidateCopy;
            View.Paste += View_Paste;
            View.ValidatePaste += View_ValidatePaste;
            View.ShowPreferences += View_ShowPreferences;

            #endregion

            #region Commands - View

            View.ToggleToolbar += View_ToggleToolbar;
            View.ShowHiddenFiles += View_ShowHiddenFiles;
            View.ValidateTextEncoding += View_ValidateTextEncoding;
            View.EncodingChanged += View_EncodingChanged;
            View.ToggleLogDrawer += View_ToggleLogDrawer;

            #endregion

            #region Commands - Go

            View.RefreshBrowser += View_RefreshBrowser;
            View.ValidateRefresh += View_ValidateRefresh;
            View.GotoFolder += View_GotoFolder;
            View.ValidateGotoFolder += View_ValidateGotoFolder;
            View.HistoryBack += View_HistoryBack;
            View.ValidateHistoryBack += View_ValidateHistoryBack;
            View.HistoryForward += View_HistoryForward;
            View.ValidateHistoryForward += View_ValidateHistoryForward;
            View.FolderUp += View_FolderUp;
            View.ValidateFolderUp += View_ValidateFolderUp;
            View.FolderInside += View_FolderInside;
            View.ValidateFolderInside += View_ValidateFolderInside;
            View.Search += View_Search;
            View.SendCustomCommand += View_SendCustomCommand;
            View.ValidateSendCustomCommand += View_ValidateSendCustomCommand;
            View.Stop += View_Stop;
            View.ValidateStop += View_ValidateStop;
            View.Disconnect += View_Disconnect;
            View.ValidateDisconnect += View_ValidateDisconnect;

            #endregion

            #region Commands - Bookmark

            View.ToggleBookmarks += View_ToggleBookmarks;

            View.ConnectBookmark += View_ConnectBookmark;
            View.ValidateConnectBookmark += View_ValidateConnectBookmark;
            View.NewBookmark += View_NewBookmark;
            View.ValidateNewBookmark += View_ValidateNewBookmark;
            View.EditBookmark += View_EditBookmark;
            View.ValidateEditBookmark += View_ValidateEditBookmark;
            View.DeleteBookmark += View_DeleteBookmark;
            View.ValidateDeleteBookmark += View_ValidateDeleteBookmark;
            View.DuplicateBookmark += View_DuplicateBookmark;
            View.ValidateDuplicateBookmark += View_ValidateDuplicateBookmark;

            #endregion

            #region Browser model delegates

            View.ModelCanExpandDelegate = _browserModel.CanExpand;
            View.ModelChildrenGetterDelegate = _browserModel.ChildrenGetter;
            View.ModelFilenameGetter = _browserModel.GetName;
            View.ModelIconGetter = _browserModel.GetIcon;
            View.ModelSizeGetter = _browserModel.GetSize;
            View.ModelSizeAsStringGetter = _browserModel.GetSizeAsString;
            View.ModelModifiedGetter = _browserModel.GetModified;
            View.ModelModifiedAsStringGetter = _browserModel.GetModifiedAsString;
            View.ModelOwnerGetter = _browserModel.GetOwner;
            View.ModelGroupGetter = _browserModel.GetGroup;
            View.ModelPermissionsGetter = _browserModel.GetPermission;
            View.ModelKindGetter = _browserModel.GetKind;
            View.ModelActiveGetter = _browserModel.GetActive;

            #endregion

            #region Bookmark model delegates

            View.BookmarkImageGetter = _bookmarkModel.GetBookmarkImage;
            View.BookmarkNicknameGetter = _bookmarkModel.GetNickname;
            View.BookmarkHostnameGetter = _bookmarkModel.GetHostname;
            View.BookmarkUrlGetter = _bookmarkModel.GetUrl;
            View.BookmarkNotesGetter = _bookmarkModel.GetNotes;
            View.BookmarkStatusImageGetter = _bookmarkModel.GetBookmarkStatusImage;

            #endregion

            _bookmarkCollection.addListener(this);

            PopulateQuickConnect();
            PopulateEncodings();
            UpdateOpenIcon();

            View.ToolbarVisible = Preferences.instance().getBoolean("browser.toolbar");

            //todo
            View.GetEditors += delegate { return new List<string> { "TODO" }; };
            View.GetBookmarks += View_GetBookmarks;
            View.GetHistory += View_GetHistory;
            View.GetBonjourHosts += View_GetBonjourHosts;
            View.ClearHistory += View_ClearHistory;
            View.ShowCertificate += View_Certificate;

            View.ValidatePathsCombobox += View_ValidatePathsCombobox;
            View.ValidateSearchField += View_ValidateSearchField;

            //View.ViewClosingEvent += View_ClosingEvent;
            View.Exit += View_Exit;

            BookmarkCollection bookmarkCollection = BookmarkCollection.defaultCollection();
            //todo eigene ListenerKlasse muss her
            //hostCollection.addListener(this);
            View.ViewClosedEvent += delegate { bookmarkCollection.removeListener(this); };
            View.SetBookmarkModel(bookmarkCollection);
        }

        public BrowserController()
            : this(ObjectFactory.GetInstance<IBrowserView>())
        {
        }

        /// <summary>
        /// The first selected path found or null if there is no selection
        /// </summary>
        public Path SelectedPath
        {
            get
            {
                List<TreePathReference> selectedPaths = View.SelectedPaths;
                if (selectedPaths.Count > 0)
                {
                    return selectedPaths[0].Unique;
                }
                return null;
            }
        }

        public List<Path> BackHistory
        {
            get { return _backHistory; }
        }

        public List<Path> ForwardHistory
        {
            get { return _forwardHistory; }
        }

        public Path Workdir
        {
            get { return _workdir; }
            set { _workdir = value; }
        }

        /// <summary>
        ///
        /// </summary>
        /// <value>
        ///   All selected paths or an empty list if there is no selection
        /// </value>
        public List<Path> SelectedPaths
        {
            get
            {
                if (IsMounted())
                {
                    List<Path> selected = new List<Path>();
                    foreach (TreePathReference reference in View.SelectedPaths)
                    {
                        selected.Add(reference.Unique);
                    }
                    return selected;
                }
                return new List<Path>();
            }
        }

        public bool ShowHiddenFiles
        {
            get { return _showHiddenFiles; }
            set
            {
                FilenameFilter = value ? NullFilter : HiddenFilter;
                _showHiddenFiles = value;
                View.HiddenFilesVisible = _showHiddenFiles;
            }
        }

        public PathFilter FilenameFilter { get; set; }

        public Comparator FilenameComparator
        {
            get { return _comparator; }
        }

        public bool SessionShouldBeConnected
        {
            set { _sessionShouldBeConnected = value; }
        }

        public void collectionLoaded()
        {
            ;
        }

        public void collectionItemAdded(object obj)
        {
            AsyncDelegate mainAction = delegate { PopulateQuickConnect(); };
            Invoke(mainAction);
        }

        public void collectionItemRemoved(object obj)
        {
            AsyncDelegate mainAction = delegate { PopulateQuickConnect(); };
            Invoke(mainAction);
        }

        public void collectionItemChanged(object obj)
        {
            AsyncDelegate mainAction = delegate { PopulateQuickConnect(); };
            Invoke(mainAction);
        }

        public void log(bool request, string transcript)
        {
            if (View.LogDrawerVisible)
            {
                AsyncDelegate mainAction = delegate { View.AddTranscriptEntry(request, transcript); };
                Invoke(mainAction);
            }
        }

        private List<KeyValuePair<String, List<String>>> View_GetCopyUrls()
        {
            List<KeyValuePair<String, List<String>>> items = new List<KeyValuePair<String, List<String>>>();
            List<TreePathReference> selected = View.SelectedPaths;
            if (selected.Count == 0)
            {
                items.Add(
                    new KeyValuePair<string, List<String>>(
                        String.Format(Locale.localizedString("{0} URL"), Locale.localizedString("Unknown")),
                        new List<string>()));
            }
            else
            {
                for (int i = 0; i < SelectedPath.getURLs().size(); i++)
                {
                    AbstractPath.DescriptiveUrl descUrl =
                        (AbstractPath.DescriptiveUrl)SelectedPath.getURLs().toArray()[i];
                    KeyValuePair<String, List<String>> entry =
                        new KeyValuePair<string, List<string>>(descUrl.getHelp(), new List<string>());
                    items.Add(entry);

                    foreach (TreePathReference reference in selected)
                    {
                        entry.Value.Add(((AbstractPath.DescriptiveUrl)reference.Unique.getURLs().toArray()[i]).getUrl());
                    }
                }
            }
            return items;
        }

        private List<KeyValuePair<String, List<String>>> View_GetOpenUrls()
        {
            List<KeyValuePair<String, List<String>>> items = new List<KeyValuePair<String, List<String>>>();
            List<TreePathReference> selected = View.SelectedPaths;
            if (selected.Count == 0)
            {
                items.Add(
                    new KeyValuePair<string, List<String>>(
                        String.Format(Locale.localizedString("{0} URL"), Locale.localizedString("Unknown")),
                        new List<string>()));
            }
            else
            {
                for (int i = 0; i < SelectedPath.getHttpURLs().size(); i++)
                {
                    AbstractPath.DescriptiveUrl descUrl =
                        (AbstractPath.DescriptiveUrl)SelectedPath.getHttpURLs().toArray()[i];
                    KeyValuePair<String, List<String>> entry =
                        new KeyValuePair<string, List<string>>(descUrl.getHelp(), new List<string>());
                    items.Add(entry);

                    foreach (TreePathReference reference in selected)
                    {
                        entry.Value.Add(((AbstractPath.DescriptiveUrl)reference.Unique.getHttpURLs().toArray()[i]).getUrl());
                    }
                }
            }
            return items;
        }

        private bool View_ValidateDuplicateBookmark()
        {
            return _bookmarkModel.Source.allowsEdit() && View.SelectedBookmarks.Count == 1;
        }

        private void View_DuplicateBookmark()
        {
            ToggleView(BrowserView.Bookmark);
            Host duplicate = new Host(View.SelectedBookmark.getAsDictionary());
            // Make sure a new UUID is asssigned for duplicate
            duplicate.setUuid(null);
            AddBookmark(duplicate);
        }

        private void View_HostModelDropped(ModelDropEventArgs dropargs)
        {
            int sourceIndex = _bookmarkModel.Source.indexOf(dropargs.SourceModels[0]);
            int destIndex = dropargs.DropTargetIndex;
            if (dropargs.DropTargetLocation == DropTargetLocation.BelowItem)
            {
                destIndex++;
            }
            if (dropargs.Effect == DragDropEffects.Copy)
            {
                Host host = new Host(((Host)dropargs.SourceModels[0]).getAsDictionary());
                host.setUuid(null);
                AddBookmark(host, destIndex);
            }
            if (dropargs.Effect == DragDropEffects.Move)
            {
                if (sourceIndex < destIndex)
                {
                    destIndex--;
                }
                foreach (Host promisedDragBookmark in dropargs.SourceModels)
                {
                    _bookmarkModel.Source.remove(promisedDragBookmark);
                    if (destIndex > _bookmarkModel.Source.size())
                    {
                        _bookmarkModel.Source.add(promisedDragBookmark);
                    }
                    else
                    {
                        _bookmarkModel.Source.add(destIndex, promisedDragBookmark);
                    }
                    //view.selectRowIndexes(NSIndexSet.indexSetWithIndex(row), false);
                    //view.scrollRowToVisible(row);
                }
            }
        }

        private void View_HostModelCanDrop(ModelDropEventArgs args)
        {
            if (!_bookmarkModel.Source.allowsEdit())
            {
                // Do not allow drags for non writable collections
                args.Effect = DragDropEffects.None;
                args.DropTargetLocation = DropTargetLocation.None;
                return;
            }
            switch (args.DropTargetLocation)
            {
                case DropTargetLocation.BelowItem:
                case DropTargetLocation.AboveItem:
                    if (args.SourceModels.Count > 1)
                    {
                        args.Effect = DragDropEffects.Move;
                    }
                    break;
                default:
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                    return;
            }
        }

        private void View_HostDropped(OlvDropEventArgs e)
        {
            if (e.DataObject is DataObject && ((DataObject)e.DataObject).ContainsFileDropList())
            {
                DataObject data = (DataObject)e.DataObject;

                if (e.DropTargetLocation == DropTargetLocation.Item)
                {
                    foreach (string file in data.GetFileDropList())
                    {
                        //check if we received at least one non-duck file
                        if (!".duck".Equals(Utils.GetSafeExtension(file)))
                        {
                            // The bookmark this file has been dropped onto
                            Host destination = (Host)e.DropTargetItem.RowObject;
                            IList<Path> roots = new List<Path>();
                            Session session = null;
                            foreach (string upload in data.GetFileDropList())
                            {
                                if (null == session)
                                {
                                    session = SessionFactory.createSession(destination);
                                }
                                // Upload to the remote host this bookmark points to
                                roots.Add(PathFactory.createPath(session,
                                                                 destination.getDefaultPath(),
                                                                 LocalFactory.createLocal(upload)));
                            }
                            if (roots.Count > 0)
                            {
                                UploadTransfer q = new UploadTransfer(Utils.ConvertToJavaList(roots));
                                // If anything has been added to the queue, then process the queue
                                if (q.numberOfRoots() > 0)
                                {
                                    TransferController.Instance.StartTransfer(q);
                                }
                            }
                        }
                    }
                    return;
                }

                if (e.DropTargetLocation == DropTargetLocation.AboveItem)
                {
                    Host destination = (Host)e.DropTargetItem.RowObject;
                    foreach (string file in data.GetFileDropList())
                    {
                        _bookmarkModel.Source.add(_bookmarkModel.Source.indexOf(destination),
                                                  HostReaderFactory.instance().read(LocalFactory.createLocal(file)));
                    }
                }
                if (e.DropTargetLocation == DropTargetLocation.BelowItem)
                {
                    Host destination = (Host)e.DropTargetItem.RowObject;
                    foreach (string file in data.GetFileDropList())
                    {
                        _bookmarkModel.Source.add(_bookmarkModel.Source.indexOf(destination) + 1,
                                                  HostReaderFactory.instance().read(LocalFactory.createLocal(file)));
                    }
                }
                if (e.DropTargetLocation == DropTargetLocation.Background)
                {
                    foreach (string file in data.GetFileDropList())
                    {
                        _bookmarkModel.Source.add(HostReaderFactory.instance().read(LocalFactory.createLocal(file)));
                    }
                }
            }
        }

        private void View_HostCanDrop(OlvDropEventArgs args)
        {
            if (!_bookmarkModel.Source.allowsEdit())
            {
                // Do not allow drags for non writable collections
                args.Effect = DragDropEffects.None;
                args.DropTargetLocation = DropTargetLocation.None;
                return;
            }

            DataObject dataObject = (DataObject)args.DataObject;
            if (dataObject.ContainsFileDropList())
            {
                //check if all files are .duck files
                foreach (string file in dataObject.GetFileDropList())
                {
                    string ext = Utils.GetSafeExtension(file);
                    if (!".duck".Equals(ext))
                    {
                        //if at least one non-duck file we prepare for uploading
                        args.Effect = DragDropEffects.Copy;
                        if (args.DropTargetLocation == DropTargetLocation.Item)
                        {
                            Host destination = (Host)args.DropTargetItem.RowObject;
                            (args.DataObject as DataObject).SetDropDescription((DropImageType)args.Effect,
                                                                               "Upload to %1",
                                                                               destination.getNickname());
                        }
                        args.DropTargetLocation = DropTargetLocation.Item;
                        return;
                    }
                }

                //at least one .duck file
                args.Effect = DragDropEffects.Copy;
                if (args.DropTargetLocation == DropTargetLocation.Item)
                {
                    args.DropTargetLocation = DropTargetLocation.Background;
                }
                return;
            }
            args.Effect = DragDropEffects.None;
        }

        private void View_HostEndDrag(DataObject data)
        {
            RemoveTemporaryFiles(data);
        }

        private string CreateAndWatchTemporaryFile(FileSystemEventHandler del)
        {
            string tfile = System.IO.Path.Combine(System.IO.Path.GetTempPath(),
                                                  Guid.NewGuid().ToString());
            using (File.Create(tfile))
            {
                FileInfo tmpFile = new FileInfo(tfile);
                tmpFile.Attributes |= FileAttributes.Hidden;
                tmpFile.Attributes |= FileAttributes.System; //good idea?
            }

            DriveInfo[] allDrives = DriveInfo.GetDrives();
            foreach (DriveInfo d in allDrives)
            {
                if (d.IsReady && d.DriveType != DriveType.CDRom)
                {
                    FileSystemWatcher watcher = new FileSystemWatcher(@d.Name, System.IO.Path.GetFileName(tfile));
                    watcher.IncludeSubdirectories = true;
                    watcher.EnableRaisingEvents = true;
                    watcher.Created += del;
                    watcher.Created += delegate { watcher.Dispose(); };
                }
            }
            return tfile;
        }

        private DataObject View_HostDrag(ObjectListView list)
        {
            DataObject data = new DataObject(DataFormats.FileDrop, new[]
                                                                       {
                                                                           CreateAndWatchTemporaryFile(
                                                                               delegate(object sender,
                                                                                        FileSystemEventArgs args)
                                                                                   {
                                                                                       Invoke(delegate
                                                                                                  {
                                                                                                      dropFolder =
                                                                                                          System.IO.Path
                                                                                                              .
                                                                                                              GetDirectoryName
                                                                                                              (
                                                                                                                  args.
                                                                                                                      FullPath);
                                                                                                      foreach (
                                                                                                          Host host in
                                                                                                              View.
                                                                                                                  SelectedBookmarks
                                                                                                          )
                                                                                                      {
                                                                                                          string
                                                                                                              filename =
                                                                                                                  host.
                                                                                                                      getNickname
                                                                                                                      () +
                                                                                                                  ".duck";
                                                                                                          foreach (
                                                                                                              char c in
                                                                                                                  System
                                                                                                                      .
                                                                                                                      IO
                                                                                                                      .
                                                                                                                      Path
                                                                                                                      .
                                                                                                                      GetInvalidFileNameChars
                                                                                                                      ()
                                                                                                              )
                                                                                                          {
                                                                                                              filename =
                                                                                                                  filename
                                                                                                                      .
                                                                                                                      Replace
                                                                                                                      (
                                                                                                                          c
                                                                                                                              .
                                                                                                                              ToString
                                                                                                                              (),
                                                                                                                          "");
                                                                                                          }

                                                                                                          Local file =
                                                                                                              LocalFactory
                                                                                                                  .
                                                                                                                  createLocal
                                                                                                                  (
                                                                                                                      dropFolder,
                                                                                                                      filename);
                                                                                                          HostWriterFactory
                                                                                                              .instance()
                                                                                                              .
                                                                                                              write(
                                                                                                                  host,
                                                                                                                  file);
                                                                                                      }
                                                                                                  });
                                                                                   }
                                                                               )
                                                                       });
            return data;
        }

        private void View_BrowserModelCanDrop(ModelDropEventArgs args)
        {
            if (IsMounted())
            {
                Path destination;
                switch (args.DropTargetLocation)
                {
                    case DropTargetLocation.Item:
                        destination = ((TreePathReference)args.DropTargetItem.RowObject).Unique;
                        if (!destination.attributes().isDirectory())
                        {
                            //dragging over file
                            destination = destination.getParent();
                        }
                        break;
                    case DropTargetLocation.Background:
                        destination = Workdir;
                        break;
                    default:
                        args.Effect = DragDropEffects.None;
                        args.DropTargetLocation = DropTargetLocation.None;
                        return;
                }
                if (!getSession().isCreateFileSupported(destination))
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                    return;
                }
                foreach (TreePathReference sourceTreePath in args.SourceModels)
                {
                    Path sourcePath = sourceTreePath.Unique;
                    if (sourcePath.attributes().isDirectory() && sourcePath.equals(destination))
                    {
                        // Do not allow dragging onto myself.
                        args.Effect = DragDropEffects.None;
                        args.DropTargetLocation = DropTargetLocation.None;
                        return;
                    }
                    if (sourcePath.attributes().isDirectory() && destination.isChild(sourcePath))
                    {
                        // Do not allow dragging a directory into its own containing items
                        args.Effect = DragDropEffects.None;
                        args.DropTargetLocation = DropTargetLocation.None;
                        return;
                    }
                    if (sourcePath.attributes().isFile() && sourcePath.getParent().equals(destination))
                    {
                        // Moving file to the same destination makes no sense
                        args.Effect = DragDropEffects.None;
                        args.DropTargetLocation = DropTargetLocation.None;
                        return;
                    }
                }
                if (Workdir == destination)
                {
                    args.DropTargetLocation = DropTargetLocation.Background;
                }
                else
                {
                    args.DropTargetItem = args.ListView.ModelToItem(new TreePathReference(destination));
                }
            }
        }

        /// <summary>
        /// A file dragged within the browser has been received
        /// </summary>
        /// <param name="dropargs"></param>
        private void View_BrowserModelDropped(ModelDropEventArgs dropargs)
        {
            Path destination;
            switch (dropargs.DropTargetLocation)
            {
                case DropTargetLocation.Item:
                    destination = ((TreePathReference)dropargs.DropTargetItem.RowObject).Unique;
                    break;
                case DropTargetLocation.Background:
                    destination = Workdir;
                    break;
                default:
                    destination = null;
                    break;
            }

            if (null != destination)
            {
                IDictionary<Path, Path> files = new Dictionary<Path, Path>();
                if (dropargs.Effect == DragDropEffects.Move)
                {
                    foreach (TreePathReference reference in dropargs.SourceModels)
                    {
                        Path next = reference.Unique;
                        Path renamed = PathFactory.createPath(getSession(), destination.getAbsolute(), next.getName(),
                                                              next.attributes().getType());
                        files.Add(next, renamed);
                    }
                    RenamePaths(files);
                }
                if (dropargs.Effect == DragDropEffects.Copy)
                {
                    foreach (TreePathReference reference in dropargs.SourceModels)
                    {
                        Path next = reference.Unique;
                        Path copy = PathFactory.createPath(getSession(), next.getAsDictionary());
                        copy.setPath(destination.getAbsolute(), next.getName());
                        files.Add(next, copy);
                    }
                    DuplicatePaths(files, false);
                }
            }
        }

        private void View_Download()
        {
            Download(SelectedPaths);
        }

        private bool View_ValidateRevertFile()
        {
            if (IsMounted() && SelectedPaths.Count == 1)
            {
                return getSession().isRevertSupported();
            }
            return false;
        }

        private void View_RevertFile()
        {
            RevertPath(SelectedPath);
        }

        private void RevertPath(Path selected)
        {
            Background(new RevertPathAction(this, selected));
        }

        private void View_ToggleBookmarks()
        {
            if (View.CurrentView == BrowserView.File)
            {
                View.CurrentView = _lastBookmarkView;
            }
            else
            {
                _lastBookmarkView = View.CurrentView;
                View.CurrentView = BrowserView.File;
            }
        }

        private bool View_ValidateSearchField()
        {
            return IsMounted() || View.CurrentView != BrowserView.File;
        }

        private bool View_ValidatePathsCombobox()
        {
            return IsMounted();
        }

        private void View_ItemsChanged()
        {
            UpdateStatusLabel();
        }

        private void View_Certificate()
        {
            if (_session is SSLSession)
            {
                X509Certificate[] certificates =
                    ((SSLSession)_session).getTrustManager().getAcceptedIssuers();
                if (0 == certificates.Length)
                {
                    Log.warn("No accepted certificates found");
                    return;
                }
                //todo: dialog does not have the focus, means we have to click twice to close the form. why?
                KeychainFactory.instance().displayCertificates(certificates);
            }
        }

        private void View_ClearHistory()
        {
            HistoryCollection.defaultCollection().clear();
        }

        private List<Host> View_GetBonjourHosts()
        {
            List<Host> b = new List<Host>();
            foreach (Host h in RendezvousCollection.defaultCollection())
            {
                b.Add(h);
            }
            return b;
        }

        private List<Host> View_GetHistory()
        {
            List<Host> b = new List<Host>();
            foreach (Host h in HistoryCollection.defaultCollection())
            {
                b.Add(h);
            }
            return b;
        }

        private List<Host> View_GetBookmarks()
        {
            List<Host> b = new List<Host>();
            foreach (Host h in BookmarkCollection.defaultCollection())
            {
                b.Add(h);
            }
            return b;
        }

        private void PopulateEncodings()
        {
            List<string> list = new List<string>();
            list.AddRange(Utils.AvailableCharsets());

            View.PopulateEncodings(list);
            //default to UTF-8
            View.SelectedEncoding = "UTF-8";
        }

        private void View_EncodingChanged(object sender, EncodingChangedArgs e)
        {
            string encoding = e.Encoding;
            if (Utils.IsBlank(encoding))
            {
                return;
            }
            View.SelectedEncoding = encoding;
            if (IsMounted())
            {
                if (_session.getEncoding().Equals(encoding))
                {
                    return;
                }
                background(new EncodingBrowserBackgroundAction(this, encoding));
            }
        }

        private void View_ConnectBookmark(object sender, ConnectBookmarkArgs connectBookmarkArgs)
        {
            Mount(connectBookmarkArgs.Bookmark);
        }

        private bool View_ValidateConnectBookmark()
        {
            return View.SelectedBookmarks.Count == 1;
        }

        private bool View_ValidateDeleteBookmark()
        {
            return _bookmarkModel.Source.allowsDelete() && View.SelectedBookmarks.Count > 0;
        }

        private bool View_ValidateEditBookmark()
        {
            return _bookmarkModel.Source.allowsEdit() && View.SelectedBookmarks.Count == 1;
        }

        private bool View_ValidateNewBookmark()
        {
            return _bookmarkModel.Source.allowsAdd();
        }

        private void View_ChangeBrowserView(object sender, ChangeBrowserViewArgs e)
        {
            ToggleView(e.View);
        }

        private void View_EditBookmark()
        {
            if (View.SelectedBookmarks.Count == 1)
            {
                BookmarkController.Factory.Create(View.SelectedBookmark).View.Show(View);
            }
            //todo: was ist mit speichern? wird das irgendwo sonst erledigt?
        }

        private void View_NewBookmark()
        {
            Host bookmark;
            if (IsMounted())
            {
                Path selected = SelectedPath;
                if (null == selected || !selected.attributes().isDirectory())
                {
                    selected = Workdir;
                }
                bookmark = new Host(_session.getHost().getAsDictionary());
                bookmark.setUuid(null);
                bookmark.setDefaultPath(selected.getAbsolute());
            }
            else
            {
                bookmark =
                    new Host(
                        Protocol.forName(
                            Preferences.instance().getProperty("connection.protocol.default")),
                        Preferences.instance().getProperty("connection.hostname.default"),
                        Preferences.instance().getInteger("connection.port.default"));
            }
            ToggleView(BrowserView.Bookmark);
            AddBookmark(bookmark);
        }

        private void AddBookmark(Host item)
        {
            AddBookmark(item, -1);
        }

        private void AddBookmark(Host item, int index)
        {
            _bookmarkModel.Filter = null;
            //todo prüfen wieso das Adden so teuer ist. Wohl wegen den Listener. Aber welche?
            if (index != -1)
            {
                _bookmarkModel.Source.add(index, item);
            }
            else
            {
                _bookmarkModel.Source.add(item);
            }
            View.SelectBookmark(item);
            View.EnsureBookmarkVisible(item);
            BookmarkController.Factory.Create(item).View.Show(View);
        }

        private void View_DeleteBookmark()
        {
            List<Host> selected = View.SelectedBookmarks;
            //View.SelectBookmark(host);
            //View.EnsureBookmarkVisible(host);
            StringBuilder alertText = new StringBuilder();
            int i = 0;
            foreach (Host host in selected)
            {
                if (i > 0)
                {
                    alertText.Append("\n");
                }
                alertText.Append(Character.toString('\u2022')).Append(" ").Append(host.getNickname());
                i++;
                if (i > 10)
                {
                    break;
                }
            }
            DialogResult result = MessageBox(Locale.localizedString("Delete Bookmark"),
                                             Locale.localizedString(
                                                 "Do you want to delete the selected bookmark?"),
                                             alertText.ToString(),
                                             eTaskDialogButtons.OKCancel,
                                             eSysIcons.Question
                );
            if (result == DialogResult.OK)
            {
                _bookmarkModel.Source.removeAll(Utils.ConvertToJavaList(selected));
            }
        }

        public override bool ViewShouldClose()
        {
            return Unmount();
        }

        protected override void Invalidate()
        {
            if (HasSession())
            {
                _session.removeConnectionListener(_listener);
            }
            _bookmarkCollection.removeListener(this);
        }

        private void View_OpenUrl()
        {
            foreach (Path selected in SelectedPaths)
            {
                Utils.StartProcess(selected.toHttpURL());
            }
        }

        private void View_SearchFieldChanged()
        {
            if (View.CurrentView == BrowserView.File)
            {
                SetPathFilter(View.SearchString);
            }
            else
            {
                SetBookmarkFilter(View.SearchString);
            }
        }

        private void SetBookmarkFilter(string searchString)
        {
            if (Utils.IsBlank(searchString))
            {
                View.SearchString = String.Empty;
                _bookmarkModel.Filter = null;
            }
            else
            {
                _bookmarkModel.Filter = new BookmarkFilter(searchString);
            }
            ReloadBookmarks();
        }

        private bool View_ValidateDisconnect()
        {
            // disconnect/stop button update
            View.ActivityRunning = IsActivityRunning();
            View.ShowActivityEnabled = IsActivityRunning() || IsMounted();

            if (!IsConnected())
            {
                return IsActivityRunning();
            }
            return IsConnected();
        }

        private bool View_ValidateStop()
        {
            return IsActivityRunning();
        }

        private bool View_ValidateSendCustomCommand()
        {
            return false;
            return IsMounted() && getSession().isSendCommandSupported(); //todo implement Send custom command
        }

        private bool View_ValidateFolderInside()
        {
            return IsMounted() && SelectedPaths.Count > 0;
        }

        private bool View_ValidateFolderUp()
        {
            return IsMounted() && !Workdir.isRoot();
        }

        private bool View_ValidateHistoryForward()
        {
            return IsMounted() && ForwardHistory.Count > 0;
        }

        private bool View_ValidateHistoryBack()
        {
            return IsMounted() && BackHistory.Count > 1;
        }

        private bool View_ValidateGotoFolder()
        {
            return false; //todo
            return IsMounted();
        }

        private bool View_ValidateRefresh()
        {
            return IsMounted();
        }

        private void View_Disconnect()
        {
            if (IsActivityRunning())
            {
                View_Stop();
            }
            else
            {
                Disconnect();
            }
        }

        /// <summary>
        /// Unmount this session
        /// </summary>
        private void Disconnect()
        {
            Background(new DisconnectAction(this));
        }

        private void View_Stop()
        {
            // Remove all pending actions)
            foreach (BackgroundAction action in BackgroundActionRegistry.instance().toArray(
                new BackgroundAction[BackgroundActionRegistry.instance().size()]))
            {
                action.cancel();
            }
            // Interrupt any pending operation by forcefully closing the socket
            Interrupt();
        }

        private void View_SendCustomCommand()
        {
            //todo implement
            throw new NotImplementedException();
        }

        private void View_Search()
        {
            View.StartSearch();
        }

        private void View_FolderInside()
        {
            Path selected = SelectedPath;
            if (null == selected)
            {
                return;
            }
            if (selected.attributes().isDirectory())
            {
                SetWorkdir(selected);
            }
            else if (selected.attributes().isFile() || View.SelectedPaths.Count > 1)
            {
                if (Preferences.instance().getBoolean("browser.doubleclick.edit"))
                {
                    View_EditEvent();
                }
                else
                {
                    View_Download();
                }
            }
        }

        /// <summary>
        /// Download to default download directory.
        /// </summary>
        /// <param name="downloads"></param>
        public void Download(List<Path> downloads)
        {
            Download(downloads, null);
        }

        public void Download(List<Path> downloads, Local downloadFolder)
        {
            Session session = getTransferSession();
            List roots = new Collection();
            foreach (Path selected in SelectedPaths)
            {
                Path path = PathFactory.createPath(session, selected.getAsDictionary());
                if (null == downloadFolder)
                {
                    path.setLocal(null);
                }
                else
                {
                    path.setLocal(LocalFactory.createLocal(downloadFolder, path.getName()));
                }
                roots.add(path);
            }
            Transfer q = new DownloadTransfer(roots);
            transfer(q);
        }

        private void View_GotoFolder()
        {
            //todo implement
            throw new NotImplementedException();
        }

        private void View_RefreshBrowser()
        {
            if (IsMounted())
            {
                Workdir.invalidate();
                foreach (TreePathReference reference in View.VisiblePaths)
                {
                    if (null == reference) continue;
                    reference.Unique.invalidate();
                }
                ReloadData(SelectedPaths);
            }
        }

        private bool View_ValidateTextEncoding()
        {
            return IsMounted() && !IsActivityRunning();
        }

        private void View_ToggleLogDrawer()
        {
            View.LogDrawerVisible = !View.LogDrawerVisible;
            Preferences.instance().setProperty("browser.logDrawer.isOpen", View.LogDrawerVisible);
        }

        private void View_ShowHiddenFiles()
        {
            ShowHiddenFiles = !ShowHiddenFiles;
            ReloadData(true);
        }

        private void View_ToggleToolbar()
        {
            View.ToolbarVisible = !View.ToolbarVisible;
            Preferences.instance().setProperty("browser.toolbar", View.ToolbarVisible);
        }

        private bool View_ValidatePaste()
        {
            //todo implement
            return false;
        }

        private void View_Paste()
        {
            //todo implement
            throw new NotImplementedException();
        }

        private bool View_ValidateCopy()
        {
            return false;
            return IsMounted() && SelectedPaths.Count > 0;
        }

        private void View_Copy()
        {
            //todo implement
            throw new NotImplementedException();
        }

        private bool View_ValidateCut()
        {
            return false;
            return IsMounted() && SelectedPaths.Count > 0;
        }

        private void View_Cut()
        {
            //todo implement
            return;
            throw new NotImplementedException();
        }

        private void View_ShowPreferences()
        {
            PreferencesController.Instance.View.Show();
        }

        private bool View_ContextMenuEnabled()
        {
            //context menu is always enabled
            return true;
        }

        private void View_Exit()
        {
            MainController.Exit();
        }

        private List<string> View_GetArchives()
        {
            List<string> result = new List<string>();
            Archive[] archives = Archive.getKnownArchives();
            foreach (Archive archive in archives)
            {
                List selected = Utils.ConvertToJavaList(SelectedPaths, null);
                result.Add(archive.getTitle(selected));
            }
            return result;
        }

        private bool View_ValidateExpandArchive()
        {
            if (IsMounted())
            {
                if (!getSession().isUnarchiveSupported())
                {
                    return false;
                }
                if (SelectedPaths.Count > 0)
                {
                    foreach (Path selected in SelectedPaths)
                    {
                        if (selected.attributes().isDirectory())
                        {
                            return false;
                        }
                        if (!Archive.isArchive(selected.getName()))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        private void View_ExpandArchive()
        {
            List<Path> expanded = new List<Path>();
            foreach (Path selected in SelectedPaths)
            {
                Archive archive = Archive.forName(selected.getName());
                if (null == archive)
                {
                    continue;
                }
                CheckOverwrite(Utils.ConvertFromJavaList<Path>(archive.getExpanded(new ArrayList { selected })),
                               new UnarchiveAction(this, archive, selected, expanded));
            }
        }

        private bool View_ValidateCreateArchive()
        {
            if (IsMounted())
            {
                if (!getSession().isArchiveSupported())
                {
                    return false;
                }
                if (SelectedPaths.Count > 0)
                {
                    foreach (Path selected in SelectedPaths)
                    {
                        if (selected.attributes().isFile() && Archive.isArchive(selected.getName()))
                        {
                            // At least one file selected is already an archive. No distinct action possible
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        private void View_CreateArchive(object sender, CreateArchiveEventArgs createArchiveEventArgs)
        {
            Archive archive = Archive.forName(createArchiveEventArgs.ArchiveName);
            List<Path> selected = SelectedPaths;
            CheckOverwrite(new List<Path> { archive.getArchive(Utils.ConvertToJavaList(selected)) },
                           new CreateArchiveAction(this, archive, selected));
        }

        private bool View_ValidateDelete()
        {
            return IsMounted() && SelectedPaths.Count > 0;
        }

        private bool View_ValidateSynchronize()
        {
            return IsMounted();
        }

        private void View_Synchronize()
        {
            Path selection;
            if (SelectedPaths.Count == 1 && SelectedPath.attributes().isDirectory())
            {
                selection = SelectedPath;
            }
            else
            {
                selection = Workdir;
            }
            string folder = View.SynchronizeDialog(Locale.localizedString("Synchronize")
                                                   + " " + selection.getName() + " "
                                                   + Locale.localizedString("with"), Environment.SpecialFolder.Desktop,
                                                   null);
            if (null != folder)
            {
                selection = PathFactory.createPath(getTransferSession(), selection.getAsDictionary());
                selection.setLocal(LocalFactory.createLocal(folder));
                Transfer q = new SyncTransfer(selection);
                transfer(q, selection);
            }
        }

        private bool View_ValidateUpload()
        {
            return IsMounted();
        }

        private void View_Upload()
        {
            // Due to the limited functionality of the OpenFileDialog class it is
            // currently not possible to select a folder. May be we should provide
            // a second menu item which allows to select a folder to upload
            string[] paths = View.UploadDialog(null);
            if (null == paths) return;

            Path destination = SelectedPath;
            if (null == destination)
            {
                destination = Workdir;
            }
            else if (!destination.attributes().isDirectory())
            {
                destination = destination.getParent();
            }
            Session session = getTransferSession();
            List roots = Utils.ConvertToJavaList(paths, path => PathFactory.createPath(session,
                                                                                       destination.getAbsolute(),
                                                                                       LocalFactory.createLocal(path)));
            Transfer q = new UploadTransfer(roots);
            transfer(q, destination);
        }

        private void View_DownloadTo()
        {
            string folderName = View.DownloadToDialog(Locale.localizedString("Download To…"),
                                                      Environment.SpecialFolder.Desktop, null);
            if (null != folderName)
            {
                Session session = getTransferSession();
                Utils.ApplyPerItemForwardDelegate<Path> apply = delegate(Path item)
                {
                    Path path = PathFactory.createPath(session,
                                                       item.
                                                           getAsDictionary
                                                           ());
                    path.setLocal(
                        LocalFactory.createLocal(folderName,
                                                 path.getLocal().
                                                     getName()));
                    return path;
                };
                Transfer q = new DownloadTransfer(Utils.ConvertToJavaList(SelectedPaths, apply));
                transfer(q);
            }
        }

        private bool View_ValidateDownloadAs()
        {
            if (IsMounted() && SelectedPaths.Count == 1)
            {
                Path selected = SelectedPath;
                if (null == selected)
                {
                    return false;
                }
                return !selected.attributes().isVolume();
            }
            return false;
        }

        private void View_DownloadAs()
        {
            string fileName = View.DownloadAsDialog(null, SelectedPath.getLocal().getDisplayName());
            if (null != fileName)
            {
                Path selection = PathFactory.createPath(getTransferSession(), SelectedPath.getAsDictionary());
                selection.setLocal(LocalFactory.createLocal(fileName));
                Transfer q = new DownloadTransfer(selection);
                transfer(q);
            }
        }

        private bool View_ValidateDownload()
        {
            if (IsMounted() && SelectedPaths.Count > 0)
            {
                Path selected = SelectedPath;
                if (null == selected)
                {
                    return false;
                }
                return !selected.attributes().isVolume();
            }
            return false;
        }

        private bool View_ValidateShowInspector()
        {
            return IsMounted() && SelectedPaths.Count > 0;
        }

        private bool View_ValidateOpenWebUrl()
        {
            return IsMounted();
        }

        private bool View_ValidateEditWith()
        {
            if (IsMounted() && SelectedPaths.Count > 0)
            {
                foreach (Path selected in SelectedPaths)
                {
                    if (!IsEditable(selected))
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        /// <param name="selected"></param>
        /// <returns>True if the selected path is editable (not a directory)</returns>
        private bool IsEditable(Path selected)
        {
            return selected.attributes().isFile();
        }

        private bool View_ValidateDuplicateFile()
        {
            return IsMounted() && SelectedPaths.Count == 1;
        }

        private bool View_ValidateRenameFile()
        {
            if (IsMounted() && SelectedPaths.Count == 1)
            {
                if (null == SelectedPath)
                {
                    return false;
                }
                return getSession().isRenameSupported(SelectedPath);
            }
            return false;
        }

        private bool View_ValidateNewFile()
        {
            return IsMounted();
        }

        private void View_NewDownload()
        {
            //todo implement
            return;
            throw new NotImplementedException();
        }

        private void View_OpenConnection()
        {
            ConnectionController c = ConnectionController.Instance(this);
            DialogResult result = c.View.ShowDialog(View);
            if (result == DialogResult.OK)
            {
                Mount(c.ConfiguredHost);
            }
        }

        private bool View_ValidateNewFolder()
        {
            return IsMounted() && _session.isCreateFolderSupported(Workdir);
        }

        private void View_DuplicateFile()
        {
            DuplicateFileController dc =
                new DuplicateFileController(ObjectFactory.GetInstance<IDuplicateFilePromptView>(), this);
            dc.Show();
        }

        private void View_NewFile()
        {
            CreateFileController fc = new CreateFileController(ObjectFactory.GetInstance<ICreateFilePromptView>(), this);
            fc.Show();
        }

        private void View_Delete()
        {
            DeletePaths(SelectedPaths);
        }

        private void View_NewFolder()
        {
            FolderController fc = new FolderController(ObjectFactory.GetInstance<INewFolderPromptView>(), this);
            fc.Show();
        }

        private bool View_RenameFile(Path path, string newName)
        {
            if (!String.IsNullOrEmpty(newName) && !newName.Equals(path.getName()))
            {
                Path renamed = PathFactory.createPath(getSession(), path.getParent().getAbsolute(), newName,
                                                      path.attributes().getType());
                RenamePath(path, renamed);
            }
            return false;
        }

        private DataObject View_BrowserDrag(ObjectListView listView)
        {
            DataObject data = new DataObject(DataFormats.FileDrop, new[]
                                                                       {
                                                                           CreateAndWatchTemporaryFile(
                                                                               delegate(object sender,
                                                                                        FileSystemEventArgs args)
                                                                                   {
                                                                                       dropFolder =
                                                                                           System.IO.Path.
                                                                                               GetDirectoryName(
                                                                                                   args.FullPath);
                                                                                       Invoke(
                                                                                           () =>
                                                                                           Download(SelectedPaths,
                                                                                                    LocalFactory.
                                                                                                        createLocal(
                                                                                                            dropFolder)));
                                                                                   }
                                                                               )
                                                                       });
            return data;
        }

        private void RemoveTemporaryFiles(DataObject data)
        {
            if (data.ContainsFileDropList())
            {
                foreach (string tmpFile in data.GetFileDropList())
                {
                    if (File.Exists(tmpFile))
                    {
                        File.Delete(tmpFile);
                    }
                    if (null != dropFolder)
                    {
                        string tmpDestFile = System.IO.Path.Combine(dropFolder, System.IO.Path.GetFileName(tmpFile));
                        if (File.Exists(tmpDestFile))
                        {
                            File.Delete(tmpDestFile);
                        }
                    }
                }
            }
        }

        private void View_BrowserEndDrag(DataObject data)
        {
            RemoveTemporaryFiles(data);
        }

        private void View_BrowserDropped(OlvDropEventArgs e)
        {
            if (IsMounted() && e.DataObject is DataObject && ((DataObject)e.DataObject).ContainsFileDropList())
            {
                Path destination;
                switch (e.DropTargetLocation)
                {
                    case DropTargetLocation.Item:
                        destination = ((TreePathReference)e.DropTargetItem.RowObject).Unique;
                        break;
                    case DropTargetLocation.Background:
                        destination = Workdir;
                        break;
                    default:
                        destination = null;
                        break;
                }

                StringCollection dropList = (e.DataObject as DataObject).GetFileDropList();
                if (dropList.Count > 0)
                {
                    IList<Path> roots = new List<Path>();
                    Session session = getTransferSession();
                    foreach (string file in dropList)
                    {
                        Path p = PathFactory.createPath(session, destination.getAbsolute(),
                                                        LocalFactory.createLocal(file));
                        roots.Add(p);
                    }
                    UploadDroppedPath(roots, destination);
                }
            }
        }

        public void UploadDroppedPath(IList<Path> roots, Path destination)
        {
            if (IsMounted())
            {
                UploadTransfer q = new UploadTransfer(Utils.ConvertToJavaList(roots));
                if (q.numberOfRoots() > 0)
                {
                    transfer(q, destination);
                }
            }
        }

        /// <summary>
        /// Check if we accept drag operation from external program
        /// </summary>
        /// <param name="args"></param>
        private void View_BrowserCanDrop(OlvDropEventArgs args)
        {
            args.Effect = DragDropEffects.None;
            if (IsMounted() && !(args.DataObject is OLVDataObject))
            {
                if (args.DataObject is DataObject && ((DataObject)args.DataObject).ContainsFileDropList())
                {
                    args.Effect = DragDropEffects.Copy;

                    Path destination;
                    switch (args.DropTargetLocation)
                    {
                        case DropTargetLocation.Item:
                            destination = ((TreePathReference)args.DropTargetItem.RowObject).Unique;
                            if (!destination.attributes().isDirectory())
                            {
                                //dragging over file
                                destination = destination.getParent();
                            }
                            break;
                        case DropTargetLocation.Background:
                            destination = Workdir;
                            break;
                        default:
                            destination = null;
                            break;
                    }
                    if (Workdir == destination)
                    {
                        args.DropTargetLocation = DropTargetLocation.Background;
                        (args.DataObject as DataObject).SetDropDescription((DropImageType)args.Effect,
                                                                           "Copy to working directory", null);
                        //todo needs localization
                    }
                    else if (null != destination)
                    {
                        args.DropTargetItem = args.ListView.ModelToItem(new TreePathReference(destination));
                        (args.DataObject as DataObject).SetDropDescription((DropImageType)args.Effect, "Copy to %1",
                                                                           destination.getName());
                        //todo needs localization
                    }
                }
            }
        }

        private void View_ShowTransfers()
        {
            TransferController.Instance.View.Show();
        }

        private void View_ShowInspector()
        {
            List<Path> selected = SelectedPaths;
            if (selected.Count > 0)
            {
                if (Preferences.instance().getBoolean("browser.info.isInspector"))
                {
                    if (null == _inspector || _inspector.View.IsDisposed)
                    {
                        _inspector = InfoController.Factory.Create(this, selected);
                    }
                    else
                    {
                        _inspector.Files = selected;
                    }
                    _inspector.View.Show(View);
                }
                else
                {
                    InfoController c = InfoController.Factory.Create(this, selected);
                    c.View.Show(View);
                }
            }
        }

        private void View_EditEvent()
        {
            foreach (Path selected in SelectedPaths)
            {
                Editor editor = EditorFactory.createEditor(this, selected);
                editor.open();
            }
        }

        private void UpdateEditIcon()
        {
            Path selected = SelectedPath;
            if (null != selected)
            {
                if (IsEditable(selected))
                {
                    string command = selected.getLocal().getDefaultApplication();
                    if (null != command)
                    {
                        Bitmap res = IconCache.Instance.ExtractIconForFilename(command);
                        if (null != res)
                        {
                            View.EditIcon = res;
                            return;
                        }
                    }
                }
            }
            View.EditIcon = IconCache.Instance.IconForName("pencil", 32);
        }

        private void UpdateOpenIcon()
        {
            View.OpenIcon = IconCache.Instance.GetDefaultBrowserIcon();
        }

        private void View_BrowserSelectionChanged()
        {
            UpdateEditIcon();

            // update inspector content if available
            List<Path> selectedPaths = SelectedPaths;

            if (Preferences.instance().getBoolean("browser.info.isInspector"))
            {
                if (_inspector != null && _inspector.Visible)
                {
                    if (selectedPaths.Count > 0)
                    {
                        background(new UpdateInspectorAction(this, selectedPaths));
                    }
                }
            }
        }

        private void View_PathSelectionChanged()
        {
            string selected = View.SelectedComboboxPath;
            Path previous = Workdir;
            if (selected != null)
            {
                Path path = PathFactory.createPath(_session, selected, AbstractPath.DIRECTORY_TYPE);
                SetWorkdir(path);
                if (previous.getParent().equals(path))
                {
                    SetWorkdir(path, previous);
                }
                else
                {
                    SetWorkdir(path);
                }
            }
        }

        private void ViewViewShownEvent()
        {
            UpdateNavigationPaths();
        }

        private void View_FolderUp()
        {
            Path previous = Workdir;
            SetWorkdir(previous.getParent(), previous);
        }

        private void View_HistoryForward()
        {
            Path selected = GetForwardPath();
            if (selected != null)
            {
                SetWorkdir(selected);
            }
        }

        private void View_HistoryBack()
        {
            Path selected = GetPreviousPath();
            if (selected != null)
            {
                Path previous = Workdir;
                if (previous.getParent().equals(selected))
                {
                    SetWorkdir(selected, previous);
                }
                else
                {
                    SetWorkdir(selected);
                }
            }
        }

        private void View_BrowserDoubleClicked()
        {
            View_FolderInside();
        }

        private void View_QuickConnect()
        {
            if (string.IsNullOrEmpty(View.QuickConnectValue))
            {
                return;
            }
            string input = View.QuickConnectValue.Trim();

            // First look for equivalent bookmarks
            BookmarkCollection bookmarkCollection = BookmarkCollection.defaultCollection();
            foreach (Host host in bookmarkCollection)
            {
                if (host.getNickname().Equals(input))
                {
                    Mount(host);
                    return;
                }
            }
            Mount(Host.parse(input));
        }

        /// <summary>
        /// Open a new browser with the current selected folder as the working directory
        /// </summary>
        private void View_NewBrowser(object sender, NewBrowserEventArgs newBrowserEventArgs)
        {
            if (newBrowserEventArgs.SelectedAsWorkingDir)
            {
                Path selected = SelectedPath;
                if (null == selected || !selected.attributes().isDirectory())
                {
                    selected = Workdir;
                }
                BrowserController c = MainController.NewBrowser(true);

                Host host = new Host(getSession().getHost().getAsDictionary());
                host.setDefaultPath(selected.getAbsolute());
                c.Mount(host);
            }
            else
            {
                BrowserController c = MainController.NewBrowser(true);
                MainController.OpenDefaultBookmark(c);
            }
        }

        public Session getSession()
        {
            return _session;
        }

        /// <summary>
        /// Transfers the files either using the queue or using
        /// the browser session if #connection.pool.max is 1
        /// </summary>
        /// <param name="transfer"></param>
        public void transfer(Transfer transfer)
        {
            this.transfer(transfer, transfer.getSession().getMaxConnections() == 1);
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="transfer"></param>
        /// <param name="useBrowserConnection"></param>
        public void transfer(Transfer transfer, bool useBrowserConnection)
        {
            this.transfer(transfer, useBrowserConnection, TransferPromptController.Create(this, transfer));
        }

        /// <summary>
        /// Will reload the data for this directory in the browser after the transfer completes
        /// </summary>
        /// <param name="transfer"></param>
        /// <param name="destination"></param>
        public void transfer(Transfer transfer, Path destination)
        {
            ReloadTransferAdapter transferAdapter = new ReloadTransferAdapter(this, transfer, destination, true);
            transfer.addListener(transferAdapter);
            this.transfer(transfer);
        }

        protected void transfer(Transfer transfer, bool useBrowserConnection, TransferPrompt prompt)
        {
            if (useBrowserConnection)
            {
                TransferAdapter transferAdapter = new TransferAdapter(this, transfer);
                transfer.addListener(transferAdapter);
                //todo hmm, bleiben hier nicht alle Transfer aktiv (gc kann nicht abräumen wegen delegate)???
                //todo Memory Leak?
                //todo mit dko anschauen, auch Überladung transfer(Transfer transfer, Path workdir)
                //View.ViewClosedEvent += delegate { Transfer.removeListener(transferAdapter); };

                //Versuch 1 das Leak zu beseitigen
                CallbackDelegate callback = delegate
                {
                    Log.debug(
                        "Callback (TransferBrowserBackgrounAction) invoked, removing transferAdapter listener");
                    transfer.removeListener(transferAdapter);
                };
                Background(new TransferBrowserBackgrounAction(this, prompt, transfer, callback));
            }
            else
            {
                // in new browser
                TransferController.Instance.StartTransfer(transfer);
            }
        }

        /// <summary>
        ///
        /// </summary>
        /// <returns>The session to be used for file transfers. Null if not mounted</returns>
        protected Session getTransferSession()
        {
            if (!IsMounted())
            {
                return null;
            }
            if (_session.getMaxConnections() == 1)
            {
                return _session;
            }
            Host h = new Host(_session.getHost().getAsDictionary());
            // Copy credentials of the browser
            h.getCredentials().setPassword(_session.getHost().getCredentials().getPassword());
            Session session = SessionFactory.createSession(h);
            return session;
        }

        /// <summary>
        ///
        /// </summary>
        /// <returns>true if a connection is being opened or is already initialized</returns>
        public bool HasSession()
        {
            return _session != null;
        }

        public bool IsMounted()
        {
            return HasSession() && Workdir != null;
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="preserveSelection">All selected files should be reselected after reloading the view</param>
        public void ReloadData(bool preserveSelection)
        {
            if (preserveSelection)
            {
                //Remember the previously selected paths
                ReloadData(SelectedPaths);
            }
            else
            {
                ReloadData(new List<Path>());
            }
        }

        public void RefreshParentPaths(ICollection<Path> paths, List<TreePathReference> selected)
        {
            bool rootRefreshed = false; //prevent multiple root updates
            foreach (Path path in paths)
            {
                Path parent = path.getParent();
                if (Workdir.equals(parent))
                {
                    if (rootRefreshed)
                    {
                        continue;
                    }
                    View.SetBrowserModel(_browserModel.ChildrenGetter(new TreePathReference(parent)));
                    rootRefreshed = true;
                }
                else
                {
                    View.RefreshBrowserObject(new TreePathReference(parent));
                }
            }
            View.SelectedPaths = selected;
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="path"></param>
        /// <returns>null if not mounted or lookup fails</returns>
        public Path Lookup(TreePathReference path)
        {
            if (IsMounted())
            {
                return (Path)_session.cache().lookup(path);
            }
            return null;
        }

        protected void ReloadData(ICollection<Path> selected)
        {
            if (null != Workdir)
            {
                IEnumerable<TreePathReference> children = _browserModel.ChildrenGetter(new TreePathReference(Workdir));
                //clear selection before resetting model. Otherwise we have weird selection effects.
                View.SelectedPaths = new List<TreePathReference>();
                View.SetBrowserModel(children);
                List<TreePathReference> s = new List<TreePathReference>();
                foreach (Path p in selected)
                {
                    s.Add(new TreePathReference(p));
                }
                View.SelectedPaths = s; //restore selection
                List<TreePathReference> toUpdate = new List<TreePathReference>();
                foreach (TreePathReference reference in View.VisiblePaths)
                {
                    if (reference.Unique.attributes().isDirectory())
                    {
                        toUpdate.Add(reference);
                    }
                }
                View.RefreshBrowserObjects(toUpdate);
            }
            else
            {
                View.SetBrowserModel(null);
            }
            View.FilenameFilter = FilenameFilter;
            UpdateStatusLabel();
        }

        public void SetWorkdir(Path directory)
        {
            SetWorkdir(directory, new List<Path>());
        }

        public void SetWorkdir(Path directory, Path selected)
        {
            SetWorkdir(directory, new List<Path> { selected });
        }

        /// <summary>
        /// Sets the current working directory. This will udpate the path selection dropdown button
        /// and also add this path to the browsing history. If the path cannot be a working directory (e.g. permission
        /// issues trying to enter the directory), reloading the browser view is canceled and the working directory
        /// not changed.
        /// </summary>
        /// <param name="directory">The new working directory to display or null to detach any working directory from the browser</param>
        /// <param name="selected"></param>
        public void SetWorkdir(Path directory, List<Path> selected)
        {
            if (null == directory)
            {
                // Clear the browser view if no working directory is given
                Invoke(delegate
                {
                    Workdir = null;
                    UpdateNavigationPaths();
                    ReloadData(false);
                });
                return;
            }
            Background(new WorkdirAction(this, directory, selected));
        }

        private void UpdateNavigationPaths()
        {
            List<string> paths = new List<string>();
            if (!IsMounted())
            {
                View.PopulatePaths(new List<string>());
            }
            else
            {
                Path p = Workdir;
                while (!p.getParent().equals(p))
                {
                    paths.Add(p.getAbsolute());
                    p = p.getParent();
                }
                paths.Add(p.getAbsolute());
                View.PopulatePaths(paths);
            }
        }

        public void RefreshObject(Path path, bool preserveSelection)
        {
            if (preserveSelection)
            {
                RefreshObject(path, View.SelectedPaths);
            }
            else
            {
                RefreshObject(path, new List<TreePathReference>());
            }
        }

        public void RefreshObject(Path path, List<TreePathReference> selected)
        {
            if (Workdir.Equals(path))
            {
                View.SetBrowserModel(_browserModel.ChildrenGetter(new TreePathReference(path)));
            }
            else
            {
                if (!path.attributes().isDirectory())
                {
                    View.RefreshBrowserObject(new TreePathReference(path.getParent()));
                }
                else
                {
                    View.RefreshBrowserObject(new TreePathReference(path));
                }
            }
            View.SelectedPaths = selected;
            UpdateStatusLabel();
        }

        public void Mount(Host host)
        {
            CallbackDelegate callbackDelegate = delegate
            {
                // The browser has no session, we are allowed to proceed
                // Initialize the browser with the new session attaching all listeners
                Session session = Init(host);
                background(new MountAction(this, session, host));
            };
            Unmount(callbackDelegate);
        }

        /// <summary>
        /// Initializes a session for the passed host. Setting up the listeners and adding any callback
        /// controllers needed for login, trust management and hostkey verification.
        /// </summary>
        /// <param name="host"></param>
        /// <returns>A session object bound to this browser controller</returns>
        private Session Init(Host host)
        {
            if (HasSession())
            {
                _session.removeConnectionListener(_listener);
            }
            _session = SessionFactory.createSession(host);
            SetWorkdir(null);
            View.SelectedEncoding = _session.getEncoding();
            _session.addProgressListener(new ProgessListener(this));
            _session.addConnectionListener(_listener = new ConnectionAdapter(this, host));
            View.ClearTranscript();
            ClearBackHistory();
            ClearForwardHistory();
            _session.addTranscriptListener(this);
            return _session;
        }

        /// <summary>
        /// Remove all entries from the back path history
        /// </summary>
        public void ClearBackHistory()
        {
            _backHistory.Clear();
        }

        /// <summary>
        /// Remove all entries from the forward path history
        /// </summary>
        public void ClearForwardHistory()
        {
            _forwardHistory.Clear();
        }

        // some simple caching as _session.isConnected() throws a ConnectionCanceledException if not connected

        /// <summary>
        ///
        /// </summary>
        /// <returns>true if mounted and the connection to the server is alive</returns>
        public bool IsConnected()
        {
            if (IsMounted())
            {
                if (_sessionShouldBeConnected)
                {
                    return _session.isConnected();
                }
            }
            return false;
        }

        /// <summary>
        ///
        /// </summary>
        /// <returns>true if there is any network activity running in the background</returns>
        public bool IsActivityRunning()
        {
            BackgroundAction current = BackgroundActionRegistry.instance().getCurrent();
            if (null == current)
            {
                return false;
            }
            if (current is BrowserBackgroundAction)
            {
                return ((BrowserBackgroundAction)current).BrowserController == this;
            }
            return false;
        }

        public static bool ApplicationShouldTerminate()
        {
            // Determine if there are any open connections
            foreach (BrowserController controller in new List<BrowserController>(MainController.Browsers))
            {
                BrowserController c = controller;
                if (!controller.Unmount(delegate(DialogResult result)
                {
                    if (DialogResult.OK == result)
                    {
                        c.View.Dispose();
                        return true;
                    }
                    return false;
                }, delegate { }))
                {
                    return false; // Disconnect cancelled
                }
            }
            return true;
        }

        public bool Unmount()
        {
            return Unmount(() => { });
        }

        public bool Unmount(CallbackDelegate disconnected)
        {
            return Unmount(result =>
            {
                if (DialogResult.OK == result)
                {
                    UnmountImpl(disconnected);
                    return true;
                }
                // No unmount yet
                return false;
            }, disconnected);
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="unmountImpl"></param>
        /// <param name="disconnected"></param>
        /// <returns>True if the unmount process is in progress or has been finished, false if cancelled</returns>
        public bool Unmount(DialogCallbackDelegate unmountImpl, CallbackDelegate disconnected)
        {
            if (IsConnected() || IsActivityRunning())
            {
                if (Preferences.instance().getBoolean("browser.confirmDisconnect"))
                {
                    DialogResult r =
                        View.MessageBox(
                            Locale.localizedString("Disconnect from") + " " + _session.getHost().getHostname(),
                            Locale.localizedString("The connection will be closed."), null,
                            eTaskDialogButtons.OKCancel,
                            eSysIcons.Question);
                    return unmountImpl(r);
                }
                UnmountImpl(disconnected);
                // Unmount in progress
                return true;
            }
            disconnected();
            // Unmount succeeded
            return true;
        }

        private void UnmountImpl(CallbackDelegate disconnected)
        {
            if (IsActivityRunning())
            {
                Interrupt();
            }
            background(new UnmountAction(this, disconnected));
        }

        private void Interrupt()
        {
            if (HasSession())
            {
                if (IsActivityRunning())
                {
                    BackgroundAction current = BackgroundActionRegistry.instance().getCurrent();
                    if (null != current)
                    {
                        current.cancel();
                    }
                }
                background(new InterruptAction(this, _session));
            }
        }


        /// <summary>
        /// Will close the session but still display the current working directory without any confirmation
        /// from the user
        /// </summary>
        private void UnmountImpl()
        {
            // This is not synchronized to the <code>mountingLock</code> intentionally; this allows to unmount
            // sessions not yet connected
            if (HasSession())
            {
                //Close the connection gracefully
                _session.close();
            }
        }

        public void UpdateStatusLabel()
        {
            string label = Locale.localizedString("Disconnected", "Status");

            switch (View.CurrentView)
            {
                case BrowserView.File:
                    if (IsMounted())
                    {
                        if (IsConnected())
                        {
                            label = View.NumberOfFiles + " " + Locale.localizedString("Files");
                        }
                    }
                    break;
                case BrowserView.Bookmark:
                case BrowserView.History:
                case BrowserView.Bonjour:
                    label = View.NumberOfBookmarks + " " + Locale.localizedString("Bookmarks");
                    break;
            }
            UpdateStatusLabel(label);
        }

        public void UpdateStatusLabel(string label)
        {
            View.StatusLabel = label;
        }

        public void AddPathToHistory(Path path)
        {
            if (_backHistory.Count > 0)
            {
                // Do not add if this was a reload
                if (path.equals(_backHistory.Contains(_backHistory[_backHistory.Count - 1])))
                {
                    return;
                }
            }
            _backHistory.Add(path);
        }

        /// <summary>
        /// Returns the prevously browsed path and moves it to the forward history
        /// </summary>
        /// <returns>The previously browsed path or null if there is none</returns>
        public Path GetPreviousPath()
        {
            int size = _backHistory.Count;
            if (size > 1)
            {
                _forwardHistory.Add(_backHistory[size - 1]);
                Path p = _backHistory[size - 2];
                //delete the fetched path - otherwise we produce a loop
                _backHistory.RemoveAt(size - 1);
                _backHistory.RemoveAt(size - 2);
                return p;
            }
            if (1 == size)
            {
                _forwardHistory.Add(_backHistory[size - 1]);
                return _backHistory[size - 1];
            }
            return null;
        }

        /// <summary>
        ///
        /// </summary>
        /// <returns>The last path browsed before #getPrevoiusPath was called</returns>
        public Path GetForwardPath()
        {
            int size = _forwardHistory.Count;
            if (size > 0)
            {
                Path path = _forwardHistory[size - 1];
                _forwardHistory.RemoveAt(size - 1);
                return path;
            }
            return null;
        }

        private void PopulateQuickConnect()
        {
            List<string> nicknames = new List<string>();
            foreach (Host host in _bookmarkCollection)
            {
                nicknames.Add(host.getNickname());
            }
            View.PopulateQuickConnect(nicknames);
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="path">The existing file</param>
        /// <param name="renamed">The renamed file</param>
        protected internal void RenamePath(Path path, Path renamed)
        {
            RenamePaths(new Dictionary<Path, Path> { { path, renamed } });
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="selected">
        /// A dictionary with the original files as the key and
        /// the destination files as the value
        /// </param>
        protected internal void RenamePaths(IDictionary<Path, Path> selected)
        {
            IDictionary<Path, Path> normalized = CheckHierarchy(selected);
            CheckMove(normalized.Values, new RenameAction(this, normalized));
        }

        /// <summary>
        /// Displays a warning dialog about files to be moved
        /// </summary>
        /// <param name="selected">The files to check for existance</param>
        /// <param name="action"></param>
        private void CheckMove(ICollection<Path> selected, BackgroundAction action)
        {
            if (selected.Count > 0)
            {
                if (Preferences.instance().getBoolean("browser.confirmMove"))
                {
                    StringBuilder alertText = new StringBuilder(
                        Locale.localizedString("Do you want to move the selected files?"));

                    StringBuilder content = new StringBuilder();
                    int i = 0;
                    IEnumerator<Path> enumerator = null;
                    for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext(); )
                    {
                        Path item = enumerator.Current;
                        // u2022 = Bullet
                        content.Append("\n" + Character.toString('\u2022') + " " + item.getName());
                        i++;
                    }
                    if (enumerator.MoveNext())
                    {
                        content.Append("\n" + Character.toString('\u2022') + " ...)");
                    }
                    DialogResult r = MessageBox(Locale.localizedString("Move"),
                                                alertText.ToString(),
                                                content.ToString(),
                                                eTaskDialogButtons.OKCancel,
                                                eSysIcons.Question);
                    if (r == DialogResult.OK)
                    {
                        CheckOverwrite(selected, action);
                    }
                }
                else
                {
                    CheckOverwrite(selected, action);
                }
            }
        }

        /// <summary>
        /// Prunes the list of selected files. Files which are a child of an already included directory
        /// are removed from the returned list.
        /// </summary>
        /// <param name="selected"></param>
        /// <returns></returns>
        protected List<Path> CheckHierarchy(ICollection<Path> selected)
        {
            List<Path> normalized = new List<Path>();
            foreach (Path f in selected)
            {
                bool duplicate = false;
                foreach (Path n in normalized)
                {
                    if (f.isChild(n))
                    {
                        // The selected file is a child of a directory
                        // already included for deletion
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate)
                {
                    normalized.Add(f);
                }
            }
            return normalized;
        }

        /// <summary>
        /// Recursively deletes the files
        /// </summary>
        /// <param name="selected">The files selected in the browser to delete</param>
        public void DeletePaths(ICollection<Path> selected)
        {
            List<Path> normalized = CheckHierarchy(selected);
            if (normalized.Count == 0)
            {
                return;
            }

            StringBuilder alertText = new StringBuilder(
                Locale.localizedString(
                    "Really delete the following files? This cannot be undone."));

            StringBuilder content = new StringBuilder();
            int i = 0;
            IEnumerator<Path> enumerator;
            for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext(); )
            {
                Path item = enumerator.Current;
                if (item.exists())
                {
                    if (i > 0) content.AppendLine();
                    // u2022 = Bullet
                    content.Append(Character.toString('\u2022') + " " + item.getName());
                }
                i++;
            }
            if (enumerator.MoveNext())
            {
                content.Append("\n" + Character.toString('\u2022') + " ...)");
            }
            DialogResult r = MessageBox(Locale.localizedString("Delete"),
                                        alertText.ToString(),
                                        content.ToString(),
                                        eTaskDialogButtons.OKCancel,
                                        eSysIcons.Question);
            if (r == DialogResult.OK)
            {
                DeletePathsImpl(normalized);
            }
        }

        private void DeletePathsImpl(List<Path> files)
        {
            background(new DeleteAction(this, files));
        }

        public void SetPathFilter(string searchString)
        {
            Log.debug("setPathFilter:" + searchString);
            if (Utils.IsBlank(searchString))
            {
                View.SearchString = String.Empty;
                // Revert to the last used default filter
                if (ShowHiddenFiles)
                {
                    FilenameFilter = new NullPathFilter();
                }
                else
                {
                    FilenameFilter = new HiddenFilesPathFilter();
                }
            }
            else
            {
                // Setting up a custom filter for the directory listing
                FilenameFilter = new CustomPathFilter(searchString);
            }
            ReloadData(true);
        }

        /// <summary>
        /// Displays a warning dialog about already existing files
        /// </summary>
        /// <param name="selected">The files to check for existance</param>
        /// <param name="action"></param>
        private void CheckOverwrite(ICollection<Path> selected, BackgroundAction action)
        {
            if (selected.Count > 0)
            {
                StringBuilder alertText = new StringBuilder(
                    Locale.localizedString(
                        "A file with the same name already exists. Do you want to replace the existing file?"));

                StringBuilder content = new StringBuilder();
                int i = 0;
                IEnumerator<Path> enumerator = null;
                bool shouldWarn = false;

                for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext(); )
                {
                    Path item = enumerator.Current;
                    if (item.exists())
                    {
                        // u2022 = Bullet
                        content.Append("\n" + Character.toString('\u2022') + " " + item.getName());
                        shouldWarn = true;
                    }
                    i++;
                }
                if (enumerator.MoveNext())
                {
                    content.Append("\n" + Character.toString('\u2022') + " ...)");
                }
                if (shouldWarn)
                {
                    DialogResult r = MessageBox(Locale.localizedString("Overwrite"),
                                                alertText.ToString(),
                                                content.ToString(),
                                                eTaskDialogButtons.OKCancel,
                                                eSysIcons.Question);
                    if (r == DialogResult.OK)
                    {
                        background(action);
                    }
                }
                else
                {
                    background(action);
                }
            }
        }

        /// <summary>
        /// Prunes the map of selected files. Files which are a child of
        /// an already included directory are removed from the returned map.
        /// </summary>
        /// <param name="selected"></param>
        /// <returns></returns>
        protected IDictionary<Path, Path> CheckHierarchy(IDictionary<Path, Path> selected)
        {
            IDictionary<Path, Path> normalized = new Dictionary<Path, Path>();
            foreach (KeyValuePair<Path, Path> keyValuePair in selected)
            {
                Path f = keyValuePair.Key;
                Path r = keyValuePair.Value;
                bool duplicate = false;

                // Temporary list that holds the keys which have to be removed after looping.
                // There is no direct way in C# to remove an item from a dictionary while looping.
                IList<Path> removals = new List<Path>();

                ICollection<Path> keys = normalized.Keys;
                foreach (Path n in keys)
                {
                    if (f.isChild(n))
                    {
                        // The selected file is a child of a directory
                        // already included for deletion
                        duplicate = true;
                        break;
                    }
                    if (n.isChild(f))
                    {
                        // Remove the previously added file as it is a child
                        // of the currently evaluated file
                        removals.Add(n);
                    }
                }
                foreach (Path remove in removals)
                {
                    normalized.Remove(remove);
                }
                if (!duplicate)
                {
                    normalized.Add(f, r);
                }
            }
            return normalized;
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="source">The original file to duplicate</param>
        /// <param name="destination">The destination of the duplicated file</param>
        /// <param name="edit">Open the duplicated file in the external editor</param>
        protected internal void DuplicatePath(Path source, Path destination, bool edit)
        {
            DuplicatePaths(new Dictionary<Path, Path> { { source, destination } }, edit);
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="selected">A dictionary with the original files as the key and the destination files as the value</param>
        /// <param name="edit">Open the duplicated files in the external editor</param>
        protected internal void DuplicatePaths(IDictionary<Path, Path> selected, bool edit)
        {
            IDictionary<Path, Path> normalized = CheckHierarchy(selected);
            CheckMove(normalized.Values, new DuplicateFileAction(this, normalized, edit));
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="view">The view to show</param>
        public void ToggleView(BrowserView view)
        {
            Log.debug("ToggleView:" + view);
            if (View.CurrentView == view) return;

            SetBookmarkFilter(null);
            switch (view)
            {
                case BrowserView.File:
                    View.LogDrawerVisible = Preferences.instance().getBoolean("browser.logDrawer.isOpen");
                    View.CurrentView = BrowserView.File;
                    UpdateStatusLabel();
                    //ReloadData(false); //not necessary?
                    break;
                case BrowserView.Bookmark:
                    View.LogDrawerVisible = false;
                    View.CurrentView = BrowserView.Bookmark;
                    _bookmarkModel.Source = BookmarkCollection.defaultCollection();
                    ReloadBookmarks();
                    break;
                case BrowserView.History:
                    View.LogDrawerVisible = false;
                    View.CurrentView = BrowserView.History;
                    _bookmarkModel.Source = HistoryCollection.defaultCollection();
                    ReloadBookmarks();
                    break;
                case BrowserView.Bonjour:
                    View.LogDrawerVisible = false;
                    View.CurrentView = BrowserView.Bonjour;
                    _bookmarkModel.Source = RendezvousCollection.defaultCollection();
                    ReloadBookmarks();
                    break;
            }
        }

        /// <summary>
        /// Reload bookmarks table from the currently selected model
        /// </summary>
        public void ReloadBookmarks()
        {
            //Note: expensive for a big bookmark list (might need a refactoring)
            View.SetBookmarkModel(_bookmarkModel.Source);
            UpdateStatusLabel();
        }

        private class BookmarkFilter : HostFilter
        {
            private readonly string _searchString;

            public BookmarkFilter(String searchString)
            {
                _searchString = searchString;
            }

            public bool accept(Host host)
            {
                return host.getNickname().ToLower().Contains(_searchString.ToLower())
                       || host.getHostname().ToLower().Contains(_searchString.ToLower());
            }
        }

        internal class ConnectionAdapter : ConnectionListener
        {
            private readonly BrowserController _controller;
            private readonly Host _host;

            public ConnectionAdapter(BrowserController controller, Host host)
            {
                _controller = controller;
                _host = host;
            }

            public void connectionWillOpen()
            {
                _controller._sessionShouldBeConnected = true;
                AsyncDelegate mainAction = delegate
                {
                    _controller.View.RefreshBookmark(_controller.getSession().getHost());
                    _controller.View.WindowTitle = _host.getNickname();
                };
                _controller.Invoke(new SimpleWindowMainAction(mainAction, _controller));
            }

            public void connectionDidOpen()
            {
                AsyncDelegate mainAction = delegate
                {
                    _controller.View.RefreshBookmark(_controller.getSession().getHost());
                    ch.cyberduck.ui.growl.Growl.instance().notify("Connection opened",
                                                                  _host.getHostname());

                    _controller.View.SecureConnection = _controller._session.isSecure();
                    _controller.View.CertBasedConnection =
                        _controller._session is SSLSession;
                    _controller.View.SecureConnectionVisible = true;
                };
                _controller.Invoke(new SimpleWindowMainAction(mainAction, _controller));
            }

            public void connectionWillClose()
            {
            }

            public void connectionDidClose()
            {
                _controller._sessionShouldBeConnected = false;
                AsyncDelegate mainAction = delegate
                {
                    _controller.View.RefreshBookmark(_controller.getSession().getHost());
                    if (!_controller.IsMounted())
                    {
                        _controller.View.WindowTitle =
                            Preferences.instance().getProperty(
                                "application.name");
                    }
                    _controller.View.SecureConnectionVisible = false;
                    _controller.UpdateStatusLabel();
                };
                _controller.Invoke(new SimpleWindowMainAction(mainAction, _controller));
            }
        }

        private class CreateArchiveAction : BrowserBackgroundAction
        {
            private readonly Archive _archive;
            private readonly List _selected;

            public CreateArchiveAction(BrowserController controller, Archive archive, IEnumerable<Path> selected)
                : base(controller)
            {
                _archive = archive;
                _selected = Utils.ConvertToJavaList(selected);
            }

            public override void run()
            {
                BrowserController._session.archive(_archive, _selected);
            }

            public override string getActivity()
            {
                return _archive.getCompressCommand(_selected);
            }

            public override void cleanup()
            {
                BrowserController.RefreshParentPaths(new List<Path> { _archive.getArchive(_selected) },
                                                     new List<TreePathReference> { new TreePathReference(_archive.getArchive(_selected)) });
            }
        }

        private class CustomPathFilter : PathFilter, IModelFilter
        {
            private readonly String _searchString;

            public CustomPathFilter(String searchString)
            {
                _searchString = searchString;
            }

            public bool Filter(object modelObject)
            {
                return accept(((TreePathReference)modelObject).Unique);
            }

            public bool accept(AbstractPath file)
            {
                if (file.getName().ToLower().IndexOf(_searchString.ToLower()) != -1)
                {
                    // Matching filename
                    return true;
                }
                if (file.attributes().isDirectory())
                {
                    // #471. Expanded item childs may match search string
                    return file.isCached();
                }
                return false;
            }
        }

        private class DeleteAction : BrowserBackgroundAction
        {
            private readonly List<Path> _normalized;

            public DeleteAction(BrowserController controller, List<Path> normalized)
                : base(controller)
            {
                _normalized = normalized;
            }

            public override void run()
            {
                foreach (Path p in _normalized)
                {
                    if (isCanceled())
                    {
                        break;
                    }
                    p.delete();
                    p.getParent().invalidate();
                    if (!BrowserController.IsConnected())
                    {
                        break;
                    }
                }
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Deleting {0}", "Status"), "");
            }

            public override void cleanup()
            {
                BrowserController.RefreshParentPaths(_normalized, new List<TreePathReference>());
            }
        }

        private class DisconnectAction : BrowserBackgroundAction
        {
            public DisconnectAction(BrowserController controller)
                : base(controller)
            {
            }

            public override void run()
            {
                BrowserController.UnmountImpl();
            }

            public override void cleanup()
            {
                if (Preferences.instance().getBoolean("browser.disconnect.showBookmarks"))
                {
                    BrowserController.ToggleView(BrowserView.Bookmark);
                }
                else
                {
                    BrowserController.View.BrowserActiveStateChanged();
                }
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Disconnecting {0}", "Status"),
                                     BrowserController.getSession().getHost().getHostname());
            }
        }

        private class DuplicateFileAction : BrowserBackgroundAction
        {
            private readonly bool _edit;
            private readonly IDictionary<Path, Path> _normalized;

            public DuplicateFileAction(BrowserController controller, IDictionary<Path, Path> normalized, bool edit)
                : base(controller)
            {
                _normalized = normalized;
                _edit = edit;
            }

            public override void run()
            {
                foreach (KeyValuePair<Path, Path> pair in _normalized)
                {
                    if (isCanceled())
                    {
                        break;
                    }
                    Path source = pair.Key;
                    Path destination = pair.Value;

                    source.copy(destination);
                    source.getParent().invalidate();
                    destination.getParent().invalidate();
                    if (!BrowserController.IsConnected())
                    {
                        break;
                    }
                }
            }

            public override void cleanup()
            {
                List<TreePathReference> selected = new List<TreePathReference>();
                foreach (Path duplicate in _normalized.Values)
                {
                    if (_edit)
                    {
                        Editor editor = EditorFactory.createEditor(BrowserController, duplicate);
                        editor.open();
                    }
                    if (duplicate.getName()[0] == '.')
                    {
                        BrowserController.ShowHiddenFiles = true;
                    }
                    selected.Add(new TreePathReference(duplicate));
                }
                BrowserController.RefreshParentPaths(_normalized.Values, selected);
            }

            public override string getActivity()
            {
                string sourceName = null;
                string destName = null;
                foreach (KeyValuePair<Path, Path> pair in _normalized)
                {
                    sourceName = pair.Key.getName();
                    destName = pair.Value.getName();
                }
                return string.Format(Locale.localizedString("Copying {0} to {1}", "Status"), sourceName, destName);
            }
        }

        private class EncodingBrowserBackgroundAction : BrowserBackgroundAction
        {
            private readonly string _encoding;

            public EncodingBrowserBackgroundAction(BrowserController controller, string encoding)
                : base(controller)
            {
                _encoding = encoding;
            }

            public override void run()
            {
                BrowserController.UnmountImpl();
            }

            public override void cleanup()
            {
                BrowserController._session.getHost().setEncoding(_encoding);
                BrowserController.View_RefreshBrowser();
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Disconnecting {0}", "Status"),
                                     BrowserController._session.getHost().getHostname());
            }
        }

        internal class InterruptAction : BrowserBackgroundAction
        {
            private readonly Object _lock = new Object();
            private readonly Session _session;

            public InterruptAction(BrowserController controller, Session session)
                : base(controller)
            {
                _session = session;
            }

            public override void run()
            {
                if (BrowserController.HasSession())
                {
                    // Aggressively close the connection to interrupt the current task
                    _session.interrupt();
                }
            }

            public override void cleanup()
            {
                ;
            }

            public override int retry()
            {
                return 0;
            }

            public override object @lock()
            {
                return _lock;
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Disconnecting {0}", "Status"),
                                     _session.getHost().getHostname());
            }
        }

        private class MountAction : BrowserBackgroundAction
        {
            private readonly Host _host;
            private readonly Session _session;
            private Path _mount;

            public MountAction(BrowserController controller,
                               Session session,
                               Host host)
                : base(controller)
            {
                _host = host;
                _session = session;
            }

            public override void run()
            {
                // Mount this session
                _mount = _session.mount();
            }

            public override void cleanup()
            {
                // Set the working directory
                BrowserController.SetWorkdir(_mount);
                if (!_session.isConnected())
                {
                    // Connection attempt failed
                    BrowserController.UnmountImpl();
                }
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Mounting {0}", "Status"),
                                     _host.getHostname());
            }
        }

        /// <summary>
        /// Simple TransferListener implementation that can be queried if the transfer has been completed
        /// </summary>
        private class PollableTransferListener : ch.cyberduck.core.TransferAdapter
        {
            private bool _ended;

            public bool TransferDidEnd
            {
                get { return _ended; }
            }

            public override void transferDidEnd()
            {
                _ended = true;
            }
        }

        internal class ProgessListener : ProgressListener
        {
            private readonly BrowserController _controller;

            public ProgessListener(BrowserController controller)
            {
                _controller = controller;
            }

            public void message(string msg)
            {
                AsyncDelegate updateLabel = delegate { _controller.View.StatusLabel = msg; };
                _controller.Invoke(updateLabel);
            }
        }

        internal class ReloadTransferAdapter : ch.cyberduck.core.TransferAdapter
        {
            private readonly BrowserController _controller;
            private readonly Path _destination;
            private readonly bool _removeListener;
            private readonly Transfer _transfer;

            public ReloadTransferAdapter(BrowserController controller, Transfer transfer, Path destination,
                                         bool removeListener)
            {
                _controller = controller;
                _transfer = transfer;
                _destination = destination;
                _removeListener = removeListener;
            }

            public override void transferDidEnd()
            {
                if (_controller.IsMounted())
                {
                    _controller.Workdir.invalidate();
                    if (!_transfer.isCanceled())
                    {
                        _controller.invoke(new ReloadAction(_controller, _destination));
                    }
                }
                if (_removeListener) _transfer.removeListener(this);
            }

            private class ReloadAction : WindowMainAction
            {
                private readonly Path _p;

                public ReloadAction(BrowserController c, Path p)
                    : base(c)
                {
                    _p = p;
                }

                public override bool isValid()
                {
                    return base.isValid() && ((BrowserController)Controller).IsConnected();
                }

                public override void run()
                {
                    ((BrowserController)Controller).RefreshObject(_p, true);
                }
            }
        }

        private class RenameAction : BrowserBackgroundAction
        {
            private readonly IDictionary<Path, Path> _normalized;

            public RenameAction(BrowserController controller, IDictionary<Path, Path> normalized)
                : base(controller)
            {
                _normalized = normalized;
            }

            public override void run()
            {
                foreach (KeyValuePair<Path, Path> pair in _normalized)
                {
                    if (isCanceled())
                    {
                        break;
                    }
                    Path original = pair.Key;
                    Path renamed = pair.Value;

                    original.getParent().invalidate();
                    original.rename(renamed);
                    renamed.invalidate();
                    renamed.getParent().invalidate();
                    if (!BrowserController.IsConnected())
                    {
                        break;
                    }
                }
            }

            public override void cleanup()
            {
                List<TreePathReference> selected = new List<TreePathReference>();
                foreach (Path p in _normalized.Values)
                {
                    selected.Add(new TreePathReference(p));
                }
                BrowserController.RefreshParentPaths(_normalized.Values, selected);
            }

            public override string getActivity()
            {
                string sourceName = null;
                string destName = null;
                foreach (KeyValuePair<Path, Path> pair in _normalized)
                {
                    sourceName = pair.Key.getName();
                    destName = pair.Value.getName();
                }
                return string.Format(Locale.localizedString("Renaming {0} to {1}", "Status"), sourceName, destName);
            }
        }

        private class RevertPathAction : BrowserBackgroundAction
        {
            private readonly Path _selected;

            public RevertPathAction(BrowserController controller, Path selected)
                : base(controller)
            {
                _selected = selected;
            }

            public override void run()
            {
                if (isCanceled())
                {
                    return;
                }
                _selected.revert();
            }

            public override void cleanup()
            {
                BrowserController.RefreshObject(_selected, false);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Reverting {0}", "Status"), _selected.getName());
            }
        }

        internal class TransferAdapter : ch.cyberduck.core.TransferAdapter
        {
            private const long Delay = 0;
            private const long Period = 500; //in milliseconds
            private readonly BrowserController _controller;
            private readonly Speedometer _meter;
            private readonly Timer _timer;

            public TransferAdapter(BrowserController controller, Transfer transfer)
            {
                _meter = new Speedometer(transfer);
                _controller = controller;
                _timer = new Timer(timerCallback, null, Timeout.Infinite, Period);
            }

            private void timerCallback(object state)
            {
                _controller.Invoke(delegate { _controller.View.StatusLabel = _meter.getProgress(); });
            }

            public override void willTransferPath(Path path)
            {
                _meter.reset();
                _timer.Change(Delay, Period);
            }

            public override void didTransferPath(Path path)
            {
                _timer.Change(Timeout.Infinite, Period);
                _meter.reset();
            }

            public override void bandwidthChanged(BandwidthThrottle bandwidth)
            {
                _meter.reset();
            }

            internal class ProgressTimerRunnable : Runnable
            {
                private readonly BrowserController _controller;
                private readonly Speedometer _meter;

                public ProgressTimerRunnable(BrowserController controller, Speedometer meter)
                {
                    _controller = controller;
                    _meter = meter;
                }

                public void run()
                {
                    AsyncDelegate mainAction = delegate { _controller.View.StatusLabel = _meter.getProgress(); };
                    _controller.Invoke(mainAction);
                }
            }
        }

        private class TransferBrowserBackgrounAction : BrowserBackgroundAction
        {
            private readonly CallbackDelegate _callback;
            private readonly TransferPrompt _prompt;
            private readonly Transfer _transfer;

            public TransferBrowserBackgrounAction(BrowserController controller,
                                                  TransferPrompt prompt,
                                                  Transfer transfer,
                                                  CallbackDelegate callback)
                : base(controller)
            {
                _prompt = prompt;
                _transfer = transfer;
                _callback = callback;
            }

            public override void run()
            {
                Log.debug("run: " + getActivity());
                TransferOptions options = new TransferOptions { closeSession = false };
                _transfer.start(_prompt, options);
            }

            public override void cleanup()
            {
                Log.debug("cleanup: " + getActivity());
                BrowserController.UpdateStatusLabel();
                _callback();
            }

            public override void cancel()
            {
                Log.debug("cancel: " + getActivity());
                _transfer.cancel();
                base.cancel();
            }

            public override string getActivity()
            {
                return _transfer.getName();
            }
        }

        private class UnarchiveAction : BrowserBackgroundAction
        {
            private readonly Archive _archive;
            private readonly List<Path> _expanded;
            private readonly Path _selected;

            public UnarchiveAction(BrowserController controller, Archive archive, Path selected, List<Path> expanded)
                : base(controller)
            {
                _archive = archive;
                _expanded = expanded;
                _selected = selected;
            }

            public override void run()
            {
                BrowserController._session.unarchive(_archive, _selected);
            }

            public override string getActivity()
            {
                return _archive.getDecompressCommand(_selected);
            }

            public override void cleanup()
            {
                _expanded.AddRange(Utils.ConvertFromJavaList<Path>(_archive.getExpanded(new ArrayList { _selected })));
                List<TreePathReference> selected = new List<TreePathReference>();
                foreach (Path p in _expanded)
                {
                    selected.Add(new TreePathReference(p));
                }
                BrowserController.RefreshParentPaths(_expanded, selected);
            }
        }

        private class UnmountAction : BrowserBackgroundAction
        {
            private readonly CallbackDelegate _callback;
            private readonly BrowserController _controller;

            public UnmountAction(BrowserController controller, CallbackDelegate callback)
                : base(controller)
            {
                _controller = controller;
                _callback = callback;
            }

            public override void run()
            {
                _controller.UnmountImpl();
            }

            public override void cleanup()
            {
                // Clear the cache on the main thread to make sure the browser model is not in an invalid state
                _controller._session.cache().clear();
                _controller._session.getHost().getCredentials().setPassword(null);

                _callback();
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Disconnecting {0}", "Status"),
                                     _controller._session.getHost().getHostname());
            }
        }

        private class UpdateInspectorAction : BrowserBackgroundAction
        {
            private readonly List<Path> _selected;

            public UpdateInspectorAction(BrowserController controller, List<Path> selected)
                : base(controller)
            {
                _selected = selected;
            }


            public override void run()
            {
                foreach (Path path in _selected)
                {
                    if (isCanceled())
                    {
                        break;
                    }
                    if (path.attributes().getPermission() == null)
                    {
                        path.readUnixPermission();
                    }
                }
            }

            public override void cleanup()
            {
                if (BrowserController._inspector != null)
                {
                    BrowserController._inspector.Files = _selected;
                }
            }
        }

        private class WorkdirAction : BrowserBackgroundAction
        {
            private readonly Path _directory;
            private readonly List<Path> _selected;

            public WorkdirAction(BrowserController controller, Path directory, List<Path> selected)
                : base(controller)
            {
                _directory = directory;
                _selected = selected;
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Listing directory {0}", "Status"),
                                     _directory.getName());
            }

            public override void cleanup()
            {
                // Remove any custom file filter
                BrowserController.SetPathFilter(null);

                BrowserController.UpdateNavigationPaths();

                // Mark the browser data source as dirty
                BrowserController.ReloadData(_selected);

                // Change to the browser view
                BrowserController.ToggleView(BrowserView.File);

                BrowserController.View.FocusBrowser();
            }

            public override void run()
            {
                if (_directory.isCached())
                {
                    //Reset the readable attribute
                    _directory.children().attributes().setReadable(true);
                }
                // Get the directory listing in the background
                _directory.children();
                if (_directory.children().attributes().isReadable())
                {
                    // Update the working directory if listing is successful
                    BrowserController._workdir = _directory;
                    // Update the current working directory
                    BrowserController.AddPathToHistory(BrowserController.Workdir);
                }
            }
        }
    }
}