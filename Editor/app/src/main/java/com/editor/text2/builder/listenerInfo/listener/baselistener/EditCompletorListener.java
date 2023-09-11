package com.editor.text2.builder.listenerInfo.listener.baselistener;

import android.text.*;
import java.util.*;
import com.editor.text2.builder.words.*;
import com.editor.text2.base.share.*;

public abstract interface EditCompletorListener extends EditListener
{
	public abstract wordIcon[] onSearchWord(CharSequence text, int index, Words lib)
	
	public abstract int onInsertWord(Editable editor, int index, CharSequence word)
}
