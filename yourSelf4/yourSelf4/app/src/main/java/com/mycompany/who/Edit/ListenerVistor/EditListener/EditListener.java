package com.mycompany.who.Edit.ListenerVistor.EditListener;
import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;

public abstract class EditListener
{
	private boolean Enabled;
	private String name;
	public EditListener(){
		name="@default";
		Enabled=true;
	}
	public EditListener(String name){
		this. name=name;
	}

	@Override
	public String toString(){
		return "监听器："+name+"  ;类型: "+getClass().getName();
	}
	@Override
	public boolean equals(Object obj)
	{
		if(super.equals(obj)||name==((EditListener)obj).name)
			return true;
		return false;
	}
	public boolean Enabled(){
		if(Enabled)
			return true;
		return false;
	}
	public void setEnabled(boolean Enabled){
		this. Enabled=Enabled;
	}
	public void setName(String name){
		this.name=name;
	}
	
}
