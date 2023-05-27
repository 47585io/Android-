package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/* 
 根据当前情况预先留下一串命令，待以后执行
 
 此监听器用于未知的，随机的任意事件，命令参数为String，可以表示任何意思，self表示编辑器本身
	
*/
public abstract class myEditRunnarListener extends myEditListener implements EditRunnarListener
{
	
	public static final String DEFAULT_STATE = "DEFAULT_STATE";
	
	public static final String DEFAULT_COMMAND = "DEFAULT_COMMAND";
	
	public static final int Default = 0;
	
	public static final int Error = -1;
	
	public static final int Warring = -2;
	
	public static final char AragSpilt = ' ';

	public static final char CommandSpilt = ':';
	
	
	abstract protected String onMakeCommand(EditText self,String state)
	//制作命令
	abstract protected int onRunCommand(EditText self,String command)
	//执行命令
	
	
	@Override
	public final String LetMeMake(EditText self,String state)
	{
		String command = "";
		try{		
			command = Make(self,state) ;	
		}
		catch (IndexOutOfBoundsException e){
			Log.e("MakeCommand Error", toString()+" "+e.toString());
		}
		return command;
	}
	
	protected String Make(EditText self,String state)
	{
		return onMakeCommand(self,state);
	}
	
	@Override
	public final int LetMeRun(EditText self,String command)
	{
		int flag = Default;
		try{	
			flag = Run(self,command);
		}
		catch (IndexOutOfBoundsException e){
			flag = Error;
			Log.e("RunCommand Error", toString()+" "+e.toString());
		}
		return flag;
	}
	
	protected int Run(EditText self,String command)
	{
		return onRunCommand(self,command);
	}
	
}
