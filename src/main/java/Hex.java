import java.util.Locale;

public class Hex {
    public Hex() {
    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();

        for(int i = 0; i < bs.length; ++i) {
            int bit = (bs[i] & 240) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 15;
            sb.append(chars[bit]);
        }

        return sb.toString().trim();
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];

        for(int i = 0; i < bytes.length; ++i) {
            int n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte)(n & 255);
        }

        return new String(bytes);
    }

    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");

        for(int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 255);
            sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
        }

        return sb.toString().toUpperCase(Locale.ENGLISH).trim();
    }

    public static String makeChecksum(String hexdata) {
        if (hexdata != null && !hexdata.equals("")) {
            hexdata = hexdata.replaceAll(" ", "");
            int total = 0;
            int len = hexdata.length();
            if (len % 2 != 0) {
                return "00";
            } else {
                for(int num = 0; num < len; num += 2) {
                    String s = hexdata.substring(num, num + 2);
                    total += Integer.parseInt(s, 16);
                }

                return Integer.toHexString(total);
            }
        } else {
            return "00";
        }
    }

    public static byte[] hexStr2Bytes(String src) {
        int m = 0;
        int n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];

        for(int i = 0; i < l; ++i) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }

        return ret;
    }

    public static String strToUnicode(String strText) throws Exception {
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < strText.length(); ++i) {
            char c = strText.charAt(i);
            String strHex = Integer.toHexString(c);
            if (c > 128) {
                str.append("\\u" + strHex);
            } else {
                str.append("\\u00" + strHex);
            }
        }

        return str.toString();
    }

    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();

        for(int i = 0; i < t; ++i) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            String s1 = s.substring(2, 4) + "00";
            String s2 = s.substring(4);
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }

        return str.toString();
    }

    public static void main(String[] args) {
        byte[] aa = new byte[]{85, -86};
        System.out.println(byte2HexStr(aa));
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    public static boolean[] hexToBitArray(String hexString) {
        byte[] byteArray = hexStringToBytes(hexString);
        boolean[] bitArray = new boolean[byteArray.length * 8];

        for(int i = 0; i < byteArray.length; ++i) {
            byte b = byteArray[i];

            for(int j = 0; j < 8; ++j) {
                bitArray[i * 8 + j] = (b >> 7 - j & 1) == 1;
            }
        }

        return bitArray;
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static long parseLong(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        } else if (radix < 2) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        } else if (radix > 36) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        } else {
            long result = 0L;
            boolean negative = false;
            int i = 0;
            int len = s.length();
            long limit = -9223372036854775807L;
            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar < '0') {
                    if (firstChar == '-') {
                        negative = true;
                        limit = Long.MIN_VALUE;
                    } else if (firstChar != '+' && len == 1) {
                        ++i;
                    }
                }

                int digit;
                for(long multmin = limit / (long)radix; i < len; result -= (long)digit) {
                    digit = Character.digit(s.charAt(i++), radix);
                    if (digit < 0) {
                    }

                    if (result < multmin) {
                    }

                    result *= (long)radix;
                    if (result < limit + (long)digit) {
                    }
                }
            }

            return negative ? result : -result;
        }
    }

    public static String getStrRemoveLast0(String hexString) {
        if (hexString != null && !hexString.isEmpty()) {
            int length;
            for(length = hexString.length(); length > 0 && hexString.charAt(length - 1) == '0'; --length) {
            }

            return padWithZero(hexString.substring(0, length));
        } else {
            return hexString;
        }
    }

    public static String padWithZero(String str) {
        return str.length() % 2 == 0 ? str : str + "0";
    }

    public static String getHexStrAddLast0(int Alllength, String hexString) {
        if (hexString != null && !hexString.isEmpty() && hexString.length() != Alllength) {
            int length = hexString.length();
            int size = Alllength - length;
            StringBuilder builder = new StringBuilder(hexString);

            for(int i = 0; i < size; ++i) {
                builder.append("0");
            }

            return builder.toString();
        } else {
            return hexString;
        }
    }

    public static String longToHex(long num) {
        byte[] bytes = new byte[]{(byte)((int)num), (byte)((int)(num >> 8)), (byte)((int)(num >> 16)), (byte)((int)(num >> 24))};
        StringBuilder sb = new StringBuilder();
        byte[] var4 = bytes;
        int var5 = bytes.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            byte b = var4[var6];
            String hex = Integer.toHexString(b & 255);
            if (hex.length() < 2) {
                sb.append('0');
            }

            sb.append(hex);
        }

        return sb.toString().toUpperCase();
    }

    public static String longToHexString(long num) {
        byte[] bytes = new byte[]{(byte)((int)(num & 255L)), (byte)((int)(num >> 8 & 255L))};
        return bytesToHexString(bytes);
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static String hexStringToSpacedHexString(String hexString) {
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < hexString.length(); ++i) {
            if (i % 2 == 0 && i != 0) {
                result.append(" ");
            }

            result.append(hexString.charAt(i));
        }

        return result.toString();
    }

    public static float parsingData(String str1, String str2, String str3, String str4) {
        int x1 = Integer.parseInt(str1, 16);
        int x2 = Integer.parseInt(str2, 16);
        int x3 = Integer.parseInt(str3, 16);
        int x4 = Integer.parseInt(str4, 16);
        int xx = x4 << 24 | x3 << 16 | x2 << 8 | x1;
        float x = Float.intBitsToFloat(xx);
        return x;
    }

    public static String getData(float floatValue) {
        int intValue = Float.floatToIntBits(floatValue);
        byte[] bytes = new byte[]{(byte)(intValue & 255), (byte)(intValue >> 8 & 255), (byte)(intValue >> 16 & 255), (byte)(intValue >> 24 & 255)};
        String hexString = bytesToHexString(bytes);
        return hexString;
    }

    public static String getDataLengthHex(String dataHex) {
        int i = dataHex.length() / 2 + 6;
        return String.format("%02X", i);
    }
}
