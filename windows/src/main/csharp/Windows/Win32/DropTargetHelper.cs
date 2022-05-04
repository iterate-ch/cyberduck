using System.Collections.Generic;
using System.Drawing;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
using System.Windows.Forms;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;
using static System.Runtime.InteropServices.ComTypes.DVASPECT;
using static System.Runtime.InteropServices.ComTypes.TYMED;
using static Windows.Win32.PInvoke;
using static Windows.Win32.System.Com.CLSCTX;
using ComTypes = System.Runtime.InteropServices.ComTypes;

namespace Windows.Win32
{
    public static unsafe partial class DropTargetHelper
    {
        private static readonly IDictionary<Control, ComTypes.IDataObject> s_dataContext = new Dictionary<Control, ComTypes.IDataObject>();

        private static readonly IDropTargetHelper s_instance;

        static DropTargetHelper()
        {
            Marshal.ThrowExceptionForHR(CoCreateInstance(CLSID_DragDropHelper, null, CLSCTX_ALL, out s_instance));
        }

        public static void DragEnter(Control control, ComTypes.IDataObject data, in Point cursorOffset, DROPIMAGETYPE effect)
        {
            s_instance.DragEnter((HWND)control.Handle, data, ToPoint(cursorOffset), effect);
        }

        public static void DragEnter(Control control, ComTypes.IDataObject data, Point cursorOffset, DROPIMAGETYPE effect, string descriptionMessage, string descriptionInsert)
        {
            SetDropDescription(data, effect, descriptionMessage, descriptionInsert);
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

        public static void DragOver(Point cursorOffset, DROPIMAGETYPE effect)
        {
            s_instance.DragOver(ToPoint(cursorOffset), effect);
        }

        public static void Drop(ComTypes.IDataObject data, Point cursorOffset, DROPIMAGETYPE effect)
        {
            s_instance.Drop(data, ToPoint(cursorOffset), effect);
        }

        public unsafe static void SetDropDescription(ComTypes.IDataObject data, DROPIMAGETYPE type, string descriptionMessage, string descriptionInsert)
        {
            FORMATETC format = new()
            {
                cfFormat = (short)RegisterClipboardFormat("DropDescription"),
                dwAspect = DVASPECT_CONTENT,
                lindex = -1,
                ptd = default,
                tymed = TYMED_HGLOBAL,
            };
            var dropDescription = (DROPDESCRIPTION*)Marshal.AllocHGlobal(sizeof(DROPDESCRIPTION));
            dropDescription->type = type;
            dropDescription->szMessage = descriptionMessage;
            dropDescription->szInsert = descriptionInsert;

            STGMEDIUM medium = new()
            {
                pUnkForRelease = default,
                tymed = TYMED_HGLOBAL,
                unionmember = (nint)dropDescription
            };

            try
            {
                data.SetData(ref format, ref medium, true);
            }
            catch
            {
                Marshal.FreeHGlobal((nint)dropDescription);
            }
        }

        public static void Show(bool show)
        {
            s_instance.Show(show);
        }

        private static ref POINT ToPoint(in Point point) => ref Unsafe.As<Point, POINT>(ref Unsafe.AsRef(point));
    }
}
