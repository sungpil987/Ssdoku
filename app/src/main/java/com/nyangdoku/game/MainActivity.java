package com.nyangdoku.game;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.webkit.*;
import android.view.View;
import android.widget.LinearLayout;
import androidx.webkit.WebViewAssetLoader;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.rewarded.*;
import com.google.android.ump.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private RewardedAd rewarded;
    private ConsentInformation consent;
    private boolean initialized, loading, showing;
    private long loadedAt;
    private static final String ORIGIN = "https://appassets.androidplatform.net";

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LinearLayout root = new LinearLayout(this);
        root.setBackgroundColor(Color.rgb(248,245,239));
        root.setOrientation(LinearLayout.VERTICAL);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}
            else v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        web=new WebView(this);
        root.addView(web,new LinearLayout.LayoutParams(-1,-1));setContentView(root);
        web.setBackgroundColor(Color.rgb(248,245,239));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient(){
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){
                WebResourceResponse response=loader.shouldInterceptRequest(request.getUrl());
                if(response!=null)return response;
                return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",java.util.Collections.emptyMap(),new java.io.ByteArrayInputStream(new byte[0]));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){return true;}
        });
        web.addJavascriptInterface(new Bridge(),"NyangAndroid");
        web.loadUrl(ORIGIN+"/assets/index.html");
        consent=UserMessagingPlatform.getConsentInformation(this);
        consent.requestConsentInfoUpdate(this,new ConsentRequestParameters.Builder().build(),()->UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->initializeAds()),error->initializeAds());
        initializeAds();
    }
    private void initializeAds(){if(initialized||!consent.canRequestAds())return;initialized=true;MobileAds.initialize(this,status->runOnUiThread(this::loadAd));}
    private void loadAd(){
        if(!initialized||!consent.canRequestAds()||loading||rewarded!=null||isFinishing())return;
        loading=true;
        RewardedAd.load(this,BuildConfig.REWARDED_AD_UNIT,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){
            @Override public void onAdLoaded(RewardedAd ad){loading=false;rewarded=ad;loadedAt=System.currentTimeMillis();}
            @Override public void onAdFailedToLoad(LoadAdError error){loading=false;rewarded=null;}
        });
    }
    private void status(String message){if(web!=null)web.evaluateJavascript("window.onNativeAdStatus&&window.onNativeAdStatus("+JSONObject.quote(message)+")",null);}
    private void showAd(){
        if(showing)return;
        if(!consent.canRequestAds()){status("광고를 사용할 수 없어요. 설정에서 광고 선택을 확인해주세요.");return;}
        if(rewarded!=null&&System.currentTimeMillis()-loadedAt>3500000)rewarded=null;
        if(rewarded==null){loadAd();status("광고를 준비 중이에요. 잠시 후 다시 눌러주세요.");return;}
        RewardedAd ad=rewarded;rewarded=null;showing=true;
        final boolean[] granted={false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){showing=false;loadAd();}
            @Override public void onAdFailedToShowFullScreenContent(AdError error){showing=false;status("광고를 열지 못했어요. 힌트는 차감되지 않아요.");loadAd();}
        });
        ad.show(this,reward->{if(!granted[0]){granted[0]=true;web.evaluateJavascript("window.onNativeReward&&window.onNativeReward()",null);}});
    }
    private class Bridge {
        @JavascriptInterface public void showRewarded(){runOnUiThread(()->showAd());}
        @JavascriptInterface public void privacyOptions(){runOnUiThread(()->{
            if(consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED){
                UserMessagingPlatform.showPrivacyOptionsForm(MainActivity.this,error->{rewarded=null;initializeAds();loadAd();if(error!=null)status("광고 선택 화면을 열지 못했어요. 다시 시도해주세요.");});
            } else status("진행 기록은 기기에 저장됩니다. 광고 제공 시 Google이 기기 식별자·광고 이용 정보를 처리할 수 있습니다. 현재 별도 동의 변경 화면은 필요하지 않습니다.");
        });}
        @JavascriptInterface public void exitApp(){runOnUiThread(()->finish());}
    }
    @Override public void onBackPressed(){web.evaluateJavascript("window.onNativeBack&&window.onNativeBack()",null);}
    @Override protected void onPause(){super.onPause();if(web!=null)web.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
    @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("NyangAndroid");web.destroy();web=null;}super.onDestroy();}
}
