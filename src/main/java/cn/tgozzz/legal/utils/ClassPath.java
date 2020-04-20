package cn.tgozzz.legal.utils;

public class ClassPath {

    /**
     * 获取 classPath 绝对路径
     * @return file：/D：/workspace/jbpmtest3/bin/
     */
    static String get() {
        return ClassLoader.getSystemResource("").getPath();
    }

    static String get(String path) {
        return ClassLoader.getSystemResource("").getPath() + path;
    }
}
