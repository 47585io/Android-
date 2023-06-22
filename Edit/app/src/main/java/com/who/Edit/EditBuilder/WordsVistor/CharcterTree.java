package com.who.Edit.EditBuilder.WordsVistor;
import java.util.*;

public class CharcterTree implements Collection<CharSequence>
{

	CharNode root;
	List<CharNode> nodes;
	
	public CharcterTree(){
		init();
	}
	public void init(){
		root = new CharNode(' ');
		nodes = new LinkedList<>();
		nodes.add(root);
	}
	
	public CharNode insertNext(CharNode node, CharSequence text, int index)
	{
		int len = text.length();
		if(index>len-1){
			//在上层中，我被调到，但我已经是最后一个字符了
			return node;
		}
		
		char c = text.charAt(index);
		CharNode n = node.findNext(c);
		
		if(n==null){
			//下层没有这个字符，新建一个分支
			n = node.addNext(c);
			nodes.add(n);
		}
		
		//并继续向下层传递
		return insertNext(n,text,index+1);
	}
	
	public CharNode findNext(CharNode node, CharSequence text, int index)
	{
		int len = text.length();
		if(index>len-1){
			//在上层中，我被调到，但我已经是最后一个字符了
			return node;
		}
		
		char c = text.charAt(index);
		CharNode n = node.findNext(c);
		
		if(n==null){
			//如果下层找不到这个字符，我们的匹配截止于node
			return node;
		}
		return findNext(n,text,index+1);
	}
	
	public void foreachBefore(CharNode node,StringBuilder last,Collection<CharSequence> coll)
	{
		if(node.parent==null){
			return;
		}
		last.insert(last.length(),String.valueOf(node.c));
	}
	
	public void foreachAfter(CharNode node,StringBuilder last,Collection<CharSequence> coll)
	{
		last.append(node.c);
		coll.add(last.toString());
		for(CharNode n:node.charNodes)
		{
			foreachAfter(n,last,coll);
			last.delete(last.length()-1,last.length());
		}
	}
	
	/* 字符节点 */
	private static class CharNode
	{
		public CharNode(char c)
		{
			this. c = c;
			chars = new HashSet<>();
			charNodes = new LinkedList<>();
		}
		
		/* 在下层中寻找一个字符 */
		public CharNode findNext(char c)
		{
			if(chars.contains(c))
			{
				for(CharNode node:charNodes)
				{
					if(node.c==c){
						return node;
					}
				}
			}
			return null;
		}
		
		/* 向下层添加一个字符 */
		public CharNode addNext(char c)
		{
			CharNode node = new CharNode(c);
			chars.add(c);
			charNodes.add(node);
			node.parent = this;
			return node;
		}
		
		char c; //本节点的字符
		CharNode parent; //父节点，用于返回遍历
		Collection<Character> chars; //子节点的字符集合，用于快速判断子节点是否有指定的字符
		Collection<CharNode> charNodes; //子节点的集合，用于前往下一层字符
	}
	
	
	@Override
	public int size(){
		return nodes.size();
	}
	@Override
	public boolean isEmpty(){
		return nodes.isEmpty();
	}
	@Override
	public boolean contains(Object p1){
		return findNext(root,(CharSequence)p1,0)!=null;
	}
	@Override
	public Iterator iterator(){
		return null;
	}
	
	@Override
	public CharSequence[] toArray()
	{
		Collection<CharSequence> coll = new LinkedList<>();
		foreachAfter(root,new StringBuilder(),coll);
		CharSequence[] arr = new CharSequence[coll.size()];
		return coll.toArray(arr);
	}
	@Override
	public <T extends Object> T[] toArray(T[] p1){
		return null;
	}
	
	@Override
	public boolean add(CharSequence p1)
	{
		insertNext(root,(CharSequence)p1,0);
		return true;
	}
	@Override
	public boolean remove(Object p1){
		return false;
	}
	@Override
	public boolean containsAll(Collection p1){
		return false;
	}
	@Override
	public boolean addAll(Collection p1){
		return false;
	}
	@Override
	public boolean removeAll(Collection p1){
		return false;
	}
	@Override
	public boolean retainAll(Collection p1){
		return false;
	}
	@Override
	public void clear(){
		init();
	}
	
}
