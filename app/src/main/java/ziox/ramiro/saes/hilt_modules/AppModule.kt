package ziox.ramiro.saes.hilt_modules

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ziox.ramiro.saes.data.repositories.AuthRepository
import ziox.ramiro.saes.data.repositories.AuthWebViewRepository
import ziox.ramiro.saes.data.repositories.BillingGooglePayRepository
import ziox.ramiro.saes.data.repositories.BillingRepository
import ziox.ramiro.saes.data.repositories.LocalAppDatabase
import ziox.ramiro.saes.features.saes.data.repositories.StorageFirebaseRepository
import ziox.ramiro.saes.features.saes.data.repositories.StorageRepository
import ziox.ramiro.saes.features.saes.data.repositories.UserFirebaseRepository
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaFirebaseRepository
import ziox.ramiro.saes.features.saes.features.agenda.data.repositories.AgendaRepository
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSRepository
import ziox.ramiro.saes.features.saes.features.ets.data.repositories.ETSWebViewRepository
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarRepository
import ziox.ramiro.saes.features.saes.features.ets_calendar.data.repositories.ETSCalendarWebViewRepository
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesRepository
import ziox.ramiro.saes.features.saes.features.grades.data.repositories.GradesWebViewRepository
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRepository
import ziox.ramiro.saes.features.saes.features.home.data.repositories.TwitterRetrofitRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexRepository
import ziox.ramiro.saes.features.saes.features.kardex.data.repositories.KardexWebViewRepository
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyRepository
import ziox.ramiro.saes.features.saes.features.occupancy.data.repositories.OccupancyWebViewRepository
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceFirebaseRepository
import ziox.ramiro.saes.features.saes.features.performance.data.repositories.PerformanceRepository
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileRepository
import ziox.ramiro.saes.features.saes.features.profile.data.repositories.ProfileWebViewRepository
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationRepository
import ziox.ramiro.saes.features.saes.features.re_registration_appointment.data.repositories.ReRegistrationWebViewRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleRepository
import ziox.ramiro.saes.features.saes.features.schedule.data.repositories.ScheduleWebViewRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleRepository
import ziox.ramiro.saes.features.saes.features.school_schedule.data.repositories.SchoolScheduleWebViewRepository
import ziox.ramiro.saes.utils.UserPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthRepository(app: Application): AuthRepository = AuthWebViewRepository(app)

    @Provides
    @Singleton
    fun provideBillingRepository(app: Application): BillingRepository = BillingGooglePayRepository(app)

    @Singleton
    @Provides
    fun provideProfileRepository(app: Application): ProfileRepository = ProfileWebViewRepository(app)

    @Singleton
    @Provides
    fun provideETSRepository(app: Application): ETSRepository = ETSWebViewRepository(app)

    @Singleton
    @Provides
    fun provideGradesRepository(app: Application): GradesRepository = GradesWebViewRepository(app)

    @Singleton
    @Provides
    fun provideETSCalendarRepository(app: Application): ETSCalendarRepository = ETSCalendarWebViewRepository(app)

    @Singleton
    @Provides
    fun provideKardexRepository(app: Application): KardexRepository = KardexWebViewRepository(app)

    @Singleton
    @Provides
    fun provideOccupancyRepository(app: Application): OccupancyRepository = OccupancyWebViewRepository(app)

    @Singleton
    @Provides
    fun provideReRegistrationRepository(app: Application): ReRegistrationRepository = ReRegistrationWebViewRepository(app)

    @Singleton
    @Provides
    fun provideScheduleRepository(app: Application): ScheduleRepository = ScheduleWebViewRepository(app)

    @Singleton
    @Provides
    fun provideSchoolScheduleRepository(app: Application): SchoolScheduleRepository = SchoolScheduleWebViewRepository(app)

    @Singleton
    @Provides
    fun provideUserFirebaseRepository(): UserFirebaseRepository = UserFirebaseRepository()

    @Singleton
    @Provides
    fun provideAgendaRepository(): AgendaRepository = AgendaFirebaseRepository()

    @Singleton
    @Provides
    fun provideTwitterRepository(): TwitterRepository = TwitterRetrofitRepository()

    @Singleton
    @Provides
    fun providePerformanceRepository(): PerformanceRepository = PerformanceFirebaseRepository()

    @Singleton
    @Provides
    fun provideUserPreferences(app: Application): UserPreferences = UserPreferences.invoke(app)

    @Singleton
    @Provides
    fun provideLocalAppDatabase(app: Application): LocalAppDatabase = LocalAppDatabase.invoke(app)

    @Singleton
    @Provides
    fun provideStorageRepository(): StorageRepository = StorageFirebaseRepository()
}