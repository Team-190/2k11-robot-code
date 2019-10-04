package edu.wpi.team190.autonomous;

import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.team190.Definitions;
import edu.wpi.team190.cntrl.CameraController;
import edu.wpi.team190.outputs.Elevator.Position;
import edu.wpi.team190.outputs.drive.SixWheelDrive;
import edu.wpi.team190.util.IValueReceiver;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author brad
 */
public class GoToTargetMode extends AutonomousMode {

    class TiltTask extends TimerTask {

        public void run() {
            Definitions.Mechanisms.wrist.tiltForScoring();
        }
    }

    class ScoreTask extends TimerTask {

        public void run() {
            Definitions.Mechanisms.rollerClaw.pushOut();
            Definitions.Mechanisms.elevator.dropForScoring();
            Definitions.Mechanisms.wrist.down();
            Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.SCORE_MODE);
        }
    }

    class RaiseElevTask extends TimerTask {

        public void run() {
            Definitions.Mechanisms.elevator.setPosition(Position.TOP, false);
        }
    }
    Timer timer = new Timer();

    public void run() {
        synchronized (this) {
            try {
                // Disable camera heading control
                CameraController.setRequestedHeadingReceiver(new IValueReceiver() {

                    public void putValue(double value) {
                        //Empty;
                    }
                });
                showAutoInfo("Starting Auto...");

                timer.schedule(new RaiseElevTask(), 10000);
                timer.schedule(new TiltTask(), 12000);
                timer.schedule(new ScoreTask(), 13000);

                Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.HEADING_CONTROL);
                Definitions.Mechanisms.driveline.setHeading(Definitions.Sensors.gyro.getAngle());
                Definitions.Mechanisms.driveline.drive(0.7, 0.7);
                Definitions.Mechanisms.wrist.stowed();
                wait(500);
                Definitions.Mechanisms.elevator.setPosition(Position.MIDDLE, false);
                wait(300);
                
                while(Definitions.Sensors.ultrasonic.getInches() > 65)
                    wait(10);
                Definitions.Mechanisms.driveline.drive(0, 0);
                
                showAutoInfo("Correcting heading...");

                CameraController.setRequestedHeadingReceiver(new IValueReceiver() {

                    public void putValue(double value) {
                        Definitions.Mechanisms.driveline.setHeading(value);
                    }
                });
                wait(1100);

                CameraController.setRequestedHeadingReceiver(new IValueReceiver() {

                    public void putValue(double value) {
                        //Empty;
                    }
                });

                showAutoInfo("Preparing to score...");
                Definitions.Mechanisms.elevator.setPosition(Position.TOP, false);   
                // Raise elevator, wait until there
                while (!Definitions.Mechanisms.elevator.isPositionWithinTolerance(0.35)) {
                    wait(10);
                }

                Definitions.Mechanisms.wrist.tiltForScoring();
                wait(500);

                showAutoInfo(" Driving forward...");
                Definitions.Mechanisms.driveline.drive(0.7, 0.7);
                while(Definitions.Sensors.ultrasonic.getInches() > 30)
                    wait(10);
                Definitions.Mechanisms.driveline.drive(0, 0);

                showAutoInfo("Scoring...");
                Definitions.Mechanisms.elevator.dropForScoring();
                Definitions.Mechanisms.wrist.down();
                Definitions.Mechanisms.rollerClaw.pushOut();
                timer.cancel();

                wait(1000);

                showAutoInfo("Reversing...");
                Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
                Definitions.Mechanisms.driveline.drive(-.5, -.5);
                
                wait(1000);

                Definitions.Mechanisms.driveline.drive(0, 0);
                showAutoInfo("Stowing...");
                Definitions.Mechanisms.wrist.stowed();
                Definitions.Mechanisms.rollerClaw.stopRollers();

                wait(500);

                Definitions.Mechanisms.elevator.setPosition(Position.BOTTOM, false);
                showAutoInfo("Done.");
            } catch (InterruptedException e) {
                timer.cancel();
            }
        }
    }

    private void showAutoInfo(String info) {
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kMain6, 1, info);
        DriverStationLCD.getInstance().updateLCD();
    }
};
