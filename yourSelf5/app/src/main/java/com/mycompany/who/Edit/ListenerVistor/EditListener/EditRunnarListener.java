package com.mycompany.who.Edit.ListenerVistor.EditListener;
import android.widget.*;
import android.util.*;
import java.util.*;

/* 根据当前情况预先留下一串命令，待以后执行 */
public abstract class EditRunnarListener extends EditListener
{
	
	abstract protected String onMakeCommand(EditText self,String state)
	//制作命令
	abstract protected void onRunCommand(EditText self,String command)
	//执行命令
	
	public final String LetMeMake(EditText self,String state)
	{
		String command = "";
		try{
			if(Enabled())
			    command = Make(self,state) ;
		}
		catch (IndexOutOfBoundsException e){
			Log.e("MakeCommand Error", toString()+" "+e.toString());
		}
		return command;
	}
	
	protected String Make(EditText self,String state){
		return onMakeCommand(self,state);
	}
	
	public final void LetMeRun(EditText self,String command)
	{
		try{
			if(Enabled())
			    Run(self,command) ;
		}
		catch (IndexOutOfBoundsException e){
			Log.e("RunCommand Error", toString()+" "+e.toString());
		}
	}
	
	protected void Run(EditText self,String command){
		onRunCommand(self,command);
	}
	
	
	public String[] spiltCommand(String command){
		return command.split(":");
	}
	
	protected String[] spiltArgs(String com){
		return com.split(" ");
	}
	protected List<Object> decodeArags(EditText self,String[] args){
		return null;
	}
	
}