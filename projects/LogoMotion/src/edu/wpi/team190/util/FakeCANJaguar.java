package edu.wpi.team190.util;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.CANJaguar.ControlMode;
import edu.wpi.first.wpilibj.CANJaguar.PositionReference;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author Paul
 */
public class FakeCANJaguar implements PIDJaguar {

    private PIDController m_PID;
    private Jaguar m_jaguar;
    private AnalogChannel m_source;
    private PIDSource m_positionsSource;
    private int m_turns;
    private static final double MAX_VOLTAGE = 5;
    private ControlMode m_controlMode;

    public FakeCANJaguar(int channel, AnalogChannel source) throws CANTimeoutException {
        m_jaguar = new Jaguar(channel);
        m_source = source;
        m_positionsSource = new PIDSource() {

            public double pidGet() {
                return (m_source.getAverageVoltage() / MAX_VOLTAGE) * m_turns;
            }
        };
    }

    public ControlMode getControlMode() {
        return m_controlMode;
    }

    public void setPositionReference(PositionReference pos) {
        if(pos != PositionReference.kNone && pos != PositionReference.kPotentiometer) {
            throw new RuntimeException("Position Reference not supported.");
        }
    }

    public void configPotentiometerTurns(int turns) {
        m_turns = turns;
    }

    public void setPID(double p, double i, double d) {
        m_PID = new PIDController(p, i, d, m_positionsSource, m_jaguar);
        m_PID.setTolerance(1);

    }

    public void enableControl() {
        if (m_controlMode != ControlMode.kPosition) {
            throw new RuntimeException("This control mode is not supported.");
        }
        if (m_PID != null) {
            m_PID.enable();
        }
    }

    public void disableControl() {
        if (m_PID != null) {
            m_PID.disable();
        }
    }

    public void setX(double x) {
        if (m_controlMode == ControlMode.kPosition) {
            if (m_PID != null) {
                m_PID.setSetpoint(x);
            }
        } else if (m_controlMode == ControlMode.kVoltage) {
            m_jaguar.set(x);
        }
    }

    public double getPosition() {
        return m_positionsSource.pidGet();
    }

    public boolean getPowerCycled() {
        return false;
    }

    public void changeControlMode(ControlMode controlMode)
            throws CANTimeoutException {
        if (controlMode != ControlMode.kVoltage && controlMode != ControlMode.kPosition) {
            throw new RuntimeException("This control mode is not supported.");
        }
        m_controlMode = controlMode;
    }
}
