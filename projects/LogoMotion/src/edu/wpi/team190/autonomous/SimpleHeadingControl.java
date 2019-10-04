package edu.wpi.team190.autonomous;

import edu.wpi.team190.Definitions;
import edu.wpi.team190.cntrl.CameraController;
import edu.wpi.team190.outputs.drive.SixWheelDrive;
import edu.wpi.team190.util.IValueReceiver;

/**
 *
 * @author Paul
 */
public class SimpleHeadingControl extends AutonomousMode {
    public void run() {
        // Disable camera heading control
        CameraController.setRequestedHeadingReceiver(new IValueReceiver() {

            public void putValue(double value) {
                // Empty.
            }
        });

        Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.HEADING_CONTROL);
        Definitions.Mechanisms.driveline.setHeading(Definitions.Sensors.gyro.getAngle());
    }
}
