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
   如果您有时间，可以自己解决这个问题(不过我已经解决了)
*/
public abstract class BlockLayout extends Layout
{
	public static final int MaxCount = 100000;
	public static final char FN = '\n', FT = '\t';
	public static final float MinScacle = 0.5f, MaxScale = 2.0f;
	public static final float TextSize = 40f;
	public static final int TextColor = 0xffaaaaaa;
	
	//临时变量
	protected char[] chars;
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
		setText(base,0,base.length());
	
		scaleLayout = scale;
		lineSpacing = spacingmult;
		this.cursorWidth = cursorWidth;
	}
	public void setPaint(TextPaint paint)
	{
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
		TextPaint paint = getPaint();
		float lastSacle = scaleLayout;
		float textSize = paint.getTextSize()/scaleLayout;
		
		//首先我们缩放文本的大小
		scaleLayout *= scale;
		scaleLayout = scaleLayout<MinScacle ? MinScacle:scaleLayout;
		scaleLayout = scaleLayout>MaxScale ? MaxScale:scaleLayout;
		paint.setTextSize(textSize*scaleLayout);
		
		//我们还应该同步maxWidth的大小
		scale = scaleLayout/lastSacle;
		for(int j=mWidths.size()-1;j>=0;--j){
			mWidths.set(j,mWidths.get(j)*scale);
		}
		maxWidth = maxWidth*scale;
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
	private void addBlock(int i,CharSequence text,int tbStart,int tbEnd)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text,tbStart,tbEnd);
		mBlocks.add(i,builder);
		mLines.add(i,0);
		mWidths.add(i,0f);
	}
	/* 移除文本块 */
	private void removeBlock(int i){
		mBlocks.remove(i);
		mLines.remove(i);
		mWidths.remove(i);
	}
	
	/* 设置文本 */
	private void setText(CharSequence text,int tbStart,int tbEnd)
	{
		clearText();
		insertForBlocks(0,text,tbStart,tbEnd);
	}
	/* 清除文本 */
	private void clearText()
	{
		mBlocks.clear();
		mLines.clear();
		mWidths.clear();
		addBlock();
		maxWidth = 0;
		lineCount = 0;
	}
	
	/* 从指定id的文本块开始，分发text中指定范围内的文本 */
	private void dispatchTextBlock(int id, CharSequence text,int tbStart,int tbEnd)
	{
		//每次从tbStart开始向后切割MaxCount个字符，并添加到mBlocks中，直至tbEnd
		while(true)
		{
			if(tbEnd-tbStart<=MaxCount){
				//最后一个块，直接切割到tbEnd
				addBlock(id,text,tbStart,tbEnd);
				break;
			}
			
			//切割范围内的文本，并插入到刚创建的文本块中
			addBlock(id,text,tbStart,tbStart+MaxCount);

			//继续向后找下个位置
			tbStart+=MaxCount;
			++id;
		}
		cacheId = id;
		//保存分发到的位置
	}
	
	/* 从全部文本的index处开始，插入text中指定范围内的文本，并进行测量 */
	public void insert(int index, CharSequence text, int tbStart, int tbEnd)
	{
		//找到index所指定的文本块，并将index偏移到文本块的下标
	    int i = findBlockIdForIndex(index);
		SpannableStringBuilder builder = mBlocks.get(i);
		int nowLen = builder.length();
		index -= cacheLen;

		int len = tbEnd-tbStart;
		if(nowLen+len<=MaxCount)
		{
			//当插入文本不会超出当前的文本块时，直接插入
			insertForBlock(i,index,text,tbStart,tbEnd);
		}
		else
		{
			/*当插入文本会超出当前的文本块时，两种方案

			 *插分删，总长度为 
			 插入文本:  len
			 分发超出部分:  (nowLen+len-MaxCount)
			 删除超出部分:  (nowLen+len-MaxCount)

			 *分插删，总长度为:
			 分发文本   len
			 插入index后的文本到末尾   nowLen-index
			 删除index后的文本    nowLen-index

			 *容易看出，
			 方案1的总量为 len + 2*(nowLen+len-MaxCount)
			 方案2的总量为 len + 2*(nowLen-index);
			 如果 nowLen+len-MaxCount <= nowLen-index，使用方案1，否则使用方案2

			 *另外也可以知道，
			 nowLen-index的最大值为MaxCount，
			 也就是说一旦nowLen+len-MaxCount > MaxCount，默认使用方案2
			 更确切地说，方案一只处理溢出小于MaxCount的情况，方案二则可处理更多情况
			 
			*/
			if(nowLen+len-MaxCount <= nowLen-index)
			{
				//方案1，先插入，之后截取多出的部分，适合小量文本
				insertForBlock(i,index,text,tbStart,tbEnd);
				nowLen = builder.length();

				if (mBlocks.size()-1 == i){
					//若无下个文本块，则添加一个
					addBlock();
				}
				else if (mBlocks.get(i+1).length()+nowLen-MaxCount > MaxCount){
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个
					addBlock(i+1);
				}

				//删除前先截取要删除的部分
				CharSequence sub = builder.subSequence(MaxCount,nowLen);
				//删除超出部分并同步文本块的行和宽，为什么要先删除呢？因为文本末尾的连接处的宽是与下一块共同测量得出的，删除时也应该先测量与下一块的连接
				deleteForBlocks(i,i,MaxCount,nowLen);
				//之后将超出的字符串添加到文本块列表中的下个文本块开头
				insertForBlock(i+1,0,sub,0,sub.length());
			}
			else
			{
				//方案2，精确计算删除和分发的部分，适合大量文本
				CharSequence sub = builder.subSequence(index,nowLen);
				//先删除这部分
				deleteForBlocks(i,i,index,nowLen);
				//逆序重新插入，保证文本整体插入位置不变
				dispatchTextBlock(i+1,text,tbStart,tbEnd);
				//将index后的部分挪到插入文本后
				addBlock(cacheId+1,sub,0,sub.length());
				//最后一并测量它们
				measureInsertBlocksAfter(i+1,cacheId+1,0,sub.length());
			}
		}
	}
	
	/* 删除全部文本中start~end之间的文本，并进行测量 */
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
			int nowLen = mBlocks.get(j).length();
			if(end-nowLen<=0){
				break;
			}
			end-=nowLen;
		}
		
		//删除范围内的文本和文本块，并进行测量
		deleteForBlocks(i,j,start,end);
	}
	
	/* 从指定文本块的指定位置插入文本，插入完成后进行一次测量 */
	private void insertForBlock(int id, int index, CharSequence text, int tbStart, int tbEnd)
	{
		mBlocks.get(id).insert(index,text,tbStart,tbEnd);
		measureInsertBlocksAfter(id,id,index,index+tbEnd-tbStart);
	}
	/* 从指定文本块开始分发文本，在分发完成后进行一次全部测量 */
	private void insertForBlocks(int id, CharSequence text, int tbStart, int tbEnd)
	{
		dispatchTextBlock(id,text,tbStart,tbEnd);
		measureInsertBlocksAfter(id,cacheId,0,mBlocks.get(cacheId).length());
	}
	/* 删除指定范围内的文本和文本块，并在删除前和后进行测量 */
	private void deleteForBlocks(int i, int j, int start, int end)
	{
		if(i==j)
		{
			//只要删除一个文本块中的内容
			SpannableStringBuilder builder = mBlocks.get(i);
			boolean is = measureDeleteBlockBefore(i,start,end);
			if(start!=0 || end!=builder.length()){
				//如果文本块没有被全部删除，我们才需要测量
			    builder.delete(start,end);
			    measureDeleteBlockAfter(i,start,is);
			}
		}
		else
		{
			SpannableStringBuilder sb = mBlocks.get(i);
			SpannableStringBuilder eb = mBlocks.get(j);

			//删除前，先进行测量
			boolean isStart = measureDeleteBlockBefore(i,start,sb.length());
			for(++i;i<j;--j){
				//中间的块必然不会进行测量，而是全部删除
				measureDeleteBlockBefore(i,0,mBlocks.get(i).length());
			}
			boolean isEnd = measureDeleteBlockBefore(j,0,end);

			//在删除完成后，只剩下了起始和末尾的块，因此只要测量它们
			if(start!=0){
				//如果sb没有被移除，它就是i-1，删除后起始位置为start
				sb.delete(start,sb.length());
				measureDeleteBlockAfter(i-1,start,isStart);
			}
			if(end!=eb.length()){
				//如果eb没有被移除，那么在之间的块移除后，它就是j，删除后起始位置为0
				eb.delete(0,end);
				measureDeleteBlockAfter(j,0,isEnd);
			}
		}
	}
	
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
		SpannableStringBuilder builder = mBlocks.get(id);
		if(start==0 && end==builder.length())
		{
			//如果文本块会被全删了，直接移除它
			lineCount-=mLines.get(id);
			removeBlock(id);
			maxWidth = checkMaxWidth();
		}
		else
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
		SpannableStringBuilder builder = mBlocks.get(id);
		if(needMeasureAllText){
			//如果需要全部测量
			width = measureBlockWidth(id,0,builder.length());
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
		SpannableStringBuilder builder = mBlocks.get(i);
		int s = tryLine_Start(builder,start);
		int e = tryLine_End(builder,end);
		float width = getDesiredWidthForType(builder,s,e,getPaint());
		if(s==0){
			float w = measureBlockJoinTextStart(i);
			width = w>width ? w:width;
		}
		if(e==builder.length()){
			float w = measureBlockJoinTextEnd(i);
			width = w>width ? w:width;
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
		CharSequence str = mBlocks.get(id);
		int len = getText().length();
		int index = toLine<hasLine-toLine ? NIndexForType(FN,str,0,toLine):lastNIndexForType(FN,str,str.length()-1,hasLine-toLine+1);
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
		
		CharSequence text = mBlocks.get(id);
		startLine+= CountForType(FN,text,0,offset-startIndex);
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
		CharSequence text = getText();
		int start = getLineStart(p1);
		int end = tryLine_End(text,start);
		return CountForType(FT,text,start,end)!=0;
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
	
	/* 获取光标坐标 */
	final public void getCursorPos(int offset,pos pos)
	{  
	    //找到index所指定的块
		moveToIndexBlock(offset);
		int id = cacheId;
		int startLine = cacheLine;
		int startIndex = cacheLen;
		CharSequence text = mBlocks.get(id);
	
		//我们仍需要去获取全部文本去测量宽，但是只测量到offset的上一行，然后我们计算它们之间的宽
		pos.x = getPrimaryHorizontal(offset);
		
		//offset在使用完后转化为当前块的下标，测量当前块的起始到offset之间的行数，并加上之前的行数，最后计算行数的高
		offset = offset-startIndex;
		int lines = CountForType(FN,text,0,offset)+startLine;
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
		
		if(oldOffset<newOffset){
			int line = CountForType(FN,text,oldOffset,newOffset);
			target.y = y+getLineHeight()*line;
		}
		else if(oldOffset>newOffset){
			int line = CountForType(FN,text,newOffset,oldOffset);
			target.y = y-getLineHeight()*line;
		}
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
		CharSequence text = getText();
		TextPaint paint = getPaint();
		float lineHeight = getLineHeight();
		RectF rf = rectF;
		
		pos s = tmp;
		pos e = tmp2;
		getCursorPos(start,s);
		if(end-start>MaxCount){
			getCursorPos(end,e);
		}
		else{
		    nearOffsetPos(start,s.x,s.y,end,e);
		}
		
		float w = getDesiredWidthForType(text,start,end,paint);
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
	/* 为了效率，我们通常不允许一个一个charAt，而是先获取范围内的chars，再遍历数组 */
	final public float getDesiredWidth(GetChars text, int start, int end, TextPaint paint)
	{
		chars = chars==null || chars.length < end-start ? new char[end-start] : chars;
		text.getChars(start,end,chars,0);
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
		chars = chars==null || chars.length < end-start ? new char[end-start] : chars;
		text.getChars(start,end,chars,0);
		cacheLine = CharArrHelper.Count(FN,chars,0,end-start);
		return cacheLine*getLineHeight();
	}
	
/*
  一串等长字符串(1000000行*15字)
  直接getChars，再遍历整个数组，耗时80ms，
  直接subSequence，再toString，再遍历String，耗时118ms
  直接charAt遍历，耗时168ms
	  
  还有，千万别用Layout的getDesiredWidth，就1000000行*15字，硬是花了我2480ms，这是怎么测的啊？！
*/
	
	/* 测量文本块宽度，但会根据不同类型，选择更省时的方案 */
	final public float getDesiredWidthForType(CharSequence text, int start, int end, TextPaint paint)
	{
		float width;
		if(text instanceof String){
			//String不用截取，直接测量
			width = getDesiredWidth((String)text,start,end,paint);
		}
		else if(text instanceof GetChars){
			//GetChars可以先截取数组再测量
			width = getDesiredWidth((GetChars)text,start,end,paint);
		}
		else{
			//我们永远不要去一个个charAt，因为效率太低
			String str = text.subSequence(start,end).toString();
			width = getDesiredWidth(str,0,str.length(),paint);
		}
		return width;
	}
	/* 测量文本块高度，但会根据不同类型，选择更省时的方案 */
	final public float getDesiredHeightForType(CharSequence text, int start, int end, TextPaint paint)
	{
		float height;
		if(text instanceof String){
			height = getDesiredHeight((String)text,start,end);
		}
		else if(text instanceof GetChars){
			height = getDesiredHeight((GetChars)text,start,end);
		}
		else{
			String str = text.subSequence(start,end).toString();
			height = getDesiredHeight(str,0,str.length());
		}
		return height;
	}
	
	/* 根据不同类型选择更省时的统计方案 */	
	final public int CountForType(char c, CharSequence text, int start, int end)
	{
		int count;
		if(text instanceof String){
			count = StringSpiltor.Count(c,(String)text,start,end);
		}
		else if(text instanceof GetChars){
			chars = chars==null || chars.length < end-start ? new char[end-start] : chars;
			((GetChars)text).getChars(start,end,chars,0);
			count = CharArrHelper.Count(c,chars,0,end-start);
		}
		else{
			String str = text.subSequence(start,end).toString();
			count = StringSpiltor.Count(c,str,0,str.length());
		}
		return count;
	}
	/* 根据不同类型选择更省时的查找方案 */
	final public int NIndexForType(char c,CharSequence text,int index, int n)
	{
		int i;
		int len = text.length();
		if(text instanceof String){
			i = StringSpiltor.NIndex(c,(String)text,index,n);
		}
		else if(text instanceof GetChars){
			chars = chars==null || chars.length < len-index ? new char[len-index] : chars;
			((GetChars)text).getChars(index,len,chars,0);
			i = index+ CharArrHelper.NIndex(c,chars,0,n);
		}
		else{
			String str = text.subSequence(index,len).toString();
			i = index+ StringSpiltor.NIndex(c,str,0,n);
		}
		return i;
	}
	final public int lastNIndexForType(char c,CharSequence text,int index, int n)
	{
		int i;
		if(text instanceof String){
			i = StringSpiltor.lastNIndex(c,(String)text,index,n);
		}
		else if(text instanceof GetChars){
			chars = chars==null || chars.length < index+1 ? new char[index+1] : chars;
			((GetChars)text).getChars(0,index+1,chars,0);
			i = CharArrHelper.lastNIndex(c,chars,index,n);
		}
		else{
			String str = text.subSequence(0,index+1).toString();
			i = StringSpiltor.lastNIndex(c,str,index,n);
		}
		return i;
	}
	
	/* 安全地获取数据 */
	final protected void fillChars(GetChars text, int start, int end){
		chars = chars==null || chars.length<end-start ? new char[end-start]:chars;
		text.getChars(start,end,chars,0);
	}
	final protected void fillWidths(CharSequence text, int start, int end, TextPaint paint){
		widths = widths==null || widths.length<end-start ? new float[end-start]:widths;
		paint.getTextWidths(text,start,end,widths);
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
