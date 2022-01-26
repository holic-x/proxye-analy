package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

/**
 * 匹配到百度首页时重定向到指定url
 */
public class InterceptRedirectHttpProxyServer {
    public static void main(String[] args) throws Exception {
        // 定义HttpProxyServerConfig对象
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHandleSsl(true);
        // 装载配置并启动代理服务（此处实现的是匹配指定url链接，随后重定向到指定的url）
        new HttpProxyServer()
                .serverConfig(config)
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                                // 调用HttpUtil工具类匹配url，若满足匹配条件则跳转到目标路径（此处以百度首页跳转到淘宝为例进行说明）
                                if (HttpUtil.checkUrl(pipeline.getHttpRequest(), "^www.baidu.com$")) {
                                    HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                                    hookResponse.setStatus(HttpResponseStatus.FOUND);
                                    hookResponse.headers().set(HttpHeaderNames.LOCATION, "http://www.taobao.com");
                                    clientChannel.writeAndFlush(hookResponse);
                                    HttpContent lastContent = new DefaultLastHttpContent();
                                    clientChannel.writeAndFlush(lastContent);
                                    return;
                                }
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }
                        });
                    }
                })  // 代理异常处理相关
                .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {
                    @Override
                    public void beforeCatch(Channel clientChannel, Throwable cause) throws Exception {
                        cause.printStackTrace();
                    }

                    @Override
                    public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable cause)
                            throws Exception {
                        cause.printStackTrace();
                    }
                })
                .start(9999);
    }
}
