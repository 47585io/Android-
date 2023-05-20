package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;
import android.widget.*;

/*
  您可以使用此基本接口扩展更多的类或接口
  
  给编辑器设置EditListener，当它调用EditListener时，以改变编辑器行为
*/
public abstract interface EditListener
{

	public abstract int getFlag()
	
	public abstract void setFlag(int flag)
		
	public abstract void setName(Object name)
		
	public abstract Object getName()
	
	public abstract EditListener findListenerByName(Object name)
	
	public abstract void setEdit(EditText t)
	
	public abstract EditText getEdit()
	
	public abstract EditListener getParent()
	
	public abstract void setParent(EditListener parent)
	
	public abstract boolean dispatchCallBack(RunLi Callback)
	
	
	/* 使用此接口管理外部调用的情况 */
	
	public static abstract interface RunLi
	{
		public abstract boolean run(EditListener li);
	}
	
}
