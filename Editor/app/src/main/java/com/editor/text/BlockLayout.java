package com.editor.text;

import android.graphics.*;
import android.text.*;
import com.editor.text.base.*;
import java.util.*;
import android.text.Layout.*;
import android.util.*;


/* 对分块文本容器进行测量的类 */
public abstract class BlockLayout extends Layout implements BlockListener
{

	public static final char FN = '\n', FT = '\t';
	public static final float MinScacle = 0.5f, MaxScale = 2.0f;
	public static final float TextSize = 40f;
	public static final int TextColor = 0xffaaaaaa, LineColor = 0xff666666;
	public static final float LineSpcing = 1.2f, CursorWidthSpcing = 0.1f;

	//临时变得
	private int cacheLine;
	private boolean isStart,isEnd;

	//记录属性
	private int mLineCount;
	private float maxWidth;
	private int mBlockSize;
	
	private float mLineSpacing;
	private float mScaleLayout;
	private float mCursorWidthSpacing;

	//每个文本块，每个块的行数，每个块的宽度
	private EditableBlockList mText;
	private int[] mLines;
	private int[] mStartLines;
	private float[] mWidths;
	

	protected BlockLayout(EditableBlockList text, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, float cursorWidth, float scale)
	{
		super(text,paint,width,align,spacingmult,spacingadd);
		
		mText = text;
		mLines = EmptyArray.INT;
		mStartLines = EmptyArray.INT;
		mWidths = EmptyArray.FLOAT;
		
		//测量所有文本块以初始化数据
		int size = text.getBlockSize();
		onAddBlocks(0,size);
		onBlocksInsertAfter(0,size-1,0,text.getBlock(size-1).length());
		afterBlocksChanged(0,0);
		
		//等待后续的测量
		text.setBlockListener(this);
		mScaleLayout = scale;
		mLineSpacing = spacingmult;
		mCursorWidthSpacing = cursorWidth;
	}
	
	public void setScale(float scale)
	{
		TextPaint paint = getPaint();
		float lastSacle = mScaleLayout;
		float textSize = paint.getTextSize()/mScaleLayout;

		//首先我们缩放文本的大小
		mScaleLayout *= scale;
		mScaleLayout = mScaleLayout<MinScacle ? MinScacle:mScaleLayout;
		mScaleLayout = mScaleLayout>MaxScale ? MaxScale:mScaleLayout;
		paint.setTextSize(textSize*mScaleLayout);

		//我们还应该同步maxWidth的大小
		scale = mScaleLayout/lastSacle;
		for(int j=mBlockSize-1;j>=0;--j){
			mWidths[j] *= scale;
		}
		maxWidth = maxWidth*scale;
	}
	public void setLineSpacing(float lineSpacing){
		mLineSpacing = lineSpacing;
	}
	public void setCursorSpacing(float cursorSpacing){
		mCursorWidthSpacing = cursorSpacing;
	}
	public float getScale(){
		return mScaleLayout;
	}
	public float getLineSpacing(){
		return mLineSpacing;
	}
	public float getcursorSpacing(){
		return mCursorWidthSpacing;
	}

/*
_______________________________________

 文本块事件
_______________________________________

*/

	@Override
	public void onAddBlocks(int i, int count)
	{
		//每次添加文本块，都同步对应的行数和宽度
		for(int j=i+count;i<j;++i)
		{
			mLines = GrowingArrayUtils.insert(mLines,mBlockSize,i,0);
			mStartLines = GrowingArrayUtils.insert(mStartLines,mBlockSize,i,0);
			mWidths = GrowingArrayUtils.insert(mWidths,mBlockSize,i,0);
			mBlockSize++;
		}
	}

	@Override
	public void onRemoveBlocks(int i, int j)
	{
		//每次移除文本块，都同步对应的行数和宽度
		for(--j;i<=j;--j)
		{
			mLineCount -= mLines[j];
			mLines = GrowingArrayUtils.remove(mLines,mBlockSize,j);
			mStartLines = GrowingArrayUtils.remove(mStartLines,mBlockSize,j);
			mWidths = GrowingArrayUtils.remove(mWidths,mBlockSize,j);
			mBlockSize--;
		}
		maxWidth = checkMaxWidth();
	}

