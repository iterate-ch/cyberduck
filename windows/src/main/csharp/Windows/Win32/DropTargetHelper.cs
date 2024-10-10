using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using Windows.Win32.Foundation;
using Windows.Win32.System.Ole;
using Windows.Win32.UI.Shell;
using ComTypes = System.Runtime.InteropServices.ComTypes;

namespace Windows.Win32
{
    public static partial class DropTargetHelper
    {
        private static readonly Dictionary<Control, IDataObject> s_dataContext = [];

        public static void DragEnter(Control control, IDataObject dataObject, in Point cursorOffset, DragDropEffects effect)
        {
            s_instance.DragEnter((HWND)control.Handle, (ComTypes.IDataObject)dataObject, cursorOffset, (DROPEFFECT)effect);
        }

        public static void DragEnter(Control control, IDataObject data, Point cursorOffset, DragDropEffects effect, string descriptionMessage, string descriptionInsert)
        {
            SetDropDescription((ComTypes.IDataObject)data, (DROPIMAGETYPE)effect, descriptionMessage, descriptionInsert);
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
                SetDropDescription(s_dataContext[control], DROPIMAGETYPE.DROPIMAGE_INVALID, null, null);
                s_dataContext.Remove(control);
            }

            DragLeave();
        }

        public static void DragOver(in Point cursorOffset, DragDropEffects effect)
        {
            s_instance.DragOver(cursorOffset, (DROPEFFECT)effect);
        }

        public static void Drop(IDataObject data, in Point cursorOffset, DragDropEffects effect)
        {
            s_instance.Drop((ComTypes.IDataObject)data, cursorOffset, (DROPEFFECT)effect);
        }

        public static void SetDropDescription(IDataObject data, DragDropEffects type, string descriptionMessage, string descriptionInsert)
        {
            SetDropDescription(data, (DROPIMAGETYPE)type, descriptionMessage, descriptionInsert);
        }

        public static void Show(bool show)
        {
            s_instance.Show(show);
        }

        private static void SetDropDescription(IDataObject data, DROPIMAGETYPE type, string descriptionMessage, string descriptionInsert)
        {
            SetDropDescription((ComTypes.IDataObject)data, type, descriptionMessage, descriptionInsert);
        }
    }
}
