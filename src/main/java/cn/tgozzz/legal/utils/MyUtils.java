package cn.tgozzz.legal.utils;

import org.springframework.stereotype.Component;

@Component
public class MyUtils {
    /**
     * 字节数据转字符串专用集合
     */
    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    /**
     * 合并byte数组
     */
    public static byte[] byteMergerAll(byte[]... values) {
        int length_byte = 0;
        for (byte[] value : values) {
            length_byte += value.length;
        }
        byte[] all_byte = new byte[length_byte];
        int countLength = 0;
        for (byte[] b : values) {
            System.arraycopy(b, 0, all_byte, countLength, b.length);
            countLength += b.length;
        }
        return all_byte;
    }

    /**
     *  * 将byte转为16进制
     */
    public static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 字节数据转十六进制字符串
     *
     * @param data
     *            输入数据
     * @return 十六进制内容
     */
    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            // 取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            // 取出字节的低四位 作为索引得到相应的十六进制标识符
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }
}
