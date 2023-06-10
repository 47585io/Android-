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
			if(mTextListener!=null){
				mTextListener.beforeTextChanged(mText,start,0,after);
			}
			beforeTextChanged(mText,start,0,after);
			mText.insert(0,b);

			//编辑器改变后的内容
			mLayout.measureTextAfter(mText,start,0,after);
			if(mTextListener!=null){
				mTextListener.onTextChanged(mText,start,0,after);
			}
			onTextChanged(mText,start,0,after);
			invalidate();

			//编辑器显示的内容
			if(mTextListener!=null){
				mTextListener.afterTextChanged(mText);
			}
			afterTextChanged(mText);
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
			int start = getSelectionStart();

			//编辑器改变前的内容
			mLayout.measureTextBefore(mText,start,beforeLength,0);
			if(mTextListener!=null){
				mTextListener.beforeTextChanged(mText,start,beforeLength,0);
			}
			beforeTextChanged(mText,start,beforeLength,0);
			mText.delete(mText.length()-beforeLength,mText.length());

			//编辑器改变后的内容
			mLayout.measureTextAfter(mText,start,beforeLength,0);
			if(mTextListener!=null){
				mTextListener.onTextChanged(mText,start,beforeLength,0);
			}
			onTextChanged(mText,start,beforeLength,0);
			invalidate();

			//编辑器显示的内容
			if(mTextListener!=null){
				mTextListener.afterTextChanged(mText);
			}
			afterTextChanged(mText);
			return true;
		}

		@Override
		public boolean finishComposingText()
		{
			//组合文本
			return true;
		}

	}


	@Override
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		//文本变化了，设置光标位置
		int index = 0;
		if(lenghtBefore!=0){
			index = start+lenghtBefore;
		}
		if(lengthAfter!=0){
			index = start+lengthAfter;
		}
		mCursor.setSelection(index,index);
	}
	@Override
	public void beforeTextChanged(CharSequence p1, int start, int lenghtBefore, int lengthAfter)
	{

	}
	@Override
	public void afterTextChanged(Editable p1)
	{

	}


	/*
	 _______________________________________

	 每次onDraw时会绘制文本，onDrawForeground时绘制光标

	 _______________________________________
	 */

    /* onDraw是View绘制的第二步，第一步是drawBackground，但无法重写 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		//在第二步的绘制时，就使用clipRect放弃绘制可视范围外的内容，节省绘制时间
		int left = getScrollX();
		int top = getScrollY();
		int right = left+getWidth();
		int bottom = top+getHeight();
		canvas.clipRect(left,top,right,bottom);
		//在clipRect后，进行之后的绘制，并且之后绘制的第三，四，五，六步也会生效

		//进行绘制，将文本画到画布上
		mLayout.draw(canvas);	
	}

	/* onDrawForeground是View绘制的最后一步 */
	public void onDrawForeground(Canvas canvas)
	{
		//每次调用cancas.drawxxx方法，都会根据当前的状态新建一个图层并绘制，最后canvas显示的内容是所有图层叠加的效果
		//注意哦，已经绘制的内容是不会被改变的，但是对canvas进行平移等操作会影响之后的图层
		//考虑到mCursor默认在(0,0)处绘制，因此需要平移图层到下方，使得在(0,0)处绘制的操作转化为在(x,y)处的绘制
		//并且还需要clipPath，以只绘制指定光标路径的内容

		float x = getScrollX();
		float y = getScrollY();
		Path path = mCursor.getCursorPath();
		//获取光标或选择范围的路径
		canvas.clipPath(path);
		//在平移前，先裁剪指定路径，但其实在之前已经clipRect了，其实也不会超出Rect的范围
		canvas.translate(x,y);
		//再平移，之后的绘制就默认到(x,y)处了，而我们剪切的范围正是(x,y)处，所以刚好命中范围
	    mCursor.draw(canvas);
		//将canvas交给CurSor绘制
	}


	/* 使用Layout进行文本布局和绘制，尽可能地节省时间 */
	final private class myLayout extends Layout
	{

		float[] widths;
		int lineCount=1, maxWidth;
		boolean NeddMeasureAll;

		public myLayout(java.lang.CharSequence base, android.text.TextPaint paint, 
		                int width, android.text.Layout.Alignment align,
						float spacingmult, float spacingadd) {
			super(base,paint,width,align,spacingmult,spacingadd);
		}

		@Override
		public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)
		{
			draw(canvas);
			float x = getScrollX();
			float y = getScrollY();
			canvas.clipPath(highlight);
			//在平移前，先裁剪指定路径，但其实在之前已经clipRect了，其实也不会超出Rect的范围
			canvas.translate(x,y);
			//再平移，之后的绘制就默认到(x,y)处了，而我们剪切的范围正是(x,y)处，所以刚好命中范围
			mCursor.draw(canvas);
		}

		/* 开始绘制 */
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
			onDraw(spanString,text,
			       x,y,width,height,
			       start,end,startLine,endLine,
				   leftPadding,lineHeight,canvas,paint);
		}

		/* 在这里完成绘制 */
		protected void onDraw
		(
		    Spanned spanString, String text,
			float x, float y,int width, int height,
	        int start, int end, int startLine, int endLine,
		    float leftPadding, float lineHeight, Canvas canvas, TextPaint paint
		)
		{
			//我们只能管理CharacterStyle及其子类的span，抱歉
			pos tmp = new pos();
			getCursorPos(text,start,tmp);
			Object[] spans = spanString.getSpans(0,spanString.length(),CharacterStyle.class);

			//绘制背景的Span
			reSetPaint(paint);
			onDrawBackground(spanString,text,
							 start,end,spans,
							 leftPadding,lineHeight,new pos(tmp),
							 canvas,paint);

			//绘制文本
			reSetPaint(paint);
			drawText(text,start,end,
					 tmp.x,tmp.y,
					 leftPadding,lineHeight,
					 canvas,paint);

			//绘制行
			onDrawLine(x,y,
			           startLine,endLine,
					   0,lineHeight,
					   canvas,paint);

			//绘制前景的Span
			onDrawForeground(spanString,text,
							 start,end,spans,
							 leftPadding,lineHeight,tmp,
							 canvas,paint);	
		}

		/* 在绘制文本前绘制背景 */
		protected void onDrawBackground
		(
		    Spanned spanString, String text,
		    int start, int end, Object[] spans, 
			float leftPadding, float lineHeight, pos tmp,
			Canvas canvas, TextPaint paint
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
					if((start>=s&&start<=e) || (end>=s&&end<=e) || (s>=start&&e<=end))
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
						drawBlock(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint);
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
			Canvas canvas, TextPaint paint
		)
		{
			int index = start;
			//pos tmp = null;
			int s = start, e = end;

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
					if((start>=s&&start<=e) || (end>=s&&end<=e) || (s>=start&&e<=end))
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
							drawText(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint);
						}
					}
				}
		   	}
		}

		/* 在绘制文本后绘制行 */
		protected void onDrawLine
		(
		    float x, float y,
		    int startLine, int endLine,
			float leftPadding, float lineHeight, 
			Canvas canvas, TextPaint paint
		)
		{
			String line = String.valueOf(endLine);
			float lineWidth = measureText(line,0,line.length(),paint);
			if(x>lineWidth+leftPadding){
				//如果x位置已经超出了行的宽度，就不用绘制了
				return ;
			}
			
			y = startLine*lineHeight;
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
			Canvas canvas, TextPaint paint
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
				drawBlock(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint);
			}
			else if(span instanceof ReplacementSpan)
			{
				//对于ReplacementSpan，进行特殊处理
				ReplacementSpan re = (ReplacementSpan) span;
				re.draw(canvas,spanString,start,end,tmp.x+leftPadding,(int)tmp.y,0,(int)(tmp.y+lineHeight),paint);
			}
			else{
				//覆盖绘制span范围内的文本
				drawText(text,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint);
			}
			return start;
		}

		/* 从x,y开始绘制指定范围内的文本，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight */
		public void drawText(String text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint)
		{
			int e = end; 
			x+=leftPadding;
			Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
			y -= fontMetrics.ascent;  //根据y坐标计算文本基线坐标

			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = text.indexOf(FN,start);
				if(end<0 || end>=e){
					//start~end之间的内容不会换行，画完就走
					canvas.drawText(text,start,e,x,y,paint);
					break;
				}
				else{
					//start~end之间的内容会换行，之后继续下行
					canvas.drawText(text,start,end,x,y,paint);
					x = leftPadding;
					y += lineHeight;
				}
				start = end+1;
			}
		}
		/* 从x,y开始绘制指定范围内的文本的块，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight */
		public void drawBlock(String text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint)
		{
			float add;
			int e = end; 
			x+=leftPadding;

			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = text.indexOf(FN,start);
				if(end<0 || end>=e){
					//start~end之间的内容不会换行，画完就走
					add = measureText(text,start,e,paint);
					canvas.drawRect(x,y,x+add,y+lineHeight,paint);
					break;
				}
				else{
					//start~end之间的内容会换行，之后继续下行
					add = measureText(text,start,end,paint);
					canvas.drawRect(x,y,x+add,y+lineHeight,paint);
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

		public void setNeedMeasureAllText(boolean is){
			NeddMeasureAll = is;
		}
		public boolean needMeasureAllText(){
			return NeddMeasureAll;
		}

		@Override
		public int getLineCount(){
			return lineCount+1;
		}
		@Override
		public int getHeight(){
			return (int)(getLineCount()*getLineHeight());
		}
		public int maxWidth(){
			return maxWidth;
		}
		public int getLeftPadding(){
			return (int)((String.valueOf(lineCount).length()+1)*getTextSize());
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

		@Override
		public int getLineTop(int p1){
			return 0;
		}
		@Override
		public int getLineDescent(int p1){
			return 0;
		}
		@Override
		public int getLineStart(int p1){
			return 0;
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
		String str = mText.toString();
		int lines = mLayout.getLineForVertical((int)y);
		int count =  (int) (x/mPaint.getTextSize());
		int start = StringSpiltor.NIndex('\n',str,0,lines);
		return start+count;
	}
	/* 获取临近光标坐标，可能会更快 */
	final public void nearOffsetPos(String text, int oldOffset, float x, float y, int newOffset, pos target)
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
		public int x,y;
		public int selectionStart,selectionEnd;
		public Drawable mDrawable;
		Path mCursorPath;

		Cursor(){
			mDrawable = new D();
			mCursorPath = new Path();
		}

		public void setSelection(int start,int end)
		{
			selectionStart = start;
			selectionEnd = end;
		}
		public void setPos(int x, int y)
		{
			this.x = x;
			this.y = y;
			selectionStart = getOffsetForPosition(x,y);
			selectionEnd = selectionStart;
		}

		public void setDrawable(Drawable draw){
			mDrawable = draw;
		}
		public void draw(Canvas canvas){
			mDrawable.draw(canvas);
		}
		public Path getCursorPath(){
			mLayout.getSelectionPath(selectionStart,selectionEnd,mCursorPath);
			return mCursorPath;
		}

		class D extends Drawable
		{

			@Override
			public void draw(Canvas p1)
			{
				p1.drawColor(0xff222222);
			}

			@Override
			public void setAlpha(int p1)
			{
				// TODO: Implement this method
			}

			@Override
			public void setColorFilter(ColorFilter p1)
			{
				// TODO: Implement this method
			}

			@Override
			public int getOpacity()
			{
				// TODO: Implement this method
				return 0;
			}

		}

	}

	public void setselection(int start, int end){
		mCursor.setSelection(start,end);
		mInput.setSelection(start,end);
	}
	public int getSelectionStart(){
		return mCursor.selectionStart;
	}
	public int getSelectionEnd(){
		return mCursor.selectionEnd;
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
		int offset = getOffsetForPosition((int)mTouch.lastX,(int)mTouch.lastY);
		setselection(offset,offset);
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


}

