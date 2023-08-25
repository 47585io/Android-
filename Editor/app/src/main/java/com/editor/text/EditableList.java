package com.editor.text;

import android.text.*;
import java.util.*;
import java.lang.reflect.*;
import com.editor.text.base.*;


public class EditableList extends Object implements Editable
{

	private int mLength;
	private int mBlockSize;
	
	private Editable[] mBlocks;
	private int[] mBlockStarts;
	private Map<Editable,Integer> mIndexOfBlocks;
	private Map<Object,List<Editable>> mSpanInBlocks;

	private Factory mEditableFactory;
	private TextWatcher mTextWatcher;
	private SelectionWatcher mSelectionWatcher;
	private BlockListener mBlockListener;
	
	private int mTextWatcherDepth;
	private int MaxCount;
	private static final int Default_MaxCount = 1024;
	private InputFilter[] mFilters = NO_FILTERS;
	private static final InputFilter[] NO_FILTERS = new InputFilter[0];
	
	
	public EditableList(){
		this("");
	}
	public EditableList(CharSequence text){
		this(text,0,text.length());
	}
	public EditableList(CharSequence text, int start, int end){
		this(text,start,end,Default_MaxCount);
	}
	public EditableList(CharSequence text, int start, int end, int count)
	{
		MaxCount = count;
		mBlocks = EmptyArray.emptyArray(Editable.class);
		mBlockStarts = EmptyArray.INT;
		mIndexOfBlocks = new IdentityHashMap<>();
		mSpanInBlocks = new IdentityHashMap<>();

		dispatchTextBlock(0,text,start,end);
		refreshInvariants(0);
		mLength = end-start;
	}
	
