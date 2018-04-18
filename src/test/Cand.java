package test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import ifc.BollValue;
import ifc.CandleValue;
import ifc.Exchange;
import ifc.KRCandleType;
import ifc.calc.CalcBoll;
import utils.BinanceClient;
import utils.DateUtil;

public class Cand implements Runnable, Exchange<String, CandlestickInterval> {

    public void execute(final Exchange exchange, final BigDecimal period) throws Exception, Exception {
        final int round = 9;
        List<CandleValue> candleValueList = exchange.getCandles(null, null, period.intValue() + 10);
        // calc boll
        List<BollValue> bollValueList = CalcBoll.me().calc(candleValueList, period, round);

        for (int ii = 0; ii < candleValueList.size(); ii++) {
            // open, high, low, close, volume, date
            CandleValue ohlcv = candleValueList.get(ii);
            System.out.print(String.format("%s\t%s\t%s\t%s\t%s", DateUtil.me().format1(ohlcv.time), ohlcv.open, ohlcv.high, ohlcv.low, ohlcv.close));
            BollValue bollRow = bollValueList.get(ii);
            System.out.print(String.format("\t|\t%s\t%s\t%s\t%s\t%s", bollRow.ma, bollRow.high1, bollRow.high2, bollRow.low1, bollRow.low2));
            if (bollRow.isValid()) {
                this.check(ohlcv, bollRow.high2, bollRow.low2, false);
            }
            System.out.println();
        }
    }

    private void check(final CandleValue ohlcv, final BigDecimal sigmaPlus2, final BigDecimal sigmaMinus2, boolean twitter) {
        {
            final BigDecimal higher = CalcBoll.me().getHigher(ohlcv);
            if (higher.compareTo(sigmaPlus2) > 0) {
                if (twitter) {
                    //        new Thread(new Twiite(String.format("下落可能！↓↓↓ 幅[%s]", sigmaPlus2.subtract(sigmaMinus2)))).start();
                } else {
                    System.out.print(String.format("\t|\t%s\t↑↑↑", sigmaPlus2.subtract(sigmaMinus2)));
                }
            }
        }
        {
            final BigDecimal lower = CalcBoll.me().getLower(ohlcv);
            if (lower.compareTo(sigmaMinus2) < 0) {
                if (twitter) {
                    //         new Thread(new Twiite(String.format("上昇可能！↑↑↑ 幅[%s]", sigmaPlus2.subtract(sigmaMinus2)))).start();
                } else {
                    System.out.print(String.format("\t|\t%s\t↓↓↓", sigmaPlus2.subtract(sigmaMinus2)));
                }
            }
        }
    }


    public List<CandleValue> getCandles(final KRCandleType candleType, final int limit) throws Exception {
        List<Candlestick> org = BinanceClient.me().apiRestClient().getCandlestickBars("XRPBTC", CandlestickInterval.FIVE_MINUTES, limit, null, null);
        final List<CandleValue> result = new ArrayList<CandleValue>();
        for (Candlestick candlestick : org) {
            result.add(new CandleValue(KRCandleType._5MIN, //
                    new BigDecimal(candlestick.getOpen()), //
                    new BigDecimal(candlestick.getHigh()), //
                    new BigDecimal(candlestick.getLow()), //
                    new BigDecimal(candlestick.getClose()), //
                    candlestick.getOpenTime() //
            ));
        }
        return result;
    }

    @Override
    public void run() {
        try {
            this.execute(this, period);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final BigDecimal period = new BigDecimal(20);

    public static void main(String[] args) throws Exception, Exception {
        Cand cand = new Cand();
        cand.execute(cand, period);
    }

    @Override
    public List<CandleValue> getCandles(String pair, CandlestickInterval candleType, int limit) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public List<BollValue> getBoll(List<CandleValue> candleValueList, String pair, CandlestickInterval candleType, int period, int round) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public boolean lastBoll(CandleValue lastOhlcv, BollValue lastBollRow) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        return false;
    }

}
