package com.mycompany.who.SuperVisor;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.Share.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;
import android.util.*;
import android.content.res.*;
import com.mycompany.who.Activity.*;

public class EditGroup extends LinearLayout
{
	
	//为提升编辑器效率，增加EditGroup
	//编辑器卡顿主要原因是单个编辑器文本过多造成的遍历刷新卡顿
	//解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡
	
	public static int WindowHeight=300;
	public static int WindowWidth=600;
	public static int MaxLine=150;
	private int EditFlag=0;
	
	protected RelativeLayout EditFather;
	protected ScrollView EditScro;
	protected HorizontalScrollView EdithScro;
	protected LinearLayout ForEdit;
	protected LinearLayout ForEditSon;
	protected Edit EditLines;
	protected ListView mWindow;
	
	private ArrayList<CodeEdit> EditList;
	private Stack<Stack<Integer>> Last;
	private Stack<Stack<Integer>> Next;
	private ThreadPoolExecutor pool=null;
	private CodeEdit historyId;
	private ArrayList<Extension> Extensions;
	private KeyPool keyPool;
	private HashMap<String,Runnable> keysRunnar;
	
	public EditGroup(Context cont){
		super(cont);
		init();
		init2(cont);
		CodeEdit.Enabled_Format = true;
		CodeEdit.Enabled_Drawer = true;
		CodeEdit.Enabled_Complete = true;
		CodeEdit.Enabled_MakeHTML = true;
		config();
	}
	
	protected void init(){
		EditList = new ArrayList<>();
		Last=new Stack<>();
		Next=new Stack<>();
		Last.add(new Stack<Integer>());
		Extensions = new ArrayList<>();
		keyPool = new KeyPool();
		keysRunnar = new HashMap<>();
	}
	protected void init2(Context cont)
	{	
		EditFather=new RelativeLayout(cont);
		EditScro=new ScrollView(cont);
		EdithScro=new HorizontalScrollView(cont);
		ForEdit=new LinearLayout(cont);
		ForEditSon=new LinearLayout(cont);
		EditLines=new Edit(cont);
		mWindow=new ListView(cont);
		
		EditFather.addView(EditScro);
		EditFather.addView(mWindow);
		EditScro.addView(EdithScro);
		EdithScro.addView(ForEdit);
		ForEdit.addView(EditLines);
		ForEdit.addView(ForEditSon);
		addView(EditFather);
	}
	protected void config(){
		EditLines.setFocusable(false);
		ForEditSon.setOrientation(LinearLayout.VERTICAL);
		mWindow.setBackgroundColor(0xFF1E2126);
		mWindow.setDivider(null);
		mWindow.setOnItemClickListener(new onMyWindowClick());
		mWindow.setOnItemLongClickListener(new onMyWindowLongClick());
	}
	
	public void AddEdit(String name){
		RCodeEdit Edit= creatAEdit(name);
		Edit.index=EditList.size();
		EditList.add(Edit);
		ForEditSon.addView(Edit);
	}
	
	protected RCodeEdit creatAEdit(String name){
		RCodeEdit Edit;
		if(EditList.size()==0){
	        Edit = new RCodeEdit(getContext());
			configEdit(Edit,name);
		}
		else
			Edit= new RCodeEdit(getContext(),(RCodeEdit)(EditList.get(0)));
		Edit.lines=EditLines;
		return Edit;
	}
	protected void configEdit(CodeEdit Edit,String name)
	{
		Edit.setPool(pool);
		for(Extension e:Extensions){
			e.oninit(Edit);
		    Edit.getFinderList().add(e.getFinder());
			Edit.getDrawerList().add(e.getDrawer());
			Edit.getFormatorList().add(e.getFormator());
			Edit.getInsertorList().add(e.getInsertor());
			Edit.getCompletorList().add(e.getCompletor());
			Edit.getCanvaserList().add(e.getCanvaser());
		}
		com.mycompany.who.Share.Share.setEdit(Edit,name);
	}
	
	
	class RCodeEdit extends CodeEdit{

