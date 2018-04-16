package auto3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.FilterType;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.BookTicker;
import com.binance.api.client.domain.market.TickerStatistics;

import utils.BinanceClient;

public class Auto3 {
    final private static BigDecimal B100 = new BigDecimal("100");
    static final ExchangeInfo exchangeInfo;
    static final Map<String, BigDecimal> tickerStatisticsMap;
    static BigDecimal BTC = new BigDecimal("0.02");
    static {
        exchangeInfo = BinanceClient.me().apiRestClient().getExchangeInfo();
        List<TickerStatistics> tickerStatistics = BinanceClient.me().apiRestClient().getAll24HrPriceStatistics();
        tickerStatisticsMap = new HashMap<String, BigDecimal>(tickerStatistics.size());
        for (TickerStatistics row : tickerStatistics) {
            System.out.println(String.format("%s\t%s\t%d", row.getSymbol(), row.getQuoteVolume(), row.getCount()));
            tickerStatisticsMap.put(row.getSymbol(), new BigDecimal(row.getCount()));
        }
    }

    public static void main(String[] args) {
        BinanceClient.me().apiRestClient().ping();
        final String XXX = "BTC";
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(() -> {
            System.out.print(".");
            loop(XXX/*, BTC*/);
        }, 0, 3, TimeUnit.SECONDS);
    }

    private static void loop(final String XXX/*, BigDecimal BTC*/) {
        //List<TradeDetail> result = new ArrayList<TradeDetail>();
        List<BookTicker> list = BinanceClient.me().apiRestClient().getBookTickers();
        for (final BookTicker bookTicker1 : list) {
            final String symbol1 = bookTicker1.getSymbol();
            if (ig(symbol1)) {
                continue;
            }
            if (!(symbol1.startsWith(XXX) || symbol1.endsWith(XXX))) {
                continue;
            }
            // XXXYYY or YYYXXX
            final String YYY = getOppo(symbol1, XXX);
            for (final BookTicker bookTicker2 : list) {
                final String symbol2 = bookTicker2.getSymbol();
                if (ig(symbol2)) {
                    continue;
                }
                if ((symbol2.startsWith(YYY) && !symbol2.equals(YYY + XXX)) || (symbol2.endsWith(YYY) && !symbol2.equals(XXX + YYY))) {
                    String ZZZ = getOppo(symbol2, YYY);
                    for (final BookTicker bookTicker3 : list) {
                        final String symbol3 = bookTicker3.getSymbol();
                        if (ig(symbol3)) {
                            continue;
                        }
                        if (symbol3.equals(XXX + ZZZ) || symbol3.equals(ZZZ + XXX)) {
                            // System.out.println(String.format("%s -> %s -> %s", symbol1, symbol2, symbol3));
                            BTC = calc(BTC, XXX, symbol1, bookTicker1, YYY, symbol2, bookTicker2, ZZZ, symbol3, bookTicker3);
                        }
                    }
                }
            }
        }
    }

    final private static BigDecimal VOL = new BigDecimal("5000");
    final private static BigDecimal ProFit = new BigDecimal("0.1"); // % include fee: 0.05% * 3 = 0.15%
    final private static boolean testMode = true;

    private static boolean ig(final String symbol) {
        if (symbol.startsWith("TRIG") || symbol.endsWith("TRIG")) {
            return true;
        }
        BigDecimal vol = tickerStatisticsMap.get(symbol);
        if (vol == null) {
            return true;
        }
        return vol.compareTo(VOL) < 0;
    }

    /**
     * XXXYYY , YYY -> XXX
     * @param symbol
     * @param one
     * @return
     */
    private static String getOppo(String symbol, String one) {
        if (symbol.startsWith(one)) {
            return symbol.substring(one.length());
        }
        if (symbol.endsWith(one)) {
            return symbol.substring(0, symbol.length() - one.length());
        }
        return null;
    }

    private static BigDecimal calc(BigDecimal BTC, String XXX, String symbol1, BookTicker bookTicker1, String YYY, String symbol2, BookTicker bookTicker2, String ZZZ, String symbol3, BookTicker bookTicker3) {
        // 1‰ñ–Ú
        BigDecimal exchange1 = calc(BTC, XXX, YYY, symbol1, bookTicker1);
        if (!checkQty(exchange1, XXX, YYY, symbol1, bookTicker1)) {
            return BTC;
        }
        // 2‰ñ–Ú
        BigDecimal exchange2 = calc(exchange1/*.setScale(findStepSize(symbol2, exchangeInfo), RoundingMode.DOWN)*/, YYY, ZZZ, symbol2, bookTicker2);
        if (!checkQty(exchange2, YYY, ZZZ, symbol2, bookTicker2)) {
            return BTC;
        }
        // 3‰ñ–Ú
        BigDecimal exchange3 = calc(exchange2/*.setScale(findStepSize(symbol3, exchangeInfo), RoundingMode.DOWN)*/, ZZZ, XXX, symbol3, bookTicker3);
        if (!checkQty(exchange3, ZZZ, XXX, symbol3, bookTicker3)) {
            return BTC;
        }
        //System.out.println(String.format("[%s,%s][%s,%s][%s,%s]", symbol1, exchange1, symbol2, exchange2, symbol3, exchange3));
        BigDecimal profitP = exchange3.divide(BTC, 8, RoundingMode.DOWN).subtract(BigDecimal.ONE).multiply(B100);
        if (profitP.compareTo(ProFit) > 0) {

            System.out.print(String.format("[%s] %5s -> %5s -> %5s", BTC.setScale(8, RoundingMode.DOWN), XXX, YYY, ZZZ));
            /*System.out.print("\t");
            System.out.print(exchange1.setScale(8, RoundingMode.DOWN));
            System.out.print("\t");
            System.out.print(exchange2.setScale(8, RoundingMode.DOWN));*/
            System.out.print("\t");
            System.out.print(exchange3.setScale(8, RoundingMode.DOWN));
            System.out.print("\t");
            System.out.print(profitP.setScale(8, RoundingMode.DOWN));
            System.out.println();

            Order order1 = order1(BTC, XXX, YYY, symbol1, bookTicker1, testMode);
            if (!testMode && order1 == null) {
                System.exit(1);
            }
            Order order2 = order1(exchange1, YYY, ZZZ, symbol2, bookTicker2, testMode);
            if (!testMode && order2 == null) {
                System.exit(1);
            }
            Order order3 = order1(exchange2, ZZZ, XXX, symbol3, bookTicker3, testMode);
            if (!testMode && order3 == null) {
                System.exit(1);
            }
            return exchange3;
        }
        return BTC;
    }

