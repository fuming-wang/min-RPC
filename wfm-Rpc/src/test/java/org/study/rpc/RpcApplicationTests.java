package org.study.rpc;


import java.util.ServiceLoader;

class RpcApplicationTests {

    public static void main(String[] args) {
        final ServiceLoader<ServiceLoader> load = ServiceLoader.load(ServiceLoader.class);
        for (ServiceLoader serviceLoader : load) {

        }
    }

}
