package com.groundspace.lampmqtt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static com.groundspace.lampmqtt.Utils.encryptHMAC;

public class MainActivity extends AppCompatActivity {

    private Button connectButton;
    private Button publishButton;
    private Button subscribeButton;
    private SeekBar seekBar_r;
    private SeekBar seekBar_g;
    private SeekBar seekBar_b;
    private TextView value_r;
    private TextView value_g;
    private TextView value_b;


    MqttAndroidClient mqttAndroidClient;

    String clientId     = "example";
    String productKey   = "a1eHWXNlmgZ";
    String deviceName   = "test_app";
    String deviceSecret = "759033821fff72b5db55266bddf79122";
    String timestamp    = String.valueOf(System.currentTimeMillis());

    // cn-shanghai
    String serverUri    = "tcp://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";
    String mqttclientId = clientId + "|securemode=3,signmethod=hmacsha1|";
    String content      = "clientId" + clientId + "deviceName" + deviceName + "productKey" + productKey;

    //final String publishMessage = "{\"code\": 520, \"topic\": \"/test1116/user/update\", \"data\": {\"uuid\": \"00 50 00 1A 0D 47 37 34 35 34 31 30 \", \"area\": 1, \"group\": 1, \"number\": 1, \"action\": \"on\"}, \"message\": \"no active session\"}";
    //final String publishMessage="{\"params\":{\"ColorGreen\":70}}\r";
    //final String publishMessage="{\"id\":1605595894621,\"params\":{\"ColorGreen\":40},\"version\":\"1.0\",\"method\":\"thing.event.property.post\"}";
    //final String publishMessage = "Hello Ground Space!";

    int qos                     = 1;
    String publishTopic         = "/sys/" + productKey + "/" + deviceName + "/thing/event/property/post";
    //String publishTopic         = "/" + productKey + "/" + deviceName + "/user/color_control";
    String subscriptionTopic    = "/" + productKey + "/" + deviceName + "/user/color_show";

    Integer redInt=0,greenInt=0,blueInt=0;//保存三色光数值


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = (Button)findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"begin to connect iothub.",Toast.LENGTH_SHORT).show();
                connectToIot();
            }
        });

        publishButton = (Button)findViewById(R.id.publish_message);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage();
            }
        });

        subscribeButton = (Button)findViewById(R.id.subscribe_topic);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic();
            }
        });

        seekBar_r=(SeekBar)findViewById(R.id.seekBar_red);
        seekBar_g=(SeekBar)findViewById(R.id.seekBar_green);
        seekBar_b=(SeekBar)findViewById(R.id.seekBar_blue);
        value_r=(TextView)findViewById(R.id.redValue);
        value_g=(TextView)findViewById(R.id.greenValue);
        value_b=(TextView)findViewById(R.id.blueValue);
        seekBar_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value_r.setText(Integer.toString(i));
                redInt=i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar_g.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value_g.setText(Integer.toString(i));
                greenInt=i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                value_b.setText(Integer.toString(i));
                blueInt=i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, mqttclientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("connect to iothub complete.");
                Toast.makeText(MainActivity.this,"connect to iothub success.",Toast.LENGTH_SHORT).show();
                if (reconnect) {
                    // Because Clean Session is true, we need to re-subscribe
                } else {
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("connection to iothub lost.");
                Toast.makeText(MainActivity.this,"connect to iothub failed.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.printf("receive message : %s\n", message.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("delivery to iot hub complete.");
            }
        });
    }

    public void connectToIot() {
        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

            String mqttUsername = deviceName + "&" + productKey;
            System.out.println("gen username : " + mqttUsername);
            mqttConnectOptions.setUserName(mqttUsername);

            System.out.println("show content : " + content);
            System.out.println("show secret : " + deviceSecret);

            String mqttPassword = "";
            try {
                mqttPassword = encryptHMAC("hmacsha1", content, deviceSecret);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("calc password : " + mqttPassword);
            mqttConnectOptions.setPassword(mqttPassword.toCharArray());

            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setMqttVersion(4);
            mqttConnectOptions.setKeepAliveInterval(90);

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.printf("connect to iot hub success.\n");
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.printf("connect to iothub failed, exception : %s\n", exception.toString());
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("subscribe topic success.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("subscribe topic failed.");
                }
            });

            // THIS DOES NOT WORK!
            mqttAndroidClient.subscribe(subscriptionTopic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // message Arrived!
                    System.out.println("Message: " + topic + " : " + new String(message.getPayload()));
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage() {

        try {
            if ( mqttAndroidClient == null || ! mqttAndroidClient.isConnected()) {
                System.out.println("not connect");
                return;
            }
            System.out.println("begin publish");
            MqttMessage message = new MqttMessage();
            String publishMessage="{\"id\":1605595894621,\"params\":{\"ColorBlue\":"+blueInt+",\"ColorGreen\":"+greenInt+",\"ColorRed\":"+redInt+"},\"version\":\"1.0\",\"method\":\"thing.event.property.post\"}";
            message.setPayload(publishMessage.getBytes());
            System.out.println(message);
            mqttAndroidClient.publish(publishTopic,message);
            if (!mqttAndroidClient.isConnected()) {
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
