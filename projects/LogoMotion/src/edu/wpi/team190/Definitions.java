package edu.wpi.team190;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.can.CANNotInitializedException;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.team190.cntrl.operator.ErrorTracker;
import edu.wpi.team190.inputs.AnalogUltraSonic;
import edu.wpi.team190.outputs.Elevator;
import edu.wpi.team190.outputs.RollerClaw;
import edu.wpi.team190.outputs.drive.SixWheelDrive;
import edu.wpi.team190.outputs.Lights;
import edu.wpi.team190.outputs.Wrist;
import edu.wpi.team190.outputs.Deployment;
import edu.wpi.team190.outputs.Alignment;
import edu.wpi.team190.util.FakeCANJaguar;
import edu.wpi.team190.util.PIDJaguar;

/**
 *
 * @author brad
 */
public class Definitions {
    public static final double GYRO_V_PER_D_PER_S = 0.0070;
    public static final int CAN_RETRIES = 5;

    public static class Sensors {
	public static final Gyro gyro = new Gyro(1);
	public static final AnalogUltraSonic ultrasonic = new AnalogUltraSonic(2);

	public static class Deployment {
	    private static final DigitalInput deployUp = new DigitalInput (4);
	    private static final DigitalInput deployDown = new DigitalInput (5);

            public static void printStatus() {
                System.out.println("Deployment");
                System.out.println(" Up sensor: " + deployUp.get());
                System.out.println(" Down sensor: " + deployDown.get());
            }
	}

        public static class Alignment {
            private static final DigitalInput alignUp = new DigitalInput (2);
            private static final DigitalInput alignDown = new DigitalInput (1);

            public static void printStatus() {
                System.out.println("Alignment");
                System.out.println(" Up: " + alignUp.get());
                System.out.println(" Down: " + alignDown.get());
            }
        }

	public static class Elevator {
	    private static final DigitalInput top = new DigitalInput(8);
	    private static final DigitalInput bottom = new DigitalInput (9);
            private static final AnalogChannel position = new AnalogChannel(3);
	}

	public static class RollerClaw {
	    private static final DigitalInput tubeDetector = new DigitalInput(3);
//	    private static final boolean TUBE_DETECTOR_ACTIVE_STATE = true;
	    private static final boolean TUBE_DETECTOR_ACTIVE_STATE = false;

            public static void printStatus() {
                System.out.println("Claw");
                System.out.println(" Detector: " + tubeDetector.get());
            }
	}

        public static class Wrist {
            private static final AnalogChannel position = new AnalogChannel(4);
        }

    }

    public static class Outputs {
	public static class DriveTrain {
	    private static Jaguar left = new Jaguar(1);
	    private static Jaguar right = new Jaguar(3);
	    /*private static Jaguar r1CIM = new Jaguar(3);
	    private static Jaguar r2CIM = new Jaguar(4);*/

            private static void init() {
            }

	    public static void setSafetyEnabled(boolean value) {
		left.setSafetyEnabled(value);
		right.setSafetyEnabled(value);
		/*r1CIM.setSafetyEnabled(value);
		r2CIM.setSafetyEnabled(value);*/
	    }
	}

	private static class RollerClaw {
	    private static final Victor topRoller = new Victor(7);
	    private static final Victor bottomRoller = new Victor(6);
	}

	private static class Wrist {
            
	    private static PIDJaguar motor;

            private static void init() {
                try {
                    motor = new FakeCANJaguar(2, Sensors.Wrist.position);
                } catch (CANTimeoutException ex) {
                    ex.printStackTrace();
                    ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, edu.wpi.team190.outputs.Wrist.WRIST_MECHANISM_NAME);
                } catch (CANNotInitializedException ex) {
                    ex.printStackTrace();
                    ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_NOT_INITED);
                }
            }
	}

	private static class Elevator {
	    private static PIDJaguar motor1;
	    private static PIDJaguar motor2;

            private static void init() {
                try {
                    motor1 = new FakeCANJaguar(10, Sensors.Elevator.position);
                } catch (CANTimeoutException ex) {
                    ex.printStackTrace();
                    ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, edu.wpi.team190.outputs.Elevator.ELEVATOR_MECHANISM_NAME);
                } catch (CANNotInitializedException ex) {
                    ex.printStackTrace();
                    ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_NOT_INITED);
                }
            }
	}

	private static class Deployment {
	    private static final Victor rampMotor = new Victor(5);
            private static final Servo servo = new Servo(8);
             // Solenoids use separate numbering.
        }

        private static class Alignment {
            private static final SpeedController wingsMotor = new Victor(9);
        }

	private static class Lights {
	    private static final DigitalOutput redLight = new DigitalOutput(13);
	    private static final DigitalOutput whiteLight = new DigitalOutput(11);
	    private static final DigitalOutput blueLight = new DigitalOutput(12);
	    private static final DigitalOutput greenLight = new DigitalOutput(14);
            private static final DigitalOutput cameraLights = new DigitalOutput(10);
	}
    }

    public static class Mechanisms {
	public static RollerClaw rollerClaw;
        public static SixWheelDrive driveline;
        public static Lights lights;
        public static Wrist wrist;
        public static Deployment deployment;
        public static Alignment alignment;
        public static Elevator elevator;

        private static void init() {
            rollerClaw = new RollerClaw(Outputs.RollerClaw.topRoller,
                                    Outputs.RollerClaw.bottomRoller,
                                    Sensors.RollerClaw.tubeDetector,
                                    Sensors.RollerClaw.TUBE_DETECTOR_ACTIVE_STATE);
            driveline = new SixWheelDrive(Outputs.DriveTrain.left,
                                        Outputs.DriveTrain.right,
                                        Sensors.gyro,
                                        Sensors.ultrasonic);
            lights = new Lights(Outputs.Lights.redLight,
				      Outputs.Lights.whiteLight,
				      Outputs.Lights.blueLight,
				      Outputs.Lights.greenLight,
                                      Outputs.Lights.cameraLights);
            deployment = new Deployment(Outputs.Deployment.rampMotor,
                                        Outputs.Deployment.servo);
            alignment = new Alignment(Outputs.Alignment.wingsMotor);
            elevator = new Elevator(Outputs.Elevator.motor1);
            wrist = new Wrist(Outputs.Wrist.motor);
        }
    }


    public static void init() {
        Sensors.gyro.setSensitivity(GYRO_V_PER_D_PER_S);
        
        Outputs.DriveTrain.init();
        Outputs.Elevator.init();
        Outputs.Wrist.init();
        Mechanisms.init();
    }
}
