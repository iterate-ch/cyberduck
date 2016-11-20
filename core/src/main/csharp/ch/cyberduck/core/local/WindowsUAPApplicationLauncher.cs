using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using ch.cyberduck.core.local;
using Windows.ApplicationModel;
using Windows.Foundation;
using Windows.Storage;
using Windows.System;

namespace Ch.Cyberduck.Core.Local
{
    public class WindowsUAPApplicationLauncher : ApplicationLauncher
    {
        public void bounce(ch.cyberduck.core.Local l)
        {
            //
        }

        public bool open(ch.cyberduck.core.Local l)
        {
            FileInfo fileInfo;

            if (string.IsNullOrEmpty(Path.GetPathRoot(l.getAbsolute())))
                fileInfo = new FileInfo(Path.Combine(Package.Current.InstalledLocation.Path, l.getAbsolute())); // root to installed location
            else
                fileInfo = new FileInfo(l.getAbsolute());

            IAsyncOperation<StorageFile> fileOperation = StorageFile.GetFileFromPathAsync(fileInfo.FullName);
            StorageFile file = fileOperation.AsTask().Result;
            return Launcher.LaunchFileAsync(file, new LauncherOptions()
            {
                DisplayApplicationPicker = true
            }).AsTask().Result;
        }

        public bool open(ch.cyberduck.core.local.Application a, string str)
        {
            return true; // no exception
        }

        public bool open(ch.cyberduck.core.Local l, ch.cyberduck.core.local.Application a, ApplicationQuitCallback aqc)
        {
            return open(l);
        }
    }
}
