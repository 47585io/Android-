package com.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.util.*;
import android.widget.*;
import com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.view.*;


/* 
 根据当前情况预先留下一串命令，待以后执行
 
 此监听器用于未知的，随机的任意事件，命令参数为String，可以表示任何意思，self表示编辑器本身
	
*/
public abstract class myEditRunnarListener extends myEditListener implements EditRunnarListener
{
	
	public static final String DEFAULT_STATE = "DEFAULT_STATE";
	
	public static final String DEFAULT_COMMAND = "DEFAULT_COMMAND";
	
	public static final RunResult Default = new RunResultForCode(0);
	
	public static final RunResult Error = new RunResultForCode(-1);
	
	public static final RunResult Warring = new RunResultForCode(-2);
	
	public static final String AragSpilt = " ";

	public static final String CommandSpilt = ":";
	

	@Override
	public abstract String onMakeCommand(View self,String state)
	
	@Override
	public abstract RunResult onRunCommand(View self,String command)
	
	
	public static class RunResultForCode implements RunResult
	{
		int code;
		
		public RunResultForCode(int i){
			code = i;
		}

		@Override
		public String toString()
		{
			return String.valueOf(code);
		}
	}

}
