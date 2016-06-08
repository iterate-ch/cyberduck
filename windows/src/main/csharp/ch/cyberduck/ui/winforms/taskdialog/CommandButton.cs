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
using System.ComponentModel;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Text;
using System.Windows.Forms;
using Ch.Cyberduck.Core.Resources;

namespace Ch.Cyberduck.Ui.Winforms.Taskdialog
{
    public partial class CommandButton : Button
    {
        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------

        private const int ARROW_WIDTH = 19;
        private const int LEFT_MARGIN = 10;
        private const int TOP_MARGIN = 10;
        private Image imgArrow1;
        private Image imgArrow2;
        private bool m_autoHeight = true;

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------
        // Override this to make sure the control is invalidated (repainted) when 'Text' is changed

        // SmallFont is the font used for secondary lines
        private Font m_smallFont;

        private eButtonState m_State = eButtonState.Normal;

        public CommandButton()
        {
            InitializeComponent();
            Font = new Font("Arial", 11.75F, FontStyle.Regular, GraphicsUnit.Point, 0);
            m_smallFont = new Font("Arial", 8F, FontStyle.Regular, GraphicsUnit.Point, 0);
        }

        public override string Text
        {
            get { return base.Text; }
            set
            {
                base.Text = value;
                if (m_autoHeight)
                    Height = GetBestHeight();
                Invalidate();
            }
        }

        public Font SmallFont
        {
            get { return m_smallFont; }
            set { m_smallFont = value; }
        }

        // AutoHeight determines whether the button automatically resizes itself to fit the Text

        [Browsable(true)]
        [Category("Behavior")]
        [DefaultValue(true)]
        public bool AutoHeight
        {
            get { return m_autoHeight; }
            set
            {
                m_autoHeight = value;
                if (m_autoHeight) Invalidate();
            }
        }

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------
        public int GetBestHeight()
        {
            return (TOP_MARGIN*2) + (int) GetSmallTextSizeF().Height + (int) GetLargeTextSizeF().Height;
        }

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------
        private string GetLargeText()
        {
            string[] lines = Text.Split(new[] {'\n'});
            return lines[0];
        }

        private string GetSmallText()
        {
            if (Text.IndexOf('\n') < 0)
                return String.Empty;

            string s = Text;
            string[] lines = s.Split(new[] {'\n'});
            s = String.Empty;
            for (int i = 1; i < lines.Length; i++)
                s += lines[i] + "\n";
            return s.Trim(new[] {'\n'});
        }

        private SizeF GetLargeTextSizeF()
        {
            int x = LEFT_MARGIN + ARROW_WIDTH + 5;
            SizeF mzSize = new SizeF(Width - x - LEFT_MARGIN, 5000.0F); // presume RIGHT_MARGIN = LEFT_MARGIN
            Graphics g = Graphics.FromHwnd(Handle);
            SizeF textSize = g.MeasureString(GetLargeText(), Font, mzSize);
            return textSize;
        }

        private SizeF GetSmallTextSizeF()
        {
            string s = GetSmallText();
            if (s == String.Empty) return new SizeF(0, 0);
            int x = LEFT_MARGIN + ARROW_WIDTH + 8; // <- indent small text slightly more
            SizeF mzSize = new SizeF(Width - x - LEFT_MARGIN, 5000.0F); // presume RIGHT_MARGIN = LEFT_MARGIN
            Graphics g = Graphics.FromHwnd(Handle);
            SizeF textSize = g.MeasureString(s, m_smallFont, mzSize);
            return textSize;
        }

        //--------------------------------------------------------------------------------

        //--------------------------------------------------------------------------------
        protected override void OnCreateControl()
        {
            base.OnCreateControl();
            imgArrow1 = IconCache.Instance.IconForName("greenArrow1");
            imgArrow2 = IconCache.Instance.IconForName("greenArrow1");
        }

