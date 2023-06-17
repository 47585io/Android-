package com.who.Edit.Base;

import android.text.*;
import java.util.*;
import com.who.Edit.Base.Share.Share3.*;


/* 均衡效率的神器，BlockLayout，
   拷贝一份原字符串，并打碎成文本块列表，可对它进行插入和删除
   额外记录每个文本块的行数和宽度，每次对单个文本块修改时同时修改它的行数和宽度，对于未修改的文本块，它的行数和宽度是不变的
   每次要跳到第几行，我们直接统计一下行就可以找到下标，每次不确定宽度，只要测量这个不确定宽度的块，因为其它块的宽度是不变的
   每次要在非常长的字符串中查找时不用toString，也不用再indexOf，直接让我找，StringBuilder自带indexOf函数
*/
public abstract class BlockLayout extends Layout
{
	public static final int MaxLine = 10000;
	public static final char FN = '\n';
	public static final String FNS = "\n";

	private int lineCount,maxWidth;
	private List<StringBuilder> mBlocks;
	private List<Integer> mLines;
	private List<Integer> mWidths;
	//每个文本块，每个块的行数，每个块的宽度

	public BlockLayout(java.lang.CharSequence base, android.text.TextPaint paint, int width, android.text.Layout.Alignment align,float spacingmult, float spacingadd)
	{
		super(base,paint,width,align,spacingmult,spacingadd);
		mBlocks = new ArrayList<>();
		mLines = new ArrayList<>();
		addBlock();
	}
	
	private void addBlock(){
		mBlocks.add(new StringBuilder());
		mLines.add(0);
	}
	private void addBlock(int index){
		mBlocks.add(index,new StringBuilder());
		mLines.add(index,0);
	}
	private void addBlock(int index,String text,int line)
	{
		/* StringBuilder插入CharSequence和String效率不一样！！！
		   String更快，因为插入CharSequence本质上也是先toString再插入的！！！
		   所以就这么说吧，如果要效率高，参数尽量是String，而不是CharSequence
		   StringBuilder插入方法有很多重载，只有参数是String的那个效率最高，因为String内部有存储一个字符数组，StringBuilder插入时必须要拷贝这个数组
		*/
		mBlocks.add(index,new StringBuilder(text));
		mLines.add(index,line);
	}
	private void addBlock(int index,StringBuilder text,int line){
		mBlocks.add(index,text);
		mLines.add(index,line);
	}
	
	public void setText(CharSequence text,int line){
		//dispatchTextBlock(text);
	}
	/* 只管分发文本块，不管怎样，大段文本块都可给我 */
	private void dispatchTextBlock(String text, int id, int line)
	{
		int nowIndex = 0;
		int nextIndex = 0;
		
		//每次从text中向后切割MaxLine行，并添加到mBlocks和mLines中
		while(true)
		{
			if(line<MaxLine){
				//最后一个块，不用再测量了，直接从上次的位置切割
				String str = text.substring(nowIndex,text.length());
				addBlock(id,str,line);
				break;
			}

			//向后找下一个位置，并切割范围内的文本，并插入到刚创建的文本块中
			nextIndex = StringSpiltor.NIndex('\n',text,nowIndex,MaxLine);
			String str = text.substring(nowIndex,nextIndex+1);
			addBlock(id,str,MaxLine);
			
			//继续向后找下个位置
			nowIndex = nextIndex+1;
			line-=MaxLine;
			++id;
		}
	}
	
	public void insert(int index, String text, int lines)
	{
		//找到index所指定的文本块
		int size = mBlocks.size();
		int i = 0;
		for(;i<size;++i)
		{
			int nowLen = mBlocks.get(i).length();
			if(index-nowLen<0){
				break;
			}
			index-=nowLen;
		}
	
		int hasLine = mLines.get(i);
		int toLine = hasLine+lines;
		if(toLine<=MaxLine){
			//当插入文本不会超出当前的文本块时，直接插入
			mBlocks.get(i).insert(index,text);
			mLines.set(i,toLine);
		}
		else
		{
			//当插入文本会超出当前的文本块时，先插入，之后截取多出的部分
			StringBuilder builder = mBlocks.get(i);
			builder.insert(index,text);
			int outLine = toLine-MaxLine;
			
			int end = builder.length();
			int start = StringSpiltor.lastNIndex(FNS,builder,end,outLine);
			String outStr = builder.substring(start+1,end);
			builder.delete(start+1,end);
			mLines.set(i,MaxLine);
			
			if(outLine>MaxLine){
				//超出的行数超过了单个文本块的最大行数
				dispatchTextBlock(outStr,i+1,outLine);
			}
			
			if (size-1 == i){
				mBlocks.add(new StringBuilder());
				mLines.add(0);
			}
			else if (mLines.get(i+1) + outLine > MaxLine){
				mBlocks.add(i+1,new StringBuilder());
				mLines.add(i+1,0);
			}
			//若无下个文本块，则添加一个
			//若有下个文本块，但它的行也不足，那么在我之后添加一个

			builder = mBlocks.get(i+1);
			hasLine = mLines.get(i+1);
			builder.insert(0,outStr);
			mLines.set(i+1,hasLine+outLine);
			//之后将截取的字符串添加到文本块列表中的下个文本块开头
		}
	}
	
	public void delete(int start, int end)
	{
		int size = mBlocks.size();
		int i = 0, j;
		
		//找到start所指定的文本块
		for(;i<size;++i)
		{
			int nowLen = mBlocks.get(i).length();
			if(start-nowLen<0){
				break;
			}
			start-=nowLen;
			end-=nowLen;
		}	
		//找到end所指定的文本块
		for(j=i;j<size;++j)
		{
			int nowLen = mBlocks.get(i).length();
			if(end-nowLen<0){
				break;
			}
			end-=nowLen;
		}
		
		if(i==j){
			//只要删除一个
			deleteForBlock(i,start,end);
			return;
		}
		
		//删除开头的块
		deleteForBlock(i,start,mBlocks.get(i).length());
		for(++i;i<j;--j){
			//删除中间的块
			deleteForBlock(i,0,mBlocks.get(i).length());
		}
		deleteForBlock(i,0,end);
		//删除末尾的块
	}
	private void deleteForBlock(int i, int start, int end)
	{
		StringBuilder builder = mBlocks.get(i);
		if(start==0 && end==builder.length()){
			mBlocks.remove(i);
			mLines.remove(i);
		}
		else{
		    int delLine = StringSpiltor.Count(FNS,builder,start,end);
		    builder.delete(start,end);
		    mLines.set(i,mLines.get(i)-delLine);
		}
	}
	
	private int findBolckIdForIndex(int index)
	{
		int size = mBlocks.size();
		int start = 0;
		int i = 0;
		for(;i<size;++i)
		{
			int nowLen = mBlocks.get(i).length();
			if(start+nowLen>index){
				break;
			}
			start+=nowLen;
		}
		return i;
	}

	@Override
	public int getLineCount()
	{
		return lineCount;
	}

	@Override
	public int getLineTop(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getLineDescent(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getLineStart(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getParagraphDirection(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public boolean getLineContainsTab(int p1)
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public Layout.Directions getLineDirections(int p1)
	{
		// TODO: Implement this method
		return null;
	}

	@Override
	public int getTopPadding()
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getBottomPadding()
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getEllipsisStart(int p1)
	{
		// TODO: Implement this method
		return 0;
	}

	@Override
	public int getEllipsisCount(int p1)
	{
		// TODO: Implement this method
		return 0;
	}
}
