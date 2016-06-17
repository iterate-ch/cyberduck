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
using ch.cyberduck.core;
using ch.cyberduck.core.worker;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogRecursiveCallback : Worker.RecursiveCallback
    {
        private readonly WindowController _controller;
        private bool _option;
        private bool _supressed;

        public DialogRecursiveCallback(WindowController controller)
        {
            _controller = controller;
        }

        public bool recurse(Path directory, Object value)
        {
            if (_supressed)
            {
                return _option;
            }
            if (_controller.Visible)
            {
                AtomicBoolean c = new AtomicBoolean(false);
                _controller.Invoke(delegate
                {
                    TaskDialogResult result =
                        _controller.View.CommandBox(LocaleFactory.localizedString("Apply changes recursively"),
                            LocaleFactory.localizedString("Apply changes recursively"),
                            String.Format(
                                LocaleFactory.localizedString(
                                    "Do you want to set {0} on {1} recursively for all contained files?"), value,
                                directory.getName()), null, null, LocaleFactory.localizedString("Always"),
                            LocaleFactory.localizedString("Continue", "Credentials"), true, TaskDialogIcon.Warning,
                            TaskDialogIcon.Information, delegate(int opt, bool verificationChecked)
                            {
                                if (verificationChecked)
                                {
                                    _supressed = true;
                                    _option = c.Value;
                                }
                            });
                    if (result.Result == TaskDialogSimpleResult.Cancel)
                    {
                        c.SetValue(false);
                    }
                }, true);
                return c.Value;
            }
            // Abort
            return false;
        }
    }
}