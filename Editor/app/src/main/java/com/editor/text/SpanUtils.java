package com.editor.text;

import android.text.*;
import com.editor.text.base.*;

public class SpanUtils
{
	public static<T> T[] getSpans(CharSequence src, int queryStart, int queryEnd, Class<T> kind)
	{
		if(src instanceof EditableBlock){
			return ((EditableBlock)src).quickGetSpans(0,src.length(),kind);
		}
		else if(src instanceof Spanned){
			return ((Spanned)src).getSpans(0,src.length(),kind);
		} 
		return EmptyArray.emptyArray(kind);
	}
}
