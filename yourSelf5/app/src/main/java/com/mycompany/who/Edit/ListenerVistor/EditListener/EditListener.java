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
	private EditText self;
	//可以不以参数传递，而是设置self，但有可能为null
	
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
	public void setEdit(EditText t){
		self = t;
	}
	public EditText getEdit(){
		return self;
	}
	
	/* 
	   解析参数比较麻烦，传参也很难，已废弃   
	*/
	/*
	public final void LetMeDo(int flag,Object... args)
	{
		if(Enabled()){
		    dispatchArgs(flag,args);
		}
	}
	protected void dispatchArgs(int flag,Object... args)
	{
		switch(flag){
			default:
			    decodeArgsWithDraw(args);
		}
	}
	protected void decodeArgsWithDraw(Object... args)
	{
		int start = args[0];
		int end = args[1];
		String src = args[2];
		DoDraw(start,end,src);
	}
	protected void DoDraw(int start,int end,String src){
		...
	}
	*/
}
