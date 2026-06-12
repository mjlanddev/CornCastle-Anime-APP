package com.mjland.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.SslErrorHandler
import android.net.http.SslError
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mjland.ui.icons.*
import com.mjland.viewmodel.PlayerViewModel
import com.mjland.model.AnimeMedia
import coil.compose.AsyncImage
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    animeTitle: String,
    animeId: Int,
    initialEpisode: Int? = null,
    onBackClick: () -> Unit,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val animeDetails by viewModel.animeDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentEp by viewModel.currentEpisode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val isAutoNextEnabled by viewModel.isAutoNextEnabled.collectAsState()

    var customFullScreenView by remember { mutableStateOf<android.view.View?>(null) }
    var customFullScreenCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }

    DisposableEffect(customFullScreenView) {
        val window = (context as? android.app.Activity)?.window
        val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, window.decorView) }
        if (customFullScreenView != null) {
            insetsController?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            window?.attributes?.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        } else {
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            window?.attributes?.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
        onDispose {
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            window?.attributes?.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            (context as? android.app.Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            if (customFullScreenView != null) {
                customFullScreenCallback?.onCustomViewHidden()
            }
        }
    }

    val backHandler = { 
        if (customFullScreenView != null) {
            customFullScreenCallback?.onCustomViewHidden()
            customFullScreenView = null
            customFullScreenCallback = null
            (context as? android.app.Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            (context as? android.app.Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            onBackClick()
        }
    }
    BackHandler(enabled = customFullScreenView != null, onBack = backHandler)

    
    LaunchedEffect(animeId) {
        viewModel.fetchDetails(animeId)
        viewModel.observeWatchHistory(context.applicationContext, animeId, initialEpisode)
    }

    
    val malId = remember(animeDetails) {
        animeDetails?.idMal ?: animeDetails?.id ?: animeId
    }

    
    val totalEpisodes = remember(animeDetails) {
        if (animeDetails == null) 1
        else {
            val count = animeDetails?.nextAiringEpisode?.episode?.let { it - 1 } ?: animeDetails?.episodes ?: 1
            if (count <= 0) 1 else count
        }
    }

    
    LaunchedEffect(totalEpisodes) {
        if (currentEp > totalEpisodes) {
            viewModel.selectEpisode(totalEpisodes)
        }
    }

    
    val embedUrl = "https://megaplay.buzz/stream/mal/$malId/$currentEp/$currentLang"

    
    val htmlContent = remember(embedUrl, currentEp) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover" />
            <style>
                body, html { margin:0; padding:0; background:#000; width:100%; height:100%; overflow:hidden; }
                iframe { border:none; width:100%; height:100%; position:absolute; top:0; left:0; padding:0; margin:0; }
            </style>
        </head>
        <body>
            <iframe id="player" src="$embedUrl" width="100%" height="100%" allowfullscreen="true" scrolling="no" allow="autoplay; encrypted-media"></iframe>
            <script>
                const PLAYER_EPISODE = $currentEp;
                window.addEventListener("message", function (event) {
                    let data = event.data;
                    if (typeof data === "string") {
                        try {
                            data = JSON.parse(data);
                        } catch (e) {
                            return;
                        }
                    }
                    data.playingEpisode = PLAYER_EPISODE;
                    if (window.AndroidBridge) {
                        window.AndroidBridge.postMessage(JSON.stringify(data));
                    }
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (customFullScreenView != null) {
            AndroidView(
                factory = { customFullScreenView!! },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = animeTitle,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (animeDetails?.format != "MOVIE") {
                                    Text(
                                        text = "Episode $currentEp • ${currentLang.uppercase()}",
                                        color = Color.White.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                } else {
                                    Text(
                                        text = "Full Movie • ${currentLang.uppercase()}",
                                        color = Color.White.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = backHandler) {
                                Icon(FluentuiSystemIconsChevronLeft, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF0C0B0F)
                        ),
                        windowInsets = WindowInsets.statusBars
                    )
                },
                containerColor = Color(0xFF060508)
            ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.06f)
                    )
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            clearCache(true)
                            settings.apply {
                                javaScriptEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                                javaScriptCanOpenWindowsAutomatically = false
                                supportMultipleWindows()
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                userAgentString = "Mozilla/5.0 (Linux; Android 13; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                            }
                            
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    view?.evaluateJavascript(
                                        """
                                        (function() {
                                            const PLAYER_EPISODE = $currentEp;
                                            window.addEventListener("message", function (event) {
                                                let data = event.data;
                                                if (typeof data === "string") {
                                                    try {
                                                        data = JSON.parse(data);
                                                    } catch (e) {
                                                        return;
                                                    }
                                                }
                                                data.playingEpisode = PLAYER_EPISODE;
                                                if (window.AndroidBridge) {
                                                    window.AndroidBridge.postMessage(JSON.stringify(data));
                                                }
                                            });
                                        })();
                                        """.trimIndent(),
                                        null
                                    )
                                }

                                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                                    handler?.proceed()
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val urlString = request?.url?.toString() ?: return false
                                    val isMainFrame = request?.isForMainFrame ?: false
                                    if (isMainFrame) {
                                        val isAllowed = urlString.contains("megaplay.buzz") ||
                                                urlString.contains("m3u8") ||
                                                urlString.contains("mp4") ||
                                                urlString.contains("stream") ||
                                                urlString.contains("blob:")
                                        return !isAllowed
                                    }
                                    return false
                                }
                            }
                            webChromeClient = object : WebChromeClient() {
                                override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                                    return false
                                }
                                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                                    super.onShowCustomView(view, callback)
                                    customFullScreenView = view
                                    customFullScreenCallback = callback
                                    (context as? android.app.Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                }
                                override fun onHideCustomView() {
                                    super.onHideCustomView()
                                    customFullScreenView = null
                                    customFullScreenCallback = null
                                    (context as? android.app.Activity)?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            }
                            setBackgroundColor(android.graphics.Color.BLACK)
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun postMessage(message: String) {
                                    try {
                                        val json = JSONObject(message)
                                        val eventType = json.optString("event", "")
                                        val type = json.optString("type", "")
                                        var progressPercent = json.optDouble("percent", -1.0).toFloat()
                                        var currentTime = json.optLong("time", -1L)
                                        var duration = json.optLong("duration", -1L)
                                        if (type == "watching-log" || json.has("currentTime")) {
                                            val logCurTime = json.optDouble("currentTime", -1.0)
                                            val logDuration = json.optDouble("duration", -1.0)
                                            if (logCurTime >= 0.0) currentTime = logCurTime.toLong()
                                            if (logDuration > 0.0) {
                                                duration = logDuration.toLong()
                                                progressPercent = (logCurTime.toFloat() / logDuration.toFloat()) * 100f
                                            }
                                        }
                                        val isComplete = (eventType == "complete" || progressPercent >= 95f)
                                        if (progressPercent >= 0f || isComplete) {
                                            val finalPercent = if (isComplete) 1.0f else (progressPercent / 100f).coerceIn(0f, 1f)
                                            val finalCurrentTime = if (isComplete) duration else currentTime
                                            val playingEp = json.optInt("playingEpisode", -1)
                                            viewModel.saveWatchProgress(
                                                context = ctx.applicationContext,
                                                progressPercent = finalPercent,
                                                currentTimeSeconds = finalCurrentTime,
                                                durationSeconds = duration,
                                                episodeOverride = if (playingEp != -1) playingEp else null
                                            )
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("WebViewPlayerBridge", "Bridge Parse Error: ${e.message}", e)
                                    }
                                }
                            }, "AndroidBridge")
                            loadDataWithBaseURL("https://megaplay.buzz", htmlContent, "text/html", "UTF-8", null)
                            tag = "frame_$embedUrl"
                        }
                    },
                    update = { webView ->
                        val currentTag = webView.tag as? String
                        val expectedTag = "frame_$embedUrl"
                        if (currentTag != expectedTag) {
                            webView.loadDataWithBaseURL("https://megaplay.buzz", htmlContent, "text/html", "UTF-8", null)
                            webView.tag = expectedTag
                        }
                    }
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (animeDetails?.format != "MOVIE" && totalEpisodes > 1) {
                    var epInputText by remember { mutableStateOf("") }
                    
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("#", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = epInputText,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) epInputText = it },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Go
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onGo = {
                                                val ep = epInputText.toIntOrNull()
                                                if (ep != null && ep in 1..totalEpisodes) {
                                                    viewModel.selectEpisode(ep)
                                                    epInputText = ""
                                                }
                                            }
                                        ),
                                        singleLine = true,
                                        decorationBox = { innerTextField ->
                                            if (epInputText.isEmpty()) {
                                                Text("Ep...", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }

                            
                            Row(
                                modifier = Modifier.wrapContentWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (currentEp < totalEpisodes) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                                        .clickable(enabled = currentEp < totalEpisodes) { viewModel.selectEpisode(currentEp + 1) }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (currentEp < totalEpisodes) Color.White else Color.White.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("NEXT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (currentEp < totalEpisodes) Color.White else Color.White.copy(alpha = 0.3f))
                                    }
                                }

                                
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (currentEp == totalEpisodes) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f))
                                        .border(
                                            width = 0.5.dp,
                                            color = if (currentEp == totalEpisodes) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { if (currentEp != totalEpisodes) viewModel.selectEpisode(totalEpisodes) }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "LATEST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentEp == totalEpisodes) MaterialTheme.colorScheme.primary else Color.White
                                    )
                                }

                                
                                val autoNextColor = if (isAutoNextEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f)
                                Box(
                                    modifier = Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAutoNextEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { viewModel.toggleAutoNext() }
                                        .border(0.5.dp, if (isAutoNextEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = if (isAutoNextEnabled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = "Auto Next",
                                            tint = autoNextColor,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text("AUTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = autoNextColor)
                                    }
                                }
                            }
                        }

                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val activeColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentLang == "sub") activeColor else Color.Transparent)
                                    .clickable { viewModel.setLanguage("sub") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SUBTITLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "sub") Color.Black else Color.White.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (currentLang == "dub") activeColor else Color.Transparent)
                                    .clickable { viewModel.setLanguage("dub") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("DUBBED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "dub") Color.Black else Color.White.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                            }
                        }
                    }
                } else if (animeDetails?.format == "MOVIE") {
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val activeColor = MaterialTheme.colorScheme.primary
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentLang == "sub") activeColor else Color.Transparent)
                                .clickable { viewModel.setLanguage("sub") },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("SUBTITLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "sub") Color.Black else Color.White.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentLang == "dub") activeColor else Color.Transparent)
                                .clickable { viewModel.setLanguage("dub") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("DUBBED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (currentLang == "dub") Color.Black else Color.White.copy(alpha = 0.5f), letterSpacing = 0.5.sp)
                        }
                    }
                }

                if (animeDetails?.format != "MOVIE" && totalEpisodes > 1) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "EPISODES",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.5.sp),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "$totalEpisodes",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                        
                        val columnsArray = 6
                        val chunkedEpisodes = (1..totalEpisodes).toList().chunked(columnsArray)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            chunkedEpisodes.forEach { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowItems.forEach { epNum ->
                                        val matchingHistory = watchHistory.find { it.episodeNumber == epNum }
                                        val localProgress = matchingHistory?.progressPercent ?: 0.0f
                                        val anilistCompletedProgress = animeDetails?.mediaListEntry?.progress ?: 0
                                        val finalProgress = if (epNum <= anilistCompletedProgress) {
                                            Math.max(localProgress, 1.0f)
                                        } else {
                                            localProgress
                                        }
                                        val isSelected = currentEp == epNum
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                            EpisodeGridItem(episodeNumber = epNum, progress = finalProgress, isSelected = isSelected, onClick = { viewModel.selectEpisode(epNum) })
                                        }
                                    }
                                    val rem = columnsArray - rowItems.size
                                    if (rem > 0) repeat(rem) { Spacer(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                } else if (animeDetails?.format == "MOVIE" || totalEpisodes <= 1) {
                    
                    Column(modifier = Modifier.fillMaxWidth()) {
                        
                        animeDetails?.relations?.edges?.let { relations ->
                            val movieRelations = relations.filter { it.node?.format == "MOVIE" || it.relationType == "PREQUEL" || it.relationType == "SEQUEL" }
                            if (movieRelations.isNotEmpty()) {
                                PlayerSectionRow(title = "Related Universe", items = movieRelations.mapNotNull { it.node })
                            }
                        }

                        animeDetails?.recommendations?.nodes?.mapNotNull { it.mediaRecommendation }?.let { recs ->
                            if (recs.isNotEmpty()) {
                                PlayerSectionRow(title = "More Like This", items = recs)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
}
}

@Composable
fun PlayerSectionRow(title: String, items: List<AnimeMedia>) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { anime ->
                Column(modifier = Modifier.width(110.dp)) {
                    AsyncImage(
                        model = anime.coverImage?.large ?: anime.coverImage?.extraLarge,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anime.title?.english ?: anime.title?.romaji ?: "Unknown",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeGridItem(
    episodeNumber: Int,
    progress: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isComplete = progress >= 0.85f
    val baseBgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        isComplete -> Color.White.copy(alpha = 0.02f)
        else -> Color.White.copy(alpha = 0.03f)
    }
    val outlineColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(baseBgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = outlineColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$episodeNumber",
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isComplete -> Color.White.copy(alpha = 0.3f)
                    else -> Color.White.copy(alpha = 0.8f)
                }
            )
        }

        
        if (progress > 0f && !isComplete) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 0.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
