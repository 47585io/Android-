package com.editor.text2.builder.listener;

import android.text.*;
import android.util.*;
import android.widget.*;
import com.editor.text2.builder.listener.baselistener.*;


public abstract class myEditFormatorListener extends myEditListener implements EditFormatorListener
{
	
	/* 从起始位置开始，反向把字符串中的want替换为to */
	final public static void replaceAll(int start, int end, Editable editor, String want, CharSequence to)
	{
		int len = want.length();
		int toLen = to.length();
		char[] arr = new char[end-start];
		editor.getChars(start,end,arr,0);
		String src = new String(arr);
		
		int nowIndex = start;
		while (true)
		{
			nowIndex = src.lastIndexOf(want, nowIndex-1);
			if(nowIndex==-1){
				return;
			}
			editor.replace(nowIndex+start, nowIndex+start+len, to,0,toLen);	
			
		}
	}
	
}
