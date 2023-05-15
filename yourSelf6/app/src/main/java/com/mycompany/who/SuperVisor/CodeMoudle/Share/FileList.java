package com.mycompany.who.SuperVisor.CodeMoudle.Share;

import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import java.io.*;
import java.util.*;


/*
  每一次的增删文件都会往文件系统中增加一个文件，而文件系统中的文件是乱序的，那每一次增删文件都要重获取所有文件？

  当然不需要，因为我们使用sortFilelist来顺序存储当前目录所有文件对象，当增删一个文件，就同时对sortFilelist进行修改
  
  这样，无论如何sortFilelist中的文件都保持顺序同步，而获取一个文件，只是获取sortFilelist中的文件对象，不需要重新获取全部文件
  
  当然，特殊情况是需要访问上级或下级目录，此时才重新获取并排序全部文件
 
*/
public class FileList
{

	private File nowDir;
	private List<File> sortFilelist;
	private FileSort sorter = new FileSort();
	private FileChangeLisrener mFileListener;
	public static String Path_Spilt="/";
	public static String End_Path="/storage/emulated/0";

	public FileList()
	{
		nowDir=new File(End_Path);
		sortFilelist=new ArrayList<>();
		refreshDate();
	}

	public void setFileChangeListener(FileChangeLisrener li){
		mFileListener = li;
	}
	
	public int getSize(){
		return sortFilelist.size();
	}
	
	/* 获取指定下标的文件，如果是一个目录会进入目录 */
	public File getFile(int index)
	{
		File f = null;
		if (index == -1){
			//如果index为-1，则获取并进入父目录
			f = nowDir=nowDir.getParentFile();
			refreshDate();
		}
		else{
			f = sortFilelist.get(index);
			if(f.isDirectory()){
				//如果是一个目录，则获取并进入目录
				nowDir = f;
				refreshDate();
			}
		}
		return f;
	}
	
	public void addAfile(String name)
	{
		File f= new File(nowDir.getPath()+Path_Spilt+name);
		try{
			f.createNewFile();
			sortFilelist.add(findAIndex(f),f);
		}
		catch (IOException e){}
	}
	public void delAfile(int index)
	{
		sortFilelist.get(index).delete();
		sortFilelist.remove(index);
	}
	public void addAFolder(String name)
	{
		File f=new File(nowDir.getPath()+Path_Spilt+name);
		f.mkdir();
		sortFilelist.add(findAIndex(f),f);
	}
	public void Rename(int index,String name)
	{
		File f=new File(nowDir.getPath()+Path_Spilt+name);
		sortFilelist.get(index).renameTo(f);
	}
	
	public void refreshDate()
	{
		sortFilelist = Array_Splitor.toList(nowDir.listFiles());
		sortFilelist = sortFile(sortFilelist);
	}
	
	public List<File> sortFile(List<File> list)
	{
		Array_Splitor.quickSort(list,sorter);
		//按首字母排列
		List<File> tmp = new ArrayList<>();
		//将目录放在前面
		for(File f : list){
			if(f.isDirectory()){
				tmp.add(f);
			}
		}
		for(File f : list){
			if(f.isFile()){
				tmp.add(f);
			}
		}
		return tmp;
	}
	
	public int findAIndex(File f)
	{
		int i = 0;
		for(;i<sortFilelist.size();++i)
		{
			File f2 = sortFilelist.get(i);
			if(f.isDirectory() && f2.isFile()){
				//f是一个目录，但是已经没有目录了，返回
				break;
			}
			else if(f.isFile() && f2.isDirectory()){
				//f是一个文件，需要向后走到文件才能开始比较
				continue;
			}	
			else{
				//f与f2要么都是文件，要么都是目录
				int flag = sorter.compare(f,f2);
				if(flag==0 || flag==1){
					//如果f与f2相等，返回
					//或者f已经大于当前位置的文件，但是没有与它相等的文件，说明有文件空缺，先返回让f插入这里
					break;
			    }
			}
		}
		return i;
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
	
	public static interface FileChangeLisrener{
		
		public void Refresh(List<File> files)
		
		public void addAFile(List<File> files,File f)
		
		public void delAFile(List<File> files,File f)
		
		public void Rename(File f,File f2)
		
	}

}
