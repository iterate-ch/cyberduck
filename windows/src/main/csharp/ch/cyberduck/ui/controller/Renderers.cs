// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Globalization;
using System.Windows.Forms;
using System.Windows.Forms.VisualStyles;
using BrightIdeasSoftware;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Resources;

namespace Ch.Cyberduck.Ui.Controller
{
    public class AnimatedImageRenderer : ImageRenderer
    {
        public AnimatedImageRenderer()
        {
        }

        public AnimatedImageRenderer(bool startAnimations) : base(startAnimations)
        {
        }

        public AspectGetterDelegate AnimationRunningAspectGetter { get; set; }

        public override void Render(Graphics g, Rectangle r)
        {
            if (null != AnimationRunningAspectGetter)
            {
                IConvertible convertable = AnimationRunningAspectGetter(RowObject) as IConvertible;
                if (convertable != null)
                {
                    bool aspectValue = convertable.ToBoolean(NumberFormatInfo.InvariantInfo);
                    if (aspectValue)
                    {
                        Pause();
                        Unpause();
                    }
                    else
                    {
                        // remove image if aspect is false
                        DrawBackground(g, r);
                        return;
                    }
                }
            }
            base.Render(g, r);
        }

        protected override void HandleHitTest(Graphics g, OlvListViewHitTestInfo hti, int x, int y)
        {
            Rectangle r = CalculateAlignedRectangle(g, Bounds);

            // Did they hit a check box?
            int width = CalculateCheckBoxWidth(g);
            Rectangle r2 = r;
            r2.Width = width;
            if (r2.Contains(x, y))
            {
                hti.HitTestLocation = HitTestLocation.CheckBox;
                return;
            }

            // Did they hit the image? If they hit the image of a 
            // non-primary column that has a checkbox, it counts as a 
            // checkbox hit
            r.X += width;
            r.Width -= width;
            width = CalculateImageWidth(g, GetImageSelector());
            r2 = r;
            r2.Width = width;
            Image img = GetImageSelector() as Image;
            // fix for vertical hitting (w/o the entire vertical column surrounding the image) 
            if (null != img)
            {
                r2.Y += (r.Height - img.Height)/2;
                r2.Height = img.Height;
            }
            // end fix
            if (r2.Contains(x, y))
            {
                if (Column.Index > 0 && Column.CheckBoxes)
                    hti.HitTestLocation = HitTestLocation.CheckBox;
                else
                    hti.HitTestLocation = HitTestLocation.Image;
                return;
            }

            // Did they hit the text?
            r.X += width;
            r.Width -= width;
            width = CalculateTextWidth(g, GetText());
            r2 = r;
            r2.Width = width;
            if (r2.Contains(x, y))
            {
                hti.HitTestLocation = HitTestLocation.Text;
                return;
            }

            hti.HitTestLocation = HitTestLocation.InCell;
        }

        protected override Image GetImage(object imageSelector)
        {
            Image img = base.GetImage(imageSelector);
            if (null != img)
            {
                return img;
            }
            if (imageSelector is string)
            {
                return
                    IconCache.Instance.IconForName((string)imageSelector, 0);
            }
            return null;
        }
    }

    public class TaskRenderer : DescribedTaskRenderer
    {
        private int _titleDescriptionSpace = 3;

        /// <summary>
        /// Space between title and description
        /// </summary>
        public int TitleDescriptionSpace
        {
            get { return _titleDescriptionSpace; }
            set { _titleDescriptionSpace = value; }
        }

        /// <summary>
        /// Title delegate. 
        /// </summary>
        /// <remarks>
        /// This delegate has precedence over DescriptionAspectName
        /// </remarks>
        public AspectGetterDelegate DescriptionAspectGetter { get; set; }

        protected override string GetDescription()
        {
            if (null != DescriptionAspectGetter)
            {
                return (string) DescriptionAspectGetter(RowObject);
            }
            return base.GetDescription();
        }

