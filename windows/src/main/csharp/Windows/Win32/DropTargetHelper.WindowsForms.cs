using System.Drawing;
using System.Windows.Forms;
using Windows.Win32.UI.Shell;
using ComTypes = System.Runtime.InteropServices.ComTypes;

#pragma warning disable CS0426, CS1061

namespace Windows.Win32;

using static Windows.Win32.UI.Shell.DROPIMAGETYPE;

public partial class DropTargetHelper
{
    public static void DragEnter(Control control, IDataObject data, in Point cursorOffset, DragDropEffects effect)
    {
        DragEnter(control, (ComTypes.IDataObject)data, cursorOffset, (DROPIMAGETYPE)effect);
    }

    public static void DragEnter(Control control, IDataObject data, Point cursorOffset, DragDropEffects effect, string descriptionMessage, string descriptionInsert)
    {
        DragEnter(control, (ComTypes.IDataObject)data, cursorOffset, (DROPIMAGETYPE)effect, descriptionMessage, descriptionInsert);
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
        DragOver(cursorOffset, (DROPIMAGETYPE)effect);
    }

    public static void Drop(IDataObject data, Point cursorOffset, DragDropEffects effect)
    {
        foreach (var pair in s_dataContext)
        {
            if (pair.Value == data)
            {
                s_dataContext.Remove(pair);
                break;
            }
        }
        Drop((ComTypes.IDataObject)data, cursorOffset, (DROPIMAGETYPE)effect);
    }

    public static void SetDropDescription(IDataObject data, DragDropEffects type, string descriptionMessage, string descriptionInsert)
    {
        SetDropDescription((ComTypes.IDataObject)data, (DROPIMAGETYPE)type, descriptionMessage, descriptionInsert);
    }
}
