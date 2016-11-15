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
using System.Collections.Generic;
using System.Drawing;
using System.Net;
using System.Text.RegularExpressions;
using System.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.diagnostics;
using ch.cyberduck.core.ftp;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.sftp.openssh;
using ch.cyberduck.core.ssl;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.browser;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Winforms.Controls;
using org.apache.log4j;
using StructureMap;
using Object = java.lang.Object;
using Path = System.IO.Path;
using TimeZone = java.util.TimeZone;

namespace Ch.Cyberduck.Ui.Controller
{
    public sealed class BookmarkController : WindowController<IBookmarkView>
    {
        private const String TimezoneIdPrefixes = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
        public const int SmallBookmarkSize = 16;
        public const int MediumBookmarkSize = 32;
        public const int LargeBookmarkSize = 64;
        private static readonly string Default = LocaleFactory.localizedString("Default");
        private static readonly Logger Log = Logger.getLogger(typeof(BookmarkController).FullName);
        private static readonly TimeZone UTC = TimeZone.getTimeZone("UTC");
        private readonly AbstractCollectionListener _bookmarkCollectionListener;
        private readonly Host _host;
        private readonly List<string> _keys = new List<string> {LocaleFactory.localizedString("None")};
        private readonly Object _syncRootFavicon = new Object();
        private readonly Object _syncRootReachability = new Object();
        private readonly Timer _ticklerFavicon;
        private readonly Timer _ticklerReachability;

        private BookmarkController(IBookmarkView view, Host host)
        {
            _host = host;
            View = view;

            _ticklerReachability = new Timer(OnRechability, null, Timeout.Infinite, Timeout.Infinite);
            _ticklerFavicon = new Timer(OnFavicon, null, Timeout.Infinite, Timeout.Infinite);

            View.ToggleOptions += View_ToggleOptions;
            View.OptionsVisible = PreferencesFactory.get().getBoolean("bookmark.toggle.options");

            Init();
        }

        private BookmarkController(Host host) : this(ObjectFactory.GetInstance<IBookmarkView>(), host)
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
            _ticklerReachability.Change(Timeout.Infinite, Timeout.Infinite);
            _ticklerFavicon.Change(Timeout.Infinite, Timeout.Infinite);
            PreferencesFactory.get().setProperty("bookmark.toggle.options", View.OptionsVisible);
            BookmarkCollection.defaultCollection().removeListener(_bookmarkCollectionListener);
            base.Invalidate();
        }

        private void InitTransferModes()
        {
            List<KeyValuePair<string, Host.TransferType>> modes = new List<KeyValuePair<string, Host.TransferType>>();
            Host.TransferType unknown = Host.TransferType.unknown;
            modes.Add(new KeyValuePair<string, Host.TransferType>(unknown.toString(), unknown));
            foreach (
                String name in
                Utils.ConvertFromJavaList<String>(PreferencesFactory.get().getList("queue.transfer.type.enabled")))
            {
                Host.TransferType t = Host.TransferType.valueOf(name);
                modes.Add(new KeyValuePair<string, Host.TransferType>(t.toString(), t));
            }
            View.PopulateTransferModes(modes);
        }

        private void View_ChangedCommentEvent()
        {
            _host.setComment(View.Notes);
            ItemChanged();
        }

        private void View_ChangedWebURLEvent()
        {
            _host.setWebURL(View.WebURL);
            UpdateFavicon();
            ItemChanged();
        }

        private void View_ChangedNicknameEvent()
        {
            _host.setNickname(View.Nickname);
            ItemChanged();
            Update();
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

        private void View_OpenPrivateKeyBrowserEvent()
        {
            string selectedKeyFile = PreferencesFactory.get().getProperty("local.user.home");
            if (null != _host.getCredentials().getIdentity())
            {
                selectedKeyFile = Path.GetDirectoryName(_host.getCredentials().getIdentity().getAbsolute());
            }
            View.ShowPrivateKeyBrowser(selectedKeyFile);
        }

        private void View_ChangedTransferEvent()
        {
            _host.setTransfer(View.SelectedTransferMode);
            ItemChanged();
        }

        private void View_ChangedConnectModeEvent()
        {
            _host.setFTPConnectMode(View.SelectedConnectMode);
            ItemChanged();
        }

        private void View_ChangedTimezoneEvent()
        {
            string selected = View.SelectedTimezone;
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
            ItemChanged();
        }

        private void View_OpenWebUrl()
        {
            BrowserLauncherFactory.get().open(_host.getWebURL());
        }

        private void View_ChangedEncodingEvent()
        {
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
            if (PreferencesFactory.get().getBoolean("bookmark.favicon.download"))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerFavicon.Change(2000, Timeout.Infinite);
            }
        }

