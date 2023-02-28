package com.mycompany.who.Edit.DrawerEdit.EditListener;

import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class EditCompletorListener extends EditListener
{
	public abstract void onBeforeSearchWord(ArrayList<Collection<String>> libs1,ArrayList<String[]> libs2);
	public abstract void onFinishSearchWord(ArrayList<ArrayList<String>> words1,ArrayList<ArrayList<String>> words2,ArrayList<Icon> adpter);
}
