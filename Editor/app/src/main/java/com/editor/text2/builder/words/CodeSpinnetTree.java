package com.editor.text2.builder.words;
import com.editor.text.base.*;

public class CodeSpinnetTree
{
	private CodeSpinnet Root;
	
	public CodeSpinnetTree(char[] text){
		Root = buildSpinnet(text);
	}
	
	public CodeSpinnet findSpinnetAt(int index)
	{
		if(index > Root.length){
			return null;
		}
		CodeSpinnet sp = findSpinnetAt(Root, index);
		return sp instanceof EmptyCodeSpinnet ? sp.parent : sp;
	}
	public CodeSpinnet findSpinnetAt(CodeSpinnet sp, int index)
	{
		
		
		return sp;
	}
	public static int getSpinnetStart(CodeSpinnet sp)
	{
		int start = 0;
		for (;sp != null; sp = sp.parent){
			start += sp.start;
		}
		return start;
	}
	
	public void insertText(int index, char[] text)
	{
		if(index > Root.length){
			throw new IndexOutOfBoundsException();
		}
		CodeSpinnet sp = buildSpinnet(text);
		CodeSpinnet thiz = findSpinnetAt(index);
		//将子树插入到当前树中
		update(thiz, text.length);
	}
	
	public void deleteText(int start, int end)
	{
		
	}
	
	//从sp开始，将其所有父节点的length加上delta
	private static void update(CodeSpinnet sp, int delta)
	{
		//尾递归，优化为循环
		for(;sp != null; sp = sp.parent){
			sp.length += delta;
		}
	}
	
	//解析指定的文本，并构建代码块子树
	private static CodeSpinnet buildSpinnet(char[] text)
	{
		int last = 0;
		int now = 0;
		int length = text.length;
		
		int top = 0;
		CodeSpinnet[] stack = new CodeSpinnet[length+1];
		CodeSpinnet root = new CodeSpinnet();
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
					CodeSpinnet sp = new EmptyCodeSpinnet();
					sp.length = now - last;
					stack[top].add(sp);
				}
				break;
			}
			if(last < now && (text[last] != STBINDOW || text[now] != ENDBINDOW)){
				//last ~ now之间间隔了一段文本，若{}则不是空文本块，若其它情况则是
				CodeSpinnet sp = new EmptyCodeSpinnet();
				sp.length = now - last;
				stack[top].add(sp);
			}
			
			if(text[now] == STBINDOW){
				//遇到前括号，让上一层括号指向它，并将它入栈
				CodeSpinnet sp = new BacketCodeSpinnet();
				stack[top].add(sp);
				stack[++top] = sp;
				sp.length = now; //暂时设为now
			}
			else if(text[now] == ENDBINDOW){
				//最近的前括号已经对应一个最近的后括号，pop
				CodeSpinnet sp = stack[top--];
				sp.length = now - sp.length;
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
	
	public static class CodeSpinnet
	{
		private int start;   //相对于父节点的起始位置
		private int[] rangeSum; //区间和
		private int length;  //代码段长度 
		private int childCount;         //子节点个数
		private CodeSpinnet[] children; //子节点数组
		private CodeSpinnet parent;     //指向父节点
		private Words date;  //代码段单词
		
		CodeSpinnet(){
			childCount = 0;
			children = EmptyArray.emptyArray(CodeSpinnet.class);
		}
		
		private void add(CodeSpinnet sp){
			sp.parent = this;
			children = GrowingArrayUtils.append(children, childCount++, sp);
		}
		
		public CodeSpinnet getParent(){
			return parent;
		}
		public CodeSpinnet getChildAt(int i){
			return children[i];
		}
		public int getChildCount(){
			return childCount;
		}
		public int startOfParent(){
			return start;
		}
		public int length(){
			return length;
		}
		public void setWords(Words w){
			date = w;
		}
		public Words getWords(){
			return date;
		}
	}
	
	private static final class EmptyCodeSpinnet extends CodeSpinnet{}
	
	private static final class BacketCodeSpinnet extends CodeSpinnet{}
	
}
