package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import java.util.*;

public abstract class EditFinderListener extends EditListener
{
	abstract public void OnFindWord(ArrayList<CodeEdit.DoAnyThing> totalList);
	abstract public void OnDrawWord(ArrayList<CodeEdit.DoAnyThing> totalList);
	abstract public void OnClearFindWord(Words words,OtherWords twords);
	abstract public void OnClearDrawWord(int start,int end,String text,ArrayList<wordIndex> nodes);
}
