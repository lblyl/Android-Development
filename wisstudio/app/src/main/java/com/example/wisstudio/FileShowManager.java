package com.example.wisstudio;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.example.wisstudio.SortComparator;
public class FileShowManager extends AsyncTask<String, String, List<HashMap<String, Object>>>{
	private static final String DEG_TAG = "FileManager";
	public static final String SDCARD_HOME = Environment.getExternalStorageDirectory().toString();
	public static final String DEFAULT_PATH = SDCARD_HOME + "/wisstudio/download/";//默认路径
	public enum SORTTYPE {
		LETTER, DATE, CHILDNUMS
	}
	private Activity activity;
	private ListView fileList;
	private Dialog waitDialog;
	public FileShowManager(Activity activity, ListView fileListView){
		this.activity = activity;
		this.fileList = fileListView;
	}
	@Override
	protected void onPreExecute() {
		//初始化控件
		this.waitDialog = new AlertDialog.Builder(this.activity)
			.setMessage("正在加载中...")
			.create();
		this.waitDialog.setCancelable(false);
		this.waitDialog.show();
		super.onPreExecute();
	}
	@Override
	protected List<HashMap<String, Object>> doInBackground(String... params) {
		List<HashMap<String, Object>> fileLists = buildListForAdapter(params[0]);
		//默认以首字母排序
		this.sortByKey(fileLists,SORTTYPE.LETTER);
		return fileLists;
	}
	@Override
	protected void onPostExecute(List<HashMap<String, Object>> result) {
		//初始化数据
		this.initData(result);
		fileList.invalidate();
		this.waitDialog.dismiss();
		super.onPostExecute(result);
	}
	/**
	 * 初始化数据
	 * */
	public void initData(List<HashMap<String, Object>> fileLists){
		SimpleAdapter adapter = new SimpleAdapter(this.activity.getApplicationContext(), fileLists, 
				R.layout.filemanager_list_item, 
				new String[]{"name","path","childnums","date","img"}, 
				new int[]{R.id.filemanager_item_info_name,R.id.filemanager_item_filePath,
					R.id.filemanager_item_info_numsAndDate_nums,R.id.filemanager_item_info_numsAndDate_date,
					R.id.filemanager_item_icon});
		this.fileList.setAdapter(adapter);
	}
	/**
	 * 构建文件List的适配器
	 * @param	path		文件路径
	 * */
	public List<HashMap<String, Object>> buildListForAdapter(String path){
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String,Object>>();
		File rootFile = new File(path);
		if(!rootFile.exists()){
			//以默认位置打开
			rootFile = new File(DEFAULT_PATH);
			if(!rootFile.exists()){
				//默认位置不存在，进行创建
				rootFile.mkdirs();
			}
		}
		File[] currentPathFiles = rootFile.listFiles();
		Log.d(DEG_TAG, DEFAULT_PATH+":"+currentPathFiles);
		if(!path.equals(SDCARD_HOME)){
			HashMap<String, Object> root = new HashMap<String, Object>();
			root.put("name", "/");
			root.put("img", R.drawable.floder_home_back);
			root.put("path", SDCARD_HOME);
			root.put("childnums","返回根目录");
			root.put("date","");
			list.add(root);
			HashMap<String, Object> pmap = new HashMap<String, Object>();
			pmap.put("name", "..");
			pmap.put("img", R.drawable.floder_up_back);
			pmap.put("path", rootFile.getParent());
			pmap.put("childnums", "返回上一级");
			pmap.put("date", "");
			list.add(pmap);
		}
		if(currentPathFiles!=null){
			//如果存在子文件则进行遍历
			for (File file : currentPathFiles){
				//根据是否为文件夹选择不同的图标
				if(file.isDirectory()){
					HashMap<String, Object> item = new HashMap<String, Object>();
					item.put("img", R.drawable.floder_back);
					item.put("name", file.getName());
					item.put("path", file.getPath());
					item.put("childnums", "共有"+this.getDirectoryNums(file)+"项");
					item.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(file.lastModified()));
					list.add(item);
				}
			}
		}
		return list;
	}
	/**
	 * 统计文件夹中的文件夹数量
	 * @param	directory	文件夹
	 * */
	public int getDirectoryNums(File directory){
		if(directory.isDirectory()){
			File[] files = directory.listFiles();
			return this.getDirectoryNums(files);
		}
		return -1;
	}
	/**
	 * 统计文件夹中的文件夹数量
	 * @param	files		文件夹下的所有文件
	 * */
	public int getDirectoryNums(File[] files){
		int nums = 0;
		if(files!=null){
			for(File file : files){
				if(file.isDirectory()){
					nums++;
				}
			}
		}
		return nums;
	}
	/**
	 * List排序
	 * @param	lists		待排序的数组
	 * @param	sortType	排列种类
	 * */
	public List<HashMap<String, Object>> sortByKey(List<HashMap<String, Object>> lists, SORTTYPE sortType){
		Collections.sort(lists, new SortComparator(sortType));
		Log.d(DEG_TAG, "list.sort:["+lists+"]");
		return lists;
	}
}
