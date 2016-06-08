// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
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

using System;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using org.apache.log4j;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller
{
    public class PromptLoginController : LoginCallback
    {
        private static readonly Logger Log = Logger.getLogger(typeof (PromptLoginController).FullName);
        private readonly WindowController _browser;
        private Host _bookmark;
        private Credentials _credentials;
        private LoginOptions _options;

        private readonly HostPasswordStore keychain
            = PasswordStoreFactory.get();

        public PromptLoginController(WindowController c)
        {
            _browser = c;
        }

        public ILoginView View { get; set; }

        public void warn(Protocol protocol, String title, String message, String continueButton, String disconnectButton,
            String preference)
        {
            AsyncController.AsyncDelegate d = delegate
            {
                _browser.CommandBox(title, title, message, String.Format("{0}|{1}", continueButton, disconnectButton),
                    false, LocaleFactory.localizedString("Don't show again", "Credentials"), SysIcons.Question,
                    PreferencesFactory.get().getProperty("website.help") + "/" + protocol.getScheme().name(),
                    delegate(int option, Boolean verificationChecked)
                    {
                        if (verificationChecked)
                        {
                            // Never show again.
                            PreferencesFactory.get().setProperty(preference, true);
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

        public void prompt(Host bookmark, Credentials credentials, String title, String reason, LoginOptions options)
        {
            View = ObjectFactory.GetInstance<ILoginView>();
            InitEventHandlers();

            _bookmark = bookmark;
            _credentials = credentials;
            _options = options;

            View.Title = LocaleFactory.localizedString(title, "Credentials");
            View.Message = LocaleFactory.localizedString(reason, "Credentials");
            View.Username = credentials.getUsername();
            View.SavePasswordState = PreferencesFactory.get().getBoolean("connection.login.useKeychain") &&
                                     PreferencesFactory.get().getBoolean("connection.login.addKeychain");
            View.DiskIcon = IconCache.Instance.IconForName(_bookmark.getProtocol().disk(), 64);

            Update();

            AsyncController.AsyncDelegate d = delegate
            {
                if (DialogResult.Cancel == View.ShowDialog(_browser.View))
                {
                    throw new LoginCanceledException();
                }
                credentials.setSaved(View.SavePasswordState);
                credentials.setUsername(Utils.SafeString(View.Username));
                credentials.setPassword(Utils.SafeString(View.Password));
            };
            _browser.Invoke(d);
        }

        public Local select(Local identity)
        {
            return identity;
        }

        private void View_ChangedPkCheckboxEvent()
        {
            if (View.PkCheckboxState)
            {
                string selectedKeyFile = PreferencesFactory.get().getProperty("local.user.home");
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
            if (View.AnonymousState)
            {
                _credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.anon.name"));
                _credentials.setPassword(PreferencesFactory.get().getProperty("connection.login.anon.pass"));
            }
            else
            {
                _credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.name"));
                _credentials.setPassword(null);
            }
            View.Username = _credentials.getUsername();
            View.Password = _credentials.getPassword();
            Update();
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
            PreferencesFactory.get().setProperty("connection.login.addKeychain", View.SavePasswordState);
        }

        private void View_ChangedPasswordEvent()
        {
            _credentials.setPassword(View.Password);
        }

        private void View_ChangedUsernameEvent()
        {
            _credentials.setUsername(View.Username);
            if (Utils.IsNotBlank(_credentials.getUsername()))
            {
                String password = keychain.getPassword(_bookmark.getProtocol().getScheme(),
                    _bookmark.getPort(), _bookmark.getHostname(), _credentials.getUsername());
                if (Utils.IsNotBlank(password))
                {
                    View.Password = password;
                }
            }
            Update();
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
            return _credentials.validate(_bookmark.getProtocol(), _options);
        }

        private void View_ChangedPrivateKey(object sender, PrivateKeyArgs e)
        {
            _credentials.setIdentity(null == e.KeyFile ? null : LocalFactory.get(e.KeyFile));
            Update();
        }

        private void Update()
        {
            View.UsernameEnabled = _options.user() && !_credentials.isAnonymousLogin();
            View.PasswordEnabled = _options.password() && !_credentials.isAnonymousLogin();
            View.UsernameLabel = _credentials.getUsernamePlaceholder() + ":";
            View.PasswordLabel = _credentials.getPasswordPlaceholder() + ":";
            {
                bool enable = _options.isKeychain() && !_credentials.isAnonymousLogin();
                View.SavePasswordEnabled = enable;
                if (!enable)
                {
                    View.SavePasswordState = false;
                }
            }
            View.AnonymousEnabled = _options.isAnonymous();
            if (_options.isAnonymous() && _credentials.isAnonymousLogin())
            {
                View.AnonymousState = true;
            }
            else
            {
                View.AnonymousState = false;
            }
            View.PkCheckboxEnabled = _options.isPublickey();
            if (_options.isPublickey() && _credentials.isPublicKeyAuthentication())
            {
                View.PkCheckboxState = true;
                View.PkLabel = _credentials.getIdentity().getAbbreviatedPath();
            }
            else
            {
                View.PkCheckboxState = false;
                View.PkLabel = LocaleFactory.localizedString("No Private Key selected", "Credentials");
            }
        }
    }
}