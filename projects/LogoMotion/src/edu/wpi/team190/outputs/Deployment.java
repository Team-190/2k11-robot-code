package edu.wpi.team190.outputs;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.SpeedController;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Derrick
 */
public class Deployment {

    private static class State {
    };

    private static class States {

        private static final State IDLE = new State();
        private static final State DEPLOY_UP = new State();
        private static final State DEPLOY_DOWN = new State();
        private static final State DEPLOY_FIRE = new State();
    }

    class StopTask extends TimerTask {

        public void run() {
            deploymentSystemStop();
        }
    }

    class DeployFireStopTask extends TimerTask {

        public void run() {
            deploymentSystemStop();
            m_state = States.DEPLOY_DOWN;
        }
    }
    private final double UP_PWR = -0.5;
    private final double DOWN_PWR = 1 / 3.0;
    private final int SERVO_ANGLE_LOCKED = 90;
    private final int SERVO_ANGLE_FIRED = 120;
    private final int RUN_TIME_MS = 2000;
    private final int FIRE_RUN_TIME_MS = 500;

    private boolean m_minibotLaunchAttempted = false;
    private boolean m_minibotArmed = false;
    private State m_state = States.IDLE;
    private final SpeedController m_deploymentMotor;
    private final Servo m_minibotServo;
    private Timer m_timer = new Timer();
    private boolean m_isFired = false;
    private Runnable m_fireHandler;

    public Deployment(SpeedController rampMotor,
            Servo minibotServo) {
        m_deploymentMotor = rampMotor;
        m_minibotServo = minibotServo;
        m_minibotServo.setAngle(SERVO_ANGLE_LOCKED);
    }

    public void lockMinibot() {
        m_minibotServo.setAngle(SERVO_ANGLE_LOCKED);
        m_minibotArmed = false;
        m_minibotLaunchAttempted = false;
        m_isFired = false;
    }

    /**
     * The deployment system should run for either a set amount of time(deploymentRunTime)
     * or until a limit switch is pressed
     */
    public void deploymentSystemUp() {
        if (m_state == States.DEPLOY_FIRE || m_state == States.DEPLOY_UP) {
            return;
        }

        m_deploymentMotor.set(UP_PWR);
        m_timer.schedule(new StopTask(), RUN_TIME_MS);

        m_state = States.DEPLOY_UP;
    }

    public void deploymentSystemDown() {
        if (m_state == States.DEPLOY_FIRE || m_state == States.DEPLOY_DOWN) {
            return;
        }

        m_deploymentMotor.set(DOWN_PWR);
        m_timer.schedule(new StopTask(), RUN_TIME_MS);

        m_state = States.DEPLOY_DOWN;
    }

    public void armMinibot() {
        m_minibotArmed = true;
        launchIfReady();
    }

    public void disarmMinibot() {
        m_minibotArmed = false;
    }

    public void minibotLaunch() {
        m_minibotLaunchAttempted = true;
        launchIfReady();
    }

    private void launchIfReady() {
        if (m_minibotArmed && m_minibotLaunchAttempted) {
            deploymentSystemFire();
        }
    }

    public void overrideArmMinibotLaunch() {
        deploymentSystemFire();
    }

    public boolean isArmed() {
        return m_minibotArmed;
    }

    public void deploymentSystemStop() {
        m_deploymentMotor.set(0);
    }

    private void deploymentSystemFire() {
        if (m_isFired) {
            return;
        }

        m_state = States.DEPLOY_FIRE;
        m_deploymentMotor.set(DOWN_PWR / 2);

        m_minibotServo.setAngle(SERVO_ANGLE_FIRED);
        m_timer.schedule(new DeployFireStopTask(), FIRE_RUN_TIME_MS);

        m_isFired = true;

        if(m_fireHandler != null)
            m_fireHandler.run();
    }

    public void setFireHandler(Runnable fireHandler) {
        m_fireHandler = fireHandler;
    }
}
