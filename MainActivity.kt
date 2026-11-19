package gr.sch.twodimzevgol

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorPanel: LinearLayout
    private val homeUrl = "https://blogs.sch.gr/2dimzevgol/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFFFFFFFF.toInt()
        window.navigationBarColor = 0xFFFFFFFF.toInt()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = FrameLayout(this).apply { setBackgroundColor(0xFFFFFFFF.toInt()) }
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = SchoolWebViewClient()
            webChromeClient = WebChromeClient()
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            visibility = View.GONE
        }
        errorPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFFF7F8FA.toInt())
            visibility = View.GONE
            addView(TextView(this@MainActivity).apply {
                text = "Δεν ήταν δυνατή η σύνδεση στο διαδίκτυο."
                textSize = 18f
                setTextColor(0xFF202124.toInt())
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams(-1, -2))
            addView(TextView(this@MainActivity).apply {
                text = "Ελέγξτε τη σύνδεσή σας και πατήστε «Προσπάθεια ξανά»."
                textSize = 15f
                setTextColor(0xFF5F6368.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 20)
            }, LinearLayout.LayoutParams(-1, -2))
            addView(Button(this@MainActivity).apply {
                text = "Προσπάθεια ξανά"
                setOnClickListener { loadHome() }
            })
        }
        root.addView(webView, FrameLayout.LayoutParams(-1, -1))
        root.addView(progress, FrameLayout.LayoutParams(-1, dp(3)).apply { gravity = Gravity.TOP })
        root.addView(errorPanel, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)

        webView.setDownloadListener { url, _, _, _, _ ->
            openExternal(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
        if (savedInstanceState != null) webView.restoreState(savedInstanceState) else loadHome()
    }

    private fun loadHome() {
        errorPanel.visibility = View.GONE
        webView.visibility = View.VISIBLE
        webView.loadUrl(homeUrl)
    }

    private inner class SchoolWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url ?: return false
            val scheme = uri.scheme ?: return false
            val host = uri.host.orEmpty().lowercase()
            if (scheme == "mailto") { openExternal(Intent(Intent.ACTION_SENDTO, uri)); return true }
            if (scheme == "tel") { openExternal(Intent(Intent.ACTION_DIAL, uri)); return true }
            if (scheme != "http" && scheme != "https") return true
            if (host == "blogs.sch.gr" || host.endsWith(".blogs.sch.gr") || host == "sch.gr" || host.endsWith(".sch.gr")) return false
            openExternal(Intent(Intent.ACTION_VIEW, uri))
            return true
        }
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            progress.visibility = View.VISIBLE
            progress.progress = 5
            errorPanel.visibility = View.GONE
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            progress.progress = 100
            progress.visibility = View.GONE
            errorPanel.visibility = View.GONE
        }
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (request?.isForMainFrame == true) {
                progress.visibility = View.GONE
                webView.visibility = View.GONE
                errorPanel.visibility = View.VISIBLE
            }
        }
    }

    private fun openExternal(intent: Intent) {
        try { startActivity(intent) }
        catch (_: ActivityNotFoundException) { Toast.makeText(this, "Δεν βρέθηκε κατάλληλη εφαρμογή.", Toast.LENGTH_SHORT).show() }
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    @Deprecated("Deprecated in Android API; retained for compatibility with older devices")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        if (::webView.isInitialized) webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }
    override fun onDestroy() {
        if (::webView.isInitialized) { webView.stopLoading(); webView.destroy() }
        super.onDestroy()
    }
}
