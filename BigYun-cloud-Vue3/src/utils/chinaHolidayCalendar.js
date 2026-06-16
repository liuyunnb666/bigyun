const CHINA_HOLIDAY_2026 = {
  '2026-01-01': { name: '元旦', type: 'holiday', restDay: true },
  '2026-01-02': { name: '元旦', type: 'holiday', restDay: true },
  '2026-01-03': { name: '元旦', type: 'holiday', restDay: true },
  '2026-01-04': { name: '调休上班', type: 'adjusted_workday', restDay: false },
  '2026-02-14': { name: '调休上班', type: 'adjusted_workday', restDay: false },
  '2026-02-15': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-16': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-17': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-18': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-19': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-20': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-21': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-22': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-23': { name: '春节', type: 'holiday', restDay: true },
  '2026-02-28': { name: '调休上班', type: 'adjusted_workday', restDay: false },
  '2026-04-04': { name: '清明节', type: 'holiday', restDay: true },
  '2026-04-05': { name: '清明节', type: 'holiday', restDay: true },
  '2026-04-06': { name: '清明节', type: 'holiday', restDay: true },
  '2026-05-01': { name: '劳动节', type: 'holiday', restDay: true },
  '2026-05-02': { name: '劳动节', type: 'holiday', restDay: true },
  '2026-05-03': { name: '劳动节', type: 'holiday', restDay: true },
  '2026-05-04': { name: '劳动节', type: 'holiday', restDay: true },
  '2026-05-05': { name: '劳动节', type: 'holiday', restDay: true },
  '2026-05-09': { name: '调休上班', type: 'adjusted_workday', restDay: false },
  '2026-06-19': { name: '端午节', type: 'holiday', restDay: true },
  '2026-06-20': { name: '端午节', type: 'holiday', restDay: true },
  '2026-06-21': { name: '端午节', type: 'holiday', restDay: true },
  '2026-09-20': { name: '调休上班', type: 'adjusted_workday', restDay: false },
  '2026-09-25': { name: '中秋节', type: 'holiday', restDay: true },
  '2026-09-26': { name: '中秋节', type: 'holiday', restDay: true },
  '2026-09-27': { name: '中秋节', type: 'holiday', restDay: true },
  '2026-10-01': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-02': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-03': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-04': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-05': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-06': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-07': { name: '国庆节', type: 'holiday', restDay: true },
  '2026-10-10': { name: '调休上班', type: 'adjusted_workday', restDay: false }
}

/**
 * 获取中国法定节假日、调休或周末信息。
 *
 * @param {string} date 日期文本，格式 YYYY-MM-DD
 * @returns {object} 节假日、调休或普通工作日信息
 */
export function getChinaHolidayInfo(date) {
  if (CHINA_HOLIDAY_2026[date]) return CHINA_HOLIDAY_2026[date]
  const day = new Date(`${date}T00:00:00`).getDay()
  if (day === 0 || day === 6) {
    return { name: '周末', type: 'weekend', restDay: true }
  }
  return { name: '工作日', type: 'workday', restDay: false }
}

/**
 * 判断日期是否为法定节假日。
 *
 * @param {string} date 日期文本，格式 YYYY-MM-DD
 * @returns {boolean} 法定节假日返回 true
 */
export function isOfficialHoliday(date) {
  return getChinaHolidayInfo(date).type === 'holiday'
}
