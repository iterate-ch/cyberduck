// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Reflection;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Aquaticprime;
using Ch.Cyberduck.Core.Editor;
using Ch.Cyberduck.Core.I18n;
using Ch.Cyberduck.Core.Local;
using Ch.Cyberduck.Core.Serializer.Impl;
using Ch.Cyberduck.Core.Urlhandler;
using Ch.Cyberduck.Ui.Growl;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using Ch.Cyberduck.Ui.Winforms.Threading;
using Ch.Cyberduck.core.local;
using Microsoft.VisualBasic.ApplicationServices;
using Windows7.DesktopIntegration;
using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using ch.cyberduck.core.importer;
using ch.cyberduck.core.serializer;
using ch.cyberduck.ui.growl;
using java.security;
using java.util;
using org.apache.log4j;
using org.apache.log4j.xml;
using sun.security.mscapi;
using ArrayList = System.Collections.ArrayList;
using Keychain = Ch.Cyberduck.Core.Keychain;
using Object = java.lang.Object;
using Path = System.IO.Path;
using SystemProxy = Ch.Cyberduck.Core.SystemProxy;
using Rendezvous = Ch.Cyberduck.Core.Rendezvous;
using UnhandledExceptionEventArgs = System.UnhandledExceptionEventArgs;

namespace Ch.Cyberduck.Ui.Controller
{
    /// <summary>
    /// A potential alternative for the VB.WindowsFormsApplicationBase: http://www.ai.uga.edu/mc/SingleInstance.html
    /// </summary>
    internal class MainController : WindowsFormsApplicationBase, CollectionListener
    {
        private static readonly Logger Logger = Logger.getLogger(typeof (MainController).FullName);
        public static readonly string StartupLanguage;
        private static readonly IList<BrowserController> _browsers = new List<BrowserController>();
        private static MainController _application;
        private static JumpListManager _jumpListManager;
        private readonly BaseController _controller = new BaseController();

        /// <summary>
        /// Saved browsers
        /// </summary>
        private readonly AbstractHostCollection _sessions =
            new FolderBookmarkCollection(
                LocalFactory.get(Preferences.instance().getProperty("application.support.path"), "Sessions"));

        /// <summary>
        /// Helper controller to ensure STA when running threads while launching
        /// </summary>
        /// <see cref="http://msdn.microsoft.com/en-us/library/system.stathreadattribute.aspx"/>
        private BrowserController _bc;

        static MainController()
        {
            StructureMapBootstrapper.Bootstrap();
            RegisterImplementations();

            if (!Debugger.IsAttached)
            {
                // Add the event handler for handling UI thread exceptions to the event.
                System.Windows.Forms.Application.ThreadException += ExceptionHandler;

                // Set the unhandled exception mode to force all Windows Forms errors to go through
                // our handler.
                System.Windows.Forms.Application.SetUnhandledExceptionMode(UnhandledExceptionMode.CatchException);

                // Add the event handler for handling non-UI thread exceptions to the event. 
                AppDomain.CurrentDomain.UnhandledException += UnhandledExceptionHandler;
            }

            // Add mscapi security provider
            Security.addProvider(new SunMSCAPI());

            ConfigureLogging();

            //make sure that a language change takes effect after a restart only
            StartupLanguage = Preferences.instance().getProperty("application.language");
        }

        /// <summary>
        /// Constructor that intializes the authentication mode for this app.
        /// </summary>
        /// <param name="mode">Mode in which to run app.</param>
        public MainController(AuthenticationMode mode) : base(mode)
        {
            InitializeAppProperties();
        }

        /// <summary>
        /// Default constructor.
        /// </summary>
        private MainController()
        {
            InitializeAppProperties();
            SaveMySettingsOnExit = true;
            Startup += ApplicationDidFinishLaunching;
            StartupNextInstance += StartupNextInstanceHandler;
            Shutdown += delegate
                {
                    try
                    {
                        RendezvousFactory.instance().quit();
                    }
                    catch (SystemException se)
                    {
                        Logger.warn("No Bonjour support available", se);
                    }
                    Preferences.instance().setProperty("uses", Preferences.instance().getInteger("uses") + 1);
                    Preferences.instance().save();
                };
        }

