package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.view.*;
import android.widget.*;

public abstract interface EditRunnarListener extends EditListener
{
	public abstract String onMakeCommand(View self, String state)

	public abstract int onRunCommand(View self, String command)
}
