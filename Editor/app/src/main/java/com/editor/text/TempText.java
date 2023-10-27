package com.editor.text;

public class TempText extends SpannableStringBuilderLite implements DisposableText
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