        internal static MainController Application
        {
            get { return _application ?? (_application = new MainController()); }
        }

        public Form ActiveMainForm
        {
            get { return MainForm; }
        }

        public static IList<BrowserController> Browsers
        {
            get { return _browsers; }
        }

        public void collectionLoaded()
        {
            RefreshJumpList();
        }

        public void collectionItemAdded(object obj)
        {
            RefreshJumpList();
        }

        public void collectionItemRemoved(object obj)
        {
            RefreshJumpList();
        }

        public void collectionItemChanged(object obj)
        {
            RefreshJumpList();
        }

        private void StartupNextInstanceHandler(object sender, StartupNextInstanceEventArgs e)
        {
            NewBrowser();
            CommandsAfterLaunch(e.CommandLine);
        }

        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        private static void Main()
        {
            System.Windows.Forms.Application.EnableVisualStyles();
            System.Windows.Forms.Application.SetCompatibleTextRenderingDefault(false);
            Application.Run();
        }

        public static void ExceptionHandler(object sender, ThreadExceptionEventArgs e)
        {
            CrashReporter.Instance.Write(e.Exception);
            Environment.Exit(1);
        }

        private static void RegisterImplementations()
        {
            UserPreferences.Register();
            SystemLocal.Register();
            LicenseImpl.Register();
            SystemProxy.Register();
            RecycleLocalTrashFeature.Register();
            DictionaryLocale.Register();
            Keychain.Register();
            PlistWriter.Register();
            PlistSerializer.Register();
            PlistDeserializer.Register();
            HostPlistReader.Register();
            TransferPlistReader.Register();
            ProfilePlistReader.Register();
            TcpReachability.Register();
            DefaultPathReferenceFactory.Register();
            PromptLoginController.Register();
            DialogTransferPromptControllerFactory.Register();
            DialogTransferErrorCallback.Register();
            HostKeyController.Register();
            UserDefaultsDateFormatter.Register();
            Rendezvous.Register();
            ProtocolFactory.register();
            WindowsTemporaryFileService.Register();
            RegistryApplicationFinder.Register();
            SystemWatchEditorFactory.Register();
            WindowsApplicationLauncher.Register();
            Win32FileDescriptor.Register();
            ExplorerRevealService.Register();
            TaskbarApplicationBadgeLabeler.Register();
            DefaultBrowserLauncher.Register();
            NotifyImpl.Register();
        }

        private static void ConfigureLogging()
        {
            // we do not save the log file in the roaming profile
            var fileName = Path.Combine(Preferences.instance().getProperty("application.support.path"), "cyberduck.log");

            DOMConfigurator.configure(
                Object.instancehelper_getClass(new DOMConfigurator())
                      .getClassLoader()
                      .getResource(Preferences.instance().getProperty("logging.config")));
            Logger root = Logger.getRootLogger();
            root.removeAllAppenders();

            RollingFileAppender appender = new RollingFileAppender(new PatternLayout(@"%d [%t] %-5p %c - %m%n"),
                                                                   fileName, true);
            appender.setMaxFileSize("1MB");
            appender.setMaxBackupIndex(0);
            root.addAppender(appender);
            root.setLevel(Debugger.IsAttached
                              ? Level.DEBUG
                              : Level.toLevel(Preferences.instance().getProperty("logging")));
        }

        /// <summary>
        /// Initializes this application with the appropriate settings.
        /// </summary>
        protected virtual void InitializeAppProperties()
        {
            IsSingleInstance = true;
            // Needed for multiple SDI because no form is the main form
            ShutdownStyle = ShutdownMode.AfterAllFormsClose;
        }

