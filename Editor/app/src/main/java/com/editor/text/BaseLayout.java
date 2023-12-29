package com.editor.text;

import android.graphics.*;
import com.editor.text.base.*;
import android.text.*;
import com.editor.text.span.*;
import android.util.*;
import android.text.style.*;
import java.util.*;
import java.util.concurrent.*;


/* 
  简单的Layout，尽可能节省时间，所以不会使用nextSpanTranstion，并尽量少用getSpans
  只绘制TextStyleSpan，为预算好宽高的文本附加样式，不会处理字体大小，边距 ，或大块背景图案
*/
public abstract class BaseLayout
{
	public static final char FN = '\n', FT = '\t', SPACE = ' ', FREE = '_', USE = '-';
	public static final int TextColor = 0xffabb2bf , LineColor = 0xff666666;
	public static final float LineSpcing = 1.15f, CursorWidthSpacing = 0.12f;
	
	private static int TabSize = 4;
	private static final char[] SPACEARR = new char[]{' '};
	private static final int LineMargin = 50;
	
	private CharSequence mText;
	private TextPaint mPaint;
	private TextPaint mSpanPaint;
	
	private boolean isSpannedText;
	private int mLineColor;
	private int mHighlightLineColor;
	
	private float mWidth;
	private float mLineSpacing;
	private float mCursorWidthSpacing;
	
	protected BaseLayout(CharSequence base, TextPaint paint, int lineColor, float lineSpacing)
	{
		mText = base;
		mPaint = paint;
		mSpanPaint = new TextPaint(paint);
		
		mWidth = 0;
		mLineColor = lineColor;
		mHighlightLineColor = paint.getColor();
		
		mLineSpacing = lineSpacing;
		mCursorWidthSpacing = CursorWidthSpacing;
		isSpannedText = base instanceof Spanned;
	}
	
	public final void setLineColor(int color){
		mLineColor = color;
	}
	public final void setLineSpacing(float lineSpacing){
		mLineSpacing = lineSpacing;
	}
	public final int getLineColor(){
		return mLineColor;
	}
	public final float getLineSpacing(){
		return mLineSpacing;
	}
	public final void increaseWidthTo(float width){
		if(width > mWidth){
			mWidth = width;
		}
	}
	public final void needDrawSpans(boolean need){
		isSpannedText = need && mText instanceof Spanned;
	}
	
	public void draw(Canvas canvas, float cursorOffsetVertical)
	{
		//获取可视区域
		Rect rect = RecylePool.obtainRect();
		if(!canvas.getClipBounds(rect)){
			RecylePool.recyleRect(rect);
			return;
		}
		
		//计算可视区域的行
		int startLine = getLineForVertical(rect.top);
		int endLine = getLineForVertical(rect.bottom);
		//计算可视区域的范围
		int start = getLineStart(startLine);
		int end = getLineStart(endLine+1);
		//绘制指定范围内的文本
		onDraw(start,end,startLine,endLine,cursorOffsetVertical,canvas,rect);
		RecylePool.recyleRect(rect);
	}
	