	@Override
	public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)
	{
		//在一段连续文本被删除前，测量要删除的起始文本块和末尾文本块
		//若文本被全删，不测量，而是等待移除文本块时同步
		if(i==j){
			//只有一个文本块
			isStart = measureDeleteBlockBefore(i,iStart,jEnd);
		}
		else{
		    isStart = measureDeleteBlockBefore(i,iStart,mText.getBlock(i).length());
			isEnd = measureDeleteBlockBefore(j,0,jEnd);
		}
	}

	@Override
	public void onBlocksDeleteAfter(int i, int j, int iStart, int jEnd)
	{
		//若删除后，文本块没有被移除，就再次测量删除的起始文本块和末尾文本块
		if(i==j && i>-1){
			//只有一个文本块
			measureDeleteBlockAfter(i,iStart,isStart);
		}
		else
		{
			if(i>-1){
				measureDeleteBlockAfter(i,iStart,isStart);
			}
			if(j>-1){
				measureDeleteBlockAfter(j,jEnd,isEnd);
			}
		}
		//无论怎样，清空本次的值
		isStart = false;
		isEnd = false;
	}

	@Override
	public void onBlocksInsertAfter(int i, int j, int iStart, int jEnd)
	{
		//在一段连续文本被插入后，测量它们，文本块在添加时并不测量
		if(i==j){
			//只插入了一个
			measureInsertBlockAfter(i,iStart,jEnd);
		}
		else
		{
			//插入的文本跨越了多个文本块，我们应该全部测量
			measureInsertBlockAfter(i,iStart,mText.getBlock(i).length());
			for(++i;i<j;++i){
				measureInsertBlockAfter(i,0,mText.getBlock(i).length());
			}
			measureInsertBlockAfter(j,0,jEnd);
		}
	}

	@Override
	public void afterBlocksChanged(int i, int iStart)
	{
		//在本次文本块变化后，需要刷新数据
		for(;i<mBlockSize;++i){
			mStartLines[i] = i==0 ? 0:mStartLines[i-1]+mLines[i-1];
		}
	}

/*
_______________________________________

 测量文本块的函数
_______________________________________

*/

	/* 在插入后测量指定文本块的指定范围内的文本的宽和行数，并做出插入决策 */
	private void measureInsertBlockAfter(int id, int start, int end)
	{
		float width = measureBlockWidth(id,start,end);
		int line = cacheLine;
		if(width>maxWidth){
			//如果出现了一个更大的宽，就记录它
			maxWidth = width;
		}
		if(width>mWidths[id]){
			mWidths[id] = width;
		}
		if(line>0){
			//在插入字符串后，计算增加的行
			mLineCount+=line;
			mLines[id] = mLines[id]+line;
		}
	}
	
	/* 在删除前测量指定文本块的指定范围内的文本的宽和行数，并做出删除决策 */
	private boolean measureDeleteBlockBefore(int id, int start, int end)
	{
		boolean is = false;
		GetChars text = mText.getBlock(id);
		//如果文本块不会被全删了，才测量
		if(start!=0 || end!=text.length())
		{
			float width = measureBlockWidth(id,start,end);
			int line = cacheLine;
			if(width>=mWidths[id]){
				//如果删除字符串是当前的块的最大宽度，重新测量当前整个块
				is = true;
			}
			if(line>0){
				//在删除文本前，计算删除的行
				mLineCount -= line;    
				mLines[id] = mLines[id]-line;
			}
		}
		return is;
	}
	
	/* 在删除后测量指定文本块的指定位置的文本的宽，对应measureDeleteBlockBefore，并对其返回值做出回应 */
	private void measureDeleteBlockAfter(int id, int start, boolean needMeasureAllText)
	{
		float width;
		float oldWidth = mWidths[id];
		GetChars text = mText.getBlock(id);
		if(needMeasureAllText){
			//如果需要全部测量
			width = measureBlockWidth(id,0,text.length());
			mWidths[id] = width;
		}
		else{
			//删除文本后，两行连接为一行，测量这行的宽度
			width = measureBlockWidth(id,start,start);
			if(width>mWidths[id]){
				//如果连接成一行的文本比当前的块的最大宽度还宽，就重新设置宽度，并准备与maxWidth比较
				mWidths[id] = width;
			}
		}
		if(width>=maxWidth){
			//当前块的最大宽度比maxWidth大，就记录它
			maxWidth = width;
		}
		else if(oldWidth>=maxWidth){
			//如果当前块原本是最大宽度，现在被删了并且比原来更小，重新检查
			maxWidth = checkMaxWidth();
		}
	}
	
	/* 测量指定文本块的指定范围内的文本的宽，并考虑连接处的宽 */
	private float measureBlockWidth(int i,int start,int end)
	{
		int st = mText.getBlockStartIndex(i);
		start = tryLine_Start(mText,st+start);
		end = tryLine_End(mText,st+end);
		char[] chars = fillChars(mText,start,end);
		float width = getDesiredWidth(chars,0,end-start,getPaint());
		RecylePool.recyleCharArray(chars);
		return width;
	}

