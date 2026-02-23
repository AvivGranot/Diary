package com.proactivediary.ui.onboarding

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Country(
    val name: String,
    val code: String,
    val flag: String,
    val iso: String
)

val COUNTRIES = listOf(
    Country("Israel", "+972", "\uD83C\uDDEE\uD83C\uDDF1", "IL"),
    Country("United States", "+1", "\uD83C\uDDFA\uD83C\uDDF8", "US"),
    Country("United Kingdom", "+44", "\uD83C\uDDEC\uD83C\uDDE7", "GB"),
    Country("Germany", "+49", "\uD83C\uDDE9\uD83C\uDDEA", "DE"),
    Country("France", "+33", "\uD83C\uDDEB\uD83C\uDDF7", "FR"),
    Country("Canada", "+1", "\uD83C\uDDE8\uD83C\uDDE6", "CA"),
    Country("Australia", "+61", "\uD83C\uDDE6\uD83C\uDDFA", "AU"),
    Country("India", "+91", "\uD83C\uDDEE\uD83C\uDDF3", "IN"),
    Country("Brazil", "+55", "\uD83C\uDDE7\uD83C\uDDF7", "BR"),
    Country("Italy", "+39", "\uD83C\uDDEE\uD83C\uDDF9", "IT"),
    Country("Spain", "+34", "\uD83C\uDDEA\uD83C\uDDF8", "ES"),
    Country("Netherlands", "+31", "\uD83C\uDDF3\uD83C\uDDF1", "NL"),
    Country("Russia", "+7", "\uD83C\uDDF7\uD83C\uDDFA", "RU"),
    Country("Japan", "+81", "\uD83C\uDDEF\uD83C\uDDF5", "JP"),
    Country("South Korea", "+82", "\uD83C\uDDF0\uD83C\uDDF7", "KR"),
    Country("Mexico", "+52", "\uD83C\uDDF2\uD83C\uDDFD", "MX"),
    Country("Argentina", "+54", "\uD83C\uDDE6\uD83C\uDDF7", "AR"),
    Country("Turkey", "+90", "\uD83C\uDDF9\uD83C\uDDF7", "TR"),
    Country("South Africa", "+27", "\uD83C\uDDFF\uD83C\uDDE6", "ZA"),
    Country("Sweden", "+46", "\uD83C\uDDF8\uD83C\uDDEA", "SE"),
    Country("Poland", "+48", "\uD83C\uDDF5\uD83C\uDDF1", "PL"),
    Country("Switzerland", "+41", "\uD83C\uDDE8\uD83C\uDDED", "CH"),
    Country("Portugal", "+351", "\uD83C\uDDF5\uD83C\uDDF9", "PT"),
    Country("Thailand", "+66", "\uD83C\uDDF9\uD83C\uDDED", "TH"),
    Country("Singapore", "+65", "\uD83C\uDDF8\uD83C\uDDEC", "SG"),
    Country("Ukraine", "+380", "\uD83C\uDDFA\uD83C\uDDE6", "UA")
)

fun detectCountryFromSim(context: Context): Country {
    try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val iso = tm?.simCountryIso?.uppercase()
            ?: tm?.networkCountryIso?.uppercase()
        if (iso != null) {
            COUNTRIES.find { it.iso == iso }?.let { return it }
        }
    } catch (_: Exception) { }
    return COUNTRIES[0] // Default: Israel
}

@Composable
fun CountryPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (Country) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Country",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            LazyColumn(modifier = Modifier.height(400.dp)) {
                items(COUNTRIES) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(country) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = country.flag, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = country.name,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        Text(
                            text = country.code,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
