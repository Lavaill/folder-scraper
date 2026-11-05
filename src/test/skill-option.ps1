param(
    [string] $Jar = (Join-Path $PSScriptRoot "..\..\target\scala-2.13\FolderScraper.jar")
)

$ErrorActionPreference = "Stop"
$jarPath = (Resolve-Path $Jar).Path
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("folder-scraper-test-" + [guid]::NewGuid())
$inputDir = Join-Path $tempRoot "input project"
$outputFile = Join-Path $tempRoot "output.txt"
$resourcePaths = @(
    "SKILL.md",
    "references/inspection-checklist.md",
    "references/plan-template.md",
    "references/handoff-template.md"
)

function Assert($condition, [string] $message) {
    if (-not $condition) { throw $message }
}

try {
    New-Item -ItemType Directory -Path $inputDir -Force | Out-Null
    [System.IO.File]::WriteAllText((Join-Path $inputDir "example.scala"), "object Example")
    Push-Location $tempRoot
    try {
        & java -jar $jarPath $inputDir $outputFile
        Assert ($LASTEXITCODE -eq 0) "Default invocation failed"
        $plainOutput = [System.IO.File]::ReadAllText($outputFile)
        Assert ($plainOutput.Contains("object Example")) "Default output is missing project content"
        Assert (-not $plainOutput.Contains("Path: SKILL.md")) "Default output unexpectedly contains the skill"
        Assert (-not $plainOutput.Contains("INSTRUCTIONS REMINDER")) "Default output unexpectedly contains the reminder"

        $variants = @(
            @("-s", $inputDir, $outputFile),
            @($inputDir, "-S", $outputFile),
            @($inputDir, $outputFile, "-S")
        )
        foreach ($arguments in $variants) {
            $console = & java -jar $jarPath @arguments | Out-String
            Assert ($LASTEXITCODE -eq 0) "Skill invocation failed: $arguments"
            Assert ($console.Contains("Suggested first chat message:")) "Suggested chat message was not printed"
            $output = [System.IO.File]::ReadAllText($outputFile)
            $summaryIndex = $output.IndexOf("END EXPORT SUMMARY", [System.StringComparison]::Ordinal)
            $reminderIndex = $output.IndexOf("INSTRUCTIONS REMINDER", [System.StringComparison]::Ordinal)
            Assert ($summaryIndex -ge 0 -and $reminderIndex -gt $summaryIndex) "Reminder is missing or not at the end of the export"
            Assert ($output.StartsWith("==================== FILE START ====================" + [Environment]::NewLine + "Path: SKILL.md")) "Skill is not at the start of the export"
            $previous = -1
            foreach ($path in $resourcePaths) {
                $index = $output.IndexOf("Path: $path", [System.StringComparison]::Ordinal)
                Assert ($index -gt $previous) "Skill resource missing or out of order: $path"
                $content = [System.IO.File]::ReadAllText((Join-Path $repoRoot ("src\ressources\" + $path.Replace("/", "\"))))
                Assert ($output.Contains($content)) "Skill resource content missing: $path"
                $previous = $index
            }
            Assert ($output.IndexOf("object Example", [System.StringComparison]::Ordinal) -gt $previous) "Project content did not follow the skill"
        }

        & java -jar $jarPath -s -S $inputDir $outputFile
        Assert ($LASTEXITCODE -ne 0) "Duplicate skill flags should be rejected"

        Add-Type -AssemblyName System.IO.Compression
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        $modifiedJar = Join-Path $tempRoot "modified.jar"
        Copy-Item $jarPath $modifiedJar
        $zip = [System.IO.Compression.ZipFile]::Open($modifiedJar, [System.IO.Compression.ZipArchiveMode]::Update)
        try {
            $zip.GetEntry("SKILL.md").Delete()
            $entry = $zip.CreateEntry("SKILL.md")
            $resourceWriter = New-Object System.IO.StreamWriter($entry.Open(), (New-Object System.Text.UTF8Encoding($false)))
            try {
                $resourceWriter.Write("Skill " + [char]::ConvertFromUtf32(0x6F22))
            } finally {
                $resourceWriter.Dispose()
            }
        } finally {
            $zip.Dispose()
        }
        & java "-Dfile.encoding=windows-1252" -jar $modifiedJar -s $inputDir $outputFile
        Assert ($LASTEXITCODE -eq 0) "Unicode skill invocation failed"
        $output = [System.IO.File]::ReadAllText($outputFile, [System.Text.Encoding]::UTF8)
        Assert ($output.Contains("Skill " + [char]::ConvertFromUtf32(0x6F22))) "Unicode skill content was not preserved"

        $zip = [System.IO.Compression.ZipFile]::Open($modifiedJar, [System.IO.Compression.ZipArchiveMode]::Update)
        try {
            $zip.GetEntry("references/plan-template.md").Delete()
        } finally {
            $zip.Dispose()
        }
        [System.IO.File]::WriteAllText($outputFile, "preserve existing output")
        & java -jar $modifiedJar -s $inputDir $outputFile
        Assert ($LASTEXITCODE -ne 0) "Missing resource should fail"
        Assert ([System.IO.File]::ReadAllText($outputFile) -eq "preserve existing output") "Missing resource overwrote the output"
        Write-Host "Skill option integration tests passed."
    } finally {
        Pop-Location
    }
} finally {
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}
