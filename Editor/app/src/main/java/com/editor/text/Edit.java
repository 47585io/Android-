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
import java.util.*;
import com.editor.text.span.*;


public class Edit extends View implements TextWatcher,SelectionWatcher
{

	private Cursor mCursor;
	private ScrollBar mScrollBar;
	private TextPaint mPaint;
	
	private BlockLayout mLayout;
	private EditableBlockList mText;
	private InputConnection mInput;
	
	private TextWatcher mTextWatcher;
	private SelectionWatcher mSelectionWatcher;
	
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
		setClickable(true);
		setLongClickable(true);
		setFocusable(true);
		setDefaultFocusHighlightEnabled(false);
		//设置在获取焦点时不用额外绘制高亮的矩形区域
	}
	private static final void configPaint(TextPaint paint)
	{
		paint.setTextSize(40);
		paint.setColor(0xffaaaaaa);
		paint.setTypeface(Typeface.MONOSPACE);
	}
	public void setText(CharSequence text,int start,int end)
	{
		mText = new EditableBlockList(text,start,end);
		mText.setTextWatcher(this);
		if(mLayout != null){
			int lineColor = mLayout.getLineColor();
			float lineSpacing = mLayout.getLineSpacing();
			mLayout = new BlockLayout(mText,mPaint,lineColor,lineSpacing);
		}
		else{
			mLayout = new BlockLayout(mText,mPaint,0xff666666,1.2f);
		}
	}
	
	public void setTextColor(int color){
		mPaint.setColor(color);
	}
	public void setLineColor(int color){
		mLayout.setLineColor(color);
	}
	public void setTextSize(float size){
		mPaint.setTextSize(size);
	}
	public void setLetterSpacing(float spacing){
		mPaint.setLetterSpacing(spacing);
	}
	public void setLineSpacing(float spacing){
		mLayout.setLineSpacing(spacing);
	}

	public int getTextColor(){
		return mPaint.getColor();
	}
	public int getLineColor(){
		return mLayout.getLineColor();
	} 
	public float getTextSize(){
		return mPaint.getTextSize();
	}
	public float getLetterSpacing(){
		return mPaint.getLetterSpacing();
	}
	public float getLineSpacing(){
		return mLayout.getLineSpacing();
	}
	public float getLineHeight(){
		return mLayout.getLineHeight();
	}
	
	public Editable getText(){
		return mText;
	}
	public TextPaint getPaint(){
		return mPaint;
	}
	public BaseLayout getLayout(){
		return mLayout;
	}
	
	public void append(CharSequence text){
		mText.append(text);
	}
	public void append(CharSequence text,int start,int end){
		mText.append(text,start,end);
	}
	public int getTextWatcherDepth(){
		return mText.getTextWatcherDepth();
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
			onInputContent(text,newCursorPosition,0,0);
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
			onInputContent(null,0,beforeLength,afterLength);	
			return true;
		}

		@Override
		public boolean commitCompletion(CompletionInfo text){
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
	
	/* 输入内容时调用 */
	protected void onInputContent(CharSequence text, int newCursorPosition, int before, int after){
		mCursor.sendInputContent(text,newCursorPosition,before,after);
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
	public void endBatchEdit(){
		--batchEditCount;
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
		int index = start+lengthAfter;
		mCursor.setSelection(index,index);	
	}
	public void afterTextChanged(Editable text)
	{
		if(mTextWatcher!=null){
			mTextWatcher.afterTextChanged(text);
		}	
		if(batchEditCount==0){
			//文本变化后，刷新界面
			invalidate();
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
		//批量编辑时，不可绘制
		if(batchEditCount == 0){
			super.draw(canvas);
		}
	}
	@Override
	protected void onDraw(Canvas canvas)
	{
		//进行绘制，将文本和光标画到画布上
		mCursor.drawBackground(canvas,mPaint);
		long l = System.currentTimeMillis();
		mLayout.draw(canvas);	
		long n = System.currentTimeMillis();
		//Toast.makeText(getContext(),(n-l)+"", 5).show();
		mCursor.drawForeground(canvas,mPaint);
	}
	@Override
	public void onDrawForeground(Canvas canvas){
		//在绘制前景时，绘制滚动条
		mScrollBar.draw(canvas);
	}

	/* 获取光标坐标 */
	public void getCursorPos(int offset, pos p){
		mLayout.getCursorPos(offset,p);
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

	private static final int CursorGlintTime = 60;
	private static final int CursorColor = 0xff99c8ea;
	private static final int SelectionColor = 0x5099c8ea;
	private static final int LineBoundsColor = 0x25616263;

	/* 光标 */
	private final class Cursor
	{
		public int mSelectionStart,mSelectionEnd;
		public float mSelectionStartX, mSelectionStartY;
		public float mSelectionEndX, mSelectionEndY;
		public Path mCursorPath;
		public Rect mLineBounds;
	
		private Cursor(){
			mCursorPath = new Path();
			mLineBounds = new Rect();
		}
		public void setSelection(int start,int end)
		{
			if(start!=mSelectionStart || end!=mSelectionEnd)
			{
				int ost = mSelectionStart;
				int oen = mSelectionEnd;
				if(start != mSelectionStart){
					mSelectionStart = start;
					mSelectionStartX = mLayout.getOffsetHorizontal(start);
					mSelectionStartY = mLayout.getOffsetVertical(start);
				}
				if(end != mSelectionEnd){
					mSelectionEnd = end;
					mSelectionEndX = start == end ? mSelectionStartX : mLayout.getOffsetHorizontal(end);
					mSelectionEndY = start == end ? mSelectionStartY : mLayout.getOffsetVertical(end);
				}
				
				//当光标位置变化，制作新的Path和Rect
				mCursorPath.rewind();
				if(mSelectionStart == mSelectionEnd){
					mLayout.getCursorPath(mSelectionStart,mCursorPath);
				}
				else{
					mLayout.getSelectionPath(mSelectionStart,mSelectionEnd,mCursorPath);
				}
				mLineBounds.set(0,(int)mSelectionStartY,(int)mLayout.getWidth(),(int)(mSelectionStartY+mLayout.getLineHeight()));
				Edit.this.onSelectionChanged(start,end,ost,oen,mText);
			}
		}
		public void sendInputContent(CharSequence text, int newCursorPosition, int before, int after)
		{
			if(text!=null){
				mText.replace(mSelectionStart,mSelectionEnd,text,0,text.length());
			}
			else if(before>0 || after>0)
			{
				int len = mText.length();
				before = before>mSelectionStart ? mSelectionStart:before;
				after = mSelectionEnd+after>len ? len-mSelectionEnd:after;
				mText.replace(mSelectionStart-before,mSelectionEnd+after,"",0,0);
			}
		}
		public void drawBackground(Canvas canvas, Paint paint)
		{
			if(mSelectionStart == mSelectionEnd)
			{
				int saveColor = paint.getColor();
				paint.setColor(LineBoundsColor);
				canvas.drawRect(mLineBounds,paint);
				paint.setColor(saveColor);
			}
		}
		public void drawForeground(Canvas canvas, Paint paint)
		{
			int saveColor = paint.getColor();
			if(mSelectionStart == mSelectionEnd){			
				paint.setColor(CursorColor);			
			}
			else{		
				paint.setColor(SelectionColor);			
			}
			canvas.drawPath(mCursorPath,paint);
			paint.setColor(saveColor);
		}
	}

	public void setSelection(int start, int end){
		mCursor.setSelection(start,end);
	}
	public void setSelection(int index){
		mCursor.setSelection(index,index);
	}
	public int getSelectionStart(){
		return mCursor.mSelectionStart;
	}
	public int getSelectionEnd(){
		return mCursor.mSelectionEnd;
	}
	public void setSelectionWatcher(SelectionWatcher li){
		mSelectionWatcher = li;
	}

	@Override
	public void onSelectionChanged(int start, int end, int oldStart, int oldEnd, CharSequence editor)
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
			float sx = mCursor.mSelectionStartX;
			float sy = mCursor.mSelectionStartY;
			if(sx<x){
				tox = (int) sx;
			}
			else if(sx>x+width){
				tox = (int) sx-width;
			}
			if(sy<y){
				toy = (int) sy;
			}
			else if(sy>y+height){
				toy = (int) sy-height;
			}	
		}
		else
		{
			//文本向后选择导致的光标移动	
			float ex = mCursor.mSelectionEndX;
			float ey = mCursor.mSelectionEndY;	
			if(ex<x){
				tox = (int) ex;
			}
			else if(ex>x+width){
				tox = (int) ex-width;
			}
			if(ey<y){
				toy = (int) ey;
			}
			else if(ey>y+height){
				toy = (int) ey-height;
			}
		}
		scrollTo(tox,toy);
	}

/*
 _______________________________________

 由视图本身管理的滚动，因此滚动条也要自己画
 _______________________________________

*/

    private static final int minScrollLen = 100;
	private static final int scrollWidth = 10;
	private static final int ScrollGnoeTime = 1000;
	private static final int ScrollColor = 0x99aaaaaa;
	

    /* 滚动条 */
    private final class ScrollBar
	{
		private Drawable mScrollDrawable;
		private Rect mHScrollRect,mVScrollRect;
		private boolean canDraw;
		private Runnable mLastRunnable;

		ScrollBar(){
			mHScrollRect = new Rect();
			mVScrollRect = new Rect();
		}

		public void setVRect()
		{
			int x = getScrollX();
			int y = getScrollY();
			int w = getWidth();
			int h = getHeight();

			float by = computeVerticalScrollRange();
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

			float rx = computeHorizontalScrollRange();
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

			float rx = computeHorizontalScrollRange();
			float by = computeVerticalScrollRange();

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
				canDraw = false;
			}
			else if(flag==VISIBLE){
				canDraw = true;
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

/*
 _______________________________________

 当视图被触摸，我们尝试滚动它，当双指触摸，尝试缩放它
 _______________________________________

*/

    /* span是否可以点击 */
    private boolean performClickSpan = false;

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

		    float dx = nowX-lastX;
		    float dy = nowY-lastY;
			consume = super.dispatchTouchEvent(event);
			if(dx>TouchSlop || dx<-TouchSlop || dy>TouchSlop || dy<-TouchSlop){
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
				dx = Math.abs(lastX-(mCursor.mSelectionStartX -sx));
				dy = Math.abs(lastY-(mCursor.mSelectionStartY -sy));

				if(dx<mPaint. getTextSize() && dy<getLineHeight()){
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
					if(scale<0.99 || scale>1.01){
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
				    mScroller.fling(sx,sy,(int)dx,(int)dy,(int)-mLayout.getLineMargin(),(int)(mLayout.getWidth()-getWidth()+ExpandWidth),0,(int)(mLayout.getHeight()-getHeight()+ExpandHeight));
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
	    invalidate();
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
			if(offset<getSelectionStart() || offset>getSelectionEnd()){
				//如果点击了被选择的区域，我们认为用户要删除这块文本
		        setSelection(offset,offset);
			}
			openInputor(getContext(),this);
			if(performClickSpan)
			{
				ClickableSpanX[] spans = mText.quickGetSpans(offset,offset,ClickableSpanX.class);
				for(int i=0;i<spans.length;++i){
					spans[i].onClick(this);
				}
			}
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
		int child = (int)(mLayout.getWidth()+ExpandWidth);
		int min = -(int)mLayout.getLineMargin();
		x = my >= child || x < min ? min:(my + x > child ? child-my:x);

		min = 0;
		my = getHeight();
		child = (int)(mLayout.getHeight()+ExpandHeight);
		y = my >= child || y < min ? min:(my + y > child ? child-my:y);
		super.scrollTo(x, y);
	}
	
	public void setPerformClickSpanEnabled(boolean enabled){
		performClickSpan = enabled;
	}
	
}
