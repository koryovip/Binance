package test;

import java.util.Calendar;
import java.util.List;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import utils.BinanceClient;
import utils.DateUtil;

public class GetCand {

    public static void main(String[] args) {
        Calendar cal1 = Calendar.getInstance();
        cal1.clear();
        cal1.set(2018, 1 - 1, 1, 0, 0, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.setTimeInMillis(BinanceClient.me().apiRestClient().getServerTime());
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        System.out.println(cal2.getTimeInMillis());
        long last = 0L;
        do {
            List<Candlestick> org = BinanceClient.me().apiRestClient().getCandlestickBars("TRIGBTC", CandlestickInterval.ONE_MINUTE, 60 * 24, cal1.getTimeInMillis(), null);
            for (Candlestick candlestick : org) {
                //System.out.println(DateUtil.me().format0(candlestick.getOpenTime()) + "\t" + candlestick);
                last = candlestick.getOpenTime();
            }
            cal1.setTimeInMillis(last);
        } while (last < cal2.getTimeInMillis());
    }

}
