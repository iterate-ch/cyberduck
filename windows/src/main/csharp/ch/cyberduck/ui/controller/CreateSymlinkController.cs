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
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using Ch.Cyberduck.Core.Resources;
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

        private class CreateSymlinkAction : WorkerBackgroundAction
        {
            public CreateSymlinkAction(BrowserController controller, Path workdir, string symlink) 
                : base(
                      controller, controller.Session,
                      new InnerCreateSymlinkWorker(controller,
                          controller.SelectedPath,
                          new Path(workdir, symlink, EnumSet.of(AbstractPath.Type.file))))
            {
            }

            private class InnerCreateSymlinkWorker : CreateSymlinkWorker
            {
                private readonly BrowserController _controller;
                private readonly string _filename;
                private readonly IList<Path> _files;
                private readonly Path _symlink;

                public InnerCreateSymlinkWorker(BrowserController controller, Path selected, Path symlink) : base(symlink, selected)
                {
                    _controller = controller;
                    _symlink = symlink;
                }

                public override void cleanup(object result)
                {
                    if (_symlink.getName().StartsWith("."))
                    {
                        _controller.ShowHiddenFiles = true;
                    }
                    _controller.Reload(_controller.Workdir, new List<Path> { _symlink }, new List<Path> { _symlink });
                }
            }
        }
    }
}