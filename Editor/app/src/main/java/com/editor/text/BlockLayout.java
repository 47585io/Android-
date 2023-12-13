package com.editor.text;

import android.text.*;
import com.editor.text.base.*;

public class BlockLayout extends BaseLayout implements BlockListener
{
	private int mLineCount;
	private int mBlockSize;
	private boolean isModify;
	//EditableBlockList中对Block的修改事件和AfterBlocksChanged是连续发送的，中间不会发送文本事件
	//也就是说，但凡涉及修改，每一次文本块事件都是连续的，立即修改立即刷新，不论文本事件回调深度如何
	
	//每个文本块的行数
	private EditableBlockList mText;
	private int[] mLines;
	private int[] mStartLines;
	
	public BlockLayout(EditableBlockList base, TextPaint paint, int lineColor, float lineSpacing)
	{
		super(base,paint,lineColor,lineSpacing);
		
		mLineCount = 0;
		mBlockSize = 0;
		mText = base;
		mLines = EmptyArray.INT;
		mStartLines = EmptyArray.INT;
		
		//测量所有文本块以初始化数据
		int size = base.getBlockSize();
		onAddBlocks(0,size);
		onBlocksInsertAfter(0,size-1,0,base.getBlock(size-1).length());
		afterBlocksChanged(0,0);
		//等待后续的测量
		base.setBlockListener(this);
	}
	
	@Override
	public void onAddBlocks(int i, int count)
	{
		//每次添加文本块，都同步对应的行数
		for(int j=i+count;i<j;++i)
		{
			mLines = GrowingArrayUtils.insert(mLines,mBlockSize,i,0);
			mStartLines = GrowingArrayUtils.insert(mStartLines,mBlockSize,i,0);
			mBlockSize++;
		}
		isModify = true;
	}

	@Override
	public void onRemoveBlocks(int i, int j)
	{
		//每次移除文本块，都同步对应的行数
		for(--j;i<=j;--j)
		{
			mLineCount -= mLines[j];
			mLines = GrowingArrayUtils.remove(mLines,mBlockSize,j);
			mStartLines = GrowingArrayUtils.remove(mStartLines,mBlockSize,j);
			mBlockSize--;
		}
		isModify = true;
	}
	
	@Override
	public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)
	{
		//在一段连续文本被删除前，测量要删除的起始文本块和末尾文本块
		//若文本被全删，不测量，而是等待移除文本块时同步
		if(i==j){
			//只有一个文本块
			measureDeleteBlockBefore(i,iStart,jEnd);
		}
		else{
		    measureDeleteBlockBefore(i,iStart,mText.getBlock(i).length());
			measureDeleteBlockBefore(j,0,jEnd);
		}
	}

	@Override
	public void onBlocksDeleteAfter(int i, int iStart)
	{
		// TODO: Implement this method
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
		if(isModify)
		{	
			if(i == 0){
				mStartLines[i++] = 0;
			}
			for(;i<mBlockSize;++i){
				mStartLines[i] = mStartLines[i-1]+mLines[i-1];
			}
			isModify = false;
		}
	}
	
	/* 在删除前测量指定文本块的指定范围内的文本的行数 */
	private void measureDeleteBlockBefore(int id, int start, int end)
	{
		GetChars text = mText.getBlock(id);
		//如果文本块不会被全删了，才测量
		if(start!=0 || end!=text.length())
		{
			int line = Count(text,FN,start,end);
			if(line > 0){
				//在删除文本前，计算删除的行
				mLineCount -= line;    
				mLines[id] = mLines[id]-line;
				isModify = true;
			}
		}
	}
	
	/* 在插入后测量指定文本块的指定范围内的文本的行数 */
	private void measureInsertBlockAfter(int id, int start, int end)
	{
		GetChars text = mText.getBlock(id);
		int line = Count(text,FN,start,end);
		if(line > 0){
			//在插入字符串后，计算增加的行
			mLineCount+=line;
			mLines[id] = mLines[id]+line;
			isModify = true;
		}
	}
	
	/* 寻找第line个换行符所在的文本块 */
    public int findBlockIdForLine(int line)
	{
		//使用二分查找法先找到最接近的位置
		int low = 0;   
		int high = mBlockSize - 1;   
		int middle = 0;
		while (low <= high)
		{   
			middle = (low + high) >> 1;   
			if (line == mStartLines[middle]) 
				break;   
			else if (line < mStartLines[middle])
				high = middle - 1;   
			else 
				low = middle + 1;
		}  

		int id = middle;
		int nowLine = mStartLines[id];
		//文本块的起始行数实际上是之前的文本块的行数(例如第二块文本块的起始行数就是第一块的行数)
		//这意味着，要寻找指定的换行所在的文本块，并且如果它等于某个文本块的起始行数，此换行应该在之前的文本块
		//为了避免单行文本太长跨越了多个文本块，导致很多文本块的startLine相同，所以这里应该找到最后面的文本块，也就是换行符的那个
		if(nowLine<line){
			for(;id<mBlockSize-1 && mStartLines[id+1]<line;++id){}
		}
		else if(nowLine>=line){
			for(;id>0 && mStartLines[id]>=line;--id){}
		}
		return id;
	}
	

	@Override
	public int getLineCount(){
		return mLineCount;
	}
	
	@Override
	public int getLineStart(int line)
	{
		//获取第line个'\n'所在的块
		int id = findBlockIdForLine(line);
		int startLine = mStartLines[id];
		int startIndex = mText.getBlockStartIndex(id);
		int toLine = line-startLine;
		int hasLine = mLines[id];

		//寻找剩余的行数的位置
		GetChars str = mText.getBlock(id);
		int len = getText().length();
		int index = toLine<hasLine-toLine ? NIndex(str,FN,0,toLine):lastNIndex(str,FN,str.length()-1,hasLine-toLine+1);
		index+=startIndex;
		index = line<1 ? 0 : (index<0 ? len : (index+1>len ? len:index+1));
		return index;
	}
	
	@Override
	public int getLineForOffset(int offset)
	{
		int id = mText.findBlockIdForIndex(offset);
		int startLine = mStartLines[id];
		int startIndex = mText.getBlockStartIndex(id);
		GetChars text = mText.getBlock(id);
		startLine += Count(text,FN,0,offset-startIndex);
		return startLine;
	}
	
	/* 统计和寻找下标 */
	final public static int Count(CharSequence text, char c, int start, int end)
	{
		char[] chars = RecylePool.obtainCharArray(end-start);
		TextUtils.getChars(text,start,end,chars,0);
		int count = Count(chars,c,0,end-start);
		RecylePool.recyleCharArray(chars);
		return count;
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
	
	final public static int NIndex(CharSequence text, char c, int index, int n)
	{
		int length = text.length();
		char[] chars = RecylePool.obtainCharArray(length-index);
		TextUtils.getChars(text,index,length,chars,0);
		int offset = index+NIndex(chars,c,0,n);	
		RecylePool.recyleCharArray(chars);
		return offset;
	}

	/* 从index开始，向后找到字符c在arr中第n次出现的位置 */
	final public static int NIndex(char[] arr, char c, int index, int n)
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
	
	final public static int lastNIndex(CharSequence text, char c, int index, int n)
	{
		char[] chars = RecylePool.obtainCharArray(index+1);
		TextUtils.getChars(text,0,index+1,chars,0);
		int offset = lastNIndex(chars,c,index,n);
		RecylePool.recyleCharArray(chars);
		return offset;
	}
	
	/* 从index开始，向前找到字符c在arr中第n次出现的位置 */
	final public static int lastNIndex(char[] arr, char c, int index, int n)
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
	
}
