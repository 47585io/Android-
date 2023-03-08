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
import android.text.*;

public class EditGroup extends RelativeLayout
{
	
	//为提升编辑器效率，增加EditGroup
	//编辑器卡顿主要原因是单个编辑器文本过多造成的遍历刷新卡顿
	//解决办法：限制单个编辑器的行，并添加多个编辑器形成编辑器组来均分文本，使效率平衡
	
	public static int WindowHeight=300;
	public static int WindowWidth=600;
	public static int MaxLine=2000;
	public String path;
	private int EditFlag=0;
	private int historyId;
	
	protected ScrollView EditScro;
	protected HorizontalScrollView EdithScro;
	protected LinearLayout ForEdit;
	protected LinearLayout ForEditSon;
	protected Edit EditLines;
	protected ListView mWindow;
	
	private EditBuilder builder;
	private EditFactory2 mfactory;
	private List<CodeEdit> EditList;
	private Stack<Stack<Integer>> Last;
	private Stack<Stack<Integer>> Next;
	private ThreadPoolExecutor pool=null;
	
	public EditGroup(Context cont){
		super(cont);
		init();
		init2(cont);
		config();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( ((EditGroup)obj).path.equals(path))
			return true;
		return false;
	}
	
	protected void init(){
		EditList = new ArrayList<>();
		Last=new Stack<>();
		Next=new Stack<>();
		Last.add(new Stack<Integer>());
		builder=new EditBuilder();
		mfactory=new EditFactory2();
		
	}
	private void init2(Context cont)
	{	
		EditScro=new ScrollView(cont);
		EdithScro=new HorizontalScrollView(cont);
		ForEdit=new LinearLayout(cont);
		ForEditSon=new LinearLayout(cont);
		EditLines=new Edit(cont);
		mWindow=new ListView(cont);

		addView(EdithScro);
		addView(mWindow);
		EdithScro.addView(EditScro);
		EditScro.addView(ForEdit);
		ForEdit.addView(EditLines);
		ForEdit.addView(ForEditSon);
	}
	protected void config(){
		EditLines.setFocusable(false);
		ForEditSon.setOrientation(LinearLayout.VERTICAL);
		mWindow.setBackgroundColor(0xFF1E2126);
		mWindow.setDivider(null);
		mWindow.setOnItemClickListener(new onMyWindowClick());
		mWindow.setOnItemLongClickListener(new onMyWindowLongClick());
		CodeEdit.Enabled_Format = true;
		CodeEdit.Enabled_Drawer = true;
		CodeEdit.Enabled_Complete = true;
		CodeEdit.Enabled_MakeHTML = true;
	}
	
