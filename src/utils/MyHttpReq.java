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
 * http隸?��豎�
 * 
 * @author taihei
 *
 */
public class MyHttpReq {

    /**
     *  GET隸?��豎� (閾?��蜉ｨ隸?��蛻?��譏ｯ蜷?��菴?��逕ｨSSL)
     * @param uri 鄂大捩(螳梧紛霍?��蠕�,蜿?��諡?��謗･蜿よ�??)
     * @param params ?��?��亥�?荳?��null?��?��?��
     * @return
     */
    public static String httpGetReq(String uri, Map<String, Object> params) {
        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//荳?��荳?��蜷域ｳ慕噪鄂大捩閾?���?台?��?��10菴搾?��磯怙莉?��http蠑�螟ｴ?��?��?��
            return "error:URI髱樊ｳ?��";
        }

        uri = paramsHandle4Get(uri, params);

        try {
            URL url = new URL(uri);
            //p蛻?���?��譏ｯ蜷?��髴?��隕�?�ttps隸?��豎�
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("GET");
                urlSslConnection.setRequestProperty("Accept", "application/json");
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {
                // �?灘ｼ?��霑樊�? HttpURLConnection譏ｯURLConnection逧?���?�?��?��
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //鮟�?�?��?��霑泌屓蛟?��譬?��蠑丈ｸ?��json
                urlConnection.setRequestProperty("Accept", "application/json");
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error: 隸?��豎ょ?��?��雍･";
    }

    /**
     * �?��get隸?��豎ら噪蜿よ�?�霑�?���?rl諡?��謗･螟��??��
     * @param uri
     * @param params
     * @return
     */
    private static String paramsHandle4Get(String uri, Map<String, Object> params) {
        // �?��蜿よ�?�霑�?��悟､?���??��
        if (null != params && !params.isEmpty()) {
            Set<String> ks = params.keySet();
            StringBuilder sb = new StringBuilder();
            for (String key : ks) {
                try {
                    sb.append(key + "=" + URLEncoder.encode(params.get(key).toString(), "UTF-8") + "&");//�??��螳?��逕ｨurl郛也�?��
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
     * post隸?��豎�(閾?��蜉ｨ隸?��蛻?��譏ｯ蜷?��逕ｨssl隸?��豎�)
     * 
     * 
     * @param uri  隸?��豎ょ慍蝮?��
     * @param content  �??��螳?��(json譬?��蠑�)
     * @return ?��?��郁�?�隸?��豎ょ?��?��雍･?��?��悟�呵?��泌屓莉?�� "error:"荳?��蠑�螟ｴ逧?���?礼?��?��荳?��?��?��?��
     */
    public static String httpPostReq(String uri, String content) {

        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//荳?��荳?��蜷域ｳ慕噪鄂大捩閾?���?台?��?��10菴搾?��磯怙莉?��http蠑�螟ｴ?��?��?��
            return "error:URI髱樊ｳ?��";
        }

        try {
            URL url = new URL(uri);
            //蛻?���?��譏ｯ蜷?��隕∵?��?��ttps隸?��豎�
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("POST");
                urlSslConnection.setDoInput(true);
                urlSslConnection.setDoOutput(true);
                urlSslConnection.setUseCaches(false);// Post 隸?��豎ゆ?��堺?��?��逕ｨ郛灘?��?��
                urlSslConnection.setRequestProperty("Accept", "application/json");//莨伜�域磁謾?��json謨?��謐ｮ
                urlSslConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//form陦?��蜊墓署�??��
                // 螯よ棡髴?��隕�?�書騾∵焚謐ｮ
                if (null != content && content.trim().length() > 0) {
                    PrintWriter printWriter = new PrintWriter(urlSslConnection.getOutputStream());
                    printWriter.write(content);
                    // flush霎灘?��?��豬∫噪郛灘?��?��
                    printWriter.flush();
                    printWriter.close();
                }
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {

                // �?灘ｼ?��霑樊�? HttpURLConnection譏ｯURLConnection逧?���?�?��?��
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                // conn.setConnectTimeout(10000);//霑樊�?雜�譌ｶ 蜊穂ｽ肴?��?��遘�
                // conn.setReadTimeout(2000);//隸?��蜿冶?��?��譌ｶ 蜊穂ｽ肴?��?��遘�
                // 蜿鷹?��?��OST隸?��豎ょ?��?��鬘ｻ隶?��鄂ｮ螯�??��倶?��?��陦?��
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);// Post 隸?��豎ゆ?��堺?��?��逕ｨ郛灘?��?��
                //              urlConnection.setRequestProperty("Accept", "application/xhtml+xml");//xml譬?��蠑剰?��泌屓蛟?��
                //鮟�?�?��?��霑泌屓蛟?��譬?��蠑丈ｸ?��json
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                // 螯よ棡髴?��隕�?�書騾∵焚謐ｮ
                if (null != content && content.length() > 0) {
                    // 闔ｷ蜿剖RLConnection�?��雎｡�?��蠎皮噪霎灘?��?��豬?��
                    PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
                    // 蜿鷹?��∬?��?��豎ょ盾謨?��
                    printWriter.write(content);
                    // flush霎灘?��?��豬∫噪郛灘?��?��
                    printWriter.flush();
                }
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error:隸?��豎ょ?��?��雍･";
    }

    /**
     * 隸?��蜿冶?��灘�?��豬∝�?��逧?��謨?��謐ｮ
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
     * post隸?��豎�(閾?��蜉ｨ隸?��蛻?��譏ｯ蜷?��逕ｨssl隸?��豎�)
     * 
     * @param uri  隸?��豎ょ慍蝮?��
     * @param params 蜿よ�?? 蜿?��荳?��null
     * @return ?��?��郁�?�隸?��豎ょ?��?��雍･?��?��悟�呵?��泌屓莉?�� "error:"荳?��蠑�螟ｴ逧?���?礼?��?��荳?��?��?��?��
     */
    public static String httpPostReq(String uri, Map<String, Object> params) {

        if (null == uri || uri.replace(" ", "").length() < 10 || (!uri.startsWith("http://") && !uri.startsWith("https://"))) {//荳?��荳?��蜷域ｳ慕噪鄂大捩閾?���?台?��?��10菴搾?��磯怙莉?��http蠑�螟ｴ?��?��?��
            return "error:URI髱樊ｳ?��";
        }

        try {
            URL url = new URL(uri);
            //蛻?���?��譏ｯ蜷?��隕∵?��?��ttps隸?��豎�
            if (uri.startsWith("https://")) {
                SSLSocketFactory sslf = defaultSSLFactory();
                HttpsURLConnection urlSslConnection = (HttpsURLConnection) url.openConnection();
                urlSslConnection.setSSLSocketFactory(sslf);
                urlSslConnection.setRequestMethod("POST");
                urlSslConnection.setDoInput(true);
                urlSslConnection.setDoOutput(true);
                urlSslConnection.setUseCaches(false);// Post 隸?��豎ゆ?��堺?��?��逕ｨ郛灘?��?��
                urlSslConnection.setRequestProperty("Accept", "application/json");//莨伜�域磁謾?��json謨?��謐ｮ
                urlSslConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//form陦?��蜊墓署�??��
                // 螯よ棡髴?��隕�?�書騾∵焚謐ｮ
                if (null != params && !params.isEmpty()) {
                    PrintWriter printWriter = new PrintWriter(urlSslConnection.getOutputStream());
                    printWriter.write(params.toString().replaceAll("[{|}]", ""));
                    // flush霎灘?��?��豬∫噪郛灘?��?��
                    printWriter.flush();
                    printWriter.close();
                }
                return returnDataHandle4UrlSSLConnection(urlSslConnection);
            } else {

                // �?灘ｼ?��霑樊�? HttpURLConnection譏ｯURLConnection逧?���?�?��?��
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                // conn.setConnectTimeout(10000);//霑樊�?雜�譌ｶ 蜊穂ｽ肴?��?��遘�
                // conn.setReadTimeout(2000);//隸?��蜿冶?��?��譌ｶ 蜊穂ｽ肴?��?��遘�
                // 蜿鷹?��?��OST隸?��豎ょ?��?��鬘ｻ隶?��鄂ｮ螯�??��倶?��?��陦?��
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setUseCaches(false);// Post 隸?��豎ゆ?��堺?��?��逕ｨ郛灘?��?��
                //              urlConnection.setRequestProperty("Accept", "application/xhtml+xml");//xml譬?��蠑剰?��泌屓蛟?��
                //鮟�?�?��?��霑泌屓蛟?��譬?��蠑丈ｸ?��json
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                // 螯よ棡髴?��隕�?�書騾∵焚謐ｮ
                if (null != params && !params.isEmpty()) {
                    // 闔ｷ蜿剖RLConnection�?��雎｡�?��蠎皮噪霎灘?��?��豬?��
                    PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
                    // 蜿鷹?��∬?��?��豎ょ盾謨?��
                    printWriter.write(params.toString().replaceAll("[{|}]", ""));
                    // flush霎灘?��?��豬∫噪郛灘?��?��
                    printWriter.flush();
                }
                return returnDataHandle4UrlConnection(urlConnection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "error:隸?��豎ょ?��?��雍･";
    }

    /**
     * 螟��??��https隸?��豎り?��泌屓蛟?��
     * @param urlSslConnection
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static String returnDataHandle4UrlSSLConnection(HttpsURLConnection urlSslConnection) throws IOException, UnsupportedEncodingException {
        if (200 == urlSslConnection.getResponseCode()) {
            // 蠕�?�芦霎灘?��?��豬?��
            InputStream is = urlSslConnection.getInputStream();
            ByteArrayOutputStream baos = getInputDataFromInputStrem(is);
            urlSslConnection.disconnect();
            return baos.toString("utf-8");
        } else {
            return "error: " + urlSslConnection.getResponseCode();
        }
    }

    /**
     * 螟��??��http隸?��豎り?��泌�?
     * @param urlConnection
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private static String returnDataHandle4UrlConnection(HttpURLConnection urlConnection) throws IOException, UnsupportedEncodingException {
        if (200 == urlConnection.getResponseCode()) {
            // 蠕�?�芦霎灘?��?��豬?��
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = getInputDataFromInputStrem(is);
            urlConnection.disconnect();// 謨?��謐ｮ隸?��蜿�??��梧?��募�?��髣?��豬?��
            return baos.toString("utf-8");
        } else {
            return "error: " + urlConnection.getResponseCode();
        }
    }

    /**
     * 蛻帛ｻ?��荳?��荳?��鮟�?�?��?��逧гsl蟾?��蜴?��,(謗･蜿玲園譛芽?��∽?��?��)
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