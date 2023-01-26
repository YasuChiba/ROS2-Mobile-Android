package com.schneewittchen.rosandroid.utility.ros;

import com.google.common.base.Preconditions;

import geometry_msgs.msg.Quaternion;
import geometry_msgs.msg.Transform;
import geometry_msgs.msg.Vector3;

public class GeometryUtils {
    public static Vector3 VectorZero() {
        Vector3 vectorZero = new Vector3();
        vectorZero.setX(0);vectorZero.setY(0);vectorZero.setZ(0);
        return vectorZero;
    }

    public static Vector3 VectorXAxis() {
        Vector3 v = new Vector3();
        v.setX(1);v.setY(0);v.setZ(0);
        return v;
    }

    public static Vector3 VectorYAxis() {
        Vector3 v = new Vector3();
        v.setX(0);v.setY(1);v.setZ(0);
        return v;
    }

    public static Vector3 VectorZAxis() {
        Vector3 v = new Vector3();
        v.setX(0);v.setY(0);v.setZ(1);
        return v;
    }

    public static Quaternion QuaternionIdentity() {
        Quaternion quaternionIdentity = new Quaternion();
        quaternionIdentity.setX(0);quaternionIdentity.setY(0);quaternionIdentity.setZ(0);quaternionIdentity.setW(1);
        return quaternionIdentity;
    }

    public static Transform TransformIdentity() {
        Transform transformIdentity = new Transform();
        transformIdentity.setTranslation(VectorZero());
        transformIdentity.setRotation(QuaternionIdentity());
        return transformIdentity;
    }

    public static double QuaternionGetMagnitudeSquared(Quaternion quaternion) {
        double x = quaternion.getX();
        double y = quaternion.getY();
        double z= quaternion.getZ();
        double w = quaternion.getW();

        return x * x + y * y + z * z + w * w;
    }

    public static double[] TransformToMatrix(Transform transform) {
        Quaternion rotationAndScale = transform.getRotation();
        Vector3 translation = transform.getTranslation();

        double x = rotationAndScale.getX();
        double y = rotationAndScale.getY();
        double z = rotationAndScale.getZ();
        double w = rotationAndScale.getW();
        double mm = QuaternionGetMagnitudeSquared(rotationAndScale);

        return new double[] { mm - 2 * y * y - 2 * z * z, 2 * x * y + 2 * z * w, 2 * x * z - 2 * y * w,
                0, 2 * x * y - 2 * z * w, mm - 2 * x * x - 2 * z * z, 2 * y * z + 2 * x * w, 0,
                2 * x * z + 2 * y * w, 2 * y * z - 2 * x * w, mm - 2 * x * x - 2 * y * y, 0,
                translation.getX(), translation.getY(), translation.getZ(), 1 };
    }

    public static Transform TransformZRotation(double angle) {

        Quaternion q = QuaternionFromAxisAngle(VectorZAxis(), angle);

        Transform returnVal = new Transform();
        returnVal.setTranslation(VectorZero());
        returnVal.setRotation(q);
        return returnVal;
    }

    public static Quaternion QuaternionFromAxisAngle(Vector3 axis, double angle) {
        Vector3 normalized = GeometryUtils.VectorNormalize(axis);
        double sin = Math.sin(angle / 2.0d);
        double cos = Math.cos(angle / 2.0d);

        Quaternion returnVal = new Quaternion();
        returnVal.setX(normalized.getX() * sin);
        returnVal.setY(normalized.getY() * sin);
        returnVal.setZ(normalized.getZ() * sin);
        returnVal.setW(cos);
        return returnVal;
    }

    public static Vector3 VectorNormalize(Vector3 vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();
        double magnitude = VectorGetMagnitude(vector);

        Vector3 returnVector = new Vector3();
        returnVector.setX(x / magnitude);
        returnVector.setY(y / magnitude);
        returnVector.setZ(z / magnitude);
        return returnVector;
    }

    public static double VectorGetMagnitude(Vector3 vector) {
        return Math.sqrt(GeometryUtils.VectorGetMagnitudeSquared(vector));
    }

    public static double VectorGetMagnitudeSquared(Vector3 vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        return x * x + y * y + z * z;
    }

    public static Transform TransformScale(Transform transform, double factor) {
        Transform r = new Transform();
        r.setTranslation(transform.getTranslation());
        r.setRotation(QuaternionScale(transform.getRotation(),Math.sqrt(factor)));
        return r;
    }

    public static Quaternion QuaternionScale(Quaternion quaternion, double factor) {
        double x = quaternion.getX();
        double y = quaternion.getY();
        double z = quaternion.getZ();
        double w = quaternion.getW();

        Quaternion r = new Quaternion();
        r.setX(x * factor);
        r.setY(y * factor);
        r.setZ(z * factor);
        r.setW(w * factor);
        return r;
    }

    public static Transform TransformInvert(Transform transform) {
        Quaternion inverseRotationAndScale = QuaternionInvert(transform.getRotation());

        Transform r = new Transform();
        r.setTranslation(QuaternionRotateAndScaleVector(inverseRotationAndScale, VectorInvert(transform.getTranslation())));
        r.setRotation(inverseRotationAndScale);
        return r;
    }

    public static Quaternion QuaternionInvert(Quaternion quaternion) {
        double mm = QuaternionGetMagnitudeSquared(quaternion);

        Preconditions.checkState(mm != 0);
        return QuaternionConjugate(QuaternionScale(quaternion, 1/mm));
    }

    public static Quaternion QuaternionConjugate(Quaternion quaternion) {
        Quaternion q = new Quaternion();
        q.setX(quaternion.getX() * -1);
        q.setY(quaternion.getY() * -1);
        q.setZ(quaternion.getZ() * -1);
        q.setW(quaternion.getW());
        return q;
    }

    public static Vector3 QuaternionRotateAndScaleVector(Quaternion quaternion, Vector3 vector) {

        Quaternion vectorQuaternion = new Quaternion();
        vectorQuaternion.setX(vector.getX());vectorQuaternion.setY(vector.getY());vectorQuaternion.setZ(vector.getZ());vectorQuaternion.setW(0);

        Quaternion rotatedQuaternion = QuaternionMultiply(quaternion, QuaternionMultiply(vectorQuaternion, QuaternionConjugate(quaternion)));

        Vector3 r = new Vector3();
        r.setX(rotatedQuaternion.getX());
        r.setY(rotatedQuaternion.getY());
        r.setZ(rotatedQuaternion.getZ());
        return r;
    }

    public static Quaternion QuaternionMultiply(Quaternion quaternion,Quaternion other) {
        double x = quaternion.getX();
        double y = quaternion.getY();
        double z = quaternion.getZ();
        double w = quaternion.getW();

        Quaternion r = new Quaternion();
        r.setX(w * other.getX() + x * other.getW() + y * other.getZ() - z * other.getY());
        r.setY(w * other.getY() + y * other.getW() + z * other.getX() - x * other.getZ());
        r.setZ(w * other.getZ() + z * other.getW() + x * other.getY() - y * other.getX());
        r.setW(w * other.getW() - x * other.getX() - y * other.getY() - z * other.getZ());
        return r;
    }

    public static Vector3 VectorInvert(Vector3 vector) {

        Vector3 r = new Vector3();
        r.setX(vector.getX() * -1);
        r.setY(vector.getY() * -1);
        r.setZ(vector.getZ() * -1);
        return r;
    }

}
