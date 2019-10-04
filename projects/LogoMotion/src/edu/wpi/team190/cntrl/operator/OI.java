package edu.wpi.team190.cntrl.operator;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO.EnhancedIOException;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.team190.autonomous.GoToTargetMode;
import edu.wpi.team190.autonomous.AutonomousMode;

/**
 *
 * @author aaron
 */
public class OI extends Thread {

    static boolean enhancedIOErrorDisplayed = true;
    final DriverStation station;
    static final int LEFT_STICK_AXIS = Joystick.AxisType.kZ.value;
    static final int RIGHT_STICK_AXIS = Joystick.AxisType.kThrottle.value;
    static boolean isEnabled = false;
    static OI instance = null;
    static DriverStationEnhancedIO enhancedIO = null;
    // Event handlers
    static IDriveHandler driveHandler;
    static IRollerClawHandler rollerClawHandler;
    static IAutonomousModeListener autonomousModeListener;
    static IElevatorHandler elevatorHandler;
    static IHumanPlayerLightHandler humanPlayerLightHandler;
    static IWristHandler wristHandler;
    static IWingsHandler wingsHandler;
    static IDeploymentHandler deploymentHandler;
    static int enhancedIOInitializedCountDown = 0;
    static boolean scoreButtonPreviousState = !IOPorts.ACTIVE_STATE;
    static Joystick m_leftStick = new Joystick(1);
    static Joystick m_rightStick = new Joystick(2);

    public interface IAutonomousModeListener {

        public void selectMode(AutonomousMode mode);

        public AutonomousMode getAutonomousMode();

        public void runAutonomousMode();
    }

    public interface IRollerClawHandler {

        public void down();

        public void up();

        public void out();

        public void in();

        public void stop();
    }

    public interface IDriveHandler {

        public void drive(double left, double right);

        public void headingControlMode();

        public void normalMode();
    }

    public interface IElevatorHandler {

        public void outerBottom();

        public void outerMiddle();

        public void outerTop();

        public void innerBottom();

        public void innerMiddle();

        public void innerTop();

        public void jogUp();

        public void jogDown();
    }

    public interface IHumanPlayerLightHandler {

        public void red();

        public void white();

        public void blue();
    }

    public interface IWristHandler {

        public void up();

        public void down();

        public void jogUp();

        public void jogDown();
    }

    public interface IWingsHandler {

        public void up();

        public void down();
    }

    public interface IDeploymentHandler {

        public void rampUp();

        public void rampDown();

        public void armAutoDeployment();

        public void disarmAutoDeployment();

        public void launchMicrobot();

        public void overrideLaunchMinibot();
    }

    public static class IOPorts {

        public static final boolean ACTIVE_STATE = false;
        public static final double ACTIVE_BELOW_THRESHOLD = 0.5;

        public static class RollerClaw {

            public static final int COLLECT = 8;
            public static final int STOP_COLLECT = 6;
            public static final int SCORE = 4;
            // Analog
            public static final int TURN_UP = 2;
            public static final int TURN_DOWN = 3;
        }

        public static class Elevator {

            public static final int OUTER_BOTTOM = 14;
            public static final int OUTER_MIDDLE = 16;
            public static final int INNER_BOTTOM = 12;

            // Analog
            public static class Analog {

                public static final int JOG_UP = 5;
                public static final int JOG_DOWN = 7;
                public static final int OUTER_TOP = 6;
                public static final int INNER_MIDDLE = 4;
                public static final int INNER_TOP = 8;
            }
        }

        public static class HumanPlayerLight {

            public static final int RED_LIGHT = 11;
            public static final int WHITE_LIGHT = 9;
            public static final int BLUE_LIGHT = 15;
        }

        public static class Wrist {

            public static final int JOG_UP = 2;
            public static final int JOG_DOWN = 10;
        }

        public static class Wings {

            public static final int UP = 7;
            public static final int DOWN = 5;
        }

        public static class Deployment {
            public static final int UP = 1;
            public static final int ARM = 3;
            public static final int OVERRIDE = 13;
        }
    }

    private static class ButtonMasks {

        public static final int BUTTON_1 = 1;
        public static final int BUTTON_2 = 2;
        public static final int BUTTON_3 = 4;
        public static final int BUTTON_4 = 8;
        public static final int BUTTON_5 = 16;
        public static final int BUTTON_6 = 32;
        public static final int BUTTON_7 = 64;
        public static final int BUTTON_8 = 128;
        public static final int BUTTON_9 = 256;
        public static final int BUTTON_10 = 512;
        public static final int BUTTON_11 = 1024;
    }

    private OI() {
        station = DriverStation.getInstance();
    }

