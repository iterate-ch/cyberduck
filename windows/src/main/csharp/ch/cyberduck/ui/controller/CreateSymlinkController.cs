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
using ch.cyberduck.core.features;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Ui.Controller.Threading;
using java.util;

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

        private class CreateSymlinkAction : BrowserControllerBackgroundAction
        {
            private readonly Path _link;
            private readonly string _symlink;
            private readonly string _target;
            private readonly Path _workdir;

            public CreateSymlinkAction(BrowserController controller, Path workdir, string symlink) : base(controller)
            {
                _workdir = workdir;
                _symlink = symlink;
                _link = new Path(_workdir, _symlink, EnumSet.of(AbstractPath.Type.file));
                if (
                    PreferencesFactory.get()
                        .getBoolean(String.Format("{0}.symlink.absolute",
                            BrowserController.Session.getHost().getProtocol().getScheme().name())))
                {
                    _target = BrowserController.SelectedPath.getAbsolute();
                }
                else
                {
                    _target = BrowserController.SelectedPath.getName();
                }
            }

            public override object run()
            {
                // Symlink pointing to existing file
                ((Symlink) BrowserController.Session.getFeature(typeof (Symlink))).symlink(_link, _target);
                return true;
            }

            public override void cleanup()
            {
                if (_symlink.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.Reload(BrowserController.Workdir, new List<Path> {_link}, new List<Path> {_link});
            }

            public override string getActivity()
            {
                return String.Format(LocaleFactory.localizedString("Uploading {0}", "Status"), _symlink);
            }
        }
    }
}