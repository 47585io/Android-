package com.mycompany.who.SuperVisor.CodeMoudle.Share;

import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
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

	public List<Icon> refresh()
	{	  
		List<Icon> tmp  = new ArrayList<Icon>();
		tmp.add(new Icon3(Share.getFileIcon("打开夹"), ".."));
		for (File c:sortFilelist)
		{
			tmp.add(new Icon3(Share.getFileIcon(c), c.getName()));
		}
		return tmp;
	}
	public void refreshDate(){
		ArrayList<File> l= Array_Splitor.toList(nowDir.listFiles());
		sortFilelist.clear();
		sortFilelist.addAll(l);
		sortFilelist = sortFile(sortFilelist);
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

	public List<File> sortFile(List<File> list)
	{
		FileSort sorter = new FileSort();
		Array_Splitor.quick(list,sorter);
		//按首字母排列
		
		List<File> tmp = new ArrayList<>();
		for(File f : list){
			if(f.isDirectory())
				tmp.add(f);
		}
		for(File f : list){
			if(f.isFile())
				tmp.add(f);
		}
		return tmp;
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