        protected override void DrawDescribedTask(Graphics g, Rectangle r, string title, string description, Image image)
        {
            Rectangle cellBounds = r;
            cellBounds.Inflate(-CellPadding.Value.Width, -CellPadding.Value.Height);
            Rectangle textBounds = cellBounds;

            if (image != null)
            {
                g.DrawImage(image, cellBounds.Location);
                int gapToText = image.Width + ImageTextSpace;
                textBounds.X += gapToText;
                textBounds.Width -= gapToText;
            }

            // Color the background if the row is selected and we're not using a translucent selection
            if (IsItemSelected && !ListView.UseTranslucentSelection)
            {
                using (SolidBrush b = new SolidBrush(GetTextBackgroundColor()))
                {
                    g.FillRectangle(b, textBounds);
                }
            }

            // Draw the title
            if (!String.IsNullOrEmpty(title))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = TitleFontOrDefault;
                    using (SolidBrush b = new SolidBrush(TitleColorOrDefault))
                    {
                        g.DrawString(title, f, b, textBounds, fmt);
                    }

                    // How tall was the title?
                    SizeF size = g.MeasureString(title, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += TitleDescriptionSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the description
            if (!String.IsNullOrEmpty(description))
            {
                using (StringFormat fmt2 = new StringFormat())
                {
                    fmt2.Trimming = StringTrimming.EllipsisCharacter;
                    using (SolidBrush b = new SolidBrush(DescriptionColorOrDefault))
                    {
                        g.DrawString(description, DescriptionFontOrDefault, b, textBounds, fmt2);
                    }
                }
            }
        }
    }

    /// <summary>
    /// This renderer draws an error
    /// </summary>
    /// <remarks>
    /// <para>This class works best with FullRowSelect = true.</para>
    /// <para>It's not designed to work with cell editing -- it will work but will look odd.</para>
    /// </remarks>
    public class ErrorRenderer : BaseRenderer
    {
        private Size cellPadding = new Size(2, 2);
        private Color descriptionColor = Color.Red;
        private Color errorColor = Color.Black;
        private Color hostColor = Color.Black;

        /// <summary>
        /// Gets or set the color of the host of the error
        /// </summary>
        /// <remarks>This color is used when the error is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        [Category("Appearance - ObjectListView"), Description("The color of the host"),
         DefaultValue(typeof (Color), "Black")]
        public Color HostColor
        {
            get { return hostColor; }
            set { hostColor = value; }
        }

