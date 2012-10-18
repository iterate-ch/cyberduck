// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.i18n;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class CreateSymlinkController : FileController
    {
        public CreateSymlinkController(ICreateSymlinkPromptView view, BrowserController browserController)
            : base(view, browserController)
        {
            view.LinkForFile = browserController.SelectedPath.getName();
        }

        public override Bitmap IconView
        {
            get { return IconCache.Instance.OverlayIcon("_unknown", "aliasbadge", IconCache.IconSize.Large); }
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result && !String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(string.Empty))
            {
                BrowserController.background(new CreateSymlinkAction(BrowserController, Workdir, View.InputText));
            }
        }

        private class CreateSymlinkAction : BrowserBackgroundAction
        {
            private readonly Path _link;
            private readonly string _symlink;
            private readonly string _target;
            private readonly Path _workdir;

            public CreateSymlinkAction(BrowserController controller, Path workdir, string symlink)
                : base(controller)
            {
                _workdir = workdir;
                _symlink = symlink;
                _link = PathFactory.createPath(controller.getSession(),
                                               _workdir.getAbsolute(),
                                               _symlink, AbstractPath.FILE_TYPE);
                _target = BrowserController.SelectedPath.getName();
            }

            public override void run()
            {
                // Symlink pointing to existing file
                _link.symlink(_target);
            }

            public override void cleanup()
            {
                if (_symlink.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.RefreshParentPath(_link);
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Uploading {0}", "Status"), _symlink);
            }
        }
    }
}