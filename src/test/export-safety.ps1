param(
    [string] $Jar = (Join-Path $PSScriptRoot "..\..\target\scala-2.13\FolderScraper.jar")
)

$ErrorActionPreference = "Stop"
$jarPath = (Resolve-Path $Jar).Path
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("folder-scraper-export-test-" + [guid]::NewGuid())
$inputDir = Join-Path $tempRoot "input"
$outputFile = Join-Path $tempRoot "output.txt"

function Assert($condition, [string] $message) {
    if (-not $condition) { throw $message }
}

try {
    New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $inputDir "z.scala"), "object Z")
    [System.IO.File]::WriteAllText((Join-Path $inputDir "a.scala"), "object A")

    & java -jar $jarPath $inputDir $outputFile
    Assert ($LASTEXITCODE -eq 0) "Export failed"
    $first = [System.IO.File]::ReadAllText($outputFile)
    Assert ($first.Contains("Files discovered: 2")) "Index file count is incorrect"
    Assert ($first.Contains("Files exported: 2")) "Summary file count is incorrect"
    Assert ($first.Contains("Files failed: 0")) "Summary failure count is incorrect"
    Assert ($first.Contains("- a.scala") -and $first.Contains("- z.scala")) "Index entries are missing"
    Assert ($first.Contains("Path: a.scala") -and $first.Contains("Path: z.scala")) "File sections are missing"
    Assert ($first.IndexOf("- a.scala") -lt $first.IndexOf("- z.scala")) "Index is not sorted"
    Assert ($first.IndexOf("Path: a.scala") -lt $first.IndexOf("Path: z.scala")) "File contents are not sorted"
    & java -jar $jarPath $inputDir $outputFile
    Assert ($LASTEXITCODE -eq 0) "Repeated export failed"
    Assert ([System.IO.File]::ReadAllText($outputFile) -eq $first) "Repeated export is not deterministic"

    $unicodeFile = Join-Path $inputDir "unicode.txt"
    $unicodeContent = "UTF-8 " + [char]::ConvertFromUtf32(0x6F22) + " " + [char]::ConvertFromUtf32(0x1F680)
    [System.IO.File]::WriteAllText($unicodeFile, $unicodeContent, (New-Object System.Text.UTF8Encoding($false)))
    foreach ($arguments in @(@($inputDir, $outputFile), @("-s", $inputDir, $outputFile))) {
        & java "-Dfile.encoding=windows-1252" -jar $jarPath @arguments
        Assert ($LASTEXITCODE -eq 0) "UTF-8 input failed with a non-UTF-8 JVM default"
        $unicodeOutput = [System.IO.File]::ReadAllText($outputFile, (New-Object System.Text.UTF8Encoding($false, $true)))
        Assert ($unicodeOutput.Contains($unicodeContent)) "UTF-8 content was not preserved"
    }
    Remove-Item -LiteralPath $unicodeFile

    $invalidFile = Join-Path $inputDir "invalid.txt"
    [System.IO.File]::WriteAllBytes($invalidFile, [byte[]](0xC3, 0x28))
    & java "-Dfile.encoding=windows-1252" -jar $jarPath $inputDir $outputFile
    Assert ($LASTEXITCODE -ne 0) "Invalid UTF-8 should produce a nonzero exit code"
    $invalidOutput = [System.IO.File]::ReadAllText($outputFile, [System.Text.Encoding]::UTF8)
    Assert ($invalidOutput.Contains("Could not read file: invalid.txt")) "Invalid UTF-8 was not reported"
    Assert ($invalidOutput.Contains("Files failed: 1")) "Invalid UTF-8 was not counted as a failure"
    Remove-Item -LiteralPath $invalidFile

    $insideOutput = Join-Path $inputDir "export.txt"
    & java -jar $jarPath $inputDir $insideOutput
    Assert ($LASTEXITCODE -eq 0) "New output inside input should be allowed"
    $insideContents = [System.IO.File]::ReadAllText($insideOutput)
    Assert (-not $insideContents.Contains("Path: export.txt")) "Output included itself"
    & java -jar $jarPath $inputDir $insideOutput
    Assert ($LASTEXITCODE -ne 0) "Existing output inside input should be rejected"
    Assert ([System.IO.File]::ReadAllText($insideOutput) -eq $insideContents) "Existing output was modified"

    $sourceFile = Join-Path $inputDir "a.scala"
    & java -jar $jarPath $inputDir $sourceFile
    Assert ($LASTEXITCODE -ne 0) "Overwriting a source file should be rejected"
    Assert ([System.IO.File]::ReadAllText($sourceFile) -eq "object A") "Source file was modified"

    $alias = Join-Path $tempRoot "source-alias.txt"
    New-Item -ItemType HardLink -Path $alias -Target $sourceFile | Out-Null
    & java -jar $jarPath $inputDir $alias
    Assert ($LASTEXITCODE -ne 0) "Overwriting a hard-linked source file should be rejected"
    Assert ([System.IO.File]::ReadAllText($sourceFile) -eq "object A") "Hard-linked source file was modified"

    $lockedFile = Join-Path $inputDir "locked.txt"
    [System.IO.File]::WriteAllText($lockedFile, "locked")
    $lock = [System.IO.File]::Open($lockedFile, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
    try {
        & java -jar $jarPath $inputDir $outputFile
        Assert ($LASTEXITCODE -ne 0) "Unreadable input should produce a nonzero exit code"
    } finally {
        $lock.Dispose()
    }
    $partial = [System.IO.File]::ReadAllText($outputFile)
    Assert ($partial.Contains("Could not read file: locked.txt")) "Unreadable file was not reported in the export"
    Assert ($partial.Contains("Files exported: 3")) "Incomplete export success count is incorrect"
    Assert ($partial.Contains("Files failed: 1")) "Incomplete export failure count is incorrect"

    $lockedOutput = [System.IO.File]::Open($outputFile, [System.IO.FileMode]::Open, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
    try {
        & java -jar $jarPath $inputDir $outputFile
        Assert ($LASTEXITCODE -ne 0) "Unwritable output should produce a nonzero exit code"
    } finally {
        $lockedOutput.Dispose()
    }
    Write-Host "Export safety integration tests passed."
} finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
