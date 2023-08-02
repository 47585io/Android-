package com.editor.text;

import android.graphics.*;
import android.os.*;
import android.text.*;
import android.util.*;
import java.lang.reflect.*;
import java.util.*;


/**
 * This is the class for text whose content and markup can both be changed.
*/
public class SpannableStringBuilder implements CharSequence, GetChars, Spannable, Editable, Appendable 
{
	
	private final static String TAG = "SpannableStringBuilder";
	/**
	 * Create a new SpannableStringBuilder with empty contents
    */
	public SpannableStringBuilder() {
			this("");
	}
    /**
     * Create a new SpannableStringBuilder containing a copy of the
     * specified text, including its spans if any.
     */
    public SpannableStringBuilder(CharSequence text) {
        this(text, 0, text.length());
    }
    /**
     * Create a new SpannableStringBuilder containing a copy of the
     * specified slice of the specified text, including its spans if any.
     */
    public SpannableStringBuilder(CharSequence text, int start, int end) 
	{
        int srclen = end - start;
        if (srclen < 0) throw new StringIndexOutOfBoundsException();
        mText = new char[srclen];
        mGapStart = srclen;
        mGapLength = mText.length - srclen;
        TextUtils.getChars(text, start, end, mText, 0);
    
		mSpanCount = 0;
        mSpanInsertCount = 0;
        mSpans = new Object[0];
        mSpanStarts = new int[0];
        mSpanEnds = new int[0];
        mSpanFlags = new int[0];
        mSpanMax = new int[0];
        mSpanOrder = new int[0];
		
        if (text instanceof Spanned) 
		{
            Spanned sp = (Spanned) text;
            Object[] spans = sp.getSpans(start, end, Object.class);
            for (int i = 0; i < spans.length; i++) {
                if (spans[i] instanceof NoCopySpan) {
                    continue;
                }
                int st = sp.getSpanStart(spans[i]) - start;
                int en = sp.getSpanEnd(spans[i]) - start;
                int fl = sp.getSpanFlags(spans[i]);
                if (st < 0)
                    st = 0;
                if (st > end - start)
                    st = end - start;
                if (en < 0)
                    en = 0;
                if (en > end - start)
                    en = end - start;
                setSpan(false, spans[i], st, en, fl, false/*enforceParagraph*/);
            }
            restoreInvariants();
        }
    }
    public static SpannableStringBuilder valueOf(CharSequence source)
	{
        if (source instanceof SpannableStringBuilder) {
            return (SpannableStringBuilder) source;
        } else {
            return new SpannableStringBuilder(source);
        }
    }
	
	public SpannableStringBuilder append(CharSequence text){
        int length = length();
        return replace(length, length, text, 0, text.length());
    }
    public SpannableStringBuilder append(CharSequence text, Object what, int flags)
	{
        int start = length();
        append(text);
        setSpan(what, start, length(), flags);
        return this;
    }
    public SpannableStringBuilder append(CharSequence text, int start, int end) {
        int length = length();
        return replace(length, length, text, start, end);
    }
    public SpannableStringBuilder append(char text) {
        return append(String.valueOf(text));
    }
	
	public SpannableStringBuilder insert(int where, CharSequence tb, int start, int end) {
        return replace(where, where, tb, start, end);
    }
    public SpannableStringBuilder insert(int where, CharSequence tb) {
        return replace(where, where, tb, 0, tb.length());
    }
    public SpannableStringBuilder delete(int start, int end) 
	{
        SpannableStringBuilder ret = replace(start, end, "", 0, 0);
        if (mGapLength > 2 * length())
            resizeFor(length());
        return ret; 
    }
	public SpannableStringBuilder replace(int start, int end, CharSequence tb) {
        return replace(start, end, tb, 0, tb.length());
    }
	
    public SpannableStringBuilder replace(final int start, final int end, CharSequence tb, int tbstart, int tbend)
	{
        checkRange("replace", start, end);
        int filtercount = mFilters.length;
		//过滤文本
        for (int i = 0; i < filtercount; i++) 
		{
            CharSequence repl = mFilters[i].filter(tb, tbstart, tbend, this, start, end);
            if (repl != null) {
                tb = repl;
                tbstart = 0;
                tbend = repl.length();
            }
        }
		
        final int origLen = end - start;
        final int newLen = tbend - tbstart;
        if (origLen == 0 && newLen == 0 && !hasNonExclusiveExclusiveSpanAt(tb, tbstart)) {
            //如果tb中没有要添加的跨度(长度为0)，提前退出，以便文本观察器不会得到通知
			return this;
        }
        TextWatcher[] textWatchers = getSpans(start, start + origLen, TextWatcher.class);
        sendBeforeTextChanged(textWatchers, start, origLen, newLen);
		
		//在文本替换过程中，尽量将光标选择保持在相同的相对位置
		//如果replaced或replacement text length为0，则已经处理了这个
		boolean adjustSelection = origLen != 0 && newLen != 0;
        int selectionStart = 0;
        int selectionEnd = 0;
        if (adjustSelection) {
			//获取光标的Span在文本中的起始和末尾位置
            selectionStart = Selection.getSelectionStart(this);
            selectionEnd = Selection.getSelectionEnd(this);
        }
		//改变文本
        change(start, end, tb, tbstart, tbend);
		
        if (adjustSelection)
		{
            boolean changed = false;
            if (selectionStart > start && selectionStart < end) 
			{
				//如果当前的光标位置正好在删除的文本之间，我们需要保留光标的Span
				//光标要变化的offset是新添加文本相对于原文本的倍数
				//原光标位置是 start + diff
				//新光标位置是 start + diff * (newLen/origLen)
                final long diff = selectionStart - start;
                final int offset = Math.toIntExact(diff * newLen / origLen);
                selectionStart = start + offset;
                changed = true;
                setSpan(false, Selection.SELECTION_START, selectionStart, selectionStart,
                        Spanned.SPAN_POINT_POINT, true/*强制执行段落*/);
            }
            if (selectionEnd > start && selectionEnd < end) 
			{
                final long diff = selectionEnd - start;
                final int offset = Math.toIntExact(diff * newLen / origLen);
                selectionEnd = start + offset;
                changed = true;
                setSpan(false, Selection.SELECTION_END, selectionEnd, selectionEnd,
                        Spanned.SPAN_POINT_POINT, true/*强制执行段落*/);
            }
            if (changed) {
                restoreInvariants();
            }
        }
		
        sendTextChanged(textWatchers, start, origLen, newLen);
        sendAfterTextChanged(textWatchers);
        // Span观察器需要在文本观察器之后调用，这可能会更新布局
        sendToSpanWatchers(start, end, newLen - origLen);
        return this;
    }
	
