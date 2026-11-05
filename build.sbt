

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version := "0.0.1"


lazy val app = (project in file("."))
  .settings(
    name := "folder-scraper",
    Compile / unmanagedResourceDirectories += baseDirectory.value / "src" / "ressources",
    assembly / assemblyJarName := "FolderScraper.jar",
    assembly / mainClass := Some("Main")
  )
