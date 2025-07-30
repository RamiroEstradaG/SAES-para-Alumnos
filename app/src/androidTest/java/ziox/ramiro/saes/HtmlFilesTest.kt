package ziox.ramiro.saes

import android.app.Activity
import android.util.Log
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
        testFiles("schedule_errors") { activity, folderName, file ->
            val repository =
                ScheduleWebViewRepository(activity, withTestFile = "$folderName/$file")

            runCatching {
                repository.getMySchedule()
            }.onSuccess {
                if (it.isEmpty()) {
                    throw AssertionError("File $file returned an empty schedule");
                }
            }.exceptionOrNull()
        }
    }

    @Test
    fun testGradesFiles() = runTest {
        testFiles("grades_errors") { activity, folderName, file ->
            val repository =
                GradesWebViewRepository(activity, withTestFile = "$folderName/$file")

            runCatching {
                repository.getMyGrades()
            }.onSuccess {
                if (it.isEmpty()) {
                    throw AssertionError("File $file returned an empty grades list");
                }
            }.exceptionOrNull()
        }

    }

    @Test
    fun testProfileFiles() = runTest {
        testFiles("profile_errors") { activity, folderName, file ->
            val repository =
                ProfileWebViewRepository(activity, withTestFile = "$folderName/$file")

            runCatching {
                repository.getMyUserData()
            }.exceptionOrNull()
        }
    }

    @Test
    fun testLoginFiles() = runTest {
        testFiles("login_errors", false) { activity, folderName, file ->
            val repository =
                AuthWebViewRepository(activity, withTestFile = "$folderName/$file")

            runCatching {
                repository.login("testuser", "testpassword", "testcaptcha")
            }.exceptionOrNull()
        }
    }

    @Test
    fun testCaptchaFiles() = runTest {
        testFiles("captcha_errors", false) { activity, folderName, file ->
            val repository =
                AuthWebViewRepository(activity, withTestFile = "$folderName/$file")

            runCatching {
                repository.getCaptcha()
            }.onSuccess {
                if (it.url == null || it.url.isBlank()) {
                    throw AssertionError("File $file returned an empty captcha");
                }
            }.exceptionOrNull()
        }
    }

    private suspend fun testFiles(
        folderName: String,
        removeUnlogged: Boolean = true,
        fileTester: suspend (Activity, String, String) -> Throwable?
    ) {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            var activity: AboutActivity? = null
            scenario.onActivity { act ->
                activity = act
            }

            if (activity == null) {
                throw AssertionError("Activity is null")
            }

            val fileList = activity.assets.list(folderName)

            val files = fileList?.filterNot { fileName ->
                activity.assets.open("$folderName/$fileName").use { inputStream ->
                    val body = inputStream.bufferedReader().use { it.readText() }

                    return@filterNot (body.length < 100 || (
                            removeUnlogged &&
                                    (body.contains("ctl00_leftColumn_LoginUser_CaptchaCodeTextBox")
                                            || body.contains("c_default_leftcolumn_loginuser_logincaptcha_CaptchaImage")
                                            || body.contains("leftColumn_LoginUser_LoginButton"))
                            ))
                }
            }

            Log.d(
                "HtmlFilesTest",
                "Invalid files: ${fileList?.size?.minus(files?.size ?: 0) ?: 0} in folder: $folderName"
            )

            if (files == null) {
                throw AssertionError("No tests found in folder: $folderName")
            }

            val failedFiles = mutableListOf<Pair<String, Throwable>>()

            Log.d("HtmlFilesTest", "Found ${files.size} schedule test files")

            withContext(Dispatchers.Main.immediate) {
                files.forEachIndexed { index, file ->
                    Log.d(
                        "HtmlFilesTest",
                        "Testing in progress: ${index + 1}/${files.size} - $file"
                    )
                    val error = fileTester(activity, folderName, file)
                    if (error != null) {
                        failedFiles.add(file to error)
                        Log.e("HtmlFilesTest", "❌ ${error.message}")
                    } else {
                        Log.d("HtmlFilesTest", "✅")
                    }
                }
            }

            Log.e("HtmlFilesTest", "Failed files: ${failedFiles.size}")

            failedFiles.forEach { (file, error) ->
                Log.e("HtmlFilesTest", "File: $file failed with error: ${error.message}", error)
            }

            Assert.assertEquals(0, failedFiles.size)
        }
    }
}