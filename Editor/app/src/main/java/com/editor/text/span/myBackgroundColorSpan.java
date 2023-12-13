package com.editor.text.span;

import android.graphics.*;
import android.text.*;

public class myBackgroundColorSpan implements BackgroundSpanX
{
	private final int mColor;
	private int mSaveColor;

	public myBackgroundColorSpan(int color){
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

	public int getBackgroundColor(){
		return mColor;
	}

	@Override
	public boolean equals(Object obj){
		return obj instanceof myBackgroundColorSpan && mColor == ((myBackgroundColorSpan)obj).mColor;
	}

	@Override
	public int hashCode(){
		return mColor;
	}

	@Override
	public void draw(float left, float top, float right, float bottom, Canvas canvas, TextPaint paint){
		canvas.drawRect(left,top,right,bottom,paint);
	}
}
