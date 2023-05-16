// 
// Copyright (c) 2010-2018 Yves Langisch. All rights reserved.
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
using System.Collections.Generic;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.sftp.openssh;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Native;
using Ch.Cyberduck.Core.TaskDialog;
using java.util.concurrent;
using org.apache.logging.log4j;
using StructureMap;
using static Ch.Cyberduck.ImageHelper;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class PromptLoginController : PromptPasswordController, LoginCallback
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(PromptLoginController).FullName);
        private readonly WindowController _browser;
        private readonly List<string> _keys = new List<string> { LocaleFactory.localizedString("None") };

        private readonly HostPasswordStore keychain = PasswordStoreFactory.get();

        public PromptLoginController(WindowController c) : base(c)
        {
            _browser = c;
        }

        public ILoginView View { get; set; }

        public void await(CountDownLatch signal, Host bookmark, string title, string message) => Dialogs.AwaitBackgroundAction(signal, bookmark, title, message, Images.CyberduckApplication);

        public void warn(Host bookmark, String title, String message, String continueButton, String disconnectButton,
            String preference)
        {
            AsyncDelegate d = delegate
            {
                _browser.CommandBox(title, title, message, String.Format("{0}|{1}", continueButton, disconnectButton),
                    false, Utils.IsNotBlank(preference) ? LocaleFactory.localizedString("Don't show again", "Credentials") : null, TaskDialogIcon.Question,
                    ProviderHelpServiceFactory.get().help(bookmark.getProtocol()),
                    delegate (int option, Boolean verificationChecked)
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

        public Credentials prompt(Host bookmark, String username, String title, String reason, LoginOptions options)
        {
            View = ObjectFactory.GetInstance<ILoginView>();
            var credentials = new Credentials().withSaved(options.save()).withUsername(username);
            InitEventHandlers(bookmark, credentials, options);


            View.Title = LocaleFactory.localizedString(title, "Credentials");
            View.Message = LocaleFactory.localizedString(reason, "Credentials");
            View.Username = username;
            View.DiskIcon = Images.Get(options.icon()).Size(64);

            InitPrivateKeys();

            Update(credentials, options);

            AsyncDelegate d = delegate
            {
                if (DialogResult.Cancel == View.ShowDialog(_browser.View))
                {
                    throw new LoginCanceledException();
                }
            };
            _browser.Invoke(d);
            return credentials;
        }

        public Local select(Local identity)
        {
            return identity;
        }

        private void InitPrivateKeys()
        {
            foreach (
                Local key in
                Utils.ConvertFromJavaList<Local>(
                    new OpenSSHPrivateKeyConfigurator(
                        LocalFactory.get(PreferencesFactory.get().getProperty("local.user.home"), ".ssh")).list()))
            {
                _keys.Add(key.getAbsolute());
            }
            View.PopulatePrivateKeys(_keys);
        }

        private void View_ChangedAnonymousCheckboxEvent(Credentials credentials, LoginOptions options)
        {
            if (View.AnonymousState)
            {
                credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.anon.name"));
                credentials.setPassword(PreferencesFactory.get().getProperty("connection.login.anon.pass"));
            }
            else
            {
                credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.name"));
                credentials.setPassword(null);
            }
            View.Username = credentials.getUsername();
            View.Password = credentials.getPassword();
            Update(credentials, options);
        }

        private void View_ChangedSavePasswordCheckboxEvent(Credentials credentials)
        {
            credentials.setSaved(View.SavePasswordState);
        }

        private void View_ChangedPasswordEvent(Credentials credentials)
        {
            credentials.setPassword(View.Password);
        }

        private void View_ChangedUsernameEvent(Host bookmark, Credentials credentials, LoginOptions options)
        {
            credentials.setUsername(View.Username);
            if (!string.IsNullOrWhiteSpace(credentials.getUsername()))
            {
                String password = keychain.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), credentials.getUsername());
                if (!string.IsNullOrWhiteSpace(password))
                {
                    View.Password = password;
                    // Make sure password fetched from keychain and set in field is set in model
                    credentials.setPassword(password);
                }
            }
            Update(credentials, options);
        }

        private void InitEventHandlers(Host bookmark, Credentials credentials, LoginOptions options)
        {
            View.ChangedUsernameEvent += () => View_ChangedUsernameEvent(bookmark, credentials, options);
            View.ChangedPasswordEvent += () => View_ChangedPasswordEvent(credentials);
            View.ChangedSavePasswordCheckboxEvent += () => View_ChangedSavePasswordCheckboxEvent(credentials);
            View.ChangedAnonymousCheckboxEvent += () => View_ChangedAnonymousCheckboxEvent(credentials, options);
            View.ChangedPrivateKey += (o, args) => View_ChangedPrivateKey(o, args, credentials, options);
            View.ValidateInput += () => View_ValidateInput(bookmark, credentials, options);
            View.OpenPrivateKeyBrowserEvent += View_OpenPrivateKeyBrowserEvent;
        }

        private void View_OpenPrivateKeyBrowserEvent()
        {
            string selectedKeyFile = PreferencesFactory.get().getProperty("local.user.home");
            if (!LocaleFactory.localizedString("None").Equals(View.SelectedPrivateKey))
            {
                selectedKeyFile = Path.GetDirectoryName(View.SelectedPrivateKey);
            }
            View.PasswordEnabled = true;
            View.ShowPrivateKeyBrowser(selectedKeyFile);
        }

        private bool View_ValidateInput(Host bookmark, Credentials credentials, LoginOptions options)
        {
            return credentials.validate(bookmark.getProtocol(), options);
        }

        private void View_ChangedPrivateKey(object sender, PrivateKeyArgs e, Credentials credentials,
            LoginOptions options)
        {
            credentials.setIdentity(null == e.KeyFile ? null : LocalFactory.get(e.KeyFile));
            if (!_keys.Contains(e.KeyFile))
            {
                _keys.Add(e.KeyFile);
                View.PopulatePrivateKeys(_keys);
            }
            Update(credentials, options);
        }

        private void Update(Credentials credentials, LoginOptions options)
        {
            View.UsernameEnabled = options.user() && !credentials.isAnonymousLogin();
            View.PasswordEnabled = options.password() && !credentials.isAnonymousLogin();
            View.UsernameLabel = options.getUsernamePlaceholder() + ":";
            View.PasswordLabel = options.getPasswordPlaceholder() + ":";
            View.SavePasswordEnabled = options.keychain() && !credentials.isAnonymousLogin();
            View.SavePasswordState = credentials.isSaved();
            View.AnonymousEnabled = options.anonymous();
            if (options.anonymous() && credentials.isAnonymousLogin())
            {
                View.AnonymousState = true;
            }
            else
            {
                View.AnonymousState = false;
            }
            View.PrivateKeyFieldEnabled = options.publickey();
            if (options.publickey() && credentials.isPublicKeyAuthentication())
            {
                View.SelectedPrivateKey = credentials.getIdentity().getAbsolute();
            }
        }
    }
}
