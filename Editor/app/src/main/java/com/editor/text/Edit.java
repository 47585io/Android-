package com.editor.text;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.editor.text.base.*;


public class Edit extends View implements TextWatcher
{ 

	private Cursor mCursor;
	private ScrollBar mScrollBar;
	private TextPaint mPaint;
	
	private myLayout mLayout;
	private EditableList mText;
	private InputConnection mInput;
	
	private TextWatcher mTextWatcher;
	private SelectionWatcher mSelectionWatcher;
	private Editable.Factory mEditableFactory;

	
	public Edit(Context cont)
	{
		super(cont);
		init();
		config();
	}
	
	protected void init()
	{
		mCursor = new Cursor();
		mScrollBar = new ScrollBar();
		mPaint = new TextPaint();
		mInput = new myInput();
		setText("",0,0);
	}
	protected void config()
	{
		configPaint(mPaint);
		setLineColor(0xff666666);
		setClickable(true);
		setLongClickable(true);
		setFocusable(true);
		setDefaultFocusHighlightEnabled(false);
		//设置在获取焦点时不用额外绘制高亮的矩形区域
	}
	protected void configPaint(TextPaint paint)
	{
		paint.setTextSize(33);
		paint.setColor(0xffaaaaaa);
		paint.setTypeface(Typeface.MONOSPACE);
	}
	
	public void setText(CharSequence text,int start,int end)
	{
		mText = new EditableList(text,start,end);
		mText.setTextWatcher(this);
		mText.setEditableFactory(mEditableFactory);
		mLayout = new myLayout(mText,mPaint,Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.2f, 0.1f, 1f);
		setLineColor(0xff666666);
	}
	public void setEditableFactory(Editable.Factory fa)
	{
		mEditableFactory = fa;
		mText.setEditableFactory(fa);
	}
	public void setTextColor(int color){
		mPaint.setColor(color);
	}
	public void setLineColor(int color){
		mLayout.lineColor = color;
	}
	public void setTextSize(float size){
		mPaint.setTextSize(size);
	}
	public void setLetterSpacing(float spacing){
		mPaint.setLetterSpacing(spacing);
	}

	public int getTextColor(){
		return mPaint.getColor();
	}
	public int getLineColor(){
		return mLayout.lineColor;
	} 
	public float getTextSize(){
		return mPaint.getTextSize();
	}
	public float getLineHeight(){
		return mLayout.getLineHeight();
	}
	public float getLetterSpacing(){
		return mPaint.getLetterSpacing();
	}
	public Layout getLayout(){
		return mLayout;
	}
	public TextPaint getPaint(){
		return mPaint;
	}
	public Editable getText(){
		return mText;
	}
	
	public void append(CharSequence text,int start,int end){
		mText.append(text,start,end);
	}
	
/*
 _______________________________________
	 
 当输入事件到来，我们修改文本
 _______________________________________ 

 */
	
	@Override
	public boolean onCheckIsTextEditor(){
		return true;
	}
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs){
		//返回与输入法建立连接的InputConnection
		return mInput;
	}

	/* 是否启用输入 */
	private boolean InputEnabled = true;

	/* 输入法想要输入时，会调用我们的某些方法，此时我们自动修改文本 */
	final private class myInput implements InputConnection
	{

		public myInput(){}

		@Override
		public boolean sendKeyEvent(KeyEvent event)
		{
			//如果输入法要主动输入或删除，不应该调用此方法
			int keyCode = event.getKeyCode();
			int action = event.getAction();

			//手指抬起
			if(action==KeyEvent.ACTION_UP)
			{
				switch(keyCode)
				{
						//如果是一个换行键，无论如何都提交换行
					case KeyEvent.KEYCODE_ENTER:
						commitText(String.valueOf('\n'),0);
						break;
						//如果是一个删除键，无论如何都删除字符
					case KeyEvent.KEYCODE_DEL:
						deleteSurroundingText(1,0);
						break;
				}	
			}
			return true;
		}

		@Override
		public boolean commitText(CharSequence text, int newCursorPosition)
		{
			if(!InputEnabled){
				//没有启用输入，直接返回
				return true;
			}

			//根据要输入的文本，进行输入
			mCursor.sendInputText(text,newCursorPosition,0,0);
			return true;
		}

		@Override
		public boolean deleteSurroundingText(int beforeLength, int afterLength)
		{
			if(!InputEnabled){
				//没有启用输入，直接返回
				return true;
			}

			//准备删除字符
			mCursor.sendInputText(null,0,beforeLength,afterLength);	
			return true;
		}

		@Override
		public boolean commitCompletion(CompletionInfo text)
		{
			//用户选择了输入法的提示栏的一个单词，我们需要获取并插入
			return commitText(text.getText(),0);
		}	

		/* 以下函数懒得实现了 */
		@Override
		public CharSequence getTextBeforeCursor(int p1, int p2){
			return null;
		}
		@Override
		public CharSequence getTextAfterCursor(int p1, int p2){
			return null;
		}
		@Override
		public CharSequence getSelectedText(int p1){
			return null;
		}
		@Override
		public int getCursorCapsMode(int p1){
			return 0;
		}
		@Override
		public ExtractedText getExtractedText(ExtractedTextRequest p1, int p2){
			return null;
		}
		@Override
		public boolean deleteSurroundingTextInCodePoints(int p1, int p2){
			return false;
		}
		@Override
		public boolean setComposingText(CharSequence p1, int p2){
			return false;
		}
		@Override
		public boolean setComposingRegion(int p1, int p2){
			return false;
		}
		@Override
		public boolean finishComposingText(){
			return false;
		}
		@Override
		public boolean commitCorrection(CorrectionInfo p1){
			return false;
		}
		@Override
		public boolean setSelection(int p1, int p2){
			return false;
		}
		@Override
		public boolean performEditorAction(int p1){
			return false;
		}
		@Override
		public boolean performContextMenuAction(int p1){
			return false;
		}
		@Override
		public boolean clearMetaKeyStates(int p1){
			return false;
		}
		@Override
		public boolean reportFullscreenMode(boolean p1){
			return false;
		}
		@Override
		public boolean performPrivateCommand(String p1, Bundle p2){
			return false;
		}
		@Override
		public boolean requestCursorUpdates(int p1){
			return false;
		}
		@Override
		public boolean commitContent(InputContentInfo p1, int p2, Bundle p3){
			return false;
		}
		@Override
		public boolean beginBatchEdit(){
			return false;
		}	
		@Override
		public boolean endBatchEdit(){
			return false;
		}
		@Override
		public Handler getHandler(){
			return Edit.this.getHandler();
		}
		@Override
		public void closeConnection(){}

	}

	/* 是否启用输入 */
	public void setInputEnabled(boolean Enabled){
		InputEnabled = Enabled;
	}

	final public static void openInputor(final Context context, final View editText)
	{
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
	

/*
 _______________________________________

 当文本修改，会触发各种事件，并且立即刷新
 
 设置batchEditCount可以关闭刷新
 _______________________________________ 
 
*/

	private int batchEditCount = 0;
	
	/* 开启一次批量编辑 */
	public void beginBatchEdit(){
		++batchEditCount;
	}
	/* 结束上次批量编辑并立即刷新 */
	public void endBatchEdit()
	{
		--batchEditCount;
		mCursor.refreshForPos();
		invalidate();
	}
	
	/* 可以设置监视文本变化的监视器 */
	public void setTextWatcher(TextWatcher li){
		mTextWatcher = li;
	}

	/* 文本变化时调用文本监视器的方法 */
	public void beforeTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mTextWatcher!=null){
			mTextWatcher.beforeTextChanged(text,start,lenghtBefore,lengthAfter);
		}
	}
	public void onTextChanged(CharSequence text, int start, int lenghtBefore, int lengthAfter)
	{
		if(mTextWatcher!=null){
			mTextWatcher.onTextChanged(text,start,lenghtBefore,lengthAfter);
		}
		if(batchEditCount==0)
		{
			//如果没有开启批量编辑，默认刷新
			//光标的变化，必须在Layout的文本块变化后调用
			//对于批量编辑，导致光标不可见，我们希望在这之后光标保持原位
			int index = start+lengthAfter;
			mCursor.setSelection(index,index);
			invalidate();
		}
	}
	public void afterTextChanged(Editable text)
	{
		if(mTextWatcher!=null){
			mTextWatcher.afterTextChanged(text);
		}	
	}

