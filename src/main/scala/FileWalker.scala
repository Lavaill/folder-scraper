import java.io.File

object FileWalker {

    private val excludedDirs = Set(".git", "target", "build", "out", ".idea", ".vscode", "null", "project", "docker", ".mvn", "mvnw")
    private val excludedExtensions = Set("class", "jar", "log", "iml", "DS_Store", "gitignore", "mvnw", "dockerfile", "sql", "cmd")
    private val excludedNames = Set("CHANGELOG", ".gitlab-ci")

    def getValidFiles(root: File): List[File] = {
        def isExcluded(file: File): Boolean = {
            val name = file.getName
            file.isHidden ||
            excludedNames.exists(name.contains(_)) ||
            excludedDirs.contains(name) ||
            (file.isFile && excludedExtensions.exists{excluded =>
                name.endsWith("." + excluded)
            })
        }

        def walk(dir: File): List[File] = {
            val children = Option(dir.listFiles()).getOrElse(Array.empty)
            children.toList
                .filterNot(isExcluded)
                .flatMap {
                    case f if f.isDirectory => walk(f)
                    case f if f.isFile => List(f)
                    case _ => Nil
                }
        }

        walk(root)
    }
}