	public void setEditableFactory(Factory fa){
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
	
	/* 在指定位置添加文本块 */
	private void addBlock(int i)
	{
		Editable block = mEditableFactory==null ? new SpannableStringBuilderLite() : mEditableFactory.newEditable("");
		mBlocks = GrowingArrayUtils.insert(mBlocks,mBlockSize,i,block);
		mBlockStarts = GrowingArrayUtils.insert(mBlockStarts,mBlockSize,i,0);
		mIndexOfBlocks.put(block,i);
		mBlockSize++;
		sendBlockAdded(i);
	}
	/* 添加文本块的同时，填充它的文本 */
	private void addBlock(int i ,CharSequence tb, int start, int end)
	{
		addBlock(i);
		repalceWithSpan(i,0,0,tb,start,end);
	}
	/* 移除文本块 */
	private void removeBlock(int i)
	{
		Editable block = mBlocks[i];
		replaceSpan(i,0,block.length(),"",0,0);
		mBlocks = GrowingArrayUtils.remove(mBlocks,mBlockSize,i);
		mBlockStarts = GrowingArrayUtils.remove(mBlockStarts,mBlockSize,i);
		mIndexOfBlocks.remove(block);
		mBlockSize--;
		sendBlockRemoved(i);
	}
	/* 从指定id的文本块开始，分发text中指定范围内的文本 */
	private int dispatchTextBlock(int id, CharSequence tb,int tbStart,int tbEnd)
	{
		//每次从tbStart开始向后切割MaxCount个字符，并添加到mBlocks中，直至tbEnd
		while(true)
		{
			if(tbEnd-tbStart<=MaxCount){
				addBlock(id,tb,tbStart,tbEnd);
				break;
			}
			addBlock(id,tb,tbStart,tbStart+MaxCount);
			tbStart+=MaxCount;
			++id;
		}
		return id;
	}
	
	public Editable getBlock(int i){
		return mBlocks[i];
	}
	public int getBlockSize(){
		return mBlockSize;
	}
	/* 寻找指定下标所在的文本块 */
	public int findBlockIdForIndex(int index)
	{
		//找到临近的位置，然后从此处开始
		int id = index/MaxCount;
		if(id<0){
			id=0;
		}
		if(id>mBlockSize-1){
			id = mBlockSize-1;
		}
		int nowIndex = mBlockStarts[id];

		if(nowIndex<index){
			for(;id<mBlockSize-1 && mBlockStarts[id+1]<index;++id){}
		}
		else if(nowIndex>index){
			for(;id>0 && mBlockStarts[id]>index;--id){}
		}
		return id;
	}
	public int getBlockStartIndex(int id){
		return mBlockStarts[id];
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
	
	@Override
	public Editable replace(int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		//过滤文本
        for (int i = 0; i < mFilters.length; i++) {
            CharSequence repl = mFilters[i].filter(tb, tbStart, tbEnd, this, start, end);
            if (repl != null) {
                tb = repl;
                tbStart = 0;
                tbEnd = repl.length();
            }
        }
		
		//文本变化前，调用文本监视器的方法
		int st = start;
		int before = end-start;
		int after = tbEnd-tbStart;
		sendBeforeTextChanged(start,before,after);
		
		//找到start和end所指定的文本块，并将它们偏移到文本块的下标
		int i = findBlockIdForIndex(start);
		int j = findBlockIdForIndex(end);
		start-=mBlockStarts[i];
		end-=mBlockStarts[j];
		
		//只要是插入，都需要扩展可能衔接在两端的span
		//若是纯删除，其实并不用扩展，也不能扩展
		//在删除后，span的位置是正确的，但插入后就不一定了
		Object[] spans = EmptyArray.OBJECT;
			
		if(before>0){
			//删除范围内的文本和文本块
		    deleteForBlocks(i,j,start,end);
		}
		if(after>0){
			//插入前，获取可能衔接在两端的span
			spans = mBlocks[i].getSpans(start,start,Object.class);
			//删除后，末尾下标已不可预测，但起始下标仍可用于插入文本
			insertForBlocks(i,start,tb,tbStart,tbEnd);
		}
		
		//在修正范围前必须刷新数据
		refreshInvariants(i);
		//遍历所有的span，修正它们绑定的文本块，并修正它们在文本块中的范围
		for(j=0;j<spans.length;++j){
			correctSpan(spans[j]);
		}
		
		//最后统计长度和光标位置，并调用文本和文本块和光标监视器的方法
		mLength += -before+after;
		//int selectionStart = Selection.getSelectionStart(this);
		//int selectionEnd = Selection.getSelectionEnd(this);
		//int index = st+after;
		//Selection.setSelection(this,index);
		
		sendAfterBlocksChanged(i,start);
		sendTextChanged(st,before,after);
		//sendSelectionChanged(index,index,selectionStart,selectionEnd);
		sendAfterTextChanged();
		return this;
	}
	
	/* 从指定文本块的指定位置插入文本 */
	private void insertForBlocks(int i, int index, CharSequence tb, int tbStart, int tbEnd)
	{
		//先插入文本，让在此范围内的span进行扩展
		repalceWithSpan(i,index,index,tb,tbStart,tbEnd);
		sendBlocksInsertAfter(i,i,index,index+tbEnd-tbStart);
		
		//再检查文本块的内容是否超出MaxCount
		int srcLen = mBlocks[i].length();	
		if(srcLen > MaxCount)
		{								   
			//将超出的文本截取出来
			CharSequence text = mBlocks[i].subSequence(MaxCount,srcLen);
			int overLen = srcLen-MaxCount;
			deleteForBlocks(i,i,MaxCount,srcLen);
			
			//如果超出的文本小于或等于MaxCount
			if(overLen<=MaxCount)
			{
				if (mBlockSize-1 == i || mBlocks[i+1].length()+overLen > MaxCount){
					//若无下个文本块，则添加一个
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个
					addBlock(i+1);
				}
				//之后将超出的文本插入下个文本块开头
				repalceWithSpan(i+1,0,0,text,0,overLen);
				sendBlocksInsertAfter(i+1,i+1,0,overLen);
			}
			else
			{
				//如果超出的文本大于MaxCount，必须分发
				int j = dispatchTextBlock(i+1,text,0,overLen);
				sendBlocksInsertAfter(i+1,j,0,mBlocks[j].length());
			}
		}
	}
	
	/* 删除指定范围内的文本和文本块 */
	private void deleteForBlocks(int i, int j, int start, int end)
	{
		if(i==j)
		{
			//只要删除一个文本块中的内容
			sendBlocksDeleteBefore(i,i,start,end);
			if(start==0 && end==mBlocks[i].length()){
				//如果文本块被移除，最后起始块下标i无效
				removeBlock(i);
				i = -1;
			}else{		
				repalceWithSpan(i,start,end,"",0,0);
			}
			sendBlocksDeleteAfter(i,i,start,start);
		}
		else
		{
			//要删除多个文本块的内容
			sendBlocksDeleteBefore(i,j,start,end);
			int ii = i, jj = i+1;
			//删除起始块的内容
			if(start==0){
				//如果起始块移除，则i,j向前移1步，并且最后起始块下标ii无效
				removeBlock(i);
				--i;
				--j;
				ii = -1;
			}else{
				//否则就删除范围内的文本
			    repalceWithSpan(i,start,mBlocks[i].length(),"",0,0);
			}
			
			for(++i;i<j;--j){
				//中间的块必然不会进行测量，而是全部删除
				removeBlock(i);
			}
			
			//删除末尾块的内容
			if(end==mBlocks[i].length()){
				//如果末尾块移除，则i,j向前移1步，并且最后末尾块下标jj无效
				removeBlock(i);
				jj = -1;
			}else{
			    repalceWithSpan(i,0,end,"",0,0);
			}
			sendBlocksDeleteAfter(ii,jj,start,0);
		}
	}
	
	/* 替换指定文本块的文本及span的绑定 */
	private void repalceWithSpan(int i, int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		Editable block = mBlocks[i];
		//在删除文本前，替换span的绑定
		replaceSpan(i,start,end,tb,tbStart,tbEnd);
		//最后将实际文本替换
		block.replace(start,end,tb,tbStart,tbEnd);
	}

	/* 替换指定文本块的span的绑定 */
	private void replaceSpan(int i, int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		//先移除指定文本块start~end范围内的span与block的绑定
		Editable block = mBlocks[i];
		if(end > start)
		{
			Object[] spans = block.getSpans(start,end,Object.class);
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				int st = block.getSpanStart(span);
				int en = block.getSpanEnd(span);
				//如果span完全被移除，则可以与文本块解除绑定
				if(st>=start && en<=end)
				{
					List<Editable> blocks = mSpanInBlocks.get(span);
					if(blocks!=null)
					{
						if(blocks.size()==1){
							//如果span只在这一个文本块中，可以直接移除span
							mSpanInBlocks.remove(span);
						}else{
							blocks.remove(block);
						}
					}
				}
			}
		}
		
		//如果要替换的文本是Spanned，范围内的span需要与block建立新的绑定
		if(tbEnd>tbStart && tb instanceof Spanned)
		{
			Object[] spans = ((Spanned)tb).getSpans(tbStart,tbEnd,Object.class);
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				List<Editable> blocks = mSpanInBlocks.get(span);
				//一个全新的span，需要映射到一个新的列表，并加入block
				if(blocks==null)
				{
					blocks = new ArrayList<>();
					blocks.add(block);
					mSpanInBlocks.put(span,blocks);
					continue;
				}
				
				if(blocks.contains(block)){
					//相同的文本块不用再次放入
					continue;
				}
				//若span绑定的blocks不为null，意味着它至少还有一个元素
				//为block寻找一个合适位置并插入，blocks之前的文本块都顺序排列
				for(int k=blocks.size()-1;k>-1;--k)
			    {
					int id = mIndexOfBlocks.get(blocks.get(k));
					if(id<i){
						blocks.add(k+1,block);
						break;
					}
					if(k==0){
						blocks.add(k,block);
						break;
					}
				}
			}
		}
	}
	
