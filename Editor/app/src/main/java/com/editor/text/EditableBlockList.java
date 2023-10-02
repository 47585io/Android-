package com.editor.text;

import android.text.*;
import java.util.*;
import java.lang.reflect.*;
import com.editor.text.base.*;
import android.util.*;


/* 将大的数据分块/分区是一个很棒的思想
   它使得对于数据的处理仅存在于小的区域中，而不必修改所有数据，只要限制每个分块的大小，便可以使效率均衡
   此类是分块文本容器的实现类
   
   已解决bug1: span绑定的文本块不按顺序排列，并且mBlockStarts未刷新
   未刷新的mIndexOfBlocks，导致block下标错误，应该在replaceSpan之前刷新
   遗漏在insertForBlocks末尾的refreshInvariants，必须刷新
   
   已解决bug2: span的范围不连续
   目前还不知道在插入后怎样获取两端的span并修正
   应该是每次截取时获取两端的span，参见replaceWithSpan，correctSpan
   
   已解决bug3: span重复时范围错误
   SpannableStringBuilder在插入文本中包含重复span时不会扩展其范围，导致该span仍处于上次的位置
   应该在插入时额外修正，即在插入前判断是否已有，如果是则应在插入后修正，分发时则不需要管(全都是新文本块)
   参见insertForBlocks，checkRepeatSpans，correctRepeatSpans
   
   已解决bug4: span插入顺序错误
   在上个文本块末尾的span，会优先截取到下个文本块末尾，因此span的优先级会反了，所以我们需要对span重新排序
   在replaceSpan中，会给新的span(也就是不在mSpanInBlocks中的span)设置一个插入顺序，
   但是此span可能刚才正处于上一文本块，但已移除并将要放入新文本块之中，此时该span的插入顺序不变
   另外注意到setSpan也有此bug，对于重复的span，不要改变插入顺序
   
   已解决bug5: 对于不同的文本块，可能有不同的决策
   EditableList在修改文本时，进行添加，移除，扩展span应先征得文本块的意见，需要与文本块保持同步
   借助接口的方法来进行判断，参见replaceSpan，expandSpans
   
   已解决bug6: 插入文本时，当插入点位于文本块起始或末尾，端点位于上个文本块末尾和下个文本块起始的span应扩展
   在插入前，先获取即将挤到两边的span，插入后，如果需要扩展，再将端点扩展包含文本
   参见getAtPointSpans，expandSpans
   
   已解决bug7: BlockListener的方法调用时并没有刷新，可能会有bug
   (length未刷新，在末尾删除时，测量下标超出界限)，应该在适当时机同步length
   
   *此类主要围绕两大内容: 文本和span如何正确分配到文本块中并与其建立绑定，文本块顺序刷新和事件发送
*/
public class EditableBlockList extends Object implements EditableBlock
{

	private int mLength;
	private int mBlockSize;
	private int mInsertionOrder;
	private int mSelectionStart, mSelectionEnd;
	
	private EditableBlock[] mBlocks; 
	private int[] mBlockStarts;
	private Map<EditableBlock,Integer> mIndexOfBlocks;
	private Map<Object,List<EditableBlock>> mSpanInBlocks;
	private Map<Object,Integer> mSpanOrders;

	private BlockFactory mEditableFactory;
	private TextWatcher mTextWatcher;
	private SelectionWatcher mSelectionWatcher;
	private BlockListener mBlockListener;
	
	private int MaxCount;
	private int ReserveCount;
	private int mTextWatcherDepth;
	private boolean AutoReleaseExcessMemory;
	private InputFilter[] mFilters = NO_FILTERS;
	
	private static final int Default_MaxCount = 1280;
	private static final int Default_ReserveCount = Default_MaxCount*2/10;
	private static final InputFilter[] NO_FILTERS = new InputFilter[0];
	
	
	public EditableBlockList(){
		this("");
	}
	public EditableBlockList(CharSequence text){
		this(text,0,text.length());
	}
	public EditableBlockList(CharSequence text, int start, int end){
		this(text,start,end,Default_MaxCount,Default_ReserveCount);
	}
	public EditableBlockList(CharSequence text, int start, int end, int count, int reserveCount)
	{
		MaxCount = count<10 ? Default_MaxCount:count;
		ReserveCount = reserveCount>=count ? count*2/10:reserveCount;
		mBlocks = EmptyArray.emptyArray(EditableBlock.class);
		mBlockStarts = EmptyArray.INT;
		mIndexOfBlocks = new IdentityHashMap<>();
		mSpanInBlocks = new IdentityHashMap<>();
		mSpanOrders = new IdentityHashMap<>();
		if(end-start==0){
			//仅仅只是在没有任何span时创建空文本块，待之后插入
			//而插入文本时，不应创建多余的空文本块
			//删除文本时，应移除多余的空文本块
			addBlock(0,true);
		}else{
			dispatchTextBlock(0,text,start,end,true);
		}
	}
	
	public void setEditableFactory(BlockFactory fa){
		mEditableFactory = fa;
	}
	public void setTextWatcher(TextWatcher wa){
		mTextWatcher = wa;
	}
	public void setSelectionWatcher(SelectionWatcher wa){
		mSelectionWatcher = wa;
	}
	public void setBlockListener(BlockListener li){
		mBlockListener = li;
	}
	
