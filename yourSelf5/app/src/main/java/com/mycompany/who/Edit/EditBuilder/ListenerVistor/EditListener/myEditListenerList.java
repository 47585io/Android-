package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import java.util.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.*;


/* 
  myEditListenerList，myEditListener的子接口，可向上转型
 
  内部使用synchronizedList存储EditListener，这个lis一定不为null
  
  管理一组的EditListener
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	private List<EditListener> lis;

	public myEditListenerList(){
		lis = Collections.synchronizedList(new ArrayList<>());
	}
	
	public void setList(EditListener li)
	{
		this.lis.clear();
		if(lis!=null){
			if(li instanceof EditListenerList){
				lis.addAll(((EditListenerList)li).getList());
			}
			else if(li instanceof EditListener)
				lis.add(li);
		}
	}
	public void setList(List<EditListener> lis)
	{
		this.lis.clear();
		if(lis!=null)
		    this.lis.addAll(lis);
	}
	public List<EditListener> getList(){
		return lis;
	}

	@Override
	public boolean equals(Object obj)
	{
		for(EditListener li:lis){
			if(li.equals(obj))
				return true;
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
	public void setName(String name)
	{
		for(EditListener li:lis){
			li.setName(name);
		}
		super.setName(name);
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
		//遍历孑元素，并传递Callback，当有EditListener返回true，直接返回true
		for(EditListener li:lis){
			if(Callback.run(li)){
				return true;
			}
		}
		return super.dispatchCallBack(Callback);
	}
	
}
