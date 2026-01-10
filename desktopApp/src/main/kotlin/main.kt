import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.terrakok.mobicon.App
import java.awt.Dimension

fun main() = application {
    Window(
        title = "MobiCon",
        state = rememberWindowState(width = 500.dp, height = 900.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(500, 900)
        App()
    }
}