	/* 在指定位置添加文本块，若send为false，则刷新mIndexOfBlocks是调用者的责任 */
	private void addBlock(int i, boolean send)
	{
		EditableBlock block = mEditableFactory==null ? new SpannableStringBuilderLite() : mEditableFactory.newEditable("");
		block.setAutoReleaseExcessMemory(AutoReleaseExcessMemory);
		mBlocks = GrowingArrayUtils.insert(mBlocks,mBlockSize,i,block);
		mBlockStarts = GrowingArrayUtils.insert(mBlockStarts,mBlockSize,i,0);
		mIndexOfBlocks.put(block,i);
		mBlockSize++;
		if(send){
			refreshInvariants(i);
			sendBlockAdded(i);
		}
	}
	/* 移除指定位置的文本块，若send为false，则刷新mIndexOfBlocks是调用者的责任 */
	private void removeBlock(int i, boolean send)
	{
		Editable block = mBlocks[i];
		replaceSpan(i,0,block.length(),"",0,0,true);
		mBlocks = GrowingArrayUtils.remove(mBlocks,mBlockSize,i);
		mBlockStarts = GrowingArrayUtils.remove(mBlockStarts,mBlockSize,i);
		mIndexOfBlocks.remove(block);
		mBlockSize--;
		mLength -= block.length();
		if(send){
			refreshInvariants(i);
			sendBlockRemoved(i);
		}
		if(mBlockSize==0){
			//在没有文本块时，必须重新添加以复活
			addBlock(0,true);
		}
		if(AutoReleaseExcessMemory){
			//释放多余空间
			ReleaseExcessMemory();
		}
	}
	/* 从指定id的文本块开始，分发text中指定范围内的文本 */
	private int dispatchTextBlock(final int id, CharSequence tb, int tbStart, int tbEnd, boolean send)
	{
		if(tbEnd-tbStart==0){
			//不会添加空文本块，因为之后在空文本块中手动插入文本时，无法修正之前之后的span
			return id;
		}
		
		//计算并添加文本块，文本块会多预留一些空间
		final int MaxCount = this.MaxCount-ReserveCount;
		final int len = tbEnd-tbStart;
		final int count = len%MaxCount==0 ? len/MaxCount:len/MaxCount+1;

		int i = id;
		int j = id+count;
		for(;i<j;++i){
			addBlock(i,false);
		}
		//必须刷新mIndexOfBlocks，注意从id开始
		refreshInvariants(id);

		//计算并填充文本，插入文本仅需mIndexOfBlocks正确，因此可以连续插入
		for(i=id;i<j;++i)
		{
			if(tbEnd-tbStart<=MaxCount){
				repalceWithSpan(i,0,0,tb,tbStart,tbEnd,false,true);
				break;
			}
			repalceWithSpan(i,0,0,tb,tbStart,tbStart+MaxCount,false,true);
			tbStart+=MaxCount;
		}

		//在修正mBlockStarts后，发送事件
		if(send)
		{
			refreshInvariants(id);
			for(i=id;i<j;++i){
				sendBlockAdded(i);
			}
			sendBlocksInsertAfter(id,j-1,0,mBlocks[j-1].length());
		}
		return j-1;
	}
	
	public Editable getBlock(int i){
		return mBlocks[i];
	}
	public int getBlockSize(){
		return mBlockSize;
	}
	public int getBlockStartIndex(int id){
		return mBlockStarts[id];
	}
	/* 寻找指定下标所在的文本块 */
	public int findBlockIdForIndex(int index)
	{
		//找到临近的位置，然后从此处开始
		int id = index/(MaxCount-ReserveCount);
		if(id>mBlockSize-1){
			id = mBlockSize-1;
		}
		if(id<0){
			id=0;
		}
		int nowIndex = mBlockStarts[id];

		//为了方便插入，我们总是尽量将index保持在上一文本块的末尾
		if(nowIndex<index){
			for(;id<mBlockSize-1 && mBlockStarts[id+1]<index;++id){}
		}
		else if(nowIndex>index){
			for(;id>0 && mBlockStarts[id]>index;--id){}
		}
		else if(nowIndex==index){
			id = id==0 ? 0:id-1;
		}
		return id;
	}
	
	@Override
	public Editable replace(int p1, int p2, CharSequence p3){
		return replace(p1,p2,p3,0,p3.length());
	}
	@Override
	public Editable insert(int p1, CharSequence p2, int p3, int p4){
		return replace(p1,p1,p2,p3,p4);
	}
	@Override
	public Editable insert(int p1, CharSequence p2){
		return replace(p1,p1,p2,0,p2.length());
	}
	@Override
	public Editable delete(int p1, int p2){
		return replace(p1,p2,"",0,0);
	}
	@Override
	public Editable append(CharSequence p1){
		int len = length();
		return replace(len,len,p1,0,p1.length());
	}
	@Override
	public Editable append(CharSequence p1, int p2, int p3){
		int len = length();
		return replace(len,len,p1,p2,p3);
	}
	@Override
	public Editable append(char p1){
		int len = length();
		return replace(len,len,String.valueOf(p1),0,1);
	}
	
	/* 先删除start~end之间的文本和文本块，并从start指定的文本块开始插入文本(插入文本中的重复的span会被移除)
	   若插入点刚好是某些span的端点，并且这些span没有被删除并且需要扩展，应该将它们扩展包含插入的文本
	*/
	@Override
	public Editable replace(int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		//过滤文本
        for (int i=0;i<mFilters.length;++i) {
            CharSequence repl = mFilters[i].filter(tb, tbStart, tbEnd, this, start, end);
            if (repl!=null) {
                tb = repl;
                tbStart = 0;
                tbEnd = repl.length();
            }
        }
		
		//文本变化前，调用文本监视器的方法
		final int st = start;
		final int before = end-start;
		final int after = tbEnd-tbStart;
		sendBeforeTextChanged(start,before,after);
		
		//找到start和end所指定的文本块，并将它们偏移到文本块的下标
		final int i = findBlockIdForIndex(start);
		final int j = findBlockIdForIndex(end);
		start-=mBlockStarts[i];
		end-=mBlockStarts[j];
		
		if(before>0){
			//删除范围内的文本和文本块
		    deleteForBlocks(i,j,start,end,true);
		}
		if(after>0)
		{
			Object[] spans = EmptyArray.OBJECT;
			if(start==0 || start==mBlocks[i].length()){
				//需要在插入前获取端点正好在插入两端的span，但前提是它们也刚好在文本块两端并且不扩展
				spans = getAtPointSpans(st);
			}
			//插入前还要移除文本中与自身重复的span
			tb = removeRepeatSpans(tb,tbStart,tbEnd);
			//删除后，末尾下标已不可预测，但起始下标仍可用于插入文本
			insertForBlocks(i,start,tb,tbStart,tbEnd);
			//插入后，扩展端点正好处于插入两端的span
			expandSpans(spans,st,st+after);
		}
		
		//最后计算光标位置，并调用文本和文本块和光标监视器的方法
		sendAfterBlocksChanged(i,start);
		sendTextChanged(st,before,after);
		setSelection(st+after,st+after);
		sendAfterTextChanged();
		return this;
	}
	
