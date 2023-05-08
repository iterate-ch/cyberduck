$packageName = 'cyberduck'
$installerType = 'exe'
$silentArgs = '/quiet InstallBonjour=0'
$url = 'https://update.cyberduck.io/windows/Cyberduck-Installer-${VERSION}.${REVISION}.exe'
$checksum = '${CHECKSUM}'
$checksumType = 'sha256'
$validExitCodes = @(0)

Install-ChocolateyPackage -PackageName "$packageName" `
                          -FileType "$installerType" `
                          -SilentArgs "$silentArgs" `
                          -Url "$url" `
                          -ValidExitCodes $validExitCodes `
                          -Checksum "$checksum" `
                          -ChecksumType "$checksumType"