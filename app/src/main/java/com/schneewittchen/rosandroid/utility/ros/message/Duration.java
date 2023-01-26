package com.schneewittchen.rosandroid.utility.ros.message;


/**
 * ROS Duration representation. Time and Duration are primitive types in ROS.
 * ROS represents each as two 32-bit integers: seconds and nanoseconds since
 * epoch.
 *
 * http://www.ros.org/wiki/msg
 *
 * @author Jason Wolfe
 * @author kwc@willowgarage.com (Ken Conley)
 *
 */
public class Duration implements Comparable<Duration> {

    public static final Duration MAX_VALUE = new Duration(Integer.MAX_VALUE, 999999999);

    public int secs;
    public int nsecs;

    public Duration() {
    }

    public Duration(int secs, int nsecs) {
        this.secs = secs;
        this.nsecs = nsecs;
        normalize();
    }

    public Duration(double secs) {
        this.secs = (int) secs;
        this.nsecs = (int) ((secs - this.secs) * 1000000000);
        normalize();
    }

    public Duration(Duration t) {
        this.secs = t.secs;
        this.nsecs = t.nsecs;
    }

    public Duration add(Duration d) {
        return new Duration(secs + d.secs, nsecs + d.nsecs);
    }

    public Duration subtract(Duration d) {
        return new Duration(secs - d.secs, nsecs - d.nsecs);
    }

    public static Duration fromMillis(long durationInMillis) {
        int secs = (int) (durationInMillis / 1000);
        int nsecs = (int) (durationInMillis % 1000) * 1000000;
        return new Duration(secs, nsecs);
    }

    public static Duration fromNano(long durationInNs) {
        int secs = (int) (durationInNs / 1000000000);
        int nsecs = (int) (durationInNs % 1000000000);
        return new Duration(secs, nsecs);
    }

    public void normalize() {
        while (nsecs < 0) {
            nsecs += 1000000000;
            secs -= 1;
        }
        while (nsecs >= 1000000000) {
            nsecs -= 1000000000;
            secs += 1;
        }
    }

    public long totalNsecs() {
        return ((long) secs) * 1000000000 + nsecs;
    }

    public boolean isZero() {
        return totalNsecs() == 0;
    }

    public boolean isPositive() {
        return totalNsecs() > 0;
    }

    public boolean isNegative() {
        return totalNsecs() < 0;
    }

    @Override
    public String toString() {
        return secs + ":" + nsecs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + nsecs;
        result = prime * result + secs;
        return result;
    }

    @Override
    /**
     * Check for equality between Time objects.
     * equals() does not normalize Time representations, so fields must match exactly.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Duration other = (Duration) obj;
        if (nsecs != other.nsecs)
            return false;
        if (secs != other.secs)
            return false;
        return true;
    }

    @Override
    public int compareTo(Duration d) {
        if ((secs > d.secs) || ((secs == d.secs) && nsecs > d.nsecs)) {
            return 1;
        }
        if ((secs == d.secs) && (nsecs == d.nsecs)) {
            return 0;
        }
        return -1;
    }

}