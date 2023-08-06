package com.editor.text;

import android.text.*;
import java.util.*;
import java.lang.reflect.*;
import com.editor.text.base.*;


public class EditableList extends Object implements Editable
{
	
	public int MaxCount = 100000;

	Editable[] mBlocks;
	int[] mBlockStarts;
	Map<Editable,Integer> mIndexOfBlocks;
	Map<Object,Editable[]> mSpanToBlocks;
	
	private Factory mEditableFactory;
	private TextWatcher mTextWatcher;
	private BlockListener mBlockListener;
	
	private int length;
	private int mBlockSize;
	private int mLowBlockIndex;
	private int cacheLen, cacheId, getChars;
	
	
	public EditableList()
	{
		mBlocks = EmptyArray.emptyArray(Editable.class);
		mBlockStarts = EmptyArray.INT;
		mIndexOfBlocks = new IdentityHashMap<>();
		mSpanToBlocks = new IdentityHashMap<>();
	}
	public EditableList(CharSequence text)
	{
		this();
		length = text.length();
		dispatchTextBlock(0,text,0,length);
	}
	public EditableList(CharSequence text,int start,int end)
	{
		this();
		dispatchTextBlock(0,text,start,end);
		length = end-start;
	}
	
	public void setEditableFactory(Factory fa){
		mEditableFactory = fa;
	}
	public void setTextWatcher(TextWatcher wa){
		mTextWatcher = wa;
	}
	public void setBlockListener(BlockListener li){
		mBlockListener = li;
	}
	
	/* 在指定位置添加文本块，并调用监听器的添加方法 */
	private void addBlock(int i)
	{
		Editable editor = mEditableFactory==null ? new SpannableStringBuilderTemplete() : mEditableFactory.newEditable("");
		mBlocks.add(i,editor);
		if(mBlockListener!=null){
		    mBlockListener.onAddBlock(i);
		}
	}
	/* 添加文本块的同时，填充它的文本，并调用监听器的添加方法，但并不调用监听器的修改方法 */
	private void addBlock(int i ,CharSequence tb, int start, int end)
	{
		addBlock(i);
		repalceWithSpan(i,0,0,tb,start,end);
	}
	/* 移除文本块，并调用监听器的移除方法，但并不调用监听器的修改方法 */
	private void removeBlock(int i)
	{
		replaceSpan(i,0,mBlocks[i].length(),"",0,0);
		mBlocks.remove(i);
		if(mBlockListener!=null){
		    mBlockListener.onRemoveBlock(i);
		}
	}
	/* 获得文本块的内容 */
	public Spanned getBlock(int i){
		return mBlocks[i];
	}
	
