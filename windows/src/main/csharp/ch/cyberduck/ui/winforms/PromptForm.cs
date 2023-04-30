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
            FormClosing += delegate (object sender, FormClosingEventArgs args)
            {
                if (DialogResult == DialogResult.Cancel || DialogResult == DialogResult.Ignore)
                {
                    return;
                }

                bool valid = true;
                if (ValidateInput != null)
                {
                    foreach (var d in ValidateInput.GetInvocationList())
                    {
                        valid = (bool)d.DynamicInvoke();
                        if (!valid)
                        {
                            break;
                        }
                    }
                }
                if (!valid)
                {
                    args.Cancel = true;
                    SystemSounds.Beep.Play();
                }
            };
        }

        protected override bool EnableAutoSizePosition => false;

        public override string[] BundleNames => new[] { "Folder", "Cryptomator", "Keychain" };

        public string InputText
        {
            get { return inputTextBox.Text; }
            set { inputTextBox.Text = value; }
        }

        public Image IconView
        {
            set { pictureBox.Image = value; }
        }

        public event ValidateInputHandler ValidateInput;

        protected static Button CreateButton(Button button, out Button result)
        {
            button.AutoSize = true;
            button.Anchor = AnchorStyles.Bottom | AnchorStyles.Right;
            button.UseVisualStyleBackColor = true;
            result = button;
            return button;
        }

        private void PromptForm_Shown(object sender, EventArgs e)
        {
            inputTextBox.Focus();
            inputTextBox.SelectAll();
        }
    }
}
