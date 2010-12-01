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
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;

namespace Ch.Cyberduck.Ui.Controller
{
    public partial class TransferControl : UserControl, IProgressView
    {
        private bool _focusRemoveAllowed = true;
        private bool _selected;

        public TransferControl()
        {            
            SetStyle(
                ControlStyles.DoubleBuffer | ControlStyles.AllPaintingInWmPaint |
                ControlStyles.OptimizedDoubleBuffer, true);

            InitializeComponent();

            // route events to parent control which is normally a ListView
            statusLabel.Click += delegate { OnClick(EventArgs.Empty); };
            progressLabel.Click += delegate { OnClick(EventArgs.Empty); };
            messageLabel.Click += delegate { OnClick(EventArgs.Empty); };
            directionPictureBox.Click += delegate { OnClick(EventArgs.Empty); };
            statusPictureBox.Click += delegate { OnClick(EventArgs.Empty); };
            progressBar.Click += delegate { OnClick(EventArgs.Empty); };

            filesComboBox.Click += delegate
                                       {
                                           _focusRemoveAllowed = false;
                                           OnClick(EventArgs.Empty);
                                       };
            filesComboBox.LostFocus += delegate { _focusRemoveAllowed = true; };
            progressBar.MarqueeAnimationSpeed = 50;

            // initialize colors
            Selected = false;
        }

        public bool FocusRemoveAllowed
        {
            get { return _focusRemoveAllowed; }
        }

        public int ProgressMaximum
        {
            set { progressBar.Maximum = value; }
        }

        public int ProgressValue
        {
            set { progressBar.Value = value; }
        }

        public bool ProgressIndeterminate
        {
            set { progressBar.Style = value ? ProgressBarStyle.Marquee : ProgressBarStyle.Blocks; }
        }

        public bool Selected
        {
            get { return _selected; }
            set
            {
                Color backColor;
                Color foreColor;
                _selected = value;
                if (value)
                {
                    backColor = Color.FromKnownColor(KnownColor.Highlight);
                    foreColor = Color.FromKnownColor(KnownColor.HighlightText);
                }
                else
                {
                    backColor = Color.FromKnownColor(KnownColor.Window);
                    foreColor = Color.FromKnownColor(KnownColor.WindowText);
                }
                BackColor = backColor;
                statusLabel.BackColor = backColor;
                statusLabel.ForeColor = foreColor;
                progressLabel.BackColor = backColor;
                progressLabel.ForeColor = foreColor;
                messageLabel.BackColor = backColor;
                messageLabel.ForeColor = foreColor;
                directionPictureBox.BackColor = backColor;
                statusPictureBox.BackColor = backColor;
            }
        }

        public void PopulateRoots(IList<string> roots)
        {
            filesComboBox.DataSource = roots;
        }

        public string StatusText
        {
            set { statusLabel.Text = value; }
        }

        public string ProgressText
        {
            set { progressLabel.Text = value; }
        }

        public string MessageText
        {
            set { messageLabel.Text = value; }
        }

        public TransferStatus TransferStatus
        {
            set
            {
                switch (value)
                {
                    case TransferStatus.InProgress:
                        progressBar.Visible = true;
                        statusLabel.Visible = false;
                        statusPictureBox.Image = ResourcesBundle.statusYellow;
                        break;
                    case TransferStatus.Complete:
                        progressBar.Visible = false;
                        statusLabel.Visible = true;
                        statusPictureBox.Image = ResourcesBundle.statusGreen;
                        break;
                    case TransferStatus.Incomplete:
                        progressBar.Visible = false;
                        statusLabel.Visible = true;
                        statusPictureBox.Image = ResourcesBundle.statusRed;
                        break;
                }
            }
        }

        public TransferDirection TransferDirection
        {
            set
            {
                if (value == TransferDirection.Download)
                {
                    directionPictureBox.Image = ResourcesBundle.arrowDown;
                }
                if (value == TransferDirection.Upload)
                {
                    directionPictureBox.Image = ResourcesBundle.arrowUp;
                }
                if (value == TransferDirection.Sync)
                {
                    directionPictureBox.Image = ResourcesBundle.sync;
                }
            }
        }

        public bool ReleaseWhenClose
        {
            set { ; }
        }

        public void Close()
        {
            ;
        }

        public void Show(IWin32Window owner)
        {
            ;
        }

        public void Show(IView owner)
        {
            ;
        }

        public DialogResult ModalResult
        {
            get { return DialogResult.None; }
        }

        public DialogResult ShowDialog()
        {
            return DialogResult.None;
        }

        public DialogResult ShowDialog(IWin32Window owner)
        {
            return DialogResult.None;
        }

        public DialogResult ShowDialog(IView owner)
        {
            return DialogResult.None;
        }

        public DialogResult MessageBox(string title, string message, string content, eTaskDialogButtons buttons,
                                       eSysIcons icon)
        {
            return DialogResult.None;
        }

        public int CommandBox(string title, string mainInstruction, string content, string expandedInfo, string footer,
                              string verificationText, string commandButtons, bool showCancelButton, eSysIcons mainIcon,
                              eSysIcons footerIcon)
        {
            return -1;
        }

        public event VoidHandler ViewShownEvent = delegate { };
        public event VoidHandler ViewClosedEvent = delegate { };
        public event FormClosingEventHandler ViewClosingEvent = delegate { };
        public event VoidHandler ViewDisposedEvent;
    }

    public enum TransferStatus
    {
        InProgress,
        Complete,
        Incomplete
    }

    public enum TransferDirection
    {
        Upload,
        Download,
        Sync
    }
}