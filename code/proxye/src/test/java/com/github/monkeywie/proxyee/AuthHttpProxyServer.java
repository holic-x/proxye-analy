package com.github.monkeywie.proxyee;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.github.monkeywie.proxyee.server.auth.BasicHttpProxyAuthenticationProvider;
import com.github.monkeywie.proxyee.server.auth.HttpAuthContext;
import com.github.monkeywie.proxyee.server.auth.model.BasicHttpToken;
import io.netty.channel.Channel;


/*
    用户名控制模式
 */
public class AuthHttpProxyServer {

    // curl -i -x 127.0.0.1:9999 -U admin:123456 https://www.baidu.com
    public static void main(String[] args) throws Exception {
        // 定义HttpProxyServerConfig对象
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setAuthenticationProvider(new BasicHttpProxyAuthenticationProvider() {
            @Override
            protected BasicHttpToken authenticate(String usr, String pwd) {
                // 默认账号密码校验，通过校验则生成自定义BasicHttpToken用于标识客户端身份
                if ("admin".equals(usr) && "123456".equals(pwd)) {
                    return new BasicHttpToken(usr, pwd);
                }
                // 如果校验不通过则返回null
                return null;
            }
        });

        // 装载配置并启动代理服务
        new HttpProxyServer()
                .serverConfig(config)
                .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                    @Override
                    public void init(HttpProxyInterceptPipeline pipeline) {
                        pipeline.addLast(new HttpProxyIntercept() {
                            @Override
                            public void beforeConnect(Channel clientChannel, HttpProxyInterceptPipeline pipeline) throws Exception {
                                System.out.println(HttpAuthContext.getToken(clientChannel));
                            }
                        });
                    }
                })
                .start(9999);
    }
}
