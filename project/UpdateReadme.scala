import sbt._, Keys._
import sbtrelease.ReleasePlugin.autoImport.ReleaseStep
import sbtrelease.Git
import scala.sys.process.Process

object UpdateReadme {
  val scalapropsDerivingName = "scalaprops-deriving"

  val updateReadmeTask = { state: State =>
    val extracted = Project.extract(state)
    val v = extracted get version
    val org = extracted get organization
    val snapshot = extracted get isSnapshot
    val modules = Seq(scalapropsDerivingName)
    val readme = "README.md"
    val readmeFile = file(readme)
    val newReadme = Predef
      .augmentString(IO.read(readmeFile))
      .lines
      .map { line =>
        if (line.startsWith("libraryDependencies") && line.contains(" %% ") && !snapshot) {
          val i = modules.map("\"" + _ + "\"").indexWhere(line.contains)
          s"""libraryDependencies += "$org" %% "${modules(i)}" % "$v""""
        } else {
          line
        }
      }
      .mkString("", "\n", "\n")
    IO.write(readmeFile, newReadme)
    val git = new Git(extracted get baseDirectory)
    git.add(readme) ! state.log
    git.commit(message = "update " + readme, sign = false, signOff = false) ! state.log
    Process("git diff HEAD^") ! state.log
    state
  }

  val updateReadmeProcess: ReleaseStep = updateReadmeTask
}
