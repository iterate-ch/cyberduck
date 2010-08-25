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
using System.Drawing.Drawing2D;
using System.Windows.Forms;
using BrightIdeasSoftware;
using ch.cyberduck;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.ui.controller;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms.Serializer;

namespace Ch.Cyberduck.Ui.Winforms
{
    [Obsolete("This class is not used anymore. Functionality is directly integrated into BrowserForm")]
    public partial class BookmarkManagerForm : BaseForm, IBookmarkManagerView
    {
        public BookmarkManagerForm()
        {
            InitializeComponent();

            bookmarkListView.RowHeight = 72;
            bookmarkListView.ShowGroups = false;
            bookmarkListView.UseOverlays = false;
            bookmarkListView.OwnerDraw = true;
            bookmarkListView.FullRowSelect = true;
            bookmarkListView.MultiSelect = false;
            bookmarkListView.HeaderStyle = ColumnHeaderStyle.None;
            bookmarkListView.UseAlternatingBackColors = true;
            bookmarkListView.AlternateRowBackColor = Color.WhiteSmoke;

            BookmarkRenderer bookmarkRenderer = new BookmarkRenderer();
            Font smallerFont = new Font(bookmarkListView.Font.FontFamily, bookmarkListView.Font.Size - 1);
            bookmarkRenderer.NicknameFont = new Font(bookmarkListView.Font, FontStyle.Bold);
            bookmarkRenderer.HostnameFont = smallerFont;
            bookmarkRenderer.UrlFont = smallerFont;
            bookmarkRenderer.NotesFont = smallerFont;
            bookmarkRenderer.UrlNotesSpace = 3;

            //taskRenderer.CellPadding = new Size(2, 5);
            descriptionColumn.Renderer = bookmarkRenderer;
            descriptionColumn.FillsFreeSpace = true;

            bookmarkImageColumn.Renderer = new ImageRenderer();
            bookmarkImageColumn.Width = 90;
            bookmarkImageColumn.TextAlign = HorizontalAlignment.Center;
            activeColumn.Renderer = new ImageRenderer();

            addToolStripButton.Tag = ResourcesBundle.addPressed;
            editToolStripButton.Tag = ResourcesBundle.editPressed;
            removeToolStripButton.Tag = ResourcesBundle.removePressed;

            actionToolStrip.Renderer = new NoGapRenderer();
        }

        public AspectGetterDelegate BookmarkActiveImageNameGetter
        {
            set { activeColumn.AspectGetter = value; }
        }

        public ImageGetterDelegate BookmarkStatusImageGetter
        {
            set { activeColumn.ImageGetter = value; }
        }

        public Host SelectedBookmark
        {
            get { return (Host) bookmarkListView.SelectedObject; }
        }

        public void SetBookmarkModel(IEnumerable hosts)
        {
            bookmarkListView.SetObjects(hosts);
        }

        public void RefreshBookmark(Host host)
        {
            bookmarkListView.RefreshObject(host);
        }

        public void AddBookmark(Host host)
        {
            bookmarkListView.AddObject(host);
        }

        public void RemoveBookmark(Host host)
        {
            bookmarkListView.RemoveObject(host);
        }

        public void EnsureBookmarkVisible(Host host)
        {
            throw new NotImplementedException();
        }

        public void SelectBookmark(Host host)
        {
            throw new NotImplementedException();
        }

        public event VoidHandler NewBookmark;
        public event ValidateCommand ValidateNewBookmark;
        public event VoidHandler EditBookmark;
        public event ValidateCommand ValidateEditBookmark;
        public event VoidHandler DeleteBookmark;
        public event ValidateCommand ValidateDeleteBookmark;

        public AspectGetterDelegate BookmarkImageNameGetter
        {
            set { bookmarkImageColumn.AspectGetter = value; }
        }

        public ImageGetterDelegate BookmarkImageGetter
        {
            set { bookmarkImageColumn.ImageGetter = value; }
        }

