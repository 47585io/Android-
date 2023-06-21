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
import java.util.concurrent.*;
import android.util.*;


/* 不安全的编辑器，使用线程绘制 */
public class UnsafeEdit extends View implements TextWatcher
{

	private Cursor mCursor;
	private Editable mText;
	private Layout mLayout;
	private BaseInputConnection mInput;
	
    private Bitmap mBitmap;
	private TextPaint mPaint;
	
	private Bitmap copyBitmap;
	private Canvas copyCanvas;
	private TextPaint copyPaint;

	private boolean isPrepareing;
	private ThreadPoolExecutor mPool;
	
	private EditTouch mTouch;
	private EditZoom mZoom;
	private TextWatcher mTextListener;
	public static int Delayed_Milis = 50;
	
   
	public UnsafeEdit(Context cont)
	{
		super(cont);
		init();
		config();
	}
	public void init()
	{
		mCursor = new Cursor();
		mPaint=new TextPaint();
		mInput = new myInput(this,true);
		mText = mInput.getEditable();
		mLayout=new DynamicLayout(mText, mPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.2f,true);
		
		mBitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
		mPool = new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
		
		mTouch = new EditTouch();
		mZoom = new EditZoom();
	}
	public void config()
	{
		mPaint.setColor(0xff222222);
		mPaint.setTextSize(66);
		setClickable(true);
		setLongClickable(true);
		setDefaultFocusHighlightEnabled(false);
		//设置在获取焦点时不用额外绘制高亮的矩形区域
	}

	public int getLineCount(){
		return 1000;
	}
	

/*
 _______________________________________

 每次onDraw时会绘制文本，onDrawForeground时绘制光标

 _______________________________________
 */

	@Override
	protected void onDraw(Canvas canvas)
	{	
	    //mText.append("hello\n");

		float x = getScrollX();
		float y = getScrollY();
		int width = getWidth();
		int height = getHeight();

		//绘制本帧的图像
		canvas.clipRect(x,y,x+width,y+height);
		canvas.drawBitmap(mBitmap,x,y,mPaint);

		//预备下帧的图像
		if(!isPrepareing){
		    prepareDraw(x,y,width,height);
		}
	}

	/* 准备指定位置的下帧的图像 */
	final protected void prepareDraw(final float x,final float y,final int width,final int height)
	{
		isPrepareing = true;
		//注意，现在在子线程中，必须使用传递的位置和宽高，否则都是错误的
		Runnable run = new Runnable()
		{
			public void run()
			{
				//我们准备好了正确的画布，图纸和笔
				prepare(width,height);
				TextPaint paint = copyPaint;
				final Bitmap but = copyBitmap;
				Canvas self = copyCanvas;	
				
				self.clipRect(0,0,width,height);
				//剪切画布的范围为位图的范围	
				self.translate(-x,-y);
				//当translate(-x,-y)后，原点变为(-x,-y)，所以所有的绘制坐标会偏移(-x,-y);
				
				float lineWidth = onDrawLine(x,y,width,height,0,getLineHeight(),self,paint);
				//在绘制文本前，画上行
				self.translate(lineWidth,0);
				//在上次的基础上，将文本挤到行的右侧
				
				try{
				    mLayout.draw(self);
					//最后让我们将文本绘制到画布上
				}catch(IndexOutOfBoundsException e){
					Log.e("mText Asynchronous write And read Exception","A IndexOutOfBoundsException at mLayout.draw() in prepareDraw() Happened!");
					//因为Layout需要在子线程中绘制，但mText会在主线程中修改，因此读取的内容在随时改变，导致在绘制时绘制了超出范围的内容
				}

				post(new Runnable()
					{
						public void run()
						{
							if(!mBitmap.equals(but)){
								//如果是不同的Bitmap，需要回收之前的Bitmap
								mBitmap.recycle(); 
							}
							mBitmap = but;
							isPrepareing = false;
							invalidate();
							//在主线程中将画好的but提交，准备绘制下一帧
						}
					});
			}
		};
		mPool.execute(run);
	}
	