	/* 从指定文本块的指定位置插入文本，超出MaxCount之后的内容被截取到之后的文本块 
	   插入文本其实很简单，按下面步骤进行，注意必须是此顺序，因为我们要发送文本块事件
	   
	   ①首先我们将文本先插入指定文本块index的位置，处于文本块内的span会自己扩展和移动，但处于两端的不会
	   换句话说，处于文本块末尾的span无法扩展，处于文本块开头的span会挤到后面
	   因此若插入端点正好是处于文本块某一端，需要修正在端点处的span与上个下个文本块的衔接(如果有)，见correctSpan
	   
	   ②然后我们把超出文本块的文本截取出来，由于我们刚才已经修正了span，所以截取的文本也是正确的
	   
	   ③最后把截取的文本拷贝到之后的文本块中，只要保持截取文本及span的位置相对不变即可，整段文本看起来好像也是对的
	   如果拷贝到的文本块都是新的文本块，可想而知，span位置看起来也都是不变的
	   如果拷贝到的文本块是旧的文本块，就会有一个问题: 截取的文本与旧文本块中可能有重复的span，而重复的span是不会设置的
	   因此我们应该检查重复的span，并将它们在文本块中的位置偏移到相对于文本的位置，见checkRepeatSpans，correctRepeatSpans
	   另外的，由于旧的文本块中仍然有span可能与上个文本块衔接，并且此span必然重复，因此还要修正它，见correctSpan
	*/
	private void insertForBlocks(final int i, final int index, CharSequence tb, int tbStart, int tbEnd)
	{
		//先插入文本，让在此范围内的span进行扩展和修正
		repalceWithSpan(i,index,index,tb,tbStart,tbEnd,true,true);
		
		//再检查文本块的内容是否超出MaxCount
		final int srcLen = mBlocks[i].length();	
		if(srcLen > this.MaxCount)
		{								   
			//将超出的文本截取出来，需要多预留一些空间以待之后使用
			final int MaxCount = this.MaxCount-ReserveCount;
			final CharSequence text = mBlocks[i].subSequence(MaxCount,srcLen);
			final int overLen = srcLen-MaxCount;
			deleteForBlocks(i,i,MaxCount,srcLen,false);
			
			//如果超出的文本小于或等于MaxCount
			if(overLen <= this.MaxCount)
			{
				if (mBlockSize-1 == i || mBlocks[i+1].length()+overLen > this.MaxCount){
					//若无下个文本块，则添加一个
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个(对于文本块的变化则必须刷新)
					addBlock(i+1,true);
				}
				//插入前需要获取重复的span，插入后再次修正范围(针对完全被截取至下个文本块的span)
				Object[] spans = EmptyArray.OBJECT;
				if(mBlocks[i+1].length()>0){
					//新文本块不用修正span，也不用修正重复span
					spans = checkRepeatSpans(mBlocks[i+1],text,0,text.length());
				}
				//之后将超出的文本插入下个文本块开头，最后刷新数据(仅需从i+1开始)
				repalceWithSpan(i+1,0,0,text,0,overLen,true,true);
				correctRepeatSpans(mBlocks[i+1],text,spans,0);
			}
			else{
				//如果超出的文本大于MaxCount，必须分发，分发时不需要修正span，也不需要修正重复span
				//就像是将修正好的文本单独拷贝到新的文本块中，由于没有重复的span，span整体位置保持不变
				dispatchTextBlock(i+1,text,0,overLen,true);
			}
		}
	}
	/* 另一个解决办法是，倒着进行，即先插入，再删除 */
	private void insertForBlocksReverse(final int i, final int index, CharSequence tb, int tbStart, int tbEnd)
	{
		//先插入文本，让在此范围内的span进行扩展和修正
		repalceWithSpan(i,index,index,tb,tbStart,tbEnd,true,true);
		
		//再检查文本块的内容是否超出MaxCount
		final EditableBlock dstBlock = mBlocks[i];
		final int srcLen = dstBlock.length();	
		if(srcLen > this.MaxCount)
		{								   
			//将超出的文本截取出来，需要多预留一些空间以待之后使用
			final int MaxCount = this.MaxCount-ReserveCount;
			final int overLen = srcLen-MaxCount;
			
			//如果超出的文本小于或等于MaxCount
			if(overLen <= this.MaxCount)
			{
				if (mBlockSize-1 == i || mBlocks[i+1].length()+overLen > this.MaxCount){
					//若无下个文本块，则添加一个
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个(对于文本块的变化则必须刷新)
					addBlock(i+1,true);
				}
				//插入前需要获取重复的span，插入后再次修正范围(针对完全被截取至下个文本块的span)
				Object[] spans = EmptyArray.OBJECT;
				if(mBlocks[i+1].length()>0){
					//新文本块不用修正span，也不用修正重复span
					spans = checkRepeatSpans(mBlocks[i+1],dstBlock,MaxCount,srcLen);
				}
				//之后将超出的文本插入下个文本块开头，最后刷新数据(仅需从i+1开始)
				repalceWithSpan(i+1,0,0,dstBlock,MaxCount,srcLen,true,true);
				correctRepeatSpans(mBlocks[i+1],dstBlock,spans,MaxCount);
			}
			else{
				//如果超出的文本大于MaxCount，必须分发，分发时不需要修正span，也不需要修正重复span
				//就像是将修正好的文本单独拷贝到新的文本块中，由于没有重复的span，span整体位置保持不变
				dispatchTextBlock(i+1,dstBlock,MaxCount,srcLen,true);
			}
			//最后删除，意义一致，span在下个文本块中正常扩展，行数测量正确
			//宽度测量在紧邻的两个文本块中无论如何都会错一个，之前是前面错，现在是后面错，但只要有一个正确能保证maxWidth正确即可
			//另一个好处是，该方案可以保证span在删除时不被完全移除，spanIsRemoved可以不要了
			deleteForBlocks(i,i,MaxCount,srcLen,false);
		}
	}
	
