package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.widget.*;

public abstract interface EditRunnarListener extends EditListener
{
	public abstract String onMakeCommand(EditText self,String state)

	public abstract int onRunCommand(EditText self,String command)
}
