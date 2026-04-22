package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    private const val SHADER_SRC = """
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            float str = iInteractive > 0.5 ? 1.0 : 0.4;
            float rad = radians(iAngle);

            // 표면 노멀 (살짝 곡면)
            vec3 N = normalize(vec3((uv - 0.5) * 0.4, 1.0));
            // 빛 방향 (회전에 따라 이동)
            vec3 L = normalize(vec3(sin(rad) * 0.8, cos(rad * 0.7) * 0.5, 1.0));
            // 시선 방향
            vec3 V = vec3(0.0, 0.0, 1.0);
            // 반사 벡터
            vec3 R = reflect(-L, N);

            // 스페큘러 하이라이트 (좁고 강한 반사)
            float spec = pow(max(dot(R, V), 0.0), 64.0) * str;

            // 프레넬 (가장자리 빛남)
            float fresnel = pow(1.0 - max(dot(N, V), 0.0), 4.0) * 0.3 * str;

            // 홀로그램 무지개 (매우 은은하게)
            float holoPhase = dot(uv, vec2(1.5, 1.0)) + iAngle / 90.0;
            vec3 rainbow = 0.5 + 0.5 * cos(6.28318 * (holoPhase + vec3(0.0, 0.33, 0.67)));

            // 합성: 대부분 투명, 하이라이트만 밝게
            half3 color = half3(spec * 0.9)                  // 흰색 스페큘러
                        + half3(rainbow) * half(0.08 * str)   // 은은한 무지개
                        + half3(fresnel);                      // 가장자리 빛

            // 알파: 스페큘러 + 프레넬 기반 (나머지는 거의 투명)
            half alpha = half(spec * 0.7 + fresnel + 0.03 * str);

            return half4(color, alpha);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
