package com.mycompany.who.Edit.ListenerVistor;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import java.util.*;

public interface EditListenerInfo
{
	
	public boolean addAListener(EditListener li)

	public boolean delAListener(EditListener li)

	public EditListener findAListener(String name)

	public boolean addListenerTo(EditListener li,int toIndex)

	public boolean delListenerFrom(int fromIndex)

	public EditListener findAListener(int fromIndex)

	
	public static class Helper{
		
		public static EditListener checkName(EditListenerList lis,String name){
			
			if(lis.getName().equals(name))
				return lis;
			
			for(EditListener li:lis.getList()){
				if(li.getName().equals(name)){
					return li;
				}
			}
			return null;
		}
		
	}
	
}