	/* 绘制指定范围内的行和文本，绘制过程中记得测量宽度 */
	private void onDraw(int start, int end, int startLine, int endLine, float cursorOffsetVertical, Canvas canvas, Rect See)
	{
		//使用当前画笔的属性初始化数据
		CharSequence text =  mText;
		TextPaint textPaint = mPaint;
		TextPaint spanPaint = mSpanPaint;
		spanPaint.set(textPaint);
		
		Paint.FontMetrics font = RecylePool.obtainFont();
		textPaint.getFontMetrics(font);
		float lineHeight = (font.bottom-font.top)*mLineSpacing;
		float leftPadding = getLineMargin();
		
		//绘制行数
		int saveColor = textPaint.getColor();
		textPaint.setColor(mLineColor);
		spanPaint.setColor(mHighlightLineColor);
		drawLineNumber(startLine,endLine,(int)(cursorOffsetVertical/lineHeight),-leftPadding,startLine*lineHeight-font.ascent,lineHeight,canvas,textPaint,spanPaint,See);
		textPaint.setColor(saveColor);
		spanPaint.setColor(saveColor);

		//获取文本和所有字符的宽度
		int length = end-start;
		char[] chars = RecylePool.obtainCharArray(length);
		TextUtils.getChars(text,start,end,chars,0);
		float[] widths = RecylePool.obtainFloatArray(length);
		getTextWidths(chars,0,length,textPaint,widths);
		
		//在绘制期间存储span的范围，累计最大的
		int[] spanStarts = EmptyArray.INT;
		int[] spanEnds = EmptyArray.INT;
		//在绘制期间存储span的绘制范围的标记，累计最大的
		char[] textRangeTable = EmptyArray.CHAR;
		
		//一行行进行绘制
		float x = 0;
		float y = startLine*lineHeight;
		int now = 0, next;
		for(;startLine<=endLine;++startLine)
		{
			//获取行的起始和末尾
			next = ArrayUtils.indexOf(chars,FN,now,length);
			next = next < 0 ? length : next;
			int i = now;
			float w = x;
			int csStart, csEnd;

			//从第一个字符开始，向后遍历每个字符，获取本行可见区域起始位置，不包含FN
			for(;i<next;++i)
			{
				if(w+widths[i] > See.left){
					//如果已经到达了可视区域左边，已经可以确定这行起始下标和坐标了
					break;
				}
				w += widths[i];
			}	
			csStart = i;
			x = w;

			//从上次的位置开始，继续向后遍历每个字符，获取本行可见区域末尾位置
			for(;i<next;++i)
			{
				if(w >= See.right){
					//如果已经到达了可视区域右边，已经可以确定这行末尾下标和坐标了
					break;
				}
				w += widths[i];
			}	
			csEnd = i;

			//如果是空行，则无法绘制任何文本和span
			if(csEnd > csStart)
			{
				if(isSpannedText)
				{
					//获取范围内的spans
					Spanned sp = (Spanned) text;
					TextStyleSpan[] spans = sp.getSpans(start+csStart,start+csEnd,TextStyleSpan.class);
					int spanCount = spans.length;
					if(spanCount > 0)
					{
						//小的数组被舍弃，获取更大的
						if(textRangeTable.length < csEnd-csStart){
							textRangeTable = RecylePool.obtainCharArray(csEnd-csStart<RecylePool.TABLE_SIZE ? RecylePool.TABLE_SIZE: csEnd-csStart);
						}
						if(spanStarts.length < spanCount){
							spanStarts = RecylePool.obtainIntArray(spanCount<RecylePool.SPAN_SIZE ? RecylePool.SPAN_SIZE : spanCount);
						}
						if(spanEnds.length < spanCount){
							spanEnds = RecylePool.obtainIntArray(spanCount<RecylePool.SPAN_SIZE ? RecylePool.SPAN_SIZE : spanCount);		
						}
						for(int k=0;k<spanCount;++k){
							//将span偏移到数组范围(start~end)
							spanStarts[k] = sp.getSpanStart(spans[k]) - start;
							spanEnds[k] = sp.getSpanEnd(spans[k]) - start;
						}
						//绘制这行的可见范围内的文本，包含span
						drawSingleLineText(chars, widths, textRangeTable, now, next, csStart, csEnd, spans, spanStarts, spanEnds, spanCount, x, y, lineHeight, font.ascent, canvas, textPaint, spanPaint);	
					}
					else{
						drawTextWithCharcterStop(chars,widths,FT,csStart,csEnd,x,y-font.ascent,canvas,textPaint);
					}
				}
				else{
					drawTextWithCharcterStop(chars,widths,FT,csStart,csEnd,x,y-font.ascent,canvas,textPaint);
				}
			}
			
			//之后继续下行
			now = next+1;
			x = 0;
			y += lineHeight;
			w += togetherWidth(widths,i,next);
			if(w > mWidth){
				//在绘制时，就可以测量当前已绘制文本的最大宽度，但并非全部文本
				mWidth = w;
			}
		}
		
		RecylePool.recyleFont(font);
		RecylePool.recyleCharArray(chars);
		RecylePool.recyleFloatArray(widths);
		//如果可以，回收最大的span数组
		if(spanStarts.length > 0){
			RecylePool.recyleIntArray(spanStarts);
		}
		if(spanEnds.length > 0){
			RecylePool.recyleIntArray(spanEnds);
		}
		if(textRangeTable.length > 0){
			RecylePool.recyleCharArray(textRangeTable);
		}
	}
	
