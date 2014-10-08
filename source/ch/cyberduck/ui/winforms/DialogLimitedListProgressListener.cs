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

using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using java.lang;

namespace Ch.Cyberduck.Ui.Winforms
{
    internal class DialogLimitedListProgressListener : LimitedListProgressListener
    {
        private readonly WindowController _controller;
        private bool _supressed;

        public DialogLimitedListProgressListener(WindowController controller)
            : base(controller)
        {
            _controller = controller;
        }

        public override void chunk(Path parent, AttributedList list)
        {
            if (_supressed)
            {
                return;
            }
            try
            {
                base.chunk(parent, list);
            }
            catch (ListCanceledException e)
            {
                if (_controller.Visible)
                {
                    AtomicBoolean c = new AtomicBoolean(true);
                    AsyncController.AsyncDelegate d = delegate
                        {
                            _controller.CommandBox(
                                string.Format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                                              string.Empty),
                                string.Format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                                              string.Empty),
                                string.Format(
                                    LocaleFactory.localizedString(
                                        "Continue listing directory with more than {0} files.", "Alert"),
                                    e.getChunk().size()),
                                string.Format("{0}|{1}", LocaleFactory.localizedString("Continue", "Credentials"),
                                              LocaleFactory.localizedString("Cancel")),
                                false,
                                LocaleFactory.localizedString("Always"),
                                SysIcons.Warning,
                                delegate(int option, bool verificationChecked)
                                    {
                                        if (option == 1)
                                        {
                                            c.SetValue(false);
                                        }
                                        if (verificationChecked)
                                        {
                                            _supressed = true;
                                        }
                                    });
                        };
                    _controller.Invoke(d, true);
                    if (!c.Value)
                    {
                        throw e;
                    }
                }
            }
        }
    }
}