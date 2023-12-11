using System.Drawing;
using System.Drawing.Imaging;
using System.IO;

namespace Ch.Cyberduck.Core.Refresh.Media.Imaging;

public class GDIIcon : Icon<Image>
{
    public GDIIcon() { }

    public GDIIcon(Stream stream) : base(stream) { }

    protected override Image Decode(Stream stream)
    {
        using var icon = new Icon(stream);
        return icon.ToBitmap();
    }

    protected override void Encode(Image image, Stream stream, out (int Width, int Height) size)
    {
        size = (image.Width, image.Height);
        image.Save(stream, ImageFormat.Png);
    }
}
