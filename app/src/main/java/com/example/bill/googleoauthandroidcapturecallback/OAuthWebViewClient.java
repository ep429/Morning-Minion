package com.example.bill.googleoauthandroidcapturecallback;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by bill on 3/22/15.
 */
public class OAuthWebViewClient extends WebViewClient {
    private MainActivity parent;

    public OAuthWebViewClient(MainActivity _parent) {
        super();
        parent = _parent;
    }

    @Override
    public boolean shouldOverrideUrlLoading (final WebView view, String url) {
        Log.d("MONGAN", url);
        if(url.contains("localhost") && url.contains("code") && !url.contains("accounts")) {
            Log.d("MONGAN", "LOCALHOST");

            Uri uri = Uri.parse(url);
            String code = uri.getQueryParameter("code");
            Log.d("MONGAN", "code=" + code);

            parent.onOAuthAuthorization(code);

            return true; // override and handle the URL ourselves
        } else {
            return false; // let the webview handle the request
        }
    }
}
