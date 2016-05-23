// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui;

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
                        _controller.View.CommandBox(LocaleFactory.localizedString("Apply changes recursively"),
                                                    LocaleFactory.localizedString("Apply changes recursively"),
                                                    String.Format(LocaleFactory.localizedString("Do you want to set {0} on {1} recursively for all contained files?"),
                                                                                value, directory.getName()),
                                                    null, null, null,
                                                    LocaleFactory.localizedString("Always"),
                                                    LocaleFactory.localizedString("Continue", "Credentials")), true, SysIcons.Warning, SysIcons.Information,
                                                    delegate(int opt, bool verificationChecked)
                                                        {
                                                            if (opt == 1)
                                                            {
                                                                c.SetValue(true);
                                                            }

                                                            if (verificationChecked)
                                                            {
                                                                _supressed = true;
                                                                _option = c.Value;
                                                            }
                                                        });
                    }, true);
                return c.Value;
            }
            // Abort
            return false;
        }
    }
}