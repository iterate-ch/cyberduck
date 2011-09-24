// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using Ch.Cyberduck.Core;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class GotoController : FileController
    {
        public GotoController(IPromptView view, BrowserController browserController) : base(view, browserController)
        {
            PrefilledText = browserController.Workdir.getAbsolute();
        }

        public override Bitmap IconView
        {
            get { return IconCache.Instance.IconForFolder(IconCache.IconSize.Large); }
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result && !String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(String.Empty))
            {
                GotoFolder(BrowserController.Workdir, View.InputText);
            }
        }

        protected override bool ValidateInput()
        {
            return Utils.IsNotBlank(View.InputText);
        }

        private void GotoFolder(Path workdir, String filename)
        {
            Path dir;
            if (!filename.StartsWith(Path.DELIMITER.ToString()))
            {
                dir = PathFactory.createPath(BrowserController.getSession(), workdir.getAbsolute(), filename,
                                             AbstractPath.DIRECTORY_TYPE);
            }
            else
            {
                dir = PathFactory.createPath(BrowserController.getSession(), filename, AbstractPath.DIRECTORY_TYPE);
            }
            if (workdir.getParent().Equals(dir))
            {
                BrowserController.SetWorkdir(dir, workdir);
            }
            else
            {
                BrowserController.SetWorkdir(dir);
            }
        }
    }
}