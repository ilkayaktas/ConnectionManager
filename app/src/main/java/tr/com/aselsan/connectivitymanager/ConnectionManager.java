package tr.com.aselsan.connectivitymanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import io.reactivex.rxjava3.subjects.PublishSubject;
import lombok.Getter;

/**
 * Created by ilkayaktas on 4/14/21 at 2:07 PM.
 */

public class ConnectionManager {
    private static ConnectionManager connectionManager;
    private final Context context;
    private static ConnectivityManager connectivityManager;
    private static Network currentNetwork;
    private static ConnectivityManager.NetworkCallback networkCallback;
    @Getter private static PublishSubject<ConnectionState> connectionStatePublisher = PublishSubject.create();

    public static ConnectionManager getInstance(Context context){
        if (connectionManager == null){
            connectionManager = new ConnectionManager(context);
            connectivityManager = (ConnectivityManager)connectionManager.context.getSystemService(Context.CONNECTIVITY_SERVICE);
            currentNetwork = connectivityManager.getActiveNetwork();
            createNetworkCallback();
        }
        return connectionManager;
    }

    private static void createNetworkCallback(){
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                System.out.println("XXXX available");

                ConnectionState connectionState = new ConnectionState();
                connectionState.connection = Connection.INTERNET_ONLINE;

                connectionStatePublisher.onNext(connectionState);
            }

            @Override
            public void onLost(Network network) {
                System.out.println("XXXX unavailable");

                ConnectionState connectionState = new ConnectionState();
                connectionState.connection = Connection.INTERNET_OFFLINE;

                connectionStatePublisher.onNext(connectionState);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                System.out.println("XXXX " + networkCapabilities);

                ConnectionState connectionState = new ConnectionState();
                connectionState.connection = Connection.INTERNET_ONLINE;
                connectionState.downloadBandwithKbps = networkCapabilities.getLinkDownstreamBandwidthKbps();
                connectionState.uploadBandwithKbps = networkCapabilities.getLinkUpstreamBandwidthKbps();

                String transport="";
                String [] str = networkCapabilities.toString().split(" ");
                for (int i = 0; i < str.length; i++) {
                    if (str[i].startsWith("Transports") && (i+1) < str.length){
                        transport = str[i+1];
                        break;
                    }
                }

                if (transport.equals("WIFI")){
                    connectionState.transport = Transport.WIFI;
                } else if (transport.equals("CELLULAR")){
                    if (networkCapabilities.getLinkDownstreamBandwidthKbps() < 10){
                        connectionState.transport = Transport.UNSTABLE;
                    } else if (networkCapabilities.getLinkDownstreamBandwidthKbps() < 300){
                        connectionState.transport = Transport.TWO_G;
                    } else if (networkCapabilities.getLinkDownstreamBandwidthKbps() < 45000){
                        connectionState.transport = Transport.THREE_G;
                    } else if (networkCapabilities.getLinkDownstreamBandwidthKbps() < 150000){
                        connectionState.transport = Transport.FOUR_G;
                    }
                }

                connectionStatePublisher.onNext(connectionState);

            }

        };
    }

    private ConnectionManager(Context context){
        this.context = context;
    }

    public static void unregister(){
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }
    public static void register(){
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    public enum Transport {
        UNSTABLE("UNSTABLE"),
        TWO_G("2G"),
        THREE_G("3G"),
        FOUR_G("4G"),
        WIFI("WIFI");

        String transport;
        Transport(String transport) {
            this.transport = transport;
        }
    }

    public enum Connection {
        INTERNET_ONLINE,
        INTERNET_OFFLINE
    }

    public static class ConnectionState{
        public Transport transport;
        public Connection connection;
        public int downloadBandwithKbps;
        public int uploadBandwithKbps;
    }

}
