param(
    [Parameter(Mandatory = $true)]
    [string]$Server,

    [Parameter(Mandatory = $true)]
    [string]$User,

    [Parameter(Mandatory = $true)]
    [string]$ZeroTierIp,

    [int]$SshPort = 22,

    [string]$ImageName = "rescue-ai-poc",

    [string]$ContainerName = "rescue-ai-poc",

    [int]$HostPort = 8080,

    [int]$ContainerPort = 8080,

    [string]$PrivateKey = ""
)

$ErrorActionPreference = "Stop"

function Assert-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' was not found in PATH."
    }
}

function Invoke-Checked {
    param(
        [string]$Description,
        [scriptblock]$Command
    )

    Write-Host ""
    Write-Host "==> $Description"

    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

Assert-Command "ssh"
Assert-Command "scp"
Assert-Command "tar"

$projectRoot = (Get-Location).Path

foreach ($requiredPath in @("Dockerfile", "pom.xml", "src")) {
    $fullPath = Join-Path $projectRoot $requiredPath

    if (-not (Test-Path $fullPath)) {
        throw "Required project item '$requiredPath' was not found in '$projectRoot'. Run this script from the project root."
    }
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$archiveName = "$ContainerName-$timestamp.tar.gz"
$localArchive = Join-Path $env:TEMP $archiveName
$remoteDir = "/tmp/$ContainerName-$timestamp"
$remoteArchive = "/tmp/$archiveName"

$sshTarget = "$User@$Server"

$sshArgs = @("-p", "$SshPort")
$scpArgs = @("-P", "$SshPort")

if ($PrivateKey) {
    $sshArgs += @("-i", $PrivateKey)
    $scpArgs += @("-i", $PrivateKey)
}

try {
    if (Test-Path $localArchive) {
        Remove-Item $localArchive -Force
    }

    Invoke-Checked "Creating deployment archive" {
        & tar `
            -czf $localArchive `
            --exclude=target `
            --exclude=.git `
            --exclude=.idea `
            Dockerfile `
            pom.xml `
            src
    }

    Invoke-Checked "Checking SSH connection and remote Docker" {
        & ssh @sshArgs $sshTarget "docker version >/dev/null && echo 'Remote Docker is available.'"
    }

    Invoke-Checked "Uploading project archive" {
        & scp @scpArgs $localArchive "${sshTarget}:$remoteArchive"
    }

    $remoteScript = @"
set -eu

REMOTE_DIR='$remoteDir'
ARCHIVE='$remoteArchive'
IMAGE_NAME='$ImageName`:latest'
CONTAINER_NAME='$ContainerName'
BIND_IP='$ZeroTierIp'
HOST_PORT='$HostPort'
CONTAINER_PORT='$ContainerPort'

cleanup() {
    rm -rf "`$REMOTE_DIR" "`$ARCHIVE"
}

trap cleanup EXIT

echo "==> Preparing remote build directory"
mkdir -p "`$REMOTE_DIR"
tar -xzf "`$ARCHIVE" -C "`$REMOTE_DIR"
cd "`$REMOTE_DIR"

echo "==> Building Docker image on server"
docker build -t "`$IMAGE_NAME" .

if docker container inspect "`$CONTAINER_NAME" >/dev/null 2>&1; then
    echo "==> Removing existing container"
    docker rm -f "`$CONTAINER_NAME"
fi

echo "==> Starting new container"
docker run -d \
    --name "`$CONTAINER_NAME" \
    --restart unless-stopped \
    -p "`$BIND_IP`:`$HOST_PORT`:`$CONTAINER_PORT" \
    "`$IMAGE_NAME"

echo "==> Waiting for application startup"
sleep 5

echo "==> Testing health endpoint from server"
curl --fail --silent --show-error \
    "http://`$BIND_IP:`$HOST_PORT/health"

echo
echo "==> Container status"
docker ps --filter "name=^/`$CONTAINER_NAME$"

echo "==> Remote deployment successful"
"@

    Write-Host ""
    Write-Host "==> Building and deploying on remote server"

    $remoteScript | & ssh @sshArgs $sshTarget "bash -s"

    if ($LASTEXITCODE -ne 0) {
        throw "Remote deployment failed with exit code $LASTEXITCODE."
    }

    $healthUrl = "http://${ZeroTierIp}:${HostPort}/health"

    Write-Host ""
    Write-Host "==> Testing ZeroTier endpoint from this PC"
    Write-Host $healthUrl

    $health = Invoke-RestMethod `
        -Method Get `
        -Uri $healthUrl `
        -TimeoutSec 15

    Write-Host ""
    Write-Host "Deployment successful."
    Write-Host "Health response:"
    $health | ConvertTo-Json -Depth 10

    Write-Host ""
    Write-Host "Presentation endpoints:"
    Write-Host "Health: http://${ZeroTierIp}:${HostPort}/health"
    Write-Host "Assist: http://${ZeroTierIp}:${HostPort}/assist"
}
finally {
    if (Test-Path $localArchive) {
        Remove-Item $localArchive -Force
    }
}
