for PROTOCOL in "ftp" "s3" "swift" "googlestorage" "googledrive" "azure" "dropbox" "onedrive"; do
    mkdir -p $PROTOCOL.iconset
    sips -s format png -z 16 16     -s dpiHeight 72.0 -s dpiWidth 72.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_16x16.png
    sips -s format png -z 32 32     -s dpiHeight 144.0 -s dpiWidth 144.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_16x16@2x.png
    sips -s format png -z 32 32     -s dpiHeight 72.0 -s dpiWidth 72.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_32x32.png
    sips -s format png -z 64 64     -s dpiHeight 144.0 -s dpiWidth 144.0 "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_32x32@2x.png
    sips -s format png -z 128 128   -s dpiHeight 72.0 -s dpiWidth 72.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_128x128.png
    sips -s format png -z 256 256   -s dpiHeight 144.0 -s dpiWidth 144.0 "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_128x128@2x.png
    #sips -s format png -z 256 256   -s dpiHeight 72.0 -s dpiWidth 72.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_256x256.png
    #sips -s format png -z 512 512   -s dpiHeight 144.0 -s dpiWidth 144.0 "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_256x256@2x.png
    #sips -s format png -z 512 512   -s dpiHeight 72.0 -s dpiWidth 72.0   "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_512x512.png
    #sips -s format png -z 1024 1024 -s dpiHeight 144.0 -s dpiWidth 144.0 "$PROTOCOL@2x.png" --out $PROTOCOL.iconset/icon_512x512@2x.png
    iconutil -c icns $PROTOCOL.iconset -o $PROTOCOL.icns
    sips -s format tiff $PROTOCOL.icns --out $PROTOCOL.tiff
done;