	public EditBuilder getEditBuilder(){
		return builder;
	}
	public List<CodeEdit> getEditList(){
		return EditList;
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
			Edit.lines=EditLines;
			path=name;
		}
		else{
			Edit= new RCodeEdit(getContext(),(EditList.get(0)));	
		}
		Edit.setOnClickListener(new Click());
		return Edit;
	}
	protected void configEdit(CodeEdit Edit,String name)
	{
		Edit.setPool(pool);
		com.mycompany.who.Share.Share.setEdit(Edit,name);
	}
	
	
	
	class RCodeEdit extends CodeEdit{

		public int index;	
		private boolean can;
		//别直接赋值，最后其实会在构造对象时赋值，等同于在构造函数中赋值
		
		public RCodeEdit(Context cont){
			super(cont);
			can=true;
		}
		public RCodeEdit(Context cont,CodeEdit Edit){
			super(cont,Edit);
			can=true;
		}
		
		@Override
		protected void onPutUR(EditDate.Token token){
			Last.peek().push(index);
			//监听器优先调用，所以Last会先开一个空间让我push
			//每一轮次onTextChanged执行后紧跟其后再开一个空间
		}		
		@Override
		public ListView getWindow(){
			return mWindow;
		}
		
		@Override
		protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			if(!can){
				//在构造对象前，会调用一次onTextChanged
				return;
			}
			if(IsModify!=0||IsModify2)
				return ;
			//已经被修改，不允许再修改
		
			EditFlag++;		
			
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
					AddEdit("");
				//若无编辑器，则添加一个
				EditList.get(index+1).getText().insert(0,src);
				//之后将截取的字符添加到编辑器列表中的下个编辑器开头，即MAX行之后的
				//不难看出，这样递归回调，最后会回到第一个编辑器

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
			if(EditFlag==0){
				reLines(builder.calaEditLines());
				Last.push(new Stack<Integer>());
			}
			//最后一个编辑器计算行
			//从第一个调用onTextChanged的编辑器开始，之后的一组的联动修改都存储在同一个Stack
	        //先开辟一个空间，待之后存储
		}

		@Override
	 	public wordIndex calc(CodeEdit Edit)
		{
			//请求测量
			historyId=((RCodeEdit)Edit).index;
			//本次窗口谁请求，单词给谁
			int offset=Edit.getSelectionStart();
			wordIndex pos = Edit.getScrollCursorPos(offset, EdithScro.getScrollX() - Edit.lines.getWidth(), EditScro.getScrollY()-builder.calaEditHeight(index));
			//start真实位置还少一个lines
			//Window必须在re内
			if (pos.start + mWindow.getWidth() > getWidth() || pos.start < 0)
				pos.start = getWidth() - mWindow.getWidth();
			//如果x超出屏幕，总是设置在最右侧

			if (pos.end + mWindow.getHeight() +  Edit.getLineHeight() * 2 > getHeight())
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
	
	class Click implements OnClickListener
	{
		@Override
		public void onClick(View p1)
		{
			historyId=((RCodeEdit) p1).index;
			mWindow.setX(-9999);
		}
	}
	
	public class EditBuilder{
		//通过EditBuilder直接操作Edit
		private wordIndex start;
		private wordIndex end;
		
		public EditBuilder(){
			start=new wordIndex(0,0,(byte)0);
			end=new wordIndex(0,0,(byte)0);
		}
		private void calaIndex(int index){
			//将start转换为 起始编辑器下标+起始位置
			for(CodeEdit e:EditList){
				if(index- e.getText().length()<0){
					start.start=((RCodeEdit)e).index;
					start.end=index;
					return;
				}
				index-=e.getText().length();
				//每次index减当前Edit.len，若长度小于0，则就是当前Edit，index就是start
			}
		}
		private void calaRange(int start,int end){
			//将start～end的范围转换为 起始编辑器下标+起始位置～末尾编辑器下标+末尾位置
			calaIndex(start);
			calaIndex(end);
		}
		
		public wordIndex searchWord(String word){
			for(CodeEdit e:EditList){
				start.end= e.getText().toString().indexOf(word);
				if(start.end!=-1){
					start.start=((RCodeEdit)(e)).index;
					return start;
				}
			}
			return null;
		}
		public void setLuagua(String luagua){
			for(CodeEdit e:EditList)
			    e.setLuagua(luagua);
		}
		public void setRunner(EditListenerRunner Runner){
			for(CodeEdit Edit:EditList)
			    Edit.setRunner(Runner);
		}
		public void setPool(ThreadPoolExecutor pool){
			for(CodeEdit Edit:EditList)
			    Edit.setPool(pool);
		}
		
		public String reDraw(){
			StringBuilder builder=new StringBuilder();
			for(CodeEdit e:EditList){
				String HTML= e.reDraw(0,e.getText().length());
				builder.append(HTML);
			}
			return builder.toString();
		}
		public String reDraw(int start,int end){
			calaRange(start,end);
			final StringBuilder builder=new StringBuilder();
			DoForAnyOnce d= new DoForAnyOnce(){
				
				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
					String HTML= Edit.reDraw(start,end);
					builder.append(HTML);
				}
			};
			d.dofor(this.start,this.end);
			return builder.toString();
		}
		public void Format(){
			Last.add(new Stack<Integer>());
			for(CodeEdit e:EditList){
				e.Format(0,e.getText().length());
				e.reDraw(0,e.getText().length());
			}
		}
		public void Format(int start,int end){
			calaRange(start,end);
			DoForAnyOnce d= new DoForAnyOnce(){

				@Override
				void doOnce(int start, int end, CodeEdit Edit)
				{
					int cale= Edit.Format(start,end);
					Edit.reDraw(start,end+cale);
				}
			};
			d.dofor(this.start,this.end);
		}
		public void Insert(int index){
			calaIndex(index);
			EditList.get(start.start).Insert(start.end);
		}
		
		public void Uedo(){
			//与顺序无关的Uedo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
	        //Bug: 多个编辑器之间会各自管理，因此任何一个的修改可能与另一个无关，造成单次Uedo不同步，但一直Uedo下去，结果是一样的
			if(Last.size()<2)
				return;
			Stack<Integer> last= Last.get(Last.size()-2);
			Last.remove(Last.size()-2);
			Next.push(last);
			for(int l:last)
				EditList.get(l).Uedo();
			EditList.get(0). reLines(calaEditLines());
		}
		public void Redo(){
			//与顺序无关的Redo，它只存储一轮次的修改的编辑器下标，具体顺序由编辑器内部管理
			if(Next.size()<1)
				return;
			Stack<Integer> next= Next.pop();
			Last.push(next);
			for(int l:next)
				EditList.get(l).Redo();
			EditList.get(0).reLines(calaEditLines());
		}
		
		abstract class DoForAnyOnce{
			public void dofor(wordIndex start,wordIndex end){
				doOnce(start.end,EditList.get(start.start).getText().length(),EditList.get(start.start++));
				//第一个编辑器的开头是start.end，结尾是它的长度
				for(;start.start<end.start;start.start++){
					doOnce(0,EditList.get(start.start).getText().length(),EditList.get(start.start));
					//中间编辑器的开头是0,结尾是它的长度
				}
				doOnce(0,end.end,EditList.get(end.start));
				//最后一个编辑器的开头是0，结尾是end.end
			}
			abstract void doOnce(int start,int end,CodeEdit Edit)
		}
		
		public int calaEditHeight(int index){
			int height=0;
			while(--index >-1){
				height+=EditList.get(index).getHeight();
			}
			return height;
		}
		public int calaEditLines(){
			int line=0;
			for(Edit e:EditList)
				line+=e.getLineCount();
			return line;
		}
		
		
	}
	
	
	class onMyWindowClick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			CodeEdit Edit = EditList.get(historyId);
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
			wordIndex node= builder.searchWord(icon.getName());
			if (node!=null)
			{
				CodeEdit Edit = EditList.get( node.start);
				wordIndex pos = Edit.getCursorPos(node.end);
				EdithScro.setScrollX(pos.start);
				EditScro.setScrollY(pos.end+builder.calaEditHeight(((RCodeEdit)(Edit)).index));   	
				Edit.setSelection(node.end, node.end + icon.getName().length());
			}
			return true;
		}
	}
	
	class EditFactory2 extends EditFactory
	{

		@Override
		public CodeEdit GetEdit(Context cont, String Lua, ThreadPoolExecutor pool)
		{
			RCodeEdit Edit = new RCodeEdit(cont);
			Edit.index=EditList.size();
			Edit.setLuagua(Lua);
			Edit.setPool(pool);
			Edit.setRunner(EditRunnerFactory.getCanvasRunner());
			return Edit;
		}

		@Override
		public CodeEdit GetFormEdit(CodeEdit Edit)
		{
			RCodeEdit E=new RCodeEdit(Edit.getContext(),Edit);
			return E;
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
	
	public void setPool(ThreadPoolExecutor pool){
		this.pool=pool;
		builder.setPool(pool);
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
