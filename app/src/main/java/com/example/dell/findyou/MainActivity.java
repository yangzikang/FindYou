package com.example.dell.findyou;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.onClick;

public class MainActivity extends Activity {
    @BindView(R.id.enter) Button enter;
    @OnClick(R.id.enter)
    void enter() {
        Intent intent = new Intent(MainActivity.this,LocationActivity.class);
        startActivity(intent);
    }
    @OnClick(R.id.connectInternet)
    void connectInterenet(){
         EventBus.getDefault().post(new MessageEvent());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){
        Intent intent = new Intent(MainActivity.this,HttpActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }
    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