/*
 _______________________________________

 每次onDraw时会调用Layout绘制文本和光标
 _______________________________________

*/

	@Override
	public void draw(Canvas canvas)
	{
		//在开始绘制时，就使用clipRect放弃绘制可视范围外的内容，节省绘制时间
		float left = getScrollX();
		float top = getScrollY();
		float right = left+getWidth();
		float bottom = top+getHeight();
		canvas.clipRect(left,top,right,bottom);
		//在clipRect后，进行之后的绘制
		super.draw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		Path path = mCursor.getCursorPath();
		//获取光标或选择范围的路径
	    mLayout.draw(canvas,path,mPaint,(int)getSelectionStartPos().y);	
		//进行绘制，将文本和光标画到画布上
	}

	@Override
	public void onDrawForeground(Canvas canvas)
	{
		//在绘制前景时，绘制滚动条
		mScrollBar.draw(canvas);
	}
	
	
	/* 使用Layout进行文本布局和绘制，尽可能地节省时间 */
	final private class myLayout extends BlockLayout
	{

		//行数的颜色
		public int lineColor;
		//专门用于绘制span的画笔，不污染原画笔
		TextPaint spanPaint;	

		//记录本次展示的Span和它们的位置，便于之后使用，在方案2中废弃
		//它的用处是: 用于扩展一些互动性的Span，例如ClickableSpan，具体操作是:
		//在点击编辑器时，在performClick中计算点击位置，遍历spanStarts, spanEnds，确定它在mSpans中的下标，拿出来并回调Click方法
		Object[] mSpans;
		int[] spanStarts, spanEnds;


		public myLayout(EditableList base, TextPaint paint, int width, Layout.Alignment align,float spacingmult, float spacingadd, float cursorWidth, float scale)
		{
			super(base,paint,width,align,spacingmult,spacingadd,cursorWidth,scale);
			spanPaint = new TextPaint(paint);
			lineColor = paint.getColor();
		}

		/* 开始绘制文本和光标 */
		@Override
		public void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)
		{
			//在绘制文本前绘制光标所在行的高亮矩形
			canvas.save();
			//先保存canvas的状态
			Rect rect = mCursor.getLineBounds();
			canvas.clipRect(rect);
			canvas.translate(0,cursorOffsetVertical);
			//裁剪行的矩形，并交给mCursor绘制
			mCursor.draw2(canvas);
			canvas.restore();
			//恢复保存前的状态

			draw(canvas);
			//先绘制文本，之后绘制光标
			//每次调用cancas.drawxxx方法，都会根据当前的状态新建一个图层并绘制，最后canvas显示的内容是所有图层叠加的效果
			//注意哦，已经绘制的内容是不会被改变的，但是对canvas进行平移等操作会影响之后的图层
			//考虑到mCursor默认在(0,0)处绘制，因此需要平移图层到下方，使得在(0,0)处绘制的操作转化为在(x,y)处的绘制
			//并且还需要clipPath，以只绘制指定光标路径的内容 

			canvas.save();
			canvas.clipPath(highlight);
			//在平移前，先裁剪指定路径，但其实在之前已经clipRect了，其实也不会超出Rect的范围
			canvas.translate(0,cursorOffsetVertical);
			//再平移，之后的绘制就默认到(x,y)处了，而我们剪切的范围正是(x,y)处，所以刚好命中范围
			mCursor.draw(canvas);
			//将canvas交给cursor绘制
			canvas.restore();
		}

		/* 开始绘制文本 */
		@Override
		public void draw(Canvas canvas)
		{
			//新一轮的绘制开始了
			Spanned spanString = (Spanned) getText();	
			int len = spanString.length();
			if(len<1){
				//新一轮的绘制结束了
				return;
			}

			//初始化文本和笔
			TextPaint textPaint = getPaint();
			TextPaint spanPaint = this.spanPaint;
			spanPaint.set(textPaint);

			//计算可视区域
			int x = getScrollX();
			int y = getScrollY();
			int width = Edit.this.getWidth();
			int height = Edit.this.getHeight();

			//计算行高和行宽
			float lineHeight = getLineHeight();
			float leftPadding = getLeftPadding();

			//计算可视区域的行
			int startLine = getLineForVertical(y);
			int endLine = getLineForVertical(y+height);

			//计算可视区域的范围
			int start = getLineStart(startLine);
			int end = getLineStart(endLine);

			//只绘制可视区域的内容
			RectF See = rectF;
			See.set(x,y,x+width,y+height);
			onDraw1(spanString,start,end,startLine,endLine,leftPadding,lineHeight,canvas,spanPaint,textPaint,See);
		}

		/* 
		 我不知道哪个方案更省时，所以您可以更改draw中调用的onDraw函数，可以为以下两个方案之一 

		 onDraw1方案:  此方案尽可能地少遍历区间树，只获取一次可见范围内的所有Span并获取它们各自的范围，然后只遍历两次Span数组(第一次是背景，第二次是前景)，由于Span是乱序的，需要计算出Span的坐标后进行绘制
		 如果不是特殊情况，只有获取每个Span各自的范围时才消耗时间(更确切地说，我害怕获取范围是需要遍历整个树的)，每个Span只绘制一次(即使跨越几行)，计算坐标基本不耗时(可以忽略)，并且不会绘制超出范围的部分

		 onDraw2方案:  此方案尽可能地缩小范围，遍历所有可见的行，并计算出本行的可见范围，每行只绘制这么一点点，在每行的绘制中为了避免获取单个Span的范围，使用nextSpanTransition来顺序获取下个区间，然后把区间内的Span全部获取并绘制，这样行行绘制下去
		 如果Span的重叠很严重(例如会跨越几行，或者几个Span挤在一起)，那么会很麻烦，因为这样就会把同一个Span的不同位置遍历几次，这个Span也要连带着被绘制几次		
		 */

		/* 方案1，会调用onDrawBackground，onDrawForeground，onDrawLine */
		protected void onDraw1(Spanned spanString, int start, int end, int startLine, int endLine, float leftPadding, float lineHeight, Canvas canvas, TextPaint spanPaint, TextPaint textPaint, RectF See)
		{
			//重新计算位置
			pos tmp = this.tmp;
			pos tmp2 = this.tmp2;
			tmp.set(0,startLine*lineHeight);
			tmp2.set(tmp);

			//我们只能管理CharacterStyle及其子类的span，抱歉
			textPaint.getFontMetrics(font);
			float ascent = font.ascent; 
			Object[] spans = spanString.getSpans(start,end,CharacterStyle.class);
			mSpans = spans; //保存本次展示的Span

			//getSpans的Span不保证顺序，因此需要获取每个Span的范围
			spanStarts = spanStarts==null||spanStarts.length<spans.length ? new int[spans.length] : spanStarts;
			spanEnds = spanEnds==null||spanEnds.length<spans.length ? new int[spans.length] : spanEnds;
			for(int i=0;i<spans.length;++i)
			{
				spanStarts[i] = spanString.getSpanStart(spans[i]);
				spanEnds[i] = spanString.getSpanEnd(spans[i]);
			}

			//绘制背景的Span
			onDrawBackground(spanString,start,end,spans,spanStarts,spanEnds,0,lineHeight,tmp2,canvas,spanPaint,See);

			//重置画笔绘制文本
			spanPaint.set(textPaint);
			drawText((GetChars)spanString,start,end,tmp.x,tmp.y-ascent,0,lineHeight,canvas,textPaint);

			//绘制行
			int saveColor = textPaint.getColor();
			textPaint.setColor(lineColor);
			onDrawLine(startLine,endLine,-leftPadding,lineHeight,canvas,textPaint,See);
			textPaint.setColor(saveColor);

			//绘制前景的Span
			onDrawForeground(spanString,start,end,spans,spanStarts,spanEnds,0,lineHeight,tmp,canvas,spanPaint,See);	
		}

		/* 在绘制文本前绘制背景 */
		protected void onDrawBackground(Spanned spanString, int start, int end, Object[] spans, int[] spanStarts, int[] spanEnds, float leftPadding, float lineHeight, pos tmp, Canvas canvas, TextPaint paint, RectF See)
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
					start = spanStarts[i];
					end = spanEnds[i];
					start = start<s ? s:start;
					end = end>e ? e:end;
					//超出范围的内容不绘制

					//计算光标坐标
					if(tmp==null){
						//第一次获取坐标
						tmp = new pos();
						getCursorPos(start,tmp);
					}
					else{
						//如果已有一个坐标，我们尝试直接使用它
						nearOffsetPos(index,tmp.x,tmp.y,start,tmp);
					}
					index = start;
					//记录坐标对应的光标

					paint.setColor(span.getBackgroundColor());
					span.updateDrawState(paint);
					//刷新画笔状态
					drawBlock((GetChars)spanString,start,end,tmp.x,tmp.y,leftPadding,lineHeight,canvas,paint);
					//绘制span范围内的文本的背景
				}
		   	}
		}

		/* 在绘制文本后绘制前景 */
		protected void onDrawForeground(Spanned spanString, int start, int end, Object[] spans, int[] spanStarts, int[] spanEnds, float leftPadding, float lineHeight, pos tmp, Canvas canvas, TextPaint paint, RectF See)
		{
			int index = start;
			//pos tmp = null;
			int s = start, e = end;
			paint.getFontMetrics(font);
			float ascent = font.ascent;  //根据y坐标计算文本基线坐标

			//遍历span
			for(int i=0;i<spans.length;++i)
			{
				if(!(spans[i] instanceof BackgroundColorSpan) && spans[i] instanceof CharacterStyle)
				{
					//不绘制背景
					CharacterStyle span = (CharacterStyle) spans[i];
					start = spanStarts[i];
					end = spanEnds[i];
					start = start<s ? s:start;
					end = end>e ? e:end;
					//超出范围的内容不绘制

					//计算光标坐标
					if(tmp==null){
						tmp = new pos();
						getCursorPos(start,tmp);
					}
					else{
						//如果已有一个坐标，我们尝试直接使用它
						nearOffsetPos(index,tmp.x,tmp.y,start,tmp);
					}
					index = start;
					//记录坐标对应的光标

					//刷新画笔状态
					span.updateDrawState(paint);
					//覆盖绘制span范围内的文本
					drawText((GetChars)spanString,start,end,tmp.x,tmp.y-ascent,leftPadding,lineHeight,canvas,paint);
				}
		   	}
		}

		/* 在绘制文本后绘制行 */
		protected void onDrawLine(int startLine, int endLine, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint, RectF See)
		{
			String line = String.valueOf(endLine);
			float lineWidth = measureText(line,0,line.length(),paint);
			if(See.left > lineWidth+leftPadding){
				//如果x位置已经超出了行的宽度，就不用绘制了
				return ;
			}

			float y = startLine*lineHeight;
			paint.getFontMetrics(font);
			y -= font.ascent;  //根据y坐标计算文本基线坐标

			//从起始行开始，绘制到末尾行，每绘制一行y+lineHeight
			for(;startLine<=endLine;++startLine)
			{
				line = String.valueOf(startLine);
				canvas.drawText(line,leftPadding,y,paint);
				y+=lineHeight;
			}
		}

		/* 方案2，会调用drawSingleLineText，onDrawLine */
		protected void onDraw2(Spanned spanString, int start, int end, int startLine, int endLine, float leftPadding, float lineHeight, Canvas canvas, TextPaint spanPaint, TextPaint textPaint, RectF See)
		{
			//先将行数绘制在左侧
			int saveColor = textPaint.getColor();
			textPaint.setColor(lineColor);
			onDrawLine(startLine,endLine,-leftPadding,lineHeight,canvas,textPaint,See);
			textPaint.setColor(saveColor);

			float x = 0;
			float y = startLine*lineHeight;
			int now = start, next;
			int len = spanString.length();

			//遍历可见的行
			for(;startLine<=endLine && now<len;++startLine)
			{
				//获取行的起始和末尾
				next = tryLine_End(now);
				//测量并保存每个字符的宽
				int count = next-now;
				fillChars((GetChars)spanString,now,next);
				fillWidths(chars,0,count,textPaint);
				
				int i = 0;
				float w = x;

				//从第一个字符开始，向后遍历每个字符
				for(;i<count;++i)
				{
					if(w+widths[i]>See.left){
						//如果已经到达了可视区域左边，已经可以确定这行起始下标和坐标了
						break;
					}
					w += widths[i];
					++now;
					//每次下标都加1，坐标加上字符的宽
				}	
				start = now;
				x = w;
				//记录当前行的起始下标和坐标

				//从上次的位置开始，继续向后遍历每个字符
				for(;i<count;++i)
				{
					if(w>=See.right){
						//如果已经到达了可视区域右边，已经可以确定这行末尾下标和坐标了
						break;
					}
					w += widths[i];
					++now;
					//每次下标都加1，坐标加上字符的宽
				}	
				end = now;
				//记录当前行的末尾下标

				drawSingleLineText(spanString,start,end,x,y,lineHeight,canvas,spanPaint,textPaint);
				//绘制这行的可见范围内的文本

				now = next+1;
				x = 0;
				y += lineHeight;
				//之后继续下行
			}
		}

		/* 从(x,y)处开始绘制start和end之间的字符串，并附带Span，但start和end必须在同一行 */
		public void drawSingleLineText(Spanned spanString, int start, int end, float x, float y, float lineHeight, Canvas canvas, TextPaint spanPaint, TextPaint textPaint)
		{
			int next;
			float xStart = x;
			float xEnd;
			//当使用一个成员多次，我们希望在前面声明它，便于以后修改
			Paint.FontMetrics font = this.font;
			textPaint.getFontMetrics(font);

			//正序遍历start~end范围内的区间，并获取区间的Span，计算坐标后绘制它们
			for (int i = start; i < end; i = next) 
			{
				//寻找在当前位置之后，在end之前的下个区间的起始位置
				next = spanString.nextSpanTransition(i, end, CharacterStyle.class);
				//获取区间内的文本
				fillChars((GetChars)spanString, i, next);
				//计算当前区间的末尾坐标
				xEnd = xStart + textPaint.measureText(chars, 0, next-i);

				int j;
				boolean isDrawText = false;
				//获取当前区间内的Span，抱歉，我们只能管理CharacterStyle类型的Span
				CharacterStyle[] spans = spanString.getSpans(i, next, CharacterStyle.class);		

				//遍历Span，首先绘制背景
				for(j = 0; j < spans.length; ++j)
				{
					if(spans[j] instanceof BackgroundColorSpan)
					{
						//如果有背景的Span，刷新画笔状态后画上矩形
						BackgroundColorSpan span = (BackgroundColorSpan) spans[j];
						spanPaint.setColor(span.getBackgroundColor());
						span.updateDrawState(spanPaint);
						canvas.drawRect(xStart, y, xEnd, y+lineHeight, spanPaint);
					}
				}

				//然后遍历Span，只绘制前景
				for(j = 0; j < spans.length; ++j)
				{
					if(!(spans[j] instanceof BackgroundColorSpan))
					{
						//如果有前景的Span，使用Span的颜色绘制文本
						CharacterStyle span = spans[j];
						span.updateDrawState(spanPaint);
						canvas.drawText(chars, 0, next-i, xStart, y-font.ascent, spanPaint);
						isDrawText = true; 
					}
				}

				if(!isDrawText){
					//如果没有绘制前景，则使用默认颜色绘制
					canvas.drawText(chars, 0, next-i, xStart, y-font.ascent, textPaint);
				}

				//继续寻找下个区间
				xStart = xEnd;
			}
		}

		/* 从x,y开始绘制指定范围内的文本，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight */
		public void drawText(GetChars text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint)
		{
			x+=leftPadding;
			int e = end-start;
			fillChars(text,start,end);
			start = 0;

			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = CharArrHelper.indexOf(FN,chars,start);
				if(end>=e || end<0)
				{
					//start~end之间的内容不会换行，画完就走
					canvas.drawText(chars,start,e-start,x,y,paint);		
					break;
				}
				else
				{
					//start~end之间的内容会换行，之后继续下行
					canvas.drawText(chars,start,end-start,x,y,paint);
					x = leftPadding;
					y += lineHeight;
				}
				start = end+1;
			}
		}

		/* 从x,y开始绘制指定范围内的文本的块，如果遇到了换行符会自动换行，每行的x坐标会追加leftPadding，每多一行y坐标会追加lineHeight */
		public void drawBlock(GetChars text, int start, int end, float x, float y, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint)
		{
			x+=leftPadding;
			int e = end-start;
			fillChars(text,start,end);
			start = 0;

			while(start<e)
			{
				//每次从start开始向后找一个换行，把之间的文本画上
				end = CharArrHelper.indexOf(FN,chars,start);
				if(end>=e || end<0)
				{
					//start~end之间的内容不会换行，画完就走
					float add = measureText(chars,start,e,paint);
					canvas.drawRect(x,y,x+add,y+lineHeight,paint);
					break;	
				}
				else
				{	
				    //start~end之间的内容会换行，之后继续下行
					float add = measureText(chars,start,end,paint);
				    canvas.drawRect(x,y,x+add,y+lineHeight,paint);
					x = leftPadding;
					y += lineHeight;
				}
				start = end+1;
			}
		}

		/* 获取应该预留给行数的宽度 */
		public float getLeftPadding()
		{
			TextPaint paint = getPaint();
			String line = String.valueOf(getLineCount());
			float lineWidth = paint.measureText(line);
			return lineWidth+paint.getTextSize();
		}

		/* 检查是否有span被点击了 */
		public void performClickForSpan(int offset)
		{
			if(mSpans==null){
				return;
			}
			for(int i=0;i<mSpans.length;++i)
			{
				if(mSpans[i] instanceof ClickableSpan)
				{
					ClickableSpan span = (ClickableSpan) mSpans[i];
					int start = spanStarts[i];
					int end = spanEnds[i];
					if(start<=offset && end>=offset){
						span.onClick(Edit.this);
					}
				}
			}
		}
	}


	/* 获取光标坐标 */
	public pos getCursorPos(int offset)
	{
		pos tmp = new pos();
		mLayout.getCursorPos(offset,tmp);
		return tmp;
	}
	/* 从坐标获取光标 */
	public int getOffsetForPosition(float x, float y){
		return mLayout.getOffsetForPosition(x,y);
	}
	
	
