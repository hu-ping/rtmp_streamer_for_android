package com.ksy.ksyrecordsdk;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.heinrichreimersoftware.materialdrawer.DrawerView;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.ksy.ksyrecordsdk.com.ksy.ksyrecordsdk.config.DrawerItemConfigAdapter;
import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.SMRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
import com.ksy.recordlib.service.exception.KsyRecordException;
import com.ksy.recordlib.service.magicfilter.MagicEngine;
import com.ksy.recordlib.service.magicfilter.utils.MagicParams;
import com.ksy.recordlib.service.magicfilter.widget.MagicCameraView;
import com.ksy.recordlib.service.util.Constants;
import com.ksy.recordlib.service.util.OnClientErrorListener;
import com.ksy.recordlib.service.util.OrientationActivity;
import com.ksy.recordlib.service.util.OrientationObserver;
import com.ksy.recordlib.service.util.RtmfpCrashHandler;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OrientationActivity, KsyRecordClient.NetworkChangeListener, KsyRecordClient.PushStreamStateListener, KsyRecordClient.SwitchCameraStateListener, KsyRecordClient.StartListener, OnClientErrorListener {

    private static final boolean DEBUG = true;

    private TextureView mSurfaceView;
//    private SurfaceView mSurfaceView;

    private FloatingActionButton mFab, change, flashlight;
    private boolean mRecording = false;
    private KsyRecordClient client;                        //core
    private SMRecordClientConfig config;
    private RelativeLayout mContainer;
    private ImageView mImageView;
    private TextView bitrate;
    private DrawerItemConfigAdapter adapter;
    private DrawerView drawer;
    private ActionBarDrawerToggle drawerToggle;

    private int orientation = 0;
    private OrientationObserver orientationObserver;
    private boolean isFirstEnter = true;
    private boolean flash = true;

    private MagicEngine magicEngine;
    private LinearLayout mFilterLayout;
    private RecyclerView mFilterListView;
//    private FilterAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);

        RtmfpCrashHandler crashHandler = RtmfpCrashHandler.getInstance();
        crashHandler.init(getApplicationContext()); //在Appliction里面设置我们的异常处理器为UncaughtExceptionHandler处理器

        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder
                .build((MagicCameraView)findViewById(R.id.glsurfaceview_camera));

        initWidget();
        setupRecord();
        initOrientationSensor();
    }

    public void onResume() {
        super.onResume();
        if (orientationObserver.canDetectOrientation()) {
            orientationObserver.enable();
        }
        client.registerNetworkMonitor();
    }

    private void initWidget() {
        mContainer = (RelativeLayout) findViewById(R.id.container); //容器
        bitrate = (TextView) findViewById(R.id.bitrate);
        mImageView = new ImageView(MainActivity.this);
        mImageView.setBackgroundColor(0xff000000);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecord();
            }
        });
        change = (FloatingActionButton) findViewById(R.id.change);
        flashlight = (FloatingActionButton) findViewById(R.id.flash);
        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFlash();
            }
        });
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera();
            }
        });
        initSurfaceView();
        setUpEnvironment();
//        initDrawer();
        startBitrateTimer();

        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        MagicCameraView cameraView = (MagicCameraView)findViewById(R.id.glsurfaceview_camera);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cameraView.getLayoutParams();
        params.width = screenSize.x;
        params.height = screenSize.x * 4 / 3;
        cameraView.setLayoutParams(params);

//        mFilterLayout = (LinearLayout)findViewById(R.id.layout_filter);
//        mFilterListView = (RecyclerView) findViewById(R.id.filter_listView);
        findViewById(R.id.btn_camera_beauty).setOnClickListener(btn_listener);
        findViewById(R.id.btn_camera_filter).setOnClickListener(btn_listener);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        mFilterListView.setLayoutManager(linearLayoutManager);
