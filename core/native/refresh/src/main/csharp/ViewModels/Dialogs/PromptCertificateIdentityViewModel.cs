using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reactive;
using System.Reactive.Linq;
using System.Security.Cryptography.X509Certificates;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;

/// <summary>
/// Prompt to select a client certificate from the certificates passed to
/// <c>CertificateIdentityCallback#prompt</c>.
/// </summary>
public class PromptCertificateIdentityViewModel : ReactiveObject
{
    private CertificateItemViewModel selectedItem;

    public ReactiveCommand<Unit, Unit> Cancel { get; }

    public Interaction<bool, Unit> Close { get; } = new();

    public ReactiveCommand<Unit, Unit> Confirm { get; }

    public ReadOnlyCollection<CertificateItemViewModel> Items { get; }

    public string Message { get; }

    public CertificateItemViewModel SelectedItem
    {
        get => selectedItem;
        set => this.RaiseAndSetIfChanged(ref selectedItem, value);
    }

    public Interaction<CertificateItemViewModel, Unit> Show { get; } = new();

    public ReactiveCommand<Unit, Unit> ShowCertificate { get; }

    public PromptCertificateIdentityViewModel(string hostname, IEnumerable<X509Certificate> certificates)
    {
        Message = string.Format(LocaleFactory.localizedString(
            "The server requires a certificate to validate your identity. Select the certificate to authenticate yourself to {0}.",
            "Keychain"), hostname);
        Items = certificates
            .Select(c => new CertificateItemViewModel(c))
            .OrderBy(c => c.Name, StringComparer.CurrentCultureIgnoreCase)
            .ToList()
            .AsReadOnly();
        selectedItem = Items.FirstOrDefault();

        var hasSelection = this.WhenAnyValue(x => x.SelectedItem).Select(x => x is not null);
        Cancel = ReactiveCommand.CreateFromObservable(OnCancel);
        Confirm = ReactiveCommand.CreateFromObservable(OnConfirm, hasSelection);
        ShowCertificate = ReactiveCommand.CreateFromObservable(OnShowCertificate, hasSelection);
    }

    private IObservable<Unit> OnCancel() => Close.Handle(false);

    private IObservable<Unit> OnConfirm() => Close.Handle(true);

    private IObservable<Unit> OnShowCertificate() => Show.Handle(SelectedItem);

    public class CertificateItemViewModel : ReactiveObject
    {
        public X509Certificate Certificate { get; }

        public string Issuer { get; }

        public string Name { get; }

        public X509Certificate2 Native { get; }

        public string Validity { get; }

        public CertificateItemViewModel(X509Certificate certificate)
        {
            Certificate = certificate;
            Native = SystemCertificateStore.ConvertCertificate(certificate);
            Name = Native.GetNameInfo(X509NameType.SimpleName, false);
            if (string.IsNullOrWhiteSpace(Name))
            {
                Name = Native.Subject;
            }

            Issuer = Native.GetNameInfo(X509NameType.SimpleName, true);
            Validity = $"{Native.NotBefore:d} – {Native.NotAfter:d}";
        }

        public override string ToString() => Name;
    }
}
