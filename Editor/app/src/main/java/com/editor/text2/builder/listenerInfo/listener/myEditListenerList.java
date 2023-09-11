package com.editor.text2.builder.listenerInfo.listener;

import java.util.*;
import android.widget.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;


/* 
  myEditListenerList，myEditListener的子接口，可向上转型
 
  内部使用List存储一组EditListener，这个lis一定不为null
  
  注意，dispatchCallback和findListenerByName是反序遍历的
  
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	private List<EditListener> lis;

	public myEditListenerList(){
		super();
		lis = new ArrayList<>();
	}
	public myEditListenerList(Object name,int flag){
		super(name,flag);
		lis = new ArrayList<>();
	}
	public myEditListenerList(EditListener li)
	{
		super(li);
		lis = new ArrayList<>();
		if(li instanceof EditListenerList)
		{
			EditListener[] list = ((EditListenerList)li).toArray();
			for(int i=0;i<list.length;++i){
				lis.add(list[i]);
			}
		}
	}
	
	@Override
	public boolean add(EditListener p1){
		return lis.add(p1);	
	}
	@Override
	public boolean remove(EditListener p1){
		return lis.remove(p1);
	}
	@Override
	public void clear(){
		lis.clear();
	}
	@Override
	public int size(){
		return lis.size();
	}
	@Override
	public boolean contains(EditListener p1){
		return lis.contains(p1);
	}
	@Override
	public EditListener[] toArray(){
		EditListener[] list = new EditListener[lis.size()];
		return lis.toArray(list);
	}
	
	@Override
	public EditListener findListenerByName(Object name)
	{
		//遍历子元素，让它们各自去寻找，找到了直接返回
		for(int i=size()-1;i>=0;--i)
		{
			EditListener li = lis.get(i).findListenerByName(name);
			if(li!=null){
				return li;
			}
		}
		return super.findListenerByName(name);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof EditListenerList)
		{
			EditListener[] list = ((EditListenerList)obj).toArray();
			if(list.length==lis.size())
			{
			    for(int i = 0;i<list.length;++i)
				{
					//equals的顺序并不重要，无论如何都对结果没有影响
				    if(!lis.get(i).equals(list[i])){
						return false;
					}
			    }
			}
		}
		return super.equals(obj);
	}

	@Override
	public boolean dispatchCallBack(RunLi Callback)
	{
		if(!Enabled()){
			return false;
		}
		
		//遍历孑元素，并传递Callback，当有一个子元素返回true，直接返回true
		for(int i=size()-1;i>=0;--i)
		{
			EditListener li = lis.get(i);
			if(li!=null && li.dispatchCallBack(Callback)){
				return true;
			}
		}
		return super.dispatchCallBack(Callback);
	}
	
}