        internal void View_ChangedServerEvent()
        {
            String input = View.Hostname;
            if (Scheme.isURL(input))
            {
                Host parsed = HostParser.parse(input);
                _host.setHostname(parsed.getHostname());
                _host.setProtocol(parsed.getProtocol());
                _host.setPort(parsed.getPort());
                _host.setDefaultPath(parsed.getDefaultPath());
            }
            else
            {
                _host.setHostname(input);
            }
            ItemChanged();
            Update();
            Reachable();
        }

        private void InitEncodings()
        {
            List<string> encodings = new List<string> {Default};
            encodings.AddRange(new DefaultCharsetProvider().availableCharsets());
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
            List<KeyValuePair<string, FTPConnectMode>> modes = new List<KeyValuePair<string, FTPConnectMode>>();
            foreach (FTPConnectMode m in FTPConnectMode.values())
            {
                modes.Add(new KeyValuePair<string, FTPConnectMode>(m.toString(), m));
            }
            View.PopulateConnectModes(modes);
        }

        private void View_ChangedProtocolEvent()
        {
            Protocol selected = View.SelectedProtocol;
            _host.setPort(selected.getDefaultPort());
            if (!_host.getProtocol().isHostnameConfigurable())
            {
                // Previously selected protocol had a default hostname. Change to default
                // of newly selected protocol.
                _host.setHostname(selected.getDefaultHostname());
            }
            if (!selected.isHostnameConfigurable())
            {
                // Hostname of newly selected protocol is not configurable. Change to default.
                _host.setHostname(selected.getDefaultHostname());
            }
            if (Utils.IsNotBlank(selected.getDefaultHostname()))
            {
                // Prefill with default hostname
                _host.setHostname(selected.getDefaultHostname());
            }
            _host.setProtocol(selected);
            ItemChanged();
            Update();
            Reachable();
        }

        private void ItemChanged()
        {
            BookmarkCollection.defaultCollection().collectionItemChanged(_host);
        }

        private void Reachable()
        {
            if (Utils.IsNotBlank(_host.getHostname()))
            {
                // Delay to 2 second. When typing changes we don't have to check the reachbility for each stroke.
                _ticklerReachability.Change(2000, Timeout.Infinite);
            }
            else
            {
                View.AlertIconEnabled = false;
            }
        }

        internal void View_ChangedUsernameEvent()
        {
            _host.getCredentials().setUsername(View.Username);
            ItemChanged();
            Update();
        }

        public void Init()
        {
            //set default favicon
            View.Favicon = IconCache.Instance.IconForName("site", 16);

            InitProtocols();
            InitPrivateKeys();
            InitClientCertificates();
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
            View.ChangedAnonymousCheckboxEvent += View_ChangedAnonymousCheckboxEvent;
            View.ChangedPrivateKeyEvent += View_ChangedPrivateKeyEvent;
            View.OpenPrivateKeyBrowserEvent += View_OpenPrivateKeyBrowserEvent;
            View.ChangedClientCertificateEvent += View_ChangedClientCertificateEvent;
            View.ChangedNicknameEvent += View_ChangedNicknameEvent;
            View.ChangedWebURLEvent += View_ChangedWebURLEvent;
            View.ChangedCommentEvent += View_ChangedCommentEvent;
            View.ChangedBrowserDownloadPathEvent += View_ChangedBrowserDownloadPathEvent;
            View.OpenDownloadFolderBrowserEvent += View_OpenDownloadFolderBrowserEvent;
            View.OpenDownloadFolderEvent += View_OpenDownloadFolderEvent;
            View.OpenUrl += View_OpenUrl;
            View.OpenWebUrl += View_OpenWebUrl;
        }

