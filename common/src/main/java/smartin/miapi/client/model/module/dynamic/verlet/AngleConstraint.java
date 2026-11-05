package smartin.miapi.client.model.module.dynamic.verlet;

import org.joml.Vector3f;

public class AngleConstraint {

    private static final float EPSILON = 1.0e-6f;

    public final int a;
    public final int b;
    public final int c;

    public float restAngle;
    public float compliance;

    private float lambda;

    public AngleConstraint(int a, int b, int c, float restAngle, float compliance) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.restAngle = restAngle;
        this.compliance = Math.max(0.0f, compliance);
    }

    public void reset() {
        lambda = 0.0f;
    }

    public void setRestAngle(float angle) {
        restAngle = angle;
    }

    public void setCompliance(float value) {
        compliance = Math.max(0.0f, value);
    }

    public static float getAngle(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f ab = new Vector3f(a).sub(b);
        Vector3f cb = new Vector3f(c).sub(b);

        float la = ab.length();
        float lc = cb.length();

        if (la < EPSILON || lc < EPSILON) {
            return 0.0f;
        }

        float cosine = ab.dot(cb) / (la * lc);

        cosine = Math.max(-1.0f, Math.min(1.0f, cosine));

        return (float) Math.acos(cosine);
    }

    public void solve(ChainSimulationState state, float dt, boolean rootFixed) {
        ChainNode na = state.nodes[a];
        ChainNode nb = state.nodes[b];
        ChainNode nc = state.nodes[c];

        Vector3f u = new Vector3f(na.pos).sub(nb.pos);
        Vector3f v = new Vector3f(nc.pos).sub(nb.pos);

        float lenU = u.length();
        float lenV = v.length();

        if (lenU < EPSILON || lenV < EPSILON) {
            return;
        }

        Vector3f uh = new Vector3f(u).div(lenU);
        Vector3f vh = new Vector3f(v).div(lenV);

        float cos = Math.max(-1.0f, Math.min(1.0f, uh.dot(vh)));

        float sin = (float) Math.sqrt(Math.max(0.0f, 1.0f - cos * cos));

        if (sin < EPSILON) {
            return;
        }

        float angle = (float) Math.acos(cos);
        float constraint = angle - restAngle;

        Vector3f gradA = new Vector3f(vh).fma(-cos, uh).div(-lenU * sin);
        Vector3f gradC = new Vector3f(uh).fma(-cos, vh).div(-lenV * sin);
        Vector3f gradB = new Vector3f(gradA).add(gradC).negate();

        float wa = (na.locked || (rootFixed && a == 0)) ? 0.0f : na.inverseMass;
        float wb = (nb.locked || (rootFixed && b == 0)) ? 0.0f : nb.inverseMass;
        float wc = (nc.locked || (rootFixed && c == 0)) ? 0.0f : nc.inverseMass;

        float alpha = compliance / (dt * dt);
        float denominator = wa * gradA.lengthSquared() + wb * gradB.lengthSquared() + wc * gradC.lengthSquared() + alpha;

        if (denominator < EPSILON) {
            return;
        }

        float deltaLambda = (-constraint - alpha * lambda) / denominator;

        lambda += deltaLambda;

        if (wa > 0.0f) {
            na.pos.fma(wa * deltaLambda, gradA);
        }

        if (wb > 0.0f) {
            nb.pos.fma(wb * deltaLambda, gradB);
        }

        if (wc > 0.0f) {
            nc.pos.fma(wc * deltaLambda, gradC);
        }
    }
}
