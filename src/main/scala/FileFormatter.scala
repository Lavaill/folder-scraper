import java.io.File

object FileFormatter {

    def header(file: File, relativePath: String): String = {
        val fileName = file.getName
        val extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase
        val fileType = extension match {
            case "scala" => "Scala Source File (.scala)"
            case "sbt" => "Sbt Source File (.sbt)"
            case "java"  => "Java Source File (.java)"
            case "kt"    => "Kotlin Source File (.kt)"
            case "xml"   => "XML Configuration File (.xml)"
            case "yml" | "yaml" => "YAML Configuration File (." + extension + ")"
            case "properties" => "Java Properties File (.properties)"
            case "conf"  => "HOCON Configuration File (.conf)"
            case "json"  => "JSON File (.json)"
            case "csv"   => "CSV File (.csv)"
            case "txt"   => "Text File (.txt)"
            case "html"  => "HTML File (.html)"
            case "js"    => "JavaScript File (.js)"
            case "css"   => "CSS File (.css)"
            case "md"    => "Markdown File (.md)"
            case _       => s"Unknown File Type (.$extension)"
        }

        s"""|==================== FILE START ====================
            |Path: $relativePath
            |Name: $fileName
            |Type: $fileType
            |----------------------------------------------------""".stripMargin
    }

    def footer(): String = {
        "===================== FILE END =====================\n"
    }
}
