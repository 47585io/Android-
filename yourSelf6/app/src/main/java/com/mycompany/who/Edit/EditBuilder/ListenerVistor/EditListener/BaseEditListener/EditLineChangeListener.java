package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

public abstract interface EditLineChangeListener extends EditListener
{
	public abstract void LineChange(int start,int before,int after)	
}
