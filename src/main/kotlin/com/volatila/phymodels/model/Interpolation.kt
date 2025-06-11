package com.volatila.phymodels.model

import javax.vecmath.Vector3f

enum class Interpolation {
    LINEAR,
    HERMITE,
    CATMULL_ROM,
    BEZIER,
    CUBIC_SPLINE,

    ;
    companion object {
        fun cubicSpline3d(keyframes: List<Vector3f>, times: Int): List<Vector3f> {
            if (keyframes.size < 2) return keyframes

            val n = keyframes.size
            val xs = FloatArray(n) { keyframes[it].x }
            val ys = FloatArray(n) { keyframes[it].y }
            val zs = FloatArray(n) { keyframes[it].z }

            val cx = cubicSpline1d(xs)
            val cy = cubicSpline1d(ys)
            val cz = cubicSpline1d(zs)

            val result = mutableListOf<Vector3f>()
            for (i in 0 until times) {
                val t = i.toFloat() / (times - 1)
                val pos = t * (n - 1)
                val seg = pos.toInt().coerceAtMost(n - 2)
                val localT = pos - seg

                val x = evalCubic(cx, seg, localT)
                val y = evalCubic(cy, seg, localT)
                val z = evalCubic(cz, seg, localT)
                result.add(Vector3f(x, y, z))
            }

            return result
        }

        // 构建三次样条系数（自然边界条件）
        private fun cubicSpline1d(values: FloatArray): Array<FloatArray> {
            val n = values.size
            val a = values.copyOf()
            val b = FloatArray(n)
            val d = FloatArray(n)
            val h = FloatArray(n - 1) { i -> 1f } // 均匀间距

            val alpha = FloatArray(n - 1)
            for (i in 1 until n - 1) {
                alpha[i] = 3f * (a[i + 1] - a[i]) - 3f * (a[i] - a[i - 1])
            }

            val c = FloatArray(n)
            val l = FloatArray(n)
            val mu = FloatArray(n)
            val z = FloatArray(n)

            l[0] = 1f
            mu[0] = 0f
            z[0] = 0f

            for (i in 1 until n - 1) {
                l[i] = 4f - mu[i - 1]
                mu[i] = 1f / l[i]
                z[i] = (alpha[i] - z[i - 1]) / l[i]
            }

            l[n - 1] = 1f
            z[n - 1] = 0f
            c[n - 1] = 0f

            for (j in n - 2 downTo 0) {
                c[j] = z[j] - mu[j] * c[j + 1]
                b[j] = (a[j + 1] - a[j]) - (c[j + 1] + 2f * c[j]) / 3f
                d[j] = (c[j + 1] - c[j]) / 3f
            }

            return Array(n - 1) { i -> floatArrayOf(a[i], b[i], c[i], d[i]) }
        }

        // 在某段区间内评估三次样条
        private fun evalCubic(coeffs: Array<FloatArray>, i: Int, t: Float): Float {
            val (a, b, c, d) = coeffs[i]
            return a + b * t + c * t * t + d * t * t * t
        }
    }
}