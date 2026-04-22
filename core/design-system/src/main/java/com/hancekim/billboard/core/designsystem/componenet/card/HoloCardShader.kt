package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    private const val SHADER_SRC = """
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            float strength = iInteractive > 0.5 ? 1.0 : 0.45;

            // 회전 각도를 라디안으로
            float rad = radians(iAngle);

            // 빛 방향 벡터 (회전에 따라 변화)
            vec3 lightDir = normalize(vec3(
                sin(rad) * 0.6,
                0.8,
                cos(rad) * 0.6
            ));

            // 표면 노멀 (uv 기반 곡면 시뮬레이션)
            vec3 normal = normalize(vec3(
                (uv.x - 0.5) * 0.3,
                (uv.y - 0.5) * 0.3,
                1.0
            ));

            // 디퓨즈 라이팅
            float diffuse = max(dot(normal, lightDir), 0.0);

            // 스페큘러 반사 (Blinn-Phong)
            vec3 viewDir = vec3(0.0, 0.0, 1.0);
            vec3 halfDir = normalize(lightDir + viewDir);
            float spec = pow(max(dot(normal, halfDir), 0.0), 32.0);

            // 메탈릭 베이스 색상 (은색)
            vec3 metalBase = vec3(0.85, 0.87, 0.92);

            // 홀로그램 색상 (위치+각도에 따라 무지개빛)
            float holoPhase = (uv.x + uv.y) * 2.0 + iAngle / 60.0;
            vec3 holoColor = 0.5 + 0.5 * cos(6.28318 * (holoPhase + vec3(0.0, 0.33, 0.67)));

            // 합성: 메탈릭 베이스 + 빛 반사 + 홀로그램
            vec3 color = metalBase * (0.4 + 0.6 * diffuse);
            color += vec3(spec) * 0.7 * strength;
            color = mix(color, holoColor, 0.25 * strength);

            // 프레넬 효과 (가장자리 밝게)
            float fresnel = pow(1.0 - max(dot(normal, viewDir), 0.0), 3.0);
            color += vec3(fresnel * 0.15 * strength);

            float alpha = (0.35 + spec * 0.4 + fresnel * 0.1) * strength;

            return half4(half3(color), half(alpha));
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
