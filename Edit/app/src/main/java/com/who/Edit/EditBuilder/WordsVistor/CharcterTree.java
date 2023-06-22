package com.who.Edit.EditBuilder.WordsVistor;
import java.util.*;

public class CharcterTree
{
	
	CharNode root;
	
	public CharcterTree(){
		root = new CharNode(' ');
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
			return node;
		}
		
		char c; //本节点的字符
		Collection<Character> chars; //子节点的字符集合，用于快速判断子节点是否有指定的字符
		Collection<CharNode> charNodes; //子节点的集合，用于前往下一层字符
	}
}
