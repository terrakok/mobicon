import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.github.terrakok.mobicon.App
import com.github.terrakok.mobicon.DeeplinkService
import kotlinx.browser.window

private val deeplink = DeeplinkService()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    deeplink.setDeepLink(window.location.toString())
    ComposeViewport { App(deeplink) }
}
