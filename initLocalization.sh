#  Copyright (c) 2003 David Kocher. All rights reserved.
#  http://cyberduck.ch/
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  Bug fixes, suggestions and comments should be sent to:
#  dkocher@cyberduck.ch

#!/bin/bash

usage ( ) {
	echo "Usage: initLocalization.sh <language>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       as the base language, English is assumed"
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1;

mkdir -p $language.lproj

for nibfile in `ls English.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    echo Copying $nibfile
    nib=`basename $nibfile .nib`
    cp -R English.lproj/$nibfile $language.lproj/$nibfile
    rm -rf $language.lproj/$nibfile/CVS
    nibtool --localizable-strings $language.lproj/$nibfile > $language.lproj/$nib.strings
done

exit 0

