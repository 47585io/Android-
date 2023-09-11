package com.editor.text2.builder.listenerInfo.listener;

import java.util.*;
import android.text.*;
import android.widget.*;
import android.util.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;


/*
  一般情况下，都可以继承此类扩展其它EditListener
  
  但在特殊情况下，使用EditListener接口会更加轻便
  
*/
public class myEditListener extends Object implements EditListener
{
	
	private int flag;
	private Object name;
	//判断或设置监听器启用状态的Mask值
	public static final int Enabled_Mask = 1;
	
	public myEditListener(){
		name="@default";
		flag|=Enabled_Mask;
	}
	public myEditListener(Object name,int flag){
		this.name=name;
		this.flag=flag;
	}
	public myEditListener(EditListener li){
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
	public void setName(Object name){
		this.name=name;
	}
	@Override
	public Object getName(){
		return name;
	}
	
	@Override
	public String toString(){
		return "监听器："+name+"; 类型: "+getClass().getSimpleName()+";";
	}
	@Override
	public boolean equals(Object obj)
	{
		if(obj!=null && obj instanceof EditListener)
		{
			EditListener li = (EditListener) obj;
			if(getName().equals(li.getName()) && li.getFlag()==getFlag()){
				return true;
			}
		}
		return super.equals(obj);
	}
	
	public boolean Enabled(){
		return (flag & Enabled_Mask) == Enabled_Mask;
	}
	@Override
	public EditListener findListenerByName(Object name){
	    return this.name.equals(name) ? this:null;
	}
	@Override
	public boolean dispatchCallBack(RunLi Callback)
	{
		boolean consume = false;
		try{
		    consume = Enabled() ? Callback.run(this):false;
		}
		catch(Exception e){
			Log.e("EditListener dispatchCallBack Error",toString()+"; I make Error: "+e.toString());
		}
		return consume;
	}
	
}
