package edu.wpi.team190.autonomous;

import edu.wpi.team190.Definitions;
import edu.wpi.team190.outputs.Elevator.Position;
import edu.wpi.team190.outputs.drive.SixWheelDrive;

/**
 *
 * @author brad
 */
public class StraightenedCameraGoToTargetMode extends AutonomousMode {

    public void run() {
        try {
            Definitions.Mechanisms.elevator.setPosition(Position.TOP, false);
            Definitions.Mechanisms.wrist.tiltForScoring();
            Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.DISTANCE_HEADING_CONTROL);
            Definitions.Mechanisms.driveline.setDistanceFromWallSetpoint(25);

            while (Definitions.Sensors.ultrasonic.getInches() > 25)
                wait(10);
            
            wait(1000);

            Definitions.Mechanisms.rollerClaw.pushOut();
            Definitions.Mechanisms.elevator.dropForScoring();

            wait(500);

            Definitions.Mechanisms.rollerClaw.stopRollers();
            Definitions.Mechanisms.wrist.stowed();

            Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
            Definitions.Mechanisms.driveline.drive(-1, -1);

            wait(2000);

            Definitions.Mechanisms.driveline.drive(0, 0);
        } catch (InterruptedException e) {
        }
    }
};
