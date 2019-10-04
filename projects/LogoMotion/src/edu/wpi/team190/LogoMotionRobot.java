/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.team190;

import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.SmartDashboard;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.team190.autonomous.AutonomousMode;
import edu.wpi.team190.cntrl.CameraController;
import edu.wpi.team190.cntrl.operator.ErrorTracker;
import edu.wpi.team190.cntrl.operator.OI;
import edu.wpi.team190.outputs.Elevator.Position;
import edu.wpi.team190.outputs.Lights;
import edu.wpi.team190.outputs.drive.SixWheelDrive;
import edu.wpi.team190.util.IValueReceiver;
import edu.wpi.team190.util.IValueSource;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class LogoMotionRobot extends SimpleRobot implements OI.IAutonomousModeListener {

    class MinibotDeploymentEvent extends TimerTask {

        public void run() {
            Definitions.Mechanisms.deployment.minibotLaunch();
            m_timer.schedule(new RaiseDeploymentEvent(), 1700);
        }
    }

    class RaiseDeploymentEvent extends TimerTask {

        public void run() {
            if (Definitions.Mechanisms.deployment.isArmed()) {
                Definitions.Mechanisms.deployment.deploymentSystemUp();
            }
        }
    }

    class ScoreResetTask extends TimerTask {

        public void run() {
            Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
            Definitions.Mechanisms.rollerClaw.stopRollers();
        }
    }

    class KillAutonomous extends TimerTask {

        public void run() {
            operatorControl();
        }
    }
    private AutonomousMode m_selectedMode;
    private static Timer m_timer;

    public LogoMotionRobot() {
        m_timer = new Timer();

        Watchdog.getInstance().setEnabled(false);
        Definitions.init();
        Definitions.Outputs.DriveTrain.setSafetyEnabled(false);

        IValueSource headingSource = new IValueSource() {

            public double getValue() {
                return Definitions.Sensors.gyro.getAngle();
            }
        };

        IValueSource distanceSource = new IValueSource() {

            public double getValue() {
                return Definitions.Sensors.ultrasonic.getInches();
            }
        };

        IValueReceiver cameraHeadingReceiver = new IValueReceiver() {

            public void putValue(double value) {
                Definitions.Mechanisms.driveline.setHeading(value);
            }
        };

        CameraController.setup(headingSource, distanceSource, cameraHeadingReceiver);
        CameraController.enable();

        /*
         * OI Event Receivers
         */

        OI.IDriveHandler driveHandler = new OI.IDriveHandler() {

            public void drive(double left, double right) {
                Definitions.Mechanisms.driveline.drive(left, right);
            }

            public void normalMode() {
                Definitions.Mechanisms.lights.setCameraLights(false);
                Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
            }

            public void headingControlMode() {
                Definitions.Mechanisms.lights.setCameraLights(true);
                Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.HEADING_CONTROL);
            }
        };
        OI.setDriveHandler(driveHandler);

        OI.IRollerClawHandler rollerClawHandler = new OI.IRollerClawHandler() {

            public void down() {
                Definitions.Mechanisms.rollerClaw.turnDown();
            }

            public void up() {
                Definitions.Mechanisms.rollerClaw.turnUp();
            }

            public void out() {
                Definitions.Mechanisms.rollerClaw.pushOut();
                Definitions.Mechanisms.elevator.dropForScoring();
                Definitions.Mechanisms.wrist.down();
                Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.SCORE_MODE);
                m_timer.schedule(new ScoreResetTask(), 1000);
            }

            public void in() {
                Definitions.Mechanisms.rollerClaw.pullIn();
                Definitions.Mechanisms.wrist.down();
                Definitions.Mechanisms.elevator.setPosition(Position.BASELINE, false);
                Definitions.Mechanisms.lights.setClawHasTubeLight(false);
            }

            public void stop() {
                Definitions.Mechanisms.rollerClaw.stopRollers();
                Definitions.Mechanisms.wrist.stowed();
            }
        };
        OI.setRollerClawHandler(rollerClawHandler);

        OI.IHumanPlayerLightHandler humanPlayerLightHandler = new OI.IHumanPlayerLightHandler() {

            public void red() {
                Definitions.Mechanisms.lights.setRequestedTubeLight(Lights.Colors.RED);
            }

            public void white() {
                Definitions.Mechanisms.lights.setRequestedTubeLight(Lights.Colors.WHITE);
            }

            public void blue() {
                Definitions.Mechanisms.lights.setRequestedTubeLight(Lights.Colors.BLUE);
            }
        };
        OI.setHumanPlayerLightHandler(humanPlayerLightHandler);
        OI.IWristHandler wristHandler = new OI.IWristHandler() {

            public void up() {
                Definitions.Mechanisms.wrist.stowed();
            }

            public void down() {
                Definitions.Mechanisms.wrist.down();
            }

            public void jogUp() {
                Definitions.Mechanisms.wrist.jogUp();
            }

            public void jogDown() {
                Definitions.Mechanisms.wrist.jogDown();
            }
        };
        OI.setWristHandler(wristHandler);
        OI.setAutonomousModeListener(this);

        OI.IElevatorHandler elevHandler = new OI.IElevatorHandler() {

            public void outerBottom() {
                Definitions.Mechanisms.elevator.setPosition(Position.BOTTOM, false);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void outerMiddle() {
                Definitions.Mechanisms.elevator.setPosition(Position.MIDDLE, false);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void outerTop() {
                Definitions.Mechanisms.elevator.setPosition(Position.TOP, false);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void innerBottom() {
                Definitions.Mechanisms.elevator.setPosition(Position.BOTTOM, true);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void innerMiddle() {
                Definitions.Mechanisms.elevator.setPosition(Position.MIDDLE, true);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void innerTop() {
                Definitions.Mechanisms.elevator.setPosition(Position.TOP, true);
                Definitions.Mechanisms.wrist.tiltForScoring();
            }

            public void jogUp() {
                Definitions.Mechanisms.elevator.jogUp();
            }

            public void jogDown() {
                Definitions.Mechanisms.elevator.jogDown();
            }
        };
        OI.setElevatorHandler(elevHandler);

        OI.IWingsHandler wingsHandler = new OI.IWingsHandler() {

            public void up() {
                // Damage results; do not use!
                Definitions.Mechanisms.alignment.alignmentSystemUp();
            }

            public void down() {
                Definitions.Mechanisms.alignment.alignmentSystemDown();
            }
        };
        OI.setWingsHandler(wingsHandler);

        OI.setDeploymentHandler(new OI.IDeploymentHandler() {

            public void rampUp() {
                Definitions.Mechanisms.deployment.deploymentSystemUp();
            }

            public void rampDown() {
                Definitions.Mechanisms.deployment.deploymentSystemDown();
            }

            public void armAutoDeployment() {
                Definitions.Mechanisms.deployment.armMinibot();
            }

            public void disarmAutoDeployment() {
                Definitions.Mechanisms.deployment.disarmMinibot();
            }

            public void launchMicrobot() {
                Definitions.Mechanisms.deployment.minibotLaunch();
            }

            public void overrideLaunchMinibot() {
                Definitions.Mechanisms.deployment.overrideArmMinibotLaunch();
            }
        });

        Definitions.Mechanisms.rollerClaw.setHasTubeHandler(new Runnable() {

            public void run() {
                Definitions.Mechanisms.wrist.stowed();
                Definitions.Mechanisms.lights.setClawHasTubeLight(true);
            }
        });

        Definitions.Mechanisms.deployment.setFireHandler(new Runnable() {

            public void run() {
                Definitions.Mechanisms.lights.showDeploymentLights();
            }
        });
    }

    /**
     * Starts any/all appropriate system threads
     */
    public void robotInit() {
        Definitions.Mechanisms.rollerClaw.start();
        Definitions.Mechanisms.lights.start();
        Definitions.Mechanisms.alignment.start();
        OI.enable();

        ErrorTracker.getInstance().clearDisplay();
    }

    /**
     * This function is called once each time the robot enters autonomous mode.
     */
    public void autonomous() {
        Definitions.Mechanisms.lights.setMorseCodeEnabled(true);
        Definitions.Mechanisms.deployment.lockMinibot();
        OI.disable();

        Definitions.Mechanisms.lights.setCameraLights(true);
        try {
            if (m_selectedMode != null) {
                m_selectedMode.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This function is called when called by the OI
     */
    public void runAutonomousMode() {
        autonomous();
        m_timer.schedule(new KillAutonomous(), 15000);
    }

    /**
     * This function is called once each time the robot enters operator control.
     */
    public void operatorControl() {
        if (m_selectedMode != null) {
            m_selectedMode.interrupt();
            m_selectedMode = null;
        }
        Definitions.Mechanisms.deployment.lockMinibot();
        Definitions.Mechanisms.lights.setCameraLights(true);
        Definitions.Mechanisms.lights.setMorseCodeEnabled(false);
        Definitions.Mechanisms.driveline.setMode(SixWheelDrive.Modes.NORMAL);
        OI.enable();
        m_timer.schedule(new MinibotDeploymentEvent(), 110000);

        // Re-enable camera heading control
        CameraController.setRequestedHeadingReceiver(new IValueReceiver() {

            public void putValue(double value) {
                Definitions.Mechanisms.driveline.setHeading(value);
            }
        });
    }

    public void selectMode(AutonomousMode mode) {
        m_selectedMode = mode;
    }

    public AutonomousMode getAutonomousMode() {
        return m_selectedMode;
    }

    public void disabled() {
        Definitions.Mechanisms.lights.setMorseCodeEnabled(true);

        while (isDisabled()) {
            SmartDashboard.log(Definitions.Sensors.ultrasonic.getInches(), "Ultrasonic");
            SmartDashboard.log(Definitions.Sensors.gyro.getAngle(), "Gyro");
            edu.wpi.first.wpilibj.Timer.delay(0.020);
        }
    }
}
