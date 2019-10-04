package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.team190.cntrl.operator.ErrorTracker;
import edu.wpi.team190.util.PIDJaguar;
import edu.wpi.team190.util.Util;

/**
 *
 * @author Derrick
 */
public class Wrist {
    public static final String WRIST_MECHANISM_NAME = "Wrist";

    public static final double UP_LIMIT = 0.024;   // was .024
    public static final double DOWN_LIMIT = UP_LIMIT + 0.92;   // .844

    public static final double DOWN_POSITION = DOWN_LIMIT - 0.010;
    public static final double JOG_AMOUNT = 0.01;
    public static final double UP_POSITION = UP_LIMIT + 0.150;
    public static final double SCORING_POSITION = UP_LIMIT + 0.41;
    
    public static final double kP = 2.3;
    public static final double kI = 0;
    public static final double kD = 0;

    public final PIDJaguar m_wristMotor;
    private double m_setpoint;
    private boolean m_JaguarIsInitialized = false;

    public Wrist(PIDJaguar wristMotor){
        m_wristMotor = wristMotor;
        try {
            m_wristMotor.setPositionReference(CANJaguar.PositionReference.kPotentiometer);
            m_wristMotor.changeControlMode(CANJaguar.ControlMode.kPosition);
            m_wristMotor.configPotentiometerTurns(1);
            m_wristMotor.setPID(kP, kI, kD);
            m_wristMotor.enableControl();
            m_JaguarIsInitialized = true;
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, WRIST_MECHANISM_NAME);
        } catch (NullPointerException ex) {
        }

        m_setpoint = Util.constrainToRange(0.0, UP_LIMIT, DOWN_LIMIT);
    }

    public void stowed() {
        if (!m_JaguarIsInitialized)
            return;
        m_setpoint = UP_POSITION;
        setJaguarSetpoint(UP_POSITION);
    }

    public void tiltForScoring() {
        if (!m_JaguarIsInitialized)
            return;
        m_setpoint = SCORING_POSITION;
        setJaguarSetpoint(SCORING_POSITION);
    }

    public void down() {
        if (!m_JaguarIsInitialized)
            return;
        m_setpoint = DOWN_POSITION;
        setJaguarSetpoint(DOWN_POSITION);
    }

    public void jogUp() {
        if (!m_JaguarIsInitialized)
            return;
        m_setpoint = Util.constrainToRange(m_setpoint + JOG_AMOUNT, UP_LIMIT, DOWN_LIMIT);
        setJaguarSetpoint(m_setpoint);
    }

    public void jogDown() {
        if (!m_JaguarIsInitialized)
            return;
        m_setpoint = Util.constrainToRange(m_setpoint - JOG_AMOUNT, UP_LIMIT, DOWN_LIMIT);
        setJaguarSetpoint(m_setpoint);
    }

    private void setJaguarSetpoint(double sp) {
        Util.constrainToRange(sp, UP_LIMIT, DOWN_LIMIT);
        if (!m_JaguarIsInitialized)
            return;
        try {
            m_wristMotor.setX(sp);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, WRIST_MECHANISM_NAME);
        }
    }

    public double getCurrentPosition() {
        try {
            return m_wristMotor.getPosition();
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, WRIST_MECHANISM_NAME);
        }
        return 0;
    }

    public boolean isPositionWithinTolerance(double tolerance) {
        return Math.abs(getCurrentPosition() - m_setpoint) <= tolerance;
    }
}
