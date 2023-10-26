package com.editor.text;
import android.text.*;

/* 一次性文本，在使用时可以随意修改，而不必拷贝一份，节省时间 */
public interface DisposableText extends Spannable
{
	public static class TempText extends SpannableStringBuilderLite implements DisposableText
	{
		public TempText(){
			super("");
		}		
		public TempText(CharSequence text){
			super(text);
		}
		public TempText(CharSequence text, int start, int end){
			super(text, start, end);
		}
		@Override
		public CharSequence subSequence(int start, int end){
			return new TempText(this, start, end);
		}
	}
}
