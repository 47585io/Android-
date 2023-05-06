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

	public void clear()
	
	public boolean contrans(EditListener li)
	
	
	public static class Helper
	{
		
		public static EditListener checkName(EditListener lis,String name)
		{
			if(lis==null || lis.getName().equals(name))
				return lis;
			if(!(lis instanceof EditListenerList))
				return null;
			
			//它是一个EditListenerList，就额外遍历它的子元素
			for(EditListener li:((EditListenerList)lis).getList()){
				if(li.getName().equals(name)){
					return li;
				}
			}
			return null;
		}
		
		public static boolean Remove(EditListener src,EditListener target)
		{
			if(src==null||target==null)
				return false;
			if(src.equals(target))
				return true;
				
			//它是一个EditListenerList，就额外看看它是否是它的子元素
			if(src instanceof EditListenerList){
				for(EditListener li:((EditListenerList)src).getList()){
					if(li.equals(target)){
						((EditListenerList)src).getList().remove(li);
						return false;
					}
				}
			}
			return false;
		}
	}
	
}
