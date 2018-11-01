package com.bannerdot;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dotlibrary.DotIndicator;

public class Main2Activity extends AppCompatActivity {
    private int[] resource={R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d,R.drawable.e};
    private DotIndicator mDot2;
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mRecyclerView=findViewById(R.id.rv);
        mDot2=findViewById(R.id.dot);
        mRecyclerView.setAdapter(new Adapter(this,resource));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        mDot2.attachToRecyclerView(mRecyclerView);

    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
    static class Adapter extends RecyclerView.Adapter<ViewHolder>{
        private Context mContext;
        private int[]imgId;

        public Adapter(Context context, int[] imgId) {
            mContext = context;
            this.imgId = imgId;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
          View view  =LayoutInflater.from(mContext).inflate(R.layout.layout_rv_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.itemView.setBackgroundResource(imgId[position]);
        }

        @Override
        public int getItemCount() {
            return imgId==null?0:imgId.length;
        }
    }

}
