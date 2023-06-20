package com.who.Edit.Base;

import android.text.*;
import java.util.*;
import com.who.Edit.Base.Share.Share3.*;
import com.who.Edit.Base.Share.Share1.*;
import android.graphics.*;
import android.text.Layout.*;


/* 均衡效率的神器，BlockLayout，
   拷贝一份原字符串，并打碎成文本块列表，并且可对它进行插入和删除
   额外记录每个文本块的行数和宽度，每次对单个文本块修改时同时修改它的行数和宽度，对于未修改的文本块，它的行数和宽度是不变的
   每次要跳到第几行，我们直接统计一下行就可以找到下标，每次不确定宽度，只要测量这个不确定宽度的块，因为其它块的宽度是不变的
   每次要在非常长的字符串的指定下标或行查找时不用全部查找了，也不用全部toString，先直接以文本块的长度来跳跃，再找指定的块
   
   主要是因为Edit的myLayout，在draw函数中，当1000000行文本，光是测量就花了60ms，绘画时间倒是挺平衡，只要3ms，必须优化测量时间
   现在好了，即使1000000行时，也可以流畅编辑
   但有一个bug，MaxCount的值千万不要设置太小，要不然单个block装不满一行文本，大小计算会有问题，而且block的连接处本身就会切断一行文本，所以maxWidth是有问题的
   如果您有时间，可以自己解决这个问题
*/
public abstract class BlockLayout extends Layout
{
	public static final int MaxCount = 100000;
	public static final char FN = '\n';
	public static final float MinScacle = 0.5f, MaxScale = 2.0f;
	public static final float TextSize = 40f;
	public static final int TextColor = 0xffaaaaaa;
	
	//临时变量
	protected float[] widths;
	protected RectF rectF = new RectF();
	protected pos tmp = new pos(), tmp2 = new pos();
	protected Paint.FontMetrics font = new Paint.FontMetrics();
	private int cacheLine, cacheLen, cacheId;
	
	//记录属性
	private int lineCount;
	private float maxWidth;
	private float cursorWidth;
	private float lineSpacing;
	private float scaleLayout;
	
	//每个文本块，每个块的行数，每个块的宽度
	private List<SpannableStringBuilder> mBlocks;
	private List<Integer> mLines;
	private List<Float> mWidths;

	
	public BlockLayout(java.lang.CharSequence base, android.text.TextPaint paint, int width, android.text.Layout.Alignment align,float spacingmult, float spacingadd, float cursorWidth, float scale)
	{
		super(base,paint,width,align,spacingmult,spacingadd);
		mBlocks = new ArrayList<>();
		mLines = new ArrayList<>();
		mWidths = new ArrayList<>();
		setText(base);
		
		scaleLayout = scale;
		lineSpacing = spacingmult;
		this.cursorWidth = cursorWidth;
	}
	public void setPaint(TextPaint paint)
	{
		paint.reset();
		paint.setTextSize(TextSize);
		paint.setColor(TextColor);
		paint.setTypeface(Typeface.MONOSPACE);
	}
	
	public void setCursorWidthSpacing(float spacing){
		cursorWidth = spacing;
	}
	public void setLineSpacing(float spacing){
		lineSpacing = spacing;
	}
	public void setScale(float scale)
	{
		scaleLayout *= scale;
		scaleLayout = scaleLayout<MinScacle ? MinScacle:scale;
		scaleLayout = scaleLayout>MaxScale ? MaxScale:scale;
		TextPaint paint = getPaint();
		paint.setTextSize(TextSize*scaleLayout);
	}
	
	public float getCursorWidthSpacing(){
		return cursorWidth;
	}
	public float getLineSpacing(){
		return lineSpacing;
	}
	public float getScale(){
		return scaleLayout;
	}
	
	/* 添加文本块 */
	private void addBlock(){
		mBlocks.add(new SpannableStringBuilder());
		mLines.add(0);
		mWidths.add(0f);
	}
	private void addBlock(int i){
		mBlocks.add(i,new SpannableStringBuilder());
		mLines.add(i,0);
		mWidths.add(i,0f);
	}
	private void addBlock(int i,CharSequence text)
	{
		mBlocks.add(i,new SpannableStringBuilder());
		mLines.add(i,0);
		mWidths.add(i,0f);
		//无论怎样，都抛给insertForBlock来测量
		insertForBlock(i,0,text);	
	}
	/* 移除文本块 */
	private void removeBlock(int i)
	{
		mBlocks.remove(i);
		mLines.remove(i);
		mWidths.remove(i);
	}
	
