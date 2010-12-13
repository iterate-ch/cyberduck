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
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Reflection;
using System.Text;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.aquaticprime;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Commondialog;
using Ch.Cyberduck.Ui.Winforms.Controls;
using org.apache.log4j;
using DataObject = System.Windows.Forms.DataObject;
using ToolStripRenderer = Ch.Cyberduck.Ui.Controller.ToolStripRenderer;

namespace Ch.Cyberduck.Ui.Winforms
{
    /// <summary>
    /// Main browser form.
    /// </summary>
    /// <remarks>Menu handling: Due to the non-native look of the ToolStripMenu controls we use the old MainMenu component which renders natively.
    /// The ToolStripMenu controls are still there and maintained since we need them for the shortcut handling (some of the need shortcuts are not
    /// supported by MainMenu, e.g. Alt+Enter). I hope to see native rendering of the menu controls in a future version of the .NET so that we can
    /// get rid of the unnecessary MainMenu components.
    /// </remarks>
    public partial class BrowserForm : BaseForm, IBrowserView
    {
        private static readonly Font FixedFont = new Font(FontFamily.GenericMonospace, 8);
        private static readonly Logger Log = Logger.getLogger(typeof (BrowserForm).FullName);
        private static readonly TypeConverter shortcutConverter = TypeDescriptor.GetConverter(typeof (Keys));
        private bool _browserStateRestored;
        private BrowserView _currentView;
        private bool _lastActivityRunning;
        private ToolStripMenuItem _lastMenuItemClicked;

        public BrowserForm()
        {
            InitializeComponent();

            ToolStripManager.RenderMode = ToolStripManagerRenderMode.System;

            BookmarkMenuCollectionListener bookmarkMenuCollectionListener = new BookmarkMenuCollectionListener(this);
            BookmarkCollection.defaultCollection().addListener(bookmarkMenuCollectionListener);
            MenuCollectionListener historyMenuCollectionListener = new MenuCollectionListener(this, historyMainMenuItem,
                                                                                              HistoryCollection.
                                                                                                  defaultCollection(),
                                                                                              Locale.localizedString(
                                                                                                  "No recently connected servers available"));
            HistoryCollection.defaultCollection().addListener(historyMenuCollectionListener);
            MenuCollectionListener bonjourMenuCollectionListener = new MenuCollectionListener(this, bonjourMainMenuItem,
                                                                                              RendezvousCollection.
                                                                                                  defaultCollection(),
                                                                                              Locale.localizedString(
                                                                                                  "No Bonjour services available"));
            RendezvousCollection.defaultCollection().addListener(bonjourMenuCollectionListener);

            if (!DesignMode)
            {
                vistaMenu1.SetImage(newBookmarkMainMenuItem, IconCache.Instance.IconForName("bookmark", 16));
                vistaMenu1.SetImage(historyMainMenuItem, IconCache.Instance.IconForName("history", 16));
                vistaMenu1.SetImage(bonjourMainMenuItem, IconCache.Instance.IconForName("rendezvous", 16));
                vistaMenu1.SetImage(transfersMainMenuItem, IconCache.Instance.IconForName("queue", 16));

                newFolderToolStripButton.Image = IconCache.Instance.IconForName("newfolder", 32);
            }

            toolBar.ContextMenu = toolbarContextMenu1;
            bookmarkListView.ContextMenu = bookmarkContextMenu;

            viewToolStrip.Renderer = new ToolStripRenderer();
            toolBar.Renderer = new ToolStripRenderer();
            actionToolStrip.Renderer = new NoGapRenderer();

            // configure browser properties
            browser.UseExplorerTheme = true;
            browser.UseTranslucentSelection = true;
            browser.OwnerDraw = true;
            browser.UseOverlays = false;
            browser.LabelEdit = true;
            browser.AllowDrop = true;

            browser.ContextMenuStrip = null;
            browser.ContextMenu = browserContextMenu;
            browser.DropSink = new ExpandingBrowserDropSink(this);
            browser.DragSource = new BrowserDragSource(this);

            browser.AllowColumnReorder = true;
            browser.ShowImagesOnSubItems = true;
            browser.TreeColumnRenderer = new BrowserRenderer();
            browser.SelectedRowDecoration = new ExplorerRowBorderDecoration();
            browser.ItemsChanged += (sender, args) => ItemsChanged();

            searchTextBox.PlaceHolderText = Locale.localizedString("Search…", "Main");

            securityToolStripStatusLabel.Visible = false;

            quickConnectToolStripComboBox.ComboBox.SelectionChangeCommitted +=
                toolStripQuickConnect_SelectionChangeCommited;

            // directly right-click on an item leads to some deferred updating of the menu items
            // since the idle event is fired after showing the menu
            contextMenuStrip.Opening += (sender, args) => Commands.Validate();
            contextMenuStrip.Opening += (sender, args) => args.Cancel = !ContextMenuEnabled();
            bookmarkContextMenu.Popup += delegate { Commands.Validate(); };
            browserContextMenu.Popup += delegate { Commands.Validate(); };

            editorMenuStrip.Opening += OnEditorActionMenuOpening;

            editMainMenuItem.MenuItems.Add(string.Empty);
            editMainMenuItem.Popup += OnEditMenuItemPopup;
            editBrowserContextMenuItem.MenuItems.Add(string.Empty);
            editBrowserContextMenuItem.Popup += OnEditMenuItemPopup;

            // add dummy entry to force the right arrow appearing in the menu
            columnContextMenu.Items.Add(string.Empty);
            archiveMenuStrip.Items.Add(string.Empty);

            archiveMenuStrip.Opening += OnArchiveMenuStripOpening;
            createArchiveMainMenuItem.MenuItems.Add(string.Empty);
            createArchiveMainMenuItem.Popup += OnArchiveMenuItemOnPopup;
            createArchiveBrowserContextMenuItem.MenuItems.Add(string.Empty);
            createArchiveBrowserContextMenuItem.Popup += OnArchiveMenuItemOnPopup;
            copyUrlMainMenuItem.MenuItems.Add(string.Empty);
            copyUrlMainMenuItem.Popup += OnCopyUrlMenuItemPopup;
            copyUrlBrowserContextMenuItem.MenuItems.Add(string.Empty);
            copyUrlBrowserContextMenuItem.Popup += OnCopyUrlMenuItemPopup;
            openUrlMainMenuItem.MenuItems.Add(string.Empty);
            openUrlMainMenuItem.Popup += OnOpenUrlMenuItemPopup;
            openUrlBrowserContextMenuItem.MenuItems.Add(string.Empty);
            openUrlBrowserContextMenuItem.Popup += OnOpenUrlMenuItemPopup;

            textEncodingMenuStrip.Items.Add(string.Empty);
            textEncodingMainMenuItem.MenuItems.Add(string.Empty);

            columnMainMenuItem.MenuItems.Add(string.Empty);
            historyMainMenuItem.MenuItems.Add(string.Empty);
            historyMenuStrip.Items.Add(string.Empty);
            bonjourMenuStrip.Items.Add(string.Empty);

            ConfigureBookmarkList(bookmarkListView, bookmarkDescriptionColumn, bookmarkImageColumn);

            newBookmarkToolStripButton.Tag = ResourcesBundle.addPressed;
            editBookmarkToolStripButton.Tag = ResourcesBundle.editPressed;
            deleteBookmarkToolStripButton.Tag = ResourcesBundle.removePressed;

            browserToolStripButton.ToolTipText = Locale.localizedString("Browser", "Preferences");
            bookmarksToolStripButton.ToolTipText = Locale.localizedString("Bookmarks", "Preferences");
            historyToolStripButton.ToolTipText = Locale.localizedString("History");
            bonjourToolStripButton.ToolTipText = Locale.localizedString("Bonjour", "Browser");

            keyMainMenuItem.Text = LicenseFactory.find().ToString();
            keyMainMenuItem.Enabled = false;

            ConfigureToolbar();
            ConfigureFileCommands();
            ConfigureEditCommands();
            ConfigureViewCommands();
            ConfigureGoCommands();
            ConfigureWindowCommands();
            ConfigureBookmarkCommands();
            ConfigureHelpCommands();
            ConfigureActionlessCommands();

            // restore additional UI settings
            Load += delegate
                        {
                            //correct width for disconnect button                            
                            Size preferredSize = disconnectStripButton.GetPreferredSize(Size.Empty);
                            disconnectStripButton.AutoSize = false;
                            disconnectStripButton.Width = preferredSize.Width;
                            splitContainer.SplitterDistance = PersistenceHandler.Get("Splitter.Distance", 400);

                            Menu = mainMenu;

                            //add menu shortcuts, needs to be done in the Load event handler
                            ConfigureShortcuts();
                        };

            Closed += delegate
                          {
                              //we save the state of the last browser form
                              //this might be improved by some other logic
                              if (MainController.Browsers.Count == 1)
                              {
                                  SaveUiSettings();
                              }
                              BookmarkCollection.defaultCollection().removeListener(bookmarkMenuCollectionListener);
                              HistoryCollection.defaultCollection().removeListener(historyMenuCollectionListener);
                              RendezvousCollection.defaultCollection().removeListener(bonjourMenuCollectionListener);
                          };
        }

        public Image Favicon
        {
            set { ; }
        }

        public override string[] BundleNames
        {
            get { return new[] {"Browser", "Main", "Localizable"}; }
        }

        protected override ContextMenu[] ContextMenuCollection
        {
            get { return new[] {browserContextMenu, bookmarkContextMenu, toolbarContextMenu1}; }
        }

        public event VoidHandler FolderUp;
        public event VoidHandler HistoryBack;
        public event VoidHandler HistoryForward;
        public event EditWithHandler EditEvent;
        public event VoidHandler ShowInspector;
        public event DropHandler BrowserCanDrop;
        public event ModelDropHandler BrowserModelCanDrop;
        public event DropHandler BrowserDropped;
        public event ModelDropHandler BrowserModelDropped;
        public event ValidateCommand ValidateNewFile;
        public event RenamePathname RenameFile;
        public event ValidateCommand ValidateRenameFile;
        public event VoidHandler Delete;
        public event VoidHandler NewFile;
        public event VoidHandler DuplicateFile;
        public event EventHandler<NewBrowserEventArgs> NewBrowser;
        public event ValidateCommand ValidateNewBrowser;
        public event ValidateCommand ValidateDuplicateFile;
        public event ValidateCommand ValidateOpenWebUrl;
        public event ValidateCommand ValidateEditWith;
        public event ValidateCommand ValidateDelete;
        public event ArchivesHandler GetArchives;
        public event CopyUrlHandler GetCopyUrls;
        public event OpenUrlHandler GetOpenUrls;
        public event EventHandler<CreateArchiveEventArgs> CreateArchive;
        public event ValidateCommand ValidateCreateArchive;
        public event VoidHandler ExpandArchive;
        public event ValidateCommand ValidateExpandArchive;
        public event VoidHandler Exit;
        public event VoidHandler QuickConnect;
        public event VoidHandler OpenConnection;
        public event ValidateCommand ValidateOpenConnection;
        public event VoidHandler NewDownload;
        public event ValidateCommand ValidateNewDownload;
        public event ValidateCommand ValidateShowInspector;
        public event VoidHandler Download;
        public event ValidateCommand ValidateDownload;
        public event VoidHandler DownloadAs;
        public event ValidateCommand ValidateDownloadAs;
        public event VoidHandler DownloadTo;
        public event ValidateCommand ValidateDownloadTo;
        public event VoidHandler Upload;
        public event ValidateCommand ValidateUpload;
        public event VoidHandler Synchronize;
        public event ValidateCommand ValidateSynchronize;
        public event VoidHandler BrowserDoubleClicked;
        public event VoidHandler BrowserSelectionChanged;
        public event VoidHandler PathSelectionChanged;
        public event VoidHandler ShowTransfers;
        public event DragHandler BrowserDrag;
        public event EndDragHandler BrowserEndDrag;
        public event DropHandler HostCanDrop;
        public event ModelDropHandler HostModelCanDrop;
        public event DropHandler HostDropped;
        public event ModelDropHandler HostModelDropped;
        public event DragHandler HostDrag;
        public event EndDragHandler HostEndDrag;
        public event VoidHandler NewFolder;
        public event ValidateCommand ValidateNewFolder;
        public event EditorsHandler GetEditors;
        public event ValidateCommand ContextMenuEnabled;
        public event VoidHandler Cut;
        public event ValidateCommand ValidateCut;
        public event VoidHandler Copy;
        public event ValidateCommand ValidateCopy;
        public event VoidHandler Paste;
        public event ValidateCommand ValidatePaste;
        public event ValidateCommand ValidateSelectAll;
        public event VoidHandler ShowPreferences;
        public event VoidHandler ToggleToolbar;
        public event VoidHandler ShowHiddenFiles;
        public event VoidHandler ToggleLogDrawer;
        public event ValidateCommand ValidateTextEncoding;
        public event VoidHandler RefreshBrowser;
        public event ValidateCommand ValidateRefresh;
        public event VoidHandler GotoFolder;
        public event ValidateCommand ValidateGotoFolder;
        public event ValidateCommand ValidateHistoryBack;
        public event ValidateCommand ValidateHistoryForward;
        public event ValidateCommand ValidateFolderUp;
        public event VoidHandler FolderInside;
        public event ValidateCommand ValidateFolderInside;
        public event VoidHandler Search;
        public event VoidHandler SendCustomCommand;
        public event ValidateCommand ValidateSendCustomCommand;
        public event VoidHandler Stop;
        public event ValidateCommand ValidateStop;
        public event VoidHandler Disconnect;
        public event ValidateCommand ValidateDisconnect;
        public event VoidHandler SearchFieldChanged;
        public event EventHandler<ChangeBrowserViewArgs> ChangeBrowserView;
        public event VoidHandler NewBookmark;
        public event ValidateCommand ValidateNewBookmark;
        public event VoidHandler EditBookmark;
        public event ValidateCommand ValidateEditBookmark;
        public event VoidHandler DeleteBookmark;
        public event ValidateCommand ValidateDeleteBookmark;
        public event VoidHandler DuplicateBookmark;
        public event ValidateCommand ValidateDuplicateBookmark;
        public event EventHandler<ConnectBookmarkArgs> ConnectBookmark;
        public event ValidateCommand ValidateConnectBookmark;
        public event VoidHandler OpenUrl;
        public event EventHandler<EncodingChangedArgs> EncodingChanged;
        public event BookmarksHandler GetBookmarks;
        public event BookmarksHandler GetHistory;
        public event BookmarksHandler GetBonjourHosts;
        public event VoidHandler ClearHistory;
        public event VoidHandler ShowCertificate;
        public event VoidHandler ItemsChanged;
        public event ValidateCommand ValidatePathsCombobox;
        public event ValidateCommand ValidateSearchField;
        public event VoidHandler ToggleBookmarks;
        public event VoidHandler RevertFile;
        public event ValidateCommand ValidateRevertFile;

