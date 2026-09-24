# =====================================================================
# ChatVault - Restore (banco PostgreSQL + arquivos)
# Uso: .\restore.ps1 -Stamp 20260924_180000
#   (restaura backup\db_<Stamp>.dump e backup\files_<Stamp>.tar.gz)
# Sem -Stamp, restaura o backup mais recente encontrado.
# =====================================================================
param(
    [string]$Stamp
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackupDir = Join-Path $ProjectRoot "backup"

if (-not $Stamp) {
    $latest = Get-ChildItem -Path $BackupDir -Filter "db_*.dump" |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $latest) { throw "Nenhum backup de banco encontrado em $BackupDir" }
    $Stamp = $latest.BaseName -replace '^db_', ''
}

$DbDump = Join-Path $BackupDir "db_$Stamp.dump"
$FilesArchive = Join-Path $BackupDir "files_$Stamp.tar.gz"

if (-not (Test-Path $DbDump)) { throw "Arquivo nao encontrado: $DbDump" }
if (-not (Test-Path $FilesArchive)) { throw "Arquivo nao encontrado: $FilesArchive" }

Write-Host "Restaurando backup $Stamp ..."

Write-Host "[1/3] Parando o app..."
docker compose stop chatvault

Write-Host "[2/3] Restaurando banco..."
docker cp "$DbDump" chatvault-database-1:/tmp/chatvault.dump
docker exec chatvault-database-1 pg_restore -U usr_chatvault -d chatvault --clean --if-exists /tmp/chatvault.dump
if ($LASTEXITCODE -ne 0) { throw "pg_restore falhou" }

Write-Host "[3/3] Restaurando arquivos..."
docker run --rm -v chatvault_chatvault_data:/data -v "${BackupDir}:/backup" alpine sh -c "tar xzf /backup/files_$Stamp.tar.gz -C /data"
if ($LASTEXITCODE -ne 0) { throw "restore de arquivos falhou" }

docker compose up -d

Write-Host ""
Write-Host "Restore concluido. Acesse http://localhost:8080"
