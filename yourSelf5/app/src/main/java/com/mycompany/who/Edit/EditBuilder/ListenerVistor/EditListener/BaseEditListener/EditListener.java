package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;
import android.widget.*;

/*
  您可以使用此基本接口扩展更多的类或接口
  
  给编辑器设置EditListener，当它调用EditListener时，以改变编辑器行为
*/
public interface EditListener
{

	public boolean Enabled()
	
	public void setEnabled(boolean Enabled)
		
	public void setName(String name)
		
	public String getName()
		
	public void setEdit(EditText t)
	
	public EditText getEdit()
	
}
