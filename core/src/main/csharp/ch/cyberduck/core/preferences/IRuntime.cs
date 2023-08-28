using System;

namespace Ch.Cyberduck.Core.Preferences
{
    public interface IRuntime
    {
        public string CompanyName { get; }

        public string DataFolderName { get; }

        public string Location { get; }

        public bool Packaged { get; }

        public string ProductName { get; }

        public string ResourcesLocation { get; }

        public string Revision { get; }

        public Version Version { get; }

        public string VersionString { get; }
    }
}