        public bool HiddenFilesVisible
        {
            set { showHiddenFilesToolStripMenuItem.Checked = value; }
        }

        public string SearchString
        {
            get { return searchTextBox.Text; }
            set { searchTextBox.Text = value; }
        }

        public String DownloadAsDialog(string initialDirectory, string fileName)
        {
            saveFileDialog.FileName = fileName;
            if (null != initialDirectory)
            {
                saveFileDialog.InitialDirectory = initialDirectory;
            }
            if (DialogResult.OK == saveFileDialog.ShowDialog(this))
            {
                return saveFileDialog.FileName;
            }
            return null;
        }

        public String DownloadToDialog(string description, Environment.SpecialFolder root, string selectedPath)
        {
            folderBrowserDialog.RootFolder = root;
            folderBrowserDialog.Description = description;
            if (null != selectedPath)
            {
                folderBrowserDialog.SelectedPath = selectedPath;
            }
            if (DialogResult.OK == folderBrowserDialog.ShowDialog(this))
            {
                return folderBrowserDialog.SelectedPath;
            }
            return null;
        }

        public String SynchronizeDialog(string description, Environment.SpecialFolder root, string selectedPath)
        {
            folderBrowserDialog.RootFolder = root;
            folderBrowserDialog.Description = description;
            if (null != selectedPath)
            {
                folderBrowserDialog.SelectedPath = selectedPath;
            }
            if (DialogResult.OK == folderBrowserDialog.ShowDialog(this))
            {
                return folderBrowserDialog.SelectedPath;
            }
            return null;
        }

        public String[] UploadDialog(string root)
        {
            using (var dialog = new SelectFileAndFolderDialog())
            {
                dialog.AcceptFiles = true;
                if (null != root)
                {
                    dialog.Path = root;
                }
                string selectText = Locale.localizedString("Choose");
                string canelText = Locale.localizedString("Cancel");

                dialog.FileNameLabel = selectText + ":";
                dialog.SelectLabel = "&" + selectText;
                dialog.CancelLabel = "&" + canelText;
                dialog.ShowDialog();
                string[] paths = dialog.SelectedPaths;
                if (paths.Length == 0)
                {
                    return null;
                }
                return paths;
            }
        }

        public int NumberOfBookmarks
        {
            get { return bookmarkListView.GetItemCount(); }
        }

        public int NumberOfFiles
        {
            get { return browser.GetItemCount(); }
        }

        public string QuickConnectValue
        {
            get
            {
                if (null != quickConnectToolStripComboBox.SelectedItem)
                {
                    return (string) quickConnectToolStripComboBox.SelectedItem;
                }
                return quickConnectToolStripComboBox.Text;
            }
        }

        public bool HistoryBackEnabled
        {
            set { historyBackButton.Enabled = value; }
        }

        public bool HistoryForwardEnabled
        {
            set { historyForwardButton.Enabled = value; }
        }

        public bool ParentPathEnabled
        {
            set { parentPathButton.Enabled = value; }
        }

        public string SelectedComboboxPath
        {
            get { return pathComboBox.Text; }
        }

        public Bitmap EditIcon
        {
            set { editToolStripSplitButton.Image = value; }
        }

        public Bitmap OpenIcon
        {
            set { openInBrowserToolStripButton.Image = value; }
        }

        public bool SecureConnectionVisible
        {
            set { securityToolStripStatusLabel.Visible = value; }
        }

        public bool CertBasedConnection
        {
            set { securityToolStripStatusLabel.Enabled = value; }
        }

        public bool ActivityRunning
        {
            set
            {
                if (_lastActivityRunning != value)
                {
                    if (value && disconnectStripButton.Image != ResourcesBundle.stop)
                    {
                        disconnectStripButton.Image = ResourcesBundle.stop;
                        disconnectStripButton.Text = Locale.localizedString("Stop");
                    }
                    else if (!value && disconnectStripButton.Image != ResourcesBundle.eject)
                    {
                        disconnectStripButton.Image = ResourcesBundle.eject;
                        disconnectStripButton.Text = Locale.localizedString("Disconnect");
                    }
                    _lastActivityRunning = value;
                }
            }
        }

        public bool ShowActivityEnabled
        {
            set { disconnectStripButton.Enabled = value; }
        }

        public void PopulateQuickConnect(List<string> nicknames)
        {
            quickConnectToolStripComboBox.Items.Clear();
            foreach (string nickname in nicknames)
            {
                quickConnectToolStripComboBox.Items.Add(nickname);
            }
        }

        public void SetBrowserModel(IEnumerable<TreePathReference> model)
        {
            // Clear the cache in order to avoid strange side effects
            browser.ClearCachedInfo();
            browser.SetObjects(model);

            //only restore the state for the first time
            if (null != model && !_browserStateRestored)
            {
                byte[] state = PersistenceHandler.Get<byte[]>("Tree.State", null);
                if (null != state)
                {
                    browser.RestoreState(state);
                }
                else
                {
                    //by default we sort ascending by filename
                    browser.Sort(0);
                }
                _browserStateRestored = true;
            }
        }

        public void RefreshBrowserObject(TreePathReference path)
        {
            browser.RefreshObject(path);
        }

        public void RefreshBrowserObjects(List<TreePathReference> list)
        {
            browser.RefreshObjects(list);
        }

