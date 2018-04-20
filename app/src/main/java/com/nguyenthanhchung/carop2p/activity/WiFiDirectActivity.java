package com.nguyenthanhchung.carop2p.activity;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nguyenthanhchung.carop2p.MainGameActivityCallBacks;
import com.nguyenthanhchung.carop2p.R;
import com.nguyenthanhchung.carop2p.fragment.BoardEmotionFragment;
import com.nguyenthanhchung.carop2p.fragment.BoardGameFragment;
import com.nguyenthanhchung.carop2p.handler.ActionListenerHandler;
import com.nguyenthanhchung.carop2p.handler.WiFiDirectReceiver;
import com.nguyenthanhchung.carop2p.model.PackageData;
import com.nguyenthanhchung.carop2p.model.TypePackage;

import java.util.ArrayList;

public class WiFiDirectActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, MainGameActivityCallBacks {

    private static final String TAG ="WiFiDirectActivity";
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    public WiFiDirectReceiver mReceiver;
    //Arrays with devices'
    private ArrayList<String> devicesNames;
    private ArrayList<String> devicesAddress;
    private ArrayList<WifiP2pDevice> devicesList;
    Context context;
    private String opponentsName;
    private ArrayAdapter<String> hybridAdapter;
    private static WiFiDirectActivity thisActivity;
    private ListView deviceListView;
    private Button btn;


    FragmentTransaction fragmentTransaction;
    BoardGameFragment boardGameFragment;
    BoardEmotionFragment emotionBoardFragmet;
    ImageButton btnOpenEmotionBoard;
    boolean isOpenedEmotionBoard = false;

    RelativeLayout layoutGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_direct);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);



        context = this.getApplicationContext();
        devicesNames = new ArrayList<>();
        devicesAddress = new ArrayList<>();

        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel= mManager.initialize(this, getMainLooper(),this);

        hybridAdapter =  new ArrayAdapter<String>(this,
                R.layout.item,
                R.id.cheese_name,
                devicesNames
        );



        deviceListView = (ListView)findViewById(R.id.list_view);
        deviceListView.setAdapter(hybridAdapter);

