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
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Commondialog;
using Ch.Cyberduck.Ui.Winforms.Controls;
using DataObject = System.Windows.Forms.DataObject;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class BrowserForm : BaseForm, IBrowserView
    {
        private static readonly Font FixedFont = new Font(FontFamily.GenericMonospace, 8);
        private BrowserView _currentView;
        private bool _lastActivityRunning;
        private ToolStripMenuItem _lastMenuItemClicked;

        public BrowserForm()
        {
            InitializeComponent();

            if (!DesignMode)
            {
                bonjourCheckBox.Image = IconCache.Instance.IconForName("rendezvous", 16);
                newFolderToolStripButton.Image = IconCache.Instance.IconForName("newfolder", 32);
            }

            ConfigureToolbar();

            //configure before setting the current view
            ConfigureCheckBoxButton(browserCheckBox);
            ConfigureCheckBoxButton(bookmarkCheckBox);
            ConfigureCheckBoxButton(historyCheckBox);
            ConfigureCheckBoxButton(bonjourCheckBox);

            //default view is the bookmark view
            CurrentView = BrowserView.Bookmark;

            // configure browser properties
            browser.UseExplorerTheme = true;
            browser.UseTranslucentSelection = true;
            browser.OwnerDraw = true;
            browser.UseOverlays = false;
            browser.LabelEdit = true;
            browser.AllowDrop = true;

            browser.DropSink = new ExpandingBrowserDropSink(this);
            browser.DragSource = new BrowserDragSource(this);

            browser.ShowImagesOnSubItems = true;
            browser.TreeColumnRenderer = new BrowserRenderer();
            browser.SelectedRowDecoration = new ExplorerRowBorderDecoration();
            browser.ItemsChanged += (sender, args) => ItemsChanged();

            searchTextBox.InactiveFont = new Font(Font, FontStyle.Italic);
            searchTextBox.InactiveText = Locale.localizedString("Search…", "Main");

            securityToolStripStatusLabel.Visible = false;

            quickConnectToolStripComboBox.ComboBox.SelectionChangeCommitted +=
                toolStripQuickConnect_SelectionChangeCommited;

            // directly right-click on an item leads to some deferred updating of the menu items
            // since the idle event is fired after showing the menu
            contextMenuStrip.Opening += (sender, args) => Commands.Validate();
            contextMenuStrip.Opening += (sender, args) => args.Cancel = !ContextMenuEnabled();

            editorMenuStrip.Opening += OnEditorMenuStripOnOpening;
            // add dummy entry to force the right arrow appearing in the menu
            columnContextMenu.Items.Add(string.Empty);
            archiveMenuStrip.Items.Add(string.Empty);
            archiveMenuStrip.Opening += OnArchiveMenuStripOnOpening;
            textEncodingMenuStrip.Items.Add(string.Empty);
            historyMenuStrip.Items.Add(string.Empty);
            bonjourMenuStrip.Items.Add(string.Empty);

            ConfigureBookmarkList(bookmarkListView, bookmarkDescriptionColumn, bookmarkImageColumn);

            newBookmarkToolStripButton.Tag = ResourcesBundle.addPressed;
            editBookmarkToolStripButton.Tag = ResourcesBundle.editPressed;
            deleteBookmarkToolStripButton.Tag = ResourcesBundle.removePressed;

            actionToolStrip.Renderer = new NoGapRenderer();

            ConfigureShortcuts();
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
                            byte[] state = PersistenceHandler.Get<byte[]>("Tree.State", null);
                            if (null != state)
                            {
                                browser.RestoreState(state);
                            }
                            splitContainer.SplitterDistance = PersistenceHandler.Get("Splitter.Distance", 400);
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

        public event VoidHandler FolderUp;
        public event VoidHandler HistoryBack;
        public event VoidHandler HistoryForward;
        public event VoidHandler EditEvent;
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
        public event EventHandler<CreateArchiveEventArgs> CreateArchive;
        public event ValidateCommand ValidateCreateArchive;
        public event VoidHandler ExpandArchive;
        public event ValidateCommand ValidateExpandArchive;
        public event VoidHandler Exit;
        public event VoidHandler ShowBookmarkManager;
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
        public event VoidHandler CopyUrl;
        public event ValidateCommand ValidateCopyUrl;
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
        public event ValidateCommand ValidateSearch;
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
        public event VoidHandler OpenWebUrl;
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
            set { editToolStripButton.Image = value; }
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
            set
            {
                new TypedColumn<TreePathReference>(treeColumnName)
                    {AspectGetter = value};
            }
        }

        public TypedColumn<TreePathReference>.TypedImageGetterDelegate ModelIconGetter
        {
            set
            {
                new TypedColumn<TreePathReference>(treeColumnName)
                    {ImageGetter = value};
            }
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
                            panelManagerMain.SelectedPanel = browserPanel;
                            EnableViewCheckBox(browserCheckBox);
                            return;
                        case BrowserView.Bookmark:
                            panelManagerMain.SelectedPanel = bookmarksPanel;
                            EnableViewCheckBox(bookmarkCheckBox);
                            return;
                        case BrowserView.History:
                            panelManagerMain.SelectedPanel = bookmarksPanel;
                            EnableViewCheckBox(historyCheckBox);
                            return;
                        case BrowserView.Bonjour:
                            panelManagerMain.SelectedPanel = bookmarksPanel;
                            EnableViewCheckBox(bonjourCheckBox);
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
            toolStripProgressBar.Style = ProgressBarStyle.Marquee;
        }

        public void StopActivityAnimation()
        {
            toolStripProgressBar.Value = 0;
            toolStripProgressBar.Style = ProgressBarStyle.Continuous;
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

        public void SetBookmarkModel(IEnumerable hosts)
        {
            bookmarkListView.SetObjects(hosts);
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
            }
            get { return toolBar.Visible; }
        }

        public void PopulateEncodings(List<string> encodings)
        {
            textEncodingMenuStrip.Items.Clear();
            foreach (string encoding in encodings)
            {
                textEncodingMenuStrip.Items.Add(encoding);
            }
        }

        public string SelectedEncoding
        {
            set
            {
                foreach (ToolStripMenuItem item in textEncodingMenuStrip.Items)
                {
                    item.Checked = value.Equals(item.Text);
                }
            }
        }

        public bool SecureConnection
        {
            set { securityToolStripStatusLabel.Image = IconCache.Instance.IconForName(value ? "locked" : "unlocked"); }
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
        }

        private void ConfigureToolbar()
        {
            openConnectionToolStripMenuItem1.CheckOnClick = true;
            openConnectionToolStripMenuItem1.Click += delegate
                                                          {
                                                              openConnectionToolStripButton.Visible =
                                                                  !openConnectionToolStripButton.Visible;
                                                              UpdateSeparators();
                                                              Preferences.instance().setProperty(
                                                                  "browser.toolbar.openconnection",
                                                                  openConnectionToolStripButton.Visible);
                                                          };
            quickConnectToolStripMenuItem.CheckOnClick = true;
            quickConnectToolStripMenuItem.Click += delegate
                                                       {
                                                           quickConnectToolStripComboBox.Visible =
                                                               !quickConnectToolStripComboBox.Visible;
                                                           UpdateSeparators();
                                                           Preferences.instance().setProperty(
                                                               "browser.toolbar.quickconnect",
                                                               quickConnectToolStripComboBox.Visible);
                                                       };
            actionToolStripMenuItem.CheckOnClick = true;
            actionToolStripMenuItem.Click += delegate
                                                 {
                                                     actionToolStripDropDownButton.Visible =
                                                         !actionToolStripDropDownButton.Visible;
                                                     UpdateSeparators();
                                                     Preferences.instance().setProperty(
                                                         "browser.toolbar.action",
                                                         actionToolStripDropDownButton.Visible);
                                                 };
            infoToolStripMenuItem1.CheckOnClick = true;
            infoToolStripMenuItem1.Click += delegate
                                                {
                                                    infoToolStripButton.Visible = !infoToolStripButton.Visible;
                                                    UpdateSeparators();
                                                    Preferences.instance().setProperty(
                                                        "browser.toolbar.info",
                                                        infoToolStripButton.Visible);
                                                };
            refreshToolStripMenuItem1.CheckOnClick = true;
            refreshToolStripMenuItem1.Click += delegate
                                                   {
                                                       refreshToolStripButton.Visible = !refreshToolStripButton.Visible;
                                                       UpdateSeparators();
                                                       Preferences.instance().setProperty(
                                                           "browser.toolbar.refresh",
                                                           refreshToolStripButton.Visible);
                                                   };
            editToolStripMenuItem1.CheckOnClick = true;
            editToolStripMenuItem1.Click += delegate
                                                {
                                                    editToolStripButton.Visible = !editToolStripButton.Visible;
                                                    UpdateSeparators();
                                                    Preferences.instance().setProperty(
                                                        "browser.toolbar.edit",
                                                        editToolStripButton.Visible);
                                                };
            openInWebBrowserToolStripMenuItem.CheckOnClick = true;
            openInWebBrowserToolStripMenuItem.Click += delegate
                                                           {
                                                               openInBrowserToolStripButton.Visible =
                                                                   !openInBrowserToolStripButton.Visible;
                                                               UpdateSeparators();
                                                               Preferences.instance().setProperty(
                                                                   "browser.toolbar.openinbrowser",
                                                                   openInBrowserToolStripButton.Visible);
                                                           };
            newFolderToolStripMenuItem1.CheckOnClick = true;
            newFolderToolStripMenuItem1.Click += delegate
                                                     {
                                                         newFolderToolStripButton.Visible =
                                                             !newFolderToolStripButton.Visible;
                                                         UpdateSeparators();
                                                         Preferences.instance().setProperty(
                                                             "browser.toolbar.newfolder",
                                                             newFolderToolStripButton.Visible);
                                                     };
            deleteToolStripMenuItem1.CheckOnClick = true;
            deleteToolStripMenuItem1.Click += delegate
                                                  {
                                                      deleteToolStripButton.Visible = !deleteToolStripButton.Visible;
                                                      UpdateSeparators();
                                                      Preferences.instance().setProperty(
                                                          "browser.toolbar.delete",
                                                          deleteToolStripButton.Visible);
                                                  };
            downloadToolStripMenuItem1.CheckOnClick = true;
            downloadToolStripMenuItem1.Click += delegate
                                                    {
                                                        downloadToolStripButton.Visible =
                                                            !downloadToolStripButton.Visible;
                                                        UpdateSeparators();
                                                        Preferences.instance().setProperty(
                                                            "browser.toolbar.download",
                                                            downloadToolStripButton.Visible);
                                                    };
            uploadToolStripMenuItem1.CheckOnClick = true;
            uploadToolStripMenuItem1.Click += delegate

                                                  {
                                                      uploadToolStripButton.Visible = !uploadToolStripButton.Visible;
                                                      UpdateSeparators();
                                                      Preferences.instance().setProperty(
                                                          "browser.toolbar.upload",
                                                          uploadToolStripButton.Visible);
                                                  };
            transfersToolStripMenuItem1.CheckOnClick = true;
            transfersToolStripMenuItem1.Click += delegate

                                                     {
                                                         transfersToolStripButton.Visible =
                                                             !transfersToolStripButton.Visible;
                                                         UpdateSeparators();
                                                         Preferences.instance().setProperty(
                                                             "browser.toolbar.transfers",
                                                             transfersToolStripButton.Visible);
                                                     };

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
            bool b6 = editToolStripButton.Visible = Preferences.instance().getBoolean("browser.toolbar.edit");
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

            UpdateSeparators();
        }

        private void ConfigureCheckBoxButton(CheckBox checkBox)
        {
            checkBox.FlatAppearance.MouseOverBackColor = ProfessionalColors.ButtonSelectedHighlight;
            checkBox.FlatAppearance.MouseDownBackColor = ProfessionalColors.ButtonPressedHighlight;
            checkBox.FlatAppearance.CheckedBackColor = ProfessionalColors.ButtonCheckedHighlight;

            checkBox.MouseEnter += delegate(object sender, EventArgs args)
                                       {
                                           CheckBox cb = sender as CheckBox;
                                           cb.FlatAppearance.BorderSize = 1;
                                           cb.FlatAppearance.BorderColor =
                                               ProfessionalColors.ButtonSelectedHighlightBorder;
                                       };


            EventHandler removeBorder = delegate(object sender, EventArgs args)
                                            {
                                                CheckBox cb = sender as CheckBox;
                                                if (!cb.Checked)
                                                {
                                                    checkBox.FlatAppearance.BorderSize = 0;
                                                }
                                                else
                                                {
                                                    cb.FlatAppearance.BorderSize = 1;
                                                    cb.FlatAppearance.BorderColor =
                                                        ProfessionalColors.ButtonSelectedHighlightBorder;
                                                }
                                            };
            checkBox.MouseLeave += removeBorder;
            checkBox.CheckedChanged += removeBorder;
        }

        public event VoidHandler OpenDownloadFolderEvent;

        private void ConfigureBookmarkCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 viewBookmarksToolStripMenuItem
                             }, (sender, args) => ToggleBookmarks(), () => true);

            Commands.Add(new ToolStripItem[]
                             {
                                 connectBookmarkContextToolStripMenuItem,
                             },
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
                         (sender, args) => NewBookmark(), () => ValidateNewBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 editBookmarkToolStripMenuItem,
                                 editBookmarkContextToolStripMenuItem1,
                                 editBookmarkToolStripButton
                             },
                         (sender, args) => EditBookmark(), () => ValidateEditBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 deleteBookmarkToolStripMenuItem,
                                 deleteBookmarkContextToolStripMenuItem1,
                                 deleteBookmarkToolStripButton
                             },
                         (sender, args) => DeleteBookmark(), () => ValidateDeleteBookmark());
            Commands.Add(new ToolStripItem[]
                             {
                                 duplicateBookmarkToolStripMenuItem1,
                                 duplicateBookmarkToolStripMenuItem
                             },
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

        private void EnableViewCheckBox(CheckBox cb)
        {
            browserCheckBox.Checked = false;
            bookmarkCheckBox.Checked = false;
            historyCheckBox.Checked = false;
            bonjourCheckBox.Checked = false;

            cb.Checked = true;
        }

        private void ConfigureHelpCommands()
        {
            //direct commands

            //todo move handlers to the controller
            Commands.Add(new ToolStripItem[] {acknowledgmentsToolStripMenuItem},
                         (sender, args) => Utils.StartProcess("Acknowledgments.rtf"), () => true);
            Commands.Add(new ToolStripItem[] {cyberduckHelpToolStripMenuItem},
                         (sender, args) => Utils.StartProcess(Preferences.instance().getProperty("website.help")),
                         () => true);
            Commands.Add(new ToolStripItem[] {reportABugToolStripMenuItem},
                         (sender, args) => Utils.StartProcess(Preferences.instance().getProperty("website.bug")),
                         () => true);
            Commands.Add(new ToolStripItem[] {aboutCyberduckToolStripMenuItem},
                         (sender, args) => new AboutBox().ShowDialog(), () => true);
            Commands.Add(new ToolStripItem[] {licenseToolStripMenuItem},
                         (sender, args) =>
                         Utils.StartProcess(MainController.StartupLanguage.Replace('-', '_') + ".lproj\\License.txt"),
                         () => true);
            Commands.Add(new ToolStripItem[] {checkToolStripMenuItem},
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
                             },
                         (sender, args) => RefreshBrowser(), () => ValidateRefresh());
            Commands.Add(new ToolStripItem[]
                             {
                                 gotoFolderToolStripMenuItem
                             },
                         (sender, args) => GotoFolder(), () => ValidateGotoFolder());
            Commands.Add(new ToolStripItem[]
                             {
                                 backToolStripMenuItem
                             }, new Control[] {historyBackButton},
                         (sender, args) => HistoryBack(), () => ValidateHistoryBack());
            Commands.Add(new ToolStripItem[]
                             {
                                 forwardToolStripMenuItem
                             }, new Control[]
                                    {
                                        historyForwardButton
                                    },
                         (sender, args) => HistoryForward(), () => ValidateHistoryForward());
            Commands.Add(new ToolStripItem[]
                             {
                                 enclosingFolderToolStripMenuItem
                             }, new Control[]
                                    {
                                        parentPathButton
                                    },
                         (sender, args) => FolderUp(), () => ValidateFolderUp());
            Commands.Add(new ToolStripItem[]
                             {
                                 insideToolStripMenuItem
                             },
                         (sender, args) => FolderInside(), () => ValidateFolderInside());
            Commands.Add(new ToolStripItem[]
                             {
                                 searchToolStripMenuItem
                             },
                         (sender, args) => Search(), () => ValidateSearch());
            Commands.Add(new ToolStripItem[]
                             {
                                 sendCommandToolStripMenuItem
                             },
                         (sender, args) => SendCustomCommand(), () => ValidateSendCustomCommand());
            Commands.Add(new ToolStripItem[]
                             {
                                 stopToolStripMenuItem
                             },
                         (sender, args) => Stop(), () => ValidateStop());
            Commands.Add(new ToolStripItem[]
                             {
                                 disconnectToolStripMenuItem,
                                 disconnectStripButton
                             },
                         (sender, args) => Disconnect(), () => ValidateDisconnect());
        }

        private void ConfigureViewCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 toggleToolbarToolStripMenuItem
                             },
                         (sender, args) => ToggleToolbar(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 showHiddenFilesToolStripMenuItem
                             },
                         (sender, args) => ShowHiddenFiles(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 textEncodingToolStripMenuItem
                             },
                         null, () => ValidateTextEncoding());
            Commands.Add(new ToolStripItem[]
                             {
                                 toggleLogDrawerToolStripMenuItem
                             },
                         (sender, args) => ToggleLogDrawer(), () => true);
        }

        private void ConfigureWindowCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 transfersToolStripMenuItem,
                                 transfersToolStripButton
                             },
                         (sender, args) => ShowTransfers(), () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 activitiyToolStripMenuItem,
                             },
                         (sender, args) => ((Form) ActivityController.Instance.View).Show(), () => true);
            //todo muss ShowActivity() sein
            //(ActivityController.Instance.View as Form).Show();
        }

        private void ConfigureEditCommands()
        {
            Commands.Add(new ToolStripItem[]
                             {
                                 cutToolStripMenuItem
                             },
                         (sender, args) => Cut(), () => ValidateCut());
            Commands.Add(new ToolStripItem[]
                             {
                                 copyToolStripMenuItem
                             },
                         (sender, args) => Copy(), () => ValidateCopy());
            Commands.Add(new ToolStripItem[]
                             {
                                 copyURLToolStripMenuItem,
                                 copyURLContextToolStripMenuItem
                             },
                         (sender, args) => CopyUrl(), () => ValidateCopyUrl());
            Commands.Add(new ToolStripItem[]
                             {
                                 pasteToolStripMenuItem
                             },
                         (sender, args) => Paste(), () => ValidatePaste());
            Commands.Add(new ToolStripItem[]
                             {
                                 selectAllToolStripMenuItem
                             },
                         (o, eventArgs) => { }, () => true); // Tree component handles the selectAll command
            Commands.Add(new ToolStripItem[]
                             {
                                 preferencesToolStripMenuItem
                             },
                         (o, eventArgs) => ShowPreferences(), () => true);
        }

        private void ConfigureShortcuts()
        {
            #region Shortcuts - Files

            newBrowserToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.N;
            openConnectionToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.O;
            newDownloadToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Alt | Keys.Down;
            newFolderToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Shift | Keys.N;
            newFileToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Shift | Keys.F;
            duplicateFileToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.D;
            openWebURLToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Alt | Keys.B;
            editWithToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.K;
            infoToolStripMenuItem.ShortcutKeys = Keys.Alt | Keys.Enter;
            downloadToolStripMenuItem.ShortcutKeys = Keys.Alt | Keys.Down;
            downloadAsToolStripMenuItem.ShortcutKeys = Keys.Alt | Keys.Shift | Keys.Down;
            uploadToolStripMenuItem.ShortcutKeys = Keys.Alt | Keys.Up;
            deleteToolStripMenuItem.ShortcutKeys = Keys.Delete;
            //exitToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.W;

            #endregion

            #region Shortcuts - Edit

            cutToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.X;
            copyToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.C;
            copyURLToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Shift | Keys.C;
            pasteToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.V;
            selectAllToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.A;
            preferencesToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Oemcomma;
            preferencesToolStripMenuItem.ShortcutKeyDisplayString = "Ctrl+,";

            #endregion

            #region Shortcuts - View

            showHiddenFilesToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Shift | Keys.R;
            toggleLogDrawerToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.L;

            #endregion

            #region Shortcuts - Go

            refreshToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.R;
            gotoFolderToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.G;
            backToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Left;
            forwardToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Right;
            enclosingFolderToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Up;
            insideToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Down;
            searchToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.F;
            sendCommandToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Alt | Keys.C;
            stopToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.OemPeriod;
            stopToolStripMenuItem.ShortcutKeyDisplayString = "Ctrl+.";
            disconnectToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Y;

            #endregion

            #region Shortcurs - Bookmark

            viewBookmarksToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.B;
            newBookmarkToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.Shift | Keys.B;
            editBookmarkToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.E;

            #endregion

            #region Shortcuts - Window

            minimizeToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.M;
            activitiyToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.D0;
            transfersToolStripMenuItem.ShortcutKeys = Keys.Control | Keys.T;

            #endregion

            #region Shortcuts - Browser Context

            refreshContextToolStripMenuItem.ShortcutKeys = refreshToolStripMenuItem.ShortcutKeys;
            infoContextToolStripMenuItem.ShortcutKeys = infoToolStripMenuItem.ShortcutKeys;
            editContextToolStripMenuItem.ShortcutKeys = editWithToolStripMenuItem.ShortcutKeys;
            downloadContextToolStripMenuItem.ShortcutKeys = downloadToolStripMenuItem.ShortcutKeys;
            downloadAsContextToolStripMenuItem.ShortcutKeys = downloadAsToolStripMenuItem.ShortcutKeys;
            deleteContextToolStripMenuItem.ShortcutKeys = deleteToolStripMenuItem.ShortcutKeys;
            duplicateFileContextToolStripMenuItem.ShortcutKeys = duplicateFileToolStripMenuItem.ShortcutKeys;
            uploadContextToolStripMenuItem.ShortcutKeys = uploadToolStripMenuItem.ShortcutKeys;
            newFolderContextToolStripMenuItem.ShortcutKeys = newFolderToolStripMenuItem.ShortcutKeys;
            newFileContextToolStripMenuItem.ShortcutKeys = newFileToolStripMenuItem.ShortcutKeys;
            copyURLContextToolStripMenuItem.ShortcutKeys = copyURLToolStripMenuItem.ShortcutKeys;
            openWebURLContextToolStripMenuItem.ShortcutKeys = openWebURLToolStripMenuItem.ShortcutKeys;
            newBookmarkContextToolStripMenuItem.ShortcutKeys = newBookmarkToolStripMenuItem.ShortcutKeys;

            #endregion

            #region Shortcuts - Bookmarks Context

            connectBookmarkContextToolStripMenuItem.ShortcutKeyDisplayString = "Enter";
            newBookmarkContextToolStripMenuItem1.ShortcutKeys = newBookmarkToolStripMenuItem.ShortcutKeys;
            //todo deleteBookmarkContextToolStripMenuItem1.ShortcutKeys = 
            editBookmarkContextToolStripMenuItem1.ShortcutKeys = editBookmarkToolStripMenuItem.ShortcutKeys;

            #endregion
        }

        private void OnArchiveMenuStripOnOpening(object sender, CancelEventArgs e)
        {
            archiveMenuStrip.Items.Clear();
            foreach (string archive in GetArchives())
            {
                ToolStripItem item = archiveMenuStrip.Items.Add(archive);
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
                         (sender, args) =>
                             {
                                 SaveUiSettings();
                                 NewBrowser(this, new NewBrowserEventArgs(false));
                             }, () => true);
            Commands.Add(new ToolStripItem[]
                             {
                                 newBrowserContextToolStripMenuItem
                             },
                         (sender, args) =>
                             {
                                 SaveUiSettings();
                                 NewBrowser(this, new NewBrowserEventArgs(true));
                             }, () => ValidateNewBrowser());
            Commands.Add(new ToolStripItem[]
                             {
                                 openConnectionToolStripMenuItem,
                                 openConnectionToolStripButton
                             },
                         (sender, args) => OpenConnection(), () => ValidateOpenConnection());
            Commands.Add(new ToolStripItem[]
                             {
                                 newDownloadToolStripMenuItem
                             },
                         (sender, args) => NewDownload(), () => ValidateNewDownload());
            Commands.Add(new ToolStripItem[]
                             {
                                 newFolderToolStripMenuItem,
                                 newFolderContextToolStripMenuItem,
                                 newFolderToolStripButton
                             },
                         (sender, args) => NewFolder(), () => ValidateNewFolder());
            Commands.Add(new ToolStripItem[]
                             {
                                 newFileToolStripMenuItem,
                                 newFileContextToolStripMenuItem
                             },
                         (sender, args) => NewFile(), () => ValidateNewFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 renameFileToolStripMenuItem,
                                 renameContextToolStripMenuItem
                             },
                         (o, eventArgs) => browser.SelectedItem.BeginEdit(), () => ValidateRenameFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 duplicateFileToolStripMenuItem,
                                 duplicateFileContextToolStripMenuItem
                             },
                         (sender, args) => DuplicateFile(), () => ValidateDuplicateFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 openWebURLToolStripMenuItem,
                                 openWebURLContextToolStripMenuItem,
                                 openInBrowserToolStripButton
                             },
                         (sender, args) => OpenWebUrl(), () => ValidateOpenWebUrl());
            Commands.Add(new ToolStripItem[]
                             {
                                 editWithToolStripMenuItem,
                                 editContextToolStripMenuItem,
                                 editToolStripButton
                             },
                         (sender, args) => EditEvent(), () => ValidateEditWith());
            Commands.Add(new ToolStripItem[]
                             {
                                 infoToolStripMenuItem,
                                 infoToolStripButton,
                                 infoContextToolStripMenuItem
                             },
                         (sender, args) => ShowInspector(), () => ValidateShowInspector());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadToolStripMenuItem,
                                 downloadContextToolStripMenuItem
                             },
                         (sender, args) => Download(), () => ValidateDownload());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadAsToolStripMenuItem,
                                 downloadAsContextToolStripMenuItem
                             },
                         (sender, args) => DownloadAs(), () => ValidateDownloadAs());
            Commands.Add(new ToolStripItem[]
                             {
                                 downloadToToolStripMenuItem,
                                 downloadToContextToolStripMenuItem,
                                 downloadToolStripButton
                             },
                         (sender, args) => DownloadTo(), () => ValidateDownloadTo());
            Commands.Add(new ToolStripItem[]
                             {
                                 uploadToolStripMenuItem,
                                 uploadContextToolStripMenuItem,
                                 uploadToolStripButton
                             },
                         (sender, args) => Upload(), () => ValidateUpload());
            Commands.Add(new ToolStripItem[]
                             {
                                 synchronizeToolStripMenuItem,
                                 synchronizeContextToolStripMenuItem
                             },
                         (sender, args) => Synchronize(), () => ValidateSynchronize());
            Commands.Add(new ToolStripItem[]
                             {
                                 deleteToolStripMenuItem,
                                 deleteContextToolStripMenuItem,
                                 deleteToolStripButton
                             },
                         (sender, args) => Delete(), () => ValidateDelete());
            Commands.Add(new ToolStripItem[]
                             {
                                 revertToolStripMenuItem,
                                 revertContxtStripMenuItem
                             }, (sender, args) => RevertFile(), () => ValidateRevertFile());
            Commands.Add(new ToolStripItem[]
                             {
                                 createArchiveToolStripMenuItem,
                                 createArchiveContextToolStripMenuItem
                             },
                         (sender, args) => { }, () => ValidateCreateArchive());
            Commands.Add(new ToolStripItem[]
                             {
                                 expandArchiveToolStripMenuItem,
                                 expandArchiveContextToolStripMenuItem
                             },
                         (sender, args) => ExpandArchive(), () => ValidateExpandArchive());
            Commands.Add(new ToolStripItem[]
                             {
                                 exitToolStripMenuItem
                             },
                         (sender, args) => Exit(), () => true);
        }

        private void SaveUiSettings()
        {
            PersistenceHandler.Set("Tree.State", browser.SaveState());
            PersistenceHandler.Set("Splitter.Distance", splitContainer.SplitterDistance);
        }

        private void OnEditorMenuStripOnOpening(object sender, CancelEventArgs args)
        {
            editorMenuStrip.Items.Clear();
            foreach (string editor in GetEditors())
            {
                editorMenuStrip.Items.Add(editor);
            }
        }

        private void browser_DoubleClick(object sender, EventArgs e)
        {
            BrowserDoubleClicked();
        }

        private void showBookmarks(object sender, EventArgs e)
        {
            ShowBookmarkManager();
        }

        private void aboutCyberduckToolStripMenuItem_Click(object sender, EventArgs e)
        {
            AboutBox about = new AboutBox();
            about.ShowDialog();
        }

        private void toolStripQuickConnect_SelectionChangeCommited(object sender, EventArgs e)
        {
            QuickConnect();
        }

        private void toolStripPath_SelectionChangeCommitted(object sender, EventArgs e)
        {
            PathSelectionChanged();
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

        private void searchTextBox_TextChanged(object sender, EventArgs e)
        {
            SearchFieldChanged();
        }

        private void minimizeToolStripMenuItem_Click(object sender, EventArgs e)
        {
            WindowState = FormWindowState.Minimized;
        }

        private void toolbarContextMenu_Closing(object sender, ToolStripDropDownClosingEventArgs e)
        {
            e.Cancel = (
                           e.CloseReason == ToolStripDropDownCloseReason.ItemClicked &&
                           _lastMenuItemClicked != null);
        }

        private void toolbarContextMenu_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            _lastMenuItemClicked = (ToolStripMenuItem) e.ClickedItem;
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
                    DropTargetHelper.DragEnter(_form.browser, e.Data, new Point(e.X, e.Y), e.Effect, "Copy to %1",
                                               "Here"); //todo needs to be localized
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
                if (!AcceptExternal && args.SourceListView != ListView)
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                    //args.InfoMessage = "This browser doesn't accept drops from other browser";                    
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
                    DropTargetHelper.DragEnter(_form.browser, e.Data, new Point(e.X, e.Y), e.Effect, "Copy to %1",
                                               "Here"); //todo needs to be localized
                }
            }

            public override void Leave()
            {
                base.Leave();
                DropTargetHelper.DragLeave(_form);
            }

            protected override void OnModelCanDrop(ModelDropEventArgs args)
            {
                //todo reordering bookmarks
                base.OnModelCanDrop(args);

                args.Effect = DragDropEffects.None;

                //args.Handled = true; // OnCanDrop is not being called anymore
                                
                if (args.Handled)
                    return;

                args.Effect = CalculateStandardDropActionFromKeys();

                // Don't allow drops from other list, if that's what's configured
                if (!AcceptExternal && args.SourceListView != ListView)
                {
                    args.Effect = DragDropEffects.None;
                    args.DropTargetLocation = DropTargetLocation.None;
                    //args.InfoMessage = "This browser doesn't accept drops from other browser";                    
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

        private class NoGapRenderer : ToolStripProfessionalRenderer
        {
            public NoGapRenderer()
            {
                RoundedEdges = false;
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