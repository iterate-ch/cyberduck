using System.IO;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public interface IIconProviderImageSource
    {
        public Stream GetStream(string name);
    }
}
