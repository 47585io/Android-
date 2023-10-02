package com.editor.text;

public interface BlockListener
{
	public void onAddBlock(int i)

	public void onRemoveBlock(int i)

	public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)

	public void onBlocksDeleteAfter(int i, int j, int iStart, int jEnd)

	public void onBlocksInsertAfter(int i, int j, int iStart, int jEnd)

	public void afterBlocksChanged(int i, int iStart)
	
	public void clearBlocks()
}
