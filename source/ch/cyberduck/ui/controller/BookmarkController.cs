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
using System.Collections.Generic;
using System.Drawing;
using System.Net;
using System.Text.RegularExpressions;
using System.Threading;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using com.enterprisedt.net.ftp;
using java.lang;
using org.apache.commons.lang;
using org.apache.log4j;
using org.spearce.jgit.transport;
using StructureMap;
using Exception = System.Exception;
using Object = java.lang.Object;
using String = System.String;
using TimeZone = java.util.TimeZone;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class BookmarkController : WindowController<IBookmarkView>
    {
        private const String TimezoneIdPrefixes =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

        private static readonly String Auto = Locale.localizedString("Auto");
        private static readonly String ConnectmodeActive = Locale.localizedString("Active");
        private static readonly String ConnectmodePassive = Locale.localizedString("Passive");
        private static readonly string Default = Locale.localizedString("Default");
        private static readonly Logger Log = Logger.getLogger(typeof (BookmarkController).Name);

        private static readonly string TransferBrowserconnection = Locale.localizedString("Use browser connection");
        private static readonly string TransferNewconnection = Locale.localizedString("Open new connection");

        private static readonly TimeZone UTC = TimeZone.getTimeZone("UTC");
        private readonly AbstractCollectionListener _bookmarkCollectionListener;

        private readonly Host _host;
        private readonly Object _syncRootFavicon = new Object();
        private readonly Object _syncRootReachability = new Object();
        private readonly Timer _ticklerFavicon;
        private readonly Timer _ticklerRechability;

        private BookmarkController(IBookmarkView view, Host host)
        {
            _host = host;
            View = view;

            _ticklerRechability = new Timer(OnRechability, null, Timeout.Infinite, Timeout.Infinite);
            _ticklerFavicon = new Timer(OnFavicon, null, Timeout.Infinite, Timeout.Infinite);

            View.ToggleOptions += View_ToggleOptions;
            View.OptionsVisible = Preferences.instance().getBoolean("bookmark.toggle.options");

            Init();
        }

        private BookmarkController(Host host)
            : this(ObjectFactory.GetInstance<IBookmarkView>(), host)
        {
            _bookmarkCollectionListener = new RemovedCollectionListener(this, host);
        }

        private void OnFavicon(object state)
        {
            Log.debug("OnFavicon");
            background(new FaviconAction(this, _host));
        }

        private void OnRechability(object state)
        {
            Log.debug("OnRechability");
            background(new ReachabilityAction(this, _host));
        }

        private void View_ToggleOptions()
        {
            View.OptionsVisible = !View.OptionsVisible;
        }

        protected override void Invalidate()
        {
            _ticklerRechability.Change(Timeout.Infinite, Timeout.Infinite);
            _ticklerFavicon.Change(Timeout.Infinite, Timeout.Infinite);
            Preferences.instance().setProperty("bookmark.toggle.options", View.OptionsVisible);
            BookmarkCollection.defaultCollection().removeListener(_bookmarkCollectionListener);
            base.Invalidate();
        }

        private void InitTransferModes()
        {
            List<string> modes = new List<string> {Default, TransferNewconnection, TransferBrowserconnection};
            View.PopulateTransferModes(modes);
        }

        private void View_ChangedCommentEvent()
        {
            _host.setComment(View.Notes);
            ItemChanged();
        }

        private void View_ChangedWebURLEvent()
        {
            if (!_host.getWebURL().Equals(View.WebURL))
            {
                _host.setWebURL(View.WebURL);
            }
            UpdateFavicon();
            ItemChanged();
        }

        private void View_ChangedNicknameEvent()
        {
            if (!_host.getNickname().Equals(View.Nickname))
            {
                _host.setNickname(View.Nickname);
            }
            View.WindowTitle = View.Nickname;
            ItemChanged();
        }

        internal void View_ChangedPathEvent()
        {
            _host.setDefaultPath(View.Path);
            ItemChanged();
            Update();
        }

        internal void View_ChangedPortEvent()
        {
            int port;
            if (int.TryParse(View.Port, out port))
            {
                _host.setPort(port);
            }
            else
            {
                _host.setPort(-1);
            }
            ItemChanged();
            Update();
            Reachable();
        }

        private void View_ChangedPublicKeyCheckboxEvent()
        {
            if (View.PkCheckboxState)
            {
                string selectedKeyFile = UserPreferences.HomeFolder;
                if (null != _host.getCredentials().getIdentity())
                {
                    selectedKeyFile = _host.getCredentials().getIdentity().getAbsolute();
                }

                View.ShowPrivateKeyBrowser(selectedKeyFile);
            }
            else
            {
                View_ChangedPrivateKey(this, new PrivateKeyArgs(null));
            }
        }

        private void View_ChangedTransferEvent()
        {
            if (View.SelectedTransferMode.Equals(Default))
            {
                _host.setMaxConnections(null);
            }
            else if (View.SelectedTransferMode.Equals(TransferBrowserconnection))
            {
                _host.setMaxConnections(new Integer(1));
            }
            else if (View.SelectedTransferMode.Equals(TransferNewconnection))
            {
                _host.setMaxConnections(new Integer(-1));
            }
            ItemChanged();
        }

        private void View_ChangedConnectModeEvent()
        {
            if (View.SelectedConnectMode.Equals(Default))
            {
                _host.setFTPConnectMode(null);
            }
            else if (View.SelectedConnectMode.Equals(ConnectmodeActive))
            {
                _host.setFTPConnectMode(FTPConnectMode.ACTIVE);
            }
            else if (View.SelectedConnectMode.Equals(ConnectmodePassive))
            {
                _host.setFTPConnectMode(FTPConnectMode.PASV);
            }
            ItemChanged();
        }

        private void View_ChangedTimezoneEvent()
        {
            string selected = View.SelectedTimezone;
            if (selected.Equals(Auto))
            {
                _host.setTimezone(null);
            }
            else
            {
                string[] ids = TimeZone.getAvailableIDs();
                foreach (string id in ids)
                {
                    TimeZone tz;
                    if ((tz = TimeZone.getTimeZone(id)).getID().Equals(selected))
                    {
                        _host.setTimezone(tz);
                        break;
                    }
                }
            }
            ItemChanged();
        }

        private void View_OpenWebUrl()
        {
            Utils.StartProcess(_host.getWebURL());
        }

        private void View_ChangedEncodingEvent()
        {
            Log.debug("encodingSelectionChanged");
            if (View.SelectedEncoding.Equals(Default))
            {
                _host.setEncoding(null);
            }
            else
            {
                _host.setEncoding(View.SelectedEncoding);
            }
            ItemChanged();
        }

        private void UpdateFavicon()
        {
            if (Preferences.instance().getBoolean("bookmark.favicon.download"))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerFavicon.Change(2000, Timeout.Infinite);
            }
        }

        internal void View_ChangedServerEvent()
        {
            String input = View.Hostname;
            if (Protocol.isURL(input))
            {
                _host.init(Host.parse(input).getAsDictionary());
            }
            else
            {
                _host.setHostname(input);
            }
            ReadOpenSshConfiguration();
            ReadPasswordFromKeychain();
            ItemChanged();
            Update();
            Reachable();
        }

        private void InitEncodings()
        {
            List<string> encodings = new List<string> {Default};
            encodings.AddRange(Utils.AvailableCharsets());
            View.PopulateEncodings(encodings);
        }

        private void InitTimezones()
        {
            List<string> timezones = new List<string> {UTC.getID()};

            string[] allTimezones = TimeZone.getAvailableIDs();
            Array.Sort(allTimezones);

            foreach (string timezone in allTimezones)
            {
                if (Regex.IsMatch(timezone, TimezoneIdPrefixes))
                {
                    timezones.Add(TimeZone.getTimeZone(timezone).getID());
                }
            }
            View.PopulateTimezones(timezones);
        }

        private void InitConnectModes()
        {
            List<string> connectModesList = new List<string> {Default, ConnectmodeActive, ConnectmodePassive};
            View.PopulateConnectModes(connectModesList);
        }

        private void View_ChangedProtocolEvent()
        {
            Log.debug("protocolSelectionChanged");
            Protocol selected = View.SelectedProtocol;
            _host.setPort(selected.getDefaultPort());
            if (_host.getProtocol().getDefaultHostname().Equals(_host.getHostname()))
            {
                _host.setHostname(selected.getDefaultHostname());
            }
            if (!selected.isWebUrlConfigurable())
            {
                _host.setWebURL(null);
            }
            if (selected.equals(Protocol.IDISK))
            {
                String member = Preferences.instance().getProperty("iToolsMember");
                if (StringUtils.isNotEmpty(member))
                {
                    // Account name configured in System Preferences
                    _host.getCredentials().setUsername(member);
                    _host.setDefaultPath(Path.DELIMITER + member);
                }
            }
            _host.setProtocol(selected);
            ReadOpenSshConfiguration();
            ItemChanged();
            Update();
            Reachable();
        }

        private void ItemChanged()
        {
            BookmarkCollection.defaultCollection().collectionItemChanged(_host);
        }

        /// <summary>
        /// Update this host credentials from the OpenSSH configuration file in ~/.ssh/config
        /// </summary>
        private void ReadOpenSshConfiguration()
        {
            if (_host.getProtocol().equals(Protocol.SFTP))
            {
                OpenSshConfig.Host entry = OpenSshConfig.create().lookup(_host.getHostname());
                if (null != entry.getIdentityFile())
                {
                    _host.getCredentials().setIdentity(
                        LocalFactory.createLocal(entry.getIdentityFile().getAbsolutePath()));
                }
                if (StringUtils.isNotBlank(entry.getUser()))
                {
                    _host.getCredentials().setUsername(entry.getUser());
                }
            }
            else
            {
                _host.getCredentials().setIdentity(null);
            }
        }

        private void Reachable()
        {
            if (StringUtils.isNotBlank(_host.getHostname()))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerRechability.Change(2000, Timeout.Infinite);
            }
            else
            {
                View.AlertIconEnabled = false;
            }
        }

        internal void View_ChangedUsernameEvent()
        {
            _host.getCredentials().setUsername(View.Username);
            ReadPasswordFromKeychain();
            ItemChanged();
            Update();
        }

        public void Init()
        {
            //set default favicon
            View.Favicon = IconCache.Instance.IconForName("site", 16);

            InitProtocols();
            InitConnectModes();
            InitEncodings();
            InitTimezones();
            InitTransferModes();
            Update();

            View.ChangedProtocolEvent += View_ChangedProtocolEvent;
            View.ChangedPortEvent += View_ChangedPortEvent;
            View.ChangedUsernameEvent += View_ChangedUsernameEvent;
            View.ChangedServerEvent += View_ChangedServerEvent;
            View.ChangedEncodingEvent += View_ChangedEncodingEvent;
            View.ChangedPathEvent += View_ChangedPathEvent;
            View.ChangedTimezoneEvent += View_ChangedTimezoneEvent;
            View.ChangedConnectModeEvent += View_ChangedConnectModeEvent;
            View.ChangedTransferEvent += View_ChangedTransferEvent;
            View.ChangedPublicKeyCheckboxEvent += View_ChangedPublicKeyCheckboxEvent;
            View.ChangedPrivateKey += View_ChangedPrivateKey;
            View.ChangedAnonymousCheckboxEvent += View_ChangedAnonymousCheckboxEvent;
            View.ChangedNicknameEvent += View_ChangedNicknameEvent;
            View.ChangedWebURLEvent += View_ChangedWebURLEvent;
            View.ChangedCommentEvent += View_ChangedCommentEvent;
            View.ChangedBrowserDownloadPathEvent += View_ChangedBrowserDownloadPathEvent;
            View.OpenDownloadFolderBrowserEvent += View_OpenDownloadFolderBrowserEvent;
            View.OpenDownloadFolderEvent += View_OpenDownloadFolderEvent;
            View.OpenUrl += View_OpenUrl;
            View.OpenWebUrl += View_OpenWebUrl;
        }

        private void View_ChangedPrivateKey(object sender, PrivateKeyArgs e)
        {
            _host.getCredentials().setIdentity(null == e.KeyFile ? null : LocalFactory.createLocal(e.KeyFile));
            Update();
            ItemChanged();
        }

        private void View_OpenUrl()
        {
            Utils.StartProcess(_host.toURL());
        }

        private void View_OpenDownloadFolderEvent()
        {
            Utils.StartProcess(_host.getDownloadFolder().getAbsolute());
        }

        private void View_ChangedAnonymousCheckboxEvent()
        {
            if (View.AnonymousChecked)
            {
                View.UsernameEnabled = false;
                View.Username = Preferences.instance().getProperty("connection.login.anon.name");
                View.PasswordEnabled = false;
                View.Password = Preferences.instance().getProperty("connection.login.anon.pass");
            }
            else
            {
                View.UsernameEnabled = true;
                View.Username = Preferences.instance().getProperty("connection.login.name");
                View.PasswordEnabled = true;
                View.Password = String.Empty;
            }
            Update();
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

        private void View_OpenDownloadFolderBrowserEvent()
        {
            View.ShowDownloadFolderBrowser(_host.getDownloadFolder().getAbsolute());
        }

        private void View_ChangedBrowserDownloadPathEvent()
        {
            _host.setDownloadFolder(View.SelectedDownloadFolder);
            ItemChanged();
            Update();
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

        /// <summary>
        /// Gets the image from URL.
        /// </summary>
        /// <param name="url">The URL.</param>
        /// <returns></returns>
        private static Image GetImageFromUrl(string url)
        {
            try
            {
                //note: first call takes a long time since proxy settings are being detected
                //todo proxy handling in general needs to be considered
                HttpWebRequest request = (HttpWebRequest) WebRequest.Create(url);
                request.Proxy = null; // disable proxy auto-detection
                request.Timeout = 5000;
                request.ReadWriteTimeout = 10000;
                HttpWebResponse response = (HttpWebResponse) request.GetResponse();
                if (request.HaveResponse)
                {
                    return Image.FromStream(response.GetResponseStream());
                }
                return null;
            }
            catch (Exception)
            {
                return null;
            }
        }

        private void Update()
        {
            View.WindowTitle = _host.getNickname();
            View.Hostname = _host.getHostname();
            View.HostFieldEnabled = _host.getProtocol().isHostnameConfigurable();
            View.Nickname = _host.getNickname();
            View.DownloadFolder = _host.getDownloadFolder().getAbsolute();
            string url;
            if (StringUtils.isNotBlank(_host.getDefaultPath()))
            {
                url = _host.toURL() + Path.normalize(_host.getDefaultPath());
            }
            else
            {
                url = _host.toURL();
            }
            View.URL = url;
            View.Port = _host.getPort().ToString();
            View.PortFieldEnabled = _host.getProtocol().isHostnameConfigurable();
            View.Path = _host.getDefaultPath();
            View.Username = _host.getCredentials().getUsername();

            if (string.Empty.Equals(_host.getProtocol().getUsernamePlaceholder()))
            {
                //todo korrekter Key
                View.UsernameLabel = Locale.localizedString("Username");
            }
            else
            {
                View.UsernameLabel = _host.getProtocol().getUsernamePlaceholder();
            }
            View.SelectedProtocol = _host.getProtocol();

            if (null == _host.getMaxConnections())
            {
                View.SelectedTransferMode = Default;
            }
            else
            {
                View.SelectedTransferMode = _host.getMaxConnections().intValue() == 1
                                                ? TransferBrowserconnection
                                                : TransferNewconnection;
            }

            View.EncodingFieldEnabled = _host.getProtocol().isEncodingConfigurable();
            View.ConnectModeFieldEnabled = _host.getProtocol().isConnectModeConfigurable();
            if (_host.getProtocol().isConnectModeConfigurable())
            {
                if (null == _host.getFTPConnectMode())
                {
                    View.SelectedConnectMode = Default;
                }
                else if (_host.getFTPConnectMode().equals(FTPConnectMode.PASV))
                {
                    View.SelectedConnectMode = ConnectmodePassive;
                }
                else if (_host.getFTPConnectMode().equals(FTPConnectMode.ACTIVE))
                {
                    View.SelectedConnectMode = ConnectmodeActive;
                }
            }
            View.PkCheckboxEnabled = _host.getProtocol().equals(Protocol.SFTP);
            if (_host.getCredentials().isPublicKeyAuthentication())
            {
                View.PkCheckboxState = true;
                View.PkLabel = _host.getCredentials().getIdentity().getAbbreviatedPath();
            }
            else
            {
                View.PkCheckboxState = false;
                View.PkLabel = Locale.localizedString("No Private Key selected");
            }
            View.WebUrlFieldEnabled = _host.getProtocol().isWebUrlConfigurable();
            View.WebUrlButtonToolTip = _host.getWebURL();
            View.WebURL = _host.getWebURL();
            View.Notes = _host.getComment();
            View.TimezoneFieldEnabled = !_host.getProtocol().isUTCTimezone();
            if (null == _host.getTimezone())
            {
                if (_host.getProtocol().isUTCTimezone())
                {
                    View.SelectedTimezone = UTC.getID();
                }
                else
                {
                    if (Preferences.instance().getBoolean("ftp.timezone.auto"))
                    {
                        View.SelectedTimezone = Auto;
                    }
                    else
                    {
                        View.SelectedTimezone =
                            TimeZone.getTimeZone(
                                Preferences.instance().getProperty("ftp.timezone.default")).getID();
                    }
                }
            }
            else
            {
                View.SelectedTimezone = _host.getTimezone().getID();
            }
        }

        public static class Factory
        {
            private static readonly IDictionary<Host, BookmarkController> Open =
                new Dictionary<Host, BookmarkController>();

            public static BookmarkController Create(Host host)
            {
                BookmarkController c;
                if (Open.TryGetValue(host, out c))
                {
                    return c;
                }
                c = new BookmarkController(host);
                c.View.ViewClosedEvent += () => Open.Remove(host);
                Open.Add(host, c);
                return c;
            }
        }

        private class FaviconAction : AbstractBackgroundAction
        {
            private readonly BookmarkController _controller;
            private readonly Host _host;
            private Image _favicon;

            public FaviconAction(BookmarkController controller, Host host)
            {
                _controller = controller;
                _host = host;
            }

            public override void run()
            {
                //try to find the favicon in the root folder
                Uri url = new Uri(_host.getWebURL());
                UriBuilder builder = new UriBuilder(url.Scheme, url.Host, url.Port);
                _favicon = GetImageFromUrl(builder.Uri + "/favicon.ico");
            }

            public override void cleanup()
            {
                if (null != _favicon)
                {
                    _controller.View.Favicon = _favicon;
                }
                else
                {
                    _controller.View.Favicon = IconCache.Instance.IconForName("site", 16);
                }
            }

            public override object @lock()
            {
                return _controller._syncRootFavicon;
            }
        }

        private class ReachabilityAction : AbstractBackgroundAction
        {
            private readonly BookmarkController _controller;
            private readonly Host _host;
            private bool _reachable;

            public ReachabilityAction(BookmarkController controller, Host host)
            {
                _controller = controller;
                _host = host;
            }

            public override void run()
            {
                _reachable = _host.isReachable();
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

        private class RemovedCollectionListener : AbstractCollectionListener
        {
            private readonly BookmarkController _controller;
            private readonly Host _host;

            public RemovedCollectionListener(BookmarkController controller, Host host)
            {
                _controller = controller;
                _host = host;
            }

            public override void collectionItemRemoved(object item)
            {
                if (item.Equals(_host))
                {
                    _controller.View.Close();
                }
            }
        }
    }
}