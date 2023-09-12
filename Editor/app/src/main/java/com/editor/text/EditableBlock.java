package com.editor.text;
import android.text.*;

/* 对于不同的文本块，可能有不同的决策，
   EditableList在添加，移除，扩展span时先征得文本块的意见，需要与文本块保持同步
*/
public interface EditableBlock extends Editable
{
	public boolean isInvalidSpan(Object span, int start, int end, int flags)
	
	public boolean canRemoveSpan(Object span, int delstart, int delend, boolean textIsRemoved)
	
	public boolean needExpandSpanStart(Object span, int flags)
	
	public boolean needExpandSpanEnd(Object span, int flags)
	
	public void enforceSetSpan(Object span, int start, int end, int flags)
	
	public<T extends Object> T[] quickGetSpans(int queryStart, int queryEnd, Class<T> kind)
	
	
	/* 创建EditableBlock的工厂 */
	public static class BlockFactory
	{
		private static final BlockFactory mInstance = new BlockFactory();
		
		public static BlockFactory getInstance(){
			return mInstance;
		}
		
		public EditableBlock newEditable(CharSequence source){
			return new SpannableStringBuilderLite(source);
		}
	}
}
