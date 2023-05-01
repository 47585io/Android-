package com.mycompany.who.Edit.ListenerVistor.EditListener;
import android.util.*;

public abstract class EditLineChangeListener extends EditListener
{
	abstract protected void onLineChange(int start,int before,int after)
	
	final public void Change(int start,int before,int after){
		try{
			if(Enabled())
			    onChange(start,before,after);
		}catch(Exception e){
			Log.e("Line Change Error",e.toString());
		}
	}
	
	protected void onChange(int start,int before,int after){
		onLineChange(start,before,after);
	}
}
