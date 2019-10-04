package edu.wpi.team190.outputs.drive;

/**
 *
 * @author paul
 */
public class DistanceHeadingControlMode extends DriveMode {
    void swapOutOn(SixWheelDrive drive) {
        drive.m_distanceController.disable();
        drive.m_headingController.disable();

        drive.m_distanceController = drive.createDistanceController(drive.m_distanceSource, 
                                                                    drive.m_distanceControllerOutput);
        drive.m_headingController = drive.createHeadingController(drive.m_headingSrc,
                                                                  drive.m_headingControllerOutput);
    }

    void swapInOn(SixWheelDrive drive) {
        drive.m_distanceController = drive.createDistanceController(drive.m_distanceSource,
                                                                    drive.m_controlCombiner.distanceOutput);
        drive.m_headingController = drive.createHeadingController(drive.m_headingSrc,
                                                                  drive.m_controlCombiner.headingOuput);

        drive.m_distanceController.enable();
        drive.m_headingController.enable();
    }

    void drive(SixWheelDrive drive, double left, double right) {
    }
}