//        //Demo mẫu send msg, xem trong log
//        btn = findViewById(R.id.btnSend);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mReceiver != null)
//                    mReceiver.sendMsg("13213213132");
//            }
//        });

        //Kết nối với thiết bị khi click
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int possition, long id) {
                if (devicesList != null) {
                    String address = devicesAddress.get(possition);
                    String name = devicesNames.get(possition);
                    Toast.makeText(context, "Connecting to " + name, Toast.LENGTH_SHORT).show();
                    connectToPeer(devicesList.get(possition));
                }
            }
        });

        thisActivity=this;
        opponentsName="Opponent";

        layoutGame = findViewById(R.id.layoutGame);
        layoutGame.setVisibility(RelativeLayout.INVISIBLE);
        //this.Show();

    }

    private void showEmotionBoard(){
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.show(emotionBoardFragmet);
        fragmentTransaction.commit();
    }

    private void hideEmotionBoard(){
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(emotionBoardFragmet);
        fragmentTransaction.commit();
    }

    private void addEvents() {
        btnOpenEmotionBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpenedEmotionBoard == false){
                    showEmotionBoard();
                    isOpenedEmotionBoard = true;
                }
            }
        });
    }



    private void addControls() {

        btnOpenEmotionBoard = findViewById(R.id.btnOpenEmotionBoard);

        // add fragment boardgame
        fragmentTransaction = getFragmentManager().beginTransaction();
        boardGameFragment = BoardGameFragment.newInstance("boardGame");
        fragmentTransaction.replace(R.id.fragmentBoardGame, boardGameFragment);
        fragmentTransaction.commit();

        // add fragment emotion board
        fragmentTransaction = getFragmentManager().beginTransaction();
        emotionBoardFragmet = BoardEmotionFragment.newInstance("EmotionBoard");
        fragmentTransaction.replace(R.id.fragmentEmotionBoard, emotionBoardFragmet);
        fragmentTransaction.hide(emotionBoardFragmet);
        fragmentTransaction.commit();
    }


    /**
     * Tìm kiếm thiết bị có thể kết nối
     * */
    public void peerAvailable(){
        devicesList =  mReceiver.getDeviceList();
        if(devicesList!=null) {
            //Xóa dữ liệu adapter
            hybridAdapter.clear();
            // Tính toán và cập nhật các thiết bị
            calculateDevices();
            //Báo adapter thay đổi
            hybridAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Tính toán danh sách thiết bị, lấy tên + ip thêm vào devicesNames và devicesAddress
     * */
    private void calculateDevices(){
        if(devicesList!=null) {
            if(devicesNames!=null) devicesNames.clear();
            if(devicesAddress!=null) devicesAddress.clear();
            for (int i=0;i<devicesList.size();i++) {
                devicesNames.add(devicesList.get(i).deviceName);
                devicesAddress.add(devicesList.get(i).deviceAddress);
            }
        }
    }
    /**
     * Kết nối với thiết bị
     * */
    public void connectToPeer(WifiP2pDevice device){
        if(device!=null){
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            opponentsName = device.deviceName;
            config.wps.setup= WpsInfo.PBC;
            mManager.connect(mChannel, config, new ActionListenerHandler(this, "Connection to peer"));
        }else{
            Log.d(TAG, "Can not find that device");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterWifiReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        registerWifiReceiver();
        mManager.discoverPeers(mChannel, new ActionListenerHandler(this, "Discover peers"));

        //findViewById(R.id.helpFAB).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendMsg("LEFT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mManager.stopPeerDiscovery(mChannel, new ActionListenerHandler(this, "Stop Discovery"));
        }
        mManager.removeGroup(mChannel, new ActionListenerHandler(this, "Group removal"));
        mManager.cancelConnect(mChannel, new ActionListenerHandler(this, "Canceling connect"));
        unregisterWifiReceiver();
        mManager=null;
        Log.d(TAG, "WifiDirectActivity stopped");
    }


    private void registerWifiReceiver() {
        mReceiver = new WiFiDirectReceiver(mManager,mChannel,this);
        mReceiver.registerReceiver();
    }

    private void unregisterWifiReceiver() {
        if(mReceiver!=null) {
            mReceiver.unregisterReceiver();
        }
        mReceiver=null;
    }

    /**
     * Nhận dữ liệu từ game để xử lí tác vụ game
     **/
    public void handleIncoming(String msg){
        int x,y;
        Log.d(TAG, "Incoming " + msg);
        if(msg.equals("RESET")){
            //resetCanvasBoard();
        }else if (msg.equals("LEFT")) {
            onBackPressed();
        }
        else{
            //Sử lý dữ liệu dựa trên msg
        }
    }

    /*Đóng show listView danh sách các thiết bị*/
    public void Show(){
        //deviceListView.setVisibility(View.INVISIBLE);
        layoutGame.setVisibility(RelativeLayout.VISIBLE);
        addControls();
    }

    public void Hide(){

    }

    /*Gửi msg sang thiết bị khác*/
    public void sendMsg(String msg){
        mReceiver.sendMsg(msg);
        Log.d(TAG, "Sending move");
    }

    //Tạo mới channel;
    @Override
    public void onChannelDisconnected() {
        Log.d(TAG, "WIFI Direct Disconnected, reinitializing");
        reinitialize();
    }

    public void reinitialize(){
        mChannel = mManager.initialize(this, getMainLooper(), this);
        if(mChannel!=null){
            Log.d(TAG,"WIFI Direct reinitialize : SUCCESS");
        }else{
            Log.d(TAG, "WIFI Direct reinitialize : FAILURE");
        }
    }

    /**
     * Nhận dữ liệu từ Fragment và gửi sang thiết bị khác bằng sendMsg
     * @param sender
     * @param strValue
     */
    @Override
    public void onMsgFromFragmentToMainGame(String sender, String strValue) {
        Log.d(TAG,"From: " + sender + " - Value: " + strValue);
        if(sender == null || strValue == null) return;
        if(sender.equals("EmotionBoard")){
            if(strValue.equals("close")){
                if(isOpenedEmotionBoard){
                    hideEmotionBoard();
                    isOpenedEmotionBoard = false;
                }
            }
        }else if(sender.equals("GameBoard")){
            PackageData packageData = new PackageData();
            packageData.type = TypePackage.TURN;
            packageData.msg = strValue;
            sendMsg(packageData.toString());
        }
    }

}
