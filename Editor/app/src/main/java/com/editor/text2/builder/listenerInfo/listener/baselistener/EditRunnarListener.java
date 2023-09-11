package com.editor.text2.builder.listenerInfo.listener.baselistener;

import android.widget.*;
import com.editor.text.*;

public abstract interface EditRunnarListener extends EditListener
{
	public abstract String onMakeCommand(Edit self, String state)

	public abstract int onRunCommand(Edit self, String command)
}
