package ziox.ramiro.saes.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AgendaTrabajoService(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return Result.success()
    }
}