		public int index;	
		public boolean can;
		//别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数中赋值
		
		public RCodeEdit(Context cont){
			super(cont);
			can=true;
		}
		public RCodeEdit(Context cont,RCodeEdit Edit){
			super(cont,Edit);
			can=true;
		}

		@Override
		public void onPutUR(){
			Last.get(Last.size()-2).push(index);
			//监听器优先调用，所以Last先开一个空间让我push
			//onTextChange紧跟其后再开一个空间
		}		
		@Override
		public ListView getWindow(){
			return mWindow;
		}
		
		@Override
		protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			if(!can){
				//First load，Return
				//在构造对象前，会调用一次onTextChanged
				can=true;
				return;
			}
			if(IsModify!=0||IsModify2)
				return ;
			//已经被修改，不允许再修改
		
			if(EditFlag==0)
				Last.push(new Stack<Integer>());
			EditFlag++;
			//从第一个调用onTextChanged的编辑器开始，之后的一组的联动修改都存储在同一个Stack
	        //先开辟一个空间，待之后存储
			
			int lineCount= getLineCount();
			if(lineCount> MaxLine){
				//在某次插入后，若超出最大的行数，截取之后的部分添加到编辑器列表中的下个编辑器开头	
				wordIndex j = subLines(MaxLine);
				String src = getText().toString().substring(j.start,j.end);
				getText().delete(j.start,j.end);	
				
				if(start+lengthAfter<j.start)
					super.onTextChanged(text, start, lengthBefore, lengthAfter);		
				else{
				    super.onTextChanged(text, start, lengthBefore, j.start-start);	
				}	
				//删除后，只把本次未被截取的前半部分染色，
				//它可能在MAX行之内，即正常染色
				//也可能在MAX行之外，即只染色start～MAX行之间

				if(EditList.size()-1<=index)
					AddEdit(""+EditList.get(index).laugua);
				//若无编辑器，则添加一个
				EditList.get(index+1).getText().insert(0,src);
				//之后将截取的字符添加到编辑器列表中的下个编辑器开头，即MAX行之后的

				if(start+lengthAfter<j.start){
					requestFocus();
					//setSelection(start+lengthAfter);
				}	
				//若本次输入的内容在自己之内，最后requestFocus，否则留着给下个
			}
			else if(start==0&&index>0&&lengthBefore!=0&&lengthAfter==0){
				EditList.get(index-1).requestFocus();
				EditList.get(index-1).setSelection(EditList.get(index-1).getText().length());
				//已经删除到头，切换到上个
			}
			else{
			    super.onTextChanged(text, start, lengthBefore, lengthAfter);
				//否则正常调用
			}
			EditFlag--;
			if(EditFlag==0)
				reLines(calaEditLines());
			//最后一个编辑器计算行
		}

