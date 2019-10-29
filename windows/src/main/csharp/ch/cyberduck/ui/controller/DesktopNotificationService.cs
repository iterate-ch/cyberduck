using ch.cyberduck.core.notification;
using Ch.Cyberduck.Core.Notifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DesktopNotificationService : AbstractDesktopNotificationService
    {
        protected override string AumID => "iterate.Cyberduck";
    }
}
