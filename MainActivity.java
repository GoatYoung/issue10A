package com.young.project02;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.young.project02.pojo.Student;
import com.young.project02.service.NetStatusService;
import com.young.project02.thread.DelThread;
import com.young.project02.thread.SearchThreadByCon;
import com.young.project02.utils.MyToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Integer[] icons = {R.drawable.icon1,R.drawable.icon2,R.drawable.icon3,R.drawable.icon4,R.drawable.icon5,R.drawable.icon6};
    private  ArrayList<Student> students;
    private int curSeletedStuIndex;
    private ListView stuList;
    private Context context;
    private SimpleAdapter adapter;
    private TextView netStatusView;
    private List<Map<String,Object>> listItems;
    private GestureDetector detector;
    private Bundle bundle;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetStatusService.MyBinder binder = (NetStatusService.MyBinder) service;
            NetStatusService netStatusService = binder.getService();
            netStatusService.setCallback(new NetStatusService.Callback() {
                @Override
                public void onNetworkChange(boolean isConn) {
                    Message msg = new Message();
                    msg.obj = isConn;
                    handler.sendMessage(msg);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if((boolean)msg.obj){
                netStatusView.setVisibility(View.GONE);
                setTitle("学生信息管理系统");
            }else{
                netStatusView.setVisibility(View.VISIBLE);
                setTitle("【网络已断开】");
            }
        }
    };


    @Override
    protected void onNewIntent(Intent intent) {
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
        super.onNewIntent(intent);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if((e1.getX()-e2.getX())>50){
                //左滑
                Intent intent = new Intent(MainActivity.this, ActivityStudent.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                MainActivity.this.startActivityForResult(intent,0x11);
                //设置切换动画
                overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(this, NetStatusService.class);
        bindService(i,conn,BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(conn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        stuList = (ListView) findViewById(R.id.StudentInfoList);
        //实例化手势监视器
        detector = new GestureDetector(this,new MyGestureListener());
        //添加上下文菜单
        registerForContextMenu(stuList);

        //根据接收到的message设置网络textview
        netStatusView = (TextView)findViewById(R.id.netStatusTextView);
        //接收传输过来的students
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        students = bundle.getParcelableArrayList("students");
        System.out.println("students:"+students);
        if(students.isEmpty()){
            new MyToast(this,"查询结果为空，请重新查询",getResources().getDrawable(android.R.drawable.ic_dialog_info),Toast.LENGTH_LONG).show();
        }
//        添加一个列表头
        listItems = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("照片",icons[students.get(i).getIcon()]);
            map.put("名字",students.get(i).getName());
            map.put("信息",students.get(i).getInfo());
            System.out.println(students.get(i).getInfo());
            listItems.add(map);
        }
        adapter = new SimpleAdapter(this,listItems,R.layout.item,new String[]{"名字","照片","信息"},new int[]{R.id.name,R.id.imageView,R.id.info});
        stuList.setAdapter(adapter);


        Button searchbtn = (Button)findViewById(R.id.searchbtn);
        //alertdialog中的编辑框
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建AlertDialog
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View myDialog =layoutInflater.inflate(R.layout.dialog,(ViewGroup)findViewById(R.id.mdialog));
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setNegativeButton("取消",null).create();
                alertDialog.setIcon(android.R.drawable.ic_menu_search);
                alertDialog.setTitle("查询学生");
                alertDialog.setView(myDialog);




                //关联对话框中的spinner
                EditText nameEdit = (EditText) myDialog.findViewById(R.id.searchNameEdit);
                Spinner mdeptSpinner = (Spinner)myDialog.findViewById(R.id.mdeptSpinner);
                Spinner mmajorSpinner = (Spinner)myDialog.findViewById(R.id.mmajorSpinner);
                mdeptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String result = parent.getItemAtPosition(position).toString();
                        if(result.equals("计算机学院")){
                            ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(MainActivity.this,  R.array.cs,android.R.layout.simple_spinner_item);
                            mmajorSpinner.setAdapter(adapter);
                        }else{
                            ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(MainActivity.this,  R.array.ee,android.R.layout.simple_spinner_item);
                            mmajorSpinner.setAdapter(adapter);
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });


                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //搜索
                        String stuName = nameEdit.getText().toString();
                        System.out.println(stuName);
                        String stuDept = mdeptSpinner.getSelectedItem().toString();
                        String stuMajor = mmajorSpinner.getSelectedItem().toString();
                        if(stuName.equals("")){
                            stuName = null;
                        }
                        if(stuDept.equals("请选择学院")){
                            stuDept=null;
                        }
                        if(stuMajor.equals("请选择专业")){
                            stuMajor=null;
                        }
                        new Thread(new SearchThreadByCon(MainActivity.this,stuName,stuDept,stuMajor)).start();
                    }
                });
                alertDialog.show();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data==null){
            return;
        }
        bundle = data.getExtras();
        if(requestCode == 0x11 && resultCode == 0x11){
            if(!bundle.isEmpty())
            {
                Student student = bundle.getParcelable("student");
                students.add(student);
                Map<String,Object> map = new HashMap<>();
                map.put("照片",icons[student.getIcon()]);
                map.put("名字",student.getName());
                map.put("信息",student.getInfo());
                listItems.add(map);
                adapter.notifyDataSetChanged();
            }

        }else if(requestCode == 0x12 && resultCode == 0x12){
            Bundle bundle = data.getExtras();
            Student student = bundle.getParcelable("student");
            listItems.remove(curSeletedStuIndex);
            Map<String,Object> map = new HashMap<>();
            map.put("照片",icons[student.getIcon()]);
            map.put("名字",student.getName());
            map.put("信息",student.getInfo());
            listItems.add(curSeletedStuIndex,map);
            students.add(curSeletedStuIndex,student);
            students.remove(curSeletedStuIndex+1);
            adapter.notifyDataSetChanged();
        }
    }
    //重写上下文菜单方法
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //通过反射加载
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu,menu);
        //获得被点击的学生的位置
        curSeletedStuIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
