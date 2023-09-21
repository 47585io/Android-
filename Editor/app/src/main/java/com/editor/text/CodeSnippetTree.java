package com.editor.text;


/* 计划将文本分为代码块，每一个代码块都是一个节点，以树状嵌套
   这样便于查找成对括号，并且可以给每一个代码块设置一个单词库的span
   插入文本时，解析其中的代码块，构建代码块对象，寻找处于哪个代码块的位置，并插入到指定位置
   但是不会写呜呜呜
*/
public class CodeSnippetTree
{	
	private CodeSnippet[] mCodeSnippets;
	private int mCodeSnippetCount;
	
	public CodeSnippetTree(){

	}
	public CodeSnippetTree(CharSequence text){

	}
	public CodeSnippetTree(CharSequence text, int start, int end){

	}
	public CodeSnippetTree(CodeSnippetTree tree){
		
	}
	
	//为节点i添加一个兄弟代码块
	private void addBrother(CharSequence text, int i)
	{
		CodeSnippet self = new CodeSnippet(text);
		mCodeSnippets[mCodeSnippetCount] = self;
    	CodeSnippet sp = mCodeSnippets[i]; 
		int next = sp.nextBrother;
		self.nextBrother = next;
		sp.nextBrother = mCodeSnippetCount;
		mCodeSnippetCount++;
	}
	//为节点i添加一个孩子代码块
	private void addChild(CharSequence text, int i)
	{
		CodeSnippet self = new CodeSnippet(text);
		mCodeSnippets[mCodeSnippetCount] = self;
    	CodeSnippet sp = mCodeSnippets[i]; 
		int child = sp.firstChild;
		self.nextBrother = child;
		sp.firstChild = mCodeSnippetCount;
		mCodeSnippetCount++;
	}
	
	//寻找插入下标所处的代码块下标
	private int findSnippetIdByIndex(int index){
		return 0;
	}
	private int treeRoot(){
		return 0;
	}
	
	public static CodeSnippetTree decodeSnippet(CharSequence text){
		return new CodeSnippetTree(text);
	}
	
	private static class CodeSnippet
	{
		CodeSnippet(CharSequence text){
			this.text=text;
		}
		
		CharSequence text;
		int firstChild;
		int nextBrother;
		int lastParent;
	}
	
	
}
