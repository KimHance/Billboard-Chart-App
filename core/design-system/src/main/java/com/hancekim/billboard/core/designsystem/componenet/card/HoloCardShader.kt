package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    private const val SHADER_SRC = """
        uniform shader inputShader;
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        half4 main(float2 fragCoord) {
            half4 img = inputShader.eval(fragCoord);
            if (img.a < 0.01) return img;

            float2 uv = fragCoord / iResolution;
            float str = iInteractive > 0.5 ? 1.0 : 0.4;
            float rad = radians(iAngle);

            // ── 1. 메탈릭 베이스: 채도 낮추고 은색 틴트 ──
            float gray = dot(vec3(img.rgb), vec3(0.299, 0.587, 0.114));
            vec3 silver = vec3(gray * 0.88, gray * 0.91, gray * 0.97);
            vec3 metalBase = mix(vec3(img.rgb), silver, 0.3 * str);

            // ── 2. 대비 부스트 (금속 느낌) ──
            metalBase = (metalBase - 0.5) * (1.0 + 0.4 * str) + 0.5;

            // ── 3. 라이팅 계산 ──
            vec3 N = normalize(vec3((uv - 0.5) * 0.3, 1.0));
            vec3 L = normalize(vec3(sin(rad) * 0.7, cos(rad * 0.6) * 0.4, 1.0));
            vec3 V = vec3(0.0, 0.0, 1.0);
            vec3 R = reflect(-L, N);

            // 디퓨즈: 밝기 변화
            float diffuse = max(dot(N, L), 0.0);
            metalBase *= (0.7 + 0.3 * diffuse);

            // ── 4. 스페큘러 하이라이트 ──
            float spec = pow(max(dot(R, V), 0.0), 48.0) * 0.5 * str;

            // ── 5. 프레넬: 가장자리 밝게 ──
            float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0) * 0.2 * str;

            // ── 6. 비네팅: 가장자리 어둡게 (금속 테두리) ──
            float vignette = 1.0 - length(uv - 0.5) * 0.5;
            metalBase *= vignette;

            // ── 합성 ──
            half3 result = half3(metalBase);
            result += half3(spec);
            result += half3(fresnel * 0.7, fresnel * 0.75, fresnel);
            result = clamp(result, 0.0, 1.0);

            return half4(result, img.a);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
