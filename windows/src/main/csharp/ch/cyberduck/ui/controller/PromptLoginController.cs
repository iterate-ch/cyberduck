// Copyright (c) 2010-2026 Yves Langisch. All rights reserved.
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

using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.features;
using Ch.Cyberduck.Core.Native;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.sftp.openssh;
using Ch.Cyberduck.Core.TaskDialog;
using ch.cyberduck.ui.core;
using java.lang;
using java.util;
using java.util.concurrent;
using org.apache.logging.log4j;
using StructureMap;
using static Ch.Cyberduck.ImageHelper;
using Path = System.IO.Path;
using UiUtils = Ch.Cyberduck.Ui.Core.Utils;

namespace Ch.Cyberduck.Ui.Controller
{
    public class PromptLoginController : PromptPasswordController, LoginCallback
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(PromptLoginController).FullName);
        private readonly IWindowController _browser;
        private readonly List<string> _keys = new List<string> { LocaleFactory.localizedString("None") };

        private readonly HostPasswordStore keychain = PasswordStoreFactory.get();
        private ILocationPromptView _locationView;

        private ILoginView _loginView;

        public PromptLoginController(IWindowController c) : base(c)
        {
            _browser = c;
        }

        public Location.Name select(Host bookmark, string title, string message, Set regions,
            Location.Name defaultRegion)
        {
            View = ObjectFactory.GetInstance<ILocationPromptView>();
            _locationView = (ILocationPromptView)View;

            _locationView.Title = LocaleFactory.localizedString(title, "Credentials");
            _locationView.Message = LocaleFactory.localizedString(message, "Credentials");

            var locations = Utils.ConvertFromJavaList<Location.Name>(regions);

            IList<KeyValuePair<string, string>> r = locations.OrderBy(name => name.ToString())
                .Select(l =>
                {
                    var key = l.getIdentifier();
                    var display = l.toString();
                    if (!string.Equals(key, display))
                    {
                        return new(key, string.Format("{0} - {1}", display, key));
                    }
                    return new KeyValuePair<string, string>(key, display);
                }).ToList();
            _locationView.PopulateLocations(r);
            if (regions.contains(defaultRegion))
            {
                _locationView.Location = defaultRegion.getIdentifier();
            }

            AsyncDelegate d = delegate
            {
                if (DialogResult.Cancel == _locationView.ShowDialog(_browser.Window))
                {
                    throw new LoginCanceledException();
                }
            };
            _browser.Invoke(d, true);

            return new Location.Name(_locationView.Location);
        }

        object ConnectionCallback.getFeature(Class type) => ConnectionCallback.__DefaultMethods.getFeature(this, type);

        public void await(CountDownLatch signal, Host bookmark, string title, string message) =>
            Dialogs.AwaitBackgroundAction(signal, bookmark, title, message, Images.CyberduckApplication);

        public void warn(Host bookmark, string title, string message, string continueButton, string disconnectButton,
            string preference)
        {
            AsyncDelegate d = delegate
            {
                var result = UiUtils.Show(
                    owner: _browser.Window,
                    title: title,
                    mainInstruction: title,
                    content: message,
                    commandLinks: [continueButton, disconnectButton],
                    mainIcon: TaskDialogIcon.Question,
                    verificationText: Utils.IsNotBlank(preference)
                        ? LocaleFactory.localizedString("Don't show again", "Credentials")
                        : null,
                    footerText: UiUtils.FormatHelp(ProviderHelpServiceFactory.get().help(bookmark.getProtocol())));

                if (result.VerificationChecked == true)
                {
                    // Never show again.
                    PreferencesFactory.get().setProperty(preference, true);
                }

                if ((int)result.Button == 1)
                {
                    throw new LoginCanceledException();
                }
            };
            _browser.Invoke(d, true);
            //Proceed nevertheless.
        }

        public Credentials prompt(Host bookmark, string username, string title, string reason, LoginOptions options)
        {
            View = ObjectFactory.GetInstance<ILoginView>();
            _loginView = (ILoginView)View;
            var credentials = new Credentials().setSaved(options.save()).setUsername(username);
            InitEventHandlers(bookmark, credentials, options);


            _loginView.Title = LocaleFactory.localizedString(title, "Credentials");
            _loginView.Message = LocaleFactory.localizedString(reason, "Credentials");
            _loginView.Username = username;
            _loginView.DiskIcon = Images.Get(options.icon()).Size(64);

            InitPrivateKeys();

            Update(credentials, options);

            AsyncDelegate d = delegate
            {
                if (DialogResult.Cancel == _loginView.ShowDialog(_browser.Window))
                {
                    throw new LoginCanceledException();
                }
            };
            _browser.Invoke(d, true);
            return credentials;
        }

        public Local select(Local identity)
        {
            return identity;
        }

        private void InitPrivateKeys()
        {
            _keys.Clear();
            foreach (
                Local key in
                Utils.ConvertFromJavaList<Local>(
                    new OpenSSHPrivateKeyConfigurator(
                        LocalFactory.get(PreferencesFactory.get().getProperty("local.user.home"), ".ssh")).list()))
            {
                _keys.Add(key.getAbsolute());
            }

            _loginView.PopulatePrivateKeys(_keys);
        }

        private void View_ChangedAnonymousCheckboxEvent(Credentials credentials, LoginOptions options)
        {
            if (_loginView.AnonymousState)
            {
                credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.anon.name"));
                credentials.setPassword(PreferencesFactory.get().getProperty("connection.login.anon.pass"));
            }
            else
            {
                credentials.setUsername(PreferencesFactory.get().getProperty("connection.login.name"));
                credentials.setPassword(null);
            }

            _loginView.Username = credentials.getUsername();
            _loginView.Password = credentials.getPassword();
            Update(credentials, options);
        }

        private void View_ChangedSavePasswordCheckboxEvent(Credentials credentials)
        {
            credentials.setSaved(_loginView.SavePasswordState);
        }

        private void View_ChangedPasswordEvent(Credentials credentials)
        {
            credentials.setPassword(_loginView.Password);
        }

        private void View_ChangedUsernameEvent(Host bookmark, Credentials credentials, LoginOptions options)
        {
            credentials.setUsername(_loginView.Username);
            if (!string.IsNullOrWhiteSpace(credentials.getUsername()))
            {
                string password = keychain.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), credentials.getUsername());
                if (!string.IsNullOrWhiteSpace(password))
                {
                    _loginView.Password = password;
                    // Make sure password fetched from keychain and set in field is set in model
                    credentials.setPassword(password);
                }
            }

            Update(credentials, options);
        }

        private void InitEventHandlers(Host bookmark, Credentials credentials, LoginOptions options)
        {
            _loginView.ChangedUsernameEvent += () => View_ChangedUsernameEvent(bookmark, credentials, options);
            _loginView.ChangedPasswordEvent += () => View_ChangedPasswordEvent(credentials);
            _loginView.ChangedSavePasswordCheckboxEvent += () => View_ChangedSavePasswordCheckboxEvent(credentials);
            _loginView.ChangedAnonymousCheckboxEvent += () => View_ChangedAnonymousCheckboxEvent(credentials, options);
            _loginView.ChangedPrivateKey += (o, args) => View_ChangedPrivateKey(o, args, credentials, options);
            _loginView.ValidateInput += () => View_ValidateInput(bookmark, credentials, options);
            _loginView.OpenPrivateKeyBrowserEvent += View_OpenPrivateKeyBrowserEvent;
        }

        private void View_OpenPrivateKeyBrowserEvent()
        {
            string selectedKeyFile = PreferencesFactory.get().getProperty("local.user.home");
            if (!LocaleFactory.localizedString("None").Equals(_loginView.SelectedPrivateKey))
            {
                selectedKeyFile = Path.GetDirectoryName(_loginView.SelectedPrivateKey);
            }

            _loginView.PasswordEnabled = true;
            _loginView.ShowPrivateKeyBrowser(selectedKeyFile);
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
                _loginView.PopulatePrivateKeys(_keys);
            }

            Update(credentials, options);
        }

        private void Update(Credentials credentials, LoginOptions options)
        {
            _loginView.UsernameEnabled = options.user() && !credentials.isAnonymousLogin();
            _loginView.PasswordEnabled = options.password() && !credentials.isAnonymousLogin();
            _loginView.UsernameLabel = options.getUsernamePlaceholder() + ":";
            _loginView.PasswordLabel = options.getPasswordPlaceholder() + ":";
            _loginView.SavePasswordEnabled = options.keychain() && !credentials.isAnonymousLogin();
            _loginView.SavePasswordState = credentials.isSaved();
            _loginView.AnonymousEnabled = options.anonymous();
            if (options.anonymous() && credentials.isAnonymousLogin())
            {
                _loginView.AnonymousState = true;
            }
            else
            {
                _loginView.AnonymousState = false;
            }

            _loginView.PrivateKeyFieldEnabled = options.publickey();
            if (options.publickey() && credentials.isPublicKeyAuthentication())
            {
                _loginView.SelectedPrivateKey = credentials.getIdentity().getAbsolute();
            }
        }
    }
}
