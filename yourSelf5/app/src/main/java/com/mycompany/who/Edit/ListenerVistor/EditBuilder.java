package com.mycompany.who.Edit.ListenerVistor;
import android.widget.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.Base.*;

/*
  知道interface里的函数加abstract有什么用吗？
  
  这是为了方便以后将interfac改成abstract class
  
*/
public interface EditBuilder
{
	
	public static interface ListenerFactory
	{
		abstract public void SwitchListener(EditText Edit,String Lua)
		
		abstract public EditListener ToLisrener(String Lua)
	}
	
	public static interface WordsPacket
	{
		abstract public void SwitchWords(EditText Edit,String Lua)
		
		abstract public Words UnPackWords(String Lua)
	}
	
	abstract public void SwitchLuagua(EditText Edit,String Lua)
	
	abstract public void trimListener(EditText Edit)
	
	abstract public void clearListener(EditText Edit)
	
}
