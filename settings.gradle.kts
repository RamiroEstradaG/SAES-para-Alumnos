pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

//plugins {
//    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.0.28"
//}

//gitHooks {
//    preCommit {
//        from { // TODO Fix this
//            """
//                ./gradlew downloadTestFiles
//                ./gradlew test
//            """.trimIndent()
//        }
//    }
//
//    createHooks(true)
//}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "SAES para Alumnos"
include(":app")
 