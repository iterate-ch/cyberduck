// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using Ch.Cyberduck.Ui.Controller.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.ui.browser;
using java.util;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class FolderController : FileController
    {
        private readonly IList<Location.Name> _regions;
        private readonly INewFolderPromptView _view;

        public FolderController(INewFolderPromptView view, BrowserController browserController,
                                IList<Location.Name> regions) : base(view, browserController)
        {
            _view = view;
            _regions = regions;
            if (HasLocation())
            {
                view.RegionsEnabled = true;
                IList<KeyValuePair<string, string>> r = new List<KeyValuePair<string, string>>();
                foreach (Location.Name region in regions)
                {
                    r.Add(new KeyValuePair<string, string>(region.getIdentifier(), region.toString()));
                }
                view.PopulateRegions(r);
            }
        }

        public override Bitmap IconView
        {
            get { return IconCache.Instance.IconForName("newfolder", 64); }
        }

        private bool HasLocation()
        {
            return _regions.Count > 0 && new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath).isRoot();
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result && !String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(String.Empty))
            {
                BrowserController.background(new CreateFolderAction(BrowserController,
                                                                    new UploadTargetFinder(Workdir).find(
                                                                        BrowserController.SelectedPath), View.InputText,
                                                                    HasLocation() ? _view.Region : null));
            }
        }

        private class CreateFolderAction : BrowserControllerBackgroundAction
        {
            private readonly string _filename;
            private readonly Path _folder;
            private readonly string _region;
            private readonly Path _workdir;

            public CreateFolderAction(BrowserController controller, Path workdir, string filename, string region)
                : base(controller)
            {
                _workdir = workdir;
                _filename = filename;
                _region = region;
                _folder = new Path(_workdir, _filename, EnumSet.of(AbstractPath.Type.directory));
            }

            public override object run()
            {
                Directory feature = (Directory) BrowserController.Session.getFeature(typeof (Directory));
                feature.mkdir(_folder, _region);
                return true;
            }

            public override void cleanup()
            {
                base.cleanup();
                if (_filename.StartsWith("."))
                {
                    BrowserController.ShowHiddenFiles = true;
                }
                BrowserController.RefreshParentPath(_folder);
            }

            public override string getActivity()
            {
                return String.Format(LocaleFactory.localizedString("Making directory {0}", "Status"), _folder.getName());
            }
        }
    }
}