package com.who.Edit.Base;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.text.style.*;
import android.view.*;
import android.view.inputmethod.*;
import com.who.Edit.Base.Share.Share1.*;
import com.who.Edit.Base.Share.Share3.*;
import com.who.Edit.Base.Share.Share4.*;
import java.math.*;


public class Edit extends View implements TextWatcher
{

	public static final char FN = '\n';

	private Cursor mCursor;
	private myInput mInput;
	private Editable mText;
	private myLayout mLayout;

	private TextPaint mPaint;
	private TextPaint copyPaint;

	private TextWatcher mTextListener;
	private EditTouch mTouch;
	private EditZoom mZoom;

	public Edit(Context cont)
	{
		super(cont);
		init();
		config();
	}
	protected void init()
	{
		mZoom = new EditZoom();
		mTouch = new EditTouch();

		mCursor = new Cursor();
		mPaint = new TextPaint();
		copyPaint = new TextPaint();
		
		mInput = new myInput(this,true);
		mText = mInput.getEditable();
		mLayout = new myLayout(mText, mPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.2f);
	}
	protected void config()
	{
		setPaint(mPaint);
		setPaint(copyPaint);
		setClickable(true);
		setLongClickable(true);
		setDefaultFocusHighlightEnabled(false);
		//设置在获取焦点时不用额外绘制高亮的矩形区域
	}
	private void setPaint(TextPaint paint)
	{
		paint.reset();
		paint.setTextSize(44);
		paint.setColor(0xff222222);
		paint.setTypeface(Typeface.MONOSPACE);
	}
	public void reSetPaint(TextPaint paint){
		paint.set(copyPaint);
	}

	public void setTextColor(int color){
		mPaint.setColor(color);
		copyPaint.setColor(color);
	}
	public void setTextSize(int size){
		mPaint.setTextSize(size);
		copyPaint.setTextSize(size);
	}
	public void setText(CharSequence text){
		mText = new SpannableStringBuilder(text);
	}

	public float getLineHeight(){
		return copyPaint.getTextSize()*1.2f;
	}
	public float getTextSize(){
		return copyPaint.getTextSize()/1.65f;
	}
	public float getLetterSpacing(){
		return copyPaint.getTextSize()*mPaint.getLetterSpacing();
	}
	public Layout getLayout(){
		return mLayout;
	}
	public TextPaint getTextPaint(){
		return copyPaint;
	}
	public Editable getText(){
		return mText;
	}

/*
_______________________________________

当输入事件到来，我们修改文本，并回调onTextChanged
_______________________________________ 
*/

