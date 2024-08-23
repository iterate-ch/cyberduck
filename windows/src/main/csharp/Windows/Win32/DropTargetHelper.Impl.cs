using System;
using System.Runtime.InteropServices;
using System.Runtime.InteropServices.ComTypes;
using Windows.Win32.UI.Shell;
using static System.Runtime.InteropServices.ComTypes.DVASPECT;
using static System.Runtime.InteropServices.ComTypes.TYMED;
using static Windows.Win32.PInvoke;

namespace Windows.Win32;

public static unsafe partial class DropTargetHelper
{
    private static readonly IDropTargetHelper s_instance;

    static DropTargetHelper()
    {
        s_instance = (IDropTargetHelper)Activator.CreateInstance(Type.GetTypeFromCLSID(CLSID_DragDropHelper));
    }

    private unsafe static void SetDropDescription(IDataObject data, DROPIMAGETYPE type, string descriptionMessage, string descriptionInsert)
    {
        FORMATETC format = new()
        {
            cfFormat = (short)RegisterClipboardFormat("DropDescription"),
            dwAspect = DVASPECT_CONTENT,
            lindex = -1,
            ptd = default,
            tymed = TYMED_HGLOBAL,
        };

        DROPDESCRIPTION* dropDescription = null;
        try
        {
            dropDescription = (DROPDESCRIPTION*)Marshal.AllocHGlobal(sizeof(DROPDESCRIPTION));
            *dropDescription = new()
            {
                type = type,
                szMessage = descriptionMessage,
                szInsert = descriptionInsert
            };

            STGMEDIUM medium = new()
            {
                pUnkForRelease = default,
                tymed = TYMED_HGLOBAL,
                unionmember = (nint)dropDescription
            };

            data.SetData(
                formatIn: ref format,
                medium: ref medium,
                release: true);
        }
        catch
        {
            Marshal.FreeHGlobal((nint)dropDescription);
        }
    }
}
