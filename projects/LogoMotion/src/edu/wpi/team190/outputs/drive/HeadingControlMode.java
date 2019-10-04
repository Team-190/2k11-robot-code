package edu.wpi.team190.outputs.drive;

/**
 *
 * @author paul
 */
public class HeadingControlMode extends DriveMode {
    void swapOutOn(SixWheelDrive drive) {
        drive.m_headingController.disable();
    }

    void swapInOn(SixWheelDrive drive) {
        drive.m_headingController.enable();
    }

    void drive(SixWheelDrive drive, double left, double right) {
        drive.m_requestedDriveMagnitude = right;
    }
}