	@Override
	public boolean onCheckIsTextEditor(){
		return true;
	}
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs){
		return mInput;
	}

	public void setTextChangeListener(TextWatcher li){
		mTextListener = li;
	}

	final public static void openInputor(final Context context, final View editText)
	{
		Activity act = (Activity) context;
		act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, 0);
	}
	final public static void closeInputor(Context context, View editText)
	{
		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}


	/* 获取输入，并自动修改文本，当文本变化时触发onTextChanged */
	final private class myInput extends BaseInputConnection
	{

		private boolean Enabled = true;

		public myInput(View v,boolean is){
			super(v,is);
		}

		@Override
		public boolean sendKeyEvent(KeyEvent event)
		{
			//手指抬起
			return true;
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition)
		{
			if(!Enabled){
				//没有启用输入，直接返回
				return true;
			}
	
			//提交缓冲区内的文本
			SpannableStringBuilder b = new SpannableStringBuilder(text);
			b.setSpan(new BackgroundColorSpan(0xffffed55),0,b.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			b.setSpan(new ForegroundColorSpan(0xffccbbaa),0,b.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int start = getSelectionStart();
			int after = text.length();

			//编辑器改变前的内容
			mLayout.measureTextBefore(mText,start,0,after);
			sendBeforeTextChanged(mText,start,0,after);
			mText.insert(start,b);

			//编辑器改变后的内容
			mLayout.measureTextAfter(mText,start,0,after);
			sendOnTextChanged(mText,start,0,after);
			invalidate();

			//编辑器显示的内容
			sendAfterTextChanged(mText);
			return true;
		}

		@Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength)
		{

			if(!Enabled){
				//没有启用输入，直接返回
				return true;
			}
			
			//准备删除字符
			int start = getSelectionStart()-beforeLength;

			//编辑器改变前的内容
			mLayout.measureTextBefore(mText,start,beforeLength,0);
			sendBeforeTextChanged(mText,start,beforeLength,0);
			mText.delete(start,start+beforeLength);

			//编辑器改变后的内容
			mLayout.measureTextAfter(mText,start,beforeLength,0);
			sendOnTextChanged(mText,start,beforeLength,0);
			invalidate();

			//编辑器显示的内容
			sendAfterTextChanged(mText);
			return true;
		}

		@Override
		public boolean finishComposingText()
		{
			//组合文本
			return true;
		}

	}

	
	protected void sendBeforeTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mTextListener!=null){
			mTextListener.beforeTextChanged(text,start,lenghtBefore,lengthAfter);
		}
		beforeTextChanged(text,start,lenghtBefore,lengthAfter);
	}
	protected void sendOnTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mTextListener!=null){
			mTextListener.onTextChanged(text,start,lenghtBefore,lengthAfter);
		}
		onTextChanged(text,start,lenghtBefore,lengthAfter);
	}
	protected void sendAfterTextChanged(Editable p1)
	{
		if(mTextListener!=null){
			mTextListener.afterTextChanged(p1);
		}
		afterTextChanged(p1);
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		//文本变化了，设置光标位置
		int index = 0;
		if(lenghtBefore!=0){
			index = start-lenghtBefore;
		}
		if(lengthAfter!=0){
			index = start+lengthAfter;
		}
		setSelection(index,index);
	}
	@Override
	public void beforeTextChanged(CharSequence p1, int start, int lenghtBefore, int lengthAfter){}
	
	@Override
	public void afterTextChanged(Editable p1){}


