using System.Net;

namespace Ch.Cyberduck.Core.Proxy;

partial class SystemProxy
{
    private static IWebProxy DefaultSystemProxy => WebRequest.DefaultWebProxy;
}
