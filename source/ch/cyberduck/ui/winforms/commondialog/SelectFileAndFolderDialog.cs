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
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;
using System.Windows.Forms;
using Microsoft.Win32;

namespace Ch.Cyberduck.Ui.Winforms.Commondialog
{
    public class SelectFileAndFolderDialog : CommonDialog
    {
        private const string HideFileExtensionKey =
            @"HideFileExt";

        private const string HideFileExtensionLocation =
            @"Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced";

        private static readonly Dictionary<int, CalcPosDelegate> m_calcPosMap =
            new Dictionary<int, CalcPosDelegate>
                {
                    {
                        InteropUtil.ID_CUSTOM_CANCEL,
                        (SelectFileAndFolderDialog @this, int baseRight, out int right, out int width) =>
                            {
                                right = baseRight;
                                width = @this.m_cancelWidth;
                            }
                        },
                    {
                        InteropUtil.ID_SELECT,
                        (SelectFileAndFolderDialog @this, int baseRight, out int right, out int width) =>
                            {
                                right = baseRight - (@this.m_cancelWidth + @this.m_buttonGap);
                                width = @this.m_selectWidth;
                            }
                        }
                };

        private readonly InteropUtil.SUBCLASSPROC m_defViewSubClassDelegate;
        private readonly InteropUtil.WndProc m_hookDelegate;
        private readonly InteropUtil.SUBCLASSPROC m_openFileSubClassDelegate;
        private List<string> _selected = new List<string>();

        private int m_buttonGap;
        private int m_cancelWidth;
        private string m_currentFolder;
        private IntPtr m_hWnd;
        private bool m_hasDirChangeFired;
        private int m_selectWidth;
        private bool m_suppressSelectionChange;
        private bool m_useCurrentDir;

        public SelectFileAndFolderDialog()
        {
            m_openFileSubClassDelegate = OpenFileSubClass;
            m_hookDelegate = HookProc;
            m_defViewSubClassDelegate = DefViewSubClass;
            Path = null;
            Title = null;
            m_useCurrentDir = false;
            AcceptFiles = true;
            m_currentFolder = null;
            m_useCurrentDir = false;
            m_cancelWidth = 0;
            m_selectWidth = 0;
            m_buttonGap = 0;
            m_hWnd = IntPtr.Zero;
        }

        public string Path { get; set; }

        public string[] SelectedPaths
        {
            get { return _selected.ToArray(); }
        }

        public string Title { get; set; }
        public bool ShowReadOnly { get; set; }
        public bool AcceptFiles { get; set; }
        public string FileNameLabel { get; set; }
        public string SelectLabel { get; set; }
        public string CancelLabel { get; set; }

        public override void Reset()
        {
            Path = null;
            Title = null;
            m_useCurrentDir = false;
            AcceptFiles = true;
            m_currentFolder = null;
            m_useCurrentDir = false;
            m_cancelWidth = 0;
            m_selectWidth = 0;
            m_buttonGap = 0;
            m_hWnd = IntPtr.Zero;
        }

