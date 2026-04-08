$mdPath = Join-Path $PSScriptRoot "implementation-comparison-table.md"
$pdfPath = Join-Path $PSScriptRoot "implementation-comparison-table.pdf"
$utf8 = New-Object System.Text.UTF8Encoding($false)
$lines = [System.IO.File]::ReadAllLines($mdPath, $utf8)

$title = ($lines | Where-Object { $_.StartsWith("# ") } | Select-Object -First 1).Substring(2)
$description = ($lines | Where-Object { $_ -like "- *" } | Select-Object -First 2) -join " "
$tableLines = $lines | Where-Object { $_.StartsWith("| **") }

$rows = @()
foreach ($line in $tableLines) {
    $parts = $line.Split("|")
    if ($parts.Length -ge 4) {
        $left = $parts[1].Trim()
        $right = $parts[2].Trim()
        $left = $left.Replace([string][char]96, "")
        $right = $right.Replace([string][char]96, "")
        $left = $left.Replace("**", "")
        $right = $right.Replace("**", "")
        $rows += @{
            Left = $left
            Right = $right
        }
    }
}

$word = $null
$document = $null

try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0

    $document = $word.Documents.Add()
    $document.PageSetup.Orientation = 1

    $selection = $word.Selection
    $selection.Font.Name = "Segoe UI"
    $selection.Font.Size = 16
    $selection.TypeText($title)
    $selection.TypeParagraph()
    $selection.Font.Size = 10
    $selection.TypeText($description)
    $selection.TypeParagraph()
    $selection.TypeParagraph()

    $range = $selection.Range
    $table = $document.Tables.Add($range, $rows.Count + 1, 2)
    $table.Borders.Enable = 1
    $table.Rows(1).Range.Bold = 1
    $table.Cell(1, 1).Range.Text = "ru/iva"
    $table.Cell(1, 2).Range.Text = "kmp-contact"
    $table.Range.Font.Name = "Segoe UI"
    $table.Range.Font.Size = 9

    for ($i = 0; $i -lt $rows.Count; $i++) {
        $table.Cell($i + 2, 1).Range.Text = $rows[$i].Left
        $table.Cell($i + 2, 2).Range.Text = $rows[$i].Right
    }

    $table.AutoFitBehavior(2)
    $document.ExportAsFixedFormat($pdfPath, 17)

    Write-Output "PDF_CREATED"
    Get-Item $pdfPath | Select-Object FullName, Length, LastWriteTime
}
finally {
    if ($document -ne $null) {
        try { $document.Close($false) } catch {}
    }
    if ($word -ne $null) {
        try { $word.Quit() } catch {}
    }
}
