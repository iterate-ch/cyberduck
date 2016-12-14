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
using System.Drawing;
using System.Reflection;
using ch.cyberduck.core;
using ch.cyberduck.core.aquaticprime;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.updater;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Core.Sparkle;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class AboutBox : BaseForm
    {
        public AboutBox()
        {
            InitializeComponent();

            Text = String.Format("About {0}", AssemblyTitle);
            logoPictureBox.Image = ApplicationIcon();
            labelProductName.Text = AssemblyProduct;
            labelVersion.Text = String.Format("Version {0} ({1})",
                PreferencesFactory.get().getProperty("application.version"),
                PreferencesFactory.get().getProperty("application.revision"));
            labelCopyright.Text = Copyright();

            Font bigBoldFont = new Font(Font.FontFamily, Font.Size + 4, FontStyle.Bold);
            labelProductName.Font = bigBoldFont;

            labelRegistered.Text = RegisteredText();

            creditsRichTextBox.Rtf = Credits();
            creditsRichTextBox.SelectAll();
            creditsRichTextBox.SelectionFont = new Font(Font.FontFamily, 9);
            creditsRichTextBox.DeselectAll();
            creditsRichTextBox.LinkClicked += (sender, e) => BrowserLauncherFactory.get().open(e.LinkText);

            ackButton.Click +=
                delegate { ApplicationLauncherFactory.get().open(LocalFactory.get("Acknowledgments.rtf")); };
            PeriodicUpdateChecker updater = PeriodicUpdateCheckerFactory.get();
            updateButton.Enabled = updater.hasUpdatePrivileges();
            updateButton.Click += delegate { updater.check(false); };
        }

        public string AssemblyTitle
        {
            get
            {
                object[] attributes = Assembly.GetEntryAssembly()
                    .GetCustomAttributes(typeof (AssemblyTitleAttribute), false);
                if (attributes.Length > 0)
                {
                    AssemblyTitleAttribute titleAttribute = (AssemblyTitleAttribute) attributes[0];
                    if (Utils.IsNotBlank(titleAttribute.Title))
                    {
                        return titleAttribute.Title;
                    }
                }
                return Path.GetFileNameWithoutExtension(Assembly.GetEntryAssembly().CodeBase);
            }
        }

        public string AssemblyDescription
        {
            get
            {
                object[] attributes =
                    Assembly.GetEntryAssembly().GetCustomAttributes(typeof (AssemblyDescriptionAttribute), false);
                if (attributes.Length == 0)
                {
                    return String.Empty;
                }
                return ((AssemblyDescriptionAttribute) attributes[0]).Description;
            }
        }

        public string AssemblyProduct
        {
            get
            {
                object[] attributes = Assembly.GetEntryAssembly()
                    .GetCustomAttributes(typeof (AssemblyProductAttribute), false);
                if (attributes.Length == 0)
                {
                    return "";
                }
                return ((AssemblyProductAttribute) attributes[0]).Product;
            }
        }

        public string AssemblyCopyright
        {
            get
            {
                object[] attributes =
                    Assembly.GetEntryAssembly().GetCustomAttributes(typeof (AssemblyCopyrightAttribute), false);
                if (attributes.Length == 0)
                {
                    return String.Empty;
                }
                return ((AssemblyCopyrightAttribute) attributes[0]).Copyright;
            }
        }

        public string AssemblyCompany
        {
            get
            {
                object[] attributes = Assembly.GetEntryAssembly()
                    .GetCustomAttributes(typeof (AssemblyCompanyAttribute), false);
                if (attributes.Length == 0)
                {
                    return String.Empty;
                }
                return ((AssemblyCompanyAttribute) attributes[0]).Company;
            }
        }

        public override string[] BundleNames
        {
            get { return new[] {"Main", "Localizable"}; }
        }

        public virtual string RegisteredText()
        {
            return LicenseFactory.find().ToString();
        }

        public virtual Image ApplicationIcon()
        {
            return IconCache.Instance.IconForName("cyberduck", 128);
        }

        public virtual string Credits()
        {
            return ResourcesBundle.Credits;
        }

        public virtual string Copyright()
        {
            return LocaleFactory.localizedString("NSHumanReadableCopyright", "InfoPlist");
        }
    }
}