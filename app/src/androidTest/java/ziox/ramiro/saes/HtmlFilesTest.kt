package ziox.ramiro.saes

import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ziox.ramiro.saes.features.about.ui.screens.AboutActivity
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.utils.runOnMainThread

@RunWith(AndroidJUnit4ClassRunner::class)
class HtmlFilesTest {

    @Test
    fun testScheduleFiles() {
        ActivityScenario.launch(AboutActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.runOnUiThread {
                    val files = activity.assets.list("schedule_errors")

                    if (files == null) {
                        throw AssertionError("No schedule tests found")
                    }

                    val failedFiles = mutableListOf<Pair<String, Throwable>>()

                    Log.d("HtmlFilesTest", "Found ${files.size} schedule test files")

                    files.forEach { file ->
                        val repository =
                            ScheduleWebViewRepository(activity, withTestFile = "schedule_errors/$file")

                        runBlocking {
                            runCatching {
                                repository.getMySchedule()
                            }.onFailure {
                                failedFiles.add(file to it)
                                Log.e("HtmlFilesTest", "File $file failed with error: ${it.message}", it)
                            }.onSuccess {
                                Log.d("HtmlFilesTest", "File $file processed successfully with ${it.size} classes")
                            }
                        }
                    }

                    Assert.assertEquals(emptyList<Pair<String, Throwable>>(), failedFiles)
                }
            }

        }
    }

    @Test
    fun testGradesFiles() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        runOnMainThread {
            val repository = GradesWebViewRepository(context)

            val files = context.assets.list("grades_tests")

            if (files.isNullOrEmpty()) {
                throw AssertionError("No grades tests found")
            }
        }
    }
}