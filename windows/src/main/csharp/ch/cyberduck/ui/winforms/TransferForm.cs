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
using Windows7.DesktopIntegration;
using Windows7.DesktopIntegration.WindowsForms;
using ch.cyberduck.core.io;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Core;
using Ch.Cyberduck.Ui.Winforms.Controls;
using Utils = Ch.Cyberduck.Core.Utils;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class TransferForm : BaseForm, ITransferView
    {
        private static readonly Font FixedFont = new Font("Courier New", 8, FontStyle.Regular);
        private static readonly int MaxHeight = 800;
        private static readonly int MaxWidth = 800;
        private static readonly int MinHeight = 250;
        private static readonly int MinWidth = 450;
        private string _currentImage = string.Empty;
        private ToolStripMenuItem _lastMenuItemClicked;

        public TransferForm()
        {
            InitializeComponent();

            MaximumSize = new Size(MaxWidth, MaxHeight);
            MinimumSize = new Size(MinWidth, MinHeight);

            ConfigureToolbar();

            bandwithSplitButton.Image = IconCache.Instance.IconForName("bandwidth", 16);
            showToolStripButton.Image = IconCache.Instance.IconForName("reveal");

            transferListView.FullRowSelect = false;
            transferListView.HeaderStyle = ColumnHeaderStyle.None;
            transferListView.ShowGroups = false;
            transferListView.View = View.Details;
            transferListView.RowHeight = 90;
            transferListView.MultiSelect = true;
            transferListView.GridLines = false;
            transferListView.UseOverlays = false;

            dummyColumn.Width = 0;
            transferColumn.FillsFreeSpace = true;
            transferListView.ItemSelectionChanged += delegate(object sender, ListViewItemSelectionChangedEventArgs e)
            {
                TransferControl uc = ((TransferControl) transferListView.GetEmbeddedControl(1, e.ItemIndex));
                if (null != uc)
                {
                    uc.Selected = e.IsSelected;
                    if (e.IsSelected && uc.FocusRemoveAllowed)
                    {
                        transferListView.Select();
                    }
                }
            };
            transferListView.ItemSelectionChanged += (sender, e) => SelectionChangedEvent();
            transferListView.ItemsChanged +=
                delegate { transferListView.GridLines = transferListView.GetItemCount() > 0; };

            //force handle creation
            IntPtr intPtr = Handle;
        }

        public override string[] BundleNames
        {
            get { return new[] {"Transfer", "Localizable"}; }
        }

        public string Url
        {
            set { urlLabel.Text = value; }
        }

        public string Local
        {
            set { localLabel.Text = value; }
        }

        public Image FileIcon
        {
            set { fileIcon.Image = value; }
        }

        public int TranscriptHeight
        {
            get { return splitContainer.Height - splitContainer.SplitterDistance; }
            set
            {
                if (splitContainer.Height > value)
                {
                    splitContainer.SplitterDistance = splitContainer.Height - value;
                }
            }
        }

        public bool TranscriptVisible
        {
            get { return !splitContainer.Panel2Collapsed; }
            set { splitContainer.Panel2Collapsed = !value; }
        }

        public float Bandwidth
        {
            get
            {
                foreach (ToolStripItem item in bandwidthMenuStrip.Items)
                {
                    if (item is ToolStripMenuItem)
                    {
                        ToolStripMenuItem m = (ToolStripMenuItem) item;
                        if (m.Checked)
                        {
                            return (float) m.Tag;
                        }
                    }
                }
                return BandwidthThrottle.UNLIMITED;
            }
            set
            {
                foreach (ToolStripItem item in bandwidthMenuStrip.Items)
                {
                    if (item is ToolStripMenuItem)
                    {
                        ToolStripMenuItem m = (ToolStripMenuItem) item;
                        m.Checked = value.Equals(m.Tag);
                        if (m.Checked)
                        {
                            if (m.Tag.Equals(BandwidthThrottle.UNLIMITED))
                            {
                                if (!_currentImage.Equals("bandwidth"))
                                {
                                    bandwithSplitButton.Image = IconCache.Instance.IconForName("bandwidth", 16);
                                    _currentImage = "bandwidth";
                                }
                            }
                            else
                            {
                                if (!_currentImage.Equals("turtle"))
                                {
                                    bandwithSplitButton.Image = IconCache.Instance.IconForName("turtle");
                                    _currentImage = "turtle";
                                }
                            }
                        }
                    }
                }
            }
        }

        public bool BandwidthEnabled
        {
            set { bandwithSplitButton.Enabled = value; }
        }

        public int QueueSize
        {
            get { return Convert.ToInt32(queueSizeUpDown.Value); }
            set { queueSizeUpDown.Value = value; }
        }

        public void SelectTransfer(IProgressView view)
        {
            transferListView.DeselectAll();
            transferListView.EnsureModelVisible(view);
            int index = transferListView.IndexOf(view);
            transferListView.FocusedItem = transferListView.Items[index];
            transferListView.SelectedObject = view;
        }

        public void AddTransfer(IProgressView view)
        {
            Control c = view as Control;
            if (null != c)
            {
                transferListView.AddObject(view);
                c.DoubleClick += delegate { ReloadEvent(); };
                transferListView.AddEmbeddedControl(c, 1, transferListView.GetItemCount() - 1, DockStyle.Fill);
            }
        }

        public void RemoveTransfer(IProgressView view)
        {
            var control = view as Control;
            transferListView.RemoveEmbeddedControl(control);
            transferListView.RemoveObject(control);
            control.Dispose();
        }

        public void AddTranscriptEntry(TranscriptListener.Type request, string entry)
        {
            transcriptBox.SelectionFont = FixedFont;
            switch (request)
            {
                case request:
                    transcriptBox.SelectionColor = Color.Black;
                    break;
                case response:
                    transcriptBox.SelectionColor = Color.DarkGray;
                    break;
            }
            transcriptBox.SelectedText = entry + Environment.NewLine;
            transcriptBox.Select(transcriptBox.TextLength, transcriptBox.TextLength);
            ScrollToBottom(transcriptBox);
        }

        public IList<IProgressView> SelectedTransfers
        {
            get
            {
                IList<IProgressView> selected = new List<IProgressView>();
                foreach (IProgressView o in transferListView.SelectedObjects)
                {
                    selected.Add(o);
                }
                return selected;
            }
        }

        public event VoidHandler ResumeEvent = delegate { };
        public event VoidHandler ReloadEvent = delegate { };
        public event VoidHandler StopEvent = delegate { };
        public event VoidHandler RemoveEvent = delegate { };
        public event VoidHandler CleanEvent = delegate { };
        public event VoidHandler OpenEvent = delegate { };
        public event VoidHandler ShowEvent = delegate { };
        public event VoidHandler TrashEvent = delegate { };
        public event VoidHandler ToggleTranscriptEvent = delegate { };
        public event VoidHandler SelectionChangedEvent = delegate { };
        public event VoidHandler BandwidthChangedEvent = delegate { };
        public event VoidHandler QueueSizeChangedEvent = delegate { };
        public event VoidHandler TranscriptHeightChangedEvent = delegate { };
        public event ValidateCommand ValidateResumeEvent = () => false;
        public event ValidateCommand ValidateReloadEvent = () => false;
        public event ValidateCommand ValidateStopEvent = () => false;
        public event ValidateCommand ValidateRemoveEvent = () => false;
        public event ValidateCommand ValidateCleanEvent = () => false;
        public event ValidateCommand ValidateOpenEvent = () => false;
        public event ValidateCommand ValidateShowEvent = () => false;

        public void PopulateBandwidthList(IList<KeyValuePair<float, string>> throttles)
        {
            for (int index = 0; index < throttles.Count; index++)
            {
                KeyValuePair<float, string> throttle = throttles[index];
                ToolStripMenuItem item = new ToolStripMenuItem(throttle.Value);
                item.Tag = throttle.Key;
                item.Click += delegate(object sender, EventArgs args)
                {
                    foreach (ToolStripItem i in bandwidthMenuStrip.Items)
                    {
                        if (i is ToolStripMenuItem)
                        {
                            ((ToolStripMenuItem) i).Checked = false;
                        }
                        ((ToolStripMenuItem) sender).Checked = true;
                    }
                    BandwidthChangedEvent();
                };

                bandwidthMenuStrip.Items.Add(item);

                if (index == 0)
                {
                    bandwidthMenuStrip.Items.Add(new ToolStripSeparator());
                }
            }
        }

        public void TaskbarOverlayIcon(Icon icon, string text)
        {
            this.SetTaskbarOverlayIcon(icon, text);
        }

        public void UpdateOverallProgressState(long progress, long maximum)
        {
            if (Utils.IsWin7OrLater)
            {
                if (progress == 0 || maximum == 0)
                {
                    Windows7Taskbar.SetProgressState(Handle, Windows7Taskbar.ThumbnailProgressState.NoProgress);
                }
                else
                {
                    Windows7Taskbar.SetProgressState(Handle, Windows7Taskbar.ThumbnailProgressState.Normal);
                    Windows7Taskbar.SetProgressValue(Handle, (ulong) progress, (ulong) maximum);
                }
            }
        }

        public static void ScrollToBottom(RichTextBox richTextBox)
        {
            NativeMethods.SendMessage(richTextBox.Handle, NativeConstants.WM_VSCROLL, NativeConstants.SB_BOTTOM, 0);
        }

        private void ConfigureToolbarMenu(ToolStripMenuItem menuItem, ToolStripButton button, String property)
        {
            menuItem.CheckOnClick = true;
            menuItem.Click += delegate
            {
                button.Visible = !button.Visible;
                PreferencesFactory.get().setProperty(property, button.Visible);
            };

            button.Visible = menuItem.Checked = PreferencesFactory.get().getBoolean(property);
        }

        private void ConfigureToolbar()
        {
            ConfigureToolbarMenu(resumeToolStripMenuItem, resumeToolStripButton, "transfer.toolbar.resume");
            ConfigureToolbarMenu(reloadToolStripMenuItem, reloadToolStripButton, "transfer.toolbar.reload");
            ConfigureToolbarMenu(stopToolStripMenuItem, stopToolStripButton, "transfer.toolbar.stop");
            ConfigureToolbarMenu(removeToolStripMenuItem, removeToolStripButton, "transfer.toolbar.remove");
            ConfigureToolbarMenu(cleanUpToolStripMenuItem, cleanUptoolStripButton, "transfer.toolbar.cleanup");
            ConfigureToolbarMenu(logToolStripMenuItem, logToolStripButton, "transfer.toolbar.log");
            ConfigureToolbarMenu(trashToolStripMenuItem, trashToolStripButton, "transfer.toolbar.trash");
            ConfigureToolbarMenu(openToolStripMenuItem, openToolStripButton, "transfer.toolbar.open");
            ConfigureToolbarMenu(showToolStripMenuItem, showToolStripButton, "transfer.toolbar.show");

            Commands.Add(new ToolStripItem[] {resumeToolStripButton}, null, (sender, args) => ResumeEvent(),
                () => ValidateResumeEvent());
            Commands.Add(new ToolStripItem[] {reloadToolStripButton}, null, (sender, args) => ReloadEvent(),
                () => ValidateReloadEvent());
            Commands.Add(new ToolStripItem[] {stopToolStripButton}, null, (sender, args) => StopEvent(),
                () => ValidateStopEvent());
            Commands.Add(new ToolStripItem[] {removeToolStripButton}, null, (sender, args) => RemoveEvent(),
                () => ValidateRemoveEvent());
            Commands.Add(new ToolStripItem[] {cleanUptoolStripButton}, null, (sender, args) => CleanEvent(),
                () => ValidateCleanEvent());
            Commands.Add(new ToolStripItem[] {logToolStripButton}, null, (sender, args) => ToggleTranscriptEvent(),
                () => true);
            Commands.Add(new ToolStripItem[] {trashToolStripButton}, null, (sender, args) => TrashEvent(),
                () => ValidateShowEvent());
            Commands.Add(new ToolStripItem[] {openToolStripButton}, null, (sender, args) => OpenEvent(),
                () => ValidateOpenEvent());
            Commands.Add(new ToolStripItem[] {showToolStripButton}, null, (sender, args) => ShowEvent(),
                () => ValidateShowEvent());
        }

        private void queueSizeUpDown_ValueChanged(object sender, EventArgs e)
        {
            QueueSizeChangedEvent();
        }

        private void toolbarMenuStrip_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            if (e.ClickedItem is ToolStripMenuItem)
            {
                _lastMenuItemClicked = (ToolStripMenuItem) e.ClickedItem;
            }
        }

        private void toolbarMenuStrip_Closing(object sender, ToolStripDropDownClosingEventArgs e)
        {
            e.Cancel = (e.CloseReason == ToolStripDropDownCloseReason.ItemClicked && _lastMenuItemClicked != null);
        }

        private void bandwithSplitButton_Click(object sender, EventArgs e)
        {
            bandwithSplitButton.SplitMenuStrip.Show(bandwithSplitButton, new Point(0, bandwithSplitButton.Height),
                ToolStripDropDownDirection.BelowRight);
        }

        private void splitContainer_SplitterMoved(object sender, SplitterEventArgs e)
        {
            TranscriptHeightChangedEvent();
        }

        private void transferListView_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                ReloadEvent();
            }
            if (e.KeyCode == Keys.Delete)
            {
                RemoveEvent();
            }
        }

        private void transferListView_KeyPress(object sender, KeyPressEventArgs e)
        {
            //do not beep
            if (e.KeyChar == (char) Keys.Enter)
            {
                e.Handled = true;
            }
        }

        private void cancelButton_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}