//
//        mAdapter = new FilterAdapter(this, types);
//        mFilterListView.setAdapter(mAdapter);
//        mAdapter.setOnFilterChangeListener(onFilterChangeListener);
//
//        animator = ObjectAnimator.ofFloat(btn_shutter,"rotation",0,360);
//        animator.setDuration(500);
//        animator.setRepeatCount(ValueAnimator.INFINITE);
    }

    private View.OnClickListener btn_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_camera_filter:
                    showFilters();
                    break;

                case R.id.btn_camera_beauty:
                    new AlertDialog.Builder(MainActivity.this)
                            .setSingleChoiceItems(new String[] { "关闭", "1", "2", "3", "4", "5"}, MagicParams.beautyLevel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            magicEngine.setBeautyLevel(which);
                                            dialog.dismiss();
                                        }
                                    })
                            .setNegativeButton("取消", null)
                            .show();
                    break;
            }
        }
    };

    private void showFilters(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(mFilterLayout, "translationY", mFilterLayout.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
//                findViewById(R.id.btn_camera_shutter).setClickable(false);

                mFilterLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        animator.start();
    }

    private void initOrientationSensor() {
        orientationObserver = new OrientationObserver(this) {
            @Override
            public void onOrientationChangedEvent(int orientation) {
                MainActivity.this.orientation = (((orientation + 45) / 90) * 90) % 360;
//                Log.e("MainActivity", "orientation=" + orientation);
            }
        };
    }

    private void startBitrateTimer() {
        if (DEBUG) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    bitrate.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //定时器实时显示当前监控信息
                            bitrate.setText("push url =" + config.getUrl() + "," + KsyRecordSender.getRecordInstance().getAVBitrate());
//                            bitrate.setText(KsyRecordSender.getRecordInstance().getAVBitrate() + "record angle =" + SMRecordClientConfig.recordOrientation + ",preview angel =" + SMRecordClientConfig.previewOrientation);
                        }
                    }, 1000);
                }
            }, 1000, 1000);
        }
    }

    private void initDrawer() {
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer = (DrawerView) findViewById(R.id.drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.open,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        final View dialogView = LayoutInflater.from(this).inflate(
                R.layout.dialog_input, null);
        final EditText editInput = (EditText) dialogView
                .findViewById(R.id.input);
        adapter = new DrawerItemConfigAdapter();
        adapter.setConfig(config);
        adapter.setContext(this);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.color_primary_dark));
        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.closeDrawer(drawer);
        drawer.addItems(adapter.getItemViews());
        drawer.setOnItemClickListener(new DrawerItem.OnItemClickListener() {
            @Override
            public void onClick(final DrawerItem drawerItem, long l, final int position) {
                if (position == Constants.SETTING_URL) {
//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle(R.string.Url)
//                            .set(InputType.TYPE_CLASS_TEXT)
//                            .input("input rtmp server url", config.getUrl(), new MaterialDialog.InputCallback() {
//                                @Override
//                                public void onInput(MaterialDialog dialog, CharSequence input) {
//                                    config.setmUrl(input.toString());
//                                    drawerItem.setTextSecondary(input.toString());
//                                }
//                            }).show();
                    new AlertDialog.Builder(MainActivity.this).setTitle("User Input")
                            .setView(dialogView)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    config.setmUrl(editInput.getText().toString());
                                    drawerItem.setTextSecondary(editInput.getText().toString());

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                }
            }
        });
    }

    private void setUpEnvironment() {
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SMRecordClientConfig.Builder builder = new SMRecordClientConfig.Builder();
        builder.setVideoProfile(CamcorderProfile.QUALITY_480P).setUrl(Constants.URL_DEFAULT);
        builder.setCameraType(Camera.CameraInfo.CAMERA_FACING_BACK);
//        builder.setVideoBitRate(Constants.CONFIG_VIDEO_BITRATE_250K);
        config = builder.build();
    }

    private void initSurfaceView() {
//        mSurfaceView = (TextureView) findViewById(R.id.textureview);

//        mSurfaceView = (SurfaceView)findViewById(R.id.surface_preview);

//        SurfaceHolder holder = mSurfaceView.getHolder();
//        // setType must be set in old version, otherwise may cause crash
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        }
    }

    private void setupRecord() {
        client = KsyRecordClient.getInstance(getApplicationContext());
        client.setConfig(config);
        client.setDisplayPreview(mSurfaceView);
//        client.setCameraSizeChangedListener(mSurfaceView);
        client.setOrientationActivity(this);
        client.setNetworkChangeListener(this);
        client.setPushStreamStateListener(this);
        client.setSwitchCameraStateListener(this);
        client.setStartListener(this);
        client.setOnClientErrorListener(this);
    }

    private void stopRecord() {
        if (client.stopRecord()) {
            mFab.setImageDrawable(getResources().getDrawable(R.mipmap.btn_record));
            mRecording = false;
            showToast("已停止");
        }

        magicEngine.stopRecord();
    }

    private void startRecord() {
        Log.d(Constants.LOG_TAG, "startRecord..");

        try {
            client.startRecord();
            mRecording = true;
            mFab.setImageDrawable(getResources().getDrawable(R.mipmap.btn_stop));
        } catch (KsyRecordException e) {
            e.printStackTrace();
            Log.d(Constants.LOG_TAG, "Client Error, reason = " + e.getMessage());
        }

        magicEngine.setConfig(config);
        magicEngine.setOnClientErrorListener(this);
        magicEngine.startRecord();
    }

    private void toggleRecord() {
        if (!mRecording) {
            startRecord();
        } else {
            stopRecord();
        }
    }

    private void toggleFlash() {
        if (client.canTurnLight()) {
            if (KsyRecordClient.CAMEAR_FLASH_SUCCESS == client.turnLight(flash)) {
                showToast("flashlight is" + (!flash));
            }
            flash = !flash;
        } else {
            showToast("can't turn flash now try it later");
        }
    }

    private void changeCamera() {
//        client.switchCamera();

        magicEngine.switchCamera();
    }

    /*
    *
    * Activity Life Circle
    * */
    @Override
    protected void onStop() {
        super.onStop();
        showToast("程序关闭");
        orientationObserver.disable();
        client.unregisterNetworkMonitor();
        if (mRecording) {
            stopRecord();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_github:
                showToast("welcome you !" );
                //Intent i = new Intent(Intent.ACTION_VIEW);
                //i.setData(Uri.parse(url));
              //  startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int getOrientation() {
        return orientation;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onNetworkChanged(int state) {
        Toast.makeText(MainActivity.this, "onNetworkChanged :" + state, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPushStreamState(int state) {
        showToast("onPushStreamState :" + state);
    }

    @Override
    public void onSwitchCameraDisable() {
        showToast("onSwitchCameraDisable :");
    }

    @Override
    public void onSwitchCameraEnable() {
        showToast("onSwitchCameraEnable :");
    }

    @Override
    public void OnStartComplete() {
        showToast("开始直播！");
    }

    @Override
    public void OnStartFailed() {
        showToast("启动直播失败！");
    }

    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        Log.d(Constants.LOG_TAG, text);
    }

    @Override
    public void onClientError(int source, int what) {
        Toast.makeText(MainActivity.this, "onClientError" + source, Toast.LENGTH_SHORT).show();
    }
}
