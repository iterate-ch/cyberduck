// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Media;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class PromptForm : BaseForm, IPromptView
    {
        public PromptForm()
        {
            InitializeComponent();

            FormClosing += delegate(object sender, FormClosingEventArgs args)
                               {
                                   bool cancel = DialogResult != DialogResult.Cancel && !ValidateInput();
                                   if (cancel)
                                   {
                                       args.Cancel = true;
                                       SystemSounds.Beep.Play();
                                   }
                               };
            MinimumSize = new Size(400, 150);
        }

        public override string[] BundleNames
        {
            get { return new[] {"Folder"}; }
        }

        public string InputText
        {
            get { return inputTextBox.Text; }
            set { inputTextBox.Text = value; }
        }

        public Bitmap IconView
        {
            set { pictureBox.Image = value; }
        }

        public event ValidateInputHandler ValidateInput;

        private void PromptForm_Shown(object sender, EventArgs e)
        {
            inputTextBox.Focus();
            inputTextBox.SelectAll();
        }
    }
}