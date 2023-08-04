package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;
import android.view.*;
import com.who.Edit.Base.Share.Share2.*;
import com.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

public abstract interface EditCompletorListener extends EditListener
{
	public abstract void onSearchWord(CharSequence text, int index, Words Wordlib)
	
	public abstract int onInsertWord(Editable editor, int index, CharSequence word)
	
	public abstract List<Icon> toList()
}
