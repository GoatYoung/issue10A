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
                setTitle("????????????????????????");
            }else{
                netStatusView.setVisibility(View.VISIBLE);
                setTitle("?????????????????????");
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
                //??????
                Intent intent = new Intent(MainActivity.this, ActivityStudent.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                MainActivity.this.startActivityForResult(intent,0x11);
                //??????????????????
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
        //????????????????????????
        detector = new GestureDetector(this,new MyGestureListener());
        //?????????????????????
        registerForContextMenu(stuList);

        //??????????????????message????????????textview
        netStatusView = (TextView)findViewById(R.id.netStatusTextView);
        //?????????????????????students
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        students = bundle.getParcelableArrayList("students");
        System.out.println("students:"+students);
        if(students.isEmpty()){
            new MyToast(this,"????????????????????????????????????",getResources().getDrawable(android.R.drawable.ic_dialog_info),Toast.LENGTH_LONG).show();
        }
//        ?????????????????????
        listItems = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("??????",icons[students.get(i).getIcon()]);
            map.put("??????",students.get(i).getName());
            map.put("??????",students.get(i).getInfo());
            System.out.println(students.get(i).getInfo());
            listItems.add(map);
        }
        adapter = new SimpleAdapter(this,listItems,R.layout.item,new String[]{"??????","??????","??????"},new int[]{R.id.name,R.id.imageView,R.id.info});
        stuList.setAdapter(adapter);


        Button searchbtn = (Button)findViewById(R.id.searchbtn);
        //alertdialog???????????????
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //??????AlertDialog
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View myDialog =layoutInflater.inflate(R.layout.dialog,(ViewGroup)findViewById(R.id.mdialog));
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setNegativeButton("??????",null).create();
                alertDialog.setIcon(android.R.drawable.ic_menu_search);
                alertDialog.setTitle("????????????");
                alertDialog.setView(myDialog);




                //?????????????????????spinner
                EditText nameEdit = (EditText) myDialog.findViewById(R.id.searchNameEdit);
                Spinner mdeptSpinner = (Spinner)myDialog.findViewById(R.id.mdeptSpinner);
                Spinner mmajorSpinner = (Spinner)myDialog.findViewById(R.id.mmajorSpinner);
                mdeptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String result = parent.getItemAtPosition(position).toString();
                        if(result.equals("???????????????")){
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


                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //??????
                        String stuName = nameEdit.getText().toString();
                        System.out.println(stuName);
                        String stuDept = mdeptSpinner.getSelectedItem().toString();
                        String stuMajor = mmajorSpinner.getSelectedItem().toString();
                        if(stuName.equals("")){
                            stuName = null;
                        }
                        if(stuDept.equals("???????????????")){
                            stuDept=null;
                        }
                        if(stuMajor.equals("???????????????")){
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
                map.put("??????",icons[student.getIcon()]);
                map.put("??????",student.getName());
                map.put("??????",student.getInfo());
                listItems.add(map);
                adapter.notifyDataSetChanged();
            }

        }else if(requestCode == 0x12 && resultCode == 0x12){
            Bundle bundle = data.getExtras();
            Student student = bundle.getParcelable("student");
            listItems.remove(curSeletedStuIndex);
            Map<String,Object> map = new HashMap<>();
            map.put("??????",icons[student.getIcon()]);
            map.put("??????",student.getName());
            map.put("??????",student.getInfo());
            listItems.add(curSeletedStuIndex,map);
            students.add(curSeletedStuIndex,student);
            students.remove(curSeletedStuIndex+1);
            adapter.notifyDataSetChanged();
        }
    }
    //???????????????????????????
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //??????????????????
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu,menu);
        //?????????????????????????????????
        curSeletedStuIndex = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
//        Map<String,Object> map = (Map<String,Object>)stuList.getItemAtPosition(index);
//        curSeletedStuID = String.valueOf(map.get("??????")).split(",")[0].split("???")[1];
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.edit:
                /*????????????????????????*/
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
//??????????????????

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
                map.put("??????",icons[student.getIcon()]);
                map.put("??????",student.getName());
                map.put("??????",student.getInfo());
                listItems.add(map);
                adapter.notifyDataSetChanged();
                Toast.makeText(this,"??????",Toast.LENGTH_SHORT).show();
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
        builder.setTitle("??????");
        builder.setMessage("????????????????????????");
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //?????????????????????
                Thread t = new Thread(new DelThread(context,students.get(curSeletedStuIndex)));
                t.start();
                //??????????????????
                students.remove(curSeletedStuIndex);
                listItems.remove(curSeletedStuIndex);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
}