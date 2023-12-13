package com.editor.text2.builder.listener;

import android.text.*;
import com.editor.text2.builder.listener.baselistener.*;
import com.editor.text2.base.share3.*;


public abstract class myEditFormatorListener extends myEditListener implements EditFormatorListener
{
	/* 把editor指定范围内的want替换为to */
	final public static void replaceAll(final int start, final int end, Editable editor, CharSequence want, CharSequence to)
	{
		final int wantLen = want.length();
		final int toLen = to.length();
		char[] ts = new char[end-start];
		editor.getChars(start, end, ts, 0);
		
		char[] ps = new char[wantLen];
		TextUtils.getChars(want, 0, wantLen, ps, 0);
		int[] psNext = StringChecker.getNext(ps);
		
		int tsStart = 0;
		int tsOffset = 0;
		while (tsStart+start < end)
		{
			final int tsAfter = StringChecker.KMP(ts, ps, psNext, tsStart);
			if(tsAfter < 0){
				break;
			}
			editor.replace(tsAfter+start+tsOffset, tsAfter+start+tsOffset+wantLen, to, 0, toLen);	
			tsStart = tsAfter+wantLen;
			tsOffset += toLen-wantLen;
		}
	}
}
