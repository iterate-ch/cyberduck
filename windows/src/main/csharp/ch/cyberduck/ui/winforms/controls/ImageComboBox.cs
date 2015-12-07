// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.Reflection;
using System.Text;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    public class ImageComboBox : ComboBox
    {
        private ImageList _icImagList = new ImageList();

        public ImageComboBox()
        {
            DrawMode = DrawMode.OwnerDrawFixed;
            DropDownStyle = ComboBoxStyle.DropDownList;
        }

        public string IconMember { get; set; }

        /// <summary>
        /// ImageList Property
        /// </summary>
        public ImageList ICImageList
        {
            get { return _icImagList; }
            set { _icImagList = value; }
        }

        /// <summary>
        /// Override OnDrawItem, To Be able To Draw Images, Text, And Font Formatting
        /// </summary>
        /// <param name="e"></param>
        protected override void OnDrawItem(DrawItemEventArgs e)
        {
            e.DrawBackground();

            if (e.Index < 0)
                //Just Draw Indented Text
                e.Graphics.DrawString(Text, e.Font, new SolidBrush(e.ForeColor),
                                      e.Bounds.Left + _icImagList.ImageSize.Width, e.Bounds.Top);
            else
            {
                string iconKey = IconKey(Items[e.Index]);
                int yOffset = (Height - _icImagList.ImageSize.Height)/2 - Margin.Top;
                int xOffset = _icImagList.ImageSize.Width + 3;

                e.Graphics.DrawRectangle(new Pen(Color.White), e.Bounds);
                if (e.State == DrawItemState.Selected)
                {
                    e.Graphics.FillRectangle(new SolidBrush(Color.FromKnownColor(KnownColor.Highlight)), e.Bounds);
                }
                else
                {
                    e.Graphics.FillRectangle(new SolidBrush(e.BackColor), e.Bounds);
                }
                if (ICImageList.Images.ContainsKey(iconKey))
                {
                    ICImageList.Draw(e.Graphics, e.Bounds.Left + 1, e.Bounds.Top + yOffset,
                                     ICImageList.Images.IndexOfKey(iconKey));
                }
                else
                {
                    xOffset = 0;
                }
                e.Graphics.DrawString(GetItemText(Items[e.Index]), e.Font, new SolidBrush(e.ForeColor),
                                      e.Bounds.Left + xOffset, e.Bounds.Top + yOffset);
            }
            e.DrawFocusRectangle();
        }

        /// <summary>
        /// Get the iconKey property via Reflection
        /// </summary>
        /// <param name="item"></param>
        /// <returns></returns>
        private string IconKey(object item)
        {
            PropertyInfo property = item.GetType().GetProperty(IconMember);
            if (property != null)
            {
                return (string) property.GetValue(item, null);
            }
            return null;
        }
    }

    [Serializable]
    public struct KeyValueIconTriple<TKey, TValue>
    {
        private readonly string _iconKey;
        private readonly TKey _key;
        private readonly TValue _value;

        public KeyValueIconTriple(TKey key, TValue value, String iconKey)
        {
            _key = key;
            _value = value;
            _iconKey = iconKey;
        }

        public TKey Key
        {
            get { return _key; }
        }

        public TValue Value
        {
            get { return _value; }
        }

        public string IconKey
        {
            get { return _iconKey; }
        }

        public override string ToString()
        {
            StringBuilder s = new StringBuilder();
            s.Append('[');
            if (Key != null)
            {
                s.Append(Key.ToString());
            }
            s.Append(", ");
            if (Value != null)
            {
                s.Append(Value.ToString());
            }
            s.Append(", ");
            if (IconKey != null)
            {
                s.Append(IconKey);
            }
            s.Append(']');
            return s.ToString();
        }
    }
}