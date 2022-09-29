package top.werls.springboottemplate.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/12
 * @since on
 */
@Slf4j
public class IPUtils {
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            String sIP = null;
            if (ip != null && !ip.contains("unknown") && ip.indexOf(",") > 0) {
                String[] ipsz = ip.split(",");
                for (String anIpsz : ipsz) {
                    if (!isInnerIP(anIpsz.trim())) {
                        sIP = anIpsz.trim();
                        break;
                    }
                }
                /*
                 * 如果多ip都是内网ip，则取第一个ip.
                 */
                if (null == sIP) {
                    sIP = ipsz[0].trim();
                }
                ip = sIP;
            }
            if (ip != null && ip.contains("unknown")){
                ip =ip.replaceAll("unknown,", "");
                ip = ip.trim();
            }
            if (StringUtils.isEmpty(ip)){
                ip = "127.0.0.1";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ip;
    }

    public static boolean isInnerIP(String ipAddress) {
        boolean isInnerIp;
        long ipNum = getIpNum(ipAddress);
        /**
         私有IP：A类  10.0.0.0-10.255.255.255
         B类  172.16.0.0-172.31.255.255
         C类  192.168.0.0-192.168.255.255
         当然，还有127这个网段是环回地址
         **/
        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");

        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");

        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");
        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd)
                || ipAddress.equals("127.0.0.1");
        return isInnerIp;
    }
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);

        return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
    }

}
