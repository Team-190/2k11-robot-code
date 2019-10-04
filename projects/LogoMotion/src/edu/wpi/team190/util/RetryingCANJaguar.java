package edu.wpi.team190.util;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.team190.cntrl.operator.ErrorTracker;

/**
 *
 * @author paul
 */
public class RetryingCANJaguar extends CANJaguar implements PIDJaguar {

    private static final String MECHANISM_NAME = "CAN Driver";
    static final int SANITY_CHECK_PERIOD_MS = 750;
    final int s_retries;
    ControlMode m_ctrlMode = ControlMode.kVoltage;
    PositionReference m_posRef = PositionReference.kNone;
    int m_turns;
    double m_p = 0;
    double m_i = 0;
    double m_d = 0;
    boolean m_controlEnable = false;
    double m_x = 0;
    boolean m_valueSet = false;
    Thread m_sanityTask = new SanityTask();

    class SanityTask extends Thread {

        public void run() {
            while (true) {
                RetryingCANJaguar j = RetryingCANJaguar.this;
                try {
                    boolean powerCycle = j.getPowerCycled();

                    if (m_valueSet && (powerCycle || !j.getControlMode().equals(j.m_ctrlMode))) {
                        if (powerCycle) {
                            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.JAG_PWR_CYCLE, MECHANISM_NAME);
                        } else {
                            ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.JAG_MISCONFIG, MECHANISM_NAME);
                        }

                        j.changeControlMode(j.m_ctrlMode);
                        j.configPotentiometerTurns(j.m_turns);
                        j.setPositionReference(j.m_posRef);
                        j.setPID(j.m_p, j.m_i, j.m_d);

                        if (j.m_controlEnable) {
                            j.enableControl();
                        } else {
                            j.disableControl();
                        }
                    }

                    j.setX(j.m_x);
                } catch (CANTimeoutException ex) {
                    // Ignore.
                    ex.printStackTrace();
                    ErrorTracker.getInstance().errorOccurred(ErrorTracker.ErrorTypes.CAN_TIMEOUT, MECHANISM_NAME);
                }
                edu.wpi.first.wpilibj.Timer.delay(0.750);
            }
        }
    }

    public RetryingCANJaguar(int deviceNumber, int retries) throws CANTimeoutException {
        super(deviceNumber);
        s_retries = retries;

        // Clear power cycled flag
        getPowerCycled();

        m_sanityTask.start();
    }

    public RetryingCANJaguar(int deviceNumber, ControlMode controlMode, int retries) throws CANTimeoutException {
        super(deviceNumber, controlMode);
        s_retries = retries;
        m_ctrlMode = controlMode;

        // Clear power cycled flag
        getPowerCycled();

        m_sanityTask.start();
    }

    public void changeControlMode(ControlMode controlMode) throws CANTimeoutException {
        m_ctrlMode = controlMode;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.changeControlMode(controlMode);
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public ControlMode getControlMode() throws CANTimeoutException {
        int retries = 0;
        while (true) {
            try {
                return super.getControlMode();
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void setPositionReference(PositionReference ref) throws CANTimeoutException {
        m_posRef = ref;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.setPositionReference(ref);
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void configPotentiometerTurns(int turns) throws CANTimeoutException {
        m_turns = turns;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.configPotentiometerTurns(turns);
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void setPID(double p, double i, double d) throws CANTimeoutException {
        m_p = p;
        m_i = i;
        m_d = d;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.setPID(p, i, d);
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void enableControl() throws CANTimeoutException {
        m_controlEnable = true;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.enableControl();
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void disableControl() throws CANTimeoutException {
        m_controlEnable = false;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.disableControl();
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public void setX(double x) throws CANTimeoutException {
        m_x = x;
        m_valueSet = true;

        int retries = 0;
        while (true) {
            try {
                super.setX(x);
                return;
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public double getPosition() throws CANTimeoutException {
        int retries = 0;
        while (true) {
            try {
                return super.getPosition();
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }

    public boolean getPowerCycled() throws CANTimeoutException {
        int retries = 0;
        while (true) {
            try {
                return super.getPowerCycled();
            } catch (CANTimeoutException e) {
                if (++retries > s_retries) {
                    throw e;
                }
            }
        }
    }
}
