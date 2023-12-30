package com.editor.text2.builder.words;
import com.editor.text.base.*;

/* 用括号嵌套层次构建的代码段树 */
public class CodeSnippetTree
{
	private CodeSnippet Root;
	
	public CodeSnippetTree(char[] text){
		Root = buildSnippet(text);
	}
	
	/* 返回下标所在的最内层的子代码块 */
	public CodeSnippet findSnippetAt(int index)
	{
		if(index > Root.length){
			return null;
		}
		CodeSnippet sp = findSnippetAt(Root, index);
		return sp instanceof EmptyCodeSnippet ? sp.parent : sp;
	}
	public static CodeSnippet findSnippetAt(CodeSnippet sp, int index)
	{
		for(;sp.childCount > 0;){
			//层层寻找下去，每层都用相对于它的下标
			int i = sp.findSnippetAt(index);
			index -= sp.sumRange(0, i);
			sp = sp.children[i];
		}
		return sp;
	}
	
	/* 寻找在文本中与sp紧邻的最内层的下个代码块 */
	public static CodeSnippet nextSnippet(CodeSnippet sp)
	{
		//父代码块是由子代码块的内容组成的，父代码块本身没有内容，因此我们只用遍历最底层的叶子
		for(;sp.parent != null; sp = sp.parent){
			//层层向上遍历，走到父节点的下个代码段(如果有)
			if(sp.upIndex < sp.parent.childCount - 1){
				sp = sp.parent.children[sp.upIndex+1];
				break;
			}
		}
		for(;sp.childCount > 0;){
			//层层向下遍历，直至最底下的那个，这些children[0]在文本中的位置相同
			sp = sp.children[0];
		}
		return sp;
	}
	
	/* 返回代码块在文本中的起始位置 */
	public static int getSnippetStart(CodeSnippet sp)
	{
		int start = 0;
		for (;sp.parent != null; sp = sp.parent){
			//层层向上遍历，累计在父节点中的起始位置
			start += sp.parent.sumRange(0, sp.upIndex);
		}
		return start;
	}
	
	
	public void insertText(int index, char[] text)
	{
		if(index > Root.length){
			throw new IndexOutOfBoundsException();
		}
		CodeSnippet sp = buildSnippet(text);
		CodeSnippet thiz = findSnippetAt(index);
		//将子树插入到当前树中，并向上更新父代码段的长度
		update(thiz, text.length);
	}
	
	public void deleteText(int start, int end)
	{
		
	}
	
	//从sp开始，将其所有父节点的length加上delta
	private static void update(CodeSnippet sp, int delta)
	{
		//尾递归，优化为循环
		for(;sp != null; sp = sp.parent){
			sp.length += delta;
		}
	}
	
	//解析指定的文本，并构建代码块子树
	private static CodeSnippet buildSnippet(char[] text)
	{
		int last = 0;
		int now = 0;
		int length = text.length;
		
		int top = 0;
		CodeSnippet[] stack = new CodeSnippet[length+1];
		CodeSnippet root = new CodeSnippet();
		stack[top] = root;
		
		while(now < length)
		{
			//寻找最临近的括号
			for(;now < length; ++now){
				char ch = text[now];
				if(ch == STBINDOW || ch == ENDBINDOW){
					break;
				}
			}
			
			if(now == length){
				//如果找不到，则返回
				if(last < now-1){
					CodeSnippet sp = new EmptyCodeSnippet();
					sp.length = now - last;
					stack[top].add(sp);
				}
				break;
			}
			if(last < now && (text[last] != STBINDOW || text[now] != ENDBINDOW)){
				//last ~ now之间间隔了一段文本，若{}则不是空文本块，若其它情况则是
				CodeSnippet sp = new EmptyCodeSnippet();
				sp.length = now - last;
				stack[top].add(sp);
			}
			
			if(text[now] == STBINDOW){
				//遇到前括号，让上一层括号指向它，并将它入栈
				CodeSnippet sp = new BacketCodeSnippet();
				stack[top].add(sp);
				stack[++top] = sp;
				sp.length = now; //暂时设为now
			}
			else if(text[now] == ENDBINDOW){
				//最近的前括号已经对应一个最近的后括号，pop
				CodeSnippet sp = stack[top--];
				sp.length = now - sp.length;
				sp.calcRange();
			}
			last = now;
			now += 1;
		}
		
		/*
		while(top > 0){
			stack[top].length = length-stack[top].length;
			top--;
		}*/
		return root;
	}
	
	private static final char STBINDOW = '{', ENDBINDOW = '}';
	
