package com.editor.text2.builder.listenerInfo.listener.baselistener;

import android.text.*;
import com.editor.text2.base.*;
import java.util.*;
import com.editor.text2.builder.words.*;

public abstract interface EditDrawerListener extends EditListener
{
	public abstract wordIndex[] onFindNodes(int start, int end, CharSequence text, Words lib)
	
	public abstract void onDrawNodes(int start, int end, Spannable editor, wordIndex[] nodes)
	
	public abstract String makeHTML(int start, int end, CharSequence text, wordIndex[] nodes)
}
