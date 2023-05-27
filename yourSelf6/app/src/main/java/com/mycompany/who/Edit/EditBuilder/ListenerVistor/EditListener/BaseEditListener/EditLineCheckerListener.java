package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

public abstract interface EditLineCheckerListener extends EditListener
{
	public abstract void onLineChanged(int start,int before,int after)	
}