        public void BrowserActiveStateChanged()
        {
            browser.Invalidate();
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelFilenameGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnName) {AspectGetter = value}; }
        }

        public TypedColumn<TreePathReference>.TypedImageGetterDelegate ModelIconGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnName) {ImageGetter = value}; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelSizeGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnSize) {AspectGetter = value}; }
        }

        public AspectToStringConverterDelegate ModelSizeAsStringGetter
        {
            set { treeColumnSize.AspectToStringConverter = value; }
        }

        public TreeListView.CanExpandGetterDelegate ModelCanExpandDelegate
        {
            set { browser.CanExpandGetter = value; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelModifiedGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnModified) {AspectGetter = value}; }
        }

        public AspectToStringConverterDelegate ModelModifiedAsStringGetter
        {
            set { treeColumnModified.AspectToStringConverter = value; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelOwnerGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnOwner) {AspectGetter = value}; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelGroupGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnGroup) {AspectGetter = value}; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelPermissionsGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnPermissions) {AspectGetter = value}; }
        }

        public TypedColumn<TreePathReference>.TypedAspectGetterDelegate ModelKindGetter
        {
            set { new TypedColumn<TreePathReference>(treeColumnKind) {AspectGetter = value}; }
        }

        public MulticolorTreeListView.ActiveGetterDelegate ModelActiveGetter
        {
            set { browser.ActiveGetter = value; }
        }

        public void AddTranscriptEntry(bool request, string entry)
        {
            transcriptBox.SelectionFont = FixedFont;
            if (request)
            {
                transcriptBox.SelectionColor = Color.Black;
            }
            else
            {
                transcriptBox.SelectionColor = Color.DarkGray;
            }
            if (transcriptBox.TextLength > 0)
            {
                entry = Environment.NewLine + entry;
            }
            transcriptBox.SelectedText = entry;
            // todo improve performance
            // Select seems to be an expensive operation
            // see http://codebetter.com/blogs/patricksmacchia/archive/2008/07/07/some-richtextbox-tricks.aspx
            transcriptBox.Select(transcriptBox.TextLength, transcriptBox.TextLength);
            transcriptBox.ScrollToCaret();
        }

        public void ClearTranscript()
        {
            transcriptBox.Clear();
        }

        public void FocusBrowser()
        {
            browser.Focus();
        }

        public TreeListView.ChildrenGetterDelegate ModelChildrenGetterDelegate
        {
            set { browser.ChildrenGetter = value; }
        }

        public BrowserView CurrentView
        {
            get { return _currentView; }
            set
            {
                if (value != _currentView)
                {
                    _currentView = value;
                    switch (value)
                    {
                        case BrowserView.File:
                            panelManager1.SelectedPanel = managedBrowserPanel1;
                            browser.Focus();
                            EnableViewToolStripButton(browserToolStripButton);
                            return;
                        case BrowserView.Bookmark:
                            panelManager1.SelectedPanel = managedBookmarkPanel2;
                            EnableViewToolStripButton(bookmarksToolStripButton);
                            bookmarkListView.Focus();
                            return;
                        case BrowserView.History:
                            panelManager1.SelectedPanel = managedBookmarkPanel2;
                            bookmarkListView.Focus();
                            EnableViewToolStripButton(historyToolStripButton);
                            return;
                        case BrowserView.Bonjour:
                            panelManager1.SelectedPanel = managedBookmarkPanel2;
                            bookmarkListView.Focus();
                            EnableViewToolStripButton(bonjourToolStripButton);
                            return;
                    }
                }
            }
        }

        public string StatusLabel
        {
            set { statusLabel.Text = value; }
        }

        public List<TreePathReference> SelectedPaths
        {
            get { return new List<TreePathReference>(new ListAdapter<TreePathReference>(browser.SelectedObjects)); }
            set { browser.SelectedObjects = value; }
        }

        public List<Host> SelectedBookmarks
        {
            get { return new List<Host>(new ListAdapter<Host>(bookmarkListView.SelectedObjects)); }
        }

        public void EnsureBookmarkVisible(Host host)
        {
            bookmarkListView.EnsureModelVisible(host);
        }

        public void SelectBookmark(Host host)
        {
            bookmarkListView.SelectObject(host, true);
            bookmarkListView.EnsureModelVisible(host);
            bookmarkListView.Focus();
        }

        public void PopulatePaths(List<string> paths)
        {
            pathComboBox.Items.Clear();
            pathComboBox.Items.AddRange(paths.ToArray());
            if (paths.Count > 0)
                pathComboBox.SelectedIndex = 0;
        }

        public void StartRenaming(Path path)
        {
            browser.GetItem(browser.IndexOf(path)).BeginEdit();
        }

        public void StartSearch()
        {
            searchTextBox.Focus();
        }

        public List<TreePathReference> VisiblePaths
        {
            get
            {
                int count = browser.GetItemCount();
                List<TreePathReference> paths = new List<TreePathReference>(count);
                for (int i = 0; i < browser.GetItemCount(); i++)
                {
                    paths.Add((TreePathReference) browser.GetModelObject(i));
                }
                return paths;
            }
        }

        public void StartActivityAnimation()
        {
            toolStripProgress.Visible = true;
        }

        public void StopActivityAnimation()
        {
            toolStripProgress.Visible = false;
        }

        public PathFilter FilenameFilter
        {
            set
            {
                if (null == value)
                {
                    browser.UseFiltering = false;
                }
                else
                {
                    browser.ModelFilter = new FilterWrapper(value);
                    browser.UseFiltering = true;
                }
            }
        }

        public bool LogDrawerVisible
        {
            get { return !splitContainer.Panel2Collapsed; }
            set { splitContainer.Panel2Collapsed = !value; }
        }

        public Host SelectedBookmark
        {
            get { return (Host) bookmarkListView.SelectedObject; }
        }

        public ImageGetterDelegate BookmarkImageGetter
        {
            set { bookmarkImageColumn.ImageGetter = value; }
        }

        public AspectGetterDelegate BookmarkNicknameGetter
        {
            set { bookmarkDescriptionColumn.AspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkHostnameGetter
        {
            set { ((BookmarkRenderer) bookmarkDescriptionColumn.Renderer).HostnameAspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkUrlGetter
        {
            set { ((BookmarkRenderer) bookmarkDescriptionColumn.Renderer).UrlAspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkNotesGetter
        {
            set { ((BookmarkRenderer) bookmarkDescriptionColumn.Renderer).NotesAspectGetter = value; }
        }

        public ImageGetterDelegate BookmarkStatusImageGetter
        {
            set { activeColumn.ImageGetter = value; }
        }

        public void SetBookmarkModel(IEnumerable hosts, Host selected)
        {
            int index = -1;
            if (null != selected)
            {
                OLVListItem item = bookmarkListView.ModelToItem(selected);
                if (null != item)
                {
                    index = item.Index;
                }
            }
            bookmarkListView.SetObjects(hosts, true);
            if (index != -1 && bookmarkListView.Items.Count > 0)
            {
                if (index >= bookmarkListView.Items.Count)
                {
                    index = bookmarkListView.Items.Count - 1;
                }
                bookmarkListView.EnsureVisible(index);
            }
        }

        public void RefreshBookmark(Host host)
        {
            bookmarkListView.RefreshObject(host);
        }

        public void AddBookmark(Host host)
        {
            bookmarkListView.AddObject(host);
        }

        public void RemoveBookmark(Host host)
        {
            bookmarkListView.RemoveObject(host);
        }

        public string WindowTitle
        {
            set { Text = value; }
        }

        public bool ToolbarVisible
        {
            set
            {
                toolBar.Visible = value;
                //todo localize
                toggleToolbarToolStripMenuItem.Text = Locale.localizedString(value ? "Hide Toolbar" : "Show Toolbar");
                toggleToolbarMainMenuItem.Text = Locale.localizedString(value ? "Hide Toolbar" : "Show Toolbar");
            }
            get { return toolBar.Visible; }
        }

        public void PopulateEncodings(List<string> encodings)
        {
            textEncodingMainMenuItem.MenuItems.Clear();
            foreach (string encoding in encodings)
            {
                string encoding1 = encoding;
                textEncodingMainMenuItem.MenuItems.Add(encoding,
                                                       (sender, args) =>
                                                       EncodingChanged(sender, new EncodingChangedArgs(encoding1)));
            }
        }

        public string SelectedEncoding
        {
            set
            {
                foreach (MenuItem item in textEncodingMainMenuItem.MenuItems)
                {
                    item.Checked = value.Equals(item.Text);
                }
            }
        }

        public bool SecureConnection
        {
            set { securityToolStripStatusLabel.Image = IconCache.Instance.IconForName(value ? "locked" : "unlocked"); }
        }

        private void OnEditMenuItemPopup(object sender, EventArgs e)
        {
            MenuItem mainItem = sender as MenuItem;
            mainItem.MenuItems.Clear();

            //Add default entry
            {
                MenuItem item = mainItem.MenuItems.Add(Locale.localizedString("Default"));
                item.Click += delegate { EditEvent(null); };
                //todo refactor! no direct IconCache access.
                vistaMenu1.SetImage(item, IconCache.ResizeImage(editToolStripSplitButton.Image, new Size(16, 16)));
                SetShortcutText(item, editWithToolStripMenuItem, null);
            }
            IList<KeyValuePair<string, string>> editors = GetEditors();
            if (editors.Count > 0)
            {
                mainItem.MenuItems.Add("-");
            }
            foreach (KeyValuePair<string, string> pair in editors)
            {
                MenuItem item = mainItem.MenuItems.Add(pair.Key);
                item.Tag = pair.Value;
                item.Click += delegate { EditEvent(item.Tag as String); };
                vistaMenu1.UpdateParent(mainItem);
                vistaMenu1.SetImage(item,
                                    IconCache.Instance.ExtractIconFromExecutable(pair.Value, IconCache.IconSize.Small));
            }
        }

        private void OnArchiveMenuStripOpening(object sender, CancelEventArgs e)
        {
            archiveMenuStrip.Items.Clear();
            foreach (string archive in GetArchives())
            {
                ToolStripMenuItem item = new ToolStripMenuItem(archive);
                string archiveName = archive;
                item.Click += delegate { CreateArchive(this, new CreateArchiveEventArgs(archiveName)); };
                archiveMenuStrip.Items.Add(item);
            }
        }

        private void OnEditorActionMenuOpening(object sender, EventArgs e)
        {
            editorMenuStrip.Items.Clear();
            foreach (KeyValuePair<string, string> pair in GetEditors())
            {
                ToolStripItem item = new ToolStripMenuItem(pair.Key);
                item.Tag = pair.Value;
                item.Image = IconCache.Instance.ExtractIconFromExecutable(pair.Value, IconCache.IconSize.Small);
                item.Click += (o, args) => EditEvent(item.Tag as String);
                editorMenuStrip.Items.Add(item);
            }
            if (editorMenuStrip.Items.Count == 0)
            {
                //Add default entry
                ToolStripItem item = new ToolStripMenuItem(Locale.localizedString("Default"));
                item.Image = editToolStripSplitButton.Image;
                item.Click += (o, args) => EditEvent(null);
                editorMenuStrip.Items.Add(item);
            }
        }

        private void OnCopyUrlMenuItemPopup(object sender, EventArgs e)
        {
            PopulateCopyUrlMenuItemPopup(sender as MenuItem, GetCopyUrls());
        }

        private void PopulateCopyUrlMenuItemPopup(MenuItem mainItem, List<KeyValuePair<String, List<String>>> items)
        {
            mainItem.MenuItems.Clear();
            int c = 0;
            foreach (KeyValuePair<string, List<string>> pair in items)
            {
                if (c > 0)
                {
                    //add separator
                    mainItem.MenuItems.Add("-");
                }
                MenuItem item = mainItem.MenuItems.Add(pair.Key);
                if (pair.Value.Count > 0)
                {
                    KeyValuePair<string, List<string>> pair1 = pair;
                    item.Click += delegate
                                      {
                                          StringBuilder sb = new StringBuilder();
                                          for (int i = 0; i < pair1.Value.Count; i++)
                                          {
                                              if (i > 0) sb.Append(Environment.NewLine);
                                              sb.Append(pair1.Value[i]);
                                          }
                                          Clipboard.SetText(sb.ToString());
                                      };
                    foreach (string url in pair.Value)
                    {
                        mainItem.MenuItems.Add(url).Enabled = false;
                    }
                }
                else
                {
                    item.Enabled = false;
                }
                c++;
            }
        }

        private void OnOpenUrlMenuItemPopup(object sender, EventArgs e)
        {
            PopulateOpenUrlMenuItemPopup(sender as MenuItem, GetOpenUrls());
        }

        private void PopulateOpenUrlMenuItemPopup(MenuItem mainItem, List<KeyValuePair<String, List<String>>> items)
        {
            mainItem.MenuItems.Clear();
            int c = 0;
            foreach (KeyValuePair<string, List<string>> pair in items)
            {
                if (c > 0)
                {
                    //add separator
                    mainItem.MenuItems.Add("-");
                }
                MenuItem item = mainItem.MenuItems.Add(pair.Key);
                if (pair.Value.Count > 0)
                {
                    KeyValuePair<string, List<string>> pair1 = pair;
                    item.Click += delegate
                                      {
                                          for (int i = 0; i < pair1.Value.Count; i++)
                                          {
                                              Utils.StartProcess(pair1.Value[i]);
                                          }
                                      };
                    foreach (string url in pair.Value)
                    {
                        mainItem.MenuItems.Add(url).Enabled = false;
                    }
                }
                else
                {
                    item.Enabled = false;
                }
                c++;
            }
        }

        private void SetShortcutText(MenuItem target, ToolStripMenuItem source, string shortCutText)
        {
            if (-1 == target.Text.IndexOf('\t'))
            {
                target.Text = String.Format("{0}\t{1}", target.Text, ShortcutToText(source.ShortcutKeys, shortCutText));
            }
        }

        private void ConfigureActionlessCommands()
        {
            Commands.Add(pathComboBox, () => ValidatePathsCombobox());
            Commands.Add(searchTextBox, () => ValidateSearchField());
        }

        private void UpdateSeparators()
        {
            toolStripSeparatorAfterOpenConnection.Visible = openConnectionToolStripMenuItem1.Checked;
            toolStripSeparatorAfterAction.Visible = quickConnectToolStripMenuItem.Checked ||
                                                    actionToolStripMenuItem.Checked;
            toolStripSeparatorAfterRefresh.Visible = infoToolStripMenuItem1.Checked ||
                                                     refreshToolStripMenuItem1.Checked;
            toolStripSeparatorAfterDelete.Visible = editToolStripMenuItem1.Checked ||
                                                    openInWebBrowserToolStripMenuItem.Checked ||
                                                    newFolderToolStripMenuItem1.Checked ||
                                                    deleteToolStripMenuItem1.Checked;

            //new
            toolStripSeparatorAfterOpenConnection.Visible = openConnectionToolbarMenuItem.Checked;
            toolStripSeparatorAfterAction.Visible = quickConnectToolbarMenuItem.Checked ||
                                                    actionContextToolbarMenuItem.Checked;
            toolStripSeparatorAfterRefresh.Visible = infoToolbarMenuItem.Checked ||
                                                     refreshToolbarMenuItem.Checked;
            toolStripSeparatorAfterDelete.Visible = editToolbarMenuItem.Checked ||
                                                    openInWebBrowserToolbarMenuItem.Checked ||
                                                    newFolderToolbarMenuItem.Checked ||
                                                    deleteToolbarMenuItem.Checked;
        }

        internal static string ShortcutToText(Keys shortcutKeys, string shortcutKeyDisplayString)
        {
            if (!string.IsNullOrEmpty(shortcutKeyDisplayString))
            {
                return shortcutKeyDisplayString;
            }
            if (shortcutKeys == Keys.None)
            {
                return String.Empty;
            }
            return shortcutConverter.ConvertToString(shortcutKeys);
        }

        private void ConfigureToolbar()
        {
            //new
            customizeToolbarMainMenuItem.MenuItems.Clear();

            EventHandler h;
            MenuItem m;

            openConnectionToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        openConnectionToolbarMenuItem.Checked = !openConnectionToolbarMenuItem.Checked;
                        openConnectionToolStripButton.Visible =
                            !openConnectionToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.openconnection",
                            openConnectionToolStripButton.Visible);
                    };
            openConnectionToolStripMenuItem1.Click += h;
            openConnectionToolbarMenuItem.Click += h;
            m = new MenuItem(openConnectionToolbarMenuItem.Text, h);
            m.Tag = openConnectionToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);
            customizeToolbarMainMenuItem.MenuItems.Add("-");

            quickConnectToolStripMenuItem.CheckOnClick = true;
            h = delegate
                    {
                        quickConnectToolbarMenuItem.Checked = !quickConnectToolbarMenuItem.Checked;
                        quickConnectToolStripComboBox.Visible =
                            !quickConnectToolStripComboBox.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.quickconnect",
                            quickConnectToolStripComboBox.Visible);
                    };
            quickConnectToolStripMenuItem.Click += h;
            quickConnectToolbarMenuItem.Click += h;
            m = new MenuItem(quickConnectToolbarMenuItem.Text, h);
            m.Tag = quickConnectToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            actionToolStripMenuItem.CheckOnClick = true;
            h = delegate
                    {
                        actionContextToolbarMenuItem.Checked = !actionContextToolbarMenuItem.Checked;
                        actionToolStripDropDownButton.Visible =
                            !actionToolStripDropDownButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.action",
                            actionToolStripDropDownButton.Visible);
                    };
            actionToolStripMenuItem.Click += h;
            actionContextToolbarMenuItem.Click += h;
            m = new MenuItem(actionContextToolbarMenuItem.Text, h);
            m.Tag = actionContextToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);
            customizeToolbarMainMenuItem.MenuItems.Add("-");

            infoToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        infoToolbarMenuItem.Checked = !infoToolbarMenuItem.Checked;
                        infoToolStripButton.Visible = !infoToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.info",
                            infoToolStripButton.Visible);
                    };
            infoToolStripMenuItem1.Click += h;
            infoToolbarMenuItem.Click += h;
            m = new MenuItem(infoToolbarMenuItem.Text, h);
            m.Tag = infoToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            refreshToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        refreshToolbarMenuItem.Checked = !refreshToolbarMenuItem.Checked;
                        refreshToolStripButton.Visible = !refreshToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.refresh",
                            refreshToolStripButton.Visible);
                    };
            refreshToolStripMenuItem1.Click += h;
            refreshToolbarMenuItem.Click += h;
            m = new MenuItem(refreshToolbarMenuItem.Text, h);
            m.Tag = refreshToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);
            customizeToolbarMainMenuItem.MenuItems.Add("-");

            editToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        editToolbarMenuItem.Checked = !editToolbarMenuItem.Checked;
                        editToolStripSplitButton.Visible = !editToolStripSplitButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.edit",
                            editToolStripSplitButton.Visible);
                    };
            editToolStripMenuItem1.Click += h;
            editToolbarMenuItem.Click += h;
            m = new MenuItem(editToolbarMenuItem.Text, h);
            m.Tag = editToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            openInWebBrowserToolStripMenuItem.CheckOnClick = true;
            h = delegate
                    {
                        openInWebBrowserToolbarMenuItem.Checked =
                            !openInWebBrowserToolbarMenuItem.Checked;
                        openInBrowserToolStripButton.Visible =
                            !openInBrowserToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.openinbrowser",
                            openInBrowserToolStripButton.Visible);
                    };
            openInWebBrowserToolStripMenuItem.Click += h;
            openInWebBrowserToolbarMenuItem.Click += h;
            m = new MenuItem(openInWebBrowserToolbarMenuItem.Text, h);
            m.Tag = openInWebBrowserToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            newFolderToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        newFolderToolbarMenuItem.Checked = !newFolderToolbarMenuItem.Checked;
                        newFolderToolStripButton.Visible =
                            !newFolderToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.newfolder",
                            newFolderToolStripButton.Visible);
                    };
            newFolderToolStripMenuItem1.Click += h;
            newFolderToolbarMenuItem.Click += h;
            m = new MenuItem(newFolderToolbarMenuItem.Text, h);
            m.Tag = newFolderToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            deleteToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        deleteToolbarMenuItem.Checked = !deleteToolbarMenuItem.Checked;
                        deleteToolStripButton.Visible = !deleteToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.delete",
                            deleteToolStripButton.Visible);
                    };
            deleteToolStripMenuItem1.Click += h;
            deleteToolbarMenuItem.Click += h;
            m = new MenuItem(deleteToolbarMenuItem.Text, h);
            m.Tag = deleteToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);
            customizeToolbarMainMenuItem.MenuItems.Add("-");

            downloadToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        downloadToolbarMenuItem.Checked = !downloadToolbarMenuItem.Checked;
                        downloadToolStripButton.Visible =
                            !downloadToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.download",
                            downloadToolStripButton.Visible);
                    };
            downloadToolStripMenuItem1.Click += h;
            downloadToolbarMenuItem.Click += h;
            m = new MenuItem(downloadToolbarMenuItem.Text, h);
            m.Tag = downloadToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            uploadToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        uploadToolbarMenuItem.Checked = !uploadToolbarMenuItem.Checked;
                        uploadToolStripButton.Visible = !uploadToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.upload",
                            uploadToolStripButton.Visible);
                    };
            uploadToolStripMenuItem1.Click += h;
            uploadToolbarMenuItem.Click += h;
            m = new MenuItem(uploadToolbarMenuItem.Text, h);
            m.Tag = uploadToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            transfersToolStripMenuItem1.CheckOnClick = true;
            h = delegate
                    {
                        transfersToolbarMenuItem.Checked = !transfersToolbarMenuItem.Checked;
                        transfersToolStripButton.Visible =
                            !transfersToolStripButton.Visible;
                        UpdateSeparators();
                        Preferences.instance().setProperty(
                            "browser.toolbar.transfers",
                            transfersToolStripButton.Visible);
                    };
            transfersToolStripMenuItem1.Click += h;
            transfersToolbarMenuItem.Click += h;
            m = new MenuItem(transfersToolbarMenuItem.Text, h);
            m.Tag = transfersToolbarMenuItem;
            customizeToolbarMainMenuItem.MenuItems.Add(m);

            bool b1 =
                openConnectionToolStripButton.Visible =
                Preferences.instance().getBoolean("browser.toolbar.openconnection");
            bool b2 =
                quickConnectToolStripComboBox.Visible =
                Preferences.instance().getBoolean("browser.toolbar.quickconnect");
            bool b3 =
                actionToolStripDropDownButton.Visible = Preferences.instance().getBoolean("browser.toolbar.action");
            bool b4 = infoToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.info");
            bool b5 = refreshToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.refresh");
            bool b6 = editToolStripSplitButton.Visible = Preferences.instance().getBoolean("browser.toolbar.edit");
            bool b7 =
                openInBrowserToolStripButton.Visible =
                Preferences.instance().getBoolean("browser.toolbar.openinbrowser");
            bool b8 = newFolderToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.newfolder");
            bool b9 = deleteToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.delete");
            bool b10 = downloadToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.download");
            bool b11 = uploadToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.upload");
            bool b12 = transfersToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.transfers");

            // update menu entries
            openConnectionToolStripMenuItem1.Checked = b1;
            quickConnectToolStripMenuItem.Checked = b2;
            actionToolStripMenuItem.Checked = b3;
            infoToolStripMenuItem1.Checked = b4;
            refreshToolStripMenuItem1.Checked = b5;
            editToolStripMenuItem1.Checked = b6;
            openInWebBrowserToolStripMenuItem.Checked = b7;
            newFolderToolStripMenuItem1.Checked = b8;
            deleteToolStripMenuItem1.Checked = b9;
            downloadToolStripMenuItem1.Checked = b10;
            uploadToolStripMenuItem1.Checked = b11;
            transfersToolStripMenuItem1.Checked = b12;

            openConnectionToolbarMenuItem.Checked = b1;
            quickConnectToolbarMenuItem.Checked = b2;
            actionContextToolbarMenuItem.Checked = b3;
            infoToolbarMenuItem.Checked = b4;
            refreshToolbarMenuItem.Checked = b5;
            editToolbarMenuItem.Checked = b6;
            openInWebBrowserToolbarMenuItem.Checked = b7;
            newFolderToolbarMenuItem.Checked = b8;
            deleteToolbarMenuItem.Checked = b9;
            downloadToolbarMenuItem.Checked = b10;
            uploadToolbarMenuItem.Checked = b11;
            transfersToolbarMenuItem.Checked = b12;

            UpdateSeparators();
        }

        public event VoidHandler OpenDownloadFolderEvent;

        private void ConfigureBookmarkCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 viewBookmarksToolStripMenuItem
                             }, new[] {toggleBookmarksMainMenuItem}, (sender, args) => ToggleBookmarks(), () => true);

            Commands.Add(new ToolStripItem[]
                             {
                                 connectBookmarkContextToolStripMenuItem,
                             }, new[] {connectBookmarkContextMenuItem},
                         (sender, args) =>
                         ConnectBookmark(this, new ConnectBookmarkArgs(bookmarkListView.SelectedObject as Host)),
                         () => ValidateConnectBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 newBookmarkToolStripMenuItem,
                                 newBookmarkContextToolStripMenuItem,
                                 newBookmarkContextToolStripMenuItem1,
                                 newBookmarkToolStripButton
                             },
                         new[] {newBookmarkContextMenuItem, newBookmarkMainMenuItem, newBookmarkBrowserContextMenuItem},
                         (sender, args) => NewBookmark(), () => ValidateNewBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 editBookmarkToolStripMenuItem,
                                 editBookmarkContextToolStripMenuItem1,
                                 editBookmarkToolStripButton
                             }, new[] {editBookmarkMainMenuItem, editBookmarkContextMenuItem},
                         (sender, args) => EditBookmark(), () => ValidateEditBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 deleteBookmarkToolStripMenuItem,
                                 deleteBookmarkContextToolStripMenuItem1,
                                 deleteBookmarkToolStripButton
                             }, new[] {deleteBookmarkContextMenuItem, deleteBookmarkMainMenuItem},
                         (sender, args) => DeleteBookmark(), () => ValidateDeleteBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 duplicateBookmarkToolStripMenuItem1,
                                 duplicateBookmarkToolStripMenuItem
                             }, new[] {duplicateBookmarkContextMenuItem, duplicateBookmarkMainMenuItem},
                         (sender, args) => DuplicateBookmark(), () => ValidateDuplicateBookmark());
        }

        private void ConfigureBookmarkList(ObjectListView l, OLVColumn descColumn, OLVColumn imageColumn)
        {
            l.RowHeight = 72;
            l.ShowGroups = false;
            l.UseOverlays = false;
            l.OwnerDraw = true;
            l.FullRowSelect = true;
            l.MultiSelect = true;
            l.HeaderStyle = ColumnHeaderStyle.None;
            l.HideSelection = false;
            l.AllowDrop = true;
            l.DropSink = new HostDropSink(this);
            l.DragSource = new HostDragSource(this);

            BookmarkRenderer bookmarkRenderer = new BookmarkRenderer();
            Font smallerFont = new Font(bookmarkListView.Font.FontFamily, bookmarkListView.Font.Size - 1);
            bookmarkRenderer.NicknameFont = new Font(bookmarkListView.Font, FontStyle.Bold);
            bookmarkRenderer.HostnameFont = smallerFont;
            bookmarkRenderer.UrlFont = smallerFont;
            bookmarkRenderer.NotesFont = smallerFont;
            bookmarkRenderer.UrlNotesSpace = 3;

            //taskRenderer.CellPadding = new Size(2, 5);
            descColumn.Renderer = bookmarkRenderer;
            descColumn.FillsFreeSpace = true;

            imageColumn.Width = 90;
            imageColumn.TextAlign = HorizontalAlignment.Center;
        }

        private void EnableViewToolStripButton(ToolStripButton cb)
        {
            browserToolStripButton.Checked = false;
            bookmarksToolStripButton.Checked = false;
            historyToolStripButton.Checked = false;
            bonjourToolStripButton.Checked = false;

            cb.Checked = true;
        }

        private void ConfigureHelpCommands()
        {
            //direct commands
            Commands.Add(new ToolStripItem[] {acknowledgmentsToolStripMenuItem}, new[] {acknowledgmentsMainMenuItem},
                         (sender, args) => Utils.StartProcess("Acknowledgments.rtf"), () => true);
            Commands.Add(new ToolStripItem[] {cyberduckHelpToolStripMenuItem}, new[] {helpMainMenuItem},
                         (sender, args) => Utils.StartProcess(Preferences.instance().getProperty("website.help")),
                         () => true);
            Commands.Add(new ToolStripItem[] {cyberduckHelpToolStripMenuItem}, new[] {donateMainMenuItem},
                         (sender, args) => Utils.StartProcess(Preferences.instance().getProperty("website.donate")),
                         () => true);
            Commands.Add(new ToolStripItem[] {reportABugToolStripMenuItem}, new[] {bugMainMenuItem},
                         (sender, args) => Utils.StartProcess(Preferences.instance().getProperty("website.bug")),
                         () => true);
            Commands.Add(new ToolStripItem[] {aboutCyberduckToolStripMenuItem}, new[] {aboutMainMenuItem},
                         (sender, args) => new AboutBox().ShowDialog(), () => true);
            Commands.Add(new ToolStripItem[] {licenseToolStripMenuItem}, new[] {licenseMainMenuItem},
                         (sender, args) =>
                         Utils.StartProcess("License.txt"),
                         () => true);
            Commands.Add(new ToolStripItem[] {checkToolStripMenuItem}, new[] {updateMainMenuItem},
                         (sender, args) => UpdateController.Instance.ForceCheckForUpdates(false),
                         () => true);
        }

        private void ConfigureGoCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 refreshToolStripMenuItem,
                                 refreshContextToolStripMenuItem,
                                 refreshToolStripButton
                             }, new[] {refreshMainMenuItem, refreshBrowserContextMenuItem},
                         (sender, args) => RefreshBrowser(), () => ValidateRefresh());
            Commands.Add(new ToolStripItem[]
                             {
                                 gotoFolderToolStripMenuItem
                             }, new[] {goToFolderMainMenuItem},
                         (sender, args) => GotoFolder(), () => ValidateGotoFolder());
            Commands.Add(new ToolStripItem[]
                             {
                                 backToolStripMenuItem
                             }, new Control[] {historyBackButton}, new[] {backMainMenuItem},
                         (sender, args) => HistoryBack(), () => ValidateHistoryBack());
            Commands.Add(new ToolStripItem[]
                             {
                                 forwardToolStripMenuItem
                             }, new Control[]
                                    {
                                        historyForwardButton
                                    }, new[] {forwardMainMenuItem},
                         (sender, args) => HistoryForward(), () => ValidateHistoryForward());
            Commands.Add(new ToolStripItem[]
                             {
                                 enclosingFolderToolStripMenuItem
                             }, new Control[]
                                    {
                                        parentPathButton
                                    }, new[] {enclosingFolderMainMenuItem},
                         (sender, args) => FolderUp(), () => ValidateFolderUp());
            Commands.Add(new ToolStripItem[]
                             {
                                 insideToolStripMenuItem
                             }, new[] {insideMainMenuItem},
                         (sender, args) => FolderInside(), () => ValidateFolderInside());
            Commands.Add(new ToolStripItem[]
                             {
                                 searchToolStripMenuItem
                             }, new[] {searchMainMenuItem},
                         (sender, args) => Search(), () => ValidateSearchField());
            Commands.Add(new ToolStripItem[]
                             {
                                 sendCommandToolStripMenuItem
                             }, new[] {sendCommandMainMenuItem},
                         (sender, args) => SendCustomCommand(), () => ValidateSendCustomCommand());
            Commands.Add(new ToolStripItem[]
                             {
                                 stopToolStripMenuItem
                             }, new[] {stopMainMenuItem},
                         (sender, args) => Stop(), () => ValidateStop());
            Commands.Add(new ToolStripItem[]
                             {
                                 disconnectToolStripMenuItem,
                                 disconnectStripButton
                             }, new[] {disconnectMainMenuItem},
                         (sender, args) => Disconnect(), () => ValidateDisconnect());


            vistaMenu1.SetImage(refreshMainMenuItem, IconCache.Instance.IconForName("reload", 16));
            vistaMenu1.SetImage(refreshBrowserContextMenuItem, IconCache.Instance.IconForName("reload", 16));
            refreshContextToolStripMenuItem.Image = IconCache.Instance.IconForName("reload", 16);
            vistaMenu1.SetImage(stopMainMenuItem, IconCache.Instance.IconForName("stop", 16));
            vistaMenu1.SetImage(disconnectMainMenuItem, IconCache.Instance.IconForName("eject", 16));
        }

        private void ConfigureViewCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 toggleToolbarToolStripMenuItem
                             }, new[] {toggleToolbarMainMenuItem},
                         (sender, args) => ToggleToolbar(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 showHiddenFilesToolStripMenuItem
                             }, new[] {showHiddenFilesMainMenuItem},
                         (sender, args) => ShowHiddenFiles(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 textEncodingToolStripMenuItem
                             }, new[] {textEncodingMainMenuItem},
                         null, () => ValidateTextEncoding());
            Commands.Add(new ToolStripItem[]
                             {
                                 toggleLogDrawerToolStripMenuItem
                             }, new[] {toggleLogDrawerMainMenuItem},
                         (sender, args) => ToggleLogDrawer(), () => true);
        }

        private void ConfigureWindowCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 transfersToolStripMenuItem,
                                 transfersToolStripButton
                             }, new[] {transfersMainMenuItem},
                         (sender, args) => ShowTransfers(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 activitiyToolStripMenuItem,
                             }, new[] {activityMainMenuItem},
                         (sender, args) => ((Form) ActivityController.Instance.View).Show(), () => false);
            Commands.Add(new ToolStripItem[]
                             {
                                 minimizeToolStripMenuItem,
                             }, new[] {minimizeMainMenuItem},
                         (sender, args) => WindowState = FormWindowState.Minimized, () => true);
        }

        private void ConfigureEditCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 cutToolStripMenuItem
                             }, new[] {cutMainMenuItem},
                         (sender, args) => Cut(), () => ValidateCut());
            Commands.Add(new ToolStripItem[]
                             {
                                 copyToolStripMenuItem
                             }, new[] {copyMainMenuItem},
                         (sender, args) => Copy(), () => ValidateCopy());
            Commands.Add(new ToolStripItem[]
                             {
                                 pasteToolStripMenuItem
                             }, new[] {pasteMainMenuItem},
                         (sender, args) => Paste(), () => ValidatePaste());
            Commands.Add(new ToolStripItem[]
                             {
                                 selectAllToolStripMenuItem
                             }, new[] {selectAllMainMenuItem},
                         (o, eventArgs) => { }, () => true); // Tree component handles the selectAll command
            Commands.Add(new ToolStripItem[]
                             {
                                 preferencesToolStripMenuItem
                             }, new[] {preferencesMainMenuItem},
                         (o, eventArgs) => ShowPreferences(), () => true);
        }

        private void ConfigureShortcut(ToolStripMenuItem toolstripItem, MenuItem menuItem, Keys keys)
        {
            ConfigureShortcut(toolstripItem, menuItem, keys, null);
        }

        private void ConfigureShortcut(ToolStripMenuItem toolstripItem, MenuItem menuItem, Keys keys,
                                       String shortCutText)
        {
            toolstripItem.ShortcutKeys = keys;
            if (null != menuItem)
            {
                SetShortcutText(menuItem, toolstripItem, shortCutText);
            }
        }

        private void ConfigureShortcuts()
        {
            #region Shortcuts - Files

            ConfigureShortcut(newBrowserToolStripMenuItem, newBrowserMainMenuItem, Keys.Control | Keys.N);
            ConfigureShortcut(openConnectionToolStripMenuItem, openConnectionMainMenuItem, Keys.Control | Keys.O);
            ConfigureShortcut(newDownloadToolStripMenuItem, newDownloadMainMenuItem, Keys.Control | Keys.Alt | Keys.Down);
            ConfigureShortcut(newFolderToolStripMenuItem, newFolderMainMenuItem, Keys.Control | Keys.Shift | Keys.N);
            ConfigureShortcut(newFileToolStripMenuItem, newFileMainMenuItem, Keys.Control | Keys.Shift | Keys.F);
            ConfigureShortcut(duplicateFileToolStripMenuItem, duplicateMainMenuItem, Keys.Control | Keys.D);
            ConfigureShortcut(editWithToolStripMenuItem, null, Keys.Control | Keys.K);
            ConfigureShortcut(infoToolStripMenuItem, infoMainMenuItem, Keys.Alt | Keys.Enter);
            ConfigureShortcut(downloadToolStripMenuItem, downloadMainMenuItem, Keys.Alt | Keys.Down);
            ConfigureShortcut(downloadAsToolStripMenuItem, downloadAsMainMenuItem, Keys.Alt | Keys.Shift | Keys.Down);
            ConfigureShortcut(uploadToolStripMenuItem, uploadMainMenuItem, Keys.Alt | Keys.Up);
            ConfigureShortcut(deleteToolStripMenuItem, deleteMainMenuItem, Keys.None,
                              ShortcutToText(Keys.Delete, String.Empty));
            ConfigureShortcut(exitToolStripMenuItem, exitMainMenuItem, Keys.Control | Keys.Q);

            #endregion

            #region Shortcuts - Edit

            ConfigureShortcut(cutToolStripMenuItem, cutMainMenuItem, Keys.Control | Keys.X);
            ConfigureShortcut(copyToolStripMenuItem, copyMainMenuItem, Keys.Control | Keys.C);
            ConfigureShortcut(copyURLToolStripMenuItem, copyUrlMainMenuItem, Keys.Control | Keys.Shift | Keys.C);
            ConfigureShortcut(pasteToolStripMenuItem, pasteMainMenuItem, Keys.Control | Keys.V);
            ConfigureShortcut(selectAllToolStripMenuItem, selectAllMainMenuItem, Keys.None,
                              ShortcutToText(Keys.Control | Keys.A, String.Empty));
            ConfigureShortcut(preferencesToolStripMenuItem, preferencesMainMenuItem, Keys.Control | Keys.Oemcomma,
                              "Ctrl+,");

            #endregion

            #region Shortcuts - View

            ConfigureShortcut(showHiddenFilesToolStripMenuItem, showHiddenFilesMainMenuItem,
                              Keys.Control | Keys.Shift | Keys.R);
            ConfigureShortcut(toggleLogDrawerToolStripMenuItem, toggleLogDrawerMainMenuItem, Keys.Control | Keys.L);

            #endregion

            #region Shortcuts - Go

            ConfigureShortcut(refreshToolStripMenuItem, refreshMainMenuItem, Keys.Control | Keys.R);
            ConfigureShortcut(gotoFolderToolStripMenuItem, goToFolderMainMenuItem, Keys.Control | Keys.G);
            ConfigureShortcut(backToolStripMenuItem, backMainMenuItem, Keys.Control | Keys.Left);
            ConfigureShortcut(forwardToolStripMenuItem, forwardMainMenuItem, Keys.Control | Keys.Right);
            ConfigureShortcut(enclosingFolderToolStripMenuItem, enclosingFolderMainMenuItem, Keys.Control | Keys.Up);
            ConfigureShortcut(insideToolStripMenuItem, insideMainMenuItem, Keys.Control | Keys.Down);
            ConfigureShortcut(searchToolStripMenuItem, searchMainMenuItem, Keys.Control | Keys.F);
            ConfigureShortcut(sendCommandToolStripMenuItem, sendCommandMainMenuItem, Keys.Control | Keys.Alt | Keys.C);
            ConfigureShortcut(stopToolStripMenuItem, stopMainMenuItem, Keys.Control | Keys.OemPeriod, "Ctrl+.");
            ConfigureShortcut(disconnectToolStripMenuItem, disconnectMainMenuItem, Keys.Control | Keys.Y);

            #endregion

            #region Shortcurs - Bookmark

            ConfigureShortcut(viewBookmarksToolStripMenuItem, toggleBookmarksMainMenuItem, Keys.Control | Keys.B);
            ConfigureShortcut(newBookmarkToolStripMenuItem, newBookmarkMainMenuItem, Keys.Control | Keys.Shift | Keys.B);
            ConfigureShortcut(editBookmarkToolStripMenuItem, editBookmarkMainMenuItem, Keys.Control | Keys.E);

            #endregion

            #region Shortcuts - Window

            ConfigureShortcut(minimizeToolStripMenuItem, minimizeMainMenuItem, Keys.Control | Keys.M);
            ConfigureShortcut(activitiyToolStripMenuItem, activityMainMenuItem, Keys.Control | Keys.D0);
            ConfigureShortcut(transfersToolStripMenuItem, transfersMainMenuItem, Keys.Control | Keys.T);

            #endregion

            #region Shortcuts - Browser Context

            //All doubly assigned shortcuts are falsely active in all child forms
            EventHandler activated =
                delegate
                    {
                        ConfigureShortcut(refreshContextToolStripMenuItem, refreshBrowserContextMenuItem,
                                          refreshToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(infoContextToolStripMenuItem, infoBrowserContextMenuItem,
                                          infoToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(editContextToolStripMenuItem, editBrowserContextMenuItem,
                                          editWithToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(downloadContextToolStripMenuItem, downloadBrowserContextMenuItem,
                                          downloadToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(downloadAsContextToolStripMenuItem, downloadAsBrowserContextMenuItem,
                                          downloadAsToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(deleteContextToolStripMenuItem, deleteBrowserContextMenuItem,
                                          deleteToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(duplicateFileContextToolStripMenuItem,
                                          duplicateFileBrowserContextMenuItem,
                                          duplicateFileToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(uploadContextToolStripMenuItem, uploadBrowserContextMenuItem,
                                          uploadToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(newFolderContextToolStripMenuItem, newFolderBrowserContextMenuItem,
                                          newFolderToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(newFileContextToolStripMenuItem, newFileBrowserContextMenuItem,
                                          newFileToolStripMenuItem.ShortcutKeys);
                        ConfigureShortcut(newBookmarkContextToolStripMenuItem,
                                          newBookmarkBrowserContextMenuItem,
                                          newBookmarkToolStripMenuItem.ShortcutKeys);
                    };
            Activated += activated;
            activated(this, EventArgs.Empty);

            Deactivate += delegate
                              {
                                  foreach (ToolStripItem item in contextMenuStrip.Items)
                                  {
                                      if (item is ToolStripMenuItem)
                                      {
                                          (item as ToolStripMenuItem).ShortcutKeys = Keys.None;
                                      }
                                  }
                              };

            #endregion

            #region Shortcuts - Bookmarks Context

            //All doubly assigned shortcuts are falsely active in all child forms
            activated = delegate
                            {
                                ConfigureShortcut(connectBookmarkContextToolStripMenuItem,
                                                  connectBookmarkContextMenuItem, Keys.None,
                                                  ShortcutToText(Keys.Enter, String.Empty));
                                ConfigureShortcut(newBookmarkContextToolStripMenuItem1, newBookmarkContextMenuItem,
                                                  newBookmarkToolStripMenuItem.ShortcutKeys);
                                ConfigureShortcut(editBookmarkContextToolStripMenuItem1, editBookmarkContextMenuItem,
                                                  editBookmarkToolStripMenuItem.ShortcutKeys);
                                //todo deleteBookmarkContextToolStripMenuItem1.ShortcutKeys =
                            };
            Activated += activated;
            activated(this, EventArgs.Empty);

            Deactivate += delegate
                              {
                                  foreach (ToolStripItem item in bookmarkContextMenuStrip.Items)
                                  {
                                      if (item is ToolStripMenuItem)
                                      {
                                          (item as ToolStripMenuItem).ShortcutKeys = Keys.None;
                                      }
                                  }
                              };

            #endregion
        }

        private void OnArchiveMenuItemOnPopup(object sender, EventArgs eventArgs)
        {
            MenuItem mainItem = sender as MenuItem;
            mainItem.MenuItems.Clear();
            foreach (string archive in GetArchives())
            {
                MenuItem item = mainItem.MenuItems.Add(archive);
                string archiveName = archive;
                item.Click += delegate { CreateArchive(this, new CreateArchiveEventArgs(archiveName)); };
            }
        }

        /// <summary>
        /// Setup event and validation handler for the file menu items
        /// </summary>
        private void ConfigureFileCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 newBrowserToolStripMenuItem
                             },
                         new[] {newBrowserMainMenuItem},
                         (sender, args) => NewBrowser(this, new NewBrowserEventArgs(false)), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 newBrowserContextToolStripMenuItem
                             },
                         new[] {newBrowserBrowserContextMenuItem},
                         (sender, args) => NewBrowser(this, new NewBrowserEventArgs(true)), () => ValidateNewBrowser());
            Commands.Add(new ToolStripItem[]
                             {
                                 openConnectionToolStripMenuItem,
                                 openConnectionToolStripButton
                             },
                         new[] {openConnectionMainMenuItem},
                         (sender, args) => OpenConnection(), () => ValidateOpenConnection());
            Commands.Add(new ToolStripItem[]
                             {
                                 newDownloadToolStripMenuItem
                             },
                         new[] {newDownloadMainMenuItem},
                         (sender, args) => NewDownload(), () => ValidateNewDownload());
            Commands.Add(new ToolStripItem[]
                             {
                                 newFolderToolStripMenuItem,
                                 newFolderContextToolStripMenuItem,
                                 newFolderToolStripButton
                             }, new[] {newFolderMainMenuItem, newFolderBrowserContextMenuItem},
                         (sender, args) => NewFolder(), () => ValidateNewFolder());
            Commands.Add(new ToolStripItem[]
                             {
                                 newFileToolStripMenuItem,
                                 newFileContextToolStripMenuItem
                             }, new[] {newFileMainMenuItem, newFileBrowserContextMenuItem},
                         (sender, args) => NewFile(), () => ValidateNewFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 renameFileToolStripMenuItem,
                                 renameContextToolStripMenuItem
                             }, new[] {renameMainMenuItem, renameBrowserContextMenuItem},
                         (o, eventArgs) => browser.SelectedItem.BeginEdit(), () => ValidateRenameFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 duplicateFileToolStripMenuItem,
                                 duplicateFileContextToolStripMenuItem
                             }, new[] {duplicateMainMenuItem, duplicateFileBrowserContextMenuItem},
                         (sender, args) => DuplicateFile(), () => ValidateDuplicateFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 openWebURLToolStripMenuItem,
                                 openURLContextToolStripMenuItem,
                                 openInBrowserToolStripButton
                             }, new MenuItem[] {},
                         (sender, args) => OpenUrl(), () => ValidateOpenWebUrl());
            Commands.Add(new ToolStripItem[]
                             {
                                 editWithToolStripMenuItem,
                                 editContextToolStripMenuItem,
                                 editToolStripSplitButton
                             }, new[] {editMainMenuItem, editBrowserContextMenuItem},
                         (sender, args) =>
                             {
                                 if (sender == editToolStripSplitButton &&
                                     editToolStripSplitButton.DropDownButtonPressed)
                                 {
                                     return;
                                 }
                                 EditEvent(null);
                             }, () => ValidateEditWith());
            Commands.Add(new ToolStripItem[]
                             {
                                 infoToolStripMenuItem,
                                 infoToolStripButton,
                                 infoContextToolStripMenuItem
                             }, new[] {infoMainMenuItem, infoBrowserContextMenuItem},
                         (sender, args) => ShowInspector(), () => ValidateShowInspector());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadToolStripMenuItem,
                                 downloadContextToolStripMenuItem
                             }, new[] {downloadMainMenuItem, downloadBrowserContextMenuItem},
                         (sender, args) => Download(), () => ValidateDownload());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadAsToolStripMenuItem,
                                 downloadAsContextToolStripMenuItem
                             }, new[] {downloadAsMainMenuItem, downloadAsBrowserContextMenuItem},
                         (sender, args) => DownloadAs(), () => ValidateDownloadAs());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadToToolStripMenuItem,
                                 downloadToContextToolStripMenuItem,
                                 downloadToolStripButton
                             }, new[] {downloadToMainMenuItem, downloadToBrowserContextMenuItem},
                         (sender, args) => DownloadTo(), () => ValidateDownloadTo());
            Commands.Add(new ToolStripItem[]
                             {
                                 uploadToolStripMenuItem,
                                 uploadContextToolStripMenuItem,
                                 uploadToolStripButton
                             }, new[] {uploadMainMenuItem, uploadBrowserContextMenuItem},
                         (sender, args) => Upload(), () => ValidateUpload());
            Commands.Add(new ToolStripItem[]
                             {
                                 synchronizeToolStripMenuItem,
                                 synchronizeContextToolStripMenuItem
                             }, new[] {synchronizeMainMenuItem, synchronizeBrowserContextMenuItem},
                         (sender, args) => Synchronize(), () => ValidateSynchronize());
            Commands.Add(new ToolStripItem[]
                             {
                                 deleteToolStripMenuItem,
                                 deleteContextToolStripMenuItem,
                                 deleteToolStripButton
                             }, new[] {deleteMainMenuItem, deleteBrowserContextMenuItem},
                         (sender, args) => Delete(), () => ValidateDelete());
            Commands.Add(new ToolStripItem[]
                             {
                                 revertToolStripMenuItem,
                                 revertContxtStripMenuItem
                             }, new[] {revertMainMenuItem, revertBrowserContextMenuItem}, (sender, args) => RevertFile(),
                         () => ValidateRevertFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 createArchiveToolStripMenuItem,
                                 createArchiveContextToolStripMenuItem
                             }, new[] {createArchiveMainMenuItem, createArchiveBrowserContextMenuItem},
                         (sender, args) => { }, () => ValidateCreateArchive());
            Commands.Add(new ToolStripItem[]
                             {
                                 expandArchiveToolStripMenuItem,
                                 expandArchiveContextToolStripMenuItem
                             }, new[] {expandArchiveMainMenuItem, expandArchiveBrowserContextMnuItem},
                         (sender, args) => ExpandArchive(), () => ValidateExpandArchive());
            Commands.Add(new ToolStripItem[]
                             {
                                 exitToolStripMenuItem
                             }, new[] {exitMainMenuItem},
                         (sender, args) => Exit(), () => true);

            vistaMenu1.SetImage(openConnectionMainMenuItem, IconCache.Instance.IconForName("connect", 16));
            vistaMenu1.SetImage(infoMainMenuItem, IconCache.Instance.IconForName("info", 16));
            vistaMenu1.SetImage(infoBrowserContextMenuItem, IconCache.Instance.IconForName("info", 16));
            infoContextToolStripMenuItem.Image = IconCache.Instance.IconForName("info", 16);
            vistaMenu1.SetImage(editMainMenuItem, IconCache.Instance.IconForName("pencil", 16));
            vistaMenu1.SetImage(editBrowserContextMenuItem, IconCache.Instance.IconForName("pencil", 16));
            editContextToolStripMenuItem.Image = IconCache.Instance.IconForName("pencil", 16);
            vistaMenu1.SetImage(deleteMainMenuItem, IconCache.Instance.IconForName("delete", 16));
            vistaMenu1.SetImage(deleteBrowserContextMenuItem, IconCache.Instance.IconForName("delete", 16));
            deleteContextToolStripMenuItem.Image = IconCache.Instance.IconForName("delete", 16);
            vistaMenu1.SetImage(newFolderMainMenuItem, IconCache.Instance.IconForName("newfolder", 16));
            vistaMenu1.SetImage(newFolderBrowserContextMenuItem, IconCache.Instance.IconForName("newfolder", 16));
            newFolderContextToolStripMenuItem.Image = IconCache.Instance.IconForName("newfolder", 16);
            vistaMenu1.SetImage(downloadMainMenuItem, IconCache.Instance.IconForName("download", 16));
            vistaMenu1.SetImage(downloadBrowserContextMenuItem, IconCache.Instance.IconForName("download", 16));
            downloadContextToolStripMenuItem.Image = IconCache.Instance.IconForName("download", 16);
            vistaMenu1.SetImage(uploadMainMenuItem, IconCache.Instance.IconForName("upload", 16));
            vistaMenu1.SetImage(uploadBrowserContextMenuItem, IconCache.Instance.IconForName("upload", 16));
            uploadContextToolStripMenuItem.Image = IconCache.Instance.IconForName("upload", 16);
            vistaMenu1.SetImage(synchronizeMainMenuItem, IconCache.Instance.IconForName("sync", 16));
            vistaMenu1.SetImage(synchronizeBrowserContextMenuItem, IconCache.Instance.IconForName("sync", 16));
            synchronizeContextToolStripMenuItem.Image = IconCache.Instance.IconForName("sync", 16);
        }

        private void SaveUiSettings()
        {
            if (browser.Objects != null)
            {
                Log.debug("Saving browser state");
                PersistenceHandler.Set("Tree.State", browser.SaveState());
            }
            PersistenceHandler.Set("Splitter.Distance", splitContainer.SplitterDistance);
        }

        private void browser_DoubleClick(object sender, EventArgs e)
        {
            BrowserDoubleClicked();
        }

        private void toolStripQuickConnect_SelectionChangeCommited(object sender, EventArgs e)
        {
            QuickConnect();
        }

        private void toolStripQuickConnect_KeyDown(object sender, KeyEventArgs e)
        {
            // KeyUp doesnt' work in conjuction with AcceptButton and modal forms because of
            // http://connect.microsoft.com/VisualStudio/feedback/ViewFeedback.aspx?FeedbackID=93673
            // http://social.msdn.microsoft.com/Forums/de-AT/visualcsharpde/thread/0557663a-6fb5-498f-8919-06446169296d
            if (e.KeyCode == Keys.Enter)
            {
                QuickConnect();
            }
        }

        private void browser_SelectionChanged(object sender, EventArgs e)
        {
            BrowserSelectionChanged();
        }

        private void browser_AfterLabelEdit(object sender, LabelEditEventArgs e)
        {
            bool c = RenameFile(((TreePathReference) browser.GetModelObject(e.Item)).Unique, e.Label);
            e.CancelEdit = c;
        }

        private void browser_KeyDown(object sender, KeyEventArgs e)
        {
            if (browser.SelectedObjects.Count > 0 && e.KeyCode == Keys.F2)
            {
                browser.GetItem(browser.SelectedIndices[0]).BeginEdit();
            }
            if (e.KeyCode == Keys.Delete)
            {
                Delete();
            }
        }

        private void transcriptBox_KeyDown(object sender, KeyEventArgs e)
        {
            // the log drawer should be closeable from inside the textbox
            if (e.KeyData == toggleLogDrawerToolStripMenuItem.ShortcutKeys)
            {
                ToggleLogDrawer();
            }
            // handle some basic shortcuts
            if ((ModifierKeys & Keys.Control) == Keys.Control)
            {
                if (e.KeyCode == Keys.A)
                {
                    transcriptBox.SelectAll();
                }
                if (e.KeyCode == Keys.C)
                {
                    transcriptBox.Copy();
                }
            }
        }

        private void bookmarkListView_DoubleClick(object sender, EventArgs e)
        {
            ConnectBookmark(this, new ConnectBookmarkArgs(bookmarkListView.SelectedObject as Host));
        }

        private void bookmarkListView_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                ConnectBookmark(this, new ConnectBookmarkArgs(bookmarkListView.SelectedObject as Host));
            }
            if (e.KeyCode == Keys.Delete)
            {
                DeleteBookmark();
            }
        }

        private void browserCheckBox_Click(object sender, EventArgs e)
        {
            ChangeBrowserView(sender, new ChangeBrowserViewArgs(BrowserView.File));
        }

        private void bookmarkCheckBox_Click(object sender, EventArgs e)
        {
            ChangeBrowserView(sender, new ChangeBrowserViewArgs(BrowserView.Bookmark));
        }

        private void historyCheckBox_Click(object sender, EventArgs e)
        {
            ChangeBrowserView(sender, new ChangeBrowserViewArgs(BrowserView.History));
        }

        private void bonjourCheckBox_Click(object sender, EventArgs e)
        {
            ChangeBrowserView(sender, new ChangeBrowserViewArgs(BrowserView.Bonjour));
        }

        private void pathComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            PathSelectionChanged();
        }

        private void toolbarContextMenu_Closing(object sender, ToolStripDropDownClosingEventArgs e)
        {
            e.Cancel = (
                           e.CloseReason == ToolStripDropDownCloseReason.ItemClicked &&
                           _lastMenuItemClicked != null);
        }

        private void toolbarContextMenu_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            if (e.ClickedItem is ToolStripMenuItem)
            {
                _lastMenuItemClicked = (ToolStripMenuItem) e.ClickedItem;
            }
        }

        private void columnContextMenu_Opening(object sender, CancelEventArgs e)
        {
            //we need to remove the ItemClicked (prevent multi-registration) event as it
            //will be registered again in MakeColumnSelectMenu
            RemoveItemClickedEvent(columnContextMenu);
            columnContextMenu.Items.Clear();
            browser.MakeColumnSelectMenu(
                columnContextMenu);
        }

        private static void RemoveItemClickedEvent(ToolStrip b)
        {
            FieldInfo f1 = typeof (ToolStrip).GetField("EventItemClicked",
                                                       BindingFlags.Static | BindingFlags.NonPublic);
            object obj = f1.GetValue(b);
            PropertyInfo pi = b.GetType().GetProperty("Events",
                                                      BindingFlags.NonPublic | BindingFlags.Instance);
            EventHandlerList list = (EventHandlerList) pi.GetValue(b, null);
            list.RemoveHandler(obj, list[obj]);
        }

        private void textEncodingMenuStrip_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            EncodingChanged(sender, new EncodingChangedArgs(e.ClickedItem.Text));
        }

        /// <summary>
        /// Bookmark menu handling with some optimization due to the expensive DropDownItems.Add method
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void bookmarkToolStripMenuItem_DropDownOpening(object sender, EventArgs e)
        {
            List<Host> bookmarks = GetBookmarks();

            //all fix items
            List<ToolStripItem> fix = new List<ToolStripItem>();
            foreach (ToolStripItem item in bookmarkToolStripMenuItem.DropDownItems)
            {
                if (!(item.Tag is Host))
                {
                    fix.Add(item);
                }
            }

            //clear the menu and add again the fix items
            bookmarkToolStripMenuItem.DropDownItems.Clear();
            bookmarkToolStripMenuItem.DropDownItems.AddRange(fix.ToArray());

            ImageList.ImageCollection icons = IconCache.Instance.GetProtocolIcons().Images;

            List<ToolStripItem> items = new List<ToolStripItem>();
            foreach (Host bookmark in bookmarks)
            {
                ToolStripItem item = new ToolStripMenuItem(bookmark.getNickname(),
                                                           icons[bookmark.getProtocol().getIdentifier()]);
                item.Tag = bookmark;
                item.Click += (o, args) => ConnectBookmark(this, new ConnectBookmarkArgs(item.Tag as Host));
                items.Add(item);
            }
            bookmarkToolStripMenuItem.DropDownItems.AddRange(items.ToArray());
        }

        private void historyMenuStrip_Opening(object sender, CancelEventArgs e)
        {
            List<Host> history = GetHistory();

            historyMenuStrip.Items.Clear();
            if (history.Count > 0)
            {
                ImageList.ImageCollection icons = IconCache.Instance.GetProtocolIcons().Images;

                List<ToolStripItem> items = new List<ToolStripItem>();
                foreach (Host h in history)
                {
                    ToolStripItem item = new ToolStripMenuItem(h.getNickname(),
                                                               icons[h.getProtocol().getIdentifier()]);
                    item.Tag = h;
                    item.Click += (o, args) => ConnectBookmark(this, new ConnectBookmarkArgs(item.Tag as Host));
                    items.Add(item);
                }

                // separator and clear item
                items.Add(new ToolStripSeparator());
                ToolStripItem clear = new ToolStripMenuItem(Locale.localizedString("Clear Menu"));
                clear.Click += (o, args) => ClearHistory();
                items.Add(clear);

                // add all added items
                historyMenuStrip.Items.AddRange(items.ToArray());
            }
            else
            {
                ToolStripItem noitem =
                    new ToolStripMenuItem(Locale.localizedString("No recently connected servers available"));
                noitem.Enabled = false;
                historyMenuStrip.Items.Add(noitem);
            }
        }

        private void securityToolStripStatusLabel_Click(object sender, EventArgs e)
        {
            ShowCertificate();
        }

        private void searchTextBox_TextChanged(object sender, EventArgs e)
        {
            SearchFieldChanged();
        }

        private void customizeToolbarMenuItem_Popup(object sender, EventArgs e)
        {
            foreach (MenuItem item in customizeToolbarMainMenuItem.MenuItems)
            {
                if (null != item.Tag)
                {
                    item.Checked = ((MenuItem) item.Tag).Checked;
                }
            }
        }

        private void columnMenuItem_Popup(object sender, EventArgs e)
        {
            //we need to remove the ItemClicked (prevent multi-registration) event as it
            //will be registered again in MakeColumnSelectMenu
            RemoveItemClickedEvent(columnContextMenu);
            columnContextMenu.Items.Clear();
            browser.MakeColumnSelectMenu(
                columnContextMenu);
            columnMainMenuItem.MenuItems.Clear();
            foreach (ToolStripMenuItem item in columnContextMenu.Items)
            {
                ToolStripMenuItem item1 = item;
                MenuItem nItem = new MenuItem(Locale.localizedString(item.Text), delegate { item1.PerformClick(); });
                //forward click event
                nItem.Checked = item.Checked;
                columnMainMenuItem.MenuItems.Add(nItem);
            }
        }

        private void browser_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar == (char) Keys.Enter)
            {
                BrowserDoubleClicked();
                e.Handled = true;
            }
        }

        private class BookmarkMenuCollectionListener : CollectionListener
        {
            private readonly BrowserForm _form;
            private readonly ImageList.ImageCollection _icons = IconCache.Instance.GetProtocolIcons().Images;
            private int _bookmarkStartPosition;

            public BookmarkMenuCollectionListener(BrowserForm f)
            {
                _form = f;
            }

            public void collectionLoaded()
            {
                _form.Invoke(new AsyncController.AsyncDelegate(BuildMenuItems));
            }

            public void collectionItemAdded(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       int pos =
                                                                           BookmarkCollection.defaultCollection().
                                                                               indexOf(obj);
                                                                       Host h = (Host) obj;
                                                                       MenuItem i = new MenuItem(h.getNickname());
                                                                       i.Tag = h;
                                                                       i.Click +=
                                                                           (o, args) =>
                                                                           _form.ConnectBookmark(this,
                                                                                                 new ConnectBookmarkArgs
                                                                                                     (h));
                                                                       _form.menuItem64.MenuItems.Add(
                                                                           _bookmarkStartPosition + pos, i);
                                                                       _form.vistaMenu1.SetImage(i,
                                                                                                 _icons[
                                                                                                     h.getProtocol()
                                                                                                         .
                                                                                                         getIdentifier
                                                                                                         ()]);
                                                                   }));
            }

            public void collectionItemRemoved(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       int pos =
                                                                           BookmarkCollection.defaultCollection().
                                                                               indexOf(obj);
                                                                       Host h = (Host) obj;
                                                                       MenuItem i = new MenuItem(h.getNickname());
                                                                       i.Tag = h;
                                                                       i.Click +=
                                                                           (o, args) =>
                                                                           _form.ConnectBookmark(this,
                                                                                                 new ConnectBookmarkArgs
                                                                                                     (h));
                                                                       _form.menuItem64.MenuItems.Add(
                                                                           _bookmarkStartPosition + pos, i);
                                                                       _form.vistaMenu1.SetImage(i,
                                                                                                 _icons[
                                                                                                     h.getProtocol().
                                                                                                         getIdentifier()
                                                                                                     ]);


                                                                       foreach (
                                                                           MenuItem item in _form.menuItem64.MenuItems)
                                                                       {
                                                                           if (obj.Equals(item.Tag))
                                                                           {
                                                                               _form.menuItem64.MenuItems.Remove(item);
                                                                               break;
                                                                           }
                                                                       }
                                                                   }));
            }

            public void collectionItemChanged(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       foreach (
                                                                           MenuItem item in _form.menuItem64.MenuItems)
                                                                       {
                                                                           if (obj.Equals(item.Tag))
                                                                           {
                                                                               Host h = (Host) obj;
                                                                               item.Text = h.getNickname();
                                                                               _form.vistaMenu1.SetImage(item,
                                                                                                         _icons[
                                                                                                             h.
                                                                                                                 getProtocol
                                                                                                                 ().
                                                                                                                 getIdentifier
                                                                                                                 ()]);
                                                                               break;
                                                                           }
                                                                       }
                                                                   }));
            }

            public void BuildMenuItems()
            {
                List<MenuItem> fix = new List<MenuItem>();
                foreach (MenuItem item in _form.menuItem64.MenuItems)
                {
                    if (!(item.Tag is Host))
                    {
                        fix.Add(item);
                    }
                }
                _bookmarkStartPosition = fix.Count;
                List<MenuItem> items = new List<MenuItem>();
                foreach (Host bookmark in BookmarkCollection.defaultCollection())
                {
                    MenuItem item = new MenuItem(bookmark.getNickname());
                    item.Tag = bookmark;
                    item.Click += (o, args) => _form.ConnectBookmark(this, new ConnectBookmarkArgs(item.Tag as Host));
                    items.Add(item);
                }

                using (MainMenu m1 = _form.mainMenu.CloneMenu())
                {
                    _form.SuspendLayout();
                    _form.Menu = m1;
                    //clear the menu and add again the fix items
                    _form.menuItem64.MenuItems.Clear();
                    _form.menuItem64.MenuItems.AddRange(fix.ToArray());
                    _form.menuItem64.MenuItems.AddRange(items.ToArray());
                    foreach (MenuItem item in items)
                    {
                        if (null != item.Tag)
                            _form.vistaMenu1.SetImage(item, _icons[((Host) item.Tag).getProtocol().getIdentifier()]);
                    }
                    _form.Menu = _form.mainMenu;
                    _form.ResumeLayout();
                }
            }
        }

        private class BrowserDragSource : SimpleDragSource
        {
            private readonly BrowserForm _form;

            public BrowserDragSource(BrowserForm _form)
            {
                this._form = _form;
            }

            public override DragDropEffects GetAllowedEffects(object data)
            {
                return DragDropEffects.Copy | DragDropEffects.Move;
            }

            public override object StartDrag(ObjectListView olv, MouseButtons button, OLVListItem item)
            {
                DataObject t = _form.BrowserDrag(olv);
                OLVDataObject data = new OLVDataObject(olv);
                data.SetData(DataFormats.FileDrop, new[] {t.GetFileDropList()[0]});
                return data;
            }

            public override void EndDrag(object dragObject, DragDropEffects effect)
            {
                base.EndDrag(dragObject, effect);
                _form.BrowserEndDrag(dragObject as DataObject);
            }
        }

        private class ExpandingBrowserDropSink : SimpleDropSink
        {
            private readonly BrowserForm _form;
            private readonly Timer _timer = new Timer();
            private object _currentDropTarget;

            public ExpandingBrowserDropSink(BrowserForm form)
            {
                _form = form;
                _timer.Tick += delegate
                                   {
                                       if (null != _currentDropTarget)
                                       {
                                           ((TreeListView) ListView).Expand(_currentDropTarget);
                                       }
                                       _timer.Stop();
                                   };
                CanDropOnBackground = true;
                FeedbackColor = Color.LightBlue;
                AcceptExternal = true;
            }

            /// <summary>
            /// Gets or sets whether this sink allows model objects to be dragged from other lists
            /// </summary>
            public bool AcceptExternal { get; set; }

            /// <summary>
            /// Draw the feedback that shows that the background is the target
            /// </summary>
            /// <param name="g"></param>
            /// <param name="bounds"></param>
            protected override void DrawFeedbackBackgroundTarget(Graphics g, Rectangle bounds)
            {
                float penWidth = 4.0f;
                Rectangle r = bounds;
                r.Inflate((int) -penWidth/2, (int) -penWidth/2);
                using (Pen p = new Pen(Color.FromArgb(128, FeedbackColor), penWidth))
                {
                    using (GraphicsPath path = GetRoundedRect(r, 30.0f))
                    {
                        g.DrawPath(p, path);
                    }
                }
            }

            protected override void OnCanDrop(OlvDropEventArgs args)
            {
                base.OnCanDrop(args);
                _form.BrowserCanDrop(args);
            }

            public override void Enter(DragEventArgs e)
            {
                base.Enter(e);
                if (!(e.Data is OLVDataObject))
                {
                    DropTargetHelper.DragEnter(_form.browser, e.Data, new Point(e.X, e.Y), e.Effect);
                }
            }

            public override void Leave()
            {
                base.Leave();
                DropTargetHelper.DragLeave(_form.browser);
            }

            // We do not currently support drags between browsers

            protected override void OnModelCanDrop(ModelDropEventArgs args)
            {
                base.OnModelCanDrop(args);

                if (args.Handled)
                    return;

                args.Effect = CalculateStandardDropActionFromKeys();

                // Don't allow drops from other list, if that's what's configured
                if (!AcceptExternal)
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                }
                else
                {
                    _form.BrowserModelCanDrop(args);
                }
                args.Handled = true;
            }

            protected override void OnDropped(OlvDropEventArgs args)
            {
                DropTargetHelper.Drop(args.DataObject as DataObject,
                                      new Point(args.MouseLocation.X, args.MouseLocation.Y), DragDropEffects.None);
                _form.BrowserDropped(args);
            }

            protected override void OnModelDropped(ModelDropEventArgs args)
            {
                base.OnModelDropped(args);
                if (!args.Handled)
                {
                    _form.BrowserModelDropped(args);
                }
                args.Handled = true;
            }

            public override void Over(DragEventArgs e)
            {
                base.Over(e);

                if (!(e.Data is OLVDataObject))
                {
                    DropTargetHelper.DragOver(new Point(e.X, e.Y), e.Effect);
                }

                bool autoExpand = Preferences.instance().getBoolean("browser.view.autoexpand");
                bool useDelay = Preferences.instance().getBoolean("browser.view.autoexpand.useDelay");
                int delay = Convert.ToInt32(Preferences.instance().getFloat("browser.view.autoexpand.delay"));

                if (autoExpand)
                {
                    if (null != DropTargetItem)
                    {
                        if (_currentDropTarget != DropTargetItem.RowObject)
                        {
                            _timer.Stop();
                            _currentDropTarget = DropTargetItem.RowObject;
                            _timer.Interval = useDelay ? delay*1000 : 0;
                            _timer.Start();
                        }
                    }
                    else
                    {
                        _timer.Stop();
                        _currentDropTarget = null;
                    }
                }
            }
        }

        /// <summary>
        /// Wraps a PathFilter class for the use with the ObjectListView component
        /// </summary>
        private class FilterWrapper : IModelFilter
        {
            private readonly PathFilter _del;

            public FilterWrapper(PathFilter del)
            {
                _del = del;
            }

            public bool Filter(object modelObject)
            {
                return _del.accept(((TreePathReference) modelObject).Unique);
            }
        }

        private class GradientStyleRenderer : ToolStripProfessionalRenderer
        {
            protected override void OnRenderButtonBackground(ToolStripItemRenderEventArgs e)
            {
                if (e.Item is ToolStripButton)
                {
                    ToolStripButton button = (ToolStripButton) e.Item;
                    if (button.Pressed || button.Checked || button.Selected)
                    {
                        Rectangle bounds = new Rectangle(0, 0, e.Item.Width - 1, e.Item.Height - 1);
                        LinearGradientBrush fillBrush = new LinearGradientBrush(bounds,
                                                                                Color.FromArgb(64, Color.LightBlue),
                                                                                Color.FromArgb(64, Color.DodgerBlue),
                                                                                LinearGradientMode.Vertical);


                        e.Graphics.FillRectangle(fillBrush, bounds);
                        //e.Graphics.FillRectangle(solidBrush, bounds);
                        e.Graphics.DrawRectangle(new Pen(ProfessionalColors.ButtonSelectedHighlightBorder, 1), bounds);
                    }
                    else
                    {
                        base.OnRenderButtonBackground(e);
                        return;
                    }
                }
                else
                {
                    base.OnRenderButtonBackground(e);
                }
            }
        }

        private class HostDragSource : SimpleDragSource
        {
            private readonly BrowserForm _form;

            public HostDragSource(BrowserForm _form)
            {
                this._form = _form;
            }

            public override DragDropEffects GetAllowedEffects(object data)
            {
                return DragDropEffects.Copy | DragDropEffects.Move;
            }

            public override object StartDrag(ObjectListView olv, MouseButtons button, OLVListItem item)
            {
                DataObject t = _form.HostDrag(olv);
                OLVDataObject data = new OLVDataObject(olv);
                data.SetData(DataFormats.FileDrop, new[] {t.GetFileDropList()[0]});
                return data;
            }

            public override void EndDrag(object dragObject, DragDropEffects effect)
            {
                base.EndDrag(dragObject, effect);
                _form.HostEndDrag(dragObject as DataObject);
            }
        }

        private class HostDropSink : SimpleDropSink
        {
            private readonly BrowserForm _form;

            public HostDropSink(BrowserForm form)
            {
                _form = form;
                CanDropOnBackground = true;
                CanDropBetween = true;
                FeedbackColor = Color.LightBlue;
            }

            /// <summary>
            /// Gets or sets whether this sink allows model objects to be dragged from other lists
            /// </summary>
            public bool AcceptExternal { get; set; }

            /// <summary>
            /// Draw the feedback that shows that the background is the target
            /// </summary>
            /// <param name="g"></param>
            /// <param name="bounds"></param>
            protected override void DrawFeedbackBackgroundTarget(Graphics g, Rectangle bounds)
            {
                float penWidth = 4.0f;
                Rectangle r = bounds;
                r.Inflate((int) -penWidth/2, (int) -penWidth/2);
                using (Pen p = new Pen(Color.FromArgb(128, FeedbackColor), penWidth))
                {
                    using (GraphicsPath path = GetRoundedRect(r, 30.0f))
                    {
                        g.DrawPath(p, path);
                    }
                }
            }

            protected override void OnCanDrop(OlvDropEventArgs args)
            {
                base.OnCanDrop(args);
                _form.HostCanDrop(args);
            }

            public override void Enter(DragEventArgs e)
            {
                base.Enter(e);

                if (!(e.Data is OLVDataObject))
                {
                    DropTargetHelper.DragEnter(_form.browser, e.Data, new Point(e.X, e.Y), e.Effect);
                }
            }

            public override void Leave()
            {
                base.Leave();
                DropTargetHelper.DragLeave(_form);
            }

            protected override void OnModelCanDrop(ModelDropEventArgs args)
            {
                base.OnModelCanDrop(args);

                args.Effect = DragDropEffects.None;

                //args.Handled = true; // OnCanDrop is not being called anymore

                if (args.Handled)
                    return;

                args.Effect = CalculateStandardDropActionFromKeys();

                // Don't allow drops from other list, if that's what's configured
                if (!AcceptExternal)
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                }
                else
                {
                    _form.HostModelCanDrop(args);
                }
                args.Handled = true;
            }

            protected override void OnDropped(OlvDropEventArgs args)
            {
                DropTargetHelper.Drop(args.DataObject as DataObject,
                                      new Point(args.MouseLocation.X, args.MouseLocation.Y), DragDropEffects.None);
                _form.HostDropped(args);
            }

            protected override void OnModelDropped(ModelDropEventArgs args)
            {
                base.OnModelDropped(args);
                if (!args.Handled)
                {
                    _form.HostModelDropped(args);
                }
                args.Handled = true;
            }

            public override void Over(DragEventArgs e)
            {
                base.Over(e);
                if (!(e.Data is OLVDataObject))
                {
                    DropTargetHelper.DragOver(new Point(e.X, e.Y), e.Effect);
                }
            }
        }

        private class MenuCollectionListener : CollectionListener
        {
            private readonly AbstractHostCollection _collection;
            private readonly String _empty;
            private readonly BrowserForm _form;
            private readonly ImageList.ImageCollection _icons = IconCache.Instance.GetProtocolIcons().Images;
            private readonly MenuItem _menu;

            public MenuCollectionListener(BrowserForm f, MenuItem menu, AbstractHostCollection collection, String empty)
            {
                _form = f;
                _menu = menu;
                _collection = collection;
                _empty = empty;
            }

            public void collectionLoaded()
            {
                _form.Invoke(new AsyncController.AsyncDelegate(BuildMenuItems));
            }

            public void collectionItemAdded(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       if (_collection.size() == 1)
                                                                       {
                                                                           BuildMenuItems();
                                                                       }
                                                                       else
                                                                       {
                                                                           int pos = _collection.indexOf(obj);
                                                                           Host h = (Host) obj;
                                                                           MenuItem i = new MenuItem(h.getNickname());
                                                                           i.Tag = h;
                                                                           i.Click +=
                                                                               (o, args) =>
                                                                               _form.ConnectBookmark(this,
                                                                                                     new ConnectBookmarkArgs
                                                                                                         (h));
                                                                           _menu.MenuItems.Add(pos, i);
                                                                           _form.vistaMenu1.SetImage(i,
                                                                                                     _icons[
                                                                                                         h.getProtocol()
                                                                                                             .
                                                                                                             getIdentifier
                                                                                                             ()]);
                                                                       }
                                                                   }));
            }

            public void collectionItemRemoved(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       foreach (MenuItem item in _menu.MenuItems)
                                                                       {
                                                                           if (obj.Equals(item.Tag))
                                                                           {
                                                                               _menu.MenuItems.Remove(item);
                                                                               break;
                                                                           }
                                                                       }
                                                                       if (
                                                                           _collection.size() == 0)
                                                                       {
                                                                           BuildMenuItems();
                                                                       }
                                                                   }));
            }

            public void collectionItemChanged(object obj)
            {
                _form.Invoke(new AsyncController.AsyncDelegate(delegate
                                                                   {
                                                                       foreach (
                                                                           MenuItem item in _menu.MenuItems)
                                                                       {
                                                                           if (obj.Equals(item.Tag))
                                                                           {
                                                                               Host h = (Host) obj;
                                                                               item.Text = h.getNickname();
                                                                               _form.vistaMenu1.SetImage(item,
                                                                                                         _icons[
                                                                                                             h.
                                                                                                                 getProtocol
                                                                                                                 ().
                                                                                                                 getIdentifier
                                                                                                                 ()]);
                                                                               break;
                                                                           }
                                                                       }
                                                                   }));
            }

            public void BuildMenuItems()
            {
                _menu.MenuItems.Clear();
                if (_collection.size() > 0)
                {
                    List<MenuItem> items = new List<MenuItem>();
                    foreach (Host bookmark in _collection)
                    {
                        MenuItem item = new MenuItem(bookmark.getNickname());
                        item.Tag = bookmark;
                        item.Click +=
                            (o, args) => _form.ConnectBookmark(this, new ConnectBookmarkArgs(item.Tag as Host));
                        items.Add(item);
                    }
                    // separator and clear item
//                    items.Add(new MenuItem("-"));
//                    MenuItem clear = new MenuItem(Locale.localizedString("Clear Menu"));
//                    clear.Click += (o, args) => _form.ClearHistory();
//                    items.Add(clear);

                    _menu.MenuItems.AddRange(items.ToArray());
                    foreach (MenuItem item in items)
                    {
                        if (null != item.Tag)
                            _form.vistaMenu1.SetImage(item, _icons[((Host) item.Tag).getProtocol().getIdentifier()]);
                    }
                }
                else
                {
                    MenuItem noitem = new MenuItem(_empty);
                    noitem.Enabled = false;
                    _menu.MenuItems.Add(noitem);
                }
            }
        }

        private class NoGapRenderer : ToolStripProfessionalRenderer
        {
            public NoGapRenderer()
            {
                RoundedEdges = false;
            }

            protected override void OnRenderToolStripBackground(ToolStripRenderEventArgs e)
            {
                Rectangle rect = e.AffectedBounds;
                e.Graphics.FillRectangle(new SolidBrush(Color.FromKnownColor(KnownColor.Control)), rect);
            }

            protected override void OnRenderButtonBackground(ToolStripItemRenderEventArgs e)
            {
                //no highlighting
                return;
            }

            protected override void OnRenderToolStripBorder(ToolStripRenderEventArgs e)
            {
                base.OnRenderToolStripBorder(e);
                using (Pen pen = new Pen(Color.Gray))
                {
                    Rectangle bounds = new Rectangle(Point.Empty, e.ToolStrip.Size);
                    e.Graphics.DrawLine(pen, bounds.Left, bounds.Top, bounds.Right, bounds.Top);
                }
            }

            protected override void OnRenderItemImage(ToolStripItemImageRenderEventArgs e)
            {
                Image img;
                if (!e.Item.Enabled)
                {
                    if (null == e.Image.Tag)
                    {
                        e.Image.Tag = CreateDisabledImage(e.Image);
                    }
                    img = (Image) e.Image.Tag;
                }
                else
                {
                    img = e.Item.Pressed && e.Item.Selected ? e.Item.Tag as Image : e.Image;
                }
                Rectangle rect = new Rectangle(new Point(e.ImageRectangle.Left - 2, e.ImageRectangle.Top - 2),
                                               new Size(img.Width, img.Height));
                e.Graphics.DrawImage(img, rect);
            }
        }
    }
}