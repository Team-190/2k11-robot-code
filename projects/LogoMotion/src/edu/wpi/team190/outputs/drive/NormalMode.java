package edu.wpi.team190.outputs.drive;

/**
 *
 * @author paul
 */
public class NormalMode extends DriveMode {
    void swapOutOn(SixWheelDrive drive) {
    }

    void swapInOn(SixWheelDrive drive) {
        drive.m_distanceController.disable();
        drive.m_headingController.disable();
    }

    void drive(SixWheelDrive drive, double left, double right) {
        drive.m_driveline.tankDrive(left, right);
    }
}
