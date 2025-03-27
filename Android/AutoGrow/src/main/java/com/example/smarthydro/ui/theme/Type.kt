import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smarthydro.R
import com.example.smarthydro.ui.theme.AquaBlue
import com.example.smarthydro.ui.theme.TextWhite

val leagueSpartan = FontFamily(
    listOf(
        Font(R.font.leaguespartan_regular, FontWeight.Normal),
        Font(R.font.leaguespartan_medium, FontWeight.Medium),
        Font(R.font.leaguespartan_semibold, FontWeight.SemiBold),
        Font(R.font.leaguespartan_bold, FontWeight.Bold),
        Font(R.font.leaguespartan_black, FontWeight.Black)
    )
)

val Typography = Typography(
    bodyLarge = TextStyle(
        color = AquaBlue,
        fontFamily = leagueSpartan,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    headlineLarge = TextStyle(
        color = TextWhite,
        fontFamily = leagueSpartan,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    headlineMedium = TextStyle(
        color = TextWhite,
        fontFamily = leagueSpartan,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = leagueSpartan,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    )

)
