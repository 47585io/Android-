package com.mycompany.who.Edit.EditBuilder.ListenerVistor;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;

/* 
  用EditListenerInfo管理一堆EditListener 
*/
public interface EditListenerInfo
{
	
	public static final int FinderIndex = 0;
	
	public static final int DrawerIndex = 1;
	
	public static final int FormatorIndex = 2;
	
	public static final int InsertorIndex = 3;
	
	public static final int CompletorIndex = 4;
	
	public static final int CanvaserIndex = 5;
	
	public static final int RunnarIndex = 6;
	
	public static final int LineCheckerIndex = 7;
	
	
	public boolean addAListener(EditListener li)

	public boolean delAListener(EditListener li)

	public EditListener findAListener(String name)

	public boolean addListenerTo(EditListener li,int toIndex)

	public boolean delListenerFrom(int fromIndex)

	public EditListener findAListener(int fromIndex)

	public int size()
	
	public void clear()
	
	public boolean contrans(EditListener li)
	
}
