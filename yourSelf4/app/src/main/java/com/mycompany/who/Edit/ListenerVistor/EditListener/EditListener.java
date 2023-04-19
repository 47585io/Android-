package com.mycompany.who.Edit.ListenerVistor.EditListener;
import android.graphics.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.*;
import java.util.*;

public abstract class EditListener extends Object
{
	private boolean Enabled;
	private String name;
	
	public EditListener(){
		name="@default";
		Enabled=true;
	}
	public EditListener(String name,boolean e){
		this. name=name;
		Enabled=e;
	}

	@Override
	public String toString(){
		return "监听器："+name+"  ;类型: "+getClass().getName();
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
	public String getName(){
		return name;
	}
	
	/* 
	   解析参数比较麻烦，传参也很难，已废弃
	public final void LetMeDo(EditText self,Object... args){
		if(!Enabled())
		return;
		Do(self,args);
	}
	protected void Do(EditText self,Object... args){
		int a = (int)args[0];
		DoDraw(self,a)
	}
	protected void DoDraw(EditText self,int a){
		...
	}
	*/
}
