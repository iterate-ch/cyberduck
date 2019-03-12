using ch.cyberduck.core.notification;
using Ch.Cyberduck.Core.Notifications;
using DesktopNotifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DesktopNotificationService : AbstractDesktopNotificationService<DesktopNotificationService.DesktopNotificationActivator>
    {
        protected override string AumID => "iterate.Cyberduck";

        protected override string ResolveGroup(string group) => null;

        [ClassInterface(ClassInterfaceType.None)]
        [ComSourceInterfaces(typeof(INotificationActivationCallback))]
        [Guid("04FAA434-8D25-4690-AF66-B63B39C1FEDE"), ComVisible(true)]
        public class DesktopNotificationActivator : NotificationActivator
        {
            public override void OnActivated(string arguments, NotificationUserInput userInput, string appUserModelId)
            {
            }
        }
    }
}
