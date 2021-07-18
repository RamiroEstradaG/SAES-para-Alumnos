package ziox.ramiro.saes.features.saes.features.profile.data.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.Headers
import java.util.*

@Entity(tableName = "profiles")
data class ProfileUser(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "school")
    val school: String,
    @ColumnInfo(name = "curp")
    val curp: String,
    @ColumnInfo(name = "rfc")
    val rfc: String,
    @ColumnInfo(name = "gender")
    val gender: Boolean,
    @Embedded
    val profilePicture: ProfilePicture,
    @ColumnInfo(name = "birthday")
    val birthday: Date,
    @ColumnInfo(name = "nationality")
    val nationality: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "isWorking")
    val isWorking: Boolean,
    @Embedded
    val address: Address,
    @Embedded
    val contactInformation: ContactInformation,
    @Embedded
    val education: Education,
    @Embedded
    val parent: Parent
)


data class ProfilePicture(
    @ColumnInfo(name = "profile_picture_url")
    val url: String,
    @ColumnInfo(name = "profile_picture_headers")
    val headers: Headers = Headers.of(mapOf())
)


data class Address(
    @ColumnInfo(name = "address")
    val street: String,
    @ColumnInfo(name = "ext_number")
    val extNumber: String,
    @ColumnInfo(name = "int_number")
    val intNumber: String,
    @ColumnInfo(name = "suburb")
    val suburb: String,
    @ColumnInfo(name = "zip")
    val zip: String,
    @ColumnInfo(name = "address_state")
    val state: String,
    @ColumnInfo(name = "address_municipality")
    val municipality: String
)


data class ContactInformation(
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,
    @ColumnInfo(name = "mobile_phone")
    val mobilePhoneNumber: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "office_phone")
    val officePhone: String
)


data class Education(
    @ColumnInfo(name = "high_school_name")
    val highSchoolName: String,
    @ColumnInfo(name = "high_school_state")
    val highSchoolState: String,
    @ColumnInfo(name = "high_school_final_grade")
    val highSchoolFinalGrade: Double,
    @ColumnInfo(name = "middle_school_grade")
    val middleSchoolFinalGrade: Double
)


data class Parent(
    @ColumnInfo(name = "guardian_name")
    val guardianName: String,
    @ColumnInfo(name = "guardian_rfc")
    val guardianRfc: String,
    @ColumnInfo(name = "father_name")
    val fatherName: String,
    @ColumnInfo(name = "mother_name")
    val motherName: String
)