package com.example.wisstudio;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileActivity extends Activity{
	private static final String DEG_TAG = "FileManager";
	public static final int RESULT_FILEMANAGER = 1;
	//文件列表
	private ListView fileList;
	//确定取消按钮
	private TextView sure;
	private TextView cancel;
	//新建目录按钮
	private Button createNewFloder;
	//监听器
	private FileManagerOnItemListener fileManagerOnItemListener;
	private FileManagerOnClickListener fileManagerOnClickListener;
	private FileManagerOnItemLongListener fileManagerOnItemLongListener;
	private FileListPopWindowMenu fileListPopWindowMenu;
	//长按文件列表单项弹出菜单
	private ItemLongClickedPopWindow fileListItemLongClickedPopWindow;
	//文件管理线程类
	private FileShowManager fileManager;
	//当前的路径
	private String currentPath;
	/**
	 * 文件管理Actvity
	 * @param	savedInstanceState	父Activity信息
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filemanager);
		//初始化控件
		this.fileList = (ListView) this.findViewById(R.id.fileManager_list);
		this.sure = (TextView) this.findViewById(R.id.fileManager_toolbar_sure);
		this.cancel = (TextView) this.findViewById(R.id.fileManager_toolbar_cancel);
		this.createNewFloder = (Button) this.findViewById(R.id.fileManager_title_newDirectory);
		this.fileManager = new FileShowManager(this, this.fileList);
		this.fileManagerOnItemListener = new FileManagerOnItemListener();
		this.fileManagerOnClickListener = new FileManagerOnClickListener();
		this.fileManagerOnItemLongListener = new FileManagerOnItemLongListener();
		//注册监听
		this.fileList.setOnItemClickListener(this.fileManagerOnItemListener);
		this.fileList.setOnItemLongClickListener(this.fileManagerOnItemLongListener);
		this.sure.setOnClickListener(this.fileManagerOnClickListener);
		this.cancel.setOnClickListener(this.fileManagerOnClickListener);
		this.createNewFloder.setOnClickListener(this.fileManagerOnClickListener);
		//启动文件查询线程
		currentPath = getIntent().getStringExtra("savePath");
		this.fileManager.execute(currentPath);
	}
	/**
	 * OnItemClickListener自定义继承类
	 * 覆盖如下方法：
	 * 1.	onItemClick
	 * */
	private class FileManagerOnItemListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			TextView path = (TextView) view.findViewById(R.id.filemanager_item_filePath);
			currentPath = path.getText().toString();
			Log.d(DEG_TAG, "path:"+currentPath);
			fileManager = new FileShowManager(FileActivity.this, fileList);
			fileManager.execute(currentPath);
		}
	}
	/**
	 * OnItemLongClickListener自定义继承类
	 * 覆盖如下方法：
	 * 1.	onItemLongClick
	 * */
	private class FileManagerOnItemLongListener implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			TextView date = (TextView) view.findViewById(R.id.filemanager_item_info_numsAndDate_date);
			if(date.getText().toString().equals("")){
				return false;
			}
			fileListItemLongClickedPopWindow = new ItemLongClickedPopWindow(FileActivity.this, ItemLongClickedPopWindow.FILEMANAGER_ITEM_POPUPWINDOW, 200, 200);
			fileListItemLongClickedPopWindow.showAsDropDown(view, view.getWidth()/2, -view.getHeight()/2);
			TextView deleteFloder = (TextView) fileListItemLongClickedPopWindow.getView(R.id.item_longclicked_deleteFloder);
			TextView newNameForFloder = (TextView) fileListItemLongClickedPopWindow.getView(R.id.item_longclicked_newNameForFloder);
			fileListPopWindowMenu = new FileListPopWindowMenu(view);
			deleteFloder.setOnClickListener(fileListPopWindowMenu);
			newNameForFloder.setOnClickListener(fileListPopWindowMenu);
			return true;
		}
	}
	/**
	 * OnClickListener自定义继承类
	 * */
	private class FileListPopWindowMenu implements OnClickListener{
		private View beLongClickedView;
		public FileListPopWindowMenu(View beLongClickedView){
			this.beLongClickedView = beLongClickedView;
		}
		@Override
		public void onClick(View v) {
			fileListItemLongClickedPopWindow.dismiss();
			TextView floderPath = (TextView) beLongClickedView.findViewById(R.id.filemanager_item_filePath);
			TextView oldFloderName = (TextView) beLongClickedView.findViewById(R.id.filemanager_item_info_name);
			final String floderPathStr = floderPath.getText().toString();
			if(v.getId()==R.id.item_longclicked_deleteFloder){
				//删除文件夹的实现
				new AlertDialog.Builder(FileActivity.this)
					.setTitle("删除目录")
					.setMessage("是否删除\""+floderPathStr+"\"目录")
					.setPositiveButton("删除", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							File deleteDirectory = new File(floderPathStr);
							if(deleteDirectory.exists()){
								deleteDirectory.delete();
								fileManager = new FileShowManager(FileActivity.this, fileList);
								fileManager.execute(currentPath);
							}
						}
					})
					.setNegativeButton("取消", null)
					.create()
					.show();
			}else if(v.getId()==R.id.item_longclicked_newNameForFloder){
				//重命名文件夹的实现
				View newNameForFloderView = LayoutInflater.from(FileActivity.this).inflate(R.layout.dialog_newnameforfloder, null);
				final TextView floderName = (TextView) newNameForFloderView.findViewById(R.id.dialog_newNameForFloder_floderName);
				floderName.setText(oldFloderName.getText().toString());
				new AlertDialog.Builder(FileActivity.this)
				.setTitle("重命名")
				.setView(newNameForFloderView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newFloderName = floderName.getText().toString();
						File newNameFloder = new File(floderPathStr);
						newNameFloder.renameTo(new File(currentPath + "/" + newFloderName));
						fileManager = new FileShowManager(FileActivity.this, fileList);
						fileManager.execute(currentPath);
					}
				})
				.setNegativeButton("取消", null)
				.create()
				.show();
			}
		}
	}
	/**
	 * OnClickListener自定义继承类
	 * 覆盖如下方法
	 * 1.	onClick
	 * */
	private class FileManagerOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.fileManager_toolbar_sure){
				//确定操作，返回确定的url
				Intent intentExtraUrl = new Intent();
				intentExtraUrl.putExtra("savePath", currentPath);
				setResult(RESULT_FILEMANAGER, intentExtraUrl);
				finish();
			}else if(v.getId()==R.id.fileManager_toolbar_cancel){
				//取消操作，不更改url
				setResult(MainActivity.REQUEST_DEFAULT);
				finish();
			}else if(v.getId()==R.id.fileManager_title_newDirectory){
				//新建目录操作
				View createNewFolderView = LayoutInflater.from(FileActivity.this).inflate(R.layout.dialog_createnewfloder, null);
				final EditText newFloderName = (EditText) createNewFolderView.findViewById(R.id.dialog_createNewFloder_floderName);
				new AlertDialog.Builder(FileActivity.this)
					.setTitle("创建新目录")
					.setView(createNewFolderView)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String createNewpath = currentPath + "/" + newFloderName.getText().toString();
							Log.d(DEG_TAG, "createNewpath:"+createNewpath);
							File file = new File(createNewpath);
							if(!file.exists()){
								file.mkdir();
								fileManager = new FileShowManager(FileActivity.this, fileList);
								fileManager.execute(currentPath);
							}else{
								//目录已存在，提示无法创建
								Toast.makeText(FileActivity.this, "目录已存在", Toast.LENGTH_SHORT).show();
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
	public void onBackPressed() {
		setResult(MainActivity.REQUEST_DEFAULT);
		super.onBackPressed();
	}
}