	/* 以单行绘制chars中指定范围的文本和span，span的范围被附加在数组范围中，以0开始 */
	private static void drawSingleLineText(char[] chars, float[] widths, char[] table, int lineStart, int lineEnd, int csStart, int csEnd, TextStyleSpan[] spans, int[] spanStarts, int[] spanEnds, int spanCount, float x, float y, float lineHeight, float ascent, Canvas canvas, TextPaint textPaint, TextPaint spanPaint)
	{
		//先绘制背景的span
		for(int k=0;k<spanCount;++k)
		{
			if(spans[k] instanceof BackgroundSpanX)
			{
				//超出行的范围的内容不绘制，背景的span使用行的范围判断，因为这需要包含可见区域外的部分，以使绘制图形完整
				if(spanStarts[k] < lineStart){
					spanStarts[k] = lineStart;
				}
				if(spanEnds[k] > lineEnd){
					spanEnds[k] = lineEnd;
				}
				//单点的span画不出来
				if(spanEnds[k] > spanStarts[k])
				{
					spans[k].updateDrawState(spanPaint);
					float xStart = togetherWidth(widths,lineStart,spanStarts[k]);
					float width = togetherWidth(widths,spanStarts[k],spanEnds[k]);
					((BackgroundSpanX)spans[k]).draw(xStart,y,xStart+width,y+lineHeight,canvas,spanPaint);
					spans[k].restoreDrawState(spanPaint);
				}
			}
		}
		
		//反向绘制文本的span，并用范围填充表，每个字符只能被绘制一次
		Arrays.fill(table,0,csEnd-csStart,FREE);
		for(int k=spanCount-1;k>-1;--k)
		{
			if(!(spans[k] instanceof BackgroundSpanX))
			{
				//超出本行可见范围的内容不绘制
				if(spanStarts[k] < csStart){
					spanStarts[k] = csStart;
				}
				if(spanEnds[k] > csEnd){
					spanEnds[k] = csEnd;
				}
				//尽可能去除span的重叠范围，最好可以不用绘制
				checkSpanRange(spanStarts,spanEnds,k,table,csStart);
				if(spanEnds[k] > spanStarts[k])
				{
					spans[k].updateDrawState(spanPaint);
					float xStart = x+togetherWidth(widths,csStart,spanStarts[k]);
					drawTextWithRangeTable(chars,widths,FT,spanStarts[k],spanEnds[k],xStart,y-ascent,canvas,spanPaint,table,csStart);
					spans[k].restoreDrawState(spanPaint);
				}
			}
		}
	
		//检查本行剩余的未绘制文本，并绘制出来
		drawTextWithRangeTable(chars,widths,FT,csStart,csEnd,x,y-ascent,canvas,textPaint,table,csStart);
	}
	
	/* 在指定的位置开始绘制一列范围内的行数 */
	private static void drawLineNumber(int startLine, int endLine, int highlightLine, float x, float y, float lineHeight, Canvas canvas, TextPaint paint, TextPaint highlightPaint, Rect See)
	{
		String line = String.valueOf(endLine);
		float lineWidth = paint.measureText(line,0,line.length());
		if(See.left > x+lineWidth){
			//如果x位置已经超出了行的宽度，就不用绘制了
			return ;
		}
 
		//从起始行开始，绘制到末尾行，每绘制一行y+lineHeight
		for(;startLine<=endLine;++startLine)
		{
			line = String.valueOf(startLine);
			if(startLine ==  highlightLine){
				canvas.drawText(line,x,y,highlightPaint);
			}else{
				canvas.drawText(line,x,y,paint);
			}
			y+=lineHeight;
		}
	}
	
	/* 绘制单行文本，不绘制已绘制的位置，并用范围填充表 */
	private static void drawTextWithRangeTable(char[] chars, float[] widths, char c, int start, int end, float x, float y, Canvas canvas, TextPaint paint, char[] table, int begin)
	{
		int length = end-begin;
		for(int i=start-begin;i<length;++i)
		{
			if(table[i] == FREE)
			{
				int j = i+1;
				table[i] = USE;
				for(;j<length;++j)
				{
					if(table[j] == USE){
						break;
					}
					table[j] = USE;
				}
				float xStart = x+togetherWidth(widths,start,i+begin);
				drawTextWithCharcterStop(chars,widths,FT,i+begin,j+begin,xStart,y,canvas,paint);
				i = j;
			}
		}
	}
	
	/* 绘制单行被中断符分隔的文本，中断符并不绘制，但是跳过中断符时也会算上中断符的宽度 */
	private static void drawTextWithCharcterStop(char[] chars, float[] widths, char c, int start, int end, float x, float y, Canvas canvas, TextPaint paint)
	{
		int en = end;
		while(start < en)
		{
			//每次从start开始向后找一个中断符c，把之间的文本画上
			end = ArrayUtils.indexOf(chars,c,start,en);
			if(end < 0){
				//start~end之间的内容不会中断，画完就走
				canvas.drawText(chars,start,en-start,x,y,paint);		
				break;
			}
			else{
				//start~end之间的内容会中断，之后继续
				canvas.drawText(chars,start,end-start,x,y,paint);
				x += togetherWidth(widths,start,end+1);
			}
			start = end+1;
		}
	}
	
