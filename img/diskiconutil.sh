#!/usr/bin/env bash
#
# Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
# https://cyberduck.io/
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#

sips="/usr/bin/sips"
iconutil="/usr/bin/iconutil"
tiffutil="/usr/bin/tiffutil"

usage() {
    echo "Converts PNG .iconset and .tiff"
	  echo "	  Usage: diskiconutil.sh <png>"
}

run() {
    name="`basename $icon .png`"
    mkdir -p $name.iconset
    $sips -s format png -z 16 16     -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_16x16.png
    $sips -s format png -z 32 32     -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_16x16@2x.png
    $sips -s format png -z 32 32     -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_32x32.png
    $sips -s format png -z 64 64     -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_32x32@2x.png
    $sips -s format png -z 64 64     -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_64x64.png
    $sips -s format png -z 128 128   -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_64x64@2x.png
    $sips -s format png -z 128 128   -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_128x128.png
    $sips -s format png -z 256 256   -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_128x128@2x.png
    $sips -s format png -z 256 256   -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_256x256.png
    $sips -s format png -z 512 512   -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_256x256@2x.png
    $sips -s format png -z 512 512   -s dpiHeight 72.0 -s dpiWidth 72.0   "$icon" --out $name.iconset/icon_512x512.png
    $sips -s format png -z 1024 1024 -s dpiHeight 144.0 -s dpiWidth 144.0 "$icon" --out $name.iconset/icon_512x512@2x.png
    $iconutil -c icns $name.iconset -o $name.icns
    $tiffutil -cathidpicheck $name.iconset/icon_64x64.png $name.iconset/icon_64x64@2x.png -out $name.tiff
}

if [ -z "$1" ]
then
    usage;
    echo "Missing image input parameter. Use diskiconutil.sh <image>"
    exit
fi

basedir=$(dirname "$0")
icon="$1"
run;