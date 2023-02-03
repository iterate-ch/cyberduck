// 
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
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
using System.Linq;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.browser;
using java.util;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class FolderController : FileController
    {
        private readonly IList<Location.Name> _regions;
        private readonly INewFolderPromptView _view;

        public FolderController(INewFolderPromptView view, BrowserController browserController,
            IList<Location.Name> regions, Location.Name defaultRegion) : base(view, browserController)
        {
            _view = view;
            _regions = regions;
            if (HasLocation())
            {
                view.RegionsEnabled = true;
                IList<KeyValuePair<string, string>> r = regions.OrderBy(name => name.ToString())
                    .Select(l =>
                    {
                        var key = l.getIdentifier();
                        var display = l.toString();
                        if (!string.Equals(key, display))
                        {
                            return new(key, string.Format("{0} - {1}", display, key));
                        }
                        return new KeyValuePair<string, string>(key, display);
                    }).ToList();
                view.PopulateRegions(r);

                if (regions.Contains(defaultRegion))
                {
                    view.Region = defaultRegion.getIdentifier();
                }
            }
        }

        public override Image IconView => Images.FolderPlus.Size(64);

        protected bool HasLocation()
        {
            return _regions.Count > 0 && new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath).isRoot();
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result && !String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(String.Empty))
            {
                BrowserController.background(new CreateDirectoryAction(BrowserController,
                    new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath), View.InputText.Trim(),
                    HasLocation() ? _view.Region : null));
            }
        }

        private class CreateDirectoryAction : WorkerBackgroundAction
        {
            public CreateDirectoryAction(BrowserController controller, Path directory, string filename, string region)
                : base(
                    controller, controller.Session,
                    new InnerCreateDirectoryWorker(controller,
                        new Path(directory, filename, EnumSet.of(AbstractPath.Type.directory)), filename, region))
            {
            }

            private class InnerCreateDirectoryWorker : CreateDirectoryWorker
            {
                private readonly BrowserController _controller;
                private readonly string _filename;
                private readonly IList<Path> _files;
                private readonly Path _folder;

                public InnerCreateDirectoryWorker(BrowserController controller, Path folder, String filename,
                    String region) : base(folder, region)
                {
                    _controller = controller;
                    _folder = folder;
                    _filename = filename;
                }

                public override void cleanup(object result)
                {
                    if (_filename.StartsWith("."))
                    {
                        _controller.ShowHiddenFiles = true;
                    }
                    List<Path> folders = new List<Path>() { _folder };
                    _controller.Reload(_controller.Workdir, folders, folders);
                }
            }
        }
    }
}