	/* 在绘制文本前从指定位置开始绘制行，并返回应该预留给行的宽度 */
	final protected float onDrawLine(float x, float y, int width, int height, float leftPadding, float lineHeight, Canvas canvas, TextPaint paint)
	{
		float lineWidth = String.valueOf(getLineCount()).length()*paint.getTextSize();
		if(x>lineWidth+leftPadding){
			//如果x位置已经超出了行的宽度，就不用绘制了
			return lineWidth;
		}
		
		int startLine = (int)(y/lineHeight);
		int endLine = (int) ((y+height)/lineHeight);
		Paint.FontMetrics fontMetrics = paint.getFontMetrics();
		y -= fontMetrics.ascent;  //根据y坐标计算文本基线坐标

		//从起始行开始，绘制到末尾行，每绘制一行y+lineHeight
		for(;startLine<=endLine;++startLine)
		{
			String line = String.valueOf(startLine);
			canvas.drawText(line,leftPadding,y,paint);
			y+=lineHeight;
		}
		return lineWidth;
	}
	
	/* 准备画布，位图和笔 */
	final private void prepare(final int width,final int height)
	{
		//其实如果大小不变，不用创建新的位图和画布，但是这需要清空上次的绘制内容并恢复画布状态
		//但是如果让mBitmap和copyBitmap相同，又会造成异步读写
		//我可以在主线程的提交中每次将copyBitmap拷贝出一个新的Bitmap给mBitmap，但会浪费主线程时间
		
		/*
		if(copyBitmap==null || copyBitmap.getWidth()!=width || copyBitmap.getHeight()!=height){
			copyBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444);
			copyCanvas = null;
		}
		
		if(copyCanvas==null){
		    copyCanvas = new Canvas(copyBitmap);
		}
		else{
			copyCanvas.restore();
			copyCanvas.drawColor(0,PorterDuff.Mode.CLEAR);
		}
		copyCanvas.save();
		*/
		
		copyBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444);
		copyCanvas = new Canvas(copyBitmap);
		
		//copyPaint应该保持mPaint的样式
		if(copyPaint==null){
			copyPaint = new TextPaint(mPaint);
		}
		else{
			copyPaint.set(mPaint);
		}
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
		act.getWindow().setSoftInputMode(
			WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
		);
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
			//提交缓冲区内的文本
			SpannableStringBuilder b = new SpannableStringBuilder(text);
			b.setSpan(new BackgroundColorSpan(0xffffed55),0,b.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			b.setSpan(new ForegroundColorSpan(0xffccbbaa),0,b.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			int start = getSelectionStart();
			int after = text.length();

			//编辑器改变前的内容
			if(mTextListener!=null){
				mTextListener.beforeTextChanged(mText,start,0,after);
			}
			beforeTextChanged(mText,start,0,after);
			mText.insert(0,b);

			//编辑器改变后的内容
			if(mTextListener!=null){
				mTextListener.onTextChanged(mText,start,0,after);
			}
			onTextChanged(mText,start,0,after);
			//invalidate();

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
			//删除字符
			int start = getSelectionStart();

			//编辑器改变前的内容
			if(mTextListener!=null){
				mTextListener.beforeTextChanged(mText,start,beforeLength,0);
			}
			beforeTextChanged(mText,start,beforeLength,0);
			mText.delete(mText.length()-beforeLength,mText.length());

			//编辑器改变后的内容
			if(mTextListener!=null){
				mTextListener.onTextChanged(mText,start,beforeLength,0);
			}
			onTextChanged(mText,start,beforeLength,0);
			//invalidate();

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
			//invalidate();
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
		//mCursor.setSelection(index,index);
	}
	@Override
	public void beforeTextChanged(CharSequence p1, int start, int lenghtBefore, int lengthAfter)
	{

	}
	@Override
	public void afterTextChanged(Editable p1)
	{

	}


/*________________________________________________________________________________*/

	/* 光标 */
	final public class Cursor
	{
		public int x,y;
		public int selectionStart,selectionEnd;
		public Drawable mDrawable;

		Cursor(){
			mDrawable = new D();
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

		class D extends Drawable
		{

			@Override
			public void draw(Canvas p1)
			{
				p1.drawRect(x,y,x+10,x+66,mPaint);
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
		super.computeScroll();

	}

	final private static class EditTouch extends onTouchToMove
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


	public float getLineHeight(){
		return mPaint.getTextSize()*1.2f;
	}

	/* 获取光标坐标 */
	final public pos getCursorPos(int offset)
	{
		String str = mText.toString();
		int lines = StringSpiltor.Count('\n',str,0,offset);
		int start = str.lastIndexOf('\n',offset-1);
		start = start == -1 ? 0:start+1;
		float x = Layout.getDesiredWidth(str,start,offset,mPaint);
		float y = lines*getLineHeight();
		return new pos(x,y);
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


}
