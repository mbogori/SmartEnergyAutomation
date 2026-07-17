package com.distrsys;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import grpc.generated.solar.*;
import grpc.generated.battery.*;
import grpc.generated.appliance.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.jmdns.ServiceInfo;

/**
 * Main client for the Smart Energy Automation System.
 *
 * The client connects to:
 *
 * 1. Solar Panel Server
 * 2. Battery Management Server
 * 3. Smart Appliance Server
 */
public class SmartEnergyClient {

        private static final Logger logger = Logger.getLogger(SmartEnergyClient.class.getName());

        public static void main(String[] args) {

                /*
                 * Create one discovery object for each required service.
                 *
                 * The service type and service names must match the values
                 * registered by the three servers.
                 */
                ServiceDiscovery solarServiceDiscovery = new ServiceDiscovery(
                                "_smartenergy._tcp.local.",
                                "SolarPanelService");

                ServiceDiscovery batteryServiceDiscovery = new ServiceDiscovery(
                                "_smartenergy._tcp.local.",
                                "BatteryManagementService");

                ServiceDiscovery applianceServiceDiscovery = new ServiceDiscovery(
                                "_smartenergy._tcp.local.",
                                "SmartApplianceService");

                /*
                 * Declare the channels outside the try block so that they
                 * can be shut down in the finally block.
                 */
                ManagedChannel solarChannel = null;
                ManagedChannel batteryChannel = null;
                ManagedChannel applianceChannel = null;

                try {
                        /*
                         * =====================================================
                         * DISCOVER SOLAR PANEL SERVICE
                         * =====================================================
                         */

                        ServiceInfo solarServiceInfo = solarServiceDiscovery
                                        .discoverService(10000);

                        if (solarServiceInfo == null) {
                                throw new RuntimeException(
                                                "Solar Panel Service was not discovered.");
                        }

                        String[] solarHostAddresses = solarServiceInfo.getHostAddresses();

                        if (solarHostAddresses.length == 0) {
                                throw new RuntimeException(
                                                "No host address was returned for "
                                                                + "the Solar Panel Service.");
                        }

                        String solarHost = solarHostAddresses[0];

                        int solarPort = solarServiceInfo.getPort();

                        System.out.println(
                                        "Solar Panel Service discovered at "
                                                        + solarHost
                                                        + ":"
                                                        + solarPort);

                        /*
                         * Build the Solar channel using the host address
                         * and port returned by jmDNS.
                         */
                        solarChannel = ManagedChannelBuilder
                                        .forAddress(
                                                        solarHost,
                                                        solarPort)
                                        .usePlaintext()
                                        .build();

                        /*
                         * =====================================================
                         * DISCOVER BATTERY MANAGEMENT SERVICE
                         * =====================================================
                         */

                        ServiceInfo batteryServiceInfo = batteryServiceDiscovery
                                        .discoverService(10000);

                        if (batteryServiceInfo == null) {
                                throw new RuntimeException(
                                                "Battery Management Service was not discovered.");
                        }

                        String[] batteryHostAddresses = batteryServiceInfo.getHostAddresses();

                        if (batteryHostAddresses.length == 0) {
                                throw new RuntimeException(
                                                "No host address was returned for "
                                                                + "the Battery Management Service.");
                        }

                        String batteryHost = batteryHostAddresses[0];

                        int batteryPort = batteryServiceInfo.getPort();

                        System.out.println(
                                        "Battery Management Service discovered at "
                                                        + batteryHost
                                                        + ":"
                                                        + batteryPort);

                        /*
                         * Build the Battery channel using the values
                         * returned by jmDNS.
                         */
                        batteryChannel = ManagedChannelBuilder
                                        .forAddress(
                                                        batteryHost,
                                                        batteryPort)
                                        .usePlaintext()
                                        .build();

                        /*
                         * =====================================================
                         * DISCOVER SMART APPLIANCE SERVICE
                         * =====================================================
                         */

                        ServiceInfo applianceServiceInfo = applianceServiceDiscovery
                                        .discoverService(10000);

                        if (applianceServiceInfo == null) {
                                throw new RuntimeException(
                                                "Smart Appliance Service was not discovered.");
                        }

                        String[] applianceHostAddresses = applianceServiceInfo.getHostAddresses();

                        if (applianceHostAddresses.length == 0) {
                                throw new RuntimeException(
                                                "No host address was returned for "
                                                                + "the Smart Appliance Service.");
                        }

                        String applianceHost = applianceHostAddresses[0];

                        int appliancePort = applianceServiceInfo.getPort();

                        System.out.println(
                                        "Smart Appliance Service discovered at "
                                                        + applianceHost
                                                        + ":"
                                                        + appliancePort);

                        /*
                         * Build the Appliance channel using the values
                         * returned by jmDNS.
                         */
                        applianceChannel = ManagedChannelBuilder
                                        .forAddress(
                                                        applianceHost,
                                                        appliancePort)
                                        .usePlaintext()
                                        .build();

                        /*
                         * =====================================================
                         * CREATE THE GENERATED gRPC STUBS
                         * =====================================================
                         */

                        /*
                         * Blocking stub for the Solar unary and
                         * server-streaming RPCs.
                         */
                        SolarPanelServiceGrpc.SolarPanelServiceBlockingStub solarBlockingStub = SolarPanelServiceGrpc
                                        .newBlockingStub(solarChannel);

                        /*
                         * Blocking stub for the Battery unary RPCs.
                         */
                        BatteryManagementServiceGrpc.BatteryManagementServiceBlockingStub batteryBlockingStub = BatteryManagementServiceGrpc
                                        .newBlockingStub(batteryChannel);

                        /*
                         * Asynchronous stub for the Appliance client-streaming
                         * and bidirectional-streaming RPCs.
                         */
                        SmartApplianceServiceGrpc.SmartApplianceServiceStub applianceAsyncStub = SmartApplianceServiceGrpc
                                        .newStub(applianceChannel);
                        /*
                         * =========================================================
                         * SOLAR PANEL SERVICE
                         * GET SOLAR STATUS - UNARY RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println("GET SOLAR STATUS - UNARY RPC");

                        SolarStatusRequest solarStatusRequest = SolarStatusRequest
                                        .newBuilder()
                                        .setPanelId("SOLAR-001")
                                        .build();

                        SolarStatusResponse solarStatusResponse = solarBlockingStub.getSolarStatus(
                                        solarStatusRequest);

                        logger.info(
                                        "Solar panel ID: "
                                                        + solarStatusResponse.getPanelId());

                        logger.info(
                                        "Solar status: "
                                                        + solarStatusResponse.getStatus());

                        logger.info(
                                        "Current output: "
                                                        + solarStatusResponse.getCurrentOutputWatts()
                                                        + " watts");

                        logger.info(
                                        "Sunlight level: "
                                                        + solarStatusResponse.getSunlightLevel()
                                                        + "%");

                        logger.info(
                                        "Message: "
                                                        + solarStatusResponse.getMessage());

                        /*
                         * =========================================================
                         * SOLAR PANEL SERVICE
                         * STREAM SOLAR OUTPUT - SERVER-STREAMING RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println(
                                        "STREAM SOLAR OUTPUT - SERVER-STREAMING RPC");

                        SolarOutputRequest solarOutputRequest = SolarOutputRequest
                                        .newBuilder()
                                        .setPanelId("SOLAR-001")
                                        .build();

                        Iterator<SolarOutputReading> solarReadings = solarBlockingStub.streamSolarOutput(
                                        solarOutputRequest);

                        while (solarReadings.hasNext()) {

                                SolarOutputReading solarReading = solarReadings.next();

                                logger.info(
                                                "Solar panel: "
                                                                + solarReading.getPanelId()
                                                                + ", output: "
                                                                + solarReading.getOutputWatts()
                                                                + " watts"
                                                                + ", sunlight: "
                                                                + solarReading.getSunlightLevel()
                                                                + "%"
                                                                + ", status: "
                                                                + solarReading.getStatus());
                        }

                        /*
                         * =========================================================
                         * BATTERY MANAGEMENT SERVICE
                         * ENABLE CHARGING - UNARY RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println("ENABLE CHARGING - UNARY RPC");

                        ChargingRequest chargingRequest = ChargingRequest
                                        .newBuilder()
                                        .setBatteryId("BATTERY-001")
                                        .setEnable(true)
                                        .build();

                        ChargingResponse chargingResponse = batteryBlockingStub.enableCharging(
                                        chargingRequest);

                        logger.info(
                                        "Battery ID: "
                                                        + chargingResponse.getBatteryId());

                        logger.info(
                                        "Charging operation successful: "
                                                        + chargingResponse.getSuccess());

                        logger.info(
                                        "Battery charging: "
                                                        + chargingResponse.getCharging());

                        logger.info(
                                        "Battery level: "
                                                        + chargingResponse.getBatteryLevel()
                                                        + "%");

                        logger.info(
                                        "Message: "
                                                        + chargingResponse.getMessage());

                        /*
                         * =========================================================
                         * BATTERY MANAGEMENT SERVICE
                         * GET BATTERY LEVEL - UNARY RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println("GET BATTERY LEVEL - UNARY RPC");

                        BatteryLevelRequest batteryLevelRequest = BatteryLevelRequest
                                        .newBuilder()
                                        .setBatteryId("BATTERY-001")
                                        .build();

                        BatteryLevelResponse batteryLevelResponse = batteryBlockingStub.getBatteryLevel(
                                        batteryLevelRequest);

                        logger.info(
                                        "Battery ID: "
                                                        + batteryLevelResponse.getBatteryId());

                        logger.info(
                                        "Battery level: "
                                                        + batteryLevelResponse.getBatteryLevel()
                                                        + "%");

                        logger.info(
                                        "Battery status: "
                                                        + batteryLevelResponse.getStatus());

                        logger.info(
                                        "Battery charging: "
                                                        + batteryLevelResponse.getCharging());

                        logger.info(
                                        "Battery supplying power: "
                                                        + batteryLevelResponse.getSupplyingPower());

                        logger.info(
                                        "Message: "
                                                        + batteryLevelResponse.getMessage());

                        /*
                         * =========================================================
                         * BATTERY MANAGEMENT SERVICE
                         * ENABLE POWER SUPPLY - UNARY RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println("ENABLE POWER SUPPLY - UNARY RPC");

                        PowerSupplyRequest powerSupplyRequest = PowerSupplyRequest
                                        .newBuilder()
                                        .setBatteryId("BATTERY-001")
                                        .setEnable(true)
                                        .build();

                        PowerSupplyResponse powerSupplyResponse = batteryBlockingStub.enablePowerSupply(
                                        powerSupplyRequest);

                        logger.info(
                                        "Battery ID: "
                                                        + powerSupplyResponse.getBatteryId());

                        logger.info(
                                        "Power-supply operation successful: "
                                                        + powerSupplyResponse.getSuccess());

                        logger.info(
                                        "Battery supplying power: "
                                                        + powerSupplyResponse.getSupplyingPower());

                        logger.info(
                                        "Battery level: "
                                                        + powerSupplyResponse.getBatteryLevel()
                                                        + "%");

                        logger.info(
                                        "Message: "
                                                        + powerSupplyResponse.getMessage());

                        /*
                         * =========================================================
                         * SMART APPLIANCE SERVICE
                         * ANALYZE CONSUMPTION HISTORY - CLIENT-STREAMING RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println(
                                        "ANALYZE CONSUMPTION HISTORY - CLIENT-STREAMING RPC");

                        /*
                         * Response observer receives the final ConsumptionAnalysis
                         * response from the server.
                         */
                        StreamObserver<ConsumptionAnalysis> consumptionAnalysisResponseObserver = new StreamObserver<ConsumptionAnalysis>() {

                                @Override
                                public void onNext(
                                                ConsumptionAnalysis response) {

                                        logger.info(
                                                        "Readings processed: "
                                                                        + response
                                                                                        .getReadingsProcessed());

                                        logger.info(
                                                        "Total consumption: "
                                                                        + response
                                                                                        .getTotalConsumptionKwh()
                                                                        + " kWh");

                                        logger.info(
                                                        "Average consumption: "
                                                                        + response
                                                                                        .getAverageConsumptionWatts()
                                                                        + " watts");

                                        logger.info(
                                                        "Efficiency score: "
                                                                        + response
                                                                                        .getEfficiencyScore());

                                        logger.info(
                                                        "Estimated savings: "
                                                                        + response
                                                                                        .getEstimatedSavingsKwh()
                                                                        + " kWh");

                                        logger.info(
                                                        "Summary: "
                                                                        + response.getSummary());
                                }

                                @Override
                                public void onError(Throwable throwable) {

                                        logger.log(
                                                        Level.SEVERE,
                                                        "Consumption analysis RPC failed.",
                                                        throwable);
                                }

                                @Override
                                public void onCompleted() {

                                        System.out.println(
                                                        "Consumption analysis stream completed.");
                                }
                        };

                        /*
                         * Request observer is used by the client to send multiple
                         * ConsumptionReading messages.
                         */
                        StreamObserver<ConsumptionReading> consumptionReadingRequestObserver = applianceAsyncStub
                                        .analyzeConsumptionHistory(
                                                        consumptionAnalysisResponseObserver);

                        try {
                                ConsumptionReading reading1 = ConsumptionReading
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-001")
                                                .setApplianceName(
                                                                "Living Room Heater")
                                                .setPowerConsumptionWatts(1500.0)
                                                .setDurationMinutes(30)
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                consumptionReadingRequestObserver.onNext(
                                                reading1);

                                ConsumptionReading reading2 = ConsumptionReading
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-002")
                                                .setApplianceName(
                                                                "Washing Machine")
                                                .setPowerConsumptionWatts(1200.0)
                                                .setDurationMinutes(45)
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                consumptionReadingRequestObserver.onNext(
                                                reading2);

                                ConsumptionReading reading3 = ConsumptionReading
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-003")
                                                .setApplianceName(
                                                                "Living Room Lights")
                                                .setPowerConsumptionWatts(300.0)
                                                .setDurationMinutes(120)
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                consumptionReadingRequestObserver.onNext(
                                                reading3);

                                /*
                                 * Tell the server that the client has finished
                                 * uploading consumption readings.
                                 */
                                consumptionReadingRequestObserver.onCompleted();

                                /*
                                 * Give the asynchronous server response time to arrive.
                                 */
                                Thread.sleep(1000);

                        } catch (RuntimeException exception) {
                                exception.printStackTrace();

                        } catch (InterruptedException exception) {
                                exception.printStackTrace();
                        }

                        /*
                         * =========================================================
                         * SMART APPLIANCE SERVICE
                         * APPLIANCE CONTROL - BIDIRECTIONAL-STREAMING RPC
                         * =========================================================
                         */

                        System.out.println();
                        System.out.println(
                                        "APPLIANCE CONTROL - BIDIRECTIONAL-STREAMING RPC");

                        /*
                         * Response observer receives appliance status updates
                         * from the server.
                         */
                        StreamObserver<ApplianceControlResponse> applianceControlResponseObserver = new StreamObserver<ApplianceControlResponse>() {

                                @Override
                                public void onNext(
                                                ApplianceControlResponse response) {

                                        logger.info(
                                                        "Appliance ID: "
                                                                        + response.getApplianceId());

                                        logger.info(
                                                        "Command successful: "
                                                                        + response.getSuccess());

                                        logger.info(
                                                        "Current status: "
                                                                        + response
                                                                                        .getCurrentStatus());

                                        logger.info(
                                                        "Power consumption: "
                                                                        + response
                                                                                        .getPowerConsumptionWatts()
                                                                        + " watts");

                                        logger.info(
                                                        "Eco Mode: "
                                                                        + response.getEcoMode());

                                        logger.info(
                                                        "Message: "
                                                                        + response.getMessage());
                                }

                                @Override
                                public void onError(Throwable throwable) {

                                        logger.log(
                                                        Level.SEVERE,
                                                        "Appliance-control RPC failed.",
                                                        throwable);
                                }

                                @Override
                                public void onCompleted() {

                                        System.out.println(
                                                        "Appliance-control stream completed.");
                                }
                        };

                        /*
                         * Request observer allows the client to send multiple
                         * appliance-control commands.
                         */
                        StreamObserver<ApplianceControlRequest> applianceControlRequestObserver = applianceAsyncStub
                                        .applianceControl(
                                                        applianceControlResponseObserver);

                        try {
                                /*
                                 * Command 1: Turn on the appliance.
                                 */
                                ApplianceControlRequest turnOnRequest = ApplianceControlRequest
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-001")
                                                .setCommand("TURN_ON")
                                                .setCommandValue("")
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                applianceControlRequestObserver.onNext(
                                                turnOnRequest);

                                Thread.sleep(1000);

                                /*
                                 * Command 2: Enable Eco Mode.
                                 */
                                ApplianceControlRequest ecoModeRequest = ApplianceControlRequest
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-001")
                                                .setCommand("SET_ECO_MODE")
                                                .setCommandValue("ON")
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                applianceControlRequestObserver.onNext(
                                                ecoModeRequest);

                                Thread.sleep(1000);

                                /*
                                 * Command 3: Request the current appliance status.
                                 */
                                ApplianceControlRequest statusRequest = ApplianceControlRequest
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-001")
                                                .setCommand("GET_STATUS")
                                                .setCommandValue("")
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                applianceControlRequestObserver.onNext(
                                                statusRequest);

                                Thread.sleep(1000);

                                /*
                                 * Command 4: Turn off the appliance.
                                 */
                                ApplianceControlRequest turnOffRequest = ApplianceControlRequest
                                                .newBuilder()
                                                .setApplianceId("APPLIANCE-001")
                                                .setCommand("TURN_OFF")
                                                .setCommandValue("")
                                                .setTimestamp(
                                                                System.currentTimeMillis())
                                                .build();

                                applianceControlRequestObserver.onNext(
                                                turnOffRequest);

                                Thread.sleep(1000);

                                /*
                                 * Tell the server that the client has finished
                                 * sending appliance-control commands.
                                 */
                                applianceControlRequestObserver.onCompleted();

                                /*
                                 * Give the final asynchronous response time to arrive.
                                 */
                                Thread.sleep(1000);

                        } catch (RuntimeException exception) {
                                exception.printStackTrace();

                        } catch (InterruptedException exception) {
                                exception.printStackTrace();
                        }

                        /*
                         * =========================================================
                         * SHUT DOWN ALL CLIENT CHANNELS
                         * =========================================================
                         */

                       
                                System.out.println();
                                System.out.println(
                                        "Smart Energy Client has completed all RPC calls."
                                );

                                } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();

                                logger.log(
                                        Level.SEVERE,
                                        "Service discovery was interrupted.",
                                        exception
                                );

                                } finally {
                                /*
                                * Shut down the gRPC channels.
                                */
                                if (solarChannel != null) {
                                        solarChannel.shutdown();
                                }

                                if (batteryChannel != null) {
                                        batteryChannel.shutdown();
                                }

                                if (applianceChannel != null) {
                                        applianceChannel.shutdown();
                                }

                                /*
                                * Close the jmDNS discovery connections.
                                */
                                try {
                                        solarServiceDiscovery.close();
                                        batteryServiceDiscovery.close();
                                        applianceServiceDiscovery.close();

                                } catch (IOException exception) {
                                        logger.log(
                                                Level.WARNING,
                                                "Could not close the service-discovery connections.",
                                                exception
                                        );
                                }
                                }
                
                        }
                }