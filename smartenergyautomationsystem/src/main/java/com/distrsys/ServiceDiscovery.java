/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt
 * to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java
 * to edit this template
 */
package com.distrsys;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * This class discovers Smart Energy services registered using jmDNS.
 *
 * The Controller creates one ServiceDiscovery object for each service
 * that it wants to discover.
 */
public class ServiceDiscovery {

    /*
     * Type and name of the service that the Controller
     * wants to discover.
     */
    private String requiredServiceType;
    private String requiredServiceName;

    /*
     * jmDNS object used to listen for registered services.
     */
    private JmDNS jmdns;

    /*
     * Stores the information about the service after it
     * has been discovered.
     */
    private ServiceInfo discoveredService;

    /**
     * Creates a ServiceDiscovery object.
     *
     * @param inServiceType type of service to discover
     * @param inServiceName name of service to discover
     */
    public ServiceDiscovery(
            String inServiceType,
            String inServiceName) {

        requiredServiceType = inServiceType;
        requiredServiceName = inServiceName;
    }

    /**
     * Searches for the required service.
     *
     * @param timeoutMilliseconds maximum time to wait
     * @return information about the discovered service
     * @throws InterruptedException if the discovery is interrupted
     */
    public ServiceInfo discoverService(
            long timeoutMilliseconds)
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        try {
            /*
             * Create a jmDNS instance.
             *
             * Unlike the supplied example, this assigns the object
             * to the class field. This allows close() to close the
             * correct jmDNS object.
             */
            jmdns = JmDNS.create(
                    InetAddress.getLocalHost()
            );

            System.out.println(
                    "Client: InetAddress.getLocalHost(): "
                            + InetAddress.getLocalHost()
            );

            /*
             * Add a listener for the required service type.
             */
            jmdns.addServiceListener(
                    requiredServiceType,
                    new ServiceListener() {

                        /**
                         * Called when a service of the required
                         * type is detected.
                         */
                        @Override
                        public void serviceAdded(
                                ServiceEvent event) {

                            ServiceInfo info =
                                    event.getInfo();

                            System.out.println(
                                    "Service added: " + info
                            );
                        }

                        /**
                         * Called when a service is removed.
                         */
                        @Override
                        public void serviceRemoved(
                                ServiceEvent event) {

                            System.out.println(
                                    "Service removed: "
                                            + event.getInfo()
                            );
                        }

                        /**
                         * Called when all the information about
                         * a detected service has been resolved.
                         */
                        @Override
                        public void serviceResolved(
                                ServiceEvent event) {

                            System.out.println(
                                    "Service resolved: "
                                            + event.getInfo()
                            );

                            ServiceInfo info =
                                    event.getInfo();

                            int port = info.getPort();

                            String resolvedServiceName =
                                    info.getName();

                            System.out.println(
                                    "Service "
                                            + resolvedServiceName
                                            + " resolved at port: "
                                            + port
                            );

                            /*
                             * Check whether this is the service
                             * that the Controller is looking for.
                             *
                             * Services with other names are ignored.
                             */
                            if (resolvedServiceName.contains(
                                    requiredServiceName)) {

                                System.out.println(
                                        "Service Listener: "
                                                + "Discovered service named: "
                                                + resolvedServiceName
                                );

                                /*
                                 * Store the complete service
                                 * information for the gRPC client.
                                 */
                                discoveredService = info;

                                /*
                                 * The required service has been found.
                                 * Release the latch.
                                 */
                                latch.countDown();
                            }
                        }
                    }
            );

        } catch (UnknownHostException exception) {
            System.out.println(
                    exception.getMessage()
            );

        } catch (IOException exception) {
            System.out.println(
                    exception.getMessage()
            );
        }

        /*
         * If the required service is not resolved, the latch
         * will wait until the timeout expires.
         */
        latch.await(
                timeoutMilliseconds,
                TimeUnit.MILLISECONDS
        );

        System.out.println(
                "Discover Service returning: "
                        + discoveredService
        );

        return discoveredService;
    }

    /**
     * Closes the jmDNS discovery connection.
     *
     * @throws IOException if jmDNS cannot be closed
     */
    public void close() throws IOException {

        if (jmdns != null) {
            jmdns.close();
        }
    }
}