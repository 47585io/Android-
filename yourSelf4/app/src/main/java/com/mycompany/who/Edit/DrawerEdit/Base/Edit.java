package com.mycompany.who.Edit.DrawerEdit.Base;
import android.widget.*;
import android.content.*;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import android.graphics.*;
import android.text.method.*;
import android.util.*;
import java.lang.reflect.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.inputmethod.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;

public class Edit extends EditText
{
	protected static KeyListener listener;
	public static EPool2 Ep;
	public static int Selected_Color=0x75515a6b;
	public static int Background_Color=0;
	public static int Text_Color=0xffabb2bf;
	public float Text_Size=14;
	
	static{
		Ep=new EPool2();
	}
	
	public Edit(Context cont){
		super(cont);
		listener = new EditText(getContext()).getKeyListener();
		config();
	}
	public Edit(Context cont,Edit Edit)
	{
		super(cont);
		Text_Size=Edit.Text_Size;
		config();
	}
	
	public void config(){	
		setTextColor(Text_Color);
		setBackgroundResource(0);
		setTypeface(Typeface.MONOSPACE);
		setHighlightColor(Selected_Color);
		setTextSize(Text_Size);
		setLetterSpacing(0.01f);
		setLineSpacing(0.2f,1.2f);
		setPadding(0,0,0,0);
	}
	public int getLineCount(){
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		if(indexs==null)
			return 0;
		return indexs.size()+1;
	}
	
	
	final public int getLineStart(int lineCount){
		if(lineCount==1)
			return 0;
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		return indexs.get(lineCount -2);
	}

	final public wordIndex subLines(int startLine){
		wordIndex j = new wordIndex(0,0,(byte)0);
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		if(indexs==null)
			return j;
		j.start= indexs.get(startLine-1)+1;
		j.end=getText().toString().length();
		return j;
	}
	final public wordIndex subLines(int startLine,int endLine){
		wordIndex j = new wordIndex(0,0,(byte)0);
		List<Integer> indexs = String_Splitor.indexsOf('\n',getText().toString());
		if(indexs==null)
			return j;
		j.start= indexs.get(startLine-1)+1;
		j.end=indexs.get(endLine-1)+1;
		return j;
	}
	
	final public void lockSelf(boolean is){
		if(is)
			setKeyListener(null);
		else
			setKeyListener(listener);
	}
	
	final public static void setCursorDrawableColor(EditText editText, int color)
	{
        try
		{
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");//获取这个字段
            fCursorDrawableRes.setAccessible(true);//代表这个字段、方法等等可以被访问
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);

            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);

            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);

            Drawable[] drawables = new Drawable[2];
            drawables[0]=editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[1]=editText.getContext().getResources().getDrawable(mCursorDrawableRes);
            drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);//SRC_IN 上下层都显示。下层居上显示。
            drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
            fCursorDrawable.set(editor, drawables);
        }
		catch (Throwable ignored){}
    }

	final public static void closeInputor(Context context, View editText)
	{
		editText.requestFocus();//请求焦点
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	final public static void openInputor(Context context, View editText)
	{
		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, 0);
	}

	
	
	public static class EPool2 extends EPool<wordIndex>
	{

		@Override
		public wordIndex creat()
		{
			return new wordIndex();
		}
		
		@Override
		public void resetE(wordIndex E)
		{
		}
		
	}
	
}
