package com.example.mapease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mapease.R;

import java.util.ArrayList;
import com.example.mapease.model.User;

public class UserAdapter extends ArrayAdapter<User> {
    Context context;
    private ArrayList<User> usersArrayList;


    public UserAdapter(Context context, ArrayList<User> usersArrayList) {
        super(context, R.layout.admin_user_list_item, usersArrayList);
        this.context = context;
        this.usersArrayList = usersArrayList;
    }

    private static class viewHolder {
        ImageView userImage;
        TextView userName;
        TextView userRole;
        TextView userBio;
        TextView userEmail;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        viewHolder myViewHolder;
        final View result;

        if(convertView == null){
            myViewHolder = new viewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(
                    R.layout.admin_user_list_item,
                    parent,
                    false
            );
            //myViewHolder.userImage = convertView.findViewById(R.id.imageView);
            myViewHolder.userName = convertView.findViewById(R.id.userName);
            myViewHolder.userBio = convertView.findViewById(R.id.userBio);
            myViewHolder.userRole = convertView.findViewById(R.id.userRole);
            myViewHolder.userEmail = convertView.findViewById(R.id.userEmail);
            convertView.setTag(myViewHolder);
        }
        else{
            myViewHolder = (viewHolder) convertView.getTag();
        }

        myViewHolder.userName.setText(user.getUsername());
        //myViewHolder.userImage.setImageResource(user.getAvatar());
        myViewHolder.userBio.setText(user.getBio());
        myViewHolder.userRole.setText(user.getRole());
        myViewHolder.userEmail.setText(user.getEmail());
        result = convertView;
        return result;
    }
}
