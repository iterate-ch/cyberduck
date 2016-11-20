using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.local;
using Windows.Storage;
using Windows.System;
using Path = System.IO.Path;

namespace Ch.Cyberduck.Core.Local
{
    public class UAPExplorerRevealService : RevealService
    {
        public bool reveal(ch.cyberduck.core.Local l)
        {
            var directory = Path.GetDirectoryName(l.getAbsolute());
            StorageFile file = StorageFile.GetFileFromPathAsync(l.getAbsolute()).GetAwaiter().GetResult();
            StorageFolder folder = StorageFolder.GetFolderFromPathAsync(directory).GetAwaiter().GetResult();
            FolderLauncherOptions options = new FolderLauncherOptions();
            options.ItemsToSelect.Add(file);
            return Launcher.LaunchFolderAsync(folder).GetAwaiter().GetResult();
        }
    }
}