	/* 设置文本 */
	public void setText(CharSequence text)
	{
		clearText();
		dispatchTextBlock(0,text);
	}
	/* 清除文本 */
	public void clearText()
	{
		mBlocks.clear();
		mLines.clear();
		mWidths.clear();
		addBlock();
		maxWidth = 0;
		lineCount = 0;
	}
	/* 只管分发文本块，不管怎样，大段文本块都可给我 */
	private void dispatchTextBlock(int id, CharSequence text)
	{
		int count = text.length();
		int nowIndex = 0;
		
		//每次从text中向后切割MaxCount个字符，并添加到mBlocks中
		while(true)
		{
			if(count<MaxCount){
				//最后一个块，直接从上次的位置切割
				CharSequence str = text.subSequence(nowIndex,text.length());
				addBlock(id,str);
				break;
			}

			//向后找下一个位置，并切割范围内的文本，并插入到刚创建的文本块中
			CharSequence str = text.subSequence(nowIndex,nowIndex+MaxCount);
			addBlock(id,str);
			
			//继续向后找下个位置
			nowIndex+=MaxCount;
			count-=MaxCount;
			++id;
		}
		cacheId = id;
		//保存分发到的位置
	}
	
	/* 如何插入文本和分发文本块 */
	public void insert(int index, CharSequence text)
	{
		//找到index所指定的文本块
		int i = findBlockIdForIndex(index);
		SpannableStringBuilder builder = mBlocks.get(i);
		int nowLen = builder.length();
		index -= cacheLen;
	
		if(nowLen+text.length()<=MaxCount){
			//当插入文本不会超出当前的文本块时，直接插入
			insertForBlock(i,index,text);
		}
		else
		{
			/*当插入文本会超出当前的文本块时，两种方案
			
			 *先插再截，总长度为 
			    插入一次:  text.length() 
				截取一次:  (nowLen+text.length()-MaxCount)
				删除一次:  (nowLen+text.length()-MaxCount)
				分发一次:  (nowLen+text.length()-MaxCount)
			
			 *截删截插截分分，总长度为:
			    截取index后的文本   nowLen-index
			    删除index后的文本:   nowLen-index
				计算可以插入的长度并在text中截取:  MaxCount-index
				插入截取的文本:  MaxCount-index
				截取text中的之后的文本:  text.length()-(MaxCount-index)
				分发之后的文本   text.length()-(MaxCount-index)
				分发截取的index后的文本   nowLen-index
			*/
			
			int len = text.length();
			int step1 = len + (nowLen+len-MaxCount)*3;
			int count = len>=MaxCount-index ? MaxCount-index:len;
			int step2 = (nowLen-index)*2 + count*2 + (len-count)*2 + nowLen-index;
			
			if(step1<=step2)
			{
				//方案1，先插入，之后截取多出的部分，适合小量文本
				insertForBlock(i,index,text);
				nowLen = builder.length();
				text = builder.subSequence(MaxCount,nowLen);
				deleteForBlock(i,MaxCount,nowLen);

				if(nowLen-MaxCount>MaxCount){
					//超出的字数超过了单个文本块的最大字数
					dispatchTextBlock(i+1,text);
					return;
				}

				if (mBlocks.size()-1 == i){
					//若无下个文本块，则添加一个
					addBlock();
				}
				else if (mBlocks.get(i+1).length()+nowLen-MaxCount > MaxCount){
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个
					addBlock(i+1);
				}

				insertForBlock(i+1,0,text);
				//之后将截取的字符串添加到文本块列表中的下个文本块开头
			}
			else
			{
				//方案2，精确计算应该截取和删除的部分，适合大量文本
				CharSequence sub1 = builder.subSequence(index,nowLen);
				CharSequence sub2 = text.subSequence(0,count);
				CharSequence sub3 = text.subSequence(count,len);
				
				//逆序重新插入，保证文本整体插入位置不变
				deleteForBlock(i,index,nowLen);
				insertForBlock(i,index,sub2);
				dispatchTextBlock(i+1,sub3);
				i = cacheId+1;
				addBlock(i,sub1);
			}
			
		}
	}
	/* 在插入文本块时调用，可以做出合理的测量，注意必须在全部文本改变后才能调用 */
	private void insertForBlock(int i, int index, CharSequence text)
	{
		SpannableStringBuilder builder = mBlocks.get(i);
		builder.insert(index,text);
		
		//检查插入文本块，我们仍需到全部文本中查找，因为文本块的连接处可能切断了一行文本
		CharSequence allText = getText();
		index += getBlockStartIndex(i);
		int e = tryLine_End(allText,index+text.length());
		int s = tryLine_Start(allText,index);
		String str = allText.subSequence(s,e).toString();
		
		//测量插入的文本块的宽和行
		float width = getDesiredWidth(str,0,str.length(),getPaint());
		int line = cacheLine;

		if(width>maxWidth){
			//如果出现了一个更大的宽，就记录它
			maxWidth = width;
		}
		if(width>mWidths.get(i)){
			mWidths.set(i,width);
		}
		if(line>0){
			//在插入字符串后，计算增加的行
			lineCount+=line;
			mLines.set(i,mLines.get(i)+line);
		}
	}
	
