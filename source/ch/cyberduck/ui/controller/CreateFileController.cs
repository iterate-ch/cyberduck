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
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller.Threading;

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
            if (!String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(string.Empty))
            {
                if (DialogResult.OK == result)
                {
                    BrowserController.background(new CreateFileAction(BrowserController, Workdir, View.InputText, false));
                }
                if (DialogResult.Yes == result)
                {
                    BrowserController.background(new CreateFileAction(BrowserController, Workdir, View.InputText, true));
                }
            }
        }

        private class CreateFileAction : BrowserBackgroundAction
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
                _file = PathFactory.createPath(getSession(),
                                               _workdir.getAbsolute(),
                                               _filename, Path.FILE_TYPE);
            }

            public override void run()
            {
                _file.touch();                
                if (_file.exists())
                {
                    if (_edit)
                    {
                        Editor editor = EditorFactory.createEditor(BrowserController, _file);
                        editor.open();
                    }
                }
            }

            public override void cleanup()
            {
                if (_filename.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.RefreshObject(_workdir, new List<TreePathReference>(){new TreePathReference(_file)});
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Uploading {0}", "Status"), _file.getName());
            }

            private class OverwriteTransferPrompt : TransferPrompt
            {
                public TransferAction prompt()
                {
                    return TransferAction.ACTION_OVERWRITE;
                }
            }
        }
    }
}