package javax.media.jai;

import java.io.Serializable;

public final class PerspectiveTransform implements Serializable {

    private static final long serialVersionUID = -3525476925806247168L;

    public double m00;
    public double m01;
    public double m02;
    public double m10;
    public double m11;
    public double m12;
    public double m20;
    public double m21;
    public double m22;

    public double[] getMatrix(double[] matrix) {
        if (matrix == null) {
            matrix = new double[9];
        }
        matrix[0] = m00;
        matrix[1] = m01;
        matrix[2] = m02;
        matrix[3] = m10;
        matrix[4] = m11;
        matrix[5] = m12;
        matrix[6] = m20;
        matrix[7] = m21;
        matrix[8] = m22;
        return matrix;
    }
}
