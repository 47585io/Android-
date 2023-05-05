package com.mycompany.who.Edit.ListenerVistor.EditListener;
import java.util.*;
import android.widget.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.BaseEditListener.*;


/* 
  EditListenerList，EditListener的子类，可向上转型

  内部使用synchronizedList存储EditListener，这个lis一定不为null
  
  管理一组的EditListener
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	private List<EditListener> lis;

	public myEditListenerList(){
		lis = Collections.synchronizedList(new ArrayList<>());
	}

	public void setList(List<EditListener> lis){
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
	
	/*
	 protected void dispatchArgs(int flag,Object... args)
	 {
		 for(EditListener li:lis){
			 li.LetMeDo(flag,args);
		 }
	 }
	 
	*/
	
}
