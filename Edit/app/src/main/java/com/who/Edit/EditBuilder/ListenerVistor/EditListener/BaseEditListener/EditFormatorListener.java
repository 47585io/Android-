package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;

public abstract interface EditFormatorListener extends EditListener
{
	public abstract void onFormat(int start, int end, Editable editor)
	
	public abstract int onInsert(int index, int count, Editable editor)
}
