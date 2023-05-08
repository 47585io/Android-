package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;

public abstract interface EditInsertorListener extends EditListener
{
	public abstract int LetMeInsert(Editable editor, int nowIndex, int count)
}
