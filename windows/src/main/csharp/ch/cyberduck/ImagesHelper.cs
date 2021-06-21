using Cyberduck.Core.Refresh;
using Cyberduck.Core.Refresh.Services;
using StructureMap;

namespace Ch.Cyberduck
{
    public static class ImagesHelper
    {
        static ImagesHelper()
        {
            Images = ObjectFactory.GetInstance<Images>();
            IconProvider = ObjectFactory.GetInstance<WinFormsIconProvider>();
        }

        public static WinFormsIconProvider IconProvider { get; }

        public static Images Images { get; }
    }
}
