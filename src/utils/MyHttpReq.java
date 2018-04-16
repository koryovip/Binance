package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * httpéš¸?½·è±ï¿½
 * 
 * @author taihei
 *
 */
public class MyHttpReq {

    /**
     *  GETéš¸?½·è±ï¿½ (é–¾?½ªèœ‰ï½¨éš¸?¿½è›»?½«è­ï½¯èœ·?½¦è´?½¿é€•ï½¨SSL)
     * @param uri é„‚å¤§æ©(è³æ¢§ç´›éœ?½¯è •ï¿½,èœ¿?½¯è«¡?½¼è¬—ï½¥èœ¿ã‚ˆç??)
     * @param params ?¿½?½¼äº¥åº?è³?½ºnull?¿½?½¼?¿½
     * @return
     */
    public static String httpGetReq(String uri, Map<String, Object> params) {
        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//è³?¿½è³?½ªèœ·åŸŸï½³æ…•å™ªé„‚å¤§æ©é–¾?½³èŸ?å°?½¸?½º10è´æ¾?½¼ç£¯æ€™è‰?½¥httpè ‘ï¿½èŸï½´?¿½?½¼?¿½
            return "error:URIé«±æ¨Šï½³?¿½";
        }

        uri = paramsHandle4Get(uri, params);

