package com.editor.text2.builder.listenerInfo;
import com.editor.text2.builder.listenerInfo.listener.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;
import java.util.*;
import com.editor.text.base.*;

public class CodeEditListenerInfo implements EditListenerInfo
{

	private EditListener[] mListeners;
	private int mListenerCount;

	public CodeEditListenerInfo(){
		mListeners = EmptyArray.emptyArray(EditListener.class);
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

		for(int i=0;i<mListeners.length;++i)
		{
			EditListener l = mListeners[i];
			if(l!=null)
			{
				if(l.equals(li)){
					//它是某个EditListener
					mListeners[i] = null;
					return true;
				}
				if(l instanceof EditListenerList && ((EditListenerList)l).remove(li)){
					//它处于一个EditListenerList中
					return true;
				}
			}	
		}
		return false;
	}

	@Override
	public EditListener findAListener(Object name)
	{	
		for(EditListener li:mListeners)
		{
			li = li==null ? null: li.findListenerByName(name);
			if(li!=null){
				return li;
			}
		}
		return null;
	}

	@Override
	public boolean addListenerTo(EditListener li, int toIndex)
	{
		checkIndex(toIndex);
		EditListener l = findAListener(toIndex);
		if(l!=null)
		{
			if(l instanceof EditListenerList){
				//目标位置已有一个EditListenerList，就直接加入
				((EditListenerList)l).add(li);
			}
			else if(l instanceof EditListener){
				//目标位置已有一个EditListener，就将它们合并
				EditListenerList list = new myEditListenerList();
				list.add(l);
				list.add(li);
				mListeners[toIndex] = list;
			}
		}
		else{
			//否则直接设置
			mListeners[toIndex] = li;
			mListenerCount++;
		}
		return true;
	}
	private void checkIndex(int index)
	{
		if(index+1>mListeners.length){
			EditListener[] list = new EditListener[index+1];
			System.arraycopy(mListeners,0,list,0,mListeners.length);
			mListeners = list;
		}
	}

	@Override
	public boolean delListenerFrom(int fromIndex){		
		mListeners[fromIndex] = null;
		return true;
	}
	@Override
	public EditListener findAListener(int fromIndex){
		return mListeners[fromIndex];
	}
	@Override
	public int size(){
		return mListenerCount;
	}
	@Override
	public void clear(){
		for(int i=0;i<mListeners.length;++i){
			mListeners[i]=null;
		}
	}
	@Override
	public boolean contrans(EditListener li)
	{
		for(int i=0;i<mListeners.length;++i){
			if(li.equals(mListeners[i])){
				return true;
			}
		}
		return false;
	}

}
	
