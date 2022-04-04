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
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Imaging;
using System.Windows.Forms;
using Windows.Win32.Graphics.Gdi;
using Windows.Win32.UI.WindowsAndMessaging;
using static System.Runtime.CompilerServices.Unsafe;
using static Windows.Win32.PInvoke;
using static Windows.Win32.UI.WindowsAndMessaging.MENU_ITEM_MASK;

//VistaMenu v1.8.1, created by Wyatt O'Day
//Visit: http://wyday.com/vistamenu/
//License: BSD

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    //Properties for the MenuItem
    internal class Properties
    {
        public Image Image;
        public IntPtr renderBmpHbitmap = IntPtr.Zero;
    }

    [ProvideProperty("Image", typeof(MenuItem))]
    public partial class VistaMenu : Component, IExtenderProvider, ISupportInitialize
    {
        private readonly bool isVistaOrLater;
        private readonly Hashtable menuParents = new Hashtable();
        private readonly MENUINFO mnuInfo = new MENUINFO();
        private readonly Hashtable properties = new Hashtable();
        private Container components;

        private bool formHasBeenIntialized;

        public VistaMenu()
        {
            isVistaOrLater = Environment.OSVersion.Platform == PlatformID.Win32NT &&
                             Environment.OSVersion.Version.Major >= 6;

            InitializeComponent();
        }

        public VistaMenu(IContainer container)
            : this()
        {
            container.Add(this);
        }

        bool IExtenderProvider.CanExtend(object o)
        {
            if (o is MenuItem)
            {
                // reject the menuitem if it's a top level element on a MainMenu bar
                if (((MenuItem)o).Parent != null)
                    return ((MenuItem)o).Parent.GetType() != typeof(MainMenu);

                // parent is null - meaning it's a context menu
                return true;
            }

            if (o is Form)
                return true;

            return false;
        }

        void ISupportInitialize.BeginInit()
        {
        }

        void ISupportInitialize.EndInit()
        {
            if (!DesignMode)
            {
                if (isVistaOrLater)
                {
                    foreach (DictionaryEntry de in properties)
                    {
                        AddVistaMenuItem((MenuItem)de.Key);
                    }
                }
                else // Pre-Vista menus
                {
                    // Declare the fonts once: 
                    //    If the user changes the menu fonts while your program is 
                    //    running, it's tough luck for the user.
                    //
                    //    This keeps a cap on the memory by avoiding unnecessary Font object 
                    //    creation/destruction on every MenuItem .Measure() and .Draw()
                    menuBoldFont = new Font(SystemFonts.MenuFont, FontStyle.Bold);


                    if (ownerForm != null)
                        ownerForm.ChangeUICues += ownerForm_ChangeUICues;

                    foreach (DictionaryEntry de in properties)
                    {
                        AddPreVistaMenuItem((MenuItem)de.Key);
                    }

                    //add event handle for each menu item's measure & draw routines
                    foreach (DictionaryEntry parent in menuParents)
                    {
                        foreach (MenuItem mnuItem in ((Menu)parent.Key).MenuItems)
                        {
                            mnuItem.DrawItem += MenuItem_DrawItem;
                            mnuItem.MeasureItem += MenuItem_MeasureItem;
                            mnuItem.OwnerDraw = true;
                        }
                    }
                }

                formHasBeenIntialized = true;
            }
        }

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            components = new Container();
        }

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                //release all the HBitmap handles created
                foreach (DictionaryEntry de in properties)
                {
                    if (((Properties)de.Value).renderBmpHbitmap != IntPtr.Zero)
                        DeleteObject((HGDIOBJ)((Properties)de.Value).renderBmpHbitmap);
                }


                if (components != null)
                    components.Dispose();
            }

            base.Dispose(disposing);
        }

        private Properties EnsurePropertiesExists(MenuItem key)
        {
            Properties p = (Properties)properties[key];

            if (p == null)
            {
                p = new Properties();

                properties[key] = p;
            }

            return p;
        }


        [DefaultValue(null)]
        [Description("The Image for the MenuItem")]
        [Category("Appearance")]
        public Image GetImage(MenuItem mnuItem)
        {
            return EnsurePropertiesExists(mnuItem).Image;
        }

        [DefaultValue(null)]
        public void SetImage(MenuItem mnuItem, Image value)
        {
            Properties prop = EnsurePropertiesExists(mnuItem);

            prop.Image = value;
            if (!DesignMode && isVistaOrLater)
            {
                //Destroy old bitmap object
                if (prop.renderBmpHbitmap != IntPtr.Zero)
                {
                    DeleteObject((HGDIOBJ)prop.renderBmpHbitmap);
                    prop.renderBmpHbitmap = IntPtr.Zero;
                }

                //if there's no Image, then just bail out
                if (value == null)
                    return;

                //convert to 32bppPArgb (the 'P' means The red, green, and blue components are premultiplied, according to the alpha component.)
                using (Bitmap renderBmp = new Bitmap(value.Width, value.Height, PixelFormat.Format32bppPArgb))
                {
                    using (Graphics g = Graphics.FromImage(renderBmp))
                        g.DrawImage(value, 0, 0, value.Width, value.Height);

                    prop.renderBmpHbitmap = renderBmp.GetHbitmap(Color.FromArgb(0, 0, 0, 0));
                }

                if (formHasBeenIntialized)
                    AddVistaMenuItem(mnuItem);
            }


            //for every Pre-Vista Windows, add the parent of the menu item to the list of parents
            if (!DesignMode && !isVistaOrLater && formHasBeenIntialized)
            {
                AddPreVistaMenuItem(mnuItem);
            }
        }

        /// <summary>
        /// Added to support SetImage in the PopUp event. Needs to be called after the last SetImage invocation.
        /// </summary>
        /// <param name="parent"></param>
        /// <remarks>yla, 20101019</remarks>
        public void UpdateParent(MenuItem parent)
        {
            MenuItem_Popup(parent, new EventArgs());
        }

        public void UpdateParent(ContextMenu parent)
        {
            MenuItem_Popup(parent, new EventArgs());
        }

        private void AddVistaMenuItem(MenuItem mnuItem)
        {
            //get the bitmap children of the parent
            if (menuParents[mnuItem.Parent] == null)
            {
                if (mnuItem.Parent.GetType() == typeof(ContextMenu))
                    ((ContextMenu)mnuItem.Parent).Popup += MenuItem_Popup;
                else
                    ((MenuItem)mnuItem.Parent).Popup += MenuItem_Popup;

                MenuItem_Popup(mnuItem.Parent, new EventArgs());
                //intialize all the topmost menus to be of type "MNS_CHECKORBMP" (for Vista classic theme)
                SetMenuInfo((HMENU)mnuItem.Parent.Handle, mnuInfo);

                menuParents[mnuItem.Parent] = true;
            }
        }

        private void AddPreVistaMenuItem(MenuItem mnuItem)
        {
            //if (menuParents[mnuItem.Parent] == null)
            {
                menuParents[mnuItem.Parent] = true;
                if (formHasBeenIntialized)
                {
                    //add all the menu items with custom paint events
                    foreach (MenuItem menu in mnuItem.Parent.MenuItems)
                    {
                        //take dynamically added items into account, means removing the handlers first
                        menu.DrawItem -= MenuItem_DrawItem;
                        menu.MeasureItem -= MenuItem_MeasureItem;

                        //and add them again. This way no multiple handler invocations should happen.
                        menu.DrawItem += MenuItem_DrawItem;
                        menu.MeasureItem += MenuItem_MeasureItem;
                        menu.OwnerDraw = true;
                    }
                }
            }
        }

        private void MenuItem_Popup(object sender, EventArgs e)
        {
            MENUITEMINFOW menuItemInfo = new();
            menuItemInfo.cbSize = (uint)SizeOf<MENUITEMINFOW>();
            menuItemInfo.fMask = MIIM_BITMAP;

            // get the menu items collection
            Menu.MenuItemCollection mi = sender.GetType() == typeof(ContextMenu)
                                             ? ((ContextMenu)sender).MenuItems
                                             : ((MenuItem)sender).MenuItems;

            // we have to track the menuPosition ourselves
            // because MenuItem.Index is only correct when
            // all the menu items are visible.
            int miOn = 0;
            for (int i = 0; i < mi.Count; i++)
            {
                if (mi[i].Visible)
                {
                    Properties p = ((Properties)properties[mi[i]]);

                    if (p != null)
                    {
                        menuItemInfo.hbmpItem = (HBITMAP)p.renderBmpHbitmap;

                        //refresh the menu item where ((Menu)sender).Handle is the parent handle
                        SetMenuItemInfo((HMENU)((Menu)sender).Handle,
                                        (uint)miOn,
                                        true,
                                        menuItemInfo);
                    }

                    miOn++;
                }
            }
        }
    }
}