        private void View_ChangedClientCertificateEvent()
        {
            _host.getCredentials().setCertificate(View.SelectedClientCertificate);
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

        private void InitClientCertificates()
        {
            List<string> keys = new List<string> {LocaleFactory.localizedString("None")};
            foreach (String certificate in Utils.ConvertFromJavaList<String>(new KeychainX509KeyManager(_host).list()))
            {
                keys.Add(certificate);
            }
            View.PopulateClientCertificates(keys);
        }

        private void View_ChangedPrivateKeyEvent(object sender, PrivateKeyArgs e)
        {
            _host.getCredentials()
                .setIdentity(null == e.KeyFile || e.KeyFile.Equals(LocaleFactory.localizedString("None"))
                    ? null
                    : LocalFactory.get(e.KeyFile));
            Update();
            ItemChanged();
        }

        private void View_OpenUrl()
        {
            BrowserLauncherFactory.get().open(new HostUrlProvider().get(_host));
        }

        private void View_OpenDownloadFolderEvent()
        {
            Local folder = new DownloadDirectoryFinder().find(_host);
            ApplicationLauncherFactory.get().open(LocalFactory.get(folder.getAbsolute()));
        }

        private void View_ChangedAnonymousCheckboxEvent()
        {
            if (View.AnonymousChecked)
            {
                View.UsernameEnabled = false;
                View.Username = PreferencesFactory.get().getProperty("connection.login.anon.name");
            }
            else
            {
                View.UsernameEnabled = true;
                if (
                    PreferencesFactory.get()
                        .getProperty("connection.login.name")
                        .Equals(PreferencesFactory.get().getProperty("connection.login.anon.name")))
                {
                    View.Username = String.Empty;
                }
                else
                {
                    View.Username = PreferencesFactory.get().getProperty("connection.login.name");
                }
            }
            ItemChanged();
            Update();
        }

        private void View_OpenDownloadFolderBrowserEvent()
        {
            Local folder = new DownloadDirectoryFinder().find(_host);
            View.ShowDownloadFolderBrowser(folder.getAbsolute());
        }

        private void View_ChangedBrowserDownloadPathEvent()
        {
            _host.setDownloadFolder(LocalFactory.get(View.SelectedDownloadFolder));
            ItemChanged();
            Update();
        }

        private void InitProtocols()
        {
            List<KeyValueIconTriple<Protocol, string>> protocols = new List<KeyValueIconTriple<Protocol, string>>();
            foreach (Protocol p in ProtocolFactory.getEnabledProtocols().toArray(new Protocol[] {}))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(p, p.getDescription(), p.getProvider()));
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
                // Note: First call takes a long time since proxy settings are being detected
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
            View.WindowTitle = BookmarkNameProvider.toString(_host);
            View.Hostname = _host.getHostname();
            View.HostFieldEnabled = _host.getProtocol().isHostnameConfigurable();
            View.Nickname = BookmarkNameProvider.toString(_host);
            View.DownloadFolder = new DownloadDirectoryFinder().find(_host).getAbsolute();
            View.URL = new HostUrlProvider(true, true).get(_host);
            View.Port = _host.getPort().ToString();
            View.PortFieldEnabled = _host.getProtocol().isPortConfigurable();
            View.Path = _host.getDefaultPath();
            View.Username = _host.getCredentials().getUsername();
            View.UsernameEnabled = !_host.getCredentials().isAnonymousLogin();
            View.UsernameLabel = _host.getProtocol().getUsernamePlaceholder() + ":";
            View.AnonymousEnabled = _host.getProtocol().isAnonymousConfigurable();
            View.AnonymousChecked = _host.getCredentials().isAnonymousLogin();
            View.SelectedProtocol = _host.getProtocol();
            View.SelectedTransferMode = _host.getTransfer();
            View.SelectedEncoding = _host.getEncoding() == null ? Default : _host.getEncoding();
            View.EncodingFieldEnabled = _host.getProtocol().isEncodingConfigurable();
            View.ConnectModeFieldEnabled = _host.getProtocol().getType() == Protocol.Type.ftp;
            if (_host.getProtocol().getType() == Protocol.Type.ftp)
            {
                View.SelectedConnectMode = _host.getFTPConnectMode();
            }
            View.PrivateKeyFieldEnabled = _host.getProtocol().getType() == Protocol.Type.sftp;

            if (_host.getCredentials().isPublicKeyAuthentication())
            {
                String key = _host.getCredentials().getIdentity().getAbsolute();
                if (!_keys.Contains(key))
                {
                    _keys.Add(key);
                    View.PopulatePrivateKeys(_keys);
                }
                View.SelectedPrivateKey = key;
            }
            else
            {
                View.SelectedPrivateKey = LocaleFactory.localizedString("None");
            }
            View.ClientCertificateFieldEnabled = _host.getProtocol().getScheme() == Scheme.https;
            if (_host.getCredentials().isCertificateAuthentication())
            {
                View.SelectedClientCertificate = _host.getCredentials().getCertificate();
            }
            else
            {
                View.SelectedClientCertificate = LocaleFactory.localizedString("None");
            }
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
                    View.SelectedTimezone =
                        TimeZone.getTimeZone(PreferencesFactory.get().getProperty("ftp.timezone.default")).getID();
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

            public override object run()
            {
                //try to find the favicon in the root folder
                try
                {
                    Uri url = new Uri(_host.getWebURL());
                    UriBuilder builder = new UriBuilder(url.Scheme, url.Host, url.Port);
                    _favicon = GetImageFromUrl(builder.Uri + "/favicon.ico");
                }
                catch
                {
                    //catch silently
                }
                return true;
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

            public override object run()
            {
                _reachable = ReachabilityFactory.get().isReachable(_host);
                return true;
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