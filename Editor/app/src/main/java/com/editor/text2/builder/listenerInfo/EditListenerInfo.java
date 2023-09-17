package com.editor.text2.builder.listenerInfo;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;

public abstract interface EditListenerInfo
{
	
	public static final int DrawerIndex = 0;

	public static final int FormatorIndex = 1;
	
	public static final int CompletorIndex = 2;

	public static final int CanvaserIndex = 3;

	public static final int RunnarIndex = 4;


	public abstract boolean addAListener(EditListener li)

	public abstract boolean delAListener(EditListener li)

	public abstract EditListener findAListener(Object name)

	public abstract boolean addListenerTo(EditListener li,int toIndex)

	public abstract boolean delListenerFrom(int fromIndex)

	public abstract EditListener findAListener(int fromIndex)

	public abstract int size()

	public abstract void clear()

	public abstract boolean contrans(EditListener li)

}

