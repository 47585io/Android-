package com.editor.text2.builder.listenerInfo.listener.baselistener;

import android.text.*;

public abstract interface EditFormatorListener extends EditListener
{
	public abstract void onFormat(int start, int end, Editable editor)
	
	public abstract int onInsert(int index, Editable editor)
}
