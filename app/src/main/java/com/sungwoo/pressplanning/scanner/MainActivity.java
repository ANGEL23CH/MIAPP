package com.sungwoo.pressplanning.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String PREFS = "press_planning_scanner";
    private static final String KEY_SERVER_URL = "server_url";
    private WebView webView;
    private TextView serverLabel;
    private SharedPreferences preferences;
    private String pendingBrowserCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        readDeepLink(getIntent());
        buildScreen();

        String saved = preferences.getString(KEY_SERVER_URL, "");
        if (saved == null || saved.trim().isEmpty()) {
            openProductionReport();
            Toast.makeText(this, "Prototipo local abierto. Configura Servidor para volver al módulo web.", Toast.LENGTH_LONG).show();
        } else {
            loadServer(saved);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readDeepLink(intent);
        if (pendingBrowserCallback != null) startScanner();
    }

    private void readDeepLink(Intent intent) {
        Uri data = intent == null ? null : intent.getData();
        if (data != null && "pressplanning".equalsIgnoreCase(data.getScheme()) && "scan".equalsIgnoreCase(data.getHost())) {
            pendingBrowserCallback = data.getQueryParameter("callback");
        }
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(7, 10, 14));

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(8), dp(6), dp(8), dp(6));
        toolbar.setBackgroundColor(Color.rgb(12, 17, 24));

        serverLabel = new TextView(this);
        serverLabel.setTextColor(Color.WHITE);
        serverLabel.setTextSize(10);
        serverLabel.setMaxLines(2);
        toolbar.addView(serverLabel, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button web = toolbarButton("Web");
        web.setOnClickListener(v -> openServerHome());
        toolbar.addView(web);

        Button report = toolbarButton("Reporte");
        report.setOnClickListener(v -> openProductionReport());
        toolbar.addView(report);

        Button server = toolbarButton("Servidor");
        server.setOnClickListener(v -> showServerDialog(false));
        toolbar.addView(server);

        Button scan = toolbarButton("Scan");
        scan.setOnClickListener(v -> startScanner());
        toolbar.addView(scan);
        root.addView(toolbar, new LinearLayout.LayoutParams(-1, -2));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " PressPlanningScanner/1.1");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new ScannerBridge(), "PressPlanningAndroid");
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);

        if (pendingBrowserCallback != null) webView.postDelayed(this::startScanner, 350);
    }

    private Button toolbarButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(10);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setPadding(dp(6), 0, dp(6), 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(44));
        params.setMargins(dp(3), 0, 0, 0);
        button.setLayoutParams(params);
        return button;
    }

    private void openServerHome() {
        String saved = preferences.getString(KEY_SERVER_URL, "");
        if (saved == null || saved.trim().isEmpty()) {
            showServerDialog(false);
        } else {
            loadServer(saved);
        }
    }

    private void openProductionReport() {
        webView.getSettings().setAllowFileAccess(false);
        serverLabel.setText("Reporte diario\nPrototipo local");
        webView.loadDataWithBaseURL(
                "https://pressplanning.local/report/",
                ProductionReportHtml.get(),
                "text/html",
                "UTF-8",
                null
        );
    }

    private void showServerDialog(boolean required) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setHint("http://192.168.1.50:8787/mobile");
        input.setText(preferences.getString(KEY_SERVER_URL, ""));
        input.setSelectAllOnFocus(true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Dirección del servidor")
                .setMessage("Escribe la IP de la computadora servidor y el puerto 8787.")
                .setView(input)
                .setPositiveButton("Guardar", null)
                .setNegativeButton(required ? "Ver reporte" : "Cancelar", (d, w) -> {
                    if (required) openProductionReport();
                })
                .create();
        dialog.setOnShowListener(v -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn -> {
            String normalized = normalizeServerUrl(input.getText().toString());
            if (normalized == null) {
                input.setError("Ejemplo: http://192.168.1.50:8787/mobile");
                return;
            }
            preferences.edit().putString(KEY_SERVER_URL, normalized).apply();
            dialog.dismiss();
            loadServer(normalized);
        }));
        dialog.setCanceledOnTouchOutside(!required);
        dialog.show();
    }

    private String normalizeServerUrl(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return null;
        if (!value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) value = "http://" + value;
        Uri uri = Uri.parse(value);
        if (uri.getHost() == null) return null;
        String path = uri.getPath();
        if (path == null || path.equals("/") || path.isEmpty()) {
            value = value.replaceAll("/+$", "") + "/mobile";
        }
        return value;
    }

    private void loadServer(String url) {
        webView.getSettings().setAllowFileAccess(false);
        serverLabel.setText(url);
        webView.loadUrl(url);
    }

    private void startScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Enfoca la etiqueta completa");
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) deliverScanResult(result.getContents());
            else notifyWebScanCancelled();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deliverScanResult(String code) {
        if (pendingBrowserCallback != null && !pendingBrowserCallback.trim().isEmpty()) {
            try {
                Uri callback = Uri.parse(pendingBrowserCallback).buildUpon().appendQueryParameter("scanResult", code).build();
                startActivity(new Intent(Intent.ACTION_VIEW, callback));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, "No se pudo regresar al navegador", Toast.LENGTH_LONG).show();
            } finally {
                pendingBrowserCallback = null;
            }
            return;
        }
        String js = "window.PressPlanningSetBarcode && window.PressPlanningSetBarcode(" + JSONObject.quote(code) + ");";
        webView.evaluateJavascript(js, null);
    }

    private void notifyWebScanCancelled() {
        webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('pressPlanningScanCancelled'));", null);
    }

    public class ScannerBridge {
        @JavascriptInterface
        public void scanBarcode() {
            runOnUiThread(MainActivity.this::startScanner);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
