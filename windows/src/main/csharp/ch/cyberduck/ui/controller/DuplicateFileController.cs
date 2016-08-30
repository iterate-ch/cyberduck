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
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Winforms;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class DuplicateFileController : FileController
    {
        public DuplicateFileController(IPromptView view, BrowserController browserController)
            : base(view, browserController)
        {
            Path selected = browserController.SelectedPath;
            StringBuilder proposal = new StringBuilder();
            proposal.Append(System.IO.Path.GetFileNameWithoutExtension(selected.getName()));
            string shortDate = UserDefaultsDateFormatter.GetShortFormat(DateTime.Now)
                .Replace('/', '.')
                .Replace(':', '.');
            proposal.Append(" (").Append(shortDate).Append(")");
            if (!string.IsNullOrEmpty(selected.getExtension()))
            {
                proposal.Append(".").Append(selected.getExtension());
            }
            PrefilledText = proposal.ToString();
        }

        public override Bitmap IconView
        {
            get
            {
                return
                    IconCache.Instance.IconForPath(BrowserController.SelectedPath, IconCache.IconSize.Large);
            }
        }

        public override void Callback(DialogResult result)
        {
            if (!String.IsNullOrEmpty(View.InputText) &&
                !View.InputText.Trim().Equals(string.Empty))
            {
                if (DialogResult.OK == result)
                {
                    DuplicateFile(BrowserController.SelectedPath, View.InputText);
                }
            }
        }

        private void DuplicateFile(Path selected, String filename)
        {
            Path duplicate = new Path(selected.getParent(), filename, selected.getType());
            BrowserController.DuplicatePath(selected, duplicate);
        }
    }
}