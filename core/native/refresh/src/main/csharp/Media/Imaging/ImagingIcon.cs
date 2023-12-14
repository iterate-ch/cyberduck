using System.IO;
using System.Windows.Media.Imaging;

namespace Ch.Cyberduck.Core.Refresh.Media.Imaging;

public class ImagingIcon : Icon<BitmapSource>
{
    public ImagingIcon() { }

    public ImagingIcon(Stream stream) : base(stream) { }

    protected override BitmapSource Decode(Stream stream)
    {
        var decoder = new IconBitmapDecoder(stream, BitmapCreateOptions.None, BitmapCacheOption.OnLoad);
        return decoder.Frames[0];
    }

    protected override void Encode(BitmapSource image, Stream stream, out (int Width, int Height) size)
    {
        size = (image.PixelWidth, image.PixelHeight);
        PngBitmapEncoder encoder = new();
        var frame = BitmapFrame.Create(image);
        encoder.Frames.Add(frame);
        encoder.Save(stream);
    }
}
