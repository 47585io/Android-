package com.editor.text;

import android.graphics.*;
import android.text.*;
import com.editor.text.base.*;


/* 对文本容器进行测量的基类 */
public abstract class BaseLayout
{
	public static final char FN = '\n', FT = '\t';
	public static final float MinScacle = 0.5f, MaxScale = 2.0f;
	public static final float TextSize = 40f;
	public static final int TextColor = 0xffaaaaaa, LineColor = 0xff666666;
	public static final float LineSpcing = 1.2f, CursorWidthSpcing = 0.1f;
	
	private int mCacheLine;
	private int mCacheStart, mCacheEnd;
	
	private CharSequence mText;
	private TextPaint mPaint;
	private float mLineSpacing;
	private float mCursorWidthSpacing;
	
	protected BaseLayout(CharSequence base, TextPaint paint, float lineSpacing, float cursorSpacing)
	{
		mText = base;
		mPaint = paint;
		mLineSpacing = lineSpacing;
		mCursorWidthSpacing = cursorSpacing;
	}
	
	final public CharSequence getText(){
		return mText;
	}
    final public TextPaint getPaint(){
		return mPaint;
	}
	public void setLineSpacing(float lineSpacing){
		mLineSpacing = lineSpacing;
	}
	public void setCursorSpacing(float cursorSpacing){
		mCursorWidthSpacing = cursorSpacing;
	}
	public float getLineSpacing(){
		return mLineSpacing;
	}
	public float getCursorSpacing(){
		return mCursorWidthSpacing;
	}
	
	public abstract float maxWidth()
	
	public float getHeight(){
		return getLineCount()*getLineHeight();
	}
	
	
	/* 获取行的数量 */
	public abstract int getLineCount()
	
	/* 获取行的宽度 */
	public float getLineWidth(int line)
	{
		CharSequence text = mText;
		int start = getLineStart(line); 
		int end = tryLine_End(text,start);
		return mPaint.measureText(text,start,end);
	}
	/* 获取行的高度 */
	public float getLineHeight()
	{
		Paint.FontMetrics font = RecylePool.obtainFont();
		mPaint.getFontMetrics(font);
		float height = font.bottom-font.top;
		RecylePool.recyleFont(font);
		return height*mLineSpacing;
	}
	
	
	/* 获取行的纵坐标 */
	public float getLineTop(int p1){
		return p1*getLineHeight();
	}
	/* 获取行底的纵坐标 */
	public float getLineDescent(int p1){
		return getLineTop(p1+1);
	}
	/* 获取纵坐标指定的行 */
	public int getLineForVertical(int vertical)
	{
		int lineCount = getLineCount();
		int line = (int)(vertical/getLineHeight());
		line = line<0 ? 0 : (line>lineCount ? lineCount:line);
		return line;
	}
	

	/* 获取行的末尾下标 */
	public int getLineEnd(int line){
		int start = getLineStart(line);
		return tryLine_End(mText,start);
	}
	/* 获取行的起始下标 */
	public abstract int getLineStart(int line)
	
	/* 获取下标所在的行 */
	public abstract int getLineForOffset(int offset)
	
	
	/* 获取指定行且指定横坐标处的offset */
	public int getOffsetForHorizontalAndLine(int line, float horiz)
	{
		CharSequence text = mText;
		int start = getLineStart(line);
		int end = tryLine_End(text,start);
		return measureOffset(text,start,end,horiz,mPaint);
	}
	/* 获取指定坐标处的offset */
	final public int getOffsetForPosition(float x, float y)
	{
		int line = getLineForVertical((int)y);
		int count = getOffsetForHorizontalAndLine(line,x);
		return count;
	}
	/* 获取offset的横坐标，非常精确 */
	public float getOffsetHorizontal(int offset)
	{
		CharSequence text = mText;
		int start = tryLine_Start(text,offset);
		return measureText(text,start,offset,mPaint);
	}
	/* 获取offset的纵坐标，非常精确 */
	public float getOffsetVertical(int offset){
		return getLineForOffset(offset)*getLineHeight();
	}
    /* 获取offset的坐标 */
	public void getCursorPos(int offset, pos pos){
		pos.x = getOffsetHorizontal(offset);
		pos.y = getOffsetVertical(offset);
	}
	
