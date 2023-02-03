using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using Ch.Cyberduck.Core.Refresh.Models;
using Splat;
using Locale = ch.cyberduck.core.i18n.Locale;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Info
{
    public class VersionViewModel
    {
        public VersionViewModel(VersionModel model)
        {
            var locale = Locator.Current.GetService<Locale>();
            Model = model;
            Checksum = ch.cyberduck.core.io.Checksum.NONE.Equals(model.Checksum) ? locale.localize("None", "Localizable") : model.Checksum.hash;
            Owner = string.IsNullOrWhiteSpace(Model.Owner) ? locale.localize("Unknown", "Localizable") : Model.Owner;
            Size = SizeFormatterFactory.get().format(Model.Size, true);
            Timestamp = UserDateFormatterFactory.get().getMediumFormat(Model.Timestamp);
        }

        public string Checksum { get; }

        public VersionModel Model { get; }

        public string Owner { get; }

        public string Size { get; }

        public string Timestamp { get; }
    }
}
