package ziox.ramiro.saes.features.saes.features.profile.data.models

import okhttp3.Headers
import java.util.*

data class User(
    val id: String,
    val name: String,
    val school: String,
    val curp: String,
    val rfc: String,
    val gender: Boolean,
    val profilePicture: ProfilePicture,
    val birthday: Date,
    val nationality: String,
    val state: String,
    val isWorking: Boolean,
    val address: Address,
    val contactInformation: ContactInformation,
    val education: Education,
    val parent: Parent
)


data class ProfilePicture(
    val url: String,
    val headers: Headers
)

data class Address(
    val street: String,
    val extNumber: String,
    val intNumber: String,
    val suburb: String,
    val zip: String,
    val state: String,
    val municipality: String
)


data class ContactInformation(
    val phoneNumber: String,
    val mobilePhoneNumber: String,
    val email: String,
    val officePhone: String
)

data class Education(
    val highSchoolName: String,
    val highSchoolState: String,
    val highSchoolFinalGrade: Double,
    val middleSchoolFinalGrade: Double
)

data class Parent(
    val guardianName: String,
    val guardianRfc: String,
    val fatherName: String,
    val motherName: String
)