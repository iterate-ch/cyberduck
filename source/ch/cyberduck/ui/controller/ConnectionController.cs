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
using System.Collections.Generic;
using System.Threading;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using com.enterprisedt.net.ftp;
using java.lang;
using org.apache.log4j;
using org.spearce.jgit.transport;
using StructureMap;
using Object = System.Object;
using String = System.String;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class ConnectionController : WindowController<IConnectionView>
    {
        private static readonly String Auto = Locale.localizedString("Auto");
        private static readonly String ConnectmodeActive = Locale.localizedString("Active");
        private static readonly String ConnectmodePassive = Locale.localizedString("Passive");

        private static readonly IDictionary<WindowController, ConnectionController> Controllers =
            new Dictionary<WindowController, ConnectionController>();

        private static readonly string Default = Locale.localizedString("Default");
        private static readonly Logger Log = Logger.getLogger(typeof (ConnectionController).Name);

        private static readonly string TransferBrowserconnection = Locale.localizedString("Use browser connection");
        private static readonly string TransferNewconnection = Locale.localizedString("Open new connection");
        private readonly Object _syncRootReachability = new Object();
        private readonly Timer _ticklerRechability;

        private ConnectionController(IConnectionView view)
        {
            View = view;

            _ticklerRechability = new Timer(OnReachability, null, Timeout.Infinite, Timeout.Infinite);

            View.ToggleOptions += View_ToggleOptions;
            View.OptionsVisible = Preferences.instance().getBoolean("connection.toggle.options");
            View.ViewClosedEvent +=
                delegate { Preferences.instance().setProperty("connection.toggle.options", View.OptionsVisible); };

            Init();
        }

        private ConnectionController()
            : this(ObjectFactory.GetInstance<IConnectionView>())
        {
        }

        public Host ConfiguredHost
        {
            get
            {
                Protocol protocol = View.SelectedProtocol;
                Host host = new Host(
                    protocol,
                    View.Hostname,
                    Integer.parseInt(View.Port),
                    View.Path);
                if (protocol.Equals(Protocol.FTP) ||
                    protocol.Equals(Protocol.FTP_TLS))
                {
                    if (View.SelectedConnectMode.Equals(Default))
                    {
                        host.setFTPConnectMode(null);
                    }
                    else if (View.SelectedConnectMode.Equals(ConnectmodeActive))
                    {
                        host.setFTPConnectMode(FTPConnectMode.ACTIVE);
                    }
                    else if (View.SelectedConnectMode.Equals(ConnectmodePassive))
                    {
                        host.setFTPConnectMode(FTPConnectMode.PASV);
                    }
                }
                Credentials credentials = host.getCredentials();
                credentials.setUsername(View.Username);
                credentials.setPassword(View.Password);
                credentials.setUseKeychain(View.SavePasswordChecked);
                if (protocol.equals(Protocol.SFTP))
                {
                    if (View.PkCheckboxState)
                    {
                        credentials.setIdentity(LocalFactory.createLocal(View.PkLabel));
                    }
                }
                if (View.SelectedEncoding.Equals(Default))
                {
                    host.setEncoding(null);
                }
                else
                {
                    host.setEncoding(View.SelectedEncoding);
                }
                return host;
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
                                                       //todo c muss wohl auch noch abgeräumt werden
                                                   };
            }
            return c;
        }

        public override bool ViewShouldClose()
        {
            //todo mit SheetController/-Form und validate mit beep
            if (Utils.IsBlank(View.Hostname))
            {
                return false;
            }
            if (Utils.IsBlank(View.Username))
            {
                return false;
            }
            return true;
        }

        private void Init()
        {
            InitProtocols();
            InitConnectModes();
            InitEncodings();

            View.Username = Preferences.instance().getProperty("connection.login.name");
            View.PkLabel = Locale.localizedString("No private key selected");
            View.SavePasswordChecked = Preferences.instance().getBoolean(
                "connection.login.useKeychain")
                                       &&
                                       Preferences.instance().getBoolean(
                                           "connection.login.addKeychain");
            View.AnonymousChecked = false;
            View.PkCheckboxState = false;
            View.SelectedEncoding = Default;
            View.SelectedConnectMode = Default;
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
                View.PkLabel = Locale.localizedString("No private key selected");
            }
        }

        private void View_ChangedSavePasswordCheckboxEvent()
        {
            Preferences.instance().setProperty("connection.login.addKeychain", View.SavePasswordChecked);
        }

        private void View_OpenUrl()
        {
            Utils.StartProcess(View.URL);
        }

        private void View_ChangedAnonymousCheckboxEvent()
        {
            if (View.AnonymousChecked)
            {
                View.UsernameEnabled = false;
                View.Username = Preferences.instance().getProperty("connection.login.anon.name");
                View.PasswordEnabled = false;
                View.Password = string.Empty;
            }
            else
            {
                View.UsernameEnabled = true;
                View.Username = Preferences.instance().getProperty("connection.login.name");
                View.PasswordEnabled = true;
            }
            UpdateUrlLabel();
        }

        private void View_ChangedPublicKeyCheckboxEvent()
        {
            string s = Locale.localizedString("No private key selected");
            if (View.PkCheckboxState)
            {
                string selectedKeyFile = UserPreferences.HomeFolder;
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
            if (Protocol.isURL(View.Hostname))
            {
                Host parsed = Host.parse(View.Hostname);
                HostChanged(parsed);
            }
            else
            {
                UpdateUrlLabel();
                UpdateIdentity();
                Reachable();
            }
        }

        private void HostChanged(Host host)
        {
            View.Hostname = host.getHostname();
            View.SelectedProtocol = host.getProtocol();
            View.Port = host.getPort().ToString();
            View.Username = host.getCredentials().getUsername();
            View.Path = host.getDefaultPath();
            View.AnonymousChecked = host.getCredentials().isAnonymousLogin();
            if (host.getCredentials().isPublicKeyAuthentication())
            {
                View.PkCheckboxState = true;
                View.PkLabel = host.getCredentials().getIdentity().toURL();
            }
            else
            {
                UpdateIdentity();
            }
            UpdateUrlLabel();
            ReadPasswordFromKeychain();
            Reachable();
        }

        public void ReadPasswordFromKeychain()
        {
            if (Preferences.instance().getBoolean("connection.login.useKeychain"))
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
                View.Password = KeychainFactory.instance().getPassword(protocol.getScheme(),
                                                                       Integer.parseInt(View.Port),
                                                                       View.Hostname,
                                                                       View.Username);
            }
        }

        private void Reachable()
        {
            if (!string.IsNullOrEmpty(View.Hostname))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerRechability.Change(2000, Timeout.Infinite);
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
            if (!protocol.isHostnameConfigurable())
            {
                View.HostFieldEnabled = false;
                View.Hostname = protocol.getDefaultHostname();
                View.PortFieldEnabled = false;
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
                View.PortFieldEnabled = true;
                View.PathEnabled = true;
            }
            //todo placeholder setzen bzw. label korrekt setzen je nach protokoll
            if (protocol.Equals(Protocol.IDISK))
            {
                String member = Preferences.instance().getProperty("iToolsMember");
                if (!string.IsNullOrEmpty(member))
                {
                    // Account name configured in System Preferences
                    View.Username = member;
                    View.UsernameEnabled = false;
                    View.Path = Path.DELIMITER + member;
                    View.PathEnabled = false;
                }
            }
            View.ConnectModeFieldEnabled = protocol.isConnectModeConfigurable();
            if (!protocol.isEncodingConfigurable())
            {
                View.SelectedEncoding = Default;
            }
            View.EncodingFieldEnabled = protocol.isEncodingConfigurable();

            UpdateIdentity();
            UpdateUrlLabel();

            OnReachability(null);
        }

        private void UpdateUrlLabel()
        {
            if (!string.IsNullOrEmpty(View.Hostname))
            {
                String url = View.SelectedProtocol.getScheme() + "://" + View.Username
                             + "@" + View.Hostname + ":" + View.Port
                             + Path.normalize(View.Path);
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
            View.PkCheckboxEnabled = View.SelectedProtocol == Protocol.SFTP;
            if (View.SelectedProtocol == Protocol.SFTP)
            {
                if (Utils.IsNotBlank(View.Hostname))
                {
                    OpenSshConfig.Host entry = OpenSshConfig.create().lookup(View.Hostname);
                    if (null != entry.getIdentityFile())
                    {
                        if (!View.PkCheckboxState)
                        {
                            // No previously manually selected key
                            View.PkCheckboxState = true;
                            View.PkLabel =
                                LocalFactory.createLocal(entry.getIdentityFile().getAbsolutePath()).getAbbreviatedPath();
                        }
                    }
                    else
                    {
                        View.PkCheckboxState = false;
                        View.PkLabel = Locale.localizedString("No private key selected");
                    }
                    if (Utils.IsNotBlank(entry.getUser()))
                    {
                        View.Username = entry.getUser();
                    }
                }
            }
            else
            {
                View.PkCheckboxState = false;
                View.PkLabel = Locale.localizedString("No private key selected");
            }
        }

        private void InitEncodings()
        {
            List<string> encodings = new List<string> {Default};
            encodings.AddRange(Utils.AvailableCharsets());
            View.PopulateEncodings(encodings);
        }

        private void InitProtocols()
        {
            List<KeyValueIconTriple<Protocol, string>> protocols = new List<KeyValueIconTriple<Protocol, string>>();
            foreach (Protocol p in Protocol.getKnownProtocols().toArray(new Protocol[] {}))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(p, p.getDescription(), p.getIdentifier()));
            }
            View.PopulateProtocols(protocols);
        }

        private void InitConnectModes()
        {
            List<string> connectModesList = new List<string> {Default, ConnectmodeActive, ConnectmodePassive};
            View.PopulateConnectModes(connectModesList);
        }


        private void View_ToggleOptions()
        {
            View.OptionsVisible = !View.OptionsVisible;
        }

        private void OnReachability(object state)
        {
            Log.debug("OnRechability");
            background(new ReachabilityAction(this, View.Hostname));
        }

        private class ReachabilityAction : AbstractBackgroundAction
        {
            private readonly ConnectionController _controller;
            private readonly string _hostname;
            private bool _reachable;

            public ReachabilityAction(ConnectionController controller, String hostname)
            {
                _controller = controller;
                _hostname = hostname;
            }

            public override void run()
            {
                if (!String.IsNullOrEmpty(_hostname))
                {
                    _reachable = new Host(_hostname).isReachable();
                }
                else
                {
                    _reachable = false;
                }
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