	private void change(int start, int end, CharSequence cs, int csStart, int csEnd) 
	{
		//删除文本的长度，替换的文本长度，较原来培加的文本的长度
        final int replacedLength = end - start;
        final int replacementLength = csEnd - csStart;
        final int nbNewChars = replacementLength - replacedLength;
		boolean changed = false;
		
		//遍历所有的span，如果span在删除的范围之后，需要将它的范围前移
        for (int i = mSpanCount - 1; i >= 0; i--)
		{
            int spanStart = mSpanStarts[i];
            if (spanStart > mGapStart){
                spanStart -= mGapLength;
			}
            int spanEnd = mSpanEnds[i];
            if (spanEnd > mGapStart){
                spanEnd -= mGapLength;
			}
            if ((mSpanFlags[i] & SPAN_PARAGRAPH) == SPAN_PARAGRAPH) 
			{
                int ost = spanStart;
                int oen = spanEnd;
                int clen = length();
                if (spanStart > start && spanStart <= end) {
                    for (spanStart = end; spanStart < clen; spanStart++)
                        if (spanStart > end && charAt(spanStart - 1) == '\n')
                            break;
                }
                if (spanEnd > start && spanEnd <= end) {
                    for (spanEnd = end; spanEnd < clen; spanEnd++)
                        if (spanEnd > end && charAt(spanEnd - 1) == '\n')
                            break;
                }
                if (spanStart != ost || spanEnd != oen) {
                    setSpan(false, mSpans[i], spanStart, spanEnd, mSpanFlags[i],
                            true/*enforceParagraph*/);
                    changed = true;
                }
            }
            int flags = 0;
            if (spanStart == start) flags |= SPAN_START_AT_START;
            else if (spanStart == end + nbNewChars) flags |= SPAN_START_AT_END;
            if (spanEnd == start) flags |= SPAN_END_AT_START;
            else if (spanEnd == end + nbNewChars) flags |= SPAN_END_AT_END;
            mSpanFlags[i] |= flags;
        }
		
        if (changed) {
            restoreInvariants();
        }
        moveGapTo(end);
        if (nbNewChars >= mGapLength) {
            resizeFor(mText.length + nbNewChars - mGapLength);
        }
        final boolean textIsRemoved = replacementLength == 0;
		//需要在间隙更新之前完成移除过程，以便将正确的先前位置传递给正确的相交跨度观察器
        if (replacedLength > 0){ 
		    //纯插入时不需要span修正
            while (mSpanCount > 0 && removeSpansForChange(start, end, textIsRemoved, treeRoot())) {
				  //根据需要不断删除spans，每次删除后从根重新开始，因为删除会使索引失效
            }
        }
		
        mGapStart += nbNewChars;
        mGapLength -= nbNewChars;
        if (mGapLength < 1)
            new Exception("mGapLength < 1").printStackTrace();
        TextUtils.getChars(cs, csStart, csEnd, mText, start);
        
		if (replacedLength > 0)
		{ 
		    //纯插入时不需要span修正
            //潜在优化:仅更新相交跨度的界限
            final boolean atEnd = (mGapStart + mGapLength == mText.length);
            for (int i = 0; i < mSpanCount; i++)
			{
                final int startFlag = (mSpanFlags[i] & START_MASK) >> START_SHIFT;
                mSpanStarts[i] = updatedIntervalBound(mSpanStarts[i], start, nbNewChars, startFlag,
								                      atEnd, textIsRemoved);
                final int endFlag = (mSpanFlags[i] & END_MASK);
                mSpanEnds[i] = updatedIntervalBound(mSpanEnds[i], start, nbNewChars, endFlag,
													atEnd, textIsRemoved);
            }
            //潜在优化:仅在边界实际更改时修复不变量
            restoreInvariants();
        }
		
		//如果增加的文本是Spanned，需要获取范围内全部的span并附加到自身
        if (cs instanceof Spanned) 
		{
            Spanned sp = (Spanned) cs;
            Object[] spans = sp.getSpans(csStart, csEnd, Object.class);
            for (int i = 0; i < spans.length; i++) 
		    {
                int st = sp.getSpanStart(spans[i]);
                int en = sp.getSpanEnd(spans[i]);
                //span的位置不可超过截取的范围
				if (st < csStart) st = csStart;
                if (en > csEnd) en = csEnd;
				
                //已有的span不会重复添加
                if (getSpanStart(spans[i]) < 0)
				{
					//将span在原字符串中较csStart的偏移量获取，并加上start偏移到新字符串中的位置
                    int copySpanStart = st - csStart + start;
                    int copySpanEnd = en - csStart + start;
                    int copySpanFlags = sp.getSpanFlags(spans[i]) | SPAN_ADDED;
                    setSpan(false, spans[i], copySpanStart, copySpanEnd, copySpanFlags, false);
                }
            }
            restoreInvariants();
        }
    }
	
	private boolean removeSpansForChange(int start, int end, boolean textIsRemoved, int i)
	{
        if ((i & 1) != 0) {
            //内部树节点
            if (resolveGap(mSpanMax[i]) >= start &&
				removeSpansForChange(start, end, textIsRemoved, leftChild(i))) {
                return true;
            }
        }
        if (i < mSpanCount) 
		{
            if ((mSpanFlags[i] & Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) == Spanned.SPAN_EXCLUSIVE_EXCLUSIVE &&
				mSpanStarts[i] >= start && mSpanStarts[i] < mGapStart + mGapLength &&
				mSpanEnds[i] >= start && mSpanEnds[i] < mGapStart + mGapLength &&
			    //下面的条件表示跨度将变为空
				(textIsRemoved || mSpanStarts[i] > start || mSpanEnds[i] < mGapStart))
			{
                mIndexOfSpan.remove(mSpans[i]);
                removeSpan(i, 0);
                return true;
            }
            return resolveGap(mSpanStarts[i]) <= end && (i & 1) != 0 &&
                removeSpansForChange(start, end, textIsRemoved, rightChild(i));
        }
        return false;
    }
    //注意:调用者负责删除mIndexOfSpan条目
    private void removeSpan(int i, int flags) 
	{
        Object object = mSpans[i];
        int start = mSpanStarts[i];
        int end = mSpanEnds[i];
        if (start > mGapStart) start -= mGapLength;
        if (end > mGapStart) end -= mGapLength;
       
		//要移除此span，其实就是把此span之后的span全部往前挪一位
		int count = mSpanCount - (i + 1);
        System.arraycopy(mSpans, i + 1, mSpans, i, count);
        System.arraycopy(mSpanStarts, i + 1, mSpanStarts, i, count);
        System.arraycopy(mSpanEnds, i + 1, mSpanEnds, i, count);
        System.arraycopy(mSpanFlags, i + 1, mSpanFlags, i, count);
        System.arraycopy(mSpanOrder, i + 1, mSpanOrder, i, count);
        
		mSpanCount--;
        invalidateIndex(i);
        mSpans[mSpanCount] = null;
        //在发送span removed通知之前，必须恢复不变量
        restoreInvariants();
        if ((flags & Spanned.SPAN_INTERMEDIATE) == 0) {
            sendSpanRemoved(object, start, end);
        }
    }
	