	/* 删除指定范围内的文本和文本块，空文本块被移除 */
	private void deleteForBlocks(int i, int j, int start, int end, boolean spanIsRemoved)
	{
		if(i==j)
		{
			//只要删除一个文本块中的内容
			sendBlocksDeleteBefore(i,i,start,end);
			if(start==0 && end==mBlocks[i].length()){
				//如果文本块被移除，最后起始块下标i无效
				removeBlock(i,true);
				i = -1;
			}else{		
				repalceWithSpan(i,start,end,"",0,0,false,spanIsRemoved);
				refreshInvariants(i);
			}
			sendBlocksDeleteAfter(i,i,start,start);
		}
		else
		{
			//要删除多个文本块的内容
			//删除文本或文本块时，mIndexOfBlocks和mBlockStarts均可不正确
			//因此可以连续删除，仅需在最后刷新数据
			sendBlocksDeleteBefore(i,j,start,end);
			int startBlockIndex = i, endBlockIndex = i+1;
			int removedBlockStartIndex = i, removedBlockEndIndex = j;
			
			//删除起始块的内容
			if(start==0){
				//如果起始块移除，则i,j向前移1步，并且最后起始块下标无效，最后末尾块下标减1
				removeBlock(i,false);
				--i;
				--j;
				startBlockIndex = -1;
				endBlockIndex--;
			}else{
				//否则就删除范围内的文本，但移除文本块起始下标加1
			    repalceWithSpan(i,start,mBlocks[i].length(),"",0,0,false,spanIsRemoved);
				removedBlockStartIndex++;
			}
			
			for(++i;i<j;--j){
				//中间的块必然不会进行测量，而是全部删除
				removeBlock(i,false);
			}
			
			//删除末尾块的内容
			if(end==mBlocks[i].length()){
				//如果末尾块移除，最后末尾块下标无效
				removeBlock(i,false);
				endBlockIndex = -1;
			}else{
				//否则就删除范围内的文本，但移除文本块起始下标减1
			    repalceWithSpan(i,0,end,"",0,0,false,spanIsRemoved);
				removedBlockEndIndex--;
			}
			
			//最后刷新数据，并发送事件
			//刷新的起始下标i永远指向末尾块的下标(仅需从末尾块开始刷新)
			refreshInvariants(i);
			for(;removedBlockStartIndex<=removedBlockEndIndex;--removedBlockEndIndex){
				sendBlockRemoved(removedBlockEndIndex);
			}
			sendBlocksDeleteAfter(startBlockIndex,endBlockIndex,start,0);
		}
	}
	
	/* 替换指定文本块的文本及span与其的绑定，若send为false，则刷新mBlockStarts是调用者的责任 */
	private void repalceWithSpan(final int i, final int start, final int end, CharSequence tb, int tbStart, int tbEnd, boolean send, boolean spanIsRemoved)
	{
		final int before = end-start;
		final int after = tbEnd-tbStart;
		final EditableBlock dstBlock = mBlocks[i];
		if(send && before>0){
			sendBlocksDeleteBefore(i,i,start,end);
		}
		
		//需要在插入文本前，获取端点处无法扩展的span或会挤到后面的span
		Object[] spans = EmptyArray.OBJECT;
		if(after>0 && (start==0 || start==dstBlock.length()))
		{
			//手动插入文本时，处于文本块内的span会自己扩展和移动，但处于两端的不会，因此修正它们之前之后的衔接
			//截取的文本内包含的span，在下个文本块中可能重复，导致无法设置，因此修正它们之前的衔接
			//新添加的空文本块，仅用于拷贝截取的文本，不会获取任何span，也不用修正
			spans = dstBlock.quickGetSpans(start,start,Object.class);
		}
		
		//需要在删除文本前，替换span的绑定。删除文本不需要修正span
		replaceSpan(i,start,end,tb,tbStart,tbEnd,spanIsRemoved);
		dstBlock.replace(start,end,tb,tbStart,tbEnd);
		mLength += -before+after;
		//需要在插入后，修正端点处的span
		if(after>0){
			correctSpans(dstBlock,spans);
		}
		
		//需要在发送事件前刷新数据
		if(send){
			refreshInvariants(i);
			if(before>0){
				sendBlocksDeleteAfter(i,i,start,end);
			}
			if(after>0){
				sendBlocksInsertAfter(i,i,start,start+after);
			}
		}
	}

	/* 替换指定文本块的span与其的绑定，spanIsRemoved为true表示默认行为，只有在insertForBlocks中才传false */
	private void replaceSpan(final int i, final int start, final int end, CharSequence tb, int tbStart, int tbEnd, boolean spanIsRemoved)
	{
		//先移除指定文本块start~end范围内的span与block的绑定
		//纯删除时，mIndexOfBlocks和mBlockStarts均可不正确
		final EditableBlock dstBlock = mBlocks[i];
		if(end > start)
		{
			final boolean textIsRemoved = (tbEnd-tbStart)==0;
			final boolean blockIsRemoved = start==0 && end==dstBlock.length();
			//移除时不需要保证顺序，直接快速获取乱序的spans
			Object[] spans = dstBlock.quickGetSpans(start,end,Object.class);
			
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				//如果span可以被移除或文本块会被移除，则可以与文本块解除绑定
				if(blockIsRemoved || dstBlock.canRemoveSpan(span,start,end,textIsRemoved))
				{
					List<EditableBlock> blocks = mSpanInBlocks.get(span);		
					if(blocks!=null)
					{
						if(blocks.size()==1 && spanIsRemoved){
							//如果span只在这一个文本块中，可以直接移除span
							//另一个情况是，本次截取是为了接下来的插入，此时该span仍保留
							mSpanInBlocks.remove(span);
							mSpanOrders.remove(span);
							recyleList(blocks);	
						}else{
							blocks.remove(dstBlock);
						}
					}	
				}
			}
		}
		
