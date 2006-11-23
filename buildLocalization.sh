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
        
build() {
	if [ "$lproj" = "all" ] ; then
		{
			echo "*** Building all localizations...";
			for lproj in `ls ./build/Deployment/Cyberduck.app/Contents/Resources/ | grep lproj`; do
				language=`basename $lproj .lproj`;
				echo "*** Building $language localization disk image...";
				ant -Dlocalization=$language release-localization 
			done;
		}
	else
		{
			language=`basename $lproj .lproj`;
			echo "*** Building $language localization disk image...";
			ant -Dlocalization=$language release-localization 
		}
	fi;
}

lproj="all";

while [ "$1" != "" ] # When there are arguments...
	do case "$1" in 
			-l | --language)
				shift;
				lproj=$1;
				echo "Using Language:$lproj";
				build;
				echo "*** DONE. ***";
				exit 0;
			;;
			*)  
				echo "Option [$1] not one of  [--language]"; # Error (!)
				exit 1
			;; # Abort Script Now
	esac;
done;

build;