        try {
            URL url = new URL(uri);
            //pè›»?½¤è­?½­è­ï½¯èœ·?½¦é«´?¿½éš•â?ttpséš¸?½·è±ï¿½
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("GET");
                urlSslConnection.setRequestProperty("Accept", "application/json");
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {
                // è¬?ç˜ï½¼?¿½éœ‘æ¨Šç£? HttpURLConnectionè­ï½¯URLConnectioné€§?¿½èŸ?å?½±?½»
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //é®Ÿå?©?½®?½¤éœ‘æ³Œå±“è›Ÿ?½¼è­¬?½¼è ‘ä¸ˆï½¸?½ºjson
                urlConnection.setRequestProperty("Accept", "application/json");
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error: éš¸?½·è±ã‚‡?½¤?½±é›ï½¥";
    }

    /**
     * èŸ?½¹getéš¸?½·è±ã‚‰å™ªèœ¿ã‚ˆç?šéœ‘å¹?½¡è¢?rlè«¡?½¼è¬—ï½¥èŸï¿½é€??¿½
     * @param uri
     * @param params
     * @return
     */
    private static String paramsHandle4Get(String uri, Map<String, Object> params) {
        // èŸ?½¹èœ¿ã‚ˆç?šéœ‘å¹?½¡æ‚Ÿï½¤?¿½é€??¿½
        if (null != params && !params.isEmpty()) {
            Set<String> ks = params.keySet();
            StringBuilder sb = new StringBuilder();
            for (String key : ks) {
                try {
                    sb.append(key + "=" + URLEncoder.encode(params.get(key).toString(), "UTF-8") + "&");//èœ??¿½è³?½¹é€•ï½¨urléƒ›ä¹Ÿï¿½?¿½
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            String pj = sb.substring(0, sb.length() - 1);
            if (pj.length() > 0) {
                if (uri.contains("?")) {
                    uri = uri + "&" + pj;
                } else {
                    uri = uri + "?" + pj;
                }
            }
        }
        return uri;
    }

    /**
     * 
     * postéš¸?½·è±ï¿½(é–¾?½ªèœ‰ï½¨éš¸?¿½è›»?½«è­ï½¯èœ·?½¦é€•ï½¨ssléš¸?½·è±ï¿½)
     * 
     * 
     * @param uri  éš¸?½·è±ã‚‡æ…è®?¿½
     * @param content  èœ??¿½è³?½¹(jsonè­¬?½¼è ‘ï¿½)
     * @return ?¿½?½¼éƒå?¶éš¸?½·è±ã‚‡?½¤?½±é›ï½¥?¿½?½¼æ‚Ÿï¿½å‘µ?½¿æ³Œå±“è‰?½¥ "error:"è³?½ºè ‘ï¿½èŸï½´é€§?¿½èŸ?ç¤¼?½¬?½¦è³?½²?¿½?½¼?¿½
     */
    public static String httpPostReq(String uri, String content) {

        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//è³?¿½è³?½ªèœ·åŸŸï½³æ…•å™ªé„‚å¤§æ©é–¾?½³èŸ?å°?½¸?½º10è´æ¾?½¼ç£¯æ€™è‰?½¥httpè ‘ï¿½èŸï½´?¿½?½¼?¿½
            return "error:URIé«±æ¨Šï½³?¿½";
        }

        try {
            URL url = new URL(uri);
            //è›»?½¤è­?½­è­ï½¯èœ·?½¦éš•âˆµ?½±?¼©ttpséš¸?½·è±ï¿½
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("POST");
                urlSslConnection.setDoInput(true);
                urlSslConnection.setDoOutput(true);
                urlSslConnection.setUseCaches(false);// Post éš¸?½·è±ã‚†?½¸å º?½½?½¿é€•ï½¨éƒ›ç˜?½­?¿½
                urlSslConnection.setRequestProperty("Accept", "application/json");//è¨ä¼œï¿½åŸŸç£è¬¾?½¶jsonè¬¨?½°è¬ï½®
                urlSslConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//formé™¦?½¨èœŠå¢“ç½²è??½¤
                // è¯ã‚ˆæ£¡é«´?¿½éš•â?æ›¸é¨¾âˆµç„šè¬ï½®
                if (null != content && content.trim().length() > 0) {
                    PrintWriter printWriter = new PrintWriter(urlSslConnection.getOutputStream());
                    printWriter.write(content);
                    // flushéœç˜?¿½?½ºè±¬âˆ«å™ªéƒ›ç˜?¿½?½²
                    printWriter.flush();
                    printWriter.close();
                }
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {

                // è¬?ç˜ï½¼?¿½éœ‘æ¨Šç£? HttpURLConnectionè­ï½¯URLConnectioné€§?¿½èŸ?å?½±?½»
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                // conn.setConnectTimeout(10000);//éœ‘æ¨Šç£?é›œï¿½è­Œï½¶ èœŠç©‚ï½½è‚´?½¯?½«é˜ï¿½
                // conn.setReadTimeout(2000);//éš¸?½»èœ¿å†¶?½¶?¿½è­Œï½¶ èœŠç©‚ï½½è‚´?½¯?½«é˜ï¿½
                // èœ¿é·¹?¿½?¿£OSTéš¸?½·è±ã‚‡?½¿?¿½é¬˜ï½»éš¶?½¾é„‚ï½®è¯ã‚??½¸å€¶?½¸?½¤é™¦?¿½
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);// Post éš¸?½·è±ã‚†?½¸å º?½½?½¿é€•ï½¨éƒ›ç˜?½­?¿½
                //              urlConnection.setRequestProperty("Accept", "application/xhtml+xml");//xmlè­¬?½¼è ‘å‰°?½¿æ³Œå±“è›Ÿ?½¼
                //é®Ÿå?©?½®?½¤éœ‘æ³Œå±“è›Ÿ?½¼è­¬?½¼è ‘ä¸ˆï½¸?½ºjson
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                // è¯ã‚ˆæ£¡é«´?¿½éš•â?æ›¸é¨¾âˆµç„šè¬ï½®
                if (null != content && content.length() > 0) {
                    // é—”ï½·èœ¿å‰–RLConnectionèŸ?½¹é›ï½¡èŸ?½¹è çš®å™ªéœç˜?¿½?½ºè±¬?¿½
                    PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
                    // èœ¿é·¹?¿½âˆ¬?½¯?½·è±ã‚‡ç›¾è¬¨?½°
                    printWriter.write(content);
                    // flushéœç˜?¿½?½ºè±¬âˆ«å™ªéƒ›ç˜?¿½?½²
                    printWriter.flush();
                }
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error:éš¸?½·è±ã‚‡?½¤?½±é›ï½¥";
    }

    /**
     * éš¸?½»èœ¿å†¶?½¾ç˜ï¿½?½¥è±¬âˆï¿½?¿½é€§?¿½è¬¨?½°è¬ï½®
     * @param is
     * @return
     * @throws IOException
     */
    private static ByteArrayOutputStream getInputDataFromInputStrem(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while (-1 != (len = is.read(buffer))) {
            baos.write(buffer, 0, len);
            baos.flush();
        }
        is.close();
        return baos;
    }

    /**
     * postéš¸?½·è±ï¿½(é–¾?½ªèœ‰ï½¨éš¸?¿½è›»?½«è­ï½¯èœ·?½¦é€•ï½¨ssléš¸?½·è±ï¿½)
     * 
     * @param uri  éš¸?½·è±ã‚‡æ…è®?¿½
     * @param params èœ¿ã‚ˆç?? èœ¿?½¯è³?½ºnull
     * @return ?¿½?½¼éƒå?¶éš¸?½·è±ã‚‡?½¤?½±é›ï½¥?¿½?½¼æ‚Ÿï¿½å‘µ?½¿æ³Œå±“è‰?½¥ "error:"è³?½ºè ‘ï¿½èŸï½´é€§?¿½èŸ?ç¤¼?½¬?½¦è³?½²?¿½?½¼?¿½
     */
    public static String httpPostReq(String uri, Map<String, Object> params) {

        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//è³?¿½è³?½ªèœ·åŸŸï½³æ…•å™ªé„‚å¤§æ©é–¾?½³èŸ?å°?½¸?½º10è´æ¾?½¼ç£¯æ€™è‰?½¥httpè ‘ï¿½èŸï½´?¿½?½¼?¿½
            return "error:URIé«±æ¨Šï½³?¿½";
        }

        try {
            URL url = new URL(uri);
            //è›»?½¤è­?½­è­ï½¯èœ·?½¦éš•âˆµ?½±?¼©ttpséš¸?½·è±ï¿½
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("POST");
                urlSslConnection.setDoInput(true);
                urlSslConnection.setDoOutput(true);
                urlSslConnection.setUseCaches(false);// Post éš¸?½·è±ã‚†?½¸å º?½½?½¿é€•ï½¨éƒ›ç˜?½­?¿½
                urlSslConnection.setRequestProperty("Accept", "application/json");//è¨ä¼œï¿½åŸŸç£è¬¾?½¶jsonè¬¨?½°è¬ï½®
                urlSslConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//formé™¦?½¨èœŠå¢“ç½²è??½¤
                // è¯ã‚ˆæ£¡é«´?¿½éš•â?æ›¸é¨¾âˆµç„šè¬ï½®
                if (null != params && !params.isEmpty()) {
                    PrintWriter printWriter = new PrintWriter(urlSslConnection.getOutputStream());
                    printWriter.write(params.toString().replaceAll("[{|}]", ""));
                    // flushéœç˜?¿½?½ºè±¬âˆ«å™ªéƒ›ç˜?¿½?½²
                    printWriter.flush();
                    printWriter.close();
                }
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {

                // è¬?ç˜ï½¼?¿½éœ‘æ¨Šç£? HttpURLConnectionè­ï½¯URLConnectioné€§?¿½èŸ?å?½±?½»
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                // conn.setConnectTimeout(10000);//éœ‘æ¨Šç£?é›œï¿½è­Œï½¶ èœŠç©‚ï½½è‚´?½¯?½«é˜ï¿½
                // conn.setReadTimeout(2000);//éš¸?½»èœ¿å†¶?½¶?¿½è­Œï½¶ èœŠç©‚ï½½è‚´?½¯?½«é˜ï¿½
                // èœ¿é·¹?¿½?¿£OSTéš¸?½·è±ã‚‡?½¿?¿½é¬˜ï½»éš¶?½¾é„‚ï½®è¯ã‚??½¸å€¶?½¸?½¤é™¦?¿½
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);// Post éš¸?½·è±ã‚†?½¸å º?½½?½¿é€•ï½¨éƒ›ç˜?½­?¿½
                //              urlConnection.setRequestProperty("Accept", "application/xhtml+xml");//xmlè­¬?½¼è ‘å‰°?½¿æ³Œå±“è›Ÿ?½¼
                //é®Ÿå?©?½®?½¤éœ‘æ³Œå±“è›Ÿ?½¼è­¬?½¼è ‘ä¸ˆï½¸?½ºjson
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                // è¯ã‚ˆæ£¡é«´?¿½éš•â?æ›¸é¨¾âˆµç„šè¬ï½®
                if (null != params && !params.isEmpty()) {
                    // é—”ï½·èœ¿å‰–RLConnectionèŸ?½¹é›ï½¡èŸ?½¹è çš®å™ªéœç˜?¿½?½ºè±¬?¿½
                    PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
                    // èœ¿é·¹?¿½âˆ¬?½¯?½·è±ã‚‡ç›¾è¬¨?½°
                    printWriter.write(params.toString().replaceAll("[{|}]", ""));
                    // flushéœç˜?¿½?½ºè±¬âˆ«å™ªéƒ›ç˜?¿½?½²
                    printWriter.flush();
                }
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error:éš¸?½·è±ã‚‡?½¤?½±é›ï½¥";
    }

    /**
     * èŸï¿½é€??¿½httpséš¸?½·è±ã‚Š?½¿æ³Œå±“è›Ÿ?½¼
     * @param urlSslConnection
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static String returnDataHandle4UrlSSLConnection(HttpsURLConnection urlSslConnection) throws IOException, UnsupportedEncodingException {
        if (200 == urlSslConnection.getResponseCode()) {
            // è •æ?œèŠ¦éœç˜?¿½?½¥è±¬?¿½
            InputStream is = urlSslConnection.getInputStream();
            ByteArrayOutputStream baos = getInputDataFromInputStrem(is);
            urlSslConnection.disconnect();
            return baos.toString("utf-8");
        } else {
            return "error: " + urlSslConnection.getResponseCode();
        }
    }

    /**
     * èŸï¿½é€??¿½httpéš¸?½·è±ã‚Š?½¿æ³Œå±?
     * @param urlConnection
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static String returnDataHandle4UrlConnection(HttpURLConnection urlConnection) throws IOException, UnsupportedEncodingException {
        if (200 == urlConnection.getResponseCode()) {
            // è •æ?œèŠ¦éœç˜?¿½?½¥è±¬?¿½
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = getInputDataFromInputStrem(is);
            urlConnection.disconnect();// è¬¨?½°è¬ï½®éš¸?½»èœ¿é–??½®æ¢§?½¯å‹Ÿï¿½?½³é«£?½­è±¬?¿½
            return baos.toString("utf-8");
        } else {
            return "error: " + urlConnection.getResponseCode();
        }
    }

    /**
     * è›»å¸›ï½»?½ºè³?¿½è³?½ªé®Ÿå?©?½®?½¤é€§Ğ³slèŸ¾?½¥èœ´?¿½,(è¬—ï½¥èœ¿ç²åœ’è­›èŠ½?½¯âˆ½?½¹?½¦)
     * @return
     */
    public static SSLSocketFactory defaultSSLFactory() {
        X509TrustManager x509TrustManager = new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {

            }
        };

        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { x509TrustManager }, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 0 = Scrypt
    1 = SHA256
    2 = ScryptNf
    3 = X11
    4 = X13
    5 = Keccak
    6 = X15
    7 = Nist5
    8 = NeoScrypt
    9 = Lyra2RE
    10 = WhirlpoolX
    11 = Qubit
    12 = Quark
    13 = Axiom
    14 = Lyra2REv2
    15 = ScryptJaneNf16
    16 = Blake256r8
    17 = Blake256r14
    18 = Blake256r8vnl
    19 = Hodl
    20 = DaggerHashimoto
    21 = Decred
    22 = CryptoNight
    23 = Lbry
    24 = Equihash
    25 = Pascal
    26 = X11Gost
    27 = Sia
    28 = Blake2s
    29 = Skunk
     */
}