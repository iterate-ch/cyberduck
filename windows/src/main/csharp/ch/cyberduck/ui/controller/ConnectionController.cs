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
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class ConnectionController : BookmarkController<IConnectionView>
    {
        private static readonly IDictionary<WindowController, ConnectionController> Controllers =
            new Dictionary<WindowController, ConnectionController>();

        private static readonly Logger Log = Logger.getLogger(typeof(ConnectionController).FullName);

        private ConnectionController(Host bookmark) : this(bookmark, bookmark.getCredentials())
        {
        }

        private ConnectionController(Host bookmark, Credentials credentials) : this(bookmark, credentials,
            new LoginOptions(bookmark.getProtocol()))
        {
        }

        private ConnectionController(Host bookmark, Credentials credentials, LoginOptions options) : this(bookmark,
            credentials,
            new LoginInputValidator(credentials, bookmark.getProtocol(), options), options)
        {
        }

        private ConnectionController(Host bookmark, Credentials credentials,
            LoginInputValidator validator, LoginOptions options) : base(bookmark, credentials, validator, options)
        {
            Init();
        }

        public override bool Singleton => true;

        protected override string ToggleProperty => "connection.toggle.options";

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

        private void Init()
        {
            View.Username = PreferencesFactory.get().getProperty("connection.login.name");
            View.SavePasswordChecked = _options.save();
            View.ChangedSavePasswordCheckboxEvent += View_ChangedSavePasswordCheckboxEvent;

            View.ChangedServerEvent += ReadPasswordFromKeychain;
            View.ChangedUsernameEvent += ReadPasswordFromKeychain;
            View.ChangedProtocolEvent += ReadPasswordFromKeychain;
            View.ChangedPasswordEvent += delegate { _credentials.setPassword(View.Password); };
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
            //
        }

        protected override void ItemChanged()
        {
            //
        }

        protected override void Update()
        {
            base.Update();
            View.Password = _credentials.getPassword();
            View.PasswordEnabled = _options.password() && !_credentials.isAnonymousLogin();
        }

        public void ReadPasswordFromKeychain()
        {
            if (_options.keychain())
            {
                if (string.IsNullOrEmpty(View.Hostname))
                {
                    return;
                }
                if (string.IsNullOrEmpty(View.Port))
                {
                    return;
                }
                if (string.IsNullOrEmpty(View.Username))
                {
                    return;
                }
                Protocol protocol = View.SelectedProtocol;
                string password = PasswordStoreFactory.get().getPassword(protocol.getScheme(),
                    Integer.parseInt(View.Port), View.Hostname,
                    View.Username);
                if (Utils.IsNotBlank(password))
                {
                    View.Password = password;
                }
            }
        }
    }
}
