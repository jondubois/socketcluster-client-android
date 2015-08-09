package socketcluster.io.socketclusterandroidclient;

import android.content.Context;
import android.app.Activity;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fangjian.WebViewJavascriptBridge;

import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lihanli on 1/06/2015.
 */
public class SocketCluster {
    private String host;
    private String port;
    private Boolean isHttps;
    private WebView webView;
    private WebViewJavascriptBridge bridge;
    private final String TAG = "SCClient";

    public SocketCluster(String host, String port, boolean isHttps, Activity context) {
        this.host = host;
        this.port = port;
        this.isHttps = isHttps;
        this.setupSCWebClient(context);
        this.registerHandles();
    }

    class UserServerHandler implements WebViewJavascriptBridge.WVJBHandler{
        @Override
        public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
            Log.d("test","Received message from javascript: "+ data);
            if (null !=jsCallback) {
                jsCallback.callback("Java said:Right back atcha");
            }
        }
    }
    private String readHtml(String remoteUrl) {
        String out = "";
        BufferedReader in = null;
        try {
            URL url = new URL(remoteUrl);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                out += str;
            }
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out;
    }

    private void setupSCWebClient(Activity context) {
        webView = new WebView(context);
        bridge = new WebViewJavascriptBridge(context, webView, new UserServerHandler());
        webView.setWebViewClient(
                new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
        InputStream is=context.getResources().openRawResource(R.raw.user_client);
        String user_client_html=WebViewJavascriptBridge.convertStreamToString(is);
        webView.loadDataWithBaseURL("file:///android_asset/", user_client_html, "text/html", "utf-8", "");

        InputStream is2 = context.getResources().openRawResource(R.raw.webviewjavascriptbridge);
        String script= WebViewJavascriptBridge.convertStreamToString(is2);
        webView.loadUrl("javascript:" + script);




    }
    private void registerHandles(){
        bridge.registerHandler("handler1", new WebViewJavascriptBridge.WVJBHandler() {
            @Override
            public void handle(String data, WebViewJavascriptBridge.WVJBResponseCallback jsCallback) {
                Log.d("test", "handler1 got:" + data);
                if (null != jsCallback) {
                    jsCallback.callback("handler1 answer");
                }
                bridge.callHandler("showAlert", "42");
            }
        });
    }

    private void callJavaScript(WebView view, String methodName, Object...params){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("javascript:try{");
        stringBuilder.append(methodName);
        stringBuilder.append("(");
        String separator = "";
        for (Object param : params) {
            stringBuilder.append(separator);
            separator = ",";
            if(param instanceof String){
                stringBuilder.append("'");
            }
            stringBuilder.append(param);
            if(param instanceof String) {
                stringBuilder.append("'");
            }

        }
        stringBuilder.append(")}catch(error){console.error(error.message);}");
        final String call = stringBuilder.toString();
        Log.i(TAG, "callJavaScript: call="+call);

        view.loadUrl(call);
    }



    public void connect() {
        Map map = new HashMap();
        map.put("hostname", host);
        map.put("secure", isHttps ? "true" : "false");
        map.put("port", port);
        String jsonText = JSONValue.toJSONString(map);
        bridge.callHandler("connectHandler", jsonText);
    }
    public void disconnect() {


    }
}