        private void CommandsAfterLaunch(ReadOnlyCollection<string> args)
        {
            if (args.Count > 0)
            {
                string filename = args[0];
                Logger.debug("applicationOpenFile:" + filename);
                Local f = LocalFactory.get(filename);
                if (f.exists())
                {
                    if ("cyberducklicense".Equals(f.getExtension()))
                    {
                        License license = LicenseFactory.create(f);
                        if (license.verify())
                        {
                            f.copy(
                                LocalFactory.get(
                                    Preferences.instance().getProperty("application.support.path"), f.getName()));
                            if (DialogResult.OK ==
                                _bc.InfoBox(license.ToString(),
                                            LocaleFactory.localizedString(
                                                "Thanks for your support! Your contribution helps to further advance development to make Cyberduck even better.",
                                                "License"),
                                            LocaleFactory.localizedString(
                                                "Your donation key has been copied to the Application Support folder.",
                                                "License"),
                                            String.Format("{0}", LocaleFactory.localizedString("Continue", "License")),
                                            null, false))
                            {
                                ;
                            }
                        }
                        else
                        {
                            if (DialogResult.OK ==
                                _bc.WarningBox(LocaleFactory.localizedString("Not a valid donation key", "License"),
                                               LocaleFactory.localizedString("Not a valid donation key", "License"),
                                               LocaleFactory.localizedString(
                                                   "This donation key does not appear to be valid.", "License"), null,
                                               String.Format("{0}", LocaleFactory.localizedString("Continue", "License")),
                                               false, Preferences.instance().getProperty("website.help") + "/faq",
                                               delegate { }))
                            {
                                ;
                            }
                        }
                    }
                    else if ("cyberduckprofile".Equals(f.getExtension()))
                    {
                        Protocol profile = (Protocol) ProfileReaderFactory.get().read(f);
                        if (null == profile)
                        {
                            return;
                        }
                        if (profile.isEnabled())
                        {
                            ProtocolFactory.register(profile);
                            Host host = new Host(profile, profile.getDefaultHostname(), profile.getDefaultPort());
                            NewBrowser().AddBookmark(host);
                            // Register in application support
                            Local profiles =
                                LocalFactory.get(
                                    Preferences.instance().getProperty("application.support.path"), "Profiles");
                            profiles.mkdir();
                            f.copy(LocalFactory.get(profiles, f.getName()));
                        }
                    }
                    else if ("duck".Equals(f.getExtension()))
                    {
                        Host bookmark = (Host) HostReaderFactory.get().read(f);
                        if (null == bookmark)
                        {
                            return;
                        }
                        NewBrowser().Mount(bookmark);
                    }
                }
            }
        }

        /// <summary>
        /// Run the application
        /// </summary>
        public virtual void Run()
        {
            // set up the main form.
            _bc = NewBrowser(true, true);
            MainForm = _bc.View as Form;
            // then, run the the main form.
            Run(CommandLineArgs);
        }

