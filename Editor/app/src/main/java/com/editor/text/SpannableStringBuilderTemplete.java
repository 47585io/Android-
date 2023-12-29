package com.editor.text;

import android.text.*;
import android.util.*;
import com.editor.text.base.*;
import java.lang.reflect.*;
import java.util.*;


/** 这是内容和标记都可以更改的文本类 */
public class SpannableStringBuilderTemplete extends SpannableStringBuilder implements EditableBlock
{

	/** 创建一个包含空内容的新SpannableStringBuilder */
	public SpannableStringBuilderTemplete() {
		super("");
	}
	/** 创建一个包含指定文本的新SpannableStringBuilder，包括其范围(如果有) */
    public SpannableStringBuilderTemplete(CharSequence text) {
        super(text, 0, text.length());
    }
	/** 创建一个新的SpannableStringBuilder，其中包含指定文本的指定部分，包括其范围(如果有) */
    public SpannableStringBuilderTemplete(CharSequence text, int start, int end) {
        super(text,start,end);
    }

	@Override
	public boolean isInvalidSpan(Object span, int start, int end, int flags){
		return start==end && (flags&SPAN_EXCLUSIVE_EXCLUSIVE)==SPAN_EXCLUSIVE_EXCLUSIVE;
	}

	@Override
	public boolean canRemoveSpan(Object span, int delstart, int delend, boolean textIsRemoved)
	{
		int spanStart = getSpanStart(span);
		int spanEnd = getSpanEnd(span);
		int spanFlags = getSpanFlags(span);
		return (spanFlags & Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE &&
			spanStart >= delstart && spanEnd <= delend &&
			(textIsRemoved || spanStart > delstart || spanEnd < delend);
	}

	@Override
	public boolean needExpandSpanStart(Object span, int flags){
		int startFlag = (flags & START_MASK) >> START_SHIFT;
		return startFlag != POINT;
	}

	@Override
	public boolean needExpandSpanEnd(Object span, int flags){
		int endFlag = flags & END_MASK;
		return endFlag == POINT;
	}

	@Override
	public void enforceSetSpan(Object span, int start, int end, int flags){
		setSpan(span,start,end,flags);
	}

	@Override
	public <T extends Object> T[] quickGetSpans(int queryStart, int queryEnd, Class<T> kind){
		return getSpans(queryStart,queryEnd,kind);
	}
	
	private static final int MARK = 1;
    private static final int POINT = 2;
    private static final int START_MASK = 0xF0;
    private static final int END_MASK = 0x0F;
    private static final int START_SHIFT = 4;

}

