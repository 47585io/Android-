package com.mycompany.who.Edit.ListenerVistor;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import java.util.*;

public interface EditListenerInfo
{
	
	public boolean addAListener(EditListener li)

	public boolean delAListener(EditListener li)

	public EditListener findAListener(String name)
	
	
	public static class FindHelper{
		
		public static EditListener checkName(List<EditListener> lis,String name){
			for(EditListener li:lis){
				if(li.getName().equals(name)){
					return li;
				}
			}
			return null;
		}
		
	}
	
}
