// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
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
using Ch.Cyberduck.Ui.Controller;
using ch.cyberduck.core;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class CreateSymlinkPromptForm : PromptForm, ICreateSymlinkPromptView
    {
        public CreateSymlinkPromptForm()
        {
            InitializeComponent();
            Text = LocaleFactory.localizedString("Create new symbolic link", "File");
        }

        public string LinkForFile
        {
            set
            {
                label.Text =
                    String.Format(
                        LocaleFactory.localizedString("Enter the name for the new symbolic link for {0}:", "File"),
                        value);
            }
        }
    }
}