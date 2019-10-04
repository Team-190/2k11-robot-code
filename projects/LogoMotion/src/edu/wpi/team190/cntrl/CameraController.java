package edu.wpi.team190.cntrl;

import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.Comparer;
import com.sun.squawk.util.MathUtils;
import edu.wpi.first.wpilibj.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import edu.wpi.team190.util.IValueReceiver;
import edu.wpi.team190.util.IValueSource;

/**
 *
 * @author Greg
 */
public class CameraController extends Thread {
    public static final double CAMERA_HORIZ_VIEW_DEG = 54;
    public static final double CAMERA_OFFSET_TO_RIGHT_IN = 0;
    public static final double CAMERA_TILT_TO_LEFT_DEG = 0;
    public static final double TARGET_OFFSET_FROM_WALL_IN = 16.5;

    // Static vars
    private static boolean s_isEnabled = true;
    private static CameraController instance = null;
    private static IValueSource s_headingSrc;
    private static IValueSource s_distanceSrc;
    private static IValueReceiver s_requestedHeadingRecv;
    private static AxisCamera s_cam;
    private static ColorImage s_curImage;
    private static ParticleAnalysisReport s_particles[];
    private static double s_requestedHeading = 0;

    private static class CameraComparer implements Comparer {
        public static final double AREA_GAIN = 5;
        public static final double DISTANCE_FROM_CENTER_GAIN = -10;
        public static final double SQUARENESS_GAIN = 2;

        private double m_degreeCorrection = 0;
        private final double m_cameraCenterAngleFromRobotHeading;
        private final double m_cameraOffsetFromCenter;
        private final double m_targetOffsetFromWall;
        private final double m_cameraHorizontalView;

        /**
         * Creates a new CameraComparer
         * @param cameraMountAngle The angle, in degrees, at which the camera is off from
         *  perpendicular to the target (positive to the left).
         * @param cameraOffsetFromCenter The distance, in inches, from the center line
         *  of the robot, at which the camera lens is mounted.
         * @param targetOffsetFromWall The distance, in inches, away from the wall
         *  at which the target is mounted
         * @param cameraHorizontalView The view, in degrees, which the camera
         *  sees along its horizontal axis.
         */
        public CameraComparer(double cameraMountAngle,
                              double cameraOffsetFromCenter,
                              double targetOffsetFromWall,
                              double cameraHorizontalView) {
            m_cameraCenterAngleFromRobotHeading = cameraMountAngle;
            m_cameraOffsetFromCenter = cameraOffsetFromCenter;
            m_targetOffsetFromWall = targetOffsetFromWall;
            m_cameraHorizontalView = cameraHorizontalView;
        }

        public int compare(Object o1, Object o2) {
            return this.compare((ParticleAnalysisReport) o1, (ParticleAnalysisReport) o2);
        }

        public int compare(ParticleAnalysisReport p1, ParticleAnalysisReport p2) {
            int p1Ratio = p1.boundingRectWidth / p1.boundingRectHeight;
            int p2Ratio = p2.boundingRectWidth / p2.boundingRectHeight;

            int centerOfImage = (p1.imageWidth / 2) +
                                (int) (m_degreeCorrection * (p1.imageWidth / m_cameraHorizontalView));

            int p1DistanceFromCenter = Math.abs(centerOfImage - p1.center_mass_x);
            int p2DistanceFromCenter = Math.abs(centerOfImage - p2.center_mass_x);

            double p1Score = p1DistanceFromCenter * DISTANCE_FROM_CENTER_GAIN +
                            p1Ratio * SQUARENESS_GAIN +
                            p1.particleArea * AREA_GAIN;
            double p2Score = p2DistanceFromCenter * DISTANCE_FROM_CENTER_GAIN +
                            p2Ratio * SQUARENESS_GAIN +
                            p2.particleArea * AREA_GAIN;

            // Sorts decensing (highest score comes first)
            // Lower scores end up at the end of the array when sorting in ascending order
            return (int) (p2Score - p1Score);
        }

