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

        private Action<string> listeners;
        protected abstract string AumID { get; }

        public void addListener(NotificationService.Listener listener)
        {
            listeners += listener.callback;
        }

        public void notify(string group, string identifier, string title, string description, string action)
        {
            var toastContent = GetToast(title, description);

            toastContent.Actions = new ToastActionsCustom()
            {
                Buttons =
                {
                    new ToastButton(action, string.Empty)
                }
            };

            Toast(toastContent, identifier, true);
        }

        public void notify(string group, string identifier, string title, string description)
        {
            var toastContent = GetToast(title, description);

            Toast(toastContent, identifier, false);
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

        private ToastContent GetToast(string title, string description)
        {
            return new ToastContent
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

        private void Toast(ToastContent toastContent, string identifier, bool handleActivated)
        {
            var doc = new XmlDocument();
            doc.LoadXml(toastContent.GetContent());

            var toast = new ToastNotification(doc);
            if (!string.IsNullOrWhiteSpace(identifier))
            {
                toast.Tag = identifier;
                toast.SuppressPopup = ShouldSuppressPopup(toast);
            }

            if (handleActivated)
            {
                toast.Activated += Toast_Activated;
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

        private void Toast_Activated(ToastNotification sender, object args) => listeners(sender.Tag);
    }
}
