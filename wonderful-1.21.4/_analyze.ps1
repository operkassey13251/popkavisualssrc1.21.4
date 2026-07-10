$root = Get-Location
$files = Get-ChildItem -Recurse -File | Where-Object { $_.FullName -notmatch '\\\.git\\' -and $_.FullName -notmatch '\\build\\' }
$matched = $files | Where-Object { Select-String -Path $_.FullName -Pattern 'popka' -SimpleMatch -Quiet }
Write-Output "=== FILES CONTAINING 'popka' (case-insensitive): $($matched.Count) ==="
$matched | ForEach-Object { $_.FullName.Replace($root.Path + '\','') }
Write-Output ""
Write-Output "=== PATHS (files/dirs) CONTAINING 'popka' IN NAME ==="
Get-ChildItem -Recurse | Where-Object { $_.Name -match 'popka' } | ForEach-Object { $_.FullName.Replace($root.Path + '\','') }