	//更新的间隔绑定
	private int updatedIntervalBound(int offset, int start, int nbNewChars, int flag, boolean atEnd, boolean textIsRemoved)
	{
        if (offset >= start && offset < mGapStart + mGapLength) 
		{
            if (flag == POINT) {
                //位于替换范围内的点应该移动到替换文本的末尾。
				//例外情况是，当该点位于范围的开始处，并且我们正在进行文本替换(与删除相反):该点停留在那里。
				if (textIsRemoved || offset > start) {
                    return mGapStart + mGapLength;
                }
            } 
			else 
			{
                if (flag == PARAGRAPH) {
                    if (atEnd) {
                        return mGapStart + mGapLength;
                    }
                }
				else 
				{ 
				    //应该将标记移动到开头，但位于范围结尾的标记除外(由于mGapLength大于0
				    //因此该标记应该< mGapStart + mGapLength，它应该在替换文本的结尾保持“不变”
                    if (textIsRemoved || offset < mGapStart - nbNewChars) {
                        return start;
                    } else {
                        //移动到替换文本的末尾(如果nbNewChars！= 0)
                        return mGapStart;
                    }
                }
            }
        }
        return offset;
    }
    
    /**
     * Return the char at the specified offset within the buffer.
     */
    public char charAt(int where) 
	{
        int len = length();
        if (where < 0) {
            throw new IndexOutOfBoundsException("charAt: " + where + " < 0");
        } else if (where >= len) {
            throw new IndexOutOfBoundsException("charAt: " + where + " >= length " + len);
        }
        if (where >= mGapStart)
            return mText[where + mGapLength];
        else
            return mText[where];
    }
    /**
     * Return the number of chars in the buffer.
     */
    public int length() {
        return mText.length - mGapLength;
    }
    private void resizeFor(int size) 
	{
        final int oldLength = mText.length;
        if (size + 1 <= oldLength) {
            return;
        }
		
		//创建一个size大小的数组，并将原数组中mGapStart之前的字符拷贝过去
        char[] newText = new char[size];
        System.arraycopy(mText, 0, newText, 0, mGapStart);
        final int newLength = newText.length;
        //新增的字符数，原来空闲的字符数
		final int delta = newLength - oldLength;
        final int after = oldLength - (mGapStart + mGapLength);
		//将末尾空闲的字符也拷贝到新数组末尾
        System.arraycopy(mText, oldLength - after, newText, newLength - after, after);
      
		//轮替mText，mGapLength增加长度
		mText = newText;
        mGapLength += delta;
		
        if (mGapLength < 1)
            new Exception("mGapLength < 1").printStackTrace();
        if (mSpanCount != 0) 
		{
			//遍历所有span，在mGapStart之后的span的范围会加delta
            for (int i = 0; i < mSpanCount; i++) {
                if (mSpanStarts[i] > mGapStart) mSpanStarts[i] += delta;
                if (mSpanEnds[i] > mGapStart) mSpanEnds[i] += delta;
            }
            calcMax(treeRoot());
        }
    }
    private void moveGapTo(int where)
	{
        if (where == mGapStart)
            return;
        boolean atEnd = (where == length());
        if (where < mGapStart) {
            int overlap = mGapStart - where;
            System.arraycopy(mText, where, mText, mGapStart + mGapLength - overlap, overlap);
        } else /* where > mGapStart */ {
            int overlap = where - mGapStart;
            System.arraycopy(mText, where + mGapLength - overlap, mText, mGapStart, overlap);
        }
        // TODO:聪明一点(虽然赢的真的没那么大)
        if (mSpanCount != 0) 
		{
            for (int i = 0; i < mSpanCount; i++) 
			{
                int start = mSpanStarts[i];
                int end = mSpanEnds[i];
                if (start > mGapStart)
                    start -= mGapLength;
                if (start > where)
                    start += mGapLength;
                else if (start == where) {
                    int flag = (mSpanFlags[i] & START_MASK) >> START_SHIFT;
                    if (flag == POINT || (atEnd && flag == PARAGRAPH))
                        start += mGapLength;
                }
                if (end > mGapStart)
                    end -= mGapLength;
                if (end > where)
                    end += mGapLength;
                else if (end == where) {
                    int flag = (mSpanFlags[i] & END_MASK);
                    if (flag == POINT || (atEnd && flag == PARAGRAPH))
                        end += mGapLength;
                }
                mSpanStarts[i] = start;
                mSpanEnds[i] = end;
            }
            calcMax(treeRoot());
        }
        mGapStart = where;
    }
    
    public void clear() 
	{
        replace(0, length(), "", 0, 0);
        mSpanInsertCount = 0;
    }
    
    public void clearSpans() 
	{
        for (int i = mSpanCount - 1; i >= 0; i--) 
		{
            Object what = mSpans[i];
            int ostart = mSpanStarts[i];
            int oend = mSpanEnds[i];
            if (ostart > mGapStart)
                ostart -= mGapLength;
            if (oend > mGapStart)
                oend -= mGapLength;
            mSpanCount = i;
            mSpans[i] = null;
            sendSpanRemoved(what, ostart, oend);
        }
        if (mIndexOfSpan != null) {
            mIndexOfSpan.clear();
        }
        mSpanInsertCount = 0;
    }
    