        //--------------------------------------------------------------------------------
        protected override void OnPaint(PaintEventArgs e)
        {
            e.Graphics.SmoothingMode = SmoothingMode.HighQuality;
            e.Graphics.TextRenderingHint = TextRenderingHint.ClearTypeGridFit;

            LinearGradientBrush brush;
            LinearGradientMode mode = LinearGradientMode.Vertical;

            Rectangle newRect = new Rectangle(ClientRectangle.X, ClientRectangle.Y, ClientRectangle.Width - 1,
                ClientRectangle.Height - 1);
            Color text_color = SystemColors.WindowText;

            Image img = imgArrow1;

            if (Enabled)
            {
                switch (m_State)
                {
                    case eButtonState.Normal:
                        e.Graphics.FillRectangle(Brushes.White, newRect);
                        if (Focused)
                            e.Graphics.DrawRectangle(new Pen(Color.SkyBlue, 1), newRect);
                        else
                            e.Graphics.DrawRectangle(new Pen(Color.White, 1), newRect);
                        text_color = Color.DarkBlue;
                        break;

                    case eButtonState.MouseOver:
                        brush = new LinearGradientBrush(newRect, Color.White, Color.WhiteSmoke, mode);
                        e.Graphics.FillRectangle(brush, newRect);
                        e.Graphics.DrawRectangle(new Pen(Color.Silver, 1), newRect);
                        img = imgArrow2;
                        text_color = Color.Blue;
                        break;

                    case eButtonState.Down:
                        brush = new LinearGradientBrush(newRect, Color.WhiteSmoke, Color.White, mode);
                        e.Graphics.FillRectangle(brush, newRect);
                        e.Graphics.DrawRectangle(new Pen(Color.DarkGray, 1), newRect);
                        text_color = Color.DarkBlue;
                        break;
                }
            }
            else
            {
                brush = new LinearGradientBrush(newRect, Color.WhiteSmoke, Color.Gainsboro, mode);
                e.Graphics.FillRectangle(brush, newRect);
                e.Graphics.DrawRectangle(new Pen(Color.DarkGray, 1), newRect);
                text_color = Color.DarkBlue;
            }

            string largetext = GetLargeText();
            string smalltext = GetSmallText();

            SizeF szL = GetLargeTextSizeF();
            //e.Graphics.DrawString(largetext, base.Font, new SolidBrush(text_color), new RectangleF(new PointF(LEFT_MARGIN + imgArrow1.Width + 5, TOP_MARGIN), szL));
            TextRenderer.DrawText(e.Graphics, largetext, Font,
                new Rectangle(LEFT_MARGIN + imgArrow1.Width + 5, TOP_MARGIN, (int) szL.Width,
                    (int) szL.Height), text_color, TextFormatFlags.Default);

            if (smalltext != String.Empty)
            {
                SizeF szS = GetSmallTextSizeF();
                e.Graphics.DrawString(smalltext, m_smallFont, new SolidBrush(text_color),
                    new RectangleF(
                        new PointF(LEFT_MARGIN + imgArrow1.Width + 8, TOP_MARGIN + (int) szL.Height),
                        szS));
            }

            e.Graphics.DrawImage(img, new Point(LEFT_MARGIN, TOP_MARGIN + (int) (szL.Height/2) - (img.Height/2)));
        }

        //--------------------------------------------------------------------------------
        protected override void OnMouseLeave(EventArgs e)
        {
            m_State = eButtonState.Normal;
            Invalidate();
            base.OnMouseLeave(e);
        }

        //--------------------------------------------------------------------------------
        protected override void OnMouseEnter(EventArgs e)
        {
            m_State = eButtonState.MouseOver;
            Invalidate();
            base.OnMouseEnter(e);
        }

        //--------------------------------------------------------------------------------
        protected override void OnMouseUp(MouseEventArgs e)
        {
            m_State = eButtonState.MouseOver;
            Invalidate();
            base.OnMouseUp(e);
        }

        //--------------------------------------------------------------------------------
        protected override void OnMouseDown(MouseEventArgs e)
        {
            m_State = eButtonState.Down;
            Invalidate();
            base.OnMouseDown(e);
        }

        //--------------------------------------------------------------------------------
        protected override void OnSizeChanged(EventArgs e)
        {
            if (m_autoHeight)
            {
                int h = GetBestHeight();
                if (Height != h)
                {
                    Height = h;
                    return;
                }
            }
            base.OnSizeChanged(e);
        }

        private enum eButtonState
        {
            Normal,
            MouseOver,
            Down
        }

        //--------------------------------------------------------------------------------
    }
}