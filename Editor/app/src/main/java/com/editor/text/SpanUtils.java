package com.editor.text;

import android.text.*;
import com.editor.text.base.*;

public class SpanUtils
{
	public static<T> T[] getSpans(CharSequence src, int queryStart, int queryEnd, Class<T> kind)
	{
		if(src instanceof EditableBlock){
			return ((EditableBlock)src).quickGetSpans(queryStart,queryEnd,kind);
		}
		else if(src instanceof Spanned){
			return ((Spanned)src).getSpans(queryStart,queryEnd,kind);
		} 
		return EmptyArray.emptyArray(kind);
	}
	
	public static void setSpans(CharSequence src, Object[] spans, int[] spanStarts, int[] spanEnds, int[] spanFlags)
	{
		if(src instanceof SpanBatchSetter){
			((SpanBatchSetter)src).setSpans(spans,spanStarts,spanEnds,spanFlags);
		}
		else if(src instanceof Spannable)
		{
			Spannable spanStr = (Spannable) src;
			for(int i=0;i<spans.length;++i){
				spanStr.setSpan(spans[i],spanStarts[i],spanEnds[i],spanFlags[i]);
			}
		}
	}

	public static void removeSpans(CharSequence src, Object... spans)
	{
		if(src instanceof SpanBatchSetter){
			((SpanBatchSetter)src).removeSpans(spans);
		}
		else if(src instanceof Spannable)
		{
			Spannable spanStr = (Spannable) src;
			for(int i=0;i<spans.length;++i){
				spanStr.removeSpan(spans[i]);
			}
		}
	}

	public static void removeSpansInRange(CharSequence src, int start, int end)
	{
		if(start==0 && end==src.length() && src instanceof Editable){
			((Editable)src).clearSpans();
		}
		else if(src instanceof SpanBatchSetter){
			((SpanBatchSetter)src).removeSpansInRange(start,end);
		}
		else if(src instanceof Spannable)
		{
			Spannable spanStr = (Spannable) src;
			Object[] spans = spanStr.getSpans(start,end,Object.class);
			for(int i=0;i<spans.length;++i){
				if(spanStr.getSpanStart(spans[i])>=start || spanStr.getSpanEnd(spans[i])<=end){
					spanStr.removeSpan(spans[i]);
				}
			}
		}
	}
	
	public static CharSequence removeRepeatSpans(CharSequence src, CharSequence dst, int start, int end){return null;}
	
	public static boolean hasSpanPointAt(CharSequence src, int offset){return false;}
}
