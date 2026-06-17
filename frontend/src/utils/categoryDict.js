import { createCategory, getCategories } from './api.js'

const DEFAULT_CATEGORIES = ['主食', '小吃', '饮品', '甜点']

const normalize = (value) => String(value || '').trim()

const uniquePush = (list, value) => {
  const name = normalize(value)
  if (name && !list.some(item => item.toLowerCase() === name.toLowerCase())) {
    list.push(name)
  }
}

const toNames = (categories) => {
  return (categories || []).reduce((list, item) => {
    uniquePush(list, typeof item === 'string' ? item : item.name)
    return list
  }, [])
}

export const getCategoryDict = async () => {
  try {
    const remoteCategories = await getCategories()
    const result = [...DEFAULT_CATEGORIES]
    toNames(remoteCategories).forEach(item => uniquePush(result, item))
    return result
  } catch (e) {
    return [...DEFAULT_CATEGORIES]
  }
}

export const saveCategoryToDict = async (category) => {
  const name = normalize(category)
  if (!name) return getCategoryDict()

  await createCategory({ name })
  return getCategoryDict()
}

export const mergeCategories = async (categories) => {
  const names = toNames(categories)
  for (const name of names) {
    await createCategory({ name })
  }
  return getCategoryDict()
}
