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
using System.Diagnostics;
using System.Drawing;
using System.Reflection;
using ch.cyberduck;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Ui.Winforms
{
    internal partial class AboutBox : BaseForm
    {
        public AboutBox()
        {
            InitializeComponent();

            Text = String.Format("About {0}", AssemblyTitle);
            logoPictureBox.Image = IconCache.Instance.IconForName("cyberduck", 128);
            labelProductName.Text = AssemblyProduct;
            labelVersion.Text = String.Format("Version {0}", Preferences.instance().getProperty("application.version"));
            labelCopyright.Text = Locale.localizedString("NSHumanReadableCopyright", "InfoPlist");

            Font bigBoldFont = new Font(Font.FontFamily, Font.Size + 4, FontStyle.Bold);
            labelProductName.Font = bigBoldFont;

            creditsRichTextBox.Rtf = ResourcesBundle.Credits;
            creditsRichTextBox.SelectAll();
            creditsRichTextBox.SelectionFont = new Font(Font.FontFamily, 9);
            creditsRichTextBox.DeselectAll();
            creditsRichTextBox.LinkClicked += (sender, e) => Utils.StartProcess(e.LinkText);

            ackButton.Click += delegate { Process.Start("Acknowledgments.rtf"); };
        }

        public string AssemblyTitle
        {
            get
            {
                object[] attributes =
                    Assembly.GetExecutingAssembly().GetCustomAttributes(typeof (AssemblyTitleAttribute), false);
                if (attributes.Length > 0)
                {
                    AssemblyTitleAttribute titleAttribute = (AssemblyTitleAttribute) attributes[0];
                    if (titleAttribute.Title != "")
                    {
                        return titleAttribute.Title;
                    }
                }
                return Path.GetFileNameWithoutExtension(Assembly.GetExecutingAssembly().CodeBase);
            }
        }

        public string AssemblyDescription
        {
            get
            {
                object[] attributes =
                    Assembly.GetExecutingAssembly().GetCustomAttributes(typeof (AssemblyDescriptionAttribute), false);
                if (attributes.Length == 0)
                {
                    return "";
                }
                return ((AssemblyDescriptionAttribute) attributes[0]).Description;
            }
        }

        public string AssemblyProduct
        {
            get
            {
                object[] attributes =
                    Assembly.GetExecutingAssembly().GetCustomAttributes(typeof (AssemblyProductAttribute), false);
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
                    Assembly.GetExecutingAssembly().GetCustomAttributes(typeof (AssemblyCopyrightAttribute), false);
                if (attributes.Length == 0)
                {
                    return "";
                }
                return ((AssemblyCopyrightAttribute) attributes[0]).Copyright;
            }
        }

        public string AssemblyCompany
        {
            get
            {
                object[] attributes =
                    Assembly.GetExecutingAssembly().GetCustomAttributes(typeof (AssemblyCompanyAttribute), false);
                if (attributes.Length == 0)
                {
                    return "";
                }
                return ((AssemblyCompanyAttribute) attributes[0]).Company;
            }
        }
    }
}