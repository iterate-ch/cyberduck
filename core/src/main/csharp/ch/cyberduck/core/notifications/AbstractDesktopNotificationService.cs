using ch.cyberduck.core;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.pool;
using Microsoft.Toolkit.Uwp.Notifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Core.Notifications
{
    public abstract class AbstractDesktopNotificationService : NotificationService
    {
        private ToastNotificationHistory history;

        protected abstract string AumID { get; }

        public void notify(string group, string identifier, string title, string description, string action)
        {
            notify(group, identifier, title, description);
        }

        public void notify(string group, string identifier, string title, string description)
        {
            var toastContent = new ToastContent
            {
                Visual = new ToastVisual()
                {
                    BindingGeneric = new ToastBindingGeneric()
                    {
                        Children =
                        {
                            new AdaptiveText()
                            {
                                Text = title
                            },
                            new AdaptiveText()
                            {
                                Text = description
                            }
                        }
                    }
                }
            };

            var doc = new XmlDocument();
            doc.LoadXml(toastContent.GetContent());

            var toast = new ToastNotification(doc);
            if (!string.IsNullOrWhiteSpace(identifier))
            {
                toast.Tag = identifier;
                toast.SuppressPopup = ShouldSuppressPopup(toast);
            }

            ToastNotifier notifier;
            if (Utils.IsRunningAsUWP)
            {
                notifier = ToastNotificationManager.CreateToastNotifier();
            }
            else
            {
                notifier = ToastNotificationManager.CreateToastNotifier(AumID);
            }

            notifier.Show(toast);
        }

        NotificationService NotificationService.setup()
        {
            history = ToastNotificationManager.History;

            return this;
        }

        void NotificationService.unregister()
        {
            if (Utils.IsRunningAsUWP)
            {
                history.Clear();
            }
            else
            {
                history.Clear(AumID);
            }
        }

        private static Func<ToastNotification, bool> GetToastComparer(ToastNotification original)
        {
            return other =>
            {
                if (!Equals(other.Group, original.Group))
                {
                    return false;
                }
                if (!Equals(other.Tag, original.Tag))
                {
                    return false;
                }
                return true;
            };
        }

        private bool ShouldSuppressPopup(ToastNotification toast)
        {
            IReadOnlyList<ToastNotification> toasts;
            if (Utils.IsRunningAsUWP)
            {
                toasts = history.GetHistory();
            }
            else
            {
                toasts = history.GetHistory(AumID);
            }
            return toasts.Any(GetToastComparer(toast));
        }
    }
}
