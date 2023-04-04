package com.mycompany.who.Edit.ListenerVistor;
import android.widget.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;
import com.mycompany.who.Edit.*;

/*
  知道interface里的函数加abstract有什么用吗？
  这是为了方便以后将interfac改成abstract class
*/
public interface EditListenerFactory
{
	
	public static interface Factory{
		
		abstract public void SwitchListener(EditText Edit,String Lua)
		
		abstract public EditListener ToLisrener(String Lua)
	}
	
	abstract public void SwitchLuagua(EditText Edit,String Lua)
	
}