	/* 每一个CodeSnippet都有自己的长度和单词，并指向父节点和自己的孩子
	 * 父节点的长度通常是子节点的长度和，子节点可以使用自己以及父节点的单词
	 * 为了二分搜索和快速修改，使用线段树记录紧挨着的孩子们的长度的前缀和
	 * 每一个代段段都拥有自己的线段树，修改当前代码段孩子的长度时，需要维护自己的线段树
	 * 同时，在括号树层面，也要向上修改父代码段的长度，不过好在很快，因为将修改控制到非常小的范围
	 */
	public static class CodeSnippet
	{
		private int length;     //代码段长度 
		private Words data;     //代码段单词
		private int upIndex;    //代码段处于父节点的数组中的下标
		private CodeSnippet parent;     //指向父节点
		private int childCount;         //子节点个数
		private CodeSnippet[] children; //子节点数组
		private int[] rangeSum;         //子节点长度的区间和
		
		CodeSnippet(){
			childCount = 0;
			children = EmptyArray.emptyArray(CodeSnippet.class);
		}
		
		private void add(CodeSnippet sp){
			sp.parent = this;
			sp.upIndex = childCount;
			children = GrowingArrayUtils.append(children, childCount++, sp);
		}
		private void remove(int i){
			GrowingArrayUtils.remove(children, childCount--, i);
		}
		
		//使用当前的子节点的长度构建线段树
		private void calcRange()
		{
			final int n = childCount;
			final int count = 2 * n;
			rangeSum = new int[count];
			//先将叶子节点的值设置到线段树最底层
			for(int i = n; i < count; ++i){	
				rangeSum[i] = children[i - n].length;
			}
			//从倒数第二层开始，反向计算内部节点的值
			for(int i = n - 1; i > 0; --i){	
			    //节点i的值是它的左子节点(i << 1)与右子节点((i << 1)|1)的和
				rangeSum[i] = rangeSum[i << 1] + rangeSum[(i << 1)|1];
			}
		}
		//修改下标为i的元素的值，并维护线段树
		private void update(int i, int num)
		{
			//找到元素i在线段树中的位置，并更新叶子的值
			i += childCount; 
			rangeSum[i] = num;
			//从此节点的位置开始，向上层层更新父节点的值
			while (i > 0) {
				//父节点(i >> 1)的值是节点i与其兄弟节点(i ^ 1)的和
				rangeSum[i >> 1] = rangeSum[i] + rangeSum[i ^ 1];
				i >>= 1;
			}
		}
		//统计指定范围内的子节点的长度和
		private int sumRange(int i, int j)
		{
			int n = childCount;
			i += n; j += n; //转换到线段树的下标
			int res = 0;
			//从此节点的位置开始，向上统计范围内的节点的值
			for (; i <= j; i >>= 1, j >>= 1)
			{
				if((i & 1) != 0){
					//1、若i(是奇数)是右子结点，说明结果应包含它但不包含它的父亲，那么将结果加上rangeSum[i]并使i增加1，最后将i除以2进入下一循环； 
					//2、若i(是偶数)是左子结点，它跟其右兄弟在要求的区间里，则此时直接将i除以2（即直接进入其父亲结点）进入下一循环即可；
					res += rangeSum[i++];
				} 
				if((j & 1) == 0){
					//1、若j是左子结点，那么需要加上rangeSum[j]并使j减去1最后将j除以2进入下一循环；
					//2、若j是右子结点，它跟其左兄弟在要求的区间里，直接将j除以2进入下一循环即可
					res += rangeSum[j--];
				} 
			}
			return res;
		}
		//index处于自己的哪个子节点中
		private int findSnippetAt(int index)
		{
			//从根节点开始，向下判断index可能处于左子节点还是右子节点，直至走到叶子节点
			int i = 1;
			int start = 0;
			for(; i < childCount;)
			{
				int left = rangeSum[i << 1];
				if (index <= start + left){
					//如果index小于左子节点的长度和，则index应落于左子节点的范围中
					i <<= 1;
				}
				else {
					//否则应落于右子节点的范围中，并且之后的位置还要额外偏移left
					i = (i << 1)|1;
					start += left;
				}
			}
			return i;
		}
		
		public CodeSnippet getParent(){
			return parent;
		}
		public CodeSnippet getChildAt(int i){
			return children[i];
		}
		public int getChildCount(){
			return childCount;
		}
		public int length(){
			return length;
		}
		public void setWords(Words w){
			data = w;
		}
		public Words getWords(){
			return data;
		}
	}
	
	private static final class EmptyCodeSnippet extends CodeSnippet{}
	
	private static final class BacketCodeSnippet extends CodeSnippet{}
	
}
