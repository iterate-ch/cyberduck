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
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.core;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.TaskDialog;
using static Windows.Win32.UI.WindowsAndMessaging.MESSAGEBOX_RESULT;
using UiUtils = Ch.Cyberduck.Ui.Core.Utils;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogTransferErrorCallback : TransferErrorCallback
    {
        private readonly IWindowController _controller;
        private bool _option;
        private bool _supressed;

        public DialogTransferErrorCallback(IWindowController controller)
        {
            _controller = controller;
        }

        public bool prompt(TransferItem item, TransferStatus status, BackgroundException failure, int pending)
        {
            if (_supressed)
            {
                return _option;
            }
            if (_controller.Visible)
            {
                AtomicBoolean c = new AtomicBoolean(true);
                _controller.Invoke(delegate
                {
                    if (UiUtils.CommandBox(
                        owner: _controller.Window,
                        title: LocaleFactory.localizedString("Error"),
                        mainInstruction: failure.getMessage() ?? LocaleFactory.localizedString("Unknown"),
                        content: failure.getDetail() ?? LocaleFactory.localizedString("Unknown"),
                        expandedInfo: null,
                        help: null,
                        verificationText: LocaleFactory.localizedString("Always"),
                        commandButtons: LocaleFactory.localizedString("Continue", "Credentials"),
                        showCancelButton: true,
                        mainIcon: TaskDialogIcon.Warning,
                        footerIcon: TaskDialogIcon.Information,
                        handler: (opt, verificationChecked) =>
                        {
                            if (verificationChecked)
                            {
                                _supressed = true;
                                _option = c.Value;
                            }
                        }) is { Button: IDCANCEL })
                    {
                        c.SetValue(true);
                    }
                }, true);
                return c.Value;
            }
            // Abort
            return false;
        }
    }
}
