package ziox.ramiro.saes

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.utils.runOnMainThread


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class HtmlFilesTest {
    @Test
    fun testScheduleFiles() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        runOnMainThread {
            val files = context.assets.list("schedule_tests")

            if (files.isNullOrEmpty()) {
                throw AssertionError("No schedule tests found")
            }

            val failedFiles = mutableListOf<String>()

            files.forEach { file ->
                val repository =
                    ScheduleWebViewRepository(context, withTestFile = "schedule_tests/$file")

                runBlocking {
                    runCatching {
                        repository.getMySchedule()
                    }.onFailure {
                        failedFiles.add(file)
                    }
                }
            }

            Assert.assertEquals(emptyList<String>(), failedFiles)
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