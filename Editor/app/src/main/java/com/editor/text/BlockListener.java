package com.editor.text;

public interface BlockListener
{
	public void onAddBlocks(int i, int count)

	public void onRemoveBlocks(int i, int j)

	public void onBlocksDeleteBefore(int i, int j, int iStart, int jEnd)

	public void onBlocksDeleteAfter(int i, int j, int iStart, int jEnd)

	public void onBlocksInsertAfter(int i, int j, int iStart, int jEnd)

	public void afterBlocksChanged(int i, int iStart)
}
