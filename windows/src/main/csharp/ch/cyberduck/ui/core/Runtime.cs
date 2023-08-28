using Ch.Cyberduck.Core.Preferences;
using System;

namespace Ch.Cyberduck.Ui.Core
{
    public record class Runtime(
            string CompanyName,
            string DataFolderName,
            string Location,
            bool Packaged,
            string ProductName,
            string ResourcesLocation,
            Version Version) : IRuntime
    {
        public string CompanyName { get; } = CompanyName;

        public string DataFolderName { get; } = DataFolderName;

        public string Location { get; } = Location;

        public string ProductName { get; } = ProductName;

        public string ResourcesLocation { get; } = ResourcesLocation;

        public bool Packaged { get; } = Packaged;

        public string Revision { get; } = Version.Revision.ToString();

        public Version Version { get; } = Version;

        public string VersionString { get; } = Version.ToString(3);

        public Runtime(Cyberduck.Core.Preferences.Runtime.ValueRuntime copy)
            : this(
                  copy.CompanyName,
                  copy.DataFolderName,
                  copy.Location,
                  copy.Packaged,
                  copy.ProductName,
                  copy.ResourcesLocation,
                  copy.Version)
        {
        }
    }
}
