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
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    internal class EllipsisLabel : Label
    {
        public EllipsisLabel()
        {
            DoubleBuffered = true;
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            if (string.IsNullOrEmpty(Text)) return;

            e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;
            e.Graphics.CompositingQuality = CompositingQuality.HighQuality;
            e.Graphics.InterpolationMode = InterpolationMode.Low;

            SizeF drawSize = e.Graphics.MeasureString(Text, Font, new PointF(),
                                                      StringFormat.GenericDefault);

            /*
            textRect = new Rectangle(Point.Empty, TextRenderer.MeasureText(Text, Font, Size, TextFormatFlags.SingleLine | TextFormatFlags.NoPrefix));
            int width = textRect.Width + 1,
                height = textRect.Height + 1;
            */

            PointF point = new PointF();
            if (AutoSize)
            {
                point.X = Padding.Left;
                point.Y = Padding.Top;
            }
            else
            {
                // Text is Left-Aligned:
                if (TextAlign == ContentAlignment.TopLeft ||
                    TextAlign == ContentAlignment.MiddleLeft ||
                    TextAlign == ContentAlignment.BottomLeft)
                    point.X = Padding.Left;

                    // Text is Center-Aligned
                else if (TextAlign == ContentAlignment.TopCenter ||
                         TextAlign == ContentAlignment.MiddleCenter ||
                         TextAlign == ContentAlignment.BottomCenter)
                    point.X = (Width - drawSize.Width)/2;

                    // Text is Right-Aligned
                else point.X = Width - (Padding.Right + drawSize.Width);

                // Text is Top-Aligned
                if (TextAlign == ContentAlignment.TopLeft ||
                    TextAlign == ContentAlignment.TopCenter ||
                    TextAlign == ContentAlignment.TopRight)
                    point.Y = Padding.Top;

                    // Text is Middle-Aligned
                else if (TextAlign == ContentAlignment.MiddleLeft ||
                         TextAlign == ContentAlignment.MiddleCenter ||
                         TextAlign == ContentAlignment.MiddleRight)
                    point.Y = (Height - drawSize.Height)/2;

                    // Text is Bottom-Aligned
                else point.Y = Height - (Padding.Bottom + drawSize.Height);
            }

            e.Graphics.Clear(BackColor);

            StringFormat sf = StringFormat.GenericDefault;
            sf.FormatFlags = StringFormatFlags.LineLimit;
            sf.Trimming = StringTrimming.EllipsisPath;
            //why do we have to pad in? We do not need that with TextRenderer.
            RectangleF rect = new RectangleF(0, -1, Width, Height +1 );
            
            using (SolidBrush brush = new SolidBrush(ForeColor))
            {
                //TextFormatFlags.PathEllipsis works only for *file paths*, e.g. ftp paths do not work
                //http://connect.microsoft.com/VisualStudio/feedback/details/95413/textrenderer-does-not-draw-correctly-using-textformatflags-wordellipsis-or-textformatflags-pathellipsis
                //TextRenderer.DrawText(e.Graphics, Text, Font, rect, ForeColor, TextFormatFlags.PathEllipsis | TextFormatFlags.NoPrefix );
                
                e.Graphics.DrawString(Text, Font, brush, rect, sf);
            }
        }
    }
}