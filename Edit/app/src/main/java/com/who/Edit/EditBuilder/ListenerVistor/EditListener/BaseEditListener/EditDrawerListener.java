package com.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

public abstract interface EditDrawerListener extends EditListener
{
	public abstract void onFindNodes(int start, int end, CharSequence text, Words WordLib)
	
	public abstract void onDrawNodes(int start, int end, Spannable editor)
	
	public abstract String makeHTML()
}
