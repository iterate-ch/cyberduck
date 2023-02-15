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

using System.Drawing;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core;
using java.util;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class FileController : WindowController<IPromptView>
    {
        protected readonly BrowserController BrowserController;
        private string _input = string.Empty;

        protected FileController(IPromptView view, BrowserController browserController)
        {
            BrowserController = browserController;
            View = view;

            View.ValidateInput += ValidateInput;
        }

        public virtual Image IconView
        {
            get { return IconProvider.GetFileIcon("_unknown", false, true, false); }
        }

        public virtual Path Workdir
        {
            get
            {
                if (BrowserController.SelectedPaths.Count == 1)
                {
                    return BrowserController.SelectedPath.getParent();
                }
                return BrowserController.Workdir;
            }
        }

        protected string PrefilledText
        {
            get { return _input; }
            set { _input = value; }
        }

        protected SessionPool Session
        {
            get { return BrowserController.Session; }
        }

        protected virtual bool ValidateInput()
        {
            string t = View.InputText.Trim();
            foreach (string f in Utils.ConvertFromJavaList<string>(PreferencesFactory.get().getList("browser.filter.regex")))
            {
                if (Regex.IsMatch(t, f))
                {
                    return false;
                }
            }
            if (!string.IsNullOrEmpty(t))
            {
                if (
                    BrowserController.Cache.get(BrowserController.Workdir)
                        .contains(new Path(BrowserController.Workdir, t, EnumSet.of(AbstractPath.Type.file))))
                {
                    return false;
                }
                if (
                    BrowserController.Cache.get(BrowserController.Workdir)
                        .contains(new Path(BrowserController.Workdir, t, EnumSet.of(AbstractPath.Type.directory))))
                {
                    return false;
                }
                return true;
            }
            return false;
        }

        public abstract void Callback(DialogResult result);

        public void Show()
        {
            View.InputText = PrefilledText;
            View.IconView = IconView;
            Callback(View.ShowDialog(BrowserController.View));
        }
    }
}
