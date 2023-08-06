package com.editor.text;

import android.graphics.*;
import android.text.*;
import com.editor.text.base.*;
import java.util.*;
import android.text.Layout.*;


public abstract class BlockLayout extends Layout implements EditableList.BlockListener
{

	public static final char FN = '\n', FT = '\t';
	public static final float MinScacle = 0.5f, MaxScale = 2.0f;
	public static final float TextSize = 40f;
	public static final int TextColor = 0xffaaaaaa;
	public static int TabSize = 4;

	//临时变量
	protected char[] chars;
	protected float[] widths;
	protected RectF rectF = new RectF();
	protected pos tmp = new pos(), tmp2 = new pos();
	protected Paint.FontMetrics font = new Paint.FontMetrics();
	
	private int cacheLine, cacheLen, cacheId;
	private boolean isStart,isEnd;

	//记录属性
	private int lineCount;
	private float maxWidth;
	private float cursorWidth;
	private float lineSpacing;
	private float scaleLayout;

	//每个文本块，每个块的行数，每个块的宽度
	private List<Editable> mBlocks;
	private List<Integer> mLines;
	private List<Float> mWidths;
	

	public BlockLayout(EditableList text, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, float cursorWidth, float scale)
	{
		super(text,paint,width,align,spacingmult,spacingadd);
		
		mBlocks = text.mBlocks;
		mLines = new ArrayList<>();
		mWidths = new ArrayList<>();
		int size = mBlocks.size();
		//测量所有文本块以初始化数据
		for(int i=0;i<size;++i){
			onAddBlock(i);
			measureInsertBlockAfter(i,0,mBlocks.get(i).length());
		}
		
		//等待后续的测量
		text.setBlockListener(this);
		scaleLayout = scale;
		lineSpacing = spacingmult;
		this.cursorWidth = cursorWidth;
	}
	
	@Override
	public void onAddBlock(int i)
	{
		//每次添加文本块，都同步对应的行数和宽度
		mLines.add(i,0);
		mWidths.add(i,0f);
	}

	@Override
	public void onRemoveBlock(int i)
	{
		//每次移除文本块，都同步对应的行数和宽度
		lineCount-=mLines.get(i);
		mLines.remove(i);
		mWidths.remove(i);
		maxWidth = checkMaxWidth();
	}

