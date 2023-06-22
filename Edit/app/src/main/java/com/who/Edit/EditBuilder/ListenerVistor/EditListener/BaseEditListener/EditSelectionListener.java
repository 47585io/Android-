package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;

public abstract interface EditSelectionListener
{
	public abstract void onSelectionChanged(int selStart, int selEnd, Spannable editor)
}
