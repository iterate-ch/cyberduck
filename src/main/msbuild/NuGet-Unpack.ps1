[CmdletBinding()]
param (
    # Source path, where all .nupkgs are stored.
    [Parameter()]
    [string]
    $PackagesListPath
)

if (-not (Test-Path -Path $PackagesListPath -PathType Leaf)) {
    return;
}

$NugetParent = [System.IO.Path]::GetDirectoryName($PackagesListPath)
$CachePath = "$NugetParent\cache"
$PackagesPath = "$NugetParent\packages"

foreach ($line in Get-Content $PackagesListPath) {
    $line = $line.Trim()
    if ($line -notmatch "\s*(?<group>[^:]+):(?<artifact>[^:]+):(?<type>[^:]+):(?<version>[^:]+):(?<scope>[^:]+)$") {
        continue
    }

    $artifact = $Matches["artifact"]
    $version = $Matches["version"]

    $NupkgFile = "$CachePath\$artifact-$version.nupkg"
    $NupkgZip = "$CachePath\$artifact-$version.zip"
    $TargetDirectory = "$PackagesPath\$artifact\$version"
    Copy-Item $NupkgFile $NupkgZip
    Expand-Archive $NupkgZip $TargetDirectory -Force
    Remove-Item $NupkgZip
    Set-Content "$TargetDirectory\.nupkg.metadata" '{}'
}
