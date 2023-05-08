package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.widget.*;

public abstract interface EditRunnarListener extends EditListener
{
	public abstract String LetMeMake(EditText self,String state)

	public abstract int LetMeRun(EditText self,String command)
}
