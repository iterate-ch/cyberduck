using Windows.Win32.Foundation;

namespace Windows.Win32;

public static partial class Foundation_PWSTR_Extensions
{

    public static unsafe PWSTR DangerousAsPWSTR(this in PCWSTR pwstr) => pwstr.Value;

}
