package com.feros.api.gps.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single vehicle telemetry record from the TATA Fleet Edge
 * /api/vehicle-snapshots endpoint.
 *
 * Field names match the real TATA Fleet Edge API response exactly.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TataVehicleSnapshot {

    /** Chassis number — used internally by TATA, NOT used as our providerVehicleId */
    @JsonProperty("vehicleId")
    private String vehicleId;

    /** Registration number — used as providerVehicleId in our mappings */
    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("gpsLatitude")
    private Double gpsLatitude;

    @JsonProperty("gpsLongitude")
    private Double gpsLongitude;

    /** Speed in km/h */
    @JsonProperty("speed")
    private Double speed;

    @JsonProperty("ignitionOn")
    private Boolean ignitionOn;

    /** ISO-8601 timestamp, e.g. "2024-01-15T10:30:00.000+0530" */
    @JsonProperty("eventDateTime")
    private String eventDateTime;

    @JsonProperty("vehicleModel")
    private String vehicleModel;
}
