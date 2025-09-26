# Changelog

[9.3.0](https://github.com/iterate-ch/cyberduck/compare/release-9-2-4...release-9-3-0)
* [Feature] Connect with Multi-Bucket Application Keys that grant access to a specific group of buckets within an
  account, including the option to limit access based on a single file prefix (B2) (#17139)
* [Feature] Support for Liquid Glass (macOS) ([#17459](https://trac.cyberduck.io/ticket/17459))
* [Feature] Connect with connection profile obtaining temporary credentials from AWS Security Token Service (STS) by assuming role with optional Multi-Factor Authentication (MFA) input (S3) (#17437)
* [Feature] Connect with connection profile obtaining temporary credentials from AWS Security Token Service (STS) by getting session token with optional Multi-Factor Authentication (MFA) input (S3) (#17506)

[9.2.4](https://github.com/iterate-ch/cyberduck/compare/release-9-2-3...release-9-2-4)
* [Bugfix] Unable to close connection window with "Cancel" (macOS) (#17366)
* [Bugfix] Choose "Cancel" in upload prompt continues transfer (macOS) (#17358)
* [Bugfix] Change button styles for bottom bar of window (macOS) (#17407)
* [Bugfix] Resumable uploads fail with Basic authentication (ownCloud)
* [Bugfix] Include "Add to My Files" shortcuts (OneDrive)
* [Bugfix] Exclude non-accessible "Personal Vault" (OneDrive) (#17318)
* [Bugfix] Cache connection profiles loaded in Preferences → Profiles (#17432)

[9.2.3](https://github.com/iterate-ch/cyberduck/compare/release-9-2-2...release-9-2-3)
* [Bugfix] Failure opening application (Windows)

[9.2.2](https://github.com/iterate-ch/cyberduck/compare/release-9-2-1...release-9-2-2)
* [Bugfix] Remove Dock Tile Plugin (macOS, Mac App Store)

[9.2.1](https://github.com/iterate-ch/cyberduck/compare/release-9-2-0...release-9-2-1)
* [Bugfix] Unable to enter username in login prompt (macOS)

[9.2.0](https://github.com/iterate-ch/cyberduck/compare/release-9-1-7...release-9-2-0)
* [Feature] Updated runtime (Windows) ([#16854](https://trac.cyberduck.io/ticket/16854))
* [Feature] Add plugin to set application icon (macOS) ([#17170](https://trac.cyberduck.io/ticket/17170))
* [Feature] Support for Mexico (Central) region (S3)
* [Feature] Support for Asia Pacific (Thailand) region (S3)
* [Feature] Support for Asia Pacific (Taipei) region (S3)

[9.1.7](https://github.com/iterate-ch/cyberduck/compare/release-9-1-6...release-9-1-7)
* [Bugfix] Timeout error when attempting to connect using public key authentication (SFTP) (Windows)
* [Bugfix] Use SHA-2 thumbprints to save trusted server certificates (Windows)
* [Bugfix] Do not save user trusted server certificates in system (Windows)

[9.1.6](https://github.com/iterate-ch/cyberduck/compare/release-9-1-5...release-9-1-6)
* [Bugfix] Use regular dual-stack endpoints for presigned URLs (S3)
* [Bugfix] No attempt to use SSH agent for authentication (SFTP) (Windows)
* [Bugfix] Share menu item not enabled (OneDrive, SharePoint) (#17082)

[9.1.5](https://github.com/iterate-ch/cyberduck/compare/release-9-1-4...release-9-1-5)
* [Bugfix] Failure notarizing builds with unsigned native library (macOS)

[9.1.4](https://github.com/iterate-ch/cyberduck/compare/release-9-1-3...release-9-1-4)
* [Bugfix] Slow file transfers (SMB) ([#16135](https://trac.cyberduck.io/ticket/16135))
* [Bugfix] Interoperability with Bitwarden SSH Agent ([#16954](https://trac.cyberduck.io/ticket/16954))
* [Bugfix] Skip trashed files for downloads (Google Drive) ([#16927](https://trac.cyberduck.io/ticket/16927))
* [Bugfix] Cannot delete entries from metadata (Google Drive) ([#16976](https://trac.cyberduck.io/ticket/16976))
* [Bugfix] Error uploading files when bucket name is set in hostname (
  S3) ([#16939](https://trac.cyberduck.io/ticket/16939))
* [Bugfix] No pre-signed URLs available when using credentials from AWS Command Line Interface (CLI) connection
  profile (S3) ([#13738](https://trac.cyberduck.io/ticket/13738))
* [Bugfix] No pre-signed URLs available when using credentials from OpenID Connect (OIDC) connection profile (S3)
* [Bugfix] Error opening connection ([#17012](https://trac.cyberduck.io/ticket/17012)) (Azure) (Windows, CLI)

[9.1.3](https://github.com/iterate-ch/cyberduck/compare/release-9-1-2...release-9-1-3)
* [Bugfix] Invalid progress in Finder transferring multiple files (macOS) ([#16738](https://trac.cyberduck.io/ticket/16738))
* [Bugfix] Diasble lookup of vaults by default. Must explicitly specify --vault (Cyptomator, CLI)
* [Bugfix] Duplicate file menu item always disabled (FTP) ([#16798](https://github.com/iterate-ch/cyberduck/issues/16798))
* [Bugfix] Allow OAuth configuration in connection profiles (WebDAV) ([#16792](https://github.com/iterate-ch/cyberduck/issues/16792))

[9.1.2](https://github.com/iterate-ch/cyberduck/compare/release-9-1-1...release-9-1-2)
* [Bugfix] No fallback to IPv4 address when IPv6 connect attempt fails with unreachable network ([#16723](https://trac.cyberduck.io/ticket/16723))
* [Bugfix] File → Rename option always disabled ([#16695](https://trac.cyberduck.io/ticket/16695))
* [Bugfix] Connect anonymously to public buckets (S3) ([#16746](https://trac.cyberduck.io/ticket/16746))
* [Bugfix] File progress in Finder shows incorrect values when transferring multiple files ([#16738](https://trac.cyberduck.io/ticket/16738))
* [Bugfix] Exit with error on transfer failure when using --quiet (CLI) ([#16750](https://trac.cyberduck.io/ticket/16750))

[9.1.1](https://github.com/iterate-ch/cyberduck/compare/release-9-1-0...release-9-1-1)
* [Bugfix] Repeated prompts for passphrase (Cryptomator, Windows) ([#16645](https://trac.cyberduck.io/ticket/16645))
* [Bugfix] Missing list of buckets (Linode Object Storage) ([#16659](https://trac.cyberduck.io/ticket/16659))
* [Bugfix] Enable encryption by default (SMB) ([#16678](https://trac.cyberduck.io/ticket/16678))
* [Bugfix] Application fails to execute and exits with no error message (CLI) ([#16683](https://trac.cyberduck.io/ticket/16683))

[9.1.0](https://github.com/iterate-ch/cyberduck/compare/release-9-0-2...release-9-1-0)
* [Feature] Display transfer progress in Finder for downloads and uploads (macOS) ([#16568](https://trac.cyberduck.io/ticket/16568))
* [Feature] Show indeterminate progress when concatenating segments after download transfer ([#13610](https://trac.cyberduck.io/ticket/13610))
* [Feature] Show indeterminate progress when waiting for large file upload to complete (Backblaze B2, Box, OpenStack Swift, S3)
* [Bugfix] Multipart uploads fail to buckets with '.' character in name (S3) ([#16401](https://trac.cyberduck.io/ticket/16401))
* [Bugfix] Usability improvements entering connection details ([#15200](https://trac.cyberduck.io/ticket/15200))
* [Bugfix] Set "Show Hidden Files"shortcut to "⌘ ⇧ ." as in Finder (macOS) ([#16459](https://trac.cyberduck.io/ticket/16459))
* [Bugfix] Do not require GroupMember.Read.All permission (Microsoft OneDrive, SharePoint) ([#16564](https://trac.cyberduck.io/ticket/16564))
* [Bugfix] Do not require storage.buckets.list permission to access bucket (Google Storage) ([#16565](https://trac.cyberduck.io/ticket/16565))
* [Bugfix] Test credentials using sts:GetCallerIdentity (AWS S3) ([#16565](https://trac.cyberduck.io/ticket/16565))
* [Bugfix] Cannot exclude files in transfer prompt for existing files (macOS) ([#16503](https://trac.cyberduck.io/ticket/16503))
* [Bugfix] Files not removed in vault when deleting folder (Cryptomator) (Box, Dropbox, Google Drive, OneDrive, WebDAV, SMB) ([#14101](https://trac.cyberduck.io/ticket/14101))
* [Bugfix] Access files with double-byte character in key (S3) ([#13407](https://trac.cyberduck.io/ticket/13407))
* [Bugfix] Failure uploading nested folders to vault (Cryptomator) (Azure, Backblaze B2, Google Storage, OpenStack Swift) ([#15489](https://trac.cyberduck.io/ticket/15489))
* [Bugfix] Error copying files between vaults (Cryptomator) ([#15422](https://trac.cyberduck.io/ticket/15422))

[9.0.3](https://github.com/iterate-ch/cyberduck/compare/release-9-0-2...release-9-0-3)
* [Bugfix] Registration key prompt in browser window regardless of registration (Windows) ([#16326](https://trac.cyberduck.io/ticket/16326))

[9.0.2](https://github.com/iterate-ch/cyberduck/compare/release-9-0-1...release-9-0-2)
* [Feature] Support for Canada West (Calgary) region (S3) ([#16215](https://trac.cyberduck.io/ticket/16215))
* [Feature] Support for Asia Pacific (Melbourne) region (S3)
* [Feature] Support for Israel (Tel Aviv) region (S3)
* [Feature] Support for Asia Pacific (Kuala Lumpur) region (S3)
* [Bugfix] No accessible elements in list of bookmarks (macOS) ([#10853](https://trac.cyberduck.io/ticket/10853))
* [Bugfix] Unable to enter device serial number for MFA delete (S3) ([#16267](https://trac.cyberduck.io/ticket/16267))

[9.0.1](https://github.com/iterate-ch/cyberduck/compare/release-9-0-0...release-9-0-1)
* [Bugfix] Auto detect of vaults not always working (Cryptomator)
* [Bugfix] Missing server prompts before transfer (Windows) (#16140, #16113)
* [Bugfix] Hang on reopening Transfer window when transfer in progress (Windows) ([#16147](https://trac.cyberduck.io/ticket/16147))
* [Bugfix] Crash when selected default protocol doesn’t exist (Windows) ([#16115](https://trac.cyberduck.io/ticket/16115))

[9.0.0](https://github.com/iterate-ch/cyberduck/compare/release-8-9-0...release-9-0-0)
* [Feature] Versioning of files edited in external application ([#15137](https://trac.cyberduck.io/ticket/15137))
* [Feature] Custom icons for protocols (Nextcloud, ownCloud)
* [Feature] Detect proxy configuration change for already open connections ([#13274](https://trac.cyberduck.io/ticket/13274))
* [Feature] Rewrite implementation of Transfers window (Windows) ([#15885](https://trac.cyberduck.io/ticket/15885))
* [Bugfix] Failure retrieving previous versions of file (ownCloud)
* [Bugfix] Missing shared folders (OneDrive Business) ([#16036](https://trac.cyberduck.io/ticket/16036))

[8.9.0](https://github.com/iterate-ch/cyberduck/compare/release-8-8-2...release-8-9-0)
* [Feature] Resumable file uploads (ownCloud) ([#15619](https://trac.cyberduck.io/ticket/15619))
* [Feature] Support authentication using SSH certificates (SFTP) ([#15183](https://trac.cyberduck.io/ticket/15183))
* [Bugfix] Failure sharing file (ownCloud) (#15855, #15839)
* [Bugfix] Disable signing and encryption by default (SMB) ([#15817](https://trac.cyberduck.io/ticket/15817))
* [Bugfix] NTLM authentication failures (WebDAV, SharePoint) ([#15127](https://trac.cyberduck.io/ticket/15127))

[8.8.2](https://github.com/iterate-ch/cyberduck/compare/release-8-8-1...release-8-8-2)
* [Bugfix] Slower file transfers caused by connections per host limit ([#15737](https://trac.cyberduck.io/ticket/15737))
* [Bugfix] Unable to edit HTTP header metadata (S3) ([#15742](https://trac.cyberduck.io/ticket/15742))

[8.8.1](https://github.com/iterate-ch/cyberduck/compare/release-8-8-0...release-8-8-1)
* [Bugfix] Mismatch with Content-MD5 for multipart uploads to vault (S3, Cryptomator) ([#15704](https://trac.cyberduck.io/ticket/15704))

[8.8.0](https://github.com/iterate-ch/cyberduck/compare/release-8-7-3...release-8-8-0)
* [Localize] Tamil Localization
* [Bugfix] Improved retry and backoff strategy for API errors (Backblaze B2) ([#15601](https://trac.cyberduck.io/ticket/15601))
* [Bugfix] Require signing and encryption by default (SMB) ([#15638](https://trac.cyberduck.io/ticket/15638))
* [Bugfix] Support upload to buckets with object lock enabled (S3) ([#15557](https://trac.cyberduck.io/ticket/15557))
* [Bugfix] Transfer incomplete message when copying between servers ([#15583](https://trac.cyberduck.io/ticket/15583))
* [Bugfix] Unresponsive application when waiting for transfer to cancel on timeout ([#15671](https://trac.cyberduck.io/ticket/15671))

[8.7.3](https://github.com/iterate-ch/cyberduck/compare/release-8-7-2...release-8-7-3)
* [Bugfix] Crash when opening application (Windows) ([#15535](https://trac.cyberduck.io/ticket/15535))

[8.7.2](https://github.com/iterate-ch/cyberduck/compare/release-8-7-1...release-8-7-2)
* [Bugfix] Missing digest header when commiting large file upload (Box) ([#14564](https://trac.cyberduck.io/ticket/14564))
* [Bugfix] Modification date not set in upload (Dropbox) ([#15381](https://trac.cyberduck.io/ticket/15381))
* [Bugfix] Setting modification date truncates file (SMB) ([#15495](https://trac.cyberduck.io/ticket/15495))

[8.7.1](https://github.com/iterate-ch/cyberduck/compare/release-8-7-0...release-8-7-1)
* [Bugfix] Crash when selecting private key outside of ~/.ssh (macOS, Mac App Store)
* [Bugfix] Application scoped bookmark to access file outside of sandbox not saved (macOS, Mac App Store) ([#15250](https://trac.cyberduck.io/ticket/15250))
* [Bugfix] Failure parsing expiration from cached temporary credentials in AWS CLI configuration from AWS SSO (S3) ([#15257](https://trac.cyberduck.io/ticket/15257))
* [Bugfix] Unable to authenticate with S3 (Credentials from AWS Command Line Interface) connection profile (S3) ([#15222](https://trac.cyberduck.io/ticket/15222))
* [Bugfix] Refreshed OAuth tokens not saved in credentials manager (Dropbox, Google Drive, Google Storage, Microsoft OneDrive, SharePoint) ([#15309](https://trac.cyberduck.io/ticket/15309))
* [Bugfix] Failure saving OpenID Connect (OIDC) token in credentials manager (Windows)
* [Bugfix] Failure completing OAuth authentication flow (Dropbox, Microsoft OneDrive, SharePoint) (Windows Store)

[8.7.0](https://github.com/iterate-ch/cyberduck/compare/release-8-6-3...release-8-7-0)
* [Feature] SMB (Server Message Block) protocol support ([#5368](https://trac.cyberduck.io/ticket/5368))
* [Feature] Support to login using temporary credentials from Security Token Service (STS API) using OpenID Connect (OIDC) web identity (S3) ([#13804](https://trac.cyberduck.io/ticket/13804))
* [Feature] Login using connection profile for AWS S3/STS + Google (OIDC) (S3)
* [Feature] Login using connection profile for AWS S3/STS + Azure AD (OIDC) (S3)
* [Feature] Support login using OAuth 2.0 in (ownCloud) ([#14876](https://trac.cyberduck.io/ticket/14876))
* [Feature] Allow to create internal share (ownCloud, Nextcloud) ([#14197](https://trac.cyberduck.io/ticket/14197))
* [Feature] Option to automatically download and install updates (macOS)
* [Feature] Set creation date for uploaded files (Backblaze B2, Box, Google Drive)
* [Bugfix] Refresh temporary access credentials obtained from CLI configuration (S3) ([#10917](https://trac.cyberduck.io/ticket/10917))
* [Bugfix] Cannot reuse nonce for GCM encryption uploading small files (Cryptomator)

[8.6.3](https://github.com/iterate-ch/cyberduck/compare/release-8-6-2...release-8-6-3)
* [Bugfix] Failure configuring credentials from AWS CLI setup (S3) ([#14970](https://trac.cyberduck.io/ticket/14970))
* [Bugfix] Frequent broken pipe errors (FTP) ([#14969](https://trac.cyberduck.io/ticket/14969))

[8.6.2](https://github.com/iterate-ch/cyberduck/compare/release-8-6-1...release-8-6-2)
* [Feature] Allow to open connections without copying authentication code after login (Microsoft OneDrive, SharePoint)
* [Bugfix] Segmented downloads with multiple connections per file causes transfers to fail ([#13374](https://trac.cyberduck.io/ticket/13374))
* [Bugfix] Use recommended part size from authorization response for large file uploads (Backblaze B2) ([#14856](https://trac.cyberduck.io/ticket/14856))
* [Bugfix] Skip determining if directory only contains hidden files (Backblaze B2) ([#14775](https://trac.cyberduck.io/ticket/14775))
* [Bugfix] Does not show folders by default that do not contain a .bzEmpty placeolder file (Backblaze B2) ([#14768](https://trac.cyberduck.io/ticket/14768))
* [Bugfix] Make settings in Preferences → Connection → Timeouts apply for transfers ([#14737](https://trac.cyberduck.io/ticket/14737))
* [Bugfix] Copy or move files between containers (Azure) ([#14826](https://trac.cyberduck.io/ticket/14826))
* [Bugfix] Upload with glob pattern including folders for local source has wrong target on server (CLI) ([#14800](https://trac.cyberduck.io/ticket/14800))
* [Bugfix] Should not attempt to read metadata of existing files with no --preserve flag passed (CLI) ([#14802](https://trac.cyberduck.io/ticket/14802))
* [Bugfix] Resolve tilde prefix in default path to home directory (SFTP)

[8.6.1](https://github.com/iterate-ch/cyberduck/compare/release-8-6-0...release-8-6-1)
* [Bugfix] Parse seconds or milliseconds from Mtime in metadata (S3)

[8.6.0](https://github.com/iterate-ch/cyberduck/compare/release-8-6-9...release-8-6-0)
* [Feature] Add preference "Use Keychain" when unlocking vaults (Cryptomator) ([#14662](https://trac.cyberduck.io/ticket/14662))
* [Bugfix] Slow listing of directory contents (Backblaze B2) ([#14527](https://trac.cyberduck.io/ticket/14527))
* [Bugfix] Set newly calculated nonces and checksum when retrying upload (Cryptomator) ([#14547](https://trac.cyberduck.io/ticket/14547))
* [Bugfix] Allow tilde character in keys (S3, Google Storage) ([#14590](https://trac.cyberduck.io/ticket/14590))
* [Bugfix] Support sso_session configuration directive (S3) ([#14568](https://trac.cyberduck.io/ticket/14568))
* [Bugfix] Allow synchronizing of folders with different folder names as parameters (CLI) ([#13911](https://trac.cyberduck.io/ticket/13911))
* [Bugfix] Only prompt for password when creating share when account is subscribed to professional plan (Dropbox) ([#14581](https://trac.cyberduck.io/ticket/14581))
* [Bugfix] File permission changes not applying recursively (SFTP, FTP) ([#14004](https://trac.cyberduck.io/ticket/14004))
* [Bugfix] No result set when searching for shared files (Google Drive) ([#14612](https://trac.cyberduck.io/ticket/14612))
* [Bugfix] Make Bonjour installation optional (Windows) ([#13416](https://trac.cyberduck.io/ticket/13416))
* [Bugfix] Allow uninstall of Bonjour components (Windows) ([#13416](https://trac.cyberduck.io/ticket/13416))
* [Bugfix] Error parsing MLST response from Serv-U FTP Server (FTP) ([#14652](https://trac.cyberduck.io/ticket/14652))
* [Bugfix] Set timestamps in metadata interoperable with rclone (S3) ([#14639](https://trac.cyberduck.io/ticket/14639))
* [Bugfix] Failure configuring CloudFront distributions for bucket (S3) ([#14669](https://trac.cyberduck.io/ticket/14669))

[8.5.9](https://github.com/iterate-ch/cyberduck/compare/release-8-5-8...release-8-5-9)
* [Bugfix] Requires force quit after opening file multiple times in external editor (macOS) ([#14412](https://trac.cyberduck.io/ticket/14412))

[8.5.8](https://github.com/iterate-ch/cyberduck/compare/release-8-5-7...release-8-5-8)
* [Bugfix] Presigned URL does not consider the server port number (S3) ([#13981](https://trac.cyberduck.io/ticket/13981))
* [Bugfix] File changes in editors are no longer uploaded when file has previously deleted by a save process ([#14455](https://trac.cyberduck.io/ticket/14455))
* [Bugfix] Unable to duplicate files (macOS) ([#14413](https://trac.cyberduck.io/ticket/14413))
* [Bugfix] Failure reverting previous version in vault (Cryptomator, OneDrive, Google Drive, Dropbox) ([#14410](https://trac.cyberduck.io/ticket/14410))
* [Bugfix] Hide directory placeholders only containing hidden files (Backblaze B2) ([#14431](https://trac.cyberduck.io/ticket/14431))

[8.5.7](https://github.com/iterate-ch/cyberduck/compare/release-8-5-6...release-8-5-7)
* [Bugfix] Cannot create or duplicate files with any suffix ([#14263](https://trac.cyberduck.io/ticket/14263))
* [Bugfix] Do not use MLST when running with interoperability connection profile (FTP) ([#14333](https://trac.cyberduck.io/ticket/14333))
* [Bugfix] Error using "Open With" to open file in external editor (Windows) ([#14332](https://trac.cyberduck.io/ticket/14332))
* [Bugfix] Overwriting large file deletes segments after transfer is complete (OpenStack Swift) ([#14326](https://trac.cyberduck.io/ticket/14326))
* [Bugfix] Edit file instantly uploads and overwrites file on server with no change ([#14251](https://trac.cyberduck.io/ticket/14251))
* [Bugfix] The bucket does not allow ACLs error when copying file to bucket with BucketOwnerEnforced ownership controls (S3) ([#14300](https://trac.cyberduck.io/ticket/14300))
* [Bugfix] Allow to copy expiring public links using shared access signature with different expiry dates (Azure) ([#14270](https://trac.cyberduck.io/ticket/14270))
* [Bugfix] Allow to create public links with different expiry dates (OpenStack Swift) ([#14256](https://trac.cyberduck.io/ticket/14256))
* [Bugfix] Review layout of input prompts (Windows)
* [Bugfix] Overwrite prompt does not allow to expand folder (Windows) ([#14287](https://trac.cyberduck.io/ticket/14287))
* [Bugfix] Transfer with Resume or Compare option marked as incomplete when all files can be skipped ([#12998](https://trac.cyberduck.io/ticket/12998))

[8.5.6](https://github.com/iterate-ch/cyberduck/compare/release-8-5-5...release-8-5-6)
* [Bugfix] Support for vaults using GCM encryption (Cryptomator) ([#14207](https://trac.cyberduck.io/ticket/14207))
* [Bugfix] Recursive search displaying folders not containing files matching the pattern ([#13925](https://trac.cyberduck.io/ticket/13925))
* [Bugfix] Copying or moving files with two browser windows may not use server side operation ([#14084](https://trac.cyberduck.io/ticket/14084))
* [Bugfix] Lazily calculate pre-signed URLs (S3, Openstack Swift, Azure)

[8.5.5](https://github.com/iterate-ch/cyberduck/compare/release-8-5-4...release-8-5-5)
* [Bugfix] Save OAuth tokens with username to allow connecting to different accounts (Google Drive, Google Storage, OneDrive, Dropbox, Box)

[8.5.4](https://github.com/iterate-ch/cyberduck/compare/release-8-5-3...release-8-5-4)
* [Bugfix] Failure authorizing using OAuth with no desktop browser installed (Linux) ([#14028](https://trac.cyberduck.io/ticket/14028))
* [Bugfix] Allow to create new buckets in regions ap-south-2, ap-southeast-3, eu-south-2, eu-central-2, me-central-1 (S3) ([#14045](https://trac.cyberduck.io/ticket/14045))
* [Bugfix] Allow custom path for IdentityAgent configuration (SFTP, Windows) ([#13933](https://trac.cyberduck.io/ticket/13933))

[8.5.3](https://github.com/iterate-ch/cyberduck/compare/release-8-5-2...release-8-5-3)
* [Bugfix] Failure uploading file with diacritic in filename (Windows) ([#13723](https://trac.cyberduck.io/ticket/13723))
* [Bugfix] Folders containing backup file dirid.c9r cannot be deleted (Cryptomator) ([#13957](https://trac.cyberduck.io/ticket/13957))
* [Bugfix] Files not decrypted after unlocking vault (Cryptomator, Box) ([#13961](https://trac.cyberduck.io/ticket/13961))
* [Bugfix] Only read specific key from SSH agent with IdentitiesOnly and public key in IdentitiyFile directive in OpenSSH configuration to limit number of authentication attempts (SFTP) ([#13935](https://trac.cyberduck.io/ticket/13935))
* [Bugfix] Interoperability with IdentityAgent from 1Password (SFTP)

[8.5.2](https://github.com/iterate-ch/cyberduck/compare/release-8-5-1...release-8-5-2)
* [Bugfix] Invalid syntax in Open in Terminal command (SFTP, macOS)
* [Bugfix] Passwords for ProxyJump hosts in .ssh/config are not saved (SFTP) ([#13936](https://trac.cyberduck.io/ticket/13936))
* [Bugfix] Files in vault not decrypted (Cryptomator, Google Storage) ([#13949](https://trac.cyberduck.io/ticket/13949))

[8.5.1](https://github.com/iterate-ch/cyberduck/compare/release-8-5-0...release-8-5-1)
* [Feature] Skip button to allow creating share with no passphrase (Dropbox, Box, ownCloud, Nextcloud) ([#13846](https://trac.cyberduck.io/ticket/13846))
* [Feature] Redirect to application without requiring to copy authentication code when opening new connection (Dropbox)
* [Feature] Allow configuration of client certificate (ownCloud, Nextcloud)
* [Feature] Use Authorization Code Flow with Proof Key for Code Exchange (PKCE) (Google Storage, Google Drive, Dropbox)
* [Bugfix] Mutual authentication does not work with client certificates with a friendly name set (Windows)

[8.5.0](https://github.com/iterate-ch/cyberduck/compare/release-8-4-5...release-8-5-0)
* [Feature] Allow to view and revert previous versions of files (ownCloud, Nextcloud) ([#10560](https://trac.cyberduck.io/ticket/10560))
* [Feature] Allow to request files with upload share link (Dropbox) ([#13832](https://trac.cyberduck.io/ticket/13832))
* [Bugfix] Try keyboard-interactive authentication always before password for interoperability with 2FA configurations (SFTP)
* [Bugfix] Connect to custom hostname from pairing response (Files.com)
* [Bugfix] Failure creating upload link to request files (Nextcloud) ([#13791](https://trac.cyberduck.io/ticket/13791))

[8.4.5](https://github.com/iterate-ch/cyberduck/compare/release-8-4-4...release-8-4-5)
* [Bugfix] Failure enabling connection profile from Preferences ([#13739](https://trac.cyberduck.io/ticket/13739))
* [Bugfix] Only set userProject parameter in requests for buckets with requester pays option enabled (Google Storage) ([#13745](https://trac.cyberduck.io/ticket/13745))
* [Bugfix] Allow glob pattern when searching for files in browser ([#13781](https://trac.cyberduck.io/ticket/13781))

[8.4.4](https://github.com/iterate-ch/cyberduck/compare/release-8-4-3...release-8-4-4)
* [Bugfix] Prioritise password authentication if password is available (SFTP) ([#13442](https://trac.cyberduck.io/ticket/13442))
* [Bugfix] Filter identities in SSH agent based on private key selection in bookmark (SFTP) ([#13680](https://trac.cyberduck.io/ticket/13680))
* [Bugfix] Switch OAuth registration with redirect URIs shorter than the allowed maximum of 32 characters in Windows Store (Google Storage, Google Drive) ([#13695](https://trac.cyberduck.io/ticket/13695))
* [Bugfix] Save OAuth expiry in keychain instead of preferences ([#13710](https://trac.cyberduck.io/ticket/13710))

[8.4.3](https://github.com/iterate-ch/cyberduck/compare/release-8-4-2...release-8-4-3)
* [Bugfix] Repeated prompt to verify server fingerprint (SFTP, Windows) ([#13638](https://trac.cyberduck.io/ticket/13638))
* [Bugfix] OAuth token not saved in credentials manager (Dropbox, Windows) ([#13613](https://trac.cyberduck.io/ticket/13613))
* [Bugfix] Malformed authorization header with wrong region when browsing buckets in multiple regions (S3)
* [Bugfix] Lower memory footprint regardless of available memory on system (macOS)

[8.4.2](https://github.com/iterate-ch/cyberduck/compare/release-8-4-1...release-8-4-2)
* [Bugfix] Setting to always open default editor application set in Preferences (Windows) ([#13546](https://trac.cyberduck.io/ticket/13546))
* [Bugfix] Unable to complete OAuth flow with missing scheme handler registered when running in sandbox (Google Storage, Google Drive) (Mac App Store)

[8.4.1](https://github.com/iterate-ch/cyberduck/compare/release-8-4-0...release-8-4-1)
* [Bugfix] Crash running on versions prior macOS 11 ([#13521](https://trac.cyberduck.io/ticket/13521))

[8.4.0](https://github.com/iterate-ch/cyberduck/compare/release-8-3-3...release-8-4-0)
* [Feature] Revert previous file versions in Info panel (S3, Backblaze B2, Dropbox, Google Storage, OneDrive, Microsoft SharePoint)
* [Feature] Delete previous file versions in Info panel (S3, Backblaze B2, Google Drive, Google Storage)
* [Feature] View previous file versions in Info panel (S3, Backblaze B2, Dropbox, Google Drive, Google Storage, OneDrive, Microsoft SharePoint)
* [Feature] View previous versions in browser by choosing View → Show Hidden (Google Drive)
* [Feature] Support applications installed from Windows Store for editing files ([#12802](https://trac.cyberduck.io/ticket/12802)) (Windows)
* [Feature] No length limitation when storing passwords in Windows Credential manager ([#12803](https://trac.cyberduck.io/ticket/12803)) (Windows)
* [Feature] Migrate OAuth out-of-band flow to an alternative method ([#13360](https://trac.cyberduck.io/ticket/13360)) (Google Drive, Google Storage)
* [Feature] Add menu item to request files for protocols that support upload share links ([#13426](https://trac.cyberduck.io/ticket/13426)) (Box, Nextcloud, ownCloud)
* [Feature] Include region identifier in region selection when creating new bucket (S3) ([#13501](https://trac.cyberduck.io/ticket/13501))
* [Bugfix] Do not attempt to set ACL on files uploaded to bucket with owner controls set that disallows ACLs (S3) ([#13386](https://trac.cyberduck.io/ticket/13386))
* [Bugfix] Register custom DNS resolver to handle IPv6 preference ([#13399](https://trac.cyberduck.io/ticket/13399))
* [Bugfix] Failure opening vault (Cryptomator, FTP) ([#13375](https://trac.cyberduck.io/ticket/13375))
* [Bugfix] Mismatch between SHA256 hash error when downloading files (Dropbox) ([#13361](https://trac.cyberduck.io/ticket/13361))
* [Bugfix] No such file error when uploading with temporary filename option (SFTP) ([#13367](https://trac.cyberduck.io/ticket/13367))
* [Bugfix] Transfer acceleration endpoints not used (S3) ([#13359](https://trac.cyberduck.io/ticket/13359))
* [Bugfix] Delete multiple files with batch request (DRACOON)
* [Bugfix] RequestTimeTooSkewed error with computer date settings not using Gregorian calendar (S3) ([#13454](https://trac.cyberduck.io/ticket/13454))
* [Bugfix] Fix error reporting to display exact parser failure for unexpected response contents (S3) ([#13383](https://trac.cyberduck.io/ticket/13383))
* [Bugfix] Invalid hostname used when completing multipart upload when connecting to single bucket (S3)
* [Bugfix] Unable to delete object version created prior enabling file versioning for bucket (S3) ([#13507](https://trac.cyberduck.io/ticket/13507))
* [Bugfix] Bucket versioning checkbox should reflect lifecycle configuration (Backblaze B2) ([#13505](https://trac.cyberduck.io/ticket/13505))

[8.3.3](https://github.com/iterate-ch/cyberduck/compare/release-8-3-2...release-8-3-3)
* [Bugfix] Failing transfers with multiple files (FTP) ([#13322](https://trac.cyberduck.io/ticket/13322))

[8.3.2](https://github.com/iterate-ch/cyberduck/compare/release-8-3-1...release-8-3-2)
* [Bugfix] Connection not released causing freeze in browser or transfer (FTP) ([#13273](https://trac.cyberduck.io/ticket/13273))

[8.3.1](https://github.com/iterate-ch/cyberduck/compare/release-8-3-0...release-8-3-1)
* [Bugfix] Failure reconnecting when control connection is closed on server (FTP) ([#13037](https://trac.cyberduck.io/ticket/13037))
* [Bugfix] Set role session name from configuration when connecting using credentials from AWS Command Line Interface (S3)
* [Bugfix] Hide file when deleting latest version to allow later restore (Backblaze B2)

[8.3.0](https://github.com/iterate-ch/cyberduck/compare/release-8-2-3...release-8-3-0)
* [Bugfix] Enable authentication using OpenSSH agent on Windows (SFTP, Windows) ([#12880](https://trac.cyberduck.io/ticket/12880))
* [Bugfix] File edited moved to trash and no longer updated on server ([#11729](https://trac.cyberduck.io/ticket/11729))
* [Bugfix] Failure copying or renaming file when bucket name is set in hostname using virtual host style (S3)
* [Bugfix] Failure deleting file (Google Drive, Cryptomator)
* [Bugfix] Failures when deleting folder with no placeholder (Azure) ([#12900](https://trac.cyberduck.io/ticket/12900))
* [Bugfix] Batch requests when deleting multiple files (Dropbox) ([#12904](https://trac.cyberduck.io/ticket/12904))
* [Bugfix] Deleting many files may fail with 413 Request Entity Too Large (Google Drive) ([#12902](https://trac.cyberduck.io/ticket/12902))
* [Bugfix] IPv6 not working for hosts with both A and AAAA entries ([#12917](https://trac.cyberduck.io/ticket/12917))
* [Bugfix] Resolve tilde in IdentityAgent configuration (SFTP) ([#12954](https://trac.cyberduck.io/ticket/12954))
* [Bugfix] Missing download overwrite prompt ([#12860](https://trac.cyberduck.io/ticket/12860))
* [Bugfix] Compose segments of download very slow after downloading is slow ([#12996](https://trac.cyberduck.io/ticket/12996))
* [Bugfix] Trim whitespace from input in username and password fields ([#12986](https://trac.cyberduck.io/ticket/12986))
* [Bugfix] Missing Glacier Instant Retrieval storage class option (S3) ([#12915](https://trac.cyberduck.io/ticket/12915))

[8.2.3](https://github.com/iterate-ch/cyberduck/compare/release-8-2-2...release-8-2-3)
* [Bugfix] When creating new vault save vault.cryptomator to make vaults readable by Cryptomator apps (Cryptomator)
* [Bugfix] Unable to download files using glob pattern (CLI) ([#12797](https://trac.cyberduck.io/ticket/12797))
* [Bugfix] Failure unlocking vault (CLI, Cryptomator, Windows) ([#12812](https://trac.cyberduck.io/ticket/12812))

[8.2.2](https://github.com/iterate-ch/cyberduck/compare/release-8-2-1...release-8-2-2)
* [Bugfix] Add support reading IdentityAgent from OpenSSH configuration (SFTP)
* [Bugfix] Unable to enable debug log configuration (Windows)

[8.2.1](https://github.com/iterate-ch/cyberduck/compare/release-8-2-0...release-8-2-1)
* [Bugfix] Try all public key algorithms available for a specific key type (SFTP) ([#12733](https://trac.cyberduck.io/ticket/12733))
* [Bugfix] Add fallback when location query for endpoint URI fails (S3) ([#12723](https://trac.cyberduck.io/ticket/12723))
* [Bugfix] Failure connecting to bucket with missing virtual host style request support (S3) ([#12746](https://trac.cyberduck.io/ticket/12746))
* [Bugfix] Fix display scaling issues (Windows) ([#12742](https://trac.cyberduck.io/ticket/12742))

[8.2.0](https://github.com/iterate-ch/cyberduck/compare/release-8-1-0...release-8-2-0)
* [Feature] Support for Box API (Box) ([#10235](https://trac.cyberduck.io/ticket/10235))
* [Bugfix] Failure opening connection (FTP-SSL, Windows)
* [Bugfix] Interoperability with SharePoint Site (Microsoft SharePoint)
* [Bugfix] Compare option for existing files in transfer fails to verify checksums ([#12688](https://trac.cyberduck.io/ticket/12688))
* [Bugfix] Failure uploading to folders in vault (Cryptomator, CLI) ([#11881](https://trac.cyberduck.io/ticket/11881))

[8.1.0](https://github.com/iterate-ch/cyberduck/compare/release-8-0-1...release-8-0-2)
* [Feature] Native support for Apple silicon ([#11101](https://trac.cyberduck.io/ticket/11101))

[8.0.2](https://github.com/iterate-ch/cyberduck/compare/release-8-0-1...release-8-0-1)
* [Bugfix] Replacing file may cause empty permission set in ACL (S3)
* [Bugfix] Failure authentication with PuTTY private key (SFTP) ([#11887](https://trac.cyberduck.io/ticket/11887))
* [Bugfix] Set storage class for transition to "ARCHIVE" when editing lifecycle configuration (Google Cloud Storage) ([#11906](https://trac.cyberduck.io/ticket/11906))
* [Bugfix] Deleted folders still displayed with versioning enabled on bucket (Google Cloud Storage) ([#11904](https://trac.cyberduck.io/ticket/11904))

[8.0.1](https://github.com/iterate-ch/cyberduck/compare/release-8-0-0...release-8-0-0)
* [Bugfix] Interoperability with servers not supporting Range header in requests (WebDAV)

[8.0.0](https://github.com/iterate-ch/cyberduck/compare/release-7-10-2...release-release-7-10-2)
* [Feature] Allow to manage additional connection profiles in Preferences ([#10823](https://trac.cyberduck.io/ticket/10823))
* [Feature] New default connection profile for ownCloud
* [Feature] Retain modification date in metadata for uploads (Google Cloud Storage) ([#11784](https://trac.cyberduck.io/ticket/11784))
* [Feature] Retain modification date in metadata for uploads (S3) ([#11784](https://trac.cyberduck.io/ticket/11784))
* [Feature] Build package in AppImage format (CLI) ([#11762](https://trac.cyberduck.io/ticket/11762))
* [Feature] Support for PuTTY v3 key files (SFTP)
* [Feature] Support public key authentication using certificates (SFTP)
* [Feature] Support for PEM ASN.1 encoded private keys (SFTP)
* [Feature] Support for host certificate keys handling @cert-authority in known_hosts (SFTP)
* [Feature] Column in browser window to display checksum ([#11824](https://trac.cyberduck.io/ticket/11824))
* [Bugfix] Change default path for NextCloud connections to include username ([#11787](https://trac.cyberduck.io/ticket/11787))
* [Bugfix] Failed to overwrite upload of object with custom metadata (Google Cloud Storage) ([#11781](https://trac.cyberduck.io/ticket/11781))
* [Bugfix] Skip duplicate file versions in output (Cyberduck CLI) ([#11786](https://trac.cyberduck.io/ticket/11786))
* [Bugfix] Fails to use selected private key file outside of ~/.ssh folder (SFTP, Mac App Store) ([#11782](https://trac.cyberduck.io/ticket/11782))
* [Bugfix] Unable to delete folders (Google Cloud Storage) ([#11808](https://trac.cyberduck.io/ticket/11808))
* [Bugfix] Allow to set Glacier storage class (Google Cloud Storage) ([#11521](https://trac.cyberduck.io/ticket/11521))
* [Bugfix] Unable to remove delete marker (S3) ([#11803](https://trac.cyberduck.io/ticket/11803))
* [Bugfix] Unable to download previous version of file (S3, Backblaze B2) (#11797, #11835)
* [Bugfix] Copying files requires permission to read bucket ACL (S3) ([#11701](https://trac.cyberduck.io/ticket/11701))
* [Bugfix] Unable to set storage class to STANDARD (S3) ([#11849](https://trac.cyberduck.io/ticket/11849))
* [Bugfix] Taskbar shows incorrect number of transfers (Windows) ([#11813](https://trac.cyberduck.io/ticket/11813))
* [Bugfix] Allow to delete trashed files (Google Drive) ([#11816](https://trac.cyberduck.io/ticket/11816))
* [Bugfix] Create directory placeholders using default storage class (S3) ([#11751](https://trac.cyberduck.io/ticket/11751))
* [Bugfix] Create master key using default storage class (S3, Cryptomator) ([#11751](https://trac.cyberduck.io/ticket/11751))
* [Bugfix] Use specific region endpoint only with --region option (S3, CLI) ([#11827](https://trac.cyberduck.io/ticket/11827))
* [Bugfix] Empty list output when listing root directory (CLI) ([#11814](https://trac.cyberduck.io/ticket/11814))

[7.10.2](https://github.com/iterate-ch/cyberduck/compare/release-7-10-1...release-release-7-10-1)
* [Feature] Add --purge option to invalidate files in Amazon CloudFront distributions and OpenStack Swift (X-CDN-Enabled header) ([#11767](https://trac.cyberduck.io/ticket/11767))
* [Bugfix] Links for CloudFront distributions and S3 website endpoints are missing in the Copy URL and Open URL menu (S3) ([#11764](https://trac.cyberduck.io/ticket/11764))
* [Bugfix] Failure downloading files (Files.com)
* [Bugfix] Return existing share URL when found (Dropbox) ([#11209](https://trac.cyberduck.io/ticket/11209))
* [Bugfix] Select origin bucket for logging target with missing permission to list all buckets (S3) ([#17755](https://trac.cyberduck.io/ticket/17755))
* [Bugfix] Unable to upload to buckets with uniform bucket-level access enabled (Google Cloud Storage) ([#11766](https://trac.cyberduck.io/ticket/11766))
* [Bugfix] Missing progress information when copying file ([#11019](https://trac.cyberduck.io/ticket/11019))

[7.10.1](https://github.com/iterate-ch/cyberduck/compare/release-7-10-0...release-release-7-10-0)
* [Feature] Support S3 interface endpoints (AWS PrivateLink for Amazon S3) ([#11735](https://trac.cyberduck.io/ticket/11735))
* [Feature] Updated support for Files.com using proprietary REST API (Files.com)
* [Feature] Allow override of protocol specific settings in connection profiles
* [Bugfix] Unable to access team drive set as default path in bookmark (Google Drive)
* [Bugfix] Login failure with file transfers using multiple connections (FTP)
* [Bugfix] Downloads from HTTP server with no WebDAV extension support (CLI) ([#11727](https://trac.cyberduck.io/ticket/11727))

[7.10.0](https://github.com/iterate-ch/cyberduck/compare/release-7-9-2...release-7-9-2)
* [Feature] Preference to set default storage class (Google Cloud Storage) ([#11521](https://trac.cyberduck.io/ticket/11521))
* [Feature] Preference to set default bucket region (Google Cloud Storage)
* [Feature] Preference to set predefined set of grantees and permissions for new files (S3, Google Cloud Storage)
* [Feature] Support to set predefined ACL bucket-owner-full-control (S3, Google Cloud Storage) ([#11697](https://trac.cyberduck.io/ticket/11697))
* [Feature] Context menu in browser table header to select columns (macOS) ([#11718](https://trac.cyberduck.io/ticket/11718))
* [Feature] Allow selection of region when creating new bucket for third party S3 providers
* [Bugfix] Recursive search yields no results ([#11424](https://trac.cyberduck.io/ticket/11424))
* [Bugfix] Canceled upload to a vault can result in an undeletable folder (Cryptomator) ([#11696](https://trac.cyberduck.io/ticket/11696))
* [Bugfix] Updated .NET Framework Installer (Windows)

[7.9.2](https://github.com/iterate-ch/cyberduck/compare/release-7-9-1...release-7-9-2)
* [Bugfix] Failure opening application with previous application support directory symbolic link (macOS)

[7.9.1](https://github.com/iterate-ch/cyberduck/compare/release-7-9-0...release-7-9-1)
* [Bugfix] Slow synchronisation of a large folder ([#11676](https://trac.cyberduck.io/ticket/11676))
* [Bugfix] Slow tranfer when using "compare" option for existing files ([#11679](https://trac.cyberduck.io/ticket/11679))
* [Bugfix] Downloads fail with error "The specified key does not exist" (S3) ([#11670](https://trac.cyberduck.io/ticket/11670))

[7.9.0](https://github.com/iterate-ch/cyberduck/compare/release-7-8-5...release-7-9-0)
* [Feature] New Big Sur style application icon (macOS)
* [Feature] New "Auto" default option for number of connections for transfers
* [Feature] Increased default to 5 concurrent connections for transfers for protocols other than FTP
* [Feature] Allow toggling versioning configuration for bucket (Google Cloud Storage)
* [Feature] Display and restore of previous file versions in bucket (Google Cloud Storage)
* [Feature] Support chacha20-poly1305@openssh.com cipher (SFTP) ([#8554](https://trac.cyberduck.io/ticket/8554))
* [Feature] Share file option to set public-read ACL on file and copy URL (S3, Google Cloud Storage)
* [Feature] Tremendously faster uploading folders with many files to vault (Cryptomator, OneDrive, Google Drive, Backblaze B2) ([#10849](https://trac.cyberduck.io/ticket/10849))
* [Feature] Substantially faster uploading folders with many files (OneDrive, Google Drive, Backblaze B2)
* [Bugfix] Maximum number of simultaneous transfers not handled properly ([#11001](https://trac.cyberduck.io/ticket/11001))
* [Bugfix] Interoperability with China region operated by 21Vianet (OneDrive, Sharepoint) ([#11415](https://trac.cyberduck.io/ticket/11415))
* [Bugfix] Support Elliptic Curve (EC) based client certificates for authentication (WebDAV, Windows)
* [Bugfix] Interoperability with OpenSSH for Windows (SFTP)
* [Bugfix] Segmented download fail with longer filenames (Windows)
* [Bugfix] Bezel style for popup buttons in window toolbars (macOS)

[7.8.5](https://github.com/iterate-ch/cyberduck/compare/release-7-8-4...release-7-8-5)
* [Feature] Support ED25519 and ECDSA keys in the PuTTY format (SFTP)
* [Bugfix] Large increasing memory usage when downloading files with segmeted downloads option ([#11151](https://trac.cyberduck.io/ticket/11151))
* [Bugfix] Faster transfer rate with segmented downloads option enabled (SFTP)
* [Bugfix] Allow revert and restore of multiple files (S3)

[7.8.4](https://github.com/iterate-ch/cyberduck/compare/release-7-8-3...release-7-8-4)
* [Bugfix] Missing log file configuration (Windows)

[7.8.3](https://github.com/iterate-ch/cyberduck/compare/release-7-8-2...release-7-8-3)
* [Bugfix] Failure renaming files (OneDrive, Sharepoint)
* [Bugfix] Optimize segmented downloads
* [Bugfix] Allow resume of segmented downloads
* [Bugfix] Ignore unavailable regions when listing containers (OpenStack Swift)
* [Bugfix] Missing default connection profile (Nextcloud & ownCloud)
* [Bugfix] Missing default connection profile (Microsoft SharePoint Site)
* [Bugfix] Missing default connection profile (Files.com)

[7.8.2](https://github.com/iterate-ch/cyberduck/compare/release-7-8-1...release-7-8-2)
* [Feature] Updated application icon when running on macOS Big Sur
* [Bugfix] Failure uploading large files with S3 compatible API (Backblaze B2) ([#11233](https://trac.cyberduck.io/ticket/11233))
* [Bugfix] Failure uploading large using legacy S3 compatible API (Google Cloud Storage) ([#11547](https://trac.cyberduck.io/ticket/11547))
* [Bugfix] MD5 mismatch error for uploads to Oracle Object Storage replacing object ([#11548](https://trac.cyberduck.io/ticket/11548))
* [Bugfix] Missing empty prefix parameter leads to permission error with IAM policy containing restriction on prefix ([#11549](https://trac.cyberduck.io/ticket/11549))
* [Bugfix] Interoperability with Oracle Object Storage using path style requests (#10956, #11548)
* [Bugfix] Failure listing files with shortcut pointing to deleted file (Google Drive)
* [Bugfix] Allow configuration of path for Nextcloud & ownCloud connections ([#11540](https://trac.cyberduck.io/ticket/11540))
* [Bugfix] Failure unlocking vaults on Google Cloud Storage (Cryptomator) ([#11528](https://trac.cyberduck.io/ticket/11528))
* [Bugfix] Fix SSL session reuse on data channel (FTP, Windows)
* [Bugfix] Failure running application (CLI, Linux)
* [Bugfix] Recursive search yields no results (Google Drive) ([#11424](https://trac.cyberduck.io/ticket/11424))

[7.8.1](https://github.com/iterate-ch/cyberduck/compare/release-7-8-1...release-7-8-1)
* [Feature] Provide armhf/aarch64 RPM and DEB packages (CLI, Rasperry Pi, Linux) ([#10447](https://trac.cyberduck.io/ticket/10447))
* [Bugfix] Missing folers in directory listing (OneDrive, Sharepoint)
* [Bugfix] Interoperability with Tencent Cloud Object Storage (S3)
* [Bugfix] No file size displayed for files (Google Drive)

[7.8.0](https://github.com/iterate-ch/cyberduck/compare/release-7-7-2...release-7-8-0)
* [Feature] Connecting to different libraries (SharePoint) ([#11043](https://trac.cyberduck.io/ticket/11043))
* [Feature] Default connection profile (Microsoft SharePoint Site) to connect so specific site (Sharepoint) (#10115, #11375)
* [Feature] Create shared link for file (OneDrive, SharePoint) ([#11373](https://trac.cyberduck.io/ticket/11373))
* [Feature] Updated toolbar icons in Preferencs window (macOS)
* [Bugfix] No attempt to authenticate using SSH agent (SFTP)
* [Bugfix] Allow browsing filenames containing forward slash (Google Drive)
* [Bugfix] Allow accessing shortcuts for files and folders (Google Drive)

[7.7.2](https://github.com/iterate-ch/cyberduck/compare/release-7-7-1...release-7-7-2)
* [Feature] Allow custom session duration for STS (S3) ([#11265](https://trac.cyberduck.io/ticket/11265))
* [Feature] Support for PreferredAuthentications in OpenSSH configuration (SFTP) ([#9964](https://trac.cyberduck.io/ticket/9964))
* [Bugfix] Exception reordering of bookmarks with drag and drop (macOS) ([#11242](https://trac.cyberduck.io/ticket/11242))
* [Bugfix] Crash opening QuickLook panel (macOS) ([#11212](https://trac.cyberduck.io/ticket/11212))
* [Bugfix] Temporary local file deleted after editing in external editor ([#11086](https://trac.cyberduck.io/ticket/11086))
* [Bugfix] Include port number when saving host key fingerprint (SFTP, Windows) ([#11255](https://trac.cyberduck.io/ticket/11255))

[7.7.1](https://github.com/iterate-ch/cyberduck/compare/release-7-7-0...release-7-7-1)
* [Bugfix] Workaround for crash on macOS 11 for some users ([#11231](https://trac.cyberduck.io/ticket/11231))
* [Bugfix] Failure connecting through jump server with alias in configuration for target host ([#11227](https://trac.cyberduck.io/ticket/11227))
* [Bugfix] Missing search field on macOS prior version 10.15 ([#11234](https://trac.cyberduck.io/ticket/11234))

[7.7.0](https://github.com/iterate-ch/cyberduck/compare/release-7-6-2...release-7-7-0)
* [Feature] Connect via SSH tunnel through bastion host with JumpHost configuration directive (SFTP) ([#2865](https://trac.cyberduck.io/ticket/2865))
* [Feature] Bookmark toggle control and search field moved to toolbar (macOS)
* [Bugfix] Accessing CloudFront and KMS configuration ignores ~/.aws/credentials ([#11175](https://trac.cyberduck.io/ticket/11175))
* [Bugfix] Failure deleting folder placeholder in versioned bucket (S3) ([#11157](https://trac.cyberduck.io/ticket/11157))
* [Bugfix] Server connection timeout when moving or duplicating large files (Backblaze B2) ([#11185](https://trac.cyberduck.io/ticket/11185))
* [Bugfix] Certificate trust prompt regardless of override in keychain (macOS) ([#11118](https://trac.cyberduck.io/ticket/11118))
* [Bugfix] Failure saving credentials (CLI, Windows) ([#11098](https://trac.cyberduck.io/ticket/11098))
* [Bugfix] Unable to connect to cn-north-1 (S3) ([#11197](https://trac.cyberduck.io/ticket/11197))
* [Bugfix] Access shared files (OneDrive Business) ([#11102](https://trac.cyberduck.io/ticket/11102))
* [Bugfix] Read external_id from AssumeRole configuration in ~/.aws/credentials (S3) ([#11229](https://trac.cyberduck.io/ticket/11229))

[7.6.2](https://github.com/iterate-ch/cyberduck/compare/release-7-6-1...release-7-6-2)
* [Feature] Allow to set password for shared link of file (Dropbox, Nextcloud, DRACOON)
* [Bugfix] Missing folders with delete marker on placeholder for buckets with versioning enabled (S3)

[7.6.1](https://github.com/iterate-ch/cyberduck/compare/release-7-6-0...release-7-6-1)
* [Bugfix] Unable to authenticate using PuTTY Pageant (SFTP, Windows)
* [Bugfix] Set modification date on uploaded files (DRACOON)
* [Bugfix] Failure authenticating with proxy using Integrated Windows Authentication (IWA) authentication (WebDAV, Windows)

[7.6.0](https://github.com/iterate-ch/cyberduck/compare/release-7-5-1...release-7-6-0)
* [Feature] Lock and unlock vaults using option in menu or toolbar (Cryptomator) ([#10798](https://trac.cyberduck.io/ticket/10798))
* [Feature] Support for Files.com using native REST API (Files.com)
* [Feature] Restore files in Glacier storage class (S3)
* [Feature] Support for AES/GCM (aes128-gcm@openssh.com, aes256-gcm@openssh.com) ciphers (SFTP) ([#9809](https://trac.cyberduck.io/ticket/9809))

[7.5.1](https://github.com/iterate-ch/cyberduck/compare/release-7-5-0...release-7-5-1)
* [Bugfix] Changes not saved when editing bookmark (Windows)
* [Bugfix] Failure running application (CLI, macOS) ([#11139](https://trac.cyberduck.io/ticket/11139))
* [Bugfix] Navigation buttons in browser window do not work (macOS) ([#11140](https://trac.cyberduck.io/ticket/11140))
* [Bugfix] Upload files using S3 direct upload to encrypted rooms (DRACOON)
* [Bugfix] Upload files to encrypted rooms (CLI, DRACOON)

[7.5.0](https://github.com/iterate-ch/cyberduck/compare/release-7-4-1...release-7-5-0)
* [Feature] Interoperability with macOS 11
* [Feature] Column for storage class in browser (S3)
* [Bugfix] Use STANDARD for default storage class (Google Cloud Storage) ([#11062](https://trac.cyberduck.io/ticket/11062))
* [Bugfix] Switch to UTF-8 for encoding HTTP authentication header (WebDAV)
* [Bugfix] SNI support for TLS connections (FTP) ([#9257](https://trac.cyberduck.io/ticket/9257))
* [Bugfix] Handle formatting errors in ~/.aws/credentials (S3)
* [Bugfix] Crash accessing files with colon in name (Windows) ([#11075](https://trac.cyberduck.io/ticket/11075))
* [Bugfix] Support Team Drives for sharing files (Google Drive)

[7.4.1](https://github.com/iterate-ch/cyberduck/compare/release-7-4-0...release-7-4-1)
* [Localize] Croatian Localization
* [Bugfix] Unable to use S3 HTTP connection profile (S3) ([#11061](https://trac.cyberduck.io/ticket/11061))
* [Bugfix] Failure uploading files with exactly 100MB file length (S3) ([#11038](https://trac.cyberduck.io/ticket/11038))
* [Bugfix] Failure opening Terminal.app when default application for .command files is not properly set (SFTP)

[7.4.0](https://github.com/iterate-ch/cyberduck/compare/release-7-3-1...release-7-4-0)
* [Feature] Support to access team folders (Dropbox Business)
* [Feature] Support for eu-south-1 (Milan) region (S3)
* [Feature] Support for af-south-1 (Cape Town) region (S3)
* [Feature] Support for ap-east-1 (Hong Kong) region (S3)
* [Feature] Support for me-south-1 (Bahrain) region (S3)
* [Feature] Making requests to dual-stack endpoints (IPv6) (S3)
* [Feature] Create new vaults in format 7 by default (Cryptomator) ([#11040](https://trac.cyberduck.io/ticket/11040))
* [Bugfix] Slow updating files after saving changes in external editor ([#11004](https://trac.cyberduck.io/ticket/11004))

[7.3.1](https://github.com/iterate-ch/cyberduck/compare/release-7-3-0...release-7-3-1)
* [Feature] Edit ACLs for files (Google Cloud Storage) ([#11008](https://trac.cyberduck.io/ticket/11008))
* [Feature] Support to set storage class (Google Cloud Storage)
* [Feature] Support for Intelligent-Tiering storage class (S3) ([#11031](https://trac.cyberduck.io/ticket/11031))
* [Feature] Support for Glacier Deep Archive storage class (S3) ([#10681](https://trac.cyberduck.io/ticket/10681))
* [Feature] Allow selection of Glacier storage class as default for uploads (S3) ([#10681](https://trac.cyberduck.io/ticket/10681))
* [Feature] Support for shared files (OneDrive)
* [Feature] Support to access shared files (OneDrive)
* [Bugfix] Missing URLs to copy of CDN distributions (S3, Rackspace)
* [Bugfix] Reduced memory usage (macOS)
* [Bugfix] Failure uploading files to vault in version 7 format (Cryptomator) ([#11020](https://trac.cyberduck.io/ticket/11020))
* [Bugfix] Interoperability with Minio (S3) ([#11018](https://trac.cyberduck.io/ticket/11018))

[7.3.0](https://github.com/iterate-ch/cyberduck/compare/release-7-2-6...release-7-3-0)
* [Feature] Support for vault format version 7 (Cryptomator) ([#10825](https://trac.cyberduck.io/ticket/10825))
* [Feature] Labels for bookmarks (macOS) ([#9719](https://trac.cyberduck.io/ticket/9719))
* [Feature] Group bookmarks by labels in menu (macOS)
* [Feature] Support TLS 1.3 ([#10962](https://trac.cyberduck.io/ticket/10962))
* [Bugfix] Download fails for files with whitespace in name (Google Cloud Storage) ([#10931](https://trac.cyberduck.io/ticket/10931))
* [Bugfix] Unable to access documents in Shared with me (Google Drive)
* [Bugfix] Read timeout after copying large file (OpenStack Swift)
* [Bugfix] Repeated prompt for authorization code (hubiC)
* [Bugfix] Immediate retry for failure with cached upload channel (Backblaze B2)
* [Bugfix] Set timestamp with X-OC-Mtime header (Nextcloud, ownCloud)
* [Bugfix] Copy files to different bucket (Backblaze B2) ([#10924](https://trac.cyberduck.io/ticket/10924))
* [Bugfix] Allow downloading files marked as abusive (Google Drive) ([#10377](https://trac.cyberduck.io/ticket/10377))
* [Bugfix] Apply public read permissions to files selected to share (Google Drive)

[7.2.6](https://github.com/iterate-ch/cyberduck/compare/release-7-2-5...release-7-2-6)
* [Feature] Importer for bookmarks from Transmit 5 (macOS)
* [Feature] Importer for bookmarks from Cloudmounter (macOS)
* [Bugfix] Copy files to different bucket (Backblaze B2) ([#10924](https://trac.cyberduck.io/ticket/10924))

[7.2.5](https://github.com/iterate-ch/cyberduck/compare/release-7-2-4...release-7-2-5)
* [Bugfix] Error downloading files from Microsoft SharePoint (SharePoint Server 2016)

[7.2.4](https://github.com/iterate-ch/cyberduck/compare/release-7-2-3...release-7-2-4)
* [Bugfix] Certificate trust validation error (Google Drive, macOS)

[7.2.3](https://github.com/iterate-ch/cyberduck/compare/release-7-2-2...release-7-2-3)
* [Bugfix] Cannot open link to documents in Google Docs (Google Drive)

[7.2.2](https://github.com/iterate-ch/cyberduck/compare/release-7-2-1...release-7-2-2)
* [Bugfix] Crash opening connection (macOS 10.[9-11]) ([#10892](https://trac.cyberduck.io/ticket/10892))
* [Bugfix] Interoperability with providers only supporting path-style requests (S3) ([#10888](https://trac.cyberduck.io/ticket/10888))
* [Bugfix] Display certificates as sheet in browser window (macOS) ([#10897](https://trac.cyberduck.io/ticket/10897))
* [Bugfix] Recursive search shows no result ([#10799](https://trac.cyberduck.io/ticket/10799))
* [Bugfix] Error moving file to encrypted vault ([#10803](https://trac.cyberduck.io/ticket/10803))

[7.2.1](https://github.com/iterate-ch/cyberduck/compare/release-7-2-0...release-7-2-1)
* [Bugfix] Failure to open application on Windows 7+ (Windows)

[7.2.0](https://github.com/iterate-ch/cyberduck/compare/release-7-1-2...release-7-2-0)
* [Bugfix] Failure validating hostname when connecting through HTTP proxy
* [Bugfix] MD5 mismatch error for uploads (Alibaba Cloud Object Storage Service) ([#10879](https://trac.cyberduck.io/ticket/10879))
* [Bugfix] Deprecated path-style request usage for (AWS GovCloud) ([#10824](https://trac.cyberduck.io/ticket/10824))
* [Bugfix] Default to use virtual hosted style to access bucket contents for third party S3 providers
* [Bugfix] Optimize MD5 checksum calculation (S3) ([#10278](https://trac.cyberduck.io/ticket/10278))
* [Bugfix] Reload directory contents after editing file
* [Bugfix] Failure replacing objects with blob type set to block (Azure)

[7.1.2](https://github.com/iterate-ch/cyberduck/compare/release-7-1-1...release-7-1-2)
* [Bugfix] Folder showing no more than 1000 files (S3) ([#10811](https://trac.cyberduck.io/ticket/10811))
* [Bugfix] Failure deleting folder recursively (FTP)

[7.1.1](https://github.com/iterate-ch/cyberduck/compare/release-7-1-0...release-7-1-1)
* [Bugfix] Failure authenticating using SSH agent (SFTP) ([#10800](https://trac.cyberduck.io/ticket/10800))
* [Bugfix] Failure overwriting existing file (Google Cloud Storage)
* [Bugfix] Missing hostname verification in TLS handshake when connecting through HTTP proxy

[7.1.0](https://github.com/iterate-ch/cyberduck/compare/release-7-0-2...release-7-1-0)
* [Feature] Application running as 64-bit (Windows)
* [Feature] Application package is notarized (macOS)
* [Feature] Support for storage classes (Google Cloud Storage)
* [Feature] Support to select region (eu, us, asia) for new buckets (Google Cloud Storage)
* [Feature] Support to set lifecycle configuration on bucket (Google Cloud Storage)
* [Bugfix] Number of buckets displayed in browser limited to 1000 (Google Cloud Storage)
* [Bugfix] Fails to authenticate where keyboard-interactive mechanism is not supported (SFTP) ([#10714](https://trac.cyberduck.io/ticket/10714))
* [Bugfix] Errors accessing Cryptomator vault (Google Drive, OneDrive)
* [Bugfix] No route to host error with IPv6 only server (macOS)
* [Bugfix] Allow to access files with non-printable characters in key name (S3)
* [Bugfix] Use AWS4 signature for pre-signed URLs for AWS in region us-east-1 (S3)
* [Bugfix] Rate limit number of requests (Google Drive) ([#10103](https://trac.cyberduck.io/ticket/10103))
* [Bugfix] Repeated prompt to validate host key when connecting to non-standard port (SFTP) ([#10772](https://trac.cyberduck.io/ticket/10772))
* [Bugfix] Segmented downloads fail with longer filenames ([#10726](https://trac.cyberduck.io/ticket/10726))

[7.0.2](https://github.com/iterate-ch/cyberduck/compare/release-7-0-1...release-7-0-2)
* [Bugfix] Failure configuring CloudFront distributions (S3) ([#10755](https://trac.cyberduck.io/ticket/10755))
* [Bugfix] Failure authenticating with STS (S3, Credentials from AWS Security Token Service) ([#10746](https://trac.cyberduck.io/ticket/10746))
* [Bugfix] Explicit argument to unlock vault prior an upload (CLI) ([#10352](https://trac.cyberduck.io/ticket/10352))

[7.0.1](https://github.com/iterate-ch/cyberduck/compare/release-7-0-0...release-7-0-1)
* [Feature] Default protocol selection for NextCloud & ownCloud
* [Feature] Create share links in NextCloud & ownCloud
* [Bugfix] Connect with application key restricted to single bucket (Backblaze B2) ([#10725](https://trac.cyberduck.io/ticket/10725))
* [Bugfix] Missing files when previously trashed and restored (Google Drive)

[7.0.0](https://github.com/iterate-ch/cyberduck/compare/release-6-9-4...release-7-0-0)
* [Feature] Segmented downloads with multiple connections per file ([#10115](https://trac.cyberduck.io/ticket/10115))
* [Feature] Allow password input in bookmark window
* [Feature] Create download authorization for files to share (Backblaze B2)
* [Feature] Make a file public and copy URL to share (Google Drive)
* [Feature] Create temporary link (4 hours) for file to share (Dropbox)
* [Feature] Create shared link for file (Microsoft OneDrive)
* [Feature] Create download share for file or folder (DRACOON)
* [Feature] Support to rename and copy files (Backblaze B2)
* [Feature] Support to open SSH terminal in bash.exe from Windows Linux Subsystem (SFTP, Windows) ([#10065](https://trac.cyberduck.io/ticket/10065))
* [Bugfix] Existing metadata not displayed (S3) ([#10647](https://trac.cyberduck.io/ticket/10647))
* [Bugfix] Browser always shows modification time in UTC timezone (Windows) ([#10629](https://trac.cyberduck.io/ticket/10629))
* [Bugfix] Interoerability with Apache Sling (WebDAV) ([#10598](https://trac.cyberduck.io/ticket/10598))
* [Bugfix] Interoerability with SAP NetWeaver Application Server (WebDAV)
* [Bugfix] Upload fails with 400 error reply (Google Drive)
* [Bugfix] Wrong MIME type set for uploaded files (S3)
* [Bugfix] Invalid signature for files with + or * character in key (S3) (#9914, #10679)
* [Bugfix] Failure uploading files larger than 4GB (OpenStack Swift) ([#10657](https://trac.cyberduck.io/ticket/10657))
* [Bugfix] Fails to launch in the newly created virtual desktop (Windows) ([#10467](https://trac.cyberduck.io/ticket/10467))
* [Bugfix] Adding bookmarks for multiple accounts using OAuth (Dropbox, Google Drive, OneDrive) ([#10562](https://trac.cyberduck.io/ticket/10562))

6.9.4
* [Bugfix] Connect button not working to initiate a connection ([#10621](https://trac.cyberduck.io/ticket/10621))
* [Bugfix] Multipart uploads fail if key begins or ends with whitespace character (S3) ([#10628](https://trac.cyberduck.io/ticket/10628))
* [Bugfix] Open OneNote notebooks in web browser (OneDrive)
* [Bugfix] Failure uploading files larger than 100GB (S3) ([#10612](https://trac.cyberduck.io/ticket/10612))

6.9.3
* [Bugfix] RFC compliant parsing of URI parameters (CLI)
* [Bugfix] Failure displaying alert window for multiple consecutive transfer failures
* [Bugfix] Checksum verification disabled by default for file transfers
* [Bugfix] Add --login option to executed shell for "Open in Terminal" feature
* [Bugfix] Disable transfer acceleration for AWS GovCloud (S3)
* [Bugfix] Login using temporary tokens from STS to AWS GovCloud (S3) ([#10594](https://trac.cyberduck.io/ticket/10594))
* [Bugfix] Support autoconfiguration from ~/.aws/credentials for AWS GovCloud and Amazon S3 China (S3)
* [Bugfix] Modification date not applied when copying file between browser windows ([#10592](https://trac.cyberduck.io/ticket/10592))
* [Bugfix] Unable to delete incomplete multipart upload (S3) ([#10568](https://trac.cyberduck.io/ticket/10568))
* [Bugfix] Updated localizations

6.9.2
* [Bugfix] Failure using RSA/ECDSA private in new OpenSSH format ([#10552](https://trac.cyberduck.io/ticket/10552))
* [Bugfix] Prompt to select parent folder for file dragged to upload (Mac App Store) ([#10580](https://trac.cyberduck.io/ticket/10580))

6.9.1
* [Bugfix] Fix code signature designated requirement issue (Mac App Store)

6.9.0
* [Localize] Estonian Localization
* [Feature] Default connection profile for Microsoft Sharepoint Online ([#10115](https://trac.cyberduck.io/ticket/10115))
* [Feature] Support for eu-west-3 (Stockholm) region (S3)
* [Feature] Improve listing performance for versioned buckets (S3) ([#10426](https://trac.cyberduck.io/ticket/10426))
* [Feature] Add --profile option to select connection profile (CLI)
* [Feature] Add --nochecksum option to skip verifying checksums (CLI)
* [Feature] Add option to disable checksum verification for file transfers ([#10215](https://trac.cyberduck.io/ticket/10215))
* [Bugfix] Stop button to interrupt transfer ([#10363](https://trac.cyberduck.io/ticket/10363))
* [Bugfix] Save passphrase for private key in keychain only after successful connect ([#10526](https://trac.cyberduck.io/ticket/10526))
* [Bugfix] Failure authenticating with proxy using Integrated Windows Authentication (IWA) authentication (WebDAV, Windows)
* [Bugfix] Failure authenticating with NTLM authentication (WebDAV, Windows) ([#10556](https://trac.cyberduck.io/ticket/10556))
* [Bugfix] Failure to read attributes of common prefix (S3) ([#8724](https://trac.cyberduck.io/ticket/8724))
* [Bugfix] Asks for OAuth authentication code on every connect attempt (Google Drive) ([#10555](https://trac.cyberduck.io/ticket/10555))
* [Bugfix] Transfers with many files to vault take a long time to finish (Cryptomator) ([#10564](https://trac.cyberduck.io/ticket/10564))
* [Bugfix] Cannot list KMS keys when using IAM Cross Account Roles (S3) ([#10565](https://trac.cyberduck.io/ticket/10565))
* [Bugfix] Updated localizations

6.8.3
* [Bugfix] Refinements to dark mode (macOS) (#10508, #10510)
* [Bugfix] Failure running script in Terminal. Not authorized to send Apple events to Terminal (macOS) ([#10475](https://trac.cyberduck.io/ticket/10475))
* [Bugfix] Upload action not enabled when server is not returning permission mask (FTP) ([#10506](https://trac.cyberduck.io/ticket/10506))
* [Bugfix] Large uploads requiring checksum of parts fail with network timeout ([#10516](https://trac.cyberduck.io/ticket/10516))

6.8.2
* [Bugfix] Failure transferring more than one file (FTP) ([#10494](https://trac.cyberduck.io/ticket/10494))

6.8.1
* [Bugfix] Interoperability with OpenStack Swift (S3 middleware) ([#10471](https://trac.cyberduck.io/ticket/10471))
* [Bugfix] Failure opening Terminal.app ([#10475](https://trac.cyberduck.io/ticket/10475))
* [Bugfix] Crash when dragging multiple files (macOS) ([#10479](https://trac.cyberduck.io/ticket/10479))
* [Bugfix] Add One Zone-Infrequent Access storage class (S3) ([#10481](https://trac.cyberduck.io/ticket/10481))
* [Bugfix] Improvements to dark mode (macOS) ([#10486](https://trac.cyberduck.io/ticket/10486))
* [Bugfix] Smart card insert prompt displayed when opening connection (Windows) ([#8595](https://trac.cyberduck.io/ticket/8595))

6.8.0
* [Feature] Support dark mode (macOS Mojave) ([#10348](https://trac.cyberduck.io/ticket/10348))
* [Bugfix] Display scaling is broken for some input components (Windows) ([#10443](https://trac.cyberduck.io/ticket/10443))
* [Bugfix] Missing session token when making the AssumeRoleRequest to obtain the cross account credentials from STS (S3) ([#10432](https://trac.cyberduck.io/ticket/10432))
* [Bugfix] Interoperability with AWS Snowball (S3) ([#10458](https://trac.cyberduck.io/ticket/10458))
* [Bugfix] No modification date displayed (Rackspace Cloudfiles) ([#10446](https://trac.cyberduck.io/ticket/10446))

6.7.1
* [Bugfix] Failure uploading to OneDrive Deutschland (OneDrive)
* [Bugfix] Updated localizations

6.7.0
* [Feature] Support display scaling (Windows) ([#8961](https://trac.cyberduck.io/ticket/8961))
* [Feature] Improve user experience for versioned buckets, e.g. properly hide folders with a delete marker (S3) ([#10357](https://trac.cyberduck.io/ticket/10357))
* [Feature] Transparently support role­based access, including cross­account using AWS Security Token Service (STS), configured in the standard AWS SDK credentials file (S3) ([#8880](https://trac.cyberduck.io/ticket/8880))
* [Feature] Prompt for an MFA token during authentication when specified in the profile from the standard AWS SDK credentials file (S3)
* [Feature] Interoperability with Microsoft Office 365 Deutschland (OneDrive) ([#10291](https://trac.cyberduck.io/ticket/10291))
* [Bugfix] Allow configuration of charset in bookmark (SFTP)
* [Bugfix] Incomplete transfers ([#10347](https://trac.cyberduck.io/ticket/10347))
* [Bugfix] Skip checksum validation for files encrypted with SSE-KMS (S3) ([#10371](https://trac.cyberduck.io/ticket/10371))
* [Bugfix] Retain metadata on server side copy of file (S3) ([#10341](https://trac.cyberduck.io/ticket/10341))
* [Bugfix] Failure creating new vault (Cryptomator, OneDrive)
* [Bugfix] Missing links in copy URL menu (CloudFront) ([#10402](https://trac.cyberduck.io/ticket/10402))

6.6.2
* [Bugfix] Cannot write to shared folders (Google Drive)
* [Bugfix] Repeated prompt for private key (SFTP, Mac App Store)
* [Bugfix] Authenticating with password protected ed25519 private key (SFTP) ([#10130](https://trac.cyberduck.io/ticket/10130))

6.6.1
* [Bugfix] Crash attempting to add empty password to keychain (SFTP, macOS) ([#10342](https://trac.cyberduck.io/ticket/10342))

6.6.0
* [Feature] Use toast notifications (Windows) ([#10268](https://trac.cyberduck.io/ticket/10268))
* [Feature] Include shared items (OneDrive) ([#10107](https://trac.cyberduck.io/ticket/10107))
* [Feature] Add support to login with Shared Access Signature (SAS) Token (Azure) ([#10321](https://trac.cyberduck.io/ticket/10321))
* [Feature] Support to queue transfers ([#9984](https://trac.cyberduck.io/ticket/9984))
* [Feature] Create and delete team drives (Google Drive)
* [Bugfix] Cannot create new vault in team drive (Google Drive, Cryptomator) ([#10324](https://trac.cyberduck.io/ticket/10324))
* [Bugfix] Missing files in folders of vault (Google Drive, Cryptomator) ([#10315](https://trac.cyberduck.io/ticket/10315))
* [Bugfix] Application error when opening connection (DRACOON) (Windows Store)
* [Bugfix] Disabled menu item to create new container (Azure)
* [Bugfix] Disabled menu item to create new bucket (Backblaze B2)

6.5.0
* [Bugfix] Improved performance accessing vaults (Cryptomator)
* [Bugfix] Improved performance accessing files (Backblaze B2, Google Drive)
* [Bugfix] Allow reuse of OAuth tokens (Cyberduck CLI, Windows)

6.4.6
* [Bugfix] Fix use proxy configuration using PAC file (macOS)
* [Bugfix] Disable extended master secret extension to allow session reuse for data connections (FTP-SSL) ([#10276](https://trac.cyberduck.io/ticket/10276))

6.4.4
* [Bugfix] Skip failures saving or finding passwords in credential manager (Windows)
* [Bugfix] Ignore 550 error replies for STAT (FTP) ([#10240](https://trac.cyberduck.io/ticket/10240))

6.4.3
* [Bugfix] Workaround error parsing codesigning entitlement by signing process in Mac App Store causing connect error due to missing entitlement key (Mac App Store) ([#10237](https://trac.cyberduck.io/ticket/10237))

6.4.1
* [Bugfix] Cannot edit ACLs and metadata in Info panel (macOS) ([#10226](https://trac.cyberduck.io/ticket/10226))

6.4.0
* [Feature] Support for eu-west-3 (Paris) region (S3) ([#10207](https://trac.cyberduck.io/ticket/10207))
* [Feature] Support for Integrated Windows Authentication (IWA) authentication (WebDAV, Windows)
* [Feature] Save passwords in credential manager (Windows) ([#9988](https://trac.cyberduck.io/ticket/9988))
* [Feature] Support authenticating with HTTP proxy (Windows)
* [Bugfix] Support for Oracle Cloud Infrastructure (OCI) Object Storage (S3) ([#10194](https://trac.cyberduck.io/ticket/10194))
* [Bugfix] Do not save passphrase for vaults by defaults (Cryptomator, Windows)
* [Bugfix] Application freezes for long time when selecting many files for upload (Windows)
* [Bugfix] Set modification date on uploaded files (OneDrive) ([#10171](https://trac.cyberduck.io/ticket/10171))
* [Bugfix] Application hangs when selecting a large number of files for upload (Windows)
* [Bugfix] Do not save vault passphrase by default (Cryptomator)
* [Bugfix] Fix failures when attempting to read attributes of incomplete multipart upload (S3)
* [Bugfix] Reloading directory after enabling "Auto detect" in preferences does not ask to unlock vault (Cryptomator) ([#10214](https://trac.cyberduck.io/ticket/10214))
* [Bugfix] Transfer labled incomplete when segment required multiple attempts to finish (S3, OpenStack Swift, Backblaze B2) ([#9552](https://trac.cyberduck.io/ticket/9552))

6.3.5
* [Bugfix] Crash on launch in update checker (Windows)

6.3.4
* [Bugfix] Application error when opening connection to BigCommerce (WebDAV)
* [Bugfix] Preload CloudFront configurations for buckets to allow display of CDN URLs (S3) ([#10184](https://trac.cyberduck.io/ticket/10184))

6.3.3
* [Bugfix] Display of created date instead of modification date (WebDAV)
* [Bugfix] Custom disk icon from connection profile not displayed in bookmarks (Windows)
* [Bugfix] Application error when opening connection (DRACOON) (Windows)

6.3.2
* [Feature] Option to save passwords (Cyberduck CLI, Linux)
* [Bugfix] Failure parsing permissions from MLSD reply (FTP)
* [Bugfix] Signed links incorrectly URL-encoded (Triton) ([#10151](https://trac.cyberduck.io/ticket/10151))
* [Bugfix] Passwords not saved in keychain (SFTP) ([#10159](https://trac.cyberduck.io/ticket/10159))
* [Bugfix] Set modification date on uploaded files (Local Disk)
* [Bugfix] Allow to switch to authentication with private key in login prompt (SFTP)

6.3.1
* [Feature] Read credentials from AWS configuration file in ~/.aws/credentials (S3)
* [Bugfix] Slow user interface performance (Mac) ([#9970](https://trac.cyberduck.io/ticket/9970))
* [Bugfix] Prompt for credentials when using public key authentication with agent (SFTP) ([#10148](https://trac.cyberduck.io/ticket/10148))
* [Bugfix] Failure opening files in vault on disk with long filename (Cryptomator, Local Disk)

6.3
* [Feature] Support for DRACOON cloud service (DRACOON)
* [Feature] Support for Google Team Drives (Google Drive) ([#9928](https://trac.cyberduck.io/ticket/9928))
* [Feature] Support for Joyent Triton Object Storage (Triton)
* [Feature] Support for China (Beijing) region (S3)
* [Bugfix] Allow custom endpoint with default connection profile (S3)
* [Bugfix] Unnecessary password prompt for connection (Local Disk)
* [Bugfix] Login where authentication is required with both password and one-time passcode (SFTP)
* [Bugfix] File not found failure when downloading folder form vault (Cryptomator)
* [Bugfix] Not possible to copy files between browser windows (FTP)

6.2.11
* [Bugfix] Application crash restoring workspace (Windows)

6.2.10
* [Feature] Option to disable auto-detect of vaults (Cryptomator)
* [Bugfix] Login where authentication is required with both password and public key method (SFTP)

6.2.9
* [Bugfix] Disable selection of client certificate in bookmark window when not applicable
* [Bugfix] Rename and override exiting file (One Drive, Dropbox)
* [Bugfix] Disable use of hashed hostname when writing to ~/.ssh/known_hosts (SFTP)

6.2.8
* [Bugfix] Delete session when canceling upload (One Drive)

6.2.7
* [Bugfix] Rename and override exiting file (Google Drive)
* [Bugfix] Reduce number of requests for uploads (Google Drive)

6.2.6
* [Bugfix] Quick Look only works one time ([#9889](https://trac.cyberduck.io/ticket/9889)) (Mac)
* [Bugfix] Disable notification for opened connection

6.2.5
* [Bugfix] Authentication using OAuth (Windows)
* [Bugfix] Failure to read attributes (S3) ([#10068](https://trac.cyberduck.io/ticket/10068))

6.2.4
* [Feature] Provide URL references to Office 365 documents in browser (OneDrive)
* [Feature] Disable Bonjour notifications
* [Bugfix] Transfer progress stops updating (Windows) ([#10042](https://trac.cyberduck.io/ticket/10042))

6.2.3
* [Feature] Allow to resume downloads (Cryptomator)
* [Feature] Allow to resume downloads (Dropbox)
* [Bugfix] Failure duplicating files (FTP) ([#10049](https://trac.cyberduck.io/ticket/10049))
* [Bugfix] Failure with input prompt for username and password (CLI, Windows) ([#10043](https://trac.cyberduck.io/ticket/10043))
* [Bugfix] Failure to detect vault when moving files into locked vault (Cryptomator)
* [Bugfix] Failure to detect vault when copying files into locked vault (Cryptomator)
* [Bugfix] Checksum mismatch when downloading versioned file (S3) ([#10055](https://trac.cyberduck.io/ticket/10055))

6.2.2
* [Bugfix] Failure copying files in vault (Cryptomator)
* [Bugfix] Failure detecting vault when uploading, copying or moving files to locked vault (Cryptomator)

6.2.1
* [Bugfix] Default to WebDAV for http:// scheme in quick connect and CLI
* [Bugfix] Selecting multiple files to copy URL will only copy first item in list ([#10024](https://trac.cyberduck.io/ticket/10024))

6.2
* [Feature] Server side copy of files (OneDrive)
* [Feature] Support for wildcard host entries in known_hosts (SFTP)
* [Bugfix] Missing x-amz-server-side-encryption header when creating folders (S3) ([#9378](https://trac.cyberduck.io/ticket/9378))
* [Bugfix] Preserve content type when overwriting files (Google Drive)
* [Bugfix] Missing registered protocols (CLI)
* [Bugfix] Long time preparing upload into vault (Cryptomator)
* [Bugfix] Set charset for basic authentication scheme used for preemtive authentication (WebDAV)
* [Bugfix] Timeout uploading larger files (Google Drive) ([#10010](https://trac.cyberduck.io/ticket/10010))
* [Bugfix] Set checksum for large file uploads in fileInfo metadata (Backblaze B2)
* [Bugfix] Default ACL for new buckets has changed to private (S3)
* [Bugfix] Failure duplicating files in vault (Cryptomator) ([#10017](https://trac.cyberduck.io/ticket/10017))

6.1
* [Feature] Search files fast without recursively listing directories (OneDrive)
* [Feature] Search files fast without recursively listing directories (S3)
* [Feature] Search files fast without recursively listing directories (Dropbox)
* [Feature] Search files fast without recursively listing directories (Google Drive)
* [Feature] Group protocols by type in bookmark window
* [Bugfix] Missing x-amz-server-side-encryption header when creating folders (S3) ([#9378](https://trac.cyberduck.io/ticket/9378))
* [Bugfix] Interoperability with vault version 6 (Cryptomator)
* [Bugfix] Missing content length header in uploads to SharePoint Online (OneDrive)
* [Bugfix] Always trust invalid certificate setting not remembered (Windows)
* [Bugfix] Unable to rename files larger than 5 GB (S3) ([#9983](https://trac.cyberduck.io/ticket/9983))
* [Bugfix] Handle expired authentication token (Backblaze B2)
* [Bugfix] "Open in Putty" toolbar button always disabled (Windows) ([#9965](https://trac.cyberduck.io/ticket/9965))
* [Bugfix] Updater ignores custom installation location (Windows) ([#9782](https://trac.cyberduck.io/ticket/9782))

6.0.4
* [Bugfix] Failure to upload to root of vault (Cryptomator)
* [Bugfix] Disable x-amz-request-payer header for non AWS endpoints (S3)

6.0.2
* [Bugfix] Invalid version number format for Mac App Store (Mac)

6.0.1
* [Bugfix] New connections do not authenticate (Dropbox) ([#9932](https://trac.cyberduck.io/ticket/9932))
* [Bugfix] Large file uploads do not resume (Backblaze B2) ([#9935](https://trac.cyberduck.io/ticket/9935))
* [Bugfix] Failure listing folders with whitespace (OneDrive) ([#9937](https://trac.cyberduck.io/ticket/9937))
* [Bugfix] Failure duplicating files (FTP) ([#9933](https://trac.cyberduck.io/ticket/9933))
* [Bugfix] Failure uploading files with temporary filename option enabled (Backblaze B2)
* [Bugfix] Failure connecting to local disk (Windows)
* [Bugfix] Multipart uploads fail if the user lacks permission to list multipart uploads (S3) ([#9948](https://trac.cyberduck.io/ticket/9948))
* [Bugfix] Synchronize files in vault (Cryptomator)
* [Bugfix] Repeated login prompt (2-Factor Authentication SFTP)
* [Bugfix] Enable copy of text from log drawer (Windows) ([#9952](https://trac.cyberduck.io/ticket/9952))

6.0
* [Feature] Create encrypted vault interopable with Cryptomator ([#7937](https://trac.cyberduck.io/ticket/7937))
* [Feature] Encrypt uploads into vault (Cyptomator)
* [Feature] Decrypt downloads from vault (Cyptomator)
* [Feature] Support for Microsoft OneDrive (OneDrive) ([#9799](https://trac.cyberduck.io/ticket/9799))
* [Feature] Browse local filesystem in browser
* [Feature] Support ssh-rsa-cert-v01@openssh.com format for private key files (SFTP)
* [Feature] Support ssh-dsa-cert-v01@openssh.com format for private key files (SFTP)
* [Feature] Configure lifecycle options for buckets (Backblaze B2)
* [Feature] Copy pre-authenticated URLs (Backblaze B2)

5.4.4
* [Bugfix] Multipart uploads do not resume (S3)
* [Bugfix] Field in login prompt disabled to enter authentication code (OAuth 2.0)

5.4.3
* [Bugfix] Interoperability with PROPFIND for listing folders (WebDAV)
* [Bugfix] Failed to generate HMAC (Spectra S3)
* [Bugfix] Quick Look only works one time ([#9889](https://trac.cyberduck.io/ticket/9889))
* [Bugfix] Set MD5 checksum in ETag request header (OpenStack Swift)

5.4.2
* [Bugfix] Updated localizations

5.4.1
* [Feature] Faster recursive delete with single operation for directories (WebDAV)
* [Bugfix] Reload OpenSSH configuration (SFTP)
* [Bugfix] Revert lenient server reply parsing (FTP)

5.4
* [Feature] Add "Open single connection" option for file transfers
* [Bugfix] Failure enabling download distribution (CloudFront) ([#9870](https://trac.cyberduck.io/ticket/9870))
* [Bugfix] Authentication failure when using PAM (iRODS) ([#9872](https://trac.cyberduck.io/ticket/9872))
* [Bugfix] Increasing memory usage when browsing folders
* [Bugfix] Drastically reduced initial memory usage ([#9878](https://trac.cyberduck.io/ticket/9878))

5.3.9
* [Bugfix] Failure dragging files to browser for upload (Mac) ([#9860](https://trac.cyberduck.io/ticket/9860))

5.3.8
* [Bugfix] NTLM authentication failure for uploads (Sharepoint) ([#9855](https://trac.cyberduck.io/ticket/9855))
* [Bugfix] Selected client certificate cleared from bookmark after opening connection ([#9842](https://trac.cyberduck.io/ticket/9842))

5.3.7
* [Bugfix] Include "Shared with me" files (Google Drive)
* [Bugfix] Unable to add new item to metadata (S3) ([#9844](https://trac.cyberduck.io/ticket/9844))
* [Bugfix] Crash in periodic background update check ([#9845](https://trac.cyberduck.io/ticket/9845))

5.3.6
* [Bugfix] Crash for connections requiring TLS connection handshake (Windows CLI)

5.3.5
* [Bugfix] Freeze of browser after idle timeout ([#9829](https://trac.cyberduck.io/ticket/9829))
* [Bugfix] Crash for connections requiring TLS connection handshake (Windows Store)

5.3.4
* [Bugfix] Error duplicating folders (FTP) ([#9818](https://trac.cyberduck.io/ticket/9818))
* [Bugfix] Cannot read distribution details (CloudFront) ([#9823](https://trac.cyberduck.io/ticket/9823))
* [Bugfix] Failure setting attributes for uploaded files with temporary filename option enabled ([#9819](https://trac.cyberduck.io/ticket/9819))

5.3.3
* [Bugfix] Downloads fail with exception after completion (OpenStack Swift) ([#9814](https://trac.cyberduck.io/ticket/9814))

5.3.2
* [Bugfix] Synchronize transfers fail with exception
* [Bugfix] Repeat transfer failures due to HTTP request timeout (Backblaze B2)

5.3.1
* [Feature] Availability in Windows App Store

5.3
* [Feature] Use multiple connections in browser for parallel background task executions
* [Feature] Choose certificate in bookmark panel for mutual authentication with TLS (WebDAV)
* [Feature] Choose SSH private key from list in bookmark panel and login prompt (SFTP)
* [Feature] Use marker and delimiter for listing files (Backblaze B2)
* [Feature] Option to resume uploads (Azure) ([#9770](https://trac.cyberduck.io/ticket/9770))
* [Feature] Support for ca-central-1 (Montreal) region (S3)
* [Feature] Support for eu-west-2 (London) region (S3)
* [Bugfix] Repeated prompt for client certificate (WebDAV) ([#9746](https://trac.cyberduck.io/ticket/9746))
* [Bugfix] Modification of distinct metadata for multiple selected files (WebDAV, Azure, S3, OpenStack Swift)
* [Bugfix] Modification of distinct permissions for multiple selected files (FTP, SFTP)
* [Bugfix] Wrong default editor selected (Windows) ([#9256](https://trac.cyberduck.io/ticket/9256))
* [Bugfix] Failure sorting by modification date in browser ([#9801](https://trac.cyberduck.io/ticket/9801))
* [Bugfix] Invalidations for files with special characters (Cloudfront) ([#9748](https://trac.cyberduck.io/ticket/9748))

5.2.2
* [Feature] Toggle transfer acceleration for bucket in Info panel (S3)
* [Feature] Support OpenSSH unencrypted private key format for ed25519 keys (SFTP) ([#8548](https://trac.cyberduck.io/ticket/8548))

5.2.1
* [Feature] All embedded assemblies are cryptographically signed (Windows)
* [Bugfix] Paginate directory listings (Dropbox)
* [Bugfix] Ignore permission failure for reading transfer acceleration configuration (S3) ([#9741](https://trac.cyberduck.io/ticket/9741))

5.2
* [Feature] Support connecting to Dropbox (Dropbox) ([#6427](https://trac.cyberduck.io/ticket/6427))
* [Feature] Tabbed windows (macOS 10.12) ([#5998](https://trac.cyberduck.io/ticket/5998))
* [Feature] Support transfer acceleration (S3) ([#9570](https://trac.cyberduck.io/ticket/9570))
* [Feature] Provide URL references to Google Docs documents in browser (Google Drive)
* [Feature] Support for US East (Ohio) region (S3)
* [Feature] Support for Asia Pacific (Seoul) region (S3)
* [Feature] Support for Asia Pacific (Mumbai) region (S3)

5.1.4
* [Bugfix] Duplicate folders displayed in file browser (Backblaze B2) ([#9717](https://trac.cyberduck.io/ticket/9717))
* [Bugfix] Updates to ACLs not showing up in Info window without refreshing (S3) ([#9731](https://trac.cyberduck.io/ticket/9731))

5.1.3
* [Bugfix] Normalize (NFC) filenames (SFTP)
* [Bugfix] Overwriting file creates duplicate (Google Drive)

5.1.2
* [Bugfix] Crash importing bookmarks from ExpanDrive (Windows) ([#9672](https://trac.cyberduck.io/ticket/9672))

5.1.1
* [Feature] Allow connections with AWS2 signature version using connection profile (S3) ([#9667](https://trac.cyberduck.io/ticket/9667))
* [Bugfix] Cleanup temporary files on application exit
* [Bugfix] Certificate trust error connecting to os.unil.cloud.switch.ch (S3) ([#9668](https://trac.cyberduck.io/ticket/9668))

5.1
* [Feature] Prompt when recursively applying permissions ([#9657](https://trac.cyberduck.io/ticket/9657))
* [Feature] Default to signature version AWS4 when connecting to third party S3 providers
* [Feature] Use batch operation to delete multiple files (Google Drive)
* [Bugfix] Prompt to insert Smart Card when connecting to TLS secured site (Windows) ([#8595](https://trac.cyberduck.io/ticket/8595))
* [Bugfix] Copy transfer from other connection fails (Spectra S3)
* [Bugfix] Interoperability with Minio (S3)
* [Bugfix] Repeated prompt to select private key for authentication (App Store) (SFTP)
* [Bugfix] Repeated prompt for changed host key fingerprint (SFTP)

5.0.11

* [Bugfix] Resuming file transfer starts over and errors out (Backblaze B2) ([#9598](https://trac.cyberduck.io/ticket/9598))
* [Bugfix] Notification icon persists after application closed (Windows) ([#9613](https://trac.cyberduck.io/ticket/9613))
* [Bugfix] Missing CDN URLs in copy menu item (Rackspace Cloudfiles) ([#9638](https://trac.cyberduck.io/ticket/9638))

5.0.10

* [Feature] Handle URL events and open browser or start file transfer (Windows)
* [Bugfix] Retain container permission when updating ACL after upload complete for file (Backblaze B2)
* [Bugfix] Display incomplete multipart uploads as hidden files in browser (S3)

5.0.9
* [Feature] Allow silent application update in background (Mac)
* [Feature] Display in-progress multipart uploads in browser (S3)
* [Feature] Importer for bookmarks from Transmit 4 (Mac)
* [Bugfix] Allow to select encryption keys from KMS different from default us-east-1 region (S3) ([#9617](https://trac.cyberduck.io/ticket/9617))
* [Bugfix] Uncomfirmed reads are unbounded (SFTP) ([#9603](https://trac.cyberduck.io/ticket/9603))
* [Bugfix] Mismatch for MD5 checksum when downloading large object (OpenStack Swift) ([#8861](https://trac.cyberduck.io/ticket/8861))

5.0.8
* [Feature] Register protocol handler for irods:// ([#9614](https://trac.cyberduck.io/ticket/9614))
* [Bugfix] Listing directory failure on folder with ~ in path (S3) ([#9611](https://trac.cyberduck.io/ticket/9611))

5.0.7
* [Bugfix] Missing upload notification after editing file (Mac) ([#9596](https://trac.cyberduck.io/ticket/9596))
* [Bugfix] Stale entries in directory cache when deleting folder ([#9608](https://trac.cyberduck.io/ticket/9608))

5.0.6
* [Feature] Signed application executable (Windows)

5.0.5
* [Feature] Set modification date in metadata for uploads (Backblaze B2)

5.0.4
* [Bugfix] Allow authentication with client certificate for empty issuer list from server (TLS)

5.0.3
* [Feature] Deleting multiple files concurrently ([#9585](https://trac.cyberduck.io/ticket/9585))

5.0.2
* [Bugfix] Fail to list directory with equals symbol in path (S3) ([#9574](https://trac.cyberduck.io/ticket/9574))
* [Bugfix] Failure to launch program (CLI Linux) ([#9586](https://trac.cyberduck.io/ticket/9586))

5.0.1
* [Feature] Support for hubiC (OVH) (OpenStack Swift) ([#7764](https://trac.cyberduck.io/ticket/7764))
* [Bugfix] Support authenticating with multiple accounts (Google Drive) ([#9567](https://trac.cyberduck.io/ticket/9567))
* [Bugfix] Copying files using multipart API (S3) ([#9578](https://trac.cyberduck.io/ticket/9578))

5.0
* [Feature] Updated user interface; new monochrome toolbar icons (Mac)
* [Feature] Support for Google Drive ([#6976](https://trac.cyberduck.io/ticket/6976))
* [Feature] Support for Spectra BlackPearl Deep Storage Gateway (Spectra S3)
* [Feature] Option to use AWS KMS–Managed Keys (SSE-KMS) for server side encryption (S3)
* [Feature] Default to use AWS4 signature version for authentication (S3)
* [Feature] Repeat failed transfers for single segments of multipart large file transfer (S3, OpenStack Swift, Backblaze B2)
* [Bugfix] Retry transfer for 421 error reply from server with a delay (FTP) ([#9368](https://trac.cyberduck.io/ticket/9368))
* [Bugfix] No prompt for importing WinSCP bookmarks (Windows) ([#9208](https://trac.cyberduck.io/ticket/9208))
* [Bugfix] Signature mismatch for presigned URLs with AWS4 signing (S3) (#9317, #9479)
* [Bugfix] Unable to negotiate acceptable set of security parameters (WebDAV) ([#9452](https://trac.cyberduck.io/ticket/9452))
* [Bugfix] Interoperability with Atlassian Confluence (WebDAV)
* [Bugfix] Interoperability with Oracle Cloud (OpenStack Swift) ([#9223](https://trac.cyberduck.io/ticket/9223))

4.9.3
* [Bugfix] Revert reading NTLM domain and workstation from environment (WebDAV, Windows)
* [Bugfix] Fix crash in software updater (Windows)

4.9.2
* [Bugfix] Fix application launch failure when running with disabled updater (Mac)

4.9.1
* [Feature] Support password change requests (SFTP) ([#8821](https://trac.cyberduck.io/ticket/8821))
* [Bugfix] Default path value setting in bookmark ignored ([#9435](https://trac.cyberduck.io/ticket/9435))
* [Bugfix] Excessive memory usage when uploading many files ([#9439](https://trac.cyberduck.io/ticket/9439))
* [Bugfix] Retry transfer when authentication token expires (Backblaze B2)
* [Bugfix] Uploads use a lot of Class C transactions ([#9417](https://trac.cyberduck.io/ticket/9417)) (Backblaze B2)

4.9
* [Feature] Support for B2 Cloud Storage (Backblaze B2) ([#9162](https://trac.cyberduck.io/ticket/9162))
* [Feature] New bundled application updater using MSI for installation (Windows)
* [Feature] New software updater (Windows)
* [Bugfix] Creating new folders with SSE restriction fails (S3) ([#9378](https://trac.cyberduck.io/ticket/9378))
* [Bugfix] Unable to duplicate folders ([#9383](https://trac.cyberduck.io/ticket/9383))
* [Bugfix] Routing failure when connecting to IPv6 address ([#8802](https://trac.cyberduck.io/ticket/8802))

4.8.4
* [Bugfix] Restore compatibility with OS X 10.7 (Mac)
* [Bugfix] No such file error when choosing Rename Existing in transfer overwrite prompt ([#9342](https://trac.cyberduck.io/ticket/9342))
* [Bugfix] Disable updater when missing admin privilege ([#9155](https://trac.cyberduck.io/ticket/9155)) (Windows)
* [Bugfix] Browser tries to expand files as directories ([#9340](https://trac.cyberduck.io/ticket/9340)) (Windows)
* [Bugfix] Authentication always takes a long time (S3) ([#9348](https://trac.cyberduck.io/ticket/9348))
* [Bugfix] Certificate trust verification failure for four level domains (WebDAV) ([#9358](https://trac.cyberduck.io/ticket/9358))

4.8.3
* [Bugfix] Fix use of unlimited strength cryptography for strong ciphers (SFTP) ([#9325](https://trac.cyberduck.io/ticket/9325))

4.8.2
* [Bugfix] Installer may fail if newer Bonjour Zeroconf dependency is installed (Windows)
* [Bugfix] Disable trashing file on overwrite download ([#9298](https://trac.cyberduck.io/ticket/9298))

4.8.1
* [Bugfix] Restore compatibility with OS X 10.8 - 10.9 (Mac)

4.8
* [Feature] Multiple connections for transfers enabled by default
* [Feature] Recursively search for files ([#8345](https://trac.cyberduck.io/ticket/8345))
* [Feature] Support for key exchange algorithm diffie-hellman-group-exchange-sha256 (SFTP) ([#8488](https://trac.cyberduck.io/ticket/8488))
* [Feature] Support for key exchange algorithm curve25519-sha256@libssh.org (SFTP) ([#8528](https://trac.cyberduck.io/ticket/8528))
* [Feature] Support ssh-ed25519 host keys (SFTP) ([#8553](https://trac.cyberduck.io/ticket/8553))
* [Feature] Support for authentication with Keystone v3 API (OpenStack Swift) ([#8813](https://trac.cyberduck.io/ticket/8813))
* [Feature] Read NTLM domain and workstation from environment (WebDAV, Windows)
* [Bugfix] Files greater than 100GB fail to upload (OpenStack Swift) ([#9131](https://trac.cyberduck.io/ticket/9131))
* [Bugfix] Changed fingerprint prompt and duplicate ECDSA host key entries in ~/.ssh/known_hosts (SFTP) ([#8867](https://trac.cyberduck.io/ticket/8867))
* [Bugfix] Certificate trust errors for DNS-named buckets (S3) ([#3813](https://trac.cyberduck.io/ticket/3813))
* [Bugfix] Search is broken always including folders ([#9076](https://trac.cyberduck.io/ticket/9076))

4.7.3
* [Feature] Shared bookmarks and history with CLI and Mountain Duck in application group support directory (Mac)
* [Feature] Importer for ExpanDrive bookmarks (Mac)
* [Feature] Allow to select PuTTY install location (Windows)
* [Feature] Authentication with PAM scheme (iRODS)
* [Bugfix] Files are damaged after being synchronized with mirror action ([#8657](https://trac.cyberduck.io/ticket/8657))
* [Bugfix] Certificate chain displayed not complete ([#8885](https://trac.cyberduck.io/ticket/8885))
* [Bugfix] Permission failure deleting files looking for interrupted multipart uploads (S3) ([#9000](https://trac.cyberduck.io/ticket/9000))
* [Bugfix] Support for Infrequent Access (Standard IA) storage class (S3)
* [Bugfix] XML validation failure with AccessControlPolicy element (Google Cloud Storage) ([#9002](https://trac.cyberduck.io/ticket/9002))
* [Bugfix] Do not try list containers if default path is provided (OpenStack Swift) ([#9038](https://trac.cyberduck.io/ticket/9038))
* [Bugfix] Faster connection setup with many containers (OpenStack Swift)
* [Bugfix] Failure uploading multiple files (iRODS) ([#8911](https://trac.cyberduck.io/ticket/8911))

4.7.2
* [Feature] Delete incomplete multipart uploads when deleting file (S3) ([#8920](https://trac.cyberduck.io/ticket/8920))
* [Bugfix] Default size of new browser window (Mac) ([#8906](https://trac.cyberduck.io/ticket/8906))
* [Bugfix] Copying HTTP URL does not work ([#8909](https://trac.cyberduck.io/ticket/8909))
* [Bugfix] Crash in NSLog (Mac) ([#8927](https://trac.cyberduck.io/ticket/8927))
* [Bugfix] Unable to select private key (Mac) (#8928, #8933)
* [Bugfix] Interoperability with Oracle Web Center ([#8953](https://trac.cyberduck.io/ticket/8953))

4.7.1
* [Feature] Invalidate multiple files using wildcards (CloudFront)
* [Bugfix] Accessibility for blind users in Transfer window (VoiceOver) ([#1343](https://trac.cyberduck.io/ticket/1343))
* [Bugfix] Interoperability with Ceph S3 ([#8779](https://trac.cyberduck.io/ticket/8779))
* [Bugfix] Interoperability with Oracle Service Cloud ([#8902](https://trac.cyberduck.io/ticket/8902))
* [Bugfix] Second level cache not invalidated when reloading browser ([#8774](https://trac.cyberduck.io/ticket/8774))
* [Bugfix] Subfolder appearing inside folder of same name (S3) ([#8769](https://trac.cyberduck.io/ticket/8769))
* [Bugfix] Directory removal fails silently (S3) ([#8803](https://trac.cyberduck.io/ticket/8803))
* [Bugfix] Resuming upload with nested folders (S3)
* [Bugfix] Routing failure when connecting to IPv6 address because of wrong default network interface ([#8802](https://trac.cyberduck.io/ticket/8802))
* [Bugfix] MD5 checksum failure for downloads (WebDAV) ([#8798](https://trac.cyberduck.io/ticket/8798))
* [Bugfix] MD5 checksum failure for large object downloads (OpenStack Swift) ([#8861](https://trac.cyberduck.io/ticket/8861))
* [Bugfix] Segmented upload marked as incomplete (OpenStack Swift) ([#8859](https://trac.cyberduck.io/ticket/8859))
* [Bugfix] Failure cancelling queued transfer ([#8844](https://trac.cyberduck.io/ticket/8844))
* [Bugfix] Copy & Paste menu items disabled (Mac) ([#8849](https://trac.cyberduck.io/ticket/8849))
* [Bugfix] Support retrieving files from buckets from Requester Pays Buckets (S3) ([#8893](https://trac.cyberduck.io/ticket/8893))
* [Bugfix] Missing C++ Redistributable Package library installation (Windows) ([#8162](https://trac.cyberduck.io/ticket/8162))

4.7
* [Feature] Support to access iRODS (Integrated Rule-Oriented Data System)
* [Feature] Support multiple connections per transfer ([#1135](https://trac.cyberduck.io/ticket/1135))
* [Feature] Connecting with temporary access credentials from EC2 (S3) ([#8610](https://trac.cyberduck.io/ticket/8610))
* [Feature] Verify checksum for downloads if available (WebDAV, S3, OpenStack Swift)
* [Feature] Using multipart when copying large files (S3) ([#8616](https://trac.cyberduck.io/ticket/8616))
* [Feature] HTTP proxy tunneling using CONNECT method ([#76](https://trac.cyberduck.io/ticket/76))
* [Bugfix] Crash opening preferences window ([#8617](https://trac.cyberduck.io/ticket/8617))
* [Bugfix] Login with combination of public key authentication one-time passcode (SFTP) ([#8597](https://trac.cyberduck.io/ticket/8597))
* [Bugfix] Enable multipart uploads for all providers (S3) ([#8677](https://trac.cyberduck.io/ticket/8677))
* [Bugfix] Incomplete download of files with Content-Encoding header (OpenStack Swift) ([#8656](https://trac.cyberduck.io/ticket/8656))
* [Bugfix] Failure to download to mounted AFP drive (Mac) ([#8670](https://trac.cyberduck.io/ticket/8670))
* [Bugfix] Regular connection failures (OpenStack Swift) ([#8634](https://trac.cyberduck.io/ticket/8634))
* [Bugfix] Handshake failure because of missing strong ciphers to negotiate for TLS ([#8703](https://trac.cyberduck.io/ticket/8703))
* [Bugfix] Skip DNS resolution when connecting through proxy ([#8733](https://trac.cyberduck.io/ticket/8733))
* [Bugfix] Disable sending of keep-alive packets for interoperability (SFTP) ([#8618](https://trac.cyberduck.io/ticket/8618))
* [Bugfix] Repeating connection failures because of server closing control connection (FTP) ([#8532](https://trac.cyberduck.io/ticket/8532))
* [Bugfix] Filezilla bookmark importer fails to read passwords ([#8694](https://trac.cyberduck.io/ticket/8694))
* [Bugfix] Unable to enter IPv6 address as hostname ([#8696](https://trac.cyberduck.io/ticket/8696))
* [Bugfix] Support retrieving files from buckets from Requester Pays Buckets (S3) ([#8893](https://trac.cyberduck.io/ticket/8893))

4.6.5
* [Feature] Drag bookmarks from history and Bonjour tab to default bookmarks (Mac) ([#2182](https://trac.cyberduck.io/ticket/2182))
* [Bugfix] Some interface items not localized (Mac) ([#8538](https://trac.cyberduck.io/ticket/8538))
* [Bugfix] Uploading file removes shared access policy (Azure) ([#8544](https://trac.cyberduck.io/ticket/8544))

4.6.4
* [Feature] Send packets to keep connection alive (SFTP)
* [Feature] Backspace keyboard shortcut to change to parent directory (Windows) ([#8457](https://trac.cyberduck.io/ticket/8457))
* [Bugfix] No localizations (Windows) ([#8497](https://trac.cyberduck.io/ticket/8497))
* [Bugfix] Crash opening preferences when default editor is no longer installed (Mac) ([#8402](https://trac.cyberduck.io/ticket/8402))
* [Bugfix] Incomplete download of files with Content-Encoding header (S3) ([#8263](https://trac.cyberduck.io/ticket/8263))
* [Bugfix] Interoperability issue with MLSD replies (FTP) ([#8511](https://trac.cyberduck.io/ticket/8511))
* [Bugfix] Allow Web URL configuration for OpenStack Swift and S3 ([#8516](https://trac.cyberduck.io/ticket/8516))

4.6.3
* [Bugfix] Occasional failure verifying donation keys

4.6.2
* [Feature] Recursively change storage class and encryption (S3) ([#8421](https://trac.cyberduck.io/ticket/8421))
* [Bugfix] Crash reading proxy settings (Mac) (#8414
* [Bugfix] No minimum size set for windows (Mac) ([#8446](https://trac.cyberduck.io/ticket/8446))
* [Bugfix] AWS Signature Version 4 for presigned temporary URLs (S3) ([#8386](https://trac.cyberduck.io/ticket/8386))
* [Bugfix] Transfer incomplete message uploading folder (S3) ([#8432](https://trac.cyberduck.io/ticket/8432))
* [Bugfix] Failure using updated password ([#8463](https://trac.cyberduck.io/ticket/8463))
* [Bugfix] Retain custom metadata when overwriting files (S3, OpenStack Swift) ([#8469](https://trac.cyberduck.io/ticket/8469))

4.6.1
* [Bugfix] Uploads to buckets in eu-central-1 (S3) ([#8375](https://trac.cyberduck.io/ticket/8375))
* [Bugfix] Supporting both Project ID and Project Number for login username (Google Cloud Storage) ([#8352](https://trac.cyberduck.io/ticket/8352))
* [Bugfix] Changes not uploaded when editing multiple files (Windows) (#8359, #8369)
* [Bugfix] Failure to read attributes downloading bucket (S3) ([#8388](https://trac.cyberduck.io/ticket/8388))
* [Bugfix] Wrong timestamp in temporary URL (OpenStack Swift) ([#8384](https://trac.cyberduck.io/ticket/8384))

4.6
* [Feature] Support PAC files for proxy configuration (Mac) ([#2607](https://trac.cyberduck.io/ticket/2607))
* [Feature] Customizable bookmark icon size (Windows) ([#6444](https://trac.cyberduck.io/ticket/6444))
* [Feature] Add support for buckets in region eu-central-1 (Frankfurt) (S3) ([#8302](https://trac.cyberduck.io/ticket/8302))
* [Feature] Support authentication signature version AWS4-HMAC-SHA256 (S3) ([#8302](https://trac.cyberduck.io/ticket/8302))
* [Feature] Improved error reporting on connection failures
* [Feature] Verify MD5 checksum for multipart uploads (S3)
* [Feature] Disabled SSLv3 (HTTP)
* [Feature] Improved performance downloading and mirroring files
* [Bugfix] Connecting using authentication with SSH agent (SFTP)
* [Bugfix] Interoperability with SSH Tectia Server (SFTP) ([#8166](https://trac.cyberduck.io/ticket/8166))
* [Bugfix] Interoperability with Eucalyptus Object Storage (S3) ([#8216](https://trac.cyberduck.io/ticket/8216))
* [Bugfix] Reconnect on disconnect ([#8205](https://trac.cyberduck.io/ticket/8205))
* [Bugfix] Remember last selected directory per bookmark in upload and save panel ([#8242](https://trac.cyberduck.io/ticket/8242))
* [Bugfix] Edited file with unicode character in parent path does not upload on save ([#8244](https://trac.cyberduck.io/ticket/8244)) (Mac)
* [Bugfix] Repeat failed networking tasks by default ([#8237](https://trac.cyberduck.io/ticket/8237))
* [Bugfix] Allow connecting to accounts with a multitude of containers (OpenStack Swift) ([#8198](https://trac.cyberduck.io/ticket/8198))

4.5.2
* [Feature] Network diagnose option in connection failure alert (Windows)
* [Bugfix] Folder contents in directory placeholders not listed (OpenStack Swift) ([#8094](https://trac.cyberduck.io/ticket/8094))
* [Bugfix] Use version 2 for sealed resources for compatibility with OS X 10.9.5 and later (Gatekeeper)
* [Bugfix] Synchronize fails to add new local files ([#8096](https://trac.cyberduck.io/ticket/8096))
* [Bugfix] Wrong public key fingerprint displayed (SFTP) ([#8173](https://trac.cyberduck.io/ticket/8173))
* [Bugfix] Expanding a folder resets the scroll position ([#7941](https://trac.cyberduck.io/ticket/7941)) (Windows)

4.5.1
* [Bugfix] Connection failure when sandboxing denies access to ~/.ssh/known_hosts (SFTP) ([#8102](https://trac.cyberduck.io/ticket/8102))

4.5
* [Feature] Connecting to Windows Azure Blob Storage (Azure) ([#6521](https://trac.cyberduck.io/ticket/6521))
* [Feature] New SSH/SFTP protocol implementation
* [Feature] TLS mutual (two-way) authentication with client certificate (WebDAV, FTP-TLS) ([#5883](https://trac.cyberduck.io/ticket/5883))
* [Feature] Public key authentification using OpenSSH agent (SFTP) (Mac) ([#75](https://trac.cyberduck.io/ticket/75))
* [Feature] Public key authentification using Pageant (SFTP) (Windows) ([#75](https://trac.cyberduck.io/ticket/75))
* [Feature] GZIP Compression (SFTP) ([#123](https://trac.cyberduck.io/ticket/123))
* [Feature] ECDSA public key authentication (SFTP) ([#7938](https://trac.cyberduck.io/ticket/7938))
* [Feature] Notifications in system tray (Windows) ([#8007](https://trac.cyberduck.io/ticket/8007))
* [Bugfix] Broken pipe with uploads (S3) (#7964, #7621)
* [Bugfix] 404 error response when downloading folders (S3, OpenStack Swift) (#7971, #8064)
* [Bugfix] Repeated prompt for private key password (SFTP) ([#8009](https://trac.cyberduck.io/ticket/8009))
* [Bugfix] Open in Putty (SFTP, Windows) ([#8063](https://trac.cyberduck.io/ticket/8063))

4.4.5
* [Bugfix] Failure copying folders from server to server ([#7946](https://trac.cyberduck.io/ticket/7946))
* [Bugfix] Sort order of bookmarks not remembered ([#7959](https://trac.cyberduck.io/ticket/7959))
* [Bugfix] Duplicate files in synchronization prompt ([#7645](https://trac.cyberduck.io/ticket/7645))
* [Bugfix] 404 error response when downloading folders (S3) ([#7971](https://trac.cyberduck.io/ticket/7971))
* [Bugfix] Null pointer downloading symbolic link with non existent target (SFTP) ([#7974](https://trac.cyberduck.io/ticket/7974))
* [Bugfix] Only single login attempt possible (WebDAV) ([#7940](https://trac.cyberduck.io/ticket/7940))
* [Bugfix] Expanding a folder resets the scroll position (Windows) ([#7941](https://trac.cyberduck.io/ticket/7941))

4.4.4
* [Feature] Default region selected in create folder panel (Openstack Swift) ([#7678](https://trac.cyberduck.io/ticket/7678))
* [Feature] Two-factor authentication (SFTP, Google Authenticator) ([#7573](https://trac.cyberduck.io/ticket/7573))
* [Bugfix] Support TLS 1.1 and TLS 1.2 (Windows) ([#7637](https://trac.cyberduck.io/ticket/7637))
* [Bugfix] Failure validating some certificates with untrusted root certificate authority (SYSS-2014-004) (Windows)
* [Bugfix] File segments not deleted when a large object is overwritten or manifest is deleted (Openstack Swift) (#7682, #7679)
* [Bugfix] Re-authentication failure (Openstack Swift) ([#7608](https://trac.cyberduck.io/ticket/7608))
* [Bugfix] Downloading dynamic large object fails (Openstack Swift) ([#7693](https://trac.cyberduck.io/ticket/7693))
* [Bugfix] Cannot update ACL with anonymous grant (S3) ([#7726](https://trac.cyberduck.io/ticket/7726))
* [Bugfix] Could not generate DH keypair (FTP-TLS, Windows) ([#7738](https://trac.cyberduck.io/ticket/7738))
* [Bugfix] ACL not maintained when replacing files (S3) ([#7756](https://trac.cyberduck.io/ticket/7756))
* [Bugfix] UNIX permissions not maintained when replacing files (SFTP)
* [Bugfix] Handle missing directory marker files objects on delete (Openstack Swift) ([#7876](https://trac.cyberduck.io/ticket/7876))
* [Bugfix] Make "Skip" transfer option only apply to files ([#7653](https://trac.cyberduck.io/ticket/7653))
* [Bugfix] Regular expression filter ignored for synchronization transfer ([#7840](https://trac.cyberduck.io/ticket/7840))
* [Bugfix] Interoperability with providers with no versioning support (S3) ([#7841](https://trac.cyberduck.io/ticket/7841))
* [Bugfix] Provide X-Cdn-Ios-Uri (Rackspace Cloudfiles)
* [Bugfix] Local file timestamp changed during file compare ([#7789](https://trac.cyberduck.io/ticket/7789))
* [Bugfix] Option to follow redirects on PUT (WebDAV) ([#6586](https://trac.cyberduck.io/ticket/6586))

4.4.3
* [Feature] Support TLS 1.1 and TLS 1.2 ([#7637](https://trac.cyberduck.io/ticket/7637))
* [Bugfix] Permission change for folders on upload ([#7635](https://trac.cyberduck.io/ticket/7635))
* [Bugfix] Some files downloaded get truncated (SFTP) ([#7641](https://trac.cyberduck.io/ticket/7641))

4.4.2
* [Feature] Copy temporary signed URLs (OpenStack) ([#7018](https://trac.cyberduck.io/ticket/7018))
* [Bugfix] Fallback on missing bulk delete support (Openstack Swift) ([#7603](https://trac.cyberduck.io/ticket/7603))
* [Bugfix] Accessibility for blind users in Transfer window (VoiceOver) ([#1343](https://trac.cyberduck.io/ticket/1343))

4.4.1
* [Feature] Large (5GB) object uploads (Openstack Swift) ([#6056](https://trac.cyberduck.io/ticket/6056))
* [Feature] Copy temporary signed URLs (Openstack Swift) ([#7018](https://trac.cyberduck.io/ticket/7018))
* [Bugfix] Code signing requirements for embedded bundles (Mac)
* [Bugfix] Temporary URL for containers with whitespace (Openstack Swift)
* [Bugfix] Prompt to switch to secured FTP Connection always shows ([#7510](https://trac.cyberduck.io/ticket/7510))
* [Bugfix] Sandboxing denies access to SSH keys (Mac) ([#7208](https://trac.cyberduck.io/ticket/7208))
* [Bugfix] Partial authentication failure (SFTP) ([#7536](https://trac.cyberduck.io/ticket/7536))
* [Bugfix] In Info panel ACL tab is disabled (S3, Windows) ([#7506](https://trac.cyberduck.io/ticket/7506))
* [Bugfix] Interoperability with WinSSHD (SFTP) ([#7522](https://trac.cyberduck.io/ticket/7522))
* [Bugfix] Softlayer connection profiles (Openstack Swift) ([#7522](https://trac.cyberduck.io/ticket/7522))
* [Bugfix] Prompt for known_hosts file if sandboxing denies access and store security scoped bookmark in Preferences (SFTP, Mac)
* [Bugfix] Prompt for private key file if sandboxing denies access and store security scoped bookmark in bookmark (SFTP, Mac)
* [Bugfix] Missing CDN URLs (Openstack Swift, Windows) ([#7532](https://trac.cyberduck.io/ticket/7532))
* [Bugfix] Content type reset to application/octet-stream (S3) ([#7598](https://trac.cyberduck.io/ticket/7598))
* [Bugfix] Listing directory failure with missing permission to read symlink target (SFTP) ([#7556](https://trac.cyberduck.io/ticket/7556))
* [Bugfix] Fallback on missing bulk delete support (Openstack Swift) ([#7603](https://trac.cyberduck.io/ticket/7603))

4.4
* [Feature] Performance improvements
* [Feature] No Java installation requirement (Mac)
* [Feature] Resumable multipart uploads (S3) ([#6208](https://trac.cyberduck.io/ticket/6208))
* [Feature] Bucket lifecycle configuration to archive objects in Glacier (S3) ([#6830](https://trac.cyberduck.io/ticket/6830))
* [Feature] Keystone (2.0) authentication support (OpenStack) ([#6330](https://trac.cyberduck.io/ticket/6330))
* [Feature] Multiple region support (OpenStack) ([#6902](https://trac.cyberduck.io/ticket/6902))
* [Feature] Set container metadata (OpenStack)
* [Feature] Edit non prefixed headers (OpenStack) ([#7209](https://trac.cyberduck.io/ticket/7209))
* [Feature] Support ECHDE cipher suites (WebDAV) ([#7344](https://trac.cyberduck.io/ticket/7344))
* [Feature] Select region when creating new container (S3, OpenStack)
* [Feature] Confirmation alert on move and rename in browser ([#595](https://trac.cyberduck.io/ticket/595))
* [Feature] Transfer action to skip existing files that match checksum, size or timestamp ([#6500](https://trac.cyberduck.io/ticket/6500))
* [Feature] Fullscreen option for browser window (Mac)
* [Feature] Remember window position of browser window when saving workspace (Mac)
* [Feature] Preview file status in overwrite prompt when choosing transfer action
* [Feature] Option to continue or cancel on failure transferring multiple files
* [Feature] Display download progress in Finder and Dock (Mac)
* [Feature] Enable website configuration for buckets (GreenQloud)
* [Feature] Option to change default port for custom origin (CloudFront) ([#5460](https://trac.cyberduck.io/ticket/5460))
* [Feature] Upload with temporary name when saving from external editor
* [Feature] CDN (Akamai) configuration for HP Cloud (Openstack)
* [Bugfix] Cannot edit non prefixed headers (OpenStack) ([#7209](https://trac.cyberduck.io/ticket/7209))
* [Bugfix] Quick Look does not display preview (Mac) ([#7231](https://trac.cyberduck.io/ticket/7231))
* [Bugfix] Failure duplicating files (FTP) ([#7224](https://trac.cyberduck.io/ticket/7224))
* [Bugfix] Save workspace does not work for multiple browser sessions to the same server ([#7213](https://trac.cyberduck.io/ticket/7213))
* [Bugfix] Permission error for upload to write-only folder ([#6240](https://trac.cyberduck.io/ticket/6240))
* [Bugfix] Authentication with none password (SFTP) ([#7322](https://trac.cyberduck.io/ticket/7322))
* [Bugfix] Interoperability issues (WebDAV) ([#7227](https://trac.cyberduck.io/ticket/7227))
* [Bugfix] Subsequent edit fails ([#7248](https://trac.cyberduck.io/ticket/7248))
* [Bugfix] Omit absolute paths in archives (ZIP, TAR) ([#6644](https://trac.cyberduck.io/ticket/6644))
* [Bugfix] Connect mode fallback failures (FTP) ([#5385](https://trac.cyberduck.io/ticket/5385))
* [Bugfix] Directory placeholder not displayed if same name as object (OpenStack) ([#6988](https://trac.cyberduck.io/ticket/6988))
* [Bugfix] Cannot set custom keyboard shortcut (Mac) ([#7045](https://trac.cyberduck.io/ticket/7045))
* [Bugfix] Registration with Growl fails (Mac) ([#7274](https://trac.cyberduck.io/ticket/7274))
* [Bugfix] Changing permissions resets extended access rights flags (SFTP, FTP) ([#3790](https://trac.cyberduck.io/ticket/3790))
* [Bugfix] Remember browser column widths (Mac) ([#6034](https://trac.cyberduck.io/ticket/6034))
* [Bugfix] Refresh list of thirdparty application bookmarks to import ([#6141](https://trac.cyberduck.io/ticket/6141))
* [Bugfix] PRET support not working anymore ([#7427](https://trac.cyberduck.io/ticket/7427))

4.3.1
* [Bugfix] Modification date shown as 01.01.1970 (Windows) ([#7177](https://trac.cyberduck.io/ticket/7177))
* [Bugfix] Filename for edited file includes absolute path (Windows) ([#7175](https://trac.cyberduck.io/ticket/7175))
* [Bugfix] Change of default editor (Windows) ([#7178](https://trac.cyberduck.io/ticket/7178))
* [Bugfix] Change bandwidth limit ([#7189](https://trac.cyberduck.io/ticket/7189))
* [Bugfix] Always asks for permsission to access Keychain (10.6) ([#7202](https://trac.cyberduck.io/ticket/7202))

4.3
* [Localize] Arabic Localization
* [Feature] Retina resolution support (10.8) ([#6760](https://trac.cyberduck.io/ticket/6760))
* [Feature] Notification center support (10.8) ([#6792](https://trac.cyberduck.io/ticket/6792))
* [Feature] Gatekeeper support (10.8) ([#6888](https://trac.cyberduck.io/ticket/6888))
* [Feature] One click setup for analytics with Qloudstat (S3, CloudFront, Cloudfiles, Google Cloud Storage)
* [Feature] Bucket location in South America (São Paulo) Region (S3)
* [Feature] Bucket location in Asia Pacific (Sydney) Region (S3)
* [Feature] Multi-Object Delete (S3)
* [Feature] Interoperability with HP Cloud (Openstack)
* [Feature] Interoperability with Lunacloud Storage (S3)
* [Feature] Website Configuration (Google Cloud Storage)
* [Feature] Website Configuration (Cloudfiles)
* [Feature] Edit container metadata (Cloudfiles)
* [Feature] Reuse Session key on data connection (FTP/TLS) ([#5087](https://trac.cyberduck.io/ticket/5087))
* [Feature] Suppress sleep mode during file transfers ([#6479](https://trac.cyberduck.io/ticket/6479))
* [Bugfix] Hangs editing file in external editor (10.8.2) ([#6878](https://trac.cyberduck.io/ticket/6878))
* [Bugfix] Moving folders deletes the folder (Cloudfiles) ([#6442](https://trac.cyberduck.io/ticket/6442))
* [Bugfix] Copy folder from server to server ([#6440](https://trac.cyberduck.io/ticket/6440))
* [Bugfix] Duplicate folder ([#6495](https://trac.cyberduck.io/ticket/6495))
* [Bugfix] Interoperability with latest API (CloudFront)
* [Bugfix] Crash in Rendezvous resolver ([#6814](https://trac.cyberduck.io/ticket/6814))
* [Bugfix] Java optional install required (App Store) ([#6090](https://trac.cyberduck.io/ticket/6090))
* [Bugfix] Changes in editor not uploaded when choosing save on close ([#6590](https://trac.cyberduck.io/ticket/6590))
* [Bugfix] Versioning for directory placeholders (S3) ([#5748](https://trac.cyberduck.io/ticket/5748))
* [Bugfix] Duplicating file does not retain permissions ([#6525](https://trac.cyberduck.io/ticket/6525))
* [Bugfix] SSL hostname verification on HTTP redirects
* [Remove] Dropped support Azure Blob Storage connections (Azure)
* [Remove] Dropped support Dropbox connections (Dropbox)
* [Remove] Dropped support Google Drive connections (Google Docs)

4.2.1
* [Bugfix] Edited files not uploaded ([#6399](https://trac.cyberduck.io/ticket/6399))
* [Bugfix] Content-Type not set to proper MIME type of file on upload (WebDAV) ([#6433](https://trac.cyberduck.io/ticket/6433))
* [Bugfix] Transfer status not saved ([#6430](https://trac.cyberduck.io/ticket/6430))

4.2
* [Feature] Drag files between browser windows to copy files from server to server
* [Feature] Support server side encryption (S3)
* [Feature] Configure access logs for buckets (Google Cloud Storage)
* [Feature] Interoperability with OpenSSH to read private key from keychain.
* [Feature] Connect to multiple projects (Google Cloud Storage) ([#5955](https://trac.cyberduck.io/ticket/5955))
* [Feature] Support OAuth 2.0 Authentication (Google Cloud Storage) ([#5955](https://trac.cyberduck.io/ticket/5955))
* [Feature] Support US West (Oregon) location (S3)
* [Feature] Try public key authentication login with default keys from OpenSSH (SFTP) ([#3982](https://trac.cyberduck.io/ticket/3982))
* [Bugfix] Performance improvement preparing files for download
* [Bugfix] ETag mismatch deleting files (Google Docs) ([#6204](https://trac.cyberduck.io/ticket/6204))
* [Bugfix] Creating new file (WebDAV) ([#6341](https://trac.cyberduck.io/ticket/6341))
* [Bugfix] No error message when SFTP subsystem is disabled (SFTP) ([#5902](https://trac.cyberduck.io/ticket/5902))

4.1.3
* [Bugfix] Freeze after file transfer is complete ([#6183](https://trac.cyberduck.io/ticket/6183))
* [Bugfix] Connection failure to Eucalyptus Cloud installation (S3) ([#6206](https://trac.cyberduck.io/ticket/6206))
* [Feature] Copy streaming URLs (Cloudfiles)
* [Feature] Option to set ACLs for Google Apps Domain (Google Cloud Storage)
* [Feature] Option to set ACLs for Google Group Email Address (Google Cloud Storage)
* [Feature] Allow setting non-metadata headers (Cloudfiles, Openstack Swift) (#6191, #6239)

4.1.2
* [Feature] Rename files (Cloudfiles)
* [Feature] Duplicate files (Cloudfiles)
* [Bugfix] Select filename only without suffix in browser ([#6158](https://trac.cyberduck.io/ticket/6158))

4.1.1
* [Feature] Importer for WinSCP bookmarks (Windows)
* [Bugfix] Runtime crash (App Store)
* [Bugfix] Connection failure with email address as username (WebDAV) ([#6066](https://trac.cyberduck.io/ticket/6066))
* [Bugfix] Synchronisation comparison failures ([#6074](https://trac.cyberduck.io/ticket/6074))
* [Bugfix] Error listing folders (Openstack Swift) ([#6089](https://trac.cyberduck.io/ticket/6089))

4.1
* [Localize] Bulgarian Localization
* [Feature] Replaced WebDAV protocol implementation
* [Feature] Sort bookmarks by nickname, hostname or protocol ([#5925](https://trac.cyberduck.io/ticket/5925))
* [Feature] Set preferred default editor (Windows) ([#5729](https://trac.cyberduck.io/ticket/5729))
* [Feature] IPv6 support (Windows) ([#5699](https://trac.cyberduck.io/ticket/5699))
* [Feature] Enable access logs for custom origin distributions (CloudFront)
* [Feature] Choose target bucket for access logs (CloudFront)
* [Feature] Choose target bucket for access logs (S3)
* [Feature] CDN (Akamai) configuration (Cloudfiles UK) ([#5989](https://trac.cyberduck.io/ticket/5989))
* [Feature] Filter bookmarks by comments ([#5926](https://trac.cyberduck.io/ticket/5926))
* [Feature] Upload arbitrary file types (Google Docs)
* [Feature] Support trackpad gestures for navigation and selection (Mac) ([#2793](https://trac.cyberduck.io/ticket/2793))
* [Bugfix] Delete only trashes documents (Google Docs) ([#5873](https://trac.cyberduck.io/ticket/5873))
* [Bugfix] Wrong timestamp in transfer prompt ([#5916](https://trac.cyberduck.io/ticket/5916))
* [Bugfix] Uploading file to collection places it in root folder instead (Google Docs) ([#5856](https://trac.cyberduck.io/ticket/5856))
* [Bugfix] Support for extended character sets (Windows) ([#6016](https://trac.cyberduck.io/ticket/6016))
* [Bugfix] Limited to 10'000 containers (Cloudfiles) ([#6037](https://trac.cyberduck.io/ticket/6037))
* [Bugfix] SSL version number incompatibility ([#5061](https://trac.cyberduck.io/ticket/5061))
* [Bugfix] Donation key validation failure (10.5) ([#5846](https://trac.cyberduck.io/ticket/5846))
* [Bugfix] CDN URL for files only available after opening Info window (Cloudfiles) ([#6040](https://trac.cyberduck.io/ticket/6040))
* [Bugfix] Interoperability (FTP) (#5866, #5949)
* [Bugfix] Copy and edit filenames in browser (Windows) (#5336, #6039)
* [Bugfix] Preserve modification dates for folders on upload ([#6048](https://trac.cyberduck.io/ticket/6048))

4.0.2
* [Feature] SSL URL for files in Akamai CDN enabled containers (Cloud Files)
* [Feature] Transcript (SFTP) ([#2944](https://trac.cyberduck.io/ticket/2944))
* [Bugfix] Incomplete transfer synchronizing root directory (WebDAV) ([#5662](https://trac.cyberduck.io/ticket/5662))
* [Bugfix] Wrong version downloaded (S3 Versioning) ([#5758](https://trac.cyberduck.io/ticket/5758))
* [Bugfix] Interoperability (FTP) (#5763, #5757, #5772, #5771, #5590)
* [Bugfix] Wrong local path for uploads with file chooser (Windows) ([#5590](https://trac.cyberduck.io/ticket/5590))
* [Bugfix] Uploads larger than 100MB fail (Google Docs) ([#5712](https://trac.cyberduck.io/ticket/5712))
* [Bugfix] Replace password for private key fails (SFTP) ([#5754](https://trac.cyberduck.io/ticket/5754))
* [Bugfix] Purge files with delimiters in object key fails (Cloudfiles) ([#5822](https://trac.cyberduck.io/ticket/5822))

4.0.1
* [Bugfix] Uploading folders fails to create directories ([#5749](https://trac.cyberduck.io/ticket/5749))

4.0
* [Localize] Ukrainian Localization
* [Feature] Version for Microsoft Windows XP, Windows Vista & Windows 7.
* [Feature] Replaced protocol implementation (FTP)
* [Feature] Connecting to Windows Azure Blob Storage ([#3938](https://trac.cyberduck.io/ticket/3938))
* [Feature] Connecting to Dropbox
* [Feature] Copy and paste files using menu item to duplicate
* [Feature] Multipart Uploads with parallelism (S3) ([#5487](https://trac.cyberduck.io/ticket/5487))
* [Feature] Support new 5TB Object Size Limit (S3)
* [Feature] Upload item in Finder context menu to upload selected file (Mac)
* [Feature] Upload item in Services menu of thirdparty programs main menu supporting files and folders (Mac)
* [Feature] Select bookmark to upload to when dragging files to application
* [Feature] Invalidation (Purge) of files in CDN (Cloudfiles/Akamai)
* [Feature] Skip option not available in transfer prompt ([#1159](https://trac.cyberduck.io/ticket/1159))
* [Feature] User interface to create symbolic links (SFTP) ([#1724](https://trac.cyberduck.io/ticket/1724))
* [Feature] Support for Asia Pacific (Tokyo) location (S3)
* [Feature] Website endpoint configuration option for buckets (S3)
* [Feature] CDN configuration for website endpoints as custom origin (CloudFront)
* [Bugfix] Limit number of concurrent transfers (#5539, #5624)
* [Bugfix] Qeued transfers start in random order ([#5632](https://trac.cyberduck.io/ticket/5632))
* [Bugfix] Dragging into topmost folder in browser ([#1945](https://trac.cyberduck.io/ticket/1945))
* [Bugfix] ACLs getting dropped when updating metadata (S3) ([#5571](https://trac.cyberduck.io/ticket/5571))
* [Bugfix] .CDN_ACCESS_LOGS folder listing is empty (Cloudfiles) ([#5350](https://trac.cyberduck.io/ticket/5350))
* [Bugfix] Unicode normalization for filenames in upload ([#5162](https://trac.cyberduck.io/ticket/5162))
* [Bugfix] Duplicate file breaks editing ([#5524](https://trac.cyberduck.io/ticket/5524))
* [Bugfix] Large transfer history causes slowdown ([#2889](https://trac.cyberduck.io/ticket/2889))
* [Bugfix] List all files regardless of document ownership ([#5570](https://trac.cyberduck.io/ticket/5570)) (Google Docs)
* [Bugfix] Images always converted to documents ([#5601](https://trac.cyberduck.io/ticket/5601)) (Google Docs)
* [Bugfix] Preserve symbolic links in transfers ([#1860](https://trac.cyberduck.io/ticket/1860)) (SFTP)
* [Bugfix] High CPU usage after transfer has completed ([#5640](https://trac.cyberduck.io/ticket/5640))
* [Bugfix] Multiple distributions created (CloudFront) ([#5675](https://trac.cyberduck.io/ticket/5675))

3.8.1
* [Bugfix] Fails to display custom language preference ([#5500](https://trac.cyberduck.io/ticket/5500))
* [Bugfix] Clear data channels not supported ([#5509](https://trac.cyberduck.io/ticket/5509)) (FTP-TLS)
* [Bugfix] Some files not displayed ([#5505](https://trac.cyberduck.io/ticket/5505)) (FTP)
* [Bugfix] Wrong file size reported for uploads ([#5503](https://trac.cyberduck.io/ticket/5503))

3.8
* [Feature] CDN configuration with custom origin server (Amazon CloudFront)
* [Feature] CDN invalidation requests (Amazon CloudFront) ([#5197](https://trac.cyberduck.io/ticket/5197))
* [Feature] Connecting to Swift Storage (Openstack)
* [Feature] FireFTP Bookmarks Importer
* [Feature] CrossFTP Bookmarks Importer
* [Feature] Perform MD5 hash calculation during upload ([#5186](https://trac.cyberduck.io/ticket/5186)) (S3, Cloudfiles)
* [Feature] Rename existing files on upload or download ([#5117](https://trac.cyberduck.io/ticket/5117))
* [Feature] Show last access timestamp in history ([#3805](https://trac.cyberduck.io/ticket/3805))
* [Feature] Synchronize comparing MD5 checksum of file (S3, Cloudfiles, Dropbox, Azure)
* [Feature] Display decimal file size (OS X 10.6) #3771)
* [Feature] Display relative date in browser (OS X 10.6)
* [Bugfix] Slow SFTP transfers ([#185](https://trac.cyberduck.io/ticket/185))
* [Bugfix] Reduced upload preparation time for thousands of files
* [Bugfix] Support for S3Fox directory placeholders (S3)
* [Bugfix] Support for Google Cloud Storage Console directory placeholders (S3)
* [Bugfix] Support for s3sync.rb directory placeholders ([#5374](https://trac.cyberduck.io/ticket/5374)) (S3)
* [Bugfix] Connect through HTTP/HTTPS proxy ([#5379](https://trac.cyberduck.io/ticket/5379)) (Google Docs)
* [Bugfix] Changing storage class fails (S3)
* [Bugfix] Apply ACLs recursively (S3)
* [Bugfix] Apply custom HTTP headers recursively (S3)
* [Bugfix] Interoperability (#2609, #4231, #2915) (WebDAV)
* [Bugfix] SSL version number incompatibility ([#5061](https://trac.cyberduck.io/ticket/5061))
* [Bugfix] Preserve upload modification date does not work for folders ([#3017](https://trac.cyberduck.io/ticket/3017))
* [Bugfix] Uploading large documents might fail ([#5411](https://trac.cyberduck.io/ticket/5411)) (Google Docs)
* [Bugfix] Web URL configuration fails for relative default path ([#4012](https://trac.cyberduck.io/ticket/4012))
* [Bugfix] Support CIDR styled patterns for hosts excluded from proxy settings ([#5142](https://trac.cyberduck.io/ticket/5142))

3.7
* [Feature] Option to upload with temporary name and rename file after transfer is complete ([#4165](https://trac.cyberduck.io/ticket/4165))
* [Feature] Copy files between browser windows with different sessions ([#21](https://trac.cyberduck.io/ticket/21))
* [Feature] Option to display hidden files in upload prompt ([#1243](https://trac.cyberduck.io/ticket/1243))
* [Feature] Import Transmit favorites ([#3073](https://trac.cyberduck.io/ticket/3073))
* [Feature] Copy and open multiple URLs ([#5135](https://trac.cyberduck.io/ticket/5135))
* [Feature] Support for PuTTY private key format (SFTP) ([#5322](https://trac.cyberduck.io/ticket/5322))
* [Feature] Duplicate Bookmarks using drag and drop with option key
* [Feature] Display only affected files in synchronization preview ([#5226](https://trac.cyberduck.io/ticket/5226))
* [Feature] Change update source to snapshot builds in Preferences
* [Bugfix] Files pasted upload to parent directory ([#5155](https://trac.cyberduck.io/ticket/5155))
* [Bugfix] Uploading .xlsx or .docx documents fails with permission error ([#5169](https://trac.cyberduck.io/ticket/5169)) (Google Docs)
* [Bugfix] Reading and writing ACLs (Google Docs)
* [Bugfix] Interoperability with cPanel Web Disk (WebDAV) ([#5188](https://trac.cyberduck.io/ticket/5188))
* [Bugfix] Downloading previous versions of file (S3) ([#5217](https://trac.cyberduck.io/ticket/5217))
* [Bugfix] Skip directories with matching timestamp from synchronization ([#557](https://trac.cyberduck.io/ticket/557))
* [Bugfix] Not using proper storage URL ([#5216](https://trac.cyberduck.io/ticket/5216)) (Swift OpenStack)
* [Bugfix] Proxy connection failure ([#5239](https://trac.cyberduck.io/ticket/5239)) (S3)
* [Bugfix] Crash importing Transmit bookmarks for some users ([#5351](https://trac.cyberduck.io/ticket/5351))

3.6.1
* [Bugfix] Permissions reset (FTP) ([#5132](https://trac.cyberduck.io/ticket/5132))
* [Bugfix] Failure reading symbolic link target for directories (SFTP) ([#5141](https://trac.cyberduck.io/ticket/5141))

3.6
* [Feature] Connecting to Google Cloud Storage (Google Cloud Storage)
* [Feature] Edit Access Control List (ACL) ([#3191](https://trac.cyberduck.io/ticket/3191)) (S3, Google Cloud Storage, Google Docs)
* [Feature] Enable Bucket Versioning ([#4511](https://trac.cyberduck.io/ticket/4511)) (S3)
* [Feature] Enable Bucket Multi-Factor Authentication Delete ([#4510](https://trac.cyberduck.io/ticket/4510)) (S3)
* [Feature] Display previous file versions in browser (S3)
* [Feature] Download previous file version (S3)
* [Feature] Revert to previous file version (S3)
* [Feature] Default root file (index.html) configuration for distribution (CloudFront)
* [Feature] Eucalyptus Walrus support enabled by default (S3)
* [Feature] Duplicate Bookmark ([#3385](https://trac.cyberduck.io/ticket/3385))
* [Feature] Import bookmarks from thirdparty applications (Filezilla, Fetch, Interarchy, Flow) ([#3373](https://trac.cyberduck.io/ticket/3373))
* [Feature] Copy directory listing to clipboard ([#2372](https://trac.cyberduck.io/ticket/2372))
* [Feature] Support for thirdparty terminal applications ([#2987](https://trac.cyberduck.io/ticket/2987))
* [Feature] Change SSH options for open in Terminal.app ([#4232](https://trac.cyberduck.io/ticket/4232))
* [Feature] Unsecure connection warning when password is transmitted in plaintext
* [Feature] Alert to change connection to TLS if server supports AUTH TLS (FTP)
* [Feature] Edit metadata for multiple files (S3, Google Cloud Storage)
* [Feature] Menu items to copy HTTP, CDN, signed & authenticated URLs to clipboard ([#4207](https://trac.cyberduck.io/ticket/4207))
* [Feature] Menu items to open HTTP, CDN, signed & authenticated URLs in Web Browser
* [Feature] Move and rename files and folders (Google Docs)
* [Feature] Update size display incrementally while calculating recursively ([#3213](https://trac.cyberduck.io/ticket/3213))
* [Feature] Hidden preference to change SSH options for 'Open in Terminal.app' ([#4232](https://trac.cyberduck.io/ticket/4232))
* [Feature] Batch editing of S3 metadata (Google Cloud Storage, S3) ([#5105](https://trac.cyberduck.io/ticket/5105))
* [Feature] Show number of active transfers in Dock ([#3808](https://trac.cyberduck.io/ticket/3808))
* [Feature] Copy directory listing to clipboard ([#2372](https://trac.cyberduck.io/ticket/2372))
* [Bugfix] Improved interoperability with Eucalyptus Walrus (S3)
* [Bugfix] Improved interoperability with Dunkel Cloud Storage (S3)
* [Bugfix] Trust validation failure with self-signed certificates for HTTP redirects (WebDAV) ([#2443](https://trac.cyberduck.io/ticket/2443))
* [Bugfix] Drag and drop of a file over a bookmark to upload ([#4562](https://trac.cyberduck.io/ticket/4562))
* [Bugfix] Connect authenticated to thirdparty buckets (S3) ([#4480](https://trac.cyberduck.io/ticket/4480))
* [Bugfix] Connections stalls after long idle (SFTP) (#5073, #4214, #3941)
* [Bugfix] Dragging files for upload is slow ([#4141](https://trac.cyberduck.io/ticket/4141))
* [Bugfix] STAT fails for directories containing spaces in filename (FTP) ([#2500](https://trac.cyberduck.io/ticket/2500))
* [Bugfix] Upload to drop box gives file listing error ([#2552](https://trac.cyberduck.io/ticket/2552))
* [Bugfix] Incompatibility with Akamai NetStorage (SFTP) ([#4015](https://trac.cyberduck.io/ticket/4015))
* [Bugfix] Usability of changing permissions in Info panel (FTP, SFTP) ([#3930](https://trac.cyberduck.io/ticket/3930))

3.5.1
* [Bugfix] Crash opening Info window ([#4536](https://trac.cyberduck.io/ticket/4536))
* [Bugfix] Nested folder not visible ([#4534](https://trac.cyberduck.io/ticket/4534)) (Google Docs)
* [Bugfix] Upload into folder not possible ([#4550](https://trac.cyberduck.io/ticket/4550)) (Google Docs)
* [Bugfix] Downloading spreadsheets fails with permission error ([#4538](https://trac.cyberduck.io/ticket/4538)) (Google Docs)
* [Feature] Replacing documents on upload adding new revision (Google Docs)

3.5
* [Localize] Slovenian Localization
* [Localize] Romanian Localization
* [Feature] Connecting to Google Docs (Google Docs)
* [Feature] Convert uploads to Google Docs format (Google Docs)
* [Feature] Download format preferences for documents (Google Docs)
* [Feature] Optical Character Recognition (OCR) for image uploads (Google Docs)
* [Feature] Support for Asia Pacific (Singapore) location (S3)
* [Feature] Support for keyboard-interactive authentiation using SecurID ([#4459](https://trac.cyberduck.io/ticket/4459)) (SFTP)
* [Feature] HTTP plain text connection support to connect to third party S3 servers ([#4181](https://trac.cyberduck.io/ticket/4181)) (S3)
* [Feature] Custom metadata attributes ([#4063](https://trac.cyberduck.io/ticket/4063)) (S3, Rackspace Cloud Files)
* [Feature] Configure Access Logs for CloudFront streaming distributions (S3)
* [Feature] Option for Reduced Redundancy Storage (RRS) (S3)
* [Bugfix] Overwrite files causes append (S3) ([#4419](https://trac.cyberduck.io/ticket/4419))
* [Bugfix] Connect anonymously to public buckets (S3) ([#4480](https://trac.cyberduck.io/ticket/4480))
* [Bugfix] Help menu items disabled ([#4406](https://trac.cyberduck.io/ticket/4406))

3.4.2
* [Localize] Georgian Localization
* [Feature] Choose localization in preferences
* [Feature] Option to disable use of system proxy settings in preferences
* [Feature] Allow arbitrary input for bandwidth throttle ([#1746](https://trac.cyberduck.io/ticket/1746))
* [Feature] Read hostname alias from ~/.ssh/config (SFTP) ([#3819](https://trac.cyberduck.io/ticket/3819))
* [Feature] Allow page setup configuration for browser view print ([#4139](https://trac.cyberduck.io/ticket/4139))
* [Bugfix] Connections fail with at sign in username (WebDAV) ([#4097](https://trac.cyberduck.io/ticket/4097))
* [Bugfix] SOCKS proxy support broken ([#3803](https://trac.cyberduck.io/ticket/3803))

3.4.1
* [Feature] Display checksum of files in Info panel (S3, Rackspace Cloud Files) ([#4043](https://trac.cyberduck.io/ticket/4043))
* [Bugfix] Incompatibilities when running on OS X 10.5 ([#4102](https://trac.cyberduck.io/ticket/4102))

3.4
* [Localize] Serbian Localization
* [Feature] Support external editors with no ODB support for different file types ([#3834](https://trac.cyberduck.io/ticket/3834))
* [Feature] Allow user to define applications to use for editing ([#3112](https://trac.cyberduck.io/ticket/3112))
* [Feature] Configure Amazon CloudFront Streaming Distributions (S3) ([#4069](https://trac.cyberduck.io/ticket/4069))

3.3.1
* [Feature] Support for new US West Location (S3)
* [Bugfix] Bookmark menu ([#3924](https://trac.cyberduck.io/ticket/3924))
* [Bugfix] SOCKS proxy support broken ([#3803](https://trac.cyberduck.io/ticket/3803))
* [Bugfix] Container listing limited to 10000 files (Rackspace Cloud Files) ([#3950](https://trac.cyberduck.io/ticket/3950))
* [Bugfix] Synchronisation does not transfer files with equal size ([#3100](https://trac.cyberduck.io/ticket/3100))
* [Bugfix] Selection in synchronize prompt ([#3901](https://trac.cyberduck.io/ticket/3901))
* [Bugfix] Bonjour bookmarks ignore TXT record with path and credentials attributes ([#3984](https://trac.cyberduck.io/ticket/3984))
* [Bugfix] Donation prompt cannot be supressed ([#3937](https://trac.cyberduck.io/ticket/3937))
* [Bugfix] Error when uploading file with different display name from real filename ([#4041](https://trac.cyberduck.io/ticket/4041))
* [Bugfix] Quick Connect needs extra keystroke to connect ([#3801](https://trac.cyberduck.io/ticket/3801))
* [Bugfix] Password in non-default keychain are copied to login keychain ([#2878](https://trac.cyberduck.io/ticket/2878))

3.3
* [Feature] Octal input field for permissions ([#13](https://trac.cyberduck.io/ticket/13))
* [Bugfix] Connect to default bookmark for new browser window ([#3798](https://trac.cyberduck.io/ticket/3798))
* [Bugfix] Excessive Growl notifications ([#2388](https://trac.cyberduck.io/ticket/2388))
* [Bugfix] Icon set explicitly for every downloaded file ([#3824](https://trac.cyberduck.io/ticket/3824))

3.3b4
* [Bugfix] Fails to launch with Japanese locale
* [Bugfix] Duplicate files in browser listing when sorting by modification date ([#3745](https://trac.cyberduck.io/ticket/3745))
* [Bugfix] Wrong menu item font size ([#3736](https://trac.cyberduck.io/ticket/3736))
* [Bugfix] Various bugfixes
* [Feature] Toolbar interface for Info window
* [Feature] CDN Log Retention (Rackspace Cloud Files)

3.3b3
* [Feature] PPC support again available
* [Bugfix] Some localizations broken ([#3648](https://trac.cyberduck.io/ticket/3648))
* [Bugfix] Various bugfixes

3.3b2
* [Feature] 64-bit support
* [Bugfix] Various bugfixes

3.3b1
* [Bugfix] Incompatibility with Mac OS X 10.6 (Snow Leopard) ([#3039](https://trac.cyberduck.io/ticket/3039))
* [Feature] New application icon (Thanks to Dietmar Kerschner)

3.2.1
* [Feature] Create placeholder objects for virtual directory (S3)
* [Feature] Info window displays signed temporary public URL with a default validity of 24 hours (S3)
* [Feature] Info window displays BitTorrent URL of file (S3)
* [Feature] Option to enable Bucket Access Logging in the Info window (S3)
* [Feature] Option to enable CloudFront Access Logging in the Info window (S3)
* [Feature] Updated to Cloudfront API Version 2009-04-02 (S3)
* [Feature] Access third party buckets using anonymous login (S3)
* [Feature] Editable hostname to connect to third party services implementing S3 ([#3125](https://trac.cyberduck.io/ticket/3125)) (S3)
* [Feature[ Option to specify object expiration using Cache-Control header ([#3185](https://trac.cyberduck.io/ticket/3185)) (S3)
* [Bugfix] MobileMe iDisk upload failures (WebDAV) ([#3149](https://trac.cyberduck.io/ticket/3149))
* [Bugfix] Status bar is draggable ([#2159](https://trac.cyberduck.io/ticket/2159))

3.2
* [Feature] Choose Time Zone for bookmark (FTP) ([#434](https://trac.cyberduck.io/ticket/434))
* [Feature] IPv6 Interoperability with support for EPSV and EPRT commands (FTP) (#2539, #2885)
* [Feature] UTF8 charset negotiation [http://tools.ietf.org/html/draft-ietf-ftpext-utf-8-option-00) (FTP)
* [Feature] Support for directory listings using MLSD (RFC 3659) (FTP)
* [Feature] Support for Modification Time (MFMT) extension [http://tools.ietf.org/html/draft-somers-ftp-mfxx-04) (FTP)
* [Feature] Support for hierarchical directory structure (Mosso)
* [Feature] Added Espresso to the list of supported editors (http://macrabbit.com/espresso/) ([#2823](https://trac.cyberduck.io/ticket/2823))
* [Feature] Display date of transfer ([#1462](https://trac.cyberduck.io/ticket/1462))
* [Feature] Display percentage transferred ([#2618](https://trac.cyberduck.io/ticket/2618))
* [Feature] Option for big sized icons in bookmark list
* [Feature] Drag URL to browser window to open connection ([#2326](https://trac.cyberduck.io/ticket/2326))
* [Feature] Display realm of HTTP authentication (WebDAV) ([#3083](https://trac.cyberduck.io/ticket/3083))
* [Bugfix] Update passwords in Keychain ([#2984](https://trac.cyberduck.io/ticket/2984))
* [Bugfix] Invalid Origin parameter when creating CloudFront distribution (S3) ([#3068](https://trac.cyberduck.io/ticket/3068))
* [Bugfix] Only reauthenticate when needed (Mosso) ([#2876](https://trac.cyberduck.io/ticket/2876))
* [Bugfix] Improve Interoperability (WebDAV) (#2974, #3076)
* [Bugfix] Different recursive permissions for directory and files ([#1787](https://trac.cyberduck.io/ticket/1787))

3.1.2
* [Feature] Enforce hostname verification (SSL)
* [Feature] Improved container listing performance (Mosso)
* [Feature] Updated bookmark icons (by Robert Curtis)
* [Bugfix] Fix Amazon CloudFront distribution configuration
* [Bugfix] Accept valid certificates without prompt if no explicit trust is given (SSL)
* [Bugfix] Files not downloaded recursively (S3)
* [Bugfix] Create and expand archives with space in filename ([#2884](https://trac.cyberduck.io/ticket/2884))
* [Bugfix] Incorrect CDN URI for public containers (Mosso) ([#2875](https://trac.cyberduck.io/ticket/2875))
* [Bugfix] Observe bandwidth setting for uploads (Mosso/WebDAV)

3.1.1
* [Bugfix] Crash if no default login name is set in Preferences ([#2852](https://trac.cyberduck.io/ticket/2852))
* [Bugfix] Upload incompatibility (WebDAV) ([#2858](https://trac.cyberduck.io/ticket/2858))

3.1
* [Feature] Amazon CloudFront Support (S3)
* [Feature] Cloud Files Support (Mosso) ([#2745](https://trac.cyberduck.io/ticket/2745))
* [Feature] Pre-configured protocol settings for MobileMe iDisk (WebDAV)
* [Feature] Send arbitrary commands over SSH using the 'Send Command...release-' interface (SFTP) ([#3](https://trac.cyberduck.io/ticket/3))
* [Feature] Archive and unarchive files and folders (ZIP, TAR etc.) (SFTP) ([#2376](https://trac.cyberduck.io/ticket/2376))
* [Feature] Clickable URL fields
* [Feature] Set quarantine attribute for launch services of downloaded files
* [Feature] Finder displays URL of downloaded files in 'Where from' in 'Get Info' window
* [Feature] Download folder in Dock bounces once when download finishes (10.5)
* [Feature] Read IdentityFile setting from OpenSSH configuration in ~/.ssh/config (SFTP) ([#152](https://trac.cyberduck.io/ticket/152))
* [Feature] Default transfer setting for regular expression of skipped files includes GIT and others ([#2829](https://trac.cyberduck.io/ticket/2829))
* [Feature] Added QuickLook button to toolbar configuration
* [Feature] Display Favicon for Web URL if available in bookmark window
* [Feature] NTLM Authentication (WebDAV) ([#2835](https://trac.cyberduck.io/ticket/2835))
* [Bugfix] Uploads fail with digest access authentication (WebDAV) ([#2268](https://trac.cyberduck.io/ticket/2268))
* [Bugfix] Microsoft SharePoint interoperability (WebDAV) ([#2223](https://trac.cyberduck.io/ticket/2223))
* [Bugfix] Manually sorting bookmarks using drag and drop works for the entire row ([#2571](https://trac.cyberduck.io/ticket/2571))
* [Bugfix] Passwords stored in non default keychain are ignored ([#2001](https://trac.cyberduck.io/ticket/2001))

3.0.3
* [Bugfix] Arbitrary Crashes ([#2142](https://trac.cyberduck.io/ticket/2142))
* [Bugfix] Various compatibility fixes for STAT listings (FTP) (#2445, #2435)
* [Bugfix] Open Web URL fails ([#2466](https://trac.cyberduck.io/ticket/2466))

3.0.2
* [Localize] Greek Localization
* [Feature] Added transcript (S3)
* [Feature] Added transcript (WebDAV)
* [Feature] Support for faster directory listings using STAT command on the control connection (FTP) ([#683](https://trac.cyberduck.io/ticket/683))
* [Feature] Display bookmark view for new browser window ([#2252](https://trac.cyberduck.io/ticket/2252))
* [Feature] Option in browser context menu to open selected folder in new browser ([#2036](https://trac.cyberduck.io/ticket/2036))
* [Feature] Option in browser context menu to add bookmark for selected folder ([#2222](https://trac.cyberduck.io/ticket/2222))
* [Feature] 'Add new bookmark' toolbar button
* [Feature] Cannot select private key in login prompt (SSH) ([#2221](https://trac.cyberduck.io/ticket/2221))
* [Feature] Authentication with both password and public key (SSH) ([#2203](https://trac.cyberduck.io/ticket/2203))
* [Bugfix] Moving or copying files to another bucket fails (S3) ([#2157](https://trac.cyberduck.io/ticket/2157))
* [Bugfix] Folders collapse in outline view when refreshing ([#2033](https://trac.cyberduck.io/ticket/2033))
* [Bugfix] Cannot connect with empty password ([#2109](https://trac.cyberduck.io/ticket/2109))
* [Bugfix] Saving when editor exits does not upload edited file ([#2120](https://trac.cyberduck.io/ticket/2120))
* [Bugfix] Crash when closing edited file before upload completes ([#2377](https://trac.cyberduck.io/ticket/2377))
* [Bugfix] Wrong selection with search filter in Transfer window ([#2336](https://trac.cyberduck.io/ticket/2336))
* [Bugfix] Stop button does not work during countdown to retry transfer ([#2121](https://trac.cyberduck.io/ticket/2121))
* [Bugfix] Transfers fail using AppleScript (SFTP) ([#2244](https://trac.cyberduck.io/ticket/2244))
* [Bugfix] Updated Growl.framework to 1.1.4

3.0.1
* [Feature] Added Taco HTML Edit to the list of supported editors (http://tacosw.com/htmledit/beta.php) ([#188](https://trac.cyberduck.io/ticket/188))
* [Bugfix] Cannot connect to servers with unknown host key ([#2044](https://trac.cyberduck.io/ticket/2044))
* [Bugfix] Cannot edit filename in Info panel ([#2049](https://trac.cyberduck.io/ticket/2049))
* [Bugfix] External editor failures (#2041, #2039, #2052, #2058)
* [Bugfix] Missing resume option in transfer prompt
* [Bugfix] Removed keep connection active feature. ([#2057](https://trac.cyberduck.io/ticket/2057))

3.0
* [Feature] Support for file renaming and copying (Amazon S3)
* [Feature] Support for copying files (WebDAV)
* [Bugfix] Transfers larger than 2GB fail (SFTP) ([#1235](https://trac.cyberduck.io/ticket/1235))

3.0b3
* [Feature] Activity Window ([#1250](https://trac.cyberduck.io/ticket/1250))

3.0b2
* [Localize] Latvian Localization
* [Feature] Quick Look files in browser ([#1580](https://trac.cyberduck.io/ticket/1580))
* [Feature] Set corresponding Web URL in bookmark. Open in browser toolbar button ([#1098](https://trac.cyberduck.io/ticket/1098))
* [Bugfix] APPE command broken (FTP) ([#1915](https://trac.cyberduck.io/ticket/1915))

3.0b1
* [Feature] Amazon Simple Storage Service (S3) protocol support (http://aws.amazon.com/s3) ([#1725](https://trac.cyberduck.io/ticket/1725))
* [Feature] WebDAV protocol support ([#464](https://trac.cyberduck.io/ticket/464))
* [Feature] Search bookmarks ([#916](https://trac.cyberduck.io/ticket/916))
* [Feature] Bookmark editor inside browser window instead of drawer
* [Feature] Read trust settings for certificates from Keychain
* [Feature] Different editors for different file types ([#146](https://trac.cyberduck.io/ticket/146))
* [Feature] Skip option not available in transfer prompt ([#1159](https://trac.cyberduck.io/ticket/1159))
* [Bugfix] Type ahead works for child items in outline view ([#471](https://trac.cyberduck.io/ticket/471))

2.8.5
* [Feature] Added ForgEdit to the list of supported editors (http://forgedit.com) ([#1779](https://trac.cyberduck.io/ticket/1779))
* [Feature] PRET (PRE Transfer) command support for distributed FTP (http://www.drftpd.org) ([#684](https://trac.cyberduck.io/ticket/684))
* [Bugfix] Downloads no longer keep modified date ([#1756](https://trac.cyberduck.io/ticket/1756))
* [Bugfix] Leap year bug when parsing date without year ([#1813](https://trac.cyberduck.io/ticket/1813))

2.8.4
* [Bugfix] Crash when dismissing sheet attached to window ([#1616](https://trac.cyberduck.io/ticket/1616))
* [Bugfix] Resolving Bonjour names blocks user interface ([#1657](https://trac.cyberduck.io/ticket/1657))
* [Bugfix] Closing browser window during connection attempt blocks user interface
* [Bugfix] Downloading to default download location when dragging folder to Finder ([#1611](https://trac.cyberduck.io/ticket/1611))
* [Feature] Add group ownership as optional browser column ([#1590](https://trac.cyberduck.io/ticket/1590))

2.8.3
* [Feature] Log Drawer in browser and transfer window
* [Feature] Toolbar button to open Terminal.app SSH session in current working directory (SFTP) ([#1508](https://trac.cyberduck.io/ticket/1508))
* [Feature] Added MacVim to the list of supported editors (http://code.google.com/p/macvim/) ([#1322](https://trac.cyberduck.io/ticket/1322))
* [Feature] Custom icon for executable files ([#945](https://trac.cyberduck.io/ticket/945))
* [Bugfix] Frequent crashes (#1401, #1409)
* [Bugfix] Does not change to correct directory when using a bookmark to the same server ([#1411](https://trac.cyberduck.io/ticket/1411))
* [Bugfix] Preserve leading and trailing whitespace when parsing filenames (FTP) ([#1381](https://trac.cyberduck.io/ticket/1381))
* [Bugfix] Directory parser compatibility with Webstar Server (FTP) ([#1302](https://trac.cyberduck.io/ticket/1302))
* [Bugfix] Directory parser compatibility with Freebox Server (FTP) ([#1258](https://trac.cyberduck.io/ticket/1258))
* [Bugfix] Directory parser compatibility with Trellix Server (FTP) ([#1213](https://trac.cyberduck.io/ticket/1213))

2.8.2
* [Feature] Show transfer progress when using browser session ([#1313](https://trac.cyberduck.io/ticket/1313))
* [Feature] Accept relative paths in bookmark setting ([#1167](https://trac.cyberduck.io/ticket/1167))
* [Feature] Ugly folder icons rendered (10.5)
* [Feature] Back and forward history menu for browser navigation buttons ([#1080](https://trac.cyberduck.io/ticket/1080))
* [Bugfix] Writing corrupted entries to the Keychain ([#1354](https://trac.cyberduck.io/ticket/1354))
* [Bugfix] Cannot drag files to working directory when there is no space left ([#60](https://trac.cyberduck.io/ticket/60))
* [Bugfix] Bookmarks drawer remembers width ([#371](https://trac.cyberduck.io/ticket/371))
* [Bugfix] Crashes after application launch (10.3.9) ([#1339](https://trac.cyberduck.io/ticket/1339))
* [Bugfix] Downloading multiple files with same name in outline hierarchy ([#1400](https://trac.cyberduck.io/ticket/1400))

2.8.1
* [Feature] Application code is signed (10.5)
* [Bugfix] Saving file in external editor does not cause upload (10.5) ([#1244](https://trac.cyberduck.io/ticket/1244))
* [Bugfix] Login failure with correct credentials ([#1231](https://trac.cyberduck.io/ticket/1231))
* [Bugfix] Failure to list directory on some servers (SFTP) ([#1170](https://trac.cyberduck.io/ticket/1170))
* [Bugfix] Transfers fails with punctuation characters in path (SCP) ([#1265](https://trac.cyberduck.io/ticket/1265))

2.8
* [Feature] Using Ganymed SSH2 library ([#185](https://trac.cyberduck.io/ticket/185))
* [Feature] Support for SCP transfers ([#1043](https://trac.cyberduck.io/ticket/1043))
* [Feature] Queuing file transfers ([#986](https://trac.cyberduck.io/ticket/986))
* [Feature] Automatic retry of failed network operations ([#783](https://trac.cyberduck.io/ticket/783))
* [Feature] Limit available bandwidth for transfers ([#48](https://trac.cyberduck.io/ticket/48))
* [Feature] Browse folder hierarchy in overwrite warning dialog ([#18](https://trac.cyberduck.io/ticket/18))
* [Feature] Browse folder hierarchy in synchronisation dialog ([#18](https://trac.cyberduck.io/ticket/18))
* [Feature] Use small icons in the bookmark drawer
* [Feature] Set the default protocol helper application for FTP and SFTP URLs ([#1049](https://trac.cyberduck.io/ticket/1049))
* [Feature] Setting default permissions for folders ([#77](https://trac.cyberduck.io/ticket/77))
* [Feature] Notes for bookmarks ([#67](https://trac.cyberduck.io/ticket/67))
* [Feature] Support for international domain names (IDN) ([#1133](https://trac.cyberduck.io/ticket/1133))
* [Feature] Move files to Trash before overwriting locally ([#936](https://trac.cyberduck.io/ticket/936))
* [Feature] Use system setting for connect mode (FTP)
* [Feature] Added WriteRoom to the list of supported editors (http://hogbaysoftware.com/projects/writeroom)
* [Bugfix] Don't use SOCKS proxy if hostname is excluded in system preferences
* [Bugfix] Resolve Alias files to upload ([#859](https://trac.cyberduck.io/ticket/859))
* [Bugfix] Subsequent type-ahead selection misbehaving ([#896](https://trac.cyberduck.io/ticket/896))

2.7.3
* [Bugfix] Problem parsing PASV response from some servers (FTP) (#779, #869)
* [Bugfix] Stalls when connection is interrupted during DNS lookup ([#960](https://trac.cyberduck.io/ticket/960))
* [Bugfix] Improved sorting in 'Kind' browser column ([#993](https://trac.cyberduck.io/ticket/993))
* [Bugfix] Renaming files using Info panel causes repeated renaming ([#1005](https://trac.cyberduck.io/ticket/1005))
* [Bugfix] Uploading folders interrupts file transfer (SFTP) ([#1001](https://trac.cyberduck.io/ticket/1001))
* [Bugfix] Preference for auto-open delay for spring-loaded folders not saved ([#633](https://trac.cyberduck.io/ticket/633))
* [Bugfix] Preference to open new browser window on launch not used ([#997](https://trac.cyberduck.io/ticket/997))
* [Bugfix] Symbolic links on local filesystem not handled properly ([#995](https://trac.cyberduck.io/ticket/995))
* [Bugfix] Send creation time of file with UTIME
* [Feature] Preference to exclude files from transfers using regular expression ([#511](https://trac.cyberduck.io/ticket/511))
* [Feature] Clear command in History menu ([#648](https://trac.cyberduck.io/ticket/648))

2.7.2
* [Feature] Option to use single connection for browser and transfers ([#57](https://trac.cyberduck.io/ticket/57))
* [Feature] Option to remember open browser windows and reconnect upon relaunching the application ([#59](https://trac.cyberduck.io/ticket/59))
* [Feature] Delay for spring-loaded folders can be set ([#633](https://trac.cyberduck.io/ticket/633))
* [Feature] Calculate size of directory (#5]
* [Feature] Duplicate files using option-drag ([#150](https://trac.cyberduck.io/ticket/150))
* [Feature] Per bookmark setting for download folder ([#158](https://trac.cyberduck.io/ticket/158))
* [Feature] Per bookmark setting to use single connection for browser and transfers
* [Feature] Kind column in browser describing file type ([#46](https://trac.cyberduck.io/ticket/46))
* [Feature] Advanced settings in connection and bookmark window in disclosable view
* [Feature] Added PageSpinner to the list of supported editors (http://www.optima-system.com/pagespinner) ([#205](https://trac.cyberduck.io/ticket/205))
* [Bugfix] Disconnecting in the background not blocking the user interface
* [Bugfix] Folders marked as inaccessible after timeouts and cannot be opened after reconnecting ([#611](https://trac.cyberduck.io/ticket/611))
* [Bugfix] Applescript/Dashboard should not use seperate session for transfers
* [Bugfix] Cannot write to group writable files (SFTP) ([#173](https://trac.cyberduck.io/ticket/173))
* [Bugfix] File length is set to zero if updating permission fails (SFTP) ([#974](https://trac.cyberduck.io/ticket/974))
* [Bugfix] Directory listing is not refreshed when upload is completed after "Try Again" ([#982](https://trac.cyberduck.io/ticket/982))

2.7.1
* [Localize] Portuguese Localization
* [Bugfix] Reporting error about failed to set permissions whereas it actually succeeded
* [Bugfix] Fail gracefully on uploading when server doesn't support changing permissions
* [Bugfix] Disconnecting from server could crash application shortly thereafter
* [Bugfix] Outline view not updated after dragged files have been uploaded
* [Bugfix] Synchronisation not awaiting selection from user

2.7
* [Feature] Don't block user interface when working in browser. All potentially lengthy operations are now performed in the background ([#921](https://trac.cyberduck.io/ticket/921))
* [Feature] All (possibly stalled) operations in progress can be interrupted ([#943](https://trac.cyberduck.io/ticket/943))
* [Feature] Improved error handling
* [Feature] Failed network operations can be repeated
* [Feature] A default bookmark can be configured ([#915](https://trac.cyberduck.io/ticket/915))
* [Feature] Option to disable spring-loaded folders ([#98](https://trac.cyberduck.io/ticket/98))
* [Feature] Individual settings how to treat duplicate files on uploads and downloads ([#500](https://trac.cyberduck.io/ticket/500))
* [Feature] Callback to alternate connect mode upon failure (FTP) ([#83](https://trac.cyberduck.io/ticket/83))
* [Feature] Add 'Download To...release-' menu option to download multiple files into designated, non-default directory ([#909](https://trac.cyberduck.io/ticket/909))
* [Feature] The bottom of the browser window shows the security status ([#9](https://trac.cyberduck.io/ticket/9))
* [Feature] Improvements to the AppleScript dictionary (#737, #918, #878, #922)
* [Feature] When duplicating files, propose a filename containing the current date and time ([#912](https://trac.cyberduck.io/ticket/912))
* [Feature] Added JarInspector to the list of supported editors (http://www.cgerdes.com)
* [Bugfix] Honor the existing permissions when replacing files

2.6.2
* [Bugfix] Crash when typing hostname in connection dialog for some users ([#711](https://trac.cyberduck.io/ticket/711))
* [Bugfix] Type-ahead selection not working for non-alphanumeric characters ([#271](https://trac.cyberduck.io/ticket/271))
* [Bugfix] Control click discards multiple selection ([#649](https://trac.cyberduck.io/ticket/649))
* [Bugfix] Timeout too slow giving I/O errors on slow connections ([#714](https://trac.cyberduck.io/ticket/714))
* [Bugfix] Passwords stored in Keychain not accessible by other applications ([#708](https://trac.cyberduck.io/ticket/708))
* [Bugfix] FTP URLs passed by another application pointing at folders fail to open ([#704](https://trac.cyberduck.io/ticket/704))

2.6.1
* [Bugfix] Crash or spinning beachball after upload ([#504](https://trac.cyberduck.io/ticket/504))
* [Bugfix] Active mode connections broken (FTP) ([#450](https://trac.cyberduck.io/ticket/450))
* [Bugfix] Removed graphical error messages for the sake of simplicity; displayed in log drawer instead (#524, #580)
* [Bugfix] Hostname reachability check slow and blocking user interface (#572, #575)
* [Bugfix] Fails to delete folders recursively in some cases ([#533](https://trac.cyberduck.io/ticket/533))
* [Bugfix] Unilingual builds broken ([#436](https://trac.cyberduck.io/ticket/436))
* [Bugfix] Cannot delete symbolic links ([#616](https://trac.cyberduck.io/ticket/616))

2.6
* [Localize] Turkish Localization
* [Feature] Automatic software udpate using the Sparkle.framework (Thanks to Andy Matuschak!) ([#300](https://trac.cyberduck.io/ticket/300))
* [Feature] Dashboard Widget included (Thanks to Claudio Procida!)
* [Feature] Marking write-only and non-accessible directories with special icon as in Finder.app
* [Feature] Reintroduced transcript drawer in browser window ([#104](https://trac.cyberduck.io/ticket/104)) and transfer window ([#375](https://trac.cyberduck.io/ticket/375))
* [Feature] New 'Download failed' and 'Upload failed' Growl notifications ([#362](https://trac.cyberduck.io/ticket/362))
* [Feature] Display alert icon if hostname cannot be resolved and Network Diagnostics.app integration
* [Feature] Indicate estimated remaining time left for transfers ([#43](https://trac.cyberduck.io/ticket/43))
* [Feature] Using custom error notifications instead of alert sheets don't block the parent window
* [Bugfix] Adjustements to the graphical user interface (Thanks to Peter Maurer!)
* [Bugfix] Stalled connection and file transfer attempts cannot be interrupted ([#55](https://trac.cyberduck.io/ticket/55))
* [Bugfix] Cannot login with colon in username ([#309](https://trac.cyberduck.io/ticket/309))
* [Bugfix] Cannot delete directory from server ([#256](https://trac.cyberduck.io/ticket/256))
* [Bugfix] Permission errors when downloading files from read-only directories ([#264](https://trac.cyberduck.io/ticket/264))
* [Bugfix] Change download keyboad shortcut ([#277](https://trac.cyberduck.io/ticket/277))
* [Bugfix] Certain operations trigger change of character encoding to default
* [Bugfix] Character encoding issues (#238, #333, #361, #390)
* [Bugfix] Improved compatibility with certain FTP servers
* [Bugfix] Cannot upload files to drop boxes ([#421](https://trac.cyberduck.io/ticket/421))

2.5.5
* [Localize] Hebrew Localization
* [Bugfix] Excape key cancels editing ([#190](https://trac.cyberduck.io/ticket/190))
* [Bugfix] Adjust permissions on created folders when transferring files ([#77](https://trac.cyberduck.io/ticket/77))
* [Bugfix] Cannot write to group writable files (SFTP) ([#173](https://trac.cyberduck.io/ticket/173))
* [Bugfix] Spotlight binary not executable ([#212](https://trac.cyberduck.io/ticket/212))
* [Feature] Added options to use Cyberduck as a portable application (see http://www.freesmug.org/portableapps/) ([#180](https://trac.cyberduck.io/ticket/180))
* [Feature] New Crash Reporter (Thanks to M. Uli Kusterer!) ([#195](https://trac.cyberduck.io/ticket/195))
* [Feature] Added skEdit to the list of supported editors (http://www.skti.org)

2.5.4
* [Localize] Thai Localization
* [Bugfix] Random crashes (#65, #94, #96, #121, #122)
* [Bugfix] High load while downloading ([#12](https://trac.cyberduck.io/ticket/12))

2.5.3
* [Feature] New and much improved Bonjour implementation
* [Feature] Inline rename files in browser (Return key invokes editing)
* [Feature] Paste files copied in Finder.app (Upload)
* [Feature] Includes Unsanity Crash Reporter
* [Bugfix] Renaming files in expanded tree of outline view bogus
* [Bugfix] Change to invalid directories not catched
* [Bugfix] Second login attempt always fails (SFTP)

2.5.2
* [Bugfix] Spinning beachball of death when connecting on some systems
* [Bugfix] Better validating drop targets in browser

2.5.1
* [Feature] Dragging files to application icon will upload to frontmost browser
* [Feature] Universal Binary
* [Feature] Printing browser view
* [Bugfix] Child items not refreshed properly in outline view
* [Bugfix] Warning before overwrite when moving or renaming files
* [Bugfix] Dragging files to the Finder.app places them at the dropped position
* [Bugfix] Always selecting parent directory as drop target when dragging to outline view
* [Bugfix] Updated application and document icon
* [Bugfix] Number of files in browser window not displayed correctly
* [Bugfix] Don't recurse into directories when deleting symbolic links
* [Bugfix] Overwriting group writable files

2.5
* [Feature] Graphical interface refinements
* [Bugfix] Caching issue with multiple connections to the same host
* [Bugfix] When moving to the parent directory the previous working directory is always selected
* [Bugfix] Selected files are always remembered when refreshing the browsing list
* [Bugfix] Remove custom icon and resource fork after download

2.5b4
* [Feature] Updated navigation bar interface elements
* [Bugfix] Vastly improved performance when listing directories
* [Bugfix] Do not reconnect if connecting to the same host from a different bookmark
* [Bugfix] Sorting history menu correctly
* [Bugfix] Refresh issues in outline view

2.5b3
* [Localize] Catalan Localization
* [Feature] Spotlight Importer for bookmarks
* [Feature] Synchronize bookmarks with .Mac
* [Feature] Send custom commands to server (FTP)
* [Feature] Auto scrolling log view
* [Feature] Updated application icon (Thanks to Admiral Potato)
* [Bugfix] New connection dialog remembers field values
* [Bugfix] Correctly parsing filenames beginning with whitespace (FTP)
* [Bugfix] Don't allow editing files with well known binary file type extensions
* [Bugfix] Excluding individual files when synchronising
* [Bugfix] Improved stability using outline view
* [Bugfix] Remember sorted column and direction
* [Bugfix] Sort child items in browser outline view

2.5b2
* [Localize] Indonesian Localization
* [Bugfix] Resorting browser view will not change selection
* [Bugfix] Remember state of expanded items in outline view
* [Feature] Updated to Growl 0.7
* [Feature] Edit menu shows all available editors

2.5b1
* [Feature] FTP using TLS support (using AUTH TLS as in draft-murray-auth-ftp-ssl-16) for the control channel (sending login credentials) and optionally the data channel (file listings and transfers) if the server is capable of.
* [Feature] Store X.509 certificates in Keychain
* [Feature] Custom icon with progress bar in the Finder when downloading files
* [Feature] Browser outline view
* [Feature] Toolbar button to switch view
* [Feature] History of recently connected hosts
* [Feature] Set character encoding per bookmark
* [Feature] Set connect mode (active/passive) per bookmark (FTP)
* [Feature] Show folders in working directory (and autocomplete) in 'Go to Folder' dialog
* [Feature] Exclude duplicate files from transfer upon alert
* [Feature] Apply button in file info dialog
* [Feature] Duplicate files on server
* [Feature] Preferences window with toolbar
* [Feature] Option to turn off disconnect prompt in Preferences
* [Feature] Option to keep connection alive in Preferences
* [Feature] Choose character encoding in connection dialog
* [Feature] Choose connect mode in connection dialog (FTP)
* [Feature] Drop down list of folders in Goto dialog
* [Feature] Sorting files by permission
* [Feature] UTF-8 is now used as the default character encoding
* [Feature] Edit with non-default editor using the browser context menu

2.4.6
* [Bugfix] Right-click a file in the browser will first select before it displays the contextual menu
* [Bugfix] Transfer success notification when remote editing file even when transfer fails (Growl)
* [Bugfix] Resolved incompatibility with SSH-1 keys in ~/.ssh/known_hosts (SFTP)
* [Bugfix] Applescript compatiblity issues with 10.4
* [Bugfix] Browser column width compatiblity issues with 10.4
* [Bugfix] Could not drag bookmark file to drawer when empty
* [Bugfix] Ignored custom port when selecting bookmark in connection dialog

2.4.5
* [Feature] Added Tag to the list of supported editors (http://www.talacia.com/)
* [Bugfix] No permissions set on transferred files when connection closed unexpectedly (SFTP)
* [Bugfix] Resuming uploads might corrupt files (SFTP)

2.4.4
* [Localize] Danish Localization
* [Localize] Polish Localization
* [Feature] Added CSSEdit to the list of supported editors (http://macrabbit.com/cssedit/)
* [Feature] Added CotEditor to the list of supported editors (http://www.aynimac.com/)
* [Feature] Gray out files in browser view when disconnected
* [Bugfix] Unexpected null reply received (FTP)

2.4.3
* [Localize] Hungarian Localization
* [Bugfix] Modification date changed when uploading files from the external editor
* [Bugfix] Browser window could lock up when reconnecting and login was needed
* [Feature] Disconnect item in menubar
* [Bugfix] Bug fixes

2.4
* [Localize] Swedish Localization
* [Feature] Additional encryption ciphers supported (SFTP)
* [Feature] Action dropdown menu in toolbar
* [Feature] Added Jedit X to the list of supported editors (http://www.artman21.net/product/JeditX/)
* [Feature] Added mi to the list of supported editors (http://mimikaki.net/)
* [Feature] Added Smultron to the list of supported editors (http://smultron.sourceforge.net/)
* [Bugfix] Rendezvous implementation updated
* [Bugfix] Folder to synchronize not selectable in some cases
* [Bugfix] Fallback to default directory when specified directory doesn't exist
* [Bugfix] Resolved incompatibility with SSH-1 keys in ~/.ssh/known_hosts (SFTP)
* [Bugfix] Resolved crash in info window when group was unknown

2.4b4
* [Bugfix] Dragging files to the Finder
* [Bugfix] Reordering bookmarks
* [Bugfix] SSH exception handling (SFTP)
* [Bugfix] Bookmark selection in connection dialog
* [Bugfix] Items in transfer window not removed
* [Bugfix] Deleting more than one bookmark at once
* [Bugfix] Corrupted resource files in Chinese localization
* [Bugfix] Corrupted resource files in Japanese localization

2.4b3
* [Localize] Russian Localization
* [Feature] Preliminary AppleScript support (see .scpt script samples)
* [Feature] Quick Connect field and hostname field in connection dialog accept URLs as input
* [Feature] Reordering bookmarks using drag and drop
* [Bugfix] Deleting more than 10 files at once
* [Bugfix] Typing in transfer window causes crash
* [Bugfix] Transcript causes crash
* [Bugfix] Reporting correct file size for large files
* [Bugfix] Disable resume on ASCII tranfers (FTP)
* [Bugfix] Incompatiblity with Filezilla Server (FTP)
* [Bugfix] Synchronizing current working directory
* [Bugfix] Graceful failure on SSH connection problems (SFTP)
* [Bugfix] Correctly resolving '~' in filenames
* [Bugfix] Bug fixes

2.4b2
* [Feature] Allow the selection of files in the browser by typing more than just the first character of the filename
* [Feature] A bookmark in the drawer can be selected by typing its hostname
* [Feature] Preserve the modification date on upload (SFTP)
* [Feature] Preserve the modification date on upload if the server supports 'CHMOD UTIME' (FTP)
* [Bugfix] Determine changed files based on modification date when synchronizing
* [Bugfix] Opening an URL linking to a file opens a browser window
* [Bugfix] Changing the username of a newly created bookmark of a connected server might affect an existing bookmark of the same server but with a different username.
* [Bugfix] Logging the response of 'LIST' (FTP)
* [Bugfix] Selection of bookmark in connection dialog
* [Bugfix] Performance issue in log view (Thanks to Douglas Davidson)
* [Feature] Live scrolling log view

2.4b1
* [Feature] Synchronization of files
* [Feature] Create new files on server
* [Feature] Support for Novell Netware file listings (FTP)
* [Feature] Growl notification support (see http://growl.info)
* [Feature] Limit number of concurrent connections to a remote host
* [Feature] Move remote files with copy & paste
* [Feature] Select a file in the browser by typing its first character
* [Feature] Put remote files into the transfer queue with copy & paste
* [Feature] Apply permissions recursively
* [Feature] Option to apply default permissions to transferred files in Preferences
* [Feature] Option to preserve modification date on download in Preferences
* [Feature] Switch encoding per browser with menu or toolbar item
* [Feature] Switch 'Show hidden files' per browser with menu or toolbar item
* [Feature] Transfer Queue with "Aqua" progress indicator
* [Feature] Provide a proxy icon for the connection in the window title bar
* [Feature] The proxy icon in the window title bar can then be dragged to the bookmark drawer or the Finder.
* [Feature] Bookmarks of recently connected hosts are saved in ~/Library/Application Support/Cyberduck/History
* [Feature] Updated toolbar icons (Thanks to Matt Ball)
* [Feature] Display the number of files in the browser
* [Feature] Added TextMate to the list of supported editors (see http://macromates.com)
* [Feature] Dialog for duplicated files lists all at once instead of consecutively asking
* [Feature] Filter field is now a standard search field
* [Feature] Read bookmarks from "/Library/Application Support/Cyberduck/" instead of the individual user bookmarks file if available
* [Feature] Check for running transfers on application quit
* [Feature] Updated FTP core (FTP)
* [Feature] Saving passwords as 'Internet Password' in the Keychain
* [Localize] Norwegian Localization
* [Bugfix] Update existing browsers when changing the default interface attributes in the Preferences
* [Bugfix] Don't cache DNS lookups forever
* [Bugfix] Bookmark drawer icon has no text-only equivalent
* [Bugfix] Tabbing between browser and filter box
* [Bugfix] Quick Connect Field has initial focus

2.3.3
* [Bugfix] Correctly parsing symbolic links

2.3.2
* [Localize] Slovak Localization
* [Localize] Czech Localization
* [Feature] Support for EPFL file listings to support servers running "publicfile" (FTP)
* [Bugfix] Improved PASV response parsing (FTP)
* [Feature] Supporting SOCKS proxies (not tested)
* [Feature] Change file permissions on multiple files
* [Feature] Copy the URL of a remote site
* [Bugfix] Use date formatting rules set in the System Preferences
* [Feature] Preference item to set the action when double-clicking files (Download or edit)
* [Bugfix] Remembering the position of the browser window
* [Bugfix] Compatibility with servers not supporting the SIZE command (FTP)

2.3.1
* [Localize] Chinese (Simplified) Localization
* [Feature] Rendezvous services in Bookmark menu
* [Bugfix] Upper level directories had to be listed first when uploading files
* [Bugfix] Changes to bookmarks won't be saved
* [Feature] Preference item to disable the update check
* [Feature] Toolbar item to open downloaded files with default application
* [Feature] Graceful application termination (Properly ask to close all connections and then quit)
* [Bugfix] Remembering choosen directories in open and save dialogs

2.3
* [Localize] Finnish Localization
* [Feature] Supporting Keyboard Interactive (PAM) Authentication (SSH)
* [Bugfix] not all available Rendezvous services were listed
* [Bugfix] Removed 'Type ahead' feature of the browser (responsible for frequent crashes)

2.3b2
* [Feature] Much improved queue management (fewer connections)
* [Feature] Resume downloads (SFTP)
* [Feature] Resume uploads (SFTP)
* [Feature] Resume uploads (FTP)
* [Feature] Move files on remote host by drag and drop
* [Feature] Drag files onto bookmark to quickly upload
* [Feature] Bookmarks are shown in the menu
* [Feature] Menu item 'Download As...release-'
* [Feature] Editing multiple files in the external editor with the same name
* [Feature] Warning if a duplicate exists when uploading files
* [Feature] 'Apply to all' checkbox in dialog asking for replace/resume/skip existing files
* [Feature] Improved caching of directory listings
* [Feature] When dropping files onto folders it will upload these into the folder
* [Feature] When typing the first letter of a file it gets selected in the browser
* [Feature] Toolbar button to remove all completed items from the queue
* [Feature] Convert line endings when downloading in ASCII mode (FTP)
* [Feature] Auto transfer mode (FTP)
* [Feature] Dock menu item to open new browser
* [Bugfix] Correctly updating incorrect keychain entries
* [Feature] Preserve modification date when downloading files
* [Bugfix] Ignoring .DS_Store files
* [Bugfix] Bug fixes

2.3b1
* [Feature] External editor support (SubEthaEdit, BBEdit, TextWrangler, Text-Edit Plus)

2.2
* [Bugfix] Supporting folders with sticky/set-uid/set-gid bits (FTP)
* [Localize] Updated Dutch localization
* [Localize] Updated French localization
* [Bugfix] Minor performance improvements

2.2b6
* [Feature] Preliminary support for Windows, VMS and OS/2 file listings (FTP)
* [Bugfix] Properly deleting symbolic links (FTP)
* [Feature] Displaying link icons in browser
* [Bugfix] Downloading files from FTP servers not supporting the SIZE command (FTP)
* [Bugfix] Fixed a bug where overwritten files with SFTP got corrupted (Thanks to Jan!)
* [Feature] Contextual Menu support
* [Bugfix] Bug fixes

2.2b5
* [Localize] Chinese (Traditional) Localization
* [Localize] Korean Localization
* [Feature] Passphrases for private key files are stored in the Keychain (SSH)
* [Feature] Bookmark editor allows to specify the port number
* [Bugfix] When a bookmark is edited, the properties are updated in the bookmark drawer instantly
* [Feature] A confirmation dialog is displayed before deleting a bookmark
* [Feature] A confirmation dialog is displayed when connecting to a new site in a browser still connected to another host
* [Bugfix] Transcript messages are only shown in the corresponding browser
* [Bugfix] Fixed a bug which caused the browser window to be 'frozen' after an upload
* [Feature] Option to adjust the 'LIST' command sent (FTP)
* [Bugfix] Fixed a bug which caused a crash when not connected to a network
* [Feature] Updated application icon

2.2b4
* [Bugfix] Fixed a bug that with some servers the permissions could not be modified (FTP)
* [Bugfix] When addding a transfer to the queue it is highlighted
* [Bugfix] Improved login process
* [Localize] Updated Japanese localization

2.2b3
* [Feature] Caching directory listings
* [Feature] Auto-refresh the directory listing after uploading a file
* [Feature] Sending 'LIST -a' to list directories (FTP)
* [Bugfix] Fixed a bug where entries in the queue would be deleted too early

2.2b2
* [Localize] Updated Japanese localization
* [Localize] Updated French localization
* [Localize] Updated Portuguese localization
* [Localize] Updated Italian localization
* [Bugfix] The log is now written with a fixed-width font
* [Feature] Various minor improvements

2.2b1
* [Feature] Keychain integration
* [Feature] New file transfer manager
* [Feature] Full Unicode support
* [Feature] Choose character encoding for file listings
* [Feature] Support for public key authentication (SSH)
* [Feature] Live filtering of directory listings
* [Feature] Improved Rendezvous support (jmDNS 0.2)
* [Feature] Drag files from the browser to the transfer manager
* [Feature] Drag links to the transfer manager to start download
* [Feature] Browser can be customized to show/hide certain columns
* [Localize] Japanese Localization
* [Localize] Italian Localization
* [Localize] Portuguese Localization
* [Localize] Spanish Localization
* [Localize] French Localization
* [Feature] Option to disable 'CHMOD' after upload (FTP)
* [Feature] Option to disable sending the 'SYST' command (FTP)
* [Bugfix] When clicking the dock icon and now window is active, a new browser window is opened
* [Feature] Bookmarks can be rearranged
* [Bugfix] When login credentials aren't reasonable, ask before trying to login

2.1
* [Feature] Bookmarks can now be edited
* [Feature] Bookmarks can now be saved as a regular file (Drag the bookmarks to the Finder)
* [Feature] Bookmarks saved as files can be imported by dropping them on the Bookmarks Drawer
* [Feature] Double-clicking a Cyberduck bookmark file in the Finder will open a new browser and connect to the remote site
* [Feature] Bookmarks can now be modified
* [Feature] Specify an initial directory upon new connection (e.g. public_html instead of the default home)
* [Feature] Use keyboard shortcut (commann-up/down) for browsing a directory
* [Feature] Uploaded files have the same permissions as locally (FTP)
* [Feature] Updated icon set
* [Bugfix] Remembering sort order while browsing
* [Feature] Toolbar icon to toggle Bookmark drawer
* [Feature] Updated SSH Core (v0.2.5)
* [Localize] Dutch Localization
* [Localize] German Localization
* [Bugfix] Smaller changes and bug fixes

2.1b5
* [Feature] HTTP is now supported again. Files can be downloaded from regular web servers
* [Feature] Cyberduck can now be configured as the default FTP helper application. See "http://www.monkeyfood.com/software/moreInternet/". This seems to work with Safari and Internet Explorer
* [Bugfix] Dragging files to the Finder is now more reliable
* [Bugfix] The transfer panel does now close again if defined so in the preferences
* [Feature] The buffer size (the size of download chunks to keep in memory before writing to disk) is now adjustable
* [Bugfix] Login to anonymous servers where no password is needed is now supported (FTP)
* [Bugfix] Smaller bug fixes

2.1b4
* [Feature] Cyberduck now has the official creator code 'CYCK' and can handle URL events from other applications
* [Bugfix] Smaller bug fixes

2.1b3
* [Localize] Localization support
* [Feature] Files and foldes can now be dragged to the Finder to download them
* [Bugfix] Windows do now no more stack exactly on the top of prior one
* [Feature] There are now equivalent menu actions for the toolbar items
* [Bugfix] Modification dates and time are now displayed correctly
* [Bugfix] The modification date column is now sorted correctly
* [Bugfix] Browser columns are now sortable in both directions
* [Bugfix] When deleting multiple files and directores, the file list will now only get updated at the end because of performance
* [Bugfix] If multiples have been selected to transfer, Cyberduck now puts them in the same queue and opens only one connection to the server
* [Feature] There is a new command 'Go to folder' for changing the working directory quickly
* [Bugfix] Many smaller bug fixes

2.1b2
* [Feature] SSH (SFTP) support
* [Feature] Completly rewritten user interface using the Cocoa Framework