		@Override
	 	public wordIndex calc(CodeEdit Edit)
		{
			//请求测量
			historyId=Edit;
			//本次窗口谁请求，单词给谁
			int offset=Edit.getSelectionStart();
			wordIndex pos = Edit.getScrollCursorPos(offset, EdithScro.getScrollX() - Edit.lines.getWidth(), EditScro.getScrollY()-calaEditHeight(index));
			//start真实位置还少一个lines
			//Window必须在re内
			if (pos.start + mWindow.getWidth() > EditFather.getWidth() || pos.start < 0)
				pos.start = EditFather. getWidth() - mWindow.getWidth();
			//如果x超出屏幕，总是设置在最右侧

			if (pos.end + mWindow.getHeight() +  Edit.getLineHeight() * 2 > EditFather.getHeight())
				pos.end = pos.end - mWindow.getHeight() - Edit.getLineHeight();
			//如果y超出屏幕，将其调整为光标之前，否则调整在光标后
			else
				pos.end = pos.end + Edit.getLineHeight();

			//测量并修改Window大小
			int height = MeasureWindowHeight();
			MarginLayoutParams prams = (ViewGroup.MarginLayoutParams) mWindow.getLayoutParams();
			if (height < WindowHeight)
				prams.height = height;
			else
				prams.height = WindowHeight;
			prams.width = WindowWidth;
			
			mWindow.setLayoutParams(prams);

			return pos;
		}
	}
	
	public void reDraw(){
		for(CodeEdit e:EditList){
			e.reDraw(0,e.getText().length());
		}
	}
	public void Format(){
		Last.add(new Stack<Integer>());
		for(CodeEdit e:EditList)
		    e.startFormat(0,e.getText().length());
	}
	
	public void Uedo(){
		Stack<Integer> last= Last.get(Last.size()-2);
		Last.remove(Last.size()-2);
		Next.push(last);
		for(int l:last)
		    EditList.get(l).Uedo();
		EditList.get(0). reLines(calaEditLines());
	}
	public void Redo(){
		Stack<Integer> next= Next.pop();
		Last.push(next);
		for(int l:next)
		    EditList.get(l).Redo();
		EditList.get(0).reLines(calaEditLines());
	}

	
	public ArrayList<CodeEdit> getEditList(){
		return EditList;
	}
	
	class onMyWindowClick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			CodeEdit Edit = historyId;
			Edit.insertWord(icon.getName(), Edit.getSelectionStart(), icon.getflag());
			mWindow.setX(-9999);
			mWindow.setY(-9999);
		}
	}
	class onMyWindowLongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果长按了就去到单词第一次出现的地方
			Icon icon = (Icon) p1.getAdapter().getItem(p3);
			CodeEdit Edit = historyId;
			int index= Edit.getText().toString().indexOf(icon.getName());
			if (index != -1)
			{
				wordIndex pos = Edit.getCursorPos(index);
				EdithScro.setScrollX(pos.start);
				EditScro.setScrollY(pos.end);   	
				Edit.setSelection(index, index + icon.getName().length());
			}
			return true;
		}
	}

	private int MeasureWindowHeight()
	{
		int height=0;
		int i;
		WordAdpter adapter= (WordAdpter) mWindow.getAdapter();
		for (i = 0;i < adapter.getCount();i++)
		{
			View view = adapter.getView(i, null, mWindow);
			view.measure(0, 0);
			height += view.getMeasuredHeight();
			//若View没有明确设定width和height时，它的大小为0
			//可以measure方法测量它的大小，这样测量的大小会被保存，然后获取测量的高
			//注意，getWidth不等于getMeasuredHeight
		}

		return height;
	}
	private int calaEditHeight(int index){
		int height=0;
		while(--index >-1){
			height+=EditList.get(index).getHeight();
		}
		return height;
	}
	private int calaEditLines(){
		int line=0;
		for(Edit e:EditList)
		    line+=e.getLineCount();
		return line;
	}

	public void setPool(ThreadPoolExecutor pool){
		this.pool=pool;
	}
	
	
	
	public void addAExtension(Extension extension)
	{
		Extensions.add(extension);
	}
	public void delAExtension(int i)
	{
		Extensions.remove(i);
	}
	public void clearExtension(){
		Extensions.clear();
	}
	public void setExtension(ArrayList<Extension> E){
		Extensions=E;
	}

	public static abstract class Extension
	{
		public String name;
		public String path;
		public int id;
		public abstract void oninit(EditText self)
		public abstract EditListener getFinder()
		public abstract EditListener getDrawer()
		public abstract EditListener getFormator()
		public abstract EditListener getInsertor()
	  	public abstract EditListener getCompletor()
		public abstract EditListener getCanvaser()
	}

	@Override
	protected void onConfigurationChanged(Configuration config)
	{
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
		    WindowHeight=600;
		    WindowWidth=600;
		}
		else if(config.orientation == Configuration.ORIENTATION_LANDSCAPE){
			WindowHeight=300;
		    WindowWidth=900;
		}
		
		super.onConfigurationChanged(config);
	}
	
	
	
}
