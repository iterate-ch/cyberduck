using System;
using System.Windows.Forms;
using BrightIdeasSoftware;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    partial class BrowserForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(BrowserForm));
            this.mainMenu = new System.Windows.Forms.MainMenu(this.components);
            this.menuItem1 = new System.Windows.Forms.MenuItem();
            this.newBrowserMainMenuItem = new System.Windows.Forms.MenuItem();
            this.openConnectionMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem4 = new System.Windows.Forms.MenuItem();
            this.newDownloadMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem6 = new System.Windows.Forms.MenuItem();
            this.newFolderMainMenuItem = new System.Windows.Forms.MenuItem();
            this.newVaultMainMenuItem = new System.Windows.Forms.MenuItem();
            this.newFileMainMenuItem = new System.Windows.Forms.MenuItem();
            this.newSymbolicLinkMainMenuItem = new System.Windows.Forms.MenuItem();
            this.renameMainMenuItem = new System.Windows.Forms.MenuItem();
            this.duplicateMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem11 = new System.Windows.Forms.MenuItem();
            this.openUrlMainMenuItem = new System.Windows.Forms.MenuItem();
            this.editMainMenuItem = new System.Windows.Forms.MenuItem();
            this.infoMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem2 = new System.Windows.Forms.MenuItem();
            this.downloadMainMenuItem = new System.Windows.Forms.MenuItem();
            this.downloadAsMainMenuItem = new System.Windows.Forms.MenuItem();
            this.downloadToMainMenuItem = new System.Windows.Forms.MenuItem();
            this.uploadMainMenuItem = new System.Windows.Forms.MenuItem();
            this.synchronizeMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem21 = new System.Windows.Forms.MenuItem();
            this.deleteMainMenuItem = new System.Windows.Forms.MenuItem();
            this.revertMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem24 = new System.Windows.Forms.MenuItem();
            this.createArchiveMainMenuItem = new System.Windows.Forms.MenuItem();
            this.expandArchiveMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem27 = new System.Windows.Forms.MenuItem();
            this.printMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem29 = new System.Windows.Forms.MenuItem();
            this.exitMainMenuItem = new System.Windows.Forms.MenuItem();
            this.mainMenuItem2 = new System.Windows.Forms.MenuItem();
            this.cutMainMenuItem = new System.Windows.Forms.MenuItem();
            this.copyMainMenuItem = new System.Windows.Forms.MenuItem();
            this.copyUrlMainMenuItem = new System.Windows.Forms.MenuItem();
            this.pasteMainMenuItem = new System.Windows.Forms.MenuItem();
            this.selectAllMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem37 = new System.Windows.Forms.MenuItem();
            this.preferencesMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem39 = new System.Windows.Forms.MenuItem();
            this.toggleToolbarMainMenuItem = new System.Windows.Forms.MenuItem();
            this.customizeToolbarMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem42 = new System.Windows.Forms.MenuItem();
            this.menuItem43 = new System.Windows.Forms.MenuItem();
            this.columnMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem45 = new System.Windows.Forms.MenuItem();
            this.showHiddenFilesMainMenuItem = new System.Windows.Forms.MenuItem();
            this.textEncodingMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem48 = new System.Windows.Forms.MenuItem();
            this.toggleLogDrawerMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem50 = new System.Windows.Forms.MenuItem();
            this.refreshMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem52 = new System.Windows.Forms.MenuItem();
            this.goToFolderMainMenuItem = new System.Windows.Forms.MenuItem();
            this.backMainMenuItem = new System.Windows.Forms.MenuItem();
            this.forwardMainMenuItem = new System.Windows.Forms.MenuItem();
            this.enclosingFolderMainMenuItem = new System.Windows.Forms.MenuItem();
            this.insideMainMenuItem = new System.Windows.Forms.MenuItem();
            this.searchMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem59 = new System.Windows.Forms.MenuItem();
            this.sendCommandMainMenuItem = new System.Windows.Forms.MenuItem();
            this.openInTerminalMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem61 = new System.Windows.Forms.MenuItem();
            this.stopMainMenuItem = new System.Windows.Forms.MenuItem();
            this.disconnectMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem64 = new System.Windows.Forms.MenuItem();
            this.toggleBookmarksMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem3 = new System.Windows.Forms.MenuItem();
            this.sortByNicknameMainMenuItem = new System.Windows.Forms.MenuItem();
            this.sortByHostnameMainMenuItem = new System.Windows.Forms.MenuItem();
            this.sortByProtocolMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem66 = new System.Windows.Forms.MenuItem();
            this.newBookmarkMainMenuItem = new System.Windows.Forms.MenuItem();
            this.deleteBookmarkMainMenuItem = new System.Windows.Forms.MenuItem();
            this.editBookmarkMainMenuItem = new System.Windows.Forms.MenuItem();
            this.duplicateBookmarkMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem71 = new System.Windows.Forms.MenuItem();
            this.historyMainMenuItem = new System.Windows.Forms.MenuItem();
            this.bonjourMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem74 = new System.Windows.Forms.MenuItem();
            this.menuItem75 = new System.Windows.Forms.MenuItem();
            this.minimizeMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem77 = new System.Windows.Forms.MenuItem();
            this.transfersMainMenuItem = new System.Windows.Forms.MenuItem();
            this.activityMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem80 = new System.Windows.Forms.MenuItem();
            this.helpMainMenuItem = new System.Windows.Forms.MenuItem();
            this.licenseMainMenuItem = new System.Windows.Forms.MenuItem();
            this.acknowledgmentsMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem84 = new System.Windows.Forms.MenuItem();
            this.bugMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem86 = new System.Windows.Forms.MenuItem();
            this.updateMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem88 = new System.Windows.Forms.MenuItem();
            this.donateMainMenuItem = new System.Windows.Forms.MenuItem();
            this.keyMainMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem7 = new System.Windows.Forms.MenuItem();
            this.aboutMainMenuItem = new System.Windows.Forms.MenuItem();
            this.statusStrip = new System.Windows.Forms.StatusStrip();
            this.toolStripProgress = new System.Windows.Forms.ToolStripStatusLabel();
            this.statusLabel = new System.Windows.Forms.ToolStripStatusLabel();
            this.securityToolStripStatusLabel = new System.Windows.Forms.ToolStripStatusLabel();
            this.menuStrip1 = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughMenuStrip();
            this.fileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newBrowserToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openConnectionToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator7 = new System.Windows.Forms.ToolStripSeparator();
            this.newDownloadToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator8 = new System.Windows.Forms.ToolStripSeparator();
            this.newFolderToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newVaultToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newFileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.renameFileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.duplicateFileToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator9 = new System.Windows.Forms.ToolStripSeparator();
            this.openWebURLToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editWithToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.infoToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator10 = new System.Windows.Forms.ToolStripSeparator();
            this.downloadToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.downloadAsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.downloadToToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.uploadToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.synchronizeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator25 = new System.Windows.Forms.ToolStripSeparator();
            this.deleteToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.revertToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator11 = new System.Windows.Forms.ToolStripSeparator();
            this.createArchiveToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.archiveMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.createArchiveContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.expandArchiveToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator12 = new System.Windows.Forms.ToolStripSeparator();
            this.printToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator35 = new System.Windows.Forms.ToolStripSeparator();
            this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cutToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.copyURLToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.pasteToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.selectAllToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator14 = new System.Windows.Forms.ToolStripSeparator();
            this.preferencesToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.viewToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toggleToolbarToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolbarToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolbarContextMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.openConnectionToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator30 = new System.Windows.Forms.ToolStripSeparator();
            this.quickConnectToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.actionToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator31 = new System.Windows.Forms.ToolStripSeparator();
            this.infoToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.refreshToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator32 = new System.Windows.Forms.ToolStripSeparator();
            this.editToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.openInWebBrowserToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openInTerminalToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newFolderToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.deleteToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator33 = new System.Windows.Forms.ToolStripSeparator();
            this.downloadToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.uploadToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.transfersToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.logToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator13 = new System.Windows.Forms.ToolStripSeparator();
            this.columnToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.columnContextMenu = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.toolStripSeparator34 = new System.Windows.Forms.ToolStripSeparator();
            this.showHiddenFilesToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.textEncodingToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.textEncodingMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.toolStripSeparator15 = new System.Windows.Forms.ToolStripSeparator();
            this.toggleLogDrawerToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.goToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.refreshToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator16 = new System.Windows.Forms.ToolStripSeparator();
            this.gotoFolderToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.backToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.forwardToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.enclosingFolderToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.insideToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.searchToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator17 = new System.Windows.Forms.ToolStripSeparator();
            this.sendCommandToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator18 = new System.Windows.Forms.ToolStripSeparator();
            this.stopToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.disconnectToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.bookmarkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.viewBookmarksToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.newBookmarkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.deleteBookmarkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editBookmarkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.duplicateBookmarkToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator19 = new System.Windows.Forms.ToolStripSeparator();
            this.historyToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.historyMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.bonjourToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.bonjourMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.toolStripSeparator36 = new System.Windows.Forms.ToolStripSeparator();
            this.windowToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.minimizeToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator20 = new System.Windows.Forms.ToolStripSeparator();
            this.transfersToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.activitiyToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.helpToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.cyberduckHelpToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.licenseToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.acknowledgmentsToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator21 = new System.Windows.Forms.ToolStripSeparator();
            this.reportABugToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator22 = new System.Windows.Forms.ToolStripSeparator();
            this.checkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator28 = new System.Windows.Forms.ToolStripSeparator();
            this.aboutCyberduckToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editorMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.editor1ToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editor2ToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.editToolStripSplitButton = new System.Windows.Forms.ToolStripSplitButton();
            this.editContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolBar = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughToolStrip();
            this.openConnectionToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparatorAfterOpenConnection = new System.Windows.Forms.ToolStripSeparator();
            this.quickConnectToolStripComboBox = new System.Windows.Forms.ToolStripComboBox();
            this.actionToolStripDropDownButton = new System.Windows.Forms.ToolStripDropDownButton();
            this.contextMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.refreshContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator6 = new System.Windows.Forms.ToolStripSeparator();
            this.newFolderContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newVaultContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newFileContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newSymlinkContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.renameContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.duplicateFileContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator2 = new System.Windows.Forms.ToolStripSeparator();
            this.copyURLContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.openURLContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.infoContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator26 = new System.Windows.Forms.ToolStripSeparator();
            this.downloadContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.downloadAsContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.downloadToContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.uploadContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.synchronizeContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator3 = new System.Windows.Forms.ToolStripSeparator();
            this.deleteContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.revertContxtStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator4 = new System.Windows.Forms.ToolStripSeparator();
            this.expandArchiveContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator5 = new System.Windows.Forms.ToolStripSeparator();
            this.newBrowserContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.newBookmarkContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparatorAfterAction = new System.Windows.Forms.ToolStripSeparator();
            this.infoToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.refreshToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparatorAfterRefresh = new System.Windows.Forms.ToolStripSeparator();
            this.openInBrowserToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.openInTerminalToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.newFolderToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.deleteToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparatorAfterDelete = new System.Windows.Forms.ToolStripSeparator();
            this.downloadToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.uploadToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.transfersToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.logToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.disconnectStripButton = new System.Windows.Forms.ToolStripButton();
            this.iconList = new System.Windows.Forms.ImageList(this.components);
            this.toolStripContainer1 = new System.Windows.Forms.ToolStripContainer();
            this.panelManager1 = new Ch.Cyberduck.Ui.Winforms.Controls.PanelManager();
            this.managedBrowserPanel1 = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.splitContainer = new System.Windows.Forms.SplitContainer();
            this.browser = new Ch.Cyberduck.Ui.Winforms.Controls.MulticolorTreeListView();
            this.treeColumnName = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnSize = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnModified = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnOwner = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnGroup = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnPermissions = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnKind = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnExtension = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnRegion = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.treeColumnVersion = ((Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn)(new Ch.Cyberduck.Ui.Winforms.Controls.SortComparatorOLVColumn()));
            this.transcriptBox = new System.Windows.Forms.RichTextBox();
            this.managedBookmarkPanel2 = new Ch.Cyberduck.Ui.Winforms.Controls.ManagedPanel();
            this.bookmarkListView = new Ch.Cyberduck.Ui.Winforms.Controls.LineSeparatedObjectListView();
            this.bookmarkImageColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.bookmarkDescriptionColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.activeColumn = ((BrightIdeasSoftware.OLVColumn)(new BrightIdeasSoftware.OLVColumn()));
            this.actionToolStrip = new System.Windows.Forms.ToolStrip();
            this.newBookmarkToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.editBookmarkToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.deleteBookmarkToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.viewPanel = new System.Windows.Forms.Panel();
            this.tableLayoutPanel1 = new System.Windows.Forms.TableLayoutPanel();
            this.viewToolStrip = new Ch.Cyberduck.Ui.Winforms.Controls.ClickThroughToolStrip();
            this.browserToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.bookmarksToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.historyToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.bonjourToolStripButton = new System.Windows.Forms.ToolStripButton();
            this.toolStripSeparator23 = new System.Windows.Forms.ToolStripSeparator();
            this.searchTextBox = new Ch.Cyberduck.ui.winforms.controls.SearchTextBox2();
            this.parentPathButton = new System.Windows.Forms.Button();
            this.pathComboBox = new System.Windows.Forms.ComboBox();
            this.historyForwardButton = new System.Windows.Forms.Button();
            this.historyBackButton = new System.Windows.Forms.Button();
            this.bookmarkContextMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.connectBookmarkContextToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator29 = new System.Windows.Forms.ToolStripSeparator();
            this.newBookmarkContextToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.deleteBookmarkContextToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.editBookmarkContextToolStripMenuItem1 = new System.Windows.Forms.ToolStripMenuItem();
            this.duplicateBookmarkToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
            this.saveFileDialog = new System.Windows.Forms.SaveFileDialog();
            this.folderBrowserDialog = new System.Windows.Forms.FolderBrowserDialog();
            this.openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.vistaMenu1 = new Ch.Cyberduck.Ui.Winforms.Controls.VistaMenu(this.components);
            this.toolbarContextMenu1 = new System.Windows.Forms.ContextMenu();
            this.openConnectionToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem91 = new System.Windows.Forms.MenuItem();
            this.quickConnectToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.actionContextToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem94 = new System.Windows.Forms.MenuItem();
            this.infoToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.refreshToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem97 = new System.Windows.Forms.MenuItem();
            this.editToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.openInWebBrowserToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.openInTerminalToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.newFolderToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.deleteToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem102 = new System.Windows.Forms.MenuItem();
            this.downloadToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.uploadToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.transfersToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.logToolbarMenuItem = new System.Windows.Forms.MenuItem();
            this.browserContextMenu = new System.Windows.Forms.ContextMenu();
            this.refreshBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem44 = new System.Windows.Forms.MenuItem();
            this.newFolderBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.newVaultBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.newFileBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.newSymlinkBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.renameBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.duplicateFileBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem96 = new System.Windows.Forms.MenuItem();
            this.copyUrlBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.openUrlBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.editBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.infoBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem105 = new System.Windows.Forms.MenuItem();
            this.downloadBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.downloadAsBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.downloadToBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.uploadBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.synchronizeBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem101 = new System.Windows.Forms.MenuItem();
            this.deleteBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.revertBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem110 = new System.Windows.Forms.MenuItem();
            this.createArchiveBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.expandArchiveBrowserContextMnuItem = new System.Windows.Forms.MenuItem();
            this.menuItem113 = new System.Windows.Forms.MenuItem();
            this.newBrowserBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.newBookmarkBrowserContextMenuItem = new System.Windows.Forms.MenuItem();
            this.bookmarkContextMenu = new System.Windows.Forms.ContextMenu();
            this.connectBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem5 = new System.Windows.Forms.MenuItem();
            this.newBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.deleteBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.editBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.duplicateBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.menuItem8 = new System.Windows.Forms.MenuItem();
            this.menuItem9 = new System.Windows.Forms.MenuItem();
            this.sortByNicknameBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.sortByHostnameBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.sortByProtocolBookmarkContextMenuItem = new System.Windows.Forms.MenuItem();
            this.statusStrip.SuspendLayout();
            this.menuStrip1.SuspendLayout();
            this.toolbarContextMenu.SuspendLayout();
            this.editorMenuStrip.SuspendLayout();
            this.toolBar.SuspendLayout();
            this.contextMenuStrip.SuspendLayout();
            this.toolStripContainer1.BottomToolStripPanel.SuspendLayout();
            this.toolStripContainer1.ContentPanel.SuspendLayout();
            this.toolStripContainer1.TopToolStripPanel.SuspendLayout();
            this.toolStripContainer1.SuspendLayout();
            this.panelManager1.SuspendLayout();
            this.managedBrowserPanel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer)).BeginInit();
            this.splitContainer.Panel1.SuspendLayout();
            this.splitContainer.Panel2.SuspendLayout();
            this.splitContainer.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.browser)).BeginInit();
            this.managedBookmarkPanel2.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.bookmarkListView)).BeginInit();
            this.actionToolStrip.SuspendLayout();
            this.viewPanel.SuspendLayout();
            this.tableLayoutPanel1.SuspendLayout();
            this.viewToolStrip.SuspendLayout();
            this.bookmarkContextMenuStrip.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.vistaMenu1)).BeginInit();
            this.SuspendLayout();
            // 
            // mainMenu
            // 
            this.mainMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.menuItem1,
            this.mainMenuItem2,
            this.menuItem39,
            this.menuItem50,
            this.menuItem64,
            this.menuItem75,
            this.menuItem80});
            // 
            // menuItem1
            // 
            this.menuItem1.Index = 0;
            this.menuItem1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.newBrowserMainMenuItem,
            this.openConnectionMainMenuItem,
            this.menuItem4,
            this.newDownloadMainMenuItem,
            this.menuItem6,
            this.newFolderMainMenuItem,
            this.newVaultMainMenuItem,
            this.newFileMainMenuItem,
            this.newSymbolicLinkMainMenuItem,
            this.renameMainMenuItem,
            this.duplicateMainMenuItem,
            this.menuItem11,
            this.openUrlMainMenuItem,
            this.editMainMenuItem,
            this.infoMainMenuItem,
            this.menuItem2,
            this.downloadMainMenuItem,
            this.downloadAsMainMenuItem,
            this.downloadToMainMenuItem,
            this.uploadMainMenuItem,
            this.synchronizeMainMenuItem,
            this.menuItem21,
            this.deleteMainMenuItem,
            this.revertMainMenuItem,
            this.menuItem24,
            this.createArchiveMainMenuItem,
            this.expandArchiveMainMenuItem,
            this.menuItem27,
            this.printMainMenuItem,
            this.menuItem29,
            this.exitMainMenuItem});
            this.menuItem1.Text = "&File";
            // 
            // newBrowserMainMenuItem
            // 
            this.newBrowserMainMenuItem.Index = 0;
            this.newBrowserMainMenuItem.Text = "New Browser";
            // 
            // openConnectionMainMenuItem
            // 
            this.openConnectionMainMenuItem.Index = 1;
            this.openConnectionMainMenuItem.Text = "Open Connection…";
            // 
            // menuItem4
            // 
            this.menuItem4.Index = 2;
            this.menuItem4.Text = "-";
            // 
            // newDownloadMainMenuItem
            // 
            this.newDownloadMainMenuItem.Index = 3;
            this.newDownloadMainMenuItem.Text = "New Download";
            // 
            // menuItem6
            // 
            this.menuItem6.Index = 4;
            this.menuItem6.Text = "-";
            // 
            // newFolderMainMenuItem
            // 
            this.newFolderMainMenuItem.Index = 5;
            this.newFolderMainMenuItem.Text = "New Folder…";
            // 
            // newVaultMainMenuItem
            // 
            this.newVaultMainMenuItem.Index = 6;
            this.newVaultMainMenuItem.Text = "New Encrypted Vault…";
            // 
            // newFileMainMenuItem
            // 
            this.newFileMainMenuItem.Index = 7;
            this.newFileMainMenuItem.Text = "New File…";
            // 
            // newSymbolicLinkMainMenuItem
            // 
            this.newSymbolicLinkMainMenuItem.Index = 8;
            this.newSymbolicLinkMainMenuItem.Text = "New Symbolic Link…";
            // 
            // renameMainMenuItem
            // 
            this.renameMainMenuItem.Index = 9;
            this.renameMainMenuItem.Text = "Rename…";
            // 
            // duplicateMainMenuItem
            // 
            this.duplicateMainMenuItem.Index = 10;
            this.duplicateMainMenuItem.Text = "Duplicate…";
            // 
            // menuItem11
            // 
            this.menuItem11.Index = 11;
            this.menuItem11.Text = "-";
            // 
            // openUrlMainMenuItem
            // 
            this.openUrlMainMenuItem.Index = 12;
            this.openUrlMainMenuItem.Text = "Open URL";
            // 
            // editMainMenuItem
            // 
            this.editMainMenuItem.Index = 13;
            this.editMainMenuItem.Text = "Edit With";
            // 
            // infoMainMenuItem
            // 
            this.infoMainMenuItem.Index = 14;
            this.infoMainMenuItem.Text = "Info";
            // 
            // menuItem2
            // 
            this.menuItem2.Index = 15;
            this.menuItem2.Text = "-";
            // 
            // downloadMainMenuItem
            // 
            this.downloadMainMenuItem.Index = 16;
            this.downloadMainMenuItem.Text = "Download";
            // 
            // downloadAsMainMenuItem
            // 
            this.downloadAsMainMenuItem.Index = 17;
            this.downloadAsMainMenuItem.Text = "Download As…";
            // 
            // downloadToMainMenuItem
            // 
            this.downloadToMainMenuItem.Index = 18;
            this.downloadToMainMenuItem.Text = "Download To…";
            // 
            // uploadMainMenuItem
            // 
            this.uploadMainMenuItem.Index = 19;
            this.uploadMainMenuItem.Text = "Upload…";
            // 
            // synchronizeMainMenuItem
            // 
            this.synchronizeMainMenuItem.Index = 20;
            this.synchronizeMainMenuItem.Text = "Synchronize…";
            // 
            // menuItem21
            // 
            this.menuItem21.Index = 21;
            this.menuItem21.Text = "-";
            // 
            // deleteMainMenuItem
            // 
            this.deleteMainMenuItem.Index = 22;
            this.deleteMainMenuItem.Text = "Delete";
            // 
            // revertMainMenuItem
            // 
            this.revertMainMenuItem.Index = 23;
            this.revertMainMenuItem.Text = "Revert";
            // 
            // menuItem24
            // 
            this.menuItem24.Index = 24;
            this.menuItem24.Text = "-";
            // 
            // createArchiveMainMenuItem
            // 
            this.createArchiveMainMenuItem.Index = 25;
            this.createArchiveMainMenuItem.Text = "Create Archive";
            // 
            // expandArchiveMainMenuItem
            // 
            this.expandArchiveMainMenuItem.Index = 26;
            this.expandArchiveMainMenuItem.Text = "Expand Archive";
            // 
            // menuItem27
            // 
            this.menuItem27.Index = 27;
            this.menuItem27.Text = "-";
            // 
            // printMainMenuItem
            // 
            this.printMainMenuItem.Enabled = false;
            this.printMainMenuItem.Index = 28;
            this.printMainMenuItem.Text = "Print…";
            // 
            // menuItem29
            // 
            this.menuItem29.Index = 29;
            this.menuItem29.Text = "-";
            // 
            // exitMainMenuItem
            // 
            this.exitMainMenuItem.Index = 30;
            this.exitMainMenuItem.Text = "Exit";
            // 
            // mainMenuItem2
            // 
            this.mainMenuItem2.Index = 1;
            this.mainMenuItem2.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.cutMainMenuItem,
            this.copyMainMenuItem,
            this.copyUrlMainMenuItem,
            this.pasteMainMenuItem,
            this.selectAllMainMenuItem,
            this.menuItem37,
            this.preferencesMainMenuItem});
            this.mainMenuItem2.Text = "&Edit";
            // 
            // cutMainMenuItem
            // 
            this.cutMainMenuItem.Index = 0;
            this.cutMainMenuItem.Text = "Cut";
            // 
            // copyMainMenuItem
            // 
            this.copyMainMenuItem.Index = 1;
            this.copyMainMenuItem.Text = "Copy";
            // 
            // copyUrlMainMenuItem
            // 
            this.copyUrlMainMenuItem.Index = 2;
            this.copyUrlMainMenuItem.Text = "Copy URL";
            // 
            // pasteMainMenuItem
            // 
            this.pasteMainMenuItem.Index = 3;
            this.pasteMainMenuItem.Text = "Paste";
            // 
            // selectAllMainMenuItem
            // 
            this.selectAllMainMenuItem.Index = 4;
            this.selectAllMainMenuItem.Text = "Select All";
            // 
            // menuItem37
            // 
            this.menuItem37.Index = 5;
            this.menuItem37.Text = "-";
            // 
            // preferencesMainMenuItem
            // 
            this.preferencesMainMenuItem.Index = 6;
            this.preferencesMainMenuItem.Text = "Preferences…";
            // 
            // menuItem39
            // 
            this.menuItem39.Index = 2;
            this.menuItem39.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.toggleToolbarMainMenuItem,
            this.customizeToolbarMainMenuItem,
            this.menuItem43,
            this.columnMainMenuItem,
            this.menuItem45,
            this.showHiddenFilesMainMenuItem,
            this.textEncodingMainMenuItem,
            this.menuItem48,
            this.toggleLogDrawerMainMenuItem});
            this.menuItem39.Text = "&View";
            // 
            // toggleToolbarMainMenuItem
            // 
            this.toggleToolbarMainMenuItem.Index = 0;
            this.toggleToolbarMainMenuItem.Text = "Hide Toolbar";
            // 
            // customizeToolbarMainMenuItem
            // 
            this.customizeToolbarMainMenuItem.Index = 1;
            this.customizeToolbarMainMenuItem.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.menuItem42});
            this.customizeToolbarMainMenuItem.Text = "Customize Toolbar…";
            this.customizeToolbarMainMenuItem.Popup += new System.EventHandler(this.customizeToolbarMenuItem_Popup);
            // 
            // menuItem42
            // 
            this.menuItem42.Index = 0;
            this.menuItem42.Text = "noch";
            // 
            // menuItem43
            // 
            this.menuItem43.Index = 2;
            this.menuItem43.Text = "-";
            // 
            // columnMainMenuItem
            // 
            this.columnMainMenuItem.Index = 3;
            this.columnMainMenuItem.Text = "Column";
            this.columnMainMenuItem.Popup += new System.EventHandler(this.columnMenuItem_Popup);
            // 
            // menuItem45
            // 
            this.menuItem45.Index = 4;
            this.menuItem45.Text = "-";
            // 
            // showHiddenFilesMainMenuItem
            // 
            this.showHiddenFilesMainMenuItem.Index = 5;
            this.showHiddenFilesMainMenuItem.Text = "Show Hidden Files";
            // 
            // textEncodingMainMenuItem
            // 
            this.textEncodingMainMenuItem.Index = 6;
            this.textEncodingMainMenuItem.Text = "Text Encoding";
            // 
            // menuItem48
            // 
            this.menuItem48.Index = 7;
            this.menuItem48.Text = "-";
            // 
            // toggleLogDrawerMainMenuItem
            // 
            this.toggleLogDrawerMainMenuItem.Index = 8;
            this.toggleLogDrawerMainMenuItem.Text = "Toggle Log Drawer";
            // 
            // menuItem50
            // 
            this.menuItem50.Index = 3;
            this.menuItem50.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.refreshMainMenuItem,
            this.menuItem52,
            this.goToFolderMainMenuItem,
            this.backMainMenuItem,
            this.forwardMainMenuItem,
            this.enclosingFolderMainMenuItem,
            this.insideMainMenuItem,
            this.searchMainMenuItem,
            this.menuItem59,
            this.sendCommandMainMenuItem,
            this.openInTerminalMainMenuItem,
            this.menuItem61,
            this.stopMainMenuItem,
            this.disconnectMainMenuItem});
            this.menuItem50.Text = "&Go";
            // 
            // refreshMainMenuItem
            // 
            this.refreshMainMenuItem.Index = 0;
            this.refreshMainMenuItem.Text = "Refresh";
            // 
            // menuItem52
            // 
            this.menuItem52.Index = 1;
            this.menuItem52.Text = "-";
            // 
            // goToFolderMainMenuItem
            // 
            this.goToFolderMainMenuItem.Index = 2;
            this.goToFolderMainMenuItem.Text = "Go to Folder…";
            // 
            // backMainMenuItem
            // 
            this.backMainMenuItem.Index = 3;
            this.backMainMenuItem.Text = "Back";
            // 
            // forwardMainMenuItem
            // 
            this.forwardMainMenuItem.Index = 4;
            this.forwardMainMenuItem.Text = "Forward";
            // 
            // enclosingFolderMainMenuItem
            // 
            this.enclosingFolderMainMenuItem.Index = 5;
            this.enclosingFolderMainMenuItem.Text = "Enclosing Folder";
            // 
            // insideMainMenuItem
            // 
            this.insideMainMenuItem.Index = 6;
            this.insideMainMenuItem.Text = "Inside";
            // 
            // searchMainMenuItem
            // 
            this.searchMainMenuItem.Index = 7;
            this.searchMainMenuItem.Text = "Search…";
            // 
            // menuItem59
            // 
            this.menuItem59.Index = 8;
            this.menuItem59.Text = "-";
            // 
            // sendCommandMainMenuItem
            // 
            this.sendCommandMainMenuItem.Index = 9;
            this.sendCommandMainMenuItem.Text = "Send Command…";
            // 
            // openInTerminalMainMenuItem
            // 
            this.openInTerminalMainMenuItem.Index = 10;
            this.openInTerminalMainMenuItem.Text = "Open in Terminal";
            // 
            // menuItem61
            // 
            this.menuItem61.Index = 11;
            this.menuItem61.Text = "-";
            // 
            // stopMainMenuItem
            // 
            this.stopMainMenuItem.Index = 12;
            this.stopMainMenuItem.Text = "Stop";
            // 
            // disconnectMainMenuItem
            // 
            this.disconnectMainMenuItem.Index = 13;
            this.disconnectMainMenuItem.Text = "Disconnect";
            // 
            // menuItem64
            // 
            this.menuItem64.Index = 4;
            this.menuItem64.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.toggleBookmarksMainMenuItem,
            this.menuItem3,
            this.menuItem66,
            this.newBookmarkMainMenuItem,
            this.deleteBookmarkMainMenuItem,
            this.editBookmarkMainMenuItem,
            this.duplicateBookmarkMainMenuItem,
            this.menuItem71,
            this.historyMainMenuItem,
            this.bonjourMainMenuItem,
            this.menuItem74});
            this.menuItem64.Text = "&Bookmark";
            // 
            // toggleBookmarksMainMenuItem
            // 
            this.toggleBookmarksMainMenuItem.Index = 0;
            this.toggleBookmarksMainMenuItem.Text = "Toggle Bookmarks";
            // 
            // menuItem3
            // 
            this.menuItem3.Index = 1;
            this.menuItem3.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.sortByNicknameMainMenuItem,
            this.sortByHostnameMainMenuItem,
            this.sortByProtocolMainMenuItem});
            this.menuItem3.Text = "Sort By";
            // 
            // sortByNicknameMainMenuItem
            // 
            this.sortByNicknameMainMenuItem.Index = 0;
            this.sortByNicknameMainMenuItem.Text = "Nickname";
            // 
            // sortByHostnameMainMenuItem
            // 
            this.sortByHostnameMainMenuItem.Index = 1;
            this.sortByHostnameMainMenuItem.Text = "Hostname";
            // 
            // sortByProtocolMainMenuItem
            // 
            this.sortByProtocolMainMenuItem.Index = 2;
            this.sortByProtocolMainMenuItem.Text = "Protocol";
            // 
            // menuItem66
            // 
            this.menuItem66.Index = 2;
            this.menuItem66.Text = "-";
            // 
            // newBookmarkMainMenuItem
            // 
            this.newBookmarkMainMenuItem.Index = 3;
            this.newBookmarkMainMenuItem.Text = "New Bookmark";
            // 
            // deleteBookmarkMainMenuItem
            // 
            this.deleteBookmarkMainMenuItem.Index = 4;
            this.deleteBookmarkMainMenuItem.Text = "Delete Bookmark";
            // 
            // editBookmarkMainMenuItem
            // 
            this.editBookmarkMainMenuItem.Index = 5;
            this.editBookmarkMainMenuItem.Text = "Edit Bookmark";
            // 
            // duplicateBookmarkMainMenuItem
            // 
            this.duplicateBookmarkMainMenuItem.Index = 6;
            this.duplicateBookmarkMainMenuItem.Text = "Duplicate Bookmark";
            // 
            // menuItem71
            // 
            this.menuItem71.Index = 7;
            this.menuItem71.Text = "-";
            // 
            // historyMainMenuItem
            // 
            this.historyMainMenuItem.Index = 8;
            this.historyMainMenuItem.Text = "History";
            // 
            // bonjourMainMenuItem
            // 
            this.bonjourMainMenuItem.Index = 9;
            this.bonjourMainMenuItem.Text = "Bonjour";
            // 
            // menuItem74
            // 
            this.menuItem74.Index = 10;
            this.menuItem74.Text = "-";
            // 
            // menuItem75
            // 
            this.menuItem75.Index = 5;
            this.menuItem75.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.minimizeMainMenuItem,
            this.menuItem77,
            this.transfersMainMenuItem,
            this.activityMainMenuItem});
            this.menuItem75.Text = "&Window";
            // 
            // minimizeMainMenuItem
            // 
            this.minimizeMainMenuItem.Index = 0;
            this.minimizeMainMenuItem.Text = "Minimize";
            // 
            // menuItem77
            // 
            this.menuItem77.Index = 1;
            this.menuItem77.Text = "-";
            // 
            // transfersMainMenuItem
            // 
            this.transfersMainMenuItem.Index = 2;
            this.transfersMainMenuItem.Text = "Transfers";
            // 
            // activityMainMenuItem
            // 
            this.activityMainMenuItem.Index = 3;
            this.activityMainMenuItem.Text = "Activity";
            // 
            // menuItem80
            // 
            this.menuItem80.Index = 6;
            this.menuItem80.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.helpMainMenuItem,
            this.licenseMainMenuItem,
            this.acknowledgmentsMainMenuItem,
            this.menuItem84,
            this.bugMainMenuItem,
            this.menuItem86,
            this.updateMainMenuItem,
            this.menuItem88,
            this.donateMainMenuItem,
            this.keyMainMenuItem,
            this.menuItem7,
            this.aboutMainMenuItem});
            this.menuItem80.Text = "&Help";
            // 
            // helpMainMenuItem
            // 
            this.helpMainMenuItem.Index = 0;
            this.helpMainMenuItem.Text = "Cyberduck Help";
            // 
            // licenseMainMenuItem
            // 
            this.licenseMainMenuItem.Index = 1;
            this.licenseMainMenuItem.Text = "License";
            // 
            // acknowledgmentsMainMenuItem
            // 
            this.acknowledgmentsMainMenuItem.Index = 2;
            this.acknowledgmentsMainMenuItem.Text = "Acknowledgments";
            // 
            // menuItem84
            // 
            this.menuItem84.Index = 3;
            this.menuItem84.Text = "-";
            // 
            // bugMainMenuItem
            // 
            this.bugMainMenuItem.Index = 4;
            this.bugMainMenuItem.Text = "Report a Bug";
            // 
            // menuItem86
            // 
            this.menuItem86.Index = 5;
            this.menuItem86.Text = "-";
            // 
            // updateMainMenuItem
            // 
            this.updateMainMenuItem.Index = 6;
            this.updateMainMenuItem.Text = "Check for Update…";
            // 
            // menuItem88
            // 
            this.menuItem88.Index = 7;
            this.menuItem88.Text = "-";
            // 
            // donateMainMenuItem
            // 
            this.donateMainMenuItem.Index = 8;
            this.donateMainMenuItem.Text = "Donate…";
            // 
            // keyMainMenuItem
            // 
            this.keyMainMenuItem.Index = 9;
            this.keyMainMenuItem.Text = "Registered to…";
            // 
            // menuItem7
            // 
            this.menuItem7.Index = 10;
            this.menuItem7.Text = "-";
            // 
            // aboutMainMenuItem
            // 
            this.aboutMainMenuItem.Index = 11;
            this.aboutMainMenuItem.Text = "About Cyberduck";
            // 
            // statusStrip
            // 
            this.statusStrip.Dock = System.Windows.Forms.DockStyle.None;
            this.statusStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toolStripProgress,
            this.statusLabel,
            this.securityToolStripStatusLabel});
            this.statusStrip.Location = new System.Drawing.Point(0, 0);
            this.statusStrip.Name = "statusStrip";
            this.statusStrip.Padding = new System.Windows.Forms.Padding(1, 0, 16, 0);
            this.statusStrip.Size = new System.Drawing.Size(1028, 22);
            this.statusStrip.TabIndex = 9;
            // 
            // toolStripProgress
            // 
            this.toolStripProgress.AutoSize = false;
            this.toolStripProgress.Image = ((System.Drawing.Image)(resources.GetObject("toolStripProgress.Image")));
            this.toolStripProgress.Margin = new System.Windows.Forms.Padding(2, 3, 0, 2);
            this.toolStripProgress.Name = "toolStripProgress";
            this.toolStripProgress.Size = new System.Drawing.Size(16, 17);
            // 
            // statusLabel
            // 
            this.statusLabel.Name = "statusLabel";
            this.statusLabel.Size = new System.Drawing.Size(970, 17);
            this.statusLabel.Spring = true;
            this.statusLabel.TextAlign = System.Drawing.ContentAlignment.MiddleLeft;
            // 
            // securityToolStripStatusLabel
            // 
            this.securityToolStripStatusLabel.Image = ((System.Drawing.Image)(resources.GetObject("securityToolStripStatusLabel.Image")));
            this.securityToolStripStatusLabel.Name = "securityToolStripStatusLabel";
            this.securityToolStripStatusLabel.Padding = new System.Windows.Forms.Padding(0, 0, 7, 0);
            this.securityToolStripStatusLabel.Size = new System.Drawing.Size(23, 17);
            this.securityToolStripStatusLabel.Click += new System.EventHandler(this.securityToolStripStatusLabel_Click);
            // 
            // menuStrip1
            // 
            this.menuStrip1.Dock = System.Windows.Forms.DockStyle.None;
            this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.fileToolStripMenuItem,
            this.editToolStripMenuItem,
            this.viewToolStripMenuItem,
            this.goToolStripMenuItem,
            this.bookmarkToolStripMenuItem,
            this.windowToolStripMenuItem,
            this.helpToolStripMenuItem});
            this.menuStrip1.Location = new System.Drawing.Point(0, 56);
            this.menuStrip1.Name = "menuStrip1";
            this.menuStrip1.Padding = new System.Windows.Forms.Padding(7, 2, 0, 2);
            this.menuStrip1.Size = new System.Drawing.Size(343, 24);
            this.menuStrip1.TabIndex = 12;
            this.menuStrip1.Text = "menuStrip1";
            this.menuStrip1.Visible = false;
            // 
            // fileToolStripMenuItem
            // 
            this.fileToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.newBrowserToolStripMenuItem,
            this.openConnectionToolStripMenuItem,
            this.toolStripSeparator7,
            this.newDownloadToolStripMenuItem,
            this.toolStripSeparator8,
            this.newFolderToolStripMenuItem,
            this.newVaultToolStripMenuItem,
            this.newFileToolStripMenuItem,
            this.renameFileToolStripMenuItem,
            this.duplicateFileToolStripMenuItem,
            this.toolStripSeparator9,
            this.openWebURLToolStripMenuItem,
            this.editWithToolStripMenuItem,
            this.infoToolStripMenuItem,
            this.toolStripSeparator10,
            this.downloadToolStripMenuItem,
            this.downloadAsToolStripMenuItem,
            this.downloadToToolStripMenuItem,
            this.uploadToolStripMenuItem,
            this.synchronizeToolStripMenuItem,
            this.toolStripSeparator25,
            this.deleteToolStripMenuItem,
            this.revertToolStripMenuItem,
            this.toolStripSeparator11,
            this.createArchiveToolStripMenuItem,
            this.expandArchiveToolStripMenuItem,
            this.toolStripSeparator12,
            this.printToolStripMenuItem,
            this.toolStripSeparator35,
            this.exitToolStripMenuItem});
            this.fileToolStripMenuItem.Name = "fileToolStripMenuItem";
            this.fileToolStripMenuItem.Size = new System.Drawing.Size(37, 20);
            this.fileToolStripMenuItem.Text = "&File";
            // 
            // newBrowserToolStripMenuItem
            // 
            this.newBrowserToolStripMenuItem.Name = "newBrowserToolStripMenuItem";
            this.newBrowserToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.newBrowserToolStripMenuItem.Text = "New Browser";
            // 
            // openConnectionToolStripMenuItem
            // 
            this.openConnectionToolStripMenuItem.Name = "openConnectionToolStripMenuItem";
            this.openConnectionToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.openConnectionToolStripMenuItem.Text = "Open Connection…";
            // 
            // toolStripSeparator7
            // 
            this.toolStripSeparator7.Name = "toolStripSeparator7";
            this.toolStripSeparator7.Size = new System.Drawing.Size(196, 6);
            // 
            // newDownloadToolStripMenuItem
            // 
            this.newDownloadToolStripMenuItem.Enabled = false;
            this.newDownloadToolStripMenuItem.Name = "newDownloadToolStripMenuItem";
            this.newDownloadToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.newDownloadToolStripMenuItem.Text = "New Download";
            // 
            // toolStripSeparator8
            // 
            this.toolStripSeparator8.Name = "toolStripSeparator8";
            this.toolStripSeparator8.Size = new System.Drawing.Size(196, 6);
            // 
            // newFolderToolStripMenuItem
            // 
            this.newFolderToolStripMenuItem.Name = "newFolderToolStripMenuItem";
            this.newFolderToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.newFolderToolStripMenuItem.Text = "New Folder…";
            // 
            // newVaultToolStripMenuItem
            // 
            this.newVaultToolStripMenuItem.Name = "newVaultToolStripMenuItem";
            this.newVaultToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.newVaultToolStripMenuItem.Text = "New Encrypted Folder…";
            // 
            // newFileToolStripMenuItem
            // 
            this.newFileToolStripMenuItem.Name = "newFileToolStripMenuItem";
            this.newFileToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.newFileToolStripMenuItem.Text = "New File…";
            // 
            // renameFileToolStripMenuItem
            // 
            this.renameFileToolStripMenuItem.Name = "renameFileToolStripMenuItem";
            this.renameFileToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.renameFileToolStripMenuItem.Text = "Rename…";
            // 
            // duplicateFileToolStripMenuItem
            // 
            this.duplicateFileToolStripMenuItem.Name = "duplicateFileToolStripMenuItem";
            this.duplicateFileToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.duplicateFileToolStripMenuItem.Text = "Duplicate…";
            // 
            // toolStripSeparator9
            // 
            this.toolStripSeparator9.Name = "toolStripSeparator9";
            this.toolStripSeparator9.Size = new System.Drawing.Size(196, 6);
            // 
            // openWebURLToolStripMenuItem
            // 
            this.openWebURLToolStripMenuItem.Name = "openWebURLToolStripMenuItem";
            this.openWebURLToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.openWebURLToolStripMenuItem.Text = "Open Web URL";
            // 
            // editWithToolStripMenuItem
            // 
            this.editWithToolStripMenuItem.Name = "editWithToolStripMenuItem";
            this.editWithToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.editWithToolStripMenuItem.Text = "Edit";
            // 
            // infoToolStripMenuItem
            // 
            this.infoToolStripMenuItem.Name = "infoToolStripMenuItem";
            this.infoToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.infoToolStripMenuItem.Text = "Info";
            // 
            // toolStripSeparator10
            // 
            this.toolStripSeparator10.Name = "toolStripSeparator10";
            this.toolStripSeparator10.Size = new System.Drawing.Size(196, 6);
            // 
            // downloadToolStripMenuItem
            // 
            this.downloadToolStripMenuItem.Name = "downloadToolStripMenuItem";
            this.downloadToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.downloadToolStripMenuItem.Text = "Download";
            // 
            // downloadAsToolStripMenuItem
            // 
            this.downloadAsToolStripMenuItem.Name = "downloadAsToolStripMenuItem";
            this.downloadAsToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.downloadAsToolStripMenuItem.Text = "Download As…";
            // 
            // downloadToToolStripMenuItem
            // 
            this.downloadToToolStripMenuItem.Name = "downloadToToolStripMenuItem";
            this.downloadToToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.downloadToToolStripMenuItem.Text = "Download To…";
            // 
            // uploadToolStripMenuItem
            // 
            this.uploadToolStripMenuItem.Name = "uploadToolStripMenuItem";
            this.uploadToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.uploadToolStripMenuItem.Text = "Upload…";
            // 
            // synchronizeToolStripMenuItem
            // 
            this.synchronizeToolStripMenuItem.Name = "synchronizeToolStripMenuItem";
            this.synchronizeToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.synchronizeToolStripMenuItem.Text = "Synchronize…";
            // 
            // toolStripSeparator25
            // 
            this.toolStripSeparator25.Name = "toolStripSeparator25";
            this.toolStripSeparator25.Size = new System.Drawing.Size(196, 6);
            // 
            // deleteToolStripMenuItem
            // 
            this.deleteToolStripMenuItem.Name = "deleteToolStripMenuItem";
            this.deleteToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.deleteToolStripMenuItem.Text = "Delete";
            // 
            // revertToolStripMenuItem
            // 
            this.revertToolStripMenuItem.Name = "revertToolStripMenuItem";
            this.revertToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.revertToolStripMenuItem.Text = "Revert";
            // 
            // toolStripSeparator11
            // 
            this.toolStripSeparator11.Name = "toolStripSeparator11";
            this.toolStripSeparator11.Size = new System.Drawing.Size(196, 6);
            // 
            // createArchiveToolStripMenuItem
            // 
            this.createArchiveToolStripMenuItem.DropDown = this.archiveMenuStrip;
            this.createArchiveToolStripMenuItem.Name = "createArchiveToolStripMenuItem";
            this.createArchiveToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.createArchiveToolStripMenuItem.Text = "Create Archive";
            // 
            // archiveMenuStrip
            // 
            this.archiveMenuStrip.Name = "archiveMenuStrip";
            this.archiveMenuStrip.OwnerItem = this.createArchiveToolStripMenuItem;
            this.archiveMenuStrip.Size = new System.Drawing.Size(61, 4);
            // 
            // createArchiveContextToolStripMenuItem
            // 
            this.createArchiveContextToolStripMenuItem.DropDown = this.archiveMenuStrip;
            this.createArchiveContextToolStripMenuItem.Name = "createArchiveContextToolStripMenuItem";
            this.createArchiveContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.createArchiveContextToolStripMenuItem.Text = "Create Archive";
            // 
            // expandArchiveToolStripMenuItem
            // 
            this.expandArchiveToolStripMenuItem.Name = "expandArchiveToolStripMenuItem";
            this.expandArchiveToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.expandArchiveToolStripMenuItem.Text = "Expand Archive";
            // 
            // toolStripSeparator12
            // 
            this.toolStripSeparator12.Name = "toolStripSeparator12";
            this.toolStripSeparator12.Size = new System.Drawing.Size(196, 6);
            // 
            // printToolStripMenuItem
            // 
            this.printToolStripMenuItem.Enabled = false;
            this.printToolStripMenuItem.Name = "printToolStripMenuItem";
            this.printToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.printToolStripMenuItem.Text = "Print…";
            // 
            // toolStripSeparator35
            // 
            this.toolStripSeparator35.Name = "toolStripSeparator35";
            this.toolStripSeparator35.Size = new System.Drawing.Size(196, 6);
            // 
            // exitToolStripMenuItem
            // 
            this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
            this.exitToolStripMenuItem.Size = new System.Drawing.Size(199, 22);
            this.exitToolStripMenuItem.Text = "Exit";
            // 
            // editToolStripMenuItem
            // 
            this.editToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.cutToolStripMenuItem,
            this.copyToolStripMenuItem,
            this.copyURLToolStripMenuItem,
            this.pasteToolStripMenuItem,
            this.selectAllToolStripMenuItem,
            this.toolStripSeparator14,
            this.preferencesToolStripMenuItem});
            this.editToolStripMenuItem.Name = "editToolStripMenuItem";
            this.editToolStripMenuItem.Size = new System.Drawing.Size(39, 20);
            this.editToolStripMenuItem.Text = "&Edit";
            // 
            // cutToolStripMenuItem
            // 
            this.cutToolStripMenuItem.Enabled = false;
            this.cutToolStripMenuItem.Name = "cutToolStripMenuItem";
            this.cutToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.cutToolStripMenuItem.Text = "Cut";
            // 
            // copyToolStripMenuItem
            // 
            this.copyToolStripMenuItem.Enabled = false;
            this.copyToolStripMenuItem.Name = "copyToolStripMenuItem";
            this.copyToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.copyToolStripMenuItem.Text = "Copy";
            // 
            // copyURLToolStripMenuItem
            // 
            this.copyURLToolStripMenuItem.Enabled = false;
            this.copyURLToolStripMenuItem.Name = "copyURLToolStripMenuItem";
            this.copyURLToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.copyURLToolStripMenuItem.Text = "Copy URL";
            // 
            // pasteToolStripMenuItem
            // 
            this.pasteToolStripMenuItem.Enabled = false;
            this.pasteToolStripMenuItem.Name = "pasteToolStripMenuItem";
            this.pasteToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.pasteToolStripMenuItem.Text = "Paste";
            // 
            // selectAllToolStripMenuItem
            // 
            this.selectAllToolStripMenuItem.Name = "selectAllToolStripMenuItem";
            this.selectAllToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.selectAllToolStripMenuItem.Text = "Select All";
            // 
            // toolStripSeparator14
            // 
            this.toolStripSeparator14.Name = "toolStripSeparator14";
            this.toolStripSeparator14.Size = new System.Drawing.Size(141, 6);
            // 
            // preferencesToolStripMenuItem
            // 
            this.preferencesToolStripMenuItem.Name = "preferencesToolStripMenuItem";
            this.preferencesToolStripMenuItem.Size = new System.Drawing.Size(144, 22);
            this.preferencesToolStripMenuItem.Text = "Preferences…";
            // 
            // viewToolStripMenuItem
            // 
            this.viewToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.toggleToolbarToolStripMenuItem,
            this.toolbarToolStripMenuItem,
            this.toolStripSeparator13,
            this.columnToolStripMenuItem,
            this.toolStripSeparator34,
            this.showHiddenFilesToolStripMenuItem,
            this.textEncodingToolStripMenuItem,
            this.toolStripSeparator15,
            this.toggleLogDrawerToolStripMenuItem});
            this.viewToolStripMenuItem.Name = "viewToolStripMenuItem";
            this.viewToolStripMenuItem.Size = new System.Drawing.Size(44, 20);
            this.viewToolStripMenuItem.Text = "&View";
            // 
            // toggleToolbarToolStripMenuItem
            // 
            this.toggleToolbarToolStripMenuItem.Name = "toggleToolbarToolStripMenuItem";
            this.toggleToolbarToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.toggleToolbarToolStripMenuItem.Text = "Hide Toolbar";
            // 
            // toolbarToolStripMenuItem
            // 
            this.toolbarToolStripMenuItem.DropDown = this.toolbarContextMenu;
            this.toolbarToolStripMenuItem.Name = "toolbarToolStripMenuItem";
            this.toolbarToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.toolbarToolStripMenuItem.Text = "Customize Toolbar…";
            // 
            // toolbarContextMenu
            // 
            this.toolbarContextMenu.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.openConnectionToolStripMenuItem1,
            this.toolStripSeparator30,
            this.quickConnectToolStripMenuItem,
            this.actionToolStripMenuItem,
            this.toolStripSeparator31,
            this.infoToolStripMenuItem1,
            this.refreshToolStripMenuItem1,
            this.toolStripSeparator32,
            this.editToolStripMenuItem1,
            this.openInWebBrowserToolStripMenuItem,
            this.openInTerminalToolStripMenuItem,
            this.newFolderToolStripMenuItem1,
            this.deleteToolStripMenuItem1,
            this.toolStripSeparator33,
            this.downloadToolStripMenuItem1,
            this.uploadToolStripMenuItem1,
            this.transfersToolStripMenuItem1,
            this.logToolStripMenuItem1});
            this.toolbarContextMenu.Name = "toolbarContextMenu";
            this.toolbarContextMenu.OwnerItem = this.toolbarToolStripMenuItem;
            this.toolbarContextMenu.Size = new System.Drawing.Size(189, 336);
            this.toolbarContextMenu.Closing += new System.Windows.Forms.ToolStripDropDownClosingEventHandler(this.toolbarContextMenu_Closing);
            this.toolbarContextMenu.ItemClicked += new System.Windows.Forms.ToolStripItemClickedEventHandler(this.toolbarContextMenu_ItemClicked);
            // 
            // openConnectionToolStripMenuItem1
            // 
            this.openConnectionToolStripMenuItem1.Checked = true;
            this.openConnectionToolStripMenuItem1.CheckState = System.Windows.Forms.CheckState.Checked;
            this.openConnectionToolStripMenuItem1.Name = "openConnectionToolStripMenuItem1";
            this.openConnectionToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.openConnectionToolStripMenuItem1.Text = "New Connection";
            // 
            // toolStripSeparator30
            // 
            this.toolStripSeparator30.Name = "toolStripSeparator30";
            this.toolStripSeparator30.Size = new System.Drawing.Size(185, 6);
            // 
            // quickConnectToolStripMenuItem
            // 
            this.quickConnectToolStripMenuItem.Name = "quickConnectToolStripMenuItem";
            this.quickConnectToolStripMenuItem.Size = new System.Drawing.Size(188, 22);
            this.quickConnectToolStripMenuItem.Text = "Quick Connect";
            // 
            // actionToolStripMenuItem
            // 
            this.actionToolStripMenuItem.Name = "actionToolStripMenuItem";
            this.actionToolStripMenuItem.Size = new System.Drawing.Size(188, 22);
            this.actionToolStripMenuItem.Text = "Action";
            // 
            // toolStripSeparator31
            // 
            this.toolStripSeparator31.Name = "toolStripSeparator31";
            this.toolStripSeparator31.Size = new System.Drawing.Size(185, 6);
            // 
            // infoToolStripMenuItem1
            // 
            this.infoToolStripMenuItem1.Name = "infoToolStripMenuItem1";
            this.infoToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.infoToolStripMenuItem1.Text = "Info";
            // 
            // refreshToolStripMenuItem1
            // 
            this.refreshToolStripMenuItem1.Name = "refreshToolStripMenuItem1";
            this.refreshToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.refreshToolStripMenuItem1.Text = "Refresh";
            // 
            // toolStripSeparator32
            // 
            this.toolStripSeparator32.Name = "toolStripSeparator32";
            this.toolStripSeparator32.Size = new System.Drawing.Size(185, 6);
            // 
            // editToolStripMenuItem1
            // 
            this.editToolStripMenuItem1.Name = "editToolStripMenuItem1";
            this.editToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.editToolStripMenuItem1.Text = "Edit";
            // 
            // openInWebBrowserToolStripMenuItem
            // 
            this.openInWebBrowserToolStripMenuItem.Name = "openInWebBrowserToolStripMenuItem";
            this.openInWebBrowserToolStripMenuItem.Size = new System.Drawing.Size(188, 22);
            this.openInWebBrowserToolStripMenuItem.Text = "Open in Web Browser";
            // 
            // openInTerminalToolStripMenuItem
            // 
            this.openInTerminalToolStripMenuItem.Name = "openInTerminalToolStripMenuItem";
            this.openInTerminalToolStripMenuItem.Size = new System.Drawing.Size(188, 22);
            this.openInTerminalToolStripMenuItem.Text = "Open in Terminal";
            // 
            // newFolderToolStripMenuItem1
            // 
            this.newFolderToolStripMenuItem1.Name = "newFolderToolStripMenuItem1";
            this.newFolderToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.newFolderToolStripMenuItem1.Text = "New Folder";
            // 
            // deleteToolStripMenuItem1
            // 
            this.deleteToolStripMenuItem1.Name = "deleteToolStripMenuItem1";
            this.deleteToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.deleteToolStripMenuItem1.Text = "Delete";
            // 
            // toolStripSeparator33
            // 
            this.toolStripSeparator33.Name = "toolStripSeparator33";
            this.toolStripSeparator33.Size = new System.Drawing.Size(185, 6);
            // 
            // downloadToolStripMenuItem1
            // 
            this.downloadToolStripMenuItem1.Name = "downloadToolStripMenuItem1";
            this.downloadToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.downloadToolStripMenuItem1.Text = "Download";
            // 
            // uploadToolStripMenuItem1
            // 
            this.uploadToolStripMenuItem1.Name = "uploadToolStripMenuItem1";
            this.uploadToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.uploadToolStripMenuItem1.Text = "Upload";
            // 
            // transfersToolStripMenuItem1
            // 
            this.transfersToolStripMenuItem1.Name = "transfersToolStripMenuItem1";
            this.transfersToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.transfersToolStripMenuItem1.Text = "Transfers";
            // 
            // logToolStripMenuItem1
            // 
            this.logToolStripMenuItem1.Name = "logToolStripMenuItem1";
            this.logToolStripMenuItem1.Size = new System.Drawing.Size(188, 22);
            this.logToolStripMenuItem1.Text = "Log";
            // 
            // toolStripSeparator13
            // 
            this.toolStripSeparator13.Name = "toolStripSeparator13";
            this.toolStripSeparator13.Size = new System.Drawing.Size(179, 6);
            // 
            // columnToolStripMenuItem
            // 
            this.columnToolStripMenuItem.DropDown = this.columnContextMenu;
            this.columnToolStripMenuItem.Name = "columnToolStripMenuItem";
            this.columnToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.columnToolStripMenuItem.Text = "Column";
            // 
            // columnContextMenu
            // 
            this.columnContextMenu.Name = "columnContextMenu";
            this.columnContextMenu.OwnerItem = this.columnToolStripMenuItem;
            this.columnContextMenu.Size = new System.Drawing.Size(61, 4);
            this.columnContextMenu.Opening += new System.ComponentModel.CancelEventHandler(this.columnContextMenu_Opening);
            // 
            // toolStripSeparator34
            // 
            this.toolStripSeparator34.Name = "toolStripSeparator34";
            this.toolStripSeparator34.Size = new System.Drawing.Size(179, 6);
            // 
            // showHiddenFilesToolStripMenuItem
            // 
            this.showHiddenFilesToolStripMenuItem.Name = "showHiddenFilesToolStripMenuItem";
            this.showHiddenFilesToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.showHiddenFilesToolStripMenuItem.Text = "Show Hidden Files";
            // 
            // textEncodingToolStripMenuItem
            // 
            this.textEncodingToolStripMenuItem.DropDown = this.textEncodingMenuStrip;
            this.textEncodingToolStripMenuItem.Name = "textEncodingToolStripMenuItem";
            this.textEncodingToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.textEncodingToolStripMenuItem.Text = "Text Encoding";
            // 
            // textEncodingMenuStrip
            // 
            this.textEncodingMenuStrip.Name = "textEncodingMenuStrip";
            this.textEncodingMenuStrip.OwnerItem = this.textEncodingToolStripMenuItem;
            this.textEncodingMenuStrip.Size = new System.Drawing.Size(61, 4);
            this.textEncodingMenuStrip.ItemClicked += new System.Windows.Forms.ToolStripItemClickedEventHandler(this.textEncodingMenuStrip_ItemClicked);
            // 
            // toolStripSeparator15
            // 
            this.toolStripSeparator15.Name = "toolStripSeparator15";
            this.toolStripSeparator15.Size = new System.Drawing.Size(179, 6);
            // 
            // toggleLogDrawerToolStripMenuItem
            // 
            this.toggleLogDrawerToolStripMenuItem.Name = "toggleLogDrawerToolStripMenuItem";
            this.toggleLogDrawerToolStripMenuItem.Size = new System.Drawing.Size(182, 22);
            this.toggleLogDrawerToolStripMenuItem.Text = "Toggle Log Drawer";
            // 
            // goToolStripMenuItem
            // 
            this.goToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.refreshToolStripMenuItem,
            this.toolStripSeparator16,
            this.gotoFolderToolStripMenuItem,
            this.backToolStripMenuItem,
            this.forwardToolStripMenuItem,
            this.enclosingFolderToolStripMenuItem,
            this.insideToolStripMenuItem,
            this.searchToolStripMenuItem,
            this.toolStripSeparator17,
            this.sendCommandToolStripMenuItem,
            this.toolStripSeparator18,
            this.stopToolStripMenuItem,
            this.disconnectToolStripMenuItem});
            this.goToolStripMenuItem.Name = "goToolStripMenuItem";
            this.goToolStripMenuItem.Size = new System.Drawing.Size(34, 20);
            this.goToolStripMenuItem.Text = "&Go";
            // 
            // refreshToolStripMenuItem
            // 
            this.refreshToolStripMenuItem.Name = "refreshToolStripMenuItem";
            this.refreshToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.refreshToolStripMenuItem.Text = "Refresh";
            // 
            // toolStripSeparator16
            // 
            this.toolStripSeparator16.Name = "toolStripSeparator16";
            this.toolStripSeparator16.Size = new System.Drawing.Size(166, 6);
            // 
            // gotoFolderToolStripMenuItem
            // 
            this.gotoFolderToolStripMenuItem.Name = "gotoFolderToolStripMenuItem";
            this.gotoFolderToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.gotoFolderToolStripMenuItem.Text = "Go to Folder…";
            // 
            // backToolStripMenuItem
            // 
            this.backToolStripMenuItem.Name = "backToolStripMenuItem";
            this.backToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.backToolStripMenuItem.Text = "Back";
            // 
            // forwardToolStripMenuItem
            // 
            this.forwardToolStripMenuItem.Name = "forwardToolStripMenuItem";
            this.forwardToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.forwardToolStripMenuItem.Text = "Forward";
            // 
            // enclosingFolderToolStripMenuItem
            // 
            this.enclosingFolderToolStripMenuItem.Name = "enclosingFolderToolStripMenuItem";
            this.enclosingFolderToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.enclosingFolderToolStripMenuItem.Text = "Enclosing Folder";
            // 
            // insideToolStripMenuItem
            // 
            this.insideToolStripMenuItem.Name = "insideToolStripMenuItem";
            this.insideToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.insideToolStripMenuItem.Text = "Inside";
            // 
            // searchToolStripMenuItem
            // 
            this.searchToolStripMenuItem.Name = "searchToolStripMenuItem";
            this.searchToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.searchToolStripMenuItem.Text = "Search…";
            // 
            // toolStripSeparator17
            // 
            this.toolStripSeparator17.Name = "toolStripSeparator17";
            this.toolStripSeparator17.Size = new System.Drawing.Size(166, 6);
            // 
            // sendCommandToolStripMenuItem
            // 
            this.sendCommandToolStripMenuItem.Enabled = false;
            this.sendCommandToolStripMenuItem.Name = "sendCommandToolStripMenuItem";
            this.sendCommandToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.sendCommandToolStripMenuItem.Text = "Send Command…";
            // 
            // toolStripSeparator18
            // 
            this.toolStripSeparator18.Name = "toolStripSeparator18";
            this.toolStripSeparator18.Size = new System.Drawing.Size(166, 6);
            // 
            // stopToolStripMenuItem
            // 
            this.stopToolStripMenuItem.Name = "stopToolStripMenuItem";
            this.stopToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.stopToolStripMenuItem.Text = "Stop";
            // 
            // disconnectToolStripMenuItem
            // 
            this.disconnectToolStripMenuItem.Name = "disconnectToolStripMenuItem";
            this.disconnectToolStripMenuItem.Size = new System.Drawing.Size(169, 22);
            this.disconnectToolStripMenuItem.Text = "Disconnect";
            // 
            // bookmarkToolStripMenuItem
            // 
            this.bookmarkToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.viewBookmarksToolStripMenuItem,
            this.toolStripSeparator1,
            this.newBookmarkToolStripMenuItem,
            this.deleteBookmarkToolStripMenuItem,
            this.editBookmarkToolStripMenuItem,
            this.duplicateBookmarkToolStripMenuItem1,
            this.toolStripSeparator19,
            this.historyToolStripMenuItem,
            this.bonjourToolStripMenuItem,
            this.toolStripSeparator36});
            this.bookmarkToolStripMenuItem.Name = "bookmarkToolStripMenuItem";
            this.bookmarkToolStripMenuItem.Size = new System.Drawing.Size(73, 20);
            this.bookmarkToolStripMenuItem.Text = "&Bookmark";
            this.bookmarkToolStripMenuItem.DropDownOpening += new System.EventHandler(this.bookmarkToolStripMenuItem_DropDownOpening);
            // 
            // viewBookmarksToolStripMenuItem
            // 
            this.viewBookmarksToolStripMenuItem.Name = "viewBookmarksToolStripMenuItem";
            this.viewBookmarksToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.viewBookmarksToolStripMenuItem.Text = "Toggle Bookmarks";
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(178, 6);
            // 
            // newBookmarkToolStripMenuItem
            // 
            this.newBookmarkToolStripMenuItem.Name = "newBookmarkToolStripMenuItem";
            this.newBookmarkToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.newBookmarkToolStripMenuItem.Text = "New Bookmark";
            // 
            // deleteBookmarkToolStripMenuItem
            // 
            this.deleteBookmarkToolStripMenuItem.Name = "deleteBookmarkToolStripMenuItem";
            this.deleteBookmarkToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.deleteBookmarkToolStripMenuItem.Text = "Delete Bookmark";
            // 
            // editBookmarkToolStripMenuItem
            // 
            this.editBookmarkToolStripMenuItem.Name = "editBookmarkToolStripMenuItem";
            this.editBookmarkToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.editBookmarkToolStripMenuItem.Text = "Edit Bookmark";
            // 
            // duplicateBookmarkToolStripMenuItem1
            // 
            this.duplicateBookmarkToolStripMenuItem1.Name = "duplicateBookmarkToolStripMenuItem1";
            this.duplicateBookmarkToolStripMenuItem1.Size = new System.Drawing.Size(181, 22);
            this.duplicateBookmarkToolStripMenuItem1.Text = "Duplicate Bookmark";
            // 
            // toolStripSeparator19
            // 
            this.toolStripSeparator19.Name = "toolStripSeparator19";
            this.toolStripSeparator19.Size = new System.Drawing.Size(178, 6);
            // 
            // historyToolStripMenuItem
            // 
            this.historyToolStripMenuItem.DropDown = this.historyMenuStrip;
            this.historyToolStripMenuItem.Image = ((System.Drawing.Image)(resources.GetObject("historyToolStripMenuItem.Image")));
            this.historyToolStripMenuItem.Name = "historyToolStripMenuItem";
            this.historyToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.historyToolStripMenuItem.Text = "History";
            // 
            // historyMenuStrip
            // 
            this.historyMenuStrip.Name = "historyMenuStrip";
            this.historyMenuStrip.OwnerItem = this.historyToolStripMenuItem;
            this.historyMenuStrip.Size = new System.Drawing.Size(61, 4);
            this.historyMenuStrip.Opening += new System.ComponentModel.CancelEventHandler(this.historyMenuStrip_Opening);
            // 
            // bonjourToolStripMenuItem
            // 
            this.bonjourToolStripMenuItem.DropDown = this.bonjourMenuStrip;
            this.bonjourToolStripMenuItem.Enabled = false;
            this.bonjourToolStripMenuItem.Image = ((System.Drawing.Image)(resources.GetObject("bonjourToolStripMenuItem.Image")));
            this.bonjourToolStripMenuItem.Name = "bonjourToolStripMenuItem";
            this.bonjourToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.bonjourToolStripMenuItem.Text = "Bonjour";
            // 
            // bonjourMenuStrip
            // 
            this.bonjourMenuStrip.Name = "bonjourMenuStrip";
            this.bonjourMenuStrip.OwnerItem = this.bonjourToolStripMenuItem;
            this.bonjourMenuStrip.Size = new System.Drawing.Size(61, 4);
            // 
            // toolStripSeparator36
            // 
            this.toolStripSeparator36.Name = "toolStripSeparator36";
            this.toolStripSeparator36.Size = new System.Drawing.Size(178, 6);
            // 
            // windowToolStripMenuItem
            // 
            this.windowToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.minimizeToolStripMenuItem,
            this.toolStripSeparator20,
            this.transfersToolStripMenuItem,
            this.activitiyToolStripMenuItem});
            this.windowToolStripMenuItem.Name = "windowToolStripMenuItem";
            this.windowToolStripMenuItem.Size = new System.Drawing.Size(63, 20);
            this.windowToolStripMenuItem.Text = "&Window";
            // 
            // minimizeToolStripMenuItem
            // 
            this.minimizeToolStripMenuItem.Name = "minimizeToolStripMenuItem";
            this.minimizeToolStripMenuItem.Size = new System.Drawing.Size(123, 22);
            this.minimizeToolStripMenuItem.Text = "Minimize";
            // 
            // toolStripSeparator20
            // 
            this.toolStripSeparator20.Name = "toolStripSeparator20";
            this.toolStripSeparator20.Size = new System.Drawing.Size(120, 6);
            // 
            // transfersToolStripMenuItem
            // 
            this.transfersToolStripMenuItem.Name = "transfersToolStripMenuItem";
            this.transfersToolStripMenuItem.Size = new System.Drawing.Size(123, 22);
            this.transfersToolStripMenuItem.Text = "Transfers";
            // 
            // activitiyToolStripMenuItem
            // 
            this.activitiyToolStripMenuItem.Enabled = false;
            this.activitiyToolStripMenuItem.Name = "activitiyToolStripMenuItem";
            this.activitiyToolStripMenuItem.Size = new System.Drawing.Size(123, 22);
            this.activitiyToolStripMenuItem.Text = "Activity";
            // 
            // helpToolStripMenuItem
            // 
            this.helpToolStripMenuItem.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.helpToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.cyberduckHelpToolStripMenuItem,
            this.licenseToolStripMenuItem,
            this.acknowledgmentsToolStripMenuItem,
            this.toolStripSeparator21,
            this.reportABugToolStripMenuItem,
            this.toolStripSeparator22,
            this.checkToolStripMenuItem,
            this.toolStripSeparator28,
            this.aboutCyberduckToolStripMenuItem});
            this.helpToolStripMenuItem.Name = "helpToolStripMenuItem";
            this.helpToolStripMenuItem.Size = new System.Drawing.Size(44, 20);
            this.helpToolStripMenuItem.Text = "&Help";
            // 
            // cyberduckHelpToolStripMenuItem
            // 
            this.cyberduckHelpToolStripMenuItem.Name = "cyberduckHelpToolStripMenuItem";
            this.cyberduckHelpToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.cyberduckHelpToolStripMenuItem.Text = "Cyberduck Help";
            // 
            // licenseToolStripMenuItem
            // 
            this.licenseToolStripMenuItem.Name = "licenseToolStripMenuItem";
            this.licenseToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.licenseToolStripMenuItem.Text = "License";
            // 
            // acknowledgmentsToolStripMenuItem
            // 
            this.acknowledgmentsToolStripMenuItem.Name = "acknowledgmentsToolStripMenuItem";
            this.acknowledgmentsToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.acknowledgmentsToolStripMenuItem.Text = "Acknowledgments";
            // 
            // toolStripSeparator21
            // 
            this.toolStripSeparator21.Name = "toolStripSeparator21";
            this.toolStripSeparator21.Size = new System.Drawing.Size(172, 6);
            // 
            // reportABugToolStripMenuItem
            // 
            this.reportABugToolStripMenuItem.Name = "reportABugToolStripMenuItem";
            this.reportABugToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.reportABugToolStripMenuItem.Text = "Report a Bug";
            // 
            // toolStripSeparator22
            // 
            this.toolStripSeparator22.Name = "toolStripSeparator22";
            this.toolStripSeparator22.Size = new System.Drawing.Size(172, 6);
            // 
            // checkToolStripMenuItem
            // 
            this.checkToolStripMenuItem.Name = "checkToolStripMenuItem";
            this.checkToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.checkToolStripMenuItem.Text = "Check for Update…";
            // 
            // toolStripSeparator28
            // 
            this.toolStripSeparator28.Name = "toolStripSeparator28";
            this.toolStripSeparator28.Size = new System.Drawing.Size(172, 6);
            // 
            // aboutCyberduckToolStripMenuItem
            // 
            this.aboutCyberduckToolStripMenuItem.Name = "aboutCyberduckToolStripMenuItem";
            this.aboutCyberduckToolStripMenuItem.Size = new System.Drawing.Size(175, 22);
            this.aboutCyberduckToolStripMenuItem.Text = "&About Cyberduck";
            // 
            // editorMenuStrip
            // 
            this.editorMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.editor1ToolStripMenuItem,
            this.editor2ToolStripMenuItem});
            this.editorMenuStrip.Name = "editorMenuStrip";
            this.editorMenuStrip.OwnerItem = this.editContextToolStripMenuItem;
            this.editorMenuStrip.Size = new System.Drawing.Size(115, 48);
            // 
            // editor1ToolStripMenuItem
            // 
            this.editor1ToolStripMenuItem.Name = "editor1ToolStripMenuItem";
            this.editor1ToolStripMenuItem.Size = new System.Drawing.Size(114, 22);
            this.editor1ToolStripMenuItem.Text = "Editor 1";
            // 
            // editor2ToolStripMenuItem
            // 
            this.editor2ToolStripMenuItem.Name = "editor2ToolStripMenuItem";
            this.editor2ToolStripMenuItem.Size = new System.Drawing.Size(114, 22);
            this.editor2ToolStripMenuItem.Text = "Editor 2";
            // 
            // editToolStripSplitButton
            // 
            this.editToolStripSplitButton.DropDown = this.editorMenuStrip;
            this.editToolStripSplitButton.Image = ((System.Drawing.Image)(resources.GetObject("editToolStripSplitButton.Image")));
            this.editToolStripSplitButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.editToolStripSplitButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.editToolStripSplitButton.Name = "editToolStripSplitButton";
            this.editToolStripSplitButton.Size = new System.Drawing.Size(48, 53);
            this.editToolStripSplitButton.Text = "Edit";
            this.editToolStripSplitButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // editContextToolStripMenuItem
            // 
            this.editContextToolStripMenuItem.DropDown = this.editorMenuStrip;
            this.editContextToolStripMenuItem.Name = "editContextToolStripMenuItem";
            this.editContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.editContextToolStripMenuItem.Text = "Edit With";
            // 
            // toolBar
            // 
            this.toolBar.AutoSize = false;
            this.toolBar.ContextMenuStrip = this.toolbarContextMenu;
            this.toolBar.Dock = System.Windows.Forms.DockStyle.None;
            this.toolBar.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.toolBar.ImageScalingSize = new System.Drawing.Size(32, 32);
            this.toolBar.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.openConnectionToolStripButton,
            this.toolStripSeparatorAfterOpenConnection,
            this.quickConnectToolStripComboBox,
            this.actionToolStripDropDownButton,
            this.toolStripSeparatorAfterAction,
            this.infoToolStripButton,
            this.refreshToolStripButton,
            this.toolStripSeparatorAfterRefresh,
            this.editToolStripSplitButton,
            this.openInBrowserToolStripButton,
            this.openInTerminalToolStripButton,
            this.newFolderToolStripButton,
            this.deleteToolStripButton,
            this.toolStripSeparatorAfterDelete,
            this.downloadToolStripButton,
            this.uploadToolStripButton,
            this.transfersToolStripButton,
            this.logToolStripButton,
            this.disconnectStripButton});
            this.toolBar.Location = new System.Drawing.Point(0, 0);
            this.toolBar.Name = "toolBar";
            this.toolBar.ShowItemToolTips = false;
            this.toolBar.Size = new System.Drawing.Size(1028, 56);
            this.toolBar.Stretch = true;
            this.toolBar.TabIndex = 13;
            this.toolBar.Text = "toolStrip1";
            // 
            // openConnectionToolStripButton
            // 
            this.openConnectionToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("openConnectionToolStripButton.Image")));
            this.openConnectionToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.openConnectionToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.openConnectionToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.openConnectionToolStripButton.Name = "openConnectionToolStripButton";
            this.openConnectionToolStripButton.Padding = new System.Windows.Forms.Padding(0, 1, 0, 1);
            this.openConnectionToolStripButton.Size = new System.Drawing.Size(100, 53);
            this.openConnectionToolStripButton.Text = "New Connection";
            this.openConnectionToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // toolStripSeparatorAfterOpenConnection
            // 
            this.toolStripSeparatorAfterOpenConnection.Name = "toolStripSeparatorAfterOpenConnection";
            this.toolStripSeparatorAfterOpenConnection.Size = new System.Drawing.Size(6, 56);
            // 
            // quickConnectToolStripComboBox
            // 
            this.quickConnectToolStripComboBox.AutoCompleteMode = System.Windows.Forms.AutoCompleteMode.Suggest;
            this.quickConnectToolStripComboBox.AutoCompleteSource = System.Windows.Forms.AutoCompleteSource.ListItems;
            this.quickConnectToolStripComboBox.FlatStyle = System.Windows.Forms.FlatStyle.System;
            this.quickConnectToolStripComboBox.Items.AddRange(new object[] {
            "Test1",
            "Test2",
            "Zest3"});
            this.quickConnectToolStripComboBox.Name = "quickConnectToolStripComboBox";
            this.quickConnectToolStripComboBox.Size = new System.Drawing.Size(180, 56);
            this.quickConnectToolStripComboBox.Text = "Quick Connect";
            this.quickConnectToolStripComboBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.toolStripQuickConnect_KeyDown);
            // 
            // actionToolStripDropDownButton
            // 
            this.actionToolStripDropDownButton.DropDown = this.contextMenuStrip;
            this.actionToolStripDropDownButton.Image = ((System.Drawing.Image)(resources.GetObject("actionToolStripDropDownButton.Image")));
            this.actionToolStripDropDownButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.actionToolStripDropDownButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.actionToolStripDropDownButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.actionToolStripDropDownButton.Name = "actionToolStripDropDownButton";
            this.actionToolStripDropDownButton.Padding = new System.Windows.Forms.Padding(0, 1, 0, 1);
            this.actionToolStripDropDownButton.Size = new System.Drawing.Size(55, 53);
            this.actionToolStripDropDownButton.Text = "Action";
            this.actionToolStripDropDownButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // contextMenuStrip
            // 
            this.contextMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.refreshContextToolStripMenuItem,
            this.toolStripSeparator6,
            this.newFolderContextToolStripMenuItem,
            this.newVaultContextToolStripMenuItem,
            this.newFileContextToolStripMenuItem,
            this.newSymlinkContextToolStripMenuItem,
            this.renameContextToolStripMenuItem,
            this.duplicateFileContextToolStripMenuItem,
            this.toolStripSeparator2,
            this.copyURLContextToolStripMenuItem,
            this.openURLContextToolStripMenuItem,
            this.editContextToolStripMenuItem,
            this.infoContextToolStripMenuItem,
            this.toolStripSeparator26,
            this.downloadContextToolStripMenuItem,
            this.downloadAsContextToolStripMenuItem,
            this.downloadToContextToolStripMenuItem,
            this.uploadContextToolStripMenuItem,
            this.synchronizeContextToolStripMenuItem,
            this.toolStripSeparator3,
            this.deleteContextToolStripMenuItem,
            this.revertContxtStripMenuItem,
            this.toolStripSeparator4,
            this.createArchiveContextToolStripMenuItem,
            this.expandArchiveContextToolStripMenuItem,
            this.toolStripSeparator5,
            this.newBrowserContextToolStripMenuItem,
            this.newBookmarkContextToolStripMenuItem});
            this.contextMenuStrip.Name = "contextMenuStrip1";
            this.contextMenuStrip.OwnerItem = this.actionToolStripDropDownButton;
            this.contextMenuStrip.Size = new System.Drawing.Size(193, 524);
            // 
            // refreshContextToolStripMenuItem
            // 
            this.refreshContextToolStripMenuItem.Name = "refreshContextToolStripMenuItem";
            this.refreshContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.refreshContextToolStripMenuItem.Text = "Refresh";
            // 
            // toolStripSeparator6
            // 
            this.toolStripSeparator6.Name = "toolStripSeparator6";
            this.toolStripSeparator6.Size = new System.Drawing.Size(189, 6);
            // 
            // newFolderContextToolStripMenuItem
            // 
            this.newFolderContextToolStripMenuItem.Name = "newFolderContextToolStripMenuItem";
            this.newFolderContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newFolderContextToolStripMenuItem.Text = "New Folder…";
            // 
            // newVaultContextToolStripMenuItem
            // 
            this.newVaultContextToolStripMenuItem.Name = "newVaultContextToolStripMenuItem";
            this.newVaultContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newVaultContextToolStripMenuItem.Text = "New Encrypted Vault…";
            // 
            // newFileContextToolStripMenuItem
            // 
            this.newFileContextToolStripMenuItem.Name = "newFileContextToolStripMenuItem";
            this.newFileContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newFileContextToolStripMenuItem.Text = "New File…";
            // 
            // newSymlinkContextToolStripMenuItem
            // 
            this.newSymlinkContextToolStripMenuItem.Name = "newSymlinkContextToolStripMenuItem";
            this.newSymlinkContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newSymlinkContextToolStripMenuItem.Text = "New Symbolic Link…";
            // 
            // renameContextToolStripMenuItem
            // 
            this.renameContextToolStripMenuItem.Name = "renameContextToolStripMenuItem";
            this.renameContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.renameContextToolStripMenuItem.Text = "Rename…";
            // 
            // duplicateFileContextToolStripMenuItem
            // 
            this.duplicateFileContextToolStripMenuItem.Name = "duplicateFileContextToolStripMenuItem";
            this.duplicateFileContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.duplicateFileContextToolStripMenuItem.Text = "Duplicate…";
            // 
            // toolStripSeparator2
            // 
            this.toolStripSeparator2.Name = "toolStripSeparator2";
            this.toolStripSeparator2.Size = new System.Drawing.Size(189, 6);
            // 
            // copyURLContextToolStripMenuItem
            // 
            this.copyURLContextToolStripMenuItem.Name = "copyURLContextToolStripMenuItem";
            this.copyURLContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.copyURLContextToolStripMenuItem.Text = "Copy URL";
            // 
            // openURLContextToolStripMenuItem
            // 
            this.openURLContextToolStripMenuItem.Name = "openURLContextToolStripMenuItem";
            this.openURLContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.openURLContextToolStripMenuItem.Text = "Open URL";
            // 
            // infoContextToolStripMenuItem
            // 
            this.infoContextToolStripMenuItem.Name = "infoContextToolStripMenuItem";
            this.infoContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.infoContextToolStripMenuItem.Text = "Info";
            // 
            // toolStripSeparator26
            // 
            this.toolStripSeparator26.Name = "toolStripSeparator26";
            this.toolStripSeparator26.Size = new System.Drawing.Size(189, 6);
            // 
            // downloadContextToolStripMenuItem
            // 
            this.downloadContextToolStripMenuItem.Name = "downloadContextToolStripMenuItem";
            this.downloadContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.downloadContextToolStripMenuItem.Text = "Download";
            // 
            // downloadAsContextToolStripMenuItem
            // 
            this.downloadAsContextToolStripMenuItem.Name = "downloadAsContextToolStripMenuItem";
            this.downloadAsContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.downloadAsContextToolStripMenuItem.Text = "Download As…";
            // 
            // downloadToContextToolStripMenuItem
            // 
            this.downloadToContextToolStripMenuItem.Name = "downloadToContextToolStripMenuItem";
            this.downloadToContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.downloadToContextToolStripMenuItem.Text = "Download To…";
            // 
            // uploadContextToolStripMenuItem
            // 
            this.uploadContextToolStripMenuItem.Name = "uploadContextToolStripMenuItem";
            this.uploadContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.uploadContextToolStripMenuItem.Text = "Upload…";
            // 
            // synchronizeContextToolStripMenuItem
            // 
            this.synchronizeContextToolStripMenuItem.Name = "synchronizeContextToolStripMenuItem";
            this.synchronizeContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.synchronizeContextToolStripMenuItem.Text = "Synchronize…";
            // 
            // toolStripSeparator3
            // 
            this.toolStripSeparator3.Name = "toolStripSeparator3";
            this.toolStripSeparator3.Size = new System.Drawing.Size(189, 6);
            // 
            // deleteContextToolStripMenuItem
            // 
            this.deleteContextToolStripMenuItem.Name = "deleteContextToolStripMenuItem";
            this.deleteContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.deleteContextToolStripMenuItem.Text = "Delete";
            // 
            // revertContxtStripMenuItem
            // 
            this.revertContxtStripMenuItem.Name = "revertContxtStripMenuItem";
            this.revertContxtStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.revertContxtStripMenuItem.Text = "Revert";
            // 
            // toolStripSeparator4
            // 
            this.toolStripSeparator4.Name = "toolStripSeparator4";
            this.toolStripSeparator4.Size = new System.Drawing.Size(189, 6);
            // 
            // expandArchiveContextToolStripMenuItem
            // 
            this.expandArchiveContextToolStripMenuItem.Name = "expandArchiveContextToolStripMenuItem";
            this.expandArchiveContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.expandArchiveContextToolStripMenuItem.Text = "Expand Archive";
            // 
            // toolStripSeparator5
            // 
            this.toolStripSeparator5.Name = "toolStripSeparator5";
            this.toolStripSeparator5.Size = new System.Drawing.Size(189, 6);
            // 
            // newBrowserContextToolStripMenuItem
            // 
            this.newBrowserContextToolStripMenuItem.Name = "newBrowserContextToolStripMenuItem";
            this.newBrowserContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newBrowserContextToolStripMenuItem.Text = "New Browser";
            // 
            // newBookmarkContextToolStripMenuItem
            // 
            this.newBookmarkContextToolStripMenuItem.Name = "newBookmarkContextToolStripMenuItem";
            this.newBookmarkContextToolStripMenuItem.Size = new System.Drawing.Size(192, 22);
            this.newBookmarkContextToolStripMenuItem.Text = "New Bookmark";
            // 
            // toolStripSeparatorAfterAction
            // 
            this.toolStripSeparatorAfterAction.Name = "toolStripSeparatorAfterAction";
            this.toolStripSeparatorAfterAction.Size = new System.Drawing.Size(6, 56);
            // 
            // infoToolStripButton
            // 
            this.infoToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("infoToolStripButton.Image")));
            this.infoToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.infoToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.infoToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.infoToolStripButton.Name = "infoToolStripButton";
            this.infoToolStripButton.Size = new System.Drawing.Size(53, 53);
            this.infoToolStripButton.Text = "Get Info";
            this.infoToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // refreshToolStripButton
            // 
            this.refreshToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("refreshToolStripButton.Image")));
            this.refreshToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.refreshToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.refreshToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.refreshToolStripButton.Name = "refreshToolStripButton";
            this.refreshToolStripButton.Size = new System.Drawing.Size(50, 53);
            this.refreshToolStripButton.Text = "Refresh";
            this.refreshToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // toolStripSeparatorAfterRefresh
            // 
            this.toolStripSeparatorAfterRefresh.Name = "toolStripSeparatorAfterRefresh";
            this.toolStripSeparatorAfterRefresh.Size = new System.Drawing.Size(6, 56);
            // 
            // openInBrowserToolStripButton
            // 
            this.openInBrowserToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("openInBrowserToolStripButton.Image")));
            this.openInBrowserToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.openInBrowserToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.openInBrowserToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.openInBrowserToolStripButton.Name = "openInBrowserToolStripButton";
            this.openInBrowserToolStripButton.Size = new System.Drawing.Size(40, 53);
            this.openInBrowserToolStripButton.Text = "Open";
            this.openInBrowserToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // openInTerminalToolStripButton
            // 
            this.openInTerminalToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.openInTerminalToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.openInTerminalToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.openInTerminalToolStripButton.Name = "openInTerminalToolStripButton";
            this.openInTerminalToolStripButton.Size = new System.Drawing.Size(40, 53);
            this.openInTerminalToolStripButton.Text = "Open";
            this.openInTerminalToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // newFolderToolStripButton
            // 
            this.newFolderToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.newFolderToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.newFolderToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.newFolderToolStripButton.Name = "newFolderToolStripButton";
            this.newFolderToolStripButton.Size = new System.Drawing.Size(71, 53);
            this.newFolderToolStripButton.Text = "New Folder";
            this.newFolderToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // deleteToolStripButton
            // 
            this.deleteToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("deleteToolStripButton.Image")));
            this.deleteToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.deleteToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.deleteToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.deleteToolStripButton.Name = "deleteToolStripButton";
            this.deleteToolStripButton.Size = new System.Drawing.Size(44, 53);
            this.deleteToolStripButton.Text = "Delete";
            this.deleteToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // toolStripSeparatorAfterDelete
            // 
            this.toolStripSeparatorAfterDelete.Name = "toolStripSeparatorAfterDelete";
            this.toolStripSeparatorAfterDelete.Size = new System.Drawing.Size(6, 56);
            // 
            // downloadToolStripButton
            // 
            this.downloadToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("downloadToolStripButton.Image")));
            this.downloadToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.downloadToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.downloadToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.downloadToolStripButton.Name = "downloadToolStripButton";
            this.downloadToolStripButton.Size = new System.Drawing.Size(65, 53);
            this.downloadToolStripButton.Text = "Download";
            this.downloadToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // uploadToolStripButton
            // 
            this.uploadToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("uploadToolStripButton.Image")));
            this.uploadToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.uploadToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.uploadToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.uploadToolStripButton.Name = "uploadToolStripButton";
            this.uploadToolStripButton.Size = new System.Drawing.Size(49, 53);
            this.uploadToolStripButton.Text = "Upload";
            this.uploadToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // transfersToolStripButton
            // 
            this.transfersToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("transfersToolStripButton.Image")));
            this.transfersToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.transfersToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.transfersToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.transfersToolStripButton.Name = "transfersToolStripButton";
            this.transfersToolStripButton.Size = new System.Drawing.Size(58, 53);
            this.transfersToolStripButton.Text = "Transfers";
            this.transfersToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // logToolStripButton
            // 
            this.logToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("logToolStripButton.Image")));
            this.logToolStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.logToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.logToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.logToolStripButton.Name = "logToolStripButton";
            this.logToolStripButton.Size = new System.Drawing.Size(36, 53);
            this.logToolStripButton.Text = "Log";
            this.logToolStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // disconnectStripButton
            // 
            this.disconnectStripButton.Alignment = System.Windows.Forms.ToolStripItemAlignment.Right;
            this.disconnectStripButton.Image = ((System.Drawing.Image)(resources.GetObject("disconnectStripButton.Image")));
            this.disconnectStripButton.ImageAlign = System.Drawing.ContentAlignment.BottomCenter;
            this.disconnectStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.disconnectStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.disconnectStripButton.Name = "disconnectStripButton";
            this.disconnectStripButton.Size = new System.Drawing.Size(70, 53);
            this.disconnectStripButton.Text = "Disconnect";
            this.disconnectStripButton.TextImageRelation = System.Windows.Forms.TextImageRelation.ImageAboveText;
            // 
            // iconList
            // 
            this.iconList.ColorDepth = System.Windows.Forms.ColorDepth.Depth8Bit;
            this.iconList.ImageSize = new System.Drawing.Size(16, 16);
            this.iconList.TransparentColor = System.Drawing.Color.Transparent;
            // 
            // toolStripContainer1
            // 
            // 
            // toolStripContainer1.BottomToolStripPanel
            // 
            this.toolStripContainer1.BottomToolStripPanel.Controls.Add(this.statusStrip);
            // 
            // toolStripContainer1.ContentPanel
            // 
            this.toolStripContainer1.ContentPanel.Controls.Add(this.panelManager1);
            this.toolStripContainer1.ContentPanel.Controls.Add(this.viewPanel);
            this.toolStripContainer1.ContentPanel.Size = new System.Drawing.Size(1028, 528);
            this.toolStripContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.toolStripContainer1.LeftToolStripPanelVisible = false;
            this.toolStripContainer1.Location = new System.Drawing.Point(0, 0);
            this.toolStripContainer1.Name = "toolStripContainer1";
            this.toolStripContainer1.RightToolStripPanelVisible = false;
            this.toolStripContainer1.Size = new System.Drawing.Size(1028, 606);
            this.toolStripContainer1.TabIndex = 16;
            this.toolStripContainer1.Text = "toolStripContainer1";
            // 
            // toolStripContainer1.TopToolStripPanel
            // 
            this.toolStripContainer1.TopToolStripPanel.Controls.Add(this.toolBar);
            this.toolStripContainer1.TopToolStripPanel.Controls.Add(this.menuStrip1);
            // 
            // panelManager1
            // 
            this.panelManager1.Controls.Add(this.managedBrowserPanel1);
            this.panelManager1.Controls.Add(this.managedBookmarkPanel2);
            this.panelManager1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelManager1.Location = new System.Drawing.Point(0, 31);
            this.panelManager1.Name = "panelManager1";
            this.panelManager1.SelectedIndex = 0;
            this.panelManager1.SelectedPanel = this.managedBrowserPanel1;
            this.panelManager1.Size = new System.Drawing.Size(1028, 497);
            this.panelManager1.TabIndex = 3;
            // 
            // managedBrowserPanel1
            // 
            this.managedBrowserPanel1.Controls.Add(this.splitContainer);
            this.managedBrowserPanel1.Location = new System.Drawing.Point(0, 0);
            this.managedBrowserPanel1.Name = "managedBrowserPanel1";
            this.managedBrowserPanel1.Size = new System.Drawing.Size(1028, 497);
            this.managedBrowserPanel1.Text = "managedBrowserPanel1";
            // 
            // splitContainer
            // 
            this.splitContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer.FixedPanel = System.Windows.Forms.FixedPanel.Panel2;
            this.splitContainer.Location = new System.Drawing.Point(0, 0);
            this.splitContainer.Name = "splitContainer";
            this.splitContainer.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer.Panel1
            // 
            this.splitContainer.Panel1.Controls.Add(this.browser);
            this.splitContainer.Panel1MinSize = 0;
            // 
            // splitContainer.Panel2
            // 
            this.splitContainer.Panel2.Controls.Add(this.transcriptBox);
            this.splitContainer.Panel2MinSize = 50;
            this.splitContainer.Size = new System.Drawing.Size(1028, 497);
            this.splitContainer.SplitterDistance = 444;
            this.splitContainer.SplitterWidth = 3;
            this.splitContainer.TabIndex = 2;
            // 
            // browser
            // 
            this.browser.ActiveForegroudColor = System.Drawing.SystemColors.ControlText;
            this.browser.ActiveGetterPath = null;
            this.browser.ActiveGetterTransferItem = null;
            this.browser.AllColumns.Add(this.treeColumnName);
            this.browser.AllColumns.Add(this.treeColumnSize);
            this.browser.AllColumns.Add(this.treeColumnModified);
            this.browser.AllColumns.Add(this.treeColumnOwner);
            this.browser.AllColumns.Add(this.treeColumnGroup);
            this.browser.AllColumns.Add(this.treeColumnPermissions);
            this.browser.AllColumns.Add(this.treeColumnKind);
            this.browser.AllColumns.Add(this.treeColumnExtension);
            this.browser.AllColumns.Add(this.treeColumnRegion);
            this.browser.AllColumns.Add(this.treeColumnVersion);
            this.browser.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.browser.CellEditActivation = BrightIdeasSoftware.ObjectListView.CellEditActivateMode.F2Only;
            this.browser.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.treeColumnName,
            this.treeColumnSize,
            this.treeColumnModified});
            this.browser.Cursor = System.Windows.Forms.Cursors.Default;
            this.browser.Dock = System.Windows.Forms.DockStyle.Fill;
            this.browser.FullRowSelect = true;
            this.browser.InactiveForegroudColor = System.Drawing.Color.Gray;
            this.browser.IsSimpleDragSource = true;
            this.browser.Location = new System.Drawing.Point(0, 0);
            this.browser.Name = "browser";
            this.browser.OwnerDraw = true;
            this.browser.ShowGroups = false;
            this.browser.Size = new System.Drawing.Size(1028, 444);
            this.browser.TabIndex = 15;
            this.browser.UseCompatibleStateImageBehavior = false;
            this.browser.View = System.Windows.Forms.View.Details;
            this.browser.VirtualMode = true;
            this.browser.Expanding += new System.EventHandler<BrightIdeasSoftware.TreeBranchExpandingEventArgs>(this.browser_Expanding);
            this.browser.CellEditFinishing += new BrightIdeasSoftware.CellEditEventHandler(this.browser_CellEditFinishing);
            this.browser.SelectionChanged += new System.EventHandler(this.browser_SelectionChanged);
            this.browser.BeforeLabelEdit += new System.Windows.Forms.LabelEditEventHandler(this.browser_BeforeLabelEdit);
            this.browser.DoubleClick += new System.EventHandler(this.browser_DoubleClick);
            this.browser.KeyDown += new System.Windows.Forms.KeyEventHandler(this.browser_KeyDown);
            this.browser.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.browser_KeyPress);
            // 
            // treeColumnName
            // 
            this.treeColumnName.CellPadding = null;
            this.treeColumnName.FillsFreeSpace = true;
            this.treeColumnName.Text = "Filename";
            this.treeColumnName.Width = 180;
            // 
            // treeColumnSize
            // 
            this.treeColumnSize.CellPadding = null;
            this.treeColumnSize.HeaderTextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            this.treeColumnSize.IsEditable = false;
            this.treeColumnSize.Text = "Size";
            this.treeColumnSize.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
            // 
            // treeColumnModified
            // 
            this.treeColumnModified.CellPadding = null;
            this.treeColumnModified.IsEditable = false;
            this.treeColumnModified.IsTileViewColumn = true;
            this.treeColumnModified.Text = "Modified";
            this.treeColumnModified.Width = 145;
            // 
            // treeColumnOwner
            // 
            this.treeColumnOwner.CellPadding = null;
            this.treeColumnOwner.DisplayIndex = 3;
            this.treeColumnOwner.IsEditable = false;
            this.treeColumnOwner.IsVisible = false;
            this.treeColumnOwner.Text = "Owner";
            // 
            // treeColumnGroup
            // 
            this.treeColumnGroup.CellPadding = null;
            this.treeColumnGroup.DisplayIndex = 4;
            this.treeColumnGroup.IsEditable = false;
            this.treeColumnGroup.IsVisible = false;
            this.treeColumnGroup.Text = "Group";
            // 
            // treeColumnPermissions
            // 
            this.treeColumnPermissions.CellPadding = null;
            this.treeColumnPermissions.DisplayIndex = 5;
            this.treeColumnPermissions.IsEditable = false;
            this.treeColumnPermissions.IsVisible = false;
            this.treeColumnPermissions.Text = "Permissions";
            // 
            // treeColumnKind
            // 
            this.treeColumnKind.CellPadding = null;
            this.treeColumnKind.DisplayIndex = 6;
            this.treeColumnKind.IsEditable = false;
            this.treeColumnKind.IsVisible = false;
            this.treeColumnKind.Text = "Kind";
            // 
            // treeColumnExtension
            // 
            this.treeColumnExtension.CellPadding = null;
            this.treeColumnExtension.DisplayIndex = 7;
            this.treeColumnExtension.IsEditable = false;
            this.treeColumnExtension.IsVisible = false;
            this.treeColumnExtension.Text = "Extension";
            // 
            // treeColumnRegion
            // 
            this.treeColumnRegion.CellPadding = null;
            this.treeColumnRegion.DisplayIndex = 8;
            this.treeColumnRegion.IsEditable = false;
            this.treeColumnRegion.IsVisible = false;
            this.treeColumnRegion.Text = "Region";
            // 
            // treeColumnVersion
            // 
            this.treeColumnVersion.CellPadding = null;
            this.treeColumnVersion.DisplayIndex = 9;
            this.treeColumnVersion.IsEditable = false;
            this.treeColumnVersion.IsVisible = false;
            this.treeColumnVersion.Text = "Version";
            // 
            // transcriptBox
            // 
            this.transcriptBox.CausesValidation = false;
            this.transcriptBox.Dock = System.Windows.Forms.DockStyle.Fill;
            this.transcriptBox.Location = new System.Drawing.Point(0, 0);
            this.transcriptBox.Name = "transcriptBox";
            this.transcriptBox.ReadOnly = true;
            this.transcriptBox.Size = new System.Drawing.Size(1028, 50);
            this.transcriptBox.TabIndex = 0;
            this.transcriptBox.Text = "";
            this.transcriptBox.KeyDown += new System.Windows.Forms.KeyEventHandler(this.transcriptBox_KeyDown);
            // 
            // managedBookmarkPanel2
            // 
            this.managedBookmarkPanel2.Controls.Add(this.bookmarkListView);
            this.managedBookmarkPanel2.Controls.Add(this.actionToolStrip);
            this.managedBookmarkPanel2.Location = new System.Drawing.Point(0, 0);
            this.managedBookmarkPanel2.Name = "managedBookmarkPanel2";
            this.managedBookmarkPanel2.Size = new System.Drawing.Size(0, 0);
            this.managedBookmarkPanel2.Text = "managedBookmarkPanel2";
            // 
            // bookmarkListView
            // 
            this.bookmarkListView.AllColumns.Add(this.bookmarkImageColumn);
            this.bookmarkListView.AllColumns.Add(this.bookmarkDescriptionColumn);
            this.bookmarkListView.AllColumns.Add(this.activeColumn);
            this.bookmarkListView.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.bookmarkListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.bookmarkImageColumn,
            this.bookmarkDescriptionColumn,
            this.activeColumn});
            this.bookmarkListView.Cursor = System.Windows.Forms.Cursors.Default;
            this.bookmarkListView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.bookmarkListView.FullRowSelect = true;
            this.bookmarkListView.Location = new System.Drawing.Point(0, 0);
            this.bookmarkListView.Name = "bookmarkListView";
            this.bookmarkListView.OwnerDraw = true;
            this.bookmarkListView.RowHeight = 37;
            this.bookmarkListView.ShowGroups = false;
            this.bookmarkListView.Size = new System.Drawing.Size(0, 0);
            this.bookmarkListView.TabIndex = 10;
            this.bookmarkListView.UseCompatibleStateImageBehavior = false;
            this.bookmarkListView.View = System.Windows.Forms.View.Details;
            this.bookmarkListView.DoubleClick += new System.EventHandler(this.bookmarkListView_DoubleClick);
            this.bookmarkListView.KeyDown += new System.Windows.Forms.KeyEventHandler(this.bookmarkListView_KeyDown);
            // 
            // bookmarkImageColumn
            // 
            this.bookmarkImageColumn.AspectName = "";
            this.bookmarkImageColumn.CellPadding = null;
            this.bookmarkImageColumn.IsEditable = false;
            this.bookmarkImageColumn.Text = "";
            this.bookmarkImageColumn.Width = 32;
            // 
            // bookmarkDescriptionColumn
            // 
            this.bookmarkDescriptionColumn.AspectName = "";
            this.bookmarkDescriptionColumn.CellPadding = null;
            this.bookmarkDescriptionColumn.Text = "";
            this.bookmarkDescriptionColumn.Width = 200;
            // 
            // activeColumn
            // 
            this.activeColumn.CellPadding = null;
            this.activeColumn.Text = "";
            // 
            // actionToolStrip
            // 
            this.actionToolStrip.AutoSize = false;
            this.actionToolStrip.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.actionToolStrip.GripMargin = new System.Windows.Forms.Padding(0);
            this.actionToolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.actionToolStrip.ImageScalingSize = new System.Drawing.Size(30, 30);
            this.actionToolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.newBookmarkToolStripButton,
            this.editBookmarkToolStripButton,
            this.deleteBookmarkToolStripButton});
            this.actionToolStrip.LayoutStyle = System.Windows.Forms.ToolStripLayoutStyle.HorizontalStackWithOverflow;
            this.actionToolStrip.Location = new System.Drawing.Point(0, -33);
            this.actionToolStrip.Name = "actionToolStrip";
            this.actionToolStrip.ShowItemToolTips = false;
            this.actionToolStrip.Size = new System.Drawing.Size(0, 33);
            this.actionToolStrip.Stretch = true;
            this.actionToolStrip.TabIndex = 5;
            this.actionToolStrip.Text = "toolStrip1";
            // 
            // newBookmarkToolStripButton
            // 
            this.newBookmarkToolStripButton.AutoSize = false;
            this.newBookmarkToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.newBookmarkToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("newBookmarkToolStripButton.Image")));
            this.newBookmarkToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.newBookmarkToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.newBookmarkToolStripButton.Margin = new System.Windows.Forms.Padding(16, 1, 0, 2);
            this.newBookmarkToolStripButton.Name = "newBookmarkToolStripButton";
            this.newBookmarkToolStripButton.Size = new System.Drawing.Size(23, 22);
            this.newBookmarkToolStripButton.Text = "toolStripButton1";
            // 
            // editBookmarkToolStripButton
            // 
            this.editBookmarkToolStripButton.AutoSize = false;
            this.editBookmarkToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.editBookmarkToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("editBookmarkToolStripButton.Image")));
            this.editBookmarkToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.editBookmarkToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.editBookmarkToolStripButton.Name = "editBookmarkToolStripButton";
            this.editBookmarkToolStripButton.Size = new System.Drawing.Size(23, 22);
            this.editBookmarkToolStripButton.Text = "toolStripButton2";
            // 
            // deleteBookmarkToolStripButton
            // 
            this.deleteBookmarkToolStripButton.AutoSize = false;
            this.deleteBookmarkToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.deleteBookmarkToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("deleteBookmarkToolStripButton.Image")));
            this.deleteBookmarkToolStripButton.ImageScaling = System.Windows.Forms.ToolStripItemImageScaling.None;
            this.deleteBookmarkToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.deleteBookmarkToolStripButton.Margin = new System.Windows.Forms.Padding(-1, 1, 0, 2);
            this.deleteBookmarkToolStripButton.Name = "deleteBookmarkToolStripButton";
            this.deleteBookmarkToolStripButton.Size = new System.Drawing.Size(22, 22);
            this.deleteBookmarkToolStripButton.Text = "toolStripButton3";
            // 
            // viewPanel
            // 
            this.viewPanel.AutoSize = true;
            this.viewPanel.BackColor = System.Drawing.Color.WhiteSmoke;
            this.viewPanel.Controls.Add(this.tableLayoutPanel1);
            this.viewPanel.Dock = System.Windows.Forms.DockStyle.Top;
            this.viewPanel.Location = new System.Drawing.Point(0, 0);
            this.viewPanel.Name = "viewPanel";
            this.viewPanel.Size = new System.Drawing.Size(1028, 31);
            this.viewPanel.TabIndex = 29;
            // 
            // tableLayoutPanel1
            // 
            this.tableLayoutPanel1.AutoSize = true;
            this.tableLayoutPanel1.BackColor = System.Drawing.SystemColors.Control;
            this.tableLayoutPanel1.ColumnCount = 6;
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle());
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 42F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 42F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 42F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 120F));
            this.tableLayoutPanel1.ColumnStyles.Add(new System.Windows.Forms.ColumnStyle(System.Windows.Forms.SizeType.Absolute, 20F));
            this.tableLayoutPanel1.Controls.Add(this.viewToolStrip, 0, 0);
            this.tableLayoutPanel1.Controls.Add(this.searchTextBox, 5, 0);
            this.tableLayoutPanel1.Controls.Add(this.parentPathButton, 4, 0);
            this.tableLayoutPanel1.Controls.Add(this.pathComboBox, 3, 0);
            this.tableLayoutPanel1.Controls.Add(this.historyForwardButton, 2, 0);
            this.tableLayoutPanel1.Controls.Add(this.historyBackButton, 1, 0);
            this.tableLayoutPanel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.tableLayoutPanel1.Location = new System.Drawing.Point(0, 0);
            this.tableLayoutPanel1.Name = "tableLayoutPanel1";
            this.tableLayoutPanel1.Padding = new System.Windows.Forms.Padding(0, 0, 2, 0);
            this.tableLayoutPanel1.RowCount = 1;
            this.tableLayoutPanel1.RowStyles.Add(new System.Windows.Forms.RowStyle(System.Windows.Forms.SizeType.Percent, 100F));
            this.tableLayoutPanel1.Size = new System.Drawing.Size(1028, 31);
            this.tableLayoutPanel1.TabIndex = 16;
            // 
            // viewToolStrip
            // 
            this.viewToolStrip.Dock = System.Windows.Forms.DockStyle.None;
            this.viewToolStrip.GripStyle = System.Windows.Forms.ToolStripGripStyle.Hidden;
            this.viewToolStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.browserToolStripButton,
            this.bookmarksToolStripButton,
            this.historyToolStripButton,
            this.bonjourToolStripButton,
            this.toolStripSeparator23});
            this.viewToolStrip.Location = new System.Drawing.Point(0, 2);
            this.viewToolStrip.Margin = new System.Windows.Forms.Padding(0, 2, 0, 0);
            this.viewToolStrip.Name = "viewToolStrip";
            this.viewToolStrip.RenderMode = System.Windows.Forms.ToolStripRenderMode.System;
            this.viewToolStrip.Size = new System.Drawing.Size(128, 28);
            this.viewToolStrip.TabIndex = 17;
            this.viewToolStrip.Text = "toolStrip3";
            // 
            // browserToolStripButton
            // 
            this.browserToolStripButton.AutoSize = false;
            this.browserToolStripButton.AutoToolTip = false;
            this.browserToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.browserToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("browserToolStripButton.Image")));
            this.browserToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.browserToolStripButton.Margin = new System.Windows.Forms.Padding(2, 1, 2, 2);
            this.browserToolStripButton.Name = "browserToolStripButton";
            this.browserToolStripButton.Size = new System.Drawing.Size(25, 25);
            this.browserToolStripButton.Text = "toolStripButton3";
            this.browserToolStripButton.Click += new System.EventHandler(this.browserCheckBox_Click);
            // 
            // bookmarksToolStripButton
            // 
            this.bookmarksToolStripButton.AutoSize = false;
            this.bookmarksToolStripButton.AutoToolTip = false;
            this.bookmarksToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.bookmarksToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("bookmarksToolStripButton.Image")));
            this.bookmarksToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.bookmarksToolStripButton.Margin = new System.Windows.Forms.Padding(2, 1, 2, 2);
            this.bookmarksToolStripButton.Name = "bookmarksToolStripButton";
            this.bookmarksToolStripButton.Size = new System.Drawing.Size(25, 25);
            this.bookmarksToolStripButton.Text = "toolStripButton4";
            this.bookmarksToolStripButton.Click += new System.EventHandler(this.bookmarkCheckBox_Click);
            // 
            // historyToolStripButton
            // 
            this.historyToolStripButton.AutoSize = false;
            this.historyToolStripButton.AutoToolTip = false;
            this.historyToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.historyToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("historyToolStripButton.Image")));
            this.historyToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.historyToolStripButton.Margin = new System.Windows.Forms.Padding(2, 1, 2, 2);
            this.historyToolStripButton.Name = "historyToolStripButton";
            this.historyToolStripButton.Size = new System.Drawing.Size(25, 25);
            this.historyToolStripButton.Text = "toolStripButton5";
            this.historyToolStripButton.Click += new System.EventHandler(this.historyCheckBox_Click);
            // 
            // bonjourToolStripButton
            // 
            this.bonjourToolStripButton.AutoSize = false;
            this.bonjourToolStripButton.AutoToolTip = false;
            this.bonjourToolStripButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.bonjourToolStripButton.Image = ((System.Drawing.Image)(resources.GetObject("bonjourToolStripButton.Image")));
            this.bonjourToolStripButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.bonjourToolStripButton.Margin = new System.Windows.Forms.Padding(2, 1, 5, 2);
            this.bonjourToolStripButton.Name = "bonjourToolStripButton";
            this.bonjourToolStripButton.Size = new System.Drawing.Size(25, 25);
            this.bonjourToolStripButton.Text = "toolStripButton6";
            this.bonjourToolStripButton.Click += new System.EventHandler(this.bonjourCheckBox_Click);
            // 
            // toolStripSeparator23
            // 
            this.toolStripSeparator23.Name = "toolStripSeparator23";
            this.toolStripSeparator23.Size = new System.Drawing.Size(6, 28);
            // 
            // searchTextBox
            // 
            this.searchTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.searchTextBox.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
            this.searchTextBox.Location = new System.Drawing.Point(909, 3);
            this.searchTextBox.Name = "searchTextBox";
            this.searchTextBox.OptionsMenu = null;
            this.searchTextBox.Size = new System.Drawing.Size(114, 24);
            this.searchTextBox.TabIndex = 14;
            this.searchTextBox.TextChanged += new Ch.Cyberduck.ui.winforms.controls.SearchTextBox2.TextChange(this.searchTextBox_TextChanged);
            this.searchTextBox.KeyUp += new System.Windows.Forms.KeyEventHandler(this.searchTextBox_KeyUp);
            // 
            // parentPathButton
            // 
            this.parentPathButton.FlatAppearance.BorderSize = 0;
            this.parentPathButton.Image = ((System.Drawing.Image)(resources.GetObject("parentPathButton.Image")));
            this.parentPathButton.Location = new System.Drawing.Point(867, 3);
            this.parentPathButton.Name = "parentPathButton";
            this.parentPathButton.Size = new System.Drawing.Size(35, 25);
            this.parentPathButton.TabIndex = 7;
            this.parentPathButton.UseVisualStyleBackColor = true;
            // 
            // pathComboBox
            // 
            this.pathComboBox.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.pathComboBox.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.pathComboBox.FormattingEnabled = true;
            this.pathComboBox.Location = new System.Drawing.Point(215, 3);
            this.pathComboBox.Name = "pathComboBox";
            this.pathComboBox.Size = new System.Drawing.Size(646, 23);
            this.pathComboBox.TabIndex = 6;
            this.pathComboBox.SelectionChangeCommitted += new System.EventHandler(this.pathComboBox_SelectionChangeCommitted);
            // 
            // historyForwardButton
            // 
            this.historyForwardButton.FlatAppearance.BorderSize = 0;
            this.historyForwardButton.Image = ((System.Drawing.Image)(resources.GetObject("historyForwardButton.Image")));
            this.historyForwardButton.Location = new System.Drawing.Point(173, 3);
            this.historyForwardButton.Name = "historyForwardButton";
            this.historyForwardButton.Size = new System.Drawing.Size(35, 25);
            this.historyForwardButton.TabIndex = 5;
            this.historyForwardButton.UseVisualStyleBackColor = true;
            // 
            // historyBackButton
            // 
            this.historyBackButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.historyBackButton.FlatAppearance.BorderSize = 0;
            this.historyBackButton.Image = ((System.Drawing.Image)(resources.GetObject("historyBackButton.Image")));
            this.historyBackButton.Location = new System.Drawing.Point(132, 3);
            this.historyBackButton.Name = "historyBackButton";
            this.historyBackButton.Size = new System.Drawing.Size(35, 25);
            this.historyBackButton.TabIndex = 4;
            this.historyBackButton.UseVisualStyleBackColor = true;
            // 
            // bookmarkContextMenuStrip
            // 
            this.bookmarkContextMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.connectBookmarkContextToolStripMenuItem,
            this.toolStripSeparator29,
            this.newBookmarkContextToolStripMenuItem1,
            this.deleteBookmarkContextToolStripMenuItem1,
            this.editBookmarkContextToolStripMenuItem1,
            this.duplicateBookmarkToolStripMenuItem});
            this.bookmarkContextMenuStrip.Name = "bookmarkContextMenuStrip";
            this.bookmarkContextMenuStrip.Size = new System.Drawing.Size(182, 120);
            // 
            // connectBookmarkContextToolStripMenuItem
            // 
            this.connectBookmarkContextToolStripMenuItem.Name = "connectBookmarkContextToolStripMenuItem";
            this.connectBookmarkContextToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.connectBookmarkContextToolStripMenuItem.Text = "Connect to server";
            // 
            // toolStripSeparator29
            // 
            this.toolStripSeparator29.Name = "toolStripSeparator29";
            this.toolStripSeparator29.Size = new System.Drawing.Size(178, 6);
            // 
            // newBookmarkContextToolStripMenuItem1
            // 
            this.newBookmarkContextToolStripMenuItem1.Name = "newBookmarkContextToolStripMenuItem1";
            this.newBookmarkContextToolStripMenuItem1.Size = new System.Drawing.Size(181, 22);
            this.newBookmarkContextToolStripMenuItem1.Text = "New Bookmark";
            // 
            // deleteBookmarkContextToolStripMenuItem1
            // 
            this.deleteBookmarkContextToolStripMenuItem1.Name = "deleteBookmarkContextToolStripMenuItem1";
            this.deleteBookmarkContextToolStripMenuItem1.Size = new System.Drawing.Size(181, 22);
            this.deleteBookmarkContextToolStripMenuItem1.Text = "Delete Bookmark";
            // 
            // editBookmarkContextToolStripMenuItem1
            // 
            this.editBookmarkContextToolStripMenuItem1.Name = "editBookmarkContextToolStripMenuItem1";
            this.editBookmarkContextToolStripMenuItem1.Size = new System.Drawing.Size(181, 22);
            this.editBookmarkContextToolStripMenuItem1.Text = "Edit Bookmark";
            // 
            // duplicateBookmarkToolStripMenuItem
            // 
            this.duplicateBookmarkToolStripMenuItem.Name = "duplicateBookmarkToolStripMenuItem";
            this.duplicateBookmarkToolStripMenuItem.Size = new System.Drawing.Size(181, 22);
            this.duplicateBookmarkToolStripMenuItem.Text = "Duplicate Bookmark";
            // 
            // saveFileDialog
            // 
            this.saveFileDialog.AddExtension = false;
            this.saveFileDialog.Filter = "|*.*";
            this.saveFileDialog.OverwritePrompt = false;
            this.saveFileDialog.Title = "Download the selected file to…";
            // 
            // openFileDialog
            // 
            this.openFileDialog.Multiselect = true;
            this.openFileDialog.Title = "Upload";
            // 
            // vistaMenu1
            // 
            this.vistaMenu1.ContainerControl = this;
            // 
            // toolbarContextMenu1
            // 
            this.toolbarContextMenu1.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.openConnectionToolbarMenuItem,
            this.menuItem91,
            this.quickConnectToolbarMenuItem,
            this.actionContextToolbarMenuItem,
            this.menuItem94,
            this.infoToolbarMenuItem,
            this.refreshToolbarMenuItem,
            this.menuItem97,
            this.editToolbarMenuItem,
            this.openInWebBrowserToolbarMenuItem,
            this.openInTerminalToolbarMenuItem,
            this.newFolderToolbarMenuItem,
            this.deleteToolbarMenuItem,
            this.menuItem102,
            this.downloadToolbarMenuItem,
            this.uploadToolbarMenuItem,
            this.transfersToolbarMenuItem,
            this.logToolbarMenuItem});
            // 
            // openConnectionToolbarMenuItem
            // 
            this.openConnectionToolbarMenuItem.Index = 0;
            this.openConnectionToolbarMenuItem.Text = "New Connection";
            // 
            // menuItem91
            // 
            this.menuItem91.Index = 1;
            this.menuItem91.Text = "-";
            // 
            // quickConnectToolbarMenuItem
            // 
            this.quickConnectToolbarMenuItem.Index = 2;
            this.quickConnectToolbarMenuItem.Text = "Quick Connect";
            // 
            // actionContextToolbarMenuItem
            // 
            this.actionContextToolbarMenuItem.Index = 3;
            this.actionContextToolbarMenuItem.Text = "Action";
            // 
            // menuItem94
            // 
            this.menuItem94.Index = 4;
            this.menuItem94.Text = "-";
            // 
            // infoToolbarMenuItem
            // 
            this.infoToolbarMenuItem.Index = 5;
            this.infoToolbarMenuItem.Text = "Info";
            // 
            // refreshToolbarMenuItem
            // 
            this.refreshToolbarMenuItem.Index = 6;
            this.refreshToolbarMenuItem.Text = "Refresh";
            // 
            // menuItem97
            // 
            this.menuItem97.Index = 7;
            this.menuItem97.Text = "-";
            // 
            // editToolbarMenuItem
            // 
            this.editToolbarMenuItem.Index = 8;
            this.editToolbarMenuItem.Text = "Edit";
            // 
            // openInWebBrowserToolbarMenuItem
            // 
            this.openInWebBrowserToolbarMenuItem.Index = 9;
            this.openInWebBrowserToolbarMenuItem.Text = "Open in Web Browser";
            // 
            // openInTerminalToolbarMenuItem
            // 
            this.openInTerminalToolbarMenuItem.Index = 10;
            this.openInTerminalToolbarMenuItem.Text = "Open in Terminal";
            // 
            // newFolderToolbarMenuItem
            // 
            this.newFolderToolbarMenuItem.Index = 11;
            this.newFolderToolbarMenuItem.Text = "New Folder";
            // 
            // deleteToolbarMenuItem
            // 
            this.deleteToolbarMenuItem.Index = 12;
            this.deleteToolbarMenuItem.Text = "Delete";
            // 
            // menuItem102
            // 
            this.menuItem102.Index = 13;
            this.menuItem102.Text = "-";
            // 
            // downloadToolbarMenuItem
            // 
            this.downloadToolbarMenuItem.Index = 14;
            this.downloadToolbarMenuItem.Text = "Download";
            // 
            // uploadToolbarMenuItem
            // 
            this.uploadToolbarMenuItem.Index = 15;
            this.uploadToolbarMenuItem.Text = "Upload";
            // 
            // transfersToolbarMenuItem
            // 
            this.transfersToolbarMenuItem.Index = 16;
            this.transfersToolbarMenuItem.Text = "Transfers";
            // 
            // logToolbarMenuItem
            // 
            this.logToolbarMenuItem.Index = 17;
            this.logToolbarMenuItem.Text = "Log";
            // 
            // browserContextMenu
            // 
            this.browserContextMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.refreshBrowserContextMenuItem,
            this.menuItem44,
            this.newFolderBrowserContextMenuItem,
            this.newVaultBrowserContextMenuItem,
            this.newFileBrowserContextMenuItem,
            this.newSymlinkBrowserContextMenuItem,
            this.renameBrowserContextMenuItem,
            this.duplicateFileBrowserContextMenuItem,
            this.menuItem96,
            this.copyUrlBrowserContextMenuItem,
            this.openUrlBrowserContextMenuItem,
            this.editBrowserContextMenuItem,
            this.infoBrowserContextMenuItem,
            this.menuItem105,
            this.downloadBrowserContextMenuItem,
            this.downloadAsBrowserContextMenuItem,
            this.downloadToBrowserContextMenuItem,
            this.uploadBrowserContextMenuItem,
            this.synchronizeBrowserContextMenuItem,
            this.menuItem101,
            this.deleteBrowserContextMenuItem,
            this.revertBrowserContextMenuItem,
            this.menuItem110,
            this.createArchiveBrowserContextMenuItem,
            this.expandArchiveBrowserContextMnuItem,
            this.menuItem113,
            this.newBrowserBrowserContextMenuItem,
            this.newBookmarkBrowserContextMenuItem});
            // 
            // refreshBrowserContextMenuItem
            // 
            this.refreshBrowserContextMenuItem.Index = 0;
            this.refreshBrowserContextMenuItem.Text = "Refresh";
            // 
            // menuItem44
            // 
            this.menuItem44.Index = 1;
            this.menuItem44.Text = "-";
            // 
            // newFolderBrowserContextMenuItem
            // 
            this.newFolderBrowserContextMenuItem.Index = 2;
            this.newFolderBrowserContextMenuItem.Text = "New Folder…";
            // 
            // newVaultBrowserContextMenuItem
            // 
            this.newVaultBrowserContextMenuItem.Index = 3;
            this.newVaultBrowserContextMenuItem.Text = "New Encrypted Vault…";
            // 
            // newFileBrowserContextMenuItem
            // 
            this.newFileBrowserContextMenuItem.Index = 4;
            this.newFileBrowserContextMenuItem.Text = "New File…";
            // 
            // newSymlinkBrowserContextMenuItem
            // 
            this.newSymlinkBrowserContextMenuItem.Index = 5;
            this.newSymlinkBrowserContextMenuItem.Text = "New Symbolic Link…";
            // 
            // renameBrowserContextMenuItem
            // 
            this.renameBrowserContextMenuItem.Index = 6;
            this.renameBrowserContextMenuItem.Text = "Rename…";
            // 
            // duplicateFileBrowserContextMenuItem
            // 
            this.duplicateFileBrowserContextMenuItem.Index = 7;
            this.duplicateFileBrowserContextMenuItem.Text = "Duplicate…";
            // 
            // menuItem96
            // 
            this.menuItem96.Index = 8;
            this.menuItem96.Text = "-";
            // 
            // copyUrlBrowserContextMenuItem
            // 
            this.copyUrlBrowserContextMenuItem.Index = 9;
            this.copyUrlBrowserContextMenuItem.Text = "Copy URL";
            // 
            // openUrlBrowserContextMenuItem
            // 
            this.openUrlBrowserContextMenuItem.Index = 10;
            this.openUrlBrowserContextMenuItem.Text = "Open URL";
            // 
            // editBrowserContextMenuItem
            // 
            this.editBrowserContextMenuItem.Index = 11;
            this.editBrowserContextMenuItem.Text = "Edit With";
            // 
            // infoBrowserContextMenuItem
            // 
            this.infoBrowserContextMenuItem.Index = 12;
            this.infoBrowserContextMenuItem.Text = "Info";
            // 
            // menuItem105
            // 
            this.menuItem105.Index = 13;
            this.menuItem105.Text = "-";
            // 
            // downloadBrowserContextMenuItem
            // 
            this.downloadBrowserContextMenuItem.Index = 14;
            this.downloadBrowserContextMenuItem.Text = "Download";
            // 
            // downloadAsBrowserContextMenuItem
            // 
            this.downloadAsBrowserContextMenuItem.Index = 15;
            this.downloadAsBrowserContextMenuItem.Text = "Download As…";
            // 
            // downloadToBrowserContextMenuItem
            // 
            this.downloadToBrowserContextMenuItem.Index = 16;
            this.downloadToBrowserContextMenuItem.Text = "Download To…";
            // 
            // uploadBrowserContextMenuItem
            // 
            this.uploadBrowserContextMenuItem.Index = 17;
            this.uploadBrowserContextMenuItem.Text = "Upload…";
            // 
            // synchronizeBrowserContextMenuItem
            // 
            this.synchronizeBrowserContextMenuItem.Index = 18;
            this.synchronizeBrowserContextMenuItem.Text = "Synchronize…";
            // 
            // menuItem101
            // 
            this.menuItem101.Index = 19;
            this.menuItem101.Text = "-";
            // 
            // deleteBrowserContextMenuItem
            // 
            this.deleteBrowserContextMenuItem.Index = 20;
            this.deleteBrowserContextMenuItem.Text = "Delete";
            // 
            // revertBrowserContextMenuItem
            // 
            this.revertBrowserContextMenuItem.Index = 21;
            this.revertBrowserContextMenuItem.Text = "Revert";
            // 
            // menuItem110
            // 
            this.menuItem110.Index = 22;
            this.menuItem110.Text = "-";
            // 
            // createArchiveBrowserContextMenuItem
            // 
            this.createArchiveBrowserContextMenuItem.Index = 23;
            this.createArchiveBrowserContextMenuItem.Text = "Create Archive";
            // 
            // expandArchiveBrowserContextMnuItem
            // 
            this.expandArchiveBrowserContextMnuItem.Index = 24;
            this.expandArchiveBrowserContextMnuItem.Text = "Expand Archive";
            // 
            // menuItem113
            // 
            this.menuItem113.Index = 25;
            this.menuItem113.Text = "-";
            // 
            // newBrowserBrowserContextMenuItem
            // 
            this.newBrowserBrowserContextMenuItem.Index = 26;
            this.newBrowserBrowserContextMenuItem.Text = "New Browser";
            // 
            // newBookmarkBrowserContextMenuItem
            // 
            this.newBookmarkBrowserContextMenuItem.Index = 27;
            this.newBookmarkBrowserContextMenuItem.Text = "New Bookmark";
            // 
            // bookmarkContextMenu
            // 
            this.bookmarkContextMenu.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.connectBookmarkContextMenuItem,
            this.menuItem5,
            this.newBookmarkContextMenuItem,
            this.deleteBookmarkContextMenuItem,
            this.editBookmarkContextMenuItem,
            this.duplicateBookmarkContextMenuItem,
            this.menuItem8,
            this.menuItem9});
            // 
            // connectBookmarkContextMenuItem
            // 
            this.connectBookmarkContextMenuItem.Index = 0;
            this.connectBookmarkContextMenuItem.Text = "Connect to server";
            // 
            // menuItem5
            // 
            this.menuItem5.Index = 1;
            this.menuItem5.Text = "-";
            // 
            // newBookmarkContextMenuItem
            // 
            this.newBookmarkContextMenuItem.Index = 2;
            this.newBookmarkContextMenuItem.Text = "New Bookmark";
            // 
            // deleteBookmarkContextMenuItem
            // 
            this.deleteBookmarkContextMenuItem.Index = 3;
            this.deleteBookmarkContextMenuItem.Text = "Delete Bookmark";
            // 
            // editBookmarkContextMenuItem
            // 
            this.editBookmarkContextMenuItem.Index = 4;
            this.editBookmarkContextMenuItem.Text = "Edit Bookmark";
            // 
            // duplicateBookmarkContextMenuItem
            // 
            this.duplicateBookmarkContextMenuItem.Index = 5;
            this.duplicateBookmarkContextMenuItem.Text = "Duplicate Bookmark";
            // 
            // menuItem8
            // 
            this.menuItem8.Index = 6;
            this.menuItem8.Text = "-";
            // 
            // menuItem9
            // 
            this.menuItem9.Index = 7;
            this.menuItem9.MenuItems.AddRange(new System.Windows.Forms.MenuItem[] {
            this.sortByNicknameBookmarkContextMenuItem,
            this.sortByHostnameBookmarkContextMenuItem,
            this.sortByProtocolBookmarkContextMenuItem});
            this.menuItem9.Text = "Sort By";
            // 
            // sortByNicknameBookmarkContextMenuItem
            // 
            this.sortByNicknameBookmarkContextMenuItem.Index = 0;
            this.sortByNicknameBookmarkContextMenuItem.Text = "Nickname";
            // 
            // sortByHostnameBookmarkContextMenuItem
            // 
            this.sortByHostnameBookmarkContextMenuItem.Index = 1;
            this.sortByHostnameBookmarkContextMenuItem.Text = "Hostname";
            // 
            // sortByProtocolBookmarkContextMenuItem
            // 
            this.sortByProtocolBookmarkContextMenuItem.Index = 2;
            this.sortByProtocolBookmarkContextMenuItem.Text = "Protocol";
            // 
            // BrowserForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(7F, 15F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1028, 606);
            this.Controls.Add(this.toolStripContainer1);
            this.MinimumSize = new System.Drawing.Size(457, 39);
            this.Name = "BrowserForm";
            this.Text = "Cyberduck";
            this.statusStrip.ResumeLayout(false);
            this.statusStrip.PerformLayout();
            this.menuStrip1.ResumeLayout(false);
            this.menuStrip1.PerformLayout();
            this.toolbarContextMenu.ResumeLayout(false);
            this.editorMenuStrip.ResumeLayout(false);
            this.toolBar.ResumeLayout(false);
            this.toolBar.PerformLayout();
            this.contextMenuStrip.ResumeLayout(false);
            this.toolStripContainer1.BottomToolStripPanel.ResumeLayout(false);
            this.toolStripContainer1.BottomToolStripPanel.PerformLayout();
            this.toolStripContainer1.ContentPanel.ResumeLayout(false);
            this.toolStripContainer1.ContentPanel.PerformLayout();
            this.toolStripContainer1.TopToolStripPanel.ResumeLayout(false);
            this.toolStripContainer1.TopToolStripPanel.PerformLayout();
            this.toolStripContainer1.ResumeLayout(false);
            this.toolStripContainer1.PerformLayout();
            this.panelManager1.ResumeLayout(false);
            this.managedBrowserPanel1.ResumeLayout(false);
            this.splitContainer.Panel1.ResumeLayout(false);
            this.splitContainer.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer)).EndInit();
            this.splitContainer.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.browser)).EndInit();
            this.managedBookmarkPanel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.bookmarkListView)).EndInit();
            this.actionToolStrip.ResumeLayout(false);
            this.actionToolStrip.PerformLayout();
            this.viewPanel.ResumeLayout(false);
            this.viewPanel.PerformLayout();
            this.tableLayoutPanel1.ResumeLayout(false);
            this.tableLayoutPanel1.PerformLayout();
            this.viewToolStrip.ResumeLayout(false);
            this.viewToolStrip.PerformLayout();
            this.bookmarkContextMenuStrip.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.vistaMenu1)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.StatusStrip statusStrip;
        private System.Windows.Forms.ToolStripStatusLabel statusLabel;
        private System.Windows.Forms.ToolStripMenuItem fileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newBrowserToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem editToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem helpToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem aboutCyberduckToolStripMenuItem;
        private System.Windows.Forms.ToolStripButton openConnectionToolStripButton;
        private System.Windows.Forms.ToolStripMenuItem viewToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem goToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem bookmarkToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem windowToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem viewBookmarksToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.ToolStripMenuItem newBookmarkToolStripMenuItem;
        private System.Windows.Forms.ImageList iconList;
        private System.Windows.Forms.ToolStripComboBox quickConnectToolStripComboBox;
        private System.Windows.Forms.ContextMenuStrip contextMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem refreshContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator2;
        private System.Windows.Forms.ToolStripMenuItem infoContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem renameContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem downloadContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem downloadAsContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem downloadToContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem editContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem deleteContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem duplicateFileContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator3;
        private System.Windows.Forms.ToolStripMenuItem uploadContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem synchronizeContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newFolderContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newVaultContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newFileContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator4;
        private System.Windows.Forms.ToolStripMenuItem createArchiveContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem expandArchiveContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator5;
        private System.Windows.Forms.ToolStripMenuItem openURLContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator6;
        private System.Windows.Forms.ToolStripMenuItem newBrowserContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newBookmarkContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripContainer toolStripContainer1;
        private System.Windows.Forms.SplitContainer splitContainer;
        private System.Windows.Forms.RichTextBox transcriptBox;
        private MulticolorTreeListView browser;
        private SortComparatorOLVColumn treeColumnName;
        private SortComparatorOLVColumn treeColumnSize;
        private SortComparatorOLVColumn treeColumnModified;
        private SortComparatorOLVColumn treeColumnOwner;
        private SortComparatorOLVColumn treeColumnGroup;
        private SortComparatorOLVColumn treeColumnPermissions;
        private SortComparatorOLVColumn treeColumnKind;
        private SortComparatorOLVColumn treeColumnExtension;
        private SortComparatorOLVColumn treeColumnRegion;
        private SortComparatorOLVColumn treeColumnVersion;
        private System.Windows.Forms.ToolStripMenuItem infoToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem activitiyToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem transfersToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openConnectionToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator7;
        private System.Windows.Forms.ToolStripMenuItem newDownloadToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator8;
        private System.Windows.Forms.ToolStripMenuItem newFolderToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newVaultToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newFileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem renameFileToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem duplicateFileToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator9;
        private System.Windows.Forms.ToolStripMenuItem openWebURLToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem editWithToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator10;
        private System.Windows.Forms.ToolStripMenuItem downloadToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem downloadAsToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem downloadToToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem uploadToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem synchronizeToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem deleteToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator11;
        private System.Windows.Forms.ToolStripMenuItem createArchiveToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem expandArchiveToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator12;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem cutToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem copyToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem copyURLToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem pasteToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem selectAllToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator14;
        private System.Windows.Forms.ToolStripMenuItem preferencesToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem showHiddenFilesToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem textEncodingToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator13;
        private System.Windows.Forms.ToolStripMenuItem toggleLogDrawerToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem toggleToolbarToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator15;
        private System.Windows.Forms.ToolStripMenuItem refreshToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator16;
        private System.Windows.Forms.ToolStripMenuItem gotoFolderToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem backToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem forwardToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem enclosingFolderToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem insideToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem searchToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator17;
        private System.Windows.Forms.ToolStripMenuItem sendCommandToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator18;
        private System.Windows.Forms.ToolStripMenuItem stopToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem disconnectToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem deleteBookmarkToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem editBookmarkToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator19;
        private System.Windows.Forms.ToolStripMenuItem historyToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem bonjourToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem minimizeToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator20;
        private System.Windows.Forms.ToolStripMenuItem cyberduckHelpToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem licenseToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem acknowledgmentsToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator21;
        private System.Windows.Forms.ToolStripMenuItem reportABugToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator22;
        private System.Windows.Forms.ContextMenuStrip editorMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem editor1ToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem editor2ToolStripMenuItem;
        private System.Windows.Forms.ContextMenuStrip archiveMenuStrip;
        private System.Windows.Forms.ContextMenuStrip textEncodingMenuStrip;
        private System.Windows.Forms.ToolStripButton refreshToolStripButton;
        private System.Windows.Forms.ToolStripButton disconnectStripButton;
        private System.Windows.Forms.ToolStripDropDownButton actionToolStripDropDownButton;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparatorAfterOpenConnection;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparatorAfterAction;
        private System.Windows.Forms.SaveFileDialog saveFileDialog;
        private System.Windows.Forms.FolderBrowserDialog folderBrowserDialog;
        private System.Windows.Forms.OpenFileDialog openFileDialog;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator25;
        private System.Windows.Forms.ToolStripMenuItem revertToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator26;
        private System.Windows.Forms.ToolStripMenuItem revertContxtStripMenuItem;
        private System.Windows.Forms.ToolStrip actionToolStrip;
        private System.Windows.Forms.ToolStripButton newBookmarkToolStripButton;
        private System.Windows.Forms.ToolStripButton editBookmarkToolStripButton;
        private System.Windows.Forms.ToolStripButton deleteBookmarkToolStripButton;
        private BrightIdeasSoftware.OLVColumn bookmarkImageColumn;
        private BrightIdeasSoftware.OLVColumn bookmarkDescriptionColumn;
        private BrightIdeasSoftware.OLVColumn activeColumn;
        private System.Windows.Forms.ContextMenuStrip bookmarkContextMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem connectBookmarkContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator29;
        private System.Windows.Forms.ToolStripMenuItem newBookmarkContextToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem deleteBookmarkContextToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem editBookmarkContextToolStripMenuItem1;
        private LineSeparatedObjectListView bookmarkListView;
        private System.Windows.Forms.Panel viewPanel;
        private System.Windows.Forms.Button parentPathButton;
        private System.Windows.Forms.ComboBox pathComboBox;
        private System.Windows.Forms.Button historyForwardButton;
        private System.Windows.Forms.Button historyBackButton;
        private System.Windows.Forms.ToolStripMenuItem checkToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator28;
        private System.Windows.Forms.ToolStripMenuItem columnToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem toolbarToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openConnectionToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem quickConnectToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem actionToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator30;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator31;
        private System.Windows.Forms.ToolStripMenuItem infoToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem refreshToolStripMenuItem1;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator32;
        private System.Windows.Forms.ToolStripMenuItem editToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem openInWebBrowserToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem openInTerminalToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem deleteToolStripMenuItem1;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator33;
        private System.Windows.Forms.ToolStripMenuItem downloadToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem uploadToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem transfersToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem logToolStripMenuItem1;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator34;
        private System.Windows.Forms.ToolStripMenuItem printToolStripMenuItem;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator35;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator36;
        private System.Windows.Forms.ToolStripButton infoToolStripButton;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparatorAfterRefresh;
        private System.Windows.Forms.ToolStripButton openInBrowserToolStripButton;
        private System.Windows.Forms.ToolStripButton logToolStripButton;
        private System.Windows.Forms.ToolStripButton newFolderToolStripButton;
        private System.Windows.Forms.ToolStripButton deleteToolStripButton;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparatorAfterDelete;
        private System.Windows.Forms.ToolStripButton downloadToolStripButton;
        private System.Windows.Forms.ToolStripButton uploadToolStripButton;
        private System.Windows.Forms.ContextMenuStrip toolbarContextMenu;
        private System.Windows.Forms.ContextMenuStrip columnContextMenu;
        private System.Windows.Forms.ContextMenuStrip historyMenuStrip;
        private System.Windows.Forms.ContextMenuStrip bonjourMenuStrip;
        private System.Windows.Forms.ToolStripStatusLabel securityToolStripStatusLabel;
        private System.Windows.Forms.ToolStripMenuItem duplicateBookmarkToolStripMenuItem1;
        private System.Windows.Forms.ToolStripMenuItem duplicateBookmarkToolStripMenuItem;
        private ClickThroughMenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripStatusLabel toolStripProgress;
        private ClickThroughToolStrip toolBar;
        private ui.winforms.controls.SearchTextBox2 searchTextBox;
        private System.Windows.Forms.TableLayoutPanel tableLayoutPanel1;
        private System.Windows.Forms.MainMenu mainMenu;
        private System.Windows.Forms.MenuItem menuItem1;
        private System.Windows.Forms.MenuItem newBrowserMainMenuItem;
        private System.Windows.Forms.MenuItem openConnectionMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem4;
        private System.Windows.Forms.MenuItem newDownloadMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem6;
        private System.Windows.Forms.MenuItem newFolderMainMenuItem;
        private System.Windows.Forms.MenuItem newVaultMainMenuItem;
        private System.Windows.Forms.MenuItem newFileMainMenuItem;
        private System.Windows.Forms.MenuItem renameMainMenuItem;
        private System.Windows.Forms.MenuItem duplicateMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem11;
        private System.Windows.Forms.MenuItem openUrlMainMenuItem;
        private System.Windows.Forms.MenuItem editMainMenuItem;
        private System.Windows.Forms.MenuItem infoMainMenuItem;
        private System.Windows.Forms.MenuItem downloadMainMenuItem;
        private System.Windows.Forms.MenuItem downloadAsMainMenuItem;
        private System.Windows.Forms.MenuItem downloadToMainMenuItem;
        private System.Windows.Forms.MenuItem uploadMainMenuItem;
        private System.Windows.Forms.MenuItem synchronizeMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem21;
        private System.Windows.Forms.MenuItem deleteMainMenuItem;
        private System.Windows.Forms.MenuItem revertMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem24;
        private System.Windows.Forms.MenuItem createArchiveMainMenuItem;
        private System.Windows.Forms.MenuItem expandArchiveMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem27;
        private System.Windows.Forms.MenuItem printMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem29;
        private System.Windows.Forms.MenuItem exitMainMenuItem;
        private System.Windows.Forms.MenuItem mainMenuItem2;
        private System.Windows.Forms.MenuItem cutMainMenuItem;
        private System.Windows.Forms.MenuItem copyMainMenuItem;
        private System.Windows.Forms.MenuItem copyUrlMainMenuItem;
        private System.Windows.Forms.MenuItem pasteMainMenuItem;
        private System.Windows.Forms.MenuItem selectAllMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem37;
        private System.Windows.Forms.MenuItem preferencesMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem39;
        private System.Windows.Forms.MenuItem toggleToolbarMainMenuItem;
        private System.Windows.Forms.MenuItem customizeToolbarMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem42;
        private System.Windows.Forms.MenuItem menuItem43;
        private System.Windows.Forms.MenuItem columnMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem45;
        private System.Windows.Forms.MenuItem showHiddenFilesMainMenuItem;
        private System.Windows.Forms.MenuItem textEncodingMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem48;
        private System.Windows.Forms.MenuItem toggleLogDrawerMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem50;
        private System.Windows.Forms.MenuItem refreshMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem52;
        private System.Windows.Forms.MenuItem goToFolderMainMenuItem;
        private System.Windows.Forms.MenuItem backMainMenuItem;
        private System.Windows.Forms.MenuItem forwardMainMenuItem;
        private System.Windows.Forms.MenuItem enclosingFolderMainMenuItem;
        private System.Windows.Forms.MenuItem insideMainMenuItem;
        private System.Windows.Forms.MenuItem searchMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem59;
        private System.Windows.Forms.MenuItem sendCommandMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem61;
        private System.Windows.Forms.MenuItem stopMainMenuItem;
        private System.Windows.Forms.MenuItem disconnectMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem64;
        private System.Windows.Forms.MenuItem toggleBookmarksMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem66;
        private System.Windows.Forms.MenuItem newBookmarkMainMenuItem;
        private System.Windows.Forms.MenuItem deleteBookmarkMainMenuItem;
        private System.Windows.Forms.MenuItem editBookmarkMainMenuItem;
        private System.Windows.Forms.MenuItem duplicateBookmarkMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem71;
        private System.Windows.Forms.MenuItem historyMainMenuItem;
        private System.Windows.Forms.MenuItem bonjourMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem74;
        private System.Windows.Forms.MenuItem menuItem75;
        private System.Windows.Forms.MenuItem minimizeMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem77;
        private System.Windows.Forms.MenuItem transfersMainMenuItem;
        private System.Windows.Forms.MenuItem activityMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem80;
        private System.Windows.Forms.MenuItem helpMainMenuItem;
        private System.Windows.Forms.MenuItem licenseMainMenuItem;
        private System.Windows.Forms.MenuItem acknowledgmentsMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem84;
        private System.Windows.Forms.MenuItem bugMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem86;
        private System.Windows.Forms.MenuItem updateMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem88;
        private System.Windows.Forms.MenuItem aboutMainMenuItem;
        private VistaMenu vistaMenu1;
        private System.Windows.Forms.ContextMenu toolbarContextMenu1;
        private System.Windows.Forms.MenuItem openConnectionToolbarMenuItem;
        private System.Windows.Forms.MenuItem menuItem91;
        private System.Windows.Forms.MenuItem quickConnectToolbarMenuItem;
        private System.Windows.Forms.MenuItem actionContextToolbarMenuItem;
        private System.Windows.Forms.MenuItem menuItem94;
        private System.Windows.Forms.MenuItem infoToolbarMenuItem;
        private System.Windows.Forms.MenuItem refreshToolbarMenuItem;
        private System.Windows.Forms.MenuItem menuItem97;
        private System.Windows.Forms.MenuItem editToolbarMenuItem;
        private System.Windows.Forms.MenuItem openInWebBrowserToolbarMenuItem;
        private System.Windows.Forms.MenuItem newFolderToolbarMenuItem;
        private System.Windows.Forms.MenuItem deleteToolbarMenuItem;
        private System.Windows.Forms.MenuItem menuItem102;
        private System.Windows.Forms.MenuItem downloadToolbarMenuItem;
        private System.Windows.Forms.MenuItem uploadToolbarMenuItem;
        private System.Windows.Forms.MenuItem transfersToolbarMenuItem;
        private System.Windows.Forms.ContextMenu browserContextMenu;
        private System.Windows.Forms.MenuItem refreshBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem44;
        private System.Windows.Forms.MenuItem infoBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem editBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem renameBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem duplicateFileBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem96;
        private System.Windows.Forms.MenuItem downloadBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem downloadAsBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem downloadToBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem101;
        private System.Windows.Forms.MenuItem deleteBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem revertBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem105;
        private System.Windows.Forms.MenuItem uploadBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem synchronizeBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem newFolderBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem newFileBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem110;
        private System.Windows.Forms.MenuItem createArchiveBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem expandArchiveBrowserContextMnuItem;
        private System.Windows.Forms.MenuItem menuItem113;
        private System.Windows.Forms.MenuItem copyUrlBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem openUrlBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem newBrowserBrowserContextMenuItem;
        private System.Windows.Forms.MenuItem newBookmarkBrowserContextMenuItem;
        private PanelManager panelManager1;
        private ManagedPanel managedBrowserPanel1;
        private ManagedPanel managedBookmarkPanel2;
        private System.Windows.Forms.ContextMenu bookmarkContextMenu;
        private System.Windows.Forms.MenuItem connectBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem5;
        private System.Windows.Forms.MenuItem newBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem deleteBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem editBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem duplicateBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem menuItem2;
        private ClickThroughToolStrip viewToolStrip;
        private System.Windows.Forms.ToolStripButton browserToolStripButton;
        private System.Windows.Forms.ToolStripButton bookmarksToolStripButton;
        private System.Windows.Forms.ToolStripButton historyToolStripButton;
        private System.Windows.Forms.ToolStripButton bonjourToolStripButton;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator23;
        private System.Windows.Forms.ToolStripMenuItem copyURLContextToolStripMenuItem;
        private System.Windows.Forms.ToolStripSplitButton editToolStripSplitButton;
        private System.Windows.Forms.MenuItem keyMainMenuItem;
        private System.Windows.Forms.MenuItem donateMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem7;
        private System.Windows.Forms.MenuItem openInTerminalMainMenuItem;
        private System.Windows.Forms.ToolStripButton openInTerminalToolStripButton;
        private System.Windows.Forms.MenuItem openInTerminalToolbarMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newFolderToolStripMenuItem1;
        private System.Windows.Forms.MenuItem menuItem3;
        private System.Windows.Forms.MenuItem sortByNicknameMainMenuItem;
        private System.Windows.Forms.MenuItem sortByHostnameMainMenuItem;
        private System.Windows.Forms.MenuItem sortByProtocolMainMenuItem;
        private System.Windows.Forms.MenuItem menuItem8;
        private System.Windows.Forms.MenuItem menuItem9;
        private System.Windows.Forms.MenuItem sortByNicknameBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem sortByHostnameBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem sortByProtocolBookmarkContextMenuItem;
        private System.Windows.Forms.MenuItem newSymbolicLinkMainMenuItem;
        private System.Windows.Forms.ToolStripMenuItem newSymlinkContextToolStripMenuItem;
        private System.Windows.Forms.MenuItem newSymlinkBrowserContextMenuItem;
        private System.Windows.Forms.ToolStripButton transfersToolStripButton;
        private System.Windows.Forms.MenuItem logToolbarMenuItem;
        private MenuItem newVaultBrowserContextMenuItem;
    }
}