        protected override bool RunDialog(IntPtr hwndOwner)
        {
            InteropUtil.Assume(Marshal.SystemDefaultCharSize == 2, "The character size should be 2");

            var nativeBuffer = Marshal.AllocCoTaskMem(InteropUtil.NumberOfFileChars*2);
            IntPtr filterBuffer = IntPtr.Zero;
            _selected = new List<string>();

            try
            {
                var openFileName = new InteropUtil.OpenFileName();
                openFileName.Initialize();
                openFileName.hwndOwner = hwndOwner;

                var chars = new char[InteropUtil.NumberOfFileChars];

                try
                {
                    if (File.Exists(Path))
                    {
                        if (AcceptFiles)
                        {
                            var fileName = System.IO.Path.GetFileName(Path);
                            var length = Math.Min(fileName.Length, InteropUtil.NumberOfFileChars);
                            fileName.CopyTo(0, chars, 0, length);
                            openFileName.lpstrInitialDir = System.IO.Path.GetDirectoryName(Path);
                        }
                        else
                        {
                            openFileName.lpstrInitialDir = System.IO.Path.GetDirectoryName(Path);
                        }
                    }
                    else if (Directory.Exists(Path))
                    {
                        openFileName.lpstrInitialDir = Path;
                    }
                    else
                    {
                        //the path does not exist.
                        //We don't just want to throw it away, however.
                        //The initial path we get is most likely provided by the user in some way.
                        //It could be what they typed into a text box before clicking a browse button,
                        //or it could be a value they had entered previously that used to be valid, but now no longer exists.
                        //In any case, we don't want to throw out the user's text. So, we find the first parent 
                        //directory of Path that exists on disk.
                        //We will set the initial directory to that path, and then set the initial file to
                        //the rest of the path. The user will get an error if the click "OK"m saying that the selected path
                        //doesn't exist, but that's ok. If we didn't do this, and showed the path, if they clicked
                        //OK without making changes we would actually change the selected path, which would be bad.
                        //This way, if the users want's to change the folder, he actually has to change something.
                        string pathToShow;
                        InitializePathDNE(Path, out openFileName.lpstrInitialDir, out pathToShow);
                        pathToShow = pathToShow ?? "";
                        var length = Math.Min(pathToShow.Length, InteropUtil.NumberOfFileChars);
                        pathToShow.CopyTo(0, chars, 0, length);
                    }
                }
                catch
                {
                }

                Marshal.Copy(chars, 0, nativeBuffer, chars.Length);

                openFileName.lpstrFile = nativeBuffer;

                if (!AcceptFiles)
                {
                    var str = string.Format("Folders\0*.{0}-{1}\0\0", Guid.NewGuid().ToString("N"),
                                            Guid.NewGuid().ToString("N"));
                    filterBuffer = openFileName.lpstrFilter = Marshal.StringToCoTaskMemUni(str);
                }
                else
                {
                    openFileName.lpstrFilter = IntPtr.Zero;
                }

                openFileName.nMaxCustFilter = 0;
                openFileName.nFilterIndex = 0;
                openFileName.nMaxFile = InteropUtil.NumberOfFileChars;
                openFileName.nMaxFileTitle = 0;
                openFileName.lpstrTitle = Title;
                openFileName.lpfnHook = m_hookDelegate;
                openFileName.templateID = InteropUtil.IDD_CustomOpenDialog;
                openFileName.hInstance = Marshal.GetHINSTANCE(typeof (SelectFileAndFolderDialog).Module);
                openFileName.Flags =
                    InteropUtil.OFN_DONTADDTORECENT |
                    InteropUtil.OFN_ENABLEHOOK |
                    InteropUtil.OFN_ENABLESIZING |
                    InteropUtil.OFN_NOTESTFILECREATE |
                    InteropUtil.OFN_ALLOWMULTISELECT |
                    InteropUtil.OFN_EXPLORER |
                    InteropUtil.OFN_FILEMUSTEXIST |
                    InteropUtil.OFN_PATHMUSTEXIST |
                    InteropUtil.OFN_NODEREFERENCELINKS |
                    InteropUtil.OFN_ENABLETEMPLATE |
                    (ShowReadOnly ? 0 : InteropUtil.OFN_HIDEREADONLY);

                m_useCurrentDir = false;
                bool hideFileExtSettingChanged = false;

                try
                {
                    if (GetHideFileExtensionSetting())
                    {
                        HideFileExtension(false);
                        hideFileExtSettingChanged = true;
                    }
                    var ret = InteropUtil.GetOpenFileNameW(ref openFileName);
                    //var extErrpr = InteropUtil.CommDlgExtendedError();
                    //InteropUtil.CheckForWin32Error();

                    if (m_useCurrentDir)
                    {
                        Path = m_currentFolder;
                        return true;
                    }
                    else if (ret)
                    {
                        Marshal.Copy(nativeBuffer, chars, 0, chars.Length);
                        var firstZeroTerm = ((IList) chars).IndexOf('\0');
                        if (firstZeroTerm >= 0 && firstZeroTerm <= chars.Length - 1)
                        {
                            Path = new string(chars, 0, firstZeroTerm);
                        }
                    }
                    return ret;
                }
                finally
                {
                    //revert registry setting
                    if (hideFileExtSettingChanged)
                    {
                        HideFileExtension(true);
                    }
                }
            }
            finally
            {
                Marshal.FreeCoTaskMem(nativeBuffer);
                if (filterBuffer != IntPtr.Zero)
                {
                    Marshal.FreeCoTaskMem(filterBuffer);
                }
            }
        }

