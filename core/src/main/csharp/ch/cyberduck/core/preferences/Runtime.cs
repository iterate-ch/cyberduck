using System;
using System.IO;
using System.Reflection;
using System.Runtime.CompilerServices;
using Windows.ApplicationModel;

namespace Ch.Cyberduck.Core.Preferences
{
    public static class Runtime
    {
        public static IRuntime Current { get; set; }

        public static string CompanyName => Current?.CompanyName;

        public static string DataFolderName => Current?.DataFolderName;

        public static string Location => Current?.Location;

        public static bool? Packaged => Current?.Packaged;

        public static string ProductName => Current?.ProductName;

        public static string ResourcesLocation => Current?.ResourcesLocation;

        public static string Revision => Current?.Revision;

        public static Version Version => Current?.Version;

        public static string VersionString => Current?.VersionString;

        public static ValueRuntime CreateDefault()
        {
            var entryAssembly = Assembly.GetEntryAssembly();
            var coreAssembly = typeof(Runtime).Assembly;

            string location;

            if (Uri.TryCreate(coreAssembly.CodeBase, UriKind.Absolute, out var codeBaseUri))
            {
                location = Path.GetDirectoryName(Uri.UnescapeDataString(codeBaseUri.LocalPath));
            }
            else
            {
                location = Path.GetDirectoryName(coreAssembly.Location);
            }

            string productName = default;
            Version version = default;

            if (entryAssembly?.GetName() is AssemblyName entryName)
            {
                productName = entryName.Name;
                version = entryName.Version;
            }

            var companyName = entryAssembly?.GetCustomAttribute<AssemblyCompanyAttribute>() switch
            {
                AssemblyCompanyAttribute company => company.Company,
                _ => productName,
            };

            var dataFolderName = productName;
            var packaged = Utils.IsRunningAsUWP;

            var resourcesLocation = packaged
                ? PackageInstalledPath()
                : location;

            return new(companyName, dataFolderName, location, packaged, productName, resourcesLocation, version);

            [MethodImpl(MethodImplOptions.NoInlining)]
            static string PackageInstalledPath() => Package.Current.InstalledPath;
        }

        public readonly record struct ValueRuntime(
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
        }
    }
}
