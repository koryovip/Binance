package utils;

import com.ConfigBinance;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;

public class BinanceClient {

    private static BinanceClient singleton = new BinanceClient();

    public static BinanceClient me() {
        return singleton;
    }

    final private BinanceApiWebSocketClient client;
    final private BinanceApiRestClient clientUser;

    private BinanceClient() {
        this.clientUser = BinanceApiClientFactory.newInstance(ConfigBinance.me().getApiKey(), ConfigBinance.me().getSecret()).newRestClient();
        this.clientUser.ping();
        this.client = BinanceApiClientFactory.newInstance().newWebSocketClient();
    }

    final public BinanceApiWebSocketClient webSocketClient() {
        return this.client;
    }

    final public BinanceApiRestClient apiRestClient() {
        return this.clientUser;
    }

}
