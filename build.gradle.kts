import java.io.ByteArrayOutputStream

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("com.google.firebase.crashlytics") version "3.0.4" apply false
}


val testFolders = listOf(
    "schedule_errors",
    "grades_errors",
    "profile_errors",
    "login_errors",
    "captcha_errors",
)

val downloadTasks = testFolders.mapIndexed { index, folder ->
    tasks.register("downloadTestFiles$index", Exec::class) {
        workingDir = file("app/src/main/assets/").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val dir = File(workingDir, folder)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        println("Downloading test files for folder: $folder")
        if (System.getProperty("os.name").lowercase().contains("windows")) {
            commandLine(
                "cmd", "/c", "gsutil", "-m", "cp", "-r",
                "gs://saes-para-alumnos.appspot.com/files/$folder",
                "."
            )
        } else {
            commandLine(
                "bash", "-c", "gsutil", "-m", "cp", "-r",
                "gs://saes-para-alumnos.appspot.com/files/$folder",
                "."
            )
        }

        isIgnoreExitValue = true
        standardOutput = ByteArrayOutputStream()

        doLast {
            val output = standardOutput.toString()
            println("Download completed for $folder: $output")
        }
    }
}

downloadTasks.forEachIndexed { index, task ->
    if (index > 0) {
        task.configure {
            dependsOn(downloadTasks[index - 1])
        }
    }
}

tasks.register("downloadTestFiles") {
    dependsOn(downloadTasks.last())
    description = "Download all test files sequentially"
}