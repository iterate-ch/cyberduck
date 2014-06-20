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

using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.ui.growl;

namespace Ch.Cyberduck.Ui.Growl
{
    internal class NotifyImpl : ch.cyberduck.ui.growl.Growl
    {
        private readonly NotifyIcon _icon = new NotifyIcon();

        public void notify(string title, string description)
        {
            _icon.Visible = true;
            _icon.ShowBalloonTip(300, title, description, ToolTipIcon.Info);
        }

        public void setup()
        {
            _icon.Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
        }

        public void unregister()
        {
            _icon.Dispose();
        }

        public void notifyWithImage(string title, string description, string image)
        {
            notify(title, description);
        }

        public static void Register()
        {
            GrowlFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : GrowlFactory
        {
            protected override object create()
            {
                return new NotifyImpl();
            }
        }
    }
}