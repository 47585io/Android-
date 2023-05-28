package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import java.util.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.util.*;


/*
  一般情况下，都可以继承此类扩展其它EditListener
  
  但在特殊情况下，使用EditListener接口会更加轻便
  
*/
public class myEditListener extends Object implements EditListener
{

	public static final int Enabled_Mask = 1;
	//判断或设置监听器启用状态的Mask值
	public static final int Disbled_Mask = ~Enabled_Mask;
	//判断或设置监听器禁用状态的Mask值
	
	private int flag;
	private Object name;
	private EditText self;
	private EditListener parent;
	//可以不以参数传递，而是设置self，但有可能为null

	public myEditListener()
	{
		name="@default";
		flag|=Enabled_Mask;
	}
	public myEditListener(Object name,int flag)
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
	public void setFlag(int flag)
	{
		this.flag = flag;
	}
	@Override
	public int getFlag()
	{
		return flag;
	}
	@Override
	public void setName(Object name)
	{
		this.name=name;
	}
	@Override
	public Object getName()
	{
		return name;
	}
	@Override
	public void setEdit(EditText t)
	{
		this.self = t;
	}
	@Override
	public EditText getEdit()
	{
		return self;
	}
	@Override
	public void setParent(EditListener parent)
	{
		this.parent=parent;
	}
	@Override
	public EditListener getParent()
	{
		return parent;
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
	
	@Override
	public String toString()
	{
		return "监听器："+name+"; 类型: "+getClass().getSimpleName()+";";
	}
	
	public boolean Enabled()
	{
		return (flag & Enabled_Mask) == Enabled_Mask;
	}
	
	@Override
	public EditListener findListenerByName(Object name)
	{
	    return this.name.equals(name) ? this:null;
	}
	
	@Override
	public boolean dispatchCallBack(EditListener.RunLi Callback)
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
