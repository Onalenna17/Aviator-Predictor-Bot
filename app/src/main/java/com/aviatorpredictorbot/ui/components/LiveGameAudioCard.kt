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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.aviatorpredictorbot.ui.theme.InkDark
import com.aviatorpredictorbot.ui.theme.LabelGrey
import com.aviatorpredictorbot.ui.theme.NeonCrimson
import com.aviatorpredictorbot.ui.theme.StatusGreen
import com.aviatorpredictorbot.ui.theme.WaitingGrey

private const val PREFS = "live_game_prefs"
private const val KEY_URL = "game_url"

/**
 * Live Game Audio Panel:
 * Combines native real-time crash sound synthesis (engine propeller, auto-cashout ding,
 * and automatic crash "flew away" sounds playing continuously as rounds progress)
 * with an optional embedded game launch view.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveGameAudioCard(
    soundMasterEnabled: Boolean = true,
    soundCrashEnabled: Boolean = true,
    onToggleMasterSound: ((Boolean) -> Unit)? = null,
    onPreviewCrash: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var urlInput by remember { mutableStateOf(loadUrl(context)) }
    var loadedUrl by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showBrowserPanel by remember { mutableStateOf(false) }

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
        // Top Header: Title & Automatic Live Audio Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                RedFieldLabel("LIVE AVIATOR AUDIO ENGINE")
                Text(
                    text = "Automated crash, engine & cashout telemetry audio",
                    fontSize = 11.sp,
                    color = LabelGrey
                )
            }

            if (onToggleMasterSound != null) {
                Switch(
                    checked = soundMasterEnabled,
                    onCheckedChange = onToggleMasterSound,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ButtonRed
                    ),
                    modifier = Modifier.testTag("live_audio_quick_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Automatic Crash Audio Status Banner
        val isSoundActive = soundMasterEnabled && soundCrashEnabled
        val bannerBg = if (isSoundActive) Color(0xFFF0FDF4) else Color(0xFFFFF1F2)
        val bannerBorder = if (isSoundActive) StatusGreen else NeonCrimson
        val bannerTextColor = if (isSoundActive) StatusGreen else NeonCrimson

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(bannerBg)
                .border(1.dp, bannerBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isSoundActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = null,
                    tint = bannerTextColor,
                    modifier = Modifier.size(20.dp)
                )

                Column {
                    Text(
                        text = if (isSoundActive) "AUTOMATIC CRASH AUDIO: ACTIVE" else "CRASH AUDIO: MUTED",
                        color = bannerTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = if (isSoundActive) "Crash sound plays automatically when round crashes" else "Turn on Master & Crash audio in Settings to listen",
                        color = LabelGrey,
                        fontSize = 10.sp
                    )
                }
            }

            if (onPreviewCrash != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ButtonRed)
                        .clickable { onPreviewCrash() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("preview_crash_sound_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Crash Sound",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "TEST CRASH",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Settings Link & Browser toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onOpenSettings != null) {
                Text(
                    text = "⚙️ Configure Engine Pitch, Sirens & Volume",
                    color = ButtonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onOpenSettings() }
                        .testTag("audio_card_open_settings")
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = if (showBrowserPanel) "Hide Web Panel ▲" else "Embed Web Operator ▼",
                color = LabelGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { showBrowserPanel = !showBrowserPanel }
                    .testTag("toggle_browser_panel")
            )
        }

        // Optional embedded browser panel if user wants to load operator page
        if (showBrowserPanel) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEF2))
            Spacer(modifier = Modifier.height(10.dp))

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
                        .height(44.dp)
                        .testTag("live_game_load_button")
                ) {
                    Text(if (loadedUrl == null) "LOAD LIVE GAME" else "RELOAD", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
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
                            .height(44.dp)
                            .testTag("live_game_stop_button")
                    ) {
                        Text("STOP GAME", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }

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
                            settings.mediaPlaybackRequiresUserGesture = false
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
