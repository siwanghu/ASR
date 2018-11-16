package server.asr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class ConnUtil {
    public static String urlEncode(String str) {
        String result = null;
        try {
            result = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getResponseString(HttpURLConnection conn) throws IOException, DemoException {
        return new String(getResponseBytes(conn));
    }

    public static byte[] getResponseBytes(HttpURLConnection conn) throws IOException, DemoException {
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("http 请求返回的状态码错误，期望200， 当前是 " + responseCode);
            if (responseCode == 401) {
                System.err.println("可能是appkey appSecret 填错");
            }
            throw new DemoException("http response code is" + responseCode);
        }

        InputStream inputStream = conn.getInputStream();
        byte[] result = getInputStreamContent(inputStream);
        return result;
    }

    public static byte[] getInputStreamContent(InputStream is) throws IOException {
        byte[] b = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len = 0;
        while (true) {
            len = is.read(b);
            if (len == -1) {
                break;
            }
            byteArrayOutputStream.write(b, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
