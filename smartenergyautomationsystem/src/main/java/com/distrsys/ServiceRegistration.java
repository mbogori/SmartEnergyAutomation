package com.distrsys;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Registers services using jmDNS.
 *
 * This class follows the singleton pattern used in the
 * jmDNS laboratory example.
 *
 * Only one ServiceRegistration object is created inside
 * each running server application.
 */
public class ServiceRegistration {

    /*
     * Stores the single ServiceRegistration object.
     */
    private static ServiceRegistration instance;

    /*
     * jmDNS object used to register services.
     */
    private final JmDNS jmdns;

    /**
     * Private constructor prevents other classes from creating
     * ServiceRegistration objects using the new keyword.
     */
    private ServiceRegistration() throws IOException {

        /*
         * Create jmDNS using the local computer's network address.
         */
        jmdns = JmDNS.create(
                InetAddress.getLocalHost()
        );
    }

    /**
     * Returns the single ServiceRegistration object.
     *
     * If the object does not exist yet, it is created.
     */
    public static ServiceRegistration getInstance()
            throws IOException {

        if (instance == null) {
            instance = new ServiceRegistration();
        }

        return instance;
    }

    /**
     * Registers a service so that clients can discover it.
     *
     * @param type jmDNS service type
     * @param name name used to identify the service
     * @param port port on which the service is running
     * @param description description of the service
     */
    public void registerService(
            String type,
            String name,
            int port,
            String description) throws IOException {

        /*
         * Create the service information that will be
         * advertised through jmDNS.
         */
        ServiceInfo serviceInfo = ServiceInfo.create(
                type,
                name,
                port,
                description
        );

        /*
         * Register and advertise the service.
         */
        jmdns.registerService(serviceInfo);

        System.out.println(
                name + " registered successfully using jmDNS."
        );

        System.out.println(
                "Service type: " + type
        );

        System.out.println(
                "Service address: "
                        + jmdns.getInetAddress().getHostAddress()
        );

        System.out.println(
                "Service port: " + port
        );
    }
}




