package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

public interface EditFinderListener extends EditListener
{
	public List<wordIndex> LetMeFind(int start, int end,String text,Words WordLib)
}
