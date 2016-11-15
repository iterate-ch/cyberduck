using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Windows.Storage;
using Windows.System;

namespace Ch.Cyberduck.Core.Local
{
	public class UAPExplorerRevealService : RevealService
	{
		public bool reveal(ch.cyberduck.core.Local l)
		{
			StorageFolder folder = StorageFolder.GetFolderFromPathAsync(l.getAbsolute()).GetResults();
			return Launcher.LaunchFolderAsync(folder).GetResults();
		}
	}
}