/*
_______________________________________

每次onDraw时会调用Layout绘制文本和光标

_______________________________________
*/

    /* onDraw是View绘制的第二步，第一步是drawBackground，但无法重写 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		//在第二步的绘制时，就使用clipRect放弃绘制可视范围外的内容，节省绘制时间
		float left = getScrollX();
		float top = getScrollY();
		float right = left+getWidth();
		float bottom = top+getHeight();
		canvas.clipRect(left,top,right,bottom);
		//在clipRect后，进行之后的绘制，并且之后绘制的第三，四，五，六步也会生效

		Path path = mCursor.getCursorPath();
		//获取光标或选择范围的路径
		mLayout.draw(canvas,path,mPaint,0);	
		//进行绘制，将文本画到画布上
	}

	
	/* 使用Layout进行文本布局和绘制，尽可能地节省时间 */
	final public class myLayout extends Layout
	{
		
		//不安全的临时变量，谁都可以使用
		float[] widths;
		Rect rect = new Rect();
		RectF rectF = new RectF();
		pos tmp = new pos(), tmp2 = new pos();
		
		//以下是每次draw时使用的
		RectF See = new RectF();
		pos start = new pos(), end = new pos();
		
		//记录一些属性，用于draw
		float cursorWidth = 0.1f;
		float lineSpacing = 0.2f;
		int lineCount=1, maxWidth;
		boolean NeddMeasureAll;

		public myLayout(java.lang.CharSequence base, android.text.TextPaint paint, 
		                int width, android.text.Layout.Alignment align,float spacingmult, float spacingadd) {
			super(base,paint,width,align,spacingmult,spacingadd);
		}

		/* 开始绘制文本和光标 */
		@Override
		public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)
		{
			draw(canvas);
			//先绘制文本，之后绘制光标
			//每次调用cancas.drawxxx方法，都会根据当前的状态新建一个图层并绘制，最后canvas显示的内容是所有图层叠加的效果
			//注意哦，已经绘制的内容是不会被改变的，但是对canvas进行平移等操作会影响之后的图层
			//考虑到mCursor默认在(0,0)处绘制，因此需要平移图层到下方，使得在(0,0)处绘制的操作转化为在(x,y)处的绘制
			//并且还需要clipPath，以只绘制指定光标路径的内容

			float x = getScrollX();
			float y = getScrollY();
			canvas.clipPath(highlight);
			//在平移前，先裁剪指定路径，但其实在之前已经clipRect了，其实也不会超出Rect的范围
			canvas.translate(x,y);
			//再平移，之后的绘制就默认到(x,y)处了，而我们剪切的范围正是(x,y)处，所以刚好命中范围
			mCursor.draw(canvas);
			//将canvas交给cursor绘制
		}

		/* 开始绘制文本 */
		@Override
		public void draw(Canvas canvas)
		{
			//新一轮的绘制开始了
			TextPaint paint = mPaint;
			Spanned spanString = mText;	
			String text = spanString.toString();

			//计算可视区域
			float x = getScrollX();
			float y = getScrollY();
			int width = Edit.this.getWidth();
			int height = Edit.this.getHeight();
			
			//计算行高和行宽
			float lineHeight = getLineHeight();
			float leftPadding = getLeftPadding();

			//计算可视区域的行
			int startLine = (int) (y/lineHeight);
			int endLine = (int) ((y+height)/lineHeight);
			startLine = startLine>lineCount ? lineCount:startLine;
			endLine = endLine>lineCount ? lineCount:endLine;

			//计算可视区域的范围
			int start = StringSpiltor.NIndex(FN,text,0,startLine);
			int end = StringSpiltor.NIndex(FN,text,start,(int)(height/lineHeight));
			start = start<0 ? 0:start;
			end = end<0 ? spanString.length():end;

			//只绘制可视区域的内容
			See.left = x;
			See.top = y;
			See.right = See.left+width;
			See.bottom = See.top+height;
			onDraw(spanString,text,start,end,startLine,endLine,leftPadding,lineHeight,canvas,paint,See);
		}

		/* 在这里完成绘制 */
		protected void onDraw
		(
		    Spanned spanString, String text,
			int start, int end,
			int startLine,int endLine,
		    float leftPadding, float lineHeight,
			Canvas canvas, TextPaint paint, RectF See
		)
		{
			//重新计算位置
			pos tmp = this.start;
			pos tmp2 = this.end;
			getCursorPos(text,start,tmp);
			tmp2.set(tmp);
			
            //我们只能管理CharacterStyle及其子类的span，抱歉
			Paint.FontMetrics fontMetrics = paint.getFontMetrics();
			float ascent = fontMetrics.ascent;  //根据y坐标计算文本基线坐标
			Object[] spans = spanString.getSpans(0,spanString.length(),CharacterStyle.class);

			//绘制背景的Span
			onDrawBackground(spanString,text,
							 start,end,spans,
							 leftPadding,lineHeight,tmp2,
							 canvas,paint,See);

			//重置画笔绘制文本
			reSetPaint(paint);
			drawText(text,start,end,
					 tmp.x,tmp.y-ascent,
					 leftPadding,lineHeight,
					 canvas,paint,See);

			//绘制行
			onDrawLine(startLine,endLine,
					   0,lineHeight,
					   canvas,paint,See);

			//绘制前景的Span
			onDrawForeground(spanString,text,
							 start,end,spans,
							 leftPadding,lineHeight,tmp,
							 canvas,paint,See);	
			reSetPaint(paint);
			//绘制完成了，重置画笔待下次绘制
		}

		/* 在绘制文本前绘制背景 */
		protected void onDrawBackground
		(
		    Spanned spanString, String text,
		    int start, int end, Object[] spans, 
			float leftPadding, float lineHeight, pos tmp,
			Canvas canvas, TextPaint paint, RectF See
		)
		{
			int index = start;
			//pos tmp = null;
			int s = start, e = end;

			//遍历span
			for(int i=0;i<spans.length;++i)
			{
				if(spans[i] instanceof BackgroundColorSpan)
				{
					//只绘制背景
					BackgroundColorSpan span = (BackgroundColorSpan) spans[i];
					start = spanString.getSpanStart(span);
					end = spanString.getSpanEnd(span);

					//如果span在可视范围内，或其跨越了整个可视范围，就绘制它
					if(!(start>e || end<s))
					{
						//计算光标坐标
						if(tmp==null){
							//第一次获取坐标
							tmp = new pos();
							getCursorPos(text,start,tmp);
						}
						else{
							//如果已有一个坐标，我们尝试直接使用它
							nearOffsetPos(text,index,tmp.x,tmp.y,start,tmp);
						}
						index = start;
						//记录坐标对应的光标

						paint.setColor(span.getBackgroundColor());
						span.updateDrawState(paint);
						//刷新画笔状态
						drawBlock(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint,See);
						//绘制span范围内的文本的背景
					}
				}
		   	}
		}

		/* 在绘制文本后绘制前景 */
		protected void onDrawForeground
		(
		    Spanned spanString, String text, 
		    int start, int end, Object[] spans,
			float leftPadding, float lineHeight, pos tmp,
			Canvas canvas, TextPaint paint, RectF See
		)
		{
			int index = start;
			//pos tmp = null;
			int s = start, e = end;
			Paint.FontMetrics fontMetrics = paint.getFontMetrics();
			float ascent = fontMetrics.ascent;  //根据y坐标计算文本基线坐标

			//遍历span
			for(int i=0;i<spans.length;++i)
			{
				if(!(spans[i] instanceof BackgroundColorSpan) && spans[i] instanceof CharacterStyle)
				{
					//不绘制背景
					CharacterStyle span = (CharacterStyle) spans[i];
					start = spanString.getSpanStart(span);
					end = spanString.getSpanEnd(span);

					//如果span在可视范围内，或其跨越了整个可视范围，就绘制它
					if(!(start>e || end<s))
					{
						//计算光标坐标
						if(tmp==null){
							tmp = new pos();
							getCursorPos(text,start,tmp);
						}
						else{
							//如果已有一个坐标，我们尝试直接使用它
							nearOffsetPos(text,index,tmp.x,tmp.y,start,tmp);
						}
						index = start;
						//记录坐标对应的光标

						//刷新画笔状态
						span.updateDrawState(paint);
						if(span instanceof ReplacementSpan){
							//对于ReplacementSpan，进行特殊处理
							ReplacementSpan re = (ReplacementSpan) span;
							re.draw(canvas,spanString,start,end,tmp.x+leftPadding,(int)tmp.y,0,(int)(tmp.y+lineHeight),paint);
						}
						else{
							//覆盖绘制span范围内的文本
							drawText(text,start,end,tmp.x,tmp.y-ascent,leftPadding,lineHeight,canvas,paint,See);
						}
					}
				}
		   	}
		}

		/* 在绘制文本后绘制行 */
		protected void onDrawLine
		(
		    int startLine, int endLine,
			float leftPadding, float lineHeight, 
			Canvas canvas, TextPaint paint, RectF See
		)
		{
			String line = String.valueOf(endLine);
			float lineWidth = measureText(line,0,line.length(),paint);
			if(See.left > lineWidth+leftPadding){
				//如果x位置已经超出了行的宽度，就不用绘制了
				return ;
			}
			
			float y = startLine*lineHeight;
			Paint.FontMetrics fontMetrics = paint.getFontMetrics();
			y -= fontMetrics.ascent;  //根据y坐标计算文本基线坐标

			//从起始行开始，绘制到末尾行，每绘制一行y+lineHeight
			for(;startLine<=endLine;++startLine)
			{
				line = String.valueOf(startLine);
				canvas.drawText(line,leftPadding,y,paint);
				y+=lineHeight;
			}
		}

		/* 可以方便地调用我绘制Span，返回start指示下次文本应该从哪里开始，为了效率，暂不使用 */
		protected int onDrawSpan
		(
			Spanned spanString, String text, 
		    int start, int end, Object span,
			float x, float y, pos tmp,
			float leftPadding, float lineHeight,
			Canvas canvas, TextPaint paint, RectF See
		)
		{
			if(span instanceof CharacterStyle){
				//刷新画笔
				((CharacterStyle)span).updateDrawState(paint);
			}
			if(span instanceof BackgroundColorSpan)
			{
				//绘制span范围内的文本的背景
				paint.setColor(((BackgroundColorSpan)span).getBackgroundColor());
				drawBlock(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint,See);
			}
			else if(span instanceof ReplacementSpan)
			{
				//对于ReplacementSpan，进行特殊处理
				ReplacementSpan re = (ReplacementSpan) span;
				re.draw(canvas,spanString,start,end,tmp.x+leftPadding,(int)tmp.y,0,(int)(tmp.y+lineHeight),paint);
			}
			else{
				//覆盖绘制span范围内的文本
				drawText(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint,See);
			}
			return start;
		}

		/* 从x,y开始绘制指定范围内的文本，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight，尽量不绘制See范围外的内容 */
		public void drawText(String text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint, RectF See)
		{
			int e = end; 
			x+=leftPadding;
			
			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = text.indexOf(FN,start);
				if(end>=e || end<0)
				{
					if(!(y+lineHeight<See.top || y>See.bottom)){	
					    //如果行在可视范围内，才会绘制
					    canvas.drawText(text,start,e,x,y,paint);
					}
					//start~end之间的内容不会换行，画完就走
					break;
				}
				else
				{
					if(!(y+lineHeight<See.top || y>See.bottom)){	
						canvas.drawText(text,start,end,x,y,paint);
					}
					//start~end之间的内容会换行，之后继续下行
					x = leftPadding;
					y += lineHeight;
				}
				start = end+1;
			}
		}
		
		/* 从x,y开始绘制指定范围内的文本的块，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight，尽量不绘制See范围外的内容 */
		public void drawBlock(String text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint, RectF See)
		{
			float add;
			int e = end; 
			x+=leftPadding;

			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = text.indexOf(FN,start);
				if(end>=e || end<0)
				{
					if(!(y+lineHeight<See.top || y>See.bottom))
					{
						//如果行在可视范围内，才会测量
					    add = measureText(text,start,e,paint);
						if(x+add>See.left){
							//如果宽度大于了可视的左侧，才会绘制
					        canvas.drawRect(x,y,x+add,y+lineHeight,paint);
						}
					}
					//start~end之间的内容不会换行，画完就走
					break;	
				}
				else
				{
					if(!(y+lineHeight<See.top || y>See.bottom))
					{
						add = measureText(text,start,end,paint);
						if(x+add>See.left){
						    canvas.drawRect(x,y,x+add,y+lineHeight,paint);
						}
					}
					//start~end之间的内容会换行，之后继续下行
					x = leftPadding;
					y += lineHeight;
				}
				start = end+1;
			}

		}

		/* 每次文本变化前，都应该手动调用，这将计算我的大小和行 */
		public void measureTextBefore(CharSequence str,int start,int count,int after)
		{
			if(count!=0) 
			{ 
				String text = str.toString();
				int line=StringSpiltor.Count('\n',text,start,start+count); 
				if(line>0)
				{
					//在删除文本前，计算删除的行
					lineCount-=line;    
				}

				//如果删除字符串比当前的maxWidth还宽，重新测量全部文本，找到最大的
				float width = getDesiredWidth(text,tryLine_Start(text,start),tryLine_End(text,start+count),getPaint());
				if(width>=maxWidth){
					setNeedMeasureAllText(true);
				}
			}
		}
		
		/* 每次文本变化后，都应该手动调用，这将计算我的大小和行 */
		public void measureTextAfter(CharSequence str,int start,int count,int after)
		{
			boolean need = true;
			String text = str.toString();	
			TextPaint paint = getPaint();
			if(needMeasureAllText())
			{
				//需要测量全部文本找到剩下的最大宽度
				need = false;
				setNeedMeasureAllText(false);
				maxWidth = (int) getDesiredWidth(text,paint);
			}
			if(count!=0 && need)
			{
				//删除文本后，两行连接为一行，测量这行的宽度
				float width = getDesiredWidth(text,tryLine_Start(text,start),tryLine_End(text,start),paint);
				if(width>maxWidth){
					maxWidth = (int) width;
				}
			}
			if (after != 0)
			{
				int line = StringSpiltor.Count('\n',text,start,start+after);	
				if(line>0)
				{
					//在插入字符串后，计算增加的行
					lineCount+=line;
				}
				if(need)
				{
					//如果插入字符串比当前的maxWidth还宽，就将maxWidth = (int) width
					float width = getDesiredWidth(text,tryLine_Start(text,start),tryLine_End(text,start+after),paint);
					if(width>maxWidth){
						maxWidth = (int) width;
					}
				}
			} 
		}
		
		/* 测量全部文本 */
		public void measureAllText()
		{
			lineCount = StringSpiltor.Count(FN,mText.toString(),0,mText.length());
			maxWidth = (int) getDesiredWidth(mText,mPaint);
		}
		public void setNeedMeasureAllText(boolean is)
		{
			NeddMeasureAll = is;
		}
		public boolean needMeasureAllText()
		{
			return NeddMeasureAll;
		}

		@Override
		public int getLineCount()
		{
			return lineCount+1;
		}
		@Override
		public int getHeight()
		{
			return (int)(getLineCount()*getLineHeight());
		}
		public int maxWidth()
		{
			return maxWidth+500;
		}
		public float getLeftPadding()
		{
			return (String.valueOf(lineCount).length()+1)*getTextSize();
		}

		/* 测量文本长度，非常精确 */
		public float measureText(CharSequence text,int start,int end,TextPaint paint)
		{
			float width = 0;
			int count = end-start;
			widths = widths==null || widths.length<count ? new float[count]:widths;
			paint.getTextWidths(text,start,end,widths);
			for(int i = 0;i<count;++i){
				width+=widths[i];
			}
			return width;
		}
		
		public float getDesiredHeight(String text, int start, int end)
		{
			return StringSpiltor.Count(FN,text,start,end)*getLineHeight();
		}
		@Override
		public int getLineForOffset(int offset)
		{
			return StringSpiltor.Count(FN,mText.toString(),0,offset);
		}
		@Override
		public int getLineForVertical(int vertical)
		{
			int line = (int)(vertical/getLineHeight());
			if(line<0){
				line=0;
			}
			if(line>lineCount){
				line = lineCount;
			}
			return line;
		}
		@Override
		public int getOffsetForHorizontal(int line, float horiz)
		{
			int start = getLineStart(line);
			int end = tryLine_End(mText.toString(),start);
			float width = 0;
			measureText(mText,start,end,mPaint);
			for(;start<end;++start)
			{
				if(width>=horiz){
					break;
				}
				width+=widths[start];
			}
			return start;
		}
		@Override
		public float getLineWidth(int line)
		{
			return measureText(mText.toString(),getLineStart(line),getLineEnd(line),mPaint);
		}
		@Override
		public int getLineStart(int p1)
		{
			return StringSpiltor.NIndex(FN,mText.toString(),0,p1-1);
		}
		@Override
		public int getLineTop(int p1)
		{
			return (int)((p1-1)*getLineHeight());
		}
		@Override
		public int getLineDescent(int p1)
		{
			return (int)(p1*getLineHeight());
		}
		
		/* 获取光标的路径 */
		@Override
		public void getCursorPath(int point, Path dest, CharSequence editingBuffer)
		{
			RectF r = rectF;
			pos p = tmp;
			float lineHeight = getLineHeight();
			float leftPadding = getLeftPadding();
			float width = cursorWidth*mPaint.getTextSize();
			getCursorPos(editingBuffer.toString(),point,p);
			
			r.left=p.x+leftPadding;
			r.top=p.y;
			r.right=r.left+width;
			r.bottom=r.top+lineHeight;
			dest.addRect(r, Path.Direction.CW);
			//添加这一点的Rect
		}
		
		/* 获取选择区域的路径 */
		@Override
		public void getSelectionPath(int start, int end, Path dest)
		{
			String text = mText.toString();
			float lineHeight = getLineHeight();
			float leftPadding = getLeftPadding();
			RectF rf = rectF;
			pos s = tmp;
			pos e = tmp2;
			getCursorPos(text,start,s);
			nearOffsetPos(text,start,s.x,s.y,end,e);
			
			float w = getDesiredWidth(text,start,end,mPaint);
			if(s.y == e.y)
			{
				//单行的情况
				rf.left = s.x+leftPadding;
				rf.top = s.y;
				rf.right = rf.left+w;
				rf.bottom = rf.top+lineHeight;
				dest.addRect(rf,Path.Direction.CW);
				//添加起始行的Rect
				return;
			}
			
			float sw = measureText(text,start,tryLine_End(text,start),mPaint);
			//float ew = measureText(text,tryLine_Start(text,end),end,mPaint);
			
			rf.left = s.x+leftPadding;
			rf.top = s.y;
			rf.right = rf.left+sw;
			rf.bottom = rf.top+lineHeight;
			dest.addRect(rf,Path.Direction.CW);
			//添加起始行的Rect
			
			if((e.y-s.y)/lineHeight > 1){
				//如果行数超过2
			    rf.left = leftPadding;
			    rf.top = rf.top+lineHeight;
			    rf.right = rf.left+w;
			    rf.bottom = e.y;
			    dest.addRect(rf,Path.Direction.CW);
			    //添加中间所有行的Rect
			}
			
			rf.left = leftPadding;
			rf.top = rf.bottom;
			rf.right = rf.left+ e.x;
			rf.bottom = rf.top+lineHeight;
			dest.addRect(rf,Path.Direction.CW);
			//添加末尾行的Rect
		}
		
		/* 获取行的Rect */
		@Override
		public int getLineBounds(int line, Rect bounds)
		{
			bounds.left = 0;
			bounds.top = getLineTop(line);
			bounds.right = (int) (bounds.left+getLineWidth(line));
			bounds.bottom = (int) (bounds.top+getLineHeight());
			return line;
		}

		@Override
		public int getParagraphDirection(int p1){
			return 0;
		}
		@Override
		public boolean getLineContainsTab(int p1){
			return false;
		}
		@Override
		public Layout.Directions getLineDirections(int p1){
			return null;
		}
		@Override
		public int getTopPadding(){
			return 0;
		}
		@Override
		public int getBottomPadding(){
			return 0;
		}
		@Override
		public int getEllipsisStart(int p1){
			return 0;
		}
		@Override
		public int getEllipsisCount(int p1){
			return 0;
		}

	}


	/*试探当前下标所在行的起始*/
	final public static int tryLine_Start(String src,int index)
	{
		int start= src.lastIndexOf('\n',index-1);	
		start = start==-1 ? 0:start+1;
		return start;
	}
    /*试探当前下标所在行的末尾*/
	final public static int tryLine_End(String src,int index)
	{
		int end=src.indexOf('\n',index);
		end = end==-1 ? src.length():end;
		return end;
	}

	/*试探当前下标所在行的起始*/
	final public static int tryLine_Start(CharSequence src,int index)
	{
		index -=1;
		int len = src.length();
		while(index<len && index>0 && src.charAt(index)!=FN){
			--index;
		}
		return index;
	}
    /*试探当前下标所在行的末尾*/
	final public static int tryLine_End(CharSequence src,int index)
	{
		int len = src.length();
		while(index<len && index>0 && src.charAt(index)!=FN){
			++index;
		}
		return index;
	}

	/* 获取光标坐标 */
	final public pos getCursorPos(int offset)
	{
		String str = mText.toString();
		int lines = StringSpiltor.Count(FN,str,0,offset);
		int start = str.lastIndexOf('\n',offset-1);
		start = start == -1 ? 0:start+1;
		float x = mLayout.measureText(str,start,offset,mPaint);
		float y = lines*getLineHeight();
		return new pos(x,y);
	}
	final private void getCursorPos(String str,int offset,pos pos)
	{
		int lines = StringSpiltor.Count(FN,str,0,offset);
		int start = str.lastIndexOf('\n',offset-1);
		start = start == -1 ? 0:start+1;
		pos. x = mLayout.measureText(str,start,offset,mPaint);
		pos. y = lines*getLineHeight();
	}
	
	/* 从坐标获取光标 */
	final public int getOffsetForPosition(float x,float y)
	{
		//String str = mText.toString();
		int lines = mLayout.getLineForVertical((int)y);
		int count = mLayout.getOffsetForHorizontal(lines,x);
		int start = mLayout.getLineStart(lines);
		return start+count;
	}
	
	/* 获取临近光标坐标，可能会更快 */
	final private void nearOffsetPos(String text, int oldOffset, float x, float y, int newOffset, pos target)
	{
		if(oldOffset<newOffset)
		{
			int line = StringSpiltor.Count(FN,text,oldOffset,newOffset);
			int index = text.lastIndexOf(FN,newOffset-1);
			index = index<0 ? 0:index+1;
			index = index>text.length() ? text.length():index;
			target.x = mLayout.measureText(text,index,newOffset,mPaint);
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset)
		{
			int line = StringSpiltor.Count(FN,text,newOffset,oldOffset);
			int index = text.lastIndexOf(FN,newOffset-1);
			index = index<0 ? 0:index+1;
			index = index>text.length() ? text.length():index;
			target.x = mLayout.measureText(text,index,newOffset,mPaint);
			target.y = y-getLineHeight()*line;
		}
	}


