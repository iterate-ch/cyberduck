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
	echo "Usage: udpateNibFromStrings.sh <language>"
	echo "       language must be English, French, Spanish, ..."
}

if [ $# -ne 1 ] ; then \
	usage ; \
	exit 1
fi

language=$1
for nibfile in `ls $language.lproj | grep .nib | grep -v ~.nib | grep -v .bak`; do
    nib=`basename $nibfile .nib`

    echo "Updating $nib in $language.lproj..."

    rm -rf $language.lproj/$nibfile.bak
    mv $language.lproj/$nibfile $language.lproj/$nibfile.bak
    nibtool --write $language.lproj/$nibfile --incremental $language.lproj/$nibfile.bak --dictionary $language.lproj/$nib.strings English.lproj/$nibfile
    cp -R $language.lproj/$nibfile.bak/CVS $language.lproj/$nibfile/CVS
done
exit 0

