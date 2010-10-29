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
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller.Threading;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class FolderController : FileController
    {
        public FolderController(IPromptView view, BrowserController browserController) : base(view, browserController)
        {
        }

        public override Bitmap IconView
        {
            get { return IconCache.Instance.IconForName("newfolder", 64); }
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result && !String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(String.Empty))
            {
                BrowserController.background(new CreateFolderAction(BrowserController, Workdir,
                                                                    View.InputText));
            }
        }

        private class CreateFolderAction : BrowserBackgroundAction
        {
            private readonly string _filename;
            private readonly Path _folder;
            private readonly Path _workdir;

            public CreateFolderAction(BrowserController controller, Path workdir, string filename)
                : base(controller)
            {
                _workdir = workdir;
                _filename = filename;
                _folder = PathFactory.createPath(getSession(), _workdir.getAbsolute(), _filename,
                                                 AbstractPath.DIRECTORY_TYPE);
            }

            public override void run()
            {
                _folder.mkdir(false);
            }

            public override void cleanup()
            {
                if (_filename.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.RefreshObject(_workdir,new List<TreePathReference>(){new TreePathReference(_folder)});
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Making directory {0}", "Status"), _folder.getName());
            }
        }
    }
}