/*
 ________________________________________

 将所有操作交还光标自己，以便实现多光标，但我太懒了，所以就这样吧
 ________________________________________
 
*/

	/* 光标 */
	final private class Cursor
	{
		public pos startPos, endPos;
		public int selectionStart,selectionEnd;

		public int mCursorGlintTime = 60;
		public Drawable mCursorDrawable;
		public Drawable mSelectionDrawable;
		public Drawable mLineDrawable;

		public Path mCursorPath;
		public Rect mLineBounds;
		public Cursor next;

		private Cursor()
		{
			startPos = new pos();
			endPos = new pos();

			mCursorDrawable = new CursorDrawable();
			mSelectionDrawable = new SelectionDrawable();
			mLineDrawable = new LineDrawable();

			mCursorPath = new Path();
			mLineBounds = new Rect();
		}

		public void setSelection(int start,int end)
		{
			if(start!=selectionStart || end!=selectionEnd)
			{
				if(start!=selectionStart){
					mLayout.getCursorPos(start,startPos);
				}
				if(end!=selectionEnd)
				{
					if(start==end){
						endPos.set(startPos);
					}
					else
					{
						if(end-start>mText.MaxCount){
							mLayout.getCursorPos(end,endPos);
						}
						else{
							mLayout.nearOffsetPos(start,startPos.x,startPos.y,end,endPos);
						}
					}
				}
				selectionStart = start;
				selectionEnd = end;
				onSelectionChanged(start,end);
			}
		}
		public void setPos(float x, float y, float x2, float y2)
		{
			int start=selectionStart;
			int end=selectionEnd;
			if(!startPos.equals(x,y)){
				start = mLayout.getOffsetForPosition(x,y);
			}
			if(x==x2 && y==y2){
				end = start;
			}
			else if(!endPos.equals(x2,y2)){
				end = mLayout.getOffsetForPosition(x2,y2);
			}
			setSelection(start,end);
		}
		public void refreshForPos()
		{
			int start = mLayout.getOffsetForPosition(startPos.x,startPos.y);
			int end = mLayout.getOffsetForPosition(endPos.x,endPos.y);
			setSelection(start,end);
		}
		public void refreshForIndex()
		{
			mLayout.getCursorPos(selectionStart,startPos);
			mLayout.getCursorPos(selectionEnd,endPos);
			onSelectionChanged(selectionStart,selectionEnd);
		}
		/* 先调用自己的onSelectionChanged，完成内部工作，再调用外部的onSelectionChanged */
		public void onSelectionChanged(int start, int end)
		{
			//当光标位置变化，制作新的Path和Rect
			mCursorPath.rewind();
			if(selectionStart==selectionEnd){
				mLayout.getCursorPath(selectionStart,mCursorPath,mText);
			}
			else{
			    mLayout.getSelectionPath(selectionStart,selectionEnd,mCursorPath);
			}
			int line = mLayout.getLineForVertical((int)startPos.y);
			mLayout.getLineBounds(line,mLineBounds);
			Edit.this.onSelectionChanged(start,end);
		}

		public void setCursorDrawable(Drawable draw){
			mCursorDrawable = draw;
		}
		public void setSelectionDrawable(Drawable draw){
			mSelectionDrawable = draw;
		}
		public void setLineHilightDrawable(Drawable draw){
			mLineDrawable = draw;
		}

		/* input不应该自己修改文本，而是交给光标修改 */
		public void sendInputText(CharSequence text,int start,int before,int after)
		{
			Cursor save = next;
			for(;next!=null;next=next.next){
				next.sendInputText(text,start,before,after);
			}
			next = save;

			if(text!=null){
				mText.replace(selectionStart,selectionEnd,text);
			}
			else if(before>0 || after>0)
			{
				int len = mText.length();
				before = before>selectionStart ? selectionStart:before;
				after = selectionEnd+after>len ? len-selectionEnd:after;
				mText.delete(selectionStart-before,selectionEnd+after);
			}
		}

		public int getSelectionStart(){
			return selectionStart;
		}
		public int getSelectionEnd(){
			return selectionEnd;
		}
		public pos getStartPos(){
			return startPos;
		}
		public pos getEndPos(){
			return endPos;
		}

		public void draw(Canvas canvas)
		{
			if(selectionStart==selectionEnd){
				mCursorDrawable.draw(canvas);
			}
			else{
				mSelectionDrawable.draw(canvas);
			}
		}
		public void draw2(Canvas canvas)
		{
			if(selectionStart==selectionEnd){
			    mLineDrawable.draw(canvas);
			}
		}

		public Path getCursorPath(){
			return mCursorPath;
		}
		public Rect getLineBounds(){
			return mLineBounds;
		}

		class CursorDrawable extends NullDrawable
		{
			@Override
			public void draw(Canvas p1){
				p1.drawColor(0xff99c8ea);
			}
		}
		class SelectionDrawable extends NullDrawable
		{
			@Override
			public void draw(Canvas p1){
				p1.drawColor(0x5099c8ea);
			}
		}
		class LineDrawable extends NullDrawable
		{
			@Override
			public void draw(Canvas p1)
			{
				p1.drawColor(0x25616263);
			}
		}
	}

	private static class NullDrawable extends Drawable
	{
		@Override
		public void draw(Canvas p1){}

		@Override
		public void setAlpha(int p1){}

		@Override
		public void setColorFilter(ColorFilter p1){}

		@Override
		public int getOpacity(){
			return 0;
		}
	}

	public void setSelection(int start, int end)
	{
		int len = mText.length();
		if(start>=0&&start<=len && end>=0&&end<=len){
		    mCursor.setSelection(start,end);
		}
	}
	public void setSelection(int index)
	{
		int len = mText.length();
		if(index>=0&&index<=len){
		    mCursor.setSelection(index,index);
		}
	}
	public int getSelectionStart(){
		return mCursor.getSelectionStart();
	}
	public int getSelectionEnd(){
		return mCursor.getSelectionEnd();
	}
	public pos getSelectionStartPos(){
		return mCursor.getStartPos();
	}
	public pos getSelectionEndPos(){
		return mCursor.getEndPos();
	}

	public void setCursorDrawable(Drawable draw){
		mCursor.setCursorDrawable(draw);
	}
	public void setSelectionDrawable(Drawable draw){
		mCursor.setSelectionDrawable(draw);
	}
	public void setLineHilightDrawable(Drawable draw){
		mCursor.setLineHilightDrawable(draw);
	}
	public void setCursorWidth(float spacing){
		//mLayout.setCursorWidthSpacing(spacing);
	}

	public void addCursor(){}

	public void removeCursor(){}

	public void setSelectionWatcher(SelectionWatcher li){
		mSelectionWatcher = li;
	}

	protected void onSelectionChanged(int start, int end)
	{
		//光标移动到超出范围的地方，需要滚动视图
		int x = getScrollX();
		int y = getScrollY();
		int tox = x;
		int toy = y;
		int width = getWidth();
		int height = getHeight();

		if(start==end || nowX+x<cursorX || nowY+y<cursorY)
		{
			//文本被修改或滑动光标导致的位置移动
			//或者，手指向上选择，因此我们只要判断前面的光标
			pos s = getSelectionStartPos();
			if(s.x<x){
				tox = (int) s.x;
			}
			else if(s.x>x+width){
				tox = (int) s.x-width;
			}
			if(s.y<y){
				toy = (int) s.y;
			}
			else if(s.y>y+height){
				toy = (int) s.y-height;
			}	
		}
		else
		{
			//文本向后选择导致的光标移动	
			pos e = getSelectionEndPos();
			if(e.x<x){
				tox = (int) e.x;
			}
			else if(e.x>x+width){
				tox = (int) e.x-width;
			}
			if(e.y<y){
				toy = (int) e.y;
			}
			else if(e.y>y+height){
				toy = (int) e.y-height;
			}
		}
		scrollTo(tox,toy);
	}

