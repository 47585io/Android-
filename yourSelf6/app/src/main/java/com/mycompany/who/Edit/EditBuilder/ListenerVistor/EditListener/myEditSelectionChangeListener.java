package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;
import android.util.*;
import android.text.*;

public abstract class myEditSelectionChangeListener extends myEditListener implements EditSelectionChangeListener
{

	abstract protected void onSelectionChange(int selStart, int selEnd)
	
	@Override
	final public void SelectionChange(int selStart, int selEnd)
	{
		try{
			if(Enabled())
				onChange(selStart, selEnd);
		}
		catch(Exception e){
			Log.e("Selection Change Error",e.toString());
		}
	}
	
	protected void onChange(int selStart, int selEnd){
		onSelectionChange(selStart,selEnd);
	}
	
}