/*
_______________________________________

 一些无聊的函数
_______________________________________
  
*/

    /* 寻找行数所在的文本块 */
    public int findBlockIdForLine(int line)
	{
		int div = mLineCount/mBlockSize;
		int id = div>1 ? line/div:line;
		if(id>mBlockSize-1){
			id = mBlockSize-1;
		}
		if(id<0){
			id = 0;
		}
		int nowLine = mStartLines[id];

		//文本块的起始行数实际上是之前的文本块的行数(例如第二块文本块的起始行数就是第一块的行数)
		//这意味着，要寻找指定的换行所在的文本块，并且如果它等于某个文本块的起始行数，此换行应该在上一文本块
		if(nowLine<line){
			for(;id<mBlockSize-1 && mStartLines[id+1]<line;++id){}
		}
		else if(nowLine>line){
			for(;id>0 && mStartLines[id]>=line;--id){}
		}
		else if(nowLine==line){
			id = id==0 ? 0 : id-1;
		}
		return id;
	}
	
	/* 检查最大的宽度 */
	public float checkMaxWidth()
	{
		float width = 0;
		for(int j=mBlockSize-1;j>=0;--j)
		{
			float w = mWidths[j];
			if(w>width){
				width = w;
			}
		}
		return width;
	}
	
/*
 _______________________________________

 接下来我们就可以实现父类的一些方法了
 _______________________________________

*/
	
    @Override
	public int getLineCount(){
		return mLineCount;
	}
	@Override
	public int getHeight(){
		return (int)(mLineCount*getLineHeight());
	}
	public int maxWidth(){
		return (int)maxWidth;
	}
	@Override
	public int getLineTop(int p1){
		return (int)(p1*getLineHeight());
	}
	@Override
	public int getLineDescent(int p1)
	{
		Paint.FontMetrics font = RecylePool.obtainFont();
		getPaint().getFontMetrics(font);
		float descent = font.descent*mLineSpacing;
		RecylePool.recyleFont(font);
		return (int)(getLineTop(p1)+descent);
	}

	/* 获取指定行的起始下标 */
	@Override
	public int getLineStart(int p1)
	{
		//获取第p1个'\n'所在的块
		int id = findBlockIdForLine(p1);
		int startLine = mStartLines[id];
		int startIndex = mText.getBlockStartIndex(id);
		int toLine = p1-startLine;
		int hasLine = mLines[id];

		//寻找剩余的行数的位置
		GetChars str = mText.getBlock(id);
		int len = getText().length();
		int index = toLine<hasLine-toLine ? NIndex(FN,str,0,toLine):lastNIndex(FN,str,str.length()-1,hasLine-toLine+1);
		index+=startIndex;
		index = p1<1 ? 0 : (index<0 ? len : (index+1>len ? len:index+1));
		return index;
	}

	/* 获取指定行的宽度，可能不那么精确 */
	@Override
	public float getLineWidth(int line)
	{
		CharSequence text = getText();
		int start = getLineStart(line); 
		int end = tryLine_End(text,start);
		return getPaint().measureText(text,start,end);
	}

	/* 获取行的高度 */
	public float getLineHeight()
	{
		TextPaint paint = getPaint();
		Paint.FontMetrics font = RecylePool.obtainFont();
		paint.getFontMetrics(font);
		float height = font.bottom-font.top;
		RecylePool.recyleFont(font);
		return height*mLineSpacing;
	}

	/* 获取offset所在的行 */
	@Override
	public int getLineForOffset(int offset)
	{
		int id = mText.findBlockIdForIndex(offset);
		int startLine = mStartLines[id];
		int startIndex = mText.getBlockStartIndex(id);
		GetChars text = mText.getBlock(id);
		startLine+= Count(FN,text,0,offset-startIndex);
		return startLine;
	}

	/* 获取纵坐标指定的行 */
	@Override
	public int getLineForVertical(int vertical)
	{
		int line = (int)(vertical/getLineHeight());
		line = line<0 ? 0 : (line>mLineCount ? mLineCount:line);
		return line;
	}

	/* 获取指定行且指定横坐标处的offset */
	@Override
	public int getOffsetForHorizontal(int line, float horiz)
	{
		CharSequence text = getText();
		int start = getLineStart(line);
		int end = tryLine_End(text,start);
		return measureOffset(text,start,end,horiz,getPaint());
	}

	@Override
	public int getParagraphDirection(int p1){
		return DIR_LEFT_TO_RIGHT;
	}
	/* 行中是否包含tab符号 */
	@Override
	public boolean getLineContainsTab(int p1)
	{
		GetChars text = (GetChars) getText();
		int start = getLineStart(p1);
		int end = tryLine_End(text,start);
		return Count(FT,text,start,end)!=0;
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
	@Override
	public int getEllipsizedWidth(){
		return 0;
	}

	/* 获取光标的路径 */
	@Override
	public void getCursorPath(int point, Path dest, CharSequence editingBuffer)
	{
		RectF r = RecylePool.obtainRect();
		pos p = RecylePool.obtainPos();
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		float width = mCursorWidthSpacing*paint.getTextSize();
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
	@Override
	public void getSelectionPath(int start, int end, Path dest)
	{
		EditableBlockList text = mText;
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		RectF rf = RecylePool.obtainRect();

		pos s = RecylePool.obtainPos();
		pos e = RecylePool.obtainPos();
		getCursorPos(start,s);
		if(end-start>1000){
			getCursorPos(end,e);
		}else{
		    nearOffsetPos(mText,start,s.x,s.y,end,e,getPaint());
		}

		float w = getDesiredWidth(text,start,end,paint);
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
		//float ew = measureText(text,tryLine_Start(text,end),end,mPaint);

		//添加起始行的Rect
		rf.left = s.x;
		rf.top = s.y;
		rf.right = rf.left+sw;
		rf.bottom = rf.top+lineHeight;
		dest.addRect(rf,Path.Direction.CW);	

		if((e.y-s.y)/lineHeight > 1)
		{
			//如果行数超过2
			//添加中间所有行的Rect
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

	/* 获取行的Rect */
	@Override
	public int getLineBounds(int line, Rect bounds)
	{
		bounds.left = 0;
		bounds.top = getLineTop(line);
		bounds.right = (int) (bounds.left+maxWidth());
		bounds.bottom = (int) (bounds.top+getLineHeight());
		return line;
	}

	/* 获取offset处的横坐标，非常精确 */
	@Override
	public float getPrimaryHorizontal(int offset)
	{
		CharSequence text = mText;
		int start = tryLine_Start(text,offset);
		return measureText(text,start,offset,getPaint());
	}
	/* 获取offset处且包含了offset的横坐标 */
	@Override
	public float getSecondaryHorizontal(int offset){
		return getPrimaryHorizontal(offset+1);
	}
	@Override
	public float getLineLeft(int line){
		return 0;
	}
	@Override
	public float getLineRight(int line){
	    return getLineWidth(line);
	}
	@Override
	public float getLineMax(int line){
		return getLineWidth(line);
	}
	@Override
	public int getOffsetToLeftOf(int offset){
		return tryLine_Start(getText(),offset);
	}
	@Override
	public int getOffsetToRightOf(int offset){
		return tryLine_End(getText(),offset);
	}

/*
_______________________________________

  其它的函数
_______________________________________

*/

	/* 获取光标坐标 */
	final public void getCursorPos(int offset, pos pos)
	{  
	    //找到index所指定的块
		int id = mText.findBlockIdForIndex(offset);
		int startLine = mStartLines[id];
		int startIndex = mText.getBlockStartIndex(id);
		GetChars text = mText.getBlock(id);

		//我们仍需要去获取全部文本去测量宽，但是只测量到offset的上一行，然后我们计算它们之间的宽
		pos.x = getPrimaryHorizontal(offset);
		//offset在使用完后转化为当前块的下标，测量当前块的起始到offset之间的行数，并加上之前的行数，最后计算行数的高
		offset = offset-startIndex;
		int lines = Count(FN,text,0,offset)+startLine;
		pos.y = lines*getLineHeight();
	}

	/* 从坐标获取下标 */
	final public int getOffsetForPosition(float x, float y)
	{
		int line = getLineForVertical((int)y);
		int count = getOffsetForHorizontal(line,x);
		return count;
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
	
	/* 为了效;率，我们通常不允许一个一个charAt，而是先获取范围内的chars，再遍历数组 */
	final public float getDesiredWidth(char[] chars, int start, int end, TextPaint paint)
	{
		int last = start;
		float width = 0, w;
		int line = 0;
		for(;start<end;++start)
		{
			if(chars[start]==FN)
			{
				w = paint.measureText(chars,last,start-last);
				width = w>width ? w:width;
				last = start+1;
				++line;
			}
		}
		w = paint.measureText(chars,last,start-last);
		width = w>width ? w:width;
		cacheLine = line;
		return width;
	}
	final public float getDesiredHeight(char[] text, int start, int end)
	{
		cacheLine = Count(text,FN,start,end);
		return cacheLine*getLineHeight();
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
		--index;
		int len = src.length();
		while(index>-1 && index<len)
		{
			if(src.charAt(index)==FN){
				++index;
				break;
			}
			--index;
		}
		return index<0 || index>len ? 0:index;
	}
	//试探当前下标所在行的末尾
	final public static int tryLine_End(CharSequence src,int index)
	{
		int len = src.length();
		while(index>-1 && index<len)
		{
			if(src.charAt(index)==FN){
				break;
			}
			++index;
		}
		return index<0 || index>len ? len:index;
	}
	
	@Override
	public abstract void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)

	@Override
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