	/* 下标为i的span在表中的可视范围还有多少 */
	private static void checkSpanRange(int[] spanStarts, int[] spanEnds, int i, char[] table, int begin)
	{
		int start = spanStarts[i]-begin;
		int end = spanEnds[i]-begin;
		//两端点尽可能地往内缩
		for(;start<end;++start){
			if(table[start]==FREE){
				break;
			}
		}
		for(;end>start;--end){
			if(table[end-1]==FREE){
				break;
			}
		}
		spanStarts[i] = start+begin;
		spanEnds[i] = end+begin;
	}
	
	/* 累计数组中的指定范围内的字符的宽度 */
	private static float togetherWidth(float[] widths, int start, int end)
	{
		float width = 0;
		for(;start<end;++start){
			width += widths[start];
		}
		return width;
	}
	
	public final CharSequence getText(){
		return mText;
	}
    public final TextPaint getPaint(){
		return mPaint;
	}
	public final float getWidth(){
		return mWidth;
	}
	public final float getHeight(){
		return getLineCount()*getLineHeight();
	}
	public final float getLineMargin(){
		return mPaint.measureText(String.valueOf(getLineCount()))+LineMargin;
	}
	
	/* 获取行的数量 */
	public abstract int getLineCount()

	/* 获取行的宽度 */
	public final float getLineWidth(int line)
	{
		int start = getLineStart(line); 
		int end = getOffsetToRightOf(start);
		return measureText(mText,start,end,mPaint);
	}
	/* 获取行的高度 */
	public final float getLineHeight()
	{
		Paint.FontMetrics font = RecylePool.obtainFont();
		mPaint.getFontMetrics(font);
		float height = font.bottom-font.top;
		RecylePool.recyleFont(font);
		return height*mLineSpacing;
	}

	/* 获取行的纵坐标 */
	public final float getLineTop(int p1){
		return p1*getLineHeight();
	}
	/* 获取行底的纵坐标 */
	public final float getLineBottom(int p1){
		return (p1+1)*getLineHeight();
	}
	/* 获取纵坐标指定的行 */
	public final int getLineForVertical(float vertical)
	{
		int lineCount = getLineCount();
		int line = (int)(vertical/getLineHeight());
		line = line<0 ? 0 : (line>lineCount ? lineCount:line);
		return line;
	}

	/* 获取行的末尾下标 */
	public final int getLineEnd(int line){
		int start = getLineStart(line);
		return getOffsetToRightOf(start);
	}
	/* 获取行的起始下标 */
	public abstract int getLineStart(int line)

	/* 获取下标所在的行 */
	public abstract int getLineForOffset(int offset)


	/* 获取offset的横坐标 */
	public final float getOffsetHorizontal(int offset){
		int start = getOffsetToLeftOf(offset);
		return measureText(mText,start,offset,mPaint);
	}
	/* 获取offset的纵坐标 */
	public final float getOffsetVertical(int offset){
		return getLineForOffset(offset)*getLineHeight();
	}
	/* 获取offset的坐标 */
	public final void getCursorPos(int offset, pos p){
		p.x = getOffsetHorizontal(offset);
		p.y = getOffsetVertical(offset);
	}
	/* 获取指定行且指定横坐标处的offset */
	public final int getOffsetForHorizontalAndLine(int line, float horiz)
	{
		int start = getLineStart(line);
		int end = getOffsetToRightOf(start);
		return measureOffset(mText,start,end,horiz,mPaint);
	}
	/* 获取指定坐标处的offset */
	public final int getOffsetForPosition(float x, float y)
	{
		int line = getLineForVertical((int)y);
		int count = getOffsetForHorizontalAndLine(line,x);
		return count;
	}
	
	/* 获取offset所在行的起始 */
	public int getOffsetToLeftOf(int offset){
		offset = TextUtils.lastIndexOf(mText,FN,offset-1);
		return offset<0 ? 0:offset+1;
	}
	/* 获取offset所在行的末尾 */
	public int getOffsetToRightOf(int offset){
		offset = TextUtils.indexOf(mText,FN,offset);
		return offset<0 ? mText.length():offset;
	}
	
