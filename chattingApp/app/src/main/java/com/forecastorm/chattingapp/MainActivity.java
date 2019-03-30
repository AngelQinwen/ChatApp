package com.forecastorm.chattingapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.forecastorm.chattingapp.websocket.CloseHandler;
import com.forecastorm.chattingapp.websocket.StompMessage;
import com.forecastorm.chattingapp.websocket.StompMessageSerializer;
import com.forecastorm.chattingapp.websocket.TopicHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity
{
    private Map<String, TopicHandler> destinations = new HashMap<>();
    private CloseHandler closeHandler;
    private WebSocket webSocket;

    private TopicHandler topicHandler;

    private EditText messageBox;
    private ImageButton send;
    private ListView messageList;
    private RelativeLayout activity_main;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity_main = findViewById(R.id.activity_main);
        messageList = findViewById(R.id.messageList);
        send = findViewById(R.id.send);
        messageBox = findViewById(R.id.messageBox);
        adapter = new MessageAdapter();
        messageList.setAdapter(adapter);


        instantiateWebSocket();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageBox.getText().toString();
                if(!message.isEmpty()){
                    webSocket.send(message);
                    messageBox.setText("");


                    StompMessage message1 = new StompMessage();
                    message1.put("message",message);
                  
                }
            }
        });


//        SocketListener client = new SocketListener();
//        TopicHandler handler = client.subscribe("/topics/event");
//        handler.addListener(new StompMessageListener() {
//            @Override
//            public void onMessage(final StompMessage message) {
//                //System.out.println(message.getHeader("destination") + ": " + message.getContent());
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        Toast.makeText(MainActivity.this, message.getHeader("destination") + ": " + message.getContent(), Toast.LENGTH_LONG).show();
//                    }
//                });
//
//            }
//        });





    }

    public void instantiateWebSocket(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();
        Request request = new Request.Builder().url("ws://192.168.0.19:8080/my-ws/websocket").build();
        SocketListener socketListener = new SocketListener();
        client.newWebSocket(request, socketListener);
        client.dispatcher().executorService().shutdown();

    }


    public class SocketListener extends WebSocketListener
    {


        public SocketListener() {


        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            sendConnectMessage(webSocket);
            for(String destination : destinations.keySet()){
                sendSubscribeMessage(webSocket,destination);
            }
                sendMessage(webSocket, "/app/hello");
           runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connect Successfully!", Toast.LENGTH_SHORT).show();

                }
            });



            closeHandler = new CloseHandler(webSocket);
        }


        @Override
        public void onMessage(final WebSocket webSocket, final String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StompMessage message = StompMessageSerializer.deserialize(text);
                    String destination = message.getHeader("destination");

                    if(destinations.containsKey(destination))
                    {
                        destinations.get(destination).onMessage(message);
                    }
                }
            });



        }

//        @Override
//        public void onMessage(WebSocket webSocket, ByteString bytes) {
//            super.onMessage(webSocket, bytes);
//        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            webSocket.close(1000,null);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            t.printStackTrace();
        }


        public TopicHandler subscribe(String destination){
            TopicHandler handler = new TopicHandler(destination);
            destinations.put(destination, handler);
            
            if(webSocket != null){
                sendSubscribeMessage(webSocket, destination);
            }
            if(webSocket != null){
                sendMessage(webSocket, "app/hello");
            }
            return handler;
        }









    }

    //Send different StompMessage types to server

    private void sendConnectMessage(WebSocket webSocket)
    {
        StompMessage message = new StompMessage("CONNECT");
        message.put("accept-version","1.1");
        message.put("heart-beat","10000,10000");
        webSocket.send(StompMessageSerializer.serialize(message));
    }


    private void sendMessage(WebSocket webSocket, String destination)
    {
        StompMessage message = new StompMessage("SEND");
        message.put("destination", destination);
        message.setContent("text if works");
        webSocket.send(StompMessageSerializer.serialize(message));
    }

    private void sendSubscribeMessage(WebSocket webSocket, String destination)
    {
        String id = "sub-001";
        StompMessage message = new StompMessage("SUBSCRIBE");
        message.put("id", id);
        message.put("destination", destination);
        webSocket.send(StompMessageSerializer.serialize(message));


    }

//    public void main(String... args){
//        SocketListener client = new SocketListener();
//        TopicHandler handler = client.subscribe("/topics/event");
//        handler.addListener(new StompMessageListener() {
//            @Override
//            public void onMessage(StompMessage message) {
//                //System.out.println(message.getHeader("destination") + ": " + message.getContent());
//
//            }
//        });
//
//    }


    private class MessageAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }




}
