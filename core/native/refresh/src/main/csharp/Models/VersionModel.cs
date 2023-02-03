using ch.cyberduck.core;
using ch.cyberduck.core.io;

namespace Ch.Cyberduck.Core.Refresh.Models
{
    public class VersionModel
    {
        public VersionModel(Path path)
        {
            Path = path;

            var attributes = path.attributes();
            Checksum = attributes.getChecksum();
            Owner = attributes.getOwner();
            Size = attributes.getSize();
            Timestamp = attributes.getModificationDate();
        }

        public Checksum Checksum { get; }

        public string Owner { get; }

        public Path Path { get; }

        public long Size { get; }

        public long Timestamp { get; }
    }
}
