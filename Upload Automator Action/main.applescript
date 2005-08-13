--  Upload files
--
--  Copyright (c) 2004 David Kocher. All rights reserved.
--  http://cyberduck.ch/
--
--  This program is free software; you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation; either version 2 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  Bug fixes, suggestions and comments should be sent to:
--  dkocher@cyberduck.ch

on run {input, parameters}
	if input is not {} then
		if the class of the input is list then
			set theServer to (server of parameters) as string
			if username is in parameters then
				set theUser to (username of parameters) as string
			else
				set theUser to "anonymous" as string
			end if
			if password is in parameters then
				set thePassword to (|password| of parameters) as string
			else
				set thePassword to "" as string
			end if
			set theProtocolIndex to (protocol of parameters) as integer
			if (theProtocolIndex is equal to 0) then
				set theProtocol to "ftp" as string
			else if (theProtocolIndex is equal to 1) then
				set theProtocol to "ftps" as string
			else if (theProtocolIndex is equal to 2) then
				set theProtocol to "sftp" as string
			else
				set theProtocol to "ftp" as string
			end if
			if path is in parameters then
				set thePath to (|path| of parameters) as string
			else
				set thePath to "" as string
			end if
			
			tell application "Cyberduck"
				set theBrowser to (make new browser)
				tell (theBrowser)
					connect to theServer with protocol theProtocol as user theUser with password thePassword
					change folder to thePath
					repeat with i in input
						upload file (POSIX path of i)
					end repeat
					disconnect
				end tell
			end tell
		end if
	end if
	return input
end run