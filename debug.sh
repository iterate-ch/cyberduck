#  Copyright (c) 2007 David Kocher. All rights reserved.
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

#!/bin/sh

usage() {
	echo ""
	echo "	  Usage: debug.sh [--enable | -e] [--disable | -d]"
	echo ""
}

enable() {
	# When enabled, you can connect to the running application using 
	# -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
	cp Info.plist.debug Info.plist;
	make;
}

disable() {
	svn revert Info.plist;
	make;
}

while [ "$1" != "" ] # When there are arguments...
	do case "$1" in 
		-e | --enable)
			echo "Enabling debug configuration...";
			enable;
			exit 0;
			echo "*** DONE. ***";
		;;
		-d | --disable)
			echo "Disabling debug configuration...";
			disable;
			echo "*** DONE. ***";
			exit 0;
		;; 
		*)	
			echo "Option [$1] not one of  [--enable, --disable]"; # Error (!)
			usage;
			exit 1
		;; # Abort Script Now
	esac;
done;

usage;
