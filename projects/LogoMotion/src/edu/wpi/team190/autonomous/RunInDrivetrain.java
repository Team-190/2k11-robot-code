package edu.wpi.team190.autonomous;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.team190.Definitions;
import edu.wpi.team190.outputs.drive.SixWheelDrive;

/**
 *
 * @author pmalmsten
 */
public class RunInDrivetrain extends AutonomousMode {
    public void run() {
        Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
        System.out.println("Autonomous - Running in Drivetrain...");
        // Ramp up, 5 seconds
        System.out.println(" Ramp Up, FWD");
        for(int i = 0; i < 5000; i++) {
            Definitions.Mechanisms.driveline.drive(i / 5000.0, i / 5000.0);
            Timer.delay(0.001);
        }
        System.out.println(" Full FWD");
        Definitions.Mechanisms.driveline.drive(1, 1);

        // Run forward for 5 min
        Timer.delay(60 * 5);

        // Ramp down
        System.out.println(" Ramp Down, FWD");
        for(int i = 5000; i > 0; i--) {
            Definitions.Mechanisms.driveline.drive(i / 5000.0, i / 5000.0);
            Timer.delay(0.001);
        }
        Definitions.Mechanisms.driveline.drive(0, 0);
        System.out.println(" Stop");

        // Ramp up, reverse, 5 seconds
        System.out.println(" Ramp Up, REV");
        for(int i = 0; i < 5000; i++) {
            Definitions.Mechanisms.driveline.drive(-i / 5000.0, -i / 5000.0);
            Timer.delay(0.001);
        }
        System.out.println(" Full REV");
        Definitions.Mechanisms.driveline.drive(-1, -1);

         // Run reverse for 5 min
        Timer.delay(60 * 5);

        // Ramp down, reverse, 5 seconds
        System.out.println(" Ramp Down, REV");
        for(int i = 5000; i > 0; i--) {
            Definitions.Mechanisms.driveline.drive(-i / 5000.0, -i / 5000.0);
            Timer.delay(0.001);
        }
        Definitions.Mechanisms.driveline.drive(0, 0);
        System.out.println(" Stop");
        System.out.println(" Done");
    }
}
