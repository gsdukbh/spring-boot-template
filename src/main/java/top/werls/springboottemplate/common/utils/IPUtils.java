package top.werls.springboottemplate.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * IP地址工具类，提供获取客户端真实IP地址和判断内网IP的功能
 *
 * @author Jiawei Lee
 * @version TODO
 * @date created 2022/7/12
 * @since on
 */
@Slf4j
public class IPUtils {

    /**
     * 获取客户端真实IP地址
     * 通过多种HTTP头部信息来获取客户端的真实IP地址，支持代理和负载均衡环境
     *
     * @param request HTTP请求对象
     * @return 客户端的真实IP地址，如果无法获取则返回"127.0.0.1"
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        try {
            // 1. 尝试从 x-forwarded-for 头部获取IP（最常用的代理头部）
            ip = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                // 2. 尝试从 Proxy-Client-IP 头部获取IP
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                // 3. 尝试从 WL-Proxy-Client-IP 头部获取IP（WebLogic代理）
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                // 4. 尝试从 HTTP_CLIENT_IP 头部获取IP
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                // 5. 尝试从 HTTP_X_FORWARDED_FOR 头部获取IP
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
                // 6. 如果以上都没有获取到，则直接从请求中获取远程地址
                ip = request.getRemoteAddr();
            }

            // 处理多IP的情况（当经过多层代理时，IP可能是逗号分隔的多个值）
            String sIP = null;
            if (ip != null && !ip.contains("unknown") && ip.indexOf(",") > 0) {
                String[] ipsz = ip.split(",");
                // 遍历所有IP，优先选择非内网IP
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

            // 清理可能存在的"unknown"字符串
            if (ip != null && ip.contains("unknown")){
                ip = ip.replaceAll("unknown,", "");
                ip = ip.trim();
            }

            // 如果最终还是没有获取到有效IP，则使用本地回环地址
            if (StringUtils.isEmpty(ip)){
                ip = "127.0.0.1";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ip;
    }

    /**
     * 判断给定的IP地址是否为内网IP
     *
     * @param ipAddress 要检查的IP地址字符串
     * @return 如果是内网IP返回true，否则返回false
     */
    public static boolean isInnerIP(String ipAddress) {
        boolean isInnerIp;
        long ipNum = getIpNum(ipAddress);
        /**
         私有IP：A类  10.0.0.0-10.255.255.255
         B类  172.16.0.0-172.31.255.255
         C类  192.168.0.0-192.168.255.255
         当然，还有127这个网段是环回地址
         **/
        // A类私有IP范围
        long aBegin = getIpNum("10.0.0.0");
        long aEnd = getIpNum("10.255.255.255");

        // B类私有IP范围
        long bBegin = getIpNum("172.16.0.0");
        long bEnd = getIpNum("172.31.255.255");

        // C类私有IP范围
        long cBegin = getIpNum("192.168.0.0");
        long cEnd = getIpNum("192.168.255.255");

        // 判断是否在私有IP范围内或是本地回环地址
        isInnerIp = isInner(ipNum, aBegin, aEnd) || isInner(ipNum, bBegin, bEnd) || isInner(ipNum, cBegin, cEnd)
                || ipAddress.equals("127.0.0.1");
        return isInnerIp;
    }

    /**
     * 判断IP地址数值是否在指定范围内
     *
     * @param userIp 用户IP的数值表示
     * @param begin  范围起始值
     * @param end    范围结束值
     * @return 如果在范围内返回true，否则返回false
     */
    private static boolean isInner(long userIp, long begin, long end) {
        return (userIp >= begin) && (userIp <= end);
    }

    /**
     * 将IP地址字符串���换为数值表示
     * 将点分十进制的IP地址转换为长整型数值，便于进行范围比较
     *
     * @param ipAddress 点分十进制格式的IP地址（如：192.168.1.1）
     * @return IP地址的数值表示
     */
    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);

        // 计算公式：a*256³ + b*256² + c*256¹ + d*256⁰
        return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
    }
}
