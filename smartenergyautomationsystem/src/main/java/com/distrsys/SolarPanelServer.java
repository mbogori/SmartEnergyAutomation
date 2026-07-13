package com.distrsys;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import grpc.generated.solar.SolarOutputReading;
import grpc.generated.solar.SolarOutputRequest;
import grpc.generated.solar.SolarPanelServiceGrpc;
import grpc.generated.solar.SolarStatusRequest;
import grpc.generated.solar.SolarStatusResponse;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * gRPC server implementation for the Solar Panel Service.
 */
public class SolarPanelServer
        extends SolarPanelServiceGrpc.SolarPanelServiceImplBase {

    private static final Logger logger =
            Logger.getLogger(SolarPanelServer.class.getName());

    /**
     * Starts the Solar Panel gRPC server.
     */
    public static void main(String[] args) {

        SolarPanelServer solarPanelServer = new SolarPanelServer();

        int port = 50051;

        try {
            Server server = ServerBuilder
                    .forPort(port)
                    .addService(solarPanelServer)
                    .build()
                    .start();

            logger.info(
                    "Solar Panel Server started, listening on port " + port
            );

            System.out.println(
                    "Solar Panel Server started, listening on port " + port
            );

            server.awaitTermination();

        } catch (IOException exception) {
            logger.log(
                    Level.SEVERE,
                    "The Solar Panel Server could not be started.",
                    exception
            );

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            logger.log(
                    Level.SEVERE,
                    "The Solar Panel Server was interrupted.",
                    exception
            );
        }
    }

    /**
     * Unary RPC implementation.
     *
     * Receives a solar-panel ID and returns the current status
     * of that solar panel.
     */
    @Override
    public void getSolarStatus(
            SolarStatusRequest request,
            StreamObserver<SolarStatusResponse> responseObserver) {

        String panelId = request.getPanelId();

        logger.info(
                "Received status request for solar panel: " + panelId
        );

        System.out.println(
                "Receiving status request for solar panel: " + panelId
        );

        SolarStatusResponse response = SolarStatusResponse
                .newBuilder()
                .setPanelId(panelId)
                .setStatus("ACTIVE")
                .setCurrentOutputWatts(5800.0)
                .setSunlightLevel(82.0)
                .setMessage("Solar panel is operating normally.")
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        logger.info(
                "Solar status response sent for panel: " + panelId
        );
    }

    /**
     * Server-streaming RPC implementation.
     *
     * Sends several simulated solar-output readings to the client.
     * A two-second delay is used between readings.
     */
    @Override
    public void streamSolarOutput(
            SolarOutputRequest request,
            StreamObserver<SolarOutputReading> responseObserver) {

        String panelId = request.getPanelId();

        logger.info(
                "Starting solar-output stream for panel: " + panelId
        );

        System.out.println(
                "Starting solar-output stream for panel: " + panelId
        );

        /*
         * Simulated solar-panel output readings.
         *
         * These values can later be replaced with readings from
         * physical sensors, a database, or an external API.
         */
        double[] outputReadings = {
                5300.0,
                5100.0,
                4600.0,
                3500.0,
                1200.0
        };

        /*
         * Simulated sunlight-level readings.
         *
         * Each sunlight value corresponds with the output reading
         * at the same array position.
         */
        double[] sunlightReadings = {
                83.0,
                78.0,
                70.0,
                52.0,
                25.0
        };

        for (int i = 0; i < outputReadings.length; i++) {

            double outputWatts = outputReadings[i];
            double sunlightLevel = sunlightReadings[i];

            String status;

            if (sunlightLevel >= 50.0) {
                status = "ACTIVE";

            } else if (sunlightLevel > 0.0) {
                status = "LOW_SUNLIGHT";

            } else {
                status = "INACTIVE";
            }

            SolarOutputReading reading = SolarOutputReading
                    .newBuilder()
                    .setPanelId(panelId)
                    .setOutputWatts(outputWatts)
                    .setSunlightLevel(sunlightLevel)
                    .setStatus(status)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(reading);

            logger.info(
                    "Sent reading for " + panelId
                            + ": output=" + outputWatts
                            + " watts, sunlight=" + sunlightLevel
                            + "%, status=" + status
            );

            try {
                Thread.sleep(2000); // Simulate delay between readings

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                logger.log(
                        Level.SEVERE,
                        "Solar-output stream was interrupted.",
                        exception
                );

                responseObserver.onError(exception);
                return;
            }
        }

        responseObserver.onCompleted();

        logger.info(
                "Solar-output stream completed for panel: " + panelId
        );
    }
}