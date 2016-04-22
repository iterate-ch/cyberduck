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

using System.Collections.Generic;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.Ui.Controller
{
    internal interface IPreferencesView : IView
    {
        bool SaveWorkspace { set; get; }
        bool NewBrowserOnStartup { set; get; }
        Host DefaultBookmark { set; get; }
        bool UseKeychain { set; get; }
        bool ConfirmDisconnect { set; get; }
        Protocol DefaultProtocol { set; get; }
        Application DefaultEditor { set; get; }
        bool AlwaysUseDefaultEditor { set; get; }
        bool ShowHiddenFiles { set; get; }
        bool DoubleClickEditor { set; get; }
        bool ReturnKeyRenames { set; get; }
        bool InfoWindowShowsCurrentSelection { set; get; }
        int BookmarkSize { set; get; }
        bool AlternatingRowBackground { set; get; }
        bool HorizontalLines { set; get; }
        bool VerticalLines { set; get; }
        string DefaultEncoding { set; get; }
        Host.TransferType TransferMode { set; get; }
        bool TransfersToFront { set; get; }
        bool TransfersToBack { set; get; }
        bool RemoveFromTransfers { set; get; }
        bool OpenAfterDownload { set; get; }
        string DownloadFolder { set; get; }
        string DuplicateDownloadAction { set; get; }
        string DuplicateUploadAction { set; get; }
        bool DuplicateDownloadOverwrite { set; get; }
        bool DuplicateUploadOverwrite { set; get; }
        bool UploadWithTemporaryFilename { set; get; }
        bool ChmodDownload { set; get; }
        bool ChmodDownloadUseDefault { set; get; }
        string ChmodDownloadType { set; get; }
        bool DownloadOwnerRead { set; get; }
        bool DownloadOwnerWrite { set; get; }
        bool DownloadOwnerExecute { set; get; }
        bool DownloadGroupRead { set; get; }
        bool DownloadGroupWrite { set; get; }
        bool DownloadGroupExecute { set; get; }
        bool DownloadOtherRead { set; get; }
        bool DownloadOtherWrite { set; get; }
        bool DownloadOtherExecute { set; get; }
        bool ChmodDownloadEnabled { set; }
        bool ChmodDownloadDefaultEnabled { set; }
        bool ChmodUpload { set; get; }
        bool ChmodUploadUseDefault { set; get; }
        string ChmodUploadType { set; get; }
        bool UploadOwnerRead { set; get; }
        bool UploadOwnerWrite { set; get; }
        bool UploadOwnerExecute { set; get; }
        bool UploadGroupRead { set; get; }
        bool UploadGroupWrite { set; get; }
        bool UploadGroupExecute { set; get; }
        bool UploadOtherRead { set; get; }
        bool UploadOtherWrite { set; get; }
        bool UploadOtherExecute { set; get; }
        bool ChmodUploadEnabled { set; }
        bool ChmodUploadDefaultEnabled { set; }
        bool PreserveModificationDownload { set; get; }
        bool PreserveModificationUpload { set; get; }
        bool DownloadSkip { set; get; }
        string DownloadSkipRegex { set; get; }
        bool DownloadSkipRegexEnabled { set; }
        bool UploadSkip { set; get; }
        string UploadSkipRegex { set; get; }
        bool UploadSkipRegexEnabled { set; }
        string DefaultBucketLocation { set; get; }
        string DefaultStorageClass { set; get; }
        string DefaultEncryption { set; get; }
        float DefaultDownloadThrottle { set; get; }
        float DefaultUploadThrottle { set; get; }
        int ConnectionTimeout { set; get; }
        int RetryDelay { set; get; }
        int Retries { set; get; }
        string DocumentExportFormat { set; get; }
        string PresentationExportFormat { set; get; }
        string SpreadsheetExportFormat { set; get; }
        bool ConvertUploads { set; get; }
        bool OcrUploads { set; get; }
        string CurrentLocale { set; get; }
        bool AutomaticUpdateCheck { set; get; }
        string LastUpdateCheck { set; }
        string UpdateFeed { set; get; }
        bool UpdateEnabled { set; }
        bool UseSystemProxy { set; get; }
        void PopulateBookmarkSize(IList<KeyValuePair<int, string>> sizes);
        void MarkDownloadSkipRegex(int position);
        void MarkUploadSkipRegex(int position);
        void PopulateDocumentExportFormats(IList<KeyValuePair<string, string>> formats);
        void PopulatePresentationExportFormats(IList<KeyValuePair<string, string>> formats);
        void PopulateSpreadsheetExportFormats(IList<KeyValuePair<string, string>> formats);
        event VoidHandler DocumentExportFormatChanged;
        event VoidHandler PresentationExportFormatChanged;
        event VoidHandler SpreadsheetExportFormatChanged;
        event VoidHandler ConvertUploadsChanged;
        event VoidHandler OcrUploadsChanged;
        void PopulateLocales(IList<KeyValuePair<string, string>> locales);
        event VoidHandler LocaleChanged;
        event VoidHandler AutomaticUpdateChangedEvent;
        event VoidHandler CheckForUpdateEvent;
        event VoidHandler UpdateFeedChangedEvent;
        event VoidHandler BookmarkSizeChangedEvent;
        event VoidHandler UseSystemProxyChangedEvent;
        event VoidHandler ChangeSystemProxyEvent;
        event VoidHandler SaveWorkspaceChangedEvent;
        event VoidHandler NewBrowserOnStartupChangedEvent;
        event VoidHandler DefaultBookmarkChangedEvent;
        event VoidHandler UseKeychainChangedEvent;
        event VoidHandler ConfirmDisconnectChangedEvent;
        event VoidHandler DefaultProtocolChangedEvent;
        event VoidHandler ShowHiddenFilesChangedEvent;
        event VoidHandler DoubleClickEditorChangedEvent;
        event VoidHandler ReturnKeyRenamesChangedEvent;
        event VoidHandler InfoWindowShowsCurrentSelectionChangedEvent;
        event VoidHandler AlternatingRowBackgroundChangedEvent;
        event VoidHandler HorizontalLinesChangedEvent;
        event VoidHandler VerticalLinesChangedEvent;
        event VoidHandler DefaultEncodingChangedEvent;
        event VoidHandler TransferModeChangedEvent;
        event VoidHandler TransfersToFrontChangedEvent;
        event VoidHandler TransfersToBackChangedEvent;
        event VoidHandler RemoveFromTransfersChangedEvent;
        event VoidHandler OpenAfterDownloadChangedEvent;
        event VoidHandler DownloadFolderChangedEvent;
        event VoidHandler DuplicateDownloadActionChangedEvent;
        event VoidHandler DuplicateUploadActionChangedEvent;
        event VoidHandler DuplicateDownloadOverwriteChangedEvent;
        event VoidHandler DuplicateUploadOverwriteChangedEvent;
        event VoidHandler UploadWithTemporaryFilenameChangedEvent;
        event VoidHandler DefaultEditorChangedEvent;
        event VoidHandler RepopulateEditorsEvent;
        event VoidHandler AlwaysUseDefaultEditorChangedEvent;
        event VoidHandler ChmodDownloadChangedEvent;
        event VoidHandler ChmodDownloadUseDefaultChangedEvent;
        event VoidHandler ChmodDownloadTypeChangedEvent;
        event VoidHandler DownloadOwnerReadChangedEvent;
        event VoidHandler DownloadOwnerWriteChangedEvent;
        event VoidHandler DownloadOwnerExecuteChangedEvent;
        event VoidHandler DownloadGroupReadChangedEvent;
        event VoidHandler DownloadGroupWriteChangedEvent;
        event VoidHandler DownloadGroupExecuteChangedEvent;
        event VoidHandler DownloadOtherReadChangedEvent;
        event VoidHandler DownloadOtherWriteChangedEvent;
        event VoidHandler DownloadOtherExecuteChangedEvent;
        event VoidHandler ChmodUploadChangedEvent;
        event VoidHandler ChmodUploadUseDefaultChangedEvent;
        event VoidHandler ChmodUploadTypeChangedEvent;
        event VoidHandler UploadOwnerReadChangedEvent;
        event VoidHandler UploadOwnerWriteChangedEvent;
        event VoidHandler UploadOwnerExecuteChangedEvent;
        event VoidHandler UploadGroupReadChangedEvent;
        event VoidHandler UploadGroupWriteChangedEvent;
        event VoidHandler UploadGroupExecuteChangedEvent;
        event VoidHandler UploadOtherReadChangedEvent;
        event VoidHandler UploadOtherWriteChangedEvent;
        event VoidHandler UploadOtherExecuteChangedEvent;
        event VoidHandler PreserveModificationDownloadChangedEvent;
        event VoidHandler PreserveModificationUploadChangedEvent;
        event VoidHandler DownloadSkipChangedEvent;
        event VoidHandler DownloadSkipRegexChangedEvent;
        event VoidHandler DownloadSkipRegexDefaultEvent;
        event VoidHandler UploadSkipChangedEvent;
        event VoidHandler UploadSkipRegexChangedEvent;
        event VoidHandler UploadSkipRegexDefaultEvent;
        event VoidHandler DefaultBucketLocationChangedEvent;
        event VoidHandler DefaultStorageClassChangedEvent;
        event VoidHandler DefaultEncryptionChangedEvent;
        event VoidHandler DefaultDownloadThrottleChangedEvent;
        event VoidHandler DefaultUploadThrottleChangedEvent;
        event VoidHandler ConnectionTimeoutChangedEvent;
        event VoidHandler RetryDelayChangedEvent;
        event VoidHandler RetriesChangedEvent;
        //todo introduce Enums to handle the objects directly instead of using strings
        void PopulateBookmarks(List<KeyValueIconTriple<Host, string>> bookmarks);
        void PopulateEditors(List<KeyValueIconTriple<Application, string>> editors);
        void PopulateProtocols(List<KeyValueIconTriple<Protocol, string>> protocols);
        void PopulateEncodings(List<string> encodings);
        void PopulateTransferModes(List<KeyValuePair<string, Host.TransferType>> modes);
        void PopulateDuplicateDownloadActions(List<string> actions);
        void PopulateDuplicateUploadActions(List<string> actions);
        void PopulateChmodDownloadTypes(List<string> types);
        void PopulateChmodUploadTypes(List<string> types);
        void PopulateDefaultBucketLocations(IList<KeyValuePair<string, string>> locations);
        void PopulateDefaultStorageClasses(IList<KeyValuePair<string, string>> classes);
        void PopulateDefaultEncryption(IList<KeyValuePair<string, string>> algorithms);
        void PopulateDefaultDownloadThrottleList(IList<KeyValuePair<float, string>> throttles);
        void PopulateDefaultUploadThrottleList(IList<KeyValuePair<float, string>> throttles);
        void PopulateUpdateFeeds(IList<KeyValuePair<string, string>> feeds);
    }
}