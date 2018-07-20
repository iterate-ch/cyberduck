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
using ch.cyberduck.ui;
using ch.cyberduck.ui.browser;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Winforms.Controls;
using java.util;
using org.apache.log4j;
using StructureMap;
using Path = System.IO.Path;
using Timer = System.Threading.Timer;
using TimeZone = java.util.TimeZone;

namespace Ch.Cyberduck.Ui.Controller
{
    public class BookmarkController : BookmarkController<IBookmarkView>
    {
        private BookmarkController(Host host) : base(host)
        {
        }
    }

    public class BookmarkController<T> : WindowController<T> where T : IBookmarkView
    {
        private const String TimezoneIdPrefixes = "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";
        public const int SmallBookmarkSize = 16;
        public const int MediumBookmarkSize = 32;
        public const int LargeBookmarkSize = 64;
        private static readonly string Default = LocaleFactory.localizedString("Default");
        private static readonly Logger Log = Logger.getLogger(typeof(BookmarkController<>).FullName);
        private static readonly TimeZone UTC = TimeZone.getTimeZone("UTC");
        private readonly AbstractCollectionListener _bookmarkCollectionListener;
        private readonly List<string> _keys = new List<string> {LocaleFactory.localizedString("None")};

        protected readonly Host _host;
        protected readonly LoginOptions _options;
        protected readonly LoginInputValidator _validator;

        private readonly Timer _ticklerFavicon;
        private readonly Timer _ticklerReachability;

        private BookmarkController(T view, Host host, LoginInputValidator validator,
            LoginOptions options) : base(validator)
        {
            _host = host;
            _validator = validator;
            _options = options;
            View = view;

            _ticklerReachability = new Timer(OnRechability, null, Timeout.Infinite, Timeout.Infinite);
            _ticklerFavicon = new Timer(OnFavicon, null, Timeout.Infinite, Timeout.Infinite);

            View.ToggleOptions += View_ToggleOptions;
            View.OptionsVisible = PreferencesFactory.get().getBoolean(ToggleProperty);

            Init();
        }

        protected BookmarkController(Host host) : this(host,
            new LoginOptions(host.getProtocol()))
        {
        }

        protected BookmarkController(Host host,
            LoginOptions options) : this(ObjectFactory.GetInstance<T>(), host,
            new LoginInputValidator(host.getCredentials(), host.getProtocol(), options), options)
        {
        }

        protected BookmarkController(Host host, LoginInputValidator validator,
            LoginOptions options) : this(ObjectFactory.GetInstance<T>(), host,
            validator, options)
        {
            _bookmarkCollectionListener = new RemovedCollectionListener(this, host);
        }

        protected virtual String ToggleProperty => "bookmark.toggle.options";

        public Host Bookmark() => _host;

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
            PreferencesFactory.get().setProperty(ToggleProperty, View.OptionsVisible);
            FolderBookmarkCollection.favoritesCollection().removeListener(_bookmarkCollectionListener);
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
            BrowserLauncherFactory.get().open(new WebUrlProvider(_host).toUrl().getUrl());
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
                _host.setHostname(parsed.getHostname(), true);
                _host.setProtocol(parsed.getProtocol(), true);
                _host.setPort(parsed.getPort());
                _host.setDefaultPath(parsed.getDefaultPath());
            }
            else
            {
                _host.setHostname(input, true);
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
            if (Objects.equals(_host.getDefaultPath(), _host.getProtocol().getDefaultPath()) ||
                !selected.isPathConfigurable())
            {
                _host.setDefaultPath(selected.getDefaultPath());
            }
            _host.setProtocol(selected, true);
            _options.configure(selected);
            _validator.configure(selected);
            ItemChanged();
            Update();
            Reachable();
        }

