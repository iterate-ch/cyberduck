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

using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using ch.cyberduck.core.azure;
using ch.cyberduck.core.b2;
using ch.cyberduck.core.bonjour;
using ch.cyberduck.core.box;
using ch.cyberduck.core.brick;
using ch.cyberduck.core.ctera;
using ch.cyberduck.core.dav;
using ch.cyberduck.core.dropbox;
using ch.cyberduck.core.eue;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.ftp;
using ch.cyberduck.core.googledrive;
using ch.cyberduck.core.googlestorage;
using ch.cyberduck.core.hubic;
using ch.cyberduck.core.importer;
using ch.cyberduck.core.irods;
using ch.cyberduck.core.local;
using ch.cyberduck.core.manta;
using ch.cyberduck.core.nextcloud;
using ch.cyberduck.core.nio;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.oauth;
using ch.cyberduck.core.onedrive;
using ch.cyberduck.core.openstack;
using ch.cyberduck.core.owncloud;
using ch.cyberduck.core.pool;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.profiles;
using ch.cyberduck.core.s3;
using ch.cyberduck.core.sds;
using ch.cyberduck.core.serializer;
using ch.cyberduck.core.sftp;
using ch.cyberduck.core.spectra;
using ch.cyberduck.core.storegate;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.transfer;
using ch.cyberduck.core.updater;
using ch.cyberduck.core.urlhandler;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Sparkle;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Core.Contracts;
using java.util;
using org.apache.logging.log4j;
using ReactiveUI;
using Splat;
using StructureMap;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.ServiceModel;
using System.ServiceModel.Description;
using System.Threading;
using System.Web;
using System.Windows.Forms;
using System.Windows.Shell;
using Windows.Services.Store;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;
using Application = ch.cyberduck.core.local.Application;
using UnhandledExceptionEventArgs = System.UnhandledExceptionEventArgs;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// A potential alternative for the VB.WindowsFormsApplicationBase: http://www.ai.uga.edu/mc/SingleInstance.html
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.Single)]
    internal class MainController : ApplicationContext, CollectionListener, ICyberduck
    {
        public static readonly string StartupLanguage;
        private static readonly IList<BrowserController> _browsers = new List<BrowserController>();
        private static readonly Logger Logger = LogManager.getLogger(typeof(MainController).FullName);
        private static MainController _application;
        private static JumpList _jumpListManager;
        private static AutoResetEvent applicationShutdown = new AutoResetEvent(true);
        private readonly BaseController _controller;
        private readonly PathKindDetector _detector = new DefaultPathKindDetector();
        private readonly SynchronizationContext mainThreadSync;

        /// <summary>
        /// Saved browsers
        /// </summary>
        private readonly AbstractHostCollection _sessions =
            new BookmarkCollection(
                LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "Sessions"),
                "session");

        private readonly CountdownEvent transfersSemaphore = new CountdownEvent(1);

        /// <summary>
        /// Helper controller to ensure STA when running threads while launching
        /// </summary>
        /// <see cref="http://msdn.microsoft.com/en-us/library/system.stathreadattribute.aspx"/>
        private BrowserController _bc;

        private WinSparkle.win_sparkle_can_shutdown_callback_t _canShutdownCallback;
        private ProfilesUpdater _profiles;
        private WinSparkle.win_sparkle_shutdown_request_callback_t _shutdownRequestCallback;
        private PeriodicUpdateChecker _updater;
        private ServiceHost serviceHost;

        /// <summary>
        /// Default constructor.
        /// </summary>
        public MainController(ProtocolFactory protocolFactory, BaseController controller, PeriodicProfilesUpdater profiles)
        {
            _application = this;
            _controller = controller;
            _profiles = profiles;

            if (!(Debugger.IsAttached || Utils.IsRunningAsUWP))
            {
                // Add the event handler for handling UI thread exceptions to the event.
                System.Windows.Forms.Application.ThreadException += ExceptionHandler;

                // Set the unhandled exception mode to force all Windows Forms errors to go through
                // our handler.
                System.Windows.Forms.Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);

                // Add the event handler for handling non-UI thread exceptions to the event.
                AppDomain.CurrentDomain.UnhandledException += UnhandledExceptionHandler;
            }

            // Initialize WindowsFormsSynchronizationContext (sets SynchronizationContext.Current)
            SynchronizationContext.SetSynchronizationContext(new WindowsFormsSynchronizationContext());
            mainThreadSync = SynchronizationContext.Current;

            protocolFactory.register(new FTPProtocol(), new FTPTLSProtocol(), new SFTPProtocol(), new DAVProtocol(),
                new DAVSSLProtocol(), new SwiftProtocol(), new S3Protocol(), new GoogleStorageProtocol(),
                new AzureProtocol(), new IRODSProtocol(), new SpectraProtocol(), new B2Protocol(), new DriveProtocol(),
                new DropboxProtocol(), new HubicProtocol(), new LocalProtocol(), new OneDriveProtocol(), new SharepointProtocol(), new SharepointSiteProtocol(),
                new MantaProtocol(), new SDSProtocol(), new StoregateProtocol(), new BrickProtocol(), new NextcloudProtocol(), new OwncloudProtocol(), new CteraProtocol(), new BoxProtocol(), new EueProtocol());
            protocolFactory.load();

            Locator.SetLocator(new StructureMapBootstrapper.SplatDependencyResolver());
            Locator.CurrentMutable.InitializeSplat();
            Locator.CurrentMutable.InitializeReactiveUI();

            // Execute OnStartup later
            mainThreadSync.Post(OnStartup, null);
        }

        public static MainController Application => _application;

        public static IList<BrowserController> Browsers
        {
            get { return _browsers; }
        }

        public Form ActiveMainForm
        {
            get { return MainForm; }
        }

        public FormCollection OpenForms => System.Windows.Forms.Application.OpenForms;

        /// <summary>
        ///
        /// </summary>
        /// <returns>Return true to allow the application to terminate</returns>
        public static bool ApplicationShouldTerminate()
        {
            Logger.debug("ApplicationShouldTerminate");

            // Determine if there are any running transfers
            bool terminate = TransferController.ApplicationShouldTerminate();
            if (!terminate)
            {
                return false;
            }

            // Determine if there are any open connections
            foreach (BrowserController controller in new List<BrowserController>(Browsers))
            {
                if (PreferencesFactory.get().getBoolean("browser.serialize"))
                {
                    if (controller.IsMounted())
                    {
                        // The workspace should be saved. Serialize all open browser sessions
                        Host serialized =
                            new HostDictionary().deserialize(
                                controller.Session.getHost().serialize(SerializerFactory.get()));
                        serialized.setWorkdir(controller.Workdir);
                        Application._sessions.add(serialized);
                    }
                }
            }
            return true;
        }

        public static bool ApplicationShouldTerminateAfterDonationPrompt()
        {
            Logger.debug("ApplicationShouldTerminateAfterDonationPrompt");
            License l = LicenseFactory.find();
            if (!l.verify(new DisabledLicenseVerifierCallback()))
            {
                string appVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();
                String lastversion = PreferencesFactory.get().getProperty("donate.reminder");
                if (appVersion.Equals(lastversion))
                {
                    // Do not display if same version is installed
                    return true;
                }

                DateTime nextReminder = new DateTime(PreferencesFactory.get().getLong("donate.reminder.date"));
                // Display prompt every n days
                nextReminder.AddDays(PreferencesFactory.get().getLong("donate.reminder.interval"));
                Logger.debug("Next reminder: " + nextReminder);
                // Display after upgrade
                if (nextReminder.CompareTo(DateTime.Now) == 1)
                {
                    // Do not display if shown in the reminder interval
                    return true;
                }
                ObjectFactory.GetInstance<IDonationController>().Show();
            }
            return true;
        }

        public static void ExceptionHandler(object sender, ThreadExceptionEventArgs e)
        {
            CrashReporter.Instance.Write(e.Exception);
            Environment.Exit(1);
        }

        public static void Exit(bool updateInProgress)
        {
            // Already shutting down. Do nothing.
            if (!applicationShutdown.WaitOne(0))
            {
                return;
            }

            NotificationServiceFactory.get().unregister();
            if (!updateInProgress && PrepareExit())
            {
                ApplicationShouldTerminateAfterDonationPrompt();
            }
            DefaultBackgroundExecutor.get().shutdown();
            _application.Shutdown(updateInProgress);
            _application.ExitThreadCore();
        }

        public static BrowserController NewBrowser()
        {
            return NewBrowser(false);
        }

        /// <summary>
        /// Makes a unmounted browser window the key window and brings it to the front
        /// </summary>
        /// <param name="force">If true, open a new browser regardeless of any unused browser window</param>
        /// <returns>A reference to a browser window</returns>
        public static BrowserController NewBrowser(bool force)
        {
            return NewBrowser(force, true);
        }

        /// <summary>
        /// Mounts the default bookmark if any
        /// </summary>
        /// <param name="controller"></param>
        public static void OpenDefaultBookmark(BrowserController controller)
        {
            String defaultBookmark = PreferencesFactory.get().getProperty("browser.open.bookmark.default");
            if (null == defaultBookmark)
            {
                return; //No default bookmark given
            }
            Host bookmark = BookmarkCollection.defaultCollection().lookup(defaultBookmark);
            if (null == bookmark)
            {
                Logger.info("Default bookmark no more available");
                return;
            }
            foreach (BrowserController browser in Browsers)
            {
                if (browser.IsMounted())
                {
                    if (bookmark.equals(browser.Session.getHost()))
                    {
                        Logger.debug("Default bookmark already mounted");
                        return;
                    }
                }
            }
            Logger.debug("Mounting default bookmark " + bookmark);
            controller.Mount(bookmark);
        }

        public static bool PrepareExit()
        {
            bool readyToExit = true;
            foreach (BrowserController controller in new List<BrowserController>(Browsers))
            {
                if (controller.IsConnected())
                {
                    if (PreferencesFactory.get().getBoolean("browser.disconnect.confirm"))
                    {
                        controller.CommandBox(LocaleFactory.localizedString("Quit"),
                            LocaleFactory.localizedString(
                                "You are connected to at least one remote site. Do you want to review open browsers?"),
                            null,
                            String.Format("{0}|{1}", LocaleFactory.localizedString("Review…"),
                                LocaleFactory.localizedString("Quit Anyway")), true,
                            LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Warning,
                            delegate (int option, bool verificationChecked)
                            {
                                if (verificationChecked)
                                {
                                    // Never show again.
                                    PreferencesFactory.get().setProperty("browser.disconnect.confirm", false);
                                }
                                switch (option)
                                {
                                    case -1: // Cancel
                                             // Quit has been interrupted. Delete any saved sessions so far.
                                        Application._sessions.clear();
                                        readyToExit = false;
                                        break;

                                    case 0: // Review
                                        if (BrowserController.ApplicationShouldTerminate())
                                        {
                                            break;
                                        }
                                        readyToExit = false;
                                        break;

                                    case 1: // Quit
                                        foreach (BrowserController c in
                                            new List<BrowserController>(Browsers))
                                        {
                                            c.View.Dispose();
                                        }
                                        break;
                                }
                            });
                    }
                    else
                    {
                        controller.Unmount();
                    }
                }
            }
            return readyToExit;
        }

        public static void UnhandledExceptionHandler(object sender, UnhandledExceptionEventArgs e)
        {
            CrashReporter.Instance.Write(e.ExceptionObject as Exception);
        }

        void CollectionListener.collectionItemAdded(object obj)
        {
            RefreshJumpList();
        }

        void CollectionListener.collectionItemChanged(object obj)
        {
            RefreshJumpList();
        }

        void CollectionListener.collectionItemRemoved(object obj)
        {
            RefreshJumpList();
        }

        void CollectionListener.collectionLoaded()
        {
            RefreshJumpList();
        }

        void ICyberduck.Connect()
        {
            // Dummy implementation.
        }

        void ICyberduck.NewInstance()
        {
            NewBrowser();
        }

        bool ICyberduck.OAuth(Uri result)
        {
            if (!Find(result, out var state, out var code))
            {
                return false;
            }

            if (Logger.isDebugEnabled())
            {
                Logger.debug($"Notify OAuth with {state} ({code})");
            }

            OAuth2TokenListenerRegistry.get().notify(state, code);
            return true;

            // Local find-method of getting OAuth-parameters
            static bool Find(Uri result, out string state, out string code)
            {
                (state, code) = (default, default);
                if (result.OriginalString.StartsWith(CteraProtocol.CTERA_REDIRECT_URI))
                {
                    var query = HttpUtility.ParseQueryString(result.Query);
                    (state, code) = (default, query.Get("ActivationCode"));
                    return true;
                }

                // Gate AbsoluteUri
                if (!result.IsAbsoluteUri)
                {
                    return false;
                }

                // Authority, AbsolutePath and HostNameType will throw if not AbsoluteUri.
                switch (result)
                {
                    case { HostNameType: UriHostNameType.Unknown, AbsolutePath: "oauth" }:
                    case { Authority: "oauth" }:
                        var query = HttpUtility.ParseQueryString(result.Query);
                        (state, code) = (query.Get("state"), query.Get("code"));
                        return true;
                }

                return false;
            }
        }

        void ICyberduck.QuickConnect(string arg)
        {
            try
            {
                Host h = HostParser.parse(arg);
                h.setCredentials(CredentialsConfiguratorFactory.get(h.getProtocol()).configure(h));
                if (AbstractPath.Type.file == _detector.detect(h.getDefaultPath()))
                {
                    Path file = new Path(PathNormalizer.normalize(h.getDefaultPath()), EnumSet.of(AbstractPath.Type.file));
                    // wait until transferCollection is loaded
                    transfersSemaphore.Wait();
                    TransferController.Instance.StartTransfer(new DownloadTransfer(h, file,
                        LocalFactory.get(PreferencesFactory.get().getProperty("queue.download.folder"),
                            file.getName())));
                }
                else
                {
                    foreach (BrowserController b in Browsers)
                    {
                        if (b.IsMounted())
                        {
                            if (
                                new HostUrlProvider().get(b.Session.getHost())
                                    .Equals(new HostUrlProvider().get(h)))
                            {
                                b.View.BringToFront();
                                if (Path.Type.directory == _detector.detect(h.getDefaultPath()))
                                {
                                    b.SetWorkdir(new Path(PathNormalizer.normalize(h.getDefaultPath()), EnumSet.of(AbstractPath.Type.directory)));
                                }
                                return;
                            }
                        }
                    }
                    NewBrowser().Mount(h);
                }
            }
            catch (HostParserException e)
            {
                Logger.warn(e.getDetail());
            }
        }

        void ICyberduck.RegisterBookmark(string bookmarkPath)
        {
            Local f = LocalFactory.get(bookmarkPath);
            try
            {
                Host bookmark = (Host)HostReaderFactory.get().read(f);
                NewBrowser().Mount(bookmark);
            }
            catch (AccessDeniedException ex)
            {
                Logger.error($"Failure reading bookmark from {f}. {ex.getMessage()}");
            }
        }

        void ICyberduck.RegisterProfile(string profilePath)
        {
            Local f = LocalFactory.get(profilePath);
            Local copy = ProtocolFactory.get().register(f);
            if (null == copy)
            {
                return;
            }
            Protocol profile = (Protocol)ProfileReaderFactory.get().read(copy);
            Host host = new Host(profile, profile.getDefaultHostname(), profile.getDefaultPort());
            NewBrowser().AddBookmark(host);
        }

        void ICyberduck.RegisterRegistration(string registrationPath)
        {
            Local f = LocalFactory.get(registrationPath);
            License license = LicenseFactory.get(f);
            if (license.verify(new DisabledLicenseVerifierCallback()))
            {
                f.copy(LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
                    f.getName()));
                _bc.InfoBox(license.ToString(),
                    LocaleFactory.localizedString(
                        "Thanks for your support! Your contribution helps to further advance development to make Cyberduck even better.",
                        "License"),
                    LocaleFactory.localizedString(
                        "Your registration key has been copied to the Application Support folder.",
                        "License"),
                    String.Format("{0}", LocaleFactory.localizedString("Continue", "License")), null, false);
                foreach (BrowserController controller in new List<BrowserController>(Browsers))
                {
                    controller.RemoveDonateButton();
                }
            }
            else
            {
                _bc.WarningBox(LocaleFactory.localizedString("Not a valid registration key", "License"),
                    LocaleFactory.localizedString("Not a valid registration key", "License"),
                    LocaleFactory.localizedString("This registration key does not appear to be valid.",
                        "License"), null,
                    String.Format("{0}", LocaleFactory.localizedString("Continue", "License")), false,
                    ProviderHelpServiceFactory.get().help(), delegate { });
            }
        }

        protected override void OnMainFormClosed(object sender, EventArgs e)
        {
            // Intercept ExitThreadCore
            // see http://referencesource.microsoft.com/#System.Windows.Forms/winforms/Managed/System/WinForms/ApplicationContext.cs,144
        }

        private static void InitializeBonjour()
        {
            // Bonjour initialization
            ThreadStart start = () =>
            {
                try
                {
                    RendezvousFactory.instance().init();
                }
                catch
                {
                    Logger.warn("No Bonjour support available");
                }
            };
            Thread thread = new Thread(start);
            thread.SetApartmentState(ApartmentState.STA);
            thread.Start();
        }

        private static void InitializeProtocolHandler()
        {
            var self = new Application(System.Windows.Forms.Application.ExecutablePath);
            if (PreferencesFactory.get().getBoolean("defaulthandler.reminder") &&
                            PreferencesFactory.get().getInteger("uses") > 0)
            {
                var handler = SchemeHandlerFactory.get();
                if (
                    !handler.isDefaultHandler(Arrays.asList(Scheme.ftp.name(), Scheme.ftps.name(), Scheme.sftp.name()), self))
                {
                    Core.Utils.CommandBox(LocaleFactory.localizedString("Default Protocol Handler", "Preferences"),
                        LocaleFactory.localizedString(
                            "Set Cyberduck as default application for FTP and SFTP locations?", "Configuration"),
                        LocaleFactory.localizedString(
                            "As the default application, Cyberduck will open when you click on FTP or SFTP links in other applications, such as your web browser. You can change this setting in the Preferences later.",
                            "Configuration"),
                        String.Format("{0}|{1}", LocaleFactory.localizedString("Change", "Configuration"),
                            LocaleFactory.localizedString("Cancel", "Configuration")), false,
                        LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Question,
                        (int option, bool verificationChecked) =>
                        {
                            if (verificationChecked)
                            {
                                // Never show again.
                                PreferencesFactory.get().setProperty("defaulthandler.reminder", false);
                            }
                            switch (option)
                            {
                                case 0:
                                    handler.setDefaultHandler(self,
                                        Arrays.asList(Scheme.ftp.name(), Scheme.ftps.name(), Scheme.sftp.name()));
                                    break;
                            }
                        });
                }
            }
        }

        private static void InitJumpList()
        {
            try
            {
                if (_jumpListManager == null)
                {
                    _jumpListManager = new JumpList();
                }
            }
            catch (Exception exception)
            {
                Logger.warn("Exception while initializing jump list", exception);
            }
        }

        private static BrowserController NewBrowser(bool force, bool show)
        {
            Logger.debug("NewBrowser");
            if (!force)
            {
                foreach (BrowserController c in Browsers)
                {
                    if (SessionPool.DISCONNECTED == c.Session && c.View.IsOnCurrentDesktop())
                    {
                        c.Invoke(c.View.BringToFront);

                        return c;
                    }
                }
            }
            BrowserController controller = new BrowserController();
            controller.View.ViewClosingEvent += delegate (object sender, FormClosingEventArgs args)
            {
                if (args.Cancel)
                {
                    return;
                }
                if (1 == Browsers.Count)
                {
                    // last browser is about to close, check if we can terminate
                    args.Cancel = !ApplicationShouldTerminate();
                }
            };
            controller.View.ViewDisposedEvent += delegate
            {
                Browsers.Remove(controller);
                if (0 == Browsers.Count)
                {
                    // Close/Dispose all non-browser forms (e.g. Transfers) to allow shutdown
                    FormCollection forms = _application.OpenForms;
                    for (int i = forms.Count - 1; i >= 0; i--)
                    {
                        forms[i].Dispose();
                    }
                    Exit(false);
                }
                else
                {
                    _application.MainForm = Browsers[0].View as Form;
                }
            };
            if (show)
            {
                controller.View.Show();
            }
            _application.MainForm = controller.View as Form;
            Browsers.Add(controller);
            return controller;
        }

        private int CanShutdownCallback()
        {
            return Convert.ToInt32(PrepareExit());
        }

        private IList<ThirdpartyBookmarkCollection> GetThirdpartyBookmarks()
        {
            return new List<ThirdpartyBookmarkCollection>
            {
                new FilezillaBookmarkCollection(),
                new WinScpBookmarkCollection(),
                new SmartFtpBookmarkCollection(),
                new TotalCommanderBookmarkCollection(),
                new FlashFxp4UserBookmarkCollection(),
                new FlashFxp4CommonBookmarkCollection(),
                new FlashFxp3BookmarkCollection(),
                new WsFtpBookmarkCollection(),
                new FireFtpBookmarkCollection(),
                new CrossFtpBookmarkCollection(),
                new CloudberryS3BookmarkCollection(),
                new CloudberryGoogleBookmarkCollection(),
                new CloudberryAzureBookmarkCollection(),
                new S3BrowserBookmarkCollection(),
                new Expandrive3BookmarkCollection(),
                new Expandrive4BookmarkCollection(),
                new Expandrive5BookmarkCollection(),
                new Expandrive6BookmarkCollection(),
                new NetDrive2BookmarkCollection()
            };
        }

        private void ImportBookmarks(CountdownEvent bookmarksSemaphore, CountdownEvent thirdpartySemaphore)
        {
            // Import thirdparty bookmarks.
            IList<ThirdpartyBookmarkCollection> thirdpartyBookmarks = GetThirdpartyBookmarks();
            _controller.Background(() =>
            {
                foreach (ThirdpartyBookmarkCollection c in thirdpartyBookmarks)
                {
                    if (!c.isInstalled())
                    {
                        Logger.info("No application installed for " + c.getBundleIdentifier());
                        continue;
                    }
                    c.load();
                    if (c.isEmpty())
                    {
                        if (!PreferencesFactory.get().getBoolean(c.getConfiguration()))
                        {
                            // Flag as imported
                            PreferencesFactory.get().setProperty(c.getConfiguration(), true);
                        }
                    }
                }
                bookmarksSemaphore.Wait();
            }, () =>
            {
                foreach (ThirdpartyBookmarkCollection c in thirdpartyBookmarks)
                {
                    AbstractHostCollection bookmarks = BookmarkCollection.defaultCollection();
                    c.filter(bookmarks);
                    if (!c.isEmpty())
                    {
                        ThirdpartyBookmarkCollection c1 = c;
                        Core.Utils.CommandBox(LocaleFactory.localizedString("Import", "Configuration"),
                            String.Format(LocaleFactory.localizedString("Import {0} Bookmarks", "Configuration"),
                                c.getName()),
                            String.Format(
                                LocaleFactory.localizedString(
                                    "{0} bookmarks found. Do you want to add these to your bookmarks?", "Configuration"),
                                c.size()),
                            String.Format("{0}", LocaleFactory.localizedString("Import", "Configuration")), true,
                            LocaleFactory.localizedString("Don't ask again", "Configuration"), TaskDialogIcon.Question,
                            (int option, bool verificationChecked) =>
                            {
                                if (verificationChecked)
                                {
                                    // Flag as imported
                                    PreferencesFactory.get().setProperty(c1.getConfiguration(), true);
                                }
                                switch (option)
                                {
                                    case 0:
                                        BookmarkCollection.defaultCollection().addAll(c1);
                                        // Flag as imported
                                        PreferencesFactory.get().setProperty(c1.getConfiguration(), true);
                                        break;
                                }
                            });
                    }
                    else
                    {
                        PreferencesFactory.get().setProperty(c.getConfiguration(), true);
                    }
                }
                thirdpartySemaphore.Signal();
            });
        }

        private void InitializeBookmarks(CountdownEvent bookmarksSemaphore)
        {
            // Load all bookmarks in background
            _controller.Background(() =>
            {
                AbstractHostCollection c = BookmarkCollection.defaultCollection();
                c.load();
                bookmarksSemaphore.Signal();
            }, () =>
            {
                if (PreferencesFactory.get().getBoolean("browser.open.untitled"))
                {
                    if (PreferencesFactory.get().getProperty("browser.open.bookmark.default") != null)
                    {
                        _bc.Invoke(() =>
                        {
                            BrowserController bc = NewBrowser();
                            OpenDefaultBookmark(bc);
                        });
                    }
                }
            });
        }

        private void InitializeSessions()
        {
            _controller.Background(delegate { HistoryCollection.defaultCollection().load(); }, delegate { });

            HistoryCollection.defaultCollection().addListener(this);
            if (PreferencesFactory.get().getBoolean("browser.serialize"))
            {
                _controller.Background(delegate { _sessions.load(); }, delegate
                {
                    foreach (Host host in _sessions)
                    {
                        Host h = host;
                        _bc.Invoke(delegate
                        {
                            BrowserController bc = NewBrowser();
                            bc.Mount(h);
                        });
                    }
                    _sessions.clear();
                });
            }
        }

        private void InitializeTransfers()
        {
            _controller.Background(delegate
            {
                var transfers = TransferCollection.defaultCollection();
                lock (transfers)
                {
                    transfers.load();
                }
                transfersSemaphore.Signal();
            }, delegate { });
            if (PreferencesFactory.get().getBoolean("queue.window.open.default"))
            {
                _bc.Invoke(() =>
                {
                    transfersSemaphore.Wait();
                    TransferController.Instance.View.Show();
                });
            }
        }

        private void InitializeUpdater()
        {
            // register callbacks
            _canShutdownCallback = CanShutdownCallback;
            _shutdownRequestCallback = ShutdownRequestCallback;
            WinSparklePeriodicUpdateChecker.SetCanShutdownCallback(_canShutdownCallback);
            WinSparklePeriodicUpdateChecker.SetShutdownRequestCallback(_shutdownRequestCallback);
            if (PreferencesFactory.get().getBoolean("update.check"))
            {
                _updater = PeriodicUpdateCheckerFactory.get();
                if (_updater.hasUpdatePrivileges())
                {
                    long next = PreferencesFactory.get().getLong("update.check.timestamp") + PreferencesFactory.get().getLong("update.check.interval") * 1000;
                    if (next < DateTimeOffset.Now.ToUnixTimeMilliseconds())
                    {
                        _updater.check(true);
                    }
                    _updater.register();
                }
            }
            if (PreferencesFactory.get().getBoolean("profiles.discovery.updater.enable"))
            {
                // Synchronize and register timer
                _profiles.register();
            }
        }

        [MethodImpl(MethodImplOptions.NoInlining)]
        private void InitStoreContext()
        {
            var storeContext = StoreContext.GetDefault();
            var initWindow = (IInitializeWithWindow)(object)storeContext;
            initWindow.Initialize((HWND)MainForm.Handle);
        }

        private void OnStartup(object state)
        {
            _bc = NewBrowser(true, true);
            MainForm = _bc.View as Form;

            /* UWP Registration, initialize as soon as possible */
            if (Utils.IsRunningAsUWP)
            {
                InitStoreContext();
            }

            InitializeTransfers();
            InitializeSessions();

            // User bookmarks and thirdparty applications
            CountdownEvent bookmarksSemaphore = new CountdownEvent(1);
            CountdownEvent thirdpartySemaphore = new CountdownEvent(1);
            InitializeBookmarks(bookmarksSemaphore);
            InitializeBonjour();
            InitializeProtocolHandler();
            ImportBookmarks(bookmarksSemaphore, thirdpartySemaphore);
            SetupServiceHost();
            InitializeUpdater();
        }

        private void RefreshJumpList()
        {
            string appIcon = System.IO.Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "cyberduck-application.ico");

            InitJumpList();

            try
            {
                Iterator iterator = HistoryCollection.defaultCollection().iterator();
                _jumpListManager.JumpItems.Clear();
                while (iterator.hasNext())
                {
                    Host host = (Host)iterator.next();
                    var file = BookmarkCollection.defaultCollection().getFile(host);
                    if (file.exists())
                    {
                        _jumpListManager.JumpItems.Add(new JumpTask()
                        {
                            ApplicationPath = file.getAbsolute(),
                            Title = BookmarkNameProvider.toString(host),
                            IconResourcePath = appIcon,
                            CustomCategory = "History"
                        });
                    }
                }
                mainThreadSync.Send(_ => _jumpListManager.Apply(), null);
            }
            catch (Exception exception)
            {
                Logger.warn("Exception while refreshing jump list", exception);
            }
        }

        private void SetupServiceHost()
        {
            serviceHost = new ServiceHost(this);
            serviceHost.AddServiceEndpoint(typeof(ICyberduck), new NetNamedPipeBinding(), new Uri("net.pipe://localhost/iterate/cyberduck.io"));
            serviceHost.Description.Behaviors.Add(new ServiceMetadataBehavior());
            serviceHost.Open();
        }

        private void Shutdown(bool updating)
        {
            // Clear temporary files
            TemporaryFileServiceFactory.get().shutdown();
            try
            {
                RendezvousFactory.instance().quit();
            }
            catch (SystemException se)
            {
                Logger.warn("No Bonjour support available", se);
            }
            PreferencesFactory.get().setProperty("uses", PreferencesFactory.get().getInteger("uses") + 1);
            PreferencesFactory.get().save();
            if (_updater != null && !updating)
            {
                _updater.unregister();
            }
            if (_profiles != null)
            {
                _profiles.unregister();
            }
        }

        private void ShutdownRequestCallback()
        {
            Logger.info("About to exit in order to install update");
            Exit(true);
        }
    }
}
