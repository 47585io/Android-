package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.util.*;
import android.text.*;


/*
  光标变化监听器，应该保证编辑器光标可以变化
  
  selStart和selEnd表示光标变化后的位置，editor表示编辑器文本

*/
public abstract class myEditSelectionChangeListener extends myEditListener implements EditSelectionChangeListener
{

	abstract protected void onSelectionChange(int selStart, int selEnd, Spannable editor)
	//光标位置变化了
	
	@Override
	final public void SelectionChange(int selStart, int selEnd, Spannable editor)
	{
		try{
			if(Enabled()){
				onChange(selStart, selEnd, editor);
			}
		}
		catch(Exception e){
			Log.e("Selection Change Error",e.toString());
		}
	}
	
	protected void onChange(int selStart, int selEnd, Spannable editor)
	{
		onSelectionChange(selStart,selEnd,editor);
	}
	
}
