package com.mycompany.who.SuperVisor;

import android.content.*;
import android.view.*;
import android.widget.*;


import java.io.*;
import java.util.*;

import com.mycompany.who.R;
import com.mycompany.who.Share.Share;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

public class FileList
{

	private File nowDir;
	private ArrayList<File> sortFilelist;
	private String End_Path="/storage/emulated/0";

	public FileList(){
		nowDir=new File(End_Path);
		sortFilelist=new ArrayList<>();
		refreshDate();
	}

	public void refresh(Context cont, ListView filelist)
	{	  
		ArrayList<Icon> tmp  = new ArrayList<Icon>();
		tmp.add(new Icon(Share.getFileIcon("打开夹"), ".."));
		for (File c:sortFilelist)
		{
			tmp.add(new Icon(Share.getFileIcon(c), c.getName()));
		}
		WordAdpter adpter = new WordAdpter<>(cont, tmp,R.layout.Fileicon);	
		filelist.setAdapter(adpter);	
	}
	public void refreshDate(){
		List<File> l=  Arrays.asList( nowDir.listFiles());
		sortFilelist.clear();
		sortFilelist.addAll(l);
		Array_Splitor.quick(sortFilelist,new FileSort());
		sortFile(sortFilelist);
	}

	public File getFile(int index)
	{
		if (index == 0)
		{
			if(nowDir.exists()&&nowDir.canRead()&&!nowDir.getPath().equals(End_Path))
			    nowDir=nowDir.getParentFile();
			return nowDir;
		}
		else if (sortFilelist.get(index - 1).isDirectory()&&sortFilelist.get(index - 1).exists())
		{
			nowDir=sortFilelist.get(index - 1);
			return nowDir;
		}
        else if(sortFilelist.get(index - 1).exists())
		    return sortFilelist.get(index - 1);
		return null;
	}
	
	public int getSize(){
		return sortFilelist.size();
	}

	public void addAfile(String name){
		File f= new File( nowDir.getPath()+"/"+name);
		try
		{
			f.createNewFile();
			sortFilelist.add(f);
		}
		catch (IOException e)
		{}
	}
	public void delAfile(int index){
		sortFilelist.get(index-1).delete();
		sortFilelist.remove(index-1);
	}
	public void addAFolder(String name){
		File f=new File(nowDir.getPath()+"/"+name);
		f.mkdir();
		sortFilelist.add(0,f);
	}
	public void Rename(int index,String name){
		File f=new File(nowDir.getPath()+"/"+name);
		sortFilelist.get(index-1).renameTo(f);
		sortFilelist.set(index-1,f);
	}

	public void notifyDataSetChanged(ListView listView, int position,WordAdpter adpter) {
		/**第一个可见的位置**/
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		/**最后一个可见的位置**/
		int lastVisiblePosition = listView.getLastVisiblePosition();

		/**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
		if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
			/**获取指定位置view对象**/
			View view = listView.getChildAt(position - firstVisiblePosition);
			adpter.getView(position, view, listView);
			//用adpter中postion位置的元素刷新listview中的项
		}

	}

	public void sortFile(ArrayList<File> list){
		ArrayList<File> tmp = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			if(!list.get(i).isDirectory()){
				tmp.add(list.get(i));
				list.remove(i--);
			}
		}
		Array_Splitor.quick(tmp,new FileSort());
		list.addAll(tmp);
	}

	class FileSort implements Comparator<File>
	{

		@Override
		public int compare(File p1, File p2)
		{
			char c1=p1.getName().toLowerCase().charAt(0);
			char c2=p2.getName().toLowerCase().charAt(0);
			if(c1>c2||c1=='.')
				return 1;
			else if(c1==c2)
				return 0;
			else
				return -1;
		}
	}



}
