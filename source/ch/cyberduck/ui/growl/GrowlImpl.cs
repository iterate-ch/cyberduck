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
using Ch.Cyberduck.Ui.Controller;
using Growl.Connector;
using ch.cyberduck.core;
using ch.cyberduck.ui.growl;

namespace Ch.Cyberduck.Ui.Growl
{
    internal class GrowlImpl : ch.cyberduck.ui.growl.Growl
    {
        private readonly Application _app = new Application(Preferences.instance().getProperty("application.name"))
            {
                Icon = IconCache.Instance.IconForName("cyberduck", 48)
            };

        private readonly GrowlConnector _connector = new GrowlConnector();

        public void notify(string title, string description)
        {
            _connector.Notify(new Notification(_app.Name, title, null, title, description));
        }

        public void setup()
        {
            _connector.Register(_app,
                                new[]
                                    {
                                        new NotificationType("Download complete"), new NotificationType("Upload complete"),
                                        new NotificationType("Synchronization complete"),
                                        new NotificationType("Connection opened"),
                                        new NotificationType("Connection failed"),
                                        new NotificationType("Download failed"), new NotificationType("Upload failed"),
                                        new NotificationType("Transfer queued"),
                                        new NotificationType("Bonjour", "Bonjour",
                                                             IconCache.Instance.IconForName("rendezvous"), true)
                                    });
        }

        public void unregister()
        {
            //
        }

        public void notifyWithImage(string title, string description, string image)
        {
            Bitmap icon = (Bitmap) ResourcesBundle.ResourceManager.GetObject(image, ResourcesBundle.Culture);
            _connector.Notify(new Notification(_app.Name, title, null, title, description, icon, false, Priority.Normal,
                                               null));
        }

        public static void Register()
        {
            GrowlFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private class Factory : GrowlFactory
        {
            protected override object create()
            {
                return new GrowlImpl();
            }
        }
    }
}