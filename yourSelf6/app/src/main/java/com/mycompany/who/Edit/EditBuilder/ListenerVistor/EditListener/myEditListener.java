package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import com.mycompany.who.Edit.Base.Share.*;


/*
  一般情况下，都可以继承此类扩展其它EditListener
  
  但在特殊情况下，使用EditListener接口会更加轻便
  
*/
public class myEditListener extends Object implements EditListener
{
	
	public static final byte Enabled_Bit = 0;
	//监听器启用位
	
	private int flag;
	private String name;
	private EditText self;
	//可以不以参数传递，而是设置self，但有可能为null
	
	public myEditListener()
	{
		name="@default";
		flag=Share.setbitTo_1(flag,Enabled_Bit);
	}
	public myEditListener(String name,int flag)
	{
		this.name=name;
		this.flag=flag;
	}
	public myEditListener(EditListener li)
	{
		this.name=li.getName();
		this.flag=li.getFlag();
	}

	@Override
	public void setFlag(int flag){
		this.flag = flag;
	}
	@Override
	public int getFlag(){
		return flag;
	}
	@Override
	public void setName(String name){
		this.name=name;
	}
	@Override
	public String getName(){
		return name;
	}
	@Override
	public void setEdit(EditText t){
		self = t;
	}
	@Override
	public EditText getEdit(){
		return self;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof EditListener)
		{
			EditListener li = (EditListener) obj;
			if(getName().equals(li.getName()) && li.getFlag()==getFlag() && li.getEdit().equals(getEdit())){
				return true;
			}
		}
		return super.equals(obj);
	}
	
	@Override
	public String toString(){
		return "监听器："+name+"  ;类型: "+getClass().getName();
	}
	
	public boolean Enabled(){
		return Share.getbit(flag,Enabled_Bit);
	}
	
	@Override
	public EditListener findListenerByName(String name){
	    return this.name.equals(name) ? this:null;
	}
	
	@Override
	public boolean dispatchCallBack(EditListener.RunLi Callback){
		return Callback.run(this);
	}
	
}
