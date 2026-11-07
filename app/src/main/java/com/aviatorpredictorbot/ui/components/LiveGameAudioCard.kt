package com.aviatorpredictorbot.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aviatorpredictorbot.ui.theme.ButtonRed
import com.aviatorpredictorbot.ui.theme.DarkField
import com.aviatorpredictorbot.ui.theme.DarkFieldBorder
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import com.aviatorpredictorbot.ui.theme.WaitingGrey

private const val PREFS = "live_game_prefs"
private const val KEY_URL = "game_url"

/**
 * Embeds the live Aviator game itself so you hear the game's OWN sound exactly as it plays
 * (engine, flight music, "flew away"). Enter the game launch URL you normally open in your
 * browser, sign in inside the panel if the operator asks you to, and the audio plays straight
 * from the game.
 *
 * The app does not ship or copy any of the game's audio; it only plays what the game page plays.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveGameAudioCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var urlInput by remember { mutableStateOf(loadUrl(context)) }
    var loadedUrl by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Pause the game page (and its audio) when the app is not in the foreground; resume after.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> webView?.onPause()
                Lifecycle.Event.ON_RESUME -> webView?.onResume()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cardShape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0xFFFAFAFC))
            .border(1.5.dp, ButtonRed, cardShape)
            .padding(16.dp)
            .testTag("live_game_audio_card")
    ) {
        RedFieldLabel("LIVE GAME (REAL GAME SOUND)")
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            placeholder = { Text("Paste your Aviator game launch URL", color = WaitingGrey, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { focusManager.clearFocus() }),
            colors = darkFieldColors(),
            shape = LightFieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("live_game_url_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    val normalized = normalizeUrl(urlInput)
                    if (normalized != null) {
                        saveUrl(context, normalized)
                        urlInput = normalized
                        if (loadedUrl == null) {
                            loadedUrl = normalized
                        } else {
                            loadedUrl = normalized
                            webView?.loadUrl(normalized)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonRed, contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("live_game_load_button")
            ) {
                Text(if (loadedUrl == null) "LOAD LIVE GAME" else "RELOAD", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }

            if (loadedUrl != null) {
                Button(
                    onClick = {
                        loadedUrl = null
                        webView?.apply { stopLoading(); loadUrl("about:blank") }
                        webView = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkField, contentColor = NeonCrimson),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("live_game_stop_button")
                ) {
                    Text("STOP GAME", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (loadedUrl == null) {
                "Load the live game to hear its own sound."
            } else {
                "Live game loaded — its sound plays as the game produces it."
            },
            color = WaitingGrey,
            fontSize = 11.sp
        )

        if (loadedUrl != null) {
            Spacer(modifier = Modifier.height(10.dp))
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false   // let the game's audio start by itself
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        webViewClient = WebViewClient()
                        webChromeClient = WebChromeClient()
                        webView = this
                        loadUrl(loadedUrl ?: "about:blank")
                    }
                },
                onRelease = { it.destroy() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("live_game_webview")
            )
        }
    }
}

private fun normalizeUrl(raw: String): String? {
    val t = raw.trim()
    if (t.isEmpty()) return null
    val withScheme = if (t.startsWith("http://") || t.startsWith("https://")) t else "https://$t"
    return if (withScheme.contains(".")) withScheme else null
}

private fun loadUrl(context: Context): String =
    try { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_URL, "") ?: "" } catch (_: Exception) { "" }

private fun saveUrl(context: Context, url: String) {
    try { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_URL, url).apply() } catch (_: Exception) { }
}
