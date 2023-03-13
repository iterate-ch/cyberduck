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
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Controls;
using System;
using System.Windows.Forms;
using Windows.Win32.Foundation;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.PInvoke;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class NewVaultPromptForm : NewFolderPromptForm, INewVaultPromptView
    {
        private readonly TableLayoutPanel passphraseLayout;
        private readonly PasswordStrengthValidator strengthValidator = new();
        private TextBox passphraseConfirmTextBox;
        private TextBox passphraseTextBox;
        private PasswordStrengthIndicator strengthIndicator;

        public NewVaultPromptForm()
        {
            InitializeComponent();

            tableLayoutPanel.SuspendLayout();

            var buttonRow = tableLayoutPanel.RowCount++;
            var layoutRow = buttonRow - 1;
            tableLayoutPanel.SetRow(buttonPanel, buttonRow);
            passphraseLayout = new TableLayoutPanel()
            {
                ColumnCount = 1,
                Dock = DockStyle.Fill,
                GrowStyle = TableLayoutPanelGrowStyle.FixedSize,
                RowCount = 3,
            };
            tableLayoutPanel.RowStyles.Insert(layoutRow, new());
            tableLayoutPanel.Controls.Add(passphraseLayout, 1, layoutRow);

            tableLayoutPanel.ResumeLayout();
        }

        public string Passphrase => passphraseTextBox.Text;

        public string PassphraseConfirm => passphraseConfirmTextBox.Text;

        public void EnablePassphrase()
        {
            passphraseTextBox = new TextBox();
            passphraseTextBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            passphraseTextBox.Name = "passphraseTextBox";
            passphraseTextBox.UseSystemPasswordChar = true;
            passphraseTextBox.TabIndex = 4;
            passphraseTextBox.TextChanged += delegate (object sender, EventArgs args)
            {
                PasswordStrengthValidator.Strength strength = strengthValidator.getScore(passphraseTextBox.Text);
                strengthIndicator.Minimum = PasswordStrengthValidator.Strength.veryweak.getScore();
                strengthIndicator.Maximum = PasswordStrengthValidator.Strength.verystrong.getScore() + 1;
                strengthIndicator.Value = strength.getScore() + 1;
            };
            passphraseLayout.Controls.Add(passphraseTextBox, 0, 0);
            SendMessage((HWND)passphraseTextBox.Handle, EM_SETCUEBANNER, 0,
                LocaleFactory.localizedString("Passphrase", "Cryptomator Key"));

            strengthIndicator = new PasswordStrengthIndicator();
            strengthIndicator.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            strengthIndicator.Value = 0;
            passphraseLayout.Controls.Add(strengthIndicator, 0, 1);

            passphraseConfirmTextBox = new TextBox();
            passphraseConfirmTextBox.Anchor = (((AnchorStyles.Left | AnchorStyles.Right)));
            passphraseConfirmTextBox.Name = "passphraseConfirmTextBox";
            passphraseConfirmTextBox.UseSystemPasswordChar = true;
            passphraseConfirmTextBox.TabIndex = 4;
            passphraseLayout.Controls.Add(passphraseConfirmTextBox, 0, 2);
            SendMessage((HWND)passphraseConfirmTextBox.Handle, EM_SETCUEBANNER, 0,
                LocaleFactory.localizedString("Confirm Passphrase", "Cryptomator"));
        }
    }
}
