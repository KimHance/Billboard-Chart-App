package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    // inputShader: 앨범 아트 픽셀을 입력으로 받아서 메탈릭 효과 후처리
    private const val SHADER_SRC = """
        uniform shader inputShader;
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        half4 main(float2 fragCoord) {
            // 원본 이미지 샘플링
            half4 img = inputShader.eval(fragCoord);
            if (img.a < 0.01) return img;

            float2 uv = fragCoord / iResolution;
            float str = iInteractive > 0.5 ? 1.0 : 0.35;
            float rad = radians(iAngle);

            // 표면 노멀 (곡면 시뮬레이션)
            vec3 N = normalize(vec3((uv - 0.5) * 0.3, 1.0));
            // 빛 방향 (회전에 따라 이동)
            vec3 L = normalize(vec3(sin(rad) * 0.7, cos(rad * 0.6) * 0.4, 1.0));
            vec3 V = vec3(0.0, 0.0, 1.0);
            vec3 R = reflect(-L, N);

            // 스페큘러: 좁은 하이라이트
            float spec = pow(max(dot(R, V), 0.0), 48.0) * 0.6 * str;

            // 프레넬: 가장자리 광택
            float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0) * 0.15 * str;

            // 메탈릭 컬러 시프트: 원본 색상의 밝기를 조절
            float brightness = dot(vec3(img.rgb), vec3(0.299, 0.587, 0.114));
            float metalBoost = (0.5 + 0.5 * dot(N, L)) * str * 0.2;

            // 합성: 원본 이미지 + 메탈릭 보정 + 스페큘러 + 프레넬
            half3 result = img.rgb;
            result += half3(metalBoost * (brightness * 0.5 + 0.5));  // 밝기 부스트
            result += half3(spec);                                     // 흰색 하이라이트
            result += half3(fresnel * 0.8, fresnel * 0.85, fresnel);  // 가장자리 (약간 푸른)
            result = clamp(result, 0.0, 1.0);

            return half4(result, img.a);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
