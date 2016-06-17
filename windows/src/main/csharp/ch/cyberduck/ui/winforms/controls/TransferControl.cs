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
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Controller;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    public partial class TransferControl : UserControl, IProgressView
    {
        private bool _focusRemoveAllowed = true;
        private bool _selected;

        public TransferControl()
        {
            Font = SystemFonts.MessageBoxFont;

            InitializeComponent();

            SetStyle(
                ControlStyles.DoubleBuffer | ControlStyles.AllPaintingInWmPaint | ControlStyles.OptimizedDoubleBuffer,
                true);

            // route events to parent control which is normally a ListView
            statusLabel.Click += DelegateOnClick;
            statusLabel.DoubleClick += DelegateOnDoubleClick;
            progressLabel.Click += DelegateOnClick;
            progressLabel.DoubleClick += DelegateOnDoubleClick;
            messageLabel.Click += DelegateOnClick;
            messageLabel.DoubleClick += DelegateOnDoubleClick;
            directionPictureBox.Click += DelegateOnClick;
            directionPictureBox.DoubleClick += DelegateOnDoubleClick;
            statusPictureBox.Click += DelegateOnClick;
            statusPictureBox.DoubleClick += DelegateOnDoubleClick;
            progressBar.Click += DelegateOnClick;
            progressBar.DoubleClick += DelegateOnDoubleClick;

            filesComboBox.Click += delegate
            {
                _focusRemoveAllowed = false;
                OnClick(EventArgs.Empty);
            };
            filesComboBox.LostFocus += delegate { _focusRemoveAllowed = true; };
            progressBar.MarqueeAnimationSpeed = 50;

            statusLabel.Font = new Font(statusLabel.Font, FontStyle.Bold);
            Disposed += delegate { statusLabel.Font.Dispose(); };

            // initialize colors
            Selected = false;

            // force handle creation in case that the transfer form is hidden during the transfer.
            // Otherwise the progress update fails due to an uninitialized control. Also refer to
            // http://trac.cyberduck.ch/ticket/5691
            IntPtr intPtr = Handle;
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
                    directionPictureBox.Image = ResourcesBundle.transfer_download;
                }
                if (value == TransferDirection.Upload)
                {
                    directionPictureBox.Image = ResourcesBundle.transfer_upload;
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

        public void Activate()
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

        public TaskDialogResult MessageBox(string title, string message, string content, string expandedInfo,
            string help, string verificationText, DialogResponseHandler handler)
        {
            return TaskDialogResult.Empty;
        }

        public TaskDialogResult MessageBox(string title, string message, string content, TaskDialogCommonButtons buttons,
            TaskDialogIcon icons)
        {
            return TaskDialogResult.Empty;
        }

        public TaskDialogResult CommandBox(string title, string mainInstruction, string content, string expandedInfo,
            string help, string verificationText, string commandButtons, bool showCancelButton, TaskDialogIcon mainIcon,
            TaskDialogIcon footerIcon, DialogResponseHandler handler)
        {
            return TaskDialogResult.Empty;
        }

        public event VoidHandler ViewShownEvent = delegate { };
        public event VoidHandler ViewClosedEvent = delegate { };
        public event FormClosingEventHandler ViewClosingEvent = delegate { };
        public event VoidHandler PositionSizeRestoredEvent = delegate { };
        public event VoidHandler ViewDisposedEvent;

        private void DelegateOnDoubleClick(object sender, EventArgs e)
        {
            OnDoubleClick(EventArgs.Empty);
        }

        private void DelegateOnClick(object sender, EventArgs e)
        {
            OnClick(EventArgs.Empty);
        }
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