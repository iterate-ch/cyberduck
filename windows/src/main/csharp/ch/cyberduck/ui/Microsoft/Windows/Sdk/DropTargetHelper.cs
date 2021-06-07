using Ch.Cyberduck.Ui.Microsoft.Windows.Sdk;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.CLSCTX;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.Constants;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.DROPIMAGETYPE;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.DVASPECT;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.PInvoke;
using static Ch.Cyberduck.Ui.Microsoft.Windows.Sdk.TYMED;

namespace Ch.Cyberduck.Ui.Microsoft.Windows.Sdk
{
    using Forms = System.Windows.Forms;

    public unsafe static class DropTargetHelper
    {
        private static readonly IDictionary<Control, Forms.IDataObject> s_dataContext = new Dictionary<Control, Forms.IDataObject>();

        private static readonly IDropTargetHelper s_instance;

        static DropTargetHelper()
        {
            CoCreateInstance(CLSID_DragDropHelper, null, (uint)CLSCTX_INPROC_SERVER, out s_instance).ThrowOnFailure();
        }

        public static void DragEnter(Control control, Forms.IDataObject data, in Point cursorOffset, DragDropEffects effect)
        {
            s_instance.DragEnter((HWND)control.Handle, (IDataObject)data, ToPoint(cursorOffset), (uint)effect);
        }

        public static void DragEnter(Control control, Forms.IDataObject data, Point cursorOffset, DragDropEffects effect, string descriptionMessage, string descriptionInsert)
        {
            SetDropDescription(data, (DROPIMAGETYPE)effect, descriptionMessage, descriptionInsert);
            DragEnter(control, data, cursorOffset, effect);
            if (!s_dataContext.ContainsKey(control))
            {
                s_dataContext.Add(control, data);
            }
            else
            {
                s_dataContext[control] = data;
            }
        }

        public static void DragLeave()
        {
            s_instance.DragLeave();
        }

        public static void DragLeave(Control control)
        {
            if (s_dataContext.ContainsKey(control))
            {
                SetDropDescription(s_dataContext[control], DROPIMAGE_INVALID, null, null);
                s_dataContext.Remove(control);
            }
            DragLeave();
        }

        public static void DragOver(Point cursorOffset, DragDropEffects effect)
        {
            s_instance.DragOver(ToPoint(cursorOffset), (uint)effect);
        }

        public static void Drop(Forms.IDataObject data, Point cursorOffset, DragDropEffects effect)
        {
            foreach (var pair in s_dataContext)
            {
                if (pair.Value == data)
                {
                    s_dataContext.Remove(pair);
                    break;
                }
            }
            s_instance.Drop((IDataObject)data, ToPoint(cursorOffset), (uint)effect);
        }

        public static void SetDropDescription(Forms.IDataObject data, DROPIMAGETYPE type, string descriptionMessage, string descriptionInsert)
        {
            FORMATETC format = default;
            format.cfFormat = (ushort)RegisterClipboardFormat("DropDescription");
            format.dwAspect = (uint)DVASPECT_CONTENT;
            format.lindex = -1;
            format.ptd = null;
            format.tymed = (uint)TYMED_HGLOBAL;

            DROPDESCRIPTION dropDescription = default;
            dropDescription.type = type;
            MemoryMarshal.Cast<char, ushort>(descriptionMessage.AsSpan()).CopyTo(dropDescription.szMessage.AsSpan());
            MemoryMarshal.Cast<char, ushort>(descriptionInsert.AsSpan()).CopyTo(dropDescription.szInsert.AsSpan());

            STGMEDIUM medium = default;
            medium.pUnkForRelease = null;
            medium.tymed = (uint)TYMED_HGLOBAL;
            medium.Anonymous.hGlobal = (nint)(&dropDescription);
            ((IDataObject)data).SetData(format, medium, false);
        }

        public static void Show(bool show)
        {
            s_instance.Show(show);
        }

        private static ref POINT ToPoint(in Point point) => ref Unsafe.As<Point, POINT>(ref Unsafe.AsRef(point));
    }
}
