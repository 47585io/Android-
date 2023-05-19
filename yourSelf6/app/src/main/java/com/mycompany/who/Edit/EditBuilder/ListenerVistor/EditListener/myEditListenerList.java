package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;
import com.mycompany.who.Edit.Base.Share.*;


/* 
  myEditListenerList，myEditListener的子接口，可向上转型
 
  内部使用List存储一组EditListener，这个lis一定不为null
  
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	public static final byte Add_Bit = 1;
	//添加元素控制位
	
	private List<EditListener> lis;
	//监听器列表

	public myEditListenerList()
	{
		super();
		lis = Collection_Spiltor.EmptyList();
	}
	public myEditListenerList(String name,int flag)
	{
		super(name,flag);
		lis = Collection_Spiltor.EmptyList();
	}
	public myEditListenerList(EditListener li)
	{
		super(li);
		lis = Collection_Spiltor.EmptyList();
		if(li instanceof EditListenerList)
		{
			EditListener[] list = ((EditListenerList)li).toArray();
			for(EditListener l:list)
			{
				//在LinkedList或其它容器中，foreach循环比普通循环高效，并且兼容性强
				//普通遍历手段无非是对容器进行for(;i<size;++i)的get操作，这样遍历的坏处是每次都要让容器重新遍历寻找指定位置的元素
				//foreach循环本质是调用itrator对容器进行遍历，它将主动权交给容器本身，由容器遍历自己的所有元素，在遍历过程中将元素传递过来，这样就只要遍历一次
		
				//在ArrayList中，foreach循环比普通循环低效
				//因为ArrayList的get方法本身就很快，而用foreach每次获取元素，会先调用迭代器的hasNext，再获取下一个，调用了两次，反而慢了
				lis.add(l);
			}
		}
	}
	
	@Override
	public boolean add(EditListener p1)
	{
		//对于EditListenerList来说，flag还规定如何加入元素
		boolean is = Share.getbit(getFlag(),Add_Bit);
		int index = is ? 0:lis.size();
		lis.add(index,p1);
		return true;
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
		if(obj instanceof EditListenerList)
		{
			EditListener[] list = ((EditListenerList)obj).toArray();
			if(list.length==lis.size())
			{
			    for(int i = 0;i<lis.size();++i)
				{
				    if(!lis.get(i).equals(list[i])){
						return false;
					}
			    }
			}
		}
		return super.equals(obj);
	}

	@Override
	public void setFlag(int flag)
	{
		for(EditListener li:lis){
			li.setFlag(flag);
		}
		super.setFlag(flag);
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
		for(EditListener li:lis)
		{
			if(li.dispatchCallBack(Callback)){
				return true;
			}
		}
		return super.dispatchCallBack(Callback);
	}
	
}
