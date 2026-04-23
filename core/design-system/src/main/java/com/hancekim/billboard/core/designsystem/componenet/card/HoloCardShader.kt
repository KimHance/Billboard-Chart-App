package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    // 메탈릭 홀로그램 셰이더: conic sheen + brushed streaks + specular strip
    private const val SHADER_SRC = """
        uniform shader inputShader;
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        // overlay(base, white) — CSS overlay blend with pure white
        half overlayWhite(half base) {
            return base < 0.5 ? 2.0 * base : 1.0;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            half4 img = inputShader.eval(fragCoord);

            float norm = mod(iAngle, 360.0);

            // === L1: Conic sheen (메탈릭 쉬인) ===
            // CSS: conic-gradient(from angle) + mix-blend-mode: screen + opacity
            float2 center = float2(0.5, 0.5);
            float2 d = uv - center;
            float theta = degrees(atan(d.y, d.x)) + 180.0;
            float sa = mod(theta - norm + 360.0, 360.0);

            float sheenAlpha = 0.0;
            half3 sheenColor = half3(1.0);

            if (sa < 40.0) {
                sheenAlpha = mix(0.0, 0.45, sa / 40.0);
                sheenColor = half3(0.863, 0.902, 0.941);
            } else if (sa < 80.0) {
                float t = (sa - 40.0) / 40.0;
                sheenAlpha = mix(0.45, 0.55, t);
                sheenColor = mix(half3(0.863,0.902,0.941), half3(0.706,0.784,0.863), half(t));
            } else if (sa < 130.0) {
                float t = (sa - 80.0) / 50.0;
                sheenAlpha = mix(0.55, 0.85, t);
                sheenColor = mix(half3(0.706,0.784,0.863), half3(0.941,0.957,0.980), half(t));
            } else if (sa < 180.0) {
                float t = (sa - 130.0) / 50.0;
                sheenAlpha = mix(0.85, 0.45, t);
                sheenColor = mix(half3(0.941,0.957,0.980), half3(0.549,0.667,0.784), half(t));
            } else if (sa < 230.0) {
                float t = (sa - 180.0) / 50.0;
                sheenAlpha = mix(0.45, 0.55, t);
                sheenColor = mix(half3(0.549,0.667,0.784), half3(0.824,0.863,0.922), half(t));
            } else if (sa < 280.0) {
                float t = (sa - 230.0) / 50.0;
                sheenAlpha = mix(0.55, 0.0, t);
                sheenColor = half3(1.0);
            }

            // Screen blend 후 알파로 원본과 보간
            float sheenOp = iInteractive > 0.5 ? 0.55 : 0.3;
            float effectiveAlpha = sheenAlpha * sheenOp;
            // screen(base, color) = base + color - base*color = base + color*(1-base)
            half3 screenResult = img.rgb + sheenColor * (half3(1.0) - img.rgb);
            half3 result = mix(img.rgb, screenResult, half(effectiveAlpha));

            // === L2: Brushed streaks (촘촘한 세로 홈 — 1px on, 2px off) ===
            // CSS: overlay blend, white lines, alpha=0.06, layer opacity=0.8
            float streakOn = 1.0 - step(1.0, mod(fragCoord.x, 3.0));
            float streakAlpha = streakOn * 0.06 * 0.8;
            // overlay(base, white=1.0) 결과를 구한 뒤 alpha로 보간
            half3 streakBlend = half3(
                overlayWhite(result.r),
                overlayWhite(result.g),
                overlayWhite(result.b)
            );
            result = mix(result, streakBlend, half(streakAlpha));

            // === L3: Specular highlight (부드러운 글로우) ===
            // 대각선 밴드 대신 conic sheen 피크 방향에 부드러운 하이라이트
            float2 lightDir = float2(cos(radians(norm)), sin(radians(norm)));
            float highlight = dot(normalize(d + float2(0.001)), lightDir);
            highlight = pow(max(highlight, 0.0), 4.0);
            float specOp = iInteractive > 0.5 ? 0.18 : 0.08;
            result = result + half3(half(highlight * specOp));

            return half4(result, img.a);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
