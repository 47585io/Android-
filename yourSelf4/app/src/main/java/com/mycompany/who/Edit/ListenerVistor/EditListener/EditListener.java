package com.mycompany.who.Edit.ListenerVistor.EditListener;
import java.util.*;

public abstract class EditListener
{
	public boolean Enabled;
	public String name;
	public EditListener(){
		name="@default";
		Enabled=true;
	}
	public EditListener(String name){
		name=name;
	}

	@Override
	public String toString()
	{
		return "监听器："+name+"  ;类型: "+getClass().getName();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(hashCode()==obj.hashCode()||name==((EditListener)obj).name)
			return true;
		return false;
	}
	public static boolean Enabled(EditListener li){
		if(li==null||!li.Enabled)
			return false;
		return true;
	}
	
}
