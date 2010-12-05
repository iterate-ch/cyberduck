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
using System.Drawing;
using System.IO;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller;
using org.apache.log4j;
using wyDay.Controls;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class UpdateForm : BaseForm, IUpdateView
    {
        private static readonly Logger Log = Logger.getLogger(typeof (UpdateForm).FullName);

        private static bool _expanded;

        public UpdateForm()
        {
            InitializeComponent();

            Closing += delegate { updater.Cancel(); };

            ConfigureUpdater();

            pictureBox.Image = IconCache.Instance.IconForName("cyberduck", 64);
            newVersionAvailableLabel.Text =
                Locale.localizedString("A new version of %@ is available!", "Sparkle").Replace("%@",
                                                                                               Preferences.instance().
                                                                                                   getProperty(
                                                                                                       "application.name"));

            //force handle creation to make the updater work
            IntPtr intPtr = Handle;
            OnLoad(new EventArgs());
        }

        public override string[] BundleNames
        {
            get { return new[] {"Sparkle"}; }
        }

        public void CheckForUpdates(bool background)
        {
            if (!background)
            {
                UpdateStatusLabel("Looking for newer versions of Cyberduck.", false);
                tableLayoutPanel.RowStyles[7].SizeType = SizeType.AutoSize;
                SetStatusChecking(true);
                updater.ForceCheckForUpdate(true);
                Show();
            }
            else
            {
                updater.ForceCheckForUpdate(true);
            }
        }

        public bool AboutToInstallUpdate
        {
            get { return updater.UpdateStepOn == UpdateStepOn.UpdateReadyToInstall; }
        }

        public static void SetButtonShield(Button btn, bool showShield)
        {
            if (showShield)
            {
                if (!_expanded)
                {
                    btn.Width += 20;
                    _expanded = true;
                }
            }
            else
            {
                if (_expanded) btn.Width -= 20;
            }
            btn.FlatStyle = FlatStyle.System;
            // BCM_SETSHIELD = 0x0000160C
            NativeMethods.SendMessage(btn.Handle, 0x160C, 0, showShield ? 1 : 0);
        }

        private void ConfigureUpdater()
        {
            String currentFeed = Preferences.instance().getProperty("update.feed");
            String feedUrl = Preferences.instance().getProperty("update.feed." + currentFeed);
            Log.debug("Setting feed URL to " + feedUrl);

            if (!File.Exists("Updater.exe"))
            {
                //as of Beta7 the updater filename is Updater.exe. wyUpdate.exe is not automatically renamed.
                updater.wyUpdateLocation = "wyUpdate.exe";
            }
            
            updater.wyUpdateCommandline = "-server=\"" + feedUrl + "\"";
            updater.ContainerForm = this;
            updater.KeepHidden = true;
            updater.Visible = false;
            updater.UpdateType = UpdateType.DoNothing;
            updater.UpdateAvailable += delegate
                                           {
                                               laterButton.Visible = true;
                                               installButton.Text = Locale.localizedString("Install and Relaunch",
                                                                                           "Sparkle");
                                               if (Utils.IsVistaOrLater)
                                               {
                                                   SetButtonShield(installButton, true);
                                               }

                                               tableLayoutPanel.RowStyles[7].SizeType = SizeType.Percent;
                                               tableLayoutPanel.RowStyles[7].Height = 100;

                                               string currentVersion =
                                                   Preferences.instance().getProperty("application.version");
                                               string newVersion = updater.Version;

                                               versionLabel.Text = Locale.localizedString(
                                                   "%1$@ %2$@ is now available (you have %3$@). Would you like to download it now?",
                                                   "Sparkle")
                                                   .Replace("%1$@",
                                                            Preferences.instance
                                                                ().getProperty
                                                                ("application.name"))
                                                   .Replace("%2$@", newVersion).Replace("%3$@", currentVersion);

                                               SetStatusChecking(false);
                                               statusLabel.Text = "Update available";
                                               changesTextBox.Text = updater.Changes.Replace("\n", "\r\n");
                                               //installButton.Enabled = true;
                                               Show();
                                           };

            // error cases
            updater.CheckingFailed +=
                delegate(object sender, FailArgs args) { UpdateStatusLabel(args.ErrorTitle + Environment.NewLine + args.ErrorMessage, true); };

            updater.UpdateFailed +=
                (sender, args) =>
                UpdateStatusLabel(args.ErrorTitle + Environment.NewLine + args.ErrorMessage, true);

            updater.DownloadingOrExtractingFailed +=
                (sender, args) =>
                UpdateStatusLabel(args.ErrorTitle + Environment.NewLine + args.ErrorMessage, true);
            // end error cases

            updater.ProgressChanged += delegate(object sender, int progress) { progressBar.Value = progress; };

            updater.BeforeDownloading += (sender, args) =>
                                             {
                                                 UpdateStatusLabel(
                                                     Locale.localizedString("Downloading update...", "Sparkle"), false);
                                                 progressBar.Style = ProgressBarStyle.Continuous;
                                                 progressBar.Value = 0;
                                                 progressBar.Visible = true;
                                             };

            updater.UpToDate += delegate
                                    {
                                        progressBar.Visible = false;
                                        UpdateStatusLabel(Locale.localizedString("You're up to date!", "Sparkle"), false);
                                    };

            updater.ReadyToBeInstalled += delegate
                                              {
                                                  progressBar.Visible = false;
                                                  statusLabel.Text = Locale.localizedString("Installing update...",
                                                                                            "Sparkle");
                                                  updater.InstallNow();
                                              };

            updater.BeforeChecking += delegate
                                          {
                                              if (Utils.IsVistaOrLater)
                                              {
                                                  SetButtonShield(installButton, false);
                                              }
                                              laterButton.Visible = false;
                                              installButton.Text = Locale.localizedString("OK", "Sparkle");
                                              progressBar.Style = ProgressBarStyle.Marquee;
                                              progressBar.Visible = true;
                                          };
        }

        private void UpdateStatusLabel(String status, bool error)
        {
            if (error)
            {
                progressBar.Visible = false;
                laterButton.Visible = false;
                installButton.Text = Locale.localizedString("Cancel");
            }
            statusLabel.Visible = true;
            statusLabel.ForeColor = error ? Color.Red : Color.FromKnownColor(KnownColor.ControlText);
            statusLabel.Text = error
                                   ? Locale.localizedString(
                                       "An error occurred during installation. Please try again later." + " ", "Sparkle")
                                     + status
                                   : status;
        }

        public void SetStatusChecking(bool checking)
        {
            statusLabel.Visible = checking;
            progressBar.Visible = checking;
            newVersionAvailableLabel.Visible = !checking;
            versionLabel.Visible = !checking;
            releaseNotesLabel.Visible = !checking;
            changesTextBox.Visible = !checking;
        }

        private void donateButton_Click(object sender, EventArgs e)
        {
            if (updater.UpdateStepOn == UpdateStepOn.UpdateAvailable)
            {
                updater.InstallNow();
                laterButton.Enabled = false;
                installButton.Enabled = false;
            }
            else
            {
                Close();
            }
        }

        private void laterButton_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}