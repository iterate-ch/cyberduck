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

using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.core;
using Ch.Cyberduck.Core.TaskDialog;
using java.lang;
using UiUtils = Ch.Cyberduck.Ui.Core.Utils;

namespace Ch.Cyberduck.Ui.Winforms.Threading
{
    public class DialogAlertCallback : AlertCallback
    {
        private readonly IWindowController _controller;
        private readonly FailureDiagnostics _diagnostics = new DefaultFailureDiagnostics();
        private readonly NotificationAlertCallback _notification = new NotificationAlertCallback();

        public DialogAlertCallback(IWindowController controller)
        {
            _controller = controller;
        }

        public bool alert(Host host, BackgroundException failure)
        {
            return alert(host, failure, new StringBuilder());
        }

        public bool alert(Host host, BackgroundException failure, StringBuilder log)
        {
            FailureDiagnostics.Type type = _diagnostics.determine(failure);
            if (type == FailureDiagnostics.Type.cancel)
            {
                return false;
            }
            if (type == FailureDiagnostics.Type.skip)
            {
                return false;
            }
            _notification.alert(host, failure, log);
            bool r = false;
            _controller.Invoke(delegate
                {
                    string footer = ProviderHelpServiceFactory.get().help(host.getProtocol());
                    string title = BookmarkNameProvider.toString(host);
                    string message = failure.getMessage() ?? LocaleFactory.localizedString("Unknown");
                    string detail = failure.getDetail() ?? LocaleFactory.localizedString("Unknown");
                    string expanded = log.length() > 0 ? log.toString() : null;
                    string commandButtons;
                    if (type == FailureDiagnostics.Type.network)
                    {
                        commandButtons = string.Format("{0}|{1}", LocaleFactory.localizedString("Try Again", "Alert"),
                                                       LocaleFactory.localizedString("Network Diagnostics", "Alert"));
                    }
                    else if (type == FailureDiagnostics.Type.quota)
                    {
                        commandButtons = string.Format("{0}|{1}", LocaleFactory.localizedString("Try Again", "Alert"),
                                                       LocaleFactory.localizedString("Help", "Main"));
                    }
                    else
                    {
                        commandButtons = string.Format("{0}", LocaleFactory.localizedString("Try Again", "Alert"));
                    }

                    UiUtils.CommandBox(
                        owner: _controller.Window,
                        title: title,
                        mainInstruction: message,
                        content: detail,
                        expandedInfo: expanded,
                        help: footer,
                        verificationText: null,
                        commandButtons: commandButtons,
                        showCancelButton: true,
                        mainIcon: TaskDialogIcon.Warning,
                        footerIcon: TaskDialogIcon.Information,
                        handler: (option, @checked) =>
                        {
                            switch (option)
                            {
                                case 0:
                                    r = true;
                                    break;
                                case 1:
                                    if (type == FailureDiagnostics.Type.network)
                                    {
                                        ReachabilityDiagnosticsFactory.get().diagnose(host);
                                    }
                                    if (type == FailureDiagnostics.Type.quota)
                                    {
                                        BrowserLauncherFactory.get().open(new DefaultProviderHelpService().help(host.getProtocol()));
                                    }
                                    r = false;
                                    break;
                            }
                        });
                }, true);
            return r;
        }
    }
}
