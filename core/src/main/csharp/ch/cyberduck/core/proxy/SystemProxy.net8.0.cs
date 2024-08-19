using System.Net;
using System.Net.Http;

namespace Ch.Cyberduck.Core.Proxy
{
    partial class SystemProxy
    {
        private static IWebProxy DefaultSystemProxy => HttpClient.DefaultProxy;
    }
}