        private static bool GetHideFileExtensionSetting()
        {
            using (RegistryKey adv = Registry.CurrentUser.OpenSubKey(HideFileExtensionLocation))
            {
                if (null != adv)
                {
                    var hide = adv.GetValue(HideFileExtensionKey);
                    if (null != hide)
                    {
                        return Convert.ToBoolean(hide);
                    }
                }
            }
            return false;
        }

        private static void HideFileExtension(bool hide)
        {
            using (RegistryKey adv = Registry.CurrentUser.OpenSubKey(HideFileExtensionLocation, true))
            {
                if (null != adv)
                {
                    adv.SetValue(HideFileExtensionKey, Convert.ToInt16(hide), RegistryValueKind.DWord);
                }
            }
        }

        private static void InitializePathDNE(string path, out string existingParent, out string initialFileNameText)
        {
            var stack = new Stack<string>();
            existingParent = System.IO.Path.GetDirectoryName(path);
            stack.Push(System.IO.Path.GetFileName(path));

            while (!string.IsNullOrEmpty(existingParent) && !Directory.Exists(existingParent))
            {
                stack.Push(existingParent);
                existingParent = System.IO.Path.GetDirectoryName(existingParent);
            }

            var builder = new StringBuilder();
            bool first = true;
            while (stack.Count > 0)
            {
                if (!first)
                {
                    builder.Append(System.IO.Path.PathSeparator);
                }
                else
                {
                    first = false;
                }
                builder.Append(stack.Pop());
            }
            initialFileNameText = builder.ToString();
        }

