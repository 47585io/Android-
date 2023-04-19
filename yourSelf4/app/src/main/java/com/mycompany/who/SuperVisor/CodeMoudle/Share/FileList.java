package com.mycompany.who.SuperVisor.CodeMoudle.Share;

import android.content.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;
import java.io.*;
import java.util.*;

public class FileList
{

	private File nowDir;
	private List<File> sortFilelist;
	private String End_Path="/storage/emulated/0";

	public FileList(){
		nowDir=new File(End_Path);
		sortFilelist=new ArrayList<>();
		refreshDate();
	}

	public List<Icon> refresh(Context cont)
	{	  
		List<Icon> tmp  = new ArrayList<Icon>();
		tmp.add(new Icon(Share.getFileIcon("打开夹"), ".."));
		for (File c:sortFilelist)
		{
			tmp.add(new Icon(Share.getFileIcon(c), c.getName()));
		}
		return tmp;
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

	public void sortFile(List<File> list){
		List<File> tmp = new ArrayList<>();
		for(int i=0;i<list.size();i++){
			//目录排列在前面
			if(!list.get(i).isDirectory()){
				tmp.add(list.get(i));
				list.remove(i--);
			}
		}
		//按首字母排列
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