    private static BigDecimal calc(BigDecimal coin, String from, String to, String symbol, BookTicker bookTicker) {
        if (symbol.startsWith(from)) {
            // BTCXXX (sell) 
            return coin.multiply(new BigDecimal(bookTicker.getBidPrice()));//.setScale(findStepSize(symbol, exchangeInfo), RoundingMode.DOWN);
        } else if (symbol.startsWith(to)) {
            // XXXBTC (buy XXX) 0.03/0.062352=0.481
            return coin.divide(new BigDecimal(bookTicker.getAskPrice()), findStepSize(symbol, exchangeInfo), RoundingMode.DOWN);
        }
        return null;
    }

    private static boolean checkQty(BigDecimal qty, String from, String to, String symbol, BookTicker bookTicker) {
        if (symbol.startsWith(from)) {
            // System.out.println(String.format("%s %s,%s", symbol, qty, bookTicker.getBidQty()));
            return qty.compareTo(new BigDecimal(bookTicker.getBidQty())) < 0;
        } else if (symbol.startsWith(to)) {
            // System.out.println(String.format("%s %s,%s", symbol, qty, bookTicker.getAskQty()));
            return qty.compareTo(new BigDecimal(bookTicker.getAskQty())) < 0;
        }
        return false;
    }

    private static int findStepSize(String symbol, ExchangeInfo exchangeInfo) {
        SymbolInfo symbolInfo = exchangeInfo.getSymbolInfo(symbol);
        String stepSize = symbolInfo.getSymbolFilter(FilterType.LOT_SIZE).getStepSize();
        return (int) Math.abs(Math.log10(Double.parseDouble(stepSize)));
    }

    private static Order order1(BigDecimal qty, String from, String to, String symbol, BookTicker bookTicker, boolean testMode) {
        NewOrderResponse orderResponse = order2(qty, from, to, symbol, bookTicker, testMode);
        if (orderResponse == null) {
            return null;
        }
        Order order = null;
        do {
            sleep1s();
            order = BinanceClient.me().apiRestClient().getOrderStatus(new OrderStatusRequest(symbol, orderResponse.getOrderId()));
            System.out.println(order);
            if (order == null) {
                return null;
            }
        } while (order.getStatus() != OrderStatus.FILLED);
        return order;
    }

    private static NewOrderResponse order2(BigDecimal qty, String from, String to, String symbol, BookTicker bookTicker, boolean testMode) {
        if (symbol.startsWith(from)) {
            // BTCXXX (sell) 
            final BigDecimal quantity = qty.setScale(findStepSize(symbol, exchangeInfo), RoundingMode.DOWN);
            final String price = bookTicker.getBidPrice();
            System.out.println(String.format("sell %s %s %s", symbol, quantity, price));
            if (testMode) {
                BinanceClient.me().apiRestClient().newOrderTest(NewOrder.limitSell(symbol, TimeInForce.GTC, quantity.toPlainString(), price));
                return null;
            } else {
                return BinanceClient.me().apiRestClient().newOrder(NewOrder.limitSell(symbol, TimeInForce.GTC, quantity.toPlainString(), price));
            }
        } else if (symbol.startsWith(to)) {
            // XXXBTC (buy XXX) 0.03/0.062352=0.481
            final String price = bookTicker.getAskPrice();
            BigDecimal quantity = qty.divide(new BigDecimal(price), findStepSize(symbol, exchangeInfo), RoundingMode.DOWN);
            System.out.println(String.format(" buy %s %s %s", symbol, quantity, price));
            if (testMode) {
                BinanceClient.me().apiRestClient().newOrderTest(NewOrder.limitBuy(symbol, TimeInForce.GTC, quantity.toPlainString(), price));
                return null;
            } else {
                return BinanceClient.me().apiRestClient().newOrder(NewOrder.limitBuy(symbol, TimeInForce.GTC, quantity.toPlainString(), price));
            }
        }
        return null;
    }

    private static final void sleeeeep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final void sleep1s() {
        sleeeeep(1000);
    }
}