    private static boolean hasNonExclusiveExclusiveSpanAt(CharSequence text, int offset) 
	{
        if (text instanceof Spanned)
		{
            Spanned spanned = (Spanned) text;
            Object[] spans = spanned.getSpans(offset, offset, Object.class);
            final int length = spans.length;
            for (int i = 0; i < length; i++) 
			{
                Object span = spans[i];
                int flags = spanned.getSpanFlags(span);
                if (flags != Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) 
					return true;
            }
        }
        return false;
    }
    private void sendToSpanWatchers(int replaceStart, int replaceEnd, int nbNewChars) 
	{
        for (int i = 0; i < mSpanCount; i++) {
            int spanFlags = mSpanFlags[i];
            // This loop handles only modified (not added) spans.
            if ((spanFlags & SPAN_ADDED) != 0) continue;
            int spanStart = mSpanStarts[i];
            int spanEnd = mSpanEnds[i];
            if (spanStart > mGapStart) spanStart -= mGapLength;
            if (spanEnd > mGapStart) spanEnd -= mGapLength;
            int newReplaceEnd = replaceEnd + nbNewChars;
            boolean spanChanged = false;
            int previousSpanStart = spanStart;
            if (spanStart > newReplaceEnd) {
                if (nbNewChars != 0) {
                    previousSpanStart -= nbNewChars;
                    spanChanged = true;
                }
            } else if (spanStart >= replaceStart) {
                // No change if span start was already at replace interval boundaries before replace
                if ((spanStart != replaceStart ||
					((spanFlags & SPAN_START_AT_START) != SPAN_START_AT_START)) &&
					(spanStart != newReplaceEnd ||
					((spanFlags & SPAN_START_AT_END) != SPAN_START_AT_END))) {
                    // TODO A correct previousSpanStart cannot be computed at this point.
                    // It would require to save all the previous spans' positions before the replace
                    // Using an invalid -1 value to convey this would break the broacast range
                    spanChanged = true;
                }
            }
            int previousSpanEnd = spanEnd;
            if (spanEnd > newReplaceEnd) {
                if (nbNewChars != 0) {
                    previousSpanEnd -= nbNewChars;
                    spanChanged = true;
                }
            } else if (spanEnd >= replaceStart) {
                // No change if span start was already at replace interval boundaries before replace
                if ((spanEnd != replaceStart ||
					((spanFlags & SPAN_END_AT_START) != SPAN_END_AT_START)) &&
					(spanEnd != newReplaceEnd ||
					((spanFlags & SPAN_END_AT_END) != SPAN_END_AT_END))) {
                    // TODO same as above for previousSpanEnd
                    spanChanged = true;
                }
            }
            if (spanChanged) {
                sendSpanChanged(mSpans[i], previousSpanStart, previousSpanEnd, spanStart, spanEnd);
            }
            mSpanFlags[i] &= ~SPAN_START_END_MASK;
        }
        // Handle added spans
        for (int i = 0; i < mSpanCount; i++) {
            int spanFlags = mSpanFlags[i];
            if ((spanFlags & SPAN_ADDED) != 0) {
                mSpanFlags[i] &= ~SPAN_ADDED;
                int spanStart = mSpanStarts[i];
                int spanEnd = mSpanEnds[i];
                if (spanStart > mGapStart) spanStart -= mGapLength;
                if (spanEnd > mGapStart) spanEnd -= mGapLength;
                sendSpanAdded(mSpans[i], spanStart, spanEnd);
            }
        }
    }
    /**
     * Mark the specified range of text with the specified object.
     * The flags determine how the span will behave when text is
     * inserted at the start or end of the span's range.
     */
    public void setSpan(Object what, int start, int end, int flags) {
        setSpan(true, what, start, end, flags, true/*enforceParagraph*/);
    }
    // Note: if send is false, then it is the caller's responsibility to restore
    // invariants. If send is false and the span already exists, then this method
    // will not change the index of any spans.
    private void setSpan(boolean send, Object what, int start, int end, int flags, boolean enforceParagraph)
	{
        checkRange("setSpan", start, end);
        int flagsStart = (flags & START_MASK) >> START_SHIFT;
        if (isInvalidParagraph(start, flagsStart)) {
            if (!enforceParagraph) {
                // do not set the span
                return;
            }
            throw new RuntimeException("PARAGRAPH span must start at paragraph boundary"
									   + " (" + start + " follows " + charAt(start - 1) + ")");
        }
        int flagsEnd = flags & END_MASK;
        if (isInvalidParagraph(end, flagsEnd)) {
            if (!enforceParagraph) {
                // do not set the span
                return;
            }
            throw new RuntimeException("PARAGRAPH span must end at paragraph boundary"
									   + " (" + end + " follows " + charAt(end - 1) + ")");
        }
        // 0-length Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        if (flagsStart == POINT && flagsEnd == MARK && start == end) {
            if (send) {
                Log.e(TAG, "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length");
            }
            // Silently ignore invalid spans when they are created from this class.
            // This avoids the duplication of the above test code before all the
            // calls to setSpan that are done in this class
            return;
        }
        int nstart = start;
        int nend = end;
        if (start > mGapStart) {
            start += mGapLength;
        } else if (start == mGapStart) {
            if (flagsStart == POINT || (flagsStart == PARAGRAPH && start == length()))
                start += mGapLength;
        }
        if (end > mGapStart) {
            end += mGapLength;
        } else if (end == mGapStart) {
            if (flagsEnd == POINT || (flagsEnd == PARAGRAPH && end == length()))
                end += mGapLength;
        }
        if (mIndexOfSpan != null) 
		{
			//如果已有该span，则修改它的范围和flags
            Integer index = mIndexOfSpan.get(what);
            if (index != null)
			{
                int i = index;
                int ostart = mSpanStarts[i];
                int oend = mSpanEnds[i];
                if (ostart > mGapStart)
                    ostart -= mGapLength;
                if (oend > mGapStart)
                    oend -= mGapLength;
                mSpanStarts[i] = start;
                mSpanEnds[i] = end;
                mSpanFlags[i] = flags;
                if (send) {
					//是否要立刻修正index在map中的位置错误，或等待以后一并修正
                    restoreInvariants();
                    sendSpanChanged(what, ostart, oend, nstart, nend);
                }
                return;
            }
        }
		
		//如果没有该span，就添加一个span
        mSpans = GrowingArrayUtils.append(mSpans, mSpanCount, what);
        mSpanStarts = GrowingArrayUtils.append(mSpanStarts, mSpanCount, start);
        mSpanEnds = GrowingArrayUtils.append(mSpanEnds, mSpanCount, end);
        mSpanFlags = GrowingArrayUtils.append(mSpanFlags, mSpanCount, flags);
        mSpanOrder = GrowingArrayUtils.append(mSpanOrder, mSpanCount, mSpanInsertCount);
        invalidateIndex(mSpanCount);
        mSpanCount++;
        mSpanInsertCount++;
     