        /// <summary>
        /// Defines the common dialog box hook procedure that is overridden to add specific functionality to a common dialog box.
        /// </summary>
        /// <returns>
        /// A zero value if the default dialog box procedure processes the message; a nonzero value if the default dialog box procedure ignores the message.
        /// </returns>
        /// <param name="hWnd">The handle to the dialog box window. </param><param name="msg">The message being received. </param><param name="wparam">Additional information about the message. </param><param name="lparam">Additional information about the message. </param>
        protected override IntPtr HookProc(IntPtr hWnd, int msg, IntPtr wParam, IntPtr lparam)
        {
            switch (unchecked((uint) msg))
            {
                case InteropUtil.WM_INITDIALOG:
                    {
                        InitDialog(hWnd);
                        break;
                    }
                case InteropUtil.WM_NOTIFY:
                    {
                        var notifyData =
                            (InteropUtil.OFNOTIFY) Marshal.PtrToStructure(lparam, typeof (InteropUtil.OFNOTIFY));
                        var results = ProcessNotifyMessage(hWnd, notifyData);
                        if (results != 0)
                        {
                            InteropUtil.SetWindowLongW(hWnd, InteropUtil.DWL_MSGRESULT, results);
                            return (IntPtr) results;
                        }
                        break;
                    }
                case InteropUtil.WM_SIZE:
                    {
                        ResizeCustomControl(hWnd);
                        break;
                    }

                case (InteropUtil.BN_CLICKED << 16) | InteropUtil.IDOK:
                    {
                        break;
                    }

                case InteropUtil.WM_COMMAND:
                    {
                        unchecked
                        {
                            var hParent = InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd));
                            var code = HIGH((uint) wParam);
                            var id = LOW((uint) wParam);
                            if (code == InteropUtil.BN_CLICKED)
                            {
                                switch (id)
                                {
                                    case InteropUtil.ID_CUSTOM_CANCEL:
                                        {
                                            //The user clicked our custom cancel button. Close the dialog.
                                            InteropUtil.SendMessage(hParent, InteropUtil.WM_CLOSE, 0, 0);
                                            break;
                                        }
                                    case InteropUtil.ID_SELECT:
                                        {
                                            if (ProcessSelection(hParent, true))
                                            {
                                                InteropUtil.SendMessage(hParent, InteropUtil.WM_CLOSE, 0, 0);
                                                break;
                                            }
                                            //The user has not selected an existing folder.
                                            //So we translate a click of our "Select" button into the OK button and forward the request to the
                                            //open file dialog.
                                            InteropUtil.SendMessage
                                                (hParent,
                                                 InteropUtil.WM_COMMAND,
                                                 (InteropUtil.BN_CLICKED << 16) | InteropUtil.IDOK,
                                                 unchecked((uint) InteropUtil.GetDlgItem(hParent, InteropUtil.IDOK))
                                                );


                                            break;
                                        }
                                }
                            }
                        }
                        break;
                    }
            }
            return base.HookProc(hWnd, msg, wParam, lparam);
        }

        private bool AddSelectedItemsFromSelection(IntPtr listview)
        {
            uint count = InteropUtil.SendMessage(listview,
                                                 InteropUtil.LVM_GETSELECTEDCOUNT, 0, 0);

            //if there is at least one item selected we always return these ones
            if (count > 0)
            {
                int ind = -1;
                for (int i = 0; i < count; i++)
                {
                    ind = InteropUtil.SendMessageInt(listview, InteropUtil.LVM_GETNEXTITEM,
                                                     ind,
                                                     InteropUtil.LVNI_SELECTED);
                    //with the index we can get the item's text
                    string item = GetFileNameFromSelectedItem(listview, ind);
                    _selected.Add(item);
                }
                return true;
            }
            return false;
        }

        private bool ProcessSelection(IntPtr handle, bool preferSelection)
        {
            var shelldll_defview = InteropUtil.GetDlgItem(handle,
                                                          InteropUtil.ID_FileList);
            var listview = InteropUtil.GetDlgItem(shelldll_defview, 1);
            IntPtr focus = InteropUtil.GetFocus();
            if (listview == focus || preferSelection)
            {
                return AddSelectedItemsFromSelection(listview);
            }
            else
            {
                //check the content of the file combobox
                var hFileName = InteropUtil.GetDlgItem(handle,
                                                       InteropUtil.ID_FileNameCombo);
                var currentText = (InteropUtil.GetWindowTextW(hFileName) ?? "").Trim();
                if (!String.IsNullOrEmpty(currentText))
                {
                    try
                    {
                        if (System.IO.Path.IsPathRooted(currentText))
                        {
                            if (Directory.Exists(currentText) || File.Exists(currentText))
                            {
                                //the contents of the text box are a rooted path, that points to an existing directory.
                                //we interpret that to mean that the user wants to select that directory.
                                _selected.Add(currentText);
                                return true;
                            }
                        }
                        else if (!String.IsNullOrEmpty(m_currentFolder))
                        {
                            var combined = System.IO.Path.Combine(m_currentFolder,
                                                                  currentText);
                            if (Directory.Exists(combined) || File.Exists(combined))
                            {
                                //the contents of the text box are a relative path, that points to a 
                                //an existing directory. We interpret the users intent to mean that they wanted
                                //to select the existing path.
                                _selected.Add(combined);
                                return true;
                            }
                        }
                    }
                    catch (Exception)
                    {
                        //try to add the selection
                        if (AddSelectedItemsFromSelection(listview))
                        {
                            return true;
                        }
                    }
                }
                //forward all wrong inputs to the standard mechanism
                return false;
            }
        }

        private void InitDialog(IntPtr hWnd)
        {
            m_hWnd = hWnd;

            var hParent = InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd));
            InteropUtil.SetWindowSubclass(hParent, m_openFileSubClassDelegate, 0, 0);

            //disable and hide the filter combo box
            var hFilterCombo = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FilterCombo));
            InteropUtil.EnableWindow(hFilterCombo, false);
            InteropUtil.SendMessage(hParent, InteropUtil.CDM_HIDECONTROL, InteropUtil.ID_FilterCombo, 0);
            InteropUtil.SendMessage(hParent, InteropUtil.CDM_HIDECONTROL, InteropUtil.ID_FilterLabel, 0);

            //update the file name label
            var hFileNameLabel = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FileNameLabel));

            if (FileNameLabel != "")
            {
                InteropUtil.SendMessageString(hFileNameLabel, InteropUtil.WM_SETTEXT, 0, FileNameLabel);
            }

            //find the button controls in the parent
            var hOkButton = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.IDOK));
            var hCancelButton = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.IDCANCEL));

            //We don't want the accelerator keys for the ok and cancel buttons to work, because
            //they are not shown on the dialog. However, we still want the buttons enabled
            //so that "esc" and "enter" have the behavior they used to. So, we just
            //clear out their text instead.
            InteropUtil.SetWindowTextW(hOkButton, "");
            InteropUtil.SetWindowTextW(hCancelButton, "");

            //find our button controls            
            var hSelectButton = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hWnd, InteropUtil.ID_SELECT));
            var hCustomCancelButton =
                InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hWnd, InteropUtil.ID_CUSTOM_CANCEL));

            if (!String.IsNullOrEmpty(SelectLabel))
            {
                InteropUtil.SetWindowTextW(hSelectButton, SelectLabel);
            }
            if (!String.IsNullOrEmpty(CancelLabel))
            {
                InteropUtil.SetWindowTextW(hCustomCancelButton, CancelLabel);
            }

            //copy the font from the parent's buttons
            InteropUtil.LoadFontFrom(hSelectButton, hOkButton);
            InteropUtil.LoadFontFrom(hCustomCancelButton, hCancelButton);

            var cancelLoc = InteropUtil.GetWindowPlacement(hCancelButton);

            //hide the ok and cancel buttons
            InteropUtil.SendMessage(hParent, InteropUtil.CDM_HIDECONTROL, InteropUtil.IDOK, 0);
            InteropUtil.SendMessage(hParent, InteropUtil.CDM_HIDECONTROL, InteropUtil.IDCANCEL, 0);

            //expand the file name combo to take up the space left by the OK and cancel buttons.
            var hFileName = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FileNameCombo));
            var fileNameLoc = InteropUtil.GetWindowPlacement(hFileName);
            fileNameLoc.Right = InteropUtil.GetWindowPlacement(hOkButton).Right;
            InteropUtil.SetWindowPlacement(hFileName, ref fileNameLoc);

            var parentLoc = InteropUtil.GetWindowPlacement(hParent);

            //subtract the height of the missing cancel button
            parentLoc.Bottom -= (cancelLoc.Bottom - cancelLoc.Top);
            InteropUtil.SetWindowPlacement(hParent, ref parentLoc);

            //move the select and custom cancel buttons to the right hand side of the window:

            var selectLoc = InteropUtil.GetWindowPlacement(hSelectButton);
            var customCancelLoc = InteropUtil.GetWindowPlacement(hCustomCancelButton);
            m_cancelWidth = customCancelLoc.Right - customCancelLoc.Left;
            m_selectWidth = selectLoc.Right - selectLoc.Left;
            m_buttonGap = customCancelLoc.Left - selectLoc.Right;

            var ctrlLoc = InteropUtil.GetWindowPlacement(hWnd);
            ctrlLoc.Right = fileNameLoc.Right;

            //ResizeCustomControl(hWnd, fileNameLoc.Right, hCustomCancelButton, hSelectButton);
            ResizeCustomControl(hWnd);
        }

        private void ResizeCustomControl(IntPtr hWnd)
        {
            if (hWnd == m_hWnd)
            {
                var hSelectButton =
                    InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(InteropUtil.AssumeNonZero(hWnd),
                                                                     InteropUtil.ID_SELECT));
                var hOkButton =
                    InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(InteropUtil.AssumeNonZero(hWnd),
                                                                     InteropUtil.ID_CUSTOM_CANCEL));

                var hParent = InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd));
                var fileName = InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FileNameCombo));

                /*var right = fileName.GetWindowPlacement().Right;
                var top = hSelectButton.GetWindowPlacement().Top;*/

                var rect = new InteropUtil.RECT();
                var selectRect = InteropUtil.GetWindowPlacement(hSelectButton);

                rect.top = selectRect.Top;
                rect.bottom = selectRect.Bottom;
                rect.right = InteropUtil.GetWindowPlacement(fileName).Right;
                rect.left = rect.right - (m_cancelWidth + m_buttonGap + m_selectWidth);

                ResizeCustomControl(hWnd, rect, hOkButton, hSelectButton);
            }
        }

        private void ResizeCustomControl(IntPtr hWnd, InteropUtil.RECT rect, params IntPtr[] buttons)
        {
            InteropUtil.Assume(buttons != null && buttons.Length > 0);

            InteropUtil.AssumeNonZero(hWnd);

            var wndLoc = InteropUtil.GetWindowPlacement(hWnd);

            wndLoc.Right = rect.right;
            InteropUtil.SetWindowPlacement(hWnd, ref wndLoc);

            foreach (var hBtn in buttons)
            {
                int btnRight, btnWidth;

                m_calcPosMap[InteropUtil.GetDlgCtrlID(hBtn)](this, rect.right, out btnRight, out btnWidth);

                PositionButton(hBtn, btnRight, btnWidth);
            }

            //see bug # 844
            //We clip hWnd to only draw in the rectangle around our custom buttons.
            //When we supply a custom dialog template to GetOpenFileName(), it adds 
            //an extra HWND to the open file dialog, and then sticks all the controls 
            //in the dialog //template inside the HWND. It then resizes the control 
            //to stretch from the top of the open file dialog to the bottom of the 
            //window, extending the bottom of the window large enough to include the 
            //additional height of the dialog template. This ends up sticking our custom
            //buttons at the bottom of the window, which is what we want.
            //
            //However, the fact that the parent window extends from the top of the open 
            //file dialog was causing some painting problems on Windows XP SP 3 systems. 
            //Basically, because the window was covering the predefined controls on the 
            //open file dialog, they were not getting painted. This results in a blank 
            //window. I tried setting an extended WS_EX_TRANSPARENT style on the dialog, 
            //but that didn't help.
            //
            //So, to fix the problem I setup a window region for the synthetic HWND. 
            //This clips the drawing of the window to only within the region containing
            //the custom buttons, and thus avoids the problem.
            //
            //I'm not sure why this wasn't an issue on Vista. 
            var hRgn = InteropUtil.CreateRectRgnIndirect(ref rect);
            try
            {
                if (InteropUtil.SetWindowRgn(hWnd, hRgn, true) == 0)
                {
                    //setting the region failed, so we need to delete the region we created above.
                    InteropUtil.DeleteObject(hRgn);
                }
            }
            catch
            {
                if (hRgn != IntPtr.Zero)
                {
                    InteropUtil.DeleteObject(hRgn);
                }
            }
        }

        private void PositionButton(IntPtr hWnd, int right, int width)
        {
            InteropUtil.AssumeNonZero(hWnd);
            var id = InteropUtil.GetDlgCtrlID(hWnd);

            //hWnd.BringWindowToTop();

            var buttonLoc = InteropUtil.GetWindowPlacement(hWnd);

            buttonLoc.Right = right;
            buttonLoc.Left = buttonLoc.Right - width;
            InteropUtil.SetWindowPlacement(hWnd, ref buttonLoc);
            InteropUtil.InvalidateRect(hWnd, IntPtr.Zero, true);
        }

        private int ProcessNotifyMessage(IntPtr hWnd, InteropUtil.OFNOTIFY notifyData)
        {
            switch (notifyData.hdr_code)
            {
                case InteropUtil.CDN_FOLDERCHANGE:
                    {
                        //CDM_GETFOLDERPATH returns garbage for some standard folders like 'Libraries'
                        //var newFolder = GetTextFromCommonDialog(InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd)),
                        //                  InteropUtil.CDM_GETFOLDERPATH);
                        var newFolder = GetTextFromCommonDialog(InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd)),
                                                                InteropUtil.CDM_GETFILEPATH);
                        /*
                        if (m_currentFolder != null && newFolder != null &&
                            Util.PathContains(newFolder, m_currentFolder))
                        {
                            m_suppressSelectionChange = true;
                        }
                        */

                        m_currentFolder = newFolder;
                        var fileNameCombo =
                            InteropUtil.AssumeNonZero(
                                InteropUtil.GetDlgItem(InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd)),
                                                       InteropUtil.ID_FileNameCombo));
                        if (m_hasDirChangeFired)
                        {
                            InteropUtil.SetWindowTextW(fileNameCombo, "");
                        }
                        m_hasDirChangeFired = true;

                        //refresh the file list to make sure that the extension is shown properly
                        var hParent = InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd));
                        SetForegroundWindow(hParent);
                        SendKeys.SendWait("{F5}");

                        break;
                    }
                case InteropUtil.CDN_FILEOK:
                    {
                        if (!AcceptFiles)
                        {
                            return 1;
                        }

                        var hParent = InteropUtil.AssumeNonZero(InteropUtil.GetParent(hWnd));
                        ProcessSelection(hParent, false);

                        break;
                    }
                case InteropUtil.CDN_INITDONE:
                    {
                        var hParent = InteropUtil.GetParent(hWnd);
                        var hFile =
                            InteropUtil.AssumeNonZero(InteropUtil.GetDlgItem(InteropUtil.AssumeNonZero(hParent),
                                                                             InteropUtil.ID_FileNameCombo));
                        InteropUtil.SetFocus(hFile);
                        break;
                    }
            }
            return 0;
        }

        private string GetTextFromCommonDialog(IntPtr hWnd, uint msg)
        {
            string str = null;
            var buffer = Marshal.AllocCoTaskMem(2*InteropUtil.NumberOfFileChars);
            try
            {
                InteropUtil.SendMessage(hWnd, msg, InteropUtil.NumberOfFileChars, unchecked((uint) buffer));
                var chars = new char[InteropUtil.NumberOfFileChars];
                Marshal.Copy(buffer, chars, 0, chars.Length);
                var firstZeroTerm = ((IList) chars).IndexOf('\0');

                if (firstZeroTerm >= 0 && firstZeroTerm <= chars.Length - 1)
                {
                    str = new string(chars, 0, firstZeroTerm);
                }
            }
            finally
            {
                Marshal.FreeCoTaskMem(buffer);
            }
            return str;
        }

        private int DefViewSubClass
            (
            IntPtr hWnd,
            uint uMsg,
            IntPtr wParam,
            IntPtr lParam,
            IntPtr uIdSubclass,
            uint dwRefData
            )
        {
            if (uMsg == InteropUtil.WM_NOTIFY)
            {
                var header = (InteropUtil.NMHDR) Marshal.PtrToStructure(lParam, typeof (InteropUtil.NMHDR));
                if (header.code == InteropUtil.LVN_ITEMCHANGED && header.hwndFrom != IntPtr.Zero && header.idFrom == 1)
                {
                    var nmListView =
                        (InteropUtil.NMLISTVIEW) Marshal.PtrToStructure(lParam, typeof (InteropUtil.NMLISTVIEW));
                    var oldSelected = (nmListView.uOldState & InteropUtil.LVIS_SELECTED) != 0;
                    var newSelected = (nmListView.uNewState & InteropUtil.LVIS_SELECTED) != 0;
                    if (!oldSelected && newSelected)
                    {
                        if (!m_suppressSelectionChange)
                        {
                            //the item went from not selected to being selected    
                            //so we want to look and see if the selected item is a folder, and if so
                            //change the text of the item box to be the item on the folder. But, before we do that
                            //we want to make sure that the box isn't currently focused.
                            var hParent = InteropUtil.GetParent(hWnd);
                            var hFNCombo = InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FileNameCombo);
                            var hFNEditBox = InteropUtil.GetDlgItem(hParent, InteropUtil.ID_FileNameTextBox);
                            var hFocus = InteropUtil.GetFocus();

                            if
                                (
                                (hFNCombo == IntPtr.Zero || hFNCombo != hFocus) &&
                                (hFNEditBox == IntPtr.Zero || hFNEditBox != hFocus)
                                )
                            {
                                SetFileNameToSelectedItem(header.hwndFrom, hFNCombo, nmListView.iItem);
                            }
                        }
                        m_suppressSelectionChange = false;
                    }
                }
            }
            return InteropUtil.DefSubclassProc(hWnd, uMsg, wParam, lParam);
        }

        private void SetFileNameToSelectedItem(IntPtr hListView, IntPtr hFNCombo, int selectedIndex)
        {
            if (selectedIndex >= 0)
            {
                var lvitem = new InteropUtil.LVITEM();
                lvitem.mask = InteropUtil.LVIF_TEXT;
                var nativeBuffer = Marshal.AllocCoTaskMem(InteropUtil.NumberOfFileChars*2);
                for (int i = 0; i < InteropUtil.NumberOfFileChars; ++i)
                {
                    Marshal.WriteInt16(nativeBuffer, i*2, '\0');
                }
                string name;

                try
                {
                    Marshal.WriteInt16(nativeBuffer, 0);
                    lvitem.pszText = nativeBuffer;
                    lvitem.cchTextMax = InteropUtil.NumberOfFileChars;
                    var length = InteropUtil.SendListViewMessage(hListView, InteropUtil.LVM_GETITEMTEXT,
                                                                 (uint) selectedIndex, ref lvitem);
                    name = Marshal.PtrToStringUni(lvitem.pszText, (int) length);
                }
                finally
                {
                    Marshal.FreeCoTaskMem(nativeBuffer);
                }

                if (name != null && m_currentFolder != null)
                {
                    try
                    {
                        var path = System.IO.Path.Combine(m_currentFolder, name);
                        if (Directory.Exists(path))
                        {
                            InteropUtil.SetWindowTextW(hFNCombo, name);
                        }
                    }
                    catch (Exception)
                    {
                    }
                }
            }
        }

        private string GetFileNameFromSelectedItem(IntPtr hListView, int selectedIndex)
        {
            if (selectedIndex >= 0)
            {
                var lvitem = new InteropUtil.LVITEM();
                lvitem.mask = InteropUtil.LVIF_TEXT;
                var nativeBuffer = Marshal.AllocCoTaskMem(InteropUtil.NumberOfFileChars*2);
                for (int i = 0; i < InteropUtil.NumberOfFileChars; ++i)
                {
                    Marshal.WriteInt16(nativeBuffer, i*2, '\0');
                }
                string name;

                try
                {
                    Marshal.WriteInt16(nativeBuffer, 0);
                    lvitem.pszText = nativeBuffer;
                    lvitem.cchTextMax = InteropUtil.NumberOfFileChars;
                    var length = InteropUtil.SendListViewMessageInt(hListView, InteropUtil.LVM_GETITEMTEXT,
                                                                    selectedIndex, ref lvitem);
                    name = Marshal.PtrToStringUni(lvitem.pszText, (int) length);
                }
                finally
                {
                    Marshal.FreeCoTaskMem(nativeBuffer);
                }

                if (name != null && m_currentFolder != null)
                {
                    try
                    {
                        var path = System.IO.Path.Combine(m_currentFolder, name);

                        return path;
                        if (Directory.Exists(path))
                        {
                            //hFNCombo.SetWindowTextW(name);
                        }
                    }
                    catch (Exception)
                    {
                    }
                }


                return name;
            }
            return string.Empty;
        }

        private int OpenFileSubClass
            (
            IntPtr hWnd,
            uint uMsg,
            IntPtr wParam,
            IntPtr lParam,
            IntPtr uIdSubclass,
            uint dwRefData
            )
        {
            switch (uMsg)
            {
                case InteropUtil.WM_PARENTNOTIFY:
                    {
                        unchecked
                        {
                            var id = InteropUtil.GetDlgCtrlID(lParam);

                            if (LOW((uint) wParam) == InteropUtil.WM_CREATE &&
                                (id == InteropUtil.ID_FileList || id == 0))
                            {
                                InteropUtil.SetWindowSubclass(lParam, m_defViewSubClassDelegate, 0, 0);
                            }
                        }
                        break;
                    }
            }
            return InteropUtil.DefSubclassProc(hWnd, uMsg, wParam, lParam);
        }

        private static uint LOW(uint x)
        {
            return x & 0xFFFF;
        }

        private static uint HIGH(uint x)
        {
            return (x & 0xFFFF0000) >> 16;
        }

        [DllImport("User32.dll", SetLastError = true)]
        public static extern int SetForegroundWindow(IntPtr hwnd);

        private delegate void CalcPosDelegate(
            SelectFileAndFolderDialog @this, int baseRight, out int right, out int width);
    }
}