package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import java.util.*;
import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/* 
  myEditListenerList，myEditListener的子接口，可向上转型
 
  内部使用List存储一组EditListener，这个lis一定不为null
  
  注意，dispatchCallback和findListenerByName是反序遍历的
  
*/
public class myEditListenerList extends myEditListener implements EditListenerList
{
	
	public static final int AddToHead_Mask = 2;
	//在之后添加元素时，期待元素添加到列表头部
	public static final int AddToTail_Mask = ~AddToHead_Mask;
	//在之后添加元素时，期待元素添加到列表尾部
	
	private List<EditListener> lis;
	//监听器列表

	public myEditListenerList()
	{
		super();
		lis = new ArrayList<>();
		setFlag(getFlag() & AddToTail_Mask);
	}
	public myEditListenerList(Object name,int flag)
	{
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
			for(int i=0;i<list.length;++i)
			{
				//在LinkedList或其它容器中，foreach循环比普通循环高效，并且兼容性强
				//普通遍历手段无非是对容器进行for(;i<size;++i)的get操作，这样遍历的坏处是每次都要让容器重新遍历寻找指定位置的元素
				//foreach循环本质是调用itrator对容器进行遍历，它将主动权交给容器本身，由容器遍历自己的所有元素，在遍历过程中将元素传递过来，这样就只要遍历一次
		
				//在ArrayList中，foreach循环比普通循环低效
				//因为ArrayList的get方法本身就很快，而用foreach每次获取元素，会先调用迭代器的hasNext，再获取下一个，调用了两次，反而慢了
				//但如果元素比较少，差距应该不会太大，目前，在ArrayList中存储10000000个元素时，使用foreach比普通遍历慢50毫秒
				lis.add(list[i]);
			}
		}
	}
	
	@Override
	public boolean add(EditListener p1)
	{
		int index = Order() ? 0:lis.size();
		lis.add(index,p1);
		p1.setParent(this);
		//在添加元素时设置parent
		return true;
	}
	@Override
	public boolean remove(EditListener p1)
	{
		if(lis.remove(p1))
		{
			//在移除元素时删除parent
			p1.setParent(null);
			return true;
		}
		return false;
	}
	@Override
	public void clear()
	{
		for(int i=size()-1;i>=0;--i){
			remove(lis.get(i));
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
	public EditListener[] toArray()
	{
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

	public boolean Order()
	{
		return (getFlag() & AddToTail_Mask) == AddToTail_Mask;
	}
	
	@Override
	public boolean dispatchCallBack(EditListener.RunLi Callback)
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
