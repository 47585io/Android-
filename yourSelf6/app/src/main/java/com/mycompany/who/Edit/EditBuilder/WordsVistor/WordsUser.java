package com.mycompany.who.Edit.EditBuilder.WordsVistor;

/* 
 如果你是Words的拥有者，

 需要管理内部的单词，并共享它们
*/
public interface WordsUser
{
	public Words getWordLib()
	
	public void setWordLib(Words Lib)
	
	public void loadWords()
	
	public void clearWords()
}
