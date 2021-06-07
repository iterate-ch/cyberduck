using Ch.Cyberduck;
using Cyberduck.Core.Refresh.Services;
using System.Drawing;

namespace Cyberduck.ch.cyberduck.ui.core.resources
{
    public class CyberduckIconSource : IconResourceSource
    {
        public override bool TryGetResource(string name, out object resource)
        {
            if (ResourcesBundle.ResourceManager.GetObject(name) is Bitmap bitmap)
            {
                resource = bitmap;
                return true;
            }
            resource = default;
            return false;
        }
    }
}
