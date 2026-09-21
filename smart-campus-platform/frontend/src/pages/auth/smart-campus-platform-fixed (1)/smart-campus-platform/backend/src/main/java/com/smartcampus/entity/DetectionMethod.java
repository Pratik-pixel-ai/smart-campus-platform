package com.smartcampus.entity;

/**
 * How a student was marked present. Stored on every record so the audit trail is
 * honest about which attendance was hardware-verified and which was simulated.
 */
public enum DetectionMethod {
    /** Simulated detection used for demos and when no BLE hardware is available. */
    DEMO,
    /** Real BLE advertisement scanned by the faculty device. */
    BLE,
    /** Entered by the faculty member by hand. */
    MANUAL,
    /** Written by the system when a session is closed. */
    SYSTEM
}
