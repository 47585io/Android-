package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.EditListener.*;


/*
  一般情况下，都可以继承此类扩展其它EditListener
  
  但在特殊情况下，使用EditListener接口会更加轻便
  
*/
public class myEditListener extends Object implements EditListener
{
	private boolean Enabled;
	private String name;
	private EditText self;
	//可以不以参数传递，而是设置self，但有可能为null
	
	public myEditListener(){
		name="@default";
		Enabled=true;
	}
	public myEditListener(String name,boolean e){
		this. name=name;
		Enabled=e;
	}

	@Override
	public String toString(){
		return "监听器："+name+"  ;类型: "+getClass().getName();
	}
	
	public boolean Enabled(){
		return Enabled;
	}
	public void setEnabled(boolean Enabled){
		this.Enabled=Enabled;
	}
	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return name;
	}
	public void setEdit(EditText t){
		self = t;
	}
	public EditText getEdit(){
		return self;
	}
	
	@Override
	public boolean dispatchCallBack(EditListener.RunLi Callback)
	{
		return Callback.run(this);
	}
	
	
}