        /// <summary>
        /// Return the color of the host of the error or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Color HostColorOrDefault
        {
            get
            {
                if (HostColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return HostColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the description of the task
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        [Category("Appearance - ObjectListView"),
         Description("The font that will be used to draw the host field of the error"), DefaultValue(null)]
        public Font HostFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the host or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Font HostFontOrDefault
        {
            get { return HostFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the color of the description of the task
        /// </summary>
        /// <remarks>This color is used when the task is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        [Category("Appearance - ObjectListView"), Description("The color of the description"),
         DefaultValue(typeof (Color), "Red")]
        public Color DescriptionColor
        {
            get { return descriptionColor; }
            set { descriptionColor = value; }
        }

        /// <summary>
        /// Return the color of the description of the task or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Color DescriptionColorOrDefault
        {
            get
            {
                if (DescriptionColor.IsEmpty ||
                    (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return DescriptionColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the description of the task
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        [Category("Appearance - ObjectListView"),
         Description("The font that will be used to draw the description of the task"), DefaultValue(null)]
        public Font DescriptionFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the title or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Font DescriptionFontOrDefault
        {
            get { return DescriptionFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the error message
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        [Category("Appearance - ObjectListView"), Description("The font that will be used to draw the error message"),
         DefaultValue(null)]
        public Font ErrorFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the error message or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Font ErrorFontOrDefault
        {
            get { return ErrorFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the color of the error message
        /// </summary>
        /// <remarks>This color is used when the error is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        [Category("Appearance - ObjectListView"), Description("The color of the error message"),
         DefaultValue(typeof (Color), "Black")]
        public Color ErrorColor
        {
            get { return errorColor; }
            set { errorColor = value; }
        }

        /// <summary>
        /// Return the color of the title of the task or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Color ErrorColorOrDefault
        {
            get
            {
                if (ErrorColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return ErrorColor;
            }
        }

        /// <summary>
        /// Gets or sets the number of pixels that renderer will leave empty around the edge of the cell
        /// </summary>
        [Category("Appearance - ObjectListView"),
         Description("The number of pixels that renderer will leave empty around the edge of the cell"),
         DefaultValue(typeof (Size), "2,2")]
        public Size CellPadding
        {
            get { return cellPadding; }
            set { cellPadding = value; }
        }

        /// <summary>
        /// Gets or sets the number of pixels that will be left between the host and the description field
        /// </summary>
        [Category("Appearance - ObjectListView"),
         Description("The number of pixels that will be left between the host and the description field"),
         DefaultValue(4)]
        public int ErrorHostSpace { get; set; }

        /// <summary>
        /// Gets or sets the number of pixels that will be left between the description and the error field
        /// </summary>
        [Category("Appearance - ObjectListView"),
         Description("The number of pixels that will be left between the description and the error field"),
         DefaultValue(4)]
        public int HostDescriptionSpace { get; set; }

        /// <summary>
        /// Gets or sets the delegate of the aspect of the model object that contains the description 
        /// </summary>
        public AspectGetterDelegate DescriptionAspectGetter { get; set; }

        /// <summary>
        /// Gets or sets the delegate of the aspect of the model object that contains the error message
        /// </summary>
        public AspectGetterDelegate ErrorAspectGetter { get; set; }

        public override void Render(Graphics g, Rectangle r)
        {
            DrawBackground(g, r);
            DrawError(g, r, Aspect as String, DescriptionAspectGetter(RowObject) as String,
                      ErrorAspectGetter(RowObject) as String);
        }

        public virtual void DrawError(Graphics g, Rectangle r, String host, String description, String error)
        {
            Rectangle cellBounds = r;
            cellBounds.Inflate(-CellPadding.Width, -CellPadding.Height);
            Rectangle textBounds = cellBounds;

            // Color the background if the row is selected and we're not using a translucent selection
            if (IsItemSelected && !ListView.UseTranslucentSelection)
            {
                using (SolidBrush b = new SolidBrush(GetTextBackgroundColor()))
                {
                    g.FillRectangle(b, textBounds);
                }
            }

            // Draw the error field
            if (!String.IsNullOrEmpty(error))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = ErrorFontOrDefault;
                    using (SolidBrush b = new SolidBrush(ErrorColorOrDefault))
                    {
                        g.DrawString(error, f, b, textBounds, fmt);
                    }

                    // How tall was the error field?
                    SizeF size = g.MeasureString(error, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += ErrorHostSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the host field
            if (!String.IsNullOrEmpty(host))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisPath;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = HostFontOrDefault;
                    using (SolidBrush b = new SolidBrush(HostColorOrDefault))
                    {
                        g.DrawString(host, f, b, textBounds, fmt);
                    }

                    // How tall was the host field?
                    SizeF size = g.MeasureString(host, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += HostDescriptionSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the description field
            if (!String.IsNullOrEmpty(description))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = DescriptionFontOrDefault;
                    using (SolidBrush b = new SolidBrush(DescriptionColorOrDefault))
                    {
                        g.DrawString(description, f, b, textBounds, fmt);
                    }

                    // How tall was the description field?
                    SizeF size = g.MeasureString(error, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    //textBounds.Y += HostDescriptionSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }
        }

        protected override void HandleHitTest(Graphics g, OlvListViewHitTestInfo hti, int x, int y)
        {
            if (Bounds.Contains(x, y))
                hti.HitTestLocation = HitTestLocation.Text;
        }
    }

    public abstract class AbstractBookmarkRenderer : BaseRenderer
    {
        private Size cellPadding = new Size(2, 2);
        private Color hostnameColor = Color.Black;
        private Color nicknameColor = Color.Black;
        private Color notesColor = Color.Black;
        private Color urlColor = Color.Black;

        /// <summary>
        /// Gets or set the color of the description of the task
        /// </summary>
        /// <remarks>This color is used when the task is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        public Color NicknameColor
        {
            get { return nicknameColor; }
            set { nicknameColor = value; }
        }

        /// <summary>
        /// Return the color of the nickname or a reasonable default
        /// </summary>
        public Color NicknameColorOrDefault
        {
            get
            {
                if (NicknameColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return NicknameColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the nickname of the bookmark
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        public Font NicknameFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the host or a reasonable default
        /// </summary>
        public Font NicknameFontOrDefault
        {
            get { return NicknameFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the color of the hostname
        /// </summary>
        /// <remarks>This color is used when the task is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        public Color HostnameColor
        {
            get { return hostnameColor; }
            set { hostnameColor = value; }
        }

        /// <summary>
        /// Return the color of the hostname or a reasonable default
        /// </summary>
        public Color HostnameColorOrDefault
        {
            get
            {
                if (HostnameColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                {
                    return GetForegroundColor();
                }
                return HostnameColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the hostname of the bookmark
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        public Font HostnameFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the hostname or a reasonable default
        /// </summary>
        public Font HostnameFontOrDefault
        {
            get { return HostnameFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the color of the URL
        /// </summary>
        /// <remarks>This color is used when the task is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        public Color UrlColor
        {
            get { return urlColor; }
            set { urlColor = value; }
        }

        /// <summary>
        /// Return the color of the URL or a reasonable default
        /// </summary>
        public Color UrlColorOrDefault
        {
            get
            {
                if (UrlColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return UrlColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the URL of the bookmark
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        public Font UrlFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the hostname or a reasonable default
        /// </summary>
        [Browsable(false)]
        public Font UrlFontOrDefault
        {
            get { return UrlFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or set the color of the notes
        /// </summary>
        /// <remarks>This color is used when the task is not selected or when the listview
        /// has a translucent selection mechanism.</remarks>
        public Color NotesColor
        {
            get { return notesColor; }
            set { notesColor = value; }
        }

        /// <summary>
        /// Return the color of the notes or a reasonable default
        /// </summary>
        public Color NotesColorOrDefault
        {
            get
            {
                if (NotesColor.IsEmpty || (IsItemSelected && ListView.Focused && !ListView.UseTranslucentSelection))
                    return GetForegroundColor();
                else
                    return NotesColor;
            }
        }

        /// <summary>
        /// Gets or set the font that will be used to draw the notes of the bookmark
        /// </summary>
        /// <remarks>If this is null, the ListView's font will be used</remarks>
        public Font NotesFont { get; set; }

        /// <summary>
        /// Return a font that has been set for the hostname or a reasonable default
        /// </summary>
        public Font NotesFontOrDefault
        {
            get { return NotesFont ?? ListView.Font; }
        }

        /// <summary>
        /// Gets or sets the number of pixels that renderer will leave empty around the edge of the cell
        /// </summary>
        public Size CellPadding
        {
            get { return cellPadding; }
            set { cellPadding = value; }
        }

        /// <summary>
        /// Gets or sets the number of pixels that will be left between the nickname and the server field
        /// </summary>
        public int NicknameHostnameSpace { get; set; }

        /// <summary>
        /// Gets or sets the number of pixels that will be left between the Hostname and the URL field
        /// </summary>
        public int HostnameUrlSpace { get; set; }

        /// <summary>
        /// Gets or sets the number of pixels that will be left between the URL and the notes field
        /// </summary>
        public int UrlNotesSpace { get; set; }

        /// <summary>
        /// Gets or sets the delegate of the aspect of the model object that contains the hostname 
        /// </summary>
        public AspectGetterDelegate HostnameAspectGetter { get; set; }

        /// <summary>
        /// Gets or sets the delegate of the aspect of the model object that contains the URL
        /// </summary>
        public AspectGetterDelegate UrlAspectGetter { get; set; }

        /// <summary>
        /// Gets or sets the delegate of the aspect of the model object that contains the notes
        /// </summary>
        public AspectGetterDelegate NotesAspectGetter { get; set; }

        public override void Render(Graphics g, Rectangle r)
        {
            DrawBackground(g, r);
            DrawBookmark(g, r, Aspect as String, HostnameAspectGetter(RowObject) as String,
                         UrlAspectGetter(RowObject) as String, NotesAspectGetter(RowObject) as String);
        }

        public abstract void DrawBookmark(Graphics g, Rectangle r, String nickname, String hostname, String url,
                                          String notes);

        protected override void HandleHitTest(Graphics g, OlvListViewHitTestInfo hti, int x, int y)
        {
            if (Bounds.Contains(x, y))
                hti.HitTestLocation = HitTestLocation.Text;
        }
    }

    public class SmallBookmarkRenderer : AbstractBookmarkRenderer
    {
        public override void DrawBookmark(Graphics g, Rectangle r, String nickname, String hostname, String url,
                                          String notes)
        {
            Rectangle cellBounds = r;
            cellBounds.Inflate(-CellPadding.Width, -CellPadding.Height);
            Rectangle textBounds = cellBounds;

            // Color the background if the row is selected and we're not using a translucent selection
            if (IsItemSelected && !ListView.UseTranslucentSelection)
            {
                using (SolidBrush b = new SolidBrush(GetTextBackgroundColor()))
                {
                    g.FillRectangle(b, textBounds);
                }
            }

            // Draw the nickname field
            if (!String.IsNullOrEmpty(nickname))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = NicknameFontOrDefault;
                    using (SolidBrush b = new SolidBrush(NicknameColorOrDefault))
                    {
                        g.DrawString(nickname, f, b, textBounds, fmt);
                    }

                    // How tall was the URL field?
                    SizeF size = g.MeasureString(nickname, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += NicknameHostnameSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }
        }
    }

    public class MediumBookmarkRenderer : AbstractBookmarkRenderer
    {
        public override void DrawBookmark(Graphics g, Rectangle r, string nickname, string hostname, string url,
                                          string notes)
        {
            Rectangle cellBounds = r;
            cellBounds.Inflate(-CellPadding.Width, -CellPadding.Height);
            Rectangle textBounds = cellBounds;

            // Color the background if the row is selected and we're not using a translucent selection
            if (IsItemSelected && !ListView.UseTranslucentSelection)
            {
                using (SolidBrush b = new SolidBrush(GetTextBackgroundColor()))
                {
                    g.FillRectangle(b, textBounds);
                }
            }

            // Draw the nickname field
            if (!String.IsNullOrEmpty(nickname))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = NicknameFontOrDefault;
                    using (SolidBrush b = new SolidBrush(NicknameColorOrDefault))
                    {
                        g.DrawString(nickname, f, b, textBounds, fmt);
                    }
                    SizeF size = g.MeasureString(nickname, f, textBounds.Width, fmt);
                    textBounds.Y += (int)size.Height;
                    textBounds.Y += NicknameHostnameSpace;
                    textBounds.Height -= (int)size.Height;
                }
            }

            // Draw the hostname field
            if (!String.IsNullOrEmpty(hostname))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = HostnameFontOrDefault;
                    using (SolidBrush b = new SolidBrush(HostnameColorOrDefault))
                    {
                        g.DrawString(hostname, f, b, textBounds, fmt);
                    }
                    SizeF size = g.MeasureString(hostname, f, textBounds.Width, fmt);
                    textBounds.Y += (int)size.Height;
                    textBounds.Y += HostnameUrlSpace;
                    textBounds.Height -= (int)size.Height;
                }
            }

            // Draw the URL field
            if (!String.IsNullOrEmpty(url))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisPath;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = UrlFontOrDefault;
                    using (SolidBrush b = new SolidBrush(UrlColorOrDefault))
                    {
                        g.DrawString(url, f, b, textBounds, fmt);
                    }
                    SizeF size = g.MeasureString(url, f, textBounds.Width, fmt);
                    textBounds.Y += (int)size.Height;
                    textBounds.Y += UrlNotesSpace;
                    textBounds.Height -= (int)size.Height;
                }
            }
        }
    }

    /// <summary>
    /// This renderer draws a bookmark
    /// </summary>
    /// <remarks>
    /// <para>This class works best with FullRowSelect = true.</para>
    /// <para>It's not designed to work with cell editing -- it will work but will look odd.</para>
    /// </remarks>
    public class LargeBookmarkRenderer : AbstractBookmarkRenderer
    {
        public override void DrawBookmark(Graphics g, Rectangle r, String nickname, String hostname, String url,
                                          String notes)
        {
            Rectangle cellBounds = r;
            cellBounds.Inflate(-CellPadding.Width, -CellPadding.Height);
            Rectangle textBounds = cellBounds;

            // Color the background if the row is selected and we're not using a translucent selection
            if (IsItemSelected && !ListView.UseTranslucentSelection)
            {
                using (SolidBrush b = new SolidBrush(GetTextBackgroundColor()))
                {
                    g.FillRectangle(b, textBounds);
                }
            }

            // Draw the nickname field
            if (!String.IsNullOrEmpty(nickname))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = NicknameFontOrDefault;
                    using (SolidBrush b = new SolidBrush(NicknameColorOrDefault))
                    {
                        g.DrawString(nickname, f, b, textBounds, fmt);
                    }

                    // How tall was the URL field?
                    SizeF size = g.MeasureString(nickname, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += NicknameHostnameSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the hostname field
            if (!String.IsNullOrEmpty(hostname))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = HostnameFontOrDefault;
                    using (SolidBrush b = new SolidBrush(HostnameColorOrDefault))
                    {
                        g.DrawString(hostname, f, b, textBounds, fmt);
                    }

                    // How tall was the URL field?
                    SizeF size = g.MeasureString(hostname, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += HostnameUrlSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the URL field
            if (!String.IsNullOrEmpty(url))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisPath;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = UrlFontOrDefault;
                    using (SolidBrush b = new SolidBrush(UrlColorOrDefault))
                    {
                        g.DrawString(url, f, b, textBounds, fmt);
                    }

                    // How tall was the URL field?
                    SizeF size = g.MeasureString(url, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    textBounds.Y += UrlNotesSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }

            // Draw the notes field
            if (!String.IsNullOrEmpty(notes))
            {
                using (StringFormat fmt = new StringFormat(StringFormatFlags.NoWrap))
                {
                    fmt.Trimming = StringTrimming.EllipsisCharacter;
                    fmt.Alignment = StringAlignment.Near;
                    fmt.LineAlignment = StringAlignment.Near;
                    Font f = NotesFontOrDefault;
                    using (SolidBrush b = new SolidBrush(NotesColorOrDefault))
                    {
                        g.DrawString(notes, f, b, textBounds, fmt);
                    }

                    // How tall was the notes field?
                    SizeF size = g.MeasureString(notes, f, textBounds.Width, fmt);
                    textBounds.Y += (int) size.Height;
                    //textBounds.Y += HostDescriptionSpace;
                    textBounds.Height -= (int) size.Height;
                }
            }
        }
    }

    /// <summary>
    /// ToolStrip renderer that does not have this ugly white line at the bottom.
    /// </summary>
    public class ToolStripRenderer : ToolStripSystemRenderer
    {
        protected override void OnRenderToolStripBorder(ToolStripRenderEventArgs e)
        {
            base.OnRenderToolStripBorder(e);

            Rectangle rect = e.AffectedBounds;
            rect.Height = 2;
            rect.Offset(0, e.AffectedBounds.Height - 2);
            e.Graphics.FillRectangle(new SolidBrush(e.BackColor), rect);
            return;
        }
    }

    /// <summary>
    /// Renderer to make the browser tree Explorer like (default explorer theme)
    /// </summary>
    public class BrowserRenderer : TreeListView.TreeRenderer
    {
        // see http://stackoverflow.com/questions/3014816/visualstylerenderer-and-themes-winforms
        private static readonly VisualStyleRenderer ClosedGlyphRenderer;
        private static readonly VisualStyleRenderer OpenedGlyphRenderer;

        static BrowserRenderer()
        {
            if (Utils.IsVistaOrLater && VisualStyleRenderer.IsSupported)
            {
                ClosedGlyphRenderer =
                    new VisualStyleRenderer(VisualStyleElement.CreateElement("Explorer::TreeView", 2, 1));
                OpenedGlyphRenderer =
                    new VisualStyleRenderer(VisualStyleElement.CreateElement("Explorer::TreeView", 2, 2));
            }
        }

        public BrowserRenderer()
        {
            IsShowLines = false;
        }

        protected override void DrawExpansionGlyph(Graphics g, Rectangle r, bool isExpanded)
        {
            if (Utils.IsVistaOrLater && VisualStyleRenderer.IsSupported)
            {
                VisualStyleRenderer renderer = isExpanded ? OpenedGlyphRenderer : ClosedGlyphRenderer;
                renderer.DrawBackground(g, r);
            }
            else
            {
                base.DrawExpansionGlyph(g, r, isExpanded);
            }
        }
    }

    /// <summary>
    /// Explorer gradient-style renderer for selected rows.
    /// </summary>
    public class ExplorerRowBorderDecoration : RowBorderDecoration
    {
        public ExplorerRowBorderDecoration()
        {
            BoundsPadding = new Size(0, -1);
            CornerRounding = 5.0f;
            BorderPen = new Pen(Color.FromArgb(128, Color.SteelBlue), 1);
        }

        protected override Rectangle CalculateBounds()
        {
            Rectangle bounds = base.CalculateBounds();
            bounds.Y--;
            bounds.Height++;
            bounds.Width--;
            // update brush
            if (FillBrush != null)
            {
                FillBrush.Dispose();
            }
            FillBrush = new LinearGradientBrush(bounds, Color.FromArgb(64, Color.LightBlue),
                                                Color.FromArgb(64, Color.DodgerBlue), LinearGradientMode.Vertical);
            return bounds;
        }
    }

    //
    //see https://sourceforge.net/p/objectlistview/discussion/812922/thread/445489ce/
    //
    public class FixedImageRenderer : BaseRenderer
    {
        protected override int DrawImage(Graphics g, Rectangle r, object imageSelector)
        {
            Image image = imageSelector as Image;
            if (image != null)
            {
                if (image.Size.Height < r.Height)
                    r.Y = AlignVertically(r, new Rectangle(Point.Empty, image.Size));

                g.DrawImageUnscaled(image, r.X, r.Y);
                return image.Width;
            }
            return 0;
        }
    }
}