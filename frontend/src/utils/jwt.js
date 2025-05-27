export function extractEmailFromAccessToken(token) {
  try {
    const payloadBase64 = token.split('.')[1]
    const decodedPayload = JSON.parse(atob(payloadBase64))
    return decodedPayload.email || null
  } catch (err) {
    console.error('JWT 파싱 실패:', err)
    return null
  }
}