//        Map<String,Object> map = (Map<String,Object>)stuList.getItemAtPosition(index);
//        curSeletedStuID = String.valueOf(map.get("信息")).split(",")[0].split("：")[1];
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.edit:
                /*传输被选中的对象*/
                Bundle bundle =new Bundle();
                bundle.putParcelable("student",students.get(curSeletedStuIndex));
                Intent intent = new Intent(this,ActivityStudent.class);
                intent.putExtras(bundle);
                startActivityForResult(intent,0x12);
                overridePendingTransition(R.anim.activity_open,R.anim.activity_stay);
                break;
            case R.id.delete:
                showConfirmDialog();
                break;
        }
        return super.onContextItemSelected(item);
    }
//重写菜单方法

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.query:
                Intent intent2 = new Intent(MainActivity.this,ActivityPhonePlace.class);
                startActivity(intent2);
                break;
            case R.id.add:
                Intent intent = new Intent(MainActivity.this,ActivityStudent.class);
                startActivityForResult(intent,0x11);
                break;
            case R.id.refresh:
                Student student = bundle.getParcelable("student");
                students.add(student);
                Map<String,Object> map = new HashMap<>();
                map.put("照片",icons[student.getIcon()]);
                map.put("名字",student.getName());
                map.put("信息",student.getInfo());
                listItems.add(map);
                adapter.notifyDataSetChanged();
                Toast.makeText(this,"刷新",Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Intent i = new Intent(MainActivity.this,ActivityConfig.class);
                startActivity(i);
                break;
            case R.id.weekday:
                Intent i2 = new Intent(MainActivity.this,WeekdayActivity.class);
                startActivity(i2);

        }
        return super.onOptionsItemSelected(item);
    }

    private void showConfirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("是否删除该学生？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //从数据库中删除
                Thread t = new Thread(new DelThread(context,students.get(curSeletedStuIndex)));
                t.start();
                //从列表中删除
                students.remove(curSeletedStuIndex);
                listItems.remove(curSeletedStuIndex);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}