package binhpt567.wv;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
	
	private WebView webView;
	private EditText urlInput;
	private String authToken;
	private String cookies;
	private String currentUrl;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		urlInput = findViewById(R.id.urlInput);
		webView = findViewById(R.id.webView);
		Button GolikeButton = findViewById(R.id.GolikeButton);
		Button HelpButton = findViewById(R.id.HelpButton);
		Button showDialogButton = findViewById(R.id.showDialogButton);
		
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		
		webView.setWebViewClient(new MyWebViewClient());
		
		urlInput.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE ||
			(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
				handleUrlOrTextInput();
				return true;
			}
			return false;
		});
		
		// Set up buttons for loading sample URL
		GolikeButton.setOnClickListener(v -> webView.loadUrl("https://app.golike.net/login"));
		HelpButton.setOnClickListener(v -> webView.loadUrl("https://www.youtube.com/@osteup"));
		
		// Dialog
		showDialogButton.setOnClickListener(v -> showAuthDialog());
	}
	
	private void handleUrlOrTextInput() {
		String input = urlInput.getText().toString().trim();
		if (TextUtils.isEmpty(input)) {
			Toast.makeText(this, "Search Empty", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (input.startsWith("http://") || input.startsWith("https://")) {
			webView.loadUrl(input);
			} else {
			String searchUrl = "https://www.google.com/search?q=" + Uri.encode(input);
			webView.loadUrl(searchUrl);
		}
	}
	
	private class MyWebViewClient extends WebViewClient {
		
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			Map<String, String> requestHeaders = request.getRequestHeaders();
			
			if (requestHeaders.containsKey("Authorization")) {
				authToken = requestHeaders.get("Authorization");
				runOnUiThread(() -> saveAuthToken(authToken));
			}
			
			return super.shouldInterceptRequest(view, request);
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			currentUrl = url;
			cookies = CookieManager.getInstance().getCookie(url);
			
			if (cookies != null) {
				saveCookies(cookies);
			}
		}
	}
	
	private void saveAuthToken(String authToken) {
		SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("auth", authToken);
		editor.apply();
	}
	
	private void saveCookies(String cookies) {
		SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("cookies", cookies);
		editor.apply();
	}
	
	private void showAuthDialog() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View dialogView = inflater.inflate(R.layout.dialog_view, null);
		
		TextView currentUrlTextView = dialogView.findViewById(R.id.currentUrlTextView);
		TextView authTokenTextView = dialogView.findViewById(R.id.authTokenTextView);
		TextView cookiesTextView = dialogView.findViewById(R.id.cookiesTextView);
		
		currentUrlTextView.setText("Current URL: " + currentUrl);
		authTokenTextView.setText("Auth: \n" + authToken);
		cookiesTextView.setText("Cookie: \n" + cookies);
		
		new AlertDialog.Builder(this)
		.setTitle("Authorization & Cookies")
		.setView(dialogView)
		.setPositiveButton("Copy Auth Token", (dialog, which) -> {
			copyToClipboard(authToken);
			showToast("Auth Token copied to clipboard");
		})
		.setNegativeButton("Copy Cookies", (dialog, which) -> {
			copyToClipboard(cookies);
			showToast("Cookies copied to clipboard");
		})
		.setNeutralButton("Close", null)
		.show();
	}
	
	private void copyToClipboard(String text) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Copied", text);
		clipboard.setPrimaryClip(clip);
	}
	
	private void showToast(String message) {
		Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
	}
}