import java.io.{BufferedWriter, File, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

object Main {
    private val skillResources = List(
        "SKILL.md",
        "references/inspection-checklist.md",
        "references/plan-template.md",
        "references/handoff-template.md"
    )

    private val instructionsReminder: String =
        """|==================== INSTRUCTIONS REMINDER ====================
           |The instructions at the top of this file (SKILL.md and references/*) remain in force for every request in this conversation.
           |- Deliver a plan first (references/plan-template.md). Ask questions only for material ambiguities.
           |- Only after the user explicitly approves the plan, deliver the handoff: one sentence, then one fenced block using the headings of references/handoff-template.md, with nothing after the block and no links or references to this file.
           |- You produce plans and handoffs, not the implementation.
           |================== END INSTRUCTIONS REMINDER ==================""".stripMargin

    private val suggestedMessage: String =
        "Follow the instructions in SKILL.md at the top of the attached file. Produce a plan first, then the handoff only after I approve the plan. My ticket: <paste ticket>"

    private def readSkillResources(): List[(String, String)] = {
        skillResources.map { path =>
            val stream = Option(getClass.getClassLoader.getResourceAsStream(path))
                .getOrElse(throw new IllegalStateException(s"Missing bundled skill resource: $path"))
            Using.resource(Source.fromInputStream(stream, "UTF-8"))(source => path -> source.mkString)
        }
    }

    private def writeLine(writer: BufferedWriter, text: String): Unit = {
        writer.write(text)
        writer.newLine()
    }

    private def writeFile(writer: BufferedWriter, path: String, content: String): Unit = {
        writeLine(writer, FileFormatter.header(new File(path), path))
        writeLine(writer, content)
        writeLine(writer, FileFormatter.footer())
    }

    def main(args: Array[String]): Unit = {
        val skillFlags = args.count(arg => arg == "-s" || arg == "-S")
        val positionalArgs = args.filterNot(arg => arg == "-s" || arg == "-S")
        if (positionalArgs.length != 2 || skillFlags > 1) {
            System.err.println("Usage: java -jar FolderScraper.jar [-s|-S] <input_directory> <output_file>")
            sys.exit(1)
        }

        try {
            val inputDir = new File(positionalArgs(0)).getCanonicalFile
            val outputFile = new File(positionalArgs(1)).getCanonicalFile
            if (!inputDir.isDirectory) {
                System.err.println(s"Invalid input directory: ${positionalArgs(0)}")
                sys.exit(1)
            }
            if (outputFile.toPath.startsWith(inputDir.toPath) && outputFile.exists()) {
                System.err.println(s"Refusing to overwrite an existing file in the input directory: $outputFile")
                sys.exit(1)
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

            val walk = FileWalker.getValidFiles(inputDir, outputFile)
            if (walk.errors.nonEmpty || walk.outputConflicts.nonEmpty) {
                walk.errors.foreach(System.err.println)
                walk.outputConflicts.foreach(file =>
                    System.err.println(s"Output conflicts with an input file: ${file.getPath}"))
                System.err.println("Export aborted; output was not written.")
                sys.exit(1)
            }

            val files = walk.files.map { file =>
                val relativePath = inputDir.toPath.relativize(file.toPath).toString
                relativePath -> file
            }.sortBy(_._1)
            var failedFiles = 0
            Using.resource(Files.newBufferedWriter(outputFile.toPath, StandardCharsets.UTF_8)) { writer =>
                skillContents.foreach { case (path, content) => writeFile(writer, path, content) }

                writeLine(writer, "==================== PROJECT FILE INDEX ====================")
                writeLine(writer, s"Files discovered: ${files.size}")
                files.foreach { case (path, _) => writeLine(writer, s"- $path") }
                writeLine(writer, "================== END PROJECT FILE INDEX ==================")
                writeLine(writer, "")

                files.foreach { case (relativePath, file) =>
                    val content = try {
                        Right(Using.resource(Source.fromFile(file, "UTF-8"))(_.getLines().mkString("\n")))
                    } catch {
                        case e: IOException => Left(e)
                        case e: SecurityException => Left(e)
                    }
                    content match {
                        case Right(value) => writeFile(writer, relativePath, value)
                        case Left(exception) =>
                            failedFiles += 1
                            val message = s"Could not read file: $relativePath ($exception)"
                            System.err.println(message)
                            writeFile(writer, relativePath, message)
                    }
                }

                writeLine(writer, "==================== EXPORT SUMMARY ====================")
                writeLine(writer, s"Files exported: ${files.size - failedFiles}")
                writeLine(writer, s"Files failed: $failedFiles")
                writeLine(writer, "================== END EXPORT SUMMARY ==================")
                if (skillFlags == 1) {
                    writeLine(writer, "")
                    writeLine(writer, instructionsReminder)
                }
            }

            if (failedFiles > 0) {
                System.err.println(s"Export incomplete: $failedFiles file(s) could not be read. Output: $outputFile")
                sys.exit(1)
            }
            println(s"Project files written to ${outputFile.getAbsolutePath} (${files.size} file(s))")
            if (skillFlags == 1) {
                println("Suggested first chat message:")
                println(suggestedMessage)
            }
        } catch {
            case e: IOException =>
                System.err.println(s"Export failed: $e")
                sys.exit(1)
            case e: SecurityException =>
                System.err.println(s"Export failed: $e")
                sys.exit(1)
        }
    }
}