	/* 获取临近光标坐标，可能会更快 */
	final public void nearOffsetPos(CharSequence text, int oldOffset, float x, float y, int newOffset, pos target, TextPaint paint)
	{
		int index = tryLine_Start(text,newOffset);
		target.x = measureText(text,index,newOffset,paint);

		if(oldOffset<newOffset){
			int line = Count(FN,text,oldOffset,newOffset);
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset){
			int line = Count(FN,text,newOffset,oldOffset);
			target.y = y-getLineHeight()*line;
		}
		else{
			target.y = y;
		}
	}
	/* 与任意内容无关的进行计算坐标，文本可以是一个文本切片，仅用有限的文本计算累计坐标，从上个位置开始 */
	final public void nearOffsetPos(char[] array, int oldOffset, float x, float y, int newOffset, pos target, TextPaint paint)
	{
		int index = ArrayUtils.lastIndexOf(array,FN,newOffset-1);
		index = index<0 ? 0:index+1;
		target.x = measureText(array,index,newOffset,paint);

		if(oldOffset<newOffset){
			int line = Count(array,FN,oldOffset,newOffset);
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset){
			int line = Count(array,FN,newOffset,oldOffset);
			target.y = y-getLineHeight()*line;
		}
		else{
			target.y = y;
		}
	}
	