	/* 在文本修改后，修正空缺span绑定的文本块和其所在文本块中的范围，span样本通常是取自修改范围的两端
	 * 若删除了一个span，并把它全删了，这倒没什么
	 * 若删除了一个span，并把它前半部分删了，因此在插入文本时会被挤至后面，这也没什么
	 * 若删除了一个span，并把它后半部分删了，因此在插入文本时仍保持在前面，这都没什么
	 * 若删除了一个span，并把它中间删了，两端保留，在插入文本后span应该扩展并包含中间所有文本块，而不是仅悬停在两端
	 */
	private void correctSpan(Object span)
	{
		List<Editable> blocks = mSpanInBlocks.get(span);
		//如果span绑定了多个文本块
		if(blocks!=null && blocks.size()>1)
		{
			int size = blocks.size();	
			//先修正span绑定的文本块，使它们连续排列
			for(int j=0;j<size-1;++j)
			{
				int id = mIndexOfBlocks.get(blocks.get(j));
				int nextId = mIndexOfBlocks.get(blocks.get(j+1));		
				//如果当前文本块的下标和下个文本块的下标不连续，需要插入中间的文本块
				if(id+1<nextId)
				{
					//从id+1开始，向nextId前进，将途中的文本块添加到blocks中
					for(++id;id<nextId;++id){
						blocks.add(++j,mBlocks[id]);
						++size;
					}
				}
			}
				
			int flags = blocks.get(0).getSpanFlags(span);
			//遍历所有文本块，修正span在文本块中的范围(请注意，即使绑定正确，也不能代表范围设置正确)
			for(int j=0;j<size;++j)
			{
				int id = mIndexOfBlocks.get(blocks.get(j));
				Editable block = mBlocks[id];
				int len = block.length();
				int start = block.getSpanStart(span);
				int end = block.getSpanEnd(span);
					
				if(j==0){
					//span应衔接在起始块末尾
					if(end!=len){
						block.setSpan(span,start,len,flags);
					}
				}
				else if(j==size-1){
					//span应衔接在末尾块起始
					if(start!=0){
						block.setSpan(span,0,end,flags);
					}
				}
				else if(start!=0 || end!=len){
					//span应附着在整个中间块
				    block.setSpan(span,0,len,flags);
				}
			}
		}
	}
	
