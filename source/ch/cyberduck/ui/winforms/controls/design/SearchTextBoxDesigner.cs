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
using System.Windows.Forms;
using System.Windows.Forms.Design;

namespace Ch.Cyberduck.Ui.Winforms.Controls.Design
{
    internal class SearchTextBoxDesigner : ControlDesigner
    {
        public SearchTextBoxDesigner()
        {
            base.AutoResizeHandles = false;
        }

        public override SelectionRules SelectionRules
        {
            get { return base.SelectionRules & ~(SelectionRules.BottomSizeable | SelectionRules.TopSizeable); }
        }

        private string Text
        {
            get { return Control.Text; }
            set
            {
                Control.Text = value;
                (Control).Select();
            }
        }

        public override void InitializeNewComponent(IDictionary defaultValues)
        {
            base.InitializeNewComponent(defaultValues);
            PropertyDescriptor textProperty = TypeDescriptor.GetProperties(base.Component)["Text"];
            if ((textProperty != null && textProperty.PropertyType == typeof (string)) &&
                (!textProperty.IsReadOnly && textProperty.IsBrowsable))
                textProperty.SetValue(base.Component, String.Empty);

            PropertyDescriptor cursorProperty = TypeDescriptor.GetProperties(base.Component)["Cursor"];
            if (cursorProperty != null && cursorProperty.PropertyType == typeof (Cursor))
                cursorProperty.SetValue(base.Component, Cursors.IBeam);

            PropertyDescriptor borderStyleProperty = TypeDescriptor.GetProperties(base.Component)["BorderStyle"];
            if (borderStyleProperty != null && borderStyleProperty.PropertyType == typeof (BorderStyle))
                borderStyleProperty.SetValue(base.Component, BorderStyle.FixedSingle);
        }

        protected override void PreFilterProperties(IDictionary properties)
        {
            base.PreFilterProperties(properties);
            string[] textArray = new[] {"Text"};
            Attribute[] attributes = new Attribute[0];
            for (int i = 0; i < textArray.Length; i++)
            {
                PropertyDescriptor oldPropertyDescriptor = (PropertyDescriptor) properties[textArray[i]];
                if (oldPropertyDescriptor != null)
                {
                    properties[textArray[i]] = TypeDescriptor.CreateProperty(typeof (SearchTextBoxDesigner),
                                                                             oldPropertyDescriptor, attributes);
                }
            }
        }

        private void ResetText()
        {
            Control.Text = String.Empty;
        }

        private bool ShouldSerializeText()
        {
            return TypeDescriptor.GetProperties(typeof (SearchTextBox))["Text"].ShouldSerializeValue(base.Component);
        }

        // Properties
    }
}