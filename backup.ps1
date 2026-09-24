# =====================================================================
# ChatVault - Backup (banco PostgreSQL + arquivos)
# Uso: .\backup.ps1
# Gera arquivos timestamped na pasta .\backup e mantém os últimos N.
# =====================================================================
param(
    [int]$Retain = 14
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BackupDir = Join-Path $ProjectRoot "backup"
$Stamp = Get-Date -Format "yyyyMMdd_HHmmss"

New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null

Write-Host "[1/2] Backing up PostgreSQL database..."
docker exec chatvault-database-1 pg_dump -U usr_chatvault -d chatvault -F c -f /tmp/chatvault.dump
if ($LASTEXITCODE -ne 0) { throw "pg_dump falhou" }
docker cp chatvault-database-1:/tmp/chatvault.dump "$BackupDir\db_$Stamp.dump"
if ($LASTEXITCODE -ne 0) { throw "docker cp (banco) falhou" }

Write-Host "[2/2] Backing up chat files..."
docker exec chatvault-chatvault-1 tar czf /tmp/chatvault_files.tar.gz -C /opt/chatvault .
if ($LASTEXITCODE -ne 0) { throw "tar falhou" }
docker cp chatvault-chatvault-1:/tmp/chatvault_files.tar.gz "$BackupDir\files_$Stamp.tar.gz"
if ($LASTEXITCODE -ne 0) { throw "docker cp (arquivos) falhou" }

# Retenção: mantém os últimos $Retain de cada tipo
Get-ChildItem -Path $BackupDir -Filter "db_*.dump" |
    Sort-Object LastWriteTime -Descending | Select-Object -Skip $Retain | Remove-Item -Force
Get-ChildItem -Path $BackupDir -Filter "files_*.tar.gz" |
    Sort-Object LastWriteTime -Descending | Select-Object -Skip $Retain | Remove-Item -Force

Write-Host ""
Write-Host "Backup concluido: db_$Stamp.dump e files_$Stamp.tar.gz em $BackupDir"
