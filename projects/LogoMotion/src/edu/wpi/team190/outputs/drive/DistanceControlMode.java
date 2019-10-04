package edu.wpi.team190.outputs.drive;

/**
 *
 * @author paul
 */
public class DistanceControlMode extends DriveMode {
    void swapOutOn(SixWheelDrive drive) {
        drive.m_distanceController.disable();
    }

    void swapInOn(SixWheelDrive drive) {
        drive.m_distanceController.enable();
    }

    void drive(SixWheelDrive drive, double left, double right) {
    }
}
