package com.distrsys;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Registers Smart Energy gRPC services using jmDNS.
 *
 * Each server creates an instance of this class and publishes
 * its service name, network address and port number.
 */
public class ServiceRegistration {

    private static final Logger logger =
            Logger.getLogger(ServiceRegistration.class.getName());

    /*
     * All three Smart Energy services use the same service type.
     *
     * The individual services are identified by their different
     * service names.
     */
    public static final String SERVICE_TYPE =
            "_smartenergy._tcp.local.";

    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    /**
     * Registers one Smart Energy service using jmDNS.
     *
     * @param serviceName name used to identify the service
     * @param port port on which the gRPC server is running
     * @param description description of the service
     * @throws IOException if jmDNS cannot be created or registered
     */
    public void registerService(
            String serviceName,
            int port,
            String description) throws IOException {

        /*
         * Obtain the network address of the computer running
         * the service.
         */
        InetAddress localAddress =
                InetAddress.getLocalHost();

        /*
         * Create the jmDNS instance using the local address.
         */
        jmdns = JmDNS.create(localAddress);

        /*
         * Create the service information that will be
         * advertised on the local network.
         */
        serviceInfo = ServiceInfo.create(
                SERVICE_TYPE,
                serviceName,
                port,
                description
        );

        /*
         * Publish the service through jmDNS.
         */
        jmdns.registerService(serviceInfo);

        logger.info(
                serviceName
                        + " registered successfully using jmDNS."
        );

        System.out.println(
                serviceName
                        + " registered successfully using jmDNS."
        );

        System.out.println(
                "Service type: " + SERVICE_TYPE
        );

        System.out.println(
                "Service address: "
                        + localAddress.getHostAddress()
        );

        System.out.println(
                "Service port: " + port
        );
    }

    /**
     * Removes the registered service and closes jmDNS.
     */
    public void unregisterService() {

        if (jmdns != null) {

            if (serviceInfo != null) {
                jmdns.unregisterService(serviceInfo);

                logger.info(
                        serviceInfo.getName()
                                + " unregistered from jmDNS."
                );
            }

            try {
                jmdns.close();

            } catch (IOException exception) {
                logger.log(
                        Level.SEVERE,
                        "Error while closing jmDNS.",
                        exception
                );
            }
        }
    }
}




