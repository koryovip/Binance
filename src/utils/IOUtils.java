package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class IOUtils {

    private static IOUtils singleton = new IOUtils();

    private IOUtils() {

    }

    public static IOUtils me() {
        return singleton;
    }

    public void write(StringBuilder sb, final String fileName) {
        write(sb.toString(), fileName);
    }

    public void write(String sb, final String fileName) {
        PrintWriter pw = null;
        try {
            //String fileName = "r:/test_out.txt"; // ファイル名
            String charSet = "utf-8"; // 文字コードセット
            int buffer = 1024;
            boolean append = false; // 追加モード
            boolean auto_flush = true; // 自動フラッシュ
            pw = new PrintWriter( //
                    new BufferedWriter( //
                            new OutputStreamWriter( //
                                    new FileOutputStream( //
                                            new File(fileName), append), //
                                    charSet),
                            buffer), // 省略するとシステム標準
                    auto_flush);
            pw.write(sb);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // .. 例外処理
        } finally {
            this.close(pw);
        }
    }

    public StringBuilder read(final String fileName) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            // String fileName = "r:/test_out.txt"; // ファイル名
            String charSet = "utf-8"; // 文字コードセット
            int buffer = 1024;
            br = new BufferedReader( //
                    new InputStreamReader( //
                            new FileInputStream( //
                                    new File(fileName)),
                            charSet),
                    buffer);// 省略するとシステム標準

            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err); // .. 例外処理 }
        } finally {
            this.close(br);
        }
        return sb;
    }

    private void close(BufferedReader br) {
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close(PrintWriter pw) {
        pw.close();
    }
}
