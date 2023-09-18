package com.editor.text;

import android.text.*;
import android.util.*;
import com.editor.text.base.*;
import java.lang.reflect.*;
import java.util.*;


/** 这是内容和标记都可以更改的文本类，删除了监视器，PARAGRAPH标志，并优化效率 */
public class SpannableStringBuilderLite implements CharSequence, GetChars, Spannable, Editable, Appendable, EditableBlock
{

    private final static String TAG = "SpannableStringBuilderLite";

    /** 创建一个包含空内容的新SpannableStringBuilder */
    public SpannableStringBuilderLite() {
        this("");
    }
    /** 创建一个包含指定文本的新SpannableStringBuilder，包括其范围(如果有) */
    public SpannableStringBuilderLite(CharSequence text) {
        this(text, 0, text.length());
    }
    /** 创建一个新的SpannableStringBuilder，其中包含指定文本的指定部分，包括其范围(如果有) */
    public SpannableStringBuilderLite(CharSequence text, int start, int end) 
    {
        int srclen = end - start;
        if (srclen < 0) throw new StringIndexOutOfBoundsException();
        //创建一个更大的文本数组来拷贝指定范围内的文本，将多余的空间作为间隙
        mText = ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(srclen));
        mGapStart = srclen;
        mGapLength = mText.length - srclen;
        TextUtils.getChars(text, start, end, mText, 0);

        mSpanCount = 0;
        mSpanInsertCount = 0;
        mSpans = EmptyArray.OBJECT;
        mSpanStarts = EmptyArray.INT;
        mSpanEnds = EmptyArray.INT;
        mSpanFlags = EmptyArray.INT;
        mSpanMax = EmptyArray.INT;
        mSpanOrder = EmptyArray.INT;