/*
 _______________________________________

 由视图本身管理的滚动，因此滚动条也要自己画
 _______________________________________

*/

    private int minScrollLen = 100;
	private int scrollWidth = 10;
	private static final int ScrollGnoeTime=1000;

    /* 滚动条 */
    private final class ScrollBar
	{
		private Drawable mScrollDrawable;
		private Rect mHScrollRect,mVScrollRect;
		private boolean canDraw;
		private Runnable mLastRunnable;

		ScrollBar(){
			mScrollDrawable = new ScrollDrawable();
			mHScrollRect = new Rect();
			mVScrollRect = new Rect();
		}

		public void setVRect()
		{
			int x = getScrollX();
			int y = getScrollY();
			int w = getWidth();
			int h = getHeight();

			float by = getVScrollRange();
			float biliy = y/by*h;
			float leny = h/by*h;
			leny = leny<minScrollLen ? minScrollLen:leny;

			int left = x+w-scrollWidth;
			int top = (int) (y+biliy);
			int right = x+w;
			int bottom = (int) (top+leny);
			mVScrollRect.set(left,top,right,bottom);
		}
		public void setHRect()
		{
			int x = getScrollX();
			int y = getScrollY();
			int w = getWidth();
			int h = getHeight();

			float rx = getHScrollRange();
			float bilix = x/rx*w;
			float lenx = w/rx*w;
			lenx = lenx<minScrollLen ? minScrollLen:lenx;

			int left = (int) (x+bilix);
			int top = y+h-scrollWidth;
			int right = (int) (left+lenx);
			int bottom = y+h;
			mHScrollRect.set(left,top,right,bottom);
		}
		public Rect getVRect(){
			return mVScrollRect;
		}
		public Rect getHRect(){
			return mHScrollRect;
		}

		public void draw(Canvas canvas)
		{
			if(canDraw){
			    drawScrollBar(canvas,mHScrollRect);
			    drawScrollBar(canvas,mVScrollRect);
			}
		}
		private void drawScrollBar(Canvas canvas, Rect r)
		{
			canvas.save();
			canvas.clipRect(r);
			canvas.translate(r.left,r.top);
			mScrollDrawable.draw(canvas);
			canvas.restore();
		}

		/* 将滚动条移动一个距离，需要移动多少真实距离呢 */
		public void moveScroll(float dx,float dy)
		{
			int x = getScrollX();
			int y = getScrollY();
			int w = getWidth();
			int h = getHeight();

			float rx = getHScrollRange();
			float by = getVScrollRange();

			float lenx = mHScrollRect.left-x+dx;
			float leny = mVScrollRect.top-y+dy;

			float bilix = lenx/w;
			float biliy = leny/h;

			int tox = (int) (rx*bilix);
			int toy = (int) (by*biliy);
			scrollTo(tox,toy);
		}

		public void setVisllble(int flag)
		{
			if(flag==GONE){
				mLastRunnable = new R();
				postDelayed(mLastRunnable,ScrollGnoeTime);
			}
			else if(flag==VISIBLE){
				canDraw = true;
				if(mLastRunnable!=null){
				    getHandler().removeCallbacks(mLastRunnable);
				}
			}
		}

		class ScrollDrawable extends NullDrawable
		{
			@Override
			public void draw(Canvas p1){
				p1.drawColor(0x99aaaaaa);
			}
		}

		class R implements Runnable
		{
			@Override
			public void run()
			{
				canDraw = false;
				invalidate();
			}
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l, t, oldl, oldt);
		mScrollBar.setVisllble(VISIBLE);
		mScrollBar.setHRect();
		mScrollBar.setVRect();
		mScrollBar.setVisllble(GONE);
		//只在视图滚动时，才设置滚动条的Rect，并移除上次还未执行的Runnable，并将要在之后消失
	}

	public void setScrollBarWidth(int width){
		scrollWidth = width;
	}
	public void setMinScrollBarLenght(int len){
		minScrollLen = len;
	}
	public void setScrollBarDrawable(Drawable draw){
		mScrollBar.mScrollDrawable = draw;
	}

