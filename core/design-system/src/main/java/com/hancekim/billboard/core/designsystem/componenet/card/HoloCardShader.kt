package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    // 레코드판 홈(groove) 질감 셰이더
    // inputShader: 앨범 아트 픽셀 입력
    private const val SHADER_SRC = """
        uniform shader inputShader;
        uniform float2 iResolution;
        uniform float iAngle;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            float2 center = float2(0.5, 0.5);
            float dist = distance(uv, center);

            // 원본 이미지
            half4 img = inputShader.eval(fragCoord);

            // 중앙 라벨 영역 (반지름 30%) — 셰이더 효과 없음
            if (dist < 0.15) return img;

            // 레코드판 홈: 동심원 패턴
            float grooveFreq = 80.0;  // 홈 간격 (너무 촘촘하지 않게)
            float groove = sin(dist * grooveFreq) * 0.5 + 0.5;

            // 홈에 의한 미세 밝기 변화
            float grooveShadow = mix(0.85, 1.0, groove);

            // 빛 반사: 회전 각도에 따라 밝은 부분 이동
            float rad = radians(iAngle);
            float2 lightDir = float2(cos(rad), sin(rad));
            float2 toCenter = normalize(uv - center);
            float lightHit = dot(toCenter, lightDir);
            float reflection = smoothstep(0.3, 0.8, lightHit) * 0.15;

            // 합성
            half3 result = img.rgb * half(grooveShadow) + half3(half(reflection));
            return half4(result, img.a);
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
