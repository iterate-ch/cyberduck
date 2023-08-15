// 
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
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

using System.Collections.Generic;
using ch.cyberduck.core;
using ch.cyberduck.core.preferences;
using ch.cyberduck.ui;
using Ch.Cyberduck.Core;
using java.lang;
using org.apache.logging.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class ConnectionController : BookmarkController<IConnectionView>
    {
        private static readonly IDictionary<WindowController, ConnectionController> Controllers =
            new Dictionary<WindowController, ConnectionController>();

        private static readonly Logger Log = LogManager.getLogger(typeof(ConnectionController).FullName);

        private ConnectionController(Host bookmark) : this(bookmark,
            new LoginOptions(bookmark.getProtocol()))
        {
        }

        private ConnectionController(Host bookmark, LoginOptions options) : this(bookmark,
            new LoginInputValidator(bookmark, options), options)
        {
        }

        private ConnectionController(Host bookmark,
            LoginInputValidator validator, LoginOptions options) : base(bookmark, validator, options)
        {
            View.SavePasswordEnabled = _options.keychain();
            View.SavePasswordChecked = bookmark.getCredentials().isSaved();
            View.ChangedSavePasswordCheckboxEvent += View_ChangedSavePasswordCheckboxEvent;
            View.ChangedPasswordEvent += delegate { _host.getCredentials().setPassword(View.Password); };
        }

        public override bool Singleton => true;

        public static ConnectionController Instance(WindowController parent)
        {
            ConnectionController c;
            if (!Controllers.TryGetValue(parent, out c))
            {
                c = new ConnectionController(new Host(ProtocolFactory.get().forName(PreferencesFactory.get().getProperty("connection.protocol.default"))));
                Controllers.Add(parent, c);
                parent.View.ViewClosedEvent += delegate
                {
                    Controllers.Remove(parent);
                    c.View.Close();
                };
            }
            return c;
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
            _host.getCredentials().setSaved(View.SavePasswordChecked);
        }

        protected override void ItemChanged()
        {
            //
        }
    }
}
