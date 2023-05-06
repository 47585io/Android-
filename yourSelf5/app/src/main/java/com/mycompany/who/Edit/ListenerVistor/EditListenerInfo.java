package com.mycompany.who.Edit.ListenerVistor;
import com.mycompany.who.Edit.ListenerVistor.EditListener.BaseEditListener.*;

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

	
	public static class Helper
	{
		public static EditListener checkName(EditListener lis,String name)
		{
			if(lis==null || lis.getName().equals(name))
				return lis;
			if(lis instanceof EditListener)
				return null;
				
			for(EditListener li:((EditListenerList)lis).getList()){
				if(li.getName().equals(name)){
					return li;
				}
			}
			return null;
		}
	}
	
}
