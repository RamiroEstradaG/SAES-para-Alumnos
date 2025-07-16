package ziox.ramiro.saes

import android.util.Log
import android.webkit.URLUtil
import androidx.test.core.app.ActivityScenario
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.features.about.ui.screens.AboutActivity
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository

@RunWith(AndroidJUnit4ClassRunner::class)
class HtmlFilesTest {
    @Test
    fun testScheduleFiles() = runTest {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val files = activity.assets.list("schedule_errors")

            if (files == null) {
                throw AssertionError("No schedule tests found")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} schedule test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEach { file ->
                    val repository =
                        ScheduleWebViewRepository(activity, withTestFile = "schedule_errors/$file")

                    runCatching {
                        repository.getMySchedule()
                    }.onFailure {
                        failedFiles.add(file to it)
                        Log.e(
                            "HtmlFilesTest",
                            "File $file failed with error: ${it.message}",
                            it
                        )
                    }.onSuccess {
                        if(it.isNotEmpty()){
                            Log.d(
                                "HtmlFilesTest",
                                "File $file processed successfully with ${it.size} grades"
                            )
                        }else{
                            failedFiles.add(file to AssertionError("File $file returned an empty schedule"))
                        }
                    }
                }
            }

            Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)

        }

    }

    @Test
    fun testGradesFiles() = runTest {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val files = activity.assets.list("grades_errors")

            if (files == null) {
                throw AssertionError("No grades tests found")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} grades test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEach { file ->
                    val repository =
                        GradesWebViewRepository(activity, withTestFile = "grades_errors/$file")

                    runCatching {
                        repository.getMyGrades()
                    }.onFailure {
                        failedFiles.add(file to it)
                        Log.e(
                            "HtmlFilesTest",
                            "File $file failed with error: ${it.message}",
                            it
                        )
                    }.onSuccess {
                        if (it.isNotEmpty()) {
                            Log.d(
                                "HtmlFilesTest",
                                "File $file processed successfully with ${it.size} grades"
                            )
                        }else {
                            failedFiles.add(file to AssertionError("File $file returned an empty grades list"))
                        }
                    }
                }
            }

            Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)

        }

    }

    @Test
    fun testProfileFiles() = runTest {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val files = activity.assets.list("profile_errors")

            if (files == null) {
                throw AssertionError("No grades tests found")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} profile test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEach { file ->
                    val repository =
                        ProfileWebViewRepository(activity, withTestFile = "profile_errors/$file")

                    runCatching {
                        repository.getMyUserData()
                    }.onFailure {
                        failedFiles.add(file to it)
                        Log.e(
                            "HtmlFilesTest",
                            "File $file failed with error: ${it.message}",
                            it
                        )
                    }.onSuccess {
                        Log.d(
                            "HtmlFilesTest",
                            "File $file processed successfully with data: $it"
                        )
                    }
                }
            }

            Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)

        }

    }

    @Test
    fun testLoginFiles() = runTest {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val files = activity.assets.list("login_errors")

            if (files == null) {
                throw AssertionError("No login tests found")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} grades test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEach { file ->
                    val repository =
                        AuthWebViewRepository(activity, withTestFile = "login_errors/$file")

                    runCatching {
                        repository.login("a", "b", "c")
                    }.onFailure {
                        failedFiles.add(file to it)
                        Log.e(
                            "HtmlFilesTest",
                            "File $file failed with error: ${it.message}",
                            it
                        )
                    }.onSuccess {
                        it.isLoggedIn
                        Log.d(
                            "HtmlFilesTest",
                            "File $file processed successfully with data: $it"
                        )
                    }
                }
            }

            Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)

        }

    }

    @Test
    fun testCaptchaFiles() = runTest {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val files = activity.assets.list("login_errors")

            if (files == null) {
                throw AssertionError("No login tests found")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} grades test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEach { file ->
                    val repository =
                        AuthWebViewRepository(activity, withTestFile = "login_errors/$file")

                    runCatching {
                        repository.getCaptcha()
                    }.onFailure {
                        failedFiles.add(file to it)
                        Log.e(
                            "HtmlFilesTest",
                            "File $file failed with error: ${it.message}",
                            it
                        )
                    }.onSuccess {
                        if(it.url.isNotBlank() && URLUtil.isValidUrl(it.url)){
                            Log.d(
                                "HtmlFilesTest",
                                "File $file processed successfully with ${it.url}"
                            )
                        }else {
                            failedFiles.add(file to AssertionError("File $file returned an empty captcha"))
                        }
                    }
                }
            }

            Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)

        }

    }
}