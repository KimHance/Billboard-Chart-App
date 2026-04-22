package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    private const val SHADER_SRC = """
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        // 홀로그램 레인보우 색상
        half3 rainbow(float t) {
            half3 a = half3(0.5);
            half3 b = half3(0.5);
            half3 c = half3(1.0);
            half3 d = half3(0.0, 0.33, 0.67);
            return a + b * cos(6.28318 * (c * half(t) + d));
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            float strength = iInteractive > 0.5 ? 1.0 : 0.5;

            // 1) 홀로그램 레인보우 — 위치 + 각도에 따라 색상 변화
            float holoPhase = (uv.x + uv.y) * 0.5 + iAngle / 360.0;
            half3 holo = rainbow(holoPhase);
            half holoAlpha = half(0.3 * strength);

            // 2) 메탈릭 스윕 (원형 그라데이션, 각도에 따라 회전)
            float sweepAngle = atan(uv.y - 0.5, uv.x - 0.5);
            float sweepT = (sweepAngle + 3.14159) / (2.0 * 3.14159);
            sweepT = fract(sweepT + iAngle / 360.0);
            float sweepBright = 0.5 + 0.5 * cos(sweepT * 6.28318 * 2.0);
            half3 sweepColor = half3(0.92, 0.95, 1.0) * half(sweepBright);
            half sweepAlpha = half(0.2 * strength);

            // 3) 브러시드 메탈 미세 줄무늬
            float brushed = fract(uv.y * iResolution.y / 2.5);
            brushed = step(0.75, brushed) * 0.06;

            // 합성
            half3 color = holo * holoAlpha
                        + sweepColor * sweepAlpha
                        + half3(half(brushed * 0.4));

            half alpha = min(holoAlpha + sweepAlpha + half(brushed), 0.75);

            return half4(color, alpha);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
