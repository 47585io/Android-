package com.mycompany.who.Edit.Share.Share2;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.R;
import android.graphics.*;

public class WordAdpter<T extends Icon> extends BaseAdapter
{

    protected List<T> mfile;
	protected int rid;
	protected Context cont;

	public WordAdpter(Context context, List<T> file,int id) {
        mfile = file;
		rid=id;
		cont=context;
    }
	
	public List<T> getList(){
		return mfile;
	}

    @Override
    public int getCount() {
        return mfile == null ? 0 : mfile.size();
    }

    @Override
    public T getItem(int position) {
        return mfile.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        ViewHolder viewHolder = null;
        if (convertView == null) {
			//如果要创建一个新的convertView项，创建并配置tag
            convertView = layoutInflater.inflate(rid, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
			//否则，用Adpter中postion的值更新convertView项tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

		Icon icon = mfile.get(position);	
        viewHolder.tvName.setText(icon.getName());
        if(icon.getPath()==null)
		    viewHolder.tvIcon.setImageResource(icon.getIcon());
		else
		    viewHolder.tvIcon.setImageBitmap(BitmapFactory.decodeFile(icon.getPath()));
	
        return convertView;
    }



    static class ViewHolder {
        protected TextView tvName;
        protected ImageView tvIcon;

        ViewHolder(View rootView) {
            initView(rootView);
        }

        private void initView(View rootView) {
            tvName = (TextView) rootView.findViewById(R.id.Filename);
            tvIcon = (ImageView) rootView.findViewById(R.id.Fileicon);
        }
    }
}

