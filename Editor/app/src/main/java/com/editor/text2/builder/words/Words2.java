package com.editor.text2.builder.words;

public interface Words2
{
	
	public Word findWordByName()
	
	public Word findWordByType()
	
	public void addFunction()
	
	public void addClass()
	
	public void addField()
	
	public static interface ClassWords
	{
	}
	
	public static interface FuncWords
	{
		public void addField()
		
		public void removeField()
	}
	
	public static interface Word
	{
		public String getName()
		
		public String getType()
		
		public String getDate()
	}
	
}
