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
using System.Drawing.Drawing2D;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.io;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;
using Windows7.DesktopIntegration.WindowsForms;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class TransferForm : BaseForm, ITransferView
    {
        private static readonly Font FixedFont = new Font("Courier New", 8, FontStyle.Regular);

        private static readonly int MaxHeight = 800;
        private static readonly int MaxWidth = 800;
        private static readonly int MinHeight = 400;
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
                                                             TransferControl uc =
                                                                 ((TransferControl)
                                                                  transferListView.GetEmbeddedControl(1, e.ItemIndex));
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
        }

        public override string[] BundleNames
        {
            get { return new[] {"Transfer"}; }
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
            set { splitContainer.SplitterDistance = splitContainer.Height - value; }
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
                            if (m.Tag.Equals((float) BandwidthThrottle.UNLIMITED))
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

        public void SetModel(IList<IProgressView> model)
        {
            transferListView.RemoveAllEmbeddedControls();
            transferListView.Objects = model;
            for (int i = 0; i < model.Count; i++)
            {
                transferListView.AddEmbeddedControl((model[i] as Control), 1, i, DockStyle.Fill);
            }
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
            transferListView.AddObject(view);
            transferListView.AddEmbeddedControl(view as Control, 1, transferListView.GetItemCount() - 1, DockStyle.Fill);
        }

        public void RemoveTransfer(IProgressView view)
        {
            transferListView.RemoveEmbeddedControl(view as Control);
            transferListView.RemoveObject(view);
        }

        public void AddTranscriptEntry(bool request, string entry)
        {
            transcriptBox.SelectionFont = FixedFont;
            if (request)
            {
                transcriptBox.SelectionColor = Color.Black;
            }
            else
            {
                transcriptBox.SelectionColor = Color.DarkGray;
            }
            transcriptBox.SelectedText = entry + Environment.NewLine;
            // todo improve performance
            // Select seems to be an expensive operation
            // see http://codebetter.com/blogs/patricksmacchia/archive/2008/07/07/some-richtextbox-tricks.aspx
            transcriptBox.Select(transcriptBox.TextLength, transcriptBox.TextLength);
            transcriptBox.ScrollToCaret();
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

        public event VoidHandler ResumeEvent;
        public event VoidHandler ReloadEvent;
        public event VoidHandler StopEvent;
        public event VoidHandler RemoveEvent;
        public event VoidHandler CleanEvent;
        public event VoidHandler OpenEvent;
        public event VoidHandler ShowEvent;
        public event VoidHandler ToggleTranscriptEvent;
        public event VoidHandler SelectionChangedEvent;
        public event VoidHandler BandwidthChangedEvent;
        public event VoidHandler QueueSizeChangedEvent = delegate { };
        public event VoidHandler TranscriptHeightChangedEvent = delegate { };
        public event ValidateCommand ValidateResumeEvent;
        public event ValidateCommand ValidateReloadEvent;
        public event ValidateCommand ValidateStopEvent;
        public event ValidateCommand ValidateRemoveEvent;
        public event ValidateCommand ValidateCleanEvent;
        public event ValidateCommand ValidateOpenEvent;
        public event ValidateCommand ValidateShowEvent;

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

        public void TaskbarBadge(string text)
        {
            if (null == text)
            {
                this.SetTaskbarOverlayIcon(null, String.Empty);
            }
            else
            {
                Bitmap bm = new Bitmap(16, 16);
                Graphics g = Graphics.FromImage(bm);
                g.SmoothingMode = SmoothingMode.AntiAlias;
                g.FillEllipse(Brushes.Navy, new Rectangle(0, 0, 15, 15));

                if (text.Length == 1)
                {
                    Font f = new Font("Segoe UI", 8, FontStyle.Bold);
                    g.DrawString(text, f, new SolidBrush(Color.White), 3, 1);
                }
                else
                {
                    Font f = new Font("Segoe UI", 7, FontStyle.Bold);
                    g.DrawString(text, f, new SolidBrush(Color.White), 1, 1);
                }
                this.SetTaskbarOverlayIcon(Icon.FromHandle(bm.GetHicon()), text);

                g.Dispose();
                bm.Dispose();
            }
        }

        private void ConfigureToolbarMenu(ToolStripMenuItem menuItem, ToolStripButton button, String property)
        {
            menuItem.CheckOnClick = true;
            menuItem.Click += delegate
                                  {
                                      button.Visible =
                                          !button.Visible;
                                      Preferences.instance().setProperty(
                                          property,
                                          button.Visible);
                                  };

            button.Visible = menuItem.Checked = Preferences.instance().getBoolean(property);
        }

        private void ConfigureToolbar()
        {
            ConfigureToolbarMenu(resumeToolStripMenuItem, resumeToolStripButton, "transfer.toolbar.resume");
            ConfigureToolbarMenu(reloadToolStripMenuItem, reloadToolStripButton, "transfer.toolbar.reload");
            ConfigureToolbarMenu(stopToolStripMenuItem, stopToolStripButton, "transfer.toolbar.stop");
            ConfigureToolbarMenu(removeToolStripMenuItem, removeToolStripButton, "transfer.toolbar.remove");
            ConfigureToolbarMenu(cleanUpToolStripMenuItem, cleanUptoolStripButton, "transfer.toolbar.cleanup");
            ConfigureToolbarMenu(logToolStripMenuItem, logToolStripButton, "transfer.toolbar.log");
            ConfigureToolbarMenu(openToolStripMenuItem, openToolStripButton, "transfer.toolbar.open");
            ConfigureToolbarMenu(showToolStripMenuItem, showToolStripButton, "transfer.toolbar.show");

            Commands.Add(new ToolStripItem[] {resumeToolStripButton}, (sender, args) => ResumeEvent(),
                         () => ValidateResumeEvent());
            Commands.Add(new ToolStripItem[] {reloadToolStripButton}, (sender, args) => ReloadEvent(),
                         () => ValidateReloadEvent());
            Commands.Add(new ToolStripItem[] {stopToolStripButton}, (sender, args) => StopEvent(),
                         () => ValidateStopEvent());
            Commands.Add(new ToolStripItem[] {removeToolStripButton}, (sender, args) => RemoveEvent(),
                         () => ValidateRemoveEvent());
            Commands.Add(new ToolStripItem[] {cleanUptoolStripButton}, (sender, args) => CleanEvent(),
                         () => ValidateCleanEvent());
            Commands.Add(new ToolStripItem[] {logToolStripButton}, (sender, args) => ToggleTranscriptEvent(), () => true);

            Commands.Add(new ToolStripItem[] {openToolStripButton}, (sender, args) => OpenEvent(),
                         () => ValidateOpenEvent());
            Commands.Add(new ToolStripItem[] {showToolStripButton}, (sender, args) => ShowEvent(),
                         () => ValidateShowEvent());
        }

        private void queueSizeUpDown_ValueChanged(object sender, EventArgs e)
        {
            QueueSizeChangedEvent();
        }

        private void toolbarMenuStrip_ItemClicked(object sender, ToolStripItemClickedEventArgs e)
        {
            _lastMenuItemClicked = (ToolStripMenuItem) e.ClickedItem;
        }

        private void toolbarMenuStrip_Closing(object sender, ToolStripDropDownClosingEventArgs e)
        {
            e.Cancel = (
                           e.CloseReason == ToolStripDropDownCloseReason.ItemClicked &&
                           _lastMenuItemClicked != null);
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

        private void transferListView_DoubleClick(object sender, EventArgs e)
        {
            ReloadEvent();
        }
    }
}