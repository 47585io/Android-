package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

public abstract interface EditFinderListener extends EditListener
{
	public abstract List<wordIndex> onFindNodes(int start, int end,String text,Words WordLib)
}
