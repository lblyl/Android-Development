package com.example.wisstudio;

import java.util.Comparator;
import java.util.HashMap;
import com.example.wisstudio.FileShowManager;

public class SortComparator implements Comparator<HashMap<String, Object>> {
	private FileShowManager.SORTTYPE sortType;
	public SortComparator(FileShowManager.SORTTYPE sortType){
		this.sortType = sortType;
	}
	@Override
	public int compare(HashMap<String, Object> lhs, HashMap<String, Object> rhs) {
		switch(sortType){
		case LETTER:
			//字母排序
			return String.valueOf(lhs.get("name")).compareTo(String.valueOf(rhs.get("name")));
		case DATE:
			//日期排序
			return String.valueOf(lhs.get("date")).compareTo(String.valueOf(rhs.get("date")));
		case CHILDNUMS:
			//含文件夹数排序
			return String.valueOf(lhs.get("childnums")).compareTo(String.valueOf(rhs.get("childnums")));
		}
		return 0;
	}
}
