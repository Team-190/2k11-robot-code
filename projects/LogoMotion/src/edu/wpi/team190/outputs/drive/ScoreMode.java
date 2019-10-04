package edu.wpi.team190.outputs.drive;

/**
 *
 * @author paul
 */
public class ScoreMode extends DriveMode {
    void swapOutOn(SixWheelDrive drive) {
    }

    void swapInOn(SixWheelDrive drive) {

    }

    void drive(SixWheelDrive drive, double left, double right) {
        drive.m_driveline.tankDrive(left -.5, right -.5);
    }
}
