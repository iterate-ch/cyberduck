using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core.TaskDialog;
using java.util;
using org.apache.logging.log4j;
using System;
using System.Security.Cryptography.X509Certificates;
using System.Threading;
using static Windows.Win32.UI.WindowsAndMessaging.MESSAGEBOX_RESULT;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Core.Interactivity;

public class DialogPromptCertificateTrustCallback : CertificateTrustCallback
{
    private static readonly Logger Log = LogManager.getLogger(typeof(DialogPromptCertificateTrustCallback).FullName);
    private static readonly ThreadLocal<Action> VerificationCallbackLocal = new();

    public static VerificationCallbackRegistration Register(Action callback)
    {
        VerificationCallbackLocal.Value = callback;
        return new();
    }

    public void prompt(string str, List l)
    {
        var serverCert = SystemCertificateStore.ConvertCertificate(l.get(0) as X509Certificate);
        var result = TaskDialog.TaskDialog.Create()
            .Title(LocaleFactory.localizedString("Certificate Error", "Keychain"))
            .Instruction(LocaleFactory.localizedString("Certificate Error", "Keychain"))
            .VerificationText(LocaleFactory.localizedString("Always Trust", "Keychain"), false)
            .Content(str)
            .CommandLinks(c =>
            {
                c(IDCONTINUE, LocaleFactory.localizedString("Continue", "Credentials"), false);
                c(IDABORT, LocaleFactory.localizedString("Disconnect"), true);
                c(IDHELP, LocaleFactory.localizedString("Show Certificate", "Keychain"), false);
            })
            .Callback((sender, e) =>
            {
                if (e is TaskDialogButtonClickedEventArgs buttonClicked)
                {
                    if (buttonClicked.ButtonId == (int)IDHELP)
                    {
                        X509Certificate2UI.DisplayCertificate(serverCert);
                        return true;
                    }
                }

                return false;
            })
            .Show();

        if (result.Button == IDABORT)
        {
            throw new ConnectionCanceledException();
        }

        if (result.VerificationChecked == true)
        {
            VerificationCallbackLocal.Value?.Invoke();
        }
    }

    public ref struct VerificationCallbackRegistration
    {
        public void Dispose()
        {
            VerificationCallbackLocal.Value = null;
        }
    }
}
