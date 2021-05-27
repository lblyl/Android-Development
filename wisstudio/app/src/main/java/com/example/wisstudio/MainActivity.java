package com.example.wisstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //LOG标签
    private static final String DEG_TAG = "webBrowser";
    /**
     * 请求码（默认，代表不做处理）
     * @param	REQUEST_DEFAULT
     * {@value #REQUEST_DEFAULT}
     * */
    public static final int REQUEST_DEFAULT = -1;
    /**
     * 请求码（历史或者书签）
     * @param	REQUEST_OPEN_FAV_OR_HIS
     * {@value #REQUEST_OPEN_FAV_OR_HIS}
     * */
    public static final int REQUEST_OPEN_FAV_OR_HIS = 0;
    /**
     * 请求码（保存图片路径）
     * @param	REQUEST_SAVE_IMAGE_PATH
     * {@value #REQUEST_SAVE_IMAGE_PATH}
     * */
    public static final int REQUEST_SAVE_IMAGE_PATH = 1;
    //webView相关
    private WebView webView;
    private WebSettings settings;
    private WebViewClient client;
    private WebChromeClient chromeClient;
    //地址栏编辑框
    private EditText webUrlStr;
    //搜索栏按钮
    private Button webUrlGoto;
    private Button webUrlFavorites;
    //接收网址
    private String url = "";
    //URL网页标题
    private String title = "";
    //工具栏按钮组
    private Button preButton;
    private Button nextButton;
    private Button toolsButton;
    private Button windowsButton;
    private Button homeButton;
    //工具栏按钮显示界面
    private ToolsPopWindow toolsPopWindow;
    //事件监听器
    private ButtonClickedListener buttonClickedListener;
    private WebViewLongClickedListener webViewLongClickedListener;
    private PopWindowMenu popWindowMenu;
    //书签管理
    private FavAndHisManager favAndHisManager;
    //进度条
    private ProgressBar webProgressBar;
    //弹出式菜单
    private ItemLongClickedPopWindow itemLongClickedPopWindow;
    //保存图片弹出对话框
    private Dialog saveImageToChoosePath;
    //保存图片按钮
    private TextView choosePath;
    private TextView imgSaveName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        //组件初始化
        this.chromeClient = new OwnerChromeClient();
        this.webUrlGoto = (Button) this.findViewById(R.id.web_url_goto);
        this.webUrlStr=(EditText) this.findViewById(R.id.web_url_input);
        this.webUrlFavorites = (Button) this.findViewById(R.id.web_url_show_favorite);
        this.webView=(WebView) this.findViewById(R.id.webview);
        this.preButton = (Button) this.findViewById(R.id.pre_button);
        this.nextButton = (Button) this.findViewById(R.id.next_button);
        this.toolsButton = (Button) this.findViewById(R.id.tools_button);
        this.windowsButton = (Button) this.findViewById(R.id.window_button);
        this.homeButton = (Button) this.findViewById(R.id.home_button);
        this.favAndHisManager = new FavAndHisManager(getApplicationContext());
        this.toolsPopWindow = new ToolsPopWindow(this, this.getWindowManager().getDefaultDisplay().getWidth()-30,
                this.getWindowManager().getDefaultDisplay().getHeight()/3);
        this.settings=this.webView.getSettings();
        this.client=new OwnerWebView();
        this.buttonClickedListener = new ButtonClickedListener();
        this.chromeClient = new OwnerChromeClient();
        this.webProgressBar = (ProgressBar) this.findViewById(R.id.web_progress_bar);
        this.webViewLongClickedListener = new WebViewLongClickedListener();
        ////设置参数
        this.settings.setDefaultTextEncodingName("UTF-8");
        this.webView.setWebViewClient(this.client);
        this.webView.setWebChromeClient(this.chromeClient);
        this.webView.loadUrl("https://www.baidu.com/");
        this.webView.setWebChromeClient(this.chromeClient);
        this.webProgressBar.setVisibility(View.GONE);
        //添加监听
        //this.webView.setOnTouchListener(this.webViewTouchListener);
        this.webUrlGoto.setOnClickListener(this.buttonClickedListener);
        this.webUrlFavorites.setOnClickListener(this.buttonClickedListener);
        this.webUrlStr.setOnClickListener(this.buttonClickedListener);
        this.preButton.setOnClickListener(buttonClickedListener);
        this.nextButton.setOnClickListener(buttonClickedListener);
        this.toolsButton.setOnClickListener(buttonClickedListener);
        this.windowsButton.setOnClickListener(buttonClickedListener);
        this.homeButton.setOnClickListener(buttonClickedListener);
        this.webView.setOnLongClickListener(this.webViewLongClickedListener);
        this.webView.setOnLongClickListener(this.webViewLongClickedListener);
    }
    public boolean onCreateOptionMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    private class ButtonClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            if(v.getId()==R.id.web_url_goto){
                Log.d(DEG_TAG,"url:"+url);
                webView.loadUrl(url);
            }else if(v.getId()==R.id.pre_button){
                if(webView.canGoBack()){
                    //后退
                    webView.goBack();
                }
            }else if(v.getId()==R.id.next_button){
                if(webView.canGoForward()){
                    //前进
                    webView.goForward();
                }
            }else if(v.getId()==R.id.tools_button){
                //展现工具视图
                LayoutInflater toolsInflater = LayoutInflater.from(getApplicationContext());
                View toolsView = toolsInflater.inflate(R.layout.tabactivity_tools, null);
                toolsPopWindow.showAtLocation(toolsView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                Button refresh = (Button) toolsPopWindow.getView(R.id.tools_normal_refresh);
                Button favorites = (Button) toolsPopWindow.getView(R.id.tools_normal_favorites);
                refresh.setOnClickListener(this);
                favorites.setOnClickListener(this);
            }else if(v.getId()==R.id.web_url_show_favorite){
                //添加书签
                favAndHisManager.addFavorite(title,url);
                favAndHisManager.getAllFavorites();
            }else if(v.getId()==R.id.tools_normal_refresh){
                //刷新
                if(!(url.equals("")&&url.equals("http://"))){
                    webView.loadUrl(url);
                }
            }else if(v.getId()==R.id.tools_normal_favorites){
                toolsPopWindow.dismiss();
                startActivity(new Intent(MainActivity.this,FavAndHisActivity.class));
            }else if(v.getId()==R.id.dialog_savePath_enter){
                Intent imgSavePath = new Intent(MainActivity.this,FileActivity.class);
                imgSavePath.putExtra("savePath", choosePath.getText().toString());
                startActivityForResult(imgSavePath,MainActivity.REQUEST_SAVE_IMAGE_PATH);
            }else if(v.getId()==R.id.window_button){
            }else if(v.getId()==R.id.home_button){
                webView.loadUrl("http://www.baidu.com");
            }
        }
    }
    /**
     * 设置工具栏回溯历史是否可用
     * */
    private void changeStatueOfWebToolsButton(){
        if(webView.canGoBack()){
            //设置可使用状态
            preButton.setEnabled(true);
        }else{
            //设置禁止状态
            preButton.setEnabled(false);
        }
        if(webView.canGoForward()){
            //设置可使用状态
            nextButton.setEnabled(true);
        }else{
            //设置禁止状态
            nextButton.setEnabled(false);
        }
    }
    private class OwnerChromeClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            MainActivity.this.setProgress(newProgress*100);
            if(newProgress==100){
                webProgressBar.setVisibility(View.GONE);
            }else{
                webProgressBar.setVisibility(View.VISIBLE);
                webProgressBar.setProgress(newProgress);
            }
        }
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            MainActivity.this.title = title;
            webUrlStr.setText(title);
        }
    }
    private class OwnerWebView extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.getSettings().setJavaScriptEnabled(true);
            webUrlStr.setText(url);
            changeStatueOfWebToolsButton();
            //添加历史
            String date = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(new Date()).toString();
            favAndHisManager.addHistory(title,url,Long.parseLong(date));
        }
    }
    /**
     * OnClickListener自定义继承类
     * 用来解决菜单功能处理问题
     * */
    private class PopWindowMenu implements View.OnClickListener {
        private int type;
        private String value;
        public PopWindowMenu(int type, String value){
            this.type = type;
            this.value = value;
            Log.d(DEG_TAG, "type:"+type+",value:"+value);
        }
        @Override
        public void onClick(View v) {
            itemLongClickedPopWindow.dismiss();
            if(v.getId()==R.id.item_longclicked_viewImage){
                //图片菜单-查看图片
                new RequestShowImageOnline(MainActivity.this).execute(value);
            }else if(v.getId()==R.id.item_longclicked_saveImage){
                //图片菜单-保存图片
                View dialogSaveImg = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_saveimg, null);
                choosePath = (TextView) dialogSaveImg.findViewById(R.id.dialog_savePath_enter);
                imgSaveName = (TextView) dialogSaveImg.findViewById(R.id.dialog_fileName_input);
                final String imgName = value.substring(value.lastIndexOf("/") + 1);
                imgSaveName.setText(imgName);
                choosePath.setOnClickListener(buttonClickedListener);
                saveImageToChoosePath = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("选择保存路径")
                        .setView(dialogSaveImg)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEG_TAG, "fileName:"+imgName+",filePath:"+choosePath.getText().toString());
                                new ImageDownloadManager(MainActivity.this).execute(imgName, value, choosePath.getText().toString());
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                saveImageToChoosePath.show();
            }else if(v.getId()==R.id.item_longclicked_viewImageAttributes){
                //图片菜单-查看属性
            }
        }
    }
    private class WebViewLongClickedListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            WebView.HitTestResult result = ((WebView) v).getHitTestResult();
            if (null == result)
                return false;
            int type = result.getType();
            if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                return false;
            if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                return true;
            }
            switch (type) {
                case WebView.HitTestResult.PHONE_TYPE:
                    // 处理拨号
                    break;
                case WebView.HitTestResult.EMAIL_TYPE:
                    // 处理Email
                    break;
                case WebView.HitTestResult.GEO_TYPE:
                    // TODO
                    break;
                case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                    // 超链接
                    Log.d(DEG_TAG, "超链接");
                    break;
                case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                case WebView.HitTestResult.IMAGE_TYPE:
                    // 处理长按图片的菜单项
                    Log.d(DEG_TAG, "图片");
                    itemLongClickedPopWindow = new ItemLongClickedPopWindow(MainActivity.this, ItemLongClickedPopWindow.IMAGE_VIEW_POPUPWINDOW, 300, 300);
                    itemLongClickedPopWindow.showAtLocation(v,Gravity.CENTER,0, 0);
                    TextView viewImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImage);
                    TextView saveImage = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_saveImage);
                    TextView viewImageAttributes = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_viewImageAttributes);
                    popWindowMenu = new PopWindowMenu(result.getType(), result.getExtra());
                    viewImage.setOnClickListener(popWindowMenu);
                    saveImage.setOnClickListener(popWindowMenu);
                    viewImageAttributes.setOnClickListener(popWindowMenu);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case REQUEST_DEFAULT:
                //不做任何处理
                break;
            case FavAndHisActivity.RESULT_FAV_HIS:
                webView.loadUrl(data.getStringExtra("url"));
                break;
            case FileActivity.RESULT_FILEMANAGER:
                choosePath.setText(data.getStringExtra("savePath"));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

