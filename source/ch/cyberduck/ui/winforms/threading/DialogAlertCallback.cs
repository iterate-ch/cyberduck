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

using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.threading;
using java.lang;
using String = System.String;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogAlertCallback : AlertCallback
    {
        private readonly WindowController _controller;
        private RepeatableBackgroundAction _action;

        public DialogAlertCallback(WindowController controller)
        {
            _controller = controller;
        }

        public void alert(RepeatableBackgroundAction rba, BackgroundException failure, StringBuilder log)
        {
            _action = rba;
            _controller.Invoke(delegate
                {
                    string footer = Preferences.instance().getProperty("website.help");
                    if (null != failure.getPath())
                    {
                        footer = Preferences.instance().getProperty("website.help") + "/" +
                                 ((Session) _action.getSessions().iterator().next()).getHost().getProtocol().
                                                                                     getProvider();
                    }
                    DialogResult result =
                        _controller.WarningBox(failure.getReadableTitle(),
                                               failure.getMessage(),
                                               failure.getDetail(),
                                               log.length() > 0 ? log.toString() : null,
                                               String.Format("{0}", Locale.localizedString("Try Again", "Alert")),
                                               true, footer);
                    Callback(result);
                }, true);
        }

        private void Callback(DialogResult result)
        {
            if (DialogResult.OK == result)
            {
                // Re-run the action with the previous lock used
                _controller.background(_action);
            }
        }
    }
}