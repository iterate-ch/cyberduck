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
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.browser;
using java.util;
using Boolean = java.lang.Boolean;

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

        private class CreateFileAction : WorkerBackgroundAction
        {
            public CreateFileAction(BrowserController controller, Path directory, string filename, bool edit)
                : base(
                    controller, controller.Session,
                    new InnerCreateFileWorker(controller,
                        new Path(directory, filename, EnumSet.of(AbstractPath.Type.file)), filename, edit))
            {
            }

            private class InnerCreateFileWorker : TouchWorker
            {
                private readonly BrowserController _controller;
                private readonly bool _edit;
                private readonly Path _file;
                private readonly string _filename;
                private readonly IList<Path> _files;

                public InnerCreateFileWorker(BrowserController controller, Path file, String filename, bool edit)
                    : base(file)
                {
                    _controller = controller;
                    _file = file;
                    _filename = filename;
                    _edit = edit;
                }

                public override void cleanup(object result)
                {
                    if (_filename.StartsWith("."))
                    {
                        _controller.ShowHiddenFiles = true;
                    }
                    List<Path> files = new List<Path>() {_file};
                    _controller.Reload(_controller.Workdir, files, files);
                    if (_edit)
                    {
                        _file.attributes().setSize(0L);
                        _controller.edit(_file);
                    }
                }
            }
        }
    }
}