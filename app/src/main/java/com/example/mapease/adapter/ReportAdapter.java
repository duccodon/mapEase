package com.example.mapease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import com.example.mapease.R;
import com.example.mapease.model.ReportTitle;

public class ReportAdapter extends ArrayAdapter<ReportTitle> {
    Context context;
    private ArrayList<ReportTitle> reportsArrayList;
    public ReportAdapter(Context context, ArrayList<ReportTitle> items) {
        super(context, 0, items);
        this.context = context;
        this.reportsArrayList = items;
    }

    private static class viewHolder {
        TextView title;
        TextView description;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReportTitle reportTitle = getItem(position);
        ReportAdapter.viewHolder myViewHolder;
        final View result;

        if(convertView == null){
            myViewHolder = new ReportAdapter.viewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(
                    R.layout.report_review_item,
                    parent,
                    false
            );
            //myViewHolder.userImage = convertView.findViewById(R.id.imageView);
            myViewHolder.title = convertView.findViewById(R.id.item_title);
            myViewHolder.description = convertView.findViewById(R.id.item_description);
            convertView.setTag(myViewHolder);
        }
        else{
            myViewHolder = (ReportAdapter.viewHolder) convertView.getTag();
        }

        myViewHolder.title.setText(reportTitle.getTitle());
        //myViewHolder.userImage.setImageResource(user.getAvatar());
        myViewHolder.description.setText(reportTitle.getDescription());
        result = convertView;
        return result;
    }
}
