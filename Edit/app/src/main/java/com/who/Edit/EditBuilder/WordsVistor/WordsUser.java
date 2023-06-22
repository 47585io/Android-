package com.who.Edit.EditBuilder.WordsVistor;

/* 
 如果你是Words的拥有者，

 需要管理内部的单词，并共享它们
 
*/
public abstract interface WordsUser
{
	public abstract Words getWordLib()
	
	public abstract void setWordLib(Words Lib)
	
	public abstract void loadWords()
	
	public abstract void clearWords()
}