	/* 在文本或文本块改变后，刷新所有的数据 */
	private void refreshInvariants(int i)
	{
		for(;i<mBlockSize;++i)
		{
			Editable block = mBlocks[i];
			mBlockStarts[i] = i>0 ? mBlockStarts[i-1]+mBlocks[i-1].length() : 0;
			Integer index = mIndexOfBlocks.get(block);
			if(index==null || index!=i){
				mIndexOfBlocks.put(block,i);
			}
		}
	}
	
	
	@Override
	public void clear()
	{
		//所有内容全部清空
		mLength = 0;
		mBlockSize = 0;
		mBlocks = EmptyArray.emptyArray(Editable.class);
		mBlockStarts = EmptyArray.INT;
		mIndexOfBlocks.clear();
		mSpanInBlocks.clear();
	}

	@Override
	public void clearSpans()
	{
		//移除所有文本块的所有span，并移除所有绑定
		for(Editable editor:mBlocks){
			editor.clearSpans();
		}
		mSpanInBlocks.clear();
	}

	@Override
	public void setSpan(final Object span, int start, int end, final int flags)
	{
		if(mSpanInBlocks.get(span)!=null){
			//如果已有这个span，先移除它，无论如何再添加一个新的
			removeSpan(span);
		}
		final List<Editable> editors = new ArrayList<>();
		mSpanInBlocks.put(span,editors);
		
		//将范围内的所有文本块都设置span，并建立绑定
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				Editable editor = mBlocks[id];
				editor.setSpan(span,start,end,flags);
				editors.add(editor);
			}
		};
		DoThing(start,end,d);
	}

	@Override
	public void removeSpan(Object p1)
	{
		//移除span与editors的绑定，并将span从这些文本块中移除
		List<Editable> blocks = mSpanInBlocks.remove(p1);
		if(blocks!=null)
		{
			int size = blocks.size();
			for(int i=0;i<size;++i)
			{
				Editable block = blocks.get(i);
				if(block!=null){
					block.removeSpan(p1);
				}
			}
		}
	}

	private static final Set spanSet = new LinkedHashSet<>();
	private static final List spanList = new LinkedList<>();
	
	@Override
	public <T extends Object> T[] getSpans(int start, int end, final Class<T> kind)
	{
		int i = findBlockIdForIndex(start);
		int j = findBlockIdForIndex(end);
		start -= mBlockStarts[i];
		end -= mBlockStarts[j];
		if(i==j){
			//如果在同一文本块，则可以直接返回
			return mBlocks[i].getSpans(start,end,kind);
		}
		
		//收集范围内所有文本块的span，并不包含重复的span
		spanSet.clear();
		spanList.clear();
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				T[] spans = mBlocks[id].getSpans(start,end,kind);
				for(int k=0;k<spans.length;++k)
				{
					if(spanSet.add(spans[k])){
						//span仍应保持顺序
						spanList.add(spans[k]);
					}
				}
			}
		};
		DoThing(i,j,start,end,d);
		
		//创建一个指定长度的数组类型的对象并转换，然后将span转移到其中
		T[] spans = (T[]) Array.newInstance(kind,spanList.size());
		spanList.toArray(spans);
		return spans;
	}

	@Override
	public int getSpanStart(Object p1)
	{
		//在设置span后，span绑定的所有文本块正序排列
		//即使replace后，所有文本块仍正序排列
		//获取span所绑定的第一个文本块，然后获取文本块的起始位置，并附加span在此文本块的起始位置
		List<Editable> blocks = mSpanInBlocks.get(p1);
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
		List<Editable> blocks = mSpanInBlocks.get(p1);
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
		List<Editable> blocks = mSpanInBlocks.get(p1);
		if(blocks==null){
			return -1;
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
		if(p1-start >= block.length()){
			//刚好在最后，理应是下一块的起始位置
			return mBlocks[i+1].charAt(0);
		}
		return mBlocks[i].charAt(p1-start);
	}

	private int getChars;

	@Override
	public void getChars(int start, int end, final char[] arr, final int index)
	{
		//收集范围内所有文本块的字符，存储到arr中
		getChars = 0;
		Do d = new Do()
		{
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
	public CharSequence subSequence(int start, int end)
	{
		return new SpannableStringBuilderLite(this,start,end);
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
	
	public static interface BlockListener
	{
		public void onAddBlock(int i)
		
		public void onRemoveBlock(int i)
		
		public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)
		
		public void onBlocksDeleteAfter(int i, int j, int iStart, int jEnd)
		
		public void onBlocksInsertAfter(int i, int j, int iStart, int jEnd)
		
		public void afterBlocksChanged(int i, int iStart)
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
	
	private void sendSelectionChanged(int st, int en, int ost, int oen){
		if(mSelectionWatcher!=null){
			mSelectionWatcher.onSelectionChanged(st,en,ost,oen,this);
		}
	}
	
}
