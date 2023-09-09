package com.editor.text;
import android.text.*;

/* 对于不同的文本块，可能有不同的决策，
   EditableList在添加，移除，扩展span时先征得文本块的意见，需要与文本块保持同步
*/
public interface EditableBlock extends Editable
{
	public boolean isInvalidSpan(Object span, int start, int end, int flags)
	
	public boolean canRemoveSpan(Object span, int flags, boolean textIsRemoved)
	
	public boolean expandByFlags(int flags)
}
