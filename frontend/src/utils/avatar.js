import defaultAvatarImg from '../assets/default-avatar.png'
import { resolveAssetUrl } from './asset'

export const getAvatar = (avatarUrl) => {
    if (!avatarUrl || avatarUrl === 'null' || avatarUrl === 'undefined' || avatarUrl.trim() === '') {
        return defaultAvatarImg
    }
    return resolveAssetUrl(avatarUrl)
}
