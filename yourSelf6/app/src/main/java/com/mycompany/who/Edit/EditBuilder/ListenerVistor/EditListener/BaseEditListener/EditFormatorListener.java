package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;

public abstract interface EditFormatorListener extends EditListener
{
	public abstract void LetMeFormat(int start, int end, Editable editor)
}