        protected virtual void ItemChanged()
        {
            FolderBookmarkCollection.favoritesCollection().collectionItemChanged(_host);
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
            ItemChanged();
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
            ProtocolFactory p = ProtocolFactory.get();
            foreach (Protocol protocol in p.find(new DefaultProtocolPredicate(
                EnumSet.of(Protocol.Type.ftp, Protocol.Type.sftp, Protocol.Type.dav))).toArray(new Protocol[] { }))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(protocol, protocol.getDescription(),
                    protocol.disk()));
            }
            foreach (Protocol protocol in p.find(new DefaultProtocolPredicate(
                EnumSet.of(Protocol.Type.s3, Protocol.Type.swift, Protocol.Type.azure, Protocol.Type.b2, Protocol.Type.dracoon,
                    Protocol.Type.googlestorage))).toArray(new Protocol[] { }))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(protocol, protocol.getDescription(),
                    protocol.disk()));
            }
            foreach (Protocol protocol in p.find(new DefaultProtocolPredicate(
                    EnumSet.of(Protocol.Type.dropbox, Protocol.Type.onedrive, Protocol.Type.googledrive)))
                .toArray(new Protocol[] { }))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(protocol, protocol.getDescription(),
                    protocol.disk()));
            }
            foreach (Protocol protocol in p.find(new DefaultProtocolPredicate(
                EnumSet.of(Protocol.Type.file))).toArray(new Protocol[] { }))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(protocol, protocol.getDescription(),
                    protocol.disk()));
            }
            foreach (Protocol protocol in p.find(new ProfileProtocolPredicate()).toArray(new Protocol[] { }))
            {
                protocols.Add(new KeyValueIconTriple<Protocol, string>(protocol, protocol.getDescription(),
                    protocol.disk()));
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

        protected virtual void Update()
        {
            View.WindowTitle = BookmarkNameProvider.toString(_host);
            View.Hostname = _host.getHostname();
            View.HostFieldEnabled = _host.getProtocol().isHostnameConfigurable();
            View.Nickname = BookmarkNameProvider.toString(_host);
            View.DownloadFolder = new DownloadDirectoryFinder().find(_host).getAbsolute();
            View.URL = new HostUrlProvider(true, true).get(_host);
            View.Port = _host.getPort().ToString();
            View.PortFieldEnabled = _host.getProtocol().isPortConfigurable();
            View.PathFieldEnabled = _host.getProtocol().isPathConfigurable();
            View.Path = _host.getDefaultPath();
            View.Username = _host.getCredentials().getUsername();
            View.UsernameEnabled = _options.user() && !_host.getCredentials().isAnonymousLogin();
            View.UsernameLabel = _host.getProtocol().getUsernamePlaceholder() + ":";
            View.AnonymousEnabled = _options.anonymous();
            View.AnonymousChecked = _host.getCredentials().isAnonymousLogin();
            View.SelectedProtocol = _host.getProtocol();
            View.SelectedTransferMode = _host.getTransferType();
            View.SelectedEncoding = _host.getEncoding() == null ? Default : _host.getEncoding();
            View.EncodingFieldEnabled = _host.getProtocol().isEncodingConfigurable();
            View.ConnectModeFieldEnabled = _host.getProtocol().getType() == Protocol.Type.ftp;
            if (_host.getProtocol().getType() == Protocol.Type.ftp)
            {
                View.SelectedConnectMode = _host.getFTPConnectMode();
            }
            View.PrivateKeyFieldEnabled = _options.publickey();

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
            View.ClientCertificateFieldEnabled = _options.certificate();
            List<string> keys = new List<string> {LocaleFactory.localizedString("None")};
            if(_options.certificate())
            {
                foreach (String certificate in Utils.ConvertFromJavaList<String>(new KeychainX509KeyManager(_host).list()))
                {
                    keys.Add(certificate);
                }
            }
            View.PopulateClientCertificates(keys);
            if (_host.getCredentials().isCertificateAuthentication())
            {
                View.SelectedClientCertificate = _host.getCredentials().getCertificate();
            }
            else
            {
                View.SelectedClientCertificate = LocaleFactory.localizedString("None");
            }
            View.WebUrlButtonToolTip = new WebUrlProvider(_host).toUrl().getUrl();
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
            private static readonly IDictionary<Host, BookmarkController<T>> Open =
                new Dictionary<Host, BookmarkController<T>>();

            public static BookmarkController<T> Create(Host host)
            {
                BookmarkController<T> c;
                if (Open.TryGetValue(host, out c))
                {
                    return c;
                }
                c = new BookmarkController<T>(host);
                c.View.ViewClosedEvent += () => Open.Remove(host);
                Open.Add(host, c);
                return c;
            }
        }

        private class FaviconAction : AbstractBackgroundAction
        {
            private readonly BookmarkController<T> _controller;
            private readonly Host _host;
            private Image _favicon;

            public FaviconAction(BookmarkController<T> controller, Host host)
            {
                _controller = controller;
                _host = host;
            }

            public override object run()
            {
                //try to find the favicon in the root folder
                try
                {
                    Uri url = new Uri(new WebUrlProvider(_host).toUrl().getUrl());
                    UriBuilder builder = new UriBuilder(url.Scheme, url.Host, url.Port);
                    _favicon = GetImageFromUrl(builder.Uri + "favicon.ico");
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
        }

        private class ReachabilityAction : AbstractBackgroundAction
        {
            private readonly BookmarkController<T> _controller;
            private readonly Host _host;
            private bool _reachable;

            public ReachabilityAction(BookmarkController<T> controller, Host host)
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
        }

        private class RemovedCollectionListener : AbstractCollectionListener
        {
            private readonly BookmarkController<T> _controller;
            private readonly Host _host;

            public RemovedCollectionListener(BookmarkController<T> controller, Host host)
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
