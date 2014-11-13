// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using System.Collections.ObjectModel;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.features;
using UploadTargetFinder = ch.cyberduck.ui.browser.UploadTargetFinder;
using DisabledApplicationQuitCallback = ch.cyberduck.core.local.DisabledApplicationQuitCallback;
using DisabledTransferErrorCallback = ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
using java.util;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class CreateFileController : FileController
    {
        public CreateFileController(IPromptView view, BrowserController browserController)
            : base(view, browserController)
        {
        }

        public override void Callback(DialogResult result)
        {
            if (!String.IsNullOrEmpty(View.InputText) && !View.InputText.Trim().Equals(string.Empty))
            {
                Path parent = new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath);
                if (DialogResult.OK == result)
                {
                    BrowserController.background(new CreateFileAction(BrowserController, parent, View.InputText, false));
                }
                if (DialogResult.Yes == result)
                {
                    BrowserController.background(new CreateFileAction(BrowserController, parent, View.InputText, true));
                }
            }
        }

        private class CreateFileAction : BrowserControllerBackgroundAction
        {
            private readonly bool _edit;
            private readonly Path _file;
            private readonly string _filename;
            private readonly Path _workdir;

            public CreateFileAction(BrowserController controller, Path workdir, string filename, bool edit)
                : base(controller)
            {
                _workdir = workdir;
                _filename = filename;
                _edit = edit;
                _file = new Path(_workdir, _filename, EnumSet.of(AbstractPath.Type.file));
            }

            public override object run()
            {
                Session session = BrowserController.Session;
                Touch feature = (Touch) session.getFeature(typeof (Touch));
                feature.touch(_file);
                if (_edit)
                {
                    _file.attributes().setSize(0L);
                    Editor editor = EditorFactory.instance().create(BrowserController, BrowserController.Session, _file);
                    editor.open(new DisabledApplicationQuitCallback(), new DisabledTransferErrorCallback());
                }
                return true;
            }

            public override void cleanup()
            {
                base.cleanup();
                if (_filename.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.RefreshParentPaths(new Collection<Path> {_file}, new Collection<Path> {_file});
            }

            public override string getActivity()
            {
                return String.Format(LocaleFactory.localizedString("Uploading {0}", "Status"), _file.getName());
            }
        }
    }
}