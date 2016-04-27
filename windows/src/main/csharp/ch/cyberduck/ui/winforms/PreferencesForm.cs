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

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using Application = ch.cyberduck.core.local.Application;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class PreferencesForm : BaseForm, IPreferencesView
    {
        private const int MaxHeight = 800;
        private const int MaxWidth = 1000;
        private const int MinHeight = 250;
        private const int MinWidth = 450;
        private Application _lastSelectedEditor;

        public PreferencesForm()
        {
            InitializeComponent();

            Load += delegate
            {
                int newWidth = 10; // border etc.
                foreach (ToolStripItem item in toolStrip.Items)
                {
                    newWidth += item.Size.Width + item.Margin.Left + item.Margin.Right;
                }
                Width = newWidth;
            };

            MaximumSize = new Size(MaxWidth, MaxHeight);
            MinimumSize = new Size(MinWidth, MinHeight);

            sftpButton.Image = IconCache.Instance.IconForName("ftp", 32);
            s3Button.Image = IconCache.Instance.IconForName("s3", 32);
            googleDocsButton.Image = IconCache.Instance.IconForName("googledrive", 32);

            connectBookmarkCombobox.ICImageList = IconCache.Instance.GetProtocolIcons();
            defaultProtocolCombobox.ICImageList = IconCache.Instance.GetProtocolIcons();

            showDownloadFolderDialogButton.Text = LocaleFactory.localizedString("Choose") + "…";

            #region Font Settings

            Font smallerFont = new Font(Font.FontFamily, Font.Size - 1);
            Font smallerAndBoldFont = new Font(Font.FontFamily, Font.Size - 1, FontStyle.Bold);

            #region Transfers Panel

            // Permissions Tab
            chmodDownloadCustomRadioButton.Font = smallerFont;
            chmodDownloadDefaultRadioButton.Font = smallerFont;
            chmodDownloadTypeCombobox.Font = smallerFont;
            ownerDownloadLabel.Font = smallerAndBoldFont;
            othersDownloadLabel.Font = smallerAndBoldFont;
            groupDownloadLabel.Font = smallerAndBoldFont;
            downerrCheckbox.Font = smallerFont;
            downerwCheckbox.Font = smallerFont;
            downerxCheckbox.Font = smallerFont;
            dgrouprCheckbox.Font = smallerFont;
            dgroupwCheckbox.Font = smallerFont;
            dgroupxCheckbox.Font = smallerFont;
            dotherrCheckbox.Font = smallerFont;
            dotherwCheckbox.Font = smallerFont;
            dotherxCheckbox.Font = smallerFont;

            chmodUploadCustomRadioButton.Font = smallerFont;
            chmodUploadDefaultRadioButton.Font = smallerFont;
            chmodUploadTypeCombobox.Font = smallerFont;
            ownerUploadLabel.Font = smallerAndBoldFont;
            othersUploadLabel.Font = smallerAndBoldFont;
            groupUploadLabel.Font = smallerAndBoldFont;
            uownerrCheckbox.Font = smallerFont;
            uownerwCheckbox.Font = smallerFont;
            uownerxCheckbox.Font = smallerFont;
            ugrouprCheckbox.Font = smallerFont;
            ugroupwCheckbox.Font = smallerFont;
            ugroupxCheckbox.Font = smallerFont;
            uotherrCheckbox.Font = smallerFont;
            uotherwCheckbox.Font = smallerFont;
            uotherxCheckbox.Font = smallerFont;

            downloadSkipRegexDefaultButton.Font = smallerFont;
            uploadSkipRegexDefaultButton.Font = smallerFont;

            #endregion

            #endregion

            generalButton_Click(this, EventArgs.Empty);
            toolStrip.Renderer = new FirefoxStyleRenderer();

            //todo
            CenterToParent();
        }

        public override string[] BundleNames
        {
            get { return new[] {"Preferences"}; }
        }

        public Application DefaultEditor
        {
            get { return (Application) editorComboBox.SelectedValue; }
            set
            {
                editorComboBox.SelectedValue = value;
                _lastSelectedEditor = value;
            }
        }

        public bool SaveWorkspace
        {
            get { return saveWorkspaceCheckbox.Checked; }
            set { saveWorkspaceCheckbox.Checked = value; }
        }

        public bool NewBrowserOnStartup
        {
            get { return newBrowserOnStartupCheckbox.Checked; }
            set { newBrowserOnStartupCheckbox.Checked = value; }
        }

        public Host DefaultBookmark
        {
            get { return (Host) connectBookmarkCombobox.SelectedValue; }
            set
            {
                if (null != value)
                {
                    connectBookmarkCombobox.SelectedValue = value;
                }
                else
                {
                    // None entry
                    connectBookmarkCombobox.SelectedIndex = 0;
                }
            }
        }

        public bool UseKeychain
        {
            get { return keychainCheckbox.Checked; }
            set { keychainCheckbox.Checked = value; }
        }

        public bool ConfirmDisconnect
        {
            get { return confirmDisconnectCheckbox.Checked; }
            set { confirmDisconnectCheckbox.Checked = value; }
        }

        public bool AlwaysUseDefaultEditor
        {
            get { return alwaysUseDefaultEditorCheckBox.Checked; }
            set { alwaysUseDefaultEditorCheckBox.Checked = value; }
        }

        public bool ShowHiddenFiles
        {
            get { return showHiddenFilesCheckbox.Checked; }
            set { showHiddenFilesCheckbox.Checked = value; }
        }

        public bool DoubleClickEditor
        {
            get { return doubleClickEditorCheckbox.Checked; }
            set { doubleClickEditorCheckbox.Checked = value; }
        }

        public bool ReturnKeyRenames
        {
            get { return returnKeyCheckbox.Checked; }
            set { returnKeyCheckbox.Checked = value; }
        }

        public bool InfoWindowShowsCurrentSelection
        {
            get { return infoWindowCheckbox.Checked; }
            set { infoWindowCheckbox.Checked = value; }
        }

        public bool AlternatingRowBackground
        {
            get { return false; }
            set { ; }
        }

        public bool HorizontalLines
        {
            get { return false; }
            set { ; }
        }

        public bool VerticalLines
        {
            get { return false; }
            set { ; }
        }

        public string DefaultEncoding
        {
            get { return defaultEncodingCombobox.Text; }
            set { defaultEncodingCombobox.Text = value; }
        }

        public Host.TransferType TransferMode
        {
            get { return (Host.TransferType) transferFilesCombobox.SelectedValue; }
            set { transferFilesCombobox.SelectedValue = value; }
        }

        public bool TransfersToFront
        {
            get { return transfersToFrontCheckbox.Checked; }
            set { transfersToFrontCheckbox.Checked = value; }
        }

        public bool TransfersToBack
        {
            get { return transfersToBackCheckbox.Checked; }
            set { transfersToBackCheckbox.Checked = value; }
        }

        public bool RemoveFromTransfers
        {
            get { return removeFromTransfersCheckbox.Checked; }
            set { removeFromTransfersCheckbox.Checked = value; }
        }

        public bool OpenAfterDownload
        {
            get { return openAfterDownloadCheckbox.Checked; }
            set { openAfterDownloadCheckbox.Checked = value; }
        }

        public string DownloadFolder
        {
            get { return downloadFolderLabel.Text; }
            set { downloadFolderLabel.Text = value; }
        }

        public string DuplicateDownloadAction
        {
            get { return duplicateDownloadCombobox.Text; }
            set { duplicateDownloadCombobox.Text = value; }
        }

        public string DuplicateUploadAction
        {
            get { return duplicateUploadCombobox.Text; }
            set { duplicateUploadCombobox.Text = value; }
        }

        public bool DuplicateDownloadOverwrite
        {
            get { return duplicateDownloadOverwriteCheckbox.Checked; }
            set { duplicateDownloadOverwriteCheckbox.Checked = value; }
        }

        public bool DuplicateUploadOverwrite
        {
            get { return duplicateUploadOverwriteCheckbox.Checked; }
            set { duplicateUploadOverwriteCheckbox.Checked = value; }
        }

        public bool UploadWithTemporaryFilename
        {
            get { return uploadTemporaryNameCheckBox.Checked; }
            set { uploadTemporaryNameCheckBox.Checked = value; }
        }

        public bool ChmodDownload
        {
            get { return chmodDownloadCheckbox.Checked; }
            set { chmodDownloadCheckbox.Checked = value; }
        }

        public bool ChmodDownloadUseDefault
        {
            get { return chmodDownloadDefaultRadioButton.Checked; }
            set
            {
                chmodDownloadDefaultRadioButton.Checked = value;
                chmodDownloadCustomRadioButton.Checked = !value;
            }
        }

        public string ChmodDownloadType
        {
            get { return chmodDownloadTypeCombobox.Text; }
            set { chmodDownloadTypeCombobox.Text = value; }
        }

        public bool DownloadOwnerRead
        {
            get { return downerrCheckbox.Checked; }
            set { downerrCheckbox.Checked = value; }
        }

        public bool DownloadOwnerWrite
        {
            get { return downerwCheckbox.Checked; }
            set { downerwCheckbox.Checked = value; }
        }

        public bool DownloadOwnerExecute
        {
            get { return downerxCheckbox.Checked; }
            set { downerxCheckbox.Checked = value; }
        }

        public bool DownloadGroupRead
        {
            get { return dgrouprCheckbox.Checked; }
            set { dgrouprCheckbox.Checked = value; }
        }

        public bool DownloadGroupWrite
        {
            get { return dgroupwCheckbox.Checked; }
            set { dgroupwCheckbox.Checked = value; }
        }

        public bool DownloadGroupExecute
        {
            get { return dgroupxCheckbox.Checked; }
            set { dgroupxCheckbox.Checked = value; }
        }

        public bool DownloadOtherRead
        {
            get { return dotherrCheckbox.Checked; }
            set { dotherrCheckbox.Checked = value; }
        }

        public bool DownloadOtherWrite
        {
            get { return dotherwCheckbox.Checked; }
            set { dotherwCheckbox.Checked = value; }
        }

        public bool DownloadOtherExecute
        {
            get { return dotherxCheckbox.Checked; }
            set { dotherxCheckbox.Checked = value; }
        }

        public bool ChmodDownloadEnabled
        {
            set
            {
                chmodDownloadCustomRadioButton.Enabled = value;
                chmodDownloadDefaultRadioButton.Enabled = value;
                chmodDownloadTypeCombobox.Enabled = value;
                ChmodDownloadDefaultEnabled = value;
            }
        }

        public bool ChmodDownloadDefaultEnabled
        {
            set
            {
                downerrCheckbox.Enabled = value;
                downerwCheckbox.Enabled = value;
                downerxCheckbox.Enabled = value;
                dgrouprCheckbox.Enabled = value;
                dgroupwCheckbox.Enabled = value;
                dgroupxCheckbox.Enabled = value;
                dotherrCheckbox.Enabled = value;
                dotherwCheckbox.Enabled = value;
                dotherxCheckbox.Enabled = value;
            }
        }

        public bool ChmodUpload
        {
            get { return chmodUploadCheckbox.Checked; }
            set { chmodUploadCheckbox.Checked = value; }
        }

        public bool ChmodUploadUseDefault
        {
            get { return chmodUploadDefaultRadioButton.Checked; }
            set
            {
                chmodUploadDefaultRadioButton.Checked = value;
                chmodUploadCustomRadioButton.Checked = !value;
            }
        }

        public string ChmodUploadType
        {
            get { return chmodUploadTypeCombobox.Text; }
            set { chmodUploadTypeCombobox.Text = value; }
        }

        public bool UploadOwnerRead
        {
            get { return uownerrCheckbox.Checked; }
            set { uownerrCheckbox.Checked = value; }
        }

        public bool UploadOwnerWrite
        {
            get { return uownerwCheckbox.Checked; }
            set { uownerwCheckbox.Checked = value; }
        }

        public bool UploadOwnerExecute
        {
            get { return uownerxCheckbox.Checked; }
            set { uownerxCheckbox.Checked = value; }
        }

        public bool UploadGroupRead
        {
            get { return ugrouprCheckbox.Checked; }
            set { ugrouprCheckbox.Checked = value; }
        }

        public bool UploadGroupWrite
        {
            get { return ugroupwCheckbox.Checked; }
            set { ugroupwCheckbox.Checked = value; }
        }

        public bool UploadGroupExecute
        {
            get { return ugroupxCheckbox.Checked; }
            set { ugroupxCheckbox.Checked = value; }
        }

        public bool UploadOtherRead
        {
            get { return uotherrCheckbox.Checked; }
            set { uotherrCheckbox.Checked = value; }
        }

        public bool UploadOtherWrite
        {
            get { return uotherwCheckbox.Checked; }
            set { uotherwCheckbox.Checked = value; }
        }

        public bool UploadOtherExecute
        {
            get { return uotherxCheckbox.Checked; }
            set { uotherxCheckbox.Checked = value; }
        }

        public bool ChmodUploadEnabled
        {
            set
            {
                chmodUploadCustomRadioButton.Enabled = value;
                chmodUploadDefaultRadioButton.Enabled = value;
                chmodUploadTypeCombobox.Enabled = value;
                ChmodUploadDefaultEnabled = value;
            }
        }

        public bool ChmodUploadDefaultEnabled
        {
            set
            {
                uownerrCheckbox.Enabled = value;
                uownerwCheckbox.Enabled = value;
                uownerxCheckbox.Enabled = value;
                ugrouprCheckbox.Enabled = value;
                ugroupwCheckbox.Enabled = value;
                ugroupxCheckbox.Enabled = value;
                uotherrCheckbox.Enabled = value;
                uotherwCheckbox.Enabled = value;
                uotherxCheckbox.Enabled = value;
            }
        }

        public bool PreserveModificationDownload
        {
            get { return preserveModificationDownloadCheckbox.Checked; }
            set { preserveModificationDownloadCheckbox.Checked = value; }
        }

        public bool PreserveModificationUpload
        {
            get { return preserveModificationUploadCheckbox.Checked; }
            set { preserveModificationUploadCheckbox.Checked = value; }
        }

        public bool DownloadSkip
        {
            get { return downloadSkipCheckbox.Checked; }
            set { downloadSkipCheckbox.Checked = value; }
        }

        public string DownloadSkipRegex
        {
            get { return downloadSkipRegexRichTextbox.Text; }
            set { downloadSkipRegexRichTextbox.Text = value; }
        }

        public bool DownloadSkipRegexEnabled
        {
            set { downloadSkipRegexRichTextbox.Enabled = value; }
        }

        public void MarkDownloadSkipRegex(int position)
        {
            int currentPos = downloadSkipRegexRichTextbox.SelectionStart;
            if (position >= 0)
            {
                downloadSkipRegexRichTextbox.SelectionStart = position;
                downloadSkipRegexRichTextbox.SelectionLength = 1;
                downloadSkipRegexRichTextbox.SelectionColor = Color.Red;
            }
            else
            {
                downloadSkipRegexRichTextbox.SelectionStart = 0;
                downloadSkipRegexRichTextbox.SelectionLength = downloadSkipRegexRichTextbox.TextLength;
                downloadSkipRegexRichTextbox.SelectionColor = Color.Black;
            }
            downloadSkipRegexRichTextbox.SelectionStart = currentPos;
            downloadSkipRegexRichTextbox.SelectionLength = 0;
            downloadSkipRegexRichTextbox.SelectionColor = Color.Black;
        }

        public bool UploadSkip
        {
            get { return uploadSkipCheckbox.Checked; }
            set { uploadSkipCheckbox.Checked = value; }
        }

        public string UploadSkipRegex
        {
            get { return uploadSkipRegexRichTextbox.Text; }
            set { uploadSkipRegexRichTextbox.Text = value; }
        }

        public bool UploadSkipRegexEnabled
        {
            set { uploadSkipRegexRichTextbox.Enabled = value; }
        }

        public void MarkUploadSkipRegex(int position)
        {
            int currentPos = uploadSkipRegexRichTextbox.SelectionStart;
            if (position >= 0)
            {
                uploadSkipRegexRichTextbox.SelectionStart = position;
                uploadSkipRegexRichTextbox.SelectionLength = 1;
                uploadSkipRegexRichTextbox.SelectionColor = Color.Red;
            }
            else
            {
                uploadSkipRegexRichTextbox.SelectionStart = 0;
                uploadSkipRegexRichTextbox.SelectionLength = uploadSkipRegexRichTextbox.TextLength;
                uploadSkipRegexRichTextbox.SelectionColor = Color.Black;
            }
            uploadSkipRegexRichTextbox.SelectionStart = currentPos;
            uploadSkipRegexRichTextbox.SelectionLength = 0;
            uploadSkipRegexRichTextbox.SelectionColor = Color.Black;
        }

        public string DefaultBucketLocation
        {
            get { return (string) defaultBucketLocationCombobox.SelectedValue; }
            set { defaultBucketLocationCombobox.SelectedValue = value; }
        }

        public string DefaultEncryption
        {
            get { return (string) defaultEncryptionComboBox.SelectedValue; }
            set { defaultEncryptionComboBox.SelectedValue = value; }
        }

        public float DefaultDownloadThrottle
        {
            get { return (float) defaultDownloadThrottleCombobox.SelectedValue; }
            set { defaultDownloadThrottleCombobox.SelectedValue = value; }
        }

        public float DefaultUploadThrottle
        {
            get { return (float) defaultUploadThrottleCombobox.SelectedValue; }
            set { defaultUploadThrottleCombobox.SelectedValue = value; }
        }

        public int ConnectionTimeout
        {
            get { return Convert.ToInt32(connectionTimeoutUpDown.Value); }
            set { connectionTimeoutUpDown.Value = value; }
        }

        public int RetryDelay
        {
            get { return Convert.ToInt32(retryDelayUpDown.Value); }
            set { retryDelayUpDown.Value = value; }
        }

        public int Retries
        {
            get { return Convert.ToInt32(retriesUpDown.Value); }
            set
            {
                retriesUpDown.Value = value;
                retryCheckbox.Checked = value != 0;
            }
        }

        public string LastUpdateCheck
        {
            set { lastUpdateLabel.Text = value; }
        }

        public string UpdateFeed
        {
            get { return (string) updateFeedComboBox.SelectedValue; }
            set { updateFeedComboBox.SelectedValue = value; }
        }

        public bool UpdateEnabled
        {
            set
            {
                updateCheckBox.Enabled = value;
                updateCheckButton.Enabled = value;
                updateFeedComboBox.Enabled = value;
            }
        }

        public bool UseSystemProxy
        {
            get { return systemProxyCheckBox.Checked; }
            set { systemProxyCheckBox.Checked = value; }
        }

        public event VoidHandler UseSystemProxyChangedEvent = delegate { };
        public event VoidHandler ChangeSystemProxyEvent = delegate { };
        public event VoidHandler SaveWorkspaceChangedEvent = delegate { };
        public event VoidHandler NewBrowserOnStartupChangedEvent = delegate { };
        public event VoidHandler DefaultBookmarkChangedEvent = delegate { };
        public event VoidHandler UseKeychainChangedEvent = delegate { };
        public event VoidHandler ConfirmDisconnectChangedEvent = delegate { };
        public event VoidHandler DefaultProtocolChangedEvent = delegate { };
        public event VoidHandler ShowHiddenFilesChangedEvent = delegate { };
        public event VoidHandler DoubleClickEditorChangedEvent = delegate { };
        public event VoidHandler ReturnKeyRenamesChangedEvent = delegate { };
        public event VoidHandler InfoWindowShowsCurrentSelectionChangedEvent = delegate { };
        public event VoidHandler AlternatingRowBackgroundChangedEvent = delegate { };
        public event VoidHandler HorizontalLinesChangedEvent = delegate { };
        public event VoidHandler VerticalLinesChangedEvent = delegate { };
        public event VoidHandler DefaultEncodingChangedEvent = delegate { };
        public event VoidHandler TransferModeChangedEvent = delegate { };
        public event VoidHandler TransfersToFrontChangedEvent = delegate { };
        public event VoidHandler TransfersToBackChangedEvent = delegate { };
        public event VoidHandler RemoveFromTransfersChangedEvent = delegate { };
        public event VoidHandler OpenAfterDownloadChangedEvent = delegate { };
        public event VoidHandler DownloadFolderChangedEvent = delegate { };
        public event VoidHandler DuplicateDownloadActionChangedEvent = delegate { };
        public event VoidHandler DuplicateUploadActionChangedEvent = delegate { };
        public event VoidHandler DuplicateDownloadOverwriteChangedEvent = delegate { };
        public event VoidHandler DuplicateUploadOverwriteChangedEvent = delegate { };
        public event VoidHandler DefaultEditorChangedEvent = delegate { };
        public event VoidHandler RepopulateEditorsEvent = delegate { };
        public event VoidHandler AlwaysUseDefaultEditorChangedEvent = delegate { };
        public event VoidHandler ChmodDownloadChangedEvent = delegate { };
        public event VoidHandler ChmodDownloadUseDefaultChangedEvent = delegate { };
        public event VoidHandler ChmodDownloadTypeChangedEvent = delegate { };
        public event VoidHandler DownloadOwnerReadChangedEvent = delegate { };
        public event VoidHandler DownloadOwnerWriteChangedEvent = delegate { };
        public event VoidHandler DownloadOwnerExecuteChangedEvent = delegate { };
        public event VoidHandler DownloadGroupReadChangedEvent = delegate { };
        public event VoidHandler DownloadGroupWriteChangedEvent = delegate { };
        public event VoidHandler DownloadGroupExecuteChangedEvent = delegate { };
        public event VoidHandler DownloadOtherReadChangedEvent = delegate { };
        public event VoidHandler DownloadOtherWriteChangedEvent = delegate { };
        public event VoidHandler DownloadOtherExecuteChangedEvent = delegate { };
        public event VoidHandler ChmodUploadChangedEvent = delegate { };
        public event VoidHandler ChmodUploadUseDefaultChangedEvent = delegate { };
        public event VoidHandler ChmodUploadTypeChangedEvent = delegate { };
        public event VoidHandler UploadOwnerReadChangedEvent = delegate { };
        public event VoidHandler UploadOwnerWriteChangedEvent = delegate { };
        public event VoidHandler UploadOwnerExecuteChangedEvent = delegate { };
        public event VoidHandler UploadGroupReadChangedEvent = delegate { };
        public event VoidHandler UploadGroupWriteChangedEvent = delegate { };
        public event VoidHandler UploadGroupExecuteChangedEvent = delegate { };
        public event VoidHandler UploadOtherReadChangedEvent = delegate { };
        public event VoidHandler UploadOtherWriteChangedEvent = delegate { };
        public event VoidHandler UploadOtherExecuteChangedEvent = delegate { };
        public event VoidHandler PreserveModificationDownloadChangedEvent = delegate { };
        public event VoidHandler PreserveModificationUploadChangedEvent = delegate { };
        public event VoidHandler DownloadSkipChangedEvent = delegate { };
        public event VoidHandler DownloadSkipRegexChangedEvent = delegate { };
        public event VoidHandler DownloadSkipRegexDefaultEvent = delegate { };
        public event VoidHandler UploadSkipChangedEvent = delegate { };
        public event VoidHandler UploadSkipRegexChangedEvent = delegate { };
        public event VoidHandler UploadSkipRegexDefaultEvent = delegate { };
        public event VoidHandler DefaultBucketLocationChangedEvent = delegate { };
        public event VoidHandler DefaultEncryptionChangedEvent = delegate { };
        public event VoidHandler DefaultDownloadThrottleChangedEvent = delegate { };
        public event VoidHandler DefaultUploadThrottleChangedEvent = delegate { };
        public event VoidHandler ConnectionTimeoutChangedEvent = delegate { };
        public event VoidHandler RetryDelayChangedEvent = delegate { };
        public event VoidHandler RetriesChangedEvent = delegate { };
        public event VoidHandler DefaultStorageClassChangedEvent = delegate { };
        public event VoidHandler LocaleChanged = delegate { };
        public event VoidHandler UploadWithTemporaryFilenameChangedEvent = delegate { };
        public event VoidHandler UpdateFeedChangedEvent = delegate { };
        public event VoidHandler BookmarkSizeChangedEvent = delegate { };

        public bool AutomaticUpdateCheck
        {
            get { return updateCheckBox.Checked; }
            set { updateCheckBox.Checked = value; }
        }

        public event VoidHandler AutomaticUpdateChangedEvent = delegate { };
        public event VoidHandler CheckForUpdateEvent = delegate { };

        public string DocumentExportFormat
        {
            get { return (string) gdDocumentsComboBox.SelectedValue; }
            set { gdDocumentsComboBox.SelectedValue = value; }
        }

        public string PresentationExportFormat
        {
            get { return (string) gdPresentationsComboBox.SelectedValue; }
            set { gdPresentationsComboBox.SelectedValue = value; }
        }

        public int BookmarkSize
        {
            get { return (int) bookmarkSizeComboBox.SelectedValue; }
            set { bookmarkSizeComboBox.SelectedValue = value; }
        }

        public string SpreadsheetExportFormat
        {
            get { return (string) gdSpreadsheetsComboBox.SelectedValue; }
            set { gdSpreadsheetsComboBox.SelectedValue = value; }
        }

        public bool ConvertUploads
        {
            get { return gdConvertCheckBox.Checked; }
            set { gdConvertCheckBox.Checked = value; }
        }

        public bool OcrUploads
        {
            get { return gdOCRcheckBox.Checked; }
            set { gdOCRcheckBox.Checked = value; }
        }

        public event VoidHandler DocumentExportFormatChanged = delegate { };
        public event VoidHandler PresentationExportFormatChanged = delegate { };
        public event VoidHandler SpreadsheetExportFormatChanged = delegate { };
        public event VoidHandler ConvertUploadsChanged = delegate { };
        public event VoidHandler OcrUploadsChanged = delegate { };

        public string DefaultStorageClass
        {
            get { return (string) defaultStorageClassComboBox.SelectedValue; }
            set { defaultStorageClassComboBox.SelectedValue = value; }
        }

        public void PopulateBookmarks(List<KeyValueIconTriple<Host, string>> bookmarks)
        {
            connectBookmarkCombobox.DataSource = bookmarks;
            connectBookmarkCombobox.ValueMember = "Key";
            connectBookmarkCombobox.DisplayMember = "Value";
            connectBookmarkCombobox.IconMember = "IconKey";
        }

        public void PopulateEditors(List<KeyValueIconTriple<Application, string>> editors)
        {
            editorComboBox.DataSource = editors;
            editorComboBox.ValueMember = "Key";
            editorComboBox.DisplayMember = "Value";
            editorComboBox.IconMember = "IconKey";

            ImageList imageList = new ImageList();
            foreach (KeyValueIconTriple<Application, string> triple in editors)
            {
                if (triple.Key.getIdentifier() != null)
                {
                    imageList.Images.Add(triple.Value,
                        IconCache.Instance.GetFileIconFromExecutable(triple.Key.getIdentifier(),
                            IconCache.IconSize.Small));
                }
            }
            editorComboBox.ICImageList = imageList;
        }

        public void PopulateProtocols(List<KeyValueIconTriple<Protocol, string>> protocols)
        {
            defaultProtocolCombobox.DataSource = protocols;
            defaultProtocolCombobox.ValueMember = "Key";
            defaultProtocolCombobox.DisplayMember = "Value";
            defaultProtocolCombobox.IconMember = "IconKey";
        }

        public void PopulateEncodings(List<string> encodings)
        {
            defaultEncodingCombobox.DataSource = encodings;
        }

        public void PopulateDocumentExportFormats(IList<KeyValuePair<string, string>> formats)
        {
            gdDocumentsComboBox.DataSource = formats;
            gdDocumentsComboBox.ValueMember = "Key";
            gdDocumentsComboBox.DisplayMember = "Value";
        }

        public void PopulateBookmarkSize(IList<KeyValuePair<int, string>> sizes)
        {
            bookmarkSizeComboBox.DataSource = sizes;
            bookmarkSizeComboBox.ValueMember = "Key";
            bookmarkSizeComboBox.DisplayMember = "Value";
        }

        public void PopulatePresentationExportFormats(IList<KeyValuePair<string, string>> formats)
        {
            gdPresentationsComboBox.DataSource = formats;
            gdPresentationsComboBox.ValueMember = "Key";
            gdPresentationsComboBox.DisplayMember = "Value";
        }

        public void PopulateSpreadsheetExportFormats(IList<KeyValuePair<string, string>> formats)
        {
            gdSpreadsheetsComboBox.DataSource = formats;
            gdSpreadsheetsComboBox.ValueMember = "Key";
            gdSpreadsheetsComboBox.DisplayMember = "Value";
        }

        public void PopulateTransferModes(List<KeyValuePair<string, Host.TransferType>> modes)
        {
            transferFilesCombobox.DataSource = null;
            transferFilesCombobox.DataSource = modes;
            transferFilesCombobox.DisplayMember = "Key";
            transferFilesCombobox.ValueMember = "Value";
        }

        public void PopulateDuplicateDownloadActions(List<string> actions)
        {
            duplicateDownloadCombobox.DataSource = actions;
        }

        public void PopulateDuplicateUploadActions(List<string> actions)
        {
            duplicateUploadCombobox.DataSource = actions;
        }

        public void PopulateChmodDownloadTypes(List<string> types)
        {
            chmodDownloadTypeCombobox.DataSource = types;
        }

        public void PopulateChmodUploadTypes(List<string> types)
        {
            chmodUploadTypeCombobox.DataSource = types;
        }

        public void PopulateDefaultBucketLocations(IList<KeyValuePair<string, string>> locations)
        {
            defaultBucketLocationCombobox.DataSource = locations;
            defaultBucketLocationCombobox.ValueMember = "Key";
            defaultBucketLocationCombobox.DisplayMember = "Value";
        }

        public void PopulateDefaultStorageClasses(IList<KeyValuePair<string, string>> classes)
        {
            defaultStorageClassComboBox.DataSource = classes;
            defaultStorageClassComboBox.ValueMember = "Key";
            defaultStorageClassComboBox.DisplayMember = "Value";
        }

        public void PopulateDefaultEncryption(IList<KeyValuePair<string, string>> algorithms)
        {
            defaultEncryptionComboBox.DataSource = algorithms;
            defaultEncryptionComboBox.ValueMember = "Key";
            defaultEncryptionComboBox.DisplayMember = "Value";
        }

        public void PopulateDefaultDownloadThrottleList(IList<KeyValuePair<float, string>> throttles)
        {
            defaultDownloadThrottleCombobox.DataSource = throttles;
            defaultDownloadThrottleCombobox.ValueMember = "Key";
            defaultDownloadThrottleCombobox.DisplayMember = "Value";
        }

        public void PopulateDefaultUploadThrottleList(IList<KeyValuePair<float, string>> throttles)
        {
            defaultUploadThrottleCombobox.DataSource = throttles;
            defaultUploadThrottleCombobox.ValueMember = "Key";
            defaultUploadThrottleCombobox.DisplayMember = "Value";
        }

        public void PopulateUpdateFeeds(IList<KeyValuePair<string, string>> feeds)
        {
            updateFeedComboBox.DataSource = feeds;
            updateFeedComboBox.ValueMember = "Key";
            updateFeedComboBox.DisplayMember = "Value";
        }

        public Protocol DefaultProtocol
        {
            get { return (Protocol) defaultProtocolCombobox.SelectedValue; }
            set { defaultProtocolCombobox.SelectedValue = value; }
        }

        public string CurrentLocale
        {
            get { return (string) languageComboBox.SelectedValue; }
            set { languageComboBox.SelectedValue = value; }
        }

        public void PopulateLocales(IList<KeyValuePair<string, string>> locales)
        {
            languageComboBox.DataSource = locales;
            languageComboBox.ValueMember = "Key";
            languageComboBox.DisplayMember = "Value";
        }

        public event VoidHandler AnonymousPasswordChangedEvent = delegate { };
        public event VoidHandler DefaultTransferModeChangedEvent = delegate { };
        public event VoidHandler LineEndingChangedEvent = delegate { };
        public event VoidHandler TextFileTypeRegexChangedEvent = delegate { };
        public event VoidHandler SecureDataChannelChangedEvent = delegate { };
        public event VoidHandler FailInsecureDataChannelChangedEvent = delegate { };

        private void generalButton_Click(object sender, EventArgs e)
        {
            if (!generalButton.Checked)
            {
                DisableAll();
                generalButton.Checked = true;
                panelManager.SelectedPanel = managedGeneralPanel;
            }
        }

        private void DisableAll()
        {
            foreach (var item in toolStrip.Items)
            {
                if (item is ToolStripButton)
                {
                    (item as ToolStripButton).Checked = false;
                }
            }
        }

        private void saveWorkspaceCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            SaveWorkspaceChangedEvent();
        }

        private void newBrowserOnStartupCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            NewBrowserOnStartupChangedEvent();
        }

        private void connectBookmarkCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultBookmarkChangedEvent();
        }

        private void keychainCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UseKeychainChangedEvent();
        }

        private void confirmDisconnectCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ConfirmDisconnectChangedEvent();
        }

        private void defaultProtocolCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultProtocolChangedEvent();
        }

        private void defaultEncodingCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultEncodingChangedEvent();
        }

        private void showHiddenFilesCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ShowHiddenFilesChangedEvent();
        }

        private void doubleClickEditorCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DoubleClickEditorChangedEvent();
        }

        private void returnKeyCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ReturnKeyRenamesChangedEvent();
        }

        private void infoWindowCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            InfoWindowShowsCurrentSelectionChangedEvent();
        }

        private void alternatingRowsCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            AlternatingRowBackgroundChangedEvent();
        }

        private void horizontalLinesCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            HorizontalLinesChangedEvent();
        }

        private void verticalLinesCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            VerticalLinesChangedEvent();
        }

        private void transfersButton_Click(object sender, EventArgs e)
        {
            if (!transfersButton.Checked)
            {
                DisableAll();
                transfersButton.Checked = true;
                panelManager.SelectedPanel = managedTransfersPanel;
            }
        }

        private void transferFilesCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            TransferModeChangedEvent();
        }

        private void transfersToFrontCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            TransfersToFrontChangedEvent();
        }

        private void transfersToBackCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            TransfersToBackChangedEvent();
        }

        private void removeFromTransfersCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            RemoveFromTransfersChangedEvent();
        }

        private void openAfterDownloadCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            OpenAfterDownloadChangedEvent();
        }

        private void showDownloadFolderDialogButton_Click(object sender, EventArgs e)
        {
            downloadFolderBrowserDialog.SelectedPath = DownloadFolder;
            if (downloadFolderBrowserDialog.ShowDialog() == DialogResult.OK)
            {
                DownloadFolder = downloadFolderBrowserDialog.SelectedPath;
                DownloadFolderChangedEvent();
            }
        }

        private void duplicateDownloadCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DuplicateDownloadActionChangedEvent();
        }

        private void duplicateUploadCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DuplicateUploadActionChangedEvent();
        }

        private void duplicateDownloadOverwriteCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DuplicateDownloadOverwriteChangedEvent();
        }

        private void duplicateUploadOverwriteCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DuplicateUploadOverwriteChangedEvent();
        }

        private void chmodDownloadCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ChmodDownloadChangedEvent();
        }

        private void chmodUploadCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            ChmodUploadChangedEvent();
        }

        private void chmodDownloadDefaultRadioButton_CheckedChanged(object sender, EventArgs e)
        {
            ChmodDownloadUseDefaultChangedEvent();
        }

        private void chmodUploadDefaultRadioButton_CheckedChanged(object sender, EventArgs e)
        {
            ChmodUploadUseDefaultChangedEvent();
        }

        private void chmodDownloadTypeCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChmodDownloadTypeChangedEvent();
        }

        private void chmodUploadTypeCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            ChmodUploadTypeChangedEvent();
        }

        private void downerrCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOwnerReadChangedEvent();
        }

        private void uownerrCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOwnerReadChangedEvent();
        }

        private void downerwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOwnerWriteChangedEvent();
        }

        private void uownerwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOwnerWriteChangedEvent();
        }

        private void downerxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOwnerExecuteChangedEvent();
        }

        private void uownerxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOwnerExecuteChangedEvent();
        }

        private void dgrouprCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadGroupReadChangedEvent();
        }

        private void ugrouprCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadGroupReadChangedEvent();
        }

        private void dgroupwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadGroupWriteChangedEvent();
        }

        private void ugroupwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadGroupWriteChangedEvent();
        }

        private void dgroupxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadGroupExecuteChangedEvent();
        }

        private void ugroupxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadGroupExecuteChangedEvent();
        }

        private void dotherrCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOtherReadChangedEvent();
        }

        private void uotherrCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOtherReadChangedEvent();
        }

        private void dotherwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOtherWriteChangedEvent();
        }

        private void uotherwCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOtherWriteChangedEvent();
        }

        private void dotherxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadOtherReadChangedEvent();
        }

        private void uotherxCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadOtherReadChangedEvent();
        }

        private void preserveModificationDownloadCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            PreserveModificationDownloadChangedEvent();
        }

        private void preserveModificationUploadCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            PreserveModificationUploadChangedEvent();
        }

        private void downloadSkipRegexRichTextbox_TextChanged(object sender, EventArgs e)
        {
            DownloadSkipRegexChangedEvent();
        }

        private void downloadSkipCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            DownloadSkipChangedEvent();
        }

        private void downloadSkipRegexDefaultButton_Click(object sender, EventArgs e)
        {
            DownloadSkipRegexDefaultEvent();
        }

        private void uploadSkipCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            UploadSkipChangedEvent();
        }

        private void uploadSkipRegexRichTextbox_TextChanged(object sender, EventArgs e)
        {
            UploadSkipRegexChangedEvent();
        }

        private void uploadSkipRegexDefaultButton_Click(object sender, EventArgs e)
        {
            UploadSkipRegexDefaultEvent();
        }

        private void sftpButton_Click(object sender, EventArgs e)
        {
            if (!sftpButton.Checked)
            {
                DisableAll();
                sftpButton.Checked = true;
                panelManager.SelectedPanel = managedSftpPanel;
            }
        }

        private void s3Button_Click(object sender, EventArgs e)
        {
            if (!s3Button.Checked)
            {
                DisableAll();
                s3Button.Checked = true;
                panelManager.SelectedPanel = managedS3Panel;
            }
        }

        private void defaultBucketLocationCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultBucketLocationChangedEvent();
        }

        private void bandwidthButton_Click(object sender, EventArgs e)
        {
            if (!bandwidthButton.Checked)
            {
                DisableAll();
                bandwidthButton.Checked = true;
                panelManager.SelectedPanel = managedBandwidthPanel;
            }
        }

        private void defaultDownloadThrottleCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultDownloadThrottleChangedEvent();
        }

        private void defaultUploadThrottleCombobox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultUploadThrottleChangedEvent();
        }

        private void connectionButton_Click(object sender, EventArgs e)
        {
            if (!connectionButton.Checked)
            {
                DisableAll();
                connectionButton.Checked = true;
                panelManager.SelectedPanel = managedConnectionPanel;
            }
        }

        private void retryCheckbox_CheckedChanged(object sender, EventArgs e)
        {
            if (retryCheckbox.Checked && retriesUpDown.Value == 0)
            {
                Retries = 1;
            }
            if (!retryCheckbox.Checked && retriesUpDown.Value != 0)
            {
                Retries = 0;
            }
        }

        private void connectionTimeoutUpDown_ValueChanged(object sender, EventArgs e)
        {
            ConnectionTimeoutChangedEvent();
        }

        private void retryDelayUpDown_ValueChanged(object sender, EventArgs e)
        {
            RetryDelayChangedEvent();
        }

        private void retriesUpDown_ValueChanged(object sender, EventArgs e)
        {
            if (Retries == 0 && retryCheckbox.Checked)
            {
                retryCheckbox.Checked = false;
            }
            if (Retries > 0 && !retryCheckbox.Checked)
            {
                retryCheckbox.Checked = true;
            }
            RetriesChangedEvent();
        }

        private void defaultStorageClassComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultStorageClassChangedEvent();
        }

        private void googleDocsButton_Click(object sender, EventArgs e)
        {
            if (!googleDocsButton.Checked)
            {
                DisableAll();
                googleDocsButton.Checked = true;
                panelManager.SelectedPanel = managedGoogleDocsPanel;
            }
        }

        private void updateButton_Click(object sender, EventArgs e)
        {
            if (!updateButton.Checked)
            {
                DisableAll();
                updateButton.Checked = true;
                panelManager.SelectedPanel = managedUpdatePanel;
            }
        }

        private void languageButton_Click(object sender, EventArgs e)
        {
            if (!languageButton.Checked)
            {
                DisableAll();
                languageButton.Checked = true;
                panelManager.SelectedPanel = managedLanguagePanel;
            }
        }

        private void gdDocumentsComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DocumentExportFormatChanged();
        }

        private void gdPresentationsComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            PresentationExportFormatChanged();
        }

        private void gdSpreadsheetsComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            SpreadsheetExportFormatChanged();
        }

        private void gdConvertCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            ConvertUploadsChanged();
        }

        private void gdOCRcheckBox_CheckedChanged(object sender, EventArgs e)
        {
            OcrUploadsChanged();
        }

        private void languageComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            LocaleChanged();
        }

        private void updateCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            AutomaticUpdateChangedEvent();
        }

        private void updateCheckButton_Click(object sender, EventArgs e)
        {
            CheckForUpdateEvent();
        }

        private void systemProxyCheckBox_CheckStateChanged(object sender, EventArgs e)
        {
            UseSystemProxyChangedEvent();
        }

        private void uploadTemporaryNameCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            UploadWithTemporaryFilenameChangedEvent();
        }

        private void updateFeedComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            UpdateFeedChangedEvent();
        }

        private void changeSystemProxyButton_Click(object sender, EventArgs e)
        {
            ChangeSystemProxyEvent();
        }

        private void editStripButton_Click(object sender, EventArgs e)
        {
            if (!editStripButton.Checked)
            {
                DisableAll();
                editStripButton.Checked = true;
                panelManager.SelectedPanel = managedEditorPanel;
            }
        }

        private void editorComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            Application selected = DefaultEditor;
            if (selected != null && selected.getIdentifier() == null)
            {
                //choose dialog
                editorOpenFileDialog.FileName = null;
                DialogResult result = editorOpenFileDialog.ShowDialog();
                if (result == DialogResult.OK)
                {
                    PreferencesFactory.get()
                        .setProperty("editor.bundleIdentifier", editorOpenFileDialog.FileName.ToLower());
                    RepopulateEditorsEvent();
                }
                else
                {
                    if (_lastSelectedEditor != null)
                    {
                        DefaultEditor = _lastSelectedEditor;
                    }
                    else
                    {
                        //dummy editor which leads to an empty selection
                        DefaultEditor = Application.notfound;
                    }
                }
            }
            else
            {
                _lastSelectedEditor = DefaultEditor;
                DefaultEditorChangedEvent();
            }
        }

        private void alwaysUseDefaultEditorCheckBox_CheckedChanged(object sender, EventArgs e)
        {
            AlwaysUseDefaultEditorChangedEvent();
        }

        private void defaultEncryptionComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            DefaultEncryptionChangedEvent();
        }

        private void browserButton_Click(object sender, EventArgs e)
        {
            if (!browserButton.Checked)
            {
                DisableAll();
                browserButton.Checked = true;
                panelManager.SelectedPanel = managedBrowserPanel;
            }
        }

        private void bookmarkSizeComboBox_SelectionChangeCommitted(object sender, EventArgs e)
        {
            BookmarkSizeChangedEvent();
        }
    }
}