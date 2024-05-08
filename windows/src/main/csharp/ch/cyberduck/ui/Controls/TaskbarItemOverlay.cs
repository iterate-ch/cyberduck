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

using System.Windows;
using System.Windows.Controls;
using System.Windows.Markup;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shell;

namespace ch.cyberduck.ui.Controls;

public class TaskbarItemOverlay
{
    public static readonly DependencyProperty ContentProperty =
        DependencyProperty.RegisterAttached("Content", typeof(object), typeof(TaskbarItemOverlay), new PropertyMetadata(OnPropertyChanged));

    public static readonly DependencyProperty TemplateProperty =
        DependencyProperty.RegisterAttached("Template", typeof(DataTemplate), typeof(TaskbarItemOverlay), new PropertyMetadata(OnPropertyChanged));

    public static object GetContent(DependencyObject dependencyObject)
    {
        return dependencyObject.GetValue(ContentProperty);
    }

    public static void SetContent(DependencyObject dependencyObject, object content)
    {
        dependencyObject.SetValue(ContentProperty, content);
    }

    public static DataTemplate GetTemplate(DependencyObject dependencyObject)
    {
        return (DataTemplate)dependencyObject.GetValue(TemplateProperty);
    }

    public static void SetTemplate(DependencyObject dependencyObject, DataTemplate template)
    {
        dependencyObject.SetValue(TemplateProperty, template);
    }

    private static void OnPropertyChanged(DependencyObject dependencyObject, DependencyPropertyChangedEventArgs e)
    {
        var taskbarItemInfo = (TaskbarItemInfo)dependencyObject;
        var content = GetContent(taskbarItemInfo);
        var template = GetTemplate(taskbarItemInfo);

        if (template == null || content == null)
        {
            taskbarItemInfo.Overlay = null;
            return;
        }

        const int ICON_WIDTH = 16;
        const int ICON_HEIGHT = 16;

        RenderTargetBitmap bmp = new(ICON_WIDTH, ICON_HEIGHT, 96, 96, PixelFormats.Default);
        ContentPresenter presenter = new()
        {
            ContentTemplate = template,
            Content = content,
        };
        presenter.Arrange(new Rect(0, 0, ICON_WIDTH, ICON_HEIGHT));
        bmp.Render(presenter);
        bmp.Freeze();

        taskbarItemInfo.Overlay = bmp;
    }
}
