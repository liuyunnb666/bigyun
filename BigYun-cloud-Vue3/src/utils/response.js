const META_KEYS = new Set([
  'code',
  'status',
  'success',
  'msg',
  'message',
  'error',
  'errorMsg',
  'errMsg',
  'errCode',
  'errorCode',
  'data',
  'result',
  'payload',
  'content'
])

const COLLECTION_KEYS = ['rows', 'list', 'records', 'items', 'content']
const TOTAL_KEYS = ['total', 'totalCount', 'count']
const PAGE_KEYS = new Set([
  ...COLLECTION_KEYS,
  ...TOTAL_KEYS,
  'page',
  'pageNum',
  'pageSize',
  'pages',
  'size',
  'current',
  'hasNext',
  'hasPrevious'
])
const SUCCESS_CODES = new Set([0, 200, '0', '200', '00000', 'SUCCESS', 'success'])

function isPlainObject(value) {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function firstDefined(...values) {
  return values.find(value => value !== undefined && value !== null && value !== '')
}

function normalizeCode(code, fallbackCode = 200) {
  if (code === undefined || code === null || code === '') {
    return fallbackCode
  }
  if (typeof code === 'string') {
    const trimmed = code.trim()
    if (/^-?\d+$/.test(trimmed)) {
      return Number(trimmed)
    }
    return trimmed
  }
  return code
}

function getCollection(data) {
  for (const key of COLLECTION_KEYS) {
    if (Array.isArray(data?.[key])) {
      return data[key]
    }
  }
  return undefined
}

function getTotal(data) {
  return firstDefined(...TOTAL_KEYS.map(key => data?.[key]))
}

function looksLikeResponseEnvelope(data) {
  if (!isPlainObject(data)) {
    return false
  }
  const hasStatus = ['code', 'status', 'success', 'errCode', 'errorCode'].some(key => key in data)
  const hasPayload = ['data', 'result', 'payload', 'content', ...COLLECTION_KEYS].some(key => key in data)
  return hasStatus && hasPayload
}

function isCollectionWrapper(data) {
  if (!isPlainObject(data) || !getCollection(data)) {
    return false
  }
  return Object.keys(data).every(key => PAGE_KEYS.has(key))
}

export function normalizeResponseBody(body, fallbackCode = 200) {
  if (Array.isArray(body)) {
    return {
      code: fallbackCode,
      msg: '操作成功',
      data: body,
      rows: body,
      total: body.length
    }
  }

  if (!isPlainObject(body)) {
    return {
      code: fallbackCode,
      msg: '操作成功',
      data: body
    }
  }

  const normalized = { ...body }
  normalized.code = normalizeCode(
    firstDefined(normalized.code, normalized.status, normalized.errCode, normalized.errorCode),
    fallbackCode
  )
  normalized.msg = firstDefined(
    normalized.msg,
    normalized.message,
    normalized.errorMsg,
    normalized.errMsg,
    typeof normalized.error === 'string' ? normalized.error : undefined
  ) || ''

  if (typeof normalized.success === 'string') {
    normalized.success = normalized.success.toLowerCase() === 'true'
  }

  let payload = firstDefined(normalized.data, normalized.result, normalized.payload, normalized.content)
  if (looksLikeResponseEnvelope(payload)) {
    payload = normalizeResponseBody(payload, fallbackCode)
  }

  if (payload !== undefined) {
    if (Array.isArray(payload)) {
      normalized.data = payload
      normalized.rows ??= payload
      normalized.total ??= payload.length
    } else if (isPlainObject(payload)) {
      const collection = getCollection(payload)
      const total = getTotal(payload)

      if (collection) {
        normalized.rows ??= collection
      }
      if (total !== undefined) {
        normalized.total ??= total
      }

      Object.keys(payload).forEach(key => {
        if (normalized[key] === undefined) {
          normalized[key] = payload[key]
        }
      })

      normalized.data = isCollectionWrapper(payload) ? collection : payload
      if (normalized.total === undefined && Array.isArray(collection) && isCollectionWrapper(payload)) {
        normalized.total = collection.length
      }
    } else {
      normalized.data = payload
    }
  } else {
    const collection = getCollection(normalized)
    const total = getTotal(normalized)
    if (collection) {
      normalized.rows ??= collection
      normalized.data ??= collection
      normalized.total ??= total ?? collection.length
    } else {
      const extraKeys = Object.keys(normalized).filter(key => !META_KEYS.has(key))
      if (extraKeys.length > 0) {
        normalized.data ??= extraKeys.reduce((acc, key) => {
          acc[key] = normalized[key]
          return acc
        }, {})
      } else {
        normalized.data ??= null
      }
    }
  }

  if (normalized.rows === undefined && Array.isArray(normalized.data)) {
    normalized.rows = normalized.data
  }
  if (normalized.total === undefined && Array.isArray(normalized.rows)) {
    normalized.total = normalized.rows.length
  }
  if (!normalized.msg) {
    // 直接判断成功状态，避免递归调用
    const isSuccess = normalized.success === true ||
                     (normalized.success !== false && SUCCESS_CODES.has(normalized.code))
    normalized.msg = isSuccess ? '操作成功' : '操作失败'
  }

  return normalized
}

export function isSuccessResponse(body, fallbackCode = 200) {
  // 如果已经是标准化的对象（有code和msg），直接判断，避免递归
  if (isPlainObject(body) && 'code' in body && 'msg' in body) {
    if (body.success === true) {
      return true
    }
    if (body.success === false) {
      return false
    }
    return SUCCESS_CODES.has(body.code)
  }

  const normalized = normalizeResponseBody(body, fallbackCode)
  if (normalized.success === true) {
    return true
  }
  if (normalized.success === false) {
    return false
  }
  return SUCCESS_CODES.has(normalized.code)
}

export function getResponseData(body, fallbackCode = 200) {
  return normalizeResponseBody(body, fallbackCode).data
}

export function getResponseMessage(body, fallbackCode = 200) {
  return normalizeResponseBody(body, fallbackCode).msg
}
