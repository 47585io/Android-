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
}
