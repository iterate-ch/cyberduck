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

using System.Collections.Generic;
using System.Media;
using System.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using ch.cyberduck.core.ftp;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.sftp;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Winforms.Controls;
using java.lang;
using org.apache.log4j;
using StructureMap;
using Object = System.Object;
using String = System.String;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class ConnectionController : WindowController<IConnectionView>
    {
        private static readonly IDictionary<WindowController, ConnectionController> Controllers =
            new Dictionary<WindowController, ConnectionController>();

        private static readonly string Default = LocaleFactory.localizedString("Default");
        private static readonly Logger Log = Logger.getLogger(typeof (ConnectionController).FullName);
        private readonly Object _syncRootReachability = new Object();
        private readonly Timer _ticklerReachability;

        private readonly HostPasswordStore keychain = PasswordStoreFactory.get();

        private ConnectionController(IConnectionView view)
        {
            View = view;

            _ticklerReachability = new Timer(OnReachability, null, Timeout.Infinite, Timeout.Infinite);

            View.ToggleOptions += View_ToggleOptions;
            View.OptionsVisible = PreferencesFactory.get().getBoolean("connection.toggle.options");
            View.ViewClosedEvent +=
                delegate { PreferencesFactory.get().setProperty("connection.toggle.options", View.OptionsVisible); };

            Init();
        }

        private ConnectionController() : this(ObjectFactory.GetInstance<IConnectionView>())
        {
        }

        public override bool Singleton
        {
            get { return true; }
        }

        public Host ConfiguredHost
        {
            get
            {
                Protocol protocol = View.SelectedProtocol;
                Host parsed = new Host(protocol, View.Hostname, Integer.parseInt(View.Port), View.Path);
                if (protocol.getType() == Protocol.Type.ftp)
                {
                    parsed.setFTPConnectMode(View.SelectedConnectMode);
                }
                Credentials credentials = parsed.getCredentials();
                credentials.setUsername(View.Username);
                credentials.setPassword(View.Password);
                credentials.setSaved(View.SavePasswordChecked);
                if (protocol.getType() == Protocol.Type.sftp)
                {
                    if (View.PkCheckboxState)
                    {
                        credentials.setIdentity(LocalFactory.get(View.PkLabel));
                    }
                }
                if (View.SelectedEncoding.Equals(Default))
                {
                    parsed.setEncoding(null);
                }
                else
                {
                    parsed.setEncoding(View.SelectedEncoding);
                }
                return parsed;
            }
        }

        public static ConnectionController Instance(WindowController parent)
        {
            ConnectionController c;
            if (!Controllers.TryGetValue(parent, out c))
            {
                c = new ConnectionController();
                Controllers.Add(parent, c);
                parent.View.ViewClosedEvent += delegate
                {
                    Controllers.Remove(parent);
                    c.View.Close();
                };
            }
            return c;
        }

        public override bool ViewShouldClose()
        {
            if (Utils.IsBlank(View.Hostname))
            {
                SystemSounds.Beep.Play();
                return false;
            }
            if (Utils.IsBlank(View.Username))
            {
                SystemSounds.Beep.Play();
                return false;
            }
            return true;
        }

        private void Init()
        {
            InitProtocols();
            InitConnectModes();
            InitEncodings();

            View.Username = PreferencesFactory.get().getProperty("connection.login.name");
            View.PkLabel = LocaleFactory.localizedString("No private key selected");
            View.SavePasswordChecked = PreferencesFactory.get().getBoolean("connection.login.useKeychain");
            View.AnonymousChecked = false;
            View.PkCheckboxState = false;
            View.SelectedEncoding = Default;
            View.SelectedConnectMode = FTPConnectMode.unknown;
            View.ChangedProtocolEvent += View_ChangedProtocolEvent;
            View.ChangedPortEvent += View_ChangedPortEvent;
            View.ChangedUsernameEvent += View_ChangedUsernameEvent;
            View.ChangedServerEvent += View_ChangedServerEvent;
            View.ChangedEncodingEvent += View_ChangedEncodingEvent;
            View.ChangedPathEvent += View_ChangedPathEvent;
            View.ChangedPublicKeyCheckboxEvent += View_ChangedPublicKeyCheckboxEvent;
            View.ChangedAnonymousCheckboxEvent += View_ChangedAnonymousCheckboxEvent;
            View.ChangedSavePasswordCheckboxEvent += View_ChangedSavePasswordCheckboxEvent;
            View.ChangedPrivateKey += View_ChangedPrivateKey;
            View.OpenUrl += View_OpenUrl;

            View_ChangedProtocolEvent();
        }

        private void View_ChangedPrivateKey(object sender, PrivateKeyArgs e)
        {
            if (null != e.KeyFile)
            {
                View.PkLabel = e.KeyFile;
                View.PasswordEnabled = false;
            }
            else
            {
                View.PkLabel = LocaleFactory.localizedString("No private key selected");
            }
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
        }

        private void View_OpenUrl()
        {
            BrowserLauncherFactory.get().open(View.URL);
        }

        private void View_ChangedAnonymousCheckboxEvent()
        {
            if (View.AnonymousChecked)
            {
                View.UsernameEnabled = false;
                View.Username = PreferencesFactory.get().getProperty("connection.login.anon.name");
                View.PasswordEnabled = false;
                View.Password = string.Empty;
            }
            else
            {
                View.UsernameEnabled = true;
                View.Username = PreferencesFactory.get().getProperty("connection.login.name");
                View.PasswordEnabled = true;
            }
            UpdateUrlLabel();
        }

        private void View_ChangedPublicKeyCheckboxEvent()
        {
            string s = LocaleFactory.localizedString("No private key selected");
            if (View.PkCheckboxState)
            {
                string selectedKeyFile = PreferencesFactory.get().getProperty("local.user.home");
                if (!s.Equals(View.PkLabel))
                {
                    selectedKeyFile = View.PkLabel;
                }

                View.PasswordEnabled = true;
                View.ShowPrivateKeyBrowser(selectedKeyFile);
            }
            else
            {
                View_ChangedPrivateKey(this, new PrivateKeyArgs(null));
            }
        }

        private void View_ChangedPathEvent()
        {
            UpdateUrlLabel();
        }

        private void View_ChangedEncodingEvent()
        {
            ;
        }

        private void View_ChangedServerEvent()
        {
            if (Scheme.isURL(View.Hostname))
            {
                Host parsed = HostParser.parse(View.Hostname);
                View.Hostname = parsed.getHostname();
                View.SelectedProtocol = parsed.getProtocol();
                View.Port = parsed.getPort().ToString();
                View.Username = parsed.getCredentials().getUsername();
                View.Path = parsed.getDefaultPath();
                View.AnonymousChecked = parsed.getCredentials().isAnonymousLogin();
            }
            UpdateUrlLabel();
            UpdateIdentity();
            ReadPasswordFromKeychain();
            Reachable();
        }

        public void ReadPasswordFromKeychain()
        {
            if (PreferencesFactory.get().getBoolean("connection.login.useKeychain"))
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
                string password = keychain.getPassword(protocol.getScheme(), Integer.parseInt(View.Port), View.Hostname,
                    View.Username);
                if (Utils.IsBlank(password))
                {
                    View.Password = password;
                }
            }
        }

        private void Reachable()
        {
            if (!string.IsNullOrEmpty(View.Hostname))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerReachability.Change(2000, Timeout.Infinite);
            }
            else
            {
                View.AlertIconEnabled = false;
            }
        }

        private void View_ChangedUsernameEvent()
        {
            UpdateUrlLabel();
            ReadPasswordFromKeychain();
        }

        private void View_ChangedPortEvent()
        {
            //if empty we set the default port the selected protocol
            if (String.IsNullOrEmpty(View.Port))
            {
                View.Port = View.SelectedProtocol.getDefaultPort().ToString();
            }
            UpdateUrlLabel();
            Reachable();
        }

        private void View_ChangedProtocolEvent()
        {
            Log.debug("View_ChangedProtocolEvent");
            Protocol protocol = View.SelectedProtocol;

            View.Port = protocol.getDefaultPort().ToString();
            View.PortFieldEnabled = protocol.isPortConfigurable();
            if (!protocol.isHostnameConfigurable())
            {
                View.HostFieldEnabled = false;
                View.Hostname = protocol.getDefaultHostname();
                View.PathEnabled = true;
            }
            else
            {
                if (!View.HostFieldEnabled)
                {
                    // Was previously configured with a static configuration
                    View.Hostname = string.Empty;
                }
                if (!View.PathEnabled)
                {
                    // Was previously configured with a static configuration
                    View.Path = string.Empty;
                }
                if (Utils.IsNotBlank(protocol.getDefaultHostname()))
                {
                    // Prefill with default hostname
                    View.Hostname = protocol.getDefaultHostname();
                }

                View.UsernameEnabled = true;
                View.HostFieldEnabled = true;
                View.PathEnabled = true;
            }
            View.UsernameLabel = protocol.getUsernamePlaceholder() + ":";
            View.PasswordLabel = protocol.getPasswordPlaceholder() + ":";
            View.ConnectModeFieldEnabled = protocol.getType() == Protocol.Type.ftp;
            if (!protocol.isEncodingConfigurable())
            {
                View.SelectedEncoding = Default;
            }
            View.EncodingFieldEnabled = protocol.isEncodingConfigurable();
            View.AnonymousEnabled = protocol.isAnonymousConfigurable();

            UpdateIdentity();
            UpdateUrlLabel();
            ReadPasswordFromKeychain();
            Reachable();
        }

        private void UpdateUrlLabel()
        {
            if (!string.IsNullOrEmpty(View.Hostname))
            {
                String url = View.SelectedProtocol.getScheme().toString() + "://" + View.Username + "@" + View.Hostname +
                             ":" + View.Port + PathNormalizer.normalize(View.Path);
                View.URL = url;
            }
            else
            {
                View.URL = String.Empty;
            }
        }

        /// <summary>
        /// Update Private Key selection
        /// </summary>
        private void UpdateIdentity()
        {
            View.PkCheckboxEnabled = View.SelectedProtocol.Equals(new SFTPProtocol());
            if (Utils.IsNotBlank(View.Hostname))
            {
                Credentials credentials =
                    CredentialsConfiguratorFactory.get(View.SelectedProtocol)
                        .configure(new Host(new SFTPProtocol(), View.Hostname));
                if (credentials.isPublicKeyAuthentication())
                {
                    // No previously manually selected key
                    View.PkCheckboxState = true;
                    View.PkLabel = credentials.getIdentity().getAbbreviatedPath();
                }
                else
                {
                    View.PkCheckboxState = false;
                    View.PkLabel = LocaleFactory.localizedString("No private key selected");
                }
                if (Utils.IsNotBlank(credentials.getUsername()))
                {
                    View.Username = credentials.getUsername();
                }
            }
        }

        private void InitEncodings()
        {
            List<string> encodings = new List<string> {Default};
            encodings.AddRange(new DefaultCharsetProvider().availableCharsets());
            View.PopulateEncodings(encodings);
        }

        private void InitProtocols()
        {
            List<KeyValueIconTriple<Protocol, string>> protocols = new List<KeyValueIconTriple<Protocol, string>>();
            foreach (Protocol p in ProtocolFactory.getEnabledProtocols().toArray(new Protocol[] {}))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(p, p.getDescription(), p.getProvider()));
            }
            View.PopulateProtocols(protocols);
            View.SelectedProtocol =
                ProtocolFactory.forName(PreferencesFactory.get().getProperty("connection.protocol.default"));
        }

        private void InitConnectModes()
        {
            List<KeyValuePair<string, FTPConnectMode>> modes = new List<KeyValuePair<string, FTPConnectMode>>();
            foreach (FTPConnectMode m in FTPConnectMode.values())
            {
                modes.Add(new KeyValuePair<string, FTPConnectMode>(m.toString(), m));
            }
            View.PopulateConnectModes(modes);
        }

        private void View_ToggleOptions()
        {
            View.OptionsVisible = !View.OptionsVisible;
        }

        private void OnReachability(object state)
        {
            Invoke(delegate { background(new ReachabilityAction(this, View.SelectedProtocol, View.Hostname)); });
        }

        private class ReachabilityAction : AbstractBackgroundAction
        {
            private readonly ConnectionController _controller;
            private readonly string _hostname;
            private readonly Protocol _protocol;
            private bool _reachable;

            public ReachabilityAction(ConnectionController controller, Protocol protocol, String hostname)
            {
                _controller = controller;
                _protocol = protocol;
                _hostname = hostname;
            }

            public override object run()
            {
                if (!String.IsNullOrEmpty(_hostname))
                {
                    _reachable = ReachabilityFactory.get().isReachable(new Host(_protocol, _hostname));
                }
                else
                {
                    _reachable = false;
                }
                return _reachable;
            }

            public override void cleanup()
            {
                _controller.View.AlertIconEnabled = !_reachable;
            }

            public override object @lock()
            {
                return _controller._syncRootReachability;
            }
        }
    }
}