	/* 获取行的区域 */
	public void getLineBounds(int line, Rect bounds)
	{
		bounds.left = 0;
		bounds.top = (int)(line*getLineHeight());
		bounds.right = (int)(bounds.left+maxWidth());
		bounds.bottom = (int) (bounds.top+getLineHeight());
	}
	/* 获取光标的路径 */
	public void getCursorPath(int point, Path dest, CharSequence editingBuffer)
	{
		RectF r = RecylePool.obtainRect();
		pos p = RecylePool.obtainPos();
		float lineHeight = getLineHeight();
		float width = mCursorWidthSpacing*mPaint.getTextSize();
		getCursorPos(point,p);

		//添加这一点的Rect
		r.left=p.x;
		r.top=p.y;
		r.right=r.left+width;
		r.bottom=r.top+lineHeight;
		dest.addRect(r, Path.Direction.CW);

		//回收这些
		RecylePool.recyleRect(r);
		RecylePool.recylePos(p);
	}
	/* 获取选择区域的路径 */
	public void getSelectionPath(int start, int end, Path dest)
	{
		CharSequence text = mText;
		TextPaint paint = mPaint;
		float lineHeight = getLineHeight();
		RectF rf = RecylePool.obtainRect();
		pos s = RecylePool.obtainPos();
		pos e = RecylePool.obtainPos();

		getCursorPos(start,s);
		if(end-start>1000){
			getCursorPos(end,e);
		}else{
		    nearOffsetPos(text,start,s.x,s.y,end,e,paint);
		}

		float w = getDisredWidth(text,start,end,paint);
		if(s.y == e.y)
		{
			//单行的情况
			//添加起始行的Rect
			rf.left = s.x;
			rf.top = s.y;
			rf.right = rf.left+w;
			rf.bottom = rf.top+lineHeight;
			dest.addRect(rf,Path.Direction.CW);
			return;
		}

		float sw = measureText(text,start,tryLine_End(text,start),paint);
		//添加起始行的Rect
		rf.left = s.x;
		rf.top = s.y;
		rf.right = rf.left+sw;
		rf.bottom = rf.top+lineHeight;
		dest.addRect(rf,Path.Direction.CW);	

		if((e.y-s.y)/lineHeight > 1)
		{
			//如果行数超过2，添加中间所有行的Rect
			rf.left = 0;
			rf.top = rf.top+lineHeight;
			rf.right = rf.left+w;
			rf.bottom = e.y;
			dest.addRect(rf,Path.Direction.CW);
		}

		//添加末尾行的Rect
		rf.left = 0;
		rf.top = rf.bottom;
		rf.right = rf.left+ e.x;
		rf.bottom = rf.top+lineHeight;
		dest.addRect(rf,Path.Direction.CW);

		//回收这些
		RecylePool.recylePos(s);
		RecylePool.recylePos(e);
		RecylePool.recyleRect(rf);
	}
	/* 行中是否包含tab符号 */
	public boolean getLineContainsTab(int p1)
	{
		CharSequence text = mText;
		int start = getLineStart(p1);
		int end = tryLine_End(text,start);
		return Count(FT,text,start,end)!=0;
	}
	
	
	/* 测量文本切片的宽 */
	final public float getDisredWidth(CharSequence text, int start, int end, TextPaint paint)
	{
		char[] chars = fillChars(text,start,end);
		float width = getDisredWidth(chars,0,end-start,paint);
		RecylePool.recyleCharArray(chars);
		mCacheStart += start;
		mCacheEnd += start;
		return width;
	}
	final public float getDisredWidth(char[] chars, int start, int end, TextPaint paint)
	{
		int last = start;
		float width = 0, w = 0;
		int line = 0;
		int maxStart = 0, maxEnd = 0;
		for(;start<end;++start)
		{
			if(chars[start]==FN)
			{
				w = paint.measureText(chars,last,start-last);
				if(w > width){
					width = w;
					maxStart = last;
					maxEnd = start;
				}
				last = start+1;
				++line;
			}
		}
		w = paint.measureText(chars,last,start-last);
		if(w > width){
			width = w;
			maxStart = last;
			maxEnd = start;
		}
		mCacheLine = line;
		mCacheStart = maxStart;
		mCacheEnd = maxEnd;
		return width;
	}
	/* 测量文本切片的高 */
	final public float getDisredHeight(CharSequence text, int start, int end)
	{
		char[] chars = fillChars(text,start,end);
		float width = getDesiredHeight(chars,0,end-start);
		RecylePool.recyleCharArray(chars);
		return width;
	}
	final public float getDesiredHeight(char[] text, int start, int end)
	{
		mCacheLine = Count(text,FN,start,end);
		return mCacheLine*getLineHeight();
	}
	/* 在测量后，可以获取文本切片的行数 */
	final protected int getCacheLine(){
		return mCacheLine;
	}
	/* 在测量后，可以获取文本切片中最宽的一行的范围 */
	final protected int getCacheStart(){
		return mCacheStart;
	}
	final protected int getCacheEnd(){
		return mCacheEnd;
	}
	
	/* 测量单行文本宽度，非常精确 */
	final public float measureText(CharSequence text,int start,int end,TextPaint paint)
	{
		char[] chars = fillChars(text,start,end);
		float width = measureText(chars,0,end-start,paint);
		RecylePool.recyleCharArray(chars);
		return width;
	}
	final public float measureText(char[] chars,int start,int end,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		float[] widths = fillWidths(chars,start,end,paint);
		for(int i = 0;i<count;++i){
			width+=widths[i];
		}
		RecylePool.recyleFloatArray(widths);
		return width;
	}
	/* 测量单行文本中，指定位置的下标 */
	final public int measureOffset(CharSequence text,int start,int end,float tox,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		float[] widths = fillWidths(text,start,end,paint);
		for(int i=0;i<count;++i)
		{
			if(width>=tox){
				break;
			}
			++start;
			width+=widths[i];
		}
		RecylePool.recyleFloatArray(widths);
		return start;
	}

	/* 统计和寻找下标 */
	final public int Count(char c, CharSequence text, int start, int end)
	{
		char[] chars = fillChars(text,start,end);
		int count = Count(chars,c,0,end-start);
		RecylePool.recyleCharArray(chars);
		return count;
	}
	final public int NIndex(char c,CharSequence text,int index, int n)
	{
		char[] chars = fillChars(text,index,text.length());
		int offset = index+NIndex(c,chars,0,n);	
		RecylePool.recyleCharArray(chars);
		return offset;
	}
	final public int lastNIndex(char c,CharSequence text,int index, int n)
	{
		char[] chars = fillChars(text,0,index+1);
		int offset = lastNIndex(c,chars,index,n);
		RecylePool.recyleCharArray(chars);
		return offset;
	}

