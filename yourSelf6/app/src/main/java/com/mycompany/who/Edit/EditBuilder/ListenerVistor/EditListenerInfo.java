package com.mycompany.who.Edit.EditBuilder.ListenerVistor;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;

/* 
  用EditListenerInfo管理一堆EditListener 
*/
public abstract interface EditListenerInfo
{
	
	public static final int FinderIndex = 0;
	
	public static final int DrawerIndex = 1;
	
	public static final int FormatorIndex = 2;
	
	public static final int InsertorIndex = 3;
	
	public static final int CompletorIndex = 4;
	
	public static final int CanvaserIndex = 5;
	
	public static final int RunnarIndex = 6;
	
	public static final int LineCheckerIndex = 7;
	
	public static final int SelectionSeerIndex = 8;
	
	
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