	@Override
	public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)
	{
		//在一段连续文本被删除前，测量要删除的起始文本和末尾文本块
		//若文本被全删，不测量，而是等待移除文本块时同步
		if(i==j){
			//只有一个文本块
			isStart = measureDeleteBlockBefore(i,iStart,jEnd);
		}
		else{
		    isStart = measureDeleteBlockBefore(i,iStart,mBlocks.get(i).length());
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
		//在一段连续文本被插入后，测量它们
		//文本块在添加时并不测量
		measureInsertBlocksAfter(i,j,iStart,jEnd);
	}
	
/*
_______________________________________

 测量文本的函数
_______________________________________

*/
	
	/* 在插入后测量指定范围内的文本和文本块 */
	private void measureInsertBlocksAfter(int i, int j, int iStart, int jEnd)
	{
		if(i==j){
			//只插入了一个
			measureInsertBlockAfter(i,iStart,jEnd);
		}
		else
		{
			//插入的文本跨越了多个文本块，我们应该全部测量
			measureInsertBlockAfter(i,iStart,mBlocks.get(i).length());
			for(++i;i<=j;++i){
				measureInsertBlockAfter(i,0,mBlocks.get(i).length());
			}
			measureInsertBlockAfter(j,0,jEnd);
		}
	}
	/* 在插入后测量指定文本块的指定范围内的文本的宽和行数，并做出插入决策 */
	private void measureInsertBlockAfter(int id, int start, int end)
	{
		float width = measureBlockWidth(id,start,end);
		int line = cacheLine;
		if(width>maxWidth){
			//如果出现了一个更大的宽，就记录它
			maxWidth = width;
		}
		if(width>mWidths.get(id)){
			mWidths.set(id,width);
		}
		if(line>0){
			//在插入字符串后，计算增加的行
			lineCount+=line;
			mLines.set(id,mLines.get(id)+line);
		}
	}
	/* 在删除前测量指定文本块的指定范围内的文本的宽和行数，并做出删除决策 */
	private boolean measureDeleteBlockBefore(int id, int start, int end)
	{
		boolean is = false;
		GetChars text = mBlocks.get(id);
		//如果文本块不会被全删了，才测量
		if(start!=0 || end!=text.length())
		{
			float width = measureBlockWidth(id,start,end);
			int line = cacheLine;
			if(width>=mWidths.get(id)){
				//如果删除字符串是当前的块的最大宽度，重新测量当前整个块
				is = true;
			}
			if(line>0){
				//在删除文本前，计算删除的行
				lineCount-=line;    
				mLines.set(id,mLines.get(id)-line);
			}
		}
		return is;
	}
	/* 在删除后测量指定文本块的指定位置的文本的宽，对应measureDeleteBlockBefore，并对其返回值做出回应 */
	private void measureDeleteBlockAfter(int id, int start, boolean needMeasureAllText)
	{
		float width;
		float w = mWidths.get(id);
		GetChars text = mBlocks.get(id);
		if(needMeasureAllText){
			//如果需要全部测量
			width = measureBlockWidth(id,0,text.length());
			mWidths.set(id,width);
		}
		else{
			//删除文本后，两行连接为一行，测量这行的宽度
			width = measureBlockWidth(id,start,start);
			if(width>mWidths.get(id)){
				//如果连接成一行的文本比当前的块的最大宽度还宽，就重新设置宽度，并准备与maxWidth比较
				mWidths.set(id,width);
			}
		}
		if(width>=maxWidth || w>=maxWidth){
			//当前块的最大宽度比maxWidth大，或者是最大宽度被删了，重新检查
			maxWidth = checkMaxWidth();
		}
	}
	
	/* 测量文本块连接处的行宽 */
	private float measureBlockJoinTextStart(int i)
	{
		int start, end;
		float startWidth;
		TextPaint paint = getPaint();
		CharSequence now = mBlocks.get(i);

		//先测量自己的开头
		start = 0;
		end = tryLine_End(now,0);
		startWidth = paint.measureText(now,start,end);
		if(i>0)
		{
			//如果可以，我们接着测量上个的末尾
			CharSequence last = mBlocks.get(i-1);
			end = last.length();
			if(end>0 && last.charAt(end-1)!=FN){
		    	start = tryLine_Start(last,end-1);
			    startWidth += paint.measureText(last,start,end);
			}
		}	
		return startWidth;
	}
	private float measureBlockJoinTextEnd(int i)
	{
		int start, end;
		float endWidth;
		TextPaint paint = getPaint();
		CharSequence now = mBlocks.get(i);

		//先测量自己的末尾
		start = tryLine_Start(now,now.length()-1);
		end = now.length();
		endWidth = paint.measureText(now,start,end);
		if(i<mBlocks.size()-1 && end>0 && now.charAt(end-1)!=FN)
		{
			//如果可以，我们接着测量下个的开头
			CharSequence next = mBlocks.get(i+1);
			start = 0;
			end = tryLine_End(next,0);
			endWidth += paint.measureText(next,start,end);
		}
		return endWidth;
	}
	/* 测量指定文本块的指定范围内的文本的宽，并考虑连接处的宽 */
	private float measureBlockWidth(int i,int start,int end)
	{
		GetChars text = mBlocks.get(i);
		int s = tryLine_Start(text,start);
		int e = tryLine_End(text,end);
		float width = getDesiredWidth(text,s,e,getPaint());
		if(s==0){
			float w = measureBlockJoinTextStart(i);
			width = w>width ? w:width;
		}
		if(e==text.length()){
			float w = measureBlockJoinTextEnd(i);
			width = w>width ? w:width;
		}
		return width;
	}

/*
_______________________________________

 一些无聊的函数
_______________________________________
  
*/
	
	/* 寻找index所指定的文本块，并记录文本块的起始下标 */
	private int findBlockIdForIndex(int index)
	{
		int size = mBlocks.size();
		int start = 0;
		int i = 0;
		for(;i<size;++i)
		{
			int nowLen = mBlocks.get(i).length();
			if(start+nowLen>=index){
				break;
			}
			start+=nowLen;
		}
		cacheLen = start;
		return i;
	}
	/* 寻找line所指定的文本块，并记录文本块的起始行 */
	private int findBlockIdForLine(int line)
	{
		int size = mBlocks.size();
		int start = 0;
		int i = 0;
		for(;i<size;++i)
		{
			int nowLine = mLines.get(i);
			if(start+nowLine>=line){
				break;
			}
			start+=nowLine;
		}
		cacheLine = start;
		return i;
	}
	/* 获取指定块的起始行 */
	private int getBlockStartLine(int id)
	{
		int line = 0;
		for(int i=0;i<id;++i){
			line+= mLines.get(i);
		}
		return line;
	}
	/* 获取指定块的起始下标 */
	private int getBlockStartIndex(int id)
	{
		int line = 0;
		for(int i=0;i<id;++i){
			line+= mBlocks.get(i).length();
		}
		return line;
	}
	/* 移动到index指定的块，移动完成后，会设置cacheLine, cacheLen, cacheId */
	private void moveToIndexBlock(int index)
	{
		int i, size = mBlocks.size();
		int startIndex = 0,startLine = 0;
		CharSequence text = null;

		for(i=0;i<size;++i)
		{
			text = mBlocks.get(i);
			int nowLen = text.length();
			int nowLine = mLines.get(i);
			if(startIndex+nowLen>=index){
				break;
			}
			startIndex+=nowLen;
			startLine+=nowLine;
		}
		cacheId = i;
		cacheLen = startIndex;
		cacheLine = startLine;
	}
	/* 移动到line指定的块，移动完成后，会设置cacheLine, cacheLen, cacheId */
	private void moveToLineBlock(int line)
	{
		int i, size = mBlocks.size();
		int startIndex = 0,startLine = 0;
		CharSequence text = null;

		for(i=0;i<size;++i)
		{
			text = mBlocks.get(i);
			int nowLen = text.length();
			int nowLine = mLines.get(i);
			if(startLine+nowLine>=line){
				break;
			}
			startIndex+=nowLen;
			startLine+=nowLine;
		}
		cacheId = i;
		cacheLen = startIndex;
		cacheLine = startLine;
	}
	/* 移动到id指定的块，移动完成后，会设置cacheLine, cacheLen, cacheId */
	private void moveToIdBlock(int id)
	{
		int i;
		int startIndex = 0,startLine = 0;
		CharSequence text = null;

		for(i=0;i<id;++i)
		{
			text = mBlocks.get(i);
			int nowLen = text.length();
			int nowLine = mLines.get(i);
			startIndex+=nowLen;
			startLine+=nowLine;
		}
		cacheId = i;
		cacheLen = startIndex;
		cacheLine = startLine;
	}
	
	/* 检查最大的宽度 */
	private float checkMaxWidth()
	{
		int j;
		float width = 0;
		for(j=mWidths.size()-1;j>=0;--j)
		{
			float w = mWidths.get(j);
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
		return lineCount;
	}
	@Override
	public int getHeight(){
		return (int)(lineCount*getLineHeight());
	}
	public float maxWidth(){
		return maxWidth;
	}
	@Override
	public int getLineTop(int p1){
		return (int)(p1*getLineHeight());
	}
	@Override
	public int getLineDescent(int p1)
	{
		getPaint().getFontMetrics(font);
		float descent = font.descent*lineSpacing;
		return (int)(getLineTop(p1)+descent);
	}

	/* 获取指定行的起始下标 */
	@Override
	public int getLineStart(int p1)
	{
		//获取第p1个'\n'所在的块
		moveToLineBlock(p1);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;

		int toLine = p1-startLine;
		int hasLine = mLines.get(id);

		//寻找剩余的行数的位置
		GetChars str = mBlocks.get(id);
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
		paint.getFontMetrics(font);
		float height = font.bottom-font.top;
		return height*lineSpacing;
	}

	/* 获取offset所在的行 */
	@Override
	public int getLineForOffset(int offset)
	{
		moveToIndexBlock(offset);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;

		GetChars text = mBlocks.get(id);
		startLine+= Count(FN,text,0,offset-startIndex);
		return startLine;
	}

	/* 获取纵坐标指定的行 */
	@Override
	public int getLineForVertical(int vertical)
	{
		int line = (int)(vertical/getLineHeight());
		line = line<0 ? 0 : (line>lineCount ? lineCount:line);
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
		RectF r = rectF;
		pos p = tmp;
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		float width = cursorWidth*paint.getTextSize();
		getCursorPos(point,p);

		r.left=p.x;
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
		EditableList text = (EditableList) getText();
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		RectF rf = rectF;

		pos s = tmp;
		pos e = tmp2;
		getCursorPos(start,s);
		if(end-start>text.MaxCount){
			getCursorPos(end,e);
		}
		else{
		    nearOffsetPos(start,s.x,s.y,end,e);
		}

		float w = getDesiredWidth(text,start,end,paint);
		if(s.y == e.y)
		{
			//单行的情况
			rf.left = s.x;
			rf.top = s.y;
			rf.right = rf.left+w;
			rf.bottom = rf.top+lineHeight;
			dest.addRect(rf,Path.Direction.CW);
			//添加起始行的Rect
			return;
		}

		float sw = measureText(text,start,tryLine_End(text,start),paint);
		//float ew = measureText(text,tryLine_Start(text,end),end,mPaint);

		rf.left = s.x;
		rf.top = s.y;
		rf.right = rf.left+sw;
		rf.bottom = rf.top+lineHeight;
		dest.addRect(rf,Path.Direction.CW);
		//添加起始行的Rect

		if((e.y-s.y)/lineHeight > 1){
			//如果行数超过2
			rf.left = 0;
			rf.top = rf.top+lineHeight;
			rf.right = rf.left+w;
			rf.bottom = e.y;
			dest.addRect(rf,Path.Direction.CW);
			//添加中间所有行的Rect
		}

		rf.left = 0;
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
		bounds.right = (int) (bounds.left+maxWidth());
		bounds.bottom = (int) (bounds.top+getLineHeight());
		return line;
	}

	/* 获取offset处的横坐标，非常精确 */
	@Override
	public float getPrimaryHorizontal(int offset)
	{
		CharSequence text = getText();
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
	final public void getCursorPos(int offset,pos pos)
	{  
	    //找到index所指定的块
		moveToIndexBlock(offset);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;
		GetChars text = mBlocks.get(id);

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
	final public void nearOffsetPos(int oldOffset, float x, float y, int newOffset, pos target)
	{
		GetChars text = (GetChars) getText();
		int index = tryLine_Start(text,newOffset);
		target.x = measureText(text,index,newOffset,getPaint());

		if(oldOffset<newOffset){
			int line = Count(FN,text,oldOffset,newOffset);
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset){
			int line = Count(FN,text,newOffset,oldOffset);
			target.y = y-getLineHeight()*line;
		}
	}
	
	/* 为了效率，我们通常不允许一个一个charAt，而是先获取范围内的chars，再遍历数组 */
	final public float getDesiredWidth(GetChars text, int start, int end, TextPaint paint)
	{
		fillChars(text,start,end);
		end = end-start;
		int last = 0;
		float width = 0, w;
		int line = 0;

		for(start=0;start<end;++start)
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
	final public float getDesiredHeight(GetChars text, int start, int end)
	{
		cacheLine = Count(FN,text,start,end);
		return cacheLine*getLineHeight();
	}
	
	/* 测量单行文本宽度，非常精确 */
	final public float measureText(CharSequence text,int start,int end,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		fillWidths(text,start,end,paint);
		
		for(int i = 0;i<count;++i){
			width+=widths[i];
		}
		return width;
	}
	final public float measureText(char[] chars,int start,int end,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		fillWidths(chars,start,end,paint);

		for(int i = 0;i<count;++i){
			width+=widths[i];
		}
		return width;
	}
	/* 测量单行文本中，指定位置的下标 */
	final public int measureOffset(CharSequence text,int start,int end,float tox,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		fillWidths(text,start,end,paint);
		
		for(int i=0;i<count;++i)
		{
			if(width>=tox){
				break;
			}
			++start;
			width+=widths[i];
		}
		return start;
	}
		
	/* 统计和测量下标 */
	final public int Count(char c, GetChars text, int start, int end)
	{
		fillChars(text,start,end);
		return CharArrHelper.Count(c,chars,0,end-start);
	}
	final public int NIndex(char c,GetChars text,int index, int n)
	{
		fillChars(text,index,text.length());
		return index+ CharArrHelper.NIndex(c,chars,0,n);	
	}
	final public int lastNIndex(char c,GetChars text,int index, int n)
	{
		fillChars(text,0,index+1);
		return CharArrHelper.lastNIndex(c,chars,index,n);
	}

	/* 安全地获取数据 */
	final protected void fillChars(GetChars text, int start, int end)
	{
		chars = chars==null || chars.length<end-start ? new char[end-start]:chars;
		text.getChars(start,end,chars,0);
	}
	final protected void fillWidths(char[] chars, int start, int end, TextPaint paint)
	{
		widths = widths==null || widths.length<end-start ? new float[end-start]:widths;
		paint.getTextWidths(chars,start,end-start,widths);
	}
	final protected void fillWidths(CharSequence text, int start, int end, TextPaint paint)
	{
		widths = widths==null || widths.length<end-start ? new float[end-start]:widths;
		paint.getTextWidths(text,start,end,widths);
	}
	
	
	final public int tryLine_Start(int index)
	{
		int id = findBlockIdForIndex(index);
		CharSequence text = mBlocks.get(id);
		int start = tryLine_Start(text,index-cacheLen);
		if(id>0 && text.charAt(start)!=FN)
		{
			text = mBlocks.get(id-1);
			start = tryLine_Start(text,text.length());
			cacheLen-=text.length();
		}
		return start+cacheLen;
	}
	final public int tryLine_End(int index)
	{
		int id = findBlockIdForIndex(index);
		CharSequence text = mBlocks.get(id);
		int end = tryLine_End(text,index-cacheLen);
		if(id<mBlocks.size()-1 && (end>=text.length() || text.charAt(end)!=FN))
		{
			cacheLen+=text.length();
			text = mBlocks.get(id+1);
			end = tryLine_End(text,0);
		}
		return end+cacheLen;
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
	
}