	@Override
	public Editable replace(int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		int s = start, e = end;
		int ts = tbStart, te = tbEnd;
		//文本变化前，调用文本监视器的方法
		if(mTextWatcher!=null){
		    mTextWatcher.beforeTextChanged(this,s,e-s,te-ts);
		}
		
		//找到start和end所指定的文本块，并将它们偏移到文本块的下标
		int i = findBlockIdForIndex(start);
		start-=cacheLen;
		int j = findBlockIdForIndex(i,cacheLen,end);
		end-=cacheLen;
		
		//删除范围内的文本和文本块
		if(end>start){
		    deleteForBlocks(i,j,start,end);
		}
		//删除后，末尾下标已不可预测，但起始下标仍可使用
		
		if(tbEnd>tbStart)
		{
			Editable editor = mBlocks.get(i);
			int nowLen = editor.length();
			int len = tbEnd-tbStart;
			if(nowLen+len <= MaxCount)
			{
				//当插入文本不会超出当前的文本块时，直接插入
				insertForBlock(i,start,tb,tbStart,tbEnd);
			}
			/*当插入文本会超出当前的文本块时，两种方案*/
			else if(nowLen+len-MaxCount <= nowLen-start)
			{
				//方案1，先插入，之后截取多出的部分，适合小量文本
				insertForBlock(i,start,tb,tbStart,tbEnd);
				nowLen = editor.length();

				if (mBlocks.size()-1 == i || mBlocks.get(i+1).length()+nowLen-MaxCount > MaxCount){
					//若有下个文本块，但它的字数也不足，那么在我之后添加一个
					addBlock(i+1);
				}

				insertForBlock(i+1,0,editor,MaxCount,nowLen);
				deleteForBlocks(i,i,MaxCount,nowLen);
			}
			else
			{
				//方案2，精确计算删除和分发的部分，适合大量文本
				//逆序重新插入，保证文本整体插入位置不变
				dispatchTextBlock(i+1,tb,tbStart,tbEnd);
				//将index后的部分挪到插入文本后
				addBlock(cacheId+1,editor,start,nowLen);
				deleteForBlocks(i,i,start,nowLen);

				if(start==0){
					//如果文本块被移除，两下标向前移
					--i;
					--cacheId;
				}
				//这里是一整块连续的修改，当成一次调用
				if(mBlockListener!=null){
					mBlockListener.onBlocksInsertAfter(i+1,cacheId+1,0,nowLen-start);
				}
			}
		}
		
		//最后统计长度，并调用文本监视器的方法
		length += -(e-s)+(te-ts);
		if(mTextWatcher!=null){
		    mTextWatcher.onTextChanged(this,s,e-s,te-ts);
		    mTextWatcher.afterTextChanged(this);
		}
		return null;
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
	
	/* 从指定文本块的指定位置插入文本，插入完成后调用监听器的修改方法 */
	private void insertForBlock(int i, int index, CharSequence text, int tbStart, int tbEnd)
	{
		repalceWithSpan(i,index,index,text,tbStart,tbEnd);
		if(mBlockListener!=null){
			mBlockListener.onBlocksInsertAfter(i,i,index,index+tbEnd-tbStart);
		}
	}
	/* 删除指定范围内的文本和文本块，删除前后调用监听器的修改方法 */
	private void deleteForBlocks(int i, int j, int start, int end)
	{
		if(i==j)
		{
			//只要删除一个文本块中的内容
			if(mBlockListener!=null){
				mBlockListener.onBlocksDeleteBefore(i,i,start,end);
			}
			if(start==0 && end==mBlocks.get(i).length()){
				//如果文本块被移除，最后起始块下标i无效
				removeBlock(i);
				i = -1;
			}else{		
				repalceWithSpan(i,start,end,"",0,0);
			}
			if(mBlockListener!=null){
				mBlockListener.onBlocksDeleteAfter(i,i,start,start);
			}
		}
		else
		{
			//要删除多个文本块的内容
			if(mBlockListener!=null){
				mBlockListener.onBlocksDeleteBefore(i,j,start,end);
			}
			
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
			    repalceWithSpan(i,start,mBlocks.get(i).length(),"",0,0);
			}
			
			for(++i;i<j;--j){
				//中间的块必然不会进行测量，而是全部删除
				removeBlock(i);
			}
			
			//删除末尾块的内容
			if(end==mBlocks.get(i).length()){
				//如果末尾块移除，则i,j向前移1步，并且最后末尾块下标jj无效
				removeBlock(i);
				jj = -1;
			}else{
			    repalceWithSpan(i,0,end,"",0,0);
			}
			
			if(mBlockListener!=null){
				mBlockListener.onBlocksDeleteAfter(ii,jj,start,0);
			}
		}
	}
	
	/* 替换指定文本块的文本及Span的绑定，但并不调用任何监听器的方法 */
	private void repalceWithSpan(int i, int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		Editable editor = mBlocks.get(i);
		//在删除文本前，替换span的绑定
		replaceSpan(i,start,end,tb,tbStart,tbEnd);
		//最后将实际文本替换
		editor.replace(start,end,tb,tbStart,tbEnd);
	}