	/* 从index开始，向后找到字符c在arr中第n次出现的位置 */
	final public static int NIndex(char c,char[] arr,int index,int n)
	{
		if (arr == null || index<0) return -1;
		for(;index<arr.length;++index)
		{
			if(arr[index]==c){
				--n;
			}
			if(n<1){
				return index;
			}
		}
		return -1;
	}
	/* 从index开始，向前找到字符c在arr中第n次出现的位置 */
	final public static int lastNIndex(char c,char[] arr,int index,int n)
	{
		if (arr == null || index>=arr.length) return -1;
		for(;index>-1;--index)
		{
			if(arr[index]==c){
				--n;
			}
			if(n<1){
				return index;
			}
		}
		return -1;
	}
	/* 统计字符在数组指定范围内出现的次数 */
	final public static int Count(char[] array, char value, int start, int end)
	{
		int count = 0;
		for(;start<end;++start){
			if(array[start]==value) ++count;
		}
		return count;
	}

	/* 用指定文本填充一个文本数组，注意，调用者负责回收文本数组 */
	private char[] fillChars(CharSequence text, int start, int end)
	{
		char[] chars = RecylePool.obtainCharArray(end-start);
		TextUtils.getChars(text,start,end,chars,0);
		return chars;
	}
	/* 用指定文本数组的内容填充一个宽度数组，注意，调用者负责回收宽度数组 */
	private float[] fillWidths(char[] chars, int start, int end, TextPaint paint)
	{
		float[] widths = RecylePool.obtainFloatArray(end-start);
		paint.getTextWidths(chars,start,end-start,widths);
		return widths;
	}
	/* 用指定文本填充一个宽度数组，注意，调用者负责回收宽度数组 */
	private float[] fillWidths(CharSequence text, int start, int end, TextPaint paint)
	{
		char[] chars = fillChars(text,start,end);
		float[] widths = fillWidths(chars,0,end-start,paint);
		RecylePool.recyleCharArray(chars);
		return widths;
	}

	//试探当前下标所在行的起始
	final public static int tryLine_Start(CharSequence src,int index)
	{
		index = TextUtils.lastIndexOf(src,FN,index);
		return index<0 ? 0:index;
	}
	//试探当前下标所在行的末尾
	final public static int tryLine_End(CharSequence src,int index)
	{
		index = TextUtils.indexOf(src,FN,index);
		return index<0 ? src.length():index;
	}
	
