package com.editor.text2.builder.listenerInfo;
import com.editor.text2.builder.listenerInfo.listener.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import java.util.*;

public class CodeEditListenerInfo implements EditListenerInfo
{

	private Map<Integer,EditListener> mlistenerS;

	public CodeEditListenerInfo()
	{
		mlistenerS = new HashMap<>();
		//我们希望它们一开始就是EditListenerList
		mlistenerS.put(FinderIndex,new myEditListenerList());
		mlistenerS.put(InsertorIndex,new myEditListenerList());
		mlistenerS.put(CompletorIndex,new myEditListenerList());
		mlistenerS.put(CanvaserIndex,new myEditListenerList());
	}

	@Override
	public boolean addAListener(EditListener li) 
	{
		if(li==null)
			return false;

		if(li instanceof EditDrawerListener){
			return addListenerTo(li,DrawerIndex);
		}
		else if(li instanceof EditFormatorListener){
			return addListenerTo(li,FormatorIndex);
		}
		else if(li instanceof EditCompletorListener){
			return addListenerTo(li,CompletorIndex);
		}
		else if(li instanceof EditCanvaserListener){
			return addListenerTo(li,CanvaserIndex);
		}
		else if(li instanceof EditRunnarListener){
			return addListenerTo(li,RunnarIndex);
		}
		return false;
	}

	@Override
	public boolean delAListener(EditListener li) 
	{	
		if(li==null)
			return false;

		for(EditListener l:mlistenerS.values())
		{
			if(l!=null)
			{
				if(l instanceof EditListenerList)
				{
					if(((EditListenerList)l).remove(li)){
						//它处于一个EditListenerList中
						return true;
					}
				}
				else if(l.equals(li))
				{
					//它是某个EditListener
					int key = 0;
					if(mlistenerS.remove(key)!=null){
						return true;
					}
				}
			}	
		}
		return false;
	}

	@Override
	public EditListener findAListener(Object name)
	{	
		for(EditListener li:mlistenerS.values())
		{
			EditListener l = li==null ? null: li.findListenerByName(name);
			if(l!=null){
				return l;
			}
		}
		return null;
	}

	@Override
	public boolean addListenerTo(EditListener li, int toIndex)
	{
		EditListener l = findAListener(toIndex);
		if(l!=null)
		{
			if(l instanceof EditListenerList){
				//目标位置已有一个EditListenerList，就直接加入
				((EditListenerList)l).add(li);
			}
			else if(l instanceof EditListener)
			{
				//目标位置已有一个EditListener，就将它们合并
				EditListenerList list = new myEditListenerList();
				list.add(l);
				list.add(li);
				mlistenerS.remove(toIndex);
				mlistenerS.put(toIndex,list);
			}
		}
		else{
			//否则直接设置
			mlistenerS.remove(toIndex);
			mlistenerS.put(toIndex,li);
		}
		return true;
	}

	@Override
	public boolean delListenerFrom(int fromIndex){		
		return mlistenerS.remove(fromIndex)!=null;
	}
	@Override
	public EditListener findAListener(int fromIndex){
		return mlistenerS.get(fromIndex);
	}
	@Override
	public int size(){
		return mlistenerS.size();
	}
	@Override
	public void clear(){
		mlistenerS.clear();
	}
	@Override
	public boolean contrans(EditListener li){
		return mlistenerS.containsValue(li);
	}

}
	
