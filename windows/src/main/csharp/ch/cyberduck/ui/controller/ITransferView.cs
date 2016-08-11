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

using System.Collections.Generic;
using System.Drawing;
using Ch.Cyberduck.Ui.Core;

namespace Ch.Cyberduck.Ui.Controller
{
    public interface ITransferView : IView
    {
        string Url { set; }
        string Local { set; }
        Image FileIcon { set; }
        float Bandwidth { set; get; }
        bool BandwidthEnabled { set; }
        int QueueSize { set; get; }
        bool TranscriptVisible { get; set; }
        int TranscriptHeight { get; set; }
        IList<IProgressView> SelectedTransfers { get; }
        void SelectTransfer(IProgressView view);
        void AddTransfer(IProgressView view);
        void RemoveTransfer(IProgressView view);
        void AddTranscriptEntry(Type request, string entry);
        event VoidHandler ResumeEvent;
        event ValidateCommand ValidateResumeEvent;
        event VoidHandler ReloadEvent;
        event ValidateCommand ValidateReloadEvent;
        event VoidHandler StopEvent;
        event ValidateCommand ValidateStopEvent;
        event VoidHandler RemoveEvent;
        event ValidateCommand ValidateRemoveEvent;
        event VoidHandler CleanEvent;
        event ValidateCommand ValidateCleanEvent;
        event VoidHandler OpenEvent;
        event ValidateCommand ValidateOpenEvent;
        event VoidHandler ShowEvent;
        event ValidateCommand ValidateShowEvent;
        event VoidHandler ToggleTranscriptEvent;
        event VoidHandler TrashEvent;
        event VoidHandler TranscriptHeightChangedEvent;
        event VoidHandler SelectionChangedEvent;
        event VoidHandler BandwidthChangedEvent;
        event VoidHandler QueueSizeChangedEvent;
        void PopulateBandwidthList(IList<KeyValuePair<float, string>> throttles);
        void TaskbarOverlayIcon(Icon icon, string text);
        void UpdateOverallProgressState(long progress, long maximum);
    }
}