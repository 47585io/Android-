package com.mycompany.who.Edit.Share;

import android.content.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.R;

public class WordAdpter<T extends Icon> extends BaseAdapter
{

    protected List<T> mfile;
	protected int rid;

	public WordAdpter(Context context, List<T> file,int id) {
        mfile = file;
		rid=id;
    }


    @Override
    public int getCount() {
        return mfile == null ? 0 : mfile.size();
    }

    @Override
    public Object getItem(int position) {
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

        viewHolder.tvName.setText(mfile.get(position).getName());
        viewHolder.tvIcon.setBackgroundResource(mfile.get(position).getIcon());

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

