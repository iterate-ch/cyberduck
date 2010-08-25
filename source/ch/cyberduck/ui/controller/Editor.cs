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
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller.Threading;
using java.io;
using java.text;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class Editor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (Editor).Name);

        private readonly BrowserController controller;

        /// <summary>
        /// The editor application
        /// </summary>
        protected string BundleIdentifier;

        /// <summary>
        /// The file has been closed in the editor while the upload was in progress
        /// </summary>
        private Boolean deferredDelete;

        /// <summary>
        /// The edited path
        /// </summary>
        protected Path edited;

        public Editor(BrowserController controller, string bundleIdentifier, Path path)
        {
            this.controller = controller;
            BundleIdentifier = bundleIdentifier;
            edited = path;
            Local folder = LocalFactory.createLocal(
                new File(ch.cyberduck.core.Preferences.instance().getProperty("editor.tmp.directory"),
                         edited.getParent().getAbsolute()));
            edited.setLocal(LocalFactory.createLocal(folder, edited.getName()));
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>TransferAction for edited file</returns>
        protected TransferAction getAction()
        {
            return TransferAction.ACTION_OVERWRITE;
        }

        public void open()
        {
            Log.debug("open");
            controller.background(new OpenBackgroundAction(this, edited, controller));
        }

        protected void save()
        {
            Log.debug("save");
            controller.background(new SaveBackgroundAction(this, edited, controller));
        }

        protected abstract void edit();

        public virtual Boolean isOpen()
        {
            return false;
        }

        protected virtual void delete()
        {
            Log.debug("delete");
            edited.getLocal().delete(ch.cyberduck.core.Preferences.instance().getBoolean("editor.file.trash"));
        }

        protected virtual void setDeferredDelete(Boolean deferredDelete)
        {
            this.deferredDelete = deferredDelete;
        }

        public Boolean isDeferredDelete()
        {
            return deferredDelete;
        }

        private class OpenBackgroundAction : BrowserBackgroundAction
        {
            private readonly Path _edited;
            private readonly Editor _editor;

            public OpenBackgroundAction(Editor editor, Path edited, BrowserController controller) : base(controller)
            {
                _editor = editor;
                _edited = edited;
            }

            public override void run()
            {
                TransferOptions options = new TransferOptions {closeSession = false};
                Transfer download = new EditorDownloadTransfer(_editor, _edited);
                download.start(new OverwriteTransferPrompt(), options);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Downloading {0}", "Status"), _edited.getName());
            }

            public override void cleanup()
            {                
                if (_edited.status().isComplete())
                {
                    Permission permissions = _edited.getLocal().attributes().getPermission();
                    if (null != permissions)
                    {
                        permissions.getOwnerPermissions()[Permission.READ] = true;
                        permissions.getOwnerPermissions()[Permission.WRITE] = true;
                        _edited.getLocal().writeUnixPermission(permissions, false);
                    }
                    // Important, should always be run on the main thread; otherwise applescript crashes
                    _editor.edit();
                }
            }

            private class EditorDownloadTransfer : DownloadTransfer
            {
                private readonly Editor _editor;

                public EditorDownloadTransfer(Editor editor, Path root) : base(root)
                {
                    _editor = editor;
                }

                public override TransferAction action(bool resumeRequested, bool reloadRequested)
                {
                    return _editor.getAction();
                }

                protected override bool shouldOpenWhenComplete()
                {
                    return false;
                }
            }
        }

        private class OverwriteTransferPrompt : TransferPrompt
        {
            public TransferAction prompt()
            {
                return TransferAction.ACTION_OVERWRITE;
            }
        }

        private class SaveBackgroundAction : BrowserBackgroundAction
        {
            private readonly Path _edited;
            private readonly Editor _editor;

            public SaveBackgroundAction(Editor editor, Path edited, BrowserController controller) : base(controller)
            {
                _editor = editor;
                _edited = edited;
            }

            public override void run()
            {
                _edited.status().reset();
                TransferOptions options = new TransferOptions {closeSession = false};
                Transfer upload = new EditorUploadTransfer(_edited);
                upload.start(new OverwriteTransferPrompt(), options);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Uploading {0}", "Status"), _edited.getName());
            }

            public override void cleanup()
            {
                if (_edited.status().isComplete())
                {
                    if (_editor.isDeferredDelete())
                    {
                        _editor.delete();
                    }
                    _editor.setDeferredDelete(false);
                }
            }

            private class EditorUploadTransfer : UploadTransfer
            {
                public EditorUploadTransfer(Path root) : base(root)
                {
                    ;
                }

                public override TransferAction action(bool resumeRequested, bool reloadRequested)
                {
                    return TransferAction.ACTION_OVERWRITE;
                }
            }
        }
    }
}