        if (text instanceof Spanned) 
        {
            //如果增加的文本是Spanned，需要获取范围内全部的span并附加到自身
            Spanned sp = (Spanned) text;
            Object[] spans = sp.getSpans(start, end, Object.class);
            for (int i = 0; i < spans.length; i++) 
            {
                if (spans[i] instanceof NoCopySpan) {
                    continue;
                }

                //将span在原字符串中较start的偏移量获取，并偏移到自身中的位置
                int st = sp.getSpanStart(spans[i]) - start;
                int en = sp.getSpanEnd(spans[i]) - start;
                int fl = sp.getSpanFlags(spans[i]);

                //范围不可超过自己的大小
                if (st < 0)
                    st = 0;
                if (st > end - start)
                    st = end - start;
                if (en < 0)
                    en = 0;
                if (en > end - start)
                    en = end - start;
                setSpan(false, spans[i], st, en, fl, false);
            }
            //设置完span后一并刷新
            quickRestoreInvariants();
        }
    }
    public static SpannableStringBuilderLite valueOf(CharSequence source)
    {
        if (source instanceof SpannableStringBuilderLite) {
            return (SpannableStringBuilderLite) source;
        } else {
            return new SpannableStringBuilderLite(source);
        }
    }

    //编辑器通常会在连续的光标位置插入字符，因此为了提升文本插入效率，使用Gap Buffer(间隙缓冲区)
    //间隙缓冲区使用GapStart和GapLenght表示文本数组中空闲的间隙位置，当有文本插入间隙时，不用将整个数组扩展，而是将指针偏移缩小间隙缓冲区
    //为了达到这种效果，每次插入新的字符，就将间隙缓冲区移到光标位置，因此若之后也在连续位置插入字符，可以大大提升效率
    //间隙缓冲区中的内容总是无效的，它不被计入总文本之中(总文本实际上是处于间隙缓冲区之前和之后的文本)，若间隙缓冲区长度不足，需要进行扩展

    /**返回文本中指定偏移量处的字符*/
    public char charAt(int where) 
    {
        int len = length();
        if (where < 0) {
            throw new IndexOutOfBoundsException("charAt: " + where + " < 0");
        } else if (where >= len) {
            throw new IndexOutOfBoundsException("charAt: " + where + " >= length " + len);
        }
        //在间隙缓冲区之后的字符的真实位置总是加上一个间隙缓冲区长度
        if (where >= mGapStart)
            return mText[where + mGapLength];
        else
            return mText[where];
    }

    /**文本长度总是数组长度减去间隙缓冲区长度*/
    public int length() {
        return mText.length - mGapLength;
    }

    /*如果此偏移量在间隙缓冲区之后，那么它的原本位置应减去间隙缓冲区长度*/
    private int resolveGap(int i) {
        return i > mGapStart ? i - mGapLength : i;
    }

    /*修改文本数组的长度的同时修改间隙缓冲区的大小*/
    private void resizeFor(int size) 
    {
        final int oldLength = mText.length;
        if (size+1 <= length()) {
            //假设最少额外扩展1个元素后，size仍装不下文本，或者装满了但间隙缓冲区已经没有长度了
            return;
        }

        //创建一个比size更大的数组，并将原数组中间隙缓冲区之前的字符拷贝到新数组开头
        char[] newText = ArrayUtils.newUnpaddedCharArray(GrowingArrayUtils.growSize(size));
        System.arraycopy(mText, 0, newText, 0, mGapStart);
        final int newLength = newText.length;
        //新增的长度，可以是负数，负数代表缩小数组长度
        final int delta = newLength - oldLength;
        //原数组中间隙缓冲区之后的内容的长度
        final int after = oldLength - (mGapStart + mGapLength);
        //将原数组中间隙缓冲区之后的字符也拷贝到新数组末尾，中间多预留一些位置以扩展间隙缓冲区大小(请注意，间隙缓冲区的大小永远是数组中空闲的大小)
        System.arraycopy(mText, oldLength - after, newText, newLength - after, after);

        //轮替mText，间隙缓冲区长度增加
        mText = newText;
        mGapLength += delta;

        if (mGapLength < 1)
            new Exception("mGapLength < 1").printStackTrace();
        if (mSpanCount != 0) 
        {
			if(mGapStart>length()*0.75){
				//遍历在间隙缓冲区之后的span，并将范围增加delta
				moveSpansPoint(delta,treeRoot());
			}
			else{
				//遍历所有span，在间隙缓冲区之后的span的范围会增加delta
				for (int i = 0; i < mSpanCount; i++) {
					if (mSpanStarts[i] > mGapStart) mSpanStarts[i] += delta;
					if (mSpanEnds[i] > mGapStart) mSpanEnds[i] += delta;
				}
			}
            //重新计算节点最大范围
            calcMax(treeRoot());
        }
    }
	
	/* 递归增大缓冲区之后的span */
	private void moveSpansPoint(int delta, int i) 
	{
		if((i & 1) != 0)
		{
			//左子节点需要在mGapStart后面
			int left = leftChild(i);
			if(mSpanMax[left] > mGapStart){
				moveSpansPoint(delta,left);
			}
		}
		if(i<mSpanCount)
		{
			//节点i自己在mGapStart后面
			if (mSpanStarts[i] > mGapStart) mSpanStarts[i] += delta;
			if (mSpanEnds[i] > mGapStart) mSpanEnds[i] += delta;
			//无法保证右子节点不在后面
			if((i & 1) != 0){
				moveSpansPoint(delta,rightChild(i));
			}
		}
	}
	
	/* 修改span数组的大小 */
	private void resizeForSpans(int size)
	{
        if (size+1 < mSpanCount) {
            //size无法装入全部span
            return;
        }
		
        //这些数组都是同时添加元素，因此它们长度相等
		mSpans = ArrayUtils.copyNewArray(Object.class,mSpans,mSpanCount,GrowingArrayUtils.growSize(size));
		mSpanStarts = ArrayUtils.copyNewIntArray(mSpanStarts,mSpanCount,GrowingArrayUtils.growSize(size));
		mSpanEnds = ArrayUtils.copyNewIntArray(mSpanEnds,mSpanCount,GrowingArrayUtils.growSize(size));
		mSpanFlags = ArrayUtils.copyNewIntArray(mSpanFlags,mSpanCount,GrowingArrayUtils.growSize(size));
		mSpanOrder = ArrayUtils.copyNewIntArray(mSpanOrder,mSpanCount,GrowingArrayUtils.growSize(size));

		//mSpanMax数组的大小是最小区间树的大小
		int sizeOfMax = 2 * treeRoot() + 1;
		if(mSpanMax.length > sizeOfMax){
			int[] newSpanMax = new int[sizeOfMax];
			System.arraycopy(mSpanMax,0,newSpanMax,0,newSpanMax.length);
			mSpanMax = newSpanMax;
		}
	}

    /*移动间隙缓冲区到指定位置*/
    private void moveGapTo(int where)
    {
        if (where == mGapStart)
            return;
        if (where < mGapStart) {
            //如果要移动到的位置在当前间隙缓冲区之前，仅需将间隙缓冲区与其位置置换
            int overlap = mGapStart - where;
            System.arraycopy(mText, where, mText, mGapStart + mGapLength - overlap, overlap);
        } else {
            //否则，按相反的顺序置换
            int overlap = where - mGapStart;
            System.arraycopy(mText, where + mGapLength - overlap, mText, mGapStart, overlap);
        }

        // 聪明一点(虽然赢的真的没那么大)
        if (mSpanCount != 0) 
        {
            //遍历所有的span，调整它们的位置
            for (int i = 0; i < mSpanCount; i++) 
            {
                int start = mSpanStarts[i];
                int end = mSpanEnds[i];

                //下面的代码分为两步理解
                //先认为我们把间隙缓冲区移除了，因此在间隙缓冲区之后的span的真实位置都前移mGapLength(也有可能是之前的span的flags变化了，这里调整回来)
                //之后又把间隙缓冲区插入到where的位置，因此在where之后的span的真实位置都后移mGapLength
                if (start > mGapStart)
                    start -= mGapLength;
                if (start > where)
                    start += mGapLength;
                else if (start == where) {
					//如果在span的端点处插入字符
                    int flag = (mSpanFlags[i] & START_MASK) >> START_SHIFT;
                    if (flag == POINT)
						start += mGapLength;
					//POINT标志的端点应将缓冲区排除在前面(自己向后移)
					//而MARK标志的端点应将缓冲区排除在后面(保持不变)
					//这里为什么将POINT标志的端点移到start += mGapLength？ 有三个原因:
					//1、防止span在removeSpansForChange中被移除，请再看一下移除span的条件
					//2、span端点在updateIntervalBound中不用再管了，请再看一下span端点修正的条件
					//3、使刚好衔接在插入点的端点扩展，每次修改文本，就将光标移到插入点，刚好处于此位置的端点可以移到缓冲区之后，以此在插入文本时扩展  
                }

                if (end > mGapStart)
                    end -= mGapLength;
                if (end > where)
                    end += mGapLength;
                else if (end == where) {
                    int flag = (mSpanFlags[i] & END_MASK);
                    if (flag == POINT)
                        end += mGapLength;
                }

                mSpanStarts[i] = start;
                mSpanEnds[i] = end;
            }
            //重新计算节点最大范围
            calcMax(treeRoot());
        }
        //最后才将mGapStart修改
        mGapStart = where;
    }
	
	public SpannableStringBuilderLite append(char text) {
        return append(String.valueOf(text));
    }
    public SpannableStringBuilderLite append(CharSequence text){
        return append(text, 0, text.length());
    }
    public SpannableStringBuilderLite append(CharSequence text, int start, int end) {
        int length = length();
        return replace(length, length, text, start, end);
    }

	public SpannableStringBuilderLite insert(int where, CharSequence tb) {
        return replace(where, where, tb, 0, tb.length());
    }
    public SpannableStringBuilderLite insert(int where, CharSequence tb, int start, int end) {
        return replace(where, where, tb, start, end);
    }
    public SpannableStringBuilderLite delete(int start, int end) {
        return replace(start, end, "", 0, 0); 
    }
    public SpannableStringBuilderLite replace(int start, int end, CharSequence tb) {
        return replace(start, end, tb, 0, tb.length());
    }

    /** 替换start~end范围的文本为tb中的tbstart~tbend之间的文本，并改变span的位置 */
    public SpannableStringBuilderLite replace(final int start, final int end, CharSequence tb, int tbstart, int tbend)
    {
		//在修改前判断范围错误，防止之后出现无法挽回的损失
        checkRange("replace", start, end);
		//过滤文本
        for (int i=0; i<mFilters.length; i++) {
            CharSequence repl = mFilters[i].filter(tb, tbstart, tbend, this, start, end);
            if (repl != null) {
                tb = repl;
                tbstart = 0;
                tbend = repl.length();
            }
        }
        final int origLen = end - start;
        final int newLen = tbend - tbstart;
        if (origLen == 0 && newLen == 0) {
            //如果tb中没有要添加的跨度(长度为0)，提前退出，以便文本观察器不会得到通知
            return this;
        }     
        //改变文本和span
        change(start, end, tb, tbstart, tbend);
		if(AutoReleaseExcessMemory){
			//修改后释放多余空间
			ReleaseExcessMemory();
		}
        return this;
    }

	/* 替换start~end范围的文本为tb中的tbstart~tbend之间的文本，并改变span的位置 */
	private void change(final int start, final int end, CharSequence cs, int csStart, int csEnd) 
    {
        //删除文本的长度，插入的文本长度，溢出文本的长度(可以是负数)
        final int replacedLength = end - start;
        final int replacementLength = csEnd - csStart;
        final int nbNewChars = replacementLength - replacedLength;

        //将间隙缓冲区移动到删除文本的末尾
        moveGapTo(end);
        if (nbNewChars >= mGapLength) {
            //间隙缓冲区不足以容纳溢出的文本，需要扩展间隙缓冲区
            resizeFor(mText.length + nbNewChars - mGapLength);
        }

        final boolean textIsRemoved = replacementLength == 0;
        //需要在间隙缓冲区位置更新之前完成移除过程，以便将正确的先前位置传递给正确的相交跨度观察器
		//由于文本还未插入，所以不要在此时释放多余空间，避免数组长度不够
        if (replacedLength > 0 && mSpanCount > 0)
		{
			//纯插入时不需要span移除，没有span时也不需要移除(因为0会导致leftChild为-1的bug)
			//要移除的span个数为0时，也不需要span移除
			final int count = markToBeRemovedSpans(start,end,textIsRemoved,treeRoot());
			if(count==1){
				//单个节点的移除，不需要遍历整个数组，沿着路径将其移除
				removeSpanForChange(start, end, textIsRemoved, treeRoot());
			}
			else if(count>1){
				//太多数量的span要移除，必须一次性移除后再刷新，因为restore消耗的时间比remove更多
				removeMarkSpans(count);
			}
        }

        //插入文本并不需要扩展数组，仅需将间隙缓冲区缩小
        //另一个情况是，nbNewChars是负数，说明要插入的文本太短，此时等同于扩展间隙缓冲区
        //无论怎样，都将间隙缓冲区对齐到插入文本的末尾
        mGapStart += nbNewChars;
        mGapLength -= nbNewChars;
        if (mGapLength < 1){
            new Exception("mGapLength < 1").printStackTrace();
		}
        TextUtils.getChars(cs, csStart, csEnd, mText, start);
        //然后插入文本，注意文本是从start开始插入的，所以start~end之间的内容已经被覆盖了，因此间隙缓冲区只用管溢出文本
		
		int updatedCount = 0;
        if (replacedLength > 0 && mSpanCount > 0)
        { 
            //修正所有在删除文本范围内的span的位置，范围之前或之后的span不修正，纯插入时不需要span修正
			//修正节点可能导致spanStarts和SpanMax错误，但IndexOfSpan仍是正确的
			//这并不影响之后添加span，因此我们暂时不刷新spanStarts和SpanMax，最好是在添加span之后一起刷新
            updatedCount = updatedIntervalBounds(start,nbNewChars,textIsRemoved,treeRoot());
			//updatedCount可能包含了一部分重复节点的端点，我们粗略地认为spanStart和spanEnd端点数量相同，但当它为1和0时例外
			updatedCount = updatedCount>1 ? updatedCount/2:updatedCount;
        }
	
		int addCount = 0;
		if (cs instanceof Spanned) 
        {
            //如果增加的文本是Spanned，需要获取范围内全部的span并附加到自身
            Spanned sp = (Spanned) cs;
            Object[] spans = sp.getSpans(csStart, csEnd, Object.class);
            for (int i = 0; i < spans.length; i++) 
            {      
                //只添加不重复的span
                if (getSpanStart(spans[i]) < 0)
                {
					Object span = spans[i];
					int st = sp.getSpanStart(span);
					int en = sp.getSpanEnd(span);
					//span的位置不可超过截取的范围
					if (st < csStart) st = csStart;
					if (en > csEnd) en = csEnd;

                    //将span在原字符串中较csStart的偏移量获取，并加上start偏移到新字符串中的位置
                    int copySpanStart = st - csStart + start;
                    int copySpanEnd = en - csStart + start;
                    int copySpanFlags = sp.getSpanFlags(span);
					//无效span不添加
                    if(!isInvalidSpan(span,copySpanStart,copySpanEnd,copySpanFlags)){
						setSpan(false, span, copySpanStart, copySpanEnd, copySpanFlags, true);
						addCount++;
					}
                }
            }
        }
		
		//添加span之后一并刷新，当然此刷新还可能包含updatedIntervalBounds时未刷新的内容
		if(updatedCount+addCount>mSpanCount/4){
			//当大量的节点被修改，我们使用快速排序
			quickRestoreInvariants();
		}
		else if(updatedCount+addCount>0){
			//小量节点被修改，使用插入排序
			if(updatedCount==0){
				//纯添加时，被修改的span都在后面
				restoreInvariants(mSpanCount-addCount);
			}else{
				restoreInvariants(1);
			}
		}
    }

    /* 文本变化后，在start~end范围内的文本中，下标为i的节点及其子节点是否要删除，删除了一个就立即返回true
	   注意，一旦任意一个节点被移除，函数直接返回，因为删除之后的节点下标都将是错误的
	   因此函数本质上只是从节点i开始向下找一个节点并移除，因此需要循环调用，以移除范围内的所有节点
	*/
    private boolean removeSpanForChange(int start, int end, boolean textIsRemoved, int i)
    {
        if ((i & 1) != 0) {
            //节点i不是叶子节点，若它的最大边界在start之后，则至少有一个左子节点可能在范围内，处理左子节点
			if (resolveGap(mSpanMax[i]) >= start &&
                removeSpanForChange(start, end, textIsRemoved, leftChild(i))) {
                return true;
            }
        }
        if (i < mSpanCount) 
        {
            if (mSpanStarts[i] >= start && mSpanStarts[i] <= mGapStart + mGapLength &&
                mSpanEnds[i] >= start && mSpanEnds[i] <= mGapStart + mGapLength){
                //如果整个节点在删除范围内，移除此节点
                mIndexOfSpan.remove(mSpans[i]);
                removeSpan(i, 0, true);
                return true;
            }
            //若节点i的start在end之前，并且有右子节点，处理右子节点(右子节点start>=节点i的start)
            return resolveGap(mSpanStarts[i]) <= end && (i & 1) != 0 &&
                removeSpanForChange(start, end, textIsRemoved, rightChild(i));
        }
        return false;
    }
	
	/* 标记即将移除的节点 */
	private int markToBeRemovedSpans(int start, int end, boolean textIsRemoved, int i)
    {
		int count = 0;
        if ((i & 1) != 0) {
            //节点i不是叶子节点，若它的最大边界在start之后，则至少有一个左子节点可能在范围内，处理左子节点
			if (resolveGap(mSpanMax[i]) >= start) {
				count = markToBeRemovedSpans(start, end, textIsRemoved, leftChild(i));
			}
        }
        if (i < mSpanCount) 
        {
            if (mSpanStarts[i] >= start && mSpanStarts[i] <= mGapStart + mGapLength &&
                mSpanEnds[i] >= start && mSpanEnds[i] <= mGapStart + mGapLength){
                //如果整个节点在删除范围内，标记此节点
				mIndexOfSpan.remove(mSpans[i]);
				invalidateIndex(i);
				mSpans[i] = null;
				count++;
            }
            
            if((i & 1) != 0){
				//若节点i的start在end之前，并且有右子节点，处理右子节点(右子节点start>=节点i的start)
				if(resolveGap(mSpanStarts[i]) <= end){
					count += markToBeRemovedSpans(start, end, textIsRemoved, rightChild(i));
				}
			}
	    }
		return count;
    }
	
	/* 移除标记的节点 */
	private void removeMarkSpans(int markCount)
	{
		if(markCount==0){
			return;
		}
		
		//这是一个简单的清除标记节点的算法，i为查找索引，j为有效索引
		int i=0, j=0;
		for(;i<mSpanCount;++i)
		{
			//当没有遇到标记的节点，它们一起向后走
			if(mSpans[j]==null)
			{
				//当遇到标记的节点，i需要向后找一个非标记的节点并移到j的位置
				for(;i<mSpanCount;++i)
				{
					if(mSpans[i]!=null){
						mSpans[j] = mSpans[i];
						mSpanStarts[j] = mSpanStarts[i];
						mSpanEnds[j] = mSpanEnds[i];
						mSpanFlags[j] = mSpanFlags[i];
						mSpanOrder[j] = mSpanOrder[i];
						mSpans[i] = null;
						break;
					}
				}
			}
			++j;
		}
		
		mSpanCount-=markCount;
		//在有序的数组中移除一些元素，不会打乱顺序
		restoreInvariants(mSpanCount);
	}
	
	/* 文本修改后，修正节点i及其子节点在修改范围内的位置，返回修正端点数 */
    private int updatedIntervalBounds(int start, int nbNewChars, boolean textIsRemoved, int i)
    {
		//resolveGap的使用时机是，保证所有节点的端点都不在间隙缓冲区中
		//由于在删除后，间隙缓冲区移动了，但节点没有移动，因此此时可能有一部分端点错误地分布在间隙缓冲区中
		//此函数正是应该将它们都移动到正确的位置(也就是间隙缓冲区两端)
		int updatedCount = 0;
		if ((i & 1) != 0) {
            //节点i不是叶子节点，若它的左子节点最大边界在start之后，则至少有一个左子节点可能在范围内，处理左子节点
			int left = leftChild(i);
			if(mSpanMax[left]>=start){
				updatedCount = updatedIntervalBounds(start,nbNewChars,textIsRemoved,left);
			}
		}
		if(i < mSpanCount)
		{
			if ((i & 1) != 0) {
				//节点i不是叶子节点，若它的spanStart<mGapStart+mGapLength，则至少有一个右子节点可能在范围内，处理右子节点
				//与遍历左子节点不同，注意这里为什么用节点i的spanStart判断是否需要遍历右子节点呢，因为右子节点的左子节点的spanStart可能小于右子节点的spanStart，但一定大于节点i的spanStart
				if(mSpanStarts[i]<mGapStart+mGapLength){
					updatedCount += updatedIntervalBounds(start,nbNewChars,textIsRemoved,rightChild(i));
				}
			}
			
			//节点i自己在范围内，就修正它的位置
			int ost = mSpanStarts[i];
			int oen = mSpanEnds[i];
			if (ost >= start && ost < mGapStart + mGapLength) 
			{
				final int startFlag = (mSpanFlags[i] & START_MASK) >> START_SHIFT;
				int nst = updatedIntervalBound(mSpanStarts[i], start, nbNewChars, startFlag,  textIsRemoved);
				if(nst!=ost){
					mSpanStarts[i] = nst;
					updatedCount++;
				}
			}
			if (oen >= start && oen < mGapStart + mGapLength) 
			{
				final int endFlag = (mSpanFlags[i] & END_MASK);
				int nen = updatedIntervalBound(mSpanEnds[i], start, nbNewChars, endFlag, textIsRemoved);
				if(nen!=oen){
					mSpanEnds[i] = nen;
					updatedCount++;
				}
			}
		}
		return updatedCount;
    }

    /* 文本修改后，在修改范围内的span位置应该移动到哪里，在修改范围外的span位置实际不变 */
    private int updatedIntervalBound(int offset, int start, int nbNewChars, int flag, boolean textIsRemoved)
    {
		if (flag == POINT) {
			//若span的端点为POINT标志，该端点应将插入文本排除在前面。也就是说，位于删除范围内的端点应移动到插入文本的末尾，即mGapStart
			//另一个情况是当端点位于start并且我们正在进行文本替换（而不是删除）时，该端点保持在start(意为将span之内的内容替换为另一个内容，span要包含替换的内容)
			if (textIsRemoved || offset > start) {
				return mGapStart + mGapLength;
			}
		} 
		else 
		{
			if (textIsRemoved || offset < mGapStart - nbNewChars) {
				//如果span端点为MARK标志(无标志的端点默认按MARK处理)，该端点应将插入文本排除在后面。所以应该将删除范围内的端点移动到开头(mGapStart - nbNewChars实际等于删除文本的end)
				return start;
			} else {
				//若offset刚好是位于范围结尾的端点，它应该包含替换的文本。因此移动到插入文本的末尾，即mGapStart
				return mGapStart;
			}
		}
        return offset;
    }
	
	//清空所有的内容
    public void clear() 
    {
        clearSpans();
		mGapStart = 0;
		mGapLength = mText.length;
		if(AutoReleaseExcessMemory){
			ReleaseExcessMemory();
		}
    }
	//清空所有的span
    public void clearSpans() 
    {
        //遍历所有的span并删除，只有mSpans中存储的是span的引用，因此将它置为null来释放span对象的空间，而其它的都是基本类型
        for (int i = mSpanCount - 1; i >= 0; i--) {
            mSpans[i] = null;
        }
        if (mIndexOfSpan != null) {
            mIndexOfSpan.clear();
        }
		mSpanCount = 0;
        mSpanInsertCount = 0;
		if(AutoReleaseExcessMemory){
			ReleaseExcessMemory();
		}
    }
	
	/* 实现接口 */
	public boolean isInvalidSpan(Object span, int start, int end, int flags){
		return start == end;
	}
	public boolean canRemoveSpan(Object span, int start, int end, boolean textIsRemoved)
	{
		if(mIndexOfSpan==null){
			return false;
		}
		Integer index = mIndexOfSpan.get(span);
		if(index!=null){
			if(resolveGap(mSpanStarts[index])>=start && resolveGap(mSpanEnds[index])<=end){
				return true;
			}
		}
		return false;
	}
	public boolean needExpandSpanStart(Object span, int flags){
		int startFlag = (flags & START_MASK) >> START_SHIFT;
		return startFlag == MARK;
	}
	public boolean needExpandSpanEnd(Object span, int flags){
		int endFlag = flags & END_MASK;
		return endFlag == POINT;
	}

	public void enforceSetSpan(Object what, int start, int end, int flags){
		setSpan(true, what, start, end, flags, true);
	}
    /** 用指定对象标记指定范围的文本 */
    public void setSpan(Object what, int start, int end, int flags) {
        setSpan(true, what, start, end, flags, false);
    }
	
    //注意:如果send为false，那么恢复不变量就是调用者的责任(如果send为false，并且跨度已经存在，则此方法不会更改任何跨度的索引)
    //因为新增的span默认在最后，不影响前面节点的顺序，所以可以暂时不刷新
    private void setSpan(boolean send, Object what, int start, int end, int flags, boolean enforce)
    {
        checkRange("setSpan", start, end);
        int flagsStart = (flags & START_MASK) >> START_SHIFT;
        int flagsEnd = flags & END_MASK;
        //0-长度跨度。SPAN_EXCLUSIVE_EXCLUSIVE
        if (!enforce && isInvalidSpan(what,start,end,flags)) {
            if (send) {
                Log.e(TAG, "SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length");
            }
            //从该类创建无效跨度时，自动忽略无效跨度。
            //这避免了在该类中完成对setSpan的所有调用之前重复上面的测试代码
            return;
        }

        //如果设置span的位置在缓冲区之后，它的真实位置应加上mGapLength
        if (start > mGapStart) {
            start += mGapLength;
        } else if (start == mGapStart) {
            if (flagsStart == POINT)
                start += mGapLength;
        }
        if (end > mGapStart) {
            end += mGapLength;
        } else if (end == mGapStart) {
            if (flagsEnd == POINT)
                end += mGapLength;
        }

        if (mIndexOfSpan != null) 
        {
            //如果已有该span，则修改它的范围和flags
            Integer index = mIndexOfSpan.get(what);
            if (index != null)
            {
                final int i = index;
				final int ost = mSpanStarts[i];
				final int oen = mSpanEnds[i];
                mSpanStarts[i] = start;
                mSpanEnds[i] = end;
                mSpanFlags[i] = flags;
                if (send && (ost!=start || oen!=end)) {
					//不用担心flags变化，它造成的位置变化已在之前应验
                    //是否要立刻修正index的位置错误，或等待以后一并修正
                    restoreInvariants(i);
                }
                return;
            }
        }

        //如果没有该span，就添加一个span
        mSpans = GrowingArrayUtils.append(mSpans, mSpanCount, what);
        mSpanStarts = GrowingArrayUtils.append(mSpanStarts, mSpanCount, start);
        mSpanEnds = GrowingArrayUtils.append(mSpanEnds, mSpanCount, end);
        mSpanFlags = GrowingArrayUtils.append(mSpanFlags, mSpanCount, flags);
        mSpanOrder = GrowingArrayUtils.append(mSpanOrder, mSpanCount, mSpanInsertCount);
        invalidateIndex(mSpanCount);
        mSpanCount++;
        mSpanInsertCount++;

        //确保有足够的空间容纳空的内部节点
        //这个神奇的公式计算出最小的完美二叉树的大小，可能大于mSpanCount
        int sizeOfMax = 2 * treeRoot() + 1;
        if (mSpanMax.length < sizeOfMax) {
            mSpanMax = new int[sizeOfMax];
        }
        if (send) {
            //需要发送事件，则更新数据，仅需从mSpanCount-1开始排序
            restoreInvariants(mSpanCount-1);
        }
    }

    /**从文本中移除指定的标记对象*/
    public void removeSpan(Object what) {
        removeSpan(what, 0, true);
    }
	//外部调用函数，会释放多余空间
    private void removeSpan(Object what, int flags, boolean send)
    {
        if (mIndexOfSpan == null) return;
        //获取span的下标，并移除它
        Integer i = mIndexOfSpan.remove(what);
        if (i != null) {
            removeSpan(i.intValue(), flags, send);
			if(AutoReleaseExcessMemory){	
			    //移除span时才需要释放空间
				ReleaseExcessMemory();
			}
        }
    }
    //注意:调用者负责删除mIndexOfSpan条目
    //此为文本修改时调用的removeSpan函数，不会释放多余空间
    private void removeSpan(int i, int flags, boolean send) 
    {
        //要移除此span，其实就是把此span之后的span全部往前挪一位
        int count = mSpanCount - (i + 1);
        System.arraycopy(mSpans, i + 1, mSpans, i, count);
        System.arraycopy(mSpanStarts, i + 1, mSpanStarts, i, count);
        System.arraycopy(mSpanEnds, i + 1, mSpanEnds, i, count);
        System.arraycopy(mSpanFlags, i + 1, mSpanFlags, i, count);
        System.arraycopy(mSpanOrder, i + 1, mSpanOrder, i, count);

        mSpanCount--;
        invalidateIndex(i);
        mSpans[mSpanCount] = null;  
		//将其置为null，以释放对象的空间
		//在发送span removed通知之前，必须恢复不变量，再用原本的数据发送事件
        if(send){
			//在有序的数组中移除一个元素，不会打乱顺序
			restoreInvariants(mSpanCount);
		}
    }

    /**返回指定标记对象开头在文本中的偏移量，如果该对象未附加到文本，则返回-1*/
    public int getSpanStart(Object what) 
    {
        if (mIndexOfSpan == null) return -1;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? -1 : resolveGap(mSpanStarts[i]);
    }
    /**返回指定标记对象末尾在文本中的偏移量，如果该对象未附加到文本，则返回-1*/
    public int getSpanEnd(Object what)
    {
        if (mIndexOfSpan == null) return -1;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? -1 : resolveGap(mSpanEnds[i]);
    }
    /**返回指定标记对象结尾的标志，如果它没有附加到此文本，则返回0*/
    public int getSpanFlags(Object what) 
    {
        if (mIndexOfSpan == null) return 0;
        Integer i = mIndexOfSpan.get(what);
        return i == null ? 0 : mSpanFlags[i];
    }

    /**返回指定类型的范围的数组，这些范围与指定的文本范围重叠。
	 种类可以是Object.class，以获得所有跨度的列表，而不考虑类型。
     */
    @SuppressWarnings("unchecked")
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        return getSpans(queryStart, queryEnd, kind, true);
    }
	@Override
	public <T extends Object> T[] quickGetSpans(int queryStart, int queryEnd, Class<T> kind){
		return getSpans(queryStart, queryEnd, kind, false);
	}
	
    /*** 返回指定类型跨度的数组，这些范围与指定的文本范围重叠。 
	 种类可能是 Object.class 以获取无论类型如何的所有跨度的列表。
	 * * @param querystart 开始索引。 
	 * @Param QueryEnd 结束索引。 
	 * @param kind 类类型进行搜索。
	 * @Param SortByInsertionOrder 如果为 true 结果按插入顺序排序。
	 * @param <t> * @return 跨度数组。
	 如果找不到结果，则为空数组。 
     **/
	private <T> T[] getSpans(int queryStart, int queryEnd,  Class<T> kind, boolean sortByInsertionOrder) 
    {
        if (kind == null) return (T[])EmptyArray.emptyArray(Object.class);
        if (mSpanCount == 0) return EmptyArray.emptyArray(kind);
		
		//如果要获取全部span，拷贝所有span
		if(queryStart==0 && queryEnd==length() && kind==Object.class)
		{
			T[] ret = (T[]) Array.newInstance(kind, mSpanCount);
			final int[] prioSortBuffer = sortByInsertionOrder ? obtain(mSpanCount) : EmptyArray.INT;
			final int[] orderSortBuffer = sortByInsertionOrder ? obtain(mSpanCount) : EmptyArray.INT;

			System.arraycopy(mSpans,0,ret,0,mSpanCount);
			if(sortByInsertionOrder){
				for(int i=0;i<mSpanCount;++i){
					prioSortBuffer[i] = mSpanFlags[i] & SPAN_PRIORITY;
					orderSortBuffer[i] = mSpanOrder[i];
				}
			}

			if (sortByInsertionOrder) {
				sort(ret, prioSortBuffer, orderSortBuffer);
				recycle(prioSortBuffer);
				recycle(orderSortBuffer);
			}
			return ret;
		}
		
		//创建列表，获取span
		final List<T> retList = EditableBlockList.obtainList();
		final List<Integer> prioList = sortByInsertionOrder ? EditableBlockList.obtainList():null;
		final List<Integer> orderList = sortByInsertionOrder ? EditableBlockList.obtainList():null;
		final int count = getSpansRec(queryStart,queryEnd,kind,treeRoot(),retList,prioList,orderList,0,sortByInsertionOrder);
		
	    //没找到span，提前回收列表
		if(count==0){
			EditableBlockList.recyleList(retList);
			if(sortByInsertionOrder){
				EditableBlockList.recyleList(prioList);
				EditableBlockList.recyleList(orderList);
			}
			return EmptyArray.emptyArray(kind);
		}
		
		//创建数组
		T[] ret = (T[]) Array.newInstance(kind, count);
		final int[] prioSortBuffer = sortByInsertionOrder ? obtain(count) : EmptyArray.INT;
        final int[] orderSortBuffer = sortByInsertionOrder ? obtain(count) : EmptyArray.INT;
		
		//将span拷贝到数组中
		retList.toArray(ret);
		if(sortByInsertionOrder){
			for(int i=0;i<count;++i){
				prioSortBuffer[i] = prioList.get(i);
				orderSortBuffer[i] = orderList.get(i);
			}	
		}
		
		//回收列表
		EditableBlockList.recyleList(retList);
		if(sortByInsertionOrder){
			EditableBlockList.recyleList(prioList);
			EditableBlockList.recyleList(orderList);
		}
		
        //如果需要排序，则按插入顺序排序，回收数组
        if (sortByInsertionOrder) {
            sort(ret, prioSortBuffer, orderSortBuffer);
            recycle(prioSortBuffer);
            recycle(orderSortBuffer);
        }
        return ret;
    }

    /** * 使用当前区间树节点下找到的跨度填充结果数组。 * 
	 * @param querystart 间隔查询的起始索引。 
	 * @Param QueryEnd 间隔查询的结束索引。
	 * @param kind 类类型进行搜索。
	 * @param i 当前树节点的索引。 
	 * @param ret 数组将被填充结果。
	 * @param priority buffer 记录找到的跨度优先级。
	 * @param insertionOrder 记录找到的跨度的插入顺序。
	 * @param count 找到的跨度数。
	 * @param sort flag 填充优先级和插入顺序。 
	 如果 false 则 * 具有优先级标志的跨度将在结果数组中进行排序。 
	 * @param <t> * @return 找到的跨度总数。 
	 */
	@SuppressWarnings("unchecked")
    private <T> int getSpansRec(int queryStart, int queryEnd, Class<T> kind, int i, List<T> ret, List<Integer> priority, List<Integer> insertionOrder, int count, boolean sort)
    {
        if ((i & 1) != 0) 
        {
            //若节点i不是叶子节点，先遍历其左子节点
            int left = leftChild(i);
            int spanMax = mSpanMax[left];
            if (spanMax > mGapStart) {
                spanMax -= mGapLength;
            }
            //若左子节点的spanMax >= queryStart，则左子节点中有至少一个在范围内的节点
            if (spanMax >= queryStart) {
                count = getSpansRec(queryStart, queryEnd, kind, left, ret, priority,
                                    insertionOrder, count, sort);
            }
        }
        if (i >= mSpanCount) return count;
        //i已经在有效元素之后，其右子节点的下标更大，因此不用找了

        int spanStart = mSpanStarts[i];
        if (spanStart > mGapStart) {
            spanStart -= mGapLength;
        }
        if (spanStart <= queryEnd) 
        {
            //若节点i自己在范围内，将自己添加到数组中
            int spanEnd = mSpanEnds[i];
            if (spanEnd > mGapStart) {
                spanEnd -= mGapLength;
            }
            if (spanEnd >= queryStart &&
                (spanStart == spanEnd || queryStart == queryEnd ||
                (spanStart != queryEnd && spanEnd != queryStart)) &&
                (Object.class == kind || kind.isInstance(mSpans[i]))) 
            {
                int spanPriority = mSpanFlags[i] & SPAN_PRIORITY;
                int target = count;
                if (sort) {
                    //如果需要排序，我们还要添加该节点的优先级和插入顺序
					priority.add(target,spanPriority);
					insertionOrder.add(target,mSpanOrder[i]);
                } 
                else if (spanPriority != 0) 
                {
                    //对具有优先级的元素进行插入排序，实际上是为即将添加的元素计算并留出一个位置
                    int j = 0;
                    for (; j < count; j++) {
                        int p = getSpanFlags(ret.get(j)) & SPAN_PRIORITY;
                        if (spanPriority > p) break;
                    }
                    target = j;
                }
                //将自己放入指定位置，但count每次指向最后的下一个元素
				ret.add(target,(T) mSpans[i]);
                count++;
            }
            //若节点i有右子节点，则还可以从右子节点开始找(因为右子节点spanStart大于或等于i)
            if ((i & 1) != 0) {
				//为什么count直接被赋值？ 因为count是递归累加的，它从传递的count开始，再次加上自己找到的个数后返回
				//这样做的原因是让count保持在数组当前最后一个元素的位置，以此按顺序放入元素
                count = getSpansRec(queryStart, queryEnd, kind, rightChild(i), ret, priority,
                                    insertionOrder, count, sort);
            }
        }
        return count;
    }

    /** 获取临时排序数组
	 * @param elementCount要返回的int[]的大小
	 * @返回一个长度至少为elementCount的int[]
	 */
    static int[] obtain(final int elementCount)
    {
        int[] result = null;
        synchronized (sCachedIntBuffer)
        {
            //如果找不到第一个可用的tmp数组，请尝试查找长度至少为elementCount的tmp数组
            int candidateIndex = -1;
            for (int i = sCachedIntBuffer.length - 1; i >= 0; i--)
            {
                if (sCachedIntBuffer[i] != null)
                {
                    if (sCachedIntBuffer[i].length >= elementCount) {
                        candidateIndex = i;
                        break;
                    } else if (candidateIndex == -1) {
                        candidateIndex = i;
                    }
                }
            }
            if (candidateIndex != -1) {
                result = sCachedIntBuffer[candidateIndex];
                sCachedIntBuffer[candidateIndex] = null;
            }
        }
        result = checkSortBuffer(result, elementCount);
        return result;
    }

    /** 
	 * 回收排序数组
	 * @param buffer要回收的数组
	 */
    static void recycle(int[] buffer)
    {
        synchronized (sCachedIntBuffer)
        {
            for (int i = 0; i < sCachedIntBuffer.length; i++) 
            {
                if (sCachedIntBuffer[i] == null || buffer.length > sCachedIntBuffer[i].length) {
                    sCachedIntBuffer[i] = buffer;
                    break;
                }
            }
        }
    }

    /** 检查数组的大小，并根据需要进行扩展
	 * @param buffer要检查的数组。
	 * @param size所需的大小。
	 * @如果当前大小大于所需大小，则返回相同的数组实例。
	 否则，将创建并返回一个*新实例。
	 */
    private static int[] checkSortBuffer(int[] buffer, int size)
    {
        if (buffer == null || size > buffer.length) {
            return ArrayUtils.newUnpaddedIntArray(GrowingArrayUtils.growSize(size));
        }
        return buffer;
    }

    //将数组表示为堆，堆的每个节点最多有两个子节点，节点从按层次从上至下，从左至右，按数组中的顺序排列
    //将下标为0的元素作为根节点，而根节点的左右子节点下标分别为1，2，并且下层的子节点下标为3，4，5，6，一直这样排列下去
    /*例如一列数 0，1，2，3，4，5，6
	 若表示为堆则是如下的结果:
	           0
	         ↙  ↘
	       1        2
	     ↙  ↘    ↙  ↘
	    3     4   5     6
	*/
    //一般地，堆中的任意节点i的父节点下标为i/2-1，而任意节点i的左子节点下标为i*2+1，而任意节点i的右子节点下标为i*2+2

    //大顶堆的性质: 大顶堆中，任意一个节点的子节点都小于它
    //要使用堆排序，需要将乱序的堆的路径全部按照大顶堆的方式排序，即从最后一个节点开始向上排序，最后到第一个节点时，所有路径都排好序，并且第一个节点最大
    //然后将第一个节点与最后一个节点交换位置，也就是将最大的节点踢出堆(实际放到数组最后)，并且需要维护刚刚交换的根节点，使其之下的路径顺序排列
    //之后再从剩下的最后一个节点开始，按相同的方式排序，每次都将剩下的最大节点移至末尾，最后所有节点按升序排列

    /** 迭代堆排序实现。它将首先按照优先级，然后按照插入顺序对跨度进行排序
	 优先级较高的范围将在优先级较低的范围之前。
	 如果优先级相同，跨度将按照插入顺序排序。
	 具有较低插入顺序的*范围将在具有较高插入顺序的范围之前。*
	 * @param array跨度要排序的数组。
	 * @ param priority的优先级
	 * @ param insertionOrder对象类型的插入顺序。
	 * @param <T> 
	 */
    static final <T> void sort(T[] array, int[] priority, int[] insertionOrder) 
    {
        int size = array.length;
        //从最后一个节点的父节点开始，向前将所有节点排序，构建一个大顶堆
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(i, array, size, priority, insertionOrder);
        }
        //从最后一个节点开始，向前一个个交换位置来排序
        for (int i = size - 1; i > 0; i--) 
        {
            //每次将节点i与根节点交换位置，使得最大的值移至末尾，末尾的值移至开头
            final T tmpSpan =  array[0];
            array[0] = array[i];
            array[i] = tmpSpan;
            final int tmpPriority =  priority[0];
            priority[0] = priority[i];
            priority[i] = tmpPriority;
            final int tmpOrder =  insertionOrder[0];
            insertionOrder[0] = insertionOrder[i];
            insertionOrder[i] = tmpOrder;

            //交换完成后，需要维护堆的顺序，仅需维护根节点的路径，并且堆的节点个数少1
            siftDown(0, array, i, priority, insertionOrder);
        }
    }

    /** 堆的维护函数
	 * @param index 要维护的元素的索引。
	 * @param array 要排序的数组。
	 * @param size当前堆大小。
	 * @ param priority 数组元素的优先级。
	 * @ param insertionOrder 数组元素的插入顺序。
	 */
    private static final <T> void siftDown(int index, T[] array, int size, int[] priority, int[] insertionOrder) 
    {
        //从index的左子节点开始
        int left = 2 * index + 1;
        while (left < size)
        {
            if (left < size - 1 && compareSpans(left, left + 1, priority, insertionOrder) < 0) {
                //如果左子节点小于右子节点，右子节点是最大的(left++)，否则左子节点最大(left)
                left++;
            }
            if (compareSpans(index, left, priority, insertionOrder) >= 0) {
                //将index节点与其左右子节点中最大的节点比较，若此节点比它的子节点都大，则路径下的顺序已经正确了，不用比较了
                break;
            }

            //将index指向的节点与其左右子节点中最大的节点交换位置
            final T tmpSpan =  array[index];
            array[index] = array[left];
            array[left] = tmpSpan;
            final int tmpPriority =  priority[index];
            priority[index] = priority[left];
            priority[left] = tmpPriority;
            final int tmpOrder =  insertionOrder[index];
            insertionOrder[index] = insertionOrder[left];
            insertionOrder[left] = tmpOrder;

            //向下走到最大的子节点，并在之后与其左右子节点比较
            index = left;
            left = 2 * index + 1;
        }
    }

    /** *比较数组中的两个span元素。比较首先基于区间的优先级标志，然后是区间的插入顺序。*
	 * @param left 要比较的元素的左索引。
	 * @param right 要比较的其他元素的右索引。
	 * @param priority span优先级
	 * @param insertionOrder span插入顺序
	 * @return 0代表两元素相等，-1代表左元素小于右元素，1代表左元素大于右元素
	 */
    private static final int compareSpans(int left, int right, int[] priority, int[] insertionOrder)
    {
        int priority1 = priority[left];
        int priority2 = priority[right];
        if (priority1 == priority2) {
            return Integer.compare(insertionOrder[left], insertionOrder[right]);
        }
        //因为高优先级必须在低优先级之前，所以要比较的参数与插入顺序检查相反
        return Integer.compare(priority2, priority1);
    }


    /**返回start之后但小于或等于limit的下一个偏移量，其中指定节点的类型和范围*/
    public int nextSpanTransition(int start, int limit, Class kind)
    {
        if (mSpanCount == 0) return limit;
        if (kind == null) {
            kind = Object.class;
        }
        //从根节点开始找
        return nextSpanTransitionRec(start, limit, kind, treeRoot());
    }

    //此函数递归遍历节点i之下的节点并寻找在指定范围内的节点偏移量
    //由于二叉树是用数组表示的，因此对树的遍历类似于递归二分数组
    //我更愿意称nextSpanTransitionRec是二分查找法的升级版，原二分查找法是找数组中指定的值，这个函数就是在指定范围内找数组中的值
    //注意，每个节点包含st和en，虽然mSpanStarts可以这样找，但mSpanEnds是未预料的，因此无论如何仍要遍历所有节点

    //可以理解为它就是将数组分为一个个的二分范围，然后从最大的范围开始，遍历之下的范围(及范围内的节点)
    //由于先遍历左子节点，再遍历右子节点，并且是先分下去，然后返回，遍历顺序实际是按数组顺序进行的
    /*  
	 例如一列数 0，1，2，3，4，5，6
	 若表示为二叉树则是如下的结果:
	           3
	         ↙  ↘
	       1        5
	     ↙  ↘    ↙  ↘
	    0     2   4     6

	 1、找到整个数组中点3，从3开始向左分发
	 2、找到0~3区间内的中点1，从1开始向左分发
	 3、找到0~1区间内的中点0，0被执行！
	 4、从0返回到1，1被执行！
	 5、1继续向右分发到2，2被执行！
	 6、从2返回到1返回到3，3被执行！
	 7、从3开始向右分发，找到3~6区间的中点5，从5开始向左分发
	 8、找到3~5区间的中点4，4被执行！
	 9、从4返回到5，5被执行！
	 10、5继续向右分发到6，6被执行！
	 11、最后从6返回到5返回到3，递归遍历完成
	 */

    //虽然函数相当于顺序遍历节点i之下的所有节点，但会利用已有条件来判断并舍弃遍历某部分的节点，并把st或en在范围内的节点的limit边界记录下来
    //每次遍历一个节点，就返回它的limit边界，此limit边界可以是自己的st或en，但大于start并且不超过上个节点的limit，并且此limit边界会限制之后的节点的limit边界
    //每次limit边界都随着返回可能缩小，最后必然是所有节点在此范围内最小的偏移量

    //从索引为i的节点开始，向下遍历其子节点，找到一个在start~limit之内且离start最近的偏移量，此偏移量可以是某个节点的起始或末尾位置
    private int nextSpanTransitionRec(int start, int limit, Class kind, int i) 
    {
        if ((i & 1) != 0) 
        {
            //若i不是叶子节点，则先遍历左子节点
            int left = leftChild(i);
            if (resolveGap(mSpanMax[left]) > start){
                //左子节点之下的最大区间在start之后，说明左子节点中有至少一个节点的spanEnd>start
                //因此可以继续遍历左子节点，找到一个spanEnd大于start但小于limit的左子节点的最小limit边界
                limit = nextSpanTransitionRec(start, limit, kind, left);
            }
        }
        //所有左子节点遍历完成，现在遍历自己和所有右子节点
        if (i < mSpanCount) 
        {
            //若节点i在有效节点范围内，看看它在不在start~limit之内，是则返回其在start~limit之内的最大的位置，否则返回limit
            int st = resolveGap(mSpanStarts[i]);
            int en = resolveGap(mSpanEnds[i]);
            if (st > start && st < limit && kind.isInstance(mSpans[i]))
                limit = st;
            if (en > start && en < limit && kind.isInstance(mSpans[i]))
                limit = en;
            if (st < limit && (i & 1) != 0) {
                //若节点i的起始位置在limit之前，则可能从i之后找一个小于limit边界的节点，从右子节点开始(因为右子节点的spanStart大于或等于i的spanStart)
				//与遍历左子节点不同，注意这里为什么用节点i的spanStart判断是否需要遍历右子节点呢，因为右子节点的左子节点的spanStart可能小于右子节点的spanStart，但一定大于节点i的spanStart
                limit = nextSpanTransitionRec(start, limit, kind, rightChild(i));
            }
        }
        return limit;
    }

    /** 返回一个新的CharSequence，它包含本对象的字符数组指定范围的字符，包括重叠范围 */
    public CharSequence subSequence(int start, int end) {
        return new SpannableStringBuilderLite(this, start, end);
    }
    /** 将范围内的字符复制到指定数组中，从指定的偏移量开始 */
    public void getChars(int start, int end, char[] dest, int destoff)
    {
        checkRange("getChars", start, end);
        //若范围完全在间隙缓冲区左边或右边，只获取一次，否则分两次获取
        if (end <= mGapStart) {
            System.arraycopy(mText, start, dest, destoff, end - start);
        } else if (start >= mGapStart) {
            System.arraycopy(mText, start + mGapLength, dest, destoff, end - start);
        } else {
            System.arraycopy(mText, start, dest, destoff, mGapStart - start);
            System.arraycopy(mText, mGapStart + mGapLength,
                             dest, destoff + (mGapStart - start),
                             end - mGapStart);
        }
    }
    /** 返回一个包含自己全部文本的字符串 */
    @Override
    public String toString()
    {
        int len = length();
        char[] buf = new char[len];
        getChars(0, len, buf, 0);
        return new String(buf);
    }
    /** 返回包含自身中指定范围的字符的字符串 */
    public String substring(int start, int end)
    {
        char[] buf = new char[end - start];
        getChars(start, end, buf, 0);
        return new String(buf);
    }

    /* 检查范围，并抛出异常 */
    private static String region(int start, int end) {
        return "(" + start + " ... " + end + ")";
    }
    private void checkRange(final String operation, int start, int end) {
        if (end < start) {
            throw new IndexOutOfBoundsException(operation + " " +
                                                region(start, end) + " has end before start");
        }
        int len = length();
        if (start > len || end > len) {
            throw new IndexOutOfBoundsException(operation + " " +
                                                region(start, end) + " ends beyond length " + len);
        }
    }

    /* 设置文本过滤器，它在文本改变前被调用，过滤器返回的文本将真正被插入 */
    public void setFilters(InputFilter[] filters)
    {
        if (filters == null) {
            throw new IllegalArgumentException();
        }
        mFilters = filters;
    }
    public InputFilter[] getFilters() {
        return mFilters;
    }
	
	/* 设置是否自动释放多余内存 */
	public void setAutoReleaseExcessMemory(boolean auto){
		AutoReleaseExcessMemory = auto;
	}
	private void ReleaseExcessMemory()
	{
		//大小应至少超出2倍，防止GrowingArrayUtils重新扩大为2倍，陷入死循环
		//修改文本数组大小时，也要修改缓冲区大小，应该改变span的位置
		if(mGapLength > length()*2){
			resizeFor(length());
		}
		//仅修改span数组大小时，与文本无关
		if(mSpans.length > mSpanCount*3){
			resizeForSpans(mSpanCount);
		}
	}


    //树的基本术语:
    //Tree 树是由节点和边组成的且不存在着任何环的一种数据结构
    //Node 节点，节点是组成树的每一个元素
    //Root 根，树的顶端节点
    //Child 孩子，一个节点直接指向的下一个节点称为该结点的孩子
    //Parent 父亲，若一个节点被指向，那么直接指向它的上个节点被称为它的父亲
    //Siblings 兄弟，具有同一个父亲(Parent)的孩子(Child)之间互称为兄弟(Sibling)
    //Ancestor 祖先，节点的祖先(Ancestor)是从根（Root）到该节点所经分支(Branch)上的所有节点
    //Descendant 子孙，反之，以某节点为根的子树中的任一节点都称为该节点的子孙(Ancestor)
    //Leaf 叶子（终端节点）没有孩子的节点(也就是度为0的节点)称为叶子(Leaf)或终端节点
    //Branch 分支(非终端节点) 至少有一个孩子的节点称为分支(Branch)或非终端节点
    //Degree 度，节点所拥有的子树个数称为节点的度(Degree)
    //Edge 边，一个节点和另一个节点之间的连接被称之为边
    //Path 路径，连接节点和其后代的节点之间的(节点,边)的序列
    //Level 层次，节点的层次(Level)从根(Root)开始定义起，根为第0层，根的孩子为第1层。以此类推，若某节点在第i层，那么其子树的根就在第i+1层。
    //Height of node 节点的高度是该节点和某个叶子之间存在的最长路径上的边的个数
    //Height of tree 树的高度是其根节点的高度
    //Depth of node 节点的 深度 是从树的根节点到该节点的边的个数。（注：树的深度指的是树中节点的最大层次。）
    //Forest 森林是n(>=0)棵互不相交的树的集合

    //若将顺序排列的一组数无限二分，可构成一颗二叉树，而二叉树的根节点必然在数组中间，从根节点分发出来的左子节点和右子节点便是二分的结果
    /*
	 例如一列数 0，1，2，3，4，5，6
	 若表示为二叉树则是如下的结果:
	            3
	          ↙  ↘
	        1        5
	      ↙  ↘    ↙  ↘
	     0     2   4     6
	 */

    //这里将跨度列表视为二叉树的跨度(以及开始和结束偏移量和标志)存储在按开始偏移量排序的线性数组中
    //为了快速搜索，这些数组采用了二分搜索法结构，这种结构是对一棵完美二叉树的有序遍历，这是一种有点不寻常但很有利的方法
    //包含值的节点的索引为0 <= i < n(其中n = mSpanCount)，从而将访问值的逻辑保留为连续数组
    //其他平衡二叉树方法(例如完全二叉树)需要对节点索引进行一些洗牌

    //这个结构的基本性质:
    //整颗树像一个等腰三角形
    //对于一棵高度为m的完美二叉树，树有2^(m+1) - 1个总节点，树根的索引是2^m - 1
    //所有叶子节点的索引都是偶数，所有内部节点的索引都是奇数，因此(i & 1) != 0可判断其是不是叶子节点
    //索引i的一个节点的高度是i的二进制表示中尾部的连续的1的个数
    //高度为h的节点i的左子节点是i - 2^(h - 1)
    //高度为h的节点i的右子节点是i + 2^(h - 1)
    //任意节点的所有左子节点(及其索引)都小于它，所有右子节点都大于它

    //获取数组中间数，此下标是二叉树根节点的下标
    private int treeRoot() {
        //使用mSpanCount而不是arr.len，目的是让根节点保持在有效范围的中间位置
        return Integer.highestOneBit(mSpanCount) - 1;
    }
    //获取下标为i的节点的左子节点在数组中的下标
    private static int leftChild(int i) {
        // (i+1) & ~i 等同于 2^(i中尾随1的个数)，即2^h
        // 则(((i + 1) & ~i) >> 1) 等同于 (2^h)/2 = 2^(h-1)
        return i - (((i + 1) & ~i) >> 1);
    }
    //获取下标为i的节点的右子节点在数组中的下标
    private static int rightChild(int i) {
        return i + (((i + 1) & ~i) >> 1);
    }

    //span数组还增加了mSpanMax[]数组，该数组表示上述二叉树结构上的区间树
    //对于每个节点，mSpanMax[]数组包含该节点及其后代的mSpanEnds的最大值
    //因此，遍历可以轻松地拒绝不包含与感兴趣区域重叠的跨度的子树
    //请注意，mSpanMax[]对于索引 >=n 的内部节点也有有效值，但这些节点有索引 <n 的后代
    //在这些情况下，它只表示其后代的最大跨度端，这是完美二叉树结构的结果，你也可以称它为二分区间树

	//这个完美二叉树的遍历也是绝了
	//对于左子节点，使用mSpanMax数组判断，因为它可能超出mSpanCount(也就是大于mSpanCount的内部节点的左子节点)
	//对于右子节点，使用mSpanStarts数组判断，由于mSpanStarts长度不够，因此只要它超出mSpanCount，就不再遍历了(也没有必要)
	//这样做的结果是，将只遍历mSpanCount内的节点，超出范围的节点只是作为一个搭手，用于遍历还在范围内的左子节点
	//这也是为什么左子节点不用判断是否超出mSpanCount的原因，因为它必须这样才能遍历完成
	/*
	 例如一列数 0，1，2，3，4，5，6
	 若表示为二叉树则是如下的结果:
	         3
           ↙  ↘
	     1        5
	   ↙  ↘    ↙  ↘
	  0     2   4     6
	  
	  又例如，mSpanCount=5，所以，下标为5的节点只是作为一个搭手，用于遍历下标为4的节点
	  由于其本身已经等于mSpanCount，所以它的右子节点更不可能在范围内了，因此下标为5和6的节点都不需要遍历
	*/
	
    //注意，此函数总是递归更新节点i及其之下的所有子节点的mSpanMax值
    //注意，对于任意n，此树的内部节点可能 >= n，因此，节点i的递归遍历的一般结构是:
    //若i不是叶子节点，则计算所有左侧子节点的最大值
    //若i在有效范围内，则计算自己的最大值
    //若i在有效范围内且有右子节点，则计算所有右侧子节点的最大值，右侧子节点的下标大于i
    private int calcMax(int i)
    {
        int max = 0;
        if ((i & 1) != 0) {
            //若i不是叶子节点，则计算左子节点的最大值
            max = calcMax(leftChild(i));
        }
        if (i < mSpanCount) 
        {
            //若i在有效的节点范围内，则计算自己的最大值
			max = max>=mSpanEnds[i] ? max:mSpanEnds[i];
            if ((i & 1) != 0){
                //若i不是叶子节点，则计算右子节点的最大值，右侧子节点的下标必然大于i，因此i若不在有效范围内可以不计算
				//但是，若i刚好是临近mSpanCount的下标(如mSpanCount-1)，则其右子节点下标将超出mSpanCount
				//为什么不制止遍历呢，因为对于索引 >=n 的内部节点有索引 <n 的后代，这意味着，此右子节点虽无效，但它有在有效范围内的左子节点
				//因此，我们只是借右子节点之手，遍历还处于范围内的左子节点，由于右子节点本身>n，因此它自己和自己的右子节点不会遍历
				//但是，在最后它仍会设置自己的最大值，为了防止下标超出，所以mSpanMax数组的大小应大于mSpanCount，并且至少包含临近mSpanCount的最小右子节点的位置
				//根据完美二叉树的特性，根节点在treeRoot处，因此treeRoot应该二分了整个mSpanMax数组，所以mSpanMax的最小大小为(2*treeRoot)+1
				int maxRight = calcMax(rightChild(i));
                max = max>=maxRight ? max:maxRight;
            }
        }
        //设置自己的最大值，并返回
        mSpanMax[i] = max;
        return max;
    }
	
	//mIndexOfSpan映射的索引修正
	private void correctIndexOfSpan()
	{
		if (mIndexOfSpan == null) {
            mIndexOfSpan = new IdentityHashMap<Object, Integer>();
        }
        //从被修改的下标开始，遍历之后的span，将span与正确的index重新绑定
        for (int i = mLowWaterMark; i < mSpanCount; i++) 
        {
            //每次从mIndexOfSpan拿出当前span的index，若没有此span或index是错误的，重新放入正确的span和index
            Integer existing = mIndexOfSpan.get(mSpans[i]);
            if (existing == null || existing != i) {
                mIndexOfSpan.put(mSpans[i], i);
            }
        }
        //修改完后mLowWaterMark置为无限大，意为所有的span都刷新了
        mLowWaterMark = Integer.MAX_VALUE;
	}
	
	//节点按spanStarts的大小正序排列，小量节点修改后使用
	private void sortNodes(int i)
	{
        //这是一个简单的插入排序，因为我们希望它大部分已被排序
		//为什么使用插入排序呢，因为已经排序的部分不会打乱，可以节省时间
        //每次向后从数组中拿出一个元素，并与其之前的元素比较，直到找到一个正确的位置，将其插入这里，这样每次排序之后，在i之前的内容都是排好序的    
		for (i=i<1 ? 1:i; i < mSpanCount; i++) 
        {
            //如果当前元素比前面的元素更小，主动进行本次排序
            //注意，i之前的元素必然按顺序排列，因此只用与i-1比较就知道需不需要排序
            if (mSpanStarts[i] < mSpanStarts[i - 1])
            {
                Object span = mSpans[i];
                int start = mSpanStarts[i];
                int end = mSpanEnds[i];
                int flags = mSpanFlags[i];
                int insertionOrder = mSpanOrder[i];
                int j = i;

                //将j之前的元素与其比较，直到找到一个比它还小的元素才停止，并将其插入到这里
                do {
                    //由于此span必然在j之前，所以最后需要将其插入到正确位置，并还需要之前的元素顺序不变
                    //因此只要还没有找到位置，那么j-1位置的元素就要向后挪一位，空出位置
                    mSpans[j] = mSpans[j - 1];
                    mSpanStarts[j] = mSpanStarts[j - 1];
                    mSpanEnds[j] = mSpanEnds[j - 1];
                    mSpanFlags[j] = mSpanFlags[j - 1];
                    mSpanOrder[j] = mSpanOrder[j - 1];
                    j--;
                    //之后判断j-2位置的元素是否小于start，如果是就break
                } while (j > 0 && start < mSpanStarts[j - 1]);

                //最后将span插入这里(也就是j-1的位置)
                mSpans[j] = span;
                mSpanStarts[j] = start;
                mSpanEnds[j] = end;
                mSpanFlags[j] = flags;
                mSpanOrder[j] = insertionOrder;
                invalidateIndex(j);
            }
        }
	}
	
	//节点按spanStarts的大小正序排列，当大量节点乱序时使用
	private void quickSortNodes()
	{
		//这是一个简单的快速排序，我们认为它大部分没有排序
		//为什么使用快速排序呢，因为已经排序的部分不会打乱，可以节省时间
		unckSort(0,mSpanCount-1);
		//大量的修改后，直接从0开始吧。
		invalidateIndex(0);
	}
	
	//递归二分数组
	private void unckSort(int low, int high)
	{
		if (low < high){
			int middle = getMiddle(low, high);    //将数组一分为二
			unckSort(low, middle - 1);    // 对左边进行递归排序
			unckSort(middle + 1, high);    // 对右边进行递归排序
		}
		//继续递归分裂，直至每个小数组只有两个元素
		//则只有low<high，才能继续
		//当一个数组只有两个元素，则此时排序，只是比较两个元素大小
		//因为整个数组都是按序分的
		//所以每个小数组排好序，则大数组也排好了
	}
	
	//找到第一个元素所在小数组的中点，将它移到中点位置，并将小于和大于它的元素分别移至它的左边和右边
	private int getMiddle(int low, int high)
	{
		//数组的第一个值作为中点（分界点或关键数据）
		Object span = mSpans[low];
		int mid = mSpanStarts[low]; 
		int end = mSpanEnds[low];
		int flags = mSpanFlags[low];
		int order = mSpanOrder[low];
		
		while (low < high)
		{
			while (low < high && mSpanStarts[high] >= mid){
				high--;
			}
			//从右边开始找一个小于中点的数，挪至左边
			//将其移动到list[low],此时list[low]==list[high]
			mSpans[low] = mSpans[high];
			mSpanStarts[low] = mSpanStarts[high];
			mSpanEnds[low] = mSpanEnds[high];
			mSpanFlags[low] = mSpanFlags[high];
			mSpanOrder[low] = mSpanOrder[high];
			
			while (low < high && mSpanStarts[low] <= mid){
				low++;
			}
			//从左边开始找一个大于中点的数，将这个大于中点的数挪至右边
			//可以挪到list[high]，因为list[high]已经挪到左边了
			mSpans[high] = mSpans[low];
			mSpanStarts[high] = mSpanStarts[low];
			mSpanEnds[high] = mSpanEnds[low];
			mSpanFlags[high] = mSpanFlags[low];
			mSpanOrder[high] = mSpanOrder[low];
			
			//一直交换下去，直至low和high相遇，
			//low说：我左边都比中点小
			//high说：我右边都比中点大
		}
		
		//所以可以把中点插入这里,这样中点就移动到中间了
		mSpans[low] = span;
		mSpanStarts[low] = mid;
		mSpanEnds[low] = end;
		mSpanFlags[low] = flags;
		mSpanOrder[low] = order;
		return low; // 返回中点的位置
	}

    //在跨度结构的任何突变后恢复二元区间树不变量(修改的内容越少恢复会越快)
    private void restoreInvariants(int i) 
    {
        if (mSpanCount == 0) return;

		//不变量1: span starts按顺序排列
		//这是一个简单的插入排序，因为我们希望它大部分已被排序
        sortNodes(i);
		
        //不变量2: 使max是每个节点及其后代的最大跨度端点
        //从根节点开始，修正所有子节点的max值
        calcMax(treeRoot());

        //不变量3: mIndexOfSpan映射的索引修正
        correctIndexOfSpan();
    }
	
	//在跨度结构的任何突变后恢复二元区间树不变量(修改内容太多时使用)
	private void quickRestoreInvariants() 
    {
        if (mSpanCount == 0) return;

		//不变量1: span starts按顺序排列
		//这是一个简单的快速排序，我们认为它大部分没有排序
        quickSortNodes();

        //不变量2: 使max是每个节点及其后代的最大跨度端点
        //从根节点开始，修正所有子节点的max值
        calcMax(treeRoot());

        //不变量3: mIndexOfSpan映射的索引修正
        correctIndexOfSpan();
    }
	
    //对mSpans的任何更新调用此函数，以便mIndexOfSpan可以被更新
    private void invalidateIndex(int i) {
        //更新mLowWaterMark的值，表示此之前的span没有刷新
        mLowWaterMark = i<=mLowWaterMark ? i:mLowWaterMark;
    }

    private static final InputFilter[] NO_FILTERS = new InputFilter[0];
    private static final int[][] sCachedIntBuffer = new int[6][0]; //存放和回收用于排序的数组
    private InputFilter[] mFilters = NO_FILTERS; //过滤器列表

    private char[] mText; //文本数组
    private int mGapStart; //数组中空闲间隙的起始位置
    private int mGapLength; //空闲间隙的长度

    //用数组表示二叉树，所有数组中相同下标的内容代表同一节点的数据
    //mSpanStarts是最重要的，其总是正序排列，而节点之下的最大区间是用mSpanMax表示的
    //虽然如此，但遍历时仍只管mSpanCount之前的内容，所有的数组中，实际也只有前mSpanCount个元素有效，之后的空间预留用于添加新元素
    //另外二叉树的顺序是从mSpanCount最高位的值这个下标开始二分得到的，因此二叉树可能大于mSpanCount
	
    private Object[] mSpans; //存储每个节点所代表的span
    private int[] mSpanStarts; //存储每个节点在文本中的起始位置
    private int[] mSpanEnds; //存储每个节点在文本中的末尾位置
    private int[] mSpanMax;  //存储此节点及其子节点最大的范围(也就是最大的mSpanEnd)
    private int[] mSpanFlags; //存储每个节点的flags
    private int[] mSpanOrder;  //存储节点插入的顺序

    private int mSpanInsertCount;  //节点插入计数器
    private int mSpanCount;  //节点个数
    private IdentityHashMap<Object, Integer> mIndexOfSpan; //存储节点在数组中的下标
    private int mLowWaterMark;  //在此之前的索引没有被触及
	private boolean AutoReleaseExcessMemory;  //是否自动释放多余内存

    //这些值与Spanned中的公共SPAN_MARK/POINT值紧密相关
    private static final int MARK = 1;
    private static final int POINT = 2;
    private static final int START_MASK = 0xF0;
    private static final int END_MASK = 0x0F;
    private static final int START_SHIFT = 4;

}