    public void execute() {
        enhancedIO = station.getEnhancedIO();

       

        if (--enhancedIOInitializedCountDown <= 0) {
            try {
                for (int i = 1; i < 17; i++) {
                    enhancedIO.setDigitalConfig(i, DriverStationEnhancedIO.tDigitalConfig.kInputPullUp);
                }
                enhancedIOInitializedCountDown = 100;
            } catch (EnhancedIOException ex) {
                ex.printStackTrace();
            }
        }

        if (driveHandler != null) {
            driveHandler.drive(-m_leftStick.getY(), -m_rightStick.getY());

            if ((station.getStickButtons(1) & ButtonMasks.BUTTON_3) == ButtonMasks.BUTTON_3
                    && ((station.getStickButtons(2) & ButtonMasks.BUTTON_3) == ButtonMasks.BUTTON_3)) {
                driveHandler.headingControlMode();
            } else {
                driveHandler.normalMode();
            }

        }
 
        if (rollerClawHandler != null) {
            try {
                if (enhancedIO.getDigital(IOPorts.RollerClaw.COLLECT) == IOPorts.ACTIVE_STATE) {
                    rollerClawHandler.in();
                } else if (enhancedIO.getDigital(IOPorts.RollerClaw.STOP_COLLECT) == IOPorts.ACTIVE_STATE) {
                    rollerClawHandler.stop();
                } else if (enhancedIO.getDigital(IOPorts.RollerClaw.SCORE) == IOPorts.ACTIVE_STATE
                        && scoreButtonPreviousState != IOPorts.ACTIVE_STATE) {
                    rollerClawHandler.out();
                } else if (enhancedIO.getAnalogIn(IOPorts.RollerClaw.TURN_DOWN) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    rollerClawHandler.down();
                } else if (enhancedIO.getAnalogIn(IOPorts.RollerClaw.TURN_UP) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    rollerClawHandler.up();
                }


                if ((station.getStickButtons(1) & ButtonMasks.BUTTON_1) == ButtonMasks.BUTTON_1
                        && ((station.getStickButtons(2) & ButtonMasks.BUTTON_1) == ButtonMasks.BUTTON_1)
                        && scoreButtonPreviousState != IOPorts.ACTIVE_STATE) {
                    rollerClawHandler.out();
                }

                if ((station.getStickButtons(1) & ButtonMasks.BUTTON_2) == ButtonMasks.BUTTON_2
                        && ((station.getStickButtons(2) & ButtonMasks.BUTTON_2) == ButtonMasks.BUTTON_2)
                        && scoreButtonPreviousState != IOPorts.ACTIVE_STATE) {
                    rollerClawHandler.in();
                }

                if(enhancedIO.getDigital(IOPorts.RollerClaw.SCORE) == IOPorts.ACTIVE_STATE
                        || ((station.getStickButtons(1) & ButtonMasks.BUTTON_1) == ButtonMasks.BUTTON_1
                        && ((station.getStickButtons(2) & ButtonMasks.BUTTON_1) == ButtonMasks.BUTTON_1))) {
                   scoreButtonPreviousState = IOPorts.ACTIVE_STATE;
                } else {
                    scoreButtonPreviousState = !IOPorts.ACTIVE_STATE;
                }
            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }

        //TODO: add auto. mode selection stuff here
        if (autonomousModeListener != null) {
            if(autonomousModeListener.getAutonomousMode() == null)
                autonomousModeListener.selectMode(new GoToTargetMode());

            if ((station.getStickButtons(1) & ButtonMasks.BUTTON_7) == ButtonMasks.BUTTON_7 &&
                    (station.getStickButtons(2) & ButtonMasks.BUTTON_7) == ButtonMasks.BUTTON_7) {
                autonomousModeListener.runAutonomousMode();
            }
        }

        if (humanPlayerLightHandler != null) {
            try {
                if (enhancedIO.getDigital(IOPorts.HumanPlayerLight.BLUE_LIGHT) == IOPorts.ACTIVE_STATE) {
                    humanPlayerLightHandler.blue();
                } else if (enhancedIO.getDigital(IOPorts.HumanPlayerLight.WHITE_LIGHT) == IOPorts.ACTIVE_STATE) {
                    humanPlayerLightHandler.white();
                } else if (enhancedIO.getDigital(IOPorts.HumanPlayerLight.RED_LIGHT) == IOPorts.ACTIVE_STATE) {
                    humanPlayerLightHandler.red();
                }
            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }

        
        if (elevatorHandler != null) {
            try {
                if (enhancedIO.getDigital(IOPorts.Elevator.INNER_BOTTOM) == IOPorts.ACTIVE_STATE) {
                    elevatorHandler.innerBottom();
                } else if (enhancedIO.getAnalogIn(IOPorts.Elevator.Analog.INNER_MIDDLE) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    elevatorHandler.innerMiddle();
                } else if (enhancedIO.getAnalogIn(IOPorts.Elevator.Analog.INNER_TOP) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    elevatorHandler.innerTop();
                } else if (enhancedIO.getDigital(IOPorts.Elevator.OUTER_BOTTOM) == IOPorts.ACTIVE_STATE) {
                    elevatorHandler.outerBottom();
                } else if (enhancedIO.getDigital(IOPorts.Elevator.OUTER_MIDDLE) == IOPorts.ACTIVE_STATE) {
                    elevatorHandler.outerMiddle();
                } else if (enhancedIO.getAnalogIn(IOPorts.Elevator.Analog.OUTER_TOP) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    elevatorHandler.outerTop();
                } else if (enhancedIO.getAnalogIn(IOPorts.Elevator.Analog.JOG_UP) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    elevatorHandler.jogUp();
                } else if (enhancedIO.getAnalogIn(IOPorts.Elevator.Analog.JOG_DOWN) < IOPorts.ACTIVE_BELOW_THRESHOLD) {
                    elevatorHandler.jogDown();
                }

            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }

        if (wristHandler != null) {
            try {
                if (enhancedIO.getDigital(IOPorts.Wrist.JOG_UP) == IOPorts.ACTIVE_STATE) {
                    wristHandler.jogUp();
                } else if (enhancedIO.getDigital(IOPorts.Wrist.JOG_DOWN) == IOPorts.ACTIVE_STATE) {
                    wristHandler.jogDown();
                }
            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }

        if (wingsHandler != null) {
            try {
                if (enhancedIO.getDigital(IOPorts.Wings.DOWN) == IOPorts.ACTIVE_STATE) {
                    wingsHandler.down();
                } else if (enhancedIO.getDigital(IOPorts.Wings.UP) == IOPorts.ACTIVE_STATE) {
                    wingsHandler.up();
                } else if ((station.getStickButtons(1) & ButtonMasks.BUTTON_10) == ButtonMasks.BUTTON_10 &&
                        (station.getStickButtons(2) & ButtonMasks.BUTTON_10) == ButtonMasks.BUTTON_10) {
                    wingsHandler.up();
                } else if ((station.getStickButtons(1) & ButtonMasks.BUTTON_5) == ButtonMasks.BUTTON_5 &&
                        (station.getStickButtons(2) & ButtonMasks.BUTTON_5) == ButtonMasks.BUTTON_5) {
                    wingsHandler.down();
                }
            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }
        if (deploymentHandler !=null) {
            try {
                if (enhancedIO.getDigital(IOPorts.Deployment.UP) == IOPorts.ACTIVE_STATE) {
                    deploymentHandler.rampUp();
                } else {
                    deploymentHandler.rampDown();
                } 

                if (enhancedIO.getDigital(IOPorts.Deployment.ARM) == IOPorts.ACTIVE_STATE) {
                    deploymentHandler.armAutoDeployment();
                } else {
                    deploymentHandler.disarmAutoDeployment();
                }

                if(enhancedIO.getDigital(IOPorts.Deployment.OVERRIDE) == IOPorts.ACTIVE_STATE) {
                    deploymentHandler.overrideLaunchMinibot();
                }

                if ((station.getStickButtons(1) & ButtonMasks.BUTTON_11) == ButtonMasks.BUTTON_11 &&
                        (station.getStickButtons(2) & ButtonMasks.BUTTON_11) == ButtonMasks.BUTTON_11) {
                    deploymentHandler.rampUp();
                } else if ((station.getStickButtons(1) & ButtonMasks.BUTTON_4) == ButtonMasks.BUTTON_4 &&
                        (station.getStickButtons(2) & ButtonMasks.BUTTON_4) == ButtonMasks.BUTTON_4) {
                    deploymentHandler.rampDown();
                }
            } catch (EnhancedIOException ex) {
                if (!enhancedIOErrorDisplayed) {
                    ex.printStackTrace();
                    enhancedIOErrorDisplayed = true;
                }
            }
        }
        
    }

    public void run() {
        while (isEnabled) {
            execute();
            station.waitForData(100);
        }
    }

    public static void enable() {
        if (instance == null) {
            isEnabled = true;
            instance = new OI();
            instance.start();
        }
    }

    public static void disable() {
        isEnabled = false;

        while (instance != null && instance.isAlive()) {
            Timer.delay(0.01);
        }

        instance = null;
    }

    private boolean buttonPressed(int buttons, int mask) {
        return (buttons & mask) != 0;
    }

    public static void setDriveHandler(IDriveHandler handler) {
        driveHandler = handler;
    }

    public static void setRollerClawHandler(IRollerClawHandler handler) {
        rollerClawHandler = handler;
    }

    public static void setAutonomousModeListener(IAutonomousModeListener listener) {
        autonomousModeListener = listener;
    }

    public static void setElevatorHandler(IElevatorHandler handler) {
        elevatorHandler = handler;
    }

    public static void setHumanPlayerLightHandler(IHumanPlayerLightHandler handler) {
        humanPlayerLightHandler = handler;
    }

    public static void setWristHandler(IWristHandler handler) {
        wristHandler = handler;
    }

    public static void setWingsHandler(IWingsHandler handler) {
        wingsHandler = handler;
    }

    public static void setDeploymentHandler(IDeploymentHandler handler) {
        deploymentHandler = handler;
    }
}
