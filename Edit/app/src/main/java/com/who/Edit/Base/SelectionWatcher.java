package com.who.Edit.Base;

import android.text.*;

public interface SelectionWatcher
{
	public void onSelectionChanged(int start, int end, int oldStart, int oldEnd, Spannable editor)
}
