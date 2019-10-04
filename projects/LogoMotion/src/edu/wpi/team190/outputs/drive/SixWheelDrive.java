package edu.wpi.team190.outputs.drive;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SmartDashboard;
import edu.wpi.first.wpilibj.SpeedController;

/**
 *
 * @author aaron
 */
public class SixWheelDrive {
    // Constants
    private static final double KHeadingProportional = 0.080;
    private static final double KHeadingIntegral = 0.00001; // 0.00618;
    private static final double KHeadingDifferential = 0.050; //0.0315;


    private static final double KDistanceProportional = 0.030;
    private static final double KDistanceIntegral = 0.00001;
    private static final double KDistanceDifferential = 0.080;

    public static class Modes {
        public static final DriveMode NORMAL = new NormalMode();
        public static final DriveMode DISTANCE_CONTROL = new DistanceControlMode();
        public static final DriveMode HEADING_CONTROL = new HeadingControlMode();
        public static final DriveMode DISTANCE_HEADING_CONTROL = new DistanceHeadingControlMode();
        public static final DriveMode SCORE_MODE = new ScoreMode();
    }

    // Members (intentionally package-level scoped such that DriveModes can
    // access things!)
    final RobotDrive m_driveline;
    final PIDSource m_headingSrc;
    PIDController m_headingController;
    PIDController m_distanceController;
    DriveMode m_mode = Modes.NORMAL;
    DriveMode m_previousMode = m_mode;
    double m_requestedDriveMagnitude = 0;
    final PIDSource m_distanceSource;
    PIDOutput m_headingControllerOutput = new PIDOutput() {

        public void pidWrite(double correction) {
            m_driveline.arcadeDrive(m_requestedDriveMagnitude, -correction);
        }
    };

    PIDOutput m_distanceControllerOutput = new PIDOutput() {

        public void pidWrite(double output) {
            m_driveline.arcadeDrive(-output, 0);
        }
    };

    class HeadingAndDistanceCombiner {
        private double m_prevHeading = 0;
        private double m_prevMagnitude = 0;

        public PIDOutput headingOuput = new PIDOutput() {

            public void pidWrite(double currentHeading) {
                m_driveline.arcadeDrive(m_prevMagnitude, -currentHeading);
                SmartDashboard.log(currentHeading, "Heading PID output");
                m_prevHeading = -currentHeading;
            }
        };

        public PIDOutput distanceOutput = new PIDOutput() {

            public void pidWrite(double currentMagnitude) {
                // Invert directions
                currentMagnitude *= -1;

                m_driveline.arcadeDrive(currentMagnitude, m_prevHeading);
                SmartDashboard.log(currentMagnitude, "Distance PID output");
                SmartDashboard.log(m_distanceController.getError(), "Distance Error");
                m_prevMagnitude = currentMagnitude;
            }
        };
    }
    final HeadingAndDistanceCombiner m_controlCombiner = new HeadingAndDistanceCombiner();

    public SixWheelDrive(SpeedController left,
                         SpeedController right,
                         PIDSource headingSrc,
                         PIDSource distanceSource) {
        m_driveline = new RobotDrive(left, right);

        m_distanceSource = distanceSource;
        m_headingSrc = headingSrc;

        m_headingController = createHeadingController(m_headingSrc, m_headingControllerOutput);
        m_distanceController = createDistanceController(m_distanceSource, m_distanceControllerOutput);
    }

    public SixWheelDrive(SpeedController leftFront, 
                         SpeedController leftRear, 
                         SpeedController rightFront, 
                         SpeedController rightRear, 
                         PIDSource headingSource,
                         PIDSource distanceSource) {
        m_driveline = new RobotDrive(leftFront, leftRear, rightFront, rightRear);

        m_distanceSource = distanceSource;
        m_headingSrc = headingSource;

        m_headingController = createHeadingController(m_headingSrc, m_headingControllerOutput);
        m_distanceController = createDistanceController(m_distanceSource, m_distanceControllerOutput);
    }

    PIDController createHeadingController(PIDSource src, PIDOutput out) {
        return new PIDController(KHeadingProportional, KHeadingIntegral, KHeadingDifferential, src, out);
    }

    PIDController createDistanceController(PIDSource src, PIDOutput out) {
        return new PIDController(KDistanceProportional, KDistanceIntegral, KDistanceDifferential, src, out);
    }

    public void setMode(DriveMode desiredMode) {
        if(m_mode == desiredMode || desiredMode == null)
            return;

        m_previousMode = m_mode;
        m_mode = desiredMode;

        // Run mode hooks
        m_previousMode.swapOutOn(this);
        m_mode.swapInOn(this);
    }

    public DriveMode getMode() {
        return m_mode;
    }

    public void drive(double leftJoy, double rightJoy) {
        m_mode.drive(this, leftJoy, rightJoy);
    }

    public void setHeading(double heading) {
        m_headingController.setSetpoint(heading);
    }

    public void setDistanceFromWallSetpoint(double setpt) {
        m_distanceController.setSetpoint(setpt);
    }

    public double getDistanceError() {
        return Math.abs(m_distanceController.getError());
    }
}
