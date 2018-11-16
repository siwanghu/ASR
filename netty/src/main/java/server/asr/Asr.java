package server.asr;

import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class Asr {
    private final String appKey = "U17XQyDGliK7A3XFVtxir0sO";

    private final String secretKey = "rpiTAMs7OqHb6RYWEknVWU0uWPFII3OM";

    private  String filename;

    private final String format = "pcm";

    private final int dev_pid = 1537;

    private String cuid = "1234567JAVA";

    private final int rate = 16000;

    public boolean methodRaw = false;

    private final String url = "http://vop.baidu.com/server_api";

    public String asr(String filename) throws IOException, DemoException {
        this.filename=filename;
        String result = this.run();
        System.out.println("识别结束：结果是：");
        System.out.println(result);
        return result;
    }

    private String run() throws IOException, DemoException {
        TokenHolder holder = new TokenHolder(appKey, secretKey, TokenHolder.ASR_SCOPE);
        holder.resfresh();
        String token = holder.getToken();
        String result = null;
        if (methodRaw) {
            result = runRawPostMethod(token);
        } else {
            result = runJsonPostMethod(token);
        }
        return result;
    }

    private String runRawPostMethod(String token) throws IOException, DemoException {
        String url2 = url + "?cuid=" + ConnUtil.urlEncode(cuid) + "&dev_pid=" + dev_pid + "&token=" + token;
        byte[] content = getFileContent(filename);
        HttpURLConnection conn = (HttpURLConnection) new URL(url2).openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Content-Type", "audio/" + format + "; rate=" + rate);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(content);
        conn.getOutputStream().close();
        String result = ConnUtil.getResponseString(conn);
        return result;
    }

    private String runJsonPostMethod(String token) throws DemoException, IOException {
        byte[] content = getFileContent(filename);
        String speech = base64Encode(content);
        JSONObject params = new JSONObject();
        params.put("dev_pid", dev_pid);
        params.put("format", format);
        params.put("rate", rate);
        params.put("token", token);
        params.put("cuid", cuid);
        params.put("channel", "1");
        params.put("len", content.length);
        params.put("speech", speech);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setDoOutput(true);
        conn.getOutputStream().write(params.toString().getBytes());
        conn.getOutputStream().close();
        String result = ConnUtil.getResponseString(conn);
        return result;
    }

    private byte[] getFileContent(String filename) throws DemoException, IOException {
        File file = new File(filename);
        if (!file.canRead()) {
            throw new DemoException("file cannot read: " + file.getAbsolutePath());
        }
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            return ConnUtil.getInputStreamContent(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String base64Encode(byte[] content) {
         Base64.Encoder encoder = Base64.getEncoder();
         String str = encoder.encodeToString(content);
        return str;
    }
}
