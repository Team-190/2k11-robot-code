package edu.wpi.team190.cntrl.operator;

import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Timer;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Tracks error occurrances and displays them on an appropriate output device.
 * @author pmalmsten
 */
public class ErrorTracker extends Thread {

    private static String NULL_MECHANISM = "No Name";
    private static ErrorTracker instance;
    private Hashtable m_globalCounts;
    private Hashtable m_mechanismCounts;
    private DriverStationLCD.Line m_currentLine;

    public static class ErrorTypes {
        public static final ErrorType CAN_TIMEOUT = new ErrorType("CAN Timeout");
        public static final ErrorType CAN_NOT_INITED = new ErrorType("CAN Not Init.ed");
        public static final ErrorType JAG_MISCONFIG = new ErrorType("Jag Misconf.");
        public static final ErrorType JAG_PWR_CYCLE = new ErrorType("Jag Pwr. Cycl.");
    }

    public static class ErrorType {
        public final String name;

        public ErrorType(String m_name) {
            this.name = m_name;
        }
    }

    public ErrorTracker() {
        m_globalCounts = new Hashtable();
        m_mechanismCounts = new Hashtable();
    }

    public void errorOccurred(ErrorType e) {
        errorOccurred(e, NULL_MECHANISM);
    }

    public synchronized void errorOccurred(ErrorType e, String m) {
        if (m_globalCounts.containsKey(e))  {
            // Increment global count
            Integer i = (Integer) m_globalCounts.get(e);
            i = new Integer(i.intValue() + 1);
            m_globalCounts.put(e, i);
        } else {
            // Init error count to 1
            // Only inserts keys when an error has occurred
            m_globalCounts.put(e, new Integer(1));
        }

        if (m_mechanismCounts.containsKey(m)) {
            // Increment mechanism count
            Integer i = (Integer) m_mechanismCounts.get(m);
            i = new Integer(i.intValue() + 1);
            m_mechanismCounts.put(m, i);
        } else {
            m_mechanismCounts.put(m, new Integer(1));
        }
    }

    public static ErrorTracker getInstance() {
        if (instance == null) {
            instance = new ErrorTracker();
            instance.start();
        }
        return instance;
    }

    private synchronized void displayGlobalCounts() {
        Enumeration e = m_globalCounts.keys();
        m_currentLine = DriverStationLCD.Line.kUser2;

        while (e.hasMoreElements()) {
            ErrorType et = (ErrorType) e.nextElement();
            Integer count = (Integer) m_globalCounts.get(et);

            if (et != null && count != null) {
                displayLine(et.name + "s: " + count);
            }
        }
    }

    private synchronized void displayMechanismCounts() {
        Enumeration e = m_mechanismCounts.keys();
        m_currentLine = DriverStationLCD.Line.kUser2;

        while (e.hasMoreElements()) {
            String mech = (String) e.nextElement();
            Integer count = (Integer) m_mechanismCounts.get(mech);

            if (mech != null && count != null) {
                displayLine(mech + ": " + count);
            }
        }
    }

    private void displayLine(String line) {
        DriverStationLCD.getInstance().println(m_currentLine, 1, line);

        if (m_currentLine == DriverStationLCD.Line.kUser2) {
            m_currentLine = DriverStationLCD.Line.kUser3;
        } else if (m_currentLine == DriverStationLCD.Line.kUser3) {
            m_currentLine = DriverStationLCD.Line.kUser4;
        } else if (m_currentLine == DriverStationLCD.Line.kUser4) {
            m_currentLine = DriverStationLCD.Line.kUser5;
        } else if (m_currentLine == DriverStationLCD.Line.kUser5) {
            m_currentLine = DriverStationLCD.Line.kUser6;
        } else if (m_currentLine == DriverStationLCD.Line.kUser6) {
            m_currentLine = DriverStationLCD.Line.kUser2;
            DriverStationLCD.getInstance().updateLCD();
            Timer.delay(2);
        }
    }

    public void clearDisplay() {
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser2, 1, "");
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser3, 1, "");
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser4, 1, "");
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser5, 1, "");
        DriverStationLCD.getInstance().println(DriverStationLCD.Line.kUser6, 1, "");
        DriverStationLCD.getInstance().updateLCD();
    }

    public void run() {
        while (true) {
            clearDisplay();
            displayGlobalCounts();
            DriverStationLCD.getInstance().updateLCD();
            Timer.delay(2);

            clearDisplay();
            displayMechanismCounts();
            DriverStationLCD.getInstance().updateLCD();
            Timer.delay(2);
        }
    }
}
