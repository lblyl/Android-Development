package com.example.wisstudio;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FavAndHisActivity extends AppCompatActivity {
    private static final String DEG_TAG = "FavAndHisActivity";
    public static final int RESULT_FAV_HIS = 0;
    public static final int RESULT_DEFAULT = -1;
    //收藏历史按钮
    private Button favorites;
    private Button history;
    //收藏历史的内容
    private ListView favoritesContent;
    private ListView historyContent;
    //popupwindow弹窗
    private ItemLongClickedPopWindow itemLongClickedPopWindow;
    //书签历史管理
    private FavAndHisManager favAndHisManager;
    //Cursor
    private Cursor favAndHisCursor;
    //Adapter
    private ListAdapter favAndHisAdapter;
    //监听
    private ButtonClickedListener buttonClickedListener;
    private ListViewOnItemLongListener itemLongListener;
    private ListViewOnItemClickedListener itemClickedListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_favoritesandhistory);
        //初始化
        this.favorites = (Button) this.findViewById(R.id.favorites);
        this.history = (Button) this.findViewById(R.id.history);
        this.favoritesContent = (ListView) this.findViewById(R.id.favoritesAndHisotry_content_favorite);
        this.historyContent = (ListView) this.findViewById(R.id.favoritesAndHisotry_content_history);
        this.buttonClickedListener = new ButtonClickedListener();
        this.itemLongListener = new ListViewOnItemLongListener();
        this.itemClickedListener = new ListViewOnItemClickedListener();
        //添加监听
        this.favorites.setOnClickListener(this.buttonClickedListener);
        this.history.setOnClickListener(this.buttonClickedListener);
        this.favoritesContent.setOnItemLongClickListener(this.itemLongListener);
        this.historyContent.setOnItemLongClickListener(this.itemLongListener);
        this.favoritesContent.setOnItemClickListener(this.itemClickedListener);
        this.historyContent.setOnItemClickListener(this.itemClickedListener);
        //初始化数据
        this.initDataFavorites();
        this.initDataHistory();
        //添加默认返回值
        setResult(RESULT_DEFAULT);
    }
    /**
     * 初始化ListView中favourites的数据
     * */
    private void initDataFavorites() {
        //获取书签管理
        this.favAndHisManager = new FavAndHisManager(this);
        this.favAndHisCursor = this.favAndHisManager.getAllFavorites();
        this.favAndHisAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.list_item, this.favAndHisCursor,
                new String[]{"_id","name","url"},
                new int[]{R.id.item_id, R.id.item_name,R.id.item_url});
        this.favoritesContent.setAdapter(this.favAndHisAdapter);
    }
    /**
     * 初始化ListView中History的数据
     * */
    private void initDataHistory() {
        //获取书签管理
        this.favAndHisManager = new FavAndHisManager(this);
        this.favAndHisCursor = this.favAndHisManager.getAllHistories();
        this.favAndHisAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.list_item, this.favAndHisCursor,
                new String[]{"_id","name","url","date"},
                new int[]{R.id.item_id, R.id.item_name,R.id.item_url,R.id.item_date});
        this.historyContent.setAdapter(this.favAndHisAdapter);
    }
    /**
     * 长按单项事件
     * 覆盖如下方法
     * 1.	onItemLongClick
     * */
    private class ListViewOnItemLongListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(DEG_TAG, "long item cliced");
            if(parent.getId()==R.id.favoritesAndHisotry_content_favorite){
                itemLongClickedPopWindow = new ItemLongClickedPopWindow(FavAndHisActivity.this,
                        ItemLongClickedPopWindow.FAVORITES_ITEM_POPUPWINDOW, 200, 200);
                itemLongClickedPopWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.favandhis_activity));
                itemLongClickedPopWindow.showAsDropDown(view, view.getWidth()/2, -view.getHeight()/2);
                TextView modifyFavorite = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_modifyFavorites);
                TextView deleteFavorite = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_deleteFavorites);
                ItemClickedListener itemClickedListener = new ItemClickedListener(view);
                modifyFavorite.setOnClickListener(itemClickedListener);
                deleteFavorite.setOnClickListener(itemClickedListener);
            }else if(parent.getId()==R.id.favoritesAndHisotry_content_history){
                itemLongClickedPopWindow = new ItemLongClickedPopWindow(FavAndHisActivity.this,
                        ItemLongClickedPopWindow.HISTORY_ITEM_POPUPWINDOW, 200, 200);
                itemLongClickedPopWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.favandhis_activity));
                itemLongClickedPopWindow.showAsDropDown(view, view.getWidth()/2, -view.getHeight()/2);
                TextView deleteHistory = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_deleteHistory);
                TextView deleteAllHistories = (TextView) itemLongClickedPopWindow.getView(R.id.item_longclicked_deleteAllHistories);
                ItemClickedListener itemClickedListener = new ItemClickedListener(view);
                deleteHistory.setOnClickListener(itemClickedListener);
                deleteAllHistories.setOnClickListener(itemClickedListener);
            }
            return false;
        }
    }
    /**
     * ListView单击单项事件
     * 覆盖如下方法
     * 1.	onClick
     * */
    private class ListViewOnItemClickedListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if(arg0.getId()==R.id.favoritesAndHisotry_content_favorite){
                Intent intent = new Intent();
                intent.putExtra("url", ((TextView) arg1.findViewById(R.id.item_url)).getText().toString());
                setResult(0, intent);
                finish();
            }else if(arg0.getId()==R.id.favoritesAndHisotry_content_history){
                Intent intent = new Intent();
                intent.putExtra("url", ((TextView) arg1.findViewById(R.id.item_url)).getText().toString());
                setResult(0, intent);
                finish();
            }
        }
    }
    /**
     * OnClickListener自定义继承类
     * 覆盖如下方法
     * 1.	onClick
     * */
    private class ButtonClickedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.favorites){
                if(!favoritesContent.isShown()){
                    favoritesContent.setVisibility(View.VISIBLE);
                    historyContent.setVisibility(View.GONE);
                }
            }else if(v.getId()==R.id.history){
                if(!historyContent.isShown()){
                    favoritesContent.setVisibility(View.GONE);
                    historyContent.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    /**
     * popupwindow按钮事件处理类
     * 		用来获取其中的id、name、url这三个值
     * 覆盖如下方法：
     * 1.	onClick
     * */
    private class ItemClickedListener implements View.OnClickListener {
        private String item_id;
        private String item_name;
        private String item_url;
        public ItemClickedListener(View item){
            this.item_id = ((TextView) item.findViewById(R.id.item_id)).getText().toString();
            this.item_name = ((TextView) item.findViewById(R.id.item_name)).getText().toString();
            this.item_url = ((TextView) item.findViewById(R.id.item_url)).getText().toString();
        }
        @Override
        public void onClick(View view) {
            //取消弹窗
            itemLongClickedPopWindow.dismiss();
            if(view.getId()==R.id.item_longclicked_modifyFavorites){
                //弹出修改窗口
                LayoutInflater modifyFavoritesInflater = LayoutInflater.from(FavAndHisActivity.this);
                View modifyFavoritesView = modifyFavoritesInflater.inflate(R.layout.dialog_modify, null);
                final TextView item_name_input = (TextView) modifyFavoritesView.findViewById(R.id.dialog_name_input);
                final TextView item_url_input = (TextView) modifyFavoritesView.findViewById(R.id.dialog_url_input);
                item_name_input.setText(item_name);
                item_url_input.setText(item_url);
                new AlertDialog.Builder(FavAndHisActivity.this)
                        .setTitle("编辑书签")
                        .setView(modifyFavoritesView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEG_TAG, "id:"+item_id+",name:"+item_name+",url:"+item_url);
                                if(favAndHisManager.modifyFavorite(item_id, item_name_input.getText().toString(),
                                        item_url_input.getText().toString())){
                                    Toast.makeText(FavAndHisActivity.this, "修改成功", Gravity.BOTTOM).show();
                                    initDataFavorites();
                                    favoritesContent.invalidate();
                                }else{
                                    Toast.makeText(FavAndHisActivity.this, "修改失败", Gravity.BOTTOM).show();
                                }
                            }
                        }).setNegativeButton("取消", null)
                        .create()
                        .show();
            }else if(view.getId()==R.id.item_longclicked_deleteFavorites){
                new AlertDialog.Builder(FavAndHisActivity.this)
                        .setTitle("删除书签")
                        .setMessage("是否要删除\""+item_name+"\"这个书签？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {

                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(favAndHisManager.deleteFavorite(item_id)){
                                    //删除成功
                                    Toast.makeText(FavAndHisActivity.this, "删除成功", Gravity.BOTTOM).show();
                                    initDataFavorites();
                                    favoritesContent.invalidate();
                                }else{
                                    Toast.makeText(FavAndHisActivity.this, "删除失败", Gravity.BOTTOM).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }else if(view.getId()==R.id.item_longclicked_deleteHistory){
                new AlertDialog.Builder(FavAndHisActivity.this)
                        .setTitle("删除历史")
                        .setMessage("是否要删除\""+item_name+"\"这个历史？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(favAndHisManager.deleteHistory(item_id)){
                                    //删除成功
                                    Toast.makeText(FavAndHisActivity.this, "删除成功", Gravity.BOTTOM).show();
                                    initDataHistory();
                                    historyContent.invalidate();
                                }else{
                                    Toast.makeText(FavAndHisActivity.this, "删除失败", Gravity.BOTTOM).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }else if(view.getId()==R.id.item_longclicked_deleteAllHistories){
                new AlertDialog.Builder(FavAndHisActivity.this)
                        .setTitle("清空历史")
                        .setMessage("是否要清空历史？")
                        .setPositiveButton("清空", new DialogInterface.OnClickListener() {

                            @SuppressLint("WrongConstant")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(favAndHisManager.deleteAllHistory()){
                                    //删除成功
                                    Toast.makeText(FavAndHisActivity.this, "成功清空", Gravity.BOTTOM).show();
                                    initDataHistory();
                                    historyContent.invalidate();
                                }else{
                                    Toast.makeText(FavAndHisActivity.this, "清空失败", Gravity.BOTTOM).show();
                                }
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create()
                        .show();
            }
        }
    }
    @Override
    public void finish() {
        if (this.favAndHisCursor != null) {
            this.favAndHisCursor.close();
        }
        super.finish();
    }
    @Override
    protected void onDestroy() {
        if (this.favAndHisCursor != null) {
            this.favAndHisCursor.close();
        }
        super.onDestroy();
    }
}