// 
// Copyright (c) 2010-2018 Yves Langisch. All rights reserved.
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

using BrightIdeasSoftware;
using ch.cyberduck.core;
using ch.cyberduck.core.bonjour;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.features;
using ch.cyberduck.core.local;
using ch.cyberduck.core.pasteboard;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.serializer;
using ch.cyberduck.core.ssl;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.vault;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.browser;
using ch.cyberduck.ui.comparator;
using ch.cyberduck.ui.Views;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Local;
using Ch.Cyberduck.Core.Refresh.Interactivity;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms;
using DynamicData;
using java.lang;
using java.text;
using java.util;
using org.apache.logging.log4j;
using StructureMap;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.IO;
using System.Windows.Forms;
using Windows.Win32;
using static Ch.Cyberduck.ImageHelper;
using static Windows.Win32.UI.WindowsAndMessaging.MESSAGEBOX_RESULT;
using Application = ch.cyberduck.core.local.Application;
using Directory = ch.cyberduck.core.features.Directory;
using Exception = System.Exception;
using Path = ch.cyberduck.core.Path;
using String = System.String;
using StringBuilder = System.Text.StringBuilder;
using X509Certificate = java.security.cert.X509Certificate;
using X509Certificate2 = System.Security.Cryptography.X509Certificates.X509Certificate2;
using X509Certificate2UI = System.Security.Cryptography.X509Certificates.X509Certificate2UI;
using TransferItem = ch.cyberduck.core.transfer.TransferItem;

