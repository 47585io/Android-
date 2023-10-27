package com.editor.text;
import android.text.*;

public interface SpanBatchSetter extends Spannable
{
	public void setSpans(Object[] spans, int[] spanStarts, int[] spanEnds, int[] spanFlags)

	public void removeSpans(Object[] spans)

	public void removeSpansInRange(int start, int end)
}
