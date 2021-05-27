package com.example.wisstudio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ImageDownloadManager extends AsyncTask<String, String, String>{
	private static final String DEG_TAG = "FileDownloadManager";
	private File file;
	private Context context;
	public ImageDownloadManager(Context context){
		this.context = context;
	}
	@SuppressLint("SdCardPath")
	@Override
	protected String doInBackground(String... params) {
		Log.d(DEG_TAG, "fileName:"+params[0]+",filePath:"+params[2]);
		if(params[2].startsWith("/sdcard/")){
			//如果是以/sdcard/为开头的，则应保存为sdcard中
			params[2] = Environment.getExternalStorageDirectory()+params[2].substring(8);
			Log.d(DEG_TAG, "saveImagePath:"+params[2]);
		}
		try{
			URL  url = new URL(params[1]);
	        HttpURLConnection conn  = (HttpURLConnection)url.openConnection();
	        conn.setDoInput(true);
	        conn.connect(); 
	        InputStream inputStream=conn.getInputStream();
	        Bitmap imgSave = BitmapFactory.decodeStream(inputStream); 
	        this.writeFile(params[0], params[2], imgSave);
	        inputStream.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	@Override
	protected void onPostExecute(String result) {
		Toast.makeText(context, "成功下载", Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
	}
	/**
	 * 将图片写入
	 * @param	fileName	图片名
	 * @param	dirPath		图片路径
	 * @param	imgSave		图片内容
	 * */
	public void writeFile(String fileName, String dirPath, Bitmap imgSave){
		try{
			File directory = new File(dirPath);
			if((directory.exists())&&(directory.isFile())){
				directory.delete();
			}else{
				directory.mkdirs();
			}
			this.file = new File(dirPath, fileName);
			if(this.file.exists()){
				this.file.delete();
			}
			this.file.createNewFile();
			FileOutputStream fo = new FileOutputStream(this.file);
			imgSave.compress(Bitmap.CompressFormat.PNG, 100, fo);
			fo.flush();
			fo.close();
		}catch(FileNotFoundException e1){
			Log.d(DEG_TAG, "文件未找到:"+e1.getMessage());
		}catch(IOException e2){
			Log.d(DEG_TAG, "文件创建错误:"+e2.getMessage());
			e2.printStackTrace();
		}
	}
}
