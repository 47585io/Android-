package com.editor.text2.builder.listener.baselistener;

import android.text.*;
import com.editor.text2.base.share2.*;
import com.editor.text2.builder.words.*;

public abstract interface EditCompletorListener extends EditListener
{
	public abstract wordIcon[] onSearchWord(CharSequence text, int index, Words lib)
	
	public abstract wordIcon[] onSearchDoc(CharSequence word, Words lib)
	
	public abstract int onInsertWord(Editable editor, int index, CharSequence word)
}
