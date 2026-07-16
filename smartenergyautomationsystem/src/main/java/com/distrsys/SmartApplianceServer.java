package com.distrsys;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import grpc.generated.appliance.ApplianceControlRequest;
import grpc.generated.appliance.ApplianceControlResponse;
import grpc.generated.appliance.ConsumptionAnalysis;
import grpc.generated.appliance.ConsumptionReading;
import grpc.generated.appliance.SmartApplianceServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * gRPC server implementation for the Smart Appliance Service.
 */
public class SmartApplianceServer
        extends SmartApplianceServiceGrpc.SmartApplianceServiceImplBase {

    private static final Logger logger =
            Logger.getLogger(SmartApplianceServer.class.getName());

    /*
     * Stores the current state of each appliance.
     *
     * The appliance ID is used as the map key.
     */
    private final Map<String, ApplianceState> appliances =
            new ConcurrentHashMap<>();

    /**
     * Starts the Smart Appliance gRPC server.
     */
public static void main(String[] args) {

    SmartApplianceServer smartApplianceServer =
            new SmartApplianceServer();

    try {
        Server server = ServerBuilder
                .forPort(50053)
                .addService(smartApplianceServer)
                .build()
                .start();

        logger.info(
                "Smart Appliance Server started, listening on port 50053"
        );

        System.out.println(
                "Smart Appliance Server started, listening on port 50053"
        );

        /*
         * The server registers itself so that clients
         * can discover it.
         */
        ServiceRegistration esr =
                ServiceRegistration.getInstance();

        esr.registerService(
                "_smartenergy._tcp.local.",
                "SmartApplianceService",
                50053,
                "service=SmartApplianceService"
        );

        server.awaitTermination();

    } catch (IOException exception) {
        logger.log(
                Level.SEVERE,
                "The Smart Appliance Server could not start or register.",
                exception
        );

    } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();

        logger.log(
                Level.SEVERE,
                "The Smart Appliance Server was interrupted.",
                exception
        );
    }
}
    /**
     * Client-streaming RPC implementation.
     *
     * Receives multiple electricity-consumption readings.
     * After the client finishes sending readings, the server
     * calculates and returns one consumption analysis.
     */
    @Override
    public StreamObserver<ConsumptionReading> analyzeConsumptionHistory(
            StreamObserver<ConsumptionAnalysis> responseObserver) {

        logger.info("Consumption-history stream started.");

        return new StreamObserver<ConsumptionReading>() {

            private int readingsProcessed = 0;
            private double totalPowerWatts = 0.0;
            private double totalConsumptionKwh = 0.0;

            /**
             * Called whenever the server receives one consumption reading.
             */
            @Override
            public void onNext(ConsumptionReading reading) {

                double powerWatts =
                        reading.getPowerConsumptionWatts();

                int durationMinutes =
                        reading.getDurationMinutes();

                /*
                 * Ignore readings containing invalid negative power
                 * or durations that are zero or less.
                 */
                if (powerWatts < 0 || durationMinutes <= 0) {
                    logger.warning(
                            "Invalid consumption reading ignored for "
                                    + reading.getApplianceId()
                    );

                    return;
                }

                /*
                 * Convert the reading into kilowatt-hours:
                 *
                 * watts / 1000 = kilowatts
                 * minutes / 60 = hours
                 * kilowatts × hours = kilowatt-hours
                 */
                double consumptionKwh =
                        (powerWatts / 1000.0)
                                * (durationMinutes / 60.0);

                readingsProcessed++;
                totalPowerWatts += powerWatts;
                totalConsumptionKwh += consumptionKwh;

                logger.info(
                        "Received consumption reading from "
                                + reading.getApplianceName()
                                + ": power=" + powerWatts
                                + " watts, duration=" + durationMinutes
                                + " minutes"
                );
            }

            /**
             * Called if an error occurs while the client is sending
             * consumption readings.
             */
            @Override
            public void onError(Throwable throwable) {

                logger.log(
                        Level.SEVERE,
                        "Error while receiving consumption readings.",
                        throwable
                );
            }

            /**
             * Called after the client finishes sending all readings.
             */
            @Override
            public void onCompleted() {

                double averageConsumptionWatts;
                double efficiencyScore;
                double savingsRate;
                String summary;

                if (readingsProcessed == 0) {
                    averageConsumptionWatts = 0.0;
                    efficiencyScore = 0.0;
                    savingsRate = 0.0;
                    summary =
                            "No valid consumption readings were received.";

                } else {
                    averageConsumptionWatts =
                            totalPowerWatts / readingsProcessed;

                    /*
                     * Simulated efficiency rules.
                     *
                     * Lower average consumption produces a higher
                     * efficiency score.
                     */
                    if (averageConsumptionWatts <= 500.0) {
                        efficiencyScore = 90.0;
                        savingsRate = 0.05;
                        summary =
                                "Appliance electricity usage was highly efficient.";

                    } else if (averageConsumptionWatts <= 1000.0) {
                        efficiencyScore = 80.0;
                        savingsRate = 0.10;
                        summary =
                                "Appliance electricity usage was efficient.";

                    } else if (averageConsumptionWatts <= 2000.0) {
                        efficiencyScore = 65.0;
                        savingsRate = 0.20;
                        summary =
                                "Appliance electricity usage was moderate.";

                    } else {
                        efficiencyScore = 50.0;
                        savingsRate = 0.30;
                        summary =
                                "High appliance electricity usage was detected.";
                    }
                }

                double estimatedSavingsKwh =
                        totalConsumptionKwh * savingsRate;

                ConsumptionAnalysis analysis = ConsumptionAnalysis
                        .newBuilder()
                        .setReadingsProcessed(readingsProcessed)
                        .setTotalConsumptionKwh(totalConsumptionKwh)
                        .setAverageConsumptionWatts(
                                averageConsumptionWatts
                        )
                        .setEfficiencyScore(efficiencyScore)
                        .setEstimatedSavingsKwh(
                                estimatedSavingsKwh
                        )
                        .setSummary(summary)
                        .build();

                responseObserver.onNext(analysis);
                responseObserver.onCompleted();

                logger.info(
                        "Consumption analysis completed. Readings processed: "
                                + readingsProcessed
                );
            }
        };
    }

    /**
     * Bidirectional-streaming RPC implementation.
     *
     * Receives appliance commands and immediately returns
     * the updated appliance status for every command.
     */
    @Override
    public StreamObserver<ApplianceControlRequest> applianceControl(
            StreamObserver<ApplianceControlResponse> responseObserver) {

        logger.info("Appliance-control stream started.");

        return new StreamObserver<ApplianceControlRequest>() {

            /**
             * Called whenever the server receives an appliance command.
             */
            @Override
            public void onNext(ApplianceControlRequest request) {

                String applianceId = request.getApplianceId();

                String command = request
                        .getCommand()
                        .trim()
                        .toUpperCase(Locale.ROOT);

                String commandValue = request
                        .getCommandValue()
                        .trim()
                        .toUpperCase(Locale.ROOT);

                /*
                 * Create a new OFF appliance state if this appliance
                 * has not been controlled before.
                 */
                ApplianceState applianceState =
                        appliances.computeIfAbsent(
                                applianceId,
                                id -> new ApplianceState()
                        );

                boolean success;
                String message;

                synchronized (applianceState) {

                    switch (command) {

                        case "TURN_ON":
                            applianceState.currentStatus =
                                    applianceState.ecoMode.equals("ON")
                                            ? "ECO_MODE"
                                            : "ON";

                            applianceState.powerConsumptionWatts =
                                    applianceState.ecoMode.equals("ON")
                                            ? 500.0
                                            : 800.0;

                            success = true;
                            message = "Appliance "
                                    + applianceId
                                    + " was switched on successfully.";
                            break;

                        case "TURN_OFF":
                            applianceState.currentStatus = "OFF";
                            applianceState.powerConsumptionWatts = 0.0;

                            success = true;
                            message = "Appliance "
                                    + applianceId
                                    + " was switched off successfully.";
                            break;

                        case "SET_ECO_MODE":

                            if (commandValue.equals("ON")) {
                                applianceState.ecoMode = "ON";

                                if (!applianceState.currentStatus
                                        .equals("OFF")) {

                                    applianceState.currentStatus =
                                            "ECO_MODE";

                                    applianceState
                                            .powerConsumptionWatts = 500.0;
                                }

                                success = true;
                                message = "Eco Mode was enabled for appliance "
                                        + applianceId + ".";

                            } else if (commandValue.equals("OFF")) {
                                applianceState.ecoMode = "OFF";

                                if (applianceState.currentStatus
                                        .equals("ECO_MODE")) {

                                    applianceState.currentStatus = "ON";

                                    applianceState
                                            .powerConsumptionWatts = 800.0;
                                }

                                success = true;
                                message = "Eco Mode was disabled for appliance "
                                        + applianceId + ".";

                            } else {
                                success = false;
                                message =
                                        "SET_ECO_MODE requires the value ON or OFF.";
                            }

                            break;

                        case "GET_STATUS":
                            success = true;
                            message = "Current appliance status returned.";
                            break;

                        default:
                            success = false;
                            message = "Unsupported appliance command: "
                                    + command;
                            break;
                    }

                    ApplianceControlResponse response =
                            ApplianceControlResponse
                                    .newBuilder()
                                    .setApplianceId(applianceId)
                                    .setSuccess(success)
                                    .setCurrentStatus(
                                            applianceState.currentStatus
                                    )
                                    .setPowerConsumptionWatts(
                                            applianceState
                                                    .powerConsumptionWatts
                                    )
                                    .setEcoMode(
                                            applianceState.ecoMode
                                    )
                                    .setMessage(message)
                                    .setTimestamp(
                                            System.currentTimeMillis()
                                    )
                                    .build();

                    responseObserver.onNext(response);
                }

                logger.info(
                        "Processed command " + command
                                + " for appliance " + applianceId
                );
            }

            /**
             * Called if an error occurs during the bidirectional stream.
             */
            @Override
            public void onError(Throwable throwable) {

                logger.log(
                        Level.SEVERE,
                        "Error during appliance-control stream.",
                        throwable
                );
            }

            /**
             * Called after the client finishes sending commands.
             */
            @Override
            public void onCompleted() {

                responseObserver.onCompleted();

                logger.info("Appliance-control stream completed.");
            }
        };
    }

    /**
     * Stores the simulated state of one appliance.
     */
    private static class ApplianceState {

        private String currentStatus = "OFF";
        private double powerConsumptionWatts = 0.0;
        private String ecoMode = "OFF";
    }
}