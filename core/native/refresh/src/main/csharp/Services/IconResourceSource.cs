namespace Cyberduck.Core.Refresh.Services
{
    public abstract class IconResourceSource
    {
        public abstract bool TryGetResource(string name, out object resource);
    }
}
