package jc.download.util;

public class Util {

    public static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'a','b','c','d','e','f' };
        char[] resultCharArray =new char[byteArray.length * 2];
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    private static byte charToByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }

    public static String hexToString(String hexString) {
        char[] encodedChar = hexString.toCharArray();
        byte[] decodedByte = new byte[encodedChar.length / 2];
        int index = 0;
        for (int i = 0; i < encodedChar.length; i++) {
            byte high = (byte)(charToByte(encodedChar[i]) << 4);
            i++;
            byte low = charToByte(encodedChar[i]);
            decodedByte[index++] = (byte)(high | low);
        }
        String str = new String(decodedByte);
        return str;
    }

    public static void chmod(String filePath) {
        String permission = "777";
        try {
            String command = "chmod " + permission + " " + filePath;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
