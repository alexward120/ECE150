package edu.ucsb.ece150.stepitup;

public class StepDetector {
    private static final double STEP_THRESHOLD = 25;
    private boolean mStepDetected = false; // Used to prevent multiple step counts for a single step
    public StepDetector() { }

    public boolean detectStep(float x, float y, float z) {
        double sample = Math.sqrt(x*x + y*y + z*z);
        // determine if a step occurred
        if (sample > STEP_THRESHOLD && !mStepDetected) {
            mStepDetected = true;
            return true;
        } else if (sample < STEP_THRESHOLD) {
            mStepDetected = false;
        }
        return false;
    }
}
