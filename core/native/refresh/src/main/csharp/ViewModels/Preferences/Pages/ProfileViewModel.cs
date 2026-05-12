using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.profiles;
using ReactiveUI;
using System.Linq;
using System.Reactive;
using System.Reactive.Linq;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfileViewModel : ReactiveObject
    {
        private bool installed;

        public ProfileViewModel(ProfileDescription description)
        {
            ProfileDescription = description;
            Installed = description.isInstalled() && description.isEnabled();
            Enabled = !description.isBundled() && !Utils.ConvertFromJavaList<Host>(BookmarkCollection.defaultCollection()).Any(host =>
                            host.getProtocol().getProvider().equals(description.getProvider())
                                && host.getProtocol().getIdentifier().equals(description.getIdentifier()));

            OpenHelp = ReactiveCommand.Create(() =>
            {
                BrowserLauncherFactory.get().open(ProfileDescription.getHelp());
            }, Observable.Return(!string.IsNullOrWhiteSpace(ProfileDescription.getHelp())));
        }

        public string DefaultHostName => string.Empty;

        public string Description => ProfileDescription.getDescription();

        public bool Enabled { get; }

        public bool Installed
        {
            get => installed;
            set => this.RaiseAndSetIfChanged(ref installed, value);
        }

        public string Name => ProfileDescription.getName();

        public ReactiveCommand<Unit, Unit> OpenHelp { get; }

        public ProfileDescription ProfileDescription { get; }

        public string Thumbnail => ProfileDescription.getThumbnail();
    }
}
