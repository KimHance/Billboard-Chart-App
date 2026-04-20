package com.hancekim.billboard.core.designsystem.componenet.card

import android.graphics.RuntimeShader

object HoloCardShader {

    private const val SHADER_SRC = """
        uniform float2 iResolution;
        uniform float iAngle;
        uniform float iInteractive;

        float4 metallicSweep(float2 uv) {
            float angle = atan(uv.y - 0.5, uv.x - 0.5);
            float t = (angle + 3.14159) / (2.0 * 3.14159);

            float3 c1 = float3(0.863, 0.902, 0.941);
            float3 c2 = float3(0.706, 0.784, 0.863);
            float3 c3 = float3(0.941, 0.957, 0.980);
            float3 c4 = float3(0.549, 0.667, 0.784);

            float3 color = mix(c1, c2, smoothstep(0.11, 0.22, t));
            color = mix(color, c3, smoothstep(0.22, 0.36, t));
            color = mix(color, c4, smoothstep(0.36, 0.50, t));
            color = mix(color, c1, smoothstep(0.50, 0.64, t));
            color = mix(color, c2, smoothstep(0.64, 1.0, t));

            float opacity = iInteractive > 0.5 ? 0.55 : 0.3;
            return float4(color, opacity);
        }

        float brushedMetal(float2 uv) {
            float stripe = fract(uv.x * iResolution.x / 3.0);
            float line = step(0.66, stripe);
            return line * 0.06;
        }

        float chromeSpecular(float2 uv) {
            float gradAngle = radians(90.0 + iAngle * 0.5);
            float t = dot(uv - 0.5, float2(cos(gradAngle), sin(gradAngle))) + 0.5;

            float band = smoothstep(0.35, 0.47, t) * (1.0 - smoothstep(0.53, 0.65, t));
            float peak = smoothstep(0.47, 0.50, t) * (1.0 - smoothstep(0.50, 0.53, t));

            float intensity = band * 0.35 + peak * 0.55;
            float opacity = iInteractive > 0.5 ? 1.0 : 0.55;
            return intensity * opacity;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;

            float4 sweep = metallicSweep(uv);
            float metal = brushedMetal(uv);
            float chrome = chromeSpecular(uv);

            float3 color = sweep.rgb * sweep.a;
            color += float3(metal * 0.8);
            color += float3(chrome);

            float alpha = max(sweep.a, max(metal, chrome));

            return half4(half3(color), half(alpha));
        }
    """

    fun create(): RuntimeShader = RuntimeShader(SHADER_SRC)
}
