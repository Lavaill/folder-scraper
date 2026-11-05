import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Main {
    private val skillResources = List(
        "SKILL.md",
        "references/inspection-checklist.md",
        "references/plan-template.md",
        "references/handoff-template.md"
    )

    private def readSkillResources(): List[(String, String)] = {
        skillResources.map { path =>
            val stream = Option(getClass.getClassLoader.getResourceAsStream(path))
                .getOrElse(throw new IllegalStateException(s"Missing bundled skill resource: $path"))
            val source = Source.fromInputStream(stream, "UTF-8")
            try {
                path -> source.mkString
            } finally {
                source.close()
            }
        }
    }

    def main(args: Array[String]): Unit = {
        val skillFlags = args.count(arg => arg == "-s" || arg == "-S")
        val positionalArgs = args.filterNot(arg => arg == "-s" || arg == "-S")
        if (positionalArgs.length != 2 || skillFlags > 1) {
            System.err.println("Usage: java -jar FolderScraper.jar [-s|-S] <input_directory> <output_file>")
            System.exit(1)
        }

        val inputDir = new File(positionalArgs(0))
        val outputFile = new File(positionalArgs(1))

        if (!inputDir.exists() || !inputDir.isDirectory) {
            println(s"Invalid input directory: ${positionalArgs(0)}")
            System.exit(1)
        }

        val skillContents = if (skillFlags == 1) {
            Try(readSkillResources()) match {
                case Success(contents) => contents
                case Failure(exception) =>
                    System.err.println(s"Could not load bundled skill: ${exception.getMessage}")
                    sys.exit(1)
            }
        } else {
            Nil
        }

        val writer = if (skillFlags == 1) new PrintWriter(outputFile, "UTF-8") else new PrintWriter(outputFile)
        skillContents.foreach { case (path, content) =>
            writer.println(FileFormatter.header(new File(path), path))
            writer.println(content)
            writer.println(FileFormatter.footer())
        }
        val files = FileWalker.getValidFiles(inputDir)

        files.foreach { file =>
            val relativePath = inputDir.toPath.relativize(file.toPath).toString


            val contentSource: Source = Source.fromFile(file)

                Try {
                    contentSource.getLines().mkString("\n")
                } match {
                    case Success(value) =>
                        writer.println(FileFormatter.header(file, relativePath))
                        writer.println(value)
                        writer.println(FileFormatter.footer())
                    case Failure(exception) =>
                        writer.println(FileFormatter.header(file, relativePath))
                        writer.println(s"Could not write file : ${relativePath}. Error: ${exception.getClass.getName}")
                        writer.println(FileFormatter.footer())
                }
            contentSource.close()
        }

        writer.close()
        println(s"Project files written to ${outputFile.getAbsolutePath}")
    }
}
