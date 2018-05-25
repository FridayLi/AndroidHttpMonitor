package org.littleshoot.proxy;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;


public class DnsSecServerResolver implements HostResolver {
    @Override
    public InetSocketAddress resolve(String host, int port)
            throws UnknownHostException {
//        return VerifiedAddressFactory.newInetSocketAddress(host, port, true);
        return null;
    }
}
