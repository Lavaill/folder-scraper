import java.io.File
import java.io.IOException
import java.nio.file.Files
import scala.collection.mutable.ListBuffer

object FileWalker {

    case class WalkResult(files: List[File], errors: List[String], outputConflicts: List[File])

    private val excludedDirs = Set(".git", "target", "build", "out", ".idea", ".vscode", "null", "project", "docker", ".mvn", "mvnw")
    private val excludedExtensions = Set("class", "jar", "log", "iml", "DS_Store", "gitignore", "mvnw", "dockerfile", "sql", "cmd", "png", "jpg", "svg")
    private val excludedNames = Set("CHANGELOG", ".gitlab-ci")

    def getValidFiles(root: File, outputFile: File): WalkResult = {
        val files = ListBuffer.empty[File]
        val errors = ListBuffer.empty[String]
        val outputConflicts = ListBuffer.empty[File]

        def isExcluded(file: File): Boolean = {
            val name = file.getName
            file.isHidden ||
            excludedNames.exists(name.contains(_)) ||
            excludedDirs.contains(name) ||
            (file.isFile && excludedExtensions.exists{excluded =>
                name.endsWith("." + excluded)
            })
        }

        def walk(dir: File): Unit = {
            try {
                val children = dir.listFiles()
                if (children == null) {
                    errors += s"Could not list directory: ${dir.getPath}"
                } else {
                    children.filterNot(isExcluded).foreach { file =>
                        if (file.isDirectory) {
                            walk(file)
                        } else if (file.isFile) {
                            if (outputFile.exists() && Files.isSameFile(file.toPath, outputFile.toPath)) {
                                outputConflicts += file
                            } else if (file.getCanonicalFile != outputFile) {
                                files += file
                            }
                        }
                    }
                }
            } catch {
                case e: IOException => errors += s"Could not inspect directory ${dir.getPath}: $e"
                case e: SecurityException => errors += s"Could not inspect directory ${dir.getPath}: $e"
            }
        }

        walk(root)
        WalkResult(files.toList, errors.toList, outputConflicts.toList)
    }
}