        /// <summary>
        /// A normal (non-single-instance) application raises the Startup event every time it starts. 
        /// A single-instance application raises the Startup  event when it starts only if the application
        /// is not already active; otherwise, it raises the StartupNextInstance  event.
        /// </summary>
        /// <see cref="http://msdn.microsoft.com/en-us/library/microsoft.visualbasic.applicationservices.windowsformsapplicationbase.startup.aspx"/>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ApplicationDidFinishLaunching(object sender, StartupEventArgs e)
        {
            Logger.debug("ApplicationDidFinishLaunching");
            CommandsAfterLaunch(CommandLineArgs);
            HistoryCollection.defaultCollection().addListener(this);
            UpdateController.Instance.CheckForUpdatesIfNecessary();

            if (Preferences.instance().getBoolean("browser.serialize"))
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
            GrowlFactory.get().setup();

            // User bookmarks and thirdparty applications
            CountdownEvent bookmarksSemaphore = new CountdownEvent(1);
            CountdownEvent thirdpartySemaphore = new CountdownEvent(1);

            // Load all bookmarks in background
            _controller.Background(delegate
                {
                    BookmarkCollection c = BookmarkCollection.defaultCollection();
                    c.load();
                    bookmarksSemaphore.Signal();
                }, delegate
                    {
                        if (Preferences.instance().getBoolean("browser.open.untitled"))
                        {
                            if (Preferences.instance().getProperty("browser.open.bookmark.default") != null)
                            {
                                _bc.Invoke(delegate
                                    {
                                        BrowserController bc = NewBrowser();
                                        OpenDefaultBookmark(bc);
                                    });
                            }
                        }
                    });
            _controller.Background(delegate { HistoryCollection.defaultCollection().load(); }, delegate { });
            _controller.Background(delegate
                {
                    lock (TransferCollection.defaultCollection())
                    {
                        TransferCollection.defaultCollection().load();
                    }
                }, delegate
                    {
                        if (Preferences.instance().getBoolean("queue.window.open.default"))
                        {
                            _bc.Invoke(delegate { TransferController.Instance.View.Show(); });
                        }
                    });

            // Bonjour initialization
            ThreadStart start = delegate
                {
                    try
                    {
                        RendezvousFactory.instance().init();
                    }
                    catch (COMException)
                    {
                        Logger.warn("No Bonjour support available");
                    }
                };
            Thread thread = new Thread(start);
            thread.SetApartmentState(ApartmentState.STA);
            thread.Start();
            if (Preferences.instance().getBoolean("defaulthandler.reminder") &&
                Preferences.instance().getInteger("uses") > 0)
            {
                if (!URLSchemeHandlerConfiguration.Instance.IsDefaultApplicationForFtp() ||
                    !URLSchemeHandlerConfiguration.Instance.IsDefaultApplicationForSftp())
                {
                    Utils.CommandBox(LocaleFactory.localizedString("Default Protocol Handler", "Preferences"),
                                     LocaleFactory.localizedString(
                                         "Set Cyberduck as default application for FTP and SFTP locations?",
                                         "Configuration"),
                                     LocaleFactory.localizedString(
                                         "As the default application, Cyberduck will open when you click on FTP or SFTP links in other applications, such as your web browser. You can change this setting in the Preferences later.",
                                         "Configuration"),
                                     String.Format("{0}|{1}", LocaleFactory.localizedString("Change", "Configuration"),
                                                   LocaleFactory.localizedString("Cancel", "Configuration")), false,
                                     LocaleFactory.localizedString("Don't ask again", "Configuration"),
                                     SysIcons.Question, delegate(int option, bool verificationChecked)
                                         {
                                             if (verificationChecked)
                                             {
                                                 // Never show again.
                                                 Preferences.instance().setProperty("defaulthandler.reminder", false);
                                             }
                                             switch (option)
                                             {
                                                 case 0:
                                                     URLSchemeHandlerConfiguration.Instance.RegisterFtpProtocol();
                                                     URLSchemeHandlerConfiguration.Instance.RegisterSftpProtocol();
                                                     break;
                                             }
                                         });
                }
            }
            // Import thirdparty bookmarks.
            IList<ThirdpartyBookmarkCollection> thirdpartyBookmarks = GetThirdpartyBookmarks();
            _controller.Background(delegate
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
                            // Flag as imported
                            Preferences.instance().setProperty(c.getConfiguration(), true);
                        }
                    }
                    bookmarksSemaphore.Wait();
                }, delegate
                    {
                        foreach (ThirdpartyBookmarkCollection c in thirdpartyBookmarks)
                        {
                            BookmarkCollection bookmarks = BookmarkCollection.defaultCollection();
                            c.filter(bookmarks);
                            if (!c.isEmpty())
                            {
                                ThirdpartyBookmarkCollection c1 = c;
                                Utils.CommandBox(LocaleFactory.localizedString("Import", "Configuration"),
                                                 String.Format(
                                                     LocaleFactory.localizedString("Import {0} Bookmarks",
                                                                                   "Configuration"), c.getName()),
                                                 String.Format(
                                                     LocaleFactory.localizedString(
                                                         "{0} bookmarks found. Do you want to add these to your bookmarks?",
                                                         "Configuration"), c.size()),
                                                 String.Format("{0}",
                                                               LocaleFactory.localizedString("Import", "Configuration")),
                                                 true, LocaleFactory.localizedString("Don't ask again", "Configuration"),
                                                 SysIcons.Question, delegate(int option, bool verificationChecked)
                                                     {
                                                         if (verificationChecked)
                                                         {
                                                             // Flag as imported
                                                             Preferences.instance()
                                                                        .setProperty(c1.getConfiguration(), true);
                                                         }
                                                         switch (option)
                                                         {
                                                             case 0:
                                                                 BookmarkCollection.defaultCollection().addAll(c1);
                                                                 // Flag as imported
                                                                 Preferences.instance()
                                                                            .setProperty(c1.getConfiguration(), true);
                                                                 break;
                                                         }
                                                     });
                            }
                        }
                        thirdpartySemaphore.Signal();
                    });
            _controller.Background(delegate
                {
                    bookmarksSemaphore.Wait();
                    thirdpartySemaphore.Wait();
                    BookmarkCollection c = BookmarkCollection.defaultCollection();
                    if (c.isEmpty())
                    {
                        FolderBookmarkCollection defaults =
                            new FolderBookmarkCollection(
                                LocalFactory.get(Preferences.instance()
                                                                    .getProperty("application.bookmarks.path")));
                        defaults.load();
                        foreach (Host bookmark in defaults)
                        {
                            if (Logger.isDebugEnabled())
                            {
                                Logger.debug("Adding default bookmark:" + bookmark);
                            }
                            c.add(bookmark);
                        }
                    }
                }, delegate { });
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
                    new S3BrowserBookmarkCollection()
                };
        }

        /// <summary>
        /// Runs this.MainForm in this application context. Converts the command
        /// line arguments correctly for the base this.Run method.
        /// </summary>
        /// <param name="commandLineArgs">Command line collection.</param>
        private void Run(ICollection commandLineArgs)
        {
            // convert the Collection<string> to string[], so that it can be used
            // in the Run method.
            ArrayList list = new ArrayList(commandLineArgs);
            string[] commandLine = (string[]) list.ToArray(typeof (string));
            base.Run(commandLine);
        }

        public static BrowserController NewBrowser()
        {
            return NewBrowser(false);
        }

        /// <summary>
        /// Mounts the default bookmark if any
        /// </summary>
        /// <param name="controller"></param>
        public static void OpenDefaultBookmark(BrowserController controller)
        {
            String defaultBookmark = Preferences.instance().getProperty("browser.open.bookmark.default");
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
                if (browser.HasSession())
                {
                    if (browser.Session.getHost().equals(bookmark))
                    {
                        Logger.debug("Default bookmark already mounted");
                        return;
                    }
                }
            }
            Logger.debug("Mounting default bookmark " + bookmark);
            controller.Mount(bookmark);
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

        public static bool ApplicationShouldTerminateAfterDonationPrompt()
        {
            Logger.debug("ApplicationShouldTerminateAfterDonationPrompt");
            License l = LicenseFactory.find();
            if (!l.verify())
            {
                string appVersion = Assembly.GetExecutingAssembly().GetName().Version.ToString();
                String lastversion = Preferences.instance().getProperty("donate.reminder");
                if (appVersion.Equals(lastversion))
                {
                    // Do not display if same version is installed
                    return true;
                }

                DateTime nextReminder = new DateTime(Preferences.instance().getLong("donate.reminder.date"));
                // Display donationPrompt every n days
                nextReminder.AddDays(Preferences.instance().getLong("donate.reminder.interval"));
                Logger.debug("Next reminder: " + nextReminder);
                // Display after upgrade
                if (nextReminder.CompareTo(DateTime.Now) == 1)
                {
                    // Do not display if shown in the reminder interval
                    return true;
                }
                DonationController controller = new DonationController();
                controller.Show();
            }
            return true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <returns>Return true to allow the application to terminate</returns>
        public static bool ApplicationShouldTerminate()
        {
            Logger.debug("ApplicationShouldTerminate");
            // Check if the automatic updater wants to install an update
            if (UpdateController.Instance.AboutToInstallUpdate)
            {
                return true;
            }

            // Determine if there are any running transfers
            bool terminate = TransferController.ApplicationShouldTerminate();
            if (!terminate)
            {
                return false;
            }

            // Determine if there are any open connections
            foreach (BrowserController controller in new List<BrowserController>(Browsers))
            {
                if (Preferences.instance().getBoolean("browser.serialize"))
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

        public static void Exit()
        {
            foreach (BrowserController controller in new List<BrowserController>(Browsers))
            {
                if (controller.IsConnected())
                {
                    if (Preferences.instance().getBoolean("browser.disconnect.confirm"))
                    {
                        controller.CommandBox(LocaleFactory.localizedString("Quit"),
                                              LocaleFactory.localizedString(
                                                  "You are connected to at least one remote site. Do you want to review open browsers?"),
                                              null,
                                              String.Format("{0}|{1}", LocaleFactory.localizedString("Review…"),
                                                            LocaleFactory.localizedString("Quit Anyway")), true,
                                              LocaleFactory.localizedString("Don't ask again", "Configuration"),
                                              SysIcons.Warning, delegate(int option, bool verificationChecked)
                                                  {
                                                      if (verificationChecked)
                                                      {
                                                          // Never show again.
                                                          Preferences.instance()
                                                                     .setProperty("browser.disconnect.confirm", false);
                                                      }
                                                      switch (option)
                                                      {
                                                          case -1: // Cancel
                                                              // Quit has been interrupted. Delete any saved sessions so far.
                                                              Application._sessions.clear();
                                                              return;
                                                          case 0: // Review
                                                              if (BrowserController.ApplicationShouldTerminate())
                                                              {
                                                                  break;
                                                              }
                                                              return;
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
            GrowlFactory.get().unregister();
            ApplicationShouldTerminateAfterDonationPrompt();
            System.Windows.Forms.Application.Exit();
        }

        private static BrowserController NewBrowser(bool force, bool show)
        {
            InitJumpList();
            Logger.debug("NewBrowser");
            if (!force)
            {
                foreach (BrowserController c in Browsers)
                {
                    if (!c.HasSession())
                    {
                        c.Invoke(delegate { c.View.BringToFront(); });

                        return c;
                    }
                }
            }
            BrowserController controller = new BrowserController();
            controller.View.ViewClosingEvent += delegate(object sender, FormClosingEventArgs args)
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
                        Exit();
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

        private static void InitJumpList()
        {
            if (Utils.IsWin7OrLater)
            {
                try
                {
                    Windows7Taskbar.SetCurrentProcessAppId(Preferences.instance().getProperty("application.name"));
                    _jumpListManager = new JumpListManager(Preferences.instance().getProperty("application.name"));
                    _jumpListManager.UserRemovedItems += (o, e) => { };
                }
                catch (Exception exception)
                {
                    Logger.warn("Exception while initializing jump list", exception);
                }
            }
        }

        public static void UnhandledExceptionHandler(object sender, UnhandledExceptionEventArgs e)
        {
            CrashReporter.Instance.Write(e.ExceptionObject as Exception);
        }

        private void RefreshJumpList()
        {
            if (Utils.IsWin7OrLater)
            {
                try
                {
                    _jumpListManager.ClearCustomDestinations();
                    Iterator iterator = HistoryCollection.defaultCollection().iterator();
                    while (iterator.hasNext())
                    {
                        Host host = (Host) iterator.next();
                        _jumpListManager.AddCustomDestination(new ShellLink
                            {
                                Path = FolderBookmarkCollection.favoritesCollection().getFile(host).getAbsolute(),
                                Title = BookmarkNameProvider.toString(host, true),
                                Category = LocaleFactory.localizedString("History"),
                                IconLocation = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "cyberduck-document.ico"),
                                IconIndex = 0
                            });
                    }
                    _jumpListManager.Refresh();
                }
                catch (Exception exception)
                {
                    Logger.warn("Exception while refreshing jump list", exception);
                }
            }
        }
    }
}