/*
________________________________________

 将所有操作交换光标自己，以便实现多光标
________________________________________
*/

	/* 光标 */
	final public class Cursor
	{
		public int selectionStart,selectionEnd;
		public Drawable mDrawable;
		Path mCursorPath;

		Cursor(){
			mDrawable = new DefaultDrawable();
			mCursorPath = new Path();
		}

		public void setSelection(int start,int end)
		{
			selectionStart = start;
			selectionEnd = end;
		}
		public void setDrawable(Drawable draw){
			mDrawable = draw;
		}
		
		public void draw(Canvas canvas){
			mDrawable.draw(canvas);
		}
		public Path getCursorPath()
		{
			mCursorPath.rewind();
			if(selectionStart==selectionEnd){
				mLayout.getCursorPath(selectionStart,mCursorPath,mText);
			}
			else{
			    mLayout.getSelectionPath(selectionStart,selectionEnd,mCursorPath);
			}
			return mCursorPath;
		}

		class DefaultDrawable extends Drawable
		{
			@Override
			public void draw(Canvas p1){
				p1.drawColor(0x25222222);
			}

			@Override
			public void setAlpha(int p1){}

			@Override
			public void setColorFilter(ColorFilter p1){}

			@Override
			public int getOpacity(){
				return 0;
			}
		}
	}

	public void setSelection(int start, int end){
		mCursor.setSelection(start,end);
		mInput.setSelection(start,end);
	}
	public int getSelectionStart(){
		return mCursor.selectionStart;
	}
	public int getSelectionEnd(){
		return mCursor.selectionEnd;
	}
	public void setCursorDrawable(Drawable draw){
		mCursor.setDrawable(draw);
	}


