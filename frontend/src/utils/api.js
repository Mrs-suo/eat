const BASE_URL = '/api'
const FILE_SERVICE_ORIGIN = 'http://localhost:8080'

export const isFileServiceUrl = (url) => {
  return typeof url === 'string' && url.startsWith(`${FILE_SERVICE_ORIGIN}/files/`)
}

export const shouldUploadFile = (url) => {
  if (!url) return false
  if (isFileServiceUrl(url)) return false
  return url.startsWith('blob:') ||
    url.startsWith('http://tmp') ||
    url.startsWith('_doc') ||
    url.startsWith('file:') ||
    !/^https?:\/\//.test(url)
}

export const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/files/upload',
      filePath,
      name: 'file',
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (res.statusCode >= 200 && res.statusCode < 300 && data.url) {
            resolve(data.url)
          } else {
            reject(new Error(data.error || '上传失败'))
          }
        } catch (e) {
          reject(new Error('上传失败'))
        }
      },
      fail: reject
    })
  })
}

export const uploadBlob = (blob, filename = 'dish-image.jpg') => {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', blob, filename)

    const xhr = new XMLHttpRequest()
    xhr.open('POST', BASE_URL + '/files/upload')
    xhr.onload = () => {
      try {
        const data = JSON.parse(xhr.responseText)
        if (xhr.status >= 200 && xhr.status < 300 && data.url) {
          resolve(data.url)
        } else {
          reject(new Error(data.error || '上传失败'))
        }
      } catch (e) {
        reject(new Error('上传失败'))
      }
    }
    xhr.onerror = () => reject(new Error('上传失败'))
    xhr.send(formData)
  })
}

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data)
        } else {
          const message = (res.data && res.data.message) || `请求失败 ${res.statusCode}`
          reject(new Error(message))
        }
      },
      fail: (err) => reject(new Error(err.errMsg || '网络错误'))
    })
  })
}

// ============ 菜品 ============
export const getFamilyDishes = (familyId) => {
  if (!familyId) return Promise.resolve([])
  return request({ url: `/dishes?familyId=${familyId}` })
}
export const getDishById = (id) => request({ url: `/dishes/${id}` })
export const getDishesByMealTime = (mealTime, familyId) => {
  if (!familyId) return Promise.resolve([])
  return request({ url: `/dishes/meal-time/${mealTime}?familyId=${familyId}` })
}
export const getDishesByDate = (familyId, date) => {
  if (!familyId) return Promise.resolve([])
  return request({ url: `/dishes/by-date?familyId=${familyId}&date=${date}` })
}
export const getDishesByCook = (cookUserId) => request({ url: `/dishes/by-cook/${encodeURIComponent(cookUserId)}` })
export const createDish = (dish) => request({ url: '/dishes', method: 'POST', data: dish })
export const deleteDish = (id) => request({ url: `/dishes/${id}`, method: 'DELETE' })

// ============ 分类 ============
export const getCategories = () => request({ url: '/categories' })
export const createCategory = (category) => {
  const data = typeof category === 'string' ? { name: category } : category
  return request({ url: '/categories', method: 'POST', data })
}

// ============ 用户 + 家庭 ============
export const loginByPhone = (phone) => request({
  url: '/users/login',
  method: 'POST',
  data: { phone }
})
export const registerByPhone = ({ phone, nickname, familyCode, familyName }) => request({
  url: '/users/register',
  method: 'POST',
  data: { phone, nickname, familyCode, familyName }
})
export const updateUserProfile = (userId, profile) => request({
  url: `/users/${encodeURIComponent(userId)}/profile`,
  method: 'PUT',
  data: profile
})
export const getCurrentFamily = (userId) => request({ url: `/users/${encodeURIComponent(userId)}/family` })

// ============ 家庭 API ============
export const createFamily = (userId, name) => request({
  url: '/families',
  method: 'POST',
  data: { userId, name }
})
export const previewFamilyByCode = (code) => request({ url: `/families/code/${encodeURIComponent(code)}` })
export const getUserFamilies = (userId) => request({ url: `/families/by-user/${encodeURIComponent(userId)}` })
export const getFamilyMembers = (familyId) => request({ url: `/families/${familyId}/members` })
export const switchCurrentFamily = (familyId, userId) => request({
  url: `/families/${familyId}/switch-current`,
  method: 'POST',
  data: { userId }
})
export const updateFamily = (familyId, userId, name) => request({
  url: `/families/${familyId}`,
  method: 'PUT',
  data: { userId, name }
})
export const inviteFamilyMember = (familyId, userId, phone) => request({
  url: `/families/${familyId}/invitations`,
  method: 'POST',
  data: { userId, phone }
})
export const getFamilyInvitations = (familyId, userId) => request({
  url: `/families/${familyId}/invitations?userId=${encodeURIComponent(userId)}`
})
export const getReceivedFamilyInvitations = (userId) => request({
  url: `/families/invitations/received?userId=${encodeURIComponent(userId)}`
})
export const acceptFamilyInvitation = (invitationId, userId) => request({
  url: `/families/invitations/${invitationId}/accept`,
  method: 'POST',
  data: { userId }
})
export const rejectFamilyInvitation = (invitationId, userId) => request({
  url: `/families/invitations/${invitationId}/reject`,
  method: 'POST',
  data: { userId }
})
export const requestJoinFamily = (userId, target) => request({
  url: '/families/join-requests',
  method: 'POST',
  data: { userId, target }
})
export const approveJoinRequest = (invitationId, userId) => request({
  url: `/families/join-requests/${invitationId}/approve`,
  method: 'POST',
  data: { userId }
})
export const rejectJoinRequest = (invitationId, userId) => request({
  url: `/families/join-requests/${invitationId}/reject`,
  method: 'POST',
  data: { userId }
})
export const removeFamilyMember = (familyId, operatorUserId, targetUserId) => request({
  url: `/families/${familyId}/members/${encodeURIComponent(targetUserId)}?userId=${encodeURIComponent(operatorUserId)}`,
  method: 'DELETE'
})
export const getTodayCook = (familyId, date) => {
  const params = date ? `?date=${date}` : ''
  return request({ url: `/families/${familyId}/today-cook${params}` })
}
export const switchCook = (familyId, userId, date) => request({
  url: `/families/${familyId}/switch-cook`,
  method: 'POST',
  data: { userId, date }
})
export const getCookHistory = (familyId) => request({ url: `/families/${familyId}/cook-history` })

// ============ 修改申请 ============
export const getPendingRequests = (familyId) => request({ url: `/edit-requests/pending?familyId=${familyId || ''}` })
export const getFamilyRequests = (familyId) => request({ url: `/edit-requests/family/${familyId}` })
export const getRequestsByUserId = (userId) => request({ url: `/edit-requests/user/${userId}` })
export const createEditRequest = (requestData) => request({ url: '/edit-requests', method: 'POST', data: requestData })
export const approveRequest = (id) => request({ url: `/edit-requests/${id}/approve`, method: 'PUT' })
export const rejectRequest = (id, reason) => request({
  url: `/edit-requests/${id}/reject?reason=${encodeURIComponent(reason)}`,
  method: 'PUT'
})

// ============ 订单 ============
export const getOrdersByUserId = (userId) => request({ url: `/orders/user/${userId}` })
export const getOrdersByFamily = (familyId) => request({ url: `/orders/family/${familyId}` })
export const createOrder = (order) => request({ url: '/orders', method: 'POST', data: order })
export const updateOrderStatus = (id, status) => request({ url: `/orders/${id}/status?status=${status}`, method: 'PUT' })