	/* 获取行的区域 */
	public void getLineBounds(int line, Rect bounds)
	{
		float lineHeight = getLineHeight();
		bounds.left = 0;
		bounds.top = (int)(line*lineHeight);
		bounds.right = (int)(bounds.left+mWidth);
		bounds.bottom = (int) (bounds.top+lineHeight);
	}
	/* 获取光标的路径 */
	public void getCursorPath(int point, Path dest)
	{
		float lineHeight = getLineHeight();
		float width = mCursorWidthSpacing*mPaint.getTextSize();
		float x = getOffsetHorizontal(point);
		float y = getOffsetVertical(point);
		dest.moveTo(x,y);
		dest.lineTo(x+width,y);
		dest.lineTo(x+width,y+lineHeight);
		dest.lineTo(x,y+lineHeight);
		dest.close();
	}
	/* 获取选择区域的路径 */
	public void getSelectionPath(int start, int end, Path dest)
	{
		int startLine = getLineForOffset(start);
		int endLine = getLineForOffset(end);
		float lineHeight = getLineHeight();
		float sx = getOffsetHorizontal(start);
		float sy = startLine*lineHeight;
		float ex = startLine==endLine ? sx+measureText(mText,start,end,mPaint) : getOffsetHorizontal(end);
		float ey = (endLine+1)*lineHeight;
		
		dest.moveTo(sx,sy);
		if(startLine == endLine){		
			dest.lineTo(ex,sy);
			dest.lineTo(ex,ey);
			dest.lineTo(sx,ey);
		}
		else{
			float dy = (endLine-startLine)*lineHeight;
			dest.lineTo(mWidth,sy);
			dest.lineTo(mWidth,sy+dy);
			dest.lineTo(ex,sy+dy);
			dest.lineTo(ex,ey);
			dest.lineTo(0,ey);
			dest.lineTo(0,ey-dy);
			dest.lineTo(sx,ey-dy);
		}
		dest.close();
	}
	
	/* 测量文本时一并附加特殊字符的宽度 */
	public static final float measureText(CharSequence text, int start, int end, TextPaint paint)
	{
		int length = end-start;
		char[] chars = RecylePool.obtainCharArray(length);
		TextUtils.getChars(text,start,end,chars,0);
		float[] widths = RecylePool.obtainFloatArray(length);
		getTextWidths(chars,0,length,paint,widths);
		float width = 0;
		for(int i=0;i<length;++i){
			width+=widths[i];
		}
		RecylePool.recyleFloatArray(widths);
		RecylePool.recyleCharArray(chars);
		return width;
	}
	/* 测量单行文本中，指定位置的下标 */
	public static final int measureOffset(CharSequence text, int start, int end, float tox, TextPaint paint)
	{
		int count = end-start;
		char[] chars = RecylePool.obtainCharArray(count);
		TextUtils.getChars(text,start,end,chars,0);
		float[] widths = RecylePool.obtainFloatArray(count);
		getTextWidths(chars,0,count,paint,widths);
		float width = 0;
		for(int i=0;i<count;++i)
		{
			if(width>=tox){
				break;
			}
			++start;
			width+=widths[i];
		}
		RecylePool.recyleFloatArray(widths);
		RecylePool.recyleCharArray(chars);
		return start;
	}
	/* 获取文本中所有字符的宽度，特殊字符额外处理 */
	public static final void getTextWidths(char[] chars, int start, int end, TextPaint paint, float[] widths)
	{
		int count = end-start;
		paint.getTextWidths(chars,start,count,widths);
		//将Tab的宽度替换为指定大小
		float tabWidth = TabSize * paint.measureText(SPACEARR,0,1);
		for(int i=0;i<count;++i){
			if(chars[i] == FT){
				widths[i] = tabWidth;
			}
		}
	}
	
	/* 回收池 */
	protected static final class RecylePool
	{
		private static final int SPAN_SIZE = 10, TABLE_SIZE = 80;
		private static final char[][] sCharArrays = new char[6][0];
		private static final int[][] sIntArrays = new int[6][0];
		private static final float[][] sFloatArrays = new float[6][0];
		private static final Rect[] sRectArray = new Rect[6];
		private static final Paint.FontMetrics[] sFontArray = new Paint.FontMetrics[6];

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
		public static Rect obtainRect()
		{
			synchronized(sRectArray)
			{
				for(int i=0;i<sRectArray.length;++i)
				{
					Rect rect = sRectArray[i];
					if (rect!=null) {
						sRectArray[i] = null;
						return rect;
					}
				}
			}
			return new Rect();
		}
		public static void recyleRect(Rect rect)
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
