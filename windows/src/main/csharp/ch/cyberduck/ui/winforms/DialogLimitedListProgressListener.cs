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

using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public class DialogLimitedListProgressListener : LimitedListProgressListener
    {
        private readonly WindowController _controller;
        private bool _supressed;

        public DialogLimitedListProgressListener(WindowController controller) : base(controller)
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
                            string.Format(LocaleFactory.localizedString("Listing directory {0}", "Status"), string.Empty),
                            string.Format(LocaleFactory.localizedString("Listing directory {0}", "Status"), string.Empty),
                            string.Format(
                                LocaleFactory.localizedString("Continue listing directory with more than {0} files.",
                                    "Alert"), e.getChunk().size()),
                            string.Format("{0}|{1}", LocaleFactory.localizedString("Continue", "Credentials"),
                                LocaleFactory.localizedString("Cancel")), false, LocaleFactory.localizedString("Always"),
                            TaskDialogIcon.Warning, delegate(int option, bool verificationChecked)
                            {
                                if (option == 0)
                                {
                                    _supressed = true;
                                }
                                if (option == 1)
                                {
                                    c.SetValue(false);
                                }
                                if (verificationChecked)
                                {
                                    _supressed = true;
                                    disable();
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