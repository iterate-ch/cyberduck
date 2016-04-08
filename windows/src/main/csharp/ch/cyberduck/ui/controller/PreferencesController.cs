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
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.features;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.io;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.s3;
using ch.cyberduck.core.transfer;
using Ch.Cyberduck.Ui.Core;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Controls;
using java.util;
using java.util.regex;
using org.apache.log4j;
using org.jets3t.service.model;
using StructureMap;
using Utils = Ch.Cyberduck.Core.Utils;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class PreferencesController : WindowController<IPreferencesView>, CollectionListener
    {
        private static readonly string ForFiles = LocaleFactory.localizedString("for Files", "Preferences");
        private static readonly string ForFolders = LocaleFactory.localizedString("for Folders", "Preferences");
        private static readonly Logger Log = Logger.getLogger(typeof (PreferencesController).FullName);

        private static readonly KeyValueIconTriple<Host, string> NoneBookmark =
            new KeyValueIconTriple<Host, string>(null, LocaleFactory.localizedString("None"), null);

        private static readonly String NullString = "null";
        private static PreferencesController _instance;
        private bool _downloadRegexInvalid;
        private bool _uploadRegexInvalid;

        private PreferencesController(IPreferencesView view)
        {
            View = view;
            Init();

            View.SaveWorkspaceChangedEvent += View_SaveWorkspaceChangedEvent;
            View.NewBrowserOnStartupChangedEvent += View_NewBrowserOnStartupChangedEvent;
            View.DefaultBookmarkChangedEvent += View_DefaultBookmarkChangedEvent;
            View.UseKeychainChangedEvent += View_UseKeychainChangedEvent;
            View.ConfirmDisconnectChangedEvent += View_ConfirmDisconnectChangedEvent;
            View.DefaultProtocolChangedEvent += View_DefaultProtocolChangedEvent;
            View.DefaultEditorChangedEvent += View_DefaultEditorChangedEvent;
            View.RepopulateEditorsEvent += View_RepopulateEditorsEvent;
            View.AlwaysUseDefaultEditorChangedEvent += View_AlwaysUseDefaultEditorChangedEvent;
            View.ShowHiddenFilesChangedEvent += View_ShowHiddenFilesChangedEvent;
            View.DoubleClickEditorChangedEvent += View_DoubleClickEditorChangedEvent;
            View.ReturnKeyRenamesChangedEvent += View_ReturnKeyRenamesChangedEvent;
            View.InfoWindowShowsCurrentSelectionChangedEvent += View_InfoWindowShowsCurrentSelectionChangedEvent;
            View.AlternatingRowBackgroundChangedEvent += View_AlternatingRowBackgroundChangedEvent;
            View.HorizontalLinesChangedEvent += View_HorizontalLinesChangedEvent;
            View.VerticalLinesChangedEvent += View_VerticalLinesChangedEvent;
            View.DefaultEncodingChangedEvent += View_DefaultEncodingChangedEvent;
            View.TransferModeChangedEvent += View_TransferModeChangedEvent;
            View.TransfersToFrontChangedEvent += View_TransfersToFrontChangedEvent;
            View.TransfersToBackChangedEvent += View_TransfersToBackChangedEvent;
            View.RemoveFromTransfersChangedEvent += View_RemoveFromTransfersChangedEvent;
            View.OpenAfterDownloadChangedEvent += View_OpenAfterDownloadChangedEvent;
            View.DownloadFolderChangedEvent += View_DownloadFolderChangedEvent;
            View.DuplicateDownloadActionChangedEvent += View_DuplicateDownloadActionChangedEvent;
            View.DuplicateUploadActionChangedEvent += View_DuplicateUploadActionChangedEvent;
            View.DuplicateDownloadOverwriteChangedEvent += View_DuplicateDownloadOverwriteChangedEvent;
            View.DuplicateUploadOverwriteChangedEvent += View_DuplicateUploadOverwriteChangedEvent;
            View.UploadWithTemporaryFilenameChangedEvent += View_UploadWithTemporaryFilenameChangedEvent;
            View.BookmarkSizeChangedEvent += View_BookmarkSizeChangedEvent;

            View.ChmodDownloadChangedEvent += View_ChmodDownloadChangedEvent;
            View.ChmodDownloadUseDefaultChangedEvent += View_ChmodDownloadUseDefaultChangedEvent;
            View.ChmodDownloadTypeChangedEvent += View_ChmodDownloadTypeChangedEvent;
            View.DownloadOwnerReadChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadOwnerWriteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadOwnerExecuteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadGroupReadChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadGroupWriteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadGroupExecuteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadOtherReadChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadOtherWriteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.DownloadOtherExecuteChangedEvent += View_DownloadDefaultPermissionsChangedEvent;
            View.ChmodUploadChangedEvent += View_ChmodUploadChangedEvent;
            View.ChmodUploadUseDefaultChangedEvent += View_ChmodUploadUseDefaultChangedEvent;
            View.ChmodUploadTypeChangedEvent += View_ChmodUploadTypeChangedEvent;
            View.UploadOwnerReadChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadOwnerWriteChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadOwnerExecuteChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadGroupReadChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadGroupWriteChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadGroupExecuteChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadOtherReadChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadOtherWriteChangedEvent += View_UploadDefaultPermissionsChangedEvent;
            View.UploadOtherExecuteChangedEvent += View_UploadDefaultPermissionsChangedEvent;

            View.PreserveModificationDownloadChangedEvent += View_PreserveModificationDownloadChangedEvent;
            View.PreserveModificationUploadChangedEvent += View_PreserveModificationUploadChangedEvent;

            View.DownloadSkipChangedEvent += View_DownloadSkipChangedEvent;
            View.DownloadSkipRegexChangedEvent += View_DownloadSkipRegexChangedEvent;
            View.DownloadSkipRegexDefaultEvent += View_DownloadSkipRegexDefaultEvent;
            View.UploadSkipChangedEvent += View_UploadSkipChangedEvent;
            View.UploadSkipRegexChangedEvent += View_UploadSkipRegexChangedEvent;
            View.UploadSkipRegexDefaultEvent += View_UploadSkipRegexDefaultEvent;

            View.DefaultDownloadThrottleChangedEvent += View_DefaultDownloadThrottleChangedEvent;
            View.DefaultUploadThrottleChangedEvent += View_DefaultUploadThrottleChangedEvent;

            View.ConnectionTimeoutChangedEvent += View_ConnectionTimeoutChangedEvent;
            View.RetryDelayChangedEvent += View_RetryDelayChangedEvent;
            View.RetriesChangedEvent += View_RetriesChangedEvent;

            View.UseSystemProxyChangedEvent += View_UseSystemProxyChangedEvent;
            View.ChangeSystemProxyEvent += View_ChangeSystemProxyEvent;

            #region S3

            View.DefaultBucketLocationChangedEvent += View_DefaultBucketLocationChangedEvent;
            View.DefaultStorageClassChangedEvent += View_DefaultStorageClassChangedEvent;
            View.DefaultEncryptionChangedEvent += View_DefaultEncryptionChangedEvent;

            #endregion

            #region Google Docs

            View.DocumentExportFormatChanged += View_DocumentExportFormatChanged;
            View.PresentationExportFormatChanged += View_PresentationExportFormatChanged;
            View.SpreadsheetExportFormatChanged += View_SpreadsheetExportFormatChanged;
            View.ConvertUploadsChanged += View_ConvertUploadsChanged;
            View.OcrUploadsChanged += View_OcrUploadsChanged;

            #endregion

            #region Language

            View.LocaleChanged += View_LocaleChanged;

            #endregion

            #region Update

            View.AutomaticUpdateChangedEvent += View_AutomaticUpdateChangedEvent;
            View.CheckForUpdateEvent += View_CheckForUpdateEvent;
            View.UpdateFeedChangedEvent += View_UpdateFeedChangedEvent;

            #endregion
        }

        public static PreferencesController Instance
        {
            get
            {
                if (null == _instance)
                {
                    _instance = new PreferencesController(ObjectFactory.GetInstance<IPreferencesView>());
                }
                return _instance;
            }
        }

        public override bool Singleton
        {
            get { return true; }
        }

        public void collectionLoaded()
        {
            ;
        }

        public void collectionItemAdded(object obj)
        {
            Invoke(delegate
            {
                Host selected = View.DefaultBookmark;
                PopulateBookmarks();
                SelectDefaultBookmark(selected);
            });
        }

        public void collectionItemRemoved(object obj)
        {
            Invoke(delegate
            {
                Host selected = View.DefaultBookmark;
                PopulateBookmarks();
                SelectDefaultBookmark(selected);
            });
        }

        public void collectionItemChanged(object obj)
        {
            Invoke(delegate
            {
                Host selected = View.DefaultBookmark;
                PopulateBookmarks();
                SelectDefaultBookmark(selected);
            });
        }

        private void View_BookmarkSizeChangedEvent()
        {
            PreferencesFactory.get().setProperty("bookmark.icon.size", View.BookmarkSize);
            foreach (BrowserController b in MainController.Browsers)
            {
                b.UpdateBookmarks();
            }
        }

        private void View_DefaultEncryptionChangedEvent()
        {
            PreferencesFactory.get()
                .setProperty("s3.encryption.algorithm",
                    NullString.Equals(View.DefaultEncryption) ? null : View.DefaultEncryption);
        }

        private void View_AlwaysUseDefaultEditorChangedEvent()
        {
            PreferencesFactory.get().setProperty("editor.alwaysUseDefault", View.AlwaysUseDefaultEditor);
        }

        private void View_RepopulateEditorsEvent()
        {
            PopulateAndSelectEditor();
        }

        private void View_DefaultEditorChangedEvent()
        {
            Application selected = View.DefaultEditor;
            PreferencesFactory.get().setProperty("editor.bundleIdentifier", selected.getIdentifier());
        }

        private void View_ChangeSystemProxyEvent()
        {
            LaunchIEOptions(4);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="activeRegister">Register to select (connections settings=4)</param>
        private void LaunchIEOptions(int activeRegister)
        {
            ApplicationLauncherFactory.get()
                .open(new Application("rundll32.exe"), "shell32.dll,Control_RunDLL inetcpl.cpl,," + activeRegister);
        }

        private void View_UpdateFeedChangedEvent()
        {
            PreferencesFactory.get().setProperty("update.feed", View.UpdateFeed);
        }

        private void View_UploadWithTemporaryFilenameChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.file.temporary", View.UploadWithTemporaryFilename);
        }

        private void View_UseSystemProxyChangedEvent()
        {
            PreferencesFactory.get().setProperty("connection.proxy.enable", View.UseSystemProxy);
        }

        private void View_CheckForUpdateEvent()
        {
            new WindowsPeriodicUpdateChecker().check(false);
        }

        private void View_AutomaticUpdateChangedEvent()
        {
            PreferencesFactory.get().setProperty("update.check", View.AutomaticUpdateCheck);
        }

        private void View_LocaleChanged()
        {
            if ("default".Equals(View.CurrentLocale))
            {
                PreferencesFactory.get().deleteProperty("application.language");
                PreferencesFactory.get().setProperty("application.language.custom", false.ToString());
            }
            else
            {
                PreferencesFactory.get().setProperty("application.language", View.CurrentLocale);
                PreferencesFactory.get().setProperty("application.language.custom", true.ToString());
            }
        }

        private void View_OcrUploadsChanged()
        {
            PreferencesFactory.get().setProperty("google.docs.upload.ocr", View.OcrUploads);
        }

        private void View_ConvertUploadsChanged()
        {
            PreferencesFactory.get().setProperty("google.docs.upload.convert", View.ConvertUploads);
        }

        private void View_SpreadsheetExportFormatChanged()
        {
            PreferencesFactory.get().setProperty("google.docs.export.spreadsheet", View.SpreadsheetExportFormat);
        }

        private void View_PresentationExportFormatChanged()
        {
            PreferencesFactory.get().setProperty("google.docs.export.presentation", View.PresentationExportFormat);
        }

        private void View_DocumentExportFormatChanged()
        {
            PreferencesFactory.get().setProperty("google.docs.export.document", View.DocumentExportFormat);
        }

        private void View_DefaultStorageClassChangedEvent()
        {
            PreferencesFactory.get().setProperty("s3.storage.class", View.DefaultStorageClass);
        }

        private void View_RetriesChangedEvent()
        {
            PreferencesFactory.get().setProperty("connection.retry", View.Retries);
        }

        private void View_RetryDelayChangedEvent()
        {
            PreferencesFactory.get().setProperty("connection.retry.delay", View.RetryDelay);
        }

        private void View_ConnectionTimeoutChangedEvent()
        {
            PreferencesFactory.get().setProperty("connection.timeout.seconds", View.ConnectionTimeout);
        }

        private void View_DefaultUploadThrottleChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.bandwidth.bytes", View.DefaultUploadThrottle);
        }

        private void View_DefaultDownloadThrottleChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.bandwidth.bytes", View.DefaultDownloadThrottle);
        }

        private void View_DefaultBucketLocationChangedEvent()
        {
            PreferencesFactory.get().setProperty("s3.location", View.DefaultBucketLocation);
        }

        private void View_UploadSkipRegexDefaultEvent()
        {
            string regex = PreferencesFactory.get().getProperty("queue.upload.skip.regex.default");
            View.UploadSkipRegex = regex;
            PreferencesFactory.get().setProperty("queue.upload.skip.regex", regex);
        }

        private void View_UploadSkipRegexChangedEvent()
        {
            string value = View.UploadSkipRegex.Trim();
            if (string.IsNullOrEmpty(value))
            {
                PreferencesFactory.get().setProperty("queue.upload.skip.enable", false);
                PreferencesFactory.get().setProperty("queue.upload.skip.regex", value);
                View.UploadSkip = false;
            }
            try
            {
                Pattern compiled = Pattern.compile(value);
                PreferencesFactory.get().setProperty("queue.upload.skip.regex", compiled.pattern());
                if (_uploadRegexInvalid)
                {
                    View.MarkUploadSkipRegex(-1);
                }
                _uploadRegexInvalid = false;
            }
            catch (PatternSyntaxException ex)
            {
                _uploadRegexInvalid = true;
                View.MarkUploadSkipRegex(ex.getIndex());
            }
        }

        private void View_UploadSkipChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.skip.enable", View.DownloadSkip);
            View.UploadSkipRegexEnabled = View.UploadSkip;
        }

        private void View_DownloadSkipRegexDefaultEvent()
        {
            string regex = PreferencesFactory.get().getProperty("queue.download.skip.regex.default");
            View.DownloadSkipRegex = regex;
            PreferencesFactory.get().setProperty("queue.download.skip.regex", regex);
        }

        private void View_DownloadSkipChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.skip.enable", View.DownloadSkip);
            View.DownloadSkipRegexEnabled = View.DownloadSkip;
        }

        private void View_DownloadSkipRegexChangedEvent()
        {
            string value = View.DownloadSkipRegex.Trim();
            if (string.IsNullOrEmpty(value))
            {
                PreferencesFactory.get().setProperty("queue.download.skip.enable", false);
                PreferencesFactory.get().setProperty("queue.download.skip.regex", value);
                View.DownloadSkip = false;
            }
            try
            {
                Pattern compiled = Pattern.compile(value);
                PreferencesFactory.get().setProperty("queue.download.skip.regex", compiled.pattern());
                if (_downloadRegexInvalid)
                {
                    View.MarkDownloadSkipRegex(-1);
                }
                _downloadRegexInvalid = false;
            }
            catch (PatternSyntaxException ex)
            {
                _downloadRegexInvalid = true;
                View.MarkDownloadSkipRegex(ex.getIndex());
            }
        }

        private void View_PreserveModificationUploadChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.timestamp.change", View.PreserveModificationUpload);
        }

        private void View_PreserveModificationDownloadChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.timestamp.change", View.PreserveModificationDownload);
        }

        private void View_ChmodUploadTypeChangedEvent()
        {
            Permission p = null;
            if (ForFiles.Equals(View.ChmodUploadType))
            {
                p = new Permission(PreferencesFactory.get().getInteger("queue.upload.permissions.file.default"));
            }
            if (ForFolders.Equals(View.ChmodUploadType))
            {
                p = new Permission(PreferencesFactory.get().getInteger("queue.upload.permissions.folder.default"));
            }
            if (null == p)
            {
                Log.error("No selected item");
                return;
            }
            Permission.Action ownerPerm = p.getUser();
            Permission.Action groupPerm = p.getGroup();
            Permission.Action otherPerm = p.getOther();

            View.UploadOwnerRead = ownerPerm.implies(Permission.Action.read);
            View.UploadOwnerWrite = ownerPerm.implies(Permission.Action.write);
            View.UploadOwnerExecute = ownerPerm.implies(Permission.Action.execute);

            View.UploadGroupRead = groupPerm.implies(Permission.Action.read);
            View.UploadGroupWrite = groupPerm.implies(Permission.Action.write);
            View.UploadGroupExecute = groupPerm.implies(Permission.Action.execute);

            View.UploadOtherRead = otherPerm.implies(Permission.Action.read);
            View.UploadOtherWrite = otherPerm.implies(Permission.Action.write);
            View.UploadOtherExecute = otherPerm.implies(Permission.Action.execute);
        }

        private void View_ChmodUploadUseDefaultChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.permissions.default", View.ChmodUploadUseDefault);
            View.ChmodUploadDefaultEnabled = View.ChmodUploadUseDefault;
        }

        private void View_ChmodUploadChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.upload.permissions.change", View.ChmodUpload);
            View.ChmodUploadEnabled = View.ChmodUpload;
        }

        private void View_ChmodDownloadTypeChangedEvent()
        {
            Permission p = null;
            if (ForFiles.Equals(View.ChmodDownloadType))
            {
                p = new Permission(PreferencesFactory.get().getInteger("queue.download.permissions.file.default"));
            }
            if (ForFolders.Equals(View.ChmodDownloadType))
            {
                p = new Permission(PreferencesFactory.get().getInteger("queue.download.permissions.folder.default"));
            }
            if (null == p)
            {
                Log.error("No selected item");
                return;
            }
            Permission.Action ownerPerm = p.getUser();
            Permission.Action groupPerm = p.getGroup();
            Permission.Action otherPerm = p.getOther();

            View.DownloadOwnerRead = ownerPerm.implies(Permission.Action.read);
            View.DownloadOwnerWrite = ownerPerm.implies(Permission.Action.write);
            View.DownloadOwnerExecute = ownerPerm.implies(Permission.Action.execute);

            View.DownloadGroupRead = groupPerm.implies(Permission.Action.read);
            View.DownloadGroupWrite = groupPerm.implies(Permission.Action.write);
            View.DownloadGroupExecute = groupPerm.implies(Permission.Action.execute);

            View.DownloadOtherRead = otherPerm.implies(Permission.Action.read);
            View.DownloadOtherWrite = otherPerm.implies(Permission.Action.write);
            View.DownloadOtherExecute = otherPerm.implies(Permission.Action.execute);
        }

        private void View_ChmodDownloadUseDefaultChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.permissions.default", View.ChmodDownloadUseDefault);
            View.ChmodDownloadDefaultEnabled = View.ChmodDownloadUseDefault;
        }

        private void View_ChmodDownloadChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.permissions.change", View.ChmodDownload);
            View.ChmodDownloadEnabled = View.ChmodDownload;
        }

        private void View_UploadDefaultPermissionsChangedEvent()
        {
            Permission.Action u = Permission.Action.none;
            if (View.UploadOwnerRead)
            {
                u = u.or(Permission.Action.read);
            }
            if (View.UploadOwnerWrite)
            {
                u = u.or(Permission.Action.write);
            }
            if (View.UploadOwnerExecute)
            {
                u = u.or(Permission.Action.execute);
            }
            Permission.Action g = Permission.Action.none;
            if (View.UploadGroupRead)
            {
                g = g.or(Permission.Action.read);
            }
            if (View.UploadGroupWrite)
            {
                g = g.or(Permission.Action.write);
            }
            if (View.UploadGroupExecute)
            {
                g = g.or(Permission.Action.execute);
            }
            Permission.Action o = Permission.Action.none;
            if (View.UploadOtherRead)
            {
                o = o.or(Permission.Action.read);
            }
            if (View.UploadOtherWrite)
            {
                o = o.or(Permission.Action.write);
            }
            if (View.UploadOtherExecute)
            {
                o = o.or(Permission.Action.execute);
            }
            Permission permission = new Permission(u, g, o);
            if (ForFiles.Equals(View.ChmodUploadType))
            {
                PreferencesFactory.get().setProperty("queue.upload.permissions.file.default", permission.getMode());
            }
            if (ForFolders.Equals(View.ChmodUploadType))
            {
                PreferencesFactory.get().setProperty("queue.upload.permissions.folder.default", permission.getMode());
            }
        }

        private void View_DownloadDefaultPermissionsChangedEvent()
        {
            Permission.Action u = Permission.Action.none;
            if (View.DownloadOwnerRead)
            {
                u = u.or(Permission.Action.read);
            }
            if (View.DownloadOwnerWrite)
            {
                u = u.or(Permission.Action.write);
            }
            if (View.DownloadOwnerExecute)
            {
                u = u.or(Permission.Action.execute);
            }
            Permission.Action g = Permission.Action.none;
            if (View.DownloadGroupRead)
            {
                g = g.or(Permission.Action.read);
            }
            if (View.DownloadGroupWrite)
            {
                g = g.or(Permission.Action.write);
            }
            if (View.DownloadGroupExecute)
            {
                g = g.or(Permission.Action.execute);
            }
            Permission.Action o = Permission.Action.none;
            if (View.DownloadOtherRead)
            {
                o = o.or(Permission.Action.read);
            }
            if (View.DownloadOtherWrite)
            {
                o = o.or(Permission.Action.write);
            }
            if (View.DownloadOtherExecute)
            {
                o = o.or(Permission.Action.execute);
            }
            Permission permission = new Permission(u, g, o);
            if (ForFiles.Equals(View.ChmodDownloadType))
            {
                PreferencesFactory.get().setProperty("queue.download.permissions.file.default", permission.getMode());
            }
            if (ForFolders.Equals(View.ChmodDownloadType))
            {
                PreferencesFactory.get().setProperty("queue.download.permissions.folder.default", permission.getMode());
            }
        }

        private void View_DuplicateUploadOverwriteChangedEvent()
        {
            if (View.DuplicateUploadOverwrite)
            {
                PreferencesFactory.get().setProperty("queue.upload.reload.action", TransferAction.overwrite.toString());
            }
            else
            {
                PreferencesFactory.get()
                    .setProperty("queue.upload.reload.action",
                        PreferencesFactory.get().getProperty("queue.upload.action"));
            }
        }

        private void View_DuplicateDownloadOverwriteChangedEvent()
        {
            if (View.DuplicateDownloadOverwrite)
            {
                PreferencesFactory.get()
                    .setProperty("queue.download.reload.action", TransferAction.overwrite.toString());
            }
            else
            {
                PreferencesFactory.get()
                    .setProperty("queue.download.reload.action",
                        PreferencesFactory.get().getProperty("queue.download.action"));
            }
        }

        private void View_DuplicateUploadActionChangedEvent()
        {
            duplicateComboboxClicked(View.DuplicateUploadAction, "queue.upload.action");
            View_DuplicateUploadOverwriteChangedEvent();
        }

        private void duplicateComboboxClicked(String selected, String property)
        {
            if (selected.Equals(TransferAction.callback.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.callback.toString());
            }
            else if (selected.Equals(TransferAction.overwrite.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.overwrite.toString());
            }
            else if (selected.Equals(TransferAction.resume.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.resume.toString());
            }
            else if (selected.Equals(TransferAction.rename.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.rename.toString());
            }
            else if (selected.Equals(TransferAction.renameexisting.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.renameexisting.toString());
            }
            else if (selected.Equals(TransferAction.comparison.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.comparison.toString());
            }
            else if (selected.Equals(TransferAction.skip.getTitle()))
            {
                PreferencesFactory.get().setProperty(property, TransferAction.skip.toString());
            }
        }

        private void View_DuplicateDownloadActionChangedEvent()
        {
            duplicateComboboxClicked(View.DuplicateDownloadAction, "queue.download.action");
            View_DuplicateDownloadOverwriteChangedEvent();
        }

        private void View_DownloadFolderChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.folder", View.DownloadFolder);
        }

        private void View_OpenAfterDownloadChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.download.complete.open", View.OpenAfterDownload);
        }

        private void View_RemoveFromTransfersChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.removeItemWhenComplete", View.RemoveFromTransfers);
        }

        private void View_TransfersToBackChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.window.open.transfer.stop", View.TransfersToBack);
        }

        private void View_TransfersToFrontChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.window.open.transfer.start", View.TransfersToFront);
        }

        private void View_TransferModeChangedEvent()
        {
            PreferencesFactory.get().setProperty("queue.transfer.type", View.TransferMode.name());
        }

        private void View_DefaultEncodingChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.charset.encoding", View.DefaultEncoding);
        }

        private void View_VerticalLinesChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.verticalLines", View.VerticalLines);
            //todo
            //BrowserController.updateBrowserTableAttributes();
        }

        private void View_HorizontalLinesChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.horizontalLines", View.HorizontalLines);
            //todo
            //BrowserController.updateBrowserTableAttributes();
        }

        private void View_AlternatingRowBackgroundChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.alternatingRows", View.AlternatingRowBackground);
            //todo
            //BrowserController.updateBrowserTableAttributes();
        }

        private void View_InfoWindowShowsCurrentSelectionChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.info.inspector", View.InfoWindowShowsCurrentSelection);
        }

        private void View_DoubleClickEditorChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.doubleclick.edit", View.DoubleClickEditor);
        }

        private void View_ReturnKeyRenamesChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.enterkey.rename", View.ReturnKeyRenames);
        }

        private void View_ShowHiddenFilesChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.showHidden", View.ShowHiddenFiles);
        }

        private void View_DefaultProtocolChangedEvent()
        {
            Protocol selected = View.DefaultProtocol;
            PreferencesFactory.get().setProperty("connection.protocol.default", selected.getProvider());
            PreferencesFactory.get().setProperty("connection.port.default", selected.getDefaultPort());
        }

        private void View_ConfirmDisconnectChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.confirmDisconnect", View.ConfirmDisconnect);
        }

        private void View_UseKeychainChangedEvent()
        {
            PreferencesFactory.get().setProperty("connection.login.useKeychain", View.UseKeychain);
        }

        private void View_DefaultBookmarkChangedEvent()
        {
            if (null == View.DefaultBookmark)
            {
                PreferencesFactory.get().deleteProperty("browser.open.bookmark.default");
            }
            else
            {
                PreferencesFactory.get().setProperty("browser.open.bookmark.default", View.DefaultBookmark.getUuid());
            }
        }

        private void SelectDefaultBookmark(Host host)
        {
            if (BookmarkCollection.defaultCollection().contains(host))
            {
                View.DefaultBookmark = host;
            }
            else
            {
                View.DefaultBookmark = null;
            }
        }

        private void SelectDefaultBookmark(string nickname)
        {
            if (null == nickname)
            {
                View.DefaultBookmark = null;
            }
            else
            {
                bool bookmarkFound = false;
                foreach (Host host in BookmarkCollection.defaultCollection())
                {
                    if (nickname.Equals(host.getUuid()))
                    {
                        View.DefaultBookmark = host;
                        bookmarkFound = true;
                        break;
                    }
                }
                if (!bookmarkFound)
                {
                    View.DefaultBookmark = null;
                }
            }
        }

        private void View_NewBrowserOnStartupChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.openUntitled", View.NewBrowserOnStartup);
        }

        private void View_SaveWorkspaceChangedEvent()
        {
            PreferencesFactory.get().setProperty("browser.serialize", View.SaveWorkspace);
        }

        private void Init()
        {
            #region General

            View.SaveWorkspace = PreferencesFactory.get().getBoolean("browser.serialize");
            View.NewBrowserOnStartup = PreferencesFactory.get().getBoolean("browser.openUntitled");
            PopulateBookmarks();
            BookmarkCollection.defaultCollection().addListener(this);
            View.ViewClosedEvent += delegate { BookmarkCollection.defaultCollection().removeListener(this); };
            SelectDefaultBookmark(PreferencesFactory.get().getProperty("browser.open.bookmark.default"));
            View.ConfirmDisconnect = PreferencesFactory.get().getBoolean("browser.confirmDisconnect");
            View.UseKeychain = PreferencesFactory.get().getBoolean("connection.login.useKeychain");
            PopulateDefaultProtocols();
            View.DefaultProtocol =
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"));
            View.AlternatingRowBackground = PreferencesFactory.get().getBoolean("browser.alternatingRows");
            View.VerticalLines = PreferencesFactory.get().getBoolean("browser.verticalLines");
            View.HorizontalLines = PreferencesFactory.get().getBoolean("browser.horizontalLines");
            PopulateEncodings();
            View.DefaultEncoding = PreferencesFactory.get().getProperty("browser.charset.encoding");

            #endregion

            #region Browser

            View.InfoWindowShowsCurrentSelection = PreferencesFactory.get().getBoolean("browser.info.inspector");
            View.ShowHiddenFiles = PreferencesFactory.get().getBoolean("browser.showHidden");
            View.DoubleClickEditor = PreferencesFactory.get().getBoolean("browser.doubleclick.edit");
            View.ReturnKeyRenames = PreferencesFactory.get().getBoolean("browser.enterkey.rename");
            PopulateBookmarkSize();
            View.BookmarkSize = PreferencesFactory.get().getInteger("bookmark.icon.size");

            #endregion

            #region Transfers - General

            PopulateTransferModes();
            View.TransferMode = Host.TransferType.valueOf(PreferencesFactory.get().getProperty("queue.transfer.type"));
            View.TransfersToFront = PreferencesFactory.get().getBoolean("queue.window.open.transfer.start");
            View.TransfersToBack = PreferencesFactory.get().getBoolean("queue.window.open.transfer.stop");
            View.RemoveFromTransfers = PreferencesFactory.get().getBoolean("queue.removeItemWhenComplete");
            View.OpenAfterDownload = PreferencesFactory.get().getBoolean("queue.download.complete.open");
            View.DownloadFolder = PreferencesFactory.get().getProperty("queue.download.folder");
            PopulateDuplicateActions();
            View.DuplicateDownloadAction = GetDuplicateAction("queue.download.action");
            View.DuplicateUploadAction = GetDuplicateAction("queue.upload.action");
            View.DuplicateDownloadOverwrite =
                PreferencesFactory.get()
                    .getProperty("queue.download.reload.action")
                    .Equals(TransferAction.overwrite.toString())
                    ? true
                    : false;
            View.DuplicateUploadOverwrite =
                PreferencesFactory.get()
                    .getProperty("queue.upload.reload.action")
                    .Equals(TransferAction.overwrite.toString())
                    ? true
                    : false;
            View.UploadWithTemporaryFilename = PreferencesFactory.get().getBoolean("queue.upload.file.temporary");

            #endregion

            #region Editor

            PopulateAndSelectEditor();
            View.AlwaysUseDefaultEditor = PreferencesFactory.get().getBoolean("editor.alwaysUseDefault");

            #endregion

            #region Transfers - Permissions

            PopulateChmodTypes();
            View.ChmodDownload = PreferencesFactory.get().getBoolean("queue.download.permissions.change");
            View.ChmodDownloadEnabled = View.ChmodDownload;
            View.ChmodDownloadUseDefault = PreferencesFactory.get().getBoolean("queue.download.permissions.default");
            View.ChmodDownloadDefaultEnabled = View.ChmodDownloadUseDefault;
            View.ChmodDownloadType = ForFiles;
            View_ChmodDownloadTypeChangedEvent();
            View.ChmodUpload = PreferencesFactory.get().getBoolean("queue.upload.permissions.change");
            View.ChmodUploadEnabled = View.ChmodUpload;
            View.ChmodUploadUseDefault = PreferencesFactory.get().getBoolean("queue.upload.permissions.default");
            View.ChmodUploadDefaultEnabled = PreferencesFactory.get().getBoolean("queue.upload.permissions.change") &&
                                             PreferencesFactory.get().getBoolean("queue.upload.permissions.default");
            View.ChmodUploadType = ForFiles;
            View_ChmodUploadTypeChangedEvent();

            #endregion

            #region Transfers - Timestamps

            View.PreserveModificationDownload = PreferencesFactory.get().getBoolean("queue.download.timestamp.change");
            View.PreserveModificationUpload = PreferencesFactory.get().getBoolean("queue.upload.timestamp.change");

            #endregion

            #region Transfers - Advanced

            View.DownloadSkip = PreferencesFactory.get().getBoolean("queue.download.skip.enable");
            View.DownloadSkipRegex = PreferencesFactory.get().getProperty("queue.download.skip.regex");
            View.DownloadSkipRegexEnabled = View.DownloadSkip;
            View.UploadSkip = PreferencesFactory.get().getBoolean("queue.upload.skip.enable");
            View.UploadSkipRegex = PreferencesFactory.get().getProperty("queue.upload.skip.regex");
            View.UploadSkipRegexEnabled = View.UploadSkip;

            PopulateDefaultDownloadThrottleList();
            PopulateDefaultUploadThrottleList();
            View.DefaultDownloadThrottle = PreferencesFactory.get().getFloat("queue.download.bandwidth.bytes");
            View.DefaultUploadThrottle = PreferencesFactory.get().getFloat("queue.upload.bandwidth.bytes");
            View.Retries = PreferencesFactory.get().getInteger("connection.retry");
            View.RetryDelay = PreferencesFactory.get().getInteger("connection.retry.delay");
            View.ConnectionTimeout = PreferencesFactory.get().getInteger("connection.timeout.seconds");
            View.UseSystemProxy = PreferencesFactory.get().getBoolean("connection.proxy.enable");

            #endregion

            #region S3

            PopulateDefaultBucketLocations();
            View.DefaultBucketLocation = PreferencesFactory.get().getProperty("s3.location");
            PopulateDefaultStorageClasses();
            View.DefaultStorageClass = PreferencesFactory.get().getProperty("s3.storage.class");
            PopulateDefaultEncryption();
            String algorithm = PreferencesFactory.get().getProperty("s3.encryption.algorithm");
            View.DefaultEncryption = Utils.IsNotBlank(algorithm) ? algorithm : NullString;

            #endregion

            #region Google Docs

            PopulateDocumentExportFormats();
            View.DocumentExportFormat = PreferencesFactory.get().getProperty("google.docs.export.document");
            PopulatePresentationExportFormats();
            View.PresentationExportFormat = PreferencesFactory.get().getProperty("google.docs.export.presentation");
            PopulateSpreadsheetExportFormats();
            View.SpreadsheetExportFormat = PreferencesFactory.get().getProperty("google.docs.export.spreadsheet");
            View.ConvertUploads = PreferencesFactory.get().getBoolean("google.docs.upload.convert");
            View.OcrUploads = PreferencesFactory.get().getBoolean("google.docs.upload.ocr");

            #endregion

            #region Update

            View.UpdateEnabled = new WindowsPeriodicUpdateChecker().hasUpdatePrivileges();
            View.AutomaticUpdateCheck = PreferencesFactory.get().getBoolean("update.check");
            long lastCheck = PreferencesFactory.get().getLong("update.check.last");
            View.LastUpdateCheck = 0 == lastCheck
                ? String.Empty
                : UserDefaultsDateFormatter.GetLongFormat(
                    new DateTime(PreferencesFactory.get().getLong("update.check.last")));
            PopulateFeeds();
            View.UpdateFeed = PreferencesFactory.get().getProperty("update.feed");

            #endregion

            #region Language

            PopulateLanguages();
            string userLanguage = PreferencesFactory.get().getProperty("application.language");

            if (PreferencesFactory.get().getBoolean("application.language.custom"))
            {
                View.CurrentLocale = userLanguage;
            }
            else
            {
                View.CurrentLocale = "default";
            }

            #endregion
        }

        private void PopulateBookmarkSize()
        {
            List<KeyValuePair<int, string>> sizes = new List<KeyValuePair<int, string>>();
            sizes.Add(new KeyValuePair<int, string>(BookmarkController.SmallBookmarkSize,
                LocaleFactory.localizedString("Use Small Icons", "Preferences")));
            sizes.Add(new KeyValuePair<int, string>(BookmarkController.MediumBookmarkSize,
                LocaleFactory.localizedString("Use Medium Icons", "Preferences")));
            sizes.Add(new KeyValuePair<int, string>(BookmarkController.LargeBookmarkSize,
                LocaleFactory.localizedString("Use Large Icons", "Preferences")));
            View.PopulateBookmarkSize(sizes);
        }

        private void PopulateDefaultEncryption()
        {
            IList<KeyValuePair<string, string>> algorithms = new List<KeyValuePair<string, string>>();
            algorithms.Add(new KeyValuePair<string, string>(NullString, LocaleFactory.localizedString("None")));
            algorithms.Add(new KeyValuePair<string, string>("AES256", LocaleFactory.localizedString("AES256", "S3")));
            View.PopulateDefaultEncryption(algorithms);
        }

        private void PopulateFeeds()
        {
            IList<KeyValuePair<string, string>> feeds = new List<KeyValuePair<string, string>>();
            feeds.Add(new KeyValuePair<string, string>("release", LocaleFactory.localizedString("Release")));
            feeds.Add(new KeyValuePair<string, string>("beta", LocaleFactory.localizedString("Beta")));
            feeds.Add(new KeyValuePair<string, string>("nightly", LocaleFactory.localizedString("Snapshot Builds")));
            View.PopulateUpdateFeeds(feeds);
        }

        private void PopulateLanguages()
        {
            IList<KeyValuePair<string, string>> locales = new List<KeyValuePair<string, string>>();

            List appLocales = PreferencesFactory.get().applicationLocales();
            locales.Add(new KeyValuePair<string, string>("default", LocaleFactory.localizedString("Default")));
            for (int i = 0; i < appLocales.size(); i++)
            {
                string locale = (string) appLocales.get(i);
                locales.Add(new KeyValuePair<string, string>(locale, PreferencesFactory.get().getDisplayName(locale)));
            }
            View.PopulateLocales(locales);
        }

        private void PopulateSpreadsheetExportFormats()
        {
            IList<KeyValuePair<string, string>> f = new List<KeyValuePair<string, string>>();
            string formats = PreferencesFactory.get().getProperty("google.docs.export.spreadsheet.formats");
            foreach (string s in formats.Split(','))
            {
                string ext = "." + s;
                f.Add(new KeyValuePair<string, string>(s,
                    String.Format("{0} ({1})", FileDescriptorFactory.get().getKind(ext), ext)));
            }
            View.PopulateSpreadsheetExportFormats(f);
        }

        private void PopulatePresentationExportFormats()
        {
            IList<KeyValuePair<string, string>> f = new List<KeyValuePair<string, string>>();
            string formats = PreferencesFactory.get().getProperty("google.docs.export.presentation.formats");
            foreach (string s in formats.Split(','))
            {
                string ext = "." + s;
                f.Add(new KeyValuePair<string, string>(s,
                    String.Format("{0} ({1})", FileDescriptorFactory.get().getKind(ext), ext)));
            }
            View.PopulatePresentationExportFormats(f);
        }

        private void PopulateDocumentExportFormats()
        {
            IList<KeyValuePair<string, string>> f = new List<KeyValuePair<string, string>>();
            string formats = PreferencesFactory.get().getProperty("google.docs.export.document.formats");
            foreach (string s in formats.Split(','))
            {
                string ext = "." + s;
                f.Add(new KeyValuePair<string, string>(s,
                    String.Format("{0} ({1})", FileDescriptorFactory.get().getKind(ext), ext)));
            }
            View.PopulateDocumentExportFormats(f);
        }

        private void PopulateDefaultUploadThrottleList()
        {
            IList<KeyValuePair<float, string>> list = new List<KeyValuePair<float, string>>();
            list.Add(new KeyValuePair<float, string>(BandwidthThrottle.UNLIMITED,
                LocaleFactory.localizedString("Unlimited Bandwidth", "Preferences")));
            foreach (String option in
                PreferencesFactory.get()
                    .getProperty("queue.bandwidth.options")
                    .Split(new[] {','}, StringSplitOptions.RemoveEmptyEntries))
            {
                list.Add(new KeyValuePair<float, string>(Convert.ToInt32(option.Trim()),
                    (SizeFormatterFactory.get().format(Convert.ToInt32(option.Trim())) + "/s")));
            }
            View.PopulateDefaultUploadThrottleList(list);
        }

        private void PopulateDefaultDownloadThrottleList()
        {
            IList<KeyValuePair<float, string>> list = new List<KeyValuePair<float, string>>();
            list.Add(new KeyValuePair<float, string>(BandwidthThrottle.UNLIMITED,
                LocaleFactory.localizedString("Unlimited Bandwidth", "Preferences")));
            foreach (String option in
                PreferencesFactory.get()
                    .getProperty("queue.bandwidth.options")
                    .Split(new[] {','}, StringSplitOptions.RemoveEmptyEntries))
            {
                list.Add(new KeyValuePair<float, string>(Convert.ToInt32(option.Trim()),
                    (SizeFormatterFactory.get().format(Convert.ToInt32(option.Trim())) + "/s")));
            }
            View.PopulateDefaultDownloadThrottleList(list);
        }

        private void PopulateDefaultBucketLocations()
        {
            IList<KeyValuePair<string, string>> defaultBucketLocations = new List<KeyValuePair<string, string>>();
            Set locations = new S3Protocol().getRegions();
            Iterator iter = locations.iterator();
            while (iter.hasNext())
            {
                Location.Name location = (Location.Name) iter.next();
                defaultBucketLocations.Add(new KeyValuePair<string, string>(location.getIdentifier(),
                    location.toString()));
            }
            View.PopulateDefaultBucketLocations(defaultBucketLocations);
        }

        private void PopulateDefaultStorageClasses()
        {
            IList<KeyValuePair<string, string>> storageClasses = new List<KeyValuePair<string, string>>();
            storageClasses.Add(new KeyValuePair<string, string>(S3Object.STORAGE_CLASS_STANDARD,
                LocaleFactory.localizedString(S3Object.STORAGE_CLASS_STANDARD, "S3")));
            storageClasses.Add(new KeyValuePair<string, string>("STANDARD_IA",
                LocaleFactory.localizedString("STANDARD_IA", "S3")));
            storageClasses.Add(new KeyValuePair<string, string>(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY,
                LocaleFactory.localizedString(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, "S3")));
            View.PopulateDefaultStorageClasses(storageClasses);
        }

        private void PopulateChmodTypes()
        {
            List<string> chmodDownloadTypes = new List<string>();
            chmodDownloadTypes.Add(ForFiles);
            chmodDownloadTypes.Add(ForFolders);
            View.PopulateChmodDownloadTypes(chmodDownloadTypes);

            List<string> chmodUploadTypes = new List<string>();
            chmodUploadTypes.Add(ForFiles);
            chmodUploadTypes.Add(ForFolders);
            View.PopulateChmodUploadTypes(chmodUploadTypes);
        }

        private string GetDuplicateAction(string property)
        {
            return TransferAction.forName(PreferencesFactory.get().getProperty(property)).getTitle();
        }

        private void PopulateDuplicateActions()
        {
            List<string> downloadActions = new List<string>();
            downloadActions.Add(TransferAction.callback.getTitle());
            downloadActions.Add(TransferAction.overwrite.getTitle());
            downloadActions.Add(TransferAction.resume.getTitle());
            downloadActions.Add(TransferAction.rename.getTitle());
            downloadActions.Add(TransferAction.renameexisting.getTitle());
            downloadActions.Add(TransferAction.comparison.getTitle());
            downloadActions.Add(TransferAction.skip.getTitle());
            View.PopulateDuplicateDownloadActions(downloadActions);

            List<string> uploadActions = new List<string>();
            uploadActions.Add(TransferAction.callback.getTitle());
            uploadActions.Add(TransferAction.overwrite.getTitle());
            uploadActions.Add(TransferAction.resume.getTitle());
            uploadActions.Add(TransferAction.rename.getTitle());
            uploadActions.Add(TransferAction.renameexisting.getTitle());
            uploadActions.Add(TransferAction.comparison.getTitle());
            uploadActions.Add(TransferAction.skip.getTitle());
            View.PopulateDuplicateUploadActions(uploadActions);
        }

        private void PopulateTransferModes()
        {
            List<KeyValuePair<string, Host.TransferType>> modes = new List<KeyValuePair<string, Host.TransferType>>();
            foreach (String name in
                Utils.ConvertFromJavaList<String>(PreferencesFactory.get().getList("queue.transfer.type.enabled")))
            {
                Host.TransferType t = Host.TransferType.valueOf(name);
                modes.Add(new KeyValuePair<string, Host.TransferType>(t.toString(), t));
            }
            View.PopulateTransferModes(modes);
        }

        private void PopulateEncodings()
        {
            List<string> encodings = new List<string>();
            encodings.AddRange(new DefaultCharsetProvider().availableCharsets());
            View.PopulateEncodings(encodings);
        }

        private void PopulateDefaultProtocols()
        {
            List<KeyValueIconTriple<Protocol, string>> protocols = new List<KeyValueIconTriple<Protocol, string>>();
            foreach (Protocol p in ProtocolFactory.getEnabledProtocols().toArray(new Protocol[] {}))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(p, p.getDescription(), p.getProvider()));
            }
            View.PopulateProtocols(protocols);
        }

        private void PopulateBookmarks()
        {
            List<KeyValueIconTriple<Host, string>> bookmarks = new List<KeyValueIconTriple<Host, string>>();
            bookmarks.Add(NoneBookmark);
            foreach (Host host in BookmarkCollection.defaultCollection())
            {
                bookmarks.Add(new KeyValueIconTriple<Host, string>(host, BookmarkNameProvider.toString(host),
                    host.getProtocol().getProvider()));
            }
            View.PopulateBookmarks(bookmarks);
        }

        private void PopulateAndSelectEditor()
        {
            List<KeyValueIconTriple<Application, string>> editors = new List<KeyValueIconTriple<Application, string>>();

            Application defaultEditor = EditorFactory.instance().getDefaultEditor();
            String defaultEditorLocation = null;
            if (defaultEditor != null && Utils.IsNotBlank(defaultEditor.getIdentifier()))
            {
                defaultEditorLocation = defaultEditor.getIdentifier();
            }
            bool defaultEditorAdded = false;

            foreach (Application editor in Utils.ConvertFromJavaList<Application>(EditorFactory.instance().getEditors())
                )
            {
                if (ApplicationFinderFactory.get().isInstalled(editor))
                {
                    editors.Add(new KeyValueIconTriple<Application, string>(editor, editor.getName(), editor.getName()));
                    if (defaultEditorLocation != null && editor.getIdentifier().Equals(defaultEditorLocation))
                    {
                        defaultEditorAdded = true;
                    }
                }
            }
            if (!defaultEditorAdded)
            {
                if (defaultEditor != null && ApplicationFinderFactory.get().isInstalled(defaultEditor))
                {
                    editors.Insert(0,
                        new KeyValueIconTriple<Application, string>(defaultEditor, defaultEditor.getName(),
                            defaultEditor.getName()));
                }
            }
            editors.Add(new KeyValueIconTriple<Application, string>(new Application(null, null),
                LocaleFactory.localizedString("Choose") + "…", String.Empty));
            View.PopulateEditors(editors);
            if (defaultEditor != null)
            {
                View.DefaultEditor = defaultEditor;
            }
            else
            {
                //dummy editor which leads to an empty selection
                View.DefaultEditor = new Application(null, null);
            }
        }
    }
}