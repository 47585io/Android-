package com.editor.text2.builder.listener;
import com.editor.text2.builder.listener.baselistener.*;

public class myEditListener implements EditListener
{
	private int flag;
	private Object name;

	public int getFlag(){
		return flag;
	}
	public void setFlag(int flag){
		this.flag = flag;
	}
	public Object getName(){
		return name;
	}
	public void setName(Object name){
		this.name = name;
	}
}
