package net.anigoo.ladies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import net.anigoo.ladies.model.Activities;
import net.anigoo.ladies.model.Activity;

public class MyActivityAdapter extends ArrayAdapter<Activity>{
    Activities mActivities;
    Context mContext;

    public MyActivityAdapter(Context context, Activities activities){
        super(context, R.layout.recently_activity_item);
        this.mActivities = activities;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mActivities.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.recently_activity_item, parent, false);
            mViewHolder.mImage = (ImageView) convertView.findViewById(R.id.product_image);
            mViewHolder.mTitle = (TextView) convertView.findViewById(R.id.product_title);
            mViewHolder.mDescription = (TextView) convertView.findViewById(R.id.activity_message);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
		Picasso.with(mContext).load(mActivities.get(position).product.image).into(mViewHolder.mImage);

        String productName = mActivities.get(position).product.name;
        if(productName.length() > 40) productName = productName.substring(0, 40) + "...";
        mViewHolder.mTitle.setText(productName);
        mViewHolder.mDescription.setText(mActivities.get(position).getMessage());

        return convertView;
    }
    static class ViewHolder{
        ImageView mImage;
        TextView mTitle;
        TextView mDescription;
    }

}














