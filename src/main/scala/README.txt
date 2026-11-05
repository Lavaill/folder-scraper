Usage:

java -jar FolderScraper.jar <input_directory> <output_file>
java -jar FolderScraper.jar -s <input_directory> <output_file>

-s (or -S) prepends the bundled SKILL.md and its three reference files to the
project export. The option may appear before, between, or after the paths.
Exports with the option are written in UTF-8; without it, the export contains
only the project files and uses the existing platform-default encoding.

Example:

java -jar Tools\FolderScraper\target\scala-2.13\FolderScraper.jar Cocon\cocon-api\ cocon-api.txt -S

Build the standalone JAR with sbt assembly. The skill resources in
src\ressources are bundled into the JAR, so it can be run from any directory.
After building, run the integration checks with:
powershell -File src\test\skill-option.ps1