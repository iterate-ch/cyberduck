========================================
About the Cyberduck AppleScript Examples
========================================

General
-------

For information about AppleScript in general, see [http://www.apple.com/applescript/].

To view the dictionary of Cyberduck available for scripting drag the Cyberduck application onto the Script Editor (/Applications/AppleScript/Script Editor) or choose 'Open Dictionary...' from the 'File' menu in Script Editor. Click the 'Cyberduck Suite' in the left pane  of the window to see the definitions.

Installation
------------

You can copy the sample scripts to ~/Library/Scripts/. Please keep in mind that you have to modify the scripts to make them usable for you.
IMPORTANT: It seems that if the script is a folder action script you have to copy the file to ~/Library/Scripts/Folder Action Scripts/ to make it work.  

Samples
-------

- Upload Sample.scpt

Note: For further information about folder action scripts, see [http://www.apple.com/applescript/folderactions/].

You have to modify this script with the correct connection settings. See the line where the connection is made: [connect to "hostname" with protocol "ftp" as user "username" with initial folder "directory"].
You must attach this script to a folder in the 'Finder' (called 'Folder Action'). Right-click on a directory in the Finder, e.g. 'Upload' on the Desktop, and choose 'Attach Folder Action...'. Select the 'Upload Sample.scpt' script in ~/Library/Scripts/Folder Action Scripts/. 
Every file dropped onto this folder will then be uploaded to the server specified above.

- Edit Sample.scpt

Shows how to check for the existance of a file, create new folders and files and opening a file for editing with the external editor.

- List Sample.scpt

Recursively lists a folder content on the server and prompts for each file found to download.

- Sync Sample.scpt

Synchronizes a remote with a local folder mirroring all missing files and replacing files on either site based on the modification time.

- Open URL Sample.scpt

You can send any FTP or SFTP URL to Cyberduck as does a third party application when Cyberduck is the default protocol handler e.g. for FTP; see [http://www.rubicode.com/Software/RCDefaultApp/].

