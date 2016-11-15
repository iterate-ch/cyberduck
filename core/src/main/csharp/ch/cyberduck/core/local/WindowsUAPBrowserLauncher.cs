using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core.local;
using Windows.System;

namespace Ch.Cyberduck.Core.Local
{
	public class WindowsUAPBrowserLauncher : BrowserLauncher
	{
		public bool open(string str)
		{
			return Launcher.LaunchUriAsync(new Uri(str)).GetResults();
		}
	}
}
