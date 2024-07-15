package org.jungppo.bambooforest.security.oauth2.response;

import static org.jungppo.bambooforest.member.domain.OAuth2Type.OAUTH2_KAKAO;
import static org.jungppo.bambooforest.security.oauth2.constants.KakaoConstants.KAKAO_ACCOUNT;
import static org.jungppo.bambooforest.security.oauth2.constants.KakaoConstants.KAKAO_ID;
import static org.jungppo.bambooforest.security.oauth2.constants.KakaoConstants.KAKAO_NICKNAME;
import static org.jungppo.bambooforest.security.oauth2.constants.KakaoConstants.KAKAO_PROFILE;
import static org.jungppo.bambooforest.security.oauth2.constants.KakaoConstants.KAKAO_PROFILE_IMAGE_URL;

import java.util.Map;
import org.jungppo.bambooforest.member.domain.OAuth2Type;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final Map<String, Object> profileAttributes;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.profileAttributes = extractProfileAttributes(attribute);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractProfileAttributes(Map<String, Object> attribute) {
        if (attribute.get(KAKAO_ACCOUNT) instanceof Map<?, ?>) {
            Map<String, Object> kakaoAccountAttributes = (Map<String, Object>) attribute.get(KAKAO_ACCOUNT);
            if (kakaoAccountAttributes.get(KAKAO_PROFILE) instanceof Map<?, ?>) {
                return (Map<String, Object>) kakaoAccountAttributes.get(KAKAO_PROFILE);
            }
        }
        throw new IllegalArgumentException("Profile attributes could not be extracted.");
    }

    @Override
    public OAuth2Type getProvider() {
        return OAUTH2_KAKAO;
    }

    @Override
    public String getProviderId() {
        return attribute.get(KAKAO_ID).toString();
    }

    @Override
    public String getName() {
        return profileAttributes.get(KAKAO_NICKNAME).toString();
    }

    @Override
    public String getProfileImage() {
        return profileAttributes.get(KAKAO_PROFILE_IMAGE_URL).toString();
    }
}
