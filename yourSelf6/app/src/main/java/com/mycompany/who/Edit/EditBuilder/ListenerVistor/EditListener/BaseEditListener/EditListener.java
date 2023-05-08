package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;
import android.widget.*;

/*
  您可以使用此基本接口扩展更多的类或接口
  
  给编辑器设置EditListener，当它调用EditListener时，以改变编辑器行为
*/
public abstract interface EditListener
{

	public abstract boolean Enabled()
	
	public abstract void setEnabled(boolean Enabled)
		
	public abstract void setName(String name)
		
	public abstract String getName()
	
	public abstract EditListener findListenerByName(String name)
	
	public abstract void setEdit(EditText t)
	
	public abstract EditText getEdit()
	
	public abstract boolean dispatchCallBack(RunLi Callback)
	
	
	/* 使用此接口管理外部调用的情况 */
	
	public static abstract interface RunLi
	{
		public abstract boolean run(EditListener li);
	}
	
}
