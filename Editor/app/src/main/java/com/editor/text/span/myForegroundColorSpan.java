package com.editor.text.span;
import android.text.*;

public class myForegroundColorSpan implements TextStyleSpan
{
	private final int mColor;
	private int mSaveColor;

	public myForegroundColorSpan(int color){
		mColor = color;
	}

	@Override
	public void updateDrawState(TextPaint paint){
		mSaveColor = paint.getColor();
		paint.setColor(mColor);
	}

	@Override
	public void restoreDrawState(TextPaint paint){
		paint.setColor(mSaveColor);
	}

	public int getForegroundColor(){
		return mColor;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof myForegroundColorSpan && mColor == ((myForegroundColorSpan)obj).mColor;
	}

	@Override
	public int hashCode(){
		return mColor;
	}
}
