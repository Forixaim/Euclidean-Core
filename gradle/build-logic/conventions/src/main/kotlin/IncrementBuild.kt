import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.OutputFile
import java.util.Properties
import java.io.File

abstract class IncrementBuild : DefaultTask() {

    @get:OutputFile
    val propertiesFile: File = project.file("build.properties")

    @TaskAction
    fun increment() {
        if (propertiesFile.exists()) {
            val props = Properties()
            propertiesFile.inputStream().use { props.load(it) }

            // Read, increment, and write safely using standard Long casting
            val nextBuildNumber = props.getProperty("buildNumber", "0").toLong() + 1
            props.setProperty("buildNumber", nextBuildNumber.toString())

            propertiesFile.outputStream().use {
                props.store(it, "Auto-incremented by IncrementBuild Task")
            }
            logger.lifecycle("🚀 Build complete. Next build number will be: #$nextBuildNumber")
        } else {
            logger.warn("⚠️ build.properties file not found at: ${propertiesFile.absolutePath}")
        }
    }
}