        /**
         * Informs the comparer of the current distance to the targets, such that
         * it may adjust its definition of the center of an image appropriately.
         * @param dist The distance, in inches, to the target.
         */
        public void setDistanceFromTarget(double dist) {
            double distanceFromTarget = dist - m_targetOffsetFromWall;
            double angleToTargetFromRobotHeading;

            if(distanceFromTarget != 0) {
                angleToTargetFromRobotHeading =
                    MathUtils.atan(m_cameraOffsetFromCenter / distanceFromTarget) * (180 / Math.PI);
            } else {
                // atan --> 45 degrees as x --> infinity
                angleToTargetFromRobotHeading = 45 * (m_cameraOffsetFromCenter >= 0? 1:-1);
            }

            m_degreeCorrection = m_cameraCenterAngleFromRobotHeading - angleToTargetFromRobotHeading;
            SmartDashboard.log(m_degreeCorrection, "Camera Degree Correction");
        }

        public double getDegreeCorrection() {
            return m_degreeCorrection;
        }
    }

    private static CameraComparer particleComparer = new CameraComparer(CAMERA_TILT_TO_LEFT_DEG,
                                                                        CAMERA_OFFSET_TO_RIGHT_IN,
                                                                        TARGET_OFFSET_FROM_WALL_IN,
                                                                        CAMERA_HORIZ_VIEW_DEG);

    private CameraController() {
        s_cam = AxisCamera.getInstance();
    }

    public void run() {
        double degreeAtCamImage;
        while (s_isEnabled) {
            if (s_cam.freshImage()) {
                try {
                    degreeAtCamImage = s_headingSrc.getValue();
                    particleComparer.setDistanceFromTarget(s_distanceSrc.getValue());

                    s_curImage = s_cam.getImage();
                    //s_curImage.write("/cameraImage.jpg");

                    // Image processing
                    BinaryImage binImage = s_curImage.thresholdHSL(238, 255, 102, 255, 56, 255);
                    s_particles = binImage.getOrderedParticleAnalysisReports();
                    s_curImage.free();
                    binImage.free();

                    //SmartDashboard.log(s_particles.length, "Particles");
                    System.out.println("Particles: " + s_particles.length);

                    if (s_particles.length > 0) {
                        Arrays.sort(s_particles, particleComparer);
                        ParticleAnalysisReport circ = s_particles[0];

                        double degreesOff = -((54.0 / 640.0) * ((circ.imageWidth / 2.0) - circ.center_mass_x));
                        degreesOff -= particleComparer.getDegreeCorrection();

                        SmartDashboard.log(degreesOff, "Degrees Off");
                        System.out.print("Degrees Off: " + degreesOff);

                        s_requestedHeading = degreeAtCamImage + degreesOff;
                        s_requestedHeadingRecv.putValue(s_requestedHeading);
                    }

                    Timer.delay(0.3);
                } catch (AxisCameraException ex) {
                    ex.printStackTrace();
                } catch (NIVisionException ex) {
                    ex.printStackTrace();
                }
            }
            Timer.delay(0.01);
        }
    }

    public static void enable() {
        if (instance == null) {
            if(s_headingSrc == null || s_distanceSrc == null || s_requestedHeadingRecv == null)
                throw new RuntimeException("CameraController must be "
                        + "initialized with setup() before being enabled.");
            
            s_isEnabled = true;
            instance = new CameraController();
            instance.start();
        }
    }

    public static void disable() {
        s_isEnabled = false;

        while (instance != null && instance.isAlive()) {
            Timer.delay(0.01);
        }

        instance = null;
    }

    public static void setup(IValueSource heading, 
                             IValueSource distance,
                             IValueReceiver requestedHeadingReceiver) {
        s_headingSrc = heading;
        s_distanceSrc = distance;
        s_requestedHeadingRecv = requestedHeadingReceiver;
    }

    public static void setHeadingSource(IValueSource heading) {
        if(heading != null) {
            s_headingSrc = heading;
        } else {
            throw new NullPointerException("Heading source must not be null");
        }
    }

    public static void setDistanceSource(IValueSource dist) {
        if(dist != null) {
            s_distanceSrc = dist;
        } else {
            throw new NullPointerException("Distance source must not be null");
        }
    }

    public static void setRequestedHeadingReceiver(IValueReceiver headingReceiver) {
        if(headingReceiver != null) {
            s_requestedHeadingRecv = headingReceiver;
        } else {
            throw new NullPointerException("Heading receiver must not be null");
        }
    }

    public static double getRequestedHeading() {
        return s_requestedHeading;
    }
}