/*
_______________________________________

 当视图被触摸，我们尝试滚动它
_______________________________________
*/

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		return mTouch.onTouch(this,event) && mZoom.onTouch(this,event);
	}

	@Override
	public boolean performClick()
	{
		//int offset = getOffsetForPosition((int)mTouch.lastX,(int)mTouch.lastY);
		//setselection(offset,offset);
		//openInputor(getContext(),this);
		return true;
	}

	@Override
	public void computeScroll()
	{
		//视图被触摸后，就慢慢地停止滚动
		float dx = mTouch.getSlopX();
		float dy = mTouch.getSlopY();
		//scrollBy(dx,dy);
		super.computeScroll();

	}

	final private class EditTouch extends onTouchToMove
	{
		public float slopX,slopY;
		public static final float Max_Slop=500,Min_Slop=10,Once_Slop=5;

		@Override
		public boolean sendMovePos(View v, MotionEvent event, float dx, float dy)
		{
			float x = Math.abs(dx);
			float y = Math.abs(dy);
			if(x>Min_Slop && slopX<Max_Slop){
				slopX+=x/10;		
			}
			if(y>Min_Slop && slopY<Max_Slop){
				slopY+=y/10;	
			}

			//dx+=slopX;
			//dy+=slopY;

			if(event.getHistorySize()==0){
				slopX=0;
				slopY=0;
			}
			else{
				v.scrollBy((int)-dx,(int)-dy);
			}
			return true;
		}

		public float getSlopX(){
			float nowSlopX = slopX-=Once_Slop;
			return nowSlopX;
		}
		public float getSlopY(){
			return slopY-=Once_Slop;
		}	
	}

	final private class EditZoom extends onTouchToZoom
	{	
	    public float scaleX=1,scaleY=1;

		@Override
		public boolean onzoom(View p1, MotionEvent p2, float bili)
		{
			if(bili>1.02){
				scaleX*=1.02;
				scaleY*=1.02;
			}
			else if(bili<0.98){
				scaleX*=0.98;
				scaleY*=0.98;
			}
			//mPaint.setTextSize(mPaint.getTextSize()*scaleX);
			return true;
		}	
	}

	/*InputConnectionWrapper是一个装饰器，指在于在一个已有的InputConnection的基础上扩展功能，该类需要一个InputConnection，并默认就是调用它的实现，可以重写以扩展功能*/

}

