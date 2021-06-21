using System.IO;
using System.Resources;

namespace Cyberduck.Core.Refresh.Services
{
    public class CyberduckImageSource
    {
        private readonly ResourceManager resourceManager = new("Images", typeof(CyberduckImageSource).Assembly);

        /// <returns><see cref="UnmanagedMemoryStream"/> or null.</returns>
        public Stream FindImage(string name) => resourceManager.GetStream(name.ToLowerInvariant());
    }
}
