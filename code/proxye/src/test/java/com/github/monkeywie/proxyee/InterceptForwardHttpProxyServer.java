package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.util.HttpUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

/**
 请求转发功能实现 可以网址显示不变 内容变
 */
public class InterceptForwardHttpProxyServer {


    // curl -k -x 127.0.0.1:9999 https://www.baidu.com

    public static void main(String[] args) throws Exception {
        // 定义HttpProxyServerConfig对象
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHandleSsl(true);
        // 装载配置并启动代理服务（此处实现的是匹配指定url链接，随后转发页面到指定的url）
        new HttpProxyServer()
                .serverConfig(config)
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest,
                                                      HttpProxyInterceptPipeline pipeline) throws Exception {
                                // 调用HttpUtil工具类匹配url，若满足匹配条件则跳转到目标路径（此处以百度的请求转发到川大为例进行说明）
                                if (HttpUtil.checkUrl(httpRequest, "^www.baidu.com$")) {
                                    pipeline.getRequestProto().setHost("www.scu.edu.cn");
//                                    pipeline.getRequestProto().setHost("www.taobao.com");
                                    pipeline.getRequestProto().setPort(443);
                                    pipeline.getRequestProto().setSsl(true);
                                }
                                pipeline.beforeRequest(clientChannel, httpRequest);
                            }
                        });
                    }
                })// 代理相关异常处理
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