	/* 替换指定文本块的Span的绑定，但并不调用任何监听器的方法 */
	private void replaceSpan(int i, int start, int end, CharSequence tb, int tbStart, int tbEnd)
	{
		//先移除指定文本块start~end范围内的span与其的绑定
		Editable editor = mBlocks.get(i);
		if(end>start)
		{
			Object[] spans = editor.getSpans(start,end,Object.class);
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				int s = editor.getSpanStart(span);
				int e = editor.getSpanEnd(span);
				//如果span完全被移除，则可以解除绑定
				if(s>=start && e<=end){
					Collection<Editable> editors = mSpanToBlocks.get(span);
					editors.remove(editor);
				}
			}
		}
		//如果要替换的文本是Spanned，editor需要与其范围内的span建立新的绑定
		if(tb instanceof Spanned)
		{
			Object[] spans = ((Spanned)tb).getSpans(tbStart,tbEnd,Object.class);
			for(int j=0;j<spans.length;++j)
			{
				Object span = spans[j];
				List<Editable> editors = mSpanToBlocks.get(span);
				//一个全新的span，需要映射到一个新的列表，并加入editor
				if(editors==null){
					editors = new ArrayList<>();
					mSpanToBlocks.put(span,editors);
				}
				editors.add(editor);
			}
		}
	}
	
	@Override
	public Editable replace(int p1, int p2, CharSequence p3)
	{
		return replace(p1,p2,p3,0,p3.length());
	}

	@Override
	public Editable insert(int p1, CharSequence p2, int p3, int p4)
	{
		return replace(p1,p1,p2,p3,p4);
	}

	@Override
	public Editable insert(int p1, CharSequence p2)
	{
		return replace(p1,p1,p2,0,p2.length());
	}

	@Override
	public Editable delete(int p1, int p2)
	{
		return replace(p1,p2,"",0,0);
	}

	@Override
	public Editable append(CharSequence p1)
	{
		int len = length();
		return replace(len,len,p1,0,p1.length());
	}

	@Override
	public Editable append(CharSequence p1, int p2, int p3)
	{
		int len = length();
		return replace(len,len,p1,p2,p3);
	}

	@Override
	public Editable append(char p1)
	{
		int len = length();
		return replace(len,len,String.valueOf(p1),0,1);
	}

	@Override
	public void clear()
	{
		//所有内容全部清空
		length = 0;
		mBlocks.clear();
		addBlock(0);
		mSpanToBlocks.clear();
	}

	@Override
	public void clearSpans()
	{
		//移除所有文本块的所有span，并移除所有绑定
		for(Editable editor:mBlocks){
			editor.clearSpans();
		}
		mSpanToBlocks.clear();
	}

	@Override
	public void setFilters(InputFilter[] p1){}

	@Override
	public InputFilter[] getFilters(){
		return null;
	}

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
				mBlocks.get(id).getChars(start,end,arr,index+getChars);
				getChars+=end-start;
				//累计已经获取的字符数，便于计算下块的字符在arr的起始获取位置
			}
		};
		DoThing(start,end,d);
	}

	@Override
	public void setSpan(final Object span, int start, int end, final int flag)
	{
		if(mSpanToBlocks.get(span)!=null){
			//如果已有这个span，先移除它，无论如何再添加一个新的
			removeSpan(span);
		}
		final List<Editable> editors = new ArrayList<>();
		mSpanToBlocks.put(span,editors);
		
		//将范围内的所有文本块都设置span，并建立绑定
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				Editable editor = mBlocks.get(id);
				editor.setSpan(span,start,end,flag);
				editors.add(editor);
			}
		};
		DoThing(start,end,d);
	}

	@Override
	public void removeSpan(Object p1)
	{
		//移除span与editors的绑定，并将span从这些文本块中移除
		Editable[] blocks = mSpanToBlocks.remove(p1);
		for(int i=0;i<blocks.length;++i)
		{
			Editable block = blocks[i];
			if(block!=null){
				block.removeSpan(p1);
			}
		}
	}

	@Override
	public <T extends Object> T[] getSpans(int start, int end, final Class<T> type)
	{
		//收集范围内所有文本块的span，并不包含重复的span
		final Set<T> spanSet = new LinkedHashSet<>();
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				T[] spans = mBlocks.get(id).getSpans(start,end,type);
				for(int i=0;i<spans.length;++i){
					spanSet.add(spans[i]);
				}
			}
		};
		DoThing(start,end,d);
		//创建一个指定长度的数组类型的对象并转换，然后将span转移到其中
		T[] spans = (T[]) Array.newInstance(type,spanSet.size());
		spanSet.toArray(spans);
		return spans;
	}

	@Override
	public int getSpanStart(Object p1)
	{
		//在设置span后，span绑定的所有文本块正序排列
		//即使replace后，所有文本块仍正序排列
		//获取span所绑定的第一个文本块，然后获取文本块的起始位置，并附加span在此文本块的起始位置
		Editable editor = mSpanToBlocks.get(p1).get(0);
		int id = mBlocks.indexOf(editor);
		int start = editor.getSpanStart(p1);
		return getBlockStartIndex(id)+start;
	}

	@Override
	public int getSpanEnd(Object p1)
	{
		//获取span所绑定的最后一个文本块，然后获取文本块的起始位置，并附加span在此文本块的末尾位置
		List<Editable> editors = mSpanToBlocks.get(p1);
		Editable editor = editors.get(editors.size()-1);
		int id = mBlocks.indexOf(editor);
		int end = editor.getSpanEnd(p1);
		return getBlockStartIndex(id)+end;
	}

	@Override
	public int getSpanFlags(Object p1)
	{
		return mSpanToBlocks.get(p1).get(0).getSpanFlags(p1);
	}

	@Override
	public int nextSpanTransition(int start, int end, Class type)
	{
		//先走到start指定的文本块，并获取文本块start后的span位置
		int i = findBlockIdForIndex(start);
		Editable editor = mBlocks.get(i);
		int next = editor.nextSpanTransition(start-cacheLen,end-cacheLen,type);
		return next+cacheLen;
	}

	@Override
	public int length(){
		return length;
	}

	@Override
	public char charAt(int p1)
	{
		//先走到start指定的文本块
		int i = findBlockIdForIndex(p1);
		Editable editor = mBlocks.get(i);
		if(p1-cacheLen>=editor.length()){
			//刚好在最后，理应是下一块的起始位置
			return mBlocks.get(i+1).charAt(0);
		}
		return mBlocks.get(i).charAt(p1-cacheLen);
	}

	@Override
	public CharSequence subSequence(int start, int end)
	{
		//累计范围内的所有文本块的字符序列
		final SpannableStringBuilderTemplete b = new SpannableStringBuilderTemplete();
		Do d = new Do()
		{
			@Override
			public void dothing(int id, int start, int end)
			{
				CharSequence text = mBlocks.get(id);
				b.append(text,start,end);
			}
		};
		DoThing(start,end,d);
		return b;
	}

	@Override
	public String toString()
	{
		//累计所有文本块的字符串
		StringBuilder builder = new StringBuilder();
		int size = mBlocks.size();
		for(int i = 0;i<size;++i){
			builder.append(mBlocks.get(i));
		}
		return builder.toString();
	}
	
	/* 寻找index所指定的文本块 */
	private int findBlockIdForIndex(int index)
	{
		return findBlockIdForIndex(0,0,index);
	}
	/* 从指定的id和已前进到的start开始，寻找index所指定的文本块，并记录文本块起始位置 */
	private int findBlockIdForIndex(int i, int start, int index)
	{
		int size = mBlocks.size();
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
	/* 获取指定块的起始下标 */
	private int getBlockStartIndex(int id)
	{
		int index = 0;
		for(int i=0;i<id;++i){
			index+= mBlocks.get(i).length();
		}
		return index;
	}
	
	/* 将start~end的范围拆分为文本块的范围，并逐块调用 */
	private void DoThing(int start, int end, Do d)
	{
		//找到start和end所指定的文本块，并将它们偏移到文本块的下标
		int i = findBlockIdForIndex(start);
		int cl = cacheLen;
		int j = findBlockIdForIndex(i,cl,end);
		start -= cl;
		end -= cacheLen;

		//从起始块开始，调至末尾块
		if(i==j){
			d.dothing(i,start,end);
		}
		else
		{
			d.dothing(i,start,mBlocks.get(i).length());
			for(++i;i<j;++i){
				d.dothing(i,0,mBlocks.get(i).length());
			}
			d.dothing(i,0,end);
		}
	}
	
	private int find(int index)
	{
		//找到临近的位置，然后从此处开始
		int id = index/MaxCount;
		if(id>mBlockSize-1){
			id = mBlockSize;
		}
		int nowIndex = mBlockStarts[id];
		
		if(nowIndex<index){
			for(;mBlockStarts[id]+mBlocks[id].length()<index;++id){}
		}
		else if(nowIndex>index){
			for(;mBlockStarts[id]>index;--id){}
		}
		return id;
	}
	private int get(int id){
		return mBlockStarts[id];
	}
	
	/* 在文本或文本块改变后，刷新所有的数据 */
	private void refreshInvariants()
	{
		for(int i=mLowBlockIndex;i<mBlockSize;++i)
		{
			Editable block = mBlocks[i];
			mBlockStarts[i] = i>0 ? mBlockStarts[i-1]+block.length() : 0;
			if(mIndexOfBlocks.get(block)!=i){
				mIndexOfBlocks.put(block,i);
			}
		}
		mLowBlockIndex = Integer.MAX_VALUE;
	}
	
	private static interface Do
	{
		public void dothing(int id, int start, int end)
	}
	
	public static interface BlockListener
	{
		public void onAddBlock(int i)
		
		public void onRemoveBlock(int i)
		
		public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)
		
		public void onBlocksDeleteAfter(int i, int j, int iStart, int jEnd)
		
		public void onBlocksInsertAfter(int i, int j, int iStart, int jEnd)
	}
	
}