		//确保有足够的空间容纳空的内部节点
		//这个神奇的公式计算出最小的完美二叉树的大小，可能大于mSpanCount
		int sizeOfMax = 2 * treeRoot() + 1;
        if (mSpanMax.length < sizeOfMax) {
            mSpanMax = new int[sizeOfMax];
        }
        if (send) {
            restoreInvariants();
            sendSpanAdded(what, nstart, nend);
        }
    }
    private boolean isInvalidParagraph(int index, int flag) {
        return flag == PARAGRAPH && index != 0 && index != length() && charAt(index - 1) != '\n';
    }
    /**
     * Remove the specified markup object from the buffer.
     */
    public void removeSpan(Object what) {
        removeSpan(what, 0 /* flags */);
    }
    /**
     * Remove the specified markup object from the buffer.
     *
     * @hide
     */
    public void removeSpan(Object what, int flags)
	{
        if (mIndexOfSpan == null) return;
        Integer i = mIndexOfSpan.remove(what);
        if (i != null) {
            removeSpan(i.intValue(), flags);
        }
    }
    /**
     * Return externally visible offset given offset into gapped buffer.
     */
    private int resolveGap(int i) {
        return i > mGapStart ? i - mGapLength : i;
    }
    /**
     * Return the buffer offset of the beginning of the specified
     * markup object, or -1 if it is not attached to this buffer.
     */
    public int getSpanStart(Object what) 
	{
        if (mIndexOfSpan == null) return -1;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? -1 : resolveGap(mSpanStarts[i]);
    }
    /**
     * Return the buffer offset of the end of the specified
     * markup object, or -1 if it is not attached to this buffer.
     */
    public int getSpanEnd(Object what)
	{
        if (mIndexOfSpan == null) return -1;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? -1 : resolveGap(mSpanEnds[i]);
    }
    /**
     * Return the flags of the end of the specified
     * markup object, or 0 if it is not attached to this buffer.
     */
    public int getSpanFlags(Object what) 
	{
        if (mIndexOfSpan == null) return 0;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? 0 : mSpanFlags[i];
    }
    /**
     * Return an array of the spans of the specified type that overlap
     * the specified range of the buffer.  The kind may be Object.class to get
     * a list of all the spans regardless of type.
     */
    @SuppressWarnings("unchecked")
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        return getSpans(queryStart, queryEnd, kind, true);
    }
    /**
     * Return an array of the spans of the specified type that overlap
     * the specified range of the buffer.  The kind may be Object.class to get
     * a list of all the spans regardless of type.
     *
     * @param queryStart Start index.
     * @param queryEnd End index.
     * @param kind Class type to search for.
     * @param sortByInsertionOrder If true the results are sorted by the insertion order.
     * @param <T>
     * @return Array of the spans. Empty array if no results are found.
     *
     * @hide
     */
    public <T> T[] getSpans(int queryStart, int queryEnd,  Class<T> kind, boolean sortByInsertionOrder) 
	{
        if (kind == null || mSpanCount == 0) return (T[]) Array.newInstance(kind,0);
        
		int count = countSpans(queryStart, queryEnd, kind, treeRoot());
        if (count == 0) {
            return (T[])Array.newInstance(kind,0);
        }
		
        T[] ret = (T[]) Array.newInstance(kind, count);
        final int[] prioSortBuffer = sortByInsertionOrder ? obtain(count) : new int[0];
        final int[] orderSortBuffer = sortByInsertionOrder ? obtain(count) : new int[0];
        getSpansRec(queryStart, queryEnd, kind, treeRoot(), ret, prioSortBuffer,
					orderSortBuffer, 0, sortByInsertionOrder);
        if (sortByInsertionOrder) {
            sort(ret, prioSortBuffer, orderSortBuffer);
            recycle(prioSortBuffer);
            recycle(orderSortBuffer);
        }
        return ret;
    }
    private int countSpans(int queryStart, int queryEnd, Class kind, int i) 
	{
        int count = 0;
        if ((i & 1) != 0) {
            // internal tree node
            int left = leftChild(i);
            int spanMax = mSpanMax[left];
            if (spanMax > mGapStart) {
                spanMax -= mGapLength;
            }
            if (spanMax >= queryStart) {
                count = countSpans(queryStart, queryEnd, kind, left);
            }
        }
        if (i < mSpanCount) {
            int spanStart = mSpanStarts[i];
            if (spanStart > mGapStart) {
                spanStart -= mGapLength;
            }
            if (spanStart <= queryEnd) {
                int spanEnd = mSpanEnds[i];
                if (spanEnd > mGapStart) {
                    spanEnd -= mGapLength;
                }
                if (spanEnd >= queryStart &&
                    (spanStart == spanEnd || queryStart == queryEnd ||
					(spanStart != queryEnd && spanEnd != queryStart)) &&
					(Object.class == kind || kind.isInstance(mSpans[i]))) {
                    count++;
                }
                if ((i & 1) != 0) {
                    count += countSpans(queryStart, queryEnd, kind, rightChild(i));
                }
            }
        }
        return count;
    }
	
    /** * 使用当前区间树节点下找到的跨度填充结果数组。 * 
	* @param querystart 间隔查询的起始索引。 
	* @Param QueryEnd 间隔查询的结束索引。
	* @param kind 类类型进行搜索。
	* @param i 当前树节点的索引。 
	* @param ret 数组将被填充结果。
	* @param priority buffer 记录找到的跨度优先级。
	* @param insertionOrder 缓冲区记录找到的跨度的插入顺序。
	* @param count 找到的跨度数。
	* @param sort flag 填充优先级和插入顺序缓冲区。 
	如果 false 则 * 具有优先级标志的跨度将在结果数组中进行排序。 
	* @param <t> * @return 找到的跨度总数。 
	*/
    @SuppressWarnings("unchecked")
    private <T> int getSpansRec(int queryStart, int queryEnd, Class<T> kind, int i, T[] ret, int[] priority, int[] insertionOrder, int count, boolean sort)
	{
        if ((i & 1) != 0) {
            //内部树节点
            int left = leftChild(i);
            int spanMax = mSpanMax[left];
            if (spanMax > mGapStart) {
                spanMax -= mGapLength;
            }
            if (spanMax >= queryStart) {
                count = getSpansRec(queryStart, queryEnd, kind, left, ret, priority,
									insertionOrder, count, sort);
            }
        }
        if (i >= mSpanCount) return count;
        int spanStart = mSpanStarts[i];
        if (spanStart > mGapStart) {
            spanStart -= mGapLength;
        }
        if (spanStart <= queryEnd) {
            int spanEnd = mSpanEnds[i];
            if (spanEnd > mGapStart) {
                spanEnd -= mGapLength;
            }
            if (spanEnd >= queryStart &&
				(spanStart == spanEnd || queryStart == queryEnd ||
				(spanStart != queryEnd && spanEnd != queryStart)) &&
				(Object.class == kind || kind.isInstance(mSpans[i]))) {
                int spanPriority = mSpanFlags[i] & SPAN_PRIORITY;
                int target = count;
                if (sort) {
                    priority[target] = spanPriority;
                    insertionOrder[target] = mSpanOrder[i];
                } else if (spanPriority != 0) {
                    //insertion sort for elements with priority
                    int j = 0;
                    for (; j < count; j++) {
                        int p = getSpanFlags(ret[j]) & SPAN_PRIORITY;
                        if (spanPriority > p) break;
                    }
                    System.arraycopy(ret, j, ret, j + 1, count - j);
                    target = j;
                }
                ret[target] = (T) mSpans[i];
                count++;
            }
            if (count < ret.length && (i & 1) != 0) {
                count = getSpansRec(queryStart, queryEnd, kind, rightChild(i), ret, priority,
									insertionOrder, count, sort);
            }
        }
        return count;
    }
	
    /** *获取临时排序缓冲区。* 
	* @param elementCount要返回的int[]的大小
	* @返回一个长度为elementCount的int[]
	*/
    private static int[] obtain(final int elementCount)
	{
        int[] result = null;
        synchronized (sCachedIntBuffer) {
            // try finding a tmp buffer with length of at least elementCount
            // if not get the first available one
            int candidateIndex = -1;
            for (int i = sCachedIntBuffer.length - 1; i >= 0; i--)
			{
                if (sCachedIntBuffer[i] != null) {
                    if (sCachedIntBuffer[i].length >= elementCount) {
                        candidateIndex = i;
                        break;
                    } else if (candidateIndex == -1) {
                        candidateIndex = i;
                    }
                }
            }
            if (candidateIndex != -1) {
                result = sCachedIntBuffer[candidateIndex];
                sCachedIntBuffer[candidateIndex] = null;
            }
        }
        result = checkSortBuffer(result, elementCount);
        return result;
    }
    /**
     * Recycle sort buffer.
     *
     * @param buffer buffer to be recycled
     */
    private static void recycle(int[] buffer)
	{
        synchronized (sCachedIntBuffer) {
            for (int i = 0; i < sCachedIntBuffer.length; i++) {
                if (sCachedIntBuffer[i] == null || buffer.length > sCachedIntBuffer[i].length) {
                    sCachedIntBuffer[i] = buffer;
                    break;
                }
            }
        }
    }
	/** *检查缓冲区的大小，并根据需要进行扩展。* 
	* @param buffer要检查的缓冲区。
	* @param size所需的大小。
	* @如果当前大小大于所需大小，则返回相同的缓冲区实例。
	否则，将创建并返回一个*新实例。
	*/
    private static int[] checkSortBuffer(int[] buffer, int size)
	{
        if (buffer == null || size > buffer.length) {
            return ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(size));
        }
        return buffer;
    }
	
    /** *迭代堆排序实现。它将首先按照优先级*然后按照插入顺序对跨度进行排序。
	优先级较高的范围将在优先级较低的范围之前。
	如果优先级相同，跨度将按照插入顺序排序。
	具有较低插入顺序的*范围将在具有较高插入顺序的范围之前。*
	* @param array跨度要排序的数组。
	* @ param priority的优先级
	* @ param insertionOrder对象类型的插入顺序。
	* @param <T> 
	*/
    private final <T> void sort(T[] array, int[] priority, int[] insertionOrder) 
	{
        int size = array.length;
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i, array, size, priority, insertionOrder);
        }
        for (int i = size - 1; i > 0; i--) 
		{
            final T tmpSpan =  array[0];
            array[0] = array[i];
            array[i] = tmpSpan;
            final int tmpPriority =  priority[0];
            priority[0] = priority[i];
            priority[i] = tmpPriority;
            final int tmpOrder =  insertionOrder[0];
            insertionOrder[0] = insertionOrder[i];
            insertionOrder[i] = tmpOrder;
            siftDown(0, array, i, priority, insertionOrder);
        }
    }
    
	/** *堆排序的帮助函数。*
	* @param index向下筛选的元素的索引。
	* @param array Span要排序的数组。
	* @param size当前堆大小。
	* @ param priority的优先级。
	* @ param insertionOrder对象类型的插入顺序。
	*/
    private final <T> void siftDown(int index, T[] array, int size, int[] priority, int[] insertionOrder) 
	{
        int left = 2 * index + 1;
        while (left < size)
		{
            if (left < size - 1 && compareSpans(left, left + 1, priority, insertionOrder) < 0) {
                left++;
            }
            if (compareSpans(index, left, priority, insertionOrder) >= 0) {
                break;
            }
            final T tmpSpan =  array[index];
            array[index] = array[left];
            array[left] = tmpSpan;
            final int tmpPriority =  priority[index];
            priority[index] = priority[left];
            priority[left] = tmpPriority;
            final int tmpOrder =  insertionOrder[index];
            insertionOrder[index] = insertionOrder[left];
            insertionOrder[left] = tmpOrder;
            index = left;
            left = 2 * index + 1;
        }
    }
	
	/** *比较数组中的两个span元素。比较首先基于区间的优先级标志，然后是区间的插入顺序。*
	* @param要比较的元素的左索引。
	* @param要比较的其他元素的右索引。
	* @param优先级范围的优先级
	* @param insertionOrder范围的插入顺序
	* @return
	*/
    private final int compareSpans(int left, int right, int[] priority, int[] insertionOrder)
	{
        int priority1 = priority[left];
        int priority2 = priority[right];
        if (priority1 == priority2) {
            return Integer.compare(insertionOrder[left], insertionOrder[right]);
        }
        // since high priority has to be before a lower priority, the arguments to compare are
        // opposite of the insertion order check.
        return Integer.compare(priority2, priority1);
    }
    /**
     * Return the next offset after <code>start</code> but less than or
     * equal to <code>limit</code> where a span of the specified type
     * begins or ends.
     */
    public int nextSpanTransition(int start, int limit, Class kind)
	{
        if (mSpanCount == 0) return limit;
        if (kind == null) {
            kind = Object.class;
        }
        return nextSpanTransitionRec(start, limit, kind, treeRoot());
    }
    private int nextSpanTransitionRec(int start, int limit, Class kind, int i) 
	{
        if ((i & 1) != 0) {
            // internal tree node
            int left = leftChild(i);
            if (resolveGap(mSpanMax[left]) > start) {
                limit = nextSpanTransitionRec(start, limit, kind, left);
            }
        }
        if (i < mSpanCount) {
            int st = resolveGap(mSpanStarts[i]);
            int en = resolveGap(mSpanEnds[i]);
            if (st > start && st < limit && kind.isInstance(mSpans[i]))
                limit = st;
            if (en > start && en < limit && kind.isInstance(mSpans[i]))
                limit = en;
            if (st < limit && (i & 1) != 0) {
                limit = nextSpanTransitionRec(start, limit, kind, rightChild(i));
            }
        }
        return limit;
    }
    /**
     * Return a new CharSequence containing a copy of the specified
     * range of this buffer, including the overlapping spans.
     */
    public CharSequence subSequence(int start, int end) {
        return new SpannableStringBuilder(this, start, end);
    }
    /**
     * Copy the specified range of chars from this buffer into the
     * specified array, beginning at the specified offset.
     */
    public void getChars(int start, int end, char[] dest, int destoff)
	{
        checkRange("getChars", start, end);
        if (end <= mGapStart) {
            System.arraycopy(mText, start, dest, destoff, end - start);
        } else if (start >= mGapStart) {
            System.arraycopy(mText, start + mGapLength, dest, destoff, end - start);
        } else {
            System.arraycopy(mText, start, dest, destoff, mGapStart - start);
            System.arraycopy(mText, mGapStart + mGapLength,
							 dest, destoff + (mGapStart - start),
							 end - mGapStart);
        }
    }
    /**
     * Return a String containing a copy of the chars in this buffer.
     */
    @Override
    public String toString()
	{
        int len = length();
        char[] buf = new char[len];
        getChars(0, len, buf, 0);
        return new String(buf);
    }
    /**
     * Return a String containing a copy of the chars in this buffer, limited to the
     * [start, end[ range.
     * @hide
     */
    public String substring(int start, int end)
	{
        char[] buf = new char[end - start];
        getChars(start, end, buf, 0);
        return new String(buf);
    }
    /**
     * Returns the depth of TextWatcher callbacks. Returns 0 when the object is not handling
     * TextWatchers. A return value greater than 1 implies that a TextWatcher caused a change that
     * recursively triggered a TextWatcher.
     */
    public int getTextWatcherDepth() {
        return mTextWatcherDepth;
    }
    private void sendBeforeTextChanged(TextWatcher[] watchers, int start, int before, int after) {
        int n = watchers.length;
        mTextWatcherDepth++;
        for (int i = 0; i < n; i++) {
            watchers[i].beforeTextChanged(this, start, before, after);
        }
        mTextWatcherDepth--;
    }
    private void sendTextChanged(TextWatcher[] watchers, int start, int before, int after) {
        int n = watchers.length;
        mTextWatcherDepth++;
        for (int i = 0; i < n; i++) {
            watchers[i].onTextChanged(this, start, before, after);
        }
        mTextWatcherDepth--;
    }
    private void sendAfterTextChanged(TextWatcher[] watchers) {
        int n = watchers.length;
        mTextWatcherDepth++;
        for (int i = 0; i < n; i++) {
            watchers[i].afterTextChanged(this);
        }
        mTextWatcherDepth--;
    }
    private void sendSpanAdded(Object what, int start, int end) {
        SpanWatcher[] recip = getSpans(start, end, SpanWatcher.class);
        int n = recip.length;
        for (int i = 0; i < n; i++) {
            recip[i].onSpanAdded(this, what, start, end);
        }
    }
    private void sendSpanRemoved(Object what, int start, int end) {
		// The bounds of a possible SpanWatcher are guaranteed to be set before this method is
        // called, so that the order of the span does not affect this broadcast.
        
		SpanWatcher[] recip = getSpans(start, end, SpanWatcher.class);
        int n = recip.length;
        for (int i = 0; i < n; i++) {
            recip[i].onSpanRemoved(this, what, start, end);
        }
    }
    private void sendSpanChanged(Object what, int oldStart, int oldEnd, int start, int end) {
       SpanWatcher[] spanWatchers = getSpans(Math.min(oldStart, start),
											  Math.min(Math.max(oldEnd, end), length()), SpanWatcher.class);
        int n = spanWatchers.length;
        for (int i = 0; i < n; i++) {
            spanWatchers[i].onSpanChanged(this, what, oldStart, oldEnd, start, end);
        }
    }
    private static String region(int start, int end) {
        return "(" + start + " ... " + end + ")";
    }
    private void checkRange(final String operation, int start, int end) {
        if (end < start) {
            throw new IndexOutOfBoundsException(operation + " " +
												region(start, end) + " has end before start");
        }
        int len = length();
        if (start > len || end > len) {
            throw new IndexOutOfBoundsException(operation + " " +
												region(start, end) + " ends beyond length " + len);
        }
	}
	
    public void setFilters(InputFilter[] filters)
	{
        if (filters == null) {
            throw new IllegalArgumentException();
        }
        mFilters = filters;
    }
    public InputFilter[] getFilters() {
        return mFilters;
    }
  
	//树的基本术语:
	//Tree 树是由结点和边组成的且不存在着任何环的一种数据结构
	//Node 结点，结点是组成树的每一个元素
	//Root 根，树的顶端结点
	//Child 孩子，一个结点直接指向的下一个结点称为该结点的孩子
	//Parent 父亲，若一个结点被指向，那么直接指向它的上个节点被称为它的父亲
	//Siblings 兄弟，具有同一个父亲(Parent)的孩子(Child)之间互称为兄弟(Sibling)
	//Ancestor 祖先，结点的祖先(Ancestor)是从根（Root）到该结点所经分支(Branch)上的所有结点
	//Descendant 子孙，反之，以某结点为根的子树中的任一结点都称为该结点的子孙(Ancestor)
	//Leaf 叶子（终端结点）没有孩子的结点(也就是度为0的结点)称为叶子(Leaf)或终端结点
	//Branch 分支(非终端结点) 至少有一个孩子的结点称为分支(Branch)或非终端结点
	//Degree 度，结点所拥有的子树个数称为结点的度(Degree)
	//Edge 边，一个结点和另一个结点之间的连接被称之为边
	//Path 路径，连接结点和其后代的结点之间的(结点,边)的序列
	//Level 层次，结点的层次(Level)从根(Root)开始定义起，根为第0层，根的孩子为第1层。以此类推，若某结点在第i层，那么其子树的根就在第i+1层。
	//Height of node 结点的高度是该结点和某个叶子之间存在的最长路径上的边的个数
	//Height of tree 树的高度是其根结点的高度
	//Depth of node 结点的 深度 是从树的根结点到该结点的边的个数。（注：树的深度指的是树中结点的最大层次。）
	//Forest 森林是n(>=0)棵互不相交的树的集合
	
	//若将顺序排列的数组无限二分，可构成一颗二叉树，而二叉树的根节点必然在数组中间，从根节点分发出来的左子节点和右子节点便是二分的结果
	/*
	  例如一列数 0，1，2，3，4，5，6
	  若表示为二叉树则是如下的结果:
	            3
			  ↙  ↘
			1        5
		  ↙  ↘    ↙  ↘
		 0     2   4     6
	*/
	
	//这里将跨度列表视为二叉树的跨度(以及开始和结束偏移量和标志)存储在按开始偏移量排序的线性数组中
	//为了快速搜索，这些数组采用了二分搜索法结构，这种结构是对一棵完美二叉树的有序遍历，这是一种有点不寻常但很有利的方法
	//包含值的节点的索引为0 <= i < n(其中n = mSpanCount)，从而将访问值的逻辑保留为连续数组
	//其他平衡二叉树方法(例如完全二叉树)需要对节点索引进行一些洗牌
	
	//这个结构的基本性质:
	//整颗树像一个等腰三角形
	//对于一棵高度为m的完美二叉树，树有2^(m+1) - 1个总节点，树根的索引是2^m - 1
	//所有叶子节点的索引都是偶数，所有内部节点的索引都是奇数
	//索引i的一个节点的高度是i的二进制表示中尾部1的个数
	//高度为h的节点i的左子节点是i - 2^(h - 1)
	//高度为h的节点i的右子节点是i + 2^(h - 1)
	//任意节点的左孑节点都小于它，右子节点都大于它
	
	//获取数组中间数，此下标是二叉树根节点的下标
	private int treeRoot() {
        return Integer.highestOneBit(mSpanCount) - 1;
    }
	//获取下标为i的节点的左子节点在数组中的下标
    private static int leftChild(int i) {
		// (i+1) & ~i 等同于 2^(i中尾随1的个数)，即2^h
		// 则(((i + 1) & ~i) >> 1) 等同于 (2^h)/2 = 2^(h-1)
        return i - (((i + 1) & ~i) >> 1);
    }
	//获取下标为i的节点的右子节点在数组中的下标
    private static int rightChild(int i) {
        return i + (((i + 1) & ~i) >> 1);
    }
	
	//span数组还增加了mSpanMax[]数组，该数组表示上述二叉树结构上的区间树
	//对于每个节点，mSpanMax[]数组包含该节点及其后代的mSpanEnds的最大值
	//因此，遍历可以轻松地拒绝不包含与感兴趣区域重叠的跨度的子树
	//请注意，mSpanMax[]对于索引> = n的内部节点也有有效值，但这些节点有索引< n的后代
	//在这些情况下，它只表示其后代的最大跨度端，这是完美二叉树结构的结果
  
	//注意，此函数总是递归更新节点i及其之下的所有子节点的mSpanMax值
	//注意，对于任意n，此树的内部节点可能> = n，因此，节点i的递归遍历的一般结构是:
	//若i不是叶子节点，则计算所有左侧子节点的最大值
	//若i在有效范围内，则计算自己的最大值
	//若i不是叶子节点，则计算所有右侧子节点的最大值，并且右子节点的值比它大
	private int calcMax(int i)
	{
        int max = 0;
        if ((i & 1) != 0) {
            //若i不是叶子节点，则计算左子节点的最大值
            max = calcMax(leftChild(i));
        }
        if (i < mSpanCount) 
		{
			//若i在有效范围内，则计算自己的最大值
            max = Math.max(max, mSpanEnds[i]);
            if ((i & 1) != 0){
				//若i不是叶子节点，则计算右子节点的最大值，并且右子节点的值比它大
                max = Math.max(max, calcMax(rightChild(i)));
            }
        }
		//设置自己的最大值，并返回
        mSpanMax[i] = max;
        return max;
    }
	
    //在跨度结构的任何突变后恢复二元区间树不变量
	private void restoreInvariants() 
	{
        if (mSpanCount == 0) return;
		
		//不变1: span starts按顺序排列
		//这是一个简单的插入排序，因为我们希望它大部分已被排序
		//每次向后从数组中拿出一个元素，并与其之前的元素比较，直到找到一个正确的位置，将其插入这里，这样每次排序之后，在i之前的内容都是排好序的	
        for (int i = 1; i < mSpanCount; i++) 
		{
			//如果当前元素比前面的元素更小，主动进行本次排序
			//注意，i之前的元素必然按顺序排列，因此只用与i-1比较就知道需不需要排序
			if (mSpanStarts[i] < mSpanStarts[i - 1])
			{
                Object span = mSpans[i];
                int start = mSpanStarts[i];
                int end = mSpanEnds[i];
                int flags = mSpanFlags[i];
                int insertionOrder = mSpanOrder[i];
                int j = i;
             
				//将j之前的元素与其比较，直到找到一个比它还小的元素才停止，并将其插入到这里
				do {
					//由于此span必然在j之前，所以最后需要将其插入到正确位置，并还需要之前的元素顺序不变
					//因此只要还没有找到位置，那么j-1位置的元素就要向后挪一位，空出位置
                    mSpans[j] = mSpans[j - 1];
                    mSpanStarts[j] = mSpanStarts[j - 1];
                    mSpanEnds[j] = mSpanEnds[j - 1];
                    mSpanFlags[j] = mSpanFlags[j - 1];
                    mSpanOrder[j] = mSpanOrder[j - 1];
                    j--;
					//之后判断j-2位置的元素是否小于start，如果是就break
                } while (j > 0 && start < mSpanStarts[j - 1]);
                
				//最后将span插入这里
				mSpans[j] = span;
                mSpanStarts[j] = start;
                mSpanEnds[j] = end;
                mSpanFlags[j] = flags;
                mSpanOrder[j] = insertionOrder;
                invalidateIndex(j);
            }
        }
		
        //不变量2: 使max是每个节点及其后代的最大跨度端点
		//从根节点开始，修正所有子节点的max值
        calcMax(treeRoot());
       
		//不变量3: mIndexOfSpan映射的索引修正
        if (mIndexOfSpan == null) {
            mIndexOfSpan = new IdentityHashMap<Object, Integer>();
        }
		//从被修改的下标开始，遍历之后的span，将span与正确的index重新绑定
        for (int i = mLowWaterMark; i < mSpanCount; i++) 
		{
			//每次从mIndexOfSpan拿出当前span的index，若没有此span或index是错误的，重新放入正确的span和index
            Integer existing = mIndexOfSpan.get(mSpans[i]);
            if (existing == null || existing != i) {
                mIndexOfSpan.put(mSpans[i], i);
            }
        }
		//修改完后mLowWaterMark置为无限大，意为暂时不用再恢复了
        mLowWaterMark = Integer.MAX_VALUE;
    }
    //对mSpans的任何更新调用此函数，以便mIndexOfSpan可以被更新
    private void invalidateIndex(int i) {
		//更新mLowWaterMark的值，表示此之前的span没有刷新
        mLowWaterMark = Math.min(i, mLowWaterMark);
    }
   
	private static final InputFilter[] NO_FILTERS = new InputFilter[0];
    private static final int[][] sCachedIntBuffer = new int[6][0];
	private InputFilter[] mFilters = NO_FILTERS;
	
	private char[] mText;
    private int mGapStart;
    private int mGapLength;
  
	private Object[] mSpans;
    private int[] mSpanStarts;
    private int[] mSpanEnds;
    private int[] mSpanMax;  //存储此节点及其子节点最大的范围
    private int[] mSpanFlags;
    private int[] mSpanOrder;  //存储跨度插入的顺序
    
	private int mSpanInsertCount;  //跨度插入计数器
    private int mSpanCount;
    private IdentityHashMap<Object, Integer> mIndexOfSpan;
 
	private int mLowWaterMark;  //在此之前的索引没有被触及
	
	//TextWatcher回调可能会触发更改，从而触发更多回调，这记录了回调的深度，以防死循环
    private int mTextWatcherDepth;

    // TODO These value are tightly related to the public SPAN_MARK/POINT values in {@link Spanned}
    private static final int MARK = 1;
    private static final int POINT = 2;
    private static final int PARAGRAPH = 3;
    private static final int START_MASK = 0xF0;
    private static final int END_MASK = 0x0F;
    private static final int START_SHIFT = 4;
 
	// These bits are not (currently) used by SPANNED flags
    private static final int SPAN_ADDED = 0x800;
    private static final int SPAN_START_AT_START = 0x1000;
    private static final int SPAN_START_AT_END = 0x2000;
    private static final int SPAN_END_AT_START = 0x4000;
    private static final int SPAN_END_AT_END = 0x8000;
    private static final int SPAN_START_END_MASK = 0xF000;
	
}
