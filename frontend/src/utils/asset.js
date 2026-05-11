const ABSOLUTE_URL_REGEX = /^(https?:)?\/\//i

const getBackendPublicBase = () => {
    const configured = import.meta.env.VITE_BACKEND_PUBLIC_URL
    if (configured && configured.trim()) {
        return configured.replace(/\/+$/, '')
    }

    if (typeof window !== 'undefined' && window.location) {
        const protocol = window.location.protocol || 'http:'
        const hostname = window.location.hostname || 'localhost'
        return `${protocol}//${hostname}:8080`
    }

    return 'http://localhost:8080'
}

export const resolveAssetUrl = (url) => {
    if (!url || typeof url !== 'string') {
        return ''
    }

    const trimmed = url.trim()
    if (!trimmed || trimmed === 'null' || trimmed === 'undefined') {
        return ''
    }

    if (trimmed.startsWith('data:') || ABSOLUTE_URL_REGEX.test(trimmed)) {
        return trimmed
    }

    const normalized = trimmed.startsWith('/') ? trimmed : `/${trimmed}`
    if (normalized.startsWith('/uploads/')) {
        return `${getBackendPublicBase()}${normalized}`
    }
    return normalized
}