namespace Ch.Cyberduck.Ui.Controller
{
    public class BrowserController : WindowController<IBrowserView>, TranscriptListener, CollectionListener,
        ProgressListener
    {
        public delegate void CallbackDelegate();

        public delegate bool DialogCallbackDelegate(DialogResult result);

        private static readonly Logger Log = LogManager.getLogger(typeof(BrowserController).FullName);
        protected static string DEFAULT = LocaleFactory.localizedString("Default");
        private readonly AbstractHostCollection _bookmarkCollection = BookmarkCollection.defaultCollection();
        private readonly BookmarkModel _bookmarkModel;
        private readonly TreeBrowserModel _browserModel;
        private readonly PathCache _cache = new PathCache(PreferencesFactory.get().getInteger("browser.cache.size"));
        private readonly ListProgressListener _limitListener;
        private readonly Navigation _navigation = new Navigation();
        private readonly IList<FileSystemWatcher> _temporaryWatcher = new List<FileSystemWatcher>();
        private readonly IDictionary<Path, Editor> _editors = new Dictionary<Path, Editor>();
        private Comparator _comparator = new NullComparator();
        private String _dropFolder; // holds the drop folder of the current drag operation
        private InfoController _inspector;
        private BrowserView _lastBookmarkView = BrowserView.Bookmark;
        private PathPasteboard _pasteboard = PathPasteboard.EMPTY;
        private Scheduler _scheduler;
        private bool _showHiddenFiles;

        public BrowserController(IBrowserView view)
        {
            View = view;

            Session = SessionPool.DISCONNECTED;
            ShowHiddenFiles = PreferencesFactory.get().getBoolean("browser.showHidden");

            _limitListener = new DialogLimitedListProgressListener(this);
            _browserModel = new TreeBrowserModel(this, _cache, _limitListener);
            _bookmarkModel = new BookmarkModel(this, _bookmarkCollection);
            View.ViewClosedEvent += delegate { _bookmarkModel.Source = null; };

            //default view is the bookmark view
            ToggleView(BrowserView.Bookmark);
            View.StopActivityAnimation();

            View.SetComparator += View_SetComparator;
            View.ChangeBrowserView += View_ChangeBrowserView;

            View.QuickConnect += View_QuickConnect;
            View.BrowserDoubleClicked += View_BrowserDoubleClicked;
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
            View.SearchFieldEnter += View_SearchFieldEnter;


            View.ContextMenuEnabled += View_ContextMenuEnabled;

            #region Commands - File

            View.NewBrowser += View_NewBrowser;
            View.ValidateNewBrowser += View_ValidateNewBrowser;
            View.OpenConnection += View_OpenConnection;
            View.ValidateOpenConnection += () => true;
            View.NewDownload += View_NewDownload;
            View.ValidateNewDownload += () => false; //todo implement
            View.NewFolder += View_NewFolder;
            View.NewVault += View_NewVault;
            View.ValidateNewFolder += View_ValidateNewFolder;
            View.ValidateNewVault += View_ValidateNewVault;
            View.NewFile += View_NewFile;
            View.ValidateNewFile += View_ValidateNewFile;
            View.NewSymbolicLink += View_NewSymbolicLink;
            View.ValidateNewSymbolicLink += View_ValidateNewSymbolicLink;
            View.RenameFile += View_RenameFile;
            View.ValidateRenameFile += View_ValidateRenameFile;
            View.DuplicateFile += View_DuplicateFile;
            View.ValidateDuplicateFile += View_ValidateDuplicateFile;
            View.OpenUrl += View_OpenUrl;
            View.ValidateOpenWebUrl += View_ValidateOpenWebUrl;
            View.CreateShareLink += View_CreateShareLink;
            View.ValidateCreateShareLink += View_ValidateCreateShareLink;
            View.RequestFiles += View_RequestFiles;
            View.ValidateRequestFiles += View_ValidateRequestFiles;
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
            View.RestoreFile += View_RestoreFile;
            View.ValidateRestoreFile += View_ValidateRestoreFile;
            View.LockUnlockVault += View_LockUnlockVault;
            View.ValidateLockUnlockVault += View_ValidateLockUnlockVault;
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
            View.OpenInTerminal += View_OpenInTerminal;
            View.ValidateOpenInTerminal += View_ValidateOpenInTerminal;
            View.Stop += View_Disconnect;
            View.ValidateStop += View_ValidateStop;
            View.Disconnect += View_Disconnect;
            View.ValidateDisconnect += View_ValidateDisconnect;

            #endregion

            #region Commands - Bookmark

            View.ToggleBookmarks += View_ToggleBookmarks;
            View.SortBookmarksByHostname += View_SortBookmarksByHostname;
            View.SortBookmarksByNickname += View_SortBookmarksByNickname;
            View.SortBookmarksByProtocol += View_SortBookmarksByProtocol;

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
            View.ModelExtensionGetter = _browserModel.GetExtension;
            View.ModelRegionGetter = _browserModel.GetRegion;
            View.ModelVersionGetter = _browserModel.GetVersion;
            View.ModelStorageClassGetter = _browserModel.GetStorageClass;
            View.ModelChecksumGetter = _browserModel.GetChecksum;

            #endregion

            #region Bookmark model delegates

            View.BookmarkImageGetter = _bookmarkModel.GetBookmarkImage;
            View.BookmarkNicknameGetter = _bookmarkModel.GetNickname;
            View.BookmarkHostnameGetter = _bookmarkModel.GetHostname;
            View.BookmarkUsernameGetter = _bookmarkModel.GetUsername;
            View.BookmarkNotesGetter = _bookmarkModel.GetNotes;
            View.BookmarkStatusImageGetter = _bookmarkModel.GetBookmarkStatusImage;

            #endregion

            _bookmarkCollection.addListener(this);
            View.ViewClosedEvent += delegate { _bookmarkCollection.removeListener(this); };

            PopulateQuickConnect();
            PopulateEncodings();
            UpdateOpenIcon();

            View.ToolbarVisible = PreferencesFactory.get().getBoolean("browser.toolbar");
            View.LogDrawerVisible = PreferencesFactory.get().getBoolean("browser.transcript.open");

            View.GetEditorsForSelection += View_GetEditorsForSelection;
            View.GetBookmarks += View_GetBookmarks;
            View.GetHistory += View_GetHistory;
            View.GetBonjourHosts += View_GetBonjourHosts;
            View.ClearHistory += View_ClearHistory;
            View.ShowCertificate += View_Certificate;

            View.ValidatePathsCombobox += View_ValidatePathsCombobox;
            View.ValidateSearchField += View_ValidateSearchField;
            View.Expanding += View_Expanding;

            View.Exit += View_Exit;
            View.SetBookmarkModel(_bookmarkCollection, null);
            SetNavigation(false);
        }

        private bool View_ValidateRequestFiles()
        {
            if (IsMounted())
            {
                var selected = SelectedPath ?? Workdir;
                Share feature = (Share)Session.getFeature(typeof(Share));
                return feature != null && feature.isSupported(selected, Share.Type.upload);
            }
            return false;
        }

        private void View_RequestFiles()
        {
            var selected = SelectedPath ?? Workdir;
            RequestFilesAction requestFiles = new RequestFilesAction(this, selected);
            Background(requestFiles);
        }

        protected override void Invalidate()
        {
            foreach (KeyValuePair<Path, Editor> entry in _editors)
            {
                Editor editor = entry.Value;
                editor.close();
            }
            _editors.Clear();
            base.Invalidate();
        }

        private void View_LockUnlockVault()
        {
            Path directory = new UploadTargetFinder(Workdir).find(SelectedPath);
            if (directory.attributes().getVault() != null)
            {
                // Lock and remove all open vaults
                LockVaultAction lockVault = new LockVaultAction(this, Session.getVaultRegistry(), directory.attributes().getVault());
                Background(lockVault);
            }
            else
            {
                // Unlock vault
                LoadVaultAction loadVault = new LoadVaultAction(this, Session.getVaultRegistry(), directory);
                Background(loadVault);
            }
        }

        private void View_CreateShareLink()
        {
            var selected = SelectedPath ?? Workdir;
            ShareLinkAction share = new ShareLinkAction(this, selected);
            Background(share);
        }

        private bool View_ValidateCreateShareLink()
        {
            if (IsMounted())
            {
                var selected = SelectedPath ?? Workdir;
                Share feature = (Share)Session.getFeature(typeof(Share));
                return feature != null && feature.isSupported(selected, Share.Type.download);
            }
            return false;
        }

        public BrowserController() : this(ObjectFactory.GetInstance<IBrowserView>())
        {
        }

        /// <summary>
        /// The first selected path found or null if there is no selection
        /// </summary>
        public Path SelectedPath
        {
            get
            {
                IList<Path> selectedPaths = View.SelectedPaths;
                if (selectedPaths.Count > 0)
                {
                    return selectedPaths[0];
                }
                return null;
            }
        }

        public Path Workdir { get; set; }

        /// <summary>
        ///
        /// </summary>
        /// <value>
        ///   All selected paths or an empty list if there is no selection
        /// </value>
        public IList<Path> SelectedPaths
        {
            get
            {
                if (IsMounted())
                {
                    return View.SelectedPaths;
                }
                return new List<Path>();
            }
            set { View.SelectedPaths = value; }
        }

        public bool ShowHiddenFiles
        {
            get { return _showHiddenFiles; }
            set
            {
                FilenameFilter = value ? SearchFilterFactory.NULL_FILTER : SearchFilterFactory.HIDDEN_FILTER;
                _showHiddenFiles = value;
                View.HiddenFilesVisible = _showHiddenFiles;
            }
        }

        public PathCache Cache
        {
            get { return _cache; }
        }

        public Filter FilenameFilter { get; set; }

        public Comparator FilenameComparator
        {
            get { return _comparator; }
            set { _comparator = value; }
        }

        public SessionPool Session { get; private set; }

        public void collectionLoaded()
        {
            AsyncDelegate mainAction = delegate { ReloadBookmarks(); };
            Invoke(mainAction);
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

        public override void message(string msg)
        {
            string label;
            if (Utils.IsNotBlank(msg))
            {
                label = msg;
            }
            else
            {
                if (View.CurrentView == BrowserView.Bookmark || View.CurrentView == BrowserView.History ||
                    View.CurrentView == BrowserView.Bonjour)
                {
                    label = String.Format(LocaleFactory.localizedString("{0} Bookmarks"), View.NumberOfBookmarks);
                }
                else
                {
                    if (IsConnected())
                    {
                        label = String.Format(LocaleFactory.localizedString("{0} Items"), View.NumberOfFiles);
                    }
                    else
                    {
                        label = String.Empty;
                    }
                }
            }
            AsyncDelegate updateLabel = delegate { View.StatusLabel = label; };
            Invoke(updateLabel);
        }

        public override void log(TranscriptListener.Type request, string transcript)
        {
            if (View.LogDrawerVisible)
            {
                AsyncDelegate mainAction = delegate { View.AddTranscriptEntry(request, transcript); };
                Invoke(mainAction);
            }
        }

        private void View_SearchFieldEnter()
        {
            if (View.CurrentView == BrowserView.File)
            {
                String input = View.SearchString;
                // Setup search filter
                Filter filter = SearchFilterFactory.create(input, ShowHiddenFiles);
                SetFilter(filter);
                if (Utils.IsBlank(input))
                {
                    // Reload with current cache
                    Reload();
                }
                else
                {
                    TaskDialogResult result =
                        QuestionBox(String.Format(LocaleFactory.localizedString("Search for {0}"), input),
                            String.Format(LocaleFactory.localizedString("Do you want to search in {0} recursively?"),
                                Workdir.getName()), null, String.Format("{0}", LocaleFactory.localizedString("Search")),
                            true);
                    if (result.Button == 0)
                    {
                        background(new SearchAction(this));
                    }
                }
            }
        }

        private void View_Expanding(object sender, PathArgs e)
        {
            Reload(Workdir, new HashSet<Path>() { e.Path }, SelectedPaths, false);
        }

        public void RemoveDonateButton()
        {
            View.RemoveDonateButton();
        }

        private void View_NewSymbolicLink()
        {
            CreateSymlinkController slc =
                new CreateSymlinkController(ObjectFactory.GetInstance<ICreateSymlinkPromptView>(), this);
            slc.Show();
        }

        private bool View_ValidateNewSymbolicLink()
        {
            return IsMounted() && Session.getFeature(typeof(Symlink)) != null && SelectedPaths.Count == 1;
        }

        private void View_SortBookmarksByProtocol()
        {
            BookmarkCollection.defaultCollection().sort(BookmarkCollection.SORT_BY_PROTOCOL);
            ReloadBookmarks();
        }

        private void View_SortBookmarksByNickname()
        {
            BookmarkCollection.defaultCollection().sort(BookmarkCollection.SORT_BY_NICKNAME);
            ReloadBookmarks();
        }

        private void View_SortBookmarksByHostname()
        {
            BookmarkCollection.defaultCollection().sort(BookmarkCollection.SORT_BY_HOSTNAME);
            ReloadBookmarks();
        }

        private bool View_ValidateOpenInTerminal()
        {
            return IsMounted() && Session.getHost().getProtocol().getType() == Protocol.Type.sftp &&
                   TerminalServiceFactory.get() != null;
        }

        private void View_OpenInTerminal()
        {
            Path workdir = null;
            if (SelectedPaths.Count == 1)
            {
                Path selected = SelectedPath;
                if (selected.isDirectory())
                {
                    workdir = selected;
                }
            }
            if (null == workdir)
            {
                workdir = Workdir;
            }
            TerminalService terminal = TerminalServiceFactory.get();
            terminal.open(Session.getHost(), workdir);
        }

        private void View_SetComparator(BrowserComparator comparator)
        {
            if (!comparator.equals(_comparator))
            {
                _comparator = comparator;
                Reload();
            }
        }

        private IList<Application> View_GetEditorsForSelection()
        {
            Path p = SelectedPath;
            if (null != p)
            {
                if (p.isFile())
                {
                    return Utils.ConvertFromJavaList<Application>(EditorFactory.getEditors(p.getName()),
                        null);
                }
            }
            return new List<Application>();
        }

        private bool View_ValidateNewBrowser()
        {
            return IsMounted();
        }

        private List<KeyValuePair<String, List<DescriptiveUrl>>> View_GetCopyUrls()
        {
            List<KeyValuePair<String, List<DescriptiveUrl>>> items = new List<KeyValuePair<String, List<DescriptiveUrl>>>();
            IList<Path> selected = View.SelectedPaths;
            if (selected.Count == 0)
            {
                items.Add(new KeyValuePair<string, List<DescriptiveUrl>>(LocaleFactory.localizedString("None"),
                    new List<DescriptiveUrl>()));
            }
            else
            {
                UrlProvider urlProvider = ((UrlProvider)Session.getFeature(typeof(UrlProvider)));
                if (urlProvider != null)
                {
                    DescriptiveUrlBag urls = urlProvider.toUrl(SelectedPath);
                    for (int i = 0; i < urls.size(); i++)
                    {
                        DescriptiveUrl descUrl = (DescriptiveUrl)urls.toArray()[i];
                        KeyValuePair<String, List<DescriptiveUrl>> entry =
                            new KeyValuePair<string, List<DescriptiveUrl>>(descUrl.getHelp(), new List<DescriptiveUrl>());
                        items.Add(entry);
                        foreach (Path path in selected)
                        {
                            entry.Value.Add(((DescriptiveUrl)urlProvider.toUrl(path).toArray()[i]));
                        }
                    }
                }
            }
            return items;
        }

        private bool IsBrowser()
        {
            return View.CurrentView == BrowserView.File;
        }

        private IList<KeyValuePair<string, List<DescriptiveUrl>>> View_GetOpenUrls()
        {
            IList<KeyValuePair<String, List<DescriptiveUrl>>> items = new List<KeyValuePair<String, List<DescriptiveUrl>>>();
            IList<Path> selected = View.SelectedPaths;
            if (selected.Count == 0)
            {
                items.Add(new KeyValuePair<string, List<DescriptiveUrl>>(LocaleFactory.localizedString("None"),
                    new List<DescriptiveUrl>()));
            }
            else
            {
                UrlProvider urlProvider = ((UrlProvider)Session.getFeature(typeof(UrlProvider)));
                if (urlProvider != null)
                {
                    DescriptiveUrlBag urls = urlProvider.toUrl(SelectedPath)
                            .filter(DescriptiveUrl.Type.http, DescriptiveUrl.Type.cname, DescriptiveUrl.Type.cdn,
                                DescriptiveUrl.Type.signed, DescriptiveUrl.Type.authenticated);
                    for (int i = 0; i < urls.size(); i++)
                    {
                        DescriptiveUrl descUrl = (DescriptiveUrl)urls.toArray()[i];
                        KeyValuePair<String, List<DescriptiveUrl>> entry = new KeyValuePair<string, List<DescriptiveUrl>>(
                            descUrl.getHelp(), new List<DescriptiveUrl>());
                        items.Add(entry);
                        foreach (Path path in selected)
                        {
                            entry.Value.Add(((DescriptiveUrl)urls.toArray()[i]));
                        }
                    }
                }
            }
            return items;
        }

        public void UpdateBookmarks()
        {
            View.UpdateBookmarks();
        }

        private bool View_ValidateDuplicateBookmark()
        {
            return _bookmarkModel.Source.allowsEdit() && View.SelectedBookmarks.Count == 1;
        }

        private void View_DuplicateBookmark()
        {
            ToggleView(BrowserView.Bookmark);
            Host duplicate = new HostDictionary().deserialize(View.SelectedBookmark.serialize(SerializerFactory.get()));
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
                Host host =
                    new HostDictionary().deserialize(
                        ((Host)dropargs.SourceModels[0]).serialize(SerializerFactory.get()));
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
                    IList<TransferItem> roots = new List<TransferItem>();
                    Host host = null;
                    foreach (string filename in data.GetFileDropList())
                    {
                        //check if we received at least one non-duck file
                        if (!".duck".Equals(Utils.GetSafeExtension(filename)))
                        {
                            // The bookmark this file has been dropped onto
                            Host destination = (Host)e.DropTargetItem.RowObject;
                            if (null == host)
                            {
                                host = destination;
                            }
                            Local local = LocalFactory.get(filename);
                            // Upload to the remote host this bookmark points to
                            roots.Add(
                                new TransferItem(
                                    new Path(
                                        new Path(PathNormalizer.normalize(destination.getDefaultPath(), true),
                                            EnumSet.of(AbstractPath.Type.directory)), local.getName(),
                                        EnumSet.of(AbstractPath.Type.file)), local));
                        }
                    }
                    if (roots.Count > 0)
                    {
                        UploadTransfer q = new UploadTransfer(host, Utils.ConvertToJavaList(roots));
                        // If anything has been added to the queue, then process the queue
                        if (q.getRoots().size() > 0)
                        {
                            TransferController.Instance.StartTransfer(q);
                        }
                    }
                    return;
                }

                if (e.DropTargetLocation == DropTargetLocation.AboveItem)
                {
                    Host destination = (Host)e.DropTargetItem.RowObject;
                    foreach (string file in data.GetFileDropList())
                    {
                        try
                        {
                            _bookmarkModel.Source.add(_bookmarkModel.Source.indexOf(destination),
                                HostReaderFactory.get().read(LocalFactory.get(file)));
                        }
                        catch (AccessDeniedException ex)
                        {
                            Log.error($"Failure reading bookmark from {file}. {ex.getMessage()}");
                        }
                    }
                }
                if (e.DropTargetLocation == DropTargetLocation.BelowItem)
                {
                    Host destination = (Host)e.DropTargetItem.RowObject;
                    foreach (string file in data.GetFileDropList())
                    {
                        try
                        {
                            _bookmarkModel.Source.add(_bookmarkModel.Source.indexOf(destination) + 1,
                                HostReaderFactory.get().read(LocalFactory.get(file)));
                        }
                        catch (AccessDeniedException ex)
                        {
                            Log.error($"Failure reading bookmark from {file}. {ex.getMessage()}");
                        }
                    }
                }
                if (e.DropTargetLocation == DropTargetLocation.Background)
                {
                    foreach (string file in data.GetFileDropList())
                    {
                        try
                        {
                            _bookmarkModel.Source.add(HostReaderFactory.get().read(LocalFactory.get(file)));
                        }
                        catch (AccessDeniedException ex)
                        {
                            Log.error($"Failure reading bookmark from {file}. {ex.getMessage()}");
                        }
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

                            DropTargetHelper.SetDropDescription(dataObject, args.Effect,
                                "Upload to %1", BookmarkNameProvider.toString(destination));
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
            RemoveTemporaryFilesystemWatcher();
        }

        private string CreateAndWatchTemporaryFile(FileSystemEventHandler del)
        {
            string tfile = System.IO.Path.Combine(System.IO.Path.GetTempPath(), Guid.NewGuid().ToString());
            using (File.Create(tfile))
            {
                FileInfo tmpFile = new FileInfo(tfile);
                tmpFile.Attributes |= FileAttributes.Hidden;
            }
            DriveInfo[] allDrives = DriveInfo.GetDrives();
            foreach (DriveInfo d in allDrives)
            {
                if (d.IsReady && d.DriveType != DriveType.CDRom)
                {
                    try
                    {
                        FileSystemWatcher watcher = new FileSystemWatcher(@d.Name, System.IO.Path.GetFileName(tfile));
                        watcher.BeginInit();
                        watcher.IncludeSubdirectories = true;
                        watcher.EnableRaisingEvents = true;
                        watcher.Created += del;
                        watcher.EndInit();
                        _temporaryWatcher.Add(watcher);
                    }
                    catch (Exception e)
                    {
                        Log.info(string.Format("Cannot watch drive {0}", d), e);
                    }
                }
            }
            return tfile;
        }

        private DataObject View_HostDrag(ObjectListView list)
        {
            DataObject data = new DataObject(DataFormats.FileDrop,
                new[]
                {
                    CreateAndWatchTemporaryFile(delegate(object sender, FileSystemEventArgs args)
                    {
                        Invoke(delegate
                        {
                            _dropFolder = System.IO.Path.GetDirectoryName(args.FullPath);
                            foreach (Host host in
                                View.SelectedBookmarks)
                            {
                                string filename = BookmarkNameProvider.toString(host) + ".duck";
                                foreach (char c in
                                    System.IO.Path.GetInvalidFileNameChars())
                                {
                                    filename = filename.Replace(c.ToString(), String.Empty);
                                }

                                Local file = LocalFactory.get(_dropFolder, filename);
                                HostWriterFactory.get().write(host, file);
                            }
                        });
                    })
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
                        destination = (Path)args.DropTargetItem.RowObject;
                        if (!destination.isDirectory())
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
                Touch feature = (Touch)Session.getFeature(typeof(Touch));
                if (!feature.isSupported(destination, String.Empty))
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                    return;
                }

                if (args.ListView == args.SourceListView)
                {
                    foreach (Path sourcePath in args.SourceModels)
                    {
                        if (sourcePath.isDirectory() && sourcePath.equals(destination))
                        {
                            // Do not allow dragging onto myself.
                            args.Effect = DragDropEffects.None;
                            args.DropTargetLocation = DropTargetLocation.None;
                            return;
                        }
                        if (sourcePath.isDirectory() && destination.isChild(sourcePath))
                        {
                            // Do not allow dragging a directory into its own containing items
                            args.Effect = DragDropEffects.None;
                            args.DropTargetLocation = DropTargetLocation.None;
                            return;
                        }
                        if (sourcePath.isFile() && sourcePath.getParent().equals(destination))
                        {
                            // Moving file to the same destination makes no sense
                            args.Effect = DragDropEffects.None;
                            args.DropTargetLocation = DropTargetLocation.None;
                            return;
                        }
                    }
                    if (args.Effect == DragDropEffects.Move)
                    {
                        Move move = (Move)Session.getFeature(typeof(Move));
                        foreach (Path sourcePath in args.SourceModels)
                        {
                            if (!move.isSupported(sourcePath, destination))
                            {
                                args.Effect = DragDropEffects.None;
                                args.DropTargetLocation = DropTargetLocation.None;
                                return;
                            }
                        }
                    }
                }
                else
                {
                    args.Effect = DragDropEffects.Copy;
                }
                if (Workdir == destination)
                {
                    args.DropTargetLocation = DropTargetLocation.Background;
                }
                else
                {
                    args.DropTargetItem = args.ListView.ModelToItem(destination);
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
                    destination = (Path)dropargs.DropTargetItem.RowObject;
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
                foreach (Path next in dropargs.SourceModels)
                {
                    Path renamed = new Path(destination, next.getName(), EnumSet.of(next.isDirectory() ? Path.Type.directory : Path.Type.file));
                    files.Add(next, renamed);
                }
                if (files.Count > 0)
                {
                    if (dropargs.Effect == DragDropEffects.Copy)
                    {
                        foreach (BrowserController controller in MainController.Browsers)
                        {
                            // Find source browser
                            if (controller.View.Browser.Equals(dropargs.SourceListView))
                            {
                                if (new AbstractHostCollection.ProfilePredicate(controller.Session.getHost()).test(Session.getHost()))
                                {
                                    CopyPaths(files);
                                }
                                else
                                {
                                    transfer(
                                        new CopyTransfer(controller.Session.getHost(), Session.getHost(),
                                            Utils.ConvertToJavaMap(files)), new List<Path>(files.Values), false);
                                }
                                break;
                            }
                        }
                    }
                    if (dropargs.Effect == DragDropEffects.Move)
                    {
                        // The file should be renamed
                        RenamePaths(files);
                    }
                }
            }
        }

        private void View_Download()
        {
            Download(SelectedPaths, new DownloadDirectoryFinder().find(Session.getHost()));
        }

        private bool View_ValidateRevertFile()
        {
            if (IsBrowser() && IsMounted() && SelectedPaths.Count > 0)
            {
                var feature = (Versioning)Session.getFeature(typeof(Versioning));
                if (feature != null)
                {
                    foreach (var path in SelectedPaths)
                    {
                        if (!feature.isRevertable(path))
                        {
                            return false;
                        }
                    }

                    return true;
                }
            }
            return false;
        }
        private bool View_ValidateRestoreFile()
        {
            if (IsBrowser() && IsMounted() && SelectedPaths.Count > 0)
            {
                var feature = (Restore)Session.getFeature(typeof(Restore));
                if (feature != null)
                {
                    foreach (var path in SelectedPaths)
                    {
                        if (!feature.isRestorable(path))
                        {
                            return false;
                        }
                    }

                    return true;
                }
            }
            return false;
        }
        private bool View_ValidateLockUnlockVault()
        {
            if (IsBrowser() && IsMounted() && !PreferencesFactory.get().getBoolean("cryptomator.vault.autodetect"))
            {
                Path selected = new UploadTargetFinder(Workdir).find(SelectedPath);
                VaultRegistry registry = Session.getVaultRegistry();
                if (registry.contains(selected))
                {
                    View.SetCryptomatorVaultTitle(LocaleFactory.localizedString("Lock Vault", "Cryptomator"));
                    return true;

                }
                View.SetCryptomatorVaultTitle(LocaleFactory.localizedString("Unlock Vault", "Cryptomator"));
                return null != Cache.get(Workdir).find(new SimplePathPredicate(Path.Type.file,
                    String.Format("{0}{1}{2}", Workdir.getAbsolute(), Path.DELIMITER, DefaultVaultRegistry.DEFAULT_MASTERKEY_FILE_NAME)));
            }
            return false;
        }

        private void View_RevertFile()
        {
            RevertPaths(SelectedPaths);
        }

        private void RevertPaths(IList<Path> files)
        {
            Background(new RevertAction(this, files));
        }

        private void View_RestoreFile()
        {
            RestorePaths(SelectedPaths);
        }

        private void RestorePaths(IList<Path> files)
        {
            Background(new RestoreAction(this, files));
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
            SetNavigation(View.CurrentView == BrowserView.File && IsMounted());
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
            SetStatus();
        }

        private void View_Certificate()
        {
            X509TrustManager feature = (X509TrustManager)Session.getFeature(typeof(X509TrustManager));
            if (feature != null)
            {
                List certificates = Arrays.asList(feature.getAcceptedIssuers());
                if (certificates.isEmpty())
                {
                    return;
                }
                X509Certificate2 cert = SystemCertificateStore.ConvertCertificate(certificates.iterator().next() as X509Certificate);
                X509Certificate2UI.DisplayCertificate(cert);
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
            list.AddRange(new DefaultCharsetProvider().availableCharsets());
            View.PopulateEncodings(list);
            View.SelectedEncoding = PreferencesFactory.get().getProperty("browser.charset.encoding");
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
                if (Session.getHost().getEncoding().Equals(encoding))
                {
                    return;
                }
                Session.getHost().setEncoding(encoding);
                Mount(Session.getHost());
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
                DefaultBookmarkController.Factory.Create(View.SelectedBookmark).View.Show(View);
            }
        }

        private void View_NewBookmark()
        {
            Host bookmark;
            if (IsMounted())
            {
                Path selected = SelectedPath;
                if (null == selected || !selected.isDirectory())
                {
                    selected = Workdir;
                }
                bookmark = new HostDictionary().deserialize(Session.getHost().serialize(SerializerFactory.get()));
                bookmark.setUuid(null);
                bookmark.setDefaultPath(selected.getAbsolute());
            }
            else
            {
                bookmark =
                    new Host(ProtocolFactory.get().forNameOrDefault(PreferencesFactory.get()
                        .getProperty("connection.protocol.default")));
            }
            ToggleView(BrowserView.Bookmark);
            AddBookmark(bookmark);
        }

        public void AddBookmark(Host item)
        {
            AddBookmark(item, -1);
        }

        private void AddBookmark(Host item, int index)
        {
            _bookmarkModel.Filter = null;
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
            DefaultBookmarkController.Factory.Create(item).View.Show(View);
        }

        private void View_DeleteBookmark()
        {
            List<Host> selected = View.SelectedBookmarks;
            StringBuilder alertText = new StringBuilder();
            int i = 0;
            foreach (Host host in selected)
            {
                if (i > 0)
                {
                    alertText.Append("\n");
                }
                alertText.Append(Character.toString('\u2022')).Append(" ").Append(BookmarkNameProvider.toString(host));
                i++;
                if (i > 10)
                {
                    break;
                }
            }
            TaskDialogResult result = QuestionBox(LocaleFactory.localizedString("Delete Bookmark"),
                LocaleFactory.localizedString("Do you want to delete the selected bookmark?"), alertText.ToString(),
                String.Format("{0}", LocaleFactory.localizedString("Delete")), true);
            if (result.Button == 0)
            {
                _bookmarkModel.Source.removeAll(Utils.ConvertToJavaList(selected));
            }
        }

        public override bool ViewShouldClose()
        {
            return Unmount();
        }

        private void View_OpenUrl()
        {
            DescriptiveUrlBag list;
            if (SelectedPaths.Count == 1)
            {
                list = ((UrlProvider)Session.getFeature(typeof(UrlProvider))).toUrl(SelectedPath);
            }
            else
            {
                list = ((UrlProvider)Session.getFeature(typeof(UrlProvider))).toUrl(Workdir);
            }
            if (!list.isEmpty())
            {
                BrowserLauncherFactory.get().open(list.find(DescriptiveUrl.Type.http).getUrl());
            }
        }

        private void View_SearchFieldChanged()
        {
            switch (View.CurrentView)
            {
                case BrowserView.File:
                    SetFilter(SearchFilterFactory.create(View.SearchString, ShowHiddenFiles));
                    // Reload with current cache
                    Reload();
                    break;
                case BrowserView.Bookmark:
                case BrowserView.History:
                case BrowserView.Bonjour:
                    SetBookmarkFilter(View.SearchString);
                    break;
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
                _bookmarkModel.Filter = new BookmarkSearchFilter(searchString);
            }
            ReloadBookmarks();
        }

        private bool View_ValidateDisconnect()
        {
            return !IsIdle() || IsConnected();
        }

        private bool View_ValidateStop()
        {
            return !IsIdle();
        }

        private bool View_ValidateSendCustomCommand()
        {
            return IsMounted() && Session.getFeature(typeof(Command)) != null;
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
            return IsMounted() && _navigation.getForward().size() > 0;
        }

        private bool View_ValidateHistoryBack()
        {
            return IsMounted() && _navigation.getBack().size() > 1;
        }

        private bool View_ValidateGotoFolder()
        {
            return IsMounted();
        }

        private bool View_ValidateRefresh()
        {
            return IsMounted();
        }

        private void View_Disconnect()
        {
            // Remove all pending actions)
            foreach (BackgroundAction action in registry.toArray(new BackgroundAction[registry.size()]))
            {
                action.cancel();
            }
            CallbackDelegate run = delegate
            {
                if (PreferencesFactory.get().getBoolean("browser.disconnect.bookmarks.show"))
                {
                    ToggleView(BrowserView.Bookmark);
                }
                else
                {
                    ToggleView(BrowserView.File);
                }
            };
            UnmountImpl(run);
        }

        /**
         * Unmount this session
         */

        private void Disconnect(CallbackDelegate runnable)
        {
            InfoController infoController = _inspector;
            if (infoController != null)
            {
                infoController.View.Close();
            }
            Background(new DisconnectAction(this, runnable));
        }

        private void View_SendCustomCommand()
        {
            new CommandController(this, Session).View.ShowDialog();
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
            if (selected.isDirectory())
            {
                SetWorkdir(selected);
            }
            else if (selected.isFile() || View.SelectedPaths.Count > 1)
            {
                if (PreferencesFactory.get().getBoolean("browser.doubleclick.edit"))
                {
                    View_EditEvent(null);
                }
                else
                {
                    View_Download();
                }
            }
        }

        public void Download(IList<Path> downloads, Local downloadFolder)
        {
            if (downloads.Count > 0)
            {
                IList<TransferItem> items = new List<TransferItem>();
                foreach (Path selected in downloads)
                {
                    items.Add(new TransferItem(selected, LocalFactory.get(downloadFolder, selected.getName())));
                }
                Transfer q = new DownloadTransfer(Session.getHost(), Utils.ConvertToJavaList(items));
                transfer(q, downloads);
            }
        }

        private void View_GotoFolder()
        {
            GotoController gc = new GotoController(ObjectFactory.GetInstance<IGotoPromptView>(), this);
            gc.Show();
        }

        private void View_RefreshBrowser()
        {
            if (IsMounted())
            {
                // Find folders to reload
                ISet<Path> folders = new HashSet<Path>();
                foreach (Path path in View.VisiblePaths)
                {
                    if (null == path) continue;
                    if (path.isDirectory())
                    {
                        // Invalidate cache regardless if rendered. Fix CD-2340
                        _cache.invalidate(path);
                        if (View.IsExpanded(path))
                        {
                            folders.Add(path);
                        }
                    }
                }
                folders.Add(Workdir);
                Reload(Workdir, folders, SelectedPaths, true);
            }
        }

        private bool View_ValidateTextEncoding()
        {
            return IsMounted();
        }

        private void View_ToggleLogDrawer()
        {
            View.LogDrawerVisible = !View.LogDrawerVisible;
            PreferencesFactory.get().setProperty("browser.transcript.open", View.LogDrawerVisible);
        }

        private void View_ShowHiddenFiles()
        {
            ShowHiddenFiles = !ShowHiddenFiles;
            if (IsMounted())
            {
                Reload();
            }
        }

        private void View_ToggleToolbar()
        {
            View.ToolbarVisible = !View.ToolbarVisible;
            PreferencesFactory.get().setProperty("browser.toolbar", View.ToolbarVisible);
        }

        private bool View_ValidatePaste()
        {
            return IsBrowser() && IsMounted() && !_pasteboard.isEmpty();
        }

        private void View_Paste()
        {
            IDictionary<Path, Path> files = new Dictionary<Path, Path>();
            Path parent = Workdir;
            for (int i = 0; i < _pasteboard.size(); i++)
            {
                Path next = (Path)_pasteboard.get(i);
                Path renamed = new Path(parent, next.getName(), next.getType(), new PathAttributes(next.attributes()));
                files.Add(next, renamed);
            }
            _pasteboard.clear();
            if (_pasteboard.isCut())
            {
                RenamePaths(files);
            }
            if (_pasteboard.isCopy())
            {
                CopyPaths(files);
            }
        }

        private bool View_ValidateCopy()
        {
            return IsBrowser() && IsMounted() && SelectedPaths.Count > 0;
        }

        private void View_Copy()
        {
            _pasteboard.clear();
            _pasteboard.setCopy(true);
            foreach (Path p in SelectedPaths)
            {
                // Writing data for private use when the item gets dragged to the transfer queue.
                _pasteboard.add(p);
            }
        }

        private bool View_ValidateCut()
        {
            return IsBrowser() && IsMounted() && SelectedPaths.Count > 0;
        }

        private void View_Cut()
        {
            _pasteboard.clear();
            _pasteboard.setCut(true);
            foreach (Path s in SelectedPaths)
            {
                // Writing data for private use when the item gets dragged to the transfer queue.
                _pasteboard.add(s);
            }
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
            MainController.Exit(false);
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
                if (Session.getFeature(typeof(Compress)) == null)
                {
                    return false;
                }
                if (SelectedPaths.Count > 0)
                {
                    foreach (Path selected in SelectedPaths)
                    {
                        if (selected.isDirectory())
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
                if (CheckOverwrite(Utils.ConvertFromJavaList<Path>(archive.getExpanded(new ArrayList { selected }))))
                {
                    background(new UnarchiveAction(this, archive, selected, expanded));
                }
            }
        }

        private bool View_ValidateCreateArchive()
        {
            if (IsMounted())
            {
                if (Session.getFeature(typeof(Compress)) == null)
                {
                    return false;
                }
                if (SelectedPaths.Count > 0)
                {
                    foreach (Path selected in SelectedPaths)
                    {
                        if (selected.isFile() && Archive.isArchive(selected.getName()))
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
            IList<Path> selected = SelectedPaths;
            if (CheckOverwrite(new List<Path> { archive.getArchive(Utils.ConvertToJavaList(selected)) }))
            {
                background(new CreateArchiveAction(this, archive, selected));
            }
        }

        private bool View_ValidateDelete()
        {
            if (IsBrowser() && IsMounted() && SelectedPaths.Count > 0)
            {
                foreach (var path in SelectedPaths)
                {
                    if (!((Delete)Session.getFeature(typeof(Delete))).isSupported(path))
                    {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        private bool View_ValidateSynchronize()
        {
            return IsMounted();
        }

        private void View_Synchronize()
        {
            Path selected;
            if (SelectedPaths.Count == 1 && SelectedPath.isDirectory())
            {
                selected = SelectedPath;
            }
            else
            {
                selected = Workdir;
            }
            string folder =
                View.SynchronizeDialog(
                    String.Format(LocaleFactory.localizedString("Synchronize {0} with"), selected.getName()),
                    new UploadDirectoryFinder().find(Session.getHost()), null);
            if (null != folder)
            {
                Local target = LocalFactory.get(folder);
                new UploadDirectoryFinder().save(Session.getHost(), target.getParent());
                transfer(new SyncTransfer(Session.getHost(), new TransferItem(selected, target)));
            }
        }

        private bool View_ValidateNewFile()
        {
            return IsMounted() &&
                   ((Touch)Session.getFeature(typeof(Touch))).isSupported(
                       new UploadTargetFinder(Workdir).find(SelectedPath), String.Empty);
        }

        private bool View_ValidateUpload()
        {
            return IsMounted() &&
                   ((Touch)Session.getFeature(typeof(Touch))).isSupported(
                       new UploadTargetFinder(Workdir).find(SelectedPath), String.Empty);
        }

        private void View_Upload()
        {
            // Due to the limited functionality of the OpenFileDialog class it is
            // currently not possible to select a folder. May be we should provide
            // a second menu item which allows to select a folder to upload
            string[] paths = View.UploadDialog(new UploadDirectoryFinder().find(Session.getHost()));
            if (null == paths || paths.Length == 0) return;

            bool parentFound = false;
            Local parent = null;
            Path destination = new UploadTargetFinder(Workdir).find(SelectedPath);
            List uploads = Utils.ConvertToJavaList(paths, delegate (string path)
            {
                Local local = LocalFactory.get(path);
                Local localParent = local.getParent();

                if (!parentFound && localParent != parent)
                {
                    parentFound = true;
                    parent = localParent;
                }
                else if (parentFound && localParent != parent)
                {
                    parent = null;
                }

                return
                    new TransferItem(
                        new Path(destination, local.getName(),
                            local.isDirectory()
                                ? EnumSet.of(AbstractPath.Type.directory)
                                : EnumSet.of(AbstractPath.Type.file)), local);
            });
            if (parent != null)
            {
                new UploadDirectoryFinder().save(Session.getHost(), parent);
            }
            transfer(new UploadTransfer(Session.getHost(), uploads));
        }

        private void View_DownloadTo()
        {
            string folder = View.DownloadToDialog(LocaleFactory.localizedString("Download To…"),
                new DownloadDirectoryFinder().find(Session.getHost()), null);
            if (null != folder && SelectedPaths.Count > 0)
            {
                Local target = LocalFactory.get(folder);
                new DownloadDirectoryFinder().save(Session.getHost(), target);
                IList<TransferItem> downloads = new List<TransferItem>();
                foreach (Path file in SelectedPaths)
                {
                    downloads.Add(new TransferItem(file, LocalFactory.get(target, file.getName())));
                }
                transfer(new DownloadTransfer(Session.getHost(), Utils.ConvertToJavaList(downloads)), SelectedPaths);
            }
        }

        private bool View_ValidateDownloadAs()
        {
            return IsMounted() && SelectedPaths.Count == 1;
        }

        private void View_DownloadAs()
        {
            string filename = View.DownloadAsDialog(new DownloadDirectoryFinder().find(Session.getHost()),
                SelectedPath.getName());
            if (null != filename)
            {
                Local target = LocalFactory.get(filename);
                Path selected = SelectedPath;
                new DownloadDirectoryFinder().save(Session.getHost(), target.getParent());
                IList<TransferItem> downloads = new List<TransferItem>();
                downloads.Add(new TransferItem(selected, target));
                List<Path> seletionList = new List<Path>();
                seletionList.Add(selected);
                transfer(new DownloadTransfer(Session.getHost(), Utils.ConvertToJavaList(downloads)), seletionList);
            }
        }

        private bool View_ValidateDownload()
        {
            return IsMounted() && SelectedPaths.Count > 0;
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
            if (Session.getHost().getCredentials().isAnonymousLogin())
            {
                return false;
            }
            return selected.isFile();
        }

        private bool View_ValidateDuplicateFile()
        {
            return IsMounted() && Session.getFeature(typeof(Copy)) != null && SelectedPaths.Count == 1;
        }

        private bool View_ValidateRenameFile()
        {
            if (IsMounted() && SelectedPaths.Count == 1)
            {
                if (null == SelectedPath)
                {
                    return false;
                }
                return ((Move)Session.getFeature(typeof(Move))).isSupported(SelectedPath, SelectedPath);
            }
            return false;
        }

        private void View_NewDownload()
        {
            throw new NotImplementedException();
        }

        private void View_OpenConnection()
        {
            ConnectionController c = ConnectionController.Instance(this);
            DialogResult result = c.View.ShowDialog(View);
            if (result == DialogResult.OK)
            {
                Mount(c.Bookmark());
            }
        }

        private bool View_ValidateNewFolder()
        {
            return IsMounted() &&
                   ((Directory)Session.getFeature(typeof(Directory))).isSupported(
                       new UploadTargetFinder(Workdir).find(SelectedPath), String.Empty);
        }

        private bool View_ValidateNewVault()
        {
            return IsMounted() && Session.getVaultRegistry() != VaultRegistry.DISABLED &&
                   null == Workdir.attributes().getVault() &&
                   ((Directory)Session.getFeature(typeof(Directory))).isSupported(
                       new UploadTargetFinder(Workdir).find(SelectedPath), String.Empty);
        }

        private void View_DuplicateFile()
        {
            DuplicateFileController dc =
                new DuplicateFileController(ObjectFactory.GetInstance<IDuplicateFilePromptView>(), this);
            dc.Show();
        }

        private void View_NewFile()
        {
            CreateFileController fc =
                new CreateFileController(ObjectFactory.GetInstance<ICreateFilePromptView>(), this);
            fc.Show();
        }

        private void View_Delete()
        {
            DeletePaths(SelectedPaths);
        }

        private void View_NewFolder()
        {
            Location feature = (Location)Session.getFeature(typeof(Location));
            FolderController fc = new FolderController(ObjectFactory.GetInstance<INewFolderPromptView>(), this,
                feature != null
                    ? (IList<Location.Name>)Utils.ConvertFromJavaList<Location.Name>(feature.getLocations())
                    : new List<Location.Name>(), feature != null ? feature.getDefault() : Location.unknown);
            fc.Show();
        }

        private void View_NewVault()
        {
            Location feature = (Location)Session.getFeature(typeof(Location));
            FolderController fc = new VaultController(ObjectFactory.GetInstance<INewVaultPromptView>(), this,
                feature != null
                    ? (IList<Location.Name>)Utils.ConvertFromJavaList<Location.Name>(feature.getLocations())
                    : new List<Location.Name>(), feature != null ? feature.getDefault() : Location.unknown);
            fc.Show();
        }

        private bool View_RenameFile(Path path, string newName)
        {
            if (!String.IsNullOrEmpty(newName) && !newName.Equals(path.getName()))
            {
                Path renamed = new Path(path.getParent(), newName, path.getType());
                RenamePath(path, renamed);
            }
            return false;
        }

        private DataObject View_BrowserDrag(ObjectListView listView)
        {
            DataObject data = new DataObject(DataFormats.FileDrop,
                new[]
                {
                    CreateAndWatchTemporaryFile(delegate(object sender, FileSystemEventArgs args)
                    {
                        _dropFolder = System.IO.Path.GetDirectoryName(args.FullPath);
                        Invoke(delegate { Download(SelectedPaths, LocalFactory.get(_dropFolder)); });
                    })
                });
            return data;
        }

        private void RemoveTemporaryFilesystemWatcher()
        {
            BeginInvoke(delegate
            {
                foreach (FileSystemWatcher watcher in _temporaryWatcher)
                {
                    watcher.Dispose();
                }
                _temporaryWatcher.Clear();
            });
        }

        private void RemoveTemporaryFiles(DataObject data)
        {
            if (data.ContainsFileDropList())
            {
                foreach (string tmpFile in data.GetFileDropList())
                {
                    try
                    {
                        if (File.Exists(tmpFile))
                        {
                            File.Delete(tmpFile);
                        }
                        if (null != _dropFolder)
                        {
                            string tmpDestFile =
                                System.IO.Path.Combine(_dropFolder, System.IO.Path.GetFileName(tmpFile));
                            if (File.Exists(tmpDestFile))
                            {
                                File.Delete(tmpDestFile);
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        Log.error("Could not remove temporary files.", e);
                    }
                }
            }
        }

        private void View_BrowserEndDrag(DataObject data)
        {
            RemoveTemporaryFiles(data);
            RemoveTemporaryFilesystemWatcher();
        }

        private void View_BrowserDropped(OlvDropEventArgs e)
        {
            if (IsMounted() && e.DataObject is DataObject && ((DataObject)e.DataObject).ContainsFileDropList())
            {
                Path destination;
                switch (e.DropTargetLocation)
                {
                    case DropTargetLocation.Item:
                        destination = (Path)e.DropTargetItem.RowObject;
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
                    IList<TransferItem> roots = new List<TransferItem>();
                    foreach (string file in dropList)
                    {
                        Local local = LocalFactory.get(file);
                        roots.Add(
                            new TransferItem(
                                new Path(destination, local.getName(),
                                    local.isDirectory()
                                        ? EnumSet.of(AbstractPath.Type.directory)
                                        : EnumSet.of(AbstractPath.Type.file)), local));
                    }
                    UploadDroppedPath(roots, destination);
                }
            }
        }

        public void UploadDroppedPath(IList<TransferItem> roots, Path destination)
        {
            if (IsMounted())
            {
                UploadTransfer q = new UploadTransfer(Session.getHost(), Utils.ConvertToJavaList(roots));
                if (q.getRoots().size() > 0)
                {
                    transfer(q);
                }
            }
        }

        /// <summary>
        /// Check if we accept drag operation from an external program
        /// </summary>
        /// <param name="args"></param>
        private void View_BrowserCanDrop(OlvDropEventArgs args)
        {
            Log.trace("Entering View_BrowserCanDrop with " + args.Effect);
            if (IsMounted() && !(args.DataObject is OLVDataObject))
            {
                if (args.DataObject is DataObject dataObject && dataObject.ContainsFileDropList())
                {
                    Path destination;
                    switch (args.DropTargetLocation)
                    {
                        case DropTargetLocation.Item:
                            destination = (Path)args.DropTargetItem.RowObject;
                            if (!destination.isDirectory())
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
                    Touch feature = (Touch)Session.getFeature(typeof(Touch));
                    if (!feature.isSupported(destination, String.Empty))
                    {
                        Log.trace("Session does not allow file creation");
                        args.Effect = DragDropEffects.None;
                        args.DropTargetLocation = DropTargetLocation.None;
                        return;
                    }
                    Log.trace("Setting effect to copy");
                    args.Effect = DragDropEffects.Copy;
                    if (Workdir == destination)
                    {
                        args.DropTargetLocation = DropTargetLocation.Background;
                    }
                    else
                    {
                        args.DropTargetItem = args.ListView.ModelToItem(destination);
                    }
                    DropTargetHelper.SetDropDescription(dataObject, args.Effect,
                        "Copy to %1", destination.getName());
                }
            }
        }

        private void View_ShowTransfers()
        {
            TransferController.Instance.ShowWindow();
            //var view = new TransfersWindow();
            //view.Show();
            /*ITransferView view = TransferController.Instance.View;

            bool isOnCurrentDesktop = true;
            try
            {
                isOnCurrentDesktop = view.IsOnCurrentDesktop();
            }
            catch (Exception e)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug("Failure determining whether window is on current desktop", e);
                }
            }

            if (!isOnCurrentDesktop)
            {
                Guid? currentDesktop = null;
                try
                {
                    currentDesktop = View.GetDesktopId();
                }
                catch (Exception e)
                {
                    if (Log.isDebugEnabled())
                    {
                        Log.debug("Cannot get browser window desktop id", e);
                    }
                }

                if (currentDesktop is { } id)
                {
                    try
                    {
                        view.MoveToDesktop(id);
                    }
                    catch (Exception e)
                    {
                        if (Log.isDebugEnabled())
                        {
                            Log.debug("cannot move window to desktop.", e);
                        }
                    }
                }
            }
            
            view.Show();*/
        }

        private void View_ShowInspector()
        {
            IList<Path> selected = SelectedPaths;
            if (selected.Count > 0)
            {
                if (PreferencesFactory.get().getBoolean("browser.info.inspector"))
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

        private void View_EditEvent(Application app)
        {
            foreach (Path selected in SelectedPaths)
            {
                edit(app, selected);
            }
        }

        public void edit(Path file)
        {
            edit(EditorFactory.getEditor(file.getName()), file);
        }

        public void edit(Application application, Path file)
        {
            Editor editor;
            if (!_editors.TryGetValue(file, out editor))
            {
                _editors.Add(file, editor = EditorFactory.instance().create(Session.getHost(), file, this));
            }
            application ??= EditorFactory.getEditor(file.getAbsolute());
            application ??= EditorFactory.getDefaultEditor();
            background(new WorkerBackgroundAction(this, Session,
                editor.open(application, new EditorApplicationQuitCallback(_editors, file),
                    new DefaultEditorListener(this, Session, editor, new ReloadEditorListener(this, file)))));
        }

        private class EditorApplicationQuitCallback : ApplicationQuitCallback
        {
            private readonly IDictionary<Path, Editor> _editors;
            private readonly Path _file;

            public EditorApplicationQuitCallback(IDictionary<Path, Editor> editors, Path file)
            {
                _editors = editors;
                _file = file;
            }

            public void callback() {
                _editors.Remove(_file);
            }
        }

        private class ReloadEditorListener : DefaultEditorListener.Listener
        {
            private readonly BrowserController _controller;
            private readonly Path _file;

            public ReloadEditorListener(BrowserController controller, Path file)
            {
                _controller = controller;
                _file = file;
            }

            public void saved()
            {
                var selected = new PathReloadFinder().find(Collections.singletonList(_file));
                HashSet<Path> folders = new HashSet<Path>();
                Iterator it = selected.iterator();
                while (it.hasNext())
                {
                    folders.Add((Path)it.next());
                }
                _controller.Reload(_controller.Workdir, folders, Utils.ConvertFromJavaList<Path>(Collections.singletonList(_file),
                    item => (Path)item), true);
            }
        }

        private void UpdateEditIcon()
        {
            System.Drawing.Image image = Images.Pencil.Size(32);
            Path selected = SelectedPath;
            if (null != selected)
            {
                if (IsEditable(selected))
                {
                    Application app = EditorFactory.getEditor(selected.getName());
                    if (IconProvider.GetApplication(app, 32) is System.Drawing.Image appIcon)
                    {
                        image = appIcon;
                    }
                }
            }
            View.EditIcon = image;
        }

        private void UpdateOpenIcon()
        {
            View.OpenIcon = IconProvider.DefaultBrowser();
        }

        private void View_BrowserSelectionChanged()
        {
            UpdateEditIcon();

            // update inspector content if available
            IList<Path> selectedPaths = SelectedPaths;

            if (PreferencesFactory.get().getBoolean("browser.info.inspector"))
            {
                if (_inspector != null && _inspector.Visible)
                {
                    if (selectedPaths.Count > 0)
                    {
                        _inspector.Files = selectedPaths;
                    }
                }
            }
        }

        private void View_PathSelectionChanged()
        {
            string selected = View.SelectedComboboxPath;
            if (selected != null)
            {
                Path workdir = Workdir;
                Path p = workdir;
                while (!p.getAbsolute().Equals(selected))
                {
                    p = p.getParent();
                }
                SetWorkdir(p);
                if (workdir.getParent().equals(p))
                {
                    SetWorkdir(p, workdir);
                }
                else
                {
                    SetWorkdir(p);
                }
            }
        }

        private void View_FolderUp()
        {
            Path previous = Workdir;
            SetWorkdir(previous.getParent(), previous);
        }

        private void View_HistoryBack()
        {
            Path selected = _navigation.back();
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

        private void View_HistoryForward()
        {
            Path selected = _navigation.forward();
            if (selected != null)
            {
                SetWorkdir(selected);
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
            AbstractHostCollection bookmarkCollection = BookmarkCollection.defaultCollection();
            foreach (Host host in bookmarkCollection)
            {
                if (BookmarkNameProvider.toString(host).Equals(input))
                {
                    Mount(host);
                    return;
                }
            }
            try
            {
                Mount(HostParser.parse(input));
            }
            catch (HostParserException e)
            {
                Log.warn(e.getDetail());
            }
        }

        /// <summary>
        /// Open a new browser with the current selected folder as the working directory
        /// </summary>
        private void View_NewBrowser(object sender, NewBrowserEventArgs newBrowserEventArgs)
        {
            if (newBrowserEventArgs.SelectedAsWorkingDir)
            {
                Path selected = SelectedPath;
                if (null == selected || !selected.isDirectory())
                {
                    selected = Workdir;
                }
                BrowserController c = MainController.NewBrowser(true);
                Host duplicate = new HostDictionary().deserialize(Session.getHost().serialize(SerializerFactory.get()));
                // Make sure a new UUID is assigned for duplicate
                duplicate.setUuid(null);
                duplicate.setDefaultPath(selected.getAbsolute());
                c.Mount(duplicate);
            }
            else
            {
                BrowserController c = MainController.NewBrowser(true);
                MainController.OpenDefaultBookmark(c);
            }
        }

        protected void transfer(Transfer transfer)
        {
            this.transfer(transfer, Utils.ConvertFromJavaList(transfer.getRoots(), delegate (object o)
            {
                TransferItem item = (TransferItem)o;
                return item.remote;
            }));
        }

        /// <summary>
        /// Transfers the files either using the queue or using
        /// the browser session if #connection.pool.max is 1
        /// </summary>
        /// <param name="transfer"></param>
        protected void transfer(Transfer transfer, IList<Path> selected)
        {
            this.transfer(transfer, selected, Host.TransferType.getType(transfer.getSource()).equals(Host.TransferType.browser));
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="transfer"></param>
        /// <param name="destination"></param>
        /// <param name="useBrowserConnection"></param>
        public void transfer(Transfer transfer, IList<Path> selected, bool browser)
        {
            TransferCallback callback = new ReloadTransferCallback(this, selected);
            if (browser)
            {
                Background(new BrowserTransferBackgroundAction(this, Session, transfer, callback));
            }
            else
            {
                TransferController.Instance.StartTransfer(transfer, new TransferOptions(), callback);
            }
        }

        public bool IsMounted()
        {
            if (Session == SessionPool.DISCONNECTED)
            {
                return false;
            }
            return Workdir != null;
        }

        public override void start(BackgroundAction action)
        {
            Invoke(delegate { View.StartActivityAnimation(); });
        }

        public override void stop(BackgroundAction action)
        {
            Invoke(delegate { View.StopActivityAnimation(); });
        }

        /// <summary>
        /// Make the browser reload its content. Will make use of the cache.
        /// </summary>
        protected void Reload()
        {
            if (IsMounted())
            {
                Reload(Workdir, new HashSet<Path> { Workdir }, SelectedPaths, false);
            }
            else
            {
                View.SetBrowserModel(null);
                SetStatus();
            }
        }

        /// <summary>
        /// Make the browser reload its content. Invalidates the cache.
        /// </summary>
        /// <param name="workdir">Use working directory as the current root of the browser</param>
        /// <param name="changed">The items that changed</param>
        /// <param name="selected">The items to be selected</param>
        public void Reload(Path workdir, IList<Path> changed, IList<Path> selected)
        {
            PathReloadFinder finder = new PathReloadFinder();

            Set set = finder.find(Utils.ConvertToJavaList(changed));
            HashSet<Path> folders = new HashSet<Path>();
            Iterator it = set.iterator();
            while (it.hasNext())
            {
                folders.Add((Path)it.next());
            }
            Reload(workdir, folders, selected, true);
        }

        /// <summary>
        /// Make the browser reload its content. Invalidates the cache.
        /// </summary>
        /// <param name="workdir">Use working directory as the current root of the browser</param>
        /// <param name="selected">Folders to render</param>
        /// <param name="folder">The items to be selected</param>
        protected void Reload(Path workdir, ISet<Path> folders, IList<Path> selected)
        {
            Reload(workdir, folders, selected, true);
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="workdir">Use working directory as the current root of the browser</param>
        /// <param name="selected">Selected files in browser</param>
        /// <param name="folder">Folder to render</param>
        protected void Reload(Path workdir, IList<Path> selected, Path folder)
        {
            Workdir = workdir;
            SetNavigation(workdir != null);
            if (Workdir != null)
            {
                if (Workdir.Equals(folder))
                {
                    IEnumerable<Path> children = _browserModel.ChildrenGetter(folder);
                    //clear selection before resetting model. Otherwise we have weird selection effects.
                    SelectedPaths = new List<Path>();
                    int savedIndex = View.TopItemIndex;
                    View.BeginBrowserUpdate();
                    View.SetBrowserModel(null); // #7670
                    View.SetBrowserModel(children);
                    View.TopItemIndex = savedIndex;
                    SelectedPaths = selected;
                    List<Path> toUpdate = new List<Path>();
                    foreach (Path path in View.VisiblePaths)
                    {
                        if (path.isDirectory())
                        {
                            toUpdate.Add(path);
                        }
                    }
                    View.RefreshBrowserObjects(toUpdate);
                    View.EndBrowserUpdate();
                }
                else
                {
                    View.RefreshBrowserObject(folder);
                }
            }
            else
            {
                View.SetBrowserModel(null);
            }
            SelectedPaths = selected;
        }

        /// <summary>
        /// Make the browser reload its content. Invalidates the cache.
        /// </summary>
        /// <param name="workdir">Use working directory as the current root of the browser</param>
        /// <param name="folders">Folders to render</param>
        /// <param name="selected">The items to be selected</param>
        /// <param name="invalidate">Invalidate the cache before rendering</param>
        public void Reload(Path workdir, ISet<Path> folders, IList<Path> selected, bool invalidate)
        {
            foreach (Path folder in folders)
            {
                if (invalidate)
                {
                    // Invalidate cache
                    _cache.invalidate(folder);
                }
                else
                {
                    if (_cache.isValid(folder))
                    {
                        Reload(workdir, selected, folder);
                        return;
                    }
                }
                background(new ListAction(this, workdir, folder, selected, _cache, _limitListener));
            }
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
            // Remove any custom file filter
            SetFilter(SearchFilterFactory.create(ShowHiddenFiles));
            if (null == directory)
            {
                Reload(null, new HashSet<Path>(), selected, false);
            }
            else
            {
                Reload(directory, new HashSet<Path> { directory }, selected, false);
            }
        }

        private void SetNavigation(bool enabled)
        {
            View.SearchEnabled = enabled;
            if (!enabled)
            {
                View.SearchString = String.Empty;
            }
            List<string> paths = new List<string>();
            if (enabled)
            {
                // Update the current working directory
                _navigation.add(Workdir);
                Path p = Workdir;
                do
                {
                    paths.Add(p.getAbsolute());
                    p = p.getParent();
                } while (!p.isRoot());
                paths.Add(p.getAbsolute());
                View.PopulatePaths(paths);
            }
            View.ComboboxPathEnabled = enabled;
            View.HistoryBackEnabled = enabled && _navigation.getBack().size() > 1;
            View.HistoryForwardEnabled = enabled && _navigation.getForward().size() > 0;
            View.ParentPathEnabled = enabled && !Workdir.isRoot();
        }

        public void RefreshObject(Path path, bool preserveSelection)
        {
            if (preserveSelection)
            {
                RefreshObject(path, View.SelectedPaths);
            }
            else
            {
                RefreshObject(path, new List<Path>());
            }
        }

        public void RefreshObject(Path path, IList<Path> selected)
        {
            if (Workdir.Equals(path))
            {
                View.SetBrowserModel(_browserModel.ChildrenGetter(path));
            }
            else
            {
                if (!path.isDirectory())
                {
                    View.RefreshBrowserObject(path.getParent());
                }
                else
                {
                    View.RefreshBrowserObject(path);
                }
            }
            SelectedPaths = selected;
            SetStatus();
        }

        public void Mount(Host host)
        {
            if (Log.isDebugEnabled())
            {
                Log.debug(string.Format("Mount session for {0}", host));
            }
            View.ClearTranscript();
            CallbackDelegate callbackDelegate =
                delegate
                {
                    Session = SessionPoolFactory.create(this, host, SessionPoolFactory.Usage.browser);
                    background(new MountAction(this, Session, host, _limitListener));
                };
            Unmount(callbackDelegate);
        }

        /// <summary>
        ///
        /// </summary>
        /// <returns>true if mounted and the connection to the server is alive</returns>
        public bool IsConnected()
        {
            return Session.getState() == ch.cyberduck.core.Session.State.open && !_cache.isEmpty();
        }

        public bool IsIdle()
        {
            return registry.isEmpty();
        }

        public static bool ApplicationShouldTerminate()
        {
            // Determine if there are any open connections
            foreach (BrowserController controller in new List<BrowserController>(MainController.Browsers))
            {
                BrowserController c = controller;
                if (!controller.Unmount(delegate (DialogResult result)
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
            if (IsConnected())
            {
                if (PreferencesFactory.get().getBoolean("browser.disconnect.confirm"))
                {
                    TaskDialogResult result = CommandBox(LocaleFactory.localizedString("Disconnect"),
                        String.Format(LocaleFactory.localizedString("Disconnect from {0}"),
                            Session.getHost().getHostname()),
                        LocaleFactory.localizedString("The connection will be closed."),
                        String.Format("{0}", LocaleFactory.localizedString("Disconnect")), true,
                        LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Question,
                        delegate (int option, bool verificationChecked)
                        {
                            if (verificationChecked)
                            {
                                // Never show again.
                                PreferencesFactory.get().setProperty("browser.disconnect.confirm", false);
                            }
                            switch (option)
                            {
                                case 0: // Disconnect
                                    unmountImpl(DialogResult.OK);
                                    break;
                            }
                        });
                    return result.Button != IDCANCEL;
                }
            }
            UnmountImpl(disconnected);
            // Unmount succeeded
            return true;
        }

        private void UnmountImpl(CallbackDelegate disconnected)
        {
            CallbackDelegate run = delegate
            {
                _scheduler?.shutdown();
                Session.shutdown();
                Session = SessionPool.DISCONNECTED;
                SetWorkdir(null);
                _cache.clear();
                foreach (KeyValuePair<Path, Editor> entry in _editors)
                {
                    Editor editor = entry.Value;
                    editor.close();
                }
                _editors.Clear();
                _navigation.clear();
                View.WindowTitle = PreferencesFactory.get().getProperty("application.name");
                disconnected();
            };

            Disconnect(run);
        }

        public void SetStatus()
        {
            BackgroundAction current = registry.getCurrent();
            message(null != current ? current.getActivity() : null);
        }

        public void SetStatus(string label)
        {
            View.StatusLabel = label;
        }

        private void PopulateQuickConnect()
        {
            List<string> nicknames = new List<string>();
            foreach (Host host in _bookmarkCollection)
            {
                nicknames.Add(BookmarkNameProvider.toString(host));
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
            if (CheckMove(selected))
            {
                MoveAction move = new MoveAction(this, Utils.ConvertToJavaMap(selected), _cache);
                Background(move);
            }
        }

        /// <summary>
        ///
        /// </summary>
        /// <param name="source">The original file to duplicate</param>
        /// <param name="destination">The destination of the duplicated file</param>
        protected internal void CopyPath(Path source, Path destination)
        {
            CopyPaths(new Dictionary<Path, Path> { { source, destination } });
        }

        protected internal void CopyPaths(IDictionary<Path, Path> selected)
        {
            if (CheckCopy(selected))
            {
                CopyAction copy = new CopyAction(this, Utils.ConvertToJavaMap(selected), _cache);
                Background(copy);
            }
        }

        /// <summary>
        /// Displays a warning dialog about files to be moved
        /// </summary>
        /// <param name="selected">The files to check for existence</param>
        /// <param name="action"></param>
        private bool CheckMove(IDictionary<Path, Path> selected)
        {
            if (PreferencesFactory.get().getBoolean("browser.move.confirm"))
            {
                StringBuilder alertText =
                    new StringBuilder(LocaleFactory.localizedString("Do you want to move the selected files?",
                        "Duplicate"));

                StringBuilder content = new StringBuilder();
                int i = 0;
                bool rename = false;
                IEnumerator<KeyValuePair<Path, Path>> enumerator = null;
                for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext();)
                {
                    KeyValuePair<Path, Path> next = enumerator.Current;
                    if (next.Key.getParent().equals(next.Value.getParent()))
                    {
                        rename = true;
                    }
                    // u2022 = Bullet
                    content.Append("\n" + Character.toString('\u2022') + " " + next.Key.getName());
                    i++;
                }
                if (enumerator.MoveNext())
                {
                    content.Append("\n" + Character.toString('\u2022') + " ...)");
                }
                bool result = false;
                CommandBox(
                    rename
                        ? LocaleFactory.localizedString("Rename", "Transfer")
                        : LocaleFactory.localizedString("Move", "Transfer"), alertText.ToString(), content.ToString(),
                    String.Format("{0}",
                        rename
                            ? LocaleFactory.localizedString("Rename", "Transfer")
                            : LocaleFactory.localizedString("Move", "Transfer")), true,
                    LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Question,
                    delegate (int option, bool verificationChecked)
                    {
                        if (verificationChecked)
                        {
                            // Never show again.
                            PreferencesFactory.get().setProperty("browser.move.confirm", false);
                        }
                        if (option == 0)
                        {
                            result = CheckOverwrite(selected.Values);
                        }
                    });
                return result;
            }
            return CheckOverwrite(selected.Values);
        }

        /// <summary>
        /// Displays a warning dialog about files to be copied
        /// </summary>
        /// <param name="selected">The files to check for existence</param>
        /// <param name="action"></param>
        private bool CheckCopy(IDictionary<Path, Path> selected)
        {
            if (PreferencesFactory.get().getBoolean("browser.copy.confirm"))
            {
                StringBuilder alertText =
                    new StringBuilder(LocaleFactory.localizedString("Do you want to copy the selected files?",
                        "Duplicate"));

                StringBuilder content = new StringBuilder();
                int i = 0;
                IEnumerator<KeyValuePair<Path, Path>> enumerator;
                for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext();)
                {
                    KeyValuePair<Path, Path> next = enumerator.Current;
                    // u2022 = Bullet
                    content.Append("\n" + Character.toString('\u2022') + " " + next.Key.getName());
                    i++;
                }
                if (enumerator.MoveNext())
                {
                    content.Append("\n" + Character.toString('\u2022') + " …)");
                }
                bool result = false;
                CommandBox(
                    LocaleFactory.localizedString("Copy", "Transfer"), alertText.ToString(), content.ToString(),
                    String.Format("{0}",
                        LocaleFactory.localizedString("Copy", "Transfer")), true,
                    LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Question,
                    delegate (int option, bool verificationChecked)
                    {
                        if (verificationChecked)
                        {
                            // Never show again.
                            PreferencesFactory.get().setProperty("browser.copy.confirm", false);
                        }
                        if (option == 0)
                        {
                            result = CheckOverwrite(selected.Values);
                        }
                    });
                return result;
            }
            return CheckOverwrite(selected.Values);
        }

        /// <summary>
        /// Recursively deletes the files
        /// </summary>
        /// <param name="selected">The files selected in the browser to delete</param>
        public void DeletePaths(ICollection<Path> selected)
        {
            ICollection<Path> normalized =
                Utils.ConvertFromJavaList<Path>(PathNormalizer.normalize(Utils.ConvertToJavaList(selected)));
            if (normalized.Count == 0)
            {
                return;
            }

            if (DeletePathsPrompt(normalized))
            {
                DeletePathsImpl(normalized);
            }
        }

        public bool DeletePathsPrompt(ICollection<Path> selected)
        {
            string alertText = String.Format(LocaleFactory.localizedString("Delete {0} files"), selected.Count);

            StringBuilder content = new StringBuilder();
            int i = 0;
            IEnumerator<Path> enumerator;
            for (enumerator = selected.GetEnumerator(); i < 10 && enumerator.MoveNext();)
            {
                Path item = enumerator.Current;
                if (i > 0) content.AppendLine();
                // u2022 = Bullet
                content.Append(Character.toString('\u2022') + " " + item.getName());
                i++;
            }
            if (enumerator.MoveNext())
            {
                content.Append("\n" + Character.toString('\u2022') + " ...)");
            }
            TaskDialogResult r = QuestionBox(LocaleFactory.localizedString("Delete"), alertText,
                content.ToString(), String.Format("{0}", LocaleFactory.localizedString("Delete")), true);
            return r.Button == 0;
        }

        private void DeletePathsImpl(ICollection<Path> files)
        {
            background(new DeleteAction(this, LoginCallbackFactory.get(this), Utils.ConvertToJavaList(files)));
        }

        public void SetFilter(Filter filter)
        {
            if (null == filter)
            {
                View.SearchString = String.Empty;
                FilenameFilter = SearchFilterFactory.create(ShowHiddenFiles);
            }
            else
            {
                FilenameFilter = filter;
            }
        }

        /// <summary>
        /// Displays a warning dialog about already existing files
        /// </summary>
        /// <param name="selected">The files to check for existance</param>
        private bool CheckOverwrite(ICollection<Path> selected)
        {
            StringBuilder alertText =
                new StringBuilder(
                    LocaleFactory.localizedString(
                        "A file with the same name already exists. Do you want to replace the existing file?"));

            StringBuilder content = new StringBuilder();
            int i = 0;
            IEnumerator<Path> enumerator = null;
            bool shouldWarn = false;
            for (enumerator = selected.GetEnumerator(); enumerator.MoveNext();)
            {
                Path item = enumerator.Current;
                if (_cache.get(item.getParent()).contains(item))
                {
                    if (i < 10)
                    {
                        // u2022 = Bullet
                        content.Append("\n" + Character.toString('\u2022') + " " + item.getName());
                    }
                    shouldWarn = true;
                }
                i++;
            }
            if (i >= 10)
            {
                content.Append("\n" + Character.toString('\u2022') + " ...)");
            }
            if (shouldWarn)
            {
                TaskDialogResult r = QuestionBox(LocaleFactory.localizedString("Overwrite"), alertText.ToString(),
                    content.ToString(), String.Format("{0}", LocaleFactory.localizedString("Overwrite")), true);
                return r.Button == 0;
            }
            else
            {
                return true;
            }
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
                    View.CurrentView = BrowserView.File;
                    SetFilter(SearchFilterFactory.create(ShowHiddenFiles));
                    Reload();
                    break;
                case BrowserView.Bookmark:
                    View.CurrentView = BrowserView.Bookmark;
                    _bookmarkModel.Source = BookmarkCollection.defaultCollection();
                    ReloadBookmarks();
                    SelectHost();
                    break;
                case BrowserView.History:
                    View.CurrentView = BrowserView.History;
                    _bookmarkModel.Source = HistoryCollection.defaultCollection();
                    ReloadBookmarks();
                    SelectHost();
                    break;
                case BrowserView.Bonjour:
                    View.CurrentView = BrowserView.Bonjour;
                    _bookmarkModel.Source = RendezvousCollection.defaultCollection();
                    ReloadBookmarks();
                    SelectHost();
                    break;
            }
        }

        private void SelectHost()
        {
            if (IsMounted())
            {
                View.SelectBookmark(Session.getHost());
            }
        }

        /// <summary>
        /// Reload bookmarks table from the currently selected model
        /// </summary>
        public void ReloadBookmarks()
        {
            ReloadBookmarks(null);
        }

        /// <summary>
        /// Reload bookmarks table from the currently selected model
        /// </summary>
        public void ReloadBookmarks(Host selected)
        {
            //Note: expensive for a big bookmark list (might need a refactoring)
            View.SetBookmarkModel(_bookmarkModel.Source, selected);
            SetStatus();
        }

        private class ListAction : WorkerBackgroundAction
        {
            public ListAction(BrowserController controller, Path workdir, Path directory, IList<Path> selected,
                PathCache cache, ListProgressListener listener)
                : base(
                    controller, controller.Session,
                    new InnerListWorker(controller, workdir, directory, selected, cache, listener))
            {
            }

            private class InnerListWorker : SessionListWorker
            {
                private readonly BrowserController _controller;
                private readonly Path _folder;
                private readonly IList<Path> _selected;
                private readonly Path _workdir;

                public InnerListWorker(BrowserController controller, Path workdir, Path folder, IList<Path> selected,
                    PathCache cache, ListProgressListener listener) : base(cache, folder, listener)
                {
                    _controller = controller;
                    _workdir = workdir;
                    _folder = folder;
                    _selected = selected;
                }

                public override void cleanup(object list)
                {
                    // Put into cache
                    base.cleanup(list);
                    // Update the working directory if listing is successful
                    if (!(AttributedList.emptyList() == list))
                    {
                        // Reload browser
                        _controller.Reload(_workdir, _selected, _folder);
                    }
                }
            }
        }

        private class SearchAction : WorkerBackgroundAction
        {
            public SearchAction(BrowserController controller)
                : base(controller, controller.Session, new InnerSearchWorker(controller))
            {
            }

            private class InnerSearchWorker : SearchWorker
            {
                private readonly BrowserController _controller;

                public InnerSearchWorker(BrowserController controller)
                    : base(controller.Workdir, controller.FilenameFilter, controller.Cache, controller._limitListener)
                {
                    _controller = controller;
                }

                public override void cleanup(object result)
                {
                    base.cleanup(result);
                    _controller.SetFilter(new RecursiveSearchFilter((AttributedList)result));
                    _controller.Reload();
                }
            }
        }

        private class CreateArchiveAction : BrowserControllerBackgroundAction
        {
            private readonly Archive _archive;
            private readonly IList<Path> _selected;
            private readonly List _selectedJava;

            public CreateArchiveAction(BrowserController controller, Archive archive, IList<Path> selected)
                : base(controller)
            {
                _archive = archive;
                _selectedJava = Utils.ConvertToJavaList(selected);
                _selected = selected;
            }

            public override object run(Session session)
            {
                ((Compress)session.getFeature(typeof(Compress))).archive(_archive,
                    BrowserController.Workdir, _selectedJava, BrowserController, BrowserController);
                return true;
            }

            public override string getActivity()
            {
                return _archive.getCompressCommand(BrowserController.Workdir, _selectedJava);
            }

            public override void cleanup()
            {
                base.cleanup();
                BrowserController.Reload(BrowserController.Workdir, _selected,
                    new List<Path> { _archive.getArchive(_selectedJava) });
            }
        }

        private class CustomPathFilter : SearchFilter, IModelFilter
        {
            public CustomPathFilter(String searchString) : base(searchString)
            {
            }

            public bool Filter(object modelObject)
            {
                return accept(modelObject);
            }
        }

        private class DeleteAction : WorkerBackgroundAction
        {
            public DeleteAction(BrowserController controller, LoginCallback prompt, List files)
                : base(controller, controller.Session, new InnerDeleteWorker(controller, prompt, files))
            {
            }

            private class InnerDeleteWorker : DeleteWorker
            {
                private readonly BrowserController _controller;
                private readonly List _files;

                public InnerDeleteWorker(BrowserController controller, LoginCallback prompt, List files)
                    : base(prompt, files, controller)
                {
                    _controller = controller;
                    _files = files;
                }

                public override void cleanup(object deleted)
                {
                    base.cleanup(deleted);
                    _controller.Reload(_controller.Workdir, (IList<Path>)Utils.ConvertFromJavaList<Path>(_files),
                        new List<Path>());
                }
            }
        }

        private class DisconnectAction : DisconnectBackgroundAction
        {
            private readonly CallbackDelegate _callback;
            private readonly BrowserController _controller;

            public DisconnectAction(BrowserController controller, CallbackDelegate callback)
                : base(controller, controller.Session)
            {
                _controller = controller;
                _callback = callback;
            }

            public override void cleanup()
            {
                base.cleanup();
                _controller._pasteboard.clear();
                _callback();
            }
        }

        private class MountAction : WorkerBackgroundAction
        {
            private readonly BrowserController _controller;
            private readonly Host _host;

            public MountAction(BrowserController controller, SessionPool pool, Host host, ListProgressListener listener)
                : base(controller, pool, new InnerMountWorker(controller, pool, listener))
            {
                _controller = controller;
                _host = host;
            }

            public override void init()
            {
                base.init();
                _controller.View.WindowTitle = BookmarkNameProvider.toString(_host, true);
                _controller.View.RefreshBookmark(_controller.Session.getHost());
            }

            private class InnerMountWorker : MountWorker
            {
                private readonly BrowserController _controller;
                private readonly SessionPool _pool;

                public InnerMountWorker(BrowserController controller, SessionPool pool, ListProgressListener listener)
                    : base(pool.getHost(), controller._cache, listener)
                {
                    _controller = controller;
                    _pool = pool;
                }

                public override void cleanup(object wd)
                {
                    base.cleanup(wd);
                    Path workdir = (Path)wd;
                    if (null == workdir)
                    {
                        _controller.Unmount();
                    }
                    else
                    {
                        _controller._pasteboard = PathPasteboardFactory.getPasteboard(_pool.getHost());
                        // Set the working directory
                        _controller.SetWorkdir(workdir);
                        _controller.View.RefreshBookmark(_pool.getHost());
                        _controller.ToggleView(BrowserView.File);
                        _controller.View.SelectedEncoding = _pool.getHost().getEncoding();
                        _controller.View.SecureConnection = _pool.getHost().getProtocol().isSecure();
                        _controller.View.CertBasedConnection = _pool.getFeature(typeof(X509TrustManager)) != null;
                        _controller.View.SecureConnectionVisible = true;
                        _controller._scheduler = (Scheduler)_pool.getFeature(typeof(Scheduler));
                        _controller._scheduler?.repeat(_pool, PasswordCallbackFactory.get(_controller));
                    }
                }
            }
        }

        private class MoveAction : WorkerBackgroundAction
        {
            public MoveAction(BrowserController controller, Map selected, PathCache cache)
                : base(controller, controller.Session, new InnerMoveWorker(controller, selected, cache))
            {
            }

            private class InnerMoveWorker : MoveWorker
            {
                private readonly BrowserController _controller;
                private readonly Map _files;

                public InnerMoveWorker(BrowserController controller, Map files, PathCache cache)
                    : base(files,
                        controller.Session.getHost().getProtocol().getStatefulness() == Protocol.Statefulness.stateful
                            ? SessionPoolFactory.create(controller, controller.Session.getHost())
                            : controller.Session, cache, controller, LoginCallbackFactory.get(controller))
                {
                    _controller = controller;
                    _files = files;
                }

                public override void cleanup(object result)
                {
                    List changed = new ArrayList();
                    changed.addAll(_files.keySet());
                    changed.addAll(_files.values());
                    _controller.Reload(_controller.Workdir, (IList<Path>)Utils.ConvertFromJavaList<Path>(changed),
                        (IList<Path>)Utils.ConvertFromJavaList<Path>(_files.values()));
                }
            }
        }

        private class CopyAction : WorkerBackgroundAction
        {
            public CopyAction(BrowserController controller, Map selected, PathCache cache)
                : base(controller, controller.Session, new InnerCopyWorker(controller, selected, cache))
            {
            }

            private class InnerCopyWorker : CopyWorker
            {
                private readonly BrowserController _controller;
                private readonly Map _files;

                public InnerCopyWorker(BrowserController controller, Map files, PathCache cache)
                    : base(files,
                        controller.Session.getHost().getProtocol().getStatefulness() == Protocol.Statefulness.stateful
                            ? SessionPoolFactory.create(controller, controller.Session.getHost())
                            : controller.Session, cache, controller, LoginCallbackFactory.get(controller))
                {
                    _controller = controller;
                    _files = files;
                }

                public override void cleanup(object result)
                {
                    List changed = new ArrayList();
                    changed.addAll(((Map)result).keySet());
                    changed.addAll(((Map)result).values());
                    _controller.Reload(_controller.Workdir, (IList<Path>)Utils.ConvertFromJavaList<Path>(changed),
                        (IList<Path>)Utils.ConvertFromJavaList<Path>(_files.values()));
                }
            }
        }

        private class ReloadTransferCallback : TransferCallback
        {
            private readonly IList<Path> _changed;
            private readonly BrowserController _controller;

            public ReloadTransferCallback(BrowserController controller, IList<Path> changed)
            {
                _controller = controller;
                _changed = changed;
            }

            public void complete(Transfer t)
            {
                _controller.invoke(new ReloadAction(_controller, _changed));
            }

            private class ReloadAction : WindowMainAction
            {
                private readonly IList<Path> _changed;

                public ReloadAction(BrowserController c, IList<Path> changed) : base(c)
                {
                    _changed = changed;
                }

                public override bool isValid()
                {
                    return base.isValid() && ((BrowserController)Controller).IsConnected();
                }

                public override void run()
                {
                    BrowserController c = (BrowserController)Controller;
                    c.Reload(c.Workdir, _changed, _changed);
                }
            }
        }

        private class RevertAction : WorkerBackgroundAction
        {
            public RevertAction(BrowserController controller, IList<Path> files)
                : base(controller, controller.Session, new InnerRevertWorker(controller, files))
            {
            }

            private class InnerRevertWorker : RevertWorker
            {
                private readonly BrowserController _controller;

                public InnerRevertWorker(BrowserController controller, IList<Path> files)
                    : base(Utils.ConvertToJavaList(files))
                {
                    _controller = controller;
                }

                public override void cleanup(object result)
                {
                    IList<Path> files = (IList<Path>)Utils.ConvertFromJavaList<Path>((List)result);
                    _controller.Reload(_controller.Workdir, files, files);
                }
            }
        }

        private class RestoreAction : WorkerBackgroundAction
        {
            public RestoreAction(BrowserController controller, IList<Path> files)
                : base(controller, controller.Session, new InnerRestoreWorker(controller, files))
            {
            }

            private class InnerRestoreWorker : RestoreWorker
            {
                private readonly BrowserController _controller;

                public InnerRestoreWorker(BrowserController controller, IList<Path> files)
                    : base(LoginCallbackFactory.get(controller), Utils.ConvertToJavaList(files))
                {
                    _controller = controller;
                }

                public override void cleanup(object result)
                {
                    IList<Path> files = (IList<Path>)Utils.ConvertFromJavaList<Path>((List)result);
                    _controller.Reload(_controller.Workdir, files, files);
                }
            }
        }

        private class ShareLinkAction : WorkerBackgroundAction
        {
            public ShareLinkAction(BrowserController controller, Path file)
                : base(controller, controller.Session, new InnerDownloadShareWorker(controller, file))
            {
            }

            private class InnerDownloadShareWorker : DownloadShareWorker
            {
                private readonly BrowserController _controller;
                private readonly Path _file;

                public InnerDownloadShareWorker(BrowserController controller, Path file)
                    : base(file, null, PasswordCallbackFactory.get(controller), new DialogPromptShareeCallback(controller.Session.getHost(), controller.View.Handle, controller))
                {
                    _controller = controller;
                    _file = file;
                }

                public override void cleanup(object result)
                {
                    DescriptiveUrl url = (DescriptiveUrl)result;
                    // Display
                    if (null != url)
                    {
                        string title = LocaleFactory.localizedString("Share…", "Main");
                        string commandButtons = String.Format("{0}|{1}", LocaleFactory.localizedString("Continue", "Credentials"),
                            DescriptiveUrl.EMPTY != url ? LocaleFactory.localizedString("Copy", "Main") : null);
                        _controller.CommandBox(title, title, MessageFormat.format(LocaleFactory.localizedString("You have successfully created a share link for {0}.", "SDS") + "\n\n{1}", _file.getName(), url.getUrl()),
                            commandButtons,
                            false, null, TaskDialogIcon.Information,
                            delegate (int option, System.Boolean verificationChecked)
                            {
                                switch (option)
                                {
                                    case 1:
                                        Clipboard.SetText(url.getUrl());
                                        break;
                                }
                            });
                    }
                }
            }
        }

        private class RequestFilesAction : WorkerBackgroundAction
        {
            public RequestFilesAction(BrowserController controller, Path file)
                : base(controller, controller.Session, new InnerUploadShareWorker(controller, file))
            {
            }

            private class InnerUploadShareWorker : UploadShareWorker
            {
                private readonly BrowserController _controller;
                private readonly Path _file;

                public InnerUploadShareWorker(BrowserController controller, Path file)
                    : base(file, null, PasswordCallbackFactory.get(controller), new DialogPromptShareeCallback(controller.Session.getHost(), controller.View.Handle, controller))
                {
                    _controller = controller;
                    _file = file;
                }

                public override void cleanup(object result)
                {
                    DescriptiveUrl url = (DescriptiveUrl)result;
                    // Display
                    if (null != url)
                    {
                        string title = LocaleFactory.localizedString("Share…", "Main");
                        string commandButtons = String.Format("{0}|{1}", LocaleFactory.localizedString("Continue", "Credentials"),
                            DescriptiveUrl.EMPTY != url ? LocaleFactory.localizedString("Copy", "Main") : null);
                        _controller.CommandBox(title, title, MessageFormat.format(LocaleFactory.localizedString("You have successfully created a share link for {0}.", "SDS") + "\n\n{1}", _file.getName(), url.getUrl()),
                            commandButtons,
                            false, null, TaskDialogIcon.Information,
                            delegate (int option, System.Boolean verificationChecked)
                            {
                                switch (option)
                                {
                                    case 1:
                                        Clipboard.SetText(url.getUrl());
                                        break;
                                }
                            });
                    }
                }
            }
        }

        private class LockVaultAction : WorkerBackgroundAction
        {
            public LockVaultAction(BrowserController controller, VaultRegistry registry, Path directory)
                : base(controller, controller.Session, new InnerLockVaultWorker(controller, registry, directory))
            {
            }

            private class InnerLockVaultWorker : LockVaultWorker
            {
                private readonly BrowserController _controller;
                private readonly Path _directory;

                public InnerLockVaultWorker(BrowserController controller, VaultRegistry registry, Path directory)
                    : base(registry, directory)
                {
                    _controller = controller;
                    _directory = directory;
                }

                public override void cleanup(object vault)
                {
                    if (vault != null)
                    {
                        _controller.Reload((Path)vault, new HashSet<Path>() { (Path)vault }, new List<Path>(), true);
                    }
                }
            }
        }
        private class LoadVaultAction : WorkerBackgroundAction
        {
            public LoadVaultAction(BrowserController controller, VaultRegistry registry, Path directory)
                : base(controller, controller.Session, new InnerLoadVaultWorker(controller, registry, directory))
            {
            }

            private class InnerLoadVaultWorker : LoadVaultWorker
            {
                private readonly BrowserController _controller;
                private readonly Path _directory;

                public InnerLoadVaultWorker(BrowserController controller, VaultRegistry registry, Path directory)
                    : base(new LoadingVaultLookupListener(registry,
                        PasswordCallbackFactory.get(controller)), directory)
                {
                    _controller = controller;
                    _directory = directory;
                }

                public override void cleanup(object vault)
                {
                    if (vault != null)
                    {
                        _controller.Reload(((Vault)vault).getHome(), new HashSet<Path>() { ((Vault)vault).getHome() }, new List<Path>(), true);
                    }
                }
            }
        }

        private class UnarchiveAction : BrowserControllerBackgroundAction
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

            public override object run(Session session)
            {
                ((Compress)session.getFeature(typeof(Compress))).unarchive(_archive, _selected,
                    BrowserController, BrowserController);
                return true;
            }

            public override string getActivity()
            {
                return _archive.getDecompressCommand(_selected);
            }

            public override void cleanup()
            {
                base.cleanup();
                _expanded.AddRange(Utils.ConvertFromJavaList<Path>(_archive.getExpanded(new ArrayList { _selected })));
                BrowserController.Reload(BrowserController.Workdir, new List<Path> { _selected }, _expanded);
            }
        }
    }
}
