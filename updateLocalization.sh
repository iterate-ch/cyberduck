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
	echo "Usage: udpateLocalization.sh <language>"
	echo "       language must be Japanese, French, Spanish, ..."
	echo "       as the base language, English is assumed"
}

if [ $# = 1 ] ; then
{
    language=$1;
    echo "**********************************"
    echo "Updating $language Localization..."
    echo "**********************************"
    # First update the nib file with the modifications in the .strings file. 
    # The .strings file is always assumed to be newer - this means that all 
    # modifications should not be made in the .nib file directly but in 
    # the .strings file instead.
    ./updateNibFromStrings.sh $language
    # Update the localized .strings file with new strings added to to the base (English) version.
    ./updateStringsFromNib.sh $language
}
else 
{
    for lproj in `ls . | grep lproj`; do
        language=`basename $lproj .lproj`
        
        if [ $language != "English" ]; then
        {
            echo "**********************************"
            echo "Updating $language Localization..."
            echo "**********************************"
        
            # Update the nib file with the modifications in the .strings file. 
            # The .strings file is always assumed to be newer - this means that all 
            # modifications should not be made in the .nib file directly but in 
            # the .strings file instead.
            ./updateNibFromStrings.sh $language
        }
        fi;
    done;
}
fi;
exit 0