	public abstract void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)

	public abstract void draw(Canvas c)

	
	/* 回收池 */
	protected static class RecylePool
	{
		private static final boolean[][] sBooleanArrays = new boolean[6][0];
		private static final char[][] sCharArrays = new char[6][0];
		private static final int[][] sIntArrays = new int[6][0];
		private static final float[][] sFloatArrays = new float[6][0];
		private static final RectF[] sRectArray = new RectF[6];
		private static final pos[] sPosArray = new pos[6];
		private static final Paint.FontMetrics[] sFontArray = new Paint.FontMetrics[6];

		public static boolean[] obtainBooleanArray(int size)
		{
			synchronized(sBooleanArrays)
			{
				for(int i=0;i<sBooleanArrays.length;++i)
				{
					boolean[] array = sBooleanArrays[i];
					if (array!=null && array.length>=size) {
						sBooleanArrays[i] = null;
						return array;
					}
				}
			}
			return ArrayUtils.newUnpaddedBooleanArray(GrowingArrayUtils.growSize(size));
		}
		public static void recyleBooleanArray(boolean[] array)
		{
			synchronized (sBooleanArrays)
			{
				for (int i=0;i<sBooleanArrays.length;i++) 
				{
					if (sBooleanArrays[i] == null || array.length > sBooleanArrays[i].length) {
						sBooleanArrays[i] = array;
						break;
					}
				}
			}
		}
		public static char[] obtainCharArray(int size)
		{
			synchronized(sCharArrays)
			{
				for(int i=0;i<sCharArrays.length;++i)
				{
					char[] array = sCharArrays[i];
					if (array!=null && array.length>=size) {
						sCharArrays[i] = null;
						return array;
					}
				}
			}
			return ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(size));
		}
		public static void recyleCharArray(char[] array)
		{
			synchronized (sCharArrays)
			{
				for (int i=0;i<sCharArrays.length;i++) 
				{
					if (sCharArrays[i] == null || array.length > sCharArrays[i].length) {
						sCharArrays[i] = array;
						break;
					}
				}
			}
		}
		public static int[] obtainIntArray(int size)
		{
			synchronized(sIntArrays)
			{
				for(int i=0;i<sIntArrays.length;++i)
				{
					int[] array = sIntArrays[i];
					if (array!=null && array.length>=size) {
						sIntArrays[i] = null;
						return array;
					}
				}
			}
			return ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(size));
		}
		public static void recyleIntArray(int[] array)
		{
			synchronized (sIntArrays)
			{
				for (int i=0;i<sIntArrays.length;i++) 
				{
					if (sIntArrays[i] == null || array.length > sIntArrays[i].length) {
						sIntArrays[i] = array;
						break;
					}
				}
			}
		}
		public static float[] obtainFloatArray(int size)
		{
			synchronized(sFloatArrays)
			{
				for(int i=0;i<sFloatArrays.length;++i)
				{
					float[] array = sFloatArrays[i];
					if (array!=null && array.length>=size) {
						sFloatArrays[i] = null;
						return array;
					}
				}
			}
			return ArrayUtils.newUnpaddedFloatArray(GrowingArrayUtils.growSize(size));
		}
		public static void recyleFloatArray(float[] array)
		{
			synchronized (sFloatArrays)
			{
				for (int i=0;i<sFloatArrays.length;i++) 
				{
					if (sFloatArrays[i] == null || array.length > sFloatArrays[i].length) {
						sFloatArrays[i] = array;
						break;
					}
				}
			}
		}
		public static RectF obtainRect()
		{
			synchronized(sRectArray)
			{
				for(int i=0;i<sRectArray.length;++i)
				{
					RectF rect = sRectArray[i];
					if (rect!=null) {
						sRectArray[i] = null;
						return rect;
					}
				}
			}
			return new RectF();
		}
		public static void recyleRect(RectF rect)
		{
			synchronized(sRectArray)
			{
				for (int i=0;i<sRectArray.length;i++) 
				{
					if (sRectArray[i] == null){
						sRectArray[i] = rect;
						break;
					}
				}
			}
		}
		public static pos obtainPos()
		{
			synchronized(sPosArray)
			{
				for(int i=0;i<sPosArray.length;++i)
				{
					pos pos = sPosArray[i];
					if (pos!=null) {
						sPosArray[i] = null;
						return pos;
					}
				}
			}
			return new pos();
		}
		public static void recylePos(pos pos)
		{
			synchronized(sPosArray)
			{
				for (int i=0;i<sPosArray.length;i++) 
				{
					if (sPosArray[i] == null){
						sPosArray[i] = pos;
						break;
					}
				}
			}
		}
		public static Paint.FontMetrics obtainFont()
		{
			synchronized(sFontArray)
			{
				for(int i=0;i<sFontArray.length;++i)
				{
					Paint.FontMetrics font = sFontArray[i];
					if (font!=null) {
						sFontArray[i] = null;
						return font;
					}
				}
			}
			return new Paint.FontMetrics();
		}
		public static void recyleFont(Paint.FontMetrics font)
		{
			synchronized(sFontArray)
			{
				for (int i=0;i<sFontArray.length;i++) 
				{
					if (sFontArray[i] == null){
						sFontArray[i] = font;
						break;
					}
				}
			}
		}
	}
	
}
