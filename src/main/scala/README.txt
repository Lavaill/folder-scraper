Usage:

java -jar FolderScraper.jar <input_directory> <output_file>
java -jar FolderScraper.jar -s <input_directory> <output_file>

-s (or -S) prepends the bundled SKILL.md and its three reference files to the
project export. The option may appear before, between, or after the paths.
Exports with the option are written in UTF-8; without it, the export contains
only the project files and uses the existing platform-default encoding.

Project files are sorted by relative path, with an index before their contents
and a summary at the end. The skill, when requested, remains first.

An output inside the input directory is allowed only if it does not already
exist; this prevents overwriting project files. Use an output outside the
input directory to overwrite an earlier export. Traversal errors abort before
writing the output. File-read errors are recorded in the export and reported
on stderr; an incomplete export or write failure returns a nonzero exit code.

Example:

java -jar Tools\FolderScraper\target\scala-2.13\FolderScraper.jar Cocon\cocon-api\ cocon-api.txt -S

Build the standalone JAR with sbt assembly. The skill resources in
src\ressources are bundled into the JAR, so it can be run from any directory.
After building, run the integration checks with:
powershell -File src\test\skill-option.ps1
powershell -File src\test\export-safety.ps1
sbt assembly "Test / runMain FileWalkerChecks"