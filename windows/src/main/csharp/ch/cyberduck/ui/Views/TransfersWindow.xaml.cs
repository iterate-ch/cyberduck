// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using ch.cyberduck.ui.ViewModels;
using CommunityToolkit.Mvvm.Messaging;
using System;
using System.Windows;

namespace ch.cyberduck.ui.Views
{
    public partial class TransfersWindow : Window
    {
        public TransfersWindow()
        {
            InitializeComponent();
            WeakReferenceMessenger.Default.Register<TransfersViewModel.BringIntoViewMessage>(this, OnBringIntoView);
        }

        private void OnBringIntoView(object recipient, TransfersViewModel.BringIntoViewMessage message)
        {
            transferList.ScrollIntoView(message.Value);
        }

        protected override Size MeasureOverride(Size availableSize)
        {
            ToolBar.Measure(new(double.PositiveInfinity, double.PositiveInfinity));
            var desired = ToolBar.DesiredSize;
            MinWidth = Math.Ceiling(desired.Width) + 16;
            if (Width < MinWidth)
            {
                Width = MinWidth;
            }

            return base.MeasureOverride(availableSize with { Width = Width });
        }
    }
}
