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

tasks.register("downloadTestFiles", Exec::class) {
    workingDir = file("src/main/assets/").apply {
        if (!exists()) {
            mkdirs()
        }
    }

    val testFolders = listOf(
        "schedule_errors",
        "grades_errors",
        "profile_errors",
        "login_errors",
    )

    testFolders.forEach { folder ->
        println("Downloading test files for folder: $folder")
        if(System.getProperty("os.name").lowercase().contains("windows")){
            commandLine(
                "cmd", "/c", "gsutil", "-m", "cp", "-r",
                "gs://saes-para-alumnos.appspot.com/files/$folder",
                "."
            )
        }else{
            commandLine(
                "bash", "-c", "gsutil", "-m", "cp", "-r",
                "gs://saes-para-alumnos.appspot.com/files/$folder",
                "."
            )
        }
    }

    standardOutput = ByteArrayOutputStream()

    doLast {
        val output = standardOutput.toString()
        println("Download completed: $output")
    }
}