        public AspectGetterDelegate BookmarkNicknameGetter
        {
            set { descriptionColumn.AspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkHostnameGetter
        {
            set { ((BookmarkRenderer) descriptionColumn.Renderer).HostnameAspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkUrlGetter
        {
            set { ((BookmarkRenderer) descriptionColumn.Renderer).UrlAspectGetter = value; }
        }

        public AspectGetterDelegate BookmarkNotesGetter
        {
            set { ((BookmarkRenderer) descriptionColumn.Renderer).NotesAspectGetter = value; }
        }

        public event VoidHandler NewBookmarkEvent = delegate { };
        public event VoidHandler EditBookmarkEvent = delegate { };
        public event VoidHandler DeleteBookmarkEvent = delegate { };

        private class NoGapRenderer : ToolStripProfessionalRenderer
        {
            public NoGapRenderer()
            {
                RoundedEdges = false;
            }

            protected override void OnRenderButtonBackground(ToolStripItemRenderEventArgs e)
            {
                //no highlighting
                return;
            }

            protected override void OnRenderToolStripBorder(ToolStripRenderEventArgs e)
            {
                base.OnRenderToolStripBorder(e);
                using (Pen pen = new Pen(Color.Gray))
                {
                    Rectangle bounds = new Rectangle(Point.Empty, e.ToolStrip.Size);
                    e.Graphics.DrawLine(pen, bounds.Left, bounds.Top, bounds.Right, bounds.Top);
                }
            }

            protected override void OnRenderItemImage(ToolStripItemImageRenderEventArgs e)
            {
                Image img;
                if (!e.Item.Enabled)
                {
                    if (null == e.Image.Tag)
                    {
                        e.Image.Tag = CreateDisabledImage(e.Image);
                    }
                    img = (Image) e.Image.Tag;
                }
                else
                {
                    img = e.Item.Pressed && e.Item.Selected ? e.Item.Tag as Image : e.Image;
                }
                Rectangle rect = new Rectangle(new Point(e.ImageRectangle.Left - 2, e.ImageRectangle.Top - 2),
                                               new Size(img.Width, img.Height));
                e.Graphics.DrawImage(img, rect);
            }
        }

        internal class RoundedButtonsStyleRenderer : ToolStripProfessionalRenderer
        {
            protected override void OnRenderButtonBackground(ToolStripItemRenderEventArgs e)
            {
                if (e.Item is ToolStripButton)
                {
                    ToolStripButton button = (ToolStripButton) e.Item;
                    LinearGradientBrush brush;
                    Rectangle bounds = new Rectangle(0, 0, e.Item.Width - 1, e.Item.Height - 1);
                    if (button.Pressed || button.Checked)
                    {
                        brush = new LinearGradientBrush(bounds, Color.WhiteSmoke, Color.LightGray,
                                                        LinearGradientMode.Vertical);
                    }
                    else if (button.Selected)
                    {
                        // hover, currently the same color as above
                        brush = new LinearGradientBrush(bounds, Color.WhiteSmoke, Color.Gainsboro,
                                                        LinearGradientMode.Vertical);
                    }
                    else
                    {
                        base.OnRenderButtonBackground(e);
                        return;
                    }
                    using (GraphicsPath path = GetRoundedRect(bounds, 10))
                    {
                        e.Graphics.FillPath(brush, path);
                        e.Graphics.DrawPath(new Pen(Color.DarkGray, 1), path);
                    }
                }
                else
                {
                    base.OnRenderButtonBackground(e);
                }
            }

            private GraphicsPath GetRoundedRect(RectangleF baseRect,
                                                float radius)
            {
                if (radius <= 0.0F)
                {
                    GraphicsPath mPath = new GraphicsPath();
                    mPath.AddRectangle(baseRect);
                    mPath.CloseFigure();
                    return mPath;
                }

                if (radius >= (Math.Min(baseRect.Width, baseRect.Height))/2.0)
                    return GetCapsule(baseRect);

                float diameter = radius*2.0F;
                SizeF sizeF = new SizeF(diameter, diameter);
                RectangleF arc = new RectangleF(baseRect.Location, sizeF);
                GraphicsPath path = new GraphicsPath();

                // top left arc 
                path.AddArc(arc, 180, 90);

                // top right arc 
                arc.X = baseRect.Right - diameter;
                path.AddArc(arc, 270, 90);

                // bottom right arc 
                arc.Y = baseRect.Bottom - diameter;
                path.AddArc(arc, 0, 90);

                // bottom left arc
                arc.X = baseRect.Left;
                path.AddArc(arc, 90, 90);

                path.CloseFigure();
                return path;
            }

            private GraphicsPath GetCapsule(RectangleF baseRect)
            {
                float diameter;
                RectangleF arc;
                GraphicsPath path = new GraphicsPath();
                try
                {
                    if (baseRect.Width > baseRect.Height)
                    {
                        // return horizontal capsule 
                        diameter = baseRect.Height;
                        SizeF sizeF = new SizeF(diameter, diameter);
                        arc = new RectangleF(baseRect.Location, sizeF);
                        path.AddArc(arc, 90, 180);
                        arc.X = baseRect.Right - diameter;
                        path.AddArc(arc, 270, 180);
                    }
                    else if (baseRect.Width < baseRect.Height)
                    {
                        // return vertical capsule 
                        diameter = baseRect.Width;
                        SizeF sizeF = new SizeF(diameter, diameter);
                        arc = new RectangleF(baseRect.Location, sizeF);
                        path.AddArc(arc, 180, 180);
                        arc.Y = baseRect.Bottom - diameter;
                        path.AddArc(arc, 0, 180);
                    }
                    else
                    {
                        // return circle 
                        path.AddEllipse(baseRect);
                    }
                }
                catch (Exception)
                {
                    path.AddEllipse(baseRect);
                }
                finally
                {
                    path.CloseFigure();
                }
                return path;
            }
        }
    }
}