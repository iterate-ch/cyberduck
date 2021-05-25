using System.Windows;
using System.Windows.Controls;

namespace Cyberduck.Core.Refresh.CustomControls
{
    public class ProfileElement : ListViewItem
    {
        static ProfileElement()
        {
            DefaultStyleKeyProperty.OverrideMetadata(typeof(ProfileElement), new FrameworkPropertyMetadata(typeof(ProfileElement)));
        }
    }
}
