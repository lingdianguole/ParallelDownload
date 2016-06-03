package jc.download;

public class DownloadException extends Exception {

    int code;
    String msg;
    String str;

    public DownloadException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() {
        return code;
    }

    public String toString() {
        if (str == null) {
            str = new StringBuilder().append("code: ").append(code).append(", msg: ").append(msg).toString();
        }
        return str;
    }
}
