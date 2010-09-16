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
using System.Collections;
using System.Drawing;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Winforms
{
    public partial class BaseForm : Form, IView
    {
        //private static readonly Logger Log = Logger.getLogger(typeof (BaseForm).Name);
        protected Commands Commands = new Commands();
        private bool _releaseWhenClose = true;

        public BaseForm()
        {
            // workaround for http://connect.microsoft.com/VisualStudio/feedback/details/115408/give-net-windows-forms-the-mfc-7-look-with-the-tahoma-font-as-default 
            // see also http://stackoverflow.com/questions/297701/default-font-for-windows-forms-application
            Font = SystemFonts.MessageBoxFont;

            InitializeComponent();

            SetStyle(
                ControlStyles.DoubleBuffer | ControlStyles.AllPaintingInWmPaint |
                ControlStyles.OptimizedDoubleBuffer, true);


            // not checking if there are any subscribers might result in 'Object reference not set to an instance...'
            Shown += delegate
                         {
                             VoidHandler shownEvent = ViewShownEvent;
                             if (null != shownEvent) ViewShownEvent();
                         };

            FormClosed += delegate
                              {
                                  VoidHandler closedEvent = ViewClosedEvent;
                                  if (null != closedEvent) ViewClosedEvent();
                                  Application.Idle -= OnApplicationIdle;
                              };

            Load += delegate
                        {
                            if (!DesignMode)
                            {
                                LocalizeTexts();
                                EventHandler localizationCompleted = LocalizationCompleted;
                                if (null != localizationCompleted) LocalizationCompleted(this, EventArgs.Empty);
                            }
                        };

            FormClosing += delegate(object sender, FormClosingEventArgs args)
                               {                                   
                                   if (!_releaseWhenClose && args.CloseReason == CloseReason.UserClosing)
                                   {
                                       //Log.debug("Cancel close event");
                                       args.Cancel = true;
                                       Hide();
                                       return;
                                   }
                                   FormClosingEventHandler closingEvent = ViewClosingEvent;
                                   if (null != closingEvent) ViewClosingEvent(sender, args);
                               };
            
            Disposed += delegate
                            {
                                VoidHandler disposedEvent = ViewDisposedEvent;
                                if (null != disposedEvent) ViewDisposedEvent();
                            };

            Application.Idle += OnApplicationIdle;

            Load += persistentFormLoad;
            FormClosing += persistentFormFormClosing;
        }

        protected PersistentFormHandler PersistenceHandler { get; set; }

        /// <summary>
        /// null if there is no localization available for this form
        /// </summary>
        public virtual string[] BundleNames
        {
            get { return new string[]{}; }
        }

        public DialogResult MessageBox(string title, string message, string content, eTaskDialogButtons buttons,
                                       eSysIcons icon)
        {
            BringToFront();
            return cTaskDialog.MessageBox(this,
                                          title,
                                          message,
                                          content,
                                          buttons,
                                          icon);
        }

        public int CommandBox(string title, string mainInstruction, string content, string expandedInfo, string footer,
                              string verificationText, string commandButtons, bool showCancelButton, eSysIcons mainIcon,
                              eSysIcons footerIcon)
        {
            BringToFront();
            return cTaskDialog.ShowCommandBox(this,
                                              title,
                                              mainInstruction,
                                              content,
                                              expandedInfo,
                                              footer,
                                              verificationText,
                                              commandButtons,
                                              showCancelButton,
                                              mainIcon,
                                              footerIcon);
        }

        public event VoidHandler ViewShownEvent;
        public event VoidHandler ViewClosedEvent;
        public event FormClosingEventHandler ViewClosingEvent;
        public event VoidHandler ViewDisposedEvent;

        public void ValidateCommands()
        {
            Commands.Validate();
        }

        public new void Close()
        {
            base.Close();
        }

        public new bool Visible
        {
            get { return ((Control) this).Visible; }
            set
            {
                if (value)
                {
                    Show();
                }
                else
                {
                    Hide();
                }
            }
        }

        public bool ReleaseWhenClose
        {
            set { _releaseWhenClose = value; }
        }

        public new bool IsDisposed
        {
            get { return base.IsDisposed; }
        }

        public new bool Disposing
        {
            get { return base.Disposing; }
        }

        public new void Show()
        {
            base.Show();
            Focus();
        }

        public void Show(IView owner)
        {
            if (owner is Form)
            {
                base.Show(owner as Form);
            }
            {
                Show();
            }
        }

        public DialogResult ModalResult
        {
            get { return DialogResult; }
        }

        public new void Show(IWin32Window owner)
        {
            base.Show(owner);
            Focus();
        }

        public new DialogResult ShowDialog()
        {
            return base.ShowDialog();
        }

        public new DialogResult ShowDialog(IWin32Window owner)
        {
            return base.ShowDialog(owner);
        }

        public DialogResult ShowDialog(IView owner)
        {
            if (owner is Form)
            {
                return base.ShowDialog(owner as Form);
            }
            return ShowDialog();
        }

        public new void Dispose()
        {
            base.Dispose();
        }

        private void OnApplicationIdle(object sender, EventArgs e)
        {
            Commands.Validate();
        }

        private void persistentFormLoad(object sender, EventArgs e)
        {
            // Create PersistenceHandler and load values from it
            PersistenceHandler = new PersistentFormHandler(GetType(), (int) FormWindowState.Normal, Bounds);
            //PersistenceHandler.Load();
            //handlerReady = true;

            // Set size and location
//            Bounds = PersistenceHandler.WindowBounds;

            // make sure we are on screen
            if (!BoundsVisible(Bounds))
                Location = new Point();

            // Set state
            WindowState = Enum.IsDefined(typeof (FormWindowState), PersistenceHandler.WindowState)
                              ? (FormWindowState) PersistenceHandler.WindowState
                              : FormWindowState.Normal;

            // Notify that values are loaded and ready for getting.
            var handler = ValuesLoaded;
            if (handler != null)
                handler(this, EventArgs.Empty);
        }

        protected event EventHandler<EventArgs> ValuesLoaded;
        protected event EventHandler<EventArgs> StoringValues;

        public static bool BoundsVisible(Rectangle bounds)
        {
            bool FoundAScreenThatContainsThePoint = false;

            for (int i = 0; i < Screen.AllScreens.Length; i++)
            {
                if (Screen.AllScreens[i].Bounds.IntersectsWith(bounds))
                    FoundAScreenThatContainsThePoint = true;
            }
            return FoundAScreenThatContainsThePoint;
        }

        private void persistentFormFormClosing(object sender, FormClosingEventArgs e)
        {
            // Set common things
            PersistenceHandler.WindowState = (int) WindowState;
            PersistenceHandler.WindowBounds = WindowState == FormWindowState.Normal ? Bounds : RestoreBounds;

            // Notify that values will be stored now, so time to store values.
            var handler = StoringValues;
            if (handler != null)
                handler(this, EventArgs.Empty);

            // Save values
            PersistenceHandler.Save();
        }

        public event EventHandler LocalizationCompleted;

        private IEnumerable RecurseObjects(object root)
        {
            Queue items = new Queue();
            items.Enqueue(root);
            while (items.Count > 0)
            {
                object obj = items.Dequeue();
                yield return obj;
                Control control = obj as Control;
                if (control != null)
                {
                    // regular controls and sub-controls
                    foreach (Control item in control.Controls)
                    {
                        items.Enqueue(item);
                    }
                    // top-level menu items
                    ToolStrip ts = control as ToolStrip;
                    if (ts != null)
                    {
                        foreach (ToolStripItem tsi in ts.Items)
                        {
                            items.Enqueue(tsi);
                        }
                    }
                }
                // child menus
                ToolStripDropDownItem tsddi = obj as ToolStripDropDownItem;
                if (tsddi != null && tsddi.HasDropDownItems)
                {
                    foreach (ToolStripItem item in tsddi.DropDownItems)
                    {
                        items.Enqueue(item);
                    }
                }
            }
        }

        private void LocalizeTexts()
        {
            foreach (var o in RecurseObjects(this))
            {
                if (o is Label ||
                    o is CheckBox ||
                    o is GroupBox ||
                    o is Button ||
                    o is TabPage ||
                    o is RadioButton ||
                    o is Form)
                {
                    Control c = (Control) o;
                    c.Text = LookupInMultipleBundles(c.Text, BundleNames);
                    continue;
                }

                if (o is ToolStripItem)
                {
                    ToolStripItem i = (ToolStripItem) o;
                    i.Text = LookupInMultipleBundles(i.Text.Replace("&", String.Empty), BundleNames);
                    continue;
                }

                if (o is ListView)
                {
                    ObjectListView lv = (ObjectListView) o;
                    foreach (OLVColumn column in lv.AllColumns)
                    {
                        column.Text = LookupInMultipleBundles(column.Text, BundleNames);
                    }
                    continue;
                }
            }
        }

        private string LookupInMultipleBundles(string toLocalize, string[] bundles)
        {
            foreach (string bundle in bundles)
            {
                string cand = Locale.localizedString(toLocalize, bundle);
                if (!toLocalize.Equals(cand)) return cand;
            }
            return toLocalize;
        }

        protected static Bitmap GetIcon(string iconIdentifier)
        {
            object obj = ResourcesBundle.ResourceManager.GetObject(iconIdentifier, ResourcesBundle.Culture);
            return (Bitmap) obj;
        }
    }
}