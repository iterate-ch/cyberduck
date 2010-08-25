// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using org.apache.log4j;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LoginController : AbstractLoginController
    {
        private static readonly Logger Log = Logger.getLogger(typeof (LoginController).Name);

        private ILoginView _view;
        private Protocol _protocol;
        private Credentials _credentials;

        public static void Register()
        {
            LoginControllerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private LoginController(ILoginView view)
        {
            _view = view;
            InitEventHandlers();
        }

        public ILoginView View
        {
            get { return _view; }
            set { _view = value; }
        }

        private void View_ChangedPkCheckboxEvent(object sender, EventArgs e)
        {
            //todo implement!
        }

        private void View_ChangedAnonymousCheckboxEvent(object sender, EventArgs e)
        {
            if (_view.AnonymousChecked)
            {
                _credentials.setUsername(
                    ch.cyberduck.core.Preferences.instance().getProperty("connection.login.anon.name"));
                _credentials.setPassword(
                    ch.cyberduck.core.Preferences.instance().getProperty("connection.login.anon.pass"));
            }
            else
            {
                _credentials.setUsername(
                    ch.cyberduck.core.Preferences.instance().getProperty("connection.login.name"));
                _credentials.setPassword(null);
            }
            _view.Username = _credentials.getUsername();
            _view.Password = _credentials.getPassword();
            Update();
        }

        private void View_ChangedSavePasswordCheckboxEvent(object sender, EventArgs e)
        {
            _credentials.setUseKeychain(_view.SavePasswordChecked);
        }

        private void View_ChangedPasswordEvent(object sender, EventArgs e)
        {
            _credentials.setPassword(_view.Password);
        }

        private void View_ChangedUsernameEvent(object sender, EventArgs e)
        {
            _credentials.setUsername(_view.Username);
            Update();
        }

        public override void warn(String title, String message, String defaultButton, String otherButton, String preference)
        {
            //todo implement, z.B. bei unsecure connection obwohl secure möglich wäre
            Log.debug("Warn called");
        }

        public override void prompt(Protocol protocol, Credentials credentials, string title, string reason)
        {
            prompt(protocol, credentials, title, reason, true, protocol.equals(Protocol.SFTP), true);
        }

        public override void prompt(Protocol protocol, Credentials credentials,
                                    String title, String reason,
                                    bool enableKeychain, bool enablePublicKey, bool enableAnonymous)
        {
            _protocol = protocol;
            _credentials = credentials;

            //todo add support for enableKeychain and enablePublicKey and enableAnonymous
            _view.Title = Locale.localizedString(title, "Credentials");
            _view.Message = Locale.localizedString(reason, "Credentials");
            _view.Username = credentials.getUsername();

            _view.UsernameLabel = protocol.getUsernamePlaceholder();
            _view.PasswordLabel = protocol.getPasswordPlaceholder();
            _view.SavePasswordChecked =
                ch.cyberduck.core.Preferences.instance().getBoolean("connection.login.useKeychain") &&
                ch.cyberduck.core.Preferences.instance().getBoolean("connection.login.addKeychain");

            Update();

            ShowViewDelegate showView = delegate
                                            {
                                                if (DialogResult.Cancel == ShowView())
                                                {
                                                    throw new LoginCanceledException();
                                                }
                                                credentials.setUsername(Utils.SafeString(_view.Username));
                                                credentials.setPassword(Utils.SafeString(_view.Password));
                                            };

            Form parent = MainController.Application.ActiveMainForm;
            lock (parent)
            {
                parent.Invoke(showView);
            }
        }

        public DialogResult ShowView()
        {
            return ((Form) _view).ShowDialog();
        }

        private void InitEventHandlers()
        {
            View.ChangedUsernameEvent += View_ChangedUsernameEvent;
            View.ChangedPasswordEvent += View_ChangedPasswordEvent;
            View.ChangedSavePasswordCheckboxEvent += View_ChangedSavePasswordCheckboxEvent;
            View.ChangedAnonymousCheckboxEvent += View_ChangedAnonymousCheckboxEvent;
            View.ChangedPkCheckboxEvent += View_ChangedPkCheckboxEvent;
        }

        private void Update()
        {
            _view.UsernameEnabled = !_credentials.isAnonymousLogin();
            _view.PasswordEnabled = !_credentials.isAnonymousLogin();
            _view.SavePasswordEnabled = !_credentials.isAnonymousLogin();
            _view.AnonymousChecked = _credentials.isAnonymousLogin();
            _view.PkCheckboxEnabled = _protocol.equals(Protocol.SFTP);
            if (_credentials.isPublicKeyAuthentication())
            {
                _view.PkCheckboxChecked = true;
                //todo
                //this.updateField(this.pkLabel, credentials.getIdentity().toURL());
            }
            else
            {
                _view.PkCheckboxChecked = false;
                //todo
                //this.pkLabel.setStringValue(Locale.localizedString("No Private Key selected"));
            }
        }

        private delegate void ShowViewDelegate();

        private class Factory : LoginControllerFactory
        {
            protected override object create()
            {
                return new LoginController(ObjectFactory.GetInstance<ILoginView>());
            }

            public override ch.cyberduck.core.LoginController create(Session s)
            {
                return new LoginController(ObjectFactory.GetInstance<ILoginView>());
            }

            public override ch.cyberduck.core.LoginController create(ch.cyberduck.ui.Controller c)
            {
                return new LoginController(ObjectFactory.GetInstance<ILoginView>());
            }
        }
    }
}