		//如果要替换的文本是Spanned，范围内的span需要与block建立新的绑定
		//插入时，mIndexOfBlocks必须是正确的
		if(tbEnd>tbStart && tb instanceof Spanned)
		{
			Spanned spanString = (Spanned) tb;
			//添加时需要保证顺序，使这些span也像是在spanString中一样
			Object[] spans = spanString.getSpans(tbStart,tbEnd,Object.class);
			
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				if(dstBlock.isInvalidSpan(span,spanString.getSpanStart(span),spanString.getSpanEnd(span),spanString.getSpanFlags(span))){
					//忽略无效跨度
					continue;
				}
				List<EditableBlock> blocks = mSpanInBlocks.get(span);
				if(blocks==null){
					//一个全新的span，需要映射到一个新的列表，并加入block
					blocks = obtainList();
					blocks.add(dstBlock);
					mSpanInBlocks.put(span,blocks);
					mSpanOrders.put(span,mInsertionOrder++);
					continue;
				}
				if(blocks.size()==0){
					//此特殊span表示在刚才的文本块中移除，但将在现在的文本块中加入
					blocks.add(dstBlock);
					continue;
				}
				
				if(blocks.contains(dstBlock)){
					//相同的文本块不用再次放入
					continue;
				}
				//若span绑定的blocks不为null，意味着它至少还有一个元素
				//为block寻找一个合适位置并插入，使blocks之中的文本块都顺序排列
				for(int k=blocks.size()-1;k>-1;--k)
			    {
					//bug: 未刷新的mIndexOfBlocks，导致block下标错误，应该在replaceSpan之前刷新
					int id = mIndexOfBlocks.get(blocks.get(k));
					if(id<i){
						blocks.add(k+1,dstBlock);
						break;
					}
					if(k==0){
						blocks.add(k,dstBlock);
						break;
					}
				}
			}
		}
	}
	
	/* 在文本或文本块改变后，刷新所有的数据 */
	private void refreshInvariants(int i)
	{
		for(;i<mBlockSize;++i)
		{
			EditableBlock block = mBlocks[i];
			mBlockStarts[i] = i>0 ? mBlockStarts[i-1]+mBlocks[i-1].length() : 0;
			Integer index = mIndexOfBlocks.get(block);
			if(index==null || index!=i){
				mIndexOfBlocks.put(block,i);
			}
		}
	}
	
	/* 在文本修改后，修正空缺span所在指定文本块中的范围，span样本通常是取自文本块的两端 */
	private void correctSpans(EditableBlock dstBlock, Object[] spans)
	{
		final int len = dstBlock.length();
		for(int k=0;k<spans.length;++k)
		{
			Object span = spans[k];
			List<EditableBlock> blocks = mSpanInBlocks.get(span);
			//文本刚刚在下标为id的文本块中插入，因此span也只可能在此文本块中的范围错误
			//如果span绑定了多个文本块，才需要修正，因为在单独文本块中的范围会自己改变
			if(blocks!=null && blocks.size()>1)
			{
				int start = dstBlock.getSpanStart(span);
				int end = dstBlock.getSpanEnd(span);
				int flags = dstBlock.getSpanFlags(span);
				if(blocks.get(0) == dstBlock){
					//span应衔接在起始块末尾
					if(end!=len){
						dstBlock.enforceSetSpan(span,start,len,flags);
					}
				}
				else if(blocks.get(blocks.size()-1) == dstBlock){
					//span应衔接在末尾块起始
					if(start!=0){
						dstBlock.enforceSetSpan(span,0,end,flags);
					}
				}
				else if(start!=0 || end!=len){
					//span应附着在整个中间块
					dstBlock.enforceSetSpan(span,0,len,flags);
				}
			}
		}
	}
	
	/* 检查在dst与src中重复的span，spans取自dst中的指定范围 */
	private static Object[] checkRepeatSpans(Spanned src, CharSequence dst, int start, int end)
	{
		Object[] spans = SpanUtils.getSpans(dst,start,end,Object.class);
		for(int i=0;i<spans.length;++i)
		{
			if(src.getSpanStart(spans[i])<0){
				//不重复的span会被置为null
				spans[i] = null;
			}
		}
		return spans;
	}
	
	/* 将重复的span从dst中截取到src的开头，可以指定在dst中截取的起始位置 */
	private static void correctRepeatSpans(EditableBlock src, CharSequence dst, Object[] spans, int start)
	{
		if(!(dst instanceof Spanned)){
			return;
		}
		Spanned dstStr = (Spanned) dst;
		for(int i=0;i<spans.length;++i)
		{
			if(spans[i] != null)
			{
				Object span = spans[i];
				int ost = src.getSpanStart(span);
				int nst = dstStr.getSpanStart(span);
				nst = nst<start ? 0:nst-start;
				if(ost!=nst){
					//此时被插入的文本必然在文本块开头，因此跨越多个文本块的span必然衔接在开头或末尾，已在correctSpan时修正的不用再次修正
					//另外的，完全被截取至单独的下个文本块且重复的span没有被correctSpan修正，应将它衔接在上次的位置之前，spanEnd已在插入时修正
					src.enforceSetSpan(span,nst,src.getSpanEnd(span),src.getSpanFlags(span));
				}
			}
		}
	}
	
	/* 移除text指定范围中与自己重复的spans */
	private CharSequence removeRepeatSpans(CharSequence text, int start, int end)
	{
		if(text instanceof Spanned)
		{
			Spannable editor = text instanceof Spannable ? (Spannable)text:new SpannableStringBuilderLite(text);
			Object[] spans = SpanUtils.getSpans(editor,start,end,Object.class);
			for(int i=0;i<spans.length;++i)
			{
				Object span = spans[i];
				if(mSpanInBlocks.get(span)!=null){
					editor.removeSpan(span);
				}
			}
			return editor;
		}
		return text;
	}
	
	/* 获取端点正好处于index的span，必须在修正后调用 */
	private Object[] getAtPointSpans(int index)
	{
		Object[] spans = quickGetSpans(index,index,Object.class);
		for(int i=0;i<spans.length;++i)
		{
			Object span = spans[i];
			if(getSpanStart(span)!=index && getSpanEnd(span)!=index){
				//端点不处于index的span会被置为null
				spans[i] = null;
			}
		}
		return spans;
	}
	
	/* 若端点正好处于index的span未进行扩展，我们应该将它扩展到理想的位置，必须在修正后调用 */
	private void expandSpans(Object[] spans, int start, int end)
	{
		for(int i=0;i<spans.length;++i)
		{
			if(spans[i] != null)
			{
				Object span = spans[i];
				List<EditableBlock> blocks = mSpanInBlocks.get(span);
				EditableBlock blockStart = blocks.get(0);
				EditableBlock blockEnd = blocks.get(blocks.size()-1);
				int ost = mBlockStarts[mIndexOfBlocks.get(blockStart)] + blockStart.getSpanStart(span);
				int oen = mBlockStarts[mIndexOfBlocks.get(blockEnd)] + blockEnd.getSpanEnd(span);
				int nst = ost;
				int nen = oen;
				int flags = blockStart.getSpanFlags(span);
				
				//spanStart端点如果处于start，扩展后它应等于start，它的spanEnd端点应大于等于start，spanEnd端点扩展后应大于等于end
				//spanEnd端点如果处于start，扩展后它应大于等于end，它的spanStart端点应小于或等于start，spanStart端点扩展后应小于等于start
				if(ost>start && blockStart.needExpandSpanStart(span,flags)){
					//也就是说spanStart端点处于或不处于start，它扩展后应该保持在start或start之前，包含start~end之间的内容
					nst = start;
				}
				if(oen<end && blockEnd.needExpandSpanEnd(span,flags)){
					//也就是说spanEnd端点处于或不处于start，它扩展后应该移动到end或end之后，包含start~end之间的内容
					nen = end;
				}
				if(ost!=nst || oen!=nen){
					//如果确实未扩展，我们应该将它扩展到理想的位置
					enforceSetSpan(span,nst,nen,flags);
				}
			}
		}
	}
	
	/* 检查是否是无效span，随文本块的规则变动，span需要跟随文本块的添加而添加 */
	public boolean isInvalidSpan(Object span, int start, int end, int flags){
		return mBlocks[0].isInvalidSpan(span,start,end,flags);
	}
	/* 当start~end之间的文本被删除，是否可以移除span */
	public boolean canRemoveSpan(Object span, int delstart, int delend, boolean textIsRemoved)
	{
		List<EditableBlock> blocks = mSpanInBlocks.get(span);
		EditableBlock blockStart = blocks.get(0);
		delstart -= mBlockStarts[mIndexOfBlocks.get(blockStart)];
		if(blocks.size()==1){
			return blockStart.canRemoveSpan(span,delstart,delend,textIsRemoved);
		}
		EditableBlock blockEnd = blocks.get(blocks.size()-1);
		delend -= mBlockStarts[mIndexOfBlocks.get(blockEnd)];
		return blockStart.canRemoveSpan(span,delstart,blockStart.length(),textIsRemoved) && blockEnd.canRemoveSpan(span,0,delend,textIsRemoved);
	}
	/* span的起始位置是否需要扩展 */
	public boolean needExpandSpanStart(Object span, int flags){
		return mBlocks[0].needExpandSpanStart(span,flags);
	}
	/* span的末尾位置是否需要扩展 */
	public boolean needExpandSpanEnd(Object span, int flags){
		return mBlocks[0].needExpandSpanEnd(span,flags);
	}
	
	@Override
	public void clear()
	{
		final int before = mLength;
		sendBeforeTextChanged(0,before,0);
		
		//所有内容全部清空
		for(int i=0;i<mBlockSize;++i){
			mBlocks[i] = null;
		}
		mIndexOfBlocks.clear();
		mSpanInBlocks.clear();
		mSpanOrders.clear();
		mLength = 0;
		mBlockSize = 0;
		mInsertionOrder = 0;
		
		sendBlocksCleared();
		sendTextChanged(0,before,0);
		setSelection(0,0);
		sendAfterTextChanged();
	}

	@Override
	public void clearSpans()
	{
		//移除所有文本块的所有span，并移除所有绑定
		for(int i=0;i<mBlockSize;++i){
			mBlocks[i].clearSpans();
		}
		mSpanInBlocks.clear();
		mSpanOrders.clear();
		mInsertionOrder = 0;
	}

	@Override
	public void enforceSetSpan(final Object span, int start, int end, final int flags){
		setSpan(span,start,end,flags,true);
	}
	@Override
	public void setSpan(final Object span, int start, int end, final int flags){
		setSpan(span,start,end,flags,false);
	}

	/* 用指定的span标记范围内的文本，enforce表示是否需要强制设置而无视其是否是无效span */
	private void setSpan(final Object span, int start, int end, final int flags, final boolean enforce)
	{
		if(!enforce && isInvalidSpan(span,start,end,flags)){
			//从该类创建无效跨度时，自动忽略无效跨度
			return;
		}
		
		if(mSpanInBlocks.get(span)!=null){
			//如果已有这个span，先移除它，但保留它的插入顺序
			int order = mSpanOrders.get(span);
			removeSpan(span);
			mSpanOrders.put(span,order);
		}
		else{
			mSpanOrders.put(span,mInsertionOrder++);
		}
		//无论如何再添加一个新的，我们不能保留已回收的list的指针
		final List<EditableBlock> blocks = obtainList();
		mSpanInBlocks.put(span,blocks);

		//将范围内的所有文本块都设置span，并建立绑定
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				if(!enforce && mBlocks[id].isInvalidSpan(span,start,end,flags)){
					//从该类创建无效跨度时，自动忽略无效跨度
					return;
				}
				EditableBlock block = mBlocks[id];
				if(!enforce){
					block.setSpan(span,start,end,flags);
				}else{
					block.enforceSetSpan(span,start,end,flags);
				}
				blocks.add(block);
			}
		};
		DoThing(start,end,d);
	}
	
	@Override
	public void removeSpan(Object span)
	{	
	    //移除span与editors的绑定，并将span从这些文本块中移除
		List<EditableBlock> blocks = mSpanInBlocks.remove(span);
		if(blocks!=null)
		{
			int size = blocks.size();
			for(int i=0;i<size;++i){
				Editable block = blocks.get(i);
				block.removeSpan(span);
			}
			recyleList(blocks);
			mSpanOrders.remove(span);
		}
	}

	@Override
	public <T extends Object> T[] getSpans(int start, int end, Class<T> kind){
		return getSpans(start,end,kind,true);
	}
	@Override
	public <T extends Object> T[] quickGetSpans(int queryStart, int queryEnd, Class<T> kind){
		return getSpans(queryStart,queryEnd,kind,false);
	}
	
	/* 获取与指定范围重叠的指定类型的span，sort表示是否按优先级和插入顺序排序 */
	private <T extends Object> T[] getSpans(int start, int end, final Class<T> kind, boolean sort)
	{
		final Set spanSet = obtainSet();
		T[] spans = EmptyArray.emptyArray(kind);
		int i = findBlockIdForIndex(start);
		int j = findBlockIdForIndex(end);
		start -= mBlockStarts[i];
		end -= mBlockStarts[j];
		
		//收集范围内所有文本块的span，并不包含重复的span
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				T[] spans = mBlocks[id].quickGetSpans(start,end,kind);
				Collections.addAll(spanSet,spans);
			}
		};
		DoThing(i,j,start,end,d);	
		
		//仍要包含两端的span，由于每个文本块都不为空，因此不用额外寻找，它们就是上个和下个文本块
		//插入文本时，新增的文本块都被填充截取的文本，而删除文本时，为空的文本块被移除
		if(start==0 && i>0){
			spans = mBlocks[i-1].quickGetSpans(mBlocks[i-1].length(),mBlocks[i-1].length(),kind);
			Collections.addAll(spanSet,spans);
		}
		if(end==mBlocks[j].length() && j<mBlockSize-1){
			spans = mBlocks[j+1].quickGetSpans(0,0,kind);
			Collections.addAll(spanSet,spans);
		}
		//创建一个指定长度的数组类型的对象并转换，然后将span转移到其中
		spans = (T[]) Array.newInstance(kind,spanSet.size());
		spanSet.toArray(spans);
		recyleSet(spanSet);
		
		//虽然无法保证span优先级，但是我们可以重新排序
		if(sort)
		{
			final int[] prioSortBuffer = SpannableStringBuilderLite.obtain(spans.length);
			final int[] orderSortBuffer = SpannableStringBuilderLite.obtain(spans.length);
			for(i=0;i<spans.length;++i){
				prioSortBuffer[i] = getSpanFlags(spans[i]) & SPAN_PRIORITY;
				orderSortBuffer[i] = mSpanOrders.get(spans[i]);
			}
			SpannableStringBuilderLite.sort(spans,prioSortBuffer,orderSortBuffer);
			SpannableStringBuilderLite.recycle(prioSortBuffer);
			SpannableStringBuilderLite.recycle(orderSortBuffer);
		}
		return spans;
	}

	@Override
	public int getSpanStart(Object p1)
	{
		//在设置span后，span绑定的所有文本块正序排列，即使replace后，所有文本块仍正序排列
		//获取span所绑定的第一个文本块，然后获取文本块的起始位置，并附加span在此文本块的起始位置
		List<EditableBlock> blocks = mSpanInBlocks.get(p1);
		if(blocks==null){
			return -1;
		}
		Editable block = blocks.get(0);
		int id = mIndexOfBlocks.get(block);
		int start = block.getSpanStart(p1);
		return mBlockStarts[id]+start;
	}

	@Override
	public int getSpanEnd(Object p1)
	{
		//获取span所绑定的最后一个文本块，然后获取文本块的起始位置，并附加span在此文本块的末尾位置
		List<EditableBlock> blocks = mSpanInBlocks.get(p1);
		if(blocks==null){
			return -1;
		}
		Editable block = blocks.get(blocks.size()-1);
		int id = mIndexOfBlocks.get(block);
		int end = block.getSpanEnd(p1);
		return mBlockStarts[id]+end;
	}

	@Override
	public int getSpanFlags(Object p1)
	{
		List<EditableBlock> blocks = mSpanInBlocks.get(p1);
		if(blocks==null){
			return 0;
		}
		return blocks.get(0).getSpanFlags(p1);
	}

	@Override
	public int nextSpanTransition(int start, int limit, Class kind)
	{
		//先走到start指定的文本块，并获取文本块start后的span位置
		int i = findBlockIdForIndex(start);
		Editable block = mBlocks[i];
		int blockStart = mBlockStarts[i];
		if(i<mBlockSize-1 && start-blockStart==block.length()){
			//刚好在最后，理应是下一块的起始位置
			block = mBlocks[i+1];
			blockStart = mBlockStarts[i+1];
		}
		int next = block.nextSpanTransition(start-blockStart,limit-blockStart,kind);
		return next+blockStart;
	}

	@Override
	public int length(){
		return mLength;
	}

	@Override
	public char charAt(int p1)
	{
		//先走到start指定的文本块
		int i = findBlockIdForIndex(p1);
		Editable block = mBlocks[i];
		int start = mBlockStarts[i];
		if(i<mBlockSize-1 && p1-start >= block.length()){
			//刚好在最后，理应是下一块的起始位置
			return mBlocks[i+1].charAt(0);
		}
		return mBlocks[i].charAt(p1-start);
	}

	@Override
	public void getChars(int start, int end, final char[] arr, final int index)
	{
		//收集范围内所有文本块的字符，存储到arr中
		Do d = new Do()
		{
			private int getChars = 0;
			
			@Override
			public void dothing(int id, int start, int end)
			{
				mBlocks[id].getChars(start,end,arr,index+getChars);
				getChars+=end-start;
				//累计已经获取的字符数，便于计算下块的字符在arr的起始获取位置
			}
		};
		DoThing(start,end,d);
	}
	
	@Override
	public CharSequence subSequence(int start, int end){
		return new EditableBlockList(this,start,end);
	}
	public String subString(int start, int end)
	{ 
		char[] buf = new char[end - start];
        getChars(start, end, buf, 0);
        return new String(buf);
	}
	@Override
	public String toString()
	{
		int len = length();
        char[] buf = new char[len];
        getChars(0, len, buf, 0);
        return new String(buf);
	}

	@Override
	public void setFilters(InputFilter[] p1){
		mFilters = p1;
	}
	@Override
	public InputFilter[] getFilters(){
		return mFilters;
	}

	@Override
	public void setAutoReleaseExcessMemory(boolean auto)
	{
		AutoReleaseExcessMemory = auto;
		for(int i=0;i<mBlockSize;++i){
			mBlocks[i].setAutoReleaseExcessMemory(auto);
		}
	}
	private void ReleaseExcessMemory()
	{
		if(mBlocks.length > mBlockSize*3){
			mBlocks = ArrayUtils.copyNewArray(mBlocks,mBlockSize,GrowingArrayUtils.growSize(mBlockSize));
			mBlockStarts = ArrayUtils.copyNewIntArray(mBlockStarts,mBlockSize,GrowingArrayUtils.growSize(mBlockSize));
		}
	}
	
	
	private static interface Do
	{
		public void dothing(int id, int start, int end)
	}
	
	/* 将start~end的范围拆分为文本块的范围，并逐块调用 */
	private void DoThing(int start, int end, Do d)
	{
		//找到start和end所指定的文本块，并将它们偏移到文本块的下标
		int i = findBlockIdForIndex(start);
		int j = findBlockIdForIndex(end);
		start -= mBlockStarts[i];
		end -= mBlockStarts[j];

		//从起始块开始，调至末尾块
		DoThing(i,j,start,end,d);
	}
	private void DoThing(int i, int j, int start, int end, Do d)
	{
		//从起始块开始，调至末尾块
		if(i==j){
			d.dothing(i,start,end);
		}
		else
		{
			d.dothing(i,start,mBlocks[i].length());
			for(++i;i<j;++i){
				d.dothing(i,0,mBlocks[i].length());
			}
			d.dothing(i,0,end);
		}
	}
	
	/* 发送文本块事件 */
	private void sendBlockAdded(int i){
		if(mBlockListener!=null){
			mBlockListener.onAddBlock(i);	
		}
	}
	private void sendBlockRemoved(int i){
		if(mBlockListener!=null){
			mBlockListener.onRemoveBlock(i);	
		}
	}
	private void sendBlocksDeleteBefore(int i, int j, int iStart, int jEnd){
		if(mBlockListener!=null){
			mBlockListener.onBlocksDeleteBefore(i,j,iStart,jEnd);
		}
	}
	private void sendBlocksDeleteAfter(int i, int j, int iStart, int jEnd){
		if(mBlockListener!=null){
			mBlockListener.onBlocksDeleteAfter(i,j,iStart,jEnd);
		}
	}
	private void sendBlocksInsertAfter(int i, int j, int iStart, int jEnd){
		if(mBlockListener!=null){
			mBlockListener.onBlocksInsertAfter(i,j,iStart,jEnd);
		}
	}
	private void sendAfterBlocksChanged(int i, int iStart){
		if(mBlockListener!=null){
			mBlockListener.afterBlocksChanged(i,iStart);
		}
	}
	private void sendBlocksCleared(){
		if(mBlockListener!=null){
			mBlockListener.clearBlocks();
		}
	}
	
	/* 发送文本事件 */
	private void sendBeforeTextChanged(int start, int before, int after) {
        mTextWatcherDepth++;
        if(mTextWatcher!=null){
			mTextWatcher.beforeTextChanged(this,start,before,after);
		}
        mTextWatcherDepth--;
    }
    private void sendTextChanged(int start, int before, int after) {
        mTextWatcherDepth++;
		if(mTextWatcher!=null){
			mTextWatcher.onTextChanged(this, start, before, after);
		}     
        mTextWatcherDepth--;
    }
    private void sendAfterTextChanged() {
        mTextWatcherDepth++;
		if(mTextWatcher!=null){
            mTextWatcher.afterTextChanged(this);
		}
        mTextWatcherDepth--;
    }
	public int getTextWatcherDepth() {
        return mTextWatcherDepth;
    }
	
	
	/* 对光标的处理 */
	public void setSelection(int start, int end)
	{
		if(start!=mSelectionStart || end!=mSelectionEnd)
		{
			int ost = mSelectionStart;
			int oen = mSelectionEnd;
			mSelectionStart = start;
			mSelectionEnd = end;
			sendSelectionChanged(start,end,ost,oen);
		}
	}
	public int getSelectionStart(){
		return mSelectionStart;
	}
	public int getSelectionEnd(){
		return mSelectionEnd;
	}
	private void sendSelectionChanged(int st, int en, int ost, int oen){
		if(mSelectionWatcher!=null){
			mSelectionWatcher.onSelectionChanged(st,en,ost,oen,this);
		}
	}
	public Editable replaceUseSelection(int before, int after, CharSequence p1, int p2, int p3){
		return replace(mSelectionStart-before,mSelectionEnd+after,p1,p2,p3);
	}
	
	
	//任意对象有一个锁和一个等待队列，锁只能被一个线程持有，其他试图获得同样锁的线程需要等待
	//任意对象都可以作为锁对象，对于拥有相同锁的所有代码块，无论在代码块中做了什么，都保证同时只有一个线程能执行这些代码块
	//在方法上加synchronized，实际上是以this对象作为锁，而在静态方法上加synchronized，实际上是以类对象作为锁
	//我们再强调下，synchronized保护的是对象而非代码，只要访问的是同一个对象的synchronized代码块，即使是不同的代码，也会被同步顺序访问
	//反之，多个线程是可以同时执行同一个synchronized方法的，只要它们访问的对象是不同的
	//此外，需要说明的，synchronized方法不能防止非synchronized方法被同时执行，因为非synchronized方法不需要锁
	
	//synchronized代码块的执行过程大概如下：
	//尝试获得锁，如果能够获得锁，继续下一步，否则加入等待队列，阻塞并等待唤醒
	//执行实例方法体代码
	//释放锁，如果等待队列上有等待的线程，从中取一个并唤醒，如果有多个等待的线程，唤醒哪一个是不一定的，不保证公平性

	
	/* 回收不使用的List，便于复用 */
	private static int ListCount = 0;
	private static final int MaxListCount = 10000;
	private static List[] ListBuffer = new List[100];
	
    static List obtainList()
	{
		//我们只能同时让一个线程执行ListBuffer代码块中的内容
		synchronized(ListBuffer)
		{
			if(ListCount>0){
				List buffer = ListBuffer[--ListCount];
				ListBuffer[ListCount] = null;
				return buffer;
			}
		}
		//但当该线程判断完毕后，若没有buffer，则自己创建一个，而不阻塞其它线程
		return new IdentityArrayList();
	}
	static void recyleList(List buffer)
	{
		//每个线程均可先各自清空自己的buffer并等待回收
		//Android 应用中默认有三个线程: 主线程、GC线程、和Heap线程
		//而且在GC线程运行的过程中，主线程会中断执行，因此若buffer在此时不清空，之后仍然会阻塞主线程
		buffer.clear();
		//我们只能同时让一个线程执行ListBuffer代码块中的内容
		synchronized(ListBuffer)
		{
			if(ListCount<MaxListCount)
			{
				if(ListCount+1 > ListBuffer.length) {
					int newSize = GrowingArrayUtils.growSize(ListCount);
					newSize = newSize>MaxListCount ? MaxListCount:newSize;
					ListBuffer = ArrayUtils.copyNewArray(ListBuffer,ListCount,newSize);
				}
				ListBuffer[ListCount++] = buffer;
			}
		}
	}
	
	/* 这边也是，回收不使用的Set */
	private static int SetCount = 0;
	private static Set[] SetBuffer = new Set[6];
	
	private static Set obtainSet()
	{
		synchronized(SetBuffer)
		{
			if(SetCount>0){
				Set buffer = SetBuffer[--SetCount];
				SetBuffer[SetCount] = null;
				return buffer;
			}
		}
		return new IdentityHashSet();
	}
	private static void recyleSet(Set buffer)
	{
		buffer.clear();
		synchronized(SetBuffer)
		{
			if(SetCount<SetBuffer.length){
				SetBuffer[SetCount++] = buffer;
			}
		}
	}
	
	
	/* 测试代码 */
	private static final StringBuilder b = new StringBuilder();
	
	public String printSpanInBlocks(Object span)
	{
		b.delete(0,b.length());
		b.append("[");
		List<EditableBlock> blocks = mSpanInBlocks.get(span);
		if(blocks!=null){
			for(Editable block:blocks){
				int id = mIndexOfBlocks.get(block);
				b.append("("+id+","+block.getSpanStart(span)+"~"+block.getSpanEnd(span)+")，");
			}
		}
		b.append("]");
		return b.toString();
	}
	
	public void printSpans(){
		for(Object span:mSpanInBlocks.keySet()){
			Log.w("Span At "+getSpanStart(span) ,printSpanInBlocks(span));
		}
	}
	
	public String printSpanOrders(Object[] spans)
	{
		b.delete(0,b.length());
		b.append("[");
		for(Object span:spans){
			int order = mSpanOrders.get(span);
			b.append("("+order+","+getSpanStart(span)+"~"+getSpanEnd(span)+")"+";");
		}
		b.append("]");
		return b.toString();
	}
	
}