/*
 _______________________________________

 当视图被触摸，我们尝试滚动它，当双指触摸，尝试缩放它
 _______________________________________

*/

    /* 用于手指离开屏幕后做惯性滚动 */
    private Scroller mScroller = new Scroller(getContext());
	private VelocityTracker mVelocityTracker;

    /* 关键指针的id和坐标 */
    private int id;
	private float lastX,lastY,nowX,nowY;

	/* 第二个指针的id和坐标 */
	private int id2;
	private float lastX2,lastY2,nowX2,nowY2;

	/* 指示下次干什么 */
	private byte flag;
	private static final byte MoveSelf = 0, MoveCursor = 1, MoveHScroll = 3, MoveVScroll=4, Selected = 2;

	/* 指示能做什么事 */
	private byte useFlag;
	private static final byte notClick = 1;

	/* 长按产生的锚点 */
	private int cursorStart;
	private float cursorX,cursorY;

	private static final int TouchSlop = 15;
	private static final int ExpandWidth = 500, ExpandHeight = 1000;
	private static final byte Left = 0, Top = 1, Right = 2, Bottom = 3;


	/* 分发事件，根据情况舎弃事件 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		boolean consume = true;
		if(event.getActionMasked()==MotionEvent.ACTION_DOWN)
		{
			//第一次还需要记录id
			id = event.getPointerId(0);
			lastX=event.getX(0);
			lastY=event.getY(0);
			useFlag = 0; //清除flag
			return super.dispatchTouchEvent(event);	
		}
		else
		{
			//获取坐标，手指上升了，就不能移动了
			int index = event.findPointerIndex(id);
			if(index!=-1)
			{
			    nowX=event.getX(index);
			    nowY=event.getY(index);
			}

			if(event.getPointerCount()==2 && index!=-1)
			{
				//缩放手势，父元素一定不能拦截我
				getParent().requestDisallowInterceptTouchEvent(true);
				//开始记录第二个手指的坐标
				index = index==0 ? 1:0;
				id2 = event.getPointerId(index);
				lastX2=nowX2;
				lastY2=nowY2;
				nowX2=event.getX(index);
			    nowY2=event.getY(index);
				return super.dispatchTouchEvent(event);	
			}

		    int h = isScrollToEdgeH();
			int v = isScrollToEdgeV();
		    float x = nowX-lastX;
		    float y = nowY-lastY;

			if ( (Math.abs(x) > Math.abs(y) 
				&& ((h == Left && x > TouchSlop) 
				|| (h == Right && x < -TouchSlop)))
				|| 
				(Math.abs(x) < Math.abs(y) 
				&& ((v == Top && y > TouchSlop) 
				|| (v == Bottom && y < -TouchSlop)))){
				getParent().requestDisallowInterceptTouchEvent(false);
			}
			else{
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			//手指倾向于x或y轴滑动，滚动条滚动到边缘后仍向外划动，且当前速度超出15，请求父元素拦截，否则自己滚动

			consume = super.dispatchTouchEvent(event);
			if(x>TouchSlop || x<-TouchSlop || y>TouchSlop || y<-TouchSlop){
				//太大幅度的触摸应该判定为滑动，而不是点击或长按
				useFlag = notClick;
			}
			lastX=nowX;
			lastY=nowY;
			//在执行完操作后保存坐标
		}
		return consume;
	}

	/* 消耗事件，根据flag来做事 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float dx, dy;
		int sx = getScrollX(), sy = getScrollY();
		int action = event.getActionMasked();

		if (mVelocityTracker == null) {
			//每次都在事件开始时创建一个计速器
			mVelocityTracker = VelocityTracker.obtain();
		}
		//每次将事件传递过去计算手指速度
        mVelocityTracker.addMovement(event);

		switch(action)
		{
			case MotionEvent.ACTION_DOWN:	
				if (!mScroller.isFinished()) {
					//上次的滑行是否结束，如果没有那么强制结束
					mScroller.abortAnimation();
				}

				//计算光标的位置
				pos cursor = getSelectionStartPos();
				dx = Math.abs(lastX-(cursor.x-sx));
				dy = Math.abs(lastY-(cursor.y-sy));

				if(dx<getTextSize() && dy<getLineHeight()){
					//手指选中了光标
					flag = MoveCursor;
				}
				else if(mScrollBar.getHRect().contains((int)nowX+sx,(int)nowY+sy)){
					//手指选中了滚动条
					flag = MoveHScroll;
				}
				else if(mScrollBar.getVRect().contains((int)nowX+sx,(int)nowY+sy)){
					flag = MoveVScroll;
				}
				else{
					//手指选中了自己
					flag = MoveSelf;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(event.getPointerCount()==2 && event.findPointerIndex(id)!=-1)
				{
					//双指缩放自己
					double len = (float) (Math.pow(nowX-nowX2,2)+Math.pow(nowY-nowY2,2));
					double hlen = (float) (Math.pow(lastX-lastX2,2)+Math.pow(lastY-lastY2,2));
					double scale = len/hlen;
					//根据手指间的距离计算缩放倍数，将textSize缩放
					if(scale<0.95 || scale>1.05){
						//太小的变化不需要去检查
					   // mLayout.setScale((float)scale);
					}
					useFlag = notClick;
					//缩放不是点击或长按
					break;
				}

				dx = nowX-lastX;
				dy = nowY-lastY;
				switch(flag)
				{
						//滚动自己
					case MoveSelf:			
						scrollBy(-(int)dx,-(int)dy);
						break;
						//移动光标
					case MoveCursor:
						int offset = getOffsetForPosition(nowX+sx,nowY+sy);
						setSelection(offset,offset);
						break;
						//开始选择
					case Selected:
						offset = getOffsetForPosition(nowX+sx,nowY+sy);
						if(offset>cursorStart){
							//手指滑动到锚点后
						    setSelection(cursorStart,offset);
						}
						else if(offset<cursorStart){
							//手指滑动到锚点前
							setSelection(offset,cursorStart);
						}	
						break;
						//移动滚动条
					case MoveHScroll:
						mScrollBar.moveScroll(dx, 0);
						break;
					case MoveVScroll:
						mScrollBar.moveScroll(0,dy);
						break;
				}
				break;
			case MotionEvent.ACTION_UP:
				if(flag==MoveSelf)
				{
				    //计算速度并获取应该在x和y轴上滑行的距离
				    mVelocityTracker.computeCurrentVelocity(1000);
				    dx = -mVelocityTracker.getXVelocity(id);
				    dy = -mVelocityTracker.getYVelocity(id);
				    //设置mScroller的滑行值，并准备开始滑行
				    mScroller.fling(sx,sy,(int)dx,(int)dy,(int)-mLayout.getLeftPadding(),(int)mLayout.maxWidth()-getWidth()+ExpandWidth,0,mLayout.getHeight()-getHeight()+ExpandHeight);
				}
			case MotionEvent.ACTION_CANCEL:
				//清除flag
				flag = -1;
				if (mVelocityTracker != null) {
					//在ACTION_CANCEL或ACTION_UP时，回收本次创建的mVelocityTracker
				    mVelocityTracker.recycle();
				    mVelocityTracker = null;
				}
		}
		postInvalidate();
		//期待将来刷新，至少在super.onTouchEvent(event)之后
		return super.onTouchEvent(event);
	}

	/* 每次在draw时都会调用我 */
	@Override
	public void computeScroll()
	{
		//视图被触摸后，就慢慢地滑行一段距离
		if (mScroller.computeScrollOffset()){
			//每次从mScroller中拿出本次应该滑行的距离，同时mScroller内部设置的总滑行值也会减少
			//当总滑行值为0，computeScrollOffset返回false，就不再滑行了
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
	}

	@Override
	public boolean performClick()
	{
		//点击移动光标
		if(useFlag!=notClick)
		{
		    int offset = getOffsetForPosition(nowX+getScrollX(),nowY+getScrollY());
			if(offset>=getSelectionStart()&&offset<=getSelectionEnd()){
				//如果点击了被选择的区域，我们认为用户要删除这块文本
			}
			else{
				//否则设置光标位置
		        setSelection(offset,offset);
			}
			openInputor(getContext(),this);
			mLayout.performClickForSpan(offset);
			//打开输入法，并回调mLayout.performClickForSpan();
		}
		return true;
	}

	@Override
	public boolean performLongClick()
	{
		//长按触发选择
		if(useFlag!=notClick)
		{
	        flag = Selected;
			cursorX = nowX+getScrollX();
			cursorY = nowY+getScrollY();
			cursorStart = getOffsetForPosition(cursorX,cursorY);
			//记录锚点
		}
		return super.performLongClick();
	}

	@Override
	public void scrollTo(int x, int y)
	{
		//不允许滑出范围外
		int my = getWidth();
		int child = (int) mLayout.maxWidth()+ExpandWidth;
		int min = -(int)mLayout.getLeftPadding();
		x = my >= child || x < min ? min:(my + x > child ? child-my:x);

		min = 0;
		my = getHeight();
		child = mLayout.getHeight()+ExpandHeight;
		y = my >= child || y < min ? min:(my + y > child ? child-my:y);
		super.scrollTo(x, y);
	}

	public float getHScrollRange(){
		return mLayout.maxWidth()+ExpandWidth;
	}
	public float getVScrollRange(){
		return mLayout.getHeight()+ExpandHeight;
	}
	private int isScrollToEdgeH()
	{
		int x = getScrollX();
		int r = (int) mLayout.maxWidth()+ExpandWidth;
		int w = getWidth();

		if (x <= -mLayout.getLeftPadding()){
			return Left;
		}
		else if (x + w >= r){
			return Right;
		}
		return -1;
	}
	private int isScrollToEdgeV()
	{
		int y = getScrollY();
		int b = mLayout.getHeight()+ExpandHeight;
		int h = getHeight();

		if(y <= 0){
			return Top;
		}
		else if(y + h >= b){
			return Bottom;
		}
		return -1;
	}

}
