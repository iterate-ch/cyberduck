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
using ch.cyberduck.core.threading;
using java.lang;
using String = System.String;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogAlertCallback : AlertCallback
    {
        private readonly WindowController _controller;

        public DialogAlertCallback(WindowController controller)
        {
            _controller = controller;
        }

        public bool alert(SessionBackgroundAction rba, BackgroundException failure, StringBuilder log)
        {
            bool r = false;
            _controller.Invoke(delegate
                {
                    String provider = rba.getSession().getHost().getProtocol().getProvider();
                    string footer = String.Format("{0}/{1}", Preferences.instance().getProperty("website.help"),
                                                  provider);
                    DialogResult result = _controller.WarningBox(LocaleFactory.localizedString("Error"),
                                                                 failure.getMessage() ??
                                                                 LocaleFactory.localizedString("Unknown"),
                                                                 failure.getDetail() ??
                                                                 LocaleFactory.localizedString("Unknown"),
                                                                 log.length() > 0 ? log.toString() : null,
                                                                 String.Format("{0}",
                                                                               LocaleFactory.localizedString(
                                                                                   "Try Again", "Alert")), true, footer);
                    r = DialogResult.OK == result;
                }, true);
            return r;
        }
    }
}