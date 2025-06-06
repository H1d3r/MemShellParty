package com.reajason.javaweb.memshell.utils;

import com.reajason.javaweb.memshell.Server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author ReaJason
 */
public class CommonUtil {

    public static final String[] INJECTOR_CLASS_NAMES = new String[]{"SignatureUtils", "NetworkUtils", "KeyUtils", "EncryptionUtils", "SessionDataUtil", "SOAPUtils", "ReflectUtil", "HttpClientUtil", "EncryptionUtil", "XMLUtil", "JSONUtil", "FileUtils", "DateUtil", "StringUtil", "MathUtil", "HttpUtil", "CSVUtil", "ImageUtil", "ThreadUtil", "ReportUtil", "EncodingUtil", "ConfigurationUtil", "HTMLUtil", "SerializationUtil"};
    public static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(new String[]{
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
    }));
    private static final String[] PACKAGE_NAMES = {
            "org.springframework",
            "org.apache.commons",
            "org.apache.logging",
            "org.apache",
            "com.fasterxml.jackson",
            "org.junit",
            "org.apache.commons.lang",
            "org.apache.http.client",
            "com.google.gso",
            "ch.qos.logback"
    };
    private static final String[] MIDDLEWARE_NAMES = {
            "Error",
            "Log",
            "Report",
            "Auth",
            "OAuth",
            "Checker"
    };

    public static byte[] gzipCompress(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(data);
        }
        return out.toByteArray();
    }

    public static byte[] gzipDecompress(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = gzip.read(buffer)) != -1) {
                out.write(buffer, 0, length);

            }
        }
        return out.toByteArray();
    }

    public static String getRandomString(int length) {
        String randomString = getRandomStringInternal(length);
        while (JAVA_KEYWORDS.contains(randomString)) {
            randomString = getRandomStringInternal(length);
        }
        return randomString;
    }

    private static String getRandomStringInternal(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(52);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static String getRandomPackageName() {
        return PACKAGE_NAMES[new Random().nextInt(PACKAGE_NAMES.length)] + "." + getRandomString(5);
    }

    public static String getPackageName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String generateShellClassName() {
        return getRandomPackageName() + ".ErrorHandler";
    }

    public static String generateInjectorClassName() {
        return getRandomPackageName() + "." + INJECTOR_CLASS_NAMES[new Random().nextInt(INJECTOR_CLASS_NAMES.length)];
    }

    public static String generateShellClassName(Server server, String shellType) {
        String packageName;
        switch (server) {
            case Jetty:
                packageName = "org.eclipse.jetty.servlet.handlers";
                break;
            case Undertow:
            case JBossEAP7:
            case WildFly:
                packageName = "io.undertow.servlet.handlers";
                break;
            case SpringWebMvc:
                packageName = "org.springframework.boot.mvc.handlers";
                break;
            case SpringWebFlux:
                packageName = "org.springframework.boot.webflux.handlers";
                break;
            case WebSphere:
                packageName = "com.ibm.ws.webcontainer.handlers";
                break;
            case WebLogic:
                packageName = "weblogic.servlet.internal.handlers";
                break;
            case Resin:
                packageName = "com.caucho.server.dispatch.handlers";
                break;
            case BES:
                packageName = "com.bes.enterprise.webtier.web.handlers";
                break;
            default:
                packageName = "org.apache.http.web.handlers";
                break;
        }
        return packageName
                + "." + getRandomString(5)
                + "." + MIDDLEWARE_NAMES[new Random().nextInt(MIDDLEWARE_NAMES.length)] + shellType;
    }
}