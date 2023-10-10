package com.editor.text;

import android.text.*;
import com.editor.text.base.*;

public abstract class BlockLayout2 extends BaseLayout implements BlockListener
{
	//记录属性
	private int mLineCount;
	private int mBlockSize;
	private char[] maxWidth;

	//每个块的行数，每个块的宽度
	private int[] mLines;
	private int[] mStartLines;
	private char[][] mWidths;
	
	//临时变量
	private boolean isStart,isEnd;
	
	protected BlockLayout2(EditableBlockList text, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, float cursorWidth, float scale)
	{
		super(text,paint,spacingmult,cursorWidth);	
		mLines = EmptyArray.INT;
		mStartLines = EmptyArray.INT;
		mWidths = EmptyArray.emptyArray(char[].class);
		maxWidth = EmptyArray.CHAR;

		//测量所有文本块以初始化数据
		int size = text.getBlockSize();
		onAddBlocks(0,size);
		onBlocksInsertAfter(0,size-1,0,text.getBlock(size-1).length());
		afterBlocksChanged(0,0);
		//等待后续的测量
		text.setBlockListener(this);
	}
	
	public void setScale(float scale){
		
	}
	public float getScale(){
		return 0;
	}
	
	@Override
	public void onAddBlocks(int i, int count)
	{
		//每次添加文本块，都同步对应的行数和宽度
		for(int j=i+count;i<j;++i)
		{
			mLines = GrowingArrayUtils.insert(mLines,mBlockSize,i,0);
			mStartLines = GrowingArrayUtils.insert(mStartLines,mBlockSize,i,0);
			mWidths = GrowingArrayUtils.insert(mWidths,mBlockSize,i,EmptyArray.CHAR);
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
		maxWidth = mWidths[checkMaxWidth()];
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
		    isStart = measureDeleteBlockBefore(i,iStart,((EditableBlockList)getText()).getBlock(i).length());
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
			measureInsertBlockAfter(i,iStart,((EditableBlockList)getText()).getBlock(i).length());
			for(++i;i<j;++i){
				measureInsertBlockAfter(i,0,((EditableBlockList)getText()).getBlock(i).length());
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
		int line = getCacheLine();
		if(width > maxWidth()){
			//如果出现了一个更大的宽，就记录它
			maxWidth = getCacheStr();
		}
		if(width > getWidth(id)){
			mWidths[id] = getCacheStr();
		}
		if(line > 0){
			//在插入字符串后，计算增加的行
			mLineCount+=line;
			mLines[id] = mLines[id]+line;
		}
	}

	/* 在删除前测量指定文本块的指定范围内的文本的宽和行数，并做出删除决策 */
	private boolean measureDeleteBlockBefore(int id, int start, int end)
	{
		boolean is = false;
		CharSequence text = ((EditableBlockList)getText()).getBlock(id);
		//如果文本块不会被全删了，才测量
		if(start!=0 || end!=text.length())
		{
			float width = measureBlockWidth(id,start,end);
			int line = getCacheLine();
			if(width >= getWidth(id)){
				//如果删除字符串是当前的块的最大宽度，重新测量当前整个块
				is = true;
			}
			if(line > 0){
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
		float oldWidth = getWidth(id);
		CharSequence text = ((EditableBlockList)getText()).getBlock(id);
		if(needMeasureAllText){
			//如果需要全部测量
			width = measureBlockWidth(id,0,text.length());
			mWidths[id] = getCacheStr();
		}
		else{
			//删除文本后，两行连接为一行，测量这行的宽度
			width = measureBlockWidth(id,start,start);
			if(width > oldWidth){
				//如果连接成一行的文本比当前的块的最大宽度还宽，就重新设置宽度，并准备与maxWidth比较
				mWidths[id] = getCacheStr();
			}
		}
		if(width >= maxWidth()){
			//当前块的最大宽度比maxWidth大，就记录它
			maxWidth = mWidths[id];
		}
		else if(oldWidth >= maxWidth()){
			//如果当前块原本是最大宽度，现在被删了并且比原来更小，重新检查
			maxWidth = mWidths[checkMaxWidth()];
		}
	}

	/* 测量指定文本块的指定范围内的文本的宽，并考虑连接处的宽 */
	private float measureBlockWidth(int i,int start,int end)
	{
		EditableBlockList text = (EditableBlockList) getText();
		int st = text.getBlockStartIndex(i);
		start = getOffsetToLeftOf(st+start);
		end = getOffsetToRightOf(st+end);
		float width = getDisredWidth(text,start,end,getPaint());
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
	public int checkMaxWidth()
	{
		int i = 0;
		float width = 0;
		TextPaint paint = getPaint();
		for(int j=mBlockSize-1;j>=0;--j)
		{
			float w = paint.measureText(mWidths[j],0,mWidths[j].length);
			if(w>width){
				width = w;
				i = j;
			}
		}
		return i;
	}
	
	private char[] getCacheStr(){
		return subCharArray(getText(),getCacheStart(),getCacheEnd());
	}
	private float getWidth(int i){
		return getPaint().measureText(mWidths[i],0,mWidths[i].length);
	}
	private static char[] subCharArray(CharSequence text, int start, int end)
	{
		char[] arr = new char[end-start];
		TextUtils.getChars(text,start,end,arr,0);
		return arr;
	}
	
/*
_______________________________________

 接下来我们就可以实现父类的一些方法了
_______________________________________

*/

	@Override
	public float maxWidth(){
		return getPaint().measureText(maxWidth,0,maxWidth.length);
	}

	@Override
	public int getLineCount(){
		return mLineCount;
	}
	
	@Override
	public int getLineStart(int p1)
	{
		//获取第p1个'\n'所在的块
		EditableBlockList text = (EditableBlockList) getText();
		int id = findBlockIdForLine(p1);
		int startLine = mStartLines[id];
		int startIndex = text.getBlockStartIndex(id);
		int toLine = p1-startLine;
		int hasLine = mLines[id];

		//寻找剩余的行数的位置
		CharSequence block = text.getBlock(id);
		int len = text.length();
		int index = toLine<hasLine-toLine ? NIndex(FN,block,0,toLine):lastNIndex(FN,block,block.length()-1,hasLine-toLine+1);
		index+=startIndex;
		index = p1<1 ? 0 : (index<0 ? len : (index+1>len ? len:index+1));
		return index;
	}

	@Override
	public int getLineForOffset(int offset)
	{
		EditableBlockList text = (EditableBlockList) getText();
		int id = text.findBlockIdForIndex(offset);
		int startLine = mStartLines[id];
		int startIndex = text.getBlockStartIndex(id);
		CharSequence block = text.getBlock(id);
		startLine+= Count(FN,block,0,offset-startIndex);
		return startLine;
	}

	@Override
	public int getOffsetToLeftOf(int offset)
	{
		//我们知道，EditableBlockList的charAt太浪费时间了
		EditableBlockList text = (EditableBlockList) getText();
		int id = text.findBlockIdForIndex(offset);
		id = text.getBlock(id).length()+text.getBlockStartIndex(id)==offset ? id+1:id;
		int index = TextUtils.lastIndexOf(text.getBlock(id), FN, offset-text.getBlockStartIndex(id));
		if(index > -1){
			return text.getBlockStartIndex(id)+index;
		}
		for(--id;id>-1;--id)
		{
			CharSequence block = text.getBlock(id);
			index = TextUtils.lastIndexOf(block,FN,block.length()-1);
			if(index > -1){
				return text.getBlockStartIndex(id)+index;
			}
		}
		return 0;
	}

	@Override
	public int getOffsetToRightOf(int offset)
	{
		EditableBlockList text = (EditableBlockList) getText();
		int id = text.findBlockIdForIndex(offset);
		id = text.getBlock(id).length()+text.getBlockStartIndex(id)==offset ? id+1:id;
		int index = TextUtils.indexOf(text.getBlock(id), FN, offset-text.getBlockStartIndex(id));
		if(index > -1){
			return text.getBlockStartIndex(id)+index;
		}
		int size = text.getBlockSize();
		for(++id;id<size;++id)
		{
			CharSequence block = text.getBlock(id);
			index = TextUtils.indexOf(block,FN,0);
			if(index > -1){
				return text.getBlockStartIndex(id)+index;
			}
		}
		return text.length();
	}

}
