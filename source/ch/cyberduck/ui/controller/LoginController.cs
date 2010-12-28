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
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using org.apache.commons.lang;
using org.apache.log4j;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class LoginController : AbstractLoginController
    {
        private static readonly Logger Log = Logger.getLogger(typeof (LoginController).FullName);
        private readonly WindowController _browser;
        private Credentials _credentials;
        private bool _enableAnonymous;

        private bool _enableKeychain;
        private bool _enablePublicKey;
        private Protocol _protocol;
        private ILoginView _view;

        private LoginController(WindowController c)
        {
            _view = ObjectFactory.GetInstance<ILoginView>();
            _browser = c;
            InitEventHandlers();
        }

        public ILoginView View
        {
            get { return _view; }
            set { _view = value; }
        }

        public static void Register()
        {
            LoginControllerFactory.addFactory(ch.cyberduck.core.Factory.NATIVE_PLATFORM, new Factory());
        }

        private void View_ChangedPkCheckboxEvent()
        {
            if (View.PkCheckboxState)
            {
                string selectedKeyFile = UserPreferences.HomeFolder;
                if (null != _credentials.getIdentity())
                {
                    selectedKeyFile = _credentials.getIdentity().getAbsolute();
                }

                View.ShowPrivateKeyBrowser(selectedKeyFile);
            }
            else
            {
                View_ChangedPrivateKey(this, new PrivateKeyArgs(null));
            }
        }

        private void View_ChangedAnonymousCheckboxEvent()
        {
            if (_view.AnonymousState)
            {
                _credentials.setUsername(Preferences.instance().getProperty("connection.login.anon.name"));
                _credentials.setPassword(Preferences.instance().getProperty("connection.login.anon.pass"));
            }
            else
            {
                _credentials.setUsername(
                    Preferences.instance().getProperty("connection.login.name"));
                _credentials.setPassword(null);
            }
            _view.Username = _credentials.getUsername();
            _view.Password = _credentials.getPassword();
            Update();
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
            Preferences.instance().setProperty("connection.login.addKeychain", _view.SavePasswordState);
        }

        private void View_ChangedPasswordEvent()
        {
            _credentials.setPassword(_view.Password);
        }

        private void View_ChangedUsernameEvent()
        {
            _credentials.setUsername(_view.Username);
            if (StringUtils.isNotBlank(_credentials.getUsername()))
            {
                String password = KeychainFactory.instance().getPassword(_protocol.getScheme(),
                                                                         _protocol.getDefaultPort(),
                                                                         _protocol.getDefaultHostname(),
                                                                         _credentials.getUsername());
                if (StringUtils.isNotBlank(password))
                {
                    _view.Password = password;
                }
            }
            Update();
        }

        public override void warn(String title, String message, String continueButton, String disconnectButton,
                                  String preference)
        {
            AsyncController.AsyncDelegate d = delegate
                                                  {
                                                      _browser.CommandBox(title,
                                                                          title,
                                                                          message,
                                                                          String.Format("{0}|{1}",
                                                                                        continueButton,
                                                                                        disconnectButton),
                                                                          false, 
                                                                          Locale.localizedString("Don't show again", "Credentials"),
                                                                          SysIcons.Question,
                                                                          Preferences.instance().getProperty("website.help") + "/" + Protocol.FTP.getIdentifier(),
                                                                          delegate(int option, Boolean verificationChecked)
                                                                              {
                                                                                  if (verificationChecked)
                                                                                  {
                                                                                      // Never show again.
                                                                                      Preferences.instance().setProperty(preference, true);
                                                                                  }
                                                                                  switch (option)
                                                                                  {
                                                                                      case 1:
                                                                                          throw new LoginCanceledException();
                                                                                  }
                                                                              });
                                                  };
            _browser.Invoke(d);
            //Proceed nevertheless.
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
            _enableKeychain = enableKeychain;
            _enablePublicKey = enablePublicKey;
            _enableAnonymous = enableAnonymous;

            _view.Title = Locale.localizedString(title, "Credentials");
            _view.Message = Locale.localizedString(reason, "Credentials");
            _view.Username = credentials.getUsername();
            _view.SavePasswordState = Preferences.instance().getBoolean("connection.login.useKeychain")
                                      && Preferences.instance().getBoolean("connection.login.addKeychain");

            _view.UsernameLabel = protocol.getUsernamePlaceholder();
            _view.PasswordLabel = protocol.getPasswordPlaceholder();
            _view.SavePasswordState =
                Preferences.instance().getBoolean("connection.login.useKeychain") &&
                Preferences.instance().getBoolean("connection.login.addKeychain");
            _view.DiskIcon = IconCache.Instance.IconForName(_protocol.disk(), 64);

            Update();

            AsyncController.AsyncDelegate d = delegate
                                                  {
                                                      if (DialogResult.Cancel == _view.ShowDialog(_browser.View))
                                                      {
                                                          throw new LoginCanceledException();
                                                      }
                                                      credentials.setUseKeychain(_view.SavePasswordState);
                                                      credentials.setUsername(Utils.SafeString(_view.Username));
                                                      credentials.setPassword(Utils.SafeString(_view.Password));
                                                  };
            _browser.Invoke(d);
        }

        private void InitEventHandlers()
        {
            View.ChangedUsernameEvent += View_ChangedUsernameEvent;
            View.ChangedPasswordEvent += View_ChangedPasswordEvent;
            View.ChangedSavePasswordCheckboxEvent += View_ChangedSavePasswordCheckboxEvent;
            View.ChangedAnonymousCheckboxEvent += View_ChangedAnonymousCheckboxEvent;
            View.ChangedPkCheckboxEvent += View_ChangedPkCheckboxEvent;
            View.ChangedPrivateKey += View_ChangedPrivateKey;
            View.ValidateInput += View_ValidateInput;
        }

        private bool View_ValidateInput()
        {
            return _credentials.validate(_protocol);
        }

        private void View_ChangedPrivateKey(object sender, PrivateKeyArgs e)
        {
            _credentials.setIdentity(null == e.KeyFile ? null : LocalFactory.createLocal(e.KeyFile));
            Update();
        }

        private void Update()
        {
            _view.UsernameEnabled = !_credentials.isAnonymousLogin();
            _view.PasswordEnabled = !_credentials.isAnonymousLogin();
            {
                bool enable = _enableKeychain && !_credentials.isAnonymousLogin();
                _view.SavePasswordEnabled = enable;
                if (!enable)
                {
                    _view.SavePasswordState = false;
                }
            }
            _view.AnonymousEnabled = _enableAnonymous;
            if (_enableAnonymous && _credentials.isAnonymousLogin())
            {
                _view.AnonymousState = true;
            }
            else
            {
                _view.AnonymousState = false;
            }
            _view.PkCheckboxEnabled = _enablePublicKey;
            if (_enablePublicKey && _credentials.isPublicKeyAuthentication())
            {
                _view.PkCheckboxState = true;
                _view.PkLabel = _credentials.getIdentity().getAbbreviatedPath();
            }
            else
            {
                _view.PkCheckboxState = false;
                View.PkLabel = Locale.localizedString("No Private Key selected", "Credentials");
            }
        }

        private class Factory : LoginControllerFactory
        {
            protected override object create()
            {
                return new LoginController(TransferController.Instance);
            }

            protected override ch.cyberduck.core.LoginController create(Session s)
            {
                foreach (BrowserController c in MainController.Browsers)
                {
                    if (c.getSession() == s)
                    {
                        return create(c);
                    }
                }
                return (ch.cyberduck.core.LoginController) create();
            }

            protected override ch.cyberduck.core.LoginController create(ch.cyberduck.ui.Controller c)
            {
                return new LoginController((WindowController) c);
            }
        }
    }
}