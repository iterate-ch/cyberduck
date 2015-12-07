// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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

using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.notification;
using java.lang;
using String = System.String;
using PreferencesFactory = ch.cyberduck.core.preferences.PreferencesFactory;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogAlertCallback : AlertCallback
    {
        private readonly WindowController _controller;
        private readonly FailureDiagnostics _diagnostics = new DefaultFailureDiagnostics();
        private readonly NotificationAlertCallback _notification = new NotificationAlertCallback();

        public DialogAlertCallback(WindowController controller)
        {
            _controller = controller;
        }

        public bool alert(Host host, BackgroundException failure, StringBuilder log)
        {
            _notification.alert(host, failure, log);
            bool r = false;
            _controller.Invoke(delegate
                {
                    String provider = host.getProtocol().getProvider();
                    string footer = String.Format("{0}/{1}", PreferencesFactory.get().getProperty("website.help"),
                                                  provider);
                    string title = LocaleFactory.localizedString("Error");
                    string message = failure.getMessage() ?? LocaleFactory.localizedString("Unknown");
                    string detail = failure.getDetail() ?? LocaleFactory.localizedString("Unknown");
                    string expanded = log.length() > 0 ? log.toString() : null;
                    string commandButtons;
                    if (_diagnostics.determine(failure) == FailureDiagnostics.Type.network)
                    {
                        commandButtons = String.Format("{0}|{1}", LocaleFactory.localizedString("Try Again", "Alert"),
                                                       LocaleFactory.localizedString("Network Diagnostics", "Alert"));
                    }
                    else
                    {
                        commandButtons = String.Format("{0}", LocaleFactory.localizedString("Try Again", "Alert"));
                    }
                    _controller.WarningBox(title, message, detail, expanded, commandButtons, true, footer,
                                           delegate(int option, bool @checked)
                                               {
                                                   switch (option)
                                                   {
                                                       case 0:
                                                           r = true;
                                                           break;
                                                       case 1:
                                                           ReachabilityFactory.get().diagnose(host);
                                                           r = false;
                                                           break;
                                                   }
                                               });
                }, true);
            return r;
        }
    }
}