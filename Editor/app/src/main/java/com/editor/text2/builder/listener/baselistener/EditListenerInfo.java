package com.editor.text2.builder.listener.baselistener;

public class EditListenerInfo
{
	public EditDrawerListener mDrawerListener;
	
	public EditFormatorListener mFormatorListener;
	
	public EditCompletorListener[] mCompletorListeners;
	
	public EditCanvaserListener[] mCanvaserListeners;
	
	public EditRunnarListener mRunnarListener;
	
	public void clear()
	{
		mDrawerListener = null;
		mFormatorListener = null;
		mCompletorListeners = null;
		mCanvaserListeners = null;
		mRunnarListener = null;
	}
	
	public EditListener findListenerByName(Object name)
	{
		for(int i=0;i<mCompletorListeners.length;++i){
			if(mCompletorListeners[i].getFlag()==name){
				return mCompletorListeners[i];
			}
		}
		for(int i=0;i<mCanvaserListeners.length;++i){
			if(mCanvaserListeners[i].getName()==name){
				return mCanvaserListeners[i];
			}
		}
		if(mDrawerListener.getName()==name){
			return mDrawerListener;
		}
		if(mFormatorListener.getName()==name){
			return mFormatorListener;
		}
		if(mRunnarListener.getName()==name){
			return mRunnarListener;
		}
		return null;
	}
	
	public boolean contains(EditListener li)
	{
		for(int i=0;i<mCompletorListeners.length;++i){
			if(mCompletorListeners[i]==li){
				return true;
			}
		}
		for(int i=0;i<mCanvaserListeners.length;++i){
			if(mCanvaserListeners[i]==li){
				return true;
			}
		}
		return mDrawerListener==li || mFormatorListener==li || mRunnarListener==li;
	}
	
	public int size()
	{
		return (mDrawerListener!=null ? 1:0) + 
		       (mFormatorListener!=null ? 1:0) +
			   (mRunnarListener!=null ? 1:0) +
			   (mCompletorListeners!=null ? mCompletorListeners.length:0) +
			   (mCanvaserListeners!=null ? mCanvaserListeners.length:0);
	}
}