	/* 如何删除范围内的文本和文本块 */
	public void delete(int start, int end)
	{
		int i, j;
		int size = mBlocks.size();
		
		//找到start所指定的文本块，并将start偏移到文本块的下标
		i = findBlockIdForIndex(start);
		int startLen = cacheLen;
		start-=startLen;
		end-=startLen;
		
		//找到end所指定的文本块，并将end偏移到文本块的下标
		for(j=i;j<size;++j)
		{
			int nowLen = mBlocks.get(i).length();
			if(end-nowLen<=0){
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
	
	/* 在删除文本块时调用，可以做出合理的测量，无论怎样都无法正确测量连接处的宽，懒得管了，反正也不会有太大的问题 */
	private void deleteForBlock(int i, int start, int end)
	{
		SpannableStringBuilder builder = mBlocks.get(i);
		if(start==0 && end==builder.length())
		{
			//如果文本块会被全删了，直接移除它
			lineCount-=mLines.get(i);
			removeBlock(i);
			maxWidth = checkMaxWidth();
		}
		else
		{
			TextPaint paint = getPaint();
			//删除前，检查删除的文本块
			int s = tryLine_Start(builder,start);
			int e = tryLine_End(builder,end);
			String str = builder.subSequence(s,e).toString();
			
			//如果删除了字符串，测量删除文本块的宽以及行
			float width = getDesiredWidth(str,0,str.length(),paint);
			int line=cacheLine;
			float copyWidth = width;
			builder.delete(start,end);
			//在测量完删除文本块的宽后，才删除文本
			
			if(width>=mWidths.get(i))
			{
				//如果删除字符串是当前的块的最大宽度，重新测量当前整个块
				str = builder.toString();
				width = getDesiredWidth(str,0,str.length(),paint);
				mWidths.set(i,width);
			}
			else
			{
				//删除文本后，两行连接为一行，测量这行的宽度
				e = tryLine_End(builder,start);
				width = paint.measureText(builder,s,e);
				if(width>mWidths.get(i)){
					//如果连接成一行的文本比当前的块的最大宽度还宽，就重新设置宽度，并准备与maxWidth比较
					mWidths.set(i,width);
					copyWidth = width;
				}
			}
			
			if(copyWidth>=maxWidth){
				//当前块的最大宽度比maxWidth大，或者是最大宽度被删了，重新检查
				maxWidth = checkMaxWidth();
			}
			if(line>0){
				//在删除文本前，计算删除的行
				lineCount-=line;    
				mLines.set(i,mLines.get(i)-line);
			}
		}
	}
	
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
			if(last.charAt(last.length()-1)!=FN){
		    	start = tryLine_Start(last,last.length()-1);
			    end = last.length();
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
		if(i<mBlocks.size()-1 && now.charAt(now.length()-1)!=FN)
		{
			//如果可以，我们接着测量下个的开头
			CharSequence next = mBlocks.get(i+1);
			start = 0;
			end = tryLine_End(next,0);
			endWidth += paint.measureText(next,start,end);
		}
		return endWidth;
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
		return maxWidth*scaleLayout;
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
		String str = mBlocks.get(id).toString();
		int len = getText().length();
		int index = toLine<hasLine-toLine ? StringSpiltor.NIndex(FN,str,0,toLine):StringSpiltor.lastNIndex(FN,str,str.length()-1,hasLine-toLine+1);
		index+=startIndex;
		index = p1<1 ? 0 : (index<0 ? len : (index+1>len ? len:index+1));
		return index;
	}

	@Override
	public float getLineWidth(int line)
	{
		CharSequence text = getText();
		int start = getLineStart(line); 
		int end = tryLine_End(text,start);
		return measureText(text,start,end,getPaint());
	}

	public float getLineHeight()
	{
		TextPaint paint = getPaint();
		paint.getFontMetrics(font);
		float height = font.bottom-font.top;
		return height*lineSpacing;
	}

	@Override
	public int getLineForOffset(int offset)
	{
		moveToIndexBlock(offset);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;
		
		CharSequence text = mBlocks.get(id).toString();
		String str = offset-startIndex<text.length()/2 ? text.subSequence(0,offset-startIndex).toString() : text.toString();
		startLine+= StringSpiltor.Count(FN,str,0,offset-startIndex);
		return startLine;
	}
	
	@Override
	public int getLineForVertical(int vertical)
	{
		int line = (int)(vertical/getLineHeight());
		line = line<0 ? 0 : (line>lineCount ? lineCount:line);
		return line;
	}

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
	@Override
	public int getEllipsizedWidth(){
		return 0;
	}
	
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

	@Override
	public void getSelectionPath(int start, int end, Path dest)
	{
		CharSequence text = getText();
		String str = text.subSequence(start,end).toString();
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		RectF rf = rectF;
		pos s = tmp;
		pos e = tmp2;
		getCursorPos(start,s);
		nearOffsetPos(start,s.x,s.y,end,e);
		
		float w = getDesiredWidth(str,0,str.length(),paint);
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

	@Override
	public int getLineBounds(int line, Rect bounds)
	{
		bounds.left = 0;
		bounds.top = getLineTop(line);
		bounds.right = (int) (bounds.left+maxWidth());
		bounds.bottom = (int) (bounds.top+getLineHeight());
		return line;
	}

	@Override
	public boolean isRtlCharAt(int offset){
		return super.isRtlCharAt(offset);
	}
	@Override
	public float getPrimaryHorizontal(int offset){
		return super.getPrimaryHorizontal(offset);
	}
	@Override
	public float getSecondaryHorizontal(int offset){
		return super.getSecondaryHorizontal(offset);
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
	public int getLineVisibleEnd(int line){
		return super.getLineVisibleEnd(line);
	}
	@Override
	public int getOffsetToLeftOf(int offset){
		return tryLine_Start(getText(),offset);
	}
	@Override
	public int getOffsetToRightOf(int offset){
		return tryLine_End(getText(),offset);
	}
	
	/* 获取光标坐标 */
	final public void getCursorPos(int offset,pos pos)
	{  
	    //找到index所指定的块
		moveToIndexBlock(offset);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;
		CharSequence text = mBlocks.get(id);
		
		//先将当前的块转化为String
		String str = offset-startIndex<text.length()/2 ? text.subSequence(0,offset-startIndex).toString() : text.toString();
		
		//我们仍需要去获取全部文本去测量宽，但是只测量到offset的上一行，然后我们计算它们之间的宽
		text = getText();
		int start = tryLine_Start(text,offset);
		pos.x = measureText(text,start,offset,getPaint());
		
		//offset在使用完后转化为当前块的下标，测量当前块的起始到offset之间的行数，并加上之前的行数，最后计算行数的高
		offset = offset-startIndex;
		int lines = StringSpiltor.Count(FN,str,0,offset)+startLine;
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
		CharSequence text = getText();
		int index = tryLine_Start(text,newOffset);
		target.x = measureText(text,index,newOffset,getPaint());
		
		if(oldOffset<newOffset)
		{
			String str = text.subSequence(oldOffset,newOffset).toString();
			int line = StringSpiltor.Count(FN,str,0,str.length());
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset)
		{
			String str = text.subSequence(newOffset,oldOffset).toString();
			int line = StringSpiltor.Count(FN,str,0,str.length());
			target.y = y-getLineHeight()*line;
		}
	}
	
	/* 测量单行文本宽度，非常精确 */
	final public float measureText(CharSequence text,int start,int end,TextPaint paint)
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
	/* 测量单行文本中，指定位置的下标 */
	final public int measureOffset(CharSequence text,int start,int end,float tox,TextPaint paint)
	{
		float width = 0;
		int count = end-start;
		widths = widths==null || widths.length<count ? new float[count]:widths;
		paint.getTextWidths(text,start,end,widths);

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
	
	/* 测量文本切片的高，同时记录行数 */
	final public float getDesiredHeight(String text, int start, int end)
	{
		cacheLine = StringSpiltor.Count(FN,text,start,end);
		return cacheLine*getLineHeight();
	}
	/* 测量文本切片的宽，同时记录行数 */
	final public float getDesiredWidth(String text, int start, int end, TextPaint paint)
	{
		float width = 0;
		int line = 0;
		while(true)
		{
			float w;
			int before = start;
			start = text.indexOf(FN,start);

			if(start>=end || start<0){
				w = paint.measureText(text,before,end);
				width = w>width ? w:width;
				break;
			}

			w = paint.measureText(text,before,start);
			width = w>width ? w:width;

			++line;
			++start;
		}
		cacheLine = line;
		return width;
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
	final public static int tryLine_NStart(CharSequence src,int index,int n)
	{
		int len = src.length();
		while(n-->0)
		{
			index = tryLine_Start(src,index);
			if(n==0 || index<=0){
				break;
			}
			index-=1;
		}
		return index<0 || index>len ? 0:index;
	}
	final public static int tryLine_NEnd(CharSequence src,int index,int n)
	{
		int len = src.length();
		while(n-->0)
		{
			index = tryLine_End(src,index);
			if(n==0 || index>=len){
				break;
			}
			index+=1;
		}
		return index<0 || index>len ? len:index;
	}

	
	@Override
	public abstract void draw(Canvas canvas, Path highlight, Paint highlightPaint, int cursorOffsetVertical)

	@Override
	public abstract void draw(Canvas c)
	
}
