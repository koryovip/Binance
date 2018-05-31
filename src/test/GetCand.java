package test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.general.SymbolStatus;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.druid.DruidPlugin;

import utils.BinanceClient;
import utils.OtherUtil;

public class GetCand {

    public static void main(String[] args) {

        DruidPlugin dp = new DruidPlugin("jdbc:sqlite:all@binance.db", "", "");
        //DruidPlugin dp = new DruidPlugin("jdbc:sqlite::memory:", "", "");
        ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
        // arp.getEngine().setSourceFactory(new ClassPathSourceFactory());
        // arp.addSqlTemplate("all.sql");

        dp.start();
        arp.start();

        ExchangeInfo info = BinanceClient.me().apiRestClient().getExchangeInfo();
        final List<String> symbols = new ArrayList<String>();
        for (SymbolInfo symbolInfo : info.getSymbols()) {
            if (symbolInfo.getStatus() != SymbolStatus.TRADING) {
                continue;
            }
            symbols.add(symbolInfo.getSymbol());
        }

        boolean getData = false;
        if (getData) {
            final int year = 2018;
            final int month = 5;
            //            final List<String> symbols = Arrays.asList(new String[] { //
            //                    "TRIGBTC" //
            //                    , "XRPBTC" //
            //                    , "BTCUSDT" //
            //            });

            for (String symbol : symbols) {
                Db.update("create table if not exists " + symbol + " (open_time integer not null, open real not null, high real not null, low real not null, close real not null, volume real not null, close_time integer, primary key(open_time))");
                doGet(year, month, symbol);
            }
        }
        BigDecimal pppp = new BigDecimal(25);
        for (String symbol : symbols) {
            getMA(symbol, pppp);
        }
    }

    public static void getMA(String symbol, BigDecimal pppp) {
        List<Record> records = Db.find("select * from " + symbol + " order by open_time asc");
        if (records.size() < pppp.intValue()) {
            return;
        }
        BigDecimal result = BigDecimal.ZERO;
        for (int len = records.size(), ii = len - pppp.intValue(); ii < len; ii++) {
            Record record = records.get(ii);
            result = result.add(new BigDecimal(record.getStr("close")));
            // System.out.println(String.format("[%10s][%s][%10s][%10s]", symbol, DateUtil.me().format3(record.getLong("open_time")), new BigDecimal(record.getStr("open")), new BigDecimal(record.getStr("close"))));
        }
        Record recordLast = OtherUtil.me().lastItem(records);
        final BigDecimal ma = result.divide(pppp, 8, RoundingMode.DOWN);
        final BigDecimal close = new BigDecimal(recordLast.getStr("close"));
        System.out.println(String.format("%10s\t%10s\t%10s\t%10s", symbol, close, ma, OtherUtil.me().persent3(close, ma, 1)));
    }

    public static void doGet(int year, int month, String symbol) {
        System.out.println(symbol);
        final String sql = "REPLACE into " + symbol + " values (?,?,?,?,?,?,null)";
        Calendar cal1 = Calendar.getInstance();
        cal1.clear();
        cal1.set(year, month - 1, 1, 23, 59, 59);

        Calendar cal2 = Calendar.getInstance();
        cal2.clear();
        cal2.setTimeInMillis(BinanceClient.me().apiRestClient().getServerTime());
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        // System.out.println(cal2.getTimeInMillis());
        long last = 0L;
        do {
            List<Candlestick> org = BinanceClient.me().apiRestClient().getCandlestickBars(symbol, CandlestickInterval.DAILY, Integer.MAX_VALUE, cal1.getTimeInMillis(), null);
            last = OtherUtil.me().lastItem(org).getOpenTime();
            /*for (Candlestick candlestick : org) {
                System.out.println(DateUtil.me().format3(candlestick.getOpenTime()) + "\t" + candlestick);
            }*/
            Db.tx(Connection.TRANSACTION_SERIALIZABLE, new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    for (Candlestick candlestick : org) {
                        // System.out.println(DateUtil.me().format0(candlestick.getOpenTime()) + "\t" + candlestick);
                        // last = candlestick.getOpenTime();
                        Db.update(sql, candlestick.getOpenTime() //
                        , new BigDecimal(candlestick.getOpen()) //
                        , new BigDecimal(candlestick.getHigh()) //
                        , new BigDecimal(candlestick.getLow()) //
                        , new BigDecimal(candlestick.getClose()) //
                        , new BigDecimal(candlestick.getVolume()) //
                        );
                    }
                    return true;
                }
            });
            cal1.setTimeInMillis(last);
            //System.out.println(DateUtil.me().format0(last));
            //System.out.println(DateUtil.me().format0(cal2.getTimeInMillis()));
        } while (last < cal2.getTimeInMillis());
    }
}
