package com.distrsys;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import grpc.generated.battery.BatteryLevelRequest;
import grpc.generated.battery.BatteryLevelResponse;
import grpc.generated.battery.BatteryManagementServiceGrpc;
import grpc.generated.battery.ChargingRequest;
import grpc.generated.battery.ChargingResponse;
import grpc.generated.battery.PowerSupplyRequest;
import grpc.generated.battery.PowerSupplyResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * gRPC server implementation for the Battery Management Service.
 */
public class BatteryManagementServer
        extends BatteryManagementServiceGrpc
                .BatteryManagementServiceImplBase {

    private static final Logger logger =
            Logger.getLogger(BatteryManagementServer.class.getName());

    /*
     * Simulated battery state.
     *
     * These values remain available while the server is running.
     * They return to their starting values when the server restarts.
     */
    private int batteryLevel = 78;
    private boolean charging = false;
    private boolean supplyingPower = false;

    /**
     * Starts the Battery Management gRPC server.
     */
    public static void main(String[] args) {

    BatteryManagementServer batteryManagementServer =
            new BatteryManagementServer();

    try {
        Server server = ServerBuilder
                .forPort(50052)
                .addService(batteryManagementServer)
                .build()
                .start();

        logger.info(
                "Battery Management Server started, listening on port 50052"
        );

        System.out.println(
                "Battery Management Server started, listening on port 50052"
        );

        /*
         * The server registers itself so that clients
         * can discover it.
         */
        ServiceRegistration esr =
                ServiceRegistration.getInstance();

        esr.registerService(
                "_smartenergy._tcp.local.",
                "BatteryManagementService",
                50052,
                "service=BatteryManagementService"
        );

        server.awaitTermination();

    } catch (IOException exception) {
        logger.log(
                Level.SEVERE,
                "The Battery Management Server could not start or register.",
                exception
        );

    } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();

        logger.log(
                Level.SEVERE,
                "The Battery Management Server was interrupted.",
                exception
        );
    }
}

    /**
     * Unary RPC implementation.
     *
     * Enables or disables battery charging.
     */
    @Override
    public synchronized void enableCharging(
            ChargingRequest request,
            StreamObserver<ChargingResponse> responseObserver) {

        String batteryId = request.getBatteryId();
        boolean enableCharging = request.getEnable();

        logger.info(
                "Received charging request for battery: " + batteryId
        );

        System.out.println(
                "Receiving charging request for battery: " + batteryId
        );

        boolean success;
        String message;

        if (enableCharging) {

            if (batteryLevel >= 100) {
                charging = false;
                success = false;
                message = "Battery is already fully charged.";

            } else {
                charging = true;

                /*
                 * The battery cannot charge and supply power
                 * at the same time.
                 */
                supplyingPower = false;

                success = true;
                message = "Battery charging has been enabled.";
            }

        } else {
            charging = false;
            success = true;
            message = "Battery charging has been disabled.";
        }

        ChargingResponse response = ChargingResponse
                .newBuilder()
                .setBatteryId(batteryId)
                .setSuccess(success)
                .setCharging(charging)
                .setBatteryLevel(batteryLevel)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info(
                "Charging response sent for battery: " + batteryId
                        + ", charging=" + charging
        );
    }

    /**
     * Unary RPC implementation.
     *
     * Returns the current battery percentage and operating status.
     */
    @Override
    public synchronized void getBatteryLevel(
            BatteryLevelRequest request,
            StreamObserver<BatteryLevelResponse> responseObserver) {

        String batteryId = request.getBatteryId();

        logger.info(
                "Received battery-level request for battery: " + batteryId
        );

        System.out.println(
                "Receiving battery-level request for battery: " + batteryId
        );

        String status;

        if (batteryLevel >= 100) {
            status = "FULL";

        } else if (charging) {
            status = "CHARGING";

        } else if (batteryLevel <= 20) {
            status = "LOW";

        } else {
            status = "NORMAL";
        }

        String message;

        if (charging) {
            message = "Battery is currently charging.";

        } else if (supplyingPower) {
            message = "Battery is currently supplying power.";

        } else {
            message = "Battery is operating normally.";
        }

        BatteryLevelResponse response = BatteryLevelResponse
                .newBuilder()
                .setBatteryId(batteryId)
                .setBatteryLevel(batteryLevel)
                .setStatus(status)
                .setCharging(charging)
                .setSupplyingPower(supplyingPower)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info(
                "Battery-level response sent for battery: " + batteryId
                        + ", level=" + batteryLevel + "%"
        );
    }

    /**
     * Unary RPC implementation.
     *
     * Enables or disables electricity supply from the battery.
     */
    @Override
    public synchronized void enablePowerSupply(
            PowerSupplyRequest request,
            StreamObserver<PowerSupplyResponse> responseObserver) {

        String batteryId = request.getBatteryId();
        boolean enablePowerSupply = request.getEnable();

        logger.info(
                "Received power-supply request for battery: " + batteryId
        );

        System.out.println(
                "Receiving power-supply request for battery: " + batteryId
        );

        boolean success;
        String message;

        if (enablePowerSupply) {

            if (batteryLevel <= 10) {
                supplyingPower = false;
                success = false;
                message =
                        "Battery level is too low to enable power supply.";

            } else {
                supplyingPower = true;

                /*
                 * The battery cannot supply power and charge
                 * at the same time.
                 */
                charging = false;

                success = true;
                message = "Battery power supply has been enabled.";
            }

        } else {
            supplyingPower = false;
            success = true;
            message = "Battery power supply has been disabled.";
        }

        PowerSupplyResponse response = PowerSupplyResponse
                .newBuilder()
                .setBatteryId(batteryId)
                .setSuccess(success)
                .setSupplyingPower(supplyingPower)
                .setBatteryLevel(batteryLevel)
                .setMessage(message)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info(
                "Power-supply response sent for battery: " + batteryId
                        + ", supplyingPower=" + supplyingPower
        );
    }
}