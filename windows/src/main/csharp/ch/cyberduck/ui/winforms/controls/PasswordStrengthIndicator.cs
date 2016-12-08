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

using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    public class PasswordStrengthIndicator : ProgressBar
    {
        private readonly Dictionary<int, Color> _colors;

        public PasswordStrengthIndicator()
        {
            SetStyle(ControlStyles.UserPaint, true);
            _colors = new Dictionary<int, Color>();
            _colors.Add(0, Color.Red);
            _colors.Add(1, Color.Red);
            _colors.Add(2, Color.DarkOrange);
            _colors.Add(3, Color.Orange);
            _colors.Add(4, Color.LightGreen);
            _colors.Add(5, Color.Green);
        }

        protected override void OnPaint(PaintEventArgs e)
        {
            SolidBrush brush = null;
            Rectangle rec = new Rectangle(0, 0, Width, Height);
            double scaleFactor = (((double) Value - (double) Minimum)/((double) Maximum - (double) Minimum));

            if (ProgressBarRenderer.IsSupported)
                ProgressBarRenderer.DrawHorizontalBar(e.Graphics, rec);

            rec.Width = (int) ((rec.Width*scaleFactor) - 4);
            rec.Height -= 4;
            Color color = Value <= Maximum ? _colors[Value] : _colors[_colors.Count];
            brush = new SolidBrush(color);
            e.Graphics.FillRectangle(brush, 2, 2, rec.Width, rec.Height);
        }
    }
}