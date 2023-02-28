package com.mycompany.who.SuperVisor;

import android.content.*;
import android.text.method.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.View.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.Share.*;

public class EditList
{
	private ArrayList<Page> EditPages;
	private int nowIndex=-1;
	private int noRepeatId=-1;
	private boolean CanWrite=true;

	public EditList()
	{
		EditPages=new ArrayList<>();
	}

	public boolean addAEdit(Page EditPage, ViewGroup ForEdit)
	{
		//添加一个命名的编辑器

		int index = contrans(EditPage.getPath());
		if (index != -1)
		{	
			return false;
			//如果是同一个文件，不重复加入
		}

		EditPages.add(EditPage);
		nowIndex=EditPages.size() - 1;
		EditPage.setId(++noRepeatId);	
		ForEdit.addView(EditPage);
		return true;
	}
	public void tabAEdit(int index, ViewGroup ForEdit)
	{
		if (EditPages.size() == 0 || index > EditPages.size() || index < 0)
		{
			return;
		}
		//异常情况下，什么也不做
		//否则把编辑器切换
		ForEdit.removeAllViews();
		ForEdit.addView(EditPages.get(index));
		nowIndex=index;
	}
	public void delAEdit(int index, ViewGroup ForEdit)
	{
		if (EditPages.size() == 0 || index > EditPages.size() || index < 0)
		{
			return;
		}
		EditPages.remove(index);


		if (EditPages.size() == 0)
		{
			ForEdit.removeAllViews();
			nowIndex=-1;
		}
		//如果在删除后没有编辑器了，清空上次的编辑器
		else if (index == 0)
		    tabAEdit(index, ForEdit);
		//否则删除了头编辑器，默认应切换至下一个，但在remove时，1会被放至0的位置，所以现在的0就是下一个
		else if (index == nowIndex)
		    tabAEdit(index - 1, ForEdit);
		//另外的，删除编辑器正是当前的并且大于0，默认应切换至上一个
		else if (index < nowIndex)
		{
			nowIndex-=1;
			//如果小于当前的编辑器，nowIndex向前偏移
		}
	}

	public void delAll(ViewGroup ForEdit){
		EditPages.clear();
		nowIndex=-1;
		ForEdit.removeAllViews();
	}

	public int contrans(String path)
	{
		int index;
		for (index = 0;index < EditPages.size();index++)
		{
			if (EditPages.get(index).getPath().equals(path))
			{
			    return index;
			}
		}
		return -1;
	}

	public void refresh(Context cont, Spinner EditNames)
	{
		//用Editpages的内容刷新EditNames
		ArrayList<Icon> tmp  = new ArrayList<Icon>();
		for (Page c:EditPages)
		{
			tmp.add(new Icon(com.mycompany.who.Share. Share.getFileIcon(c.getName()), c.getName()));
		}
		WordAdpter adpter = new WordAdpter<>(cont, tmp,R.layout.FileIcon2);	
		EditNames.setAdapter(adpter);
	}
	public void refreshText(Context cont, Spinner EditNames)
	{
		ArrayList<String> tmp = new ArrayList<>();
		for (Page c:EditPages)
			tmp.add(c.getName());
		ArrayAdapter<String> adpter = new ArrayAdapter<>(cont,android.R.layout.simple_list_item_1,tmp);
		EditNames.setAdapter(adpter);
	}

	public void lockEdit()
	{
		//如果禁止输入，清除编译器的输入监听器，否则还原
		CodeEdit Edit;
		for (Page c:EditPages)
		{
			Edit=c.getEdit();
			if (Edit == null)
				continue;
			if (CanWrite)
			    Edit.lockSelf(false);
			else
				Edit.lockSelf(true);
		}
	}

	public int getNowIndex()
	{
		//获取当前选中的编辑器下标
		return nowIndex;
	}
	public int getNoRepeatId()
	{
		//获取当前的编辑器id
		return noRepeatId;
	}
	public int fromIdToIndex(int id){
		int i=0;
		for(Page page:EditPages){
		    if(page.getId()==id)
				return i;
			i++;
		}
		return -1;
	}
	public CodeEdit getEdit(int id)
	{
		//搜索正确id的那个，而不是下标
		for (Page page:EditPages)
		{
			if (page.getId() == id)
				return page.getEdit();
		}
		return null;
	}
	public CodeEdit getEditAt(int index)
	{
		//获取下标的那个
		if (index < 0)
			return null;
		return EditPages.get(index).getEdit();
	}
	public Page getPageAt(int index)
	{
		if (index < 0)
			return null;
		return EditPages.get(index);
	}
	public int getPageCount(){
		return EditPages.size();
	}

	public boolean getCanWrite()
	{
		return CanWrite;
	}
	public void setCanWrite(boolean can)
	{
		CanWrite=can;
		lockEdit();
	}
	
	public void save(){
		for(final Page page:EditPages){
			if(page.getEdit()!=null){
				myLog log= new myLog(page.getPath());
				log.e(page.getEdit().getText(),true);
				log.close();
			}
		}
	}
	public void save(final int index){
		try{
		Page page = EditPages.get(index);
		if(page.getEdit()!=null){
		    myLog log= new myLog(page.getPath());
		    log.e(page.getEdit().getText(),true);
		    log.close();
		}
		}catch(Exception e){
			
		}
	}

}
