package top.werls.springboottemplate.common.utils.dynamicCompilation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 代码安全检查器
 * 提供多层次的安全检查机制，防止恶意代码执行
 *
 * @author Li JiaWei
 * @version 1.0
 * @since 2025-08-21
 */
public class SecurityChecker {

    /** 危险类名的正则表达式 */
    private static final Set<Pattern> DANGEROUS_PATTERNS = new HashSet<>();

    /** 危险关键字 */
    private static final Set<String> DANGEROUS_KEYWORDS = new HashSet<>();

    /** 允许的包前缀 */
    private static final Set<String> ALLOWED_PACKAGES = new HashSet<>();

    static {
        // 初始化危险模式
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.reflect", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.io", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.net", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.Runtime", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.Process", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.System", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.Thread", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.ClassLoader", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("javax\\.tools", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("sun\\.misc\\.Unsafe", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("java\\.lang\\.invoke", Pattern.CASE_INSENSITIVE));

        // 动态字符串拼接检测
        DANGEROUS_PATTERNS.add(Pattern.compile("Class\\.forName", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("getClass\\(\\)", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("getDeclaredMethod", Pattern.CASE_INSENSITIVE));
        DANGEROUS_PATTERNS.add(Pattern.compile("getMethod", Pattern.CASE_INSENSITIVE));

        // 危险关键字
        DANGEROUS_KEYWORDS.add("native");
        DANGEROUS_KEYWORDS.add("synchronized");
        DANGEROUS_KEYWORDS.add("volatile");

        // 允许的包前缀
        ALLOWED_PACKAGES.add("java.lang.String");
        ALLOWED_PACKAGES.add("java.lang.Math");
        ALLOWED_PACKAGES.add("java.util.List");
        ALLOWED_PACKAGES.add("java.util.Map");
        ALLOWED_PACKAGES.add("java.util.Set");
        ALLOWED_PACKAGES.add("java.util");
        ALLOWED_PACKAGES.add("java.time");
        ALLOWED_PACKAGES.add("java.math");
    }

    /**
     * 执行全面的安全检查
     *
     * @param code 待检查的源代码
     * @return 检查结果
     */
    public static SecurityCheckResult checkCode(String code) {
        SecurityCheckResult result = new SecurityCheckResult();

        // 0. 包名检查 - 只允许dynamicCompilation包
        if (!isAllowedPackage(code)) {
            result.addViolation("只允许dynamicCompilation包下的类");
        }

        // 1. 基础模式匹配检查
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(code).find()) {
                result.addViolation("检测到危险模式: " + pattern.pattern());
            }
        }

        // 2. 关键字检查
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (code.contains(keyword)) {
                result.addViolation("检测到危险关键字: " + keyword);
            }
        }

        // 3. 字符串拼接绕过检查
        if (containsStringConcatenation(code)) {
            result.addViolation("检测到可疑的字符串拼接，可能用于绕过安全检查");
        }

        // 4. 导入语句检查
        checkImportStatements(code, result);

        // 5. 字节码长度检查
        if (code.length() > 10000) {
            result.addViolation("代码长度超过限制");
        }

        return result;
    }

    /**
     * 检查是否包含可疑的字符串拼接
     */
    private static boolean containsStringConcatenation(String code) {
        // 只检查真正可疑的字符串拼接模式，排除正常的StringBuilder使用
        // 检查可能用于绕过安全检查的字符串拼接
        if (code.contains("\" + \"")) {
            // 检查是否是试图拼接危险类名
            String[] dangerousPartials = {
                "java.lang.Run", "time", "Runt", "ime",
                "java.lang.Sys", "tem", "Syst", "em",
                "java.lang.reflect", "ref", "lect",
                "java.io", "java.net"
            };

            for (String partial : dangerousPartials) {
                if (code.contains("\"" + partial + "\"")) {
                    return true;
                }
            }
        }

        // 检查可疑的动态类名构建
        if (code.contains("String.valueOf") &&
            (code.contains("Runtime") || code.contains("System") || code.contains("reflect"))) {
            return true;
        }

        return false;
    }

    /**
     * 检查导入语句
     */
    private static void checkImportStatements(String code, SecurityCheckResult result) {
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("import ")) {
                String importClass = line.substring(7, line.length() - 1);
                if (!isAllowedImport(importClass)) {
                    result.addViolation("不允许的导入: " + importClass);
                }
            }
        }
    }

    /**
     * 检查是否为允许的导入
     */
    private static boolean isAllowedImport(String importClass) {
        for (String allowedPackage : ALLOWED_PACKAGES) {
            if (importClass.startsWith(allowedPackage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为允许的包
     */
    private static boolean isAllowedPackage(String code) {
        // 查找package声明
        String[] lines = code.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("package ")) {
                String packageName = line.substring(8).replace(";", "").trim();
                // 只允许dynamicCompilation包及其子包
                return packageName.startsWith("dynamicCompilation");
            }
        }
        // 如果没有package声明，认为是默认包，不允许
        return false;
    }

    /**
     * 安全检查结果
     */
    public static class SecurityCheckResult {
        private final Set<String> violations = new HashSet<>();

        public void addViolation(String violation) {
            violations.add(violation);
        }

        public boolean isSafe() {
            return violations.isEmpty();
        }

        public Set<String> getViolations() {
            return violations;
        }

        @Override
        public String toString() {
            return "SecurityCheckResult{" +
                    "safe=" + isSafe() +
                    ", violations=" + violations +
                    '}';
        }
    }
}
