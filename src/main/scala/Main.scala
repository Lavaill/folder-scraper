import java.io.{File, PrintWriter}
import scala.io.Source

object Main {
    def main(args: Array[String]): Unit = {
        if (args.length != 2) {
            println("Usage: scala Main <input_directory> <output_file>")
            System.exit(1)
        }

        val inputDir = new File(args(0))
        val outputFile = new File(args(1))

        if (!inputDir.exists() || !inputDir.isDirectory) {
            println(s"Invalid input directory: ${args(0)}")
            System.exit(1)
        }

        val writer = new PrintWriter(outputFile)
        val files = FileWalker.getValidFiles(inputDir)

        files.foreach { file =>
            val relativePath = inputDir.toPath.relativize(file.toPath).toString
            writer.println(FileFormatter.header(file, relativePath))
            val contentSource: Source = Source.fromFile(file)

            val contentString = contentSource.getLines().mkString("\n")
            writer.println(contentString)
            writer.println(FileFormatter.footer())
            contentSource.close()
        }

        writer.close()
        println(s"Project files written to ${outputFile.getAbsolutePath}")
    }
}
