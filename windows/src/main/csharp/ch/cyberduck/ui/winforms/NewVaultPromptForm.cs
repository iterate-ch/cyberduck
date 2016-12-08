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
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Controls;

namespace Ch.Cyberduck.windows.src.main.csharp.ch.cyberduck.ui.winforms
{
    public partial class NewVaultPromptForm : NewFolderPromptForm, INewVaultPromptView
    {
        private readonly PasswordStrengthValidator strengthValidator = new PasswordStrengthValidator();
        private TextBox passphraseConfirmTextBox;
        private Label passphraseLabel;
        private TextBox passphraseTextBox;
        private PasswordStrengthIndicator strengthIndicator;

        public NewVaultPromptForm()
        {
            InitializeComponent();
        }

        public string Passphrase => passphraseTextBox.Text;

        public string PassphraseConfirm => passphraseConfirmTextBox.Text;

        public void EnablePassphrase()
        {
            int offset = RegionsEnabled ? 1 : 0;
            passphraseTextBox = new TextBox();
            passphraseTextBox.Location = new Point(84, 70);
            passphraseTextBox.Name = "passphraseTextBox";
            passphraseTextBox.Size = new Size(121, 23);
            passphraseTextBox.UseSystemPasswordChar = true;
            passphraseTextBox.TabIndex = 4;
            passphraseTextBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            passphraseTextBox.TextChanged += delegate(object sender, EventArgs args)
            {
                PasswordStrengthValidator.Strength strength = strengthValidator.getScore(passphraseTextBox.Text);
                strengthIndicator.Minimum = PasswordStrengthValidator.Strength.veryweak.getScore();
                strengthIndicator.Maximum = PasswordStrengthValidator.Strength.verystrong.getScore() + 1;
                strengthIndicator.Value = strength.getScore() + 1;
            };
            tableLayoutPanel.RowCount++;
            tableLayoutPanel.RowStyles.Insert(2 + offset, new RowStyle(SizeType.AutoSize));
            tableLayoutPanel.Controls.Add(passphraseTextBox, 1, 2 + offset);
            tableLayoutPanel.SetColumnSpan(passphraseTextBox, 2);
            NativeMethods.SendMessage(passphraseTextBox.Handle, NativeConstants.EM_SETCUEBANNER, 0,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"));

            passphraseConfirmTextBox = new TextBox();
            passphraseConfirmTextBox.Location = new Point(84, 70);
            passphraseConfirmTextBox.Name = "passphraseConfirmTextBox";
            passphraseConfirmTextBox.Size = new Size(121, 23);
            passphraseConfirmTextBox.UseSystemPasswordChar = true;
            passphraseConfirmTextBox.TabIndex = 4;
            passphraseConfirmTextBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            tableLayoutPanel.RowCount++;
            tableLayoutPanel.RowStyles.Insert(3 + offset, new RowStyle(SizeType.AutoSize));
            tableLayoutPanel.Controls.Add(passphraseConfirmTextBox, 1, 3 + offset);
            tableLayoutPanel.SetColumnSpan(passphraseConfirmTextBox, 2);
            tableLayoutPanel.SetRow(okButton, 4 + offset);
            tableLayoutPanel.SetRow(cancelButton, 4 + offset);
            NativeMethods.SendMessage(passphraseConfirmTextBox.Handle, NativeConstants.EM_SETCUEBANNER, 0,
                LocaleFactory.localizedString("Confirm Passphrase", "Cryptomator"));

            strengthIndicator = new PasswordStrengthIndicator();
            strengthIndicator.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            tableLayoutPanel.Controls.Add(strengthIndicator, 3, 2 + offset);
            strengthIndicator.Value = 0;
        }
    }
}