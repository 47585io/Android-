package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;


/* 
  myEditListenerList，myEditListener的子接口，可向上转型
 
  内部使用List存储一组EditListener，这个lis一定不为null
  
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	private List<EditListener> lis;

	public myEditListenerList()
	{
		super();
		lis = Collection_Spiltor.EmptyList();
	}
	public myEditListenerList(String name,boolean e)
	{
		super(name,e);
		lis = Collection_Spiltor.EmptyList();
	}
	public myEditListenerList(EditListener li)
	{
		super(li);
		lis = Collection_Spiltor.EmptyList();
		if(li instanceof EditListenerList)
		{
			EditListener[] list = ((EditListenerList)li).toArray();
			for(EditListener l:list){
				lis.add(l);
			}
		}
	}
	
	@Override
	public int size()
	{
		return lis.size();
	}

	@Override
	public boolean contains(EditListener p1)
	{
		return lis.contains(p1);
	}

	@Override
	public boolean add(EditListener p1)
	{
		return lis.add(p1);
	}

	@Override
	public boolean remove(EditListener p1)
	{
		return lis.remove(p1);
	}

	@Override
	public void clear()
	{
		lis.clear();
	}

	@Override
	public EditListener[] toArray()
	{
		EditListener[] list = new EditListener[lis.size()];
		return lis.toArray(list);
	}
	
	@Override
	public EditListener findListenerByName(String name)
	{
		for(EditListener li:lis)
		{
			EditListener l = li.findListenerByName(name);
			if(l!=null){
				return l;
			}
		}
		return super.findListenerByName(name);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof EditListenerList){
			EditListener[] list = ((EditListenerList)obj).toArray();
			if(list.length==lis.size())
			{
			    for(int i = 0;i<lis.size();++i){
				    if(!lis.get(i).equals(list[i])){
						return false;
					}
			    }
			}
		}
		return super.equals(obj);
	}

	@Override
	public void setEnabled(boolean Enabled)
	{
		for(EditListener li:lis){
			li.setEnabled(Enabled);
		}
		super.setEnabled(Enabled);
	}
	
	@Override
	public void setEdit(EditText t)
	{
		for(EditListener li:lis){
			li.setEdit(t);
		}
		super.setEdit(t);
	}

	@Override
	public boolean dispatchCallBack(EditListener.RunLi Callback)
	{
		//遍历孑元素，并传递Callback，当有一个子元素返回true，直接返回true
		for(EditListener li:lis){
			if(Callback.run(li)){
				return true;
			}
		}
		return super.dispatchCallBack(Callback);
	}
	
}
