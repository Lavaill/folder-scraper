import java.io.File

object FileWalkerChecks {
    def main(args: Array[String]): Unit = {
        val unreadable = new File("unreadable-directory") {
            override def listFiles(): Array[File] = null
        }
        val result = FileWalker.getValidFiles(unreadable, new File("output.txt").getCanonicalFile)
        assert(result.files.isEmpty)
        assert(result.errors.size == 1 && result.errors.head.contains("Could not list directory"))

        val denied = new File("denied-directory") {
            override def listFiles(): Array[File] = throw new SecurityException("access denied")
        }
        val deniedResult = FileWalker.getValidFiles(denied, new File("output.txt").getCanonicalFile)
        assert(deniedResult.errors.size == 1 && deniedResult.errors.head.contains("access denied"))
        println("File walker error check passed.")
    }
}
