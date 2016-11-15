using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core.local;
using Windows.Storage;
using Windows.System;

namespace Ch.Cyberduck.Core.Local
{
	public class WindowsUAPApplicationLauncher : ApplicationLauncher
	{
		public void bounce(Local l)
		{
			//
		}

		public bool open(Local l)
		{
			StorageFile file = StorageFile.GetFileFromPathAsync(l.getAbsolute()).GetResults();
			return Launcher.LaunchFileAsync(file).GetResults();
		}

		public bool open(Application a, string str)
		{
			throw new NotImplementedException();
		}

		public bool open(Local l, Application a, ApplicationQuitCallback aqc)
		{
			